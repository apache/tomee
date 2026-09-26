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
package org.apache.openejb.arquillian.tests.jaxrs.cdi;

import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Singleton;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InterceptorBinding;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class RsCDIInterceptorTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(RsCDIInterceptorTest.class)
            .addAsWebInfResource(new StringAsset("<beans xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" bean-discovery-mode=\"all\" version=\"4.0\">" +
                "<interceptors><class>" + MockingInterceptor.class.getName() + "</class></interceptors>" +
                "</beans>"), "beans.xml");
    }

    @Test
    public void ejb() throws IOException {
        final String response = IO.slurp(new URL(base.toExternalForm() + "session-bean/check-ejb"));
        assertEquals("mock", response);
    }

    @Test
    public void pojo() throws IOException {
        final String response = IO.slurp(new URL(base.toExternalForm() + "pojo/check-pojo"));
        assertEquals("mock", response);
    }

    @Path("/session-bean")
    @Singleton
    @IBinding
    public static class InterceptedEJBRs {
        @GET
        @Path("/check-ejb")
        public String check() {
            return null;
        }
    }

    @Path("/pojo")
    @IBinding
    public static class InterceptedRs {
        @GET
        @Path("/check-pojo")
        public String check() {
            return null;
        }
    }

    @Interceptor
    @IBinding
    public static class MockingInterceptor {
        @AroundInvoke
        public Object mock(final InvocationContext ic) throws Exception {
            return "mock";
        }
    }

    @InterceptorBinding
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface IBinding {

    }
}
