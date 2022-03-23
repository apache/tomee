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
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.ActivationConfigProperty;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.test.mdb.BasicMdbBean;

import jakarta.jms.MessageListener;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.InvalidPropertyException;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.UnavailableException;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class MdbConfigTest extends TestCase {
    public void test() throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        // System services
        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // JMS persistence datasource
        final ResourceInfo dataSourceInfo = config.configureService("Default Unmanaged JDBC Database", ResourceInfo.class);
        dataSourceInfo.properties.setProperty("JdbcUrl", "jdbc:hsqldb:mem:MdbConfigTest");
        assembler.createResource(dataSourceInfo);

        // JMS
        assembler.createResource(config.configureService("Default JMS Resource Adapter", ResourceInfo.class));

        // JMS Container
        final MdbContainerInfo mdbContainerInfo = config.configureService(MdbContainerInfo.class);
        assembler.createContainer(mdbContainerInfo);

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

        // generate ejb jar application
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(createJaxbMdb("JmsMdb", BasicMdbBean.class.getName(), MessageListener.class.getName()));
        ejbJar.addEnterpriseBean(createJaxbMdb("FakeMdb", FakeMdb.class.getName(), FakeMessageListener.class.getName()));
        final EjbModule ejbModule = new EjbModule(getClass().getClassLoader(), "FakeEjbJar", "fake.jar", ejbJar, null);

        // configure and deploy it
        final EjbJarInfo info = config.configureApplication(ejbModule);
        assembler.createEjbJar(info);
    }

    public static class FakeMdb implements FakeMessageListener {
        public void doIt(final Properties properties) {
        }
    }

    public static interface FakeMessageListener {
        public void doIt(Properties properties);
    }

    public static class FakeRA implements ResourceAdapter {
        public boolean started;

        public void start(final BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
            assertFalse("Already started", started);
            assertNotNull("bootstrapContext is null", bootstrapContext);
            assertNotNull("bootstrapContext.getWorkManager() is null", bootstrapContext.getWorkManager());
            assertNotNull("bootstrapContext.getXATerminator() is null", bootstrapContext.getXATerminator());
            try {
                assertNotNull("bootstrapContext.createTimer() is null", bootstrapContext.createTimer());
            } catch (final UnavailableException e) {
                throw new ResourceAdapterInternalException("bootstrapContext.createTimer() threw an exception", e);
            }
        }

        public void stop() {
            assertTrue("RA was not started", started);
        }

        public void endpointActivation(final MessageEndpointFactory messageEndpointFactory, final ActivationSpec activationSpec) throws ResourceException {
            assertNotNull("messageEndpointFactory is null", messageEndpointFactory);
            assertNotNull("activationSpec is null", activationSpec);
            assertTrue("activationSpec should be an instance of FakeActivationSpec", activationSpec instanceof FakeActivationSpec);

            final MessageEndpoint endpoint = messageEndpointFactory.createEndpoint(null);
            assertNotNull("endpoint is null", endpoint);
            assertTrue("endpoint should be an instance of FakeMessageListener", endpoint instanceof FakeMessageListener);
        }

        public void endpointDeactivation(final MessageEndpointFactory messageEndpointFactory, final ActivationSpec activationSpec) {
            assertNotNull("messageEndpointFactory is null", messageEndpointFactory);
            assertNotNull("activationSpec is null", activationSpec);
            assertTrue("activationSpec should be an instance of FakeActivationSpec", activationSpec instanceof FakeActivationSpec);
        }

        public XAResource[] getXAResources(final ActivationSpec[] activationSpecs) throws ResourceException {
            return new XAResource[0];
        }
    }

    public static class FakeActivationSpec implements ActivationSpec {
        private FakeRA fakeRA;
        protected boolean validated;

        public void validate() throws InvalidPropertyException {
            validated = true;
        }

        public FakeRA getResourceAdapter() {
            return fakeRA;
        }

        public void setResourceAdapter(final ResourceAdapter resourceAdapter) {
            assertNotNull("resourceAdapter is null", resourceAdapter);
            assertTrue("resourceAdapter should be an instance of FakeRA", resourceAdapter instanceof FakeRA);
            this.fakeRA = (FakeRA) resourceAdapter;
            assertTrue("ActivationSpec has not been validated", validated);
        }
    }

    private MessageDrivenBean createJaxbMdb(final String ejbName, final String mdbClass, final String messageListenerInterface) {
        final MessageDrivenBean bean = new MessageDrivenBean(ejbName);
        bean.setEjbClass(mdbClass);
        bean.setMessagingType(messageListenerInterface);

        final ActivationConfig activationConfig = new ActivationConfig();
        activationConfig.getActivationConfigProperty().add(new ActivationConfigProperty("destination", ejbName));
        activationConfig.getActivationConfigProperty().add(new ActivationConfigProperty("destinationType", "jakarta.jms.Queue"));
        bean.setActivationConfig(activationConfig);

        return bean;
    }
}
