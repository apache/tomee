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
package org.apache.openejb.test.mdb;

import org.apache.openejb.test.TestManager;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.InitialContext;
import java.util.Properties;

public abstract class MdbTestClient extends org.apache.openejb.test.NamedTestCase {
    protected InitialContext initialContext;
    protected ConnectionFactory connectionFactory;


    public MdbTestClient(final String name) {
        super("MDB." + name);
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        final Properties properties = TestManager.getServer().getContextEnvironment();
        initialContext = new InitialContext(properties);
        connectionFactory = TestManager.getJms().getConnectionFactory();
    }

    protected Connection createConnection() throws JMSException {
        final Connection connection = connectionFactory.createConnection();
        connection.start();
        return connection;
    }
}
