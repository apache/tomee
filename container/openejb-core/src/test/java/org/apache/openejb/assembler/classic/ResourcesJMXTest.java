/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.assembler.classic;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.monitoring.LocalMBeanServer;

import javax.management.*;
import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class ResourcesJMXTest extends TestCase {
    public void test() throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        // System services
        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // FakeRA
        final ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.service = "Resource";
        resourceInfo.className = FakeRA.class.getName();
        resourceInfo.id = "FakeRA";
        resourceInfo.properties = new Properties();
        assembler.createResource(resourceInfo);

        // FakeRA container
        final ContainerInfo containerInfo = config.configureService(MdbContainerInfo.class);
        containerInfo.id = "FakeContainer";
        containerInfo.displayName = "Fake Container";
        containerInfo.properties.setProperty("ResourceAdapter", "FakeRA");
        containerInfo.properties.setProperty("MessageListenerInterface", FakeMessageListener.class.getName());
        containerInfo.properties.setProperty("ActivationSpecClass", FakeActivationSpec.class.getName());
        assembler.createContainer(containerInfo);

        final ResourceInfo testResource = new ResourceInfo();
        testResource.id = "testResource";
        testResource.className = FakeResource.class.getName();

        final Properties p = new Properties();
        p.put("host", "localhost");
        p.put("port", "12345");
        p.put("parameter", "test param");
        testResource.properties = p;

        assembler.createResource(testResource);

        {
            ObjectName on = new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=<empty>,j2eeType=ResourceAdapter,name=FakeRA");
            assertNotNull(LocalMBeanServer.get().getMBeanInfo(on));
            assertEquals("faketest", LocalMBeanServer.get().getAttribute(on, "name"));
            assertEquals(10, LocalMBeanServer.get().getAttribute(on, "priority"));
        }
        {
            ObjectName on = new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=<empty>,j2eeType=Resource,name=testResource");
            assertNotNull(LocalMBeanServer.get().getMBeanInfo(on));
            assertEquals("localhost", LocalMBeanServer.get().getAttribute(on, "host"));
            assertEquals(12345, LocalMBeanServer.get().getAttribute(on, "port"));
            assertEquals("test param", LocalMBeanServer.get().getAttribute(on, "parameter"));
        }

        OpenEJB.destroy();

        assertFalse(LocalMBeanServer.get().isRegistered(new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=<empty>,j2eeType=ResourceAdapter,name=FakeRA")));
        assertFalse(LocalMBeanServer.get().isRegistered(new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=<empty>,j2eeType=Resource,name=testResource")));
    }

    public interface FakeMessageListener {
        void doIt(Properties properties);
    }

    public static class FakeRA implements ResourceAdapter {

        private String name = "faketest";
        private int priority = 10;


        public void start(final BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
        }

        public void stop() {
        }

        public void endpointActivation(final MessageEndpointFactory messageEndpointFactory, final ActivationSpec activationSpec) throws ResourceException {
            final MessageEndpoint endpoint = messageEndpointFactory.createEndpoint(null);
        }

        public void endpointDeactivation(final MessageEndpointFactory messageEndpointFactory, final ActivationSpec activationSpec) {
        }

        public XAResource[] getXAResources(final ActivationSpec[] activationSpecs) throws ResourceException {
            return new XAResource[0];
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }
    }

    public static class FakeActivationSpec implements ActivationSpec {
        private FakeRA fakeRA;

        public void validate() throws InvalidPropertyException {
        }

        public FakeRA getResourceAdapter() {
            return fakeRA;
        }

        public void setResourceAdapter(final ResourceAdapter resourceAdapter) {
            this.fakeRA = (FakeRA) resourceAdapter;
        }
    }

    public static class FakeResource {
        private String host;
        private int port;
        private String parameter;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getParameter() {
            return parameter;
        }

        public void setParameter(String parameter) {
            this.parameter = parameter;
        }
    }

}
