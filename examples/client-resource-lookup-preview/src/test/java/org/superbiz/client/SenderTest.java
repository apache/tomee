/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.client;

import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SenderTest {

    @BeforeClass
    public static void configureClientResources() {
        // can be set this way or with the key Resource/<type>
        // in fact we create on client side a mini jndi tree
        // the key is the jndi name (the one used for the lookup)
        System.setProperty("aConnectionFactory", "connectionfactory:org.apache.activemq.ActiveMQConnectionFactory:tcp://localhost:11616");
        System.setProperty("aQueue", "queue:org.apache.activemq.command.ActiveMQQueue:LISTENER");
    }

    @Test
    public void send() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
        final Context context = new InitialContext(properties);

        final Queue destination = (Queue) context.lookup("java:aQueue");
        assertNotNull(destination);
        assertEquals("LISTENER", destination.getQueueName());

        final ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup("java:aConnectionFactory");
        assertNotNull(connectionFactory);
    }
}
