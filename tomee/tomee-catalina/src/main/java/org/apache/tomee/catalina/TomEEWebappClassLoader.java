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
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.classloader.ClassLoaderConfigurer;
import org.apache.openejb.classloader.CompositeClassLoaderConfigurer;
import org.apache.openejb.classloader.WebAppEnricher;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.config.QuickJarsTxtParser;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.SystemInstance;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TomEEWebappClassLoader extends WebappClassLoader {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, TomEEWebappClassLoader.class.getName());
    private static final ThreadLocal<ClassLoaderConfigurer> INIT_CONFIGURER = new ThreadLocal<ClassLoaderConfigurer>();
    private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<Context>();

    public static final String TOMEE_WEBAPP_FIRST = "tomee.webapp-first";

    private boolean restarting;
    private boolean forceStopPhase = Boolean.parseBoolean(SystemInstance.get().getProperty("tomee.webappclassloader.force-stop-phase", "false"));
    private ClassLoaderConfigurer configurer;
    private final int hashCode;
    private Collection<File> additionalRepos;
    private volatile boolean stopped = false;

    public TomEEWebappClassLoader() {
        hashCode = construct();
        setJavaseClassLoader(getSystemClassLoader());
    }

    public TomEEWebappClassLoader(final ClassLoader parent) {
        super(parent);
        hashCode = construct();
        setJavaseClassLoader(getSystemClassLoader());
    }

    private int construct() {
        setDelegate(isDelegate());
        configurer = INIT_CONFIGURER.get();
        return super.hashCode();
    }

    @Override
    public void stop() throws LifecycleException {
        // in our destroyapplication method we need a valid classloader to TomcatWebAppBuilder.afterStop()
        if (forceStopPhase && restarting) {
            internalStop();
        }
    }

    public Collection<File> getAdditionalRepos() {
        initAdditionalRepos();
        return additionalRepos;
    }

    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        if ("org.apache.openejb.hibernate.OpenEJBJtaPlatform".equals(name)
                || "org.apache.openejb.jpa.integration.hibernate.PrefixNamingStrategy".equals(name)
                || "org.apache.openejb.jpa.integration.eclipselink.PrefixSessionCustomizer".equals(name)
                || "org.apache.openejb.eclipselink.JTATransactionController".equals(name)
                || "org.apache.tomee.mojarra.TomEEInjectionProvider".equals(name)) {
            // don't load them from system classloader (breaks all in embedded mode and no sense in other cases)
            synchronized (this) {
                final ClassLoader old = getJavaseClassLoader();
                setJavaseClassLoader(NoClassClassLoader.INSTANCE);
                final boolean delegate = getDelegate();
                setDelegate(false);
                try {
                    return super.loadClass(name);
                } finally {
                    setJavaseClassLoader(old);
                    setDelegate(delegate);
                }
            }
        }

        // avoid to redefine classes from server in this classloader is it not already loaded
        if (URLClassLoaderFirst.shouldDelegateToTheContainer(this, name)) { // dynamic validation handling overriding
            try {
                return OpenEJB.class.getClassLoader().loadClass(name);
            } catch (final ClassNotFoundException e) {
                return super.loadClass(name);
            } catch (final NoClassDefFoundError ncdfe) {
                return super.loadClass(name);
            }
        } else if (name.startsWith("javax.faces.") || name.startsWith("org.apache.webbeans.jsf.")) {
            final boolean delegate = getDelegate();
            synchronized (this) {
                setDelegate(false);
                try {
                    return super.loadClass(name);
                } finally {
                    setDelegate(delegate);
                }
            }
        }
        synchronized (this) { // TODO: rework it to avoid it but not a big issue, see first if of this method
            return super.loadClass(name);
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
    protected boolean filter(final String name) {
        return !"org.apache.tomee.mojarra.TomEEInjectionProvider".equals(name) && URLClassLoaderFirst.shouldSkip(name);
    }

    @Override
    protected void checkStateForClassLoading(final String className) throws ClassNotFoundException {
        if (stopped) { // keep same error than parent
            super.checkStateForClassLoading(className);
        }
    }

    public void internalStop() throws LifecycleException {
        if (getState().isAvailable()) {
            // reset classloader because of tomcat classloaderlogmanager
            // to be sure we reset the right loggers
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(this);
            try {
                super.stop();
            } finally {
                Thread.currentThread().setContextClassLoader(loader);
            }
            stopped = true;
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
        if (additionalRepos != null) {
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

    public static boolean isDelegate() {
        return !SystemInstance.get().getOptions().get(TOMEE_WEBAPP_FIRST, true);
    }

    @Override
    public InputStream getResourceAsStream(final String name) {
        if (!getState().isAvailable()) {
            return null;
        }
        return super.getResourceAsStream(name);
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        if (!getState().isAvailable()) {
            return null;
        }

        if ("META-INF/services/javax.servlet.ServletContainerInitializer".equals(name)) {
            final Collection<URL> list = new ArrayList<URL>(Collections.list(super.getResources(name)));
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
    public String toString() {
        return "TomEE" + super.toString();
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
            return path.startsWith(WEB_INF_CLASSES)? path.substring(WEB_INF_CLASSES.length()) : path;
        }
    }
}
