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
package org.apache.xbean.finder.archive;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Supports JarArchive and FileArchive URLs
 *
 * @version $Rev$ $Date$
 */
public class ClasspathArchive extends CompositeArchive {

    private final List<URL> urls = new ArrayList<URL>();
    private final ClassLoader loader;

    public ClasspathArchive(ClassLoader loader, URL... urls) {
        this(loader, Arrays.asList(urls));
    }

    public ClasspathArchive(ClassLoader loader, Iterable<URL> urls) {
        super(archives(loader, urls));
        this.loader = loader;

    }

    public static List<Archive> archives(ClassLoader loader, Iterable<URL> urls) {
        List<Archive> archives = new ArrayList<Archive>();

        for (URL location : urls) {
            try {
                archives.add(archive(loader, location));
            } catch (Exception e) {
                // TODO This is what we did before, so not too urgent to change, but not ideal
                e.printStackTrace();
            }
        }

        return archives;
    }

    public static Archive archive(ClassLoader loader, URL location) {

        if (location.getProtocol().equals("jar")) {

            return new JarArchive(loader, location);

        } else if (location.getProtocol().equals("file")) {

            try {

                // See if it's actually a jar

                URL jarUrl = new URL("jar", "", location.toExternalForm() + "!/");
                JarURLConnection juc = (JarURLConnection) jarUrl.openConnection();
                juc.getJarFile();

                return new JarArchive(loader, jarUrl);

            } catch (IOException e) {

                return new FileArchive(loader, location);

            }
        }

        throw new UnsupportedOperationException("unsupported archive type: " + location);
    }

    public static List<Archive> archives(ClassLoader loader, URL... urls) {
        return archives(loader, Arrays.asList(urls));
    }

    @Override
    public InputStream getBytecode(String className) throws IOException, ClassNotFoundException {
        int pos = className.indexOf("<");
        if (pos > -1) {
            className = className.substring(0, pos);
        }
        pos = className.indexOf(">");
        if (pos > -1) {
            className = className.substring(0, pos);
        }
        if (!className.endsWith(".class")) {
            className = className.replace('.', '/') + ".class";
        }

        URL resource = loader.getResource(className);
        if (resource != null) return resource.openStream();

        throw new ClassNotFoundException(className);
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return loader.loadClass(className);
    }
}