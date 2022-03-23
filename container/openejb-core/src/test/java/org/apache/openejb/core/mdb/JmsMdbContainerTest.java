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
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.util.NetworkUtil;
import org.junit.AfterClass;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.MessageDrivenContext;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import javax.naming.InitialContext;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.apache.openejb.util.Join.join;

/**
 * @version $Rev$ $Date$
 */
public class JmsMdbContainerTest extends TestCase {

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        // define props for RA in order to change the default activeMQ port
        final Properties props = new Properties();
        final String brokerAddress = NetworkUtil.getLocalAddress("tcp://", "");
        final String brokerXmlConfig = "broker:(" + brokerAddress + ")?useJmx=false";
        props.put("BrokerXmlConfig", brokerXmlConfig);
        props.put("StartupTimeout", 10000);

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        assembler.createResource(config.configureService(ResourceInfo.class, "Default Unmanaged JDBC Database",
            new Properties(), "Default Unmanaged JDBC Database", "DataSource"));
        assembler.createResource(config.configureService(ResourceInfo.class, "Default JMS Resource Adapter",
            props, "Default JMS Resource Adapter", "ActiveMQResourceAdapter"));

        // Setup the descriptor information

        WidgetBean.lifecycle.clear();

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new MessageDrivenBean(WidgetBean.class));

        assembler.createApplication(config.configureApplication(ejbJar));

        final InitialContext initialContext = new InitialContext();

        final ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup("java:openejb/Resource/Default JMS Connection Factory");

        sendMessage(connectionFactory, "WidgetBean", "test");

        final Stack<Lifecycle> lifecycle = WidgetBean.lifecycle;

        final List expected = Arrays.asList(Lifecycle.values());

        assertEquals(join("\n", expected), join("\n", lifecycle));

    }

    private void sendMessage(final ConnectionFactory connectionFactory, final String bean, final String text) throws JMSException, InterruptedException {
        WidgetBean.lock.lock();

        try {
            final Connection connection = connectionFactory.createConnection();
            connection.start();

            // Create a Session
            final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            final Queue queue = session.createQueue(bean);

            // Create a MessageProducer from the Session to the Topic or Queue
            final MessageProducer producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            // Create a message
            final TextMessage message = session.createTextMessage(text);

            // Tell the producer to send the message
            producer.send(message);

            WidgetBean.messageRecieved.await();
        } finally {
            WidgetBean.lock.unlock();
        }
    }

    public static enum Lifecycle {
        CONSTRUCTOR, INJECTION, POST_CONSTRUCT, ON_MESSAGE
    }

    public static class WidgetBean implements jakarta.jms.MessageListener {

        public static Lock lock = new ReentrantLock();
        public static Condition messageRecieved = lock.newCondition();

        private static final Stack<Lifecycle> lifecycle = new Stack<Lifecycle>();

        @Resource
        ConnectionFactory connectionFactory;

        public WidgetBean() {
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

        @Override
        public void onMessage(final Message message) {
            lifecycle.push(Lifecycle.ON_MESSAGE);

            lock.lock();
            try {
                messageRecieved.signalAll();
            } finally {
                lock.unlock();
            }
        }

    }
}
