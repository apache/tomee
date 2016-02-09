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
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.classloader.ClassLoaderConfigurer;
import org.apache.openejb.classloader.WebAppEnricher;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;

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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class LazyStopWebappClassLoader extends WebappClassLoader {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, LazyStopWebappClassLoader.class.getName());
    private static final ThreadLocal<ClassLoaderConfigurer> INIT_CONFIGURER = new ThreadLocal<ClassLoaderConfigurer>();
    private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<Context>();

    public static final String TOMEE_WEBAPP_FIRST = "tomee.webapp-first";
    public static final String CLASS_EXTENSION = ".class";
    
    private boolean restarting;
    private final boolean forceStopPhase = Boolean.parseBoolean(SystemInstance.get().getProperty("tomee.webappclassloader.force-stop-phase", "false"));
    private ClassLoaderConfigurer configurer;
    private final boolean isEar;
    private final ClassLoader containerClassLoader;
    private volatile boolean originalDelegate;
    private final int hashCode;
    private Collection<File> additionalRepos;
    private final Map<String, Boolean> filterTempCache = new HashMap<String, Boolean>(); // used only in sync block + isEar

    public LazyStopWebappClassLoader() {
        j2seClassLoader = getSystemClassLoader();
        hashCode = construct();
        containerClassLoader = ParentClassLoaderFinder.Helper.get();
        isEar = getParent() != containerClassLoader;
        originalDelegate = getDelegate();
    }

    public LazyStopWebappClassLoader(final ClassLoader parent) {
        super(parent);
        j2seClassLoader = getSystemClassLoader();
        hashCode = construct();
        setJavaseClassLoader(getSystemClassLoader());
        containerClassLoader = ParentClassLoaderFinder.Helper.get();
        isEar = getParent() != containerClassLoader;
        originalDelegate = getDelegate();
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
        if (forceStopPhase && restarting) {
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
            || "org.apache.openejb.eclipselink.JTATransactionController".equals(name)
            || "org.apache.tomee.mojarra.TomEEInjectionProvider".equals(name)) {
            // don't load them from system classloader (breaks all in embedded mode and no sense in other cases)
            synchronized (this) {
                final ClassLoader old = getJavaseClassLoader();
                j2seClassLoader = NoClassClassLoader.INSTANCE;
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
        if (URLClassLoaderFirst.shouldDelegateToTheContainer(this, name)) { // dynamic validation handling overriding
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
        } else if (name.startsWith("javax.faces.") || name.startsWith("org.apache.webbeans.jsf.")) {
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
                final boolean filter = filter(name);
                filterTempCache.put(name, filter); // will be called again by super.loadClass() so cache it
                if (!filter) {
                    if (URLClassLoaderFirst.class.isInstance(getParent())) { // true
                        final URLClassLoaderFirst urlClassLoaderFirst = URLClassLoaderFirst.class.cast(getParent());
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
    protected boolean filter(final String name) {
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

    public void internalStop() throws LifecycleException {
        if (isStarted()) {
            // reset classloader because of tomcat classloaderlogmanager
            // to be sure we reset the right loggers
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(this);
            try {
                super.stop();
            } finally {
                Thread.currentThread().setContextClassLoader(loader);
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

    public synchronized void initAdditionalRepos() {
        if (additionalRepos != null) {
            return;
        }
        if (CONTEXT.get() != null) {
            additionalRepos = new LinkedList<File>();
            final String root = CONTEXT.get().getServletContext().getRealPath("/");
            if (root != null) {
                final String externalRepositories = SystemInstance.get().getProperty("tomee." + new File(root).getName() + ".externalRepositories");
                if (externalRepositories != null) {
                    setSearchExternalFirst(true);
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
    }

    // embeddeding implementation of sthg (JPA, JSF) can lead to classloading issues if we don't enrich the webapp
    // with our integration jars
    // typically the class will try to be loaded by the common classloader
    // but the interface implemented or the parent class
    // will be in the webapp
    @Override
    public void start() throws LifecycleException {
        if (this.repositories == null) {
            this.repositories = new String[0];
        }
        super.start(); // do it first otherwise we can't use this as classloader

        // mainly for tomee-maven-plugin
        initAdditionalRepos();
        if (additionalRepos != null) {
            for (final File f : additionalRepos) {
                try { // not addURL to look here first
                    super.addRepository(f.toURI().toURL().toExternalForm());
                } catch (final MalformedURLException e) {
                    LOGGER.error(e.getMessage());
                }
            }
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
    }

    public void addURL(final URL url) {
        if (configurer == null || configurer.accept(url)) {
            super.addURL(url);
        }
    }

    @Override
    protected boolean validateJarFile(final File file) throws IOException {
        return super.validateJarFile(file) && TomEEClassLoaderEnricher.validateJarFile(file) && jarIsAccepted(file);
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
        if (!isStarted()) {
            return null;
        }
        return super.getResourceAsStream(name);
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        if (!isStarted()) {
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
        return "LazyStop" + super.toString();
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
}
