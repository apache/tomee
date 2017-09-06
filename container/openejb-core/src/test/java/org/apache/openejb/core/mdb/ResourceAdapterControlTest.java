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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

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
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(ApplicationComposer.class)
public class ResourceAdapterControlTest {
    
    private static final Logger logger = Logger.getLogger(ResourceAdapterControlTest.class.getName());
    
    @Resource(name = "ResourceAdapterControlTest/test/ejb/Mdb")
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

                .p("cf", "new://Resource?type=javax.jms.ConnectionFactory")
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
                    getActivationConfig().addProperty("MdbActiveOnStartup", "false");
                    getActivationConfig().addProperty("MdbJMXControl", "default:type=test");
                }});
            }});
    }

    @Test
    public void ensureControl() throws Exception {
        Mdb.awaiter.messages.clear();

        assertFalse(Mdb.awaiter.messages.poll(), sendAndWait("Will be received after", 10, TimeUnit.SECONDS));

        setControl("start");
        assertTrue(Mdb.awaiter.semaphore.tryAcquire(1, TimeUnit.MINUTES));
        assertEquals("Will be received after", Mdb.awaiter.messages.poll());

        final long start = System.currentTimeMillis();
        assertTrue(sendAndWait("First", 1, TimeUnit.MINUTES));
        assertEquals("First", Mdb.awaiter.messages.poll());
        final long end = System.currentTimeMillis();

        setControl("stop");
        // default would be wait 10s, but if machine is slow we compute it from the first msg stats
        final long waitWithoutResponse = Math.max(10, 5 * (end - start) / 1000);
        logger.info("We'll wait " + waitWithoutResponse + "s to get a message on a stopped listener");
        assertFalse(Mdb.awaiter.messages.poll(), sendAndWait("Will be received after", waitWithoutResponse, TimeUnit.SECONDS));
        assertNull(Mdb.awaiter.messages.poll());

        setControl("start");
        assertTrue(sendAndWait("Second", 1, TimeUnit.MINUTES));
        assertEquals("Will be received after", Mdb.awaiter.messages.poll());

        assertTrue(Mdb.awaiter.semaphore.tryAcquire(1, TimeUnit.MINUTES));
        assertEquals("Second", Mdb.awaiter.messages.poll());
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
        Connection c = null;
        try {
            c = connectionFactory.createConnection();
            Session session = null;

            try {
                session = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = null;
                try {
                    producer = session.createProducer(queue);
                    logger.log(Level.INFO, "Sending Message {0}", txt);
                    producer.send(session.createTextMessage(txt));
                } finally {
                    if (producer != null) {
                        producer.close();
                    }
                }
            } finally {
                if (session != null) {
                    session.close();
                }
            }
        } finally {
            if (c != null) {
                c.close();
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
                String text = TextMessage.class.cast(message).getText();
                logger.log(Level.INFO, "Got Messag: {0}", text);
                awaiter.messages.add(text);
                logger.log(Level.INFO, "Mssages on store: {0}", ArrayUtils.toString(awaiter.messages.toArray(new String[0])) );
            } catch (final JMSException e) {
                throw new IllegalStateException(e);
            } finally {
                awaiter.semaphore.release();
            }
        }
    }

    public static class MessageAwaiter {
        private final Semaphore semaphore = new Semaphore(0);
        private ConcurrentLinkedQueue<String> messages = new ConcurrentLinkedQueue<String>();
    }
}
