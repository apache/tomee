/**
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
import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.classloader.ClassLoaderConfigurer;
import org.apache.openejb.classloader.WebAppEnricher;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

public class LazyStopWebappClassLoader extends WebappClassLoader {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, LazyStopWebappClassLoader.class.getName());

    public static final String TOMEE_WEBAPP_FIRST = "tomee.webapp-first";

    private boolean restarting = false;
    private volatile Context relatedContext;
    private boolean forceStopPhase = Boolean.parseBoolean(SystemInstance.get().getProperty("tomee.webappclassloader.force-stop-phase", "false"));
    private ClassLoaderConfigurer configurer = null;

    public LazyStopWebappClassLoader() {
        construct();
    }

    public LazyStopWebappClassLoader(final ClassLoader parent) {
        super(parent);
        construct();
    }

    private void construct() {
        setDelegate(isDelegate());
        configurer = ClassLoaderUtil.configurer(LazyStopWebappLoader.getCurrentAppId());
    }

    @Override
    public void stop() throws LifecycleException {
        // in our destroyapplication method we need a valid classloader to TomcatWebAppBuilder.afterStop()
        if (forceStopPhase && (restarting || TomcatContextUtil.isReloading(relatedContext))) {
            internalStop();
        }
    }

    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        if ("org.apache.openejb.hibernate.OpenEJBJtaPlatform".equals(name)
                || "org.apache.openejb.jpa.integration.hibernate.PrefixNamingStrategy".equals(name)
                || "org.apache.openejb.jpa.integration.eclipselink.PrefixSessionCustomizer".equals(name)
                || "org.apache.openejb.eclipselink.JTATransactionController".equals(name)
                || "org.apache.tomee.mojarra.TomEEInjectionProvider".equals(name)) {
            // don't load them from system classloader (breaks all in embedded mode and no sense in other cases)
            synchronized (system) {
                final ClassLoader old = system;
                system = NoClassClassLoader.INSTANCE;
                try {
                    return super.loadClass(name);
                } finally {
                    system = old;
                }
            }
        }

        // avoid to redefine classes from server in this classloader is it not already loaded
        if (URLClassLoaderFirst.shouldSkip(name)) {
            try {
                return OpenEJB.class.getClassLoader().loadClass(name);
            } catch (ClassNotFoundException e) {
                return super.loadClass(name);
            } catch (NoClassDefFoundError ncdfe) {
                return super.loadClass(name);
            }
        }
        return super.loadClass(name);
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

    // embeddeding implementation of sthg (JPA, JSF) can lead to classloading issues if we don't enrich the webapp
    // with our integration jars
    // typically the class will try to be loaded by the common classloader
    // but the interface implemented or the parent class
    // will be in the webapp
    @Override
    public void start() throws LifecycleException {
        super.start(); // do it first otherwise we can't use this as classloader

        // add configurer enrichments
        if (configurer != null) {
            // add now we removed all we wanted
            final URL[] enrichment = configurer.additionalURLs();
            for (URL url : enrichment) {
                super.addURL(url);
            }
        }

        // add internal enrichments
        for (URL url : SystemInstance.get().getComponent(WebAppEnricher.class).enrichment(this)) {
            super.addURL(url);
        }
    }

    public void addURL(final URL url) {
        if (configurer == null || configurer.accept(url)) {
            super.addURL(url);
        }
    }

    @Override
    protected boolean validateJarFile(File file) throws IOException {
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
        } catch (MalformedURLException e) {
            // no-op
        }
        return true;
    }

    public void setRelatedContext(final Context standardContext) {
        relatedContext = standardContext;
    }

    public static boolean isDelegate() {
        return !SystemInstance.get().getOptions().get(TOMEE_WEBAPP_FIRST, true);
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        return URLClassLoaderFirst.filterResources(name, super.getResources(name));
    }

    @Override
    public String toString() {
        return "LazyStop" + super.toString();
    }

    private static class NoClassClassLoader extends ClassLoader {
        private static final NoClassClassLoader INSTANCE = new NoClassClassLoader();

        @Override
        public Class<?> loadClass(final String name) throws ClassNotFoundException {
            throw new ClassNotFoundException();
        }
    }
}
