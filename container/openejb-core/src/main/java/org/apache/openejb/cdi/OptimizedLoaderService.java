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
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.classloader.ClassLoaderAwareHandler;
import org.apache.webbeans.service.DefaultLoaderService;
import org.apache.webbeans.spi.LoaderService;
import org.apache.webbeans.spi.plugins.OpenWebBeansPlugin;

import javax.enterprise.inject.spi.Extension;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class OptimizedLoaderService implements LoaderService {

    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB.createChild("cdi"), OptimizedLoaderService.class);

    public static final ThreadLocal<Collection<String>> ADDITIONAL_EXTENSIONS = new ThreadLocal<Collection<String>>();

    private final LoaderService loaderService;

    public OptimizedLoaderService() {
        this(new DefaultLoaderService());
    }

    public OptimizedLoaderService(final LoaderService loaderService) {
        this.loaderService = loaderService;
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

        final Collection<Extension> extensionCopy = new ArrayList<>(list);

        final Iterator<Extension> it = list.iterator();
        while (it.hasNext()) {
            if (it.hasNext()) {
                if (isFiltered(extensionCopy, it.next())) {
                    it.remove();
                }
            }
        }
        return list;
    }

    // mainly intended to avoid conflicts between internal and overrided spec extensions
    private boolean isFiltered(final Collection<Extension> extensions, final Extension next) {
        final ClassLoader containerLoader = ParentClassLoaderFinder.Helper.get();
        final Class<? extends Extension> extClass = next.getClass();
        if (extClass.getClassLoader() != containerLoader) {
            return false;
        }

        final String name = extClass.getName();
        switch (name) {
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
                return "true".equals(SystemInstance.get().getProperty("tomee.batchee.cdi.use-extension", "false"));
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
        {
            final Class<?> clazz;
            try {
                clazz = loader.loadClass("org.apache.webbeans.jsf.plugin.OpenWebBeansJsfPlugin");
                try {
                    list.add(OpenWebBeansPlugin.class.cast(
                            Proxy.newProxyInstance(loader, new Class<?>[]{OpenWebBeansPlugin.class}, new ClassLoaderAwareHandler(clazz.getSimpleName(), clazz.newInstance(), loader))));
                } catch (final Exception e) {
                    log.error("Unable to load OpenWebBeansPlugin: OpenWebBeansJsfPlugin");
                }
            } catch (final ClassNotFoundException e) {
                // ignore
            }
        }
        return list;
    }
}
