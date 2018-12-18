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
package org.apache.openejb.maven.plugin.customizer.monkey.jar;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.openejb.loader.IO;
import org.apache.openejb.maven.plugin.customizer.monkey.index.Item;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * The Jar patcher.
 */
public class JarPatcher {
    /**
     * Patch.
     *
     * @param stream the stream
     * @param tmp    the tmp
     * @param key    the key
     * @param value  the value
     */
    public void patch(final PrintStream stream, final File tmp, final File key, final List<Item> value) {
        stream.println("  " + key);
        if (key.isDirectory()) {
            throw new IllegalArgumentException("Directory not yet supported - shouldn't occur");
        }
        if (!key.getName().endsWith(".jar")) {
            throw new IllegalArgumentException(key + " not a jar - shouldn't occur");
        }
        doJarPatch(stream, tmp, key, value);
    }

    private void doJarPatch(final PrintStream stream, final File tmp, final File jarFile, final List<Item> items) {
        final File exploded = new File(tmp, jarFile.getName() + ".exploded");
        try {
            // explode the jar
            final int method = unjar(exploded, jarFile);

            // patch files
            for (final Item item : items) {
                switch (item.getAction()) {
                    case ADD_OR_UPDATE:
                        replace(stream, jarFile.getName(), exploded, item.getPath(), item.getFile());
                        break;
                    case REMOVE:
                        try {
                            FileUtils.forceDelete(new File(exploded, item.getPath()));
                        } catch (final IOException e) {
                            throw new IllegalStateException(e);
                        }
                        break;
                    default:
                }
            }

            // recreate the jar
            try {
                FileUtils.forceDelete(jarFile);
            } catch (final IOException e) {
                if (!jarFile.renameTo(new File(jarFile.getParentFile(), jarFile.getName() + "_patched"))) {
                    throw new IllegalStateException(e);
                }
            }
            final String name = jarFile.getName();
            jar(method, exploded, new File(jarFile.getParentFile(), jarName(name)));
        } finally {
            try {
                FileUtils.deleteDirectory(exploded);
            } catch (IOException e) {
                // no-op
            }
        }
    }

    private String jarName(final String name) {
        return name.substring(0, name.length() - ".jar".length()) + "tomee-monkey-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".jar";
    }

    private void replace(final PrintStream stream, final String name, final File exploded, final String path, final File file) {
        final File existing = new File(exploded, path);
        if (!existing.isFile()) {
            stream.println("  - No " + path + " in " + name + ", creating it.");
            existing.getParentFile().mkdirs();
        } else {
            stream.println("  - Replacing " + path + " in " + name + " with " + file);

            // try to delete the file first otherwise try to overwrite it directly
            existing.delete();
        }

        OutputStream os = null;
        InputStream is = null;
        try {
            os = new FileOutputStream(existing);
            is = new FileInputStream(file);
            IO.copy(is, os);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        } finally {
            IO.close(os);
            IO.close(is);
        }
    }

    private int unjar(final File exploded, final File from) {
        int method = -1;

        JarArchiveInputStream stream = null;
        try {
            stream = new JarArchiveInputStream(new FileInputStream(from));
            JarArchiveEntry entry;
            while ((entry = stream.getNextJarEntry()) != null) {
                final File archiveEntry = new File(exploded, entry.getName());
                archiveEntry.getParentFile().mkdirs();
                if (entry.isDirectory()) {
                    archiveEntry.mkdir();
                    continue;
                }

                final OutputStream out = new FileOutputStream(archiveEntry);
                IOUtils.copy(stream, out);
                out.close();
                if (method < 0) {
                    method = entry.getMethod();
                }
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            IO.close(stream);
        }

        return method;
    }

    private void jar(final int method, final File exploded, final File target) {
        JarArchiveOutputStream stream = null;
        try {
            stream = new JarArchiveOutputStream(new FileOutputStream(target));
            final String prefix = exploded.getCanonicalFile().getAbsolutePath() + File.separator;
            for (final String f : exploded.list()) {
                jar(method, stream, new File(exploded, f).getCanonicalFile(), prefix);
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            IO.close(stream);
        }
    }

    private void jar(final int method, final JarArchiveOutputStream jar, final File f, final String prefix) throws IOException {
        final String path = f.getPath().replace(prefix, "").replace(File.separator, "/");
        final ZipArchiveEntry zip = new ZipArchiveEntry(f, path);
        zip.setMethod(method);
        final JarArchiveEntry archiveEntry = new JarArchiveEntry(zip);
        jar.putArchiveEntry(archiveEntry);
        if (f.isDirectory()) {
            jar.closeArchiveEntry();
            final File[] files = f.listFiles();
            if (files != null) {
                for (final File child : files) {
                    jar(method, jar, child.getCanonicalFile(), prefix);
                }
            }
        } else {
            final InputStream is = new FileInputStream(f);
            IOUtils.copy(is, jar);
            is.close();
            jar.closeArchiveEntry();
        }
    }
}
