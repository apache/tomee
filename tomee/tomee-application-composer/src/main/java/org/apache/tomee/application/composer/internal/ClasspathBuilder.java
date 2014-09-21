/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tomee.application.composer.internal;

import org.apache.openejb.config.DeploymentsResolver;
import org.apache.openejb.loader.JarLocation;
import org.apache.openejb.loader.provisining.ProvisioningResolver;
import org.apache.openejb.util.URLs;
import org.apache.tomee.application.composer.component.AutoDiscovery;
import org.apache.tomee.application.composer.component.Libraries;
import org.apache.tomee.application.composer.component.Library;
import org.apache.xbean.finder.MetaAnnotatedClass;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

public final class ClasspathBuilder {
    public static List<URL> buildClasspath(final MetaAnnotatedClass<?> meta, final ClassLoader loader) {
        final List<URL> jarList = handleAutoDiscovery(meta, loader);
        handleLibraries(meta, jarList, loader);
        return jarList;
    }

    private static void handleLibraries(final MetaAnnotatedClass<?> meta, final List<URL> jarList, final ClassLoader loader) {
        // @Library/@Libraries
        final ProvisioningResolver resolver = new ProvisioningResolver();

        final Libraries libs = meta.getAnnotation(Libraries.class);
        if (libs != null) {
            for (final Library lib : libs.value()) {
                resolve(jarList, resolver, lib, loader);
            }
        }
        final Library lib = meta.getAnnotation(Library.class);
        if (lib != null) {
            resolve(jarList, resolver, lib, loader);
        }
    }

    private static List<URL> handleAutoDiscovery(final MetaAnnotatedClass<?> meta, final ClassLoader loader) {
        final AutoDiscovery autoDiscovery = meta.getAnnotation(AutoDiscovery.class);
        if (autoDiscovery != null) {
            final File appJar = JarLocation.jarLocation(meta.get());
            if (autoDiscovery.classpath()) {
                final List<URL> jarList = DeploymentsResolver.loadFromClasspath(loader);
                if (!autoDiscovery.application()) {
                    final Iterator<URL> urls = jarList.iterator();
                    while (urls.hasNext()) {
                        if (URLs.toFile(urls.next()).equals(appJar)) {
                            urls.remove();
                            break;
                        }
                    }
                }
                return jarList;
            } else if (autoDiscovery.application()) {
                try {
                    return new ArrayList<>(asList(appJar.toURI().toURL()));
                } catch (final MalformedURLException e) {
                    throw new IllegalArgumentException(e);
                }
            } else {
                logger().warning("You set @AutoDiscovery on " + meta.getName() + " but you asked to scan nothing");
                return new LinkedList<>();
            }
        }
        return new LinkedList<>();
    }

    private static void resolve(final List<URL> jarList, final ProvisioningResolver resolver, final Library lib, final ClassLoader loader) {
        for (final String location : resolver.realLocation(lib.value())) {
            try {
                final URL url = new File(location).toURI().toURL();
                if (lib.isApplicative()) {
                    jarList.add(url);
                } else {
                    if (URLClassLoader.class.isInstance(loader)) {
                        try {
                            final Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                            if (!m.isAccessible()) {
                                m.setAccessible(true);
                            }
                            m.invoke(loader, url);
                        } catch (final Exception e) {
                            throw new IllegalStateException(e);
                        }
                    } else {
                        logger().severe("Can't add (yet) " + lib.value() + " since the classloader is not an URLClassloader");
                    }
                }
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    // don't initialize logger system before we boot if there is no issue
    private static Logger logger() {
        return Logger.getLogger(ClasspathBuilder.class.getName());
    }

    private ClasspathBuilder() {
        // no-op
    }
}
