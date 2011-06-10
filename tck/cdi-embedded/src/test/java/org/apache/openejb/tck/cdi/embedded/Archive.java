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
package org.apache.openejb.tck.cdi.embedded;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static junit.framework.Assert.assertNotNull;

/**
 * @version $Rev$ $Date$
 */
public class Archive {

    private final String name;

    private final InputStream in;


    public Archive(Iterable<URL> urls, Iterable<Class<?>> classes) {

        try {
            ClassLoader loader = Archive.class.getClassLoader();

            // Create the ZIP file
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(byteArrayOutputStream));

            this.name = name(classes);

            for (Class clazz : classes) {

                String name = clazz.getName().replace('.', File.separatorChar) + ".class";

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

            for (final URL url : urls) {

                final String name = "META-INF/" + new File(url.getFile()).getName();

                out.putNextEntry(new ZipEntry(name));

                final InputStream in = new BufferedInputStream(url.openStream());

                int i = -1;
                while ((i = in.read()) != -1) {
                    out.write(i);
                }

                in.close();
            }

            // Complete the ZIP file
            out.close();

            this.in = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public InputStream getIn() {
        return in;
    }

    private String name(Iterable<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (clazz.getName().endsWith("AbstractJSR299Test")) continue;
            if (clazz.getName().endsWith("Test")) {
                return clazz.getName() + ".jar";
            }
        }

        return "test-archive.jar";
    }
}
