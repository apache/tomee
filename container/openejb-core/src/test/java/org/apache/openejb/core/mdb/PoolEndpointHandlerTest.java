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

import org.apache.openejb.core.mdb.connector.api.InboundListener;
import org.apache.openejb.core.mdb.connector.api.SampleConnection;
import org.apache.openejb.core.mdb.connector.api.SampleConnectionFactory;
import org.apache.openejb.core.mdb.connector.impl.SampleActivationSpec;
import org.apache.openejb.core.mdb.connector.impl.SampleManagedConnectionFactory;
import org.apache.openejb.core.mdb.connector.impl.SampleResourceAdapter;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.Queue;
import javax.management.ObjectName;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class PoolEndpointHandlerTest {

    private static final String TEXT = "foo";
    private static CountDownLatch latch;
    private static AtomicLong counter = new AtomicLong();

    @Configuration
    public Properties config() {
        return new PropertiesBuilder()

            .p("sra", "new://Resource?class-name=" + SampleResourceAdapter.class.getName())

            .p("mdbs", "new://Container?type=MESSAGE")
            .p("mdbs.ResourceAdapter", "sra")
            .p("mdbs.pool", "true")
            .p("mdbs.MaxSize", "10")
            .p("mdbs.MinSize", "2")
            .p("mdbs.StrictPooling", "true")
            .p("mdbs.ActivationSpecClass", SampleActivationSpec.class.getName())
            .p("mdbs.MessageListenerInterface", InboundListener.class.getName())

            .p("cf", "new://Resource?type=" + SampleConnectionFactory.class.getName() + "&class-name=" + SampleManagedConnectionFactory.class.getName())
            .p("cf.ResourceAdapter", "sra")
            .p("cf.TransactionSupport", "none")
            .build();
    }

    @Module
    public MessageDrivenBean jar() {
        return new MessageDrivenBean(Listener.class);
    }

    @Resource(name = "target")
    private Queue destination;

    @Resource(name = "cf")
    private SampleConnectionFactory cf;

    @Test
    public void shouldSendMessage() throws Exception {
        assertNotNull(cf);

        for (int i = 0; i < 100; i++) {
            final SampleConnection connection = cf.getConnection();
            try {
                connection.sendMessage(TEXT);
            } finally {
                connection.close();
            }
        }

        latch = new CountDownLatch(100);

        // start MDB delivery
        setControl("start");

        latch.await(30, TimeUnit.SECONDS);
        System.out.println("Actual instances: " + counter.get());
        assertTrue("Expected at least 2 instances, actual:" + counter.get(), counter.get() >= 2);
        assertTrue("Expected at most 10 instances, actual:" + counter.get(), counter.get() <= 10);
    }

    private void setControl(final String action) throws Exception {
        LocalMBeanServer.get().invoke(
                new ObjectName("default:type=test"),
                action, new Object[0], new String[0]);
    }

    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "DeliveryActive", propertyValue = "false"),
            @ActivationConfigProperty(propertyName = "MdbJMXControl", propertyValue = "default:type=test")
    })
    public static class Listener implements InboundListener {

        public Listener() {
            counter.incrementAndGet();
        }

        @Override
        public void receiveMessage(String message) {
            latch.countDown();
            if (!TEXT.equals(message)) {
                throw new IllegalStateException(String.format("Expected %s, received %s", TEXT, message));
            }

            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}