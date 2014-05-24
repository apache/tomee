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
package org.apache.ziplock;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @version $Revision$ $Date$
 */
public class Files {

    public static List<File> collect(final File dir, final String regex) {
        return collect(dir, Pattern.compile(regex));
    }

    public static List<File> collect(final File dir, final Pattern pattern) {
        return collect(dir, new FileFilter() {
            @Override
            public boolean accept(final File file) {
                return pattern.matcher(file.getName()).matches();
            }
        });
    }


    public static List<File> collect(final File dir, final FileFilter filter) {
        final List<File> accepted = new ArrayList<File>();
        if (filter.accept(dir)) {
            accepted.add(dir);
        }

        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File file : files) {
                accepted.addAll(collect(file, filter));
            }
        }

        return accepted;
    }

    public static void remove(final File file) {
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (files != null) {
                for (final File child : files) {
                    remove(child);
                }
            }
        }
        if (!file.delete()) {
            throw new IllegalStateException("Could not delete file: " + file.getAbsolutePath());
        }
    }


    public static File tmpdir() {
        try {
            File file = null;
            try {
                file = File.createTempFile("temp", "dir");
            } catch (final Throwable e) {
                //Use a local tmp directory
                final File tmp = new File("tmp");
                if (!tmp.exists() && !tmp.mkdirs()) {
                    throw new IOException("Failed to create local tmp directory: " + tmp.getAbsolutePath());
                }

                file = File.createTempFile("temp", "dir", tmp);
            }

            if (!file.delete()) {
                throw new IOException("Failed to create temp dir. Delete failed");
            }

            mkdir(file);
            deleteOnExit(file);

            return file;

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File mkdir(final File file) {
        if (file.exists()) {
            return file;
        }
        if (!file.mkdirs()) {
            throw new RuntimeException("Cannot mkdir: " + file.getAbsolutePath());
        }
        return file;
    }

    // Shutdown hook for recursive delete on tmp directories
    static final List<String> delete = new ArrayList<String>();

    static {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Files.class.getClassLoader());
        try {
            final Thread deleteShutdownHook = new Thread() {
                @Override
                public void run() {
                    for (final String path : delete) {
                        try {
                            remove(new File(path));
                        } catch (final Throwable e) {
                            System.err.println(e.getMessage());
                        }
                    }
                }
            };
            try {
                Runtime.getRuntime().addShutdownHook(deleteShutdownHook);
            } catch (final Throwable e) {
                //Ignore
            }
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
    }

    public static void deleteOnExit(final File file) {
        delete.add(file.getAbsolutePath());
    }

}
