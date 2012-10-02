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
import org.apache.myfaces.shared.util.ClassUtils;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.loader.Files;
import org.apache.openejb.util.URLs;
import org.apache.xbean.finder.UrlSet;

import javax.faces.context.ExternalContext;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class TomEEFacesConfigResourceProvider extends DefaultFacesConfigResourceProvider {
    private static final String META_INF_PREFIX = "META-INF/";
    private static final String FACES_CONFIG_SUFFIX = ".faces-config.xml";
    private static final String FACES_CONFIG_IMPLICIT = "META-INF/faces-config.xml";
    private static final Map<ClassLoader, Collection<URL>> CACHED_RESOURCES = new HashMap<ClassLoader, Collection<URL>>();

    @Override
    public Collection<URL> getMetaInfConfigurationResources(final ExternalContext context) throws IOException {
        final ClassLoader loader = getClassLoader();

        Collection<URL> urlSet = CACHED_RESOURCES.get(loader);
        if (urlSet != null) {
            return new HashSet<URL>(urlSet); // copy it since it can be modified then
        }

        urlSet  = new HashSet<URL>();

        final Enumeration<URL> resources = loader.getResources(FACES_CONFIG_IMPLICIT);
        while (resources.hasMoreElements()) {
            urlSet.add(resources.nextElement());
        }

        // Scan files inside META-INF ending with .faces-config.xml
        for (URL url : NewLoaderLogic.applyBuiltinExcludes(new UrlSet(loader)).getUrls()) {
            final File file = URLs.toFile(url);
            if (!file.exists()) {
                continue;
            }

            if (!file.isDirectory()) { // browse all entries to see if we have a matching file
                final Enumeration<JarEntry> e = new JarFile(file).entries();
                while (e.hasMoreElements()) {
                    try {
                        final String name = e.nextElement().getName();
                        if (name.startsWith(META_INF_PREFIX) && name.endsWith(FACES_CONFIG_SUFFIX)) {
                            final Enumeration<URL> e2 = loader.getResources(name);
                            while (e2.hasMoreElements()) {
                                urlSet.add(e2.nextElement());
                            }
                        }
                    } catch (Throwable ignored) {
                        // no-op
                    }
                }
            } else {
                final File metaInf = new File(file, META_INF_PREFIX);
                if (metaInf.exists() && metaInf.isDirectory()) {
                    for (File f : Files.collect(metaInf, FacesConfigSuffixFilter.INSTANCE)) {
                        if (!f.isDirectory()) {
                            urlSet.add(f.toURI().toURL());
                        }
                    }
                }
            }
        }

        CACHED_RESOURCES.put(loader, urlSet);
        return new HashSet<URL>(urlSet);
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
