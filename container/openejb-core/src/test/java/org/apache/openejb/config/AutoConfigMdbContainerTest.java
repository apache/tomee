/**
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
package org.apache.openejb.config;

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.sys.ServiceProvider;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.MessageDrivenBean;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.InvalidPropertyException;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class AutoConfigMdbContainerTest extends TestCase {

    private ConfigurationFactory config;
    private Assembler assembler;

    public void test() {
    }

    protected void _setUp() throws Exception {
        config = new ConfigurationFactory();
        assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final ServiceProvider provider = new ServiceProvider(EmailResourceAdapter.class, EmailResourceAdapter.class.getSimpleName(), "Resource");
        provider.getTypes().add(EmailResourceAdapter.class.getName());
        ServiceUtils.getServiceProviders().add(provider);
    }

    public void _testJmsMdbNoContainerConfigured() throws Exception {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new MessageDrivenBean(JmsBean.class));
        final EjbJarInfo info = config.configureApplication(ejbJar);
//        assembler.createApplication(info);
    }

    public void _testConfiguredContainerSelection() throws Exception {

        // Create a JMS MDB Container
        final MdbContainerInfo info = config.configureService(MdbContainerInfo.class);
        assertEquals(MessageListener.class.getName(), info.properties.get("MessageListenerInterface"));
        assembler.createContainer(info);

        // Create an Email MDB Container
        final Container container = new Container("EmailContainer", "MESSAGE", null);
        final Properties properties = container.getProperties();
        properties.setProperty("ResourceAdapter", EmailResourceAdapter.class.getSimpleName());
        properties.setProperty("MessageListenerInterface", EmailConsumer.class.getName());
        properties.setProperty("ActivationSpecClass", EmailAccountInfo.class.getName());
        assembler.createContainer(config.configureService(container, MdbContainerInfo.class));


    }


    @MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "FooQueue")})
    public static class JmsBean implements MessageListener {

        public void onMessage(final Message message) {
        }
    }

    @MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "address", propertyValue = "dblevins@apache.org")})
    public static class EmailBean implements EmailConsumer {

        public void receiveEmail(final Properties headers, final String body) {
        }
    }

    public static interface EmailConsumer {
        public void receiveEmail(Properties headers, String body);
    }

    public static class EmailResourceAdapter implements jakarta.resource.spi.ResourceAdapter {
        public boolean started;

        private final Map<String, EmailConsumer> consumers = new HashMap<>();

        public void start(final BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
        }

        public void stop() {
        }

        public void endpointActivation(final MessageEndpointFactory messageEndpointFactory, final ActivationSpec activationSpec) throws ResourceException {
            final EmailAccountInfo accountInfo = (EmailAccountInfo) activationSpec;

            final EmailConsumer emailConsumer = (EmailConsumer) messageEndpointFactory.createEndpoint(null);
            consumers.put(accountInfo.getAddress(), emailConsumer);
        }

        public void endpointDeactivation(final MessageEndpointFactory messageEndpointFactory, final ActivationSpec activationSpec) {
            final EmailAccountInfo accountInfo = (EmailAccountInfo) activationSpec;

            final EmailConsumer emailConsumer = consumers.remove(accountInfo.getAddress());
            final MessageEndpoint endpoint = (MessageEndpoint) emailConsumer;
            endpoint.release();
        }

        public XAResource[] getXAResources(final ActivationSpec[] activationSpecs) throws ResourceException {
            return new XAResource[0];
        }

        public void deliverEmail(final Properties headers, final String body) throws Exception {
            final String to = headers.getProperty("To");

            final EmailConsumer emailConsumer = consumers.get(to);

            if (emailConsumer == null) throw new Exception("No such account");

            final MessageEndpoint endpoint = (MessageEndpoint) emailConsumer;

            endpoint.beforeDelivery(EmailConsumer.class.getMethod("receiveEmail", Properties.class, String.class));
            emailConsumer.receiveEmail(headers, body);
            endpoint.afterDelivery();
        }
    }

    public static class EmailAccountInfo implements ActivationSpec {
        private EmailResourceAdapter emailResourceAdapter;

        private String address;

        public void validate() throws InvalidPropertyException {
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(final String address) {
            this.address = address;
        }

        public EmailResourceAdapter getResourceAdapter() {
            return emailResourceAdapter;
        }

        public void setResourceAdapter(final jakarta.resource.spi.ResourceAdapter resourceAdapter) {
            this.emailResourceAdapter = (EmailResourceAdapter) resourceAdapter;
        }
    }

}
