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
import org.apache.openejb.server.cxf.rs.beans.MyFirstRestClass;
import org.apache.openejb.server.cxf.rs.beans.MySecondRestClass;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class MultipleApplicationsTest {

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
    @Classes(cdi = true, value = {Application1.class, Application2.class, MyFirstRestClass.class})
    public WebApp war() {
        return new WebApp().contextRoot("foo");
    }

    @Test
    public void app1() {
        assertEquals("Hi from REST World!", WebClient.create("http://localhost:" + port + "/foo/").path("app1/first/hi").get(String.class));
    }

    @Test
    public void app2() {
        assertEquals("hi bar", WebClient.create("http://localhost:" + port + "/foo/").path("app2/second/hi2/bar").get(String.class));
    }

    @ApplicationPath("app1")
    public static class Application1 extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return Collections.<Class<?>>singleton(MyFirstRestClass.class);
        }
    }

    @ApplicationPath("app2")
    public static class Application2 extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return Collections.<Class<?>>singleton(MySecondRestClass.class);
        }
    }
}
