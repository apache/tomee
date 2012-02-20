/**
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
package org.apache.openejb.arquillian.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev: 1157006 $ $Date: 2011-08-12 01:23:04 -0700 (Fri, 12 Aug 2011) $
 */
public class Files {

    public static File createTempDir() throws IOException {
        return createTempDir("tomee", ".conf");
    }

    public static File createTempDir(String prefix, String suffix) throws IOException {
        File tempDir = File.createTempFile(prefix, suffix);
        tempDir.delete();
        tempDir.mkdirs();
        deleteOnExit(tempDir);
        return tempDir;
    }
    
    private Files() {
        // no-op
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

    public static void mkdir(File dir) {
        if (dir.exists()) return;
        if (!dir.mkdirs()) {
            throw new IllegalStateException("cannot make directory: " + dir.getAbsolutePath());
        }
    }

    public static void writable(File file) {
        if (!file.canWrite()) {
            throw new IllegalStateException("Not writable: " + file.getAbsolutePath());
        }
    }

    public static void readable(File file) {
        if (!file.canRead()) {
            throw new IllegalStateException("Not readable: " + file.getAbsolutePath());
        }
    }

    public static void assertDir(File file) {
        if (!file.isDirectory()) {
            throw new IllegalStateException("Not a directory: " + file.getAbsolutePath());
        }
    }

    public static void assertFile(File file) {
        if (!file.isFile()) {
            throw new IllegalStateException("Not a file: " + file.getAbsolutePath());
        }
    }
}
