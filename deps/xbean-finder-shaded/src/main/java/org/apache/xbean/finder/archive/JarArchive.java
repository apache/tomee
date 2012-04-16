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
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * @version $Rev$ $Date$
 */
public class JarArchive implements Archive {

    private final ClassLoader loader;
    private final URL url;
    private List<String> list;
    private final JarFile jar;

    public JarArchive(ClassLoader loader, URL url) {
//        if (!"jar".equals(url.getProtocol())) throw new IllegalArgumentException("not a jar url: " + url);

        try {
            this.loader = loader;
            this.url = url;
            URL u = url;

            String jarPath = url.getFile();
            if (jarPath.contains("!")) {
                jarPath = jarPath.substring(0, jarPath.indexOf("!"));
                u = new URL(jarPath);
            }
            jar = new JarFile(u.getFile().replace("%20", " ")); // no more an url
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public URL getUrl() {
        return url;
    }

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

        ZipEntry entry = jar.getEntry(className);
        if (entry == null) throw new ClassNotFoundException(className);

        return jar.getInputStream(entry);
    }


    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return loader.loadClass(className);
    }

    public Iterator<Entry> iterator() {
        return new JarIterator();
    }

    private class JarIterator implements Iterator<Entry> {

        private final Enumeration<JarEntry> stream;
        private Entry next;

        private JarIterator() {
            stream = jar.entries();
        }

        private boolean advance() {
            if (next != null) return true;

            if (!stream.hasMoreElements()) return false;

            final JarEntry entry = stream.nextElement();

            if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                return advance();
            }

            final String className = entry.getName().replaceFirst(".class$", "");

            if (className.contains(".")) {
                return advance();
            }

            next = new ClassEntry(entry, className.replace('/', '.'));

            return true;
        }

        public boolean hasNext() {
            return advance();
        }

        public Entry next() {
            if (!hasNext()) throw new NoSuchElementException();
            Entry entry = next;
            next = null;
            return entry;
        }

        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        private class ClassEntry implements Entry {
            private final String name;
            private final JarEntry entry;

            private ClassEntry(JarEntry entry, String name) {
                this.name = name;
                this.entry = entry;
            }

            public String getName() {
                return name;
            }

            public InputStream getBytecode() throws IOException {
                return jar.getInputStream(entry);
            }
        }
    }
}

