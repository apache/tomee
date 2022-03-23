/**
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

package org.apache.tomee.loader;

import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.util.URLs;
import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.apache.xbean.finder.ClassLoaders;
import org.apache.xbean.finder.filter.Filter;
import org.apache.xbean.finder.filter.Filters;

import jakarta.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static java.lang.ClassLoader.getSystemClassLoader;

// todo: share common tld parsing, tomcat has a built in method for it, ensure we reuse it
public class TomEEJarScanner extends StandardJarScanner {
    private static final TomEEFilter DEFAULT_JAR_SCAN_FILTER = new TomEEFilter(null);

    public TomEEJarScanner() {
        setJarScanFilter(DEFAULT_JAR_SCAN_FILTER);
    }

    private void configureFilter(final JarScanFilter jarScanFilter) {
        setJarScanFilter(new TomEEFilter(jarScanFilter));
    }

    @Override
    public void setJarScanFilter(final JarScanFilter jarScanFilter) {
        super.setJarScanFilter(jarScanFilter);
        if (!TomEEFilter.class.isInstance(jarScanFilter)) {
            configureFilter(jarScanFilter);
        }
    }

    @Override
    public void scan(final JarScanType scanType, final ServletContext context, final JarScannerCallback callback) {
        super.scan(scanType, context, callback);
        if (!embeddedSurefireScanning(scanType, context, callback) && isScanClassPath() && !URLClassLoader.class.isInstance(getSystemClassLoader())
                && !Boolean.getBoolean("tomee.classpath.scanning.disabled")) {
            // TODO: check on tomcat upgrade if it is fixed
            final String cp = System.getProperty("java.class.path");
            final Collection<URL> urls = new HashSet<>();
            for (final String jar : cp.split(File.pathSeparator)) {
                if(!jar.isEmpty()){
                    try {
                        urls.add(new File(jar).toURI().toURL());
                    } catch (MalformedURLException e) {
                        // no-op
                    }
                }
            }
            doScan(scanType, callback, new LinkedList<>(urls));
        }
    }

    private boolean embeddedSurefireScanning(final JarScanType scanType, final ServletContext context, final JarScannerCallback callback) {
        if (isScanClassPath() && System.getProperty("surefire.real.class.path") != null) {
            try {
                final Set<URL> urls = ClassLoaders.findUrls(context.getClassLoader().getParent());
                doScan(scanType, callback, new LinkedList<>(urls));
                return true;
            } catch (final IOException e) {
                // no-op
            }
        }
        return false;
    }

    private void doScan(final JarScanType scanType, final JarScannerCallback callback, final Deque<URL> urls) {
        Method process = null;
        final boolean scanAllDirectories = isScanAllDirectories();
        for (final URL url : urls) {
            final File cpe = URLs.toFile(url);
            if ((cpe.getName().endsWith(".jar") ||
                    scanType == JarScanType.PLUGGABILITY ||
                    scanAllDirectories) &&
                    getJarScanFilter().check(scanType, cpe.getName())) {
                try {
                    if (process == null) {
                        process = StandardJarScanner.class.getDeclaredMethod("process",
                                JarScanType.class, JarScannerCallback.class, URL.class, String.class, boolean.class, Deque.class);
                        if (!process.isAccessible()) {
                            process.setAccessible(true);
                        }
                    }
                    process.invoke(this, scanType, callback, url, null, true, urls);
                } catch (final Exception ioe) {
                    // no-op
                }
            }
        }
    }

    public /*context.xml*/ static class TomEEFilter implements JarScanFilter {
        private static final Filter INCLUDE = Filters.tokens("jakarta.faces-2.", "jakarta.faces-2.",
                "jakarta.faces-3.", "jakarta.faces-4.", "spring-security-taglibs", "spring-webmvc");
        private final JarScanFilter delegate;

        public TomEEFilter() {
            this(null);
        }

        public TomEEFilter(final JarScanFilter jarScanFilter) {
            this.delegate = jarScanFilter;
        }

        @Override
        public boolean check(final JarScanType jarScanType, final String jarName) {
            if (jarScanType == JarScanType.TLD) {
                if (INCLUDE.accept(jarName)) {
                    return true;
                }
                if (jarName.startsWith("tomcat-websocket") || jarName.startsWith("myfaces-impl") /* see org.apache.tomee.jasper.TomEETldScanner.scanPlatform */) {
                    return false;
                }
            }
            if (jarName.startsWith("johnzon-") || jarName.startsWith("xx-arquillian-tomee-")) {
                return false; // but we scan it in openejb scanning
            }
            return !NewLoaderLogic.skip(jarName) && (delegate == null || delegate.check(jarScanType, jarName));
        }

        public JarScanFilter getDelegate() {
            return delegate;
        }
    }
}
