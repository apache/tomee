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
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.config.sys.ServiceProvider;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.EjbJar;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
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

    public void test(){}

    protected void _setUp() throws Exception {
        config = new ConfigurationFactory();
        assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        ServiceProvider provider = new ServiceProvider(EmailResourceAdapter.class, EmailResourceAdapter.class.getSimpleName(), "Resource");
        provider.getTypes().add(EmailResourceAdapter.class.getName());
        ServiceUtils.getServiceProviders().add(provider);
    }

    public void _testJmsMdbNoContainerConfigured() throws Exception {
        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new MessageDrivenBean(JmsBean.class));
        EjbJarInfo info = config.configureApplication(ejbJar);
//        assembler.createApplication(info);
    }

    public void _testConfiguredContainerSelection() throws Exception {

        // Create a JMS MDB Container
        MdbContainerInfo info = config.configureService(MdbContainerInfo.class);
        assertEquals(MessageListener.class.getName(), info.properties.get("MessageListenerInterface"));
        assembler.createContainer(info);

        // Create an Email MDB Container
        Container container = new Container("EmailContainer", "MESSAGE", null);
        Properties properties = container.getProperties();
        properties.setProperty("ResourceAdapter", EmailResourceAdapter.class.getSimpleName());
        properties.setProperty("MessageListenerInterface", EmailConsumer.class.getName());
        properties.setProperty("ActivationSpecClass", EmailAccountInfo.class.getName());
        assembler.createContainer(config.configureService(container, MdbContainerInfo.class));


    }


    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
            @ActivationConfigProperty(propertyName = "destination", propertyValue = "FooQueue")})
    public static class JmsBean implements MessageListener {

        public void onMessage(Message message) {
        }
    }

    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "address", propertyValue = "dblevins@apache.org")})
    public static class EmailBean implements EmailConsumer {

        public void receiveEmail(Properties headers, String body) {
        }
    }

    public static interface EmailConsumer {
        public void receiveEmail(Properties headers, String body);
    }

    public static class EmailResourceAdapter implements javax.resource.spi.ResourceAdapter {
        public boolean started;

        private final Map<String, EmailConsumer> consumers = new HashMap<String, EmailConsumer>();

        public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
        }

        public void stop() {
        }

        public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) throws ResourceException {
            EmailAccountInfo accountInfo = (EmailAccountInfo) activationSpec;

            EmailConsumer emailConsumer = (EmailConsumer) messageEndpointFactory.createEndpoint(null);
            consumers.put(accountInfo.getAddress(), emailConsumer);
        }

        public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
            EmailAccountInfo accountInfo = (EmailAccountInfo) activationSpec;

            EmailConsumer emailConsumer = consumers.remove(accountInfo.getAddress());
            MessageEndpoint endpoint = (MessageEndpoint) emailConsumer;
            endpoint.release();
        }

        public XAResource[] getXAResources(ActivationSpec[] activationSpecs) throws ResourceException {
            return new XAResource[0];
        }

        public void deliverEmail(Properties headers, String body) throws Exception {
            String to = headers.getProperty("To");

            EmailConsumer emailConsumer = consumers.get(to);

            if (emailConsumer == null) throw new Exception("No such account");

            MessageEndpoint endpoint = (MessageEndpoint) emailConsumer;

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

        public void setAddress(String address) {
            this.address = address;
        }

        public EmailResourceAdapter getResourceAdapter() {
            return emailResourceAdapter;
        }

        public void setResourceAdapter(javax.resource.spi.ResourceAdapter resourceAdapter) {
            this.emailResourceAdapter = (EmailResourceAdapter) resourceAdapter;
        }
    }

}
