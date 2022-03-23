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
package org.apache.openejb.server.cxf.rs;

import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@EnableServices("jaxrs")
@RunWith(ApplicationComposer.class)
public class CheckedExceptionMapperTest {

    private static int port = -1;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder()
            .p("httpejbd.port", Integer.toString(port))
            .p(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true")
            .build();
    }

    @Module
    @Classes({ExampleExceptionMapper.class})
    public EjbModule module() {
        final SingletonBean bean = new SingletonBean(ExampleRest.class);
        bean.setRestService(true);

        final EjbJar ejbJar = new EjbJar("beans");
        ejbJar.addEnterpriseBean(bean);

        final OpenejbJar openejbJar = new OpenejbJar();
        openejbJar.addEjbDeployment(new EjbDeployment(bean));

        final Properties properties = openejbJar.getEjbDeployment().iterator().next().getProperties();
        properties.setProperty("cxf.jaxrs.providers", "org.apache.openejb.server.cxf.rs.CheckedExceptionMapperTest$ExampleExceptionMapper");

        final EjbModule module = new EjbModule(ejbJar);
        module.setOpenejbJar(openejbJar);

        return module;
    }

    @Test
    public void testThrowException() throws IOException {
        assertEquals("Exception!", IO.slurp(new URL("http://localhost:" + port + "/CheckedExceptionMapperTest/example/throw/")));
    }

    public static class ExampleException extends Exception {
        public ExampleException(String message) {
            super(message);
        }
    }

    @Provider
    public static class ExampleExceptionMapper implements ExceptionMapper<ExampleException> {
        @Override
        public Response toResponse(ExampleException ex) {
            return Response.ok("Exception!").build();
        }
    }

    @Path("/example")
    public static class ExampleRest {
        @GET
        @Path(value = "/throw")
        public String throwException() throws ExampleException {
            throw new ExampleException("exception");
        }
    }
}
