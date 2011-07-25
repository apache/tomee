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
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.util.NetworkUtil;

import static org.apache.openejb.util.Join.join;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.MessageDrivenContext;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @version $Rev$ $Date$
 */
public class JmsMdbContainerTest extends TestCase {
    public void test() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        // define props for RA in order to change the default activeMQ port
        Properties props = new Properties();
        String brokerAddress = NetworkUtil.getLocalAddress("tcp://", "");
        String brokerXmlConfig = "broker:(" + brokerAddress + ")?useJmx=false";
        props.put("BrokerXmlConfig", brokerXmlConfig);

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        assembler.createResource(config.configureService(ResourceInfo.class, "Default Unmanaged JDBC Database",
                new Properties(), "Default Unmanaged JDBC Database", "DataSource"));
        assembler.createResource(config.configureService(ResourceInfo.class, "Default JMS Resource Adapter",
                props, "Default JMS Resource Adapter", "ActiveMQResourceAdapter"));

        // Setup the descriptor information

        WidgetBean.lifecycle.clear();

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new MessageDrivenBean(WidgetBean.class));

        assembler.createApplication(config.configureApplication(ejbJar));

        InitialContext initialContext = new InitialContext();

        ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup("java:openejb/Resource/Default JMS Connection Factory");

        sendMessage(connectionFactory, "WidgetBean", "test");

        Stack<Lifecycle> lifecycle = WidgetBean.lifecycle;

        List expected = Arrays.asList(Lifecycle.values());

        assertEquals(join("\n", expected), join("\n", lifecycle));

    }

    private void sendMessage(ConnectionFactory connectionFactory, String bean, String text) throws JMSException, InterruptedException {
        WidgetBean.lock.lock();

        try {
            Connection connection = connectionFactory.createConnection();
            connection.start();

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Queue queue = session.createQueue(bean);

            // Create a MessageProducer from the Session to the Topic or Queue
            MessageProducer producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            // Create a message
            TextMessage message = session.createTextMessage(text);

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

    public static class WidgetBean implements javax.jms.MessageListener {

        public static Lock lock = new ReentrantLock();
        public static Condition messageRecieved = lock.newCondition();

        private static Stack<Lifecycle> lifecycle = new Stack<Lifecycle>();

        @Resource
        ConnectionFactory connectionFactory;

        public WidgetBean() {
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

        public void onMessage(Message message) {
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
