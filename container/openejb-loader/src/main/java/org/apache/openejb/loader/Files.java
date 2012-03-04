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
package org.apache.openejb.loader;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
                return pattern.matcher(file.getName()).matches();
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

    public static File exists(File file, String s) {
        if (!file.exists()) throw new RuntimeException(s + " does not exist: " + file.getAbsolutePath());
        return file;
    }

    public static File exists(File file) {
        if (!file.exists()) throw new RuntimeException("Does not exist: " + file.getAbsolutePath());
        return file;
    }

    public static File dir(File file) {
        if (!file.isDirectory()) throw new RuntimeException("Not a directory: " + file.getAbsolutePath());
        return file;
    }

    public static File file(File file) {
        exists(file);
        if (!file.isFile()) throw new RuntimeException("Not a file: " + file.getAbsolutePath());
        return file;
    }

    public static File writable(File file) {
        if (!file.canWrite()) throw new RuntimeException("Not writable: " + file.getAbsolutePath());
        return file;
    }

    public static File readable(File file) {
        if (!file.canRead()) throw new RuntimeException("Not readable: " + file.getAbsolutePath());
        return file;
    }

    public static File readableFile(File file) {
        return readable(file(file));
    }

    public static File mkdir(File file) {
        if (file.exists()) return file;
        if (!file.mkdirs()) throw new RuntimeException("Cannot mkdir: " + file.getAbsolutePath());
        return file;
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

    public static File mkparent(File file) {
        mkdirs(file.getParentFile());
        return file;
    }

    public static File mkdirs(File file) {

        if (!file.exists()) {

            if (!file.mkdirs()) throw new RuntimeException("Cannot mkdirs: " + file.getAbsolutePath());

            return file;
        }

        return dir(file);
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

    public static File select(File dir, String pattern) {
        final List<File> matches = collect(dir, pattern);
        if (matches.size() == 0) throw new IllegalStateException(String.format("Missing '%s'", pattern));
        if (matches.size() > 1) throw new IllegalStateException(String.format("Too many found '%s': %s", pattern, join(", ", matches)));
        return matches.get(0);
    }

    private static String join(String delimiter, Collection<File> collection) {
        if (collection.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (File obj : collection) {
            sb.append(obj.getName()).append(delimiter);
        }
        return sb.substring(0, sb.length() - delimiter.length());
    }

}
