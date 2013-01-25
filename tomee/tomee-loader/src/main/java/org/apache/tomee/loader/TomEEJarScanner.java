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

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.startup.TldConfig;
import org.apache.catalina.startup.XmlErrorHandler;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.config.TldScanner;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.DaemonThreadFactory;
import org.apache.openejb.util.URLs;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.file.Matcher;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.scan.Constants;
import org.apache.tomcat.util.scan.StandardJarScanner;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TomEEJarScanner extends StandardJarScanner {

    private static final Log log = LogFactory.getLog(StandardJarScanner.class);

    protected static final Set<String[]> DEFAULT_JARS_TO_SKIP;

    /**
     * The string resources for this package.
     */
    private static final StringManager sm = StringManager.getManager(Constants.Package);

    private static final Method tldConfigScanStream;
    private static final Field tldConfig;
    private static final Method tldLocationScanStream;
    private static final Field tldLocationCache;

    // tld of the server
    private static final Set<URL> SERVER_URLS;

    // TldConfig of the server (common)
    private static final Set<String> TAG_LIB_URIS;
    private static final ArrayList<String> LISTENERS;

    // TldLocationCache of the server (common)
    private static final Hashtable<String, Object> MAPPINGS;

    private static final Thread SERVER_SCANNING_THREAD;

    static {
        final Set<String> defaultJarsToSkip = new HashSet<String>();
        final String jarList = System.getProperty(Constants.SKIP_JARS_PROPERTY);
        if (jarList != null) {
            final StringTokenizer tokenizer = new StringTokenizer(jarList, ",");
            while (tokenizer.hasMoreElements()) {
                defaultJarsToSkip.add(tokenizer.nextToken());
            }
        }

        final Set<String[]> ignoredJarsTokens = new HashSet<String[]>();
        for (final String pattern : defaultJarsToSkip) {
            ignoredJarsTokens.add(Matcher.tokenizePathAsArray(pattern));
        }
        DEFAULT_JARS_TO_SKIP = ignoredJarsTokens;

        try {
            final ClassLoader loader = TomEEJarScanner.class.getClassLoader();

            tldConfigScanStream = TldConfig.class.getDeclaredMethod("tldScanStream", InputStream.class);
            tldConfigScanStream.setAccessible(true);
            tldConfig = loader.loadClass("org.apache.catalina.startup.TldConfig$TldJarScannerCallback")
                            .getDeclaredFields()[0]; // there is a unique field and this way it is portable
                            //.getDeclaredField("this$0");
            tldConfig.setAccessible(true);

            final Class<?> tldLocationsCache = loader.loadClass("org.apache.jasper.compiler.TldLocationsCache");
            tldLocationScanStream = tldLocationsCache.getDeclaredMethod("tldScanStream", String.class, String.class, InputStream.class);
            tldLocationScanStream.setAccessible(true);
            tldLocationCache = loader.loadClass("org.apache.jasper.compiler.TldLocationsCache$TldJarScannerCallback")
                                    .getDeclaredFields()[0];
            tldLocationCache.setAccessible(true);

            // init server cache
            SERVER_URLS = TldScanner.scan(TomEEJarScanner.class.getClassLoader());

            final Context fakeWebApp = (Context) Proxy.newProxyInstance(loader, new Class<?>[]{Context.class},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            if ("getTldNamespaceAware".equals(method.getName())) {
                                return Globals.STRICT_SERVLET_COMPLIANCE;
                            } else if ("getTldValidation".equals(method.getName())) {
                                return Globals.STRICT_SERVLET_COMPLIANCE;
                            }
                            return null;
                        }
                    });
            final TldConfig config = new TldConfig();
            config.lifecycleEvent(new LifecycleEvent(fakeWebApp, Lifecycle.AFTER_INIT_EVENT, null));

            final Object fakeSc = Proxy.newProxyInstance(loader, new Class<?>[]{ ServletContext.class }, new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                    return null;
                }
            });
            final Object locationsCacheInstance = tldLocationsCache.getConstructor(ServletContext.class).newInstance(fakeSc);

            if (!SERVER_URLS.isEmpty()) {
                SERVER_SCANNING_THREAD = new Thread() {
                    @Override
                    public void run() {
                        for (URL current : SERVER_URLS) {
                            if (current.toExternalForm().contains("myfaces-impl-")) {
                                // done elsewhere
                                continue;
                            }

                            tldConfig(config, current);
                            if (tldLocationsCache != null) {
                                tldLocationCache(locationsCacheInstance, current);
                            }
                        }
                    }
                };
                SERVER_SCANNING_THREAD.setName("TomEE-server-tld-reading");
                SERVER_SCANNING_THREAD.start();
            } else {
                SERVER_SCANNING_THREAD = null;
            }

            TAG_LIB_URIS = (Set<String>) Reflections.get(config, "taglibUris");
            LISTENERS = (ArrayList<String>) Reflections.get(config, "listeners");
            MAPPINGS= (Hashtable<String, Object>) Reflections.get(locationsCacheInstance, "mappings");
        } catch (Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    @Override
    public void scan(final ServletContext context, final ClassLoader classLoader, final JarScannerCallback callback, final Set<String> jarsToIgnore) {
        if ("FragmentJarScannerCallback".equals(callback.getClass().getSimpleName())) {
            final EmbeddedJarScanner embeddedJarScanner = new EmbeddedJarScanner();
            embeddedJarScanner.scan(context, classLoader, callback, jarsToIgnore);
        } else if ("TldJarScannerCallback".equals(callback.getClass().getSimpleName())) {

            final String cbName = callback.getClass().getName();
            if (cbName.equals(tldConfig.getDeclaringClass().getName())) {
                ensureServerTldsScanned();
                try {
                    final TldConfig config;
                    try {
                        config = (TldConfig) tldConfig.get(callback);
                    } catch (IllegalAccessException e) {
                        throw new OpenEJBException("scan with default algo");
                    }

                    final Set<URL> urls = TldScanner.scan(context.getClassLoader());
                    for (URL url : urls) {
                        if (!SERVER_URLS.contains(url)) {
                            tldConfig(config, url);
                        }
                    }

                    // add already scanned ones
                    for (String uri : TAG_LIB_URIS) {
                        config.addTaglibUri(uri);
                    }
                    for (String listener : LISTENERS) {
                        config.addApplicationListener(listener);
                    }

                    return; // done, next code is a fallback if scan() throw an exception
                } catch (OpenEJBException oe) {
                    // no-op
                }
            } else if (cbName.equals(tldLocationCache.getDeclaringClass().getName())) {
                ensureServerTldsScanned();
                try {
                    final Object tldLocationsCache;
                    try {
                        tldLocationsCache = tldLocationCache.get(callback);
                    } catch (IllegalAccessException e) {
                        throw new OpenEJBException("scan with default algo");
                    }

                    final Set<URL> urls = TldScanner.scan(context.getClassLoader());
                    for (URL url : urls) {
                        if (!SERVER_URLS.contains(url)) {
                            tldLocationCache(tldLocationsCache, url);
                        }
                    }

                    // add server ones
                    final Hashtable<String, Object> mappings = (Hashtable<String, Object>) Reflections.get(tldLocationsCache, "mappings");
                    mappings.putAll((Map<String, Object>) MAPPINGS.clone());

                    return;
                } catch (OpenEJBException oe) {
                    // no-op
                }
            } else { log.debug("This callback " + callback + " is not known and perf optim will not be available"); }

            // Scan WEB-INF/lib
            final Set<String> dirList = context.getResourcePaths(Constants.WEB_INF_LIB);
            if (dirList != null) {
                for (final String path : dirList) {
                    if (path.endsWith(Constants.JAR_EXT) &&
                            !Matcher.matchPath(DEFAULT_JARS_TO_SKIP,
                                    path.substring(path.lastIndexOf('/') + 1))) {
                        // Need to scan this JAR
                        URL url = null;
                        try {
                            // File URLs are always faster to work with so use them
                            // if available.
                            final String realPath = context.getRealPath(path);
                            if (realPath == null) {
                                url = context.getResource(path);
                            } else {
                                url = (new File(realPath)).toURI().toURL();
                            }
                            this.process(callback, url);
                        } catch (IOException e) {
                            log.warn(sm.getString("jarScan.webinflibFail", url), e);
                        }
                    } else {
                        if (log.isTraceEnabled()) {
                            log.trace(sm.getString("jarScan.webinflibJarNoScan", path));
                        }
                    }
                }
            }

            // Scan the classpath
            if (this.isScanClassPath()) {
                if (log.isTraceEnabled()) {
                    log.trace(sm.getString("jarScan.classloaderStart"));
                }


                try {
                    final ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    final Set<URL> tldFileUrls = TldScanner.scan(loader);

                    final Set<URL> jarUlrs = this.discardFilePaths(tldFileUrls);

                    for (final URL url : jarUlrs) {
                        final String jarName = this.getJarName(url);

                        // Skip JARs known not to be interesting and JARs
                        // in WEB-INF/lib we have already scanned
                        if (jarName != null && !(Matcher.matchPath(DEFAULT_JARS_TO_SKIP, jarName) || url.toString().contains(Constants.WEB_INF_LIB + jarName))) {

                            if (log.isDebugEnabled()) {
                                log.debug(sm.getString("jarScan.classloaderJarScan", url));
                            }
                            try {
                                this.process(callback, url);
                            } catch (IOException ioe) {
                                log.warn(sm.getString(
                                        "jarScan.classloaderFail", url), ioe);
                            }
                        } else {
                            if (log.isTraceEnabled()) {
                                log.trace(sm.getString("jarScan.classloaderJarNoScan", url));
                            }
                        }
                    }
                } catch (OpenEJBException e) {
                    log.warn("JarScan.TldScan Failed ", e);
                }
            }

        } else {
            super.scan(context, classLoader, callback, jarsToIgnore);
        }
    }

    private void ensureServerTldsScanned() {
        if (SERVER_SCANNING_THREAD == null) {
            return;
        }

        try {
            SERVER_SCANNING_THREAD.join();
        } catch (InterruptedException e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    private Set<URL> discardFilePaths(final Set<URL> tldFileUrls) {
        final Set<String> jarPaths = new HashSet<String>();

        for (final URL tldFileUrl : tldFileUrls) {
            jarPaths.add(URLs.toFilePath(tldFileUrl));
        }

        final Set<URL> jars = new HashSet<URL>();
        for (final String jarPath : jarPaths) {
            try {
                final URL url = new File(jarPath).toURI().toURL();
                jars.add(url);
            } catch (MalformedURLException e) {
                log.warn("Skipping JAR file " + jarPath, e);
            }
        }
        return jars;
    }

    private static void tldLocationCache(final Object tldLocationsCache, final URL url) {
        String resource = url.toString();
        String entry = null;

        if (resource.contains("!/")) {
            final String path = url.getPath();
            final int endIndex = path.indexOf("!/");
            resource = path.substring(0, endIndex);
            entry = path.substring(endIndex + 2, path.length());
        }

        InputStream is = null;
        try {
            is = url.openStream();
            tldLocationScanStream.invoke(tldLocationsCache, resource, entry, is);
        } catch (Exception e) {
            log.warn(sm.getString("jarScan.webinflibFail", url.toExternalForm()), e);
        } finally {
            IO.close(is);
        }
    }

    private static void tldConfig(final TldConfig config, final URL current) {
        InputStream is = null;
        try {
            is = current.openStream();
            final XmlErrorHandler handler = (XmlErrorHandler) tldConfigScanStream.invoke(config, is);
            handler.logFindings(log, current.toExternalForm());
        } catch (Exception e) {
            log.warn(sm.getString("jarScan.webinflibFail", current), e);
        } finally {
            IO.close(is);
        }
    }

    /*
    * Scan a URL for JARs with the optional extensions to look at all files
    * and all directories.
    */
    private void process(final JarScannerCallback callback, final URL url) throws IOException {
        final URLConnection conn = url.openConnection();
        if (conn instanceof JarURLConnection) {

            callback.scan((JarURLConnection) conn);

        } else {

            final String urlStr = url.toString();

            if (urlStr.startsWith("file:") || urlStr.startsWith("jndi:")) {

                if (urlStr.endsWith(Constants.JAR_EXT)) {

                    final URL jarURL = new URL("jar:" + urlStr + "!/");
                    callback.scan((JarURLConnection) jarURL.openConnection());

                } else {
                    try {

                        final File f = new File(url.toURI());

                        if (f.isFile() && this.isScanAllFiles()) {

                            // Treat this file as a JAR
                            final URL jarURL = new URL("jar:" + urlStr + "!/");
                            callback.scan((JarURLConnection) jarURL.openConnection());

                        } else if (f.isDirectory() && this.isScanAllDirectories()) {

                            final File metainf = new File(f.getAbsoluteFile() + File.separator + "META-INF");

                            if (metainf.isDirectory()) {
                                callback.scan(f);
                            }
                        }
                    } catch (URISyntaxException e) {
                        // Wrap the exception and re-throw
                        final IOException ioe = new IOException();
                        ioe.initCause(e);
                        throw ioe;
                    }
                }
            }
        }
    }

    /*
     * Extract the JAR name, if present, from a URL
     */
    private String getJarName(final URL url) {

        String name = null;

        final String path = url.getPath();
        final int end = path.indexOf(Constants.JAR_EXT);
        if (end != -1) {
            final int start = path.lastIndexOf('/', end);
            name = path.substring(start + 1, end + 4);
        } else if (this.isScanAllDirectories()) {
            final int start = path.lastIndexOf('/');
            name = path.substring(start + 1);
        }

        return name;
    }
}
