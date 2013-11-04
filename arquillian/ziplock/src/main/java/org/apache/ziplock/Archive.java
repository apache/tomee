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
package org.apache.ziplock;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * @version $Revision$ $Date$
 */
public class Archive {

    final Map<String, String> manifest = new HashMap<String, String>();
    final Map<String, byte[]> entries = new HashMap<String, byte[]>();

    public static Archive archive() {
        return new Archive();
    }

    public Archive manifest(String key, Object value) {
        manifest.put(key, value.toString());
        return this;
    }

    public Archive manifest(String key, Class value) {
        manifest.put(key, value.getName());
        return this;
    }

    public Archive copyTo(String path, File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                copyTo(path + "/" + child.getName(), child);
            }
        } else {
            try {
                entries.put(path, IO.readBytes(file));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return this;
    }

    public Archive addTo(String path, Class<?> clazz) {
        try {
            final String name = clazz.getName().replace('.', '/') + ".class";

            final URL resource = this.getClass().getClassLoader().getResource(name);

            final InputStream from = new BufferedInputStream(resource.openStream());
            final ByteArrayOutputStream to = new ByteArrayOutputStream();

            final byte[] buffer = new byte[1024];
            int length;
            while ((length = from.read(buffer)) != -1) {
                to.write(buffer, 0, length);
            }
            to.flush();

            if (path != null && path.length() != 0) {
                entries.put(path + "/" + name, to.toByteArray());
            } else {
                entries.put(name, to.toByteArray());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return this;
    }


    public Archive add(Class<?> clazz) {
        return addTo(null, clazz);
    }

    public File toJar() throws IOException {
        return toJar("archive-", ".jar");
    }

    public File toJar(final String prefix, final String suffix) throws IOException {
        final File file = File.createTempFile(prefix, suffix);
        file.deleteOnExit();

        // Create the ZIP file
        final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

        for (Map.Entry<String, byte[]> entry : entries().entrySet()) {
            out.putNextEntry(new ZipEntry(entry.getKey()));
            out.write(entry.getValue());
        }

        // Complete the ZIP file
        out.close();
        return file;
    }

    public File asJar() {
        try {
            return toJar();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File asJar(final String prefix, final String suffix) {
        try {
            return toJar(prefix, suffix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File toDir() throws IOException {

        File classpath = Files.tmpdir();

        for (Map.Entry<String, byte[]> entry : entries().entrySet()) {

            final String key = entry.getKey().replace('/', File.separatorChar);

            final File file = new File(classpath, key);

            File d = file.getParentFile();

            if (!d.exists()) {
                if (!d.mkdirs()) {
                    throw new IllegalStateException("Cannot Make Directory: "+d.getAbsolutePath());
                }
            }

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));

            out.write(entry.getValue());

            out.close();
        }

        return classpath;
    }

    public File asDir() {
        try {
            return toDir();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HashMap<String, byte[]> entries() {
        final HashMap<String, byte[]> entries = new HashMap<String, byte[]>(this.entries);
        entries.put("META-INF/MANIFEST.MF", buildManifest().getBytes());
        return entries;
    }

    private String buildManifest() {
        return Join.join("\r\n", new Join.NameCallback<Map.Entry<String, String>>() {
            @Override
            public String getName(Map.Entry<String, String> entry) {
                return entry.getKey() + ": " + entry.getValue();
            }
        }, manifest.entrySet());
    }

}
