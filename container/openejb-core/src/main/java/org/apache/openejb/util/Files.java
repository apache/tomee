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
package org.apache.openejb.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @version $Rev$ $Date$
 */
public class Files {

    public static File path(String... parts) {
        File dir = null;
        for (String part : parts) {
            if (dir == null) {
                dir = new File(part);
            } else {
                dir = new File(dir, part);
            }
        }

        return dir;
    }

    public static File path(File dir, String... parts) {
        for (String part : parts) {
            dir = new File(dir, part);
        }

        return dir;
    }

    public static List<File> collect(final File dir, final String regex) {
        return collect(dir, Pattern.compile(regex));
    }

    public static List<File> collect(final File dir, final Pattern pattern) {
        return collect(dir, new FileFilter() {
            @Override
            public boolean accept(File file) {
                return pattern.matcher(file.getAbsolutePath()).matches();
            }
        });
    }


    public static List<File> collect(File dir, FileFilter filter) {
        final List<File> accepted = new ArrayList<File>();
        if (filter.accept(dir)) accepted.add(dir);

        final File[] files = dir.listFiles();
        if (files != null) for (File file : files) {
            accepted.addAll(collect(file, filter));
        }

        return accepted;
    }

    public static void exists(File file, String s) {
        if (!file.exists()) throw new RuntimeException(s + " does not exist: " + file.getAbsolutePath());
    }

    public static void dir(File file) {
        if (!file.isDirectory()) throw new RuntimeException("Not a directory: " + file.getAbsolutePath());
    }

    public static void file(File file) {
        if (!file.isFile()) throw new RuntimeException("Not a file: " + file.getAbsolutePath());
    }

    public static void writable(File file) {
        if (!file.canWrite()) throw new RuntimeException("Not writable: " + file.getAbsolutePath());
    }

    public static void readable(File file) {
        if (!file.canRead()) throw new RuntimeException("Not readable: " + file.getAbsolutePath());
    }

    public static void mkdir(File file) {
        if (file.exists()) return;
        if (!file.mkdirs()) throw new RuntimeException("Cannot mkdir: " + file.getAbsolutePath());
    }

    public static File tmpdir() {
        try {
            final File file = File.createTempFile("temp", "dir");
            if (!file.delete()) throw new IllegalStateException("Cannot make temp dir.  Delete failed");
            mkdir(file);
            deleteOnExit(file);
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void mkparent(File file) {
        mkdirs(file.getParentFile());
    }

    public static void mkdirs(File file) {

        if (!file.exists()) {

            assert file.mkdirs() : "mkdirs " + file;

            return;
        }

        assert file.isDirectory() : "not a directory" + file;
    }


    // Shutdown hook for recursive delete on tmp directories
    static final List<String> delete = new ArrayList<String>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                delete();
            }
        });
    }

    public static void deleteOnExit(File file) {
        delete.add(file.getAbsolutePath());
    }

    private static void delete() {
        for (String path : delete) {
            delete(new File(path));
        }
    }

    public static void delete(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    delete(f);
                }
            }

            file.delete();
        }
    }

}
