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
package org.apache.openejb.resource.activemq.jms2;

import org.apache.activemq.ActiveMQXAConnectionFactory;

import jakarta.jms.JMSContext;
import jakarta.jms.Session;
import jakarta.jms.XAJMSContext;
import java.net.URI;

public class TomEEXAConnectionFactory extends ActiveMQXAConnectionFactory {
    public TomEEXAConnectionFactory() {
        super();
    }

    public TomEEXAConnectionFactory(final String userName, final String password, final String brokerURL) {
        super(userName, password, brokerURL);
    }

    public TomEEXAConnectionFactory(final String userName, final String password, final URI brokerURL) {
        super(userName, password, brokerURL);
    }

    public TomEEXAConnectionFactory(final String brokerURL) {
        super(brokerURL);
    }

    public TomEEXAConnectionFactory(final URI brokerURL) {
        super(brokerURL);
    }

    @Override
    public JMSContext createContext() {
        return new JMSContextImpl(this, Session.AUTO_ACKNOWLEDGE, null, null, true);
    }

    @Override
    public JMSContext createContext(final int sessionMode) {
        return new JMSContextImpl(this, sessionMode, null, null, true);
    }

    @Override
    public JMSContext createContext(final String userName, final String password) {
        return new JMSContextImpl(this, Session.AUTO_ACKNOWLEDGE, userName, password, true);
    }

    @Override
    public JMSContext createContext(final String userName, final String password, final int sessionMode) {
        return new JMSContextImpl(this, sessionMode, userName, password, true);
    }

    @Override
    public XAJMSContext createXAContext() {
        return new XAJMSContextImpl(this, Session.SESSION_TRANSACTED, userName, password);
    }

    @Override
    public XAJMSContext createXAContext(String userName, String password) {
        return new XAJMSContextImpl(this, Session.SESSION_TRANSACTED, userName, password);
    }
}
