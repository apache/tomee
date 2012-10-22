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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;

public class SWClassLoader extends ClassLoader {
    private final Archive<?> archive;
    private final String prefix;
    private final Collection<Closeable> closeables = new ArrayList<Closeable>();

    public SWClassLoader(final String prefix, final ClassLoader parent, final Archive<?> ar) {
        super(parent);
        this.prefix = prefix;
        this.archive = ar;
    }

    @Override
    protected Enumeration<URL> findResources(final String name) throws IOException {
        final ArchivePath path = ArchivePaths.create(prefix + name);
        final Node node = archive.get(path);
        if (node != null) {
            return new Enumerator(Arrays.asList(new URL(null, "archive:" + archive.getName() + "/", new ArchiveStreamHandler(node, closeables))));
        }
        return super.findResources(name);
    }

    @Override
    protected URL findResource(String name) {
        final ArchivePath path = ArchivePaths.create(prefix + name);
        final Node node = archive.get(path);
        if (node != null) {
            try {
                return new URL(null, "archive:" + archive.getName() + "/", new ArchiveStreamHandler(node, closeables));
            } catch (MalformedURLException e) {
                // no-op: let reuse parent method
            }
        }
        return super.findResource(name);
    }

    private static class ArchiveStreamHandler extends URLStreamHandler {
        private final Node node;
        private final Collection<Closeable> closeables;

        private ArchiveStreamHandler(final Node node, final Collection<Closeable> closeables) {
            this.node = node;
            this.closeables = closeables;
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
                    final Asset asset = node.getAsset();
                    final InputStream input = asset.openStream();
                    synchronized (closeables) {
                        closeables.add(input);
                    }
                    return input;

                }
            };
        }
    }

    public void close() {
        for (Closeable cl : closeables) {
            try {
                cl.close();
            } catch (IOException e) {
                // no-op
            }
        }
    }
}
