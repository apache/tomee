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
package org.apache.openejb.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @version $Rev$ $Date$
 */
public class Zips {

    public static void unzip(final File zipFile, final File destination) throws IOException {
        unzip(zipFile, destination, false);
    }

    public static void unzip(final File zipFile, final File destination, final boolean noparent) throws IOException {

        Files.dir(destination);
        Files.writable(destination);

        Files.file(zipFile);
        Files.readable(zipFile);

        final InputStream read = IO.read(zipFile);
        try {
            unzip(read, destination, noparent);
        } finally {
            IO.close(read);
        }
    }

    public static void unzip(final InputStream read, final File destination, final boolean noparent) throws IOException {
        try {
            // Open the ZIP file
            final ZipInputStream in = new ZipInputStream(read);

            ZipEntry entry;

            while ((entry = in.getNextEntry()) != null) {
                String path = entry.getName();
                if (noparent) {
                    path = path.replaceFirst("^[^/]+/", "");
                }
                final File file = new File(destination, path);

                if (entry.isDirectory()) {
                    Files.mkdir(file);
                    continue;
                }

                Files.mkdir(file.getParentFile());
                IO.copy(in, file);

                final long lastModified = entry.getTime();
                if (lastModified > 0) {
                    file.setLastModified(lastModified);
                }

            }

            in.close();

        } catch (final IOException e) {
            throw new IOException("Unable to unzip " + read, e);
        }
    }
}
