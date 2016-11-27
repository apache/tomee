/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.rs.johnzon;

import org.apache.johnzon.mapper.Converter;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.config.sys.Service;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.PojoDeployment;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.RandomPort;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class DisableTomEEJohnzonTest {

    private static final String PAYLOAD = "{\"not\": \"johnzon\"}";

    @RandomPort("http")
    private URL base;

    @Module
    public static EjbModule service() throws Exception {
        final EjbModule module = new EjbModule(new EjbJar(), new OpenejbJar());
        final EnterpriseBean bean = new SingletonBean(Endpoint.class).localBean();
        module.getEjbJar().addEnterpriseBean(bean);

        final PojoDeployment e = new PojoDeployment();
        e.setClassName("jaxrs-application");
        e.getProperties().setProperty("cxf.jaxrs.providers", "org.apache.openejb.server.cxf.rs.johnzon.DisableTomEEJohnzonTest$TestWriter");
        module.getOpenejbJar().getPojoDeployment().add(e);

        return module;
    }

    @Configuration
    public static Properties properties() {
        return new PropertiesBuilder()
            .p("org.apache.openejb.server.cxf.rs.CxfRSService$TomEEJohnzonProvider.activated", "false")
            .p("org.apache.openejb.server.cxf.rs.CxfRSService$TomEEJsonpProvider.activated", "false")
            .build();
    }

    @Test
    public void run() throws IOException {
        assertEquals(PAYLOAD, IO.slurp(new URL(base.toExternalForm() + getClass().getSimpleName() + "/test")));
    }


    @Path("test")
    public static class Endpoint {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Payload get() {
            return new Payload();
        }
    }


    public static class Payload {

        String its = "johnzon";

        public String getIts() {
            return its;
        }

        public void setIts(String its) {
            this.its = its;
        }
    }


    @Provider
    @Produces(MediaType.WILDCARD)
    public static class TestWriter implements MessageBodyWriter {
        @Override
        public boolean isWriteable(Class aClass, Type type, Annotation[] annotations, MediaType mediaType) {
            return aClass.getName().equals(Payload.class.getName());
        }

        @Override
        public long getSize(Object o, Class aClass, Type type, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(Object o, Class aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
            outputStream.write(PAYLOAD.getBytes("UTF-8"));

        }
    }

}
