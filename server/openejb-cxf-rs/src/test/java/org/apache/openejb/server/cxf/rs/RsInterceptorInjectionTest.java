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
package org.apache.openejb.server.cxf.rs;

import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Singleton;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.InvocationContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class RsInterceptorInjectionTest {

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
    public static SingletonBean service() throws Exception {
        final SingletonBean bean = new SingletonBean(RsInjection.class);
        bean.setLocalBean(new Empty());
        return bean;
    }

    @Test
    public void rest() throws IOException {
        final String response = ClientBuilder.newClient()
                .target("http://127.0.0.1:" + port + "/RsInterceptorInjectionTest/")
                .path("injections/check")
                .request()
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .get(String.class);
        assertEquals("true", response);
    }

    @Singleton
    @Interceptors(RsEjbInterceptor.class)
    @Path("/injections")
    public static class RsInjection {

        @GET
        @Path("/check")
        public boolean check() {
            return false;
        }
    }

    public static class RsEjbInterceptor {

        @Context
        private HttpHeaders httpHeaders;

        @Context
        private Providers providers;

        @Context
        private HttpServletResponse response;

        @Context
        private Request request;

        @Context
        private HttpServletRequest httpServletRequest;

        @Context
        private ServletRequest servletRequest;

        @Context
        private UriInfo uriInfo;

        @Context
        private SecurityContext securityContext;

        @Context
        private ContextResolver contextResolver;

// TODO TOMEE-685 - does it make sense since we don't define a strict servlet?
//        @Context
//        private ServletConfig servletConfig;


        @AroundInvoke
        private Object invoke(InvocationContext context) throws Exception {
            // Are they injected?
            Assert.assertNotNull("httpHeaders", httpHeaders);
            Assert.assertNotNull("providers", providers);
            Assert.assertNotNull("response", response);
            Assert.assertNotNull("request", request);
            Assert.assertNotNull("httpServletRequest", httpServletRequest);
            Assert.assertNotNull("uriInfo", uriInfo);
            Assert.assertNotNull("securityContext", securityContext);
            Assert.assertNotNull("contextResolver", contextResolver);

            // Do the thread locals actually point anywhere?
            Assert.assertTrue(httpHeaders.getRequestHeaders().size() > 0);
            Assert.assertTrue(providers.getExceptionMapper(FooException.class) == null);
            Assert.assertTrue(response.getHeaderNames() != null);
            Assert.assertTrue(request.getMethod() != null);
            Assert.assertTrue(httpServletRequest.getMethod() != null);
            Assert.assertTrue(uriInfo.getPath() != null);
// TODO OPENEJB-1979 - JAX-RS SecurityContext.isCallerInRole always returns true in Embedded EJBContainer
//            Assert.assertTrue(!securityContext.isUserInRole("ThereIsNoWayThisShouldEverPass"));
            Assert.assertTrue(contextResolver.getContext(null) == null);

            context.proceed();

            // Test again to ensure thread locals are still valid
            Assert.assertTrue(httpHeaders.getRequestHeaders().size() > 0);
            Assert.assertTrue(providers.getExceptionMapper(FooException.class) == null);
            Assert.assertTrue(response.getHeaderNames() != null);
            Assert.assertTrue(request.getMethod() != null);
            Assert.assertTrue(httpServletRequest.getMethod() != null);
            Assert.assertTrue(uriInfo.getPath() != null);
// TODO OPENEJB-1979 - JAX-RS SecurityContext.isCallerInRole always returns true in Embedded EJBContainer
//            Assert.assertTrue(!securityContext.isUserInRole("ThereIsNoWayThisShouldEverPass"));
            Assert.assertTrue(contextResolver.getContext(null) == null);

            return true;
        }
    }

    public static class FooException extends RuntimeException {
    }

}
