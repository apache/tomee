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
package org.apache.openejb.service;

import org.apache.openejb.cdi.CompositeBeans;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.FinderFactory;
import org.apache.openejb.config.ReadDescriptors;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.config.event.BeforeAppInfoBuilderEvent;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.observer.Observes;
import org.apache.xbean.finder.archive.FileArchive;
import org.apache.xbean.finder.archive.JarArchive;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Designed to easily add a container jar in scanning.
 *
 * Definition can look like:
 *
 * 1. scanner = new://Service?class-name=org.apache.openejb.service.ScanJarService
 * 2. scanner.path = cdi-lib/foo.jar
 * 
 * if url is a directory containing multiple libs just use scanner.directory = true 
 */
public class ScanJarService {
    private File path;
    private boolean directory;

    private volatile CompositeBeans beans;

    public void addScanningResult(@Observes final BeforeAppInfoBuilderEvent event) throws Exception {
        for (final EjbModule ejbModule : event.getAppModule().getEjbModules()) { // ear
            if (ejbModule.getModuleId().startsWith("ear-scoped-cdi-beans")) {
                doMerge(ejbModule);
                return;
            }
        }
        // else a war
        for (final WebModule webModule : event.getAppModule().getWebModules()) {
            for (final EjbModule ejbModule : event.getAppModule().getEjbModules()) {
                if (ejbModule.getModuleId().equals(webModule.getModuleId())) {
                    doMerge(ejbModule);
                    return;
                }
            }
        }
    }

    private void doMerge(final EjbModule ejbModule) throws Exception {
        final Beans beans = ejbModule.getBeans();
        if (CompositeBeans.class.isInstance(beans)) {
            final CompositeBeans cb = CompositeBeans.class.cast(beans);
            ensureInit();
            merge(cb);
        } else if (beans != null) {
            ensureInit();
            for (final URL key : this.beans.getManagedClasses().keySet()) {
                beans.getManagedClasses().putAll(this.beans.getManagedClasses());
                addIfNotNull(beans.getInterceptors(), this.beans.getInterceptorsByUrl().get(key));
                addIfNotNull(beans.getAlternativeClasses(), this.beans.getAlternativesByUrl().get(key));
                addIfNotNull(beans.getAlternativeStereotypes(), this.beans.getAlternativeStereotypesByUrl().get(key));
                addIfNotNull(beans.getDecorators(), this.beans.getDecoratorsByUrl().get(key));
            }
        }
    }

    private void addIfNotNull(final List<String> out, final Collection<String> in) {
        if (in != null) {
            out.addAll(in);
        }
    }

    private void merge(final CompositeBeans cb) {
        cb.getManagedClasses().putAll(this.beans.getManagedClasses());
        cb.getNotManagedClasses().putAll(this.beans.getNotManagedClasses());
        cb.getAlternativesByUrl().putAll(this.beans.getAlternativesByUrl());
        cb.getAlternativeStereotypesByUrl().putAll(this.beans.getAlternativeStereotypesByUrl());
        cb.getInterceptorsByUrl().putAll(this.beans.getInterceptorsByUrl());
        cb.getDecoratorsByUrl().putAll(this.beans.getDecoratorsByUrl());
    }

    private void ensureInit() throws Exception {
        if (beans != null) {
            return;
        }

        synchronized (this) {
            if (beans != null) {
                return;
            }

            final ClassLoader loader = ParentClassLoaderFinder.Helper.get();
            final CompositeBeans mergedModel = new CompositeBeans();
            for (final File file : findFiles()) {
                final URL url = file.toURI().toURL();
                if (file.isDirectory()) {
                    final FinderFactory.OpenEJBAnnotationFinder finder = new FinderFactory.OpenEJBAnnotationFinder(new FileArchive(loader, file));
                    mergedModel.getManagedClasses().put(url, finder.getAnnotatedClassNames());

                    final File beansXml = new File(file, "META-INF/beans.xml");
                    if (beansXml.exists()) {
                        try (final FileInputStream inputStream = new FileInputStream(beansXml)) {
                            final Beans beansModel = ReadDescriptors.readBeans(inputStream);
                            mergedModel.mergeClasses(url, beansModel);
                        }
                    }
                } else {
                    final FinderFactory.OpenEJBAnnotationFinder finder = new FinderFactory.OpenEJBAnnotationFinder(new JarArchive(loader, url));
                    mergedModel.getManagedClasses().put(url, finder.getAnnotatedClassNames());

                    try (final URLClassLoader cl = new URLClassLoader(new URL[]{ url })) {
                        try (final InputStream is = cl.getResourceAsStream("META-INF/beans.xml")) {
                            if (is != null) {
                                final Beans beansModel = ReadDescriptors.readBeans(is);
                                mergedModel.mergeClasses(url, beansModel);
                            }
                        }
                    }
                }
            }

            beans = mergedModel;
        }
    }

    private Iterable<? extends File> findFiles() {
        final Collection<File> files = new LinkedList<>();
        if (!directory) {
            files.add(path);
        } else {
            final File[] children = path.listFiles(new FileFilter() {
                @Override
                public boolean accept(final File pathname) {
                    final String name = pathname.getName();
                    return name.endsWith(".jar") || name.endsWith(".zip") || pathname.isDirectory();
                }
            });
            if (children != null) {
                Collections.addAll(files, children);
            }
        }
        return files;
    }

    public void setPath(final String path) throws Exception {
        this.path = new File(path).getCanonicalFile();
        
    }

    public void setDirectory(final boolean directory) {
        this.directory = directory;
    }
}
