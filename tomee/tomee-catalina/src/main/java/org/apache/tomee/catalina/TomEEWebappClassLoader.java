/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.catalina;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.loader.ParallelWebappClassLoader;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.juli.ClassLoaderLogManager;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.classloader.ClassLoaderConfigurer;
import org.apache.openejb.classloader.CompositeClassLoaderConfigurer;
import org.apache.openejb.classloader.WebAppEnricher;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.config.QuickJarsTxtParser;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.AppFinder;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.URLs;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;
import org.apache.openejb.util.reflection.Reflections;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

// TODO: rework it
// constraint: be able to use EM in web components (contextDestroyed() listener for instance) and opposite (link TWAB/Assembler)
// issue: StandardRoot is not lazy stopped
// proposals:
// - change the Assembler TWAB.undeployWebapps call to be correct.
// - lazy stop StandardRoot
// - integrate more finely with StandardContext to be able to ensure we are called when expected
public class TomEEWebappClassLoader extends ParallelWebappClassLoader {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, TomEEWebappClassLoader.class.getName());
    private static final ThreadLocal<ClassLoaderConfigurer> INIT_CONFIGURER = new ThreadLocal<>();
    private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

    public static final String TOMEE_WEBAPP_FIRST = "tomee.webapp-first";
    public static final String TOMEE_EAR_DEFAULT = "tomee.ear.webapp-first";

    static {
        boolean result = ClassLoader.registerAsParallelCapable();
        if (!result) {
            LOGGER.warning("Can't register // tomee webapp classloader");
        }
    }

    public static final String CLASS_EXTENSION = ".class";
    protected String[] forceSkip;

    private boolean restarting;
    private boolean forceStopPhase = Boolean.parseBoolean(SystemInstance.get().getProperty("tomee.webappclassloader.force-stop-phase", "false"));
    private ClassLoaderConfigurer configurer;
    private final boolean isEar;
    private final ClassLoader containerClassLoader;
    private volatile boolean originalDelegate;
    private final int hashCode;
    private Collection<File> additionalRepos;
    private volatile boolean stopped = false;
    private final Map<String, Boolean> filterTempCache = new HashMap<>(); // used only in sync block + isEar
    private volatile LazyStopStandardRoot webResourceRoot;

    public TomEEWebappClassLoader() {
        hashCode = construct();
        setJavaseClassLoader(getSystemClassLoader());
        containerClassLoader = ParentClassLoaderFinder.Helper.get();
        isEar = getInternalParent() != null && !getInternalParent().equals(containerClassLoader) && defaultEarBehavior();
        originalDelegate = getDelegate();
    }

    public TomEEWebappClassLoader(final ClassLoader parent) {
        super(parent);
        hashCode = construct();
        setJavaseClassLoader(getSystemClassLoader());
        containerClassLoader = ParentClassLoaderFinder.Helper.get();
        isEar = getInternalParent() != null && !getInternalParent().equals(containerClassLoader) && defaultEarBehavior();
        originalDelegate = getDelegate();
    }

    public ClassLoader getInternalParent() {
        return getParent();
    }

    private int construct() {
        setDelegate(isDelegate());
        configurer = INIT_CONFIGURER.get();
        return super.hashCode();
    }

    @Override
    public void setDelegate(final boolean delegate) {
        this.delegate = delegate;
        this.originalDelegate = delegate;
    }

    @Override
    public void stop() throws LifecycleException {
        // in our destroyapplication method we need a valid classloader to TomcatWebAppBuilder.afterStop()
        if (forceStopPhase || restarting) {
            internalStop();
        }
    }

    public Collection<File> getAdditionalRepos() {
        initAdditionalRepos();
        return additionalRepos;
    }

    @Override
    public Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        if ("org.apache.openejb.hibernate.OpenEJBJtaPlatform".equals(name)
                || "org.apache.openejb.jpa.integration.hibernate.PrefixNamingStrategy".equals(name)
                || "org.apache.openejb.jpa.integration.eclipselink.PrefixSessionCustomizer".equals(name)
                || "org.apache.openejb.jpa.integration.eclipselink.OpenEJBServerPlatform".equals(name)
                || "org.apache.openejb.jpa.integration.eclipselink.OpenEJBServerPlatform$OpenEJBJTATransactionController".equals(name)
                || "org.apache.openejb.eclipselink.JTATransactionController".equals(name)
                || "org.apache.tomee.mojarra.TomEEInjectionProvider".equals(name)) {
            // don't load them from system classloader (breaks all in embedded mode and no sense in other cases)
            synchronized (this) {
                final ClassLoader old = getJavaseClassLoader();
                setJavaseClassLoader(NoClassClassLoader.INSTANCE);
                delegate = false;
                try {
                    return super.loadClass(name, resolve);
                } finally {
                    setJavaseClassLoader(old);
                    setDelegate(originalDelegate);
                }
            }
        }

        // avoid to redefine classes from server in this classloader is it not already loaded
        if (URLClassLoaderFirst.shouldDelegateToTheContainer(this, name) || shouldForceLoadFromTheContainer(name)) { // dynamic validation handling overriding
            try {
                return OpenEJB.class.getClassLoader().loadClass(name); // we could use containerClassLoader but this is server loader so cut it even more
            } catch (final ClassNotFoundException e) {
                synchronized (this) {
                    return super.loadClass(name, resolve);
                }
            } catch (final NoClassDefFoundError ncdfe) {
                synchronized (this) {
                    return super.loadClass(name, resolve);
                }
            }
        } else if (name.startsWith("jakarta.faces.") || name.startsWith("org.apache.webbeans.jsf")) {
            synchronized (this) {
                delegate = false;
                try {
                    return super.loadClass(name, resolve);
                } finally {
                    setDelegate(originalDelegate);
                }
            }
        }
        synchronized (this) { // TODO: rework it to avoid it and get aligned on Java 7 classloaders (but not a big issue)
            if (isEar) {
                final boolean filter = filter(name, true);
                filterTempCache.put(name, filter); // will be called again by super.loadClass() so cache it
                if (!filter) {
                    if (URLClassLoaderFirst.class.isInstance(getInternalParent())) { // true
                        final URLClassLoaderFirst urlClassLoaderFirst = URLClassLoaderFirst.class.cast(getInternalParent());
                        Class<?> c = urlClassLoaderFirst.findAlreadyLoadedClass(name);
                        if (c != null) {
                            return c;
                        }
                        c = urlClassLoaderFirst.loadInternal(name, resolve);
                        if (c != null) {
                            return c;
                        }
                    }
                    return loadWithDelegate(getResource(name.replace('.', '/') + CLASS_EXTENSION) == null, resolve, name);
                }
            }
            return super.loadClass(name, resolve);
        }
    }

    private boolean shouldForceLoadFromTheContainer(final String name) {
        if (forceSkip == null) {
            return false;
        }

        for (final String p : forceSkip) {
            if (name.startsWith(p)) {
                return true;
            }
        }

        return false;
    }

    private Class<?> loadWithDelegate(final boolean delegate, final boolean resolve, final String name) throws ClassNotFoundException {
        setDelegate(delegate);
        try {
            return super.loadClass(name, resolve);
        } finally {
            filterTempCache.remove(name); // no more needed since class is loaded, avoid to waste mem
            setDelegate(originalDelegate);
        }
    }

    @Override
    public void setResources(final WebResourceRoot resources) {
        this.resources = resources;
        if (StandardRoot.class.isInstance(resources)) {
            final List<WebResourceSet> jars = (List<WebResourceSet>) Reflections.get(resources, "jarResources");
            if (jars != null && !jars.isEmpty()) {
                final Iterator<WebResourceSet> jarIt = jars.iterator();
                while (jarIt.hasNext()) {
                    final WebResourceSet set = jarIt.next();
                    if (set.getBaseUrl() == null) {
                        continue;
                    }
                    final File file = URLs.toFile(set.getBaseUrl());
                    try {
                        if (file.exists() && (!TomEEClassLoaderEnricher.validateJarFile(file) || !jarIsAccepted(file))) {
                            // need to remove this resource
                            LOGGER.warning("Removing " + file.getAbsolutePath() + " since it is offending");
                            jarIt.remove();
                        }
                    } catch (final IOException e) {
                        // ignore
                    }
                }
            }
        }
    }

    @Override
    protected boolean filter(final String inName, final boolean isClassName) {
        final String name = inName == null || isClassName ? inName : inName.replace('/', '.').replace(".class", "");
        if ("org.apache.tomee.mojarra.TomEEInjectionProvider".equals(name)) {
            return false;
        }
        if (isEar) { // check we are called from super and we already cached the result in loadClass
            synchronized (this) {
                final Boolean cache = filterTempCache.get(name);
                if (cache != null) {
                    return cache;
                }
            }
        }
        return URLClassLoaderFirst.shouldSkip(name);
    }

    public void internalDestroy() {
        try {
            if (!stopped) {
                try {
                    internalStop();
                } catch (final LifecycleException e) {
                    // no-op
                }
            }
            super.destroy();
        } finally {
            cleanUpClassLoader();
        }
    }

    public void internalStop() throws LifecycleException {
        if (stopped) {
            return;
        }
        // reset classloader because of tomcat classloaderlogmanager
        // to be sure we reset the right loggers
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = thread.getContextClassLoader();
        thread.setContextClassLoader(this);
        try {
            super.stop();
            // super.destroy();
            if (webResourceRoot != null) {
                webResourceRoot.internalStop();
                webResourceRoot = null;
            }
            stopped = true;
        } finally {
            thread.setContextClassLoader(loader);
            if (!forceStopPhase) {
                cleanUpClassLoader();
            }
        }
    }

    public void restarting() {
        restarting = true;
    }

    public void restarted() {
        restarting = false;
    }

    public boolean isRestarting() {
        return restarting;
    }

    public boolean isForceStopPhase() {
        return forceStopPhase;
    }

    public boolean isStopped() {
        return stopped;
    }

    public synchronized void initAdditionalRepos() {
        if (additionalRepos != null) {
            return;
        }
        if (CONTEXT.get() != null) {
            additionalRepos = new LinkedList<>();
            final String contextPath = CONTEXT.get().getServletContext().getContextPath();
            final String name = contextPath.isEmpty() ? "ROOT" : contextPath.substring(1);
            final String externalRepositories = SystemInstance.get().getProperty("tomee." + name + ".externalRepositories");
            if (externalRepositories != null) {
                for (final String additional : externalRepositories.split(",")) {
                    final String trim = additional.trim();
                    if (!trim.isEmpty()) {
                        final File file = new File(trim);
                        additionalRepos.add(file);
                    }
                }
            }
        }
    }

    // embeddeding implementation of sthg (JPA, JSF) can lead to classloading issues if we don't enrich the webapp
    // with our integration jars
    // typically the class will try to be loaded by the common classloader
    // but the interface implemented or the parent class
    // will be in the webapp
    @Override
    public void start() throws LifecycleException {
        super.start(); // do it first otherwise we can't use this as classloader

        // mainly for tomee-maven-plugin
        initAdditionalRepos();
        if (additionalRepos != null && !additionalRepos.isEmpty()) {
            for (final File f : additionalRepos) {
                final DirResourceSet webResourceSet = new PremptiveDirResourceSet(resources, "/", f.getAbsolutePath(), "/");
                resources.addPreResources(webResourceSet);
            }
            resources.setCachingAllowed(false);
        }

        // add configurer enrichments
        if (configurer != null) {
            // add now we removed all we wanted
            final URL[] enrichment = configurer.additionalURLs();
            for (final URL url : enrichment) {
                super.addURL(url);
            }
        }

        // add internal enrichments
        for (final URL url : SystemInstance.get().getComponent(WebAppEnricher.class).enrichment(this)) {
            super.addURL(url);
        }

        // WEB-INF/jars.xml
        final File war = Contexts.warPath(CONTEXT.get());
        final File jarsXml = new File(war, "WEB-INF/" + QuickJarsTxtParser.FILE_NAME);
        final ClassLoaderConfigurer configurerTxt = QuickJarsTxtParser.parse(jarsXml);
        if (configurerTxt != null) {
            configurer = new CompositeClassLoaderConfigurer(configurer, configurerTxt);
        }

        stopped = false;
    }

    public void addURL(final URL url) {
        if (configurer == null || configurer.accept(url)) {
            super.addURL(url);
        }
    }

    private boolean jarIsAccepted(final File file) {
        if (configurer == null) {
            return true;
        }

        try {
            if (!configurer.accept(file.toURI().toURL())) {
                LOGGER.warning("jar '" + file.getAbsolutePath() + "' is excluded: " + file.getName() + ". It will be ignored.");
                return false;
            }
        } catch (final MalformedURLException e) {
            // no-op
        }
        return true;
    }

    protected boolean defaultEarBehavior() {
        return !SystemInstance.get().getOptions().get(TOMEE_EAR_DEFAULT, false /*bck compat*/);
    }

    private static boolean isDelegate() {
        return !SystemInstance.get().getOptions().get(TOMEE_WEBAPP_FIRST, true);
    }

    @Override
    public InputStream getResourceAsStream(final String name) {
        if (!getState().isAvailable()) {
            final ClassLoader loader = ParentClassLoaderFinder.Helper.get();
            return loader == null ? null : loader.getResourceAsStream(name);
        }
        try {
            return super.getResourceAsStream(name);
        } catch (final NullPointerException npe) {
            // workaround cause of a bug in tomcat 8.5.0, keeping it even if we upgraded until we don't support 8.5.0 anymore
            final URL url = super.getResource(name);
            if (url != null) {
                try {
                    return url.openStream();
                } catch (final IOException e) {
                    // no-op
                }
            }
            return null;
        }
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        if (!getState().isAvailable()) {
            final ClassLoader loader = ParentClassLoaderFinder.Helper.get();
            return loader == null ? Collections.<URL>emptyEnumeration() : loader.getResources(name);
        }

        if ("META-INF/services/jakarta.servlet.ServletContainerInitializer".equals(name)) {
            final Collection<URL> list = new ArrayList<>(Collections.list(super.getResources(name)));
            final Iterator<URL> it = list.iterator();
            while (it.hasNext()) {
                final URL next = it.next();
                final File file = Files.toFile(next);
                if (!file.isFile() && NewLoaderLogic.skip(next)) {
                    it.remove();
                }
            }
            return Collections.enumeration(list);
        }
        if ("META-INF/services/jakarta.websocket.ContainerProvider".equals(name)) {
            final Collection<URL> list = new ArrayList<>(Collections.list(super.getResources(name)));
            final Iterator<URL> it = list.iterator();
            while (it.hasNext()) {
                final URL next = it.next();
                final File file = Files.toFile(next);
                if (!file.isFile() && NewLoaderLogic.skip(next)) {
                    it.remove();
                }
            }
            return Collections.enumeration(list);
        }
        if ("META-INF/faces-config.xml".equals(name)) { // mojarra workaround
            try {
                if (AppFinder.findAppContextOrWeb(
                        Thread.currentThread().getContextClassLoader(), AppFinder.WebBeansContextTransformer.INSTANCE) == null
                        && Boolean.parseBoolean(SystemInstance.get().getProperty("tomee.jsf.ignore-owb", "true"))) {
                    final Collection<URL> list = new HashSet<>(Collections.list(super.getResources(name)));
                    final Iterator<URL> it = list.iterator();
                    while (it.hasNext()) {
                        final String fileName = Files.toFile(it.next()).getName();
                        if (fileName.startsWith("openwebbeans-"/*jsf|el22*/) && fileName.endsWith(".jar")) {
                            it.remove();
                        }
                    }
                    return Collections.enumeration(list);
                }
            } catch (final Throwable th) {
                // no-op
            }
        }
        return URLClassLoaderFirst.filterResources(name, super.getResources(name));
    }

    @Override
    public boolean equals(final Object other) {
        return other != null && ClassLoader.class.isInstance(other) && hashCode() == other.hashCode();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public TomEEWebappClassLoader copyWithoutTransformers() {
        final TomEEWebappClassLoader result = new TomEEWebappClassLoader(getInternalParent());
        result.additionalRepos = additionalRepos;
        result.configurer = configurer;
        super.copyStateWithoutTransformers(result);
        try {
            result.start();
        } catch (LifecycleException e) {
            throw new IllegalStateException(e);
        }

        return result;
    }

    @Override
    public void destroy() {
        if (forceStopPhase) {
            internalDestroy();
        }
    }

    private void cleanUpClassLoader() {
        final LogManager lm = LogManager.getLogManager();
        if (ClassLoaderLogManager.class.isInstance(lm)) { // weak ref but ensure it is really removed otherwise in some cases we leak
            Map.class.cast(Reflections.get(lm, "classLoaderLoggers")).remove(this);
        }
    }

    public static void initContext(final ClassLoaderConfigurer configurer) {
        INIT_CONFIGURER.set(configurer);
    }

    public static void initContext(final Context ctx) {
        CONTEXT.set(ctx);
    }

    public static void cleanContext() {
        INIT_CONFIGURER.remove();
        CONTEXT.remove();
    }

    public void setWebResourceRoot(LazyStopStandardRoot webResourceRoot) {
        this.webResourceRoot = webResourceRoot;
    }

    void setForceSkip(final String[] forceSkip) {
        this.forceSkip = forceSkip;
    }

    private static class NoClassClassLoader extends ClassLoader {
        private static final NoClassClassLoader INSTANCE = new NoClassClassLoader();

        @Override
        public Class<?> loadClass(final String name) throws ClassNotFoundException {
            throw new ClassNotFoundException();
        }
    }

    private static final class PremptiveDirResourceSet extends DirResourceSet {
        private static final String WEB_INF_CLASSES = "/WEB-INF/classes";

        public PremptiveDirResourceSet(final WebResourceRoot resources, final String s, final String absolutePath, final String s1) {
            super(resources, s, absolutePath, s1);
        }

        @Override
        public WebResource getResource(final String path) {
            return super.getResource(computePath(path));
        }

        @Override
        public String[] list(final String path) {
            return super.list(computePath(path));
        }

        private static String computePath(final String path) {
            if (WEB_INF_CLASSES.equals(path)) {
                return "/";
            }
            return path.startsWith(WEB_INF_CLASSES) ? path.substring(WEB_INF_CLASSES.length()) : path;
        }
    }
}
