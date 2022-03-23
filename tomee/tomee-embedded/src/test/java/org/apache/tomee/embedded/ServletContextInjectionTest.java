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
package org.apache.tomee.embedded;

import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.JarLocation;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Assert;
import org.junit.Test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ServletContextInjectionTest {
    @Provider
    @ApplicationScoped
    public static class Filter implements ContainerRequestFilter {
        @Override
        public void filter(final ContainerRequestContext requestContext) throws IOException {

        }

        @Context
        public void setApplication(final Application application) {
            final Instance<Loader> loader = CDI.current().select(Loader.class);
            if (!loader.isUnsatisfied()) {
                loader.iterator().next().useServletContext();
            }
        }
    }

    @ApplicationScoped
    public static class Loader {
        @Inject
        private ServletContext servletContext;

        public void useServletContext() {
            servletContext.getResourceAsStream("/null");
        }
    }

    @Path("path")
    public static class Resource {
        @GET
        public Response get() {
            return Response.ok("ok").build();
        }
    }

    @Test
    public void testWebApp() {
        Container container = null;
        try {
            container = new Container(
                    new Configuration()
                            .http(NetworkUtil.getNextAvailablePort())
                            .property("openejb.container.additional.exclude", "org.apache.tomee.embedded.")
                            .property("openejb.additional.include", "tomee-"))
                    .deployPathsAsWebapp(JarLocation.jarLocation(Resource.class));

            assertEquals("ok", IO.slurp(
                    new URL("http://localhost:" + container.getConfiguration().getHttpPort() + "/api/path")));
        } catch (final Exception e) {
            if (container != null) {
                container.close();
            }
            Assert.fail();
        }
    }

    @Test
    public void testEar() throws Exception {
        final Map<String, Object> loaderContents = new HashMap<String, Object>();
        loaderContents.put("WEB-INF/web.xml", "<web-app><module-name>loader</module-name></web-app>");
        loaderContents.put("WEB-INF/classes/" + Resource.class.getName().replace('.', File.separatorChar) + ".class", Resource.class);
        loaderContents.put("WEB-INF/classes/" + Filter.class.getName().replace('.', File.separatorChar) + ".class", Filter.class);
        loaderContents.put("WEB-INF/classes/" + Loader.class.getName().replace('.', File.separatorChar) + ".class", Loader.class);
        final File loaderApp = jarArchive(loaderContents, "loader");

        final Map<String, Object> webAppContents = new HashMap<String, Object>();
        webAppContents.put("WEB-INF/web.xml", "<web-app><module-name>webapp</module-name></web-app>");
        webAppContents.put("WEB-INF/classes/" + Resource.class.getName().replace('.', File.separatorChar) + ".class", Resource.class);
        final File webapp = jarArchive(webAppContents, "webapp");

        final File appsDir = Files.tmpdir();
        final File ear = new File(appsDir, "app.ear");
        final Map<String, Object> contents = new HashMap<>();
        contents.put("loader.war", loaderApp);
        contents.put("webapp.war", webapp);
        final File file = jarArchive(ear, contents);

        Container container = null;
        try {
            container = new Container(
                    new Configuration()
                            .http(NetworkUtil.getNextAvailablePort())
                            .property("openejb.container.additional.exclude", "org.apache.tomee.embedded.")
                            .property("openejb.additional.include", "tomee-"));
            container.deploy("app.ear", file);

            assertEquals("ok", IO.slurp(
                    new URL("http://localhost:" + container.getConfiguration().getHttpPort() + "/loader/path")));

            assertEquals("ok", IO.slurp(
                    new URL("http://localhost:" + container.getConfiguration().getHttpPort() + "/webapp/path")));
        } catch (final Exception e) {
            Assert.fail();
        } finally {
            if (container != null) {
                container.close();
            }
        }
    }

    private static File jarArchive(final Map<String, ?> entries, final String archiveNamePrefix, final Class... classes) throws IOException {
        File classpath;
        try {
            classpath = File.createTempFile(archiveNamePrefix, ".jar");
        } catch (final Throwable e) {
            final File tmp = new File("tmp");
            if (!tmp.exists() && !tmp.mkdirs()) {
                throw new IOException("Failed to create local tmp directory: " + tmp.getAbsolutePath());
            }

            classpath = File.createTempFile(archiveNamePrefix, ".jar", tmp);
        }
        classpath.deleteOnExit();

        return jarArchive(classpath, entries, classes);
    }

    private static File jarArchive(final File archive, final Map<String, ?> entries, final Class... classes) throws IOException {
        final ClassLoader loader = ServletContextInjectionTest.class.getClassLoader();

        // Create the ZIP file
        final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(archive)));

        for (final Class clazz : classes) {
            final String name = clazz.getName().replace('.', File.separatorChar) + ".class";

            final URL resource = loader.getResource(name);
            assertNotNull(resource);

            // Add ZIP entry to output stream.
            out.putNextEntry(new ZipEntry(name));

            final InputStream in = new BufferedInputStream(resource.openStream());

            int i;
            while ((i = in.read()) != -1) {
                out.write(i);
            }

            // Complete the entry
            in.close();
            out.closeEntry();
        }

        if (entries != null) for (final Map.Entry<String, ?> entry : entries.entrySet()) {

            out.putNextEntry(new ZipEntry(entry.getKey()));

            final Object value = entry.getValue();

            if (value instanceof String) {

                final String s = (String) value;
                out.write(s.getBytes());

            } else if (value instanceof File) {

                final File file = (File) value;
                if (file.isDirectory())
                    throw new IllegalArgumentException(entry.getKey() + " is a directory, not a file.");
                IO.copy(file, out);

            } else if (value instanceof URL) {

                IO.copy((URL) value, out);

            } else if (value instanceof Class) {
                final String name = ((Class) value).getName().replace('.', File.separatorChar) + ".class";

                final URL resource = loader.getResource(name);
                assertNotNull(resource);

                IO.copy(resource, out);
            }

            out.closeEntry();
        }

        // Complete the ZIP file
        out.close();

        return archive;
    }
}
