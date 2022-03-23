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
package org.apache.openejb.server.cxf;

import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.ServletMapping;
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

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.jws.WebService;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@EnableServices("jax-ws")
@RunWith(ApplicationComposer.class)
public class WsJMXTest {

    private static final ObjectName[] names = new ObjectName[2];
    private static int port = -1;

    @BeforeClass
    public static void beforeClass() throws MalformedObjectNameException {
        port = NetworkUtil.getNextAvailablePort();
        names[0] = new ObjectName("openejb.management:j2eeType=JAX-WS,J2EEServer=openejb,J2EEApplication=<empty>,EndpointType=EJB,name=AnEjbEndpoint");
        names[1] = new ObjectName("openejb.management:j2eeType=JAX-WS,J2EEServer=openejb,J2EEApplication=<empty>,EndpointType=POJO,name=AnPojoEndpoint");
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder()
            .p("httpejbd.port", Integer.toString(port))
            .p(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true")
            .build();
    }

    @Module
    @Classes({AnEjbEndpoint.class, AnPojoEndpoint.class})
    public WebApp war() {
        final WebApp webapp = new WebApp().contextRoot("app");

        Servlet servlet = new Servlet();
        servlet.setServletName("toto");
        servlet.setServletClass(AnPojoEndpoint.class.getName());

        ServletMapping servletMapping = new ServletMapping();
        servletMapping.setServletName("pojo");
        servletMapping.getUrlPattern().add("/toto");

        webapp.getServlet().add(servlet);
        webapp.getServletMapping().add(servletMapping);

        return webapp;
    }

    @Test
    public void checkServiceWasDeployed() throws Exception {
        assertTrue(LocalMBeanServer.get().isRegistered(names[0]));
        assertTrue(LocalMBeanServer.get().isRegistered(names[1]));
        assertThat(String.class.cast(LocalMBeanServer.get().invoke(names[0], "getWsdl", new Object[0], new String[0])), CoreMatchers.containsString("<soap:address location=\"http://127.0.0.1:" + port + "/app/AnEjbEndpoint\"/>"));
        assertThat(String.class.cast(LocalMBeanServer.get().invoke(names[1], "getWsdl", new Object[0], new String[0])), CoreMatchers.containsString("<soap:address location=\"http://127.0.0.1:" + port + "/app/AnPojoEndpointService\"/>"));
    }

    @AfterClass
    public static void after() {
        assertFalse(LocalMBeanServer.get().isRegistered(names[0]));
        assertFalse(LocalMBeanServer.get().isRegistered(names[1]));
    }

    @Singleton
    @Lock(LockType.READ)
    @WebService
    public static class AnEjbEndpoint {
        public String sayHello(final String me) {
            return "Hello " + me;
        }
    }

    @WebService
    public static class AnPojoEndpoint {
        public String sayHi(final String me) {
            return "Hi " + me;
        }
    }
}
