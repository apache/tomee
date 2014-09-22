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

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Set;

// todo: share common tld parsing, tomcat has a built in method for it, ensure we reuse it
public class TomEEJarScanner extends StandardJarScanner {
    public TomEEJarScanner() {
        setJarScanFilter(new TomEEFilter(null));
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
        embeddedSurefireScanning(scanType, context, callback);
    }

    private void embeddedSurefireScanning(final JarScanType scanType, final ServletContext context, final JarScannerCallback callback) {
        if (isScanClassPath() && System.getProperty("surefire.real.class.path") != null) {
            Method process = null;
            try {
                final Set<URL> urls = ClassLoaders.findUrls(context.getClassLoader().getParent());
                final boolean scanAllDirectories = isScanAllDirectories();
                for (final URL url : urls) {
                    final File cpe = URLs.toFile(url);
                    if ((cpe.getName().endsWith(".jar") ||
                            scanType == JarScanType.PLUGGABILITY ||
                            scanAllDirectories) &&
                            getJarScanFilter().check(scanType, cpe.getName())) {
                        try {
                            if (process == null) {
                                process = StandardJarScanner.class.getDeclaredMethod("process", JarScanType.class, JarScannerCallback.class, URL.class, String.class, boolean.class);
                                if (!process.isAccessible()) {
                                    process.setAccessible(true);
                                }
                            }
                            process.invoke(this, scanType, callback, url, null, true);
                        } catch (final Exception ioe) {
                            // no-op
                        }
                    }
                }
            } catch (final IOException e) {
                // no-op
            }
        }
    }

    private static class TomEEFilter implements JarScanFilter {
        private static final Filter INCLUDE = Filters.tokens("javax.faces-2.", "spring-security-taglibs", "spring-webmvc");
        private final JarScanFilter delegate;

        public TomEEFilter(final JarScanFilter jarScanFilter) {
            this.delegate = jarScanFilter;
        }

        @Override
        public boolean check(final JarScanType jarScanType, final String jarName) {
            if (jarScanType == JarScanType.TLD) {
                if (INCLUDE.accept(jarName)) {
                    return true;
                }
                if (jarName.startsWith("tomcat-websocket")) {
                    return false;
                }
            }
            if (jarName.startsWith("johnzon-")) {
                return false; // but we scan it in openejb scnaning
            }
            return !NewLoaderLogic.skip(jarName) && (delegate == null || delegate.check(jarScanType, jarName));
        }
    }
}
