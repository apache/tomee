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
package org.apache.openejb.loader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
public class JarLocation {

    public static File get() {
        return jarLocation(JarLocation.class);
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

    public static File toFile(final String classFileName, final URL url) {
        String path = url.getFile();
        path = path.substring(0, path.length() - classFileName.length());
        return new File(decode(path));
    }

    public static String decode(final String fileName) {
        if (fileName.indexOf('%') == -1) {
            return fileName;
        }

        final StringBuilder result = new StringBuilder(fileName.length());
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (int i = 0; i < fileName.length(); ) {
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

}
