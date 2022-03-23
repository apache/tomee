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
package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.LocalBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Singleton;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class EJBProviderTest {

    @RandomPort("http")
    private int port;

    @Module
    @Classes(cdi = true, value = {Helper.class, MyPro.class, Res.class})
    public WebApp war() {
        return new WebApp()
            .contextRoot("foo");
    }

    @Test
    public void isEJB() {
        assertEquals("Oh Yeah!", WebClient.create("http://localhost:" + port + "/foo").accept("provider/type").path("res2").get(String.class));
    }

    @Path("res2")
    public static class Res {
        @GET
        @Produces("provider/type")
        public String f() {
            return "failed";
        }
    }

    @Provider
    @Singleton
    @LocalBean
    @Produces("provider/type")
    public static class MyPro implements MessageBodyWriter<String> {
        @Inject
        private Helper helper;

        @Resource
        private SessionContext sc;

        @Override
        public boolean isWriteable(final Class<?> type, final Type genericType,
                                   final Annotation[] annotations, final MediaType mediaType) {
            return true;
        }

        @Override
        public long getSize(final String s, final Class<?> type, final Type genericType,
                            final Annotation[] annotations, final MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(final String s, final Class<?> type, final Type genericType,
                            final Annotation[] annotations,
                            final MediaType mediaType,
                            final MultivaluedMap<String, Object> httpHeaders,
                            final OutputStream entityStream) throws IOException, WebApplicationException {
            assertEquals(MyPro.class, sc.getInvokedBusinessInterface());
            entityStream.write(helper.data().getBytes());
        }
    }

    @Dependent
    public static class Helper {
        String data() {
            return "Oh Yeah!";
        }
    }
}
