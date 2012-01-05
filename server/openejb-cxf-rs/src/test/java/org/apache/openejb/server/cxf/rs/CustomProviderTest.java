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

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.OpenEjbContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.Singleton;
import javax.ejb.embeddable.EJBContainer;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;

public class CustomProviderTest {
    private static EJBContainer container;
    private static String providers;

    @BeforeClass public static void start() throws Exception {
        providers = System.getProperty(CxfRsHttpListener.OPENEJB_CXF_JAXRS_PROVIDERS_KEY);
        Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        properties.setProperty(CxfRsHttpListener.OPENEJB_CXF_JAXRS_PROVIDERS_KEY, ReverseProvider.class.getName());
        properties.setProperty(CustomSpecificService.class.getName() + CxfRsHttpListener.OPENEJB_CXF_JAXRS_PROVIDERS_SUFFIX, ConstantProvider.class.getName());
        container = EJBContainer.createEJBContainer(properties);
    }

    @AfterClass public static void close() throws Exception {
        if (container != null) {
            container.close();
        }
        if (providers == null) {
            System.getProperties().remove(CxfRsHttpListener.OPENEJB_CXF_JAXRS_PROVIDERS_KEY);
        } else {
            System.setProperty(CxfRsHttpListener.OPENEJB_CXF_JAXRS_PROVIDERS_KEY, providers);
        }
    }

    @Test public void customProvider() {
        String response = WebClient.create("http://localhost:4204").accept("openejb/reverse")
            .path("/custom1/reverse").get(String.class);
        assertEquals("provider", response);
    }

    @Test public void customSpecificProvider() {
        String response = WebClient.create("http://localhost:4204").accept("openejb/constant")
            .path("/custom2/constant").get(String.class);
        assertEquals("it works!", response);
    }

    @Singleton
    @Path("/custom1")
    public static class CustomService {
        @GET @Path("/reverse") @Produces("openejb/reverse") public String go() {
            return "redivorp";
        }
    }

    @Singleton
    @Path("/custom2")
    public static class CustomSpecificService {
        @GET @Path("/constant") @Produces("openejb/constant") public String go() {
            return "will be overriden";
        }
    }

    @Provider
    @Produces("openejb/reverse")
    public static class ReverseProvider<T> implements MessageBodyWriter<T> {
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
        public long getSize(T t, Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public boolean isWriteable(Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return true;
        }

        @Override
        public void writeTo(T t, Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
            entityStream.write(reverse((String) t).getBytes());
        }
    }

    @Provider
    @Produces("openejb/constant")
    public static class ConstantProvider<T> implements MessageBodyWriter<T> {
        @Override
        public long getSize(T t, Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public boolean isWriteable(Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return true;
        }

        @Override
        public void writeTo(T t, Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
            entityStream.write("it works!".getBytes());
        }
    }
}
