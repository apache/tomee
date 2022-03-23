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
package org.apache.openejb.resource.activemq;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.DeployApplication;
import org.apache.openejb.testing.ApplicationComposers;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Test;
import org.junit.runners.model.Statement;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// inspired from MessagingBeanTest in examples
public class ProperConnectionShutdownTest {
    @Test
    public void run() throws Throwable {
        final Thread[] threadsBefore = listThreads();
        final AtomicReference<Thread[]> threadWhile = new AtomicReference<>();

        // run test
        final Statement testInContainer = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                messages.sendMessage("Hello World!");
                messages.sendMessage("How are you?");

                threadWhile.set(listThreads());

                messages.sendMessage("Still spinning?");

                assertEquals(messages.receiveMessage(), "Hello World!");
                assertEquals(messages.receiveMessage(), "How are you?");
                assertEquals(messages.receiveMessage(), "Still spinning?");

                // all worked, now hold a connection
                // not daemon!
                // oops, I forgot to close it
                new Thread(messages::blockConnection).start();
            }
        };
        new DeployApplication(this, testInContainer, new ApplicationComposers(this)).evaluate();

        Thread.sleep(2250); // AMQ state (started) polling for transport thread is 1s
        while (Join.join("", listThreads()).contains("ActiveMQ Session Task")) { // let few sec to AMQ to leave the holding task
            Thread.sleep(1000);
        }

        int retry = 0;
        boolean threadsCompleted = false;
        while (retry < 30) {

            threadsCompleted = checkThreads(threadsBefore);
            if (threadsCompleted) break;
            retry++;

            Thread.sleep(1000);
        }

        assertTrue(threadsCompleted);
    }

    private boolean checkThreads(Thread[] threadsBefore) {
        // ensure no connection are leaking
        final Thread[] threadsAfter = listThreads();

        int countAMQ = 0;
        int countOthers = 0;
        for (final Thread t : threadsAfter) {
            if (!t.isAlive()) {
                continue;
            }
            if (t.getName().contains("AMQ") || t.getName().toLowerCase(Locale.ENGLISH).contains("activemq")) {
                countAMQ++;
            } else {
                countOthers++;
            }
        }

        if (countAMQ > 0) {
            return false;
        }

        // we expect PoolIdleReleaseTimer, CurrentTime and LogAsyncStream
        return countOthers <= threadsBefore.length + 3;
    }

    private Thread[] listThreads() {
        final Thread[] threads = new Thread[Thread.activeCount()];
        final int count = Thread.enumerate(threads);
        if (count < threads.length) {
            final Thread[] copy = new Thread[count];
            System.arraycopy(threads, 0, copy, 0, count);
            return copy;
        }
        return threads;
    }

    @EJB
    private Messages messages;

    @Configuration
    public Properties config() {
        return new PropertiesBuilder()
            .p("Default JMS Resource Adapter.BrokerXmlConfig", "broker:(tcp://localhost:" + NetworkUtil.getNextAvailablePort() + ")?useJmx=false")
            .build();
    }

    @Module
    @Classes(innerClassesAsBean = true)
    public WebApp app() {
        return new WebApp();
    }

    @Stateless
    public static class Messages {

        @Resource
        private ConnectionFactory connectionFactory;

        @Resource
        private Queue chatQueue;

        public void sendMessage(String text) throws JMSException {

            Connection connection = null;
            Session session = null;

            try {
                connection = connectionFactory.createConnection();
                connection.start();

                // Create a Session
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create a MessageProducer from the Session to the Topic or Queue
                MessageProducer producer = session.createProducer(chatQueue);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

                // Create a message
                TextMessage message = session.createTextMessage(text);

                // Tell the producer to send the message
                producer.send(message);
            } finally {
                // Clean up
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }
        }

        public String receiveMessage() throws JMSException {

            Connection connection = null;
            Session session = null;
            MessageConsumer consumer = null;
            try {
                connection = connectionFactory.createConnection();
                connection.start();

                // Create a Session
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create a MessageConsumer from the Session to the Topic or Queue
                consumer = session.createConsumer(chatQueue);

                // Wait for a message
                TextMessage message = (TextMessage) consumer.receive(1000);

                return message.getText();
            } finally {
                if (consumer != null) {
                    consumer.close();
                }
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }

        }

        public void blockConnection() {
            try {
                connectionFactory.createConnection();
            } catch (final JMSException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
