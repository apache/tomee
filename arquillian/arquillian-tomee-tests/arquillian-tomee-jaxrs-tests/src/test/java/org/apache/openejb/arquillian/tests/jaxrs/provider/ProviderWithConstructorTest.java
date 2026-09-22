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
package org.apache.openejb.arquillian.tests.jaxrs.provider;

import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class ProviderWithConstructorTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        // the provider writes the context path, so the archive name matters
        return ShrinkWrap.create(WebArchive.class, "app.war")
            .addClasses(AnEndpointToCheckAProvider.class, ApplicationWithProvider.class, ConstructorProvider.class)
            .setWebXML(new StringAsset("<web-app xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" version=\"6.0\">" +
                "<servlet>" +
                "<servlet-name>REST Application</servlet-name>" +
                "<servlet-class>" + Application.class.getName() + "</servlet-class>" +
                "<init-param>" +
                "<param-name>jakarta.ws.rs.Application</param-name>" +
                "<param-value>" + ApplicationWithProvider.class.getName() + "</param-value>" +
                "</init-param>" +
                "</servlet>" +
                "</web-app>"));
    }

    @Test
    public void checkServiceWasDeployed() {
        assertEquals("/app", WebClient.create(base.toExternalForm()).path("/foo").accept("openejb/constructor").get(String.class));
    }

    @Path("/foo")
    public static class AnEndpointToCheckAProvider {
        @GET
        @Produces("openejb/constructor")
        public AnEndpointToCheckAProvider bar() {
            return new AnEndpointToCheckAProvider();
        }
    }

    public static class ApplicationWithProvider extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            final Set<Class<?>> classes = new HashSet<Class<?>>();
            classes.add(AnEndpointToCheckAProvider.class);
            classes.add(ConstructorProvider.class);
            return classes;
        }
    }

    @Provider
    @Produces("openejb/constructor")
    public static class ConstructorProvider<T> implements MessageBodyWriter<T> {
        private final HttpServletRequest request;

        public ConstructorProvider(final @Context HttpServletRequest request) {
            if (request == null) {
                throw new IllegalArgumentException();
            }
            this.request = request;
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
            entityStream.write(request.getContextPath().getBytes());
        }
    }

}
