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


import org.apache.openejb.loader.Files;
import org.apache.xbean.finder.UrlSet;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;


/**
 * @version $Rev$ $Date$
 */
public final class URLs {
    public static File toFile(final URL url) {
        return Files.toFile(url);
    }

    public static URL toFileUrl(final URL url) {
        if ("jar".equals(url.getProtocol())) {
            try {
                final String spec = url.getFile();

                int separator = spec.indexOf('!');
                /*
                 * REMIND: we don't handle nested JAR URLs
                 */
                if (separator == -1) {
                    throw new MalformedURLException("no ! found in jar url spec:" + spec);
                }

                return new URL(spec.substring(0, separator++));
            } catch (final MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        } else if ("file".equals(url.getProtocol())) {
            return url;
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
        final String sunboot = JavaSecurityManagers.getSystemProperty("sun.boot.class.path", "");
        UrlSet urls = new UrlSet(original.getUrls());
        urls = urls.exclude(ClassLoader.getSystemClassLoader().getParent());
        urls = urls.excludeJvm();
        if (!sunboot.isEmpty()) { // else on java9 it excludes new File(".") so all maven builds fail
            urls = urls.excludePaths(sunboot);
        }
        urls = urls.exclude(".*/JavaVM.framework/.*");
        return urls;
    }

    public static UrlSet cullOpenEJBJars(final UrlSet original) throws IOException {
        UrlSet urls = new UrlSet(original.getUrls());
        urls = urls.exclude(".*openejb.*");
        return urls;
    }

    public static URI uri(final String uri) {
        if (!uri.startsWith("file") && !uri.startsWith("jar") && !uri.isEmpty()) {
            final File f = new File(uri);
            if (f.exists()) {
                return f.toURI();
            }
        }
        return URI.create(uri.replace(" ", "%20").replace("#", "%23"));
    }

    private URLs() {
    }
}
