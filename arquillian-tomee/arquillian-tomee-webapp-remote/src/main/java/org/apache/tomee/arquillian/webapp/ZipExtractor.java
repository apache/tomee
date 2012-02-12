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

package org.apache.tomee.arquillian.webapp;

import java.io. *;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractor {

    public static void unzip(File source, File targetDirectory) {
        OutputStream os = null;
        ZipInputStream is = null;

        try {
            is = new ZipInputStream(new FileInputStream(source));
            ZipEntry entry;

            while ((entry = is.getNextEntry()) != null) {
                String name = entry.getName();
                File file = new File(targetDirectory, name);

                if (name.endsWith("/")) {
                    file.mkdir();
                } else {
                    file.createNewFile();

                    int bytesRead;
                    byte data[] = new byte[8192];

                    os = new FileOutputStream(file);
                    while ((bytesRead = is.read(data)) != -1) {
                        os.write(data, 0, bytesRead);
                    }

                    is.closeEntry();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }

            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                }
            }

        }
    }
}
