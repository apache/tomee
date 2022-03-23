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

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.SimpleLog;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.MessageDriven;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

@SimpleLog
@Classes(innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class MDBPlaceholderTest {
    @Resource(name = "overriden")
    private Queue queue;

    @Resource
    private ConnectionFactory connectionFactory;

    @Test
    public void sendMessage() throws Exception {
        PlaceholdedMdb.called = false;

        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            final TextMessage requestMessage = session.createTextMessage("sent");
            producer = session.createProducer(queue);
            producer.send(requestMessage);

            // wait
            PlaceholdedMdb.latch.await(1, TimeUnit.MINUTES);
            assertTrue(PlaceholdedMdb.called);
        } finally {
            MdbUtil.close(producer);
            MdbUtil.close(session);
            MdbUtil.close(connection);
        }
    }

    @Configuration
    public Properties p() {
        return new PropertiesBuilder()
                .p("Default JMS Resource Adapter.BrokerXmlConfig", "broker:(tcp://localhost:" + NetworkUtil.getNextAvailablePort() + ")?useJmx=false")
                .p("overriden", "new://Resource?type=Queue")
                .p("my.custom.key", "overriden")
                .build();
    }

    @MessageDriven(activationConfig = {
            @jakarta.ejb.ActivationConfigProperty(propertyName = "maxSessions", propertyValue = "1"),
            @jakarta.ejb.ActivationConfigProperty(propertyName = "maxMessagesPerSessions", propertyValue = "1"),
            @jakarta.ejb.ActivationConfigProperty(propertyName = "destination", propertyValue = "${my.custom.key}")
    })
    public static class PlaceholdedMdb implements MessageListener {
        private static volatile boolean called = false;
        private static final CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void onMessage(final Message message) {
            called = true;
            latch.countDown();
        }
    }
}
