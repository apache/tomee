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

import org.apache.activemq.ra.ActiveMQConnectionRequestInfo;
import org.apache.activemq.ra.MessageActivationSpec;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.resource.activemq.ActiveMQResourceAdapter;
import org.apache.openejb.resource.activemq.TomEEMessageActivationSpec;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.SimpleLog;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.reflection.Reflections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSConnectionFactoryDefinition;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSDestinationDefinition;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.Queue;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

@SimpleLog
@RunWith(ApplicationComposer.class)
@Classes(cdi = true, innerClassesAsBean = true)
public class MDBSpecTest {
    @Configuration
    public Properties config() {
        return new PropertiesBuilder()
                // .p("openejb.offline", "true") // helpful in dev but not working cause of dynamic resources

                .p("amq", "new://Resource?type=ActiveMQResourceAdapter")
                .p("amq.DataSource", "")
                .p("amq.BrokerXmlConfig", "broker:(vm://localhost)")

                .p("mdbs", "new://Container?type=MESSAGE")
                .p("mdbs.ResourceAdapter", "amq")

                .p("testcontainer", "new://Container?type=MANAGED")

                .build();
    }

    @Resource(name = "amq")
    private ActiveMQResourceAdapter amq;

    @Resource(name = "jms/input")
    private Queue destination;

    @Inject
    @JMSConnectionFactory("jms/ConnectionFactory")
    private JMSContext context;

    @Before
    public void resetLatch() {
        Listener.reset();
    }

    @Test
    public void checkConfig() throws InterruptedException {
        // first it works in term of communication
        context.createProducer().send(destination, "hello");
        assertTrue(Listener.sync());

        // then we should check we don't create a connection factory but use the config one
        // Note: if you have time use a custom connection factory to have a better tracking, for now it should be good enough otherwise
    }

    private Object createFactory(final ActiveMQResourceAdapter amq) {
        return Reflections.invokeByReflection(
                amq,
                "createConnectionFactory",
                new Class<?>[]{ActiveMQConnectionRequestInfo.class, MessageActivationSpec.class},
                new Object[]{null, new TomEEMessageActivationSpec() {{
                    setConnectionFactoryLookup("jms/XAConnectionFactory");
                }}});
    }

    @JMSDestinationDefinition(name = "jms/input", destinationName = "jms/input", interfaceName = "jakarta.jms.Queue")
    @JMSConnectionFactoryDefinition(name = "jms/ConnectionFactory", transactional = false)
    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
            @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/input"),
            @ActivationConfigProperty(propertyName = "connectionFactoryLookup", propertyValue = "jms/ConnectionFactory")
    })
    public static class Listener implements MessageListener {
        public static volatile CountDownLatch latch;
        public static volatile boolean ok = false;

        @Override
        public void onMessage(final Message message) {
            try {
                try {
                    final String body = message.getBody(String.class);
                    ok = "hello".equals(body);
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
}
