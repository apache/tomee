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
package org.apache.openejb.arquillian.tests.jaxrs.jmx;

import org.apache.openejb.monitoring.LocalMBeanServer;
import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import javax.management.ObjectName;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

// client side so the class rule sees the name set by the test; the embedded container shares the JVM and its MBeanServer
@RunWith(Arquillian.class)
public class RsJMXTest {
    private static ObjectName name;

    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "app.war").addClasses(RsJMXTest.class, AnEndpoint.class);
    }

    @Test
    public void checkServiceWasDeployed() throws Exception {
        name = new ObjectName("openejb.management:j2eeType=JAX-RS,J2EEServer=openejb,J2EEApplication=http_//" + base.getHost() + "_" + base.getPort() + "/app,EndpointType=Pojo,name=" + AnEndpoint.class.getName());

        assertTrue(LocalMBeanServer.get().isRegistered(name));

        final String wadlXml = String.class.cast(LocalMBeanServer.get().invoke(name, "getWadl", new Object[]{null}, new String[0]));
        assertThat(wadlXml, wadlXml, CoreMatchers.containsString("<resources base=\"" + base.toExternalForm()));

        /* need a fix from cxf which will be shipped soon so deactivating it ATM
        final String wadlJson = String.class.cast(LocalMBeanServer.get().invoke(name, "getWadl", new Object[]{"json"}, new String[0]));
        assertThat(wadlJson, wadlJson, CoreMatchers.containsString("{\"application\":{"));
        */
    }

    // Arquillian runs @AfterClass before the undeployment, a class rule runs after it
    @ClassRule
    public static final TestRule AFTER = new ExternalResource() {
        @Override
        protected void after() {
            assertFalse(LocalMBeanServer.get().isRegistered(name));
        }
    };

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
