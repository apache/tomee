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
package org.apache.openejb.resource.activemq;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.network.NetworkConnector;
import org.apache.activemq.security.JaasAuthenticationPlugin;
import org.junit.Test;

import java.net.URI;
import java.net.URLEncoder;

import static java.util.Arrays.asList;
import static org.apache.openejb.util.NetworkUtil.getNextAvailablePort;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ActiveMQ5FactoryTest {
    @Test
    public void systemUsage() throws Exception {
        final URI brokerURI = new URI("amq5factory:broker:(tcp://localhost:" + getNextAvailablePort() + ")?" +
                "systemUsage=true&systemUsage.memory.limit=1024");
        final BrokerService bs = new ActiveMQ5Factory().createBroker(brokerURI);
        bs.stop();
        ActiveMQ5Factory.brokers.remove(brokerURI);
        assertEquals(1024, bs.getSystemUsage().getMemoryUsage().getLimit());
    }

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

    @Test
    public void duplex() throws Exception {
        final int port = getNextAvailablePort();
        for (final boolean b : asList(true, false)) {
            // broker:(tcp://localhost:${port})?networkConnectorURIs=static%3A%2F%2Ftcp%3A%2F%2Flocalhost%3A${port}%3Fduplex%3Dtrue
            final URI brokerURI = new URI("amq5factory:broker:(tcp://localhost:" + port + ")?" +
                    "networkConnectorURIs=" + URLEncoder.encode("static://tcp://localhost:" + port + "?duplex=" + b, "UTF-8"));
            final BrokerService bs = new ActiveMQ5Factory().createBroker(brokerURI);
            bs.stop();
            ActiveMQ5Factory.brokers.remove(brokerURI);
            final NetworkConnector nc = bs.getNetworkConnectors().iterator().next();
            assertEquals("duplex is " + b, b, nc.isDuplex());
        }
    }
}
