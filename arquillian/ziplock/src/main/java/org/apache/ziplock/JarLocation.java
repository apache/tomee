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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @version $Rev$ $Date$
 */
public class JarLocation {

    public static File get() {
        return jarLocation(JarLocation.class);
    }

    public static File jarFromPrefix(final String prefix) {
        return jarFromRegex(prefix + ".*\\.jar");
    }

    public static File jarFromRegex(final String regex) {
        final Pattern pattern = Pattern.compile(regex);
        try {
            final Set<URL> urls = ClassLoaders.findUrls(Thread.currentThread().getContextClassLoader());
            for (final URL url : urls) {
                final String decode = decode(url.getFile());
                File f = new File(decode.replaceFirst("file:", ""));
                if (!f.exists() && f.getPath().endsWith("!")) {
                    f = new File(f.getPath().substring(0, f.getPath().length() - 1));
                }
                if (f.exists() && pattern.matcher(f.getName()).matches()) {
                    return f;
                }
            }
            throw new IllegalArgumentException(regex + " not found in " + urls);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static File jarFromResource(final String resourceName) {
        return jarFromResource(Thread.currentThread().getContextClassLoader(), resourceName);
    }

    public static File jarFromResource(final ClassLoader loader, final String resourceName) {
        try {
            URL url = loader.getResource(resourceName);
            if (url == null) {
                throw new IllegalStateException("classloader.getResource(classFileName) returned a null URL");
            }

            if ("jar".equals(url.getProtocol())) {
                final String spec = url.getFile();

                int separator = spec.indexOf('!');
                /*
                 * REMIND: we don't handle nested JAR URLs
                 */
                if (separator == -1) {
                    throw new MalformedURLException("no ! found in jar url spec:" + spec);
                }

                url = new URL(spec.substring(0, separator++));

                return new File(decode(url.getFile()));

            } else if ("file".equals(url.getProtocol())) {
                return toFile(resourceName, url);
            } else {
                throw new IllegalArgumentException("Unsupported URL scheme: " + url.toExternalForm());
            }
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static File jarLocation(final Class clazz) {
        try {
            final String classFileName = clazz.getName().replace(".", "/") + ".class";
            final ClassLoader loader = clazz.getClassLoader();
            return jarFromResource(loader, classFileName);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static File toFile(final String classFileName, final URL url) {
        String path = url.getFile();
        path = path.substring(0, path.length() - classFileName.length());
        return new File(decode(path));
    }

    public static String decode(final String fileName) {
        return ClassLoaders.decode(fileName);
    }

}
