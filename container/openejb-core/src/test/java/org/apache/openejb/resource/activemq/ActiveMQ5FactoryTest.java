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
package org.apache.openejb.resource.activemq;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.security.JaasAuthenticationPlugin;
import org.junit.Test;

import java.net.URI;

import static org.apache.openejb.util.NetworkUtil.getNextAvailablePort;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ActiveMQ5FactoryTest {
    @Test
    public void setPlugins() throws Exception {
        final URI brokerURI = new URI("amq5factory:broker:(tcp://localhost:" + getNextAvailablePort() + ")?" +
                "amq.plugins=jaas&" +
                "jaas.class=" + JaasAuthenticationPlugin.class.getName() + "&" +
                "jaas.discoverLoginConfig=false");
        final BrokerService bs = new ActiveMQ5Factory().createBroker(brokerURI);
        bs.stop();
        ActiveMQ5Factory.brokers.remove(brokerURI);
        assertNotNull(bs.getPlugins());
        assertEquals(1, bs.getPlugins().length);
        assertTrue(JaasAuthenticationPlugin.class.isInstance(bs.getPlugins()[0]));
        assertFalse(JaasAuthenticationPlugin.class.cast(bs.getPlugins()[0]).isDiscoverLoginConfig()); // default is true
    }
}
