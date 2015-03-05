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
import org.apache.openejb.config.event.BeforeAppInfoBuilderEvent;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.util.URLs;
import org.apache.xbean.finder.archive.FileArchive;
import org.apache.xbean.finder.archive.JarArchive;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Designed to easily add a container jar in scanning.
 *
 * Definition can look like:
 *
 * 1. scanner = new://Service?class-name=org.apache.openejb.service.ScanJarService
 * 2. scanner.url = cdi-lib/foo.jar
 * 
 */
public class ScanJarService {
    private URL url;
    private final List<String> beans = new ArrayList<>();
    private Beans beansModel;

    public void addScanningResult(@Observes final BeforeAppInfoBuilderEvent event) {
        for (final EjbModule ejbModule : event.getAppModule().getEjbModules()) {
            if (ejbModule.getModuleId().startsWith("ear-scoped-cdi-beans")) {
                final Beans beans = ejbModule.getBeans();
                if (CompositeBeans.class.isInstance(beans)) {
                    final CompositeBeans cb = CompositeBeans.class.cast(beans);
                    cb.getManagedClasses().put(url, new ArrayList<>(this.beans));

                    if (beansModel != null) {
                        if (beansModel.getAlternativeClasses() != null) {
                            cb.getAlternativesByUrl().put(url, beansModel.getAlternativeClasses());
                        }
                        if (beansModel.getAlternativeStereotypes() != null) {
                            cb.getAlternativeStereotypesByUrl().put(url, beansModel.getAlternativeStereotypes());
                        }
                        if (beansModel.getInterceptors() != null) {
                            cb.getInterceptorsByUrl().put(url, beansModel.getInterceptors());
                        }
                        if (beansModel.getDecorators() != null) {
                            cb.getDecoratorsByUrl().put(url, beansModel.getDecorators());
                        }
                    }
                }
                return;
            }
        }
    }

    public void setUrl(final String url) throws Exception {
        final File f = new File(url);
        if (f.exists()) {
            this.url = f.toURI().toURL();
        } else {
            this.url = new URL(url);
        }

        final File file = URLs.toFile(this.url);
        final ClassLoader loader = ParentClassLoaderFinder.Helper.get();
        beans.addAll(
                new FinderFactory.OpenEJBAnnotationFinder(
                        file.isDirectory() ? new FileArchive(loader, file) : new JarArchive(loader, this.url))
                        .getAnnotatedClassNames());

        if (file.isDirectory()) {
            final File beansXml = new File(file, "META-INF/beans.xml");
            if (beansXml.exists()) {
                final FileInputStream inputStream = new FileInputStream(beansXml);
                beansModel = ReadDescriptors.readBeans(inputStream);
                inputStream.close();
            }
        } else {
            final URLClassLoader cl = new URLClassLoader(new URL[] { this.url });
            final InputStream is = cl.getResourceAsStream("META-INF/beans.xml");
            if (is != null) {
                beansModel = ReadDescriptors.readBeans(is);
                is.close();
            }
            cl.close();
        }
    }
}
