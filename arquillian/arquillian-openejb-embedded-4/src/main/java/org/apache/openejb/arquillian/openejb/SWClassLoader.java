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
package org.apache.openejb.arquillian.openejb;

import org.apache.openejb.util.Enumerator;
import org.apache.openejb.util.reflection.Reflections;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.UrlAsset;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class SWClassLoader extends ClassLoader {
    static {
        try {
            final Field handler = URL.class.getDeclaredField("handlers");
            handler.setAccessible(true);
            ((Hashtable<String, URLStreamHandler>) handler.get(null)).put("archive", new ArchiveStreamHandler());
        } catch (final Exception e) {
            // no-op
        }
    }

    private final Archive<?> archive;
    private final String prefix;
    private final Collection<Closeable> closeables = new ArrayList<Closeable>();

    public SWClassLoader(final String prefix, final ClassLoader parent, final Archive<?> ar) {
        super(parent);
        this.prefix = prefix;
        this.archive = ar;
        ArchiveStreamHandler.set(ar, prefix, closeables);
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        if (name != null && !name.contains("META-INF/services/javax")) { // we want to avoid duplicates but we need container stuff, see bval tcks
            final Node node = findNode(name);
            if (node != null) {
                return enumerator(new URL(null, "archive:" + archive.getName() + "/" + name, new ArchiveStreamHandler()));
            }
        }
        return super.getResources(name);
    }

    @Override
    protected Enumeration<URL> findResources(final String name) throws IOException {
        final Node node = findNode(name);
        if (node != null) {
            return enumerator(new URL(null, "archive:" + archive.getName() + "/" + name, new ArchiveStreamHandler()));
        }
        return super.findResources(name);
    }

    private Node findNode(final String name) {
        ArchivePath path = ArchivePaths.create(prefix + name);
        Node node = archive.get(path);
        if (node == null) {
            path = ArchivePaths.create(name);
            node = archive.get(path);


        }
        return node;
    }

    private static Enumeration<URL> enumerator(final URL url) {
        return new Enumerator(Arrays.asList(url));
    }

    @Override
    protected URL findResource(final String name) {
        final Node node = findNode(name);
        if (node != null) {
            try {
                return new URL(null, "archive:" + archive.getName() + "/" + name, new ArchiveStreamHandler());
            } catch (final MalformedURLException e) {
                // no-op: let reuse parent method
            }
        }
        return super.findResource(name);
    }

    private static class ArchiveStreamHandler extends URLStreamHandler {
        public static final Map<String, Archive<?>> archives = new HashMap<String, Archive<?>>();
        public static final Map<String, String> prefixes = new HashMap<String, String>();
        public static final Map<String, Collection<Closeable>> closeables = new HashMap<String, Collection<Closeable>>();

        public static void set(final Archive<?> ar, final String p, final Collection<Closeable> c) {
            final String archiveName = ar.getName();
            archives.put(archiveName, ar);
            prefixes.put(archiveName, p);
            closeables.put(archiveName, c);
        }

        public static void reset(final String archiveName) {
            archives.remove(archiveName);
            prefixes.remove(archiveName);
            closeables.remove(archiveName);
        }

        @Override
        protected URLConnection openConnection(final URL u) throws IOException {
            final String arName = key(u);
            if (!prefixes.containsKey(arName)) {
                throw new IOException(u.toExternalForm() + " not found");
            }

            String path = prefixes.get(arName) + path(arName, u);
            Node node = archives.get(arName).get(path);
            if (node == null) {
                path = path(arName, u);
                node = archives.get(arName).get(path);
            }
            if (node == null) {
                throw new IOException(u.toExternalForm() + " not found");
            }

            final Asset asset = node.getAsset();
            if (UrlAsset.class.isInstance(asset)) {
                return URL.class.cast(Reflections.get(asset, "url")).openConnection();
            } else if (FileAsset.class.isInstance(asset)) {
                return File.class.cast(Reflections.get(asset, "file")).toURI().toURL().openConnection();
            } else if (ClassLoaderAsset.class.isInstance(asset)) {
                return ClassLoader.class.cast(Reflections.get(asset, "classLoader")).getResource(String.class.cast(Reflections.get(asset, "resourceName"))).openConnection();
            }

            return new URLConnection(u) {
                @Override
                public void connect() throws IOException {
                    // no-op
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    final InputStream input = asset.openStream();
                    final Collection<Closeable> c = closeables.get(arName);
                    c.add(input);
                    return input;
                }
            };
        }

        private static String path(final String arName, final URL url) {
            final String p = url.getPath();
            final String out = p.substring(arName.length(), p.length());
            if (prefixes.get(arName).endsWith("/") && out.startsWith("/")) {
                return out.substring(1);
            }
            return out;
        }

        private static String key(final URL url) {
            final String p = url.getPath();
            if (p == null) {
                return null;
            }
            final int endIndex = p.indexOf('/');
            if (endIndex >= 0) {
                return p.substring(0, endIndex);
            }
            return p;
        }
    }

    public void close() {
        ArchiveStreamHandler.reset(archive.getName());
        for (final Closeable cl : closeables) {
            try {
                cl.close();
            } catch (final IOException e) {
                // no-op
            }
        }
    }

    // to let frameworks using TCCL use the archive directly
    public Archive<?> getArchive() {
        return archive;
    }
}
