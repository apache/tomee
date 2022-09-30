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
package org.apache.tomee.server.composer;

import org.tomitribe.util.Files;
import org.tomitribe.util.IO;
import org.tomitribe.util.Join;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
        return add(name, () -> readBytes(content));
    }

    public static byte[] readBytes(final File content) {
        try {
            return IO.readBytes(content);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static byte[] readBytes(final URL content) {
        try {
            return IO.readBytes(content);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Archive add(final String name, final URL content) throws IOException {
        return add(name, IO.readBytes(content));
    }

    public Archive add(final Class<?> clazz) {
        try {
            final String name = clazz.getName().replace('.', '/') + ".class";

            final URL resource = this.getClass().getClassLoader().getResource(name);

            if (resource == null) throw new IllegalStateException("Cannot find class file for " + clazz.getName());

            return add(name, resource);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Archive add(final String name, final Properties properties) {
        return add(name, () -> {
            try {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                properties.store(out,"");
                return out.toByteArray();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public Archive addDir(final File dir) {
        try {

            addDir(null, dir);

        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }

        return this;
    }

    private void addDir(final String path, final File dir) throws IOException {
        for (final File file : dir.listFiles()) {

            final String childPath = (path != null) ? path + "/" + file.getName() : file.getName();

            if (file.isFile()) {
                entries.put(childPath, () -> readBytes(file));
            } else {
                addDir(childPath, file);
            }
        }
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

    public File toJar() throws IOException {
        final File file = File.createTempFile("archive-", ".jar");
        file.deleteOnExit();

        return toJar(file);
    }

    public File toJar(final File file) throws IOException {
        // Create the ZIP file
        final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

        for (final Map.Entry<String, Supplier<byte[]>> entry : entries().entrySet()) {
            out.putNextEntry(new ZipEntry(entry.getKey()));
            out.write(entry.getValue().get());
        }

        // Complete the ZIP file
        out.close();
        return file;
    }

    public File asJar() {
        try {
            return toJar();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File toDir() throws IOException {

        final File classpath = Files.tmpdir();

        toDir(classpath);

        return classpath;
    }

    public void toDir(final File dir) throws IOException {
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

    public File asDir() {
        try {
            return toDir();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HashMap<String, Supplier<byte[]>> entries() {
        final HashMap<String, Supplier<byte[]>> entries = new HashMap<>(this.entries);
        entries.put("META-INF/MANIFEST.MF", buildManifest()::getBytes);
        return entries;
    }

    private String buildManifest() {
        return Join.join("\r\n", entry -> entry.getKey() + ": " + entry.getValue(), manifest.entrySet());
    }

}