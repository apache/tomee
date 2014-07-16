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
package org.apache.ziplock;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ClassLoaders {
    private static final boolean DONT_USE_GET_URLS = Boolean.getBoolean("xbean.finder.use.get-resources");
    private static final ClassLoader SYSTEM = ClassLoader.getSystemClassLoader();

    private static final boolean UNIX = !System.getProperty("os.name").toLowerCase().contains("win");

    public static Set<URL> findUrls(final ClassLoader classLoader) throws IOException {
        if (classLoader == null || (SYSTEM.getParent() != null && classLoader == SYSTEM.getParent())) {
            return Collections.emptySet();
        }

        final Set<URL> urls =  new HashSet<URL>();

        if (URLClassLoader.class.isInstance(classLoader) && !DONT_USE_GET_URLS) {
            if (!isSurefire(classLoader)) {
                for (final Collection<URL> item : Arrays.asList(
                        Arrays.asList(URLClassLoader.class.cast(classLoader).getURLs()), findUrls(classLoader.getParent()))) {
                    for (final URL url : item) {
                        addIfNotSo(urls, url);
                    }
                }
            } else { // http://jira.codehaus.org/browse/SUREFIRE-928 - we could reuse findUrlFromResources but this seems faster
                for (final URL url : fromClassPath()) {
                    urls.add(url);
                }
            }
        } else {
            for (final URL url : findUrlFromResources(classLoader)) {
                urls.add(url);
            }
        }

        return urls;
    }

    private static void addIfNotSo(final Set<URL> urls, final URL url) {
        if (UNIX && isNative(url)) {
            return;
        }

        urls.add(url);
    }

    public static File toFile(final URL url) {
        if ("jar".equals(url.getProtocol())) {
            try {
                final String spec = url.getFile();
                final int separator = spec.indexOf('!');
                if (separator == -1) {
                    return null;
                }
                return toFile(new URL(spec.substring(0, separator + 1)));
            } catch (final MalformedURLException e) {
                return null;
            }
        } else if ("file".equals(url.getProtocol())) {
            String path = decode(url.getFile());
            if (path.endsWith("!")) {
                path = path.substring(0, path.length() - 1);
            }
            return new File(path);
        }
        return null;
    }

    public static String decode(final String fileName) {
        if (fileName.indexOf('%') == -1) {
            return fileName;
        }

        final StringBuilder result = new StringBuilder(fileName.length());
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (int i = 0; i < fileName.length();) {
            final char c = fileName.charAt(i);

            if (c == '%') {
                out.reset();
                do {
                    if (i + 2 >= fileName.length()) {
                        throw new IllegalArgumentException("Incomplete % sequence at: " + i);
                    }

                    final int d1 = Character.digit(fileName.charAt(i + 1), 16);
                    final int d2 = Character.digit(fileName.charAt(i + 2), 16);

                    if (d1 == -1 || d2 == -1) {
                        throw new IllegalArgumentException("Invalid % sequence (" + fileName.substring(i, i + 3) + ") at: " + String.valueOf(i));
                    }

                    out.write((byte) ((d1 << 4) + d2));

                    i += 3;

                } while (i < fileName.length() && fileName.charAt(i) == '%');


                result.append(out.toString());

                continue;
            } else {
                result.append(c);
            }

            i++;
        }
        return result.toString();
    }

    public static boolean isNative(final URL url) {
        final File file = toFile(url);
        if (file != null) {
            final String name = file.getName();
            if (!name.endsWith(".jar") && !file.isDirectory()
                    && name.contains(".so") && file.getAbsolutePath().startsWith("/usr/lib")) {
                return true;
            }
        }
        return false;
    }


    private static boolean isSurefire(final ClassLoader classLoader) {
        return System.getProperty("surefire.real.class.path") != null && classLoader == SYSTEM;
    }

    private static Collection<URL> fromClassPath() {
        final String[] cp = System.getProperty("java.class.path").split(System.getProperty("path.separator", ":"));
        final Set<URL> urls = new HashSet<URL>();
        for (final String path : cp) {
            try {
                urls.add(new File(path).toURI().toURL()); // don't build the url in plain String since it is not portable
            } catch (final MalformedURLException e) {
                // ignore
            }
        }
        return urls;
    }

    public static Set<URL> findUrlFromResources(final ClassLoader classLoader) throws IOException {
        final Set<URL> set = new HashSet<URL>();
        for (final URL url : Collections.list(classLoader.getResources("META-INF"))) {
            final String externalForm = url.toExternalForm();
            set.add(new URL(externalForm.substring(0, externalForm.lastIndexOf("META-INF"))));
        }
        set.addAll(Collections.list(classLoader.getResources("")));
        return set;
    }

    private ClassLoaders() {
        // no-op
    }
}
