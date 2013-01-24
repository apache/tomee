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
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InterceptorBinding;
import javax.interceptor.InvocationContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Set;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class CdiInterceptorContextTest {
    @Module
    @Classes(value = { Endpoint.class, AnswerPerfect.class }, cdiInterceptors = AnswerPerfect.class)
    public WebApp war() {
        return new WebApp()
                .contextRoot("app")
                .addServlet("REST Application", Application.class.getName())
                .addInitParam("REST Application", "javax.ws.rs.Application", PerfectApplication.class.getName());
    }

    @Test
    public void checkServiceWasDeployed() {
        assertEquals("perfect", WebClient.create("http://localhost:4204/app").path("/foo").get(String.class));
    }

    @Path("/foo")
    @Perfect
    public static class Endpoint {
        @GET
        public String bar() {
            return "bar";
        }
    }

    @InterceptorBinding
    @Target(TYPE)
    @Retention(RUNTIME)
    public static @interface Perfect {

    }

    @Interceptor @Perfect
    public static class AnswerPerfect {
        @Context
        private HttpServletRequest request;

        @AroundInvoke
        public Object invoke(final InvocationContext ic) throws Exception {
            if (ic.getMethod().getName().equals("bar") && "foo".equals(request.getRequestURI())) {
                return "perfect";
            }
            return ic.proceed();
        }
    }

    public static class PerfectApplication extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            final Set<Class<?>> classes = new HashSet<Class<?>>();
            classes.add(Endpoint.class);
            return classes;
        }
    }
}
