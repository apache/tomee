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
import org.apache.openejb.config.DeploymentFilterable;
import org.apache.openejb.server.rest.RESTService;
import org.apache.openejb.util.NetworkUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import jakarta.ejb.Singleton;
import jakarta.ejb.embeddable.EJBContainer;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import static org.junit.Assert.assertEquals;

public class DiscoverCustomProviderTest {
    private static EJBContainer container;
    private static int port = -1;

    @BeforeClass
    public static void start() throws Exception {
        port = NetworkUtil.getNextAvailablePort();
        final Properties properties = new Properties();
        properties.setProperty("httpejbd.port", Integer.toString(port));
        properties.setProperty(DeploymentFilterable.CLASSPATH_INCLUDE, ".*openejb-cxf-rs.*");
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        properties.setProperty(RESTService.OPENEJB_JAXRS_PROVIDERS_AUTO_PROP, "true");
        properties.setProperty(CxfRsHttpListener.CXF_JAXRS_PREFIX + "debug", "true");
        container = EJBContainer.createEJBContainer(properties);
    }

    @AfterClass
    public static void close() throws Exception {
        if (container != null) {
            container.close();
        }
    }

    @Test
    public void customProvider() {
        final String response = WebClient.create("http://localhost:" + port + "/openejb-cxf-rs")
            .accept("discover/reverse")
            .path("the/service").get(String.class);
        assertEquals("it rocks", response);
    }

    @Singleton
    @Path("the")
    public static class TheService {
        @GET
        @Path("service")
        @Produces("discover/reverse")
        public AtomicReference<String> go() {
            return new AtomicReference<>("skcor ti");
        }
    }

    @Provider
    @Produces("discover/reverse")
    public static class ReverseProvider<T> implements MessageBodyWriter<T> {
        private String reverse(String str) {
            if (str == null) {
                return "";
            }
            return new StringBuilder(str).reverse().toString();
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
            entityStream.write(reverse(AtomicReference.class.cast(t).get().toString()).getBytes());
        }
    }
}
