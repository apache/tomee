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
package org.apache.openejb.util;


import static org.apache.openejb.loader.JarLocation.decode;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xbean.finder.UrlSet;


/**
 * @version $Rev$ $Date$
 */
public class URLs {
    public static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, URLs.class.getPackage().getName());

    public static File toFile(final URL url) {
        if ("jar".equals(url.getProtocol())) {
            try {
                final String spec = url.getFile();

                int separator = spec.indexOf('!');
                /*
                 * REMIND: we don't handle nested JAR URLs
                 */
                if (separator == -1) throw new MalformedURLException("no ! found in jar url spec:" + spec);

                return toFile(new URL(spec.substring(0, separator++)));
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        } else if ("file".equals(url.getProtocol())) {
            return new File(decode(url.getFile()));
        } else {
            throw new IllegalArgumentException("Unsupported URL scheme: " + url.toExternalForm());
        }
    }

    public static String toFilePath(final URL url) {
        return toFile(url).getAbsolutePath();
    }

    public static UrlSet cullSystemAndOpenEJBJars(final UrlSet original) throws IOException {
        return cullSystemJars(cullOpenEJBJars(original));
    }

    public static UrlSet cullSystemJars(final UrlSet original) throws IOException {
        UrlSet urls = new UrlSet(original.getUrls());
        urls = urls.exclude(ClassLoader.getSystemClassLoader().getParent());
        urls = urls.excludeJavaExtDirs();
        urls = urls.excludeJavaEndorsedDirs();
        urls = urls.excludeJavaHome();
        urls = urls.excludePaths(System.getProperty("sun.boot.class.path", ""));
        urls = urls.exclude(".*/JavaVM.framework/.*");
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Culled {0} system urls from set", original.size() - urls.size());
        return urls;
    }

    public static UrlSet cullOpenEJBJars(final UrlSet original) throws IOException {
        UrlSet urls = new UrlSet(original.getUrls());
        urls = urls.exclude(".*openejb.*");
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Culled {0} OpenEJB urls from set", original.size() - urls.size());
        return urls;
    }

    private URLs() { }
}
