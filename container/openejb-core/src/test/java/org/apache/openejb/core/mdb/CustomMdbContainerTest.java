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
import junit.framework.Assert;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.ConnectorModule;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.ResourceAdapter;
import org.apache.openejb.jee.InboundResource;
import org.apache.openejb.jee.MessageAdapter;
import org.apache.openejb.jee.MessageListener;
import org.apache.openejb.util.Join;

import javax.naming.InitialContext;
import javax.jms.ConnectionFactory;
import javax.annotation.Resource;
import javax.annotation.PostConstruct;
import javax.ejb.MessageDrivenContext;
import javax.ejb.MessageDriven;
import javax.ejb.ActivationConfigProperty;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.ResourceException;
import javax.transaction.xa.XAResource;
import java.util.Stack;
import java.util.List;
import java.util.Arrays;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

/**
 * @version $Rev$ $Date$
 */
public class CustomMdbContainerTest extends TestCase {
    public void test() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // Setup the descriptor information

        EmailBean.lifecycle.clear();


        AppModule app = new AppModule(this.getClass().getClassLoader(), "testapp");

        Connector connector = new Connector("email-ra");
        ResourceAdapter adapter = connector.setResourceAdapter(new ResourceAdapter(EmailResourceAdapter.class));
        InboundResource inbound = adapter.setInboundResourceAdapter(new InboundResource());
        MessageAdapter messageAdapter = inbound.setMessageAdapter(new MessageAdapter());
        MessageListener listener = messageAdapter.addMessageListener(new MessageListener(EmailConsumer.class, EmailAccountInfo.class));
        listener.getActivationSpec().addRequiredConfigProperty("address");
        app.getResourceModules().add(new ConnectorModule(connector));

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new MessageDrivenBean(EmailBean.class));
        app.getEjbModules().add(new EjbModule(ejbJar));

        AppInfo appInfo = config.configureApplication(app);
        assembler.createApplication(appInfo);

        InitialContext initialContext = new InitialContext();

        EmailResourceAdapter ra = (EmailResourceAdapter) initialContext.lookup("java:openejb/Resource/email-raRA");

        Properties headers = new Properties();
        headers.put("To", "dblevins@apache.org");
        headers.put("From", "dblevins@visi.com");
        headers.put("Subject", "Hello");

        ra.deliverEmail(headers, "How's it going?");

        Stack<Lifecycle> lifecycle = EmailBean.lifecycle;

        List expected = Arrays.asList(Lifecycle.values());

        Assert.assertEquals(Join.join("\n", expected), Join.join("\n", lifecycle));

    }

    public static enum Lifecycle {
        CONSTRUCTOR, INJECTION, POST_CONSTRUCT, ON_MESSAGE
    }

    @MessageDriven(activationConfig = {@ActivationConfigProperty(propertyName = "address", propertyValue = "dblevins@apache.org")})
    public static class EmailBean implements EmailConsumer {

        public static Lock lock = new ReentrantLock();
        public static Condition messageRecieved = lock.newCondition();

        private static Stack<Lifecycle> lifecycle = new Stack<Lifecycle>();

        public EmailBean() {
            lifecycle.push(Lifecycle.CONSTRUCTOR);
        }

        @Resource
        public void setMessageDrivenContext(MessageDrivenContext messageDrivenContext) {
            lifecycle.push(Lifecycle.INJECTION);
        }

        @PostConstruct
        public void init() {
            lifecycle.push(Lifecycle.POST_CONSTRUCT);
        }

        public void receiveEmail(Properties headers, String body) {
            lifecycle.push(Lifecycle.ON_MESSAGE);
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
