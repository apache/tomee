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

    public static File path(final String... parts) {
        File dir = null;
        for (final String part : parts) {
            if (dir == null) {
                dir = new File(part);
            } else {
                dir = new File(dir, part);
            }
        }

        return null != dir ? dir.getAbsoluteFile() : dir;
    }

    public static File path(final File dir, final String... parts) {
        File base = dir;
        int idx = 0;
        if (parts.length >= 1) {
            final File partFile = new File(parts[0]);
            if (partFile.exists() && partFile.isAbsolute()) {
                base = partFile;
                idx = 1;
            }
        }

        for (int i = idx; i < parts.length; i++) {
            base = new File(base, parts[i]);
        }

        return base.getAbsoluteFile();
    }

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
        if (filter.accept(dir)) accepted.add(dir);

        final File[] files = dir.listFiles();
        if (files != null) for (final File file : files) {
            accepted.addAll(collect(file, filter));
        }

        return accepted;
    }

    public static File exists(final File file, final String s) {
        if (!file.exists()) throw new FileDoesNotExistException(s + " does not exist: " + file.getAbsolutePath());
        return file;
    }

    public static File exists(final File file) {
        if (!file.exists()) throw new FileDoesNotExistException("Does not exist: " + file.getAbsolutePath());
        return file;
    }

    public static File dir(final File file) {
        if (!file.isDirectory()) throw new FileRuntimeException("Not a directory: " + file.getAbsolutePath());
        return file;
    }

    public static File file(final File file) {
        exists(file);
        if (!file.isFile()) throw new FileRuntimeException("Not a file: " + file.getAbsolutePath());
        return file;
    }

    public static File notHidden(final File file) {
        exists(file);
        if (file.isHidden()) throw new FileRuntimeException("File is hidden: " + file.getAbsolutePath());
        return file;
    }

    public static File writable(final File file) {
        if (!file.canWrite()) throw new FileRuntimeException("Not writable: " + file.getAbsolutePath());
        return file;
    }

    public static File readable(final File file) {
        if (!file.canRead()) throw new FileRuntimeException("Not readable: " + file.getAbsolutePath());
        return file;
    }

    public static File readableFile(final File file) {
        return readable(file(file));
    }

    public static File mkdir(final File file) {
        if (file.exists()) return file;
        if (!file.mkdirs()) throw new FileRuntimeException("Cannot mkdir: " + file.getAbsolutePath());
        return file;
    }

    public static File mkdir(final File file, final String name) {
        return mkdir(new File(file, name));
    }

    public static File tmpdir() {
        try {
            File file = null;
            try {
                file = File.createTempFile("temp", "dir");
            } catch (Throwable e) {
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

        } catch (IOException e) {
            throw new FileRuntimeException(e);
        }
    }

    public static File mkparent(final File file) {
        mkdirs(file.getParentFile());
        return file;
    }

    public static File mkdirs(final File file) {

        if (!file.exists()) {

            if (!file.mkdirs()) throw new FileRuntimeException("Cannot mkdirs: " + file.getAbsolutePath());

            return file;
        }

        return dir(file);
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
                    delete();
                }
            };
            try {
                Runtime.getRuntime().addShutdownHook(deleteShutdownHook);
            } catch (Throwable e) {
                //Ignore
            }
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
    }

    public static void deleteOnExit(final File file) {
        delete.add(file.getAbsolutePath());
        flagForDeleteOnExit(file);
    }

    public static void flagForDeleteOnExit(final File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                final File[] files = file.listFiles();
                if (null != files) {
                    for (final File f : files) {
                        flagForDeleteOnExit(f);
                    }
                }
            }

            try {
                file.deleteOnExit();
            } catch (SecurityException e) {
                //Ignore
            }
        }
    }

    private static void delete() {
        for (final String path : delete) {
            delete(new File(path));
        }
    }

    public static void delete(final File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                final File[] files = file.listFiles();
                if (null != files) {
                    for (final File f : files) {
                        delete(f);
                    }
                }
            }
            try {
                if (!file.delete()) {
                    file.deleteOnExit();
                }
            } catch (Throwable e) {
                //Ignore
            }
        }
    }

    public static void remove(final File file) {
        if (file == null) return;
        if (!file.exists()) return;

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

    public static File select(final File dir, final String pattern) {
        final List<File> matches = collect(dir, pattern);
        if (matches.size() == 0) throw new IllegalStateException(String.format("Missing '%s'", pattern));
        if (matches.size() > 1)
            throw new IllegalStateException(String.format("Too many found '%s': %s", pattern, join(", ", matches)));
        return matches.get(0);
    }

    private static String join(final String delimiter, final Collection<File> collection) {
        if (collection.size() == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (final File obj : collection) {
            sb.append(obj.getName()).append(delimiter);
        }
        return sb.substring(0, sb.length() - delimiter.length());
    }

    public static class FileRuntimeException extends RuntimeException {
        public FileRuntimeException(final String str) {
            super(str);
        }

        public FileRuntimeException(final Exception e) {
            super(e);
        }
    }

    public static class FileDoesNotExistException extends FileRuntimeException {
        public FileDoesNotExistException(final String str) {
            super(str);
        }

        public FileDoesNotExistException(final Exception e) {
            super(e);
        }
    }

}
