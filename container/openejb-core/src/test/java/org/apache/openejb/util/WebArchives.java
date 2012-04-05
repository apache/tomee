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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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


/**
 * @version $Rev$ $Date$
 */
public class WebArchives {

    public static File jarArchive(Class... classes) throws IOException {
        return jarArchive(new HashMap<String, String>(), "temp", classes);
    }


    public static File jarArchive(Map<String, String> entries, String archiveNamePrefix, Class... classes) throws IOException {

        ClassLoader loader = WebArchives.class.getClassLoader();

        File classpath = File.createTempFile(archiveNamePrefix, ".war");

        // Create the ZIP file
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(classpath)));

        for (Class clazz : classes) {
            String name = clazz.getName().replace('.', File.separatorChar) + ".class";

            URL resource = loader.getResource(name);
            assertNotNull(resource);

            // Add ZIP entry to output stream.
            out.putNextEntry(new ZipEntry("WEB-INF/classes/" + name));

            InputStream in = new BufferedInputStream(resource.openStream());

            int i = -1;
            while ((i = in.read()) != -1) {
                out.write(i);
            }

            // Complete the entry
            in.close();
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

    @WebServlet(name = "manager servlet", urlPatterns = "/")
    public static class Foo extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setHeader("Content-Type", "text/html");
            Debug.Trace.report(resp.getOutputStream());
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(jarArchive(Foo.class));
    }
}