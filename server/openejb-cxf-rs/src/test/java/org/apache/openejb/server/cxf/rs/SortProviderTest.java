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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Comparator;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.Providers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore("no more supported by CXF - chaining providers")
public class SortProviderTest {
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
                .p("cxf.jaxrs.provider-comparator", MyComp.class.getName())
                .p(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true")
                .build();
    }

    @Test
    public void run() throws IOException {
        assertTrue(MyComp.saw);
        assertEquals("it works!", IO.slurp(new URL("http://localhost:" + port + "/openejb/test")));
    }

    public static class MyComp implements Comparator<Object> {
        private static boolean saw;

        @Override
        public int compare(final Object o1, final Object o2) {
            saw = true;

            final Class<?> c1 = o1.getClass();
            if (c1 == ATestProvider11.class) {
                return -1;
            }

            final Class<?> c2 = o2.getClass();
            if (c2 == ATestProvider11.class) {
                return 1;
            }
            if (c1 == ATestProviderA.class) {
                return -1;
            }
            if (c2 == ATestProviderA.class) {
                return 1;
            }

            return c1.getName().compareTo(c2.getName());
        }
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
    public static class ATestProviderA implements MessageBodyWriter<AtomicReference<String>> {
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
            entityStream.write(reverse(t.get()).getBytes());
        }
    }

    @Provider
    @Produces("test/test")
    public static class ATestProvider11 implements MessageBodyWriter<AtomicReference<?>> {
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
            entityStream.write("it works!".getBytes());
        }
    }
}
