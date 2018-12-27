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

package org.apache.openejb.config;

import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.apache.xbean.finder.archive.CompositeArchive;
import org.apache.xbean.finder.archive.FilteredArchive;
import org.apache.xbean.finder.filter.Filter;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.apache.openejb.loader.JarLocation.jarLocation;

public class WebappAggregatedArchive implements Archive, ScanConstants {
    private final Map<URL, List<String>> map = new HashMap<>();
    private ScanUtil.ScanHandler handler;
    private boolean scanXmlExists; // faster than using an empty handler
    private Archive archive;

    public WebappAggregatedArchive(final Module module, final Iterable<URL> urls, final Filter filter) {
        final URL scanXml = (URL) module.getAltDDs().get(ScanConstants.SCAN_XML_NAME);
        if (scanXml != null) {
            try {
                handler = ScanUtil.read(scanXml);
                scanXmlExists = true;
                if ((handler.getPackages() == null || handler.getPackages().isEmpty())
                        && handler.getClasses() != null && !handler.getClasses().isEmpty()
                        && handler.isOptimized()) { // only classes, skip scanning
                    final Collection<Class<?>> loaded = new ArrayList<>(handler.getClasses().size());
                    for (final String clazz : handler.getClasses()) {
                        try {
                            final Class<?> aClass = module.getClassLoader().loadClass(clazz);
                            loaded.add(aClass);
                            final URL jar = jarLocation(aClass).toURI().toURL();
                            List<String> list = map.computeIfAbsent(jar, k -> new ArrayList<>());
                            list.add(clazz);
                        } catch (final ClassNotFoundException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                    archive = new ClassesArchive(loaded.toArray(new Class[loaded.size()]));
                    return;
                }
            } catch (final IOException e) {
                // ignored, will not use filtering with scan.xml
            }
        }

        archive = new CompositeArchive(doScan(module.getClassLoader(), urls, filter));
    }

    private List<Archive> doScan(final ClassLoader loader, final Iterable<URL> urls, final Filter filter) {
        final List<Archive> archives = new ArrayList<>();
        for (final URL url : urls) {
            final List<String> classes = new ArrayList<>();
            final Archive archive = new FilteredArchive(
                    new ConfigurableClasspathArchive(loader, singletonList(url)),
                    new ScanXmlSaverFilter(scanXmlExists, handler, classes, filter));
            map.put(url, classes);
            archives.add(archive);
        }
        return archives;
    }

    // for internal usage mainly like faked modules
    public WebappAggregatedArchive(final Archive delegate, final Iterable<URL> urls) {
        final List<Archive> archives = doScan(Thread.currentThread().getContextClassLoader(), urls, null);
        final List<String> classes = new ArrayList<>();
        final Archive archive = new FilteredArchive(delegate, new ScanXmlSaverFilter(scanXmlExists, handler, classes, null));
        try {
            this.map.put(new URL("jar:file://!/META-INF/beans.xml"), classes);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        archives.add(archive);
        this.archive = new CompositeArchive(archives);
    }

    public WebappAggregatedArchive(final Module module, final Iterable<URL> urls) {
        this(module, urls, null);
    }

    public WebappAggregatedArchive(final ClassLoader classLoader, final Map<String, Object> altDDs, final Collection<URL> xmls) {
        this(new ConfigurableClasspathArchive.FakeModule(classLoader, altDDs), xmls);
    }

    public Map<URL, List<String>> getClassesMap() {
        return map;
    }

    @Override
    public InputStream getBytecode(final String className) throws IOException, ClassNotFoundException {
        return archive.getBytecode(className);
    }

    @Override
    public Class<?> loadClass(final String className) throws ClassNotFoundException {
        return archive.loadClass(className);
    }

    @Override
    public Iterator<Entry> iterator() {
        return archive.iterator();
    }

    public static class ScanXmlSaverFilter implements Filter {
        private final boolean scanXmlExists;
        private final ScanUtil.ScanHandler handler;
        private final List<String> classes;
        private final Filter otherFilter;

        public ScanXmlSaverFilter(final boolean scanXmlExists, final ScanUtil.ScanHandler handler, final List<String> classes, final Filter otherFilter) {
            this.scanXmlExists = scanXmlExists;
            this.handler = handler;
            this.classes = classes;
            this.otherFilter = otherFilter;
        }

        @Override
        public boolean accept(final String name) {
            final boolean accept = otherFilter == null || otherFilter.accept(name);
            if (scanXmlExists) {
                for (final String packageName : handler.getPackages()) {
                    if (name.startsWith(packageName) && accept) {
                        classes.add(name);
                        return true;
                    }
                }
                for (final String className : handler.getClasses()) {
                    if (className.equals(name) && accept) {
                        classes.add(name);
                        return true;
                    }
                }
                return false;
            }
            if (accept) {
                classes.add(name);
            }
            return accept;
        }
    }
}
