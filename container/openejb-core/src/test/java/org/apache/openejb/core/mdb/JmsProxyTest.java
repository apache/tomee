/**
 *
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

import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;

public class JmsProxyTest extends JmsTest {

    public void testProxy() throws Exception {
        // create reciever object
        final JmsProxyTest.TestObject testObject = new JmsProxyTest.TestObject("foo");
        final MdbInvoker mdbInvoker = new MdbInvoker(connectionFactory, testObject);

        // Create a Session
        final Connection connection = connectionFactory.createConnection();
        connection.start();
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the request Queue
        final Destination requestQueue = session.createQueue(REQUEST_QUEUE_NAME);
        final MessageConsumer consumer = session.createConsumer(requestQueue);
        consumer.setMessageListener(mdbInvoker);

        // create in invoker
        final JmsProxyTest.TestInterface testInterface = MdbProxy.newProxyInstance(JmsProxyTest.TestInterface.class, connectionFactory, REQUEST_QUEUE_NAME);
        assertEquals("foobar", testInterface.echo("bar"));
        assertEquals("foobar", testInterface.echo("bar"));
        assertEquals("foobar", testInterface.echo("bar"));
        assertEquals("foobar", testInterface.echo("bar"));
        assertEquals("foobar", testInterface.echo("bar"));
    }

    public static interface TestInterface {
        String echo(String msg);
    }

    public static class TestObject implements JmsProxyTest.TestInterface {
        private final String prefix;


        public TestObject(final String prefix) {
            this.prefix = prefix;
        }

        public String echo(final String msg) {
            return prefix + msg;
        }
    }
}
