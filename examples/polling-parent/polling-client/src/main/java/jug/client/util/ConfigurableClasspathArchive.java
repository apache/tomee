/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jug.client.util;

import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.apache.xbean.finder.archive.ClasspathArchive;
import org.apache.xbean.finder.archive.CompositeArchive;
import org.apache.xbean.finder.archive.FilteredArchive;
import org.apache.xbean.finder.filter.Filter;
import org.apache.xbean.finder.filter.FilterList;
import org.apache.xbean.finder.filter.PackageFilter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigurableClasspathArchive extends CompositeArchive {

    private static final SAXParserFactory SAX_FACTORY = SAXParserFactory.newInstance();
    private static final String SCAN_XML = "META-INF/scan.xml";

    public ConfigurableClasspathArchive(final ClassLoader loader, final Iterable<URL> urls) {
        super(archive(loader, urls, true));
    }

    public static List<Archive> archive(final ClassLoader loader, final Iterable<URL> urls, boolean forceDescriptor) {
        final List<Archive> archives = new ArrayList<Archive>();
        for (URL location : urls) {
            try {
                archives.add(archive(loader, location, true));
            } catch (Exception e) {
                // ignored
            }
        }
        return archives;
    }

    public static Archive archive(final ClassLoader loader, final URL location, boolean forceDescriptor) {
        try {
            URL scanXml = loader.getResource(SCAN_XML);
            if (scanXml == null && !forceDescriptor) {
                return ClasspathArchive.archive(loader, location);
            } else if (scanXml == null) {
                return new ClassesArchive();
            }

            // read descriptors
            ScanHandler scan;
            if (scanXml != null) {
                scan = read(scanXml);
            } else {
                scan = new ScanHandler();
            }

            final Archive packageArchive = packageArchive(scan.getPackages(), loader, location);
            final Archive classesArchive = classesArchive(scan.getPackages(), scan.getClasses(), loader);

            if (packageArchive != null && classesArchive != null) {
                return new CompositeArchive(classesArchive, packageArchive);
            } else if (packageArchive != null) {
                return packageArchive;
            }
            return classesArchive;
        } catch (IOException e) {
            if (forceDescriptor) {
                return new ClassesArchive();
            }
            return ClasspathArchive.archive(loader, location);
        }
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

    public static ScanHandler read(final URL scanXml) throws IOException {
        final SAXParser parser;
        try {
            synchronized (SAX_FACTORY) {
                parser = SAX_FACTORY.newSAXParser();
            }
            final ScanHandler handler = new ScanHandler();
            parser.parse(new BufferedInputStream(scanXml.openStream()), handler);
            return handler;
        } catch (Exception e) {
            throw new IOException("can't parse " + scanXml.toExternalForm());
        }
    }

    public static final class ScanHandler extends DefaultHandler {

        private final Set<String> classes = new HashSet<String>();
        private final Set<String> packages = new HashSet<String>();
        private Set<String> current = null;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals("class")) {
                current = classes;
            } else if (qName.equals("package")) {
                current = packages;
            }
        }

        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
            if (current != null) {
                current.add(new String(ch, start, length));
            }
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            current = null;
        }

        public Set<String> getPackages() {
            return packages;
        }

        public Set<String> getClasses() {
            return classes;
        }
    }
}

