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

package org.apache.openejb.tck.impl;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;

public class AbstractContainers {
    protected static final String tmpDir = System.getProperty("java.io.tmpdir");

    protected void writeToFile(File file, InputStream archive) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try {
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = archive.read(buffer)) > -1) {
                fos.write(buffer, 0, bytesRead);
            }
            Util.close(fos);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static void delete(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                delete(f);
            }
        }
        if (!file.delete()) {
            file.deleteOnExit();
        }
    }

    protected static final class Util {
        static void close(Closeable closeable) throws IOException {
            if (closeable == null)
                return;
            try {
                if (closeable instanceof Flushable) {
                    ((Flushable) closeable).flush();
                }
            } catch (IOException e) {
                // no-op
            }
            try {
                closeable.close();
            } catch (IOException e) {
                // no-op
            }
        }
    }
}
