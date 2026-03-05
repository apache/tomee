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
package org.apache.openejb.maven.plugin.customizer.monkey.classloader;

import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The type Class loader factory is used to load classes drom the supplied *.jar and *.zip
 */
public class ClassLoaderFactory {

    /**
     * Create class loader.
     *
     * @param libFolder the lib folder
     * @return the class loader
     */
    public ClassLoader create(final File libFolder) {
        final Collection<URL> urls = new ArrayList<>();
        final File[] children = libFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".jar") || name.endsWith(".zip");
            }
        });
        if (children == null) {
            throw new IllegalArgumentException("No library found in " + libFolder);
        }

        for (final File c : children) {
            try {
                urls.add(c.toURI().toURL());
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }

        return new URLClassLoader(urls.toArray(new URL[urls.size()]), new ClassLoader() {
            @Override
            protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("java.")) {
                    return getSystemClassLoader().loadClass(name);
                }
                throw new ClassNotFoundException(name);
            }

            @Override
            public URL getResource(final String name) {
                return null;
            }
        });
    }

    /**
     * Release.
     *
     * @param loader the loader
     */
    public void release(final ClassLoader loader) {
        if (Closeable.class.isInstance(loader)) { // release files to be able to delete them later
            try {
                Closeable.class.cast(loader).close();
            } catch (final IOException e) {
                // no-op
            }
        }
    }
}
