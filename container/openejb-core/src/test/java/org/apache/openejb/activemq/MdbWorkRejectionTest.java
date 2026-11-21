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
package org.apache.openejb.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.SimpleLog;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.JMSContext;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.Queue;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * Saturates the thread pool of the resource adapter so that the work of an MDB with
 * maxSessions=1 is rejected, then checks the MDB still consumes once the pool recovers.
 */
@SimpleLog
@RunWith(ApplicationComposer.class)
@Classes(cdi = true, innerClassesAsBean = true)
public class MdbWorkRejectionTest {
    @Configuration
    public Properties config() {
        return new PropertiesBuilder()
                .p("amq", "new://Resource?type=ActiveMQResourceAdapter")
                .p("amq.DataSource", "")
                .p("amq.BrokerXmlConfig", "broker:(vm://localhost)?useJmx=false&persistent=false")
                .p("amq.threadPoolSize", "2")
                .p("amq.QueueSize", "1")
                .p("amq.OfferTimeout", System.getProperty("offerTimeout", "1 second"))

                .p("mdbs", "new://Container?type=MESSAGE")
                .p("mdbs.ResourceAdapter", "amq")
                .build();
    }

    @Test
    public void mdbConsumesAgainAfterRejectedWork() throws Exception {
        // a plain connection, so the sends are not held back by the transaction of the test method
        final JMSContext context = new ActiveMQConnectionFactory("vm://localhost?create=false").createContext();
        final Queue block = context.createQueue("block");
        final Queue victim = context.createQueue("victim");
        // occupy both pool threads and the single queue slot
        for (int i = 0; i < 3; i++) {
            context.createProducer().send(block, "block-" + i);
        }
        assertTrue("blockers did not start", Blocker.STARTED.tryAcquire(2, 10, TimeUnit.SECONDS));

        // the work for this message can't be scheduled and is rejected after the offer timeout
        context.createProducer().send(victim, "rejected");
        Thread.sleep(3000);

        // the external system recovers
        Blocker.RELEASE.countDown();
        assertTrue("blockers did not drain", Blocker.STARTED.tryAcquire(1, 10, TimeUnit.SECONDS));

        context.createProducer().send(victim, "after-recovery");
        assertTrue("MDB with maxSessions=1 never consumed again", Victim.RECEIVED.await(20, TimeUnit.SECONDS));
        context.close();
    }

    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
            @ActivationConfigProperty(propertyName = "destination", propertyValue = "block"),
            @ActivationConfigProperty(propertyName = "maxSessions", propertyValue = "3")
    })
    public static class Blocker implements MessageListener {
        static final Semaphore STARTED = new Semaphore(0);
        static final CountDownLatch RELEASE = new CountDownLatch(1);

        @Override
        public void onMessage(final Message message) {
            STARTED.release();
            try {
                RELEASE.await();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
            @ActivationConfigProperty(propertyName = "destination", propertyValue = "victim"),
            @ActivationConfigProperty(propertyName = "maxSessions", propertyValue = "1")
    })
    public static class Victim implements MessageListener {
        static final CountDownLatch RECEIVED = new CountDownLatch(1);

        @Override
        public void onMessage(final Message message) {
            try {
                if ("after-recovery".equals(message.getBody(String.class))) {
                    RECEIVED.countDown();
                }
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
