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

import org.apache.activemq.ra.ActiveMQConnectionFactory;
import org.apache.activemq.ra.ActiveMQConnectionRequestInfo;
import org.apache.activemq.ra.ActiveMQManagedConnectionFactory;

import javax.jms.JMSContext;
import javax.jms.Session;
import javax.resource.spi.ConnectionManager;

public class TomEERAConnectionFactory extends ActiveMQConnectionFactory {
    public TomEERAConnectionFactory(final ActiveMQManagedConnectionFactory factory, final ConnectionManager manager,
                                    final ActiveMQConnectionRequestInfo connectionRequestInfo) {
        super(factory, manager, connectionRequestInfo);
    }

    @Override
    public JMSContext createContext() {
        return new JMSContextImpl(this, Session.AUTO_ACKNOWLEDGE, null, null, false);
    }

    @Override
    public JMSContext createContext(final int sessionMode) {
        return new JMSContextImpl(this, sessionMode, null, null, false);
    }

    @Override
    public JMSContext createContext(final String userName, final String password) {
        return new JMSContextImpl(this, Session.AUTO_ACKNOWLEDGE, userName, password, false);
    }

    @Override
    public JMSContext createContext(final String userName, final String password, final int sessionMode) {
        return new JMSContextImpl(this, sessionMode, userName, password, false);
    }
}
