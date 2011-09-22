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
package org.apache.openejb.tomcat.loader;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.file.Matcher;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.scan.Constants;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @version $Rev$ $Date$
 */
public class EmbeddedJarScanner implements JarScanner {

    private static final Log log = LogFactory.getLog(EmbeddedJarScanner.class);

    /**
     * The string resources for this package.
     */
    private static final StringManager sm = StringManager.getManager(Constants.Package);

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
    public void scan(ServletContext context, ClassLoader classloader, JarScannerCallback callback, Set<String> jarsToSkip) {

        try {
            final UrlSet classpath = new UrlSet(classloader);

            UrlSet excluded = classpath.exclude(".*/WEB-INF/lib/.*");
            excluded = excluded.exclude(".*myfaces-impl-.*");
            excluded = excluded.exclude(".*openejb-jsf-.*");

            final UrlSet scan = classpath.exclude(excluded);

            for (URL url : scan) {
                // Need to scan this JAR
                if (log.isDebugEnabled()) {
                    log.debug(sm.getString("jarScan.webinflibJarScan", url.toExternalForm()));
                }

                try {
                    process(callback, url);
                } catch (IOException e) {
                    log.warn(sm.getString("jarScan.webinflibFail", url), e);
                }
            }
        } catch (IOException e) {
            log.warn(sm.getString("jarScan.classloaderFail", new URL[]{}), e);
        }
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
                    callback.scan((JarURLConnection) jarURL.openConnection());

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
