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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * @version $Rev$ $Date$
 */
public class JarArchive implements Archive, AutoCloseable {

    private final ClassLoader loader;
    private final URL url;
    private final JarFile jar;
    private final MJarSupport mjar = new MJarSupport();

    /*
     * Supports only 'file:/...' or 'jar:file:/...!/' URLs
     */
    public JarArchive(ClassLoader loader, URL url){
//        if (!"jar".equals(url.getProtocol())) throw new IllegalArgumentException("not a jar url: " + url);

        this.loader = loader;
        this.url = url;
        File jarFile = null;
        String jarPath;
        int idx;

        // Wipe out 'jar:' prefix AND '!/{...}' suffix(if any)
        if("jar".equalsIgnoreCase(url.getProtocol())){

            try{
                jarPath = url.getPath();
                url = new URL(jarPath.endsWith("!/") ?
                        jarPath.substring(0, jarPath.lastIndexOf("!/"))
                        : jarPath);
            }catch(MalformedURLException ex){
                throw new IllegalArgumentException(
                        "Please provide 'file:/...' or 'jar:file:/...!/' URL"
                                + " instead of '" + FileArchive.decode(String.valueOf(url)) + "'");
            }
        }

        try{
            // handle 'file:/...' URL
            if("file".equalsIgnoreCase(url.getProtocol())){

                // Testing if file DOEN't exists AND trying
                //  substrings up to every '!/{...}' as path
                idx = 0;
                jarPath = FileArchive.decode(url.getPath());
                for(String jp = jarPath; !(jarFile = new File(jp)).exists()
                        && (idx = jarPath.indexOf("!/", idx + 1)) > 0;
                    jp = jarPath.substring(0, idx)){}

                // All substrings attempted, but referenced file wasn't discovered
                if(!jarFile.exists()){

                    // To be caught later and wrapped into IllegalStateEx - default behavior
                    throw new FileNotFoundException(FileArchive.decode(String.valueOf(url)));
                }

            }else{
                throw new IllegalArgumentException(
                        "Please provide 'file:/...' or 'jar:file:/...!/' URL"
                                + " instead of '" + FileArchive.decode(String.valueOf(url)) + "'");
            }

            jar = new JarFile(jarFile);

        }catch(IOException e){
            throw new IllegalStateException("Cannot open jar(zip) '"
                    + jarFile != null ? // why can it be null? but since compiler thinks so...
                    jarFile.getAbsolutePath()
                    : FileArchive.decode(String.valueOf(url)) + "'", e);
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
        // assume the loader knows how to handle mjar release if activated
        return loader.loadClass(className);
    }

    public Iterator<Entry> iterator() {
        return new JarIterator();
    }

    @Override
    public void close() throws Exception {
        jar.close();
    }

    private class JarIterator implements Iterator<Entry> {

        private final Iterator<JarEntry> stream;
        private Entry next;

        private JarIterator() {
            final Enumeration<JarEntry> entries = jar.entries();
            try {
                final Manifest manifest = jar.getManifest();
                if (manifest != null) {
                    mjar.load(manifest);
                }
            } catch (IOException e) {
                // no-op
            }
            if (mjar.isMjar()) { // sort it to ensure we browse META-INF/versions first
                final List<JarEntry> list = new ArrayList<JarEntry>(Collections.list(entries));
                Collections.sort(list, new Comparator<JarEntry>() {
                    public int compare(JarEntry o1, JarEntry o2) {
                        final String n2 = o2.getName();
                        final String n1 = o1.getName();
                        final boolean n1v = n1.startsWith("META-INF/versions/");
                        final boolean n2v = n2.startsWith("META-INF/versions/");
                        if (n1v && n2v) {
                            return n1.compareTo(n2);
                        }
                        if (n1v) {
                            return -1;
                        }
                        if (n2v) {
                            return 1;
                        }
                        try {
                            return Integer.parseInt(n2) - Integer.parseInt(n1);
                        } catch (final NumberFormatException nfe) {
                            return n2.compareTo(n1);
                        }
                    }
                });
                stream = list.iterator();
            } else {
                stream = Collections.list(entries).iterator();
            }
        }

        private boolean advance() {
            if (next != null) {
                return true;
            }
            while (stream.hasNext()) {
                final JarEntry entry = stream.next();
                final String entryName = entry.getName();
                if (entry.isDirectory() || !entryName.endsWith(".class") || entryName.endsWith("module-info.class")) {
                    continue;
                }

                String className = entryName;
                if (entryName.endsWith(".class")) {
                    className = className.substring(0, className.length() - 6);
                }
                if (className.contains(".")) {
                    continue;
                }

                if (entryName.startsWith("META-INF/versions/")) { // JarFile will handle it for us
                    continue;
                }

                next = new ClassEntry(entry, className.replace('/', '.'));
                return true;
            }
            return false;
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
                try {
                    if (mjar.isMjar()) {
                        // JarFile handles it for us :)
                        final ZipEntry entry = jar.getJarEntry(this.entry.getName());
                        if (entry != null) {
                            return jar.getInputStream(entry);
                        }
                    }
                    return jar.getInputStream(entry);
                } catch (RuntimeException e) {
                    throw new RuntimeException("Cannot read '" + name + " from " + jar.getName() + "'", e);
                } catch (IOException e) {
                    throw new IOException("Cannot read '" + name + " from " + jar.getName() + "'", e);
                }
            }
        }
    }
}

