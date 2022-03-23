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

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class RsJMXTest {
    private static ObjectName name;
    private static int port = -1;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder()
            .p("httpejbd.port", Integer.toString(port))
            .build();
    }

    @Module
    @Classes(AnEndpoint.class)
    public WebApp war() {
        return new WebApp().contextRoot("app");
    }

    @BeforeClass
    public static void before() throws MalformedObjectNameException {
        name = new ObjectName("openejb.management:j2eeType=JAX-RS,J2EEServer=openejb,J2EEApplication=http_//127.0.0.1_" + port + "/app,EndpointType=Pojo,name=org.apache.openejb.server.cxf.rs.RsJMXTest$AnEndpoint");
    }

    @Test
    public void checkServiceWasDeployed() throws Exception {
        assertTrue(LocalMBeanServer.get().isRegistered(name));

        final String wadlXml = String.class.cast(LocalMBeanServer.get().invoke(name, "getWadl", new Object[]{null}, new String[0]));
        assertThat(wadlXml, wadlXml, CoreMatchers.containsString("<resources base=\"http://localhost:" + port + "/app/"));

        /* need a fix from cxf which will be shipped soon so deactivating it ATM
        final String wadlJson = String.class.cast(LocalMBeanServer.get().invoke(name, "getWadl", new Object[]{"json"}, new String[0]));
        assertThat(wadlJson, wadlJson, CoreMatchers.containsString("{\"application\":{"));
        */
    }

    @AfterClass
    public static void after() {
        assertFalse(LocalMBeanServer.get().isRegistered(name));
    }

    @Path("foo")
    public static class AnEndpoint {
        @GET
        public String bar() {
            return "bar";
        }

        @GET
        @Path("babar")
        public String babar() {
            return "babar";
        }
    }
}
