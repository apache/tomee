/**
 * Licensed to the Apache Software Foundation (ASF) under one or
 * more
 * contributor license agreements.  See the NOTICE file
 * distributed with
 * this work for additional information regarding copyright
 * ownership.
 * The ASF licenses this file to You under the Apache License,
 * Version 2.0
 * (the "License"); you may not use this file except in
 * compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software
 *  distributed under the License is distributed on an "AS IS"
 *  BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing
 *  permissions and
 *  limitations under the License.
 */
package org.apache.openejb.tck.impl;

import org.apache.openejb.tck.util.ZipUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 * @version $Rev$ $Date$
 */
public class Archive {
    private final String name;

    private final InputStream in;


    public Archive(Iterable<URL> urls, Iterable<Class<?>>
            classes) {

        try {
            ClassLoader loader = Archive.class.getClassLoader();

            // Create the ZIP file
            final ByteArrayOutputStream byteArrayOutputStream =
                    new ByteArrayOutputStream();

            final ZipOutputStream out = new ZipOutputStream(new
                    BufferedOutputStream(byteArrayOutputStream));

            this.name = name(classes);

            for (Class clazz : classes) {

                String name = clazz.getName().replace(".", "/")
                        + ".class";
                // cla  zz.getName().replace('.',
                // File.separatorChar) + ".class"; // shouldn't
                // work under windows

                URL resource = loader.getResource(name);
                if (resource == null) {
                    String path =
                            clazz.getProtectionDomain().getCodeSource() + "!" + name;
                    try {
                        resource = new URL(path);
                    } catch (MalformedURLException mue) {
                        fail("can't find " + clazz.getName() + "neither from" + name + " nor from" + path);
                    }
                }
                assertNotNull(resource);
                InputStream in = new BufferedInputStream(resource.openStream());
                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(name));
                ZipUtil.copy(in, out);

                // Complete the entry
                out.closeEntry();
            }

            for (final URL url : urls) {

                final String fileName = new
                        File(url.getFile()).getName();
                final String name = "META-INF/" + fileName;

                out.putNextEntry(new ZipEntry(name));

                final InputStream in = new BufferedInputStream(url.openStream());

                ZipUtil.copy(in, out);

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
            if (clazz.getName().endsWith("AbstractJSR299Test"))
                continue;
            if (clazz.getName().endsWith("Test")) {
                return clazz.getName() + ".jar";
            }
        }

        return "test-archive.jar";
    }
}

