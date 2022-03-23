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
package org.apache.tomee.embedded;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ManualDeploymentTest {
    @Test
    public void run() throws IOException {
        final Configuration configuration = new Configuration().randomHttpPort();
        configuration.setDir(Files.mkdirs(new File("target/" + getClass().getSimpleName() + "-tomcat")).getAbsolutePath());

        try (final Container container = new Container(configuration)) {
            // tomee-embedded (this "container url" is filtered: name prefix + it is a directory (target/test-classes)
            final File parent = Files.mkdirs(new File("target/" + getClass().getSimpleName()));
            final File war = ShrinkWrap.create(WebArchive.class, "the-webapp")
                    .addClass(Foo.class)
                    .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml") // activate CDI
                    .as(ExplodedExporter.class)
                    .exportExploded(parent);

            final Context ctx = container.addContext("", war.getAbsolutePath());

            final Wrapper wrapper = Tomcat.addServlet(ctx, "awesome", AServlet.class.getName());
            ctx.addServletMappingDecoded("/awesome", wrapper.getName());

            assertEquals("Awesome", IO.slurp(new URL("http://localhost:" + configuration.getHttpPort() + "/awesome")).trim());
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public static class AServlet extends HttpServlet {
        @Inject
        private Foo foo;

        @Override
        protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().write(foo.howIsIt());
        }
    }

    public static class Foo {
        public String howIsIt() {
            return "Awesome";
        }
    }
}
