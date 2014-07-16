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
package org.apache.openejb.arquillian.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @version $Rev: 1157006 $ $Date: 2011-08-12 01:23:04 -0700 (Fri, 12 Aug 2011) $
 */
public final class Files {
    private static final Logger LOGGER = Logger.getLogger(Files.class.getName());

    public static File path(final String... parts) {
        File dir = null;
        for (final String part : parts) {
            if (dir == null) {
                dir = new File(part);
            } else {
                dir = new File(dir, part);
            }
        }

        return dir;
    }

    public static File path(File dir, final String... parts) {
        for (final String part : parts) {
            dir = new File(dir, part);
        }

        return dir;
    }

    public static File createTempDir() throws IOException {
        return createTempDir("tomee", ".conf");
    }

    public static File createTempDir(final String prefix, final String suffix) throws IOException {
        File tempDir;
        try {
            tempDir = File.createTempFile(prefix, suffix);
        } catch (final Throwable e) {
            final File tmp = new File("tmp");
            if (!tmp.exists() && !tmp.mkdirs()) {
                throw new IOException("Failed to create local tmp directory: " + tmp.getAbsolutePath());
            }
            tempDir = File.createTempFile(prefix, suffix, tmp);
        }
        if (!tempDir.delete() && tempDir.mkdirs()) {
            throw new IOException("Failed to create temp directory: " + tempDir.getAbsolutePath());
        }
        deleteOnExit(tempDir);
        return tempDir;
    }

    private Files() {
        // no-op
    }

    // Shutdown hook for recursive delete on tmp directories
    static final List<String> delete = new ArrayList<String>();

    private static volatile boolean shutdown;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown = true;
                delete();
            }
        });
    }

    public static void deleteOnExit(final File file) {
        delete.add(file.getAbsolutePath());
    }

    private static void delete() {
        for (final String path : delete) {
            delete(new File(path));
        }
    }

    public static void delete(final File file) {
        if (file.exists()) {
            tryTodelete(file);

            if (!file.delete() && !shutdown) {
                file.deleteOnExit();
            } else if (!willBeDelete(file)) {
                LOGGER.severe("can't delete " + file.getAbsolutePath());
            }
        }
    }

    private static boolean willBeDelete(final File file) {
        File current = file;
        while (current != null) {
            if (delete.contains(current.getAbsolutePath())) {
                return true;
            }
            current = current.getParentFile();
        }
        return false;
    }

    public static void mkdir(final File dir) {
        if (dir.exists()) {
            return;
        }
        if (!dir.mkdirs()) {
            throw new IllegalStateException("cannot make directory: " + dir.getAbsolutePath());
        }
    }

    public static void writable(final File file) {
        if (!file.canWrite()) {
            throw new IllegalStateException("Not writable: " + file.getAbsolutePath());
        }
    }

    public static void readable(final File file) {
        if (!file.canRead()) {
            throw new IllegalStateException("Not readable: " + file.getAbsolutePath());
        }
    }

    public static void assertDir(final File file) {
        if (!file.isDirectory()) {
            throw new IllegalStateException("Not a directory: " + file.getAbsolutePath());
        }
    }

    public static void assertFile(final File file) {
        if (!file.isFile()) {
            throw new IllegalStateException("Not a file: " + file.getAbsolutePath());
        }
    }

    public static void tryTodelete(final File file) {
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (files != null) {
                for (final File f : files) {
                    delete(f);
                }
            }
        }
    }
}
