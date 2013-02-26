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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.openejb;

import org.apache.openejb.util.Enumerator;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;

import java.io.Closeable;
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
        } catch (Exception e) {
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
    protected Enumeration<URL> findResources(final String name) throws IOException {
        final ArchivePath path = ArchivePaths.create(prefix + name);
        final Node node = archive.get(path);
        if (node != null) {
            return new Enumerator(Arrays.asList(new URL(null, "archive:" + archive.getName() + "/" + name, new ArchiveStreamHandler())));
        }
        return super.findResources(name);
    }

    @Override
    protected URL findResource(String name) {
        ArchivePath path = ArchivePaths.create(prefix + name);
        Node node = archive.get(path);
        if (node == null) {
            path = ArchivePaths.create(name);
            node = archive.get(path);
        }

        if (node != null) {
            try {
                return new URL(null, "archive:" + archive.getName() + "/" + name, new ArchiveStreamHandler());
            } catch (MalformedURLException e) {
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
            return new URLConnection(u) {
                @Override
                public void connect() throws IOException {
                    // no-op
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    final String arName = key(url);

                    String path = prefixes.get(arName) + path(arName, url);
                    Node node = archives.get(arName).get(path);
                    if (node == null) {
                        path = path(arName, url);
                        node = archives.get(arName).get(path);
                    }

                    final Asset asset = node.getAsset();
                    final InputStream input = asset.openStream();
                    final Collection<Closeable> c = closeables.get(arName);
                    c.add(input);
                    return input;

                }
            };
        }

        private static String path(final String arName, final URL url) {
            final String p = url.getPath();
            String out = p.substring(arName.length(), p.length());
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
            int endIndex = p.indexOf('/');
            if (endIndex >= 0) {
                return p.substring(0, endIndex);
            }
            return p;
        }
    }

    public void close() {
        ArchiveStreamHandler.reset(archive.getName());
        for (Closeable cl : closeables) {
            try {
                cl.close();
            } catch (IOException e) {
                // no-op
            }
        }
    }

    // to let frameworks using TCCL use the archive directly
    public Archive<?> getArchive() {
        return archive;
    }
}
