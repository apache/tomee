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
package org.apache.openejb.config;

import org.apache.openejb.core.EmptyResourcesClassLoader;
import org.apache.openejb.loader.SystemInstance;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.apache.xbean.finder.archive.ClasspathArchive;
import org.apache.xbean.finder.archive.CompositeArchive;
import org.apache.xbean.finder.archive.FilteredArchive;
import org.apache.xbean.finder.filter.Filter;
import org.apache.xbean.finder.filter.FilterList;
import org.apache.xbean.finder.filter.PackageFilter;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigurableClasspathArchive extends CompositeArchive implements ScanConstants {
    public ConfigurableClasspathArchive(final Module module, final URL... urls) {
        this(module, Arrays.asList(urls));
    }

    public ConfigurableClasspathArchive(final Module module, final Iterable<URL> urls) {
        this(module, false, urls);
    }

    public ConfigurableClasspathArchive(final ClassLoader loader, final Iterable<URL> urls) {
        this(loader, false, urls);
    }

    public ConfigurableClasspathArchive(final ClassLoader loader, boolean forceDescriptor, final Iterable<URL> urls) {
        this(new FakeModule(loader), forceDescriptor, urls);
    }

    public ConfigurableClasspathArchive(final ClassLoader loader, final URL url) {
        this(new FakeModule(loader), Arrays.asList(url));
    }

    public ConfigurableClasspathArchive(final Module module, boolean forceDescriptor, final Iterable<URL> urls) {
        super(archive(module, urls, forceDescriptor));
    }

    public static List<Archive> archive(final Module module, final Iterable<URL> urls, boolean forceDescriptor) {
        final List<Archive> archives = new ArrayList<Archive>();
        for (URL location : urls) {
            try {
                archives.add(archive(module, location, forceDescriptor));
            } catch (Exception e) {
                // ignored
            }
        }
        return archives;
    }

    public static Archive archive(final Module module, final URL location, boolean forceDescriptor) {
        final ClassLoader loader = module.getClassLoader();
        final String name = "META-INF/" + name();
        try {
            URL scanXml = new URLClassLoader(new URL[] { location }, new EmptyResourcesClassLoader()).getResource(name);
            if (scanXml == null && !forceDescriptor) {
                return ClasspathArchive.archive(loader, location);
            } else if (scanXml == null) {
                return new ClassesArchive();
            }

            // read descriptors
            ScanUtil.ScanHandler scan;
            if (scanXml != null) {
                scan = ScanUtil.read(scanXml);
            } else {
                scan = new ScanUtil.ScanHandler();
            }

            final Archive packageArchive = packageArchive(scan.getPackages(), loader, location);
            final Archive classesArchive = classesArchive(scan.getPackages(), scan.getClasses(), loader);

            if (packageArchive != null && classesArchive != null) {
                return new CompositeArchive(classesArchive, packageArchive);
            } else if (packageArchive != null) {
                return  packageArchive;
            }
            return classesArchive;
        } catch (IOException e) {
            if (forceDescriptor) {
                return new ClassesArchive();
            }
            return ClasspathArchive.archive(loader, location);
        }
    }

    private static String name() {
        return SystemInstance.get().getProperty(SCAN_XML_PROPERTY, SCAN_XML_NAME);
    }

    public static Archive packageArchive(final Set<String> packageNames, final ClassLoader loader, final URL url) {
        if (!packageNames.isEmpty()) {
            return new FilteredArchive(ClasspathArchive.archive(loader, url), filters(packageNames));
        }
        return null;
    }

    private static Filter filters(final Set<String> packageNames) {
        final List<Filter> filters = new ArrayList<Filter>();
        for (String packageName : packageNames) {
            filters.add(new PackageFilter(packageName));
        }
        return new FilterList(filters);
    }

    public static Archive classesArchive(final Set<String> packages, final Set<String> classnames, final ClassLoader loader) {
        Class<?>[] classes = new Class<?>[classnames.size()];
        int i = 0;
        for (String clazz : classnames) {
            // skip classes managed by package filtering
            if (packages != null && clazzInPackage(packages, clazz)) {
                continue;
            }

            try {
                classes[i++] = loader.loadClass(clazz);
            } catch (ClassNotFoundException e) {
                // ignored
            }
        }

        if (i != classes.length) { // shouldn't occur
            final Class<?>[] updatedClasses = new Class<?>[i];
            System.arraycopy(classes, 0, updatedClasses, 0, i);
            classes = updatedClasses;
        }

        return new ClassesArchive(classes);
    }

    private static boolean clazzInPackage(final Collection<String> packagename, final String clazz) {
        for (String str : packagename) {
            if (clazz.startsWith(str)) {
                return true;
            }
        }
        return false;
    }

    public static class FakeModule extends Module {
        public FakeModule(final ClassLoader loader) {
            this(loader, null);
        }

        public FakeModule(final ClassLoader loader, final Map<String, Object> altDD)  {
            this(loader, altDD, name());
        }

        public FakeModule(final ClassLoader loader, final Map<String, Object> altDD, final String name) {
            super(false);
            setClassLoader(loader);

            final URL scanXml;
            if (altDD == null) {
                scanXml = loader.getResource("META-INF/" + name);
            } else {
                scanXml = (URL) altDD.get(name);
            }
            if (scanXml != null) {
                getAltDDs().put(SCAN_XML_NAME, scanXml);
            }
        }
    }
}
