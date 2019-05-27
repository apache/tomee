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
package org.apache.openejb.maven.plugin.customizer.monkey.index;

import org.apache.openejb.loader.JarLocation;
import org.apache.openejb.maven.plugin.customizer.monkey.classloader.ClassLoaderFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import static java.util.Arrays.asList;

/**
 * The type File indexer.
 */
public class FileIndexer {
    private final ClassLoader loader;
    private final File binaries;
    private final String binaryRoot;
    private final String ignore;

    private final ClassLoaderFactory loaderFactory = new ClassLoaderFactory();
    private final Map<File, List<Item>> index = new TreeMap<>();
    private final List<String> filesToRemove;

    /**
     * Instantiates a new File indexer.
     *
     * @param base               the base
     * @param patchedFilesFolder the patched files folder
     * @param configuration      the configuration
     * @param ignore             the ignore
     */
    public FileIndexer(final File base, final File patchedFilesFolder, final Properties configuration, final String ignore) {
        this.binaries = patchedFilesFolder;
        try {
            final String path = patchedFilesFolder.getCanonicalFile().getAbsolutePath();
            this.binaryRoot = path + (path.endsWith(File.separator) ? "" : File.separator);
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }

        final File libs = new File(base, "lib");
        if (!libs.isDirectory()) {
            throw new IllegalArgumentException("lib folder not found");
        }
        this.loader = loaderFactory.create(libs);

        final String toRemove = configuration.getProperty("remove");
        this.filesToRemove = toRemove == null ? Collections.<String>emptyList() : new ArrayList<>(asList(toRemove.split(" *, *")));

        this.ignore = ignore;
    }

    /**
     * Gets index.
     *
     * @return the index
     */
    public Map<File, List<Item>> getIndex() {
        return index;
    }

    /**
     * Index file indexer.
     *
     * @return the file indexer
     */
    public FileIndexer index() {
        if (!index.isEmpty()) {
            return this;
        }

        try {
            doIndex(binaries);
            for (final String toRemove : filesToRemove) {
                try {
                    addItem(null, Item.Action.REMOVE, toRemove);
                } catch (final IOException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        } finally {
            loaderFactory.release(loader);
        }
        return this;
    }

    /**
     * Dump file indexer.
     *
     * @param out the out
     * @return the file indexer
     */
    public FileIndexer dump(final PrintStream out) {
        out.println("Index:");
        for (final Map.Entry<File, List<Item>> items : index.entrySet()) {
            Collections.sort(items.getValue());
            out.println();
            out.println("  Location: " + items.getKey());
            for (final Item i : items.getValue()) {
                out.println("    - " + i.getPath());
            }
        }
        return this;
    }


    private void doIndex(final File binaries) {
        if (binaries.isFile()) {
            if (!ignore.equals(binaries.getName())) {
                doIndexLeaf(binaries);
            }
        } else {
            final File[] children = binaries.listFiles();
            if (children == null) {
                return;
            }
            for (final File file : children) {
                doIndex(file);
            }
        }
    }


    private void doIndexLeaf(final File file) {
        try {
            final String relative = file.getCanonicalFile().getAbsolutePath().substring(binaryRoot.length());
            final String resource = relative.replace(File.separatorChar, '/');
            addItem(file, Item.Action.ADD_OR_UPDATE, resource);
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }


    private void addItem(final File file, final Item.Action action, final String resource) throws IOException {
        final File jar = JarLocation.jarFromResource(loader, resource).getCanonicalFile();
        List<Item> list = index.get(jar);
        if (list == null) {
            list = new ArrayList<>();
            index.put(jar, list);
        }
        list.add(new Item(resource, file, action));
    }
}
