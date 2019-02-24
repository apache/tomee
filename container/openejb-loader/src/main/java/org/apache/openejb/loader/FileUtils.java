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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("PMD.OverrideBothEqualsAndHashcode")
public class FileUtils {

    private File home;

    private FileUtils(final String homeDir, final String defaultDir) {
        this(homeDir, defaultDir, SystemInstance.get().getProperties());
    }

    @SuppressWarnings("UseOfObsoleteCollectionType")
    public FileUtils(final String homeDir, final String defaultDir, final Hashtable env) {

        String homePath = (String) env.get(homeDir);

        if (homePath == null) {
            homePath = (String) env.get(defaultDir);
        }

        if (homePath == null) {
            homePath = System.getProperty("user.dir");
        }

        home = new File(homePath);
        if (!home.exists() || (home.exists() && !home.isDirectory())) {
            homePath = System.getProperty("user.dir");
            home = new File(homePath);
        }

        try {
            home = home.getCanonicalFile();
        } catch (final IOException e) {
            // this shouldn't happen, but let's get absolute file
            home = home.getAbsoluteFile();
        }
    }

    public File getDirectory(final String path) throws IOException {
        return getDirectory(path, false);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof FileUtils)) {
            return false;
        }
        final FileUtils that = (FileUtils) obj;
        return this.getDirectory().equals(that.getDirectory());
    }

    public File getDirectory(final String path, final boolean create) throws IOException {
        File dir = new File(home, path);
        dir = dir.getCanonicalFile();

        if (!dir.exists() && create) {
            try {
                if (!dir.mkdirs()) {
                    throw new IOException("Cannot create the directory " + dir.getPath());
                }
            } catch (final SecurityException e) {
                throw new IOException(
                    "Permission denied: Cannot create the directory " + dir.getPath() + " : " + e.getMessage());
            }
        } else if (dir.exists() && !dir.isDirectory()) {
            throw new IOException("The path specified is not a valid directory: " + dir.getPath());
        }

        return dir;
    }

    public File getDirectory() {
        return home;
    }

    public void setDirectory(final File dir) {
        this.home = dir;
    }

    public File getFile(final String path) throws IOException {
        return getFile(path, true);
    }

    public File getFile(final String path, final boolean validate) throws IOException {
        File file = new File(path);

        if (!file.isAbsolute()) {
            file = new File(home, path);
        }

        if (validate && !file.exists()) {
            throw new FileNotFoundException("The path specified is not a valid file: " + file.getPath());
        } else if (validate && file.isDirectory()) {
            throw new FileNotFoundException("The path specified is a directory, not a file: " + file.getPath());
        }

        return file;
    }

    public static File createTempDirectory(final String pathPrefix) throws IOException {
        for (int maxAttempts = 100; maxAttempts > 0; --maxAttempts) {

            final String path = pathPrefix + ThreadLocalRandom.current().nextLong();
            final File tmpDir = new File(path);

            if (!tmpDir.exists() && tmpDir.mkdirs()) {
                return tmpDir;
            }
        }
        throw new IOException("Cannot create temporary directory at: " + pathPrefix);
    }

    public static File createTempDirectory() throws IOException {
        final String prefix = System.getProperty("java.io.tmpdir", File.separator + "tmp") + File.separator + "openejb";
        return createTempDirectory(prefix);
    }

}
