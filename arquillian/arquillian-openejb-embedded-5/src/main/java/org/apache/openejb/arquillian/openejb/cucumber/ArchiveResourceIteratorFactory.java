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
package org.apache.openejb.arquillian.openejb.cucumber;

import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceIteratorFactory;
import org.apache.openejb.arquillian.openejb.SWClassLoader;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.Node;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class ArchiveResourceIteratorFactory implements ResourceIteratorFactory {
    @Override
    public boolean isFactoryFor(final URL url) {
        return url.getProtocol().startsWith("archive");
    }

    @Override
    public Iterator<Resource> createIterator(final URL url, final String path, final String suffix) {
        return findResources(path, suffix).iterator();
    }

    private Collection<Resource> findResources(final String path, final String suffix) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final Collection<Resource> resources = new ArrayList<Resource>();
        if (SWClassLoader.class.isInstance(loader)) {
            final Collection<Archive<?>> archives = SWClassLoader.class.cast(loader).getArchives();
            final ClassLoader parent = loader.getParent();
            for (final Archive<?> archive : archives) {
                final Map<ArchivePath, Node> content = archive.getContent(new Filter<ArchivePath>() {
                    @Override
                    public boolean include(final ArchivePath object) {
                        final String currentPath = classloaderPath(object);

                        return !(parent != null && parent.getResource(currentPath) != null)
                                && currentPath.startsWith('/' + path) && currentPath.endsWith(suffix);

                    }
                });

                for (final Map.Entry<ArchivePath, Node> entry : content.entrySet()) {
                    resources.add(new SWResource(entry.getKey(), entry.getValue()));
                }
            }
        }
        return resources;
    }

    private static class SWResource implements Resource {
        private final Node node;
        private final String path;

        public SWResource(final ArchivePath key, final Node value) {
            path = classloaderPath(key);
            node = value;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getAbsolutePath() {
            return path;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return node.getAsset().openStream();
        }

        @Override
        public String getClassName(final String extension) {
            return path.replace('/', '.') + extension;
        }
    }

    private static String classloaderPath(final ArchivePath key) {
        return key.get().replace("/WEB-INF/classes/", "");
    }
}

