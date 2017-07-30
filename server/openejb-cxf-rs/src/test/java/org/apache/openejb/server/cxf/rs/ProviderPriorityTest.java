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

import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposerRule;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Priority;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

public class ProviderPriorityTest {
    @Rule
    public final ApplicationComposerRule container = new ApplicationComposerRule(this);

    private final int port = NetworkUtil.getNextAvailablePort();

    @Module
    @Classes(innerClassesAsBean = true)
    public WebApp web() {
        return new WebApp();
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder()
                .p("httpejbd.port", Integer.toString(port))
                .p(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true")
                .build();
    }

    @Test
    public void run() throws IOException {
        assertEquals("B", IO.slurp(new URL("http://localhost:" + port + "/openejb/test")));
    }

    @Path("/test")
    public static class Endpoint {
        @Context
        private Providers providers;

        @GET
        @Produces("test/test")
        public AtomicReference<String> asserts() {
            return new AtomicReference<>("fail");
        }
    }

    @Provider
    @Produces("test/test")
    @Priority(200)
    public static class ProviderA implements MessageBodyWriter<AtomicReference<String>> {
        private String reverse(String str) {
            if (str == null) {
                return "";
            }

            StringBuilder s = new StringBuilder(str.length());
            for (int i = str.length() - 1; i >= 0; i--) {
                s.append(str.charAt(i));
            }
            return s.toString();
        }

        @Override
        public long getSize(AtomicReference<String> t, Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public boolean isWriteable(Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return true;
        }

        @Override
        public void writeTo(AtomicReference<String> t, Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
            entityStream.write("A".getBytes());
        }
    }

    @Provider
    @Produces("test/test")
    @Priority(1)
    public static class ProviderB implements MessageBodyWriter<AtomicReference<?>> {
        @Override
        public long getSize(AtomicReference<?> t, Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public boolean isWriteable(Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return true;
        }

        @Override
        public void writeTo(AtomicReference<?> t, Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
            entityStream.write("B".getBytes());
        }
    }

    @Provider
    @Produces("test/test")
    @Priority(50)
    public static class ProviderC implements MessageBodyWriter<AtomicReference<?>> {
        @Override
        public long getSize(AtomicReference<?> t, Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public boolean isWriteable(Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return true;
        }

        @Override
        public void writeTo(AtomicReference<?> t, Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
            entityStream.write("C".getBytes());
        }
    }
}
