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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.myfaces;

import org.apache.myfaces.config.DefaultFacesConfigResourceProvider;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.loader.Files;
import org.apache.openejb.util.AppFinder;
import org.apache.openejb.util.URLs;
import org.apache.xbean.finder.UrlSet;

import jakarta.faces.context.ExternalContext;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class TomEEFacesConfigResourceProvider extends DefaultFacesConfigResourceProvider {
    private static final String META_INF_PREFIX = "META-INF/";
    private static final String FACES_CONFIG_SUFFIX = ".faces-config.xml";
    private static final String FACES_CONFIG_IMPLICIT = "META-INF/faces-config.xml";
    private static final Map<ClassLoader, Collection<URL>> CACHED_RESOURCES = new HashMap<>();

    @Override
    public Collection<URL> getMetaInfConfigurationResources(final ExternalContext notUsedNullIsPassedFromInitializer) throws IOException {
        final ClassLoader loader = getClassLoader();

        Collection<URL> urlSet = CACHED_RESOURCES.get(loader);
        if (urlSet != null) {
            return new HashSet<>(urlSet); // copy it since it can be modified then
        }

        urlSet  = new HashSet<>();

        final Enumeration<URL> resources = loader.getResources(FACES_CONFIG_IMPLICIT);
        while (resources.hasMoreElements()) {
            urlSet.add(resources.nextElement());
        }

        final List<URL> urls = NewLoaderLogic.applyBuiltinExcludes(new UrlSet(loader)).getUrls();

        final ExecutorService es = Executors.newFixedThreadPool(2 * Runtime.getRuntime().availableProcessors() + 1);
        final Collection<Future<Set<URL>>> futures = new ArrayList<>(urls.size());

        // Scan files inside META-INF ending with .faces-config.xml
        for (final URL url : urls) {
            final File file = URLs.toFile(url);
            if (!file.exists()) {
                continue;
            }

            futures.add(es.submit(new Callable<Set<URL>>() {
                @Override
                public Set<URL> call() throws Exception {
                    final Set<URL> currentSet = new HashSet<>();

                    if (!file.isDirectory()) { // browse all entries to see if we have a matching file
                        final Enumeration<JarEntry> e = new JarFile(file).entries();
                        while (e.hasMoreElements()) {
                            try {
                                final String name = e.nextElement().getName();
                                if (name.startsWith(META_INF_PREFIX) && name.endsWith(FACES_CONFIG_SUFFIX)) {
                                    final Enumeration<URL> e2 = loader.getResources(name);
                                    while (e2.hasMoreElements()) {
                                        currentSet.add(e2.nextElement());
                                    }
                                }
                            } catch (final Throwable ignored) {
                                // no-op
                            }
                        }
                    } else {
                        final File metaInf = new File(file, META_INF_PREFIX);
                        if (metaInf.exists() && metaInf.isDirectory()) {
                            for (final File f : Files.collect(metaInf, FacesConfigSuffixFilter.INSTANCE)) {
                                if (!f.isDirectory()) {
                                    currentSet.add(f.toURI().toURL());
                                }
                            }
                        }
                    }

                    return currentSet;
                }
            }));
        }

        es.shutdown();

        for (final Future<Set<URL>> set : futures) {
            try {
                urlSet.addAll(set.get());
            } catch (final Exception e) {
                // no-op
            }
        }

        try {
            if (AppFinder.findAppContextOrWeb(
                    Thread.currentThread().getContextClassLoader(), AppFinder.WebBeansContextTransformer.INSTANCE) == null) {
                final Iterator<URL> toFilter = urlSet.iterator();
                while (toFilter.hasNext()) {
                    final URL url = toFilter.next();
                    if (TomEEMyFacesContainerInitializer.isOwb(url)) {
                        toFilter.remove();
                    }
                }
            }
        } catch (final Throwable th) {
            // no-op
        }

        CACHED_RESOURCES.put(loader, urlSet);
        return new HashSet<>(urlSet);
    }

    private ClassLoader getClassLoader() {
        ClassLoader loader = ClassUtils.getContextClassLoader();
        if (loader == null) {
            loader = this.getClass().getClassLoader();
        }
        return loader;
    }

    public static void clear(final ClassLoader loader) {
        CACHED_RESOURCES.remove(loader);
    }

    private static class FacesConfigSuffixFilter implements FileFilter {
        public static final FacesConfigSuffixFilter INSTANCE = new FacesConfigSuffixFilter();

        @Override
        public boolean accept(final File pathname) {
            return pathname.isDirectory() || pathname.getName().endsWith(FACES_CONFIG_SUFFIX);
        }
    }
}
