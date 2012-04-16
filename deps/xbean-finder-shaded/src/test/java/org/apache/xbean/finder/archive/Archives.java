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
package org.apache.xbean.finder.archive;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
* @version $Rev$ $Date$
*/
public class Archives {

    public static File fileArchive(Class[] classes) throws IOException {
        return fileArchive(new HashMap<String, String>(), classes);
    }

    public static File fileArchive(Map<String, String> entries, Class... classes) throws IOException {

        ClassLoader loader = Archives.class.getClassLoader();

        File classpath = File.createTempFile("path with spaces", "classes");

        assertTrue(classpath.delete());
        assertTrue(classpath.mkdirs());


        for (Class clazz : classes) {
            String name = clazz.getName().replace('.', File.separatorChar) + ".class";
            File file = new File(classpath, name);

            File d = file.getParentFile();

            if (!d.exists()) assertTrue(d.getAbsolutePath(), d.mkdirs());

            URL resource = loader.getResource(name);
            assertNotNull(resource);

            InputStream in = new BufferedInputStream(resource.openStream());
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));

            int i = -1;
            while ((i = in.read()) != -1) {
                out.write(i);
            }

            out.close();
            in.close();
        }

        for (Map.Entry<String, String> entry : entries.entrySet()) {

            final String key = entry.getKey().replace('/', File.separatorChar);

            final File file = new File(classpath, key);

            File d = file.getParentFile();

            if (!d.exists()) assertTrue(d.getAbsolutePath(), d.mkdirs());

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));

            out.write(entry.getValue().getBytes());

            out.close();
        }

        return classpath;
    }

    public static File jarArchive(Class... classes) throws IOException {
        return jarArchive(new HashMap<String, String>(), classes);
    }

    public static File jarArchive(Map<String, String> entries, Class... classes) throws IOException {

        ClassLoader loader = Archives.class.getClassLoader();

        File classpath = File.createTempFile("path with spaces", ".jar");

        // Create the ZIP file
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(classpath)));

        for (Class clazz : classes) {
            String name = clazz.getName().replace('.', '/') + ".class";

            URL resource = loader.getResource(name);
            assertNotNull(resource);

            // Add ZIP entry to output stream.
            out.putNextEntry(new ZipEntry(name));

            InputStream in = new BufferedInputStream(resource.openStream());

            int i = -1;
            while ((i = in.read()) != -1) {
                out.write(i);
            }

            // Complete the entry
            out.closeEntry();
        }

        for (Map.Entry<String, String> entry : entries.entrySet()) {

            out.putNextEntry(new ZipEntry(entry.getKey()));

            out.write(entry.getValue().getBytes());
        }

        // Complete the ZIP file
        out.close();
        return classpath;
    }
}
