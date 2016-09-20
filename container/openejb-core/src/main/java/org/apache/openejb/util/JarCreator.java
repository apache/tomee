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

package org.apache.openejb.util;

import org.apache.openejb.loader.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public final class JarCreator {
    private static final int BUFFER_SIZE = 1024;

    private JarCreator() {
        // no-op
    }

    public static void jarDir(final File dir, final File zipName) throws IOException, IllegalArgumentException {
        final String[] entries = dir.list();
        final JarOutputStream out = new JarOutputStream(new FileOutputStream(zipName));

        try {
            String prefix = dir.getAbsolutePath();
            if (!prefix.endsWith(File.separator)) {
                prefix += File.separator;
            }

            for (final String entry : entries) {
                final File f = new File(dir, entry);
                jarFile(out, f, prefix);
            }
        } finally {
            IO.close(out);
        }
    }

    private static void jarFile(final JarOutputStream out, final File f, final String prefix) throws IOException {
        if (f.isDirectory()) {
            final File[] files = f.listFiles();
            if (null != files) {
                for (final File child : files) {
                    jarFile(out, child, prefix);
                }
            }
        } else {
            final String path = f.getPath().replace(prefix, "");

            try (final FileInputStream in = new FileInputStream(f)) {
                final JarEntry entry = new JarEntry(path);
                out.putNextEntry(entry);

                final byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}
