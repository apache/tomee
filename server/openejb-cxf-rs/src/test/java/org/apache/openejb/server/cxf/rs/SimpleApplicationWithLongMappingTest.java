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
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.server.cxf.rs.beans.HookedRest;
import org.apache.openejb.server.cxf.rs.beans.MyExpertRestClass;
import org.apache.openejb.server.cxf.rs.beans.MyFirstRestClass;
import org.apache.openejb.server.cxf.rs.beans.MyRESTApplication;
import org.apache.openejb.server.cxf.rs.beans.MySecondRestClass;
import org.apache.openejb.server.cxf.rs.beans.RestWithInjections;
import org.apache.openejb.server.cxf.rs.beans.SimpleEJB;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import java.util.Properties;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class SimpleApplicationWithLongMappingTest {

    private static int port = -1;
    public static String BASE_URL = "undefined";

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
        BASE_URL = "http://localhost:" + port + "/foo/mapping/part2";
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder()
            .p("httpejbd.port", Integer.toString(port))
            .p(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true")
            .build();
    }

    @Module
    @Classes(cdi = true, value = {MySecondRestClass.class, HookedRest.class, RestWithInjections.class, SimpleEJB.class, MyExpertRestClass.class, MyFirstRestClass.class})
    public WebApp war() {
        return new WebApp()
            .contextRoot("foo")
            .addServlet("REST Application", Application.class.getName())
            .addInitParam("REST Application", "jakarta.ws.rs.Application", MyRESTApplication.class.getName())
            .addServletMapping("REST Application", "/mapping/part2/*");
    }

    @Test
    public void first() {
        final String hi = WebClient.create(BASE_URL).path("/first/hi").accept(TEXT_PLAIN_TYPE).get(String.class);
        assertEquals("Hi from REST World!", hi);
    }

    @Test
    public void second() {
        final String hi = WebClient.create(BASE_URL).path("/second/hi2/2nd").accept(TEXT_PLAIN_TYPE).get(String.class);
        assertEquals("hi 2nd", hi);
    }

    @Test(expected = WebApplicationException.class)
    public void nonListed() {
        WebClient.create(BASE_URL).path("/non-listed/yata/foo").accept(TEXT_PLAIN_TYPE).get(String.class);
    }

    @Test
    public void hooked() {
        assertEquals(true, WebClient.create(BASE_URL).path("/hooked/post").accept(TEXT_PLAIN_TYPE).get(Boolean.class));
    }

    @Test
    public void injectEjb() {
        assertEquals(true, WebClient.create(BASE_URL).path("/inject/ejb").accept(TEXT_PLAIN_TYPE).get(Boolean.class));
    }
}
