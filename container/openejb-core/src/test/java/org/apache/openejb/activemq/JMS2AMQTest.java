/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.activemq;

import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.SimpleLog;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContextsService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.EJB;
import jakarta.ejb.MessageDriven;
import jakarta.ejb.Singleton;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.jms.XAConnectionFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionScoped;
import jakarta.transaction.UserTransaction;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SimpleLog
@RunWith(ApplicationComposer.class)
public class JMS2AMQTest {
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

                .p("cf", "new://Resource?type=" + ConnectionFactory.class.getName())
                .p("cf.ResourceAdapter", "amq")

                .p("xaCf", "new://Resource?class-name=" + ActiveMQXAConnectionFactory.class.getName())
                .p("xaCf.BrokerURL", "vm://localhost")

                .build();
    }

    @Module
    @Classes(cdi = true, value = { JustHereToCheckDeploymentIsOk.class, ProducerBean.class })
    public MessageDrivenBean jar() {
        return new MessageDrivenBean(Listener.class);
    }

    @Resource(name = "target")
    private Queue destination;

    @Resource(name = "target2")
    private Queue destination2;

    @Resource(name = "target3")
    private Queue destination3;

    @Resource(name = "xaCf")
    private XAConnectionFactory xacf;

    @Resource(name = "cf")
    private ConnectionFactory cf;

    @Inject
    @JMSConnectionFactory("cf")
    private JMSContext context;

    @Inject // just there to ensure the injection works and we don't require @JMSConnectionFactory
    private JMSContext defaultContext;

    @Inject
    private JustHereToCheckDeploymentIsOk session;

    @Resource
    private UserTransaction ut;

    @EJB
    private ProducerBean pb;

    @Before
    public void resetLatch() {
        Listener.reset();
    }

    @Test
    public void serialize() throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        final JMSContext c = SerializationUtils.deserialize(SerializationUtils.serialize(Serializable.class.cast(context)));
        ut.begin();
        session.ok();
        ut.commit();
    }

    @Test
    public void cdi() throws InterruptedException {
        final String text = TEXT + "3";

        final AtomicReference<Throwable> error = new AtomicReference<>();
        final CountDownLatch ready = new CountDownLatch(1);
        final CountDownLatch over = new CountDownLatch(1);
        new Thread() {
            {
                setName(JMS2AMQTest.class.getName() + ".cdi#receiver");
            }

            @Override
            public void run() {
                final ContextsService contextsService = WebBeansContext.currentInstance().getContextsService();
                contextsService.startContext(RequestScoped.class, null); // spec defines it for request scope an transaction scope
                try {
                    ready.countDown();
                    assertEquals(text, context.createConsumer(destination3).receiveBody(String.class, TimeUnit.MINUTES.toMillis(1)));

                    // ensure we dont do a NPE if there is nothing to read
                    assertNull(context.createConsumer(destination3).receiveBody(String.class, 100));
                } catch (final Throwable t) {
                    error.set(t);
                } finally {
                    contextsService.endContext(RequestScoped.class, null);
                    over.countDown();
                }
            }
        }.start();

        ready.await(1, TimeUnit.MINUTES);
        sleep(150); // just to ensure we called receive already

        // now send the message
        try (final JMSContext context = cf.createContext()) {
            context.createProducer().send(destination3, text);
        } catch (final JMSRuntimeException ex) {
            fail(ex.getMessage());
        }

        over.await(1, TimeUnit.MINUTES);

        // ensure we got the message and no exception
        final Throwable exception = error.get();
        if (exception != null) {
            exception.printStackTrace();
        }
        assertNull(exception == null ? "ok" : exception.getMessage(), exception);
    }

    @Test
    public void cdiListenerAPI() throws InterruptedException {
        final String text = TEXT + "4";

        final AtomicReference<Throwable> error = new AtomicReference<>();
        final CountDownLatch ready = new CountDownLatch(1);
        final CountDownLatch over = new CountDownLatch(1);
        new Thread() {
            {
                setName(JMS2AMQTest.class.getName() + ".cdiListenerAPI#receiver");
            }

            @Override
            public void run() {
                final ContextsService contextsService = WebBeansContext.currentInstance().getContextsService();
                contextsService.startContext(RequestScoped.class, null);
                try {
                    final JMSConsumer consumer = context.createConsumer(destination3);
                    consumer.setMessageListener(new MessageListener() {
                        @Override
                        public void onMessage(final Message message) {
                            try {
                                assertEquals(text, message.getBody(String.class));
                            } catch (final Throwable e) {
                                error.set(e);
                            } finally {
                                over.countDown();
                                consumer.close();
                            }
                        }
                    });
                    ready.countDown();
                } catch (final Throwable t) {
                    error.set(t);
                } finally {
                    try {
                        over.await(1, TimeUnit.MINUTES);
                    } catch (final InterruptedException e) {
                        Thread.interrupted();
                    }
                    contextsService.endContext(RequestScoped.class, null);
                }
            }
        }.start();

        ready.await(1, TimeUnit.MINUTES);

        // now send the message
        try (final JMSContext context = cf.createContext()) {
            context.createProducer().send(destination3, text);
        } catch (final JMSRuntimeException ex) {
            fail(ex.getMessage());
        }

        over.await(1, TimeUnit.MINUTES);

        // ensure we got the message and no exception
        final Throwable exception = error.get();
        if (exception != null) {
            exception.printStackTrace();
        }
        assertNull(exception == null ? "ok" : exception.getMessage(), exception);
    }

    @Test
    public void sendToMdb() throws Exception {
        try (final JMSContext context = cf.createContext()) {
            context.createProducer().send(destination, TEXT);
            assertTrue(Listener.sync());
        } catch (final JMSRuntimeException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void sendMessageToMdb() throws Exception {
        try (final JMSContext context = cf.createContext()) {
            Message message = context.createMessage();
            message.setStringProperty("text", TEXT);
            context.createProducer().send(destination, message);
            assertTrue(Listener.sync());
        } catch (final JMSRuntimeException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void sendToMdbWithDefaultCf() throws Exception {
        defaultContext.createProducer().send(destination, TEXT);
        assertTrue(Listener.sync());
    }

    @Test
    public void sendToMdbWithTxAndCheckLeaks() throws Exception {
        for (int i = 0; i < 50; i++) {
            pb.sendInNewTx();
        }

        assertTrue(Listener.sync());

        final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> objs = mBeanServer.queryNames(new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost,endpoint=dynamicProducer,*"), null);
        Assert.assertEquals(0, objs.size());
    }

    @Test
    public void receive() throws InterruptedException {
        final String text = TEXT + "2";
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final CountDownLatch ready = new CountDownLatch(1);
        final CountDownLatch over = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                {
                    setName(JMS2AMQTest.class.getName() + ".receive#receiver");
                }

                try (final JMSContext context = cf.createContext()) {
                    try (final JMSConsumer consumer = context.createConsumer(destination2)) {
                        ready.countDown();
                        assertEquals(text, consumer.receiveBody(String.class, TimeUnit.MINUTES.toMillis(1)));
                    }
                } catch (final Throwable ex) {
                    error.set(ex);
                } finally {
                    over.countDown();
                }
            }
        }.start();

        ready.await(1, TimeUnit.MINUTES);
        sleep(150); // just to ensure we called receive already

        // now send the message
        try (final JMSContext context = cf.createContext()) {
            context.createProducer().send(destination2, text);
        } catch (final JMSRuntimeException ex) {
            fail(ex.getMessage());
        }

        over.await(1, TimeUnit.MINUTES);

        // ensure we got the message and no exception
        final Throwable exception = error.get();
        if (exception != null) {
            exception.printStackTrace();
        }
        assertNull(exception == null ? "ok" : exception.getMessage(), exception);
    }

    @Test
    public void receiveGetBody() throws InterruptedException {
        final String text = TEXT + "2";
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final CountDownLatch ready = new CountDownLatch(1);
        final CountDownLatch over = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                {
                    setName(JMS2AMQTest.class.getName() + ".receiveGetBody#receiver");
                }

                try (final JMSContext context = cf.createContext()) {
                    try (final JMSConsumer consumer = context.createConsumer(destination2)) {
                        ready.countDown();
                        final Message receive = consumer.receive(TimeUnit.MINUTES.toMillis(1));
                        assertEquals(text, receive.getBody(String.class));
                    }
                } catch (final Throwable ex) {
                    error.set(ex);
                } finally {
                    over.countDown();
                }
            }
        }.start();

        ready.await(1, TimeUnit.MINUTES);
        sleep(150); // just to ensure we called receive already

        // now send the message
        try (final JMSContext context = cf.createContext()) {
            context.createProducer().send(destination2, text);
        } catch (final JMSRuntimeException ex) {
            fail(ex.getMessage());
        }

        over.await(1, TimeUnit.MINUTES);

        // ensure we got the message and no exception
        final Throwable exception = error.get();
        if (exception != null) {
            exception.printStackTrace();
        }
        assertNull(exception == null ? "ok" : exception.getMessage(), exception);
    }

    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
            @ActivationConfigProperty(propertyName = "destination", propertyValue = "target")
    })
    public static class Listener implements MessageListener {
        public static volatile CountDownLatch latch;
        public static volatile boolean ok = false;

        @Override
        public void onMessage(final Message message) {
            try {
                try {
                    ok = (TextMessage.class.isInstance(message)
                            && TEXT.equals(TextMessage.class.cast(message).getText())
                            && TEXT.equals(message.getBody(String.class)))
                            || message.getStringProperty("text").equals(TEXT);
                } catch (final JMSException e) {
                    // no-op
                }
            } finally {
                latch.countDown();
            }
        }

        public static void reset() {
            latch = new CountDownLatch(1);
            ok = false;
        }

        public static boolean sync() throws InterruptedException {
            latch.await(1, TimeUnit.MINUTES);
            return ok;
        }
    }

    @TransactionScoped
    public static class JustHereToCheckDeploymentIsOk implements Serializable {
        @Inject
        private JMSContext context;

        public void ok() {
            assertNotNull(context);
        }
    }

    @Singleton
    public static class ProducerBean {
        @Inject
        @JMSConnectionFactory("cf")
        private JMSContext context;

        @Resource(name = "target")
        private Queue destination;

        @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
        public void sendInNewTx() {
            context.createProducer().send(destination, TEXT);
        }
    }
}
