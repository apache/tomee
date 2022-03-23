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
package org.apache.openejb.core.mdb;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.ConnectorModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.InboundResourceadapter;
import org.apache.openejb.jee.MessageAdapter;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.MessageListener;
import org.apache.openejb.jee.ResourceAdapter;
import org.apache.openejb.util.Join;
import org.junit.Assert;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.ejb.MessageDrivenContext;
import javax.naming.InitialContext;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.InvalidPropertyException;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @version $Rev$ $Date$
 */
public class NoMessageDeliveryTest extends TestCase {

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        OpenEJB.destroy();
    }

    public void test() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // Setup the descriptor information

        EmailBean.lifecycle.clear();


        final AppModule app = new AppModule(this.getClass().getClassLoader(), "testapp");

        final Connector connector = new Connector("email-ra");
        final ResourceAdapter adapter = new ResourceAdapter(EmailResourceAdapter.class);
        connector.setResourceAdapter(adapter);
        final InboundResourceadapter inbound = adapter.setInboundResourceAdapter(new InboundResourceadapter());
        final MessageAdapter messageAdapter = inbound.setMessageAdapter(new MessageAdapter());
        final MessageListener listener = messageAdapter.addMessageListener(new MessageListener(EmailConsumer.class, EmailAccountInfo.class));
        listener.getActivationSpec().addRequiredConfigProperty("address");
        app.getConnectorModules().add(new ConnectorModule(connector));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new MessageDrivenBean(EmailBean.class));
        app.getEjbModules().add(new EjbModule(ejbJar));

        final AppInfo appInfo = config.configureApplication(app);
        assembler.createApplication(appInfo);

        final InitialContext initialContext = new InitialContext();

        final EmailResourceAdapter ra = (EmailResourceAdapter) initialContext.lookup("java:openejb/Resource/email-raRA");

        final Properties headers = new Properties();
        headers.put("To", "dblevins@apache.org");
        headers.put("From", "dblevins@visi.com");
        headers.put("Subject", "Hello");

        ra.deliverEmail(headers, "How's it going?");

        final Stack<Lifecycle> lifecycle = EmailBean.lifecycle;

        final List expected = Arrays.asList(Lifecycle.CONSTRUCTOR, Lifecycle.INJECTION, Lifecycle.POST_CONSTRUCT);

        Assert.assertEquals(Join.join("\n", expected), Join.join("\n", lifecycle));
    }

    public enum Lifecycle {
        CONSTRUCTOR, INJECTION, POST_CONSTRUCT, ON_MESSAGE
    }

    @MessageDriven(activationConfig = {@ActivationConfigProperty(propertyName = "address", propertyValue = "dblevins@apache.org")})
    public static class EmailBean implements EmailConsumer {

        public static Lock lock = new ReentrantLock();
        public static Condition messageRecieved = lock.newCondition();

        private static final Stack<Lifecycle> lifecycle = new Stack<Lifecycle>();

        public EmailBean() {
            lifecycle.push(Lifecycle.CONSTRUCTOR);
        }

        @Resource
        public void setMessageDrivenContext(final MessageDrivenContext messageDrivenContext) {
            lifecycle.push(Lifecycle.INJECTION);
        }

        @PostConstruct
        public void init() {
            lifecycle.push(Lifecycle.POST_CONSTRUCT);
        }

        public void receiveEmail(final Properties headers, final String body) {
            lifecycle.push(Lifecycle.ON_MESSAGE);
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
