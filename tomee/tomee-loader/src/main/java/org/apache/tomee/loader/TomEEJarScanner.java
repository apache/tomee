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

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.TldScanner;
import org.apache.openejb.util.URLs;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.file.Matcher;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.scan.Constants;
import org.apache.tomcat.util.scan.StandardJarScanner;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

public class TomEEJarScanner extends StandardJarScanner {

    private static final Log log = LogFactory.getLog(StandardJarScanner.class);

    private static final Set<String> defaultJarsToSkip = new HashSet<String>();

    /**
     * The string resources for this package.
     */
    private static final StringManager sm = StringManager.getManager(Constants.Package);

    static {
        String jarList = System.getProperty(Constants.SKIP_JARS_PROPERTY);
        if (jarList != null) {
            StringTokenizer tokenizer = new StringTokenizer(jarList, ",");
            while (tokenizer.hasMoreElements()) {
                defaultJarsToSkip.add(tokenizer.nextToken());
            }
        }
    }

    @Override
    public void scan(ServletContext context, ClassLoader classLoader, JarScannerCallback callback, Set<String> jarsToIgnore) {
        if ("FragmentJarScannerCallback".equals(callback.getClass().getSimpleName())) {
            EmbeddedJarScanner embeddedJarScanner = new EmbeddedJarScanner();
            embeddedJarScanner.scan(context, classLoader, callback, jarsToIgnore);
        } else if ("TldJarScannerCallback".equals(callback.getClass().getSimpleName())) {

            final Set<String> ignoredJars = defaultJarsToSkip;

            final Set<String[]> ignoredJarsTokens = new HashSet<String[]>();

            for (String pattern : ignoredJars) {
                ignoredJarsTokens.add(Matcher.tokenizePathAsArray(pattern));
            }

            // Scan WEB-INF/lib
            Set<String> dirList = context.getResourcePaths(Constants.WEB_INF_LIB);
            if (dirList != null) {
                Iterator<String> it = dirList.iterator();
                while (it.hasNext()) {
                    String path = it.next();
                    if (path.endsWith(Constants.JAR_EXT) &&
                            !Matcher.matchPath(ignoredJarsTokens,
                                    path.substring(path.lastIndexOf('/') + 1))) {
                        // Need to scan this JAR
                        URL url = null;
                        try {
                            // File URLs are always faster to work with so use them
                            // if available.
                            String realPath = context.getRealPath(path);
                            if (realPath == null) {
                                url = context.getResource(path);
                            } else {
                                url = (new File(realPath)).toURI().toURL();
                            }
                            process(callback, url);
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
            if (isScanClassPath()) {
                if (log.isTraceEnabled()) {
                    log.trace(sm.getString("jarScan.classloaderStart"));
                }


                try {
                    final ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    final Set<URL> tldFileUrls = TldScanner.scan(loader);

                    final Set<URL> jarUlrs = discardFilePaths(tldFileUrls);

                    for (URL url : jarUlrs) {
                        String jarName = getJarName(url);

                        // Skip JARs known not to be interesting and JARs
                        // in WEB-INF/lib we have already scanned
                        if (jarName != null && !(Matcher.matchPath(ignoredJarsTokens, jarName) || url.toString().contains(Constants.WEB_INF_LIB + jarName))) {

                            if (log.isDebugEnabled()) {
                                log.debug(sm.getString("jarScan.classloaderJarScan", url));
                            }
                            try {
                                process(callback, url);
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


//        String openejbWar = System.getProperty("tomee.war");
//
//        if (openejbWar == null) {
//            EmbeddedJarScanner embeddedJarScanner = new EmbeddedJarScanner();
//            embeddedJarScanner.scan(context, classLoader, callback, jarsToIgnore);
//            return;
//        }
//
//        Set<String> newIgnores = new HashSet<String>();
//        if (jarsToIgnore != null) {
//            newIgnores.addAll(jarsToIgnore);
//        }
//
//        if (openejbWar != null && "FragmentJarScannerCallback".equals(callback.getClass().getSimpleName())) {
//            File openejbApp = new File(openejbWar);
//            File libFolder = new File(openejbApp, "lib");
//            for (File f : libFolder.listFiles()) {
//                if (f.getName().toLowerCase().endsWith(".jar")) {
//                    newIgnores.add(f.getName());
//                }
//            }
//        }
//
//        super.scan(context, classLoader, callback, newIgnores);
    }

    private Set<URL> discardFilePaths(Set<URL> tldFileUrls) {
        final Set<String> jarPaths = new HashSet<String>();

        for (URL tldFileUrl : tldFileUrls) {
            jarPaths.add(URLs.toFilePath(tldFileUrl));
        }

        final Set<URL> jars = new HashSet<URL>();
        for (String jarPath : jarPaths) {
            try {
                final URL url = new File(jarPath).toURI().toURL();
                jars.add(url);
            } catch (MalformedURLException e) {
                log.warn("Skipping JAR file " + jarPath, e);
            }
        }
        return jars;
    }

    /*
    * Scan a URL for JARs with the optional extensions to look at all files
    * and all directories.
    */
    private void process(JarScannerCallback callback, URL url) throws IOException {

        if (log.isTraceEnabled()) {
            log.trace(sm.getString("jarScan.jarUrlStart", url));
        }

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

                        if (f.isFile() && isScanAllFiles()) {

                            // Treat this file as a JAR
                            final URL jarURL = new URL("jar:" + urlStr + "!/");
                            callback.scan((JarURLConnection) jarURL.openConnection());

                        } else if (f.isDirectory() && isScanAllDirectories()) {

                            final File metainf = new File(f.getAbsoluteFile() + File.separator + "META-INF");

                            if (metainf.isDirectory()) {
                                callback.scan(f);
                            }
                        }
                    } catch (URISyntaxException e) {
                        // Wrap the exception and re-throw
                        IOException ioe = new IOException();
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
    private String getJarName(URL url) {

        String name = null;

        String path = url.getPath();
        int end = path.indexOf(Constants.JAR_EXT);
        if (end != -1) {
            int start = path.lastIndexOf('/', end);
            name = path.substring(start + 1, end + 4);
        } else if (isScanAllDirectories()) {
            int start = path.lastIndexOf('/');
            name = path.substring(start + 1);
        }

        return name;
    }
}
