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

package org.apache.openejb.cdi;

import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.activemq.jms2.cdi.JMS2CDIExtension;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.service.DefaultLoaderService;
import org.apache.webbeans.spi.LoaderService;
import org.apache.webbeans.spi.plugins.OpenWebBeansPlugin;

import jakarta.enterprise.inject.spi.Extension;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class OptimizedLoaderService implements LoaderService {

    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB.createChild("cdi"), OptimizedLoaderService.class);

    public static final ThreadLocal<Collection<String>> ADDITIONAL_EXTENSIONS = new ThreadLocal<Collection<String>>();

    private final LoaderService loaderService;
    private final Properties config;

    public OptimizedLoaderService(final Properties appConfig) {
        this(
                is("openejb.cdi.ignore-not-loadable-extensions", appConfig, SystemInstance.get().getProperties(), "true") ?
                        new FilterableServiceLoader() : new DefaultLoaderService(),
                appConfig);
    }

    public OptimizedLoaderService(final LoaderService loaderService, final Properties appConfig) {
        this.loaderService = loaderService;
        this.config = appConfig;
    }

    @Override
    public <T> List<T> load(final Class<T> serviceType) {
        return load(serviceType, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public <T> List<T> load(final Class<T> serviceType, final ClassLoader classLoader) {
        // ServiceLoader is expensive (can take up to a half second).  This is an optimization
        if (OpenWebBeansPlugin.class.equals(serviceType)) {
            return (List<T>) loadWebBeansPlugins(classLoader);
        }

        // As far as we know, this only is reached for CDI Extension discovery
        if (Extension.class.equals(serviceType)) {
            return (List<T>) loadExtensions(classLoader);
        }
        return loaderService.load(serviceType, classLoader);
    }

    protected List<? extends Extension> loadExtensions(final ClassLoader classLoader) {
        final List<Extension> list = loaderService.load(Extension.class, classLoader);
        final Collection<String> additional = ADDITIONAL_EXTENSIONS.get();
        if (additional != null) {
            for (final String name : additional) {
                try {
                    list.add(Extension.class.cast(classLoader.loadClass(name).newInstance()));
                } catch (final Exception ignored) {
                    // no-op
                }
            }
        }

        if (hasJms()) {
            list.add(new JMS2CDIExtension());
        }

        final Collection<Extension> extensionCopy = new ArrayList<>(list);

        final Iterator<Extension> it = list.iterator();
        while (it.hasNext()) {
            if (it.hasNext()) {
                if (isFiltered(extensionCopy, it.next())) {
                    it.remove();
                }
            }
        }

        if ("true".equals(OptimizedLoaderService.this.config.getProperty("openejb.cdi.extensions.sorted",
                SystemInstance.get().getProperty("openejb.cdi.extensions.sorted")))) {
            list.sort(new Comparator<Extension>() {
                @Override
                public int compare(final Extension o1, final Extension o2) {
                    final int val1 = getVal(o1);
                    final int val2 = getVal(o1);
                    if (val1 == val2) {
                        return o1.getClass().getName().compareTo(o2.getClass().getName());
                    }
                    return val1 - val2;
                }

                private int getVal(final Extension o1) {
                    final String key = "openejb.cdi.extensions." + o1.getClass().getName() + ".ordinal";
                    final String config = OptimizedLoaderService.this.config.getProperty(key, SystemInstance.get().getProperty(key));
                    return config == null ? 0 : Integer.parseInt(config.trim());
                }
            });
        }

        return list;
    }

    private boolean hasJms() {
        try {
            Thread.currentThread().getContextClassLoader().loadClass("org.apache.activemq.ra.ActiveMQManagedConnectionFactory");
            return true;
        } catch (final NoClassDefFoundError | ClassNotFoundException e) {
            return false;
        }
    }

    // mainly intended to avoid conflicts between internal and overrided spec extensions
    private boolean isFiltered(final Collection<Extension> extensions, final Extension next) {
        final ClassLoader containerLoader = ParentClassLoaderFinder.Helper.get();
        final Class<? extends Extension> extClass = next.getClass();
        final String name = extClass.getName();

        final String activeKey = name + ".active";
        final SystemInstance systemInstance = SystemInstance.get();
        if (!is(activeKey, config, systemInstance.getProperties(), "true")) {
            return true;
        }

        switch (name) {
            case "org.apache.geronimo.microprofile.openapi.cdi.GeronimoOpenAPIExtension":
                return true;
            case "org.apache.bval.cdi.BValExtension":
                for (final Extension e : extensions) {
                    final String en = e.getClass().getName();

                    // org.hibernate.validator.internal.cdi.ValidationExtension but allowing few evolutions of packages
                    if (en.startsWith("org.hibernate.validator.") && en.endsWith("ValidationExtension")) {
                        log.info("Skipping BVal CDI integration cause hibernate was found in the application");
                        return true;
                    }
                }
                break;
            case "org.apache.batchee.container.cdi.BatchCDIInjectionExtension": // see org.apache.openejb.batchee.BatchEEServiceManager
                return "true".equals(systemInstance.getProperty("tomee.batchee.cdi.use-extension", "false"));
            case "org.apache.commons.jcs.jcache.cdi.MakeJCacheCDIInterceptorFriendly":
                final String spi = "META-INF/services/javax.cache.spi.CachingProvider";
                try {
                    final Enumeration<URL> appResources = Thread.currentThread().getContextClassLoader().getResources(spi);
                    if (appResources != null && appResources.hasMoreElements()) {
                        final Collection<URL> containerResources = Collections.list(containerLoader.getResources(spi));
                        do {
                            if (!containerResources.contains(appResources.nextElement())) {
                                log.info("Skipping JCS CDI integration cause another provide was found in the application");
                                return true;
                            }
                        } while (appResources.hasMoreElements());
                    }
                } catch (final Exception e) {
                    // no-op
                }
                break;
            default:
        }
        return false;
    }

    private List<? extends OpenWebBeansPlugin> loadWebBeansPlugins(final ClassLoader loader) {
        final List<OpenWebBeansPlugin> list = new ArrayList<>(2);
        list.add(new CdiPlugin());
        {
            final Class<?> clazz;
            try {
                clazz = loader.loadClass("org.apache.geronimo.openejb.cdi.GeronimoWebBeansPlugin");
                try {
                    list.add(OpenWebBeansPlugin.class.cast(clazz.newInstance()));
                } catch (final Exception e) {
                    log.error("Unable to load OpenWebBeansPlugin: GeronimoWebBeansPlugin");
                }
            } catch (final ClassNotFoundException e) {
                // ignore
            }
        }
        return list;
    }

    private static boolean is(final String activeKey, final Properties config, final Properties fallback, final String or) {
        return Boolean.parseBoolean(config.getProperty(activeKey, fallback.getProperty(activeKey, or)));
    }

    private static class FilterableServiceLoader implements LoaderService {
        private static final String SERVICE_CONFIG = "META-INF/services/" + Extension.class.getName();
        private static final String FILE_ENCODING = "UTF-8";

        private List<Class<?>> foundServiceClasses = new ArrayList<>();
        private ClassLoader loader;

        private List<Extension> loadServiceImplementations() {
            final List<Class<?>> result = resolveServiceImplementations();
            if (result == null) {
                return Collections.emptyList();
            }
            final List<Extension> foundServices = new ArrayList<>();
            for (final Class<?> serviceClass : result) {
                try {
                    foundServices.add(Extension.class.cast(serviceClass.newInstance()));
                } catch (final NoClassDefFoundError | InstantiationException | IllegalAccessException e) {
                    log.error("Ignoring a CDI Extension, cause it can't be instantiated (" + serviceClass + "): " + e.getMessage());
                }
            }
            return foundServices;
        }

        private List<Class<?>> resolveServiceImplementations() {
            for (final URL configFile : getConfigFileList()) {
                loadConfiguredServices(configFile);
            }
            return foundServiceClasses;
        }

        private List<URL> getConfigFileList() {
            final List<URL> serviceFiles = new ArrayList<>();
            try {
                final Enumeration<URL> serviceFileEnumerator = loader.getResources(SERVICE_CONFIG);
                while (serviceFileEnumerator.hasMoreElements()) {
                    serviceFiles.add(serviceFileEnumerator.nextElement());
                }
            } catch (final Exception e) {
                throw new IllegalStateException("Failed to load Extension configured in " + SERVICE_CONFIG, e);
            }
            return serviceFiles;
        }

        private void loadConfiguredServices(final URL serviceFile) {
            try (final InputStream inputStream = serviceFile.openStream()) {
                String serviceClassName;
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, FILE_ENCODING));
                while ((serviceClassName = bufferedReader.readLine()) != null) {
                    serviceClassName = extractConfiguredServiceClassName(serviceClassName);
                    if (!"".equals(serviceClassName)) {
                        loadService(serviceClassName);
                    }
                }
            } catch (final Exception e) {
                throw new IllegalStateException("Failed to process service-config: " + serviceFile, e);
            }
        }

        private String extractConfiguredServiceClassName(String currentConfigLine) {
            int startOfComment = currentConfigLine.indexOf('#');
            if (startOfComment > -1) {
                currentConfigLine = currentConfigLine.substring(0, startOfComment);
            }
            return currentConfigLine.trim();
        }

        private void loadService(final String serviceClassName) {
            final Class<Extension> serviceClass;
            try {
                serviceClass = (Class<Extension>) loader.loadClass(serviceClassName);
                if (!foundServiceClasses.contains(serviceClass)) {
                    foundServiceClasses.add(serviceClass);
                }
            } catch (final NoClassDefFoundError |ClassNotFoundException e) {
                log.error("Ignoring " + serviceClassName + " cause it can't be loaded.");
            }
        }

        @Override
        public <T> List<T> load(final Class<T> serviceType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> List<T> load(final Class<T> serviceType, final ClassLoader classLoader) {
            loader = classLoader;
            try {
                return (List<T>) loadServiceImplementations();
            } finally {
                loader = null;
                foundServiceClasses.clear();
            }
        }
    }
}
