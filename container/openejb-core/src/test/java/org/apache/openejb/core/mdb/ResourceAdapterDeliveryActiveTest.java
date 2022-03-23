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

import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.*;
import javax.management.ObjectName;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.Assert.*;

import java.lang.IllegalStateException;

@RunWith(ApplicationComposer.class)
public class ResourceAdapterDeliveryActiveTest {
    private static final Logger logger = Logger.getLogger(ResourceAdapterDeliveryActiveTest.class.getName());
    @Resource(name = "ResourceAdapterDeliveryActiveTest/test/ejb/Mdb")
    private Queue queue;

    @Resource
    private ConnectionFactory connectionFactory;

    @Configuration
    public Properties config() {
        return new PropertiesBuilder()
                .p("ra", "new://Resource?type=ActiveMQResourceAdapter")
                .p("ra.brokerXmlConfig", "broker:(vm://localhost)?useJmx=false&persistent=false")
                .p("ra.serverUrl", "vm://localhost")

                .p("mdb", "new://Container?type=MESSAGE")
                .p("mdb.resourceAdapter", "ra")
                .p("mdb.InstanceLimit", "1")

                .p("cf", "new://Resource?type=jakarta.jms.ConnectionFactory")
                .p("cf.resourceAdapter", "ra")

                .p("openejb.deploymentId.format", "{appId}/{ejbJarId}/{ejbName}")
                .build();
    }

    @Module
    @Classes(value = Mdb.class)
    public EjbModule app() {
        return new EjbModule(
            new EjbJar("test") {{
                addEnterpriseBean(new MessageDrivenBean("ejb/Mdb", Mdb.class) {{
                    setActivationConfig(new ActivationConfig());
                    getActivationConfig().addProperty("DeliveryActive", "false");
                    getActivationConfig().addProperty("MdbJMXControl", "default:type=test");
                }});
            }});
    }

    @Test
    public void ensureControl() throws Exception {
        assertFalse(Mdb.awaiter.message, sendAndWait("Will be received after", 10, TimeUnit.SECONDS));

        setControl("start");
        assertTrue(Mdb.awaiter.semaphore.tryAcquire(1, TimeUnit.MINUTES));
        assertEquals("Will be received after", Mdb.awaiter.message);

        final long start = System.currentTimeMillis();
        assertTrue(sendAndWait("First", 1, TimeUnit.MINUTES));
        assertEquals("First", Mdb.awaiter.message);
        final long end = System.currentTimeMillis();

        Mdb.awaiter.message = null;
        setControl("stop");
        // default would be wait 10s, but if machine is slow we compute it from the first msg stats
        final long waitWithoutResponse = Math.max(10, 5 * (end - start) / 1000);
        System.out.println("We'll wait " + waitWithoutResponse + "s to get a message on a stopped listener");
        assertFalse(Mdb.awaiter.message, sendAndWait("Will be received after", waitWithoutResponse, TimeUnit.SECONDS));
        assertNull(Mdb.awaiter.message);

        setControl("start");
        assertTrue(sendAndWait("Second", 1, TimeUnit.MINUTES));
        assertEquals("Will be received after", Mdb.awaiter.message);

        Mdb.awaiter.message = null;
        assertTrue(Mdb.awaiter.semaphore.tryAcquire(1, TimeUnit.MINUTES));
        assertEquals("Second", Mdb.awaiter.message);
    }

    private void setControl(final String action) throws Exception {
        LocalMBeanServer.get().invoke(
                new ObjectName("default:type=test"),
                action, new Object[0], new String[0]);
    }

    private boolean sendAndWait(final String second, final long wait, final TimeUnit unit) throws JMSException {
        doSend(second);
        try {
            return Mdb.awaiter.semaphore.tryAcquire(wait, unit);
        } catch (final InterruptedException e) {
            Thread.interrupted();
            fail();
            return false;
        }
    }

    private void doSend(final String txt) throws JMSException {
        try (Connection c = connectionFactory.createConnection()) {
            try (Session session = c.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                try (MessageProducer producer = session.createProducer(queue)) {
                    producer.send(session.createTextMessage(txt));
                }
            }
        }
    }

    @MessageDriven(name = "ejb/Mdb", activationConfig = {
            @ActivationConfigProperty(propertyName = "maxSessions", propertyValue = "1"),
            @ActivationConfigProperty(propertyName = "maxMessagesPerBatch", propertyValue = "1")
    })
    public static class Mdb implements MessageListener {
        static final MessageAwaiter awaiter = new MessageAwaiter();

        @Override
        public synchronized void onMessage(final Message message) {
            try {
                awaiter.message = TextMessage.class.cast(message).getText();
            } catch (final JMSException e) {
                throw new IllegalStateException(e);
            } finally {
                awaiter.semaphore.release();
            }
        }
    }

    public static class MessageAwaiter {
        private final Semaphore semaphore = new Semaphore(0);
        private volatile String message;
    }
}
