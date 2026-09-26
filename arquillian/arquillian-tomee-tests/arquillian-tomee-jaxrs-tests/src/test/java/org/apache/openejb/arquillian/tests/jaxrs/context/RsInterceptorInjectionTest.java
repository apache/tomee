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
package org.apache.openejb.arquillian.tests.jaxrs.context;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class RsInterceptorInjectionTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(RsInterceptorInjectionTest.class);
    }

    @Test
    public void rest() throws IOException {
        final String response = ClientBuilder.newClient()
                .target(base.toExternalForm())
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
            checkNotNull("httpHeaders", httpHeaders);
            checkNotNull("providers", providers);
            checkNotNull("response", response);
            checkNotNull("request", request);
            checkNotNull("httpServletRequest", httpServletRequest);
            checkNotNull("uriInfo", uriInfo);
            checkNotNull("securityContext", securityContext);
            checkNotNull("contextResolver", contextResolver);

            // Do the thread locals actually point anywhere?
            check(httpHeaders.getRequestHeaders().size() > 0, "httpHeaders.getRequestHeaders().size() > 0");
            check(providers.getExceptionMapper(FooException.class) == null, "providers.getExceptionMapper(FooException.class) == null");
            check(response.getHeaderNames() != null, "response.getHeaderNames() != null");
            check(request.getMethod() != null, "request.getMethod() != null");
            check(httpServletRequest.getMethod() != null, "httpServletRequest.getMethod() != null");
            check(uriInfo.getPath() != null, "uriInfo.getPath() != null");
// TODO OPENEJB-1979 - JAX-RS SecurityContext.isCallerInRole always returns true in Embedded EJBContainer
//            Assert.assertTrue(!securityContext.isUserInRole("ThereIsNoWayThisShouldEverPass"));
            check(contextResolver.getContext(null) == null, "contextResolver.getContext(null) == null");

            context.proceed();

            // Test again to ensure thread locals are still valid
            check(httpHeaders.getRequestHeaders().size() > 0, "httpHeaders.getRequestHeaders().size() > 0");
            check(providers.getExceptionMapper(FooException.class) == null, "providers.getExceptionMapper(FooException.class) == null");
            check(response.getHeaderNames() != null, "response.getHeaderNames() != null");
            check(request.getMethod() != null, "request.getMethod() != null");
            check(httpServletRequest.getMethod() != null, "httpServletRequest.getMethod() != null");
            check(uriInfo.getPath() != null, "uriInfo.getPath() != null");
// TODO OPENEJB-1979 - JAX-RS SecurityContext.isCallerInRole always returns true in Embedded EJBContainer
//            Assert.assertTrue(!securityContext.isUserInRole("ThereIsNoWayThisShouldEverPass"));
            check(contextResolver.getContext(null) == null, "contextResolver.getContext(null) == null");

            return true;
        }

        private static void checkNotNull(final String name, final Object value) {
            if (value == null) {
                throw new IllegalStateException(name + " was not injected");
            }
        }

        private static void check(final boolean condition, final String description) {
            if (!condition) {
                throw new IllegalStateException("check failed: " + description);
            }
        }
    }

    public static class FooException extends RuntimeException {
    }

}
