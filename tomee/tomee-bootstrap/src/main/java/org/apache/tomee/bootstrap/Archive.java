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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.bootstrap;


import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.Join;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.jar.JarEntry;Paths
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Archive {

    private final Map<String, String> manifest = new HashMap<>();
    private final Map<String, Supplier<byte[]>> entries = new HashMap<>();

    public static Archive archive() {
        return new Archive();
    }

    public Archive manifest(final String key, final Object value) {
        manifest.put(key, value.toString());
        return this;
    }

    public Archive manifest(final String key, final Class value) {
        manifest.put(key, value.getName());
        return this;
    }

    public Archive add(final String name, final byte[] bytes) {
        entries.put(name, () -> bytes);
        return this;
    }

    public Archive add(final String name, final Supplier<byte[]> content) {
        entries.put(name, content);
        return this;
    }

    public Archive add(final String name, final String content) {
        return add(name, content::getBytes);
    }

    public Archive add(final String name, final File content) {
        if (content.isDirectory()) {
            return addDir(name, content);
        }
        return add(name, () -> readBytes(content));
    }

    public Archive add(final String name, final Archive archive) {
        this.manifest.putAll(archive.manifest);
        for (final Map.Entry<String, Supplier<byte[]>> entry : archive.entries.entrySet()) {
            this.entries.put(name + "/" + entry.getKey(), entry.getValue());
        }
        return this;
    }

    private static byte[] readBytes(final File content) {
        try {
            return IO.readBytes(content);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Archive add(final String name, final URL content) {
        try {
            return add(name, IO.readBytes(content));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Archive add(final Class<?> clazz) {
        final String name = clazz.getName().replace('.', '/') + ".class";

        final URL resource = this.getClass().getClassLoader().getResource(name);
        if (resource == null) throw new IllegalStateException("Cannot find class file for " + clazz.getName());
        add(name, resource);

        // Add any parent classes needed
        if (!clazz.isAnonymousClass() && clazz.getDeclaringClass() != null) {
            add(clazz.getDeclaringClass());
        }

        // Add any anonymous nested classes
        Stream.of(clazz.getDeclaredClasses())
                .filter(Class::isAnonymousClass)
                .forEach(this::add);

        return this;
    }

    public Archive addDir(final File dir) {
        return addDir(null, dir);
    }

    private Archive addDir(final String path, final File dir) {
        for (final File file : dir.listFiles()) {

            final String childPath = (path != null) ? path + "/" + file.getName() : file.getName();

            if (file.isFile()) {
                entries.put(childPath, () -> readBytes(file));
            } else {
                addDir(childPath, file);
            }
        }
        return this;
    }

    public Archive addJar(final File file) {
        try {
            final JarFile jarFile = new JarFile(file);

            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                final byte[] bytes = IO.readBytes(jarFile.getInputStream(entry));
                this.entries.put(entry.getName(), () -> bytes);
            }

            return this;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File toJar() {
        final File file;
        try {
            file = File.createTempFile("archive-", ".jar");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        file.deleteOnExit();

        return toJar(file);
    }

    public File toJar(final File file) {
        // Create the ZIP file
        try {
            try (final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
                for (final Map.Entry<String, Supplier<byte[]>> entry : entries().entrySet()) {
                    out.putNextEntry(new ZipEntry(entry.getKey()));
                    out.write(entry.getValue().get());
                }
            }
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public File toDir() {

        final File classpath = Files.tmpdir();

        toDir(classpath);

        return classpath;
    }

    public void toDir(final File dir) {
        Files.exists(dir);
        Files.dir(dir);
        Files.writable(dir);

        for (final Map.Entry<String, Supplier<byte[]>> entry : entries().entrySet()) {

            final String key = entry.getKey().replace('/', File.separatorChar);

            final File file = new File(dir, key);

            Files.mkparent(file);

            try {
                IO.copy(entry.getValue().get(), file);
            } catch (Exception e) {
                throw new IllegalStateException("Cannot write entry " + entry.getKey(), e);
            }
        }
    }

    private HashMap<String, Supplier<byte[]>> entries() {
        final HashMap<String, Supplier<byte[]>> entries = new HashMap<>(this.entries);
        if (manifest.size() > 0) {
            entries.put("META-INF/MANIFEST.MF", buildManifest()::getBytes);
        }
        return entries;
    }

    private String buildManifest() {
        return Join.join("\r\n", entry -> entry.getKey() + ": " + entry.getValue(), manifest.entrySet());
    }

}