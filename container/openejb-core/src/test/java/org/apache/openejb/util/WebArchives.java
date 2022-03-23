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
package org.apache.openejb.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import static org.junit.Assert.assertNotNull;


/**
 * @version $Rev$ $Date$
 */
public class WebArchives {

    public static File warArchive(final Class... classes) throws IOException {
        return warArchive(new HashMap<>(), "temp", classes);
    }


    public static File warArchive(final Map<String, String> entries, final String archiveNamePrefix, final Class... classes) throws IOException {

        final ClassLoader loader = WebArchives.class.getClassLoader();

        File classpath;
        try {
            classpath = File.createTempFile(archiveNamePrefix, ".war");
        } catch (final Throwable e) {
            final File tmp = new File("tmp");
            if (!tmp.exists() && !tmp.mkdirs()) {
                throw new IOException("Failed to create local tmp directory: " + tmp.getAbsolutePath());
            }

            classpath = File.createTempFile(archiveNamePrefix, ".war", tmp);
        }

        // Create the ZIP file
        final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(classpath)));

        for (final Class clazz : classes) {
            final String name = clazz.getName().replace('.', File.separatorChar) + ".class";

            final URL resource = loader.getResource(name);
            assertNotNull(resource);

            // Add ZIP entry to output stream.
            out.putNextEntry(new ZipEntry("WEB-INF/classes/" + name));

            final InputStream in = new BufferedInputStream(resource.openStream());

            int i;
            while ((i = in.read()) != -1) {
                out.write(i);
            }

            // Complete the entry
            in.close();
            out.closeEntry();
        }

        for (final Map.Entry<String, String> entry : entries.entrySet()) {

            out.putNextEntry(new ZipEntry(entry.getKey()));

            out.write(entry.getValue().getBytes());
        }

        // Complete the ZIP file
        out.close();
        return classpath;
    }

    @WebServlet(name = "manager servlet", urlPatterns = "/")
    public static class Foo extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            resp.setHeader("Content-Type", "text/html");
            Debug.Trace.report(resp.getOutputStream());

        }
    }

    public static void main(final String[] args) throws IOException {
        System.out.println(warArchive(Foo.class));
    }
}