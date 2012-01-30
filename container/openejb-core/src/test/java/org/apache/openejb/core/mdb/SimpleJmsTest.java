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

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;

public class SimpleJmsTest extends JmsTest {

    public void testProxy() throws Exception {
        // create reciever object
        TestObject testObject = new TestObject("foo");
        MdbInvoker mdbInvoker = new MdbInvoker(connectionFactory, testObject);

        // Create a Session
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the request Queue
        Destination requestQueue = session.createQueue(REQUEST_QUEUE_NAME);
        MessageConsumer consumer = session.createConsumer(requestQueue);
        consumer.setMessageListener(mdbInvoker);

        // create in invoker
        TestInterface testInterface = MdbProxy.newProxyInstance(TestInterface.class, connectionFactory, REQUEST_QUEUE_NAME);
        assertEquals("foobar", testInterface.echo("bar"));
        assertEquals("foobar", testInterface.echo("bar"));
        assertEquals("foobar", testInterface.echo("bar"));
        assertEquals("foobar", testInterface.echo("bar"));
        assertEquals("foobar", testInterface.echo("bar"));
    }

    public static interface TestInterface {
        String echo(String msg);
    }

    public static class TestObject implements TestInterface {
        private final String prefix;


        public TestObject(String prefix) {
            this.prefix = prefix;
        }

        public String echo(String msg) {
            return prefix + msg;
        }
    }
}
