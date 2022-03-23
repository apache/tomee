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
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
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
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class RsCDIInterceptorTest {

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
    @Classes(cdi = true, value = {InterceptedEJBRs.class, InterceptedRs.class}, cdiInterceptors = MockingInterceptor.class)
    public WebApp war() {
        return new WebApp()
            .contextRoot("foo");
    }

    @Test
    public void ejb() throws IOException {
        final String response = IO.slurp(new URL("http://127.0.0.1:" + port + "/foo/session-bean/check-ejb"));
        assertEquals("mock", response);
    }

    @Test
    public void pojo() throws IOException {
        final String response = IO.slurp(new URL("http://127.0.0.1:" + port + "/foo/pojo/check-pojo"));
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
