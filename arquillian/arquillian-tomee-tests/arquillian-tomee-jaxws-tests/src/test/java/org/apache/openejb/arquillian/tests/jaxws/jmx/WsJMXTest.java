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
package org.apache.openejb.arquillian.tests.jaxws.jmx;

import org.apache.openejb.monitoring.LocalMBeanServer;
import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.jws.WebService;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

// client side, the embedded container shares this JVM and its MBeanServer
@RunWith(Arquillian.class)
public class WsJMXTest {

    private static final ObjectName[] names = new ObjectName[2];

    @ArquillianResource
    private URL base;

    @BeforeClass
    public static void beforeClass() throws MalformedObjectNameException {
        names[0] = new ObjectName("openejb.management:j2eeType=JAX-WS,J2EEServer=openejb,J2EEApplication=<empty>,EndpointType=EJB,name=AnEjbEndpoint");
        names[1] = new ObjectName("openejb.management:j2eeType=JAX-WS,J2EEServer=openejb,J2EEApplication=<empty>,EndpointType=POJO,name=AnPojoEndpoint");
    }

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "app.war")
                .addClasses(AnEjbEndpoint.class, AnPojoEndpoint.class)
                // no mapping for the servlet, the endpoint gets the default /AnPojoEndpointService one
                .setWebXML(new StringAsset("<web-app xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" version=\"6.0\">" +
                        "<servlet><servlet-name>toto</servlet-name><servlet-class>" + AnPojoEndpoint.class.getName() + "</servlet-class></servlet>" +
                        "</web-app>"));
    }

    @Test
    @Ignore("TOMEE-4707 a pojo endpoint servlet without servlet-mapping gets the default /AnPojoEndpointService address but TomcatWsRegistry doesn't map it, its WSDL is a 404")
    public void checkServiceWasDeployed() throws Exception {
        assertTrue(LocalMBeanServer.get().isRegistered(names[0]));
        assertTrue(LocalMBeanServer.get().isRegistered(names[1]));
        assertThat(String.class.cast(LocalMBeanServer.get().invoke(names[0], "getWsdl", new Object[0], new String[0])), CoreMatchers.containsString("<soap:address location=\"" + base.toExternalForm() + "webservices/AnEjbEndpoint\"/>"));
        assertThat(String.class.cast(LocalMBeanServer.get().invoke(names[1], "getWsdl", new Object[0], new String[0])), CoreMatchers.containsString("<soap:address location=\"" + base.toExternalForm() + "AnPojoEndpointService\"/>"));
    }

    // Arquillian runs @AfterClass before the undeployment, a class rule runs after it
    @ClassRule
    public static final TestRule AFTER = new ExternalResource() {
        @Override
        protected void after() {
            assertFalse(LocalMBeanServer.get().isRegistered(names[0]));
            assertFalse(LocalMBeanServer.get().isRegistered(names[1]));
        }
    };

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
