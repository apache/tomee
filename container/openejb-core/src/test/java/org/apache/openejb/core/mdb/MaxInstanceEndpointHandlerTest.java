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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.*;
import javax.management.ObjectName;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class MaxInstanceEndpointHandlerTest {

    private static final String TEXT = "foo";

    @Configuration
    public Properties config() {
        return new PropertiesBuilder()

            .p("sra", "new://Resource?type=ActiveMQResourceAdapter")
            .p("sra.threadPoolSize", "100")

            .p("mdbs", "new://Container?type=MESSAGE")
            .p("mdbs.ResourceAdapter", "sra")
            .p("mdbs.pool", "false")
            .p("mdbs.InstanceLimit", "30")
            .p("mdbs.activation.maxSessions", "50")

            .p("cf", "new://Resource?type=jakarta.jms.ConnectionFactory")
            .p("cf.ResourceAdapter", "sra")
            .p("cf.TransactionSupport", "none")
            .p("cf.ConnectionMaxWaitTime", "30 seconds")
            .p("cf.MaxThreadPoolSize", "40")
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

        for (int i = 0; i < 100; i++) {
            try (final Connection connection = cf.createConnection(); final Session session = connection.createSession()) {
                connection.start();
                final TextMessage textMessage = session.createTextMessage(TEXT);
                session.createProducer(destination).send(textMessage);
            }
        }

        // start MDB delivery
        setControl("start");

        assertTrue(Listener.sync());
        assertEquals(30, Listener.COUNTER.get());
    }

    private void setControl(final String action) throws Exception {
        LocalMBeanServer.get().invoke(
                new ObjectName("default:type=test"),
                action, new Object[0], new String[0]);
    }

    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "DeliveryActive", propertyValue = "false"),
            @ActivationConfigProperty(propertyName = "MdbJMXControl", propertyValue = "default:type=test"),
            @ActivationConfigProperty(propertyName = "destination", propertyValue = "target"),
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
    })
    public static class Listener implements MessageListener {
        public static CountDownLatch latch;

        static final AtomicLong COUNTER = new AtomicLong();

        @PostConstruct
        public void postConstruct() {
            COUNTER.incrementAndGet();
        }

        public static void reset() {
            latch = new CountDownLatch(100);
        }

        public static boolean sync() throws InterruptedException {
            latch.await(1, TimeUnit.MINUTES);
            return true;
        }

        @Override
        public void onMessage(Message message) {
            latch.countDown();
        }
    }

}