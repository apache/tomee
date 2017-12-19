/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.mdb;

import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.management.ObjectName;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class PoolEndpointHandlerTest {

    private static final String TEXT = "foo";

    @Configuration
    public Properties config() {
        return new PropertiesBuilder()

                .p("amq", "new://Resource?type=ActiveMQResourceAdapter")
                .p("amq.DataSource", "")
                .p("amq.BrokerXmlConfig", "broker:(vm://localhost)")

                .p("target", "new://Resource?type=Queue")

                .p("mdbs", "new://Container?type=MESSAGE")
                .p("mdbs.ResourceAdapter", "amq")
                .p("mdbs.pool", "true")

                .p("cf", "new://Resource?type=" + ConnectionFactory.class.getName())
                .p("cf.ResourceAdapter", "amq")
                .build();
    }

    @Module
    public MessageDrivenBean jar() {
        return new MessageDrivenBean(Listener.class);
    }

    @Resource(name = "target")
    private Queue destination;

    @Resource(name = "cf")
    private ConnectionFactory cf;

    @Before
    public void resetLatch() {
        Listener.reset();
    }

    @Test
    public void shouldSendMessage() throws Exception {
        assertNotNull(cf);

        for (int i = 0; i < 1_000; i++) {
            final Connection connection = cf.createConnection();
            try {
                final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                final MessageProducer producer = session.createProducer(destination);
                producer.send(session.createTextMessage(TEXT));
            } finally {
                connection.close();
            }
        }

        // start MDB delivery
        setControl("start");

        assertTrue(Listener.sync());
        assertEquals(10, Listener.COUNTER.get());
    }

    private void setControl(final String action) throws Exception {
        LocalMBeanServer.get().invoke(
                new ObjectName("default:type=test"),
                action, new Object[0], new String[0]);
    }

    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
            @ActivationConfigProperty(propertyName = "destination", propertyValue = "target"),
            @ActivationConfigProperty(propertyName = "DeliveryActive", propertyValue = "false"),
            @ActivationConfigProperty(propertyName = "MdbJMXControl", propertyValue = "default:type=test")
    })
    public static class Listener implements MessageListener {
        public static CountDownLatch latch;
        private static final List<Boolean> BOOLEANS = new CopyOnWriteArrayList<>();

        static final AtomicLong COUNTER = new AtomicLong();

        @PostConstruct
        public void postConstruct() {
            COUNTER.incrementAndGet();
        }

        @Override
        public void onMessage(final Message message) {
            try {
                try {
                    boolean ok = TextMessage.class.isInstance(message) && TEXT.equals(TextMessage.class.cast(message).getText());
                    BOOLEANS.add(ok);
                } catch (final JMSException e) {
                }
            } finally {
                latch.countDown();
            }
        }

        public static void reset() {
            latch = new CountDownLatch(1000);
            BOOLEANS.clear();
        }

        public static boolean sync() throws InterruptedException {
            latch.await(1, TimeUnit.MINUTES);
            for (boolean result : BOOLEANS) {
                if(!result) {
                  return false;
                }
            }
            return true;
        }
    }

}