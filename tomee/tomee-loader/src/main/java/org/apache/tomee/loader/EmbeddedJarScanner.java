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
package org.apache.tomee.loader;

import org.apache.catalina.deploy.WebXml;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.util.URLs;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.scan.Constants;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class EmbeddedJarScanner implements JarScanner {

    private static final Log log = LogFactory.getLog(EmbeddedJarScanner.class);

    /**
     * The string resources for this package.
     */
    private static final StringManager sm = StringManager.getManager(Constants.Package);

    private static final String FRAGMENT_CALLBACK = "org.apache.catalina.startup.ContextConfig$FragmentJarScannerCallback";

    /**
     * Scan the provided ServletContext and classloader for JAR files. Each JAR
     * file found will be passed to the callback handler to be processed.
     *
     * @param context       The ServletContext - used to locate and access
     *                      WEB-INF/lib
     * @param classloader   The classloader - used to access JARs not in
     *                      WEB-INF/lib
     * @param callback      The handler to process any JARs found
     * @param jarsToSkip    List of JARs to ignore. If this list is null, a
     *                      default list will be read from the system property
     *                      defined by {@link Constants#SKIP_JARS_PROPERTY}
     */
    @Override
    public void scan(final ServletContext context, final ClassLoader classloader, final JarScannerCallback callback, final Set<String> jarsToSkip) {

        try {
            final org.apache.xbean.finder.UrlSet scan = NewLoaderLogic.applyBuiltinExcludes(new org.apache.xbean.finder.UrlSet(classloader).excludeJvm(), null);

            // scan = scan.exclude(".*/WEB-INF/lib/.*"); // doing it simply prevent ServletContainerInitializer to de discovered

            for (final URL url : scan) {
                if (isWebInfClasses(url) && !FRAGMENT_CALLBACK.equals(callback.getClass().getName())) { // we need all fragments to let SCI working
                    continue;
                }

                // Need to scan this JAR
                if (log.isDebugEnabled()) {
                    log.debug(sm.getString("jarScan.webinflibJarScan", url.toExternalForm()));
                }

                try {
                    process(callback, url);
                } catch (final IOException e) {
                    log.warn(sm.getString("jarScan.webinflibFail", url), e);
                }
            }
        } catch (final IOException e) {
            log.warn(sm.getString("jarScan.classloaderFail", new URL[]{}), e);
        }
    }

    private static boolean isWebInfClasses(final URL url) {
        final File file = URLs.toFile(url);
        if (file == null || !file.exists() || ! "classes".equals(file.getName())) {
            return false;
        }

        final File webInf = file.getParentFile();
        return webInf != null && !(!webInf.exists() || !"WEB-INF".equals(webInf.getName()));
    }

    /*
     * Scan a URL for JARs with the optional extensions to look at all files
     * and all directories.
     */
    private void process(JarScannerCallback callback, URL url)
            throws IOException {

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

                    final String fileName = URLs.toFile(jarURL).getName();
                    // bug in tomcat 7.0.47 so we need to handle it manually
                    // TODO: remove this hack when upgrading to Tomcat 7.0.48
                    if (fileName.contains("tomcat7-websocket") && FRAGMENT_CALLBACK.equals(callback.getClass().getName())) {
                        final WebXml fragment = new WebXml();
                        fragment.setName("org_apache_tomcat_websocket");
                        fragment.setDistributable(true);
                        fragment.setMetadataComplete(true);
                        fragment.setVersion("3.0");
                        fragment.setURL(jarURL);
                        fragment.setJarName(fileName);
                        Map.class.cast(Reflections.get(callback, "fragments")).put(fragment.getName(), fragment);
                    } else {
                        callback.scan(JarURLConnection.class.cast(jarURL.openConnection()));
                    }

                } else {

                    try {

                        final File f = new File(url.toURI());

                        if (f.isFile()) {
                            // Treat this file as a JAR
                            final URL jarURL = new URL("jar:" + urlStr + "!/");
                            callback.scan((JarURLConnection) jarURL.openConnection());

                        } else if (f.isDirectory()) {

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

}
