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
package org.apache.openejb.server.discovery;

import junit.framework.TestCase;
import org.apache.openejb.server.DiscoveryListener;
import org.apache.openejb.server.DiscoveryRegistry;
import org.apache.openejb.util.Join;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
public class MultipointDiscoveryAgentTest extends TestCase {

    public void test() throws Exception {

        final URI testService = new URI("green://localhost:0");

        final int PEERS = 4;

        final CountDownLatch[] latches = {
                new CountDownLatch(PEERS + 1),
                new CountDownLatch(PEERS + 1)
        };

        final DiscoveryListener listener = new DiscoveryListener() {
            public void serviceAdded(URI service) {
                System.out.println("added = " + service);
                if (testService.equals(service)) {
                    latches[0].countDown();
                }
            }

            public void serviceRemoved(URI service) {
                System.out.println("removed = " + service);
                if (testService.equals(service)) {
                    latches[1].countDown();
                }
            }
        };

        final List<Node> nodes = new ArrayList<Node>();
        final Node root = new Node("0", listener);


        nodes.add(root);

        for (int i = 0; i < PEERS; i++) {
            final Node node = new Node("0", listener, root.getAgent().getPort());
            nodes.add(node);
        }

        final Node owner = nodes.get(nodes.size() / 2);


        for (int i = 0; i < 3; i++) {
            latches[0] = new CountDownLatch(PEERS + 1);
            latches[1] = new CountDownLatch(PEERS + 1);

            // OK, do the broadcast
            owner.getRegistry().registerService(testService);

            // Notification should have reached all participants
            assertTrue("round=" + i + ". Add failed", latches[0].await(30, TimeUnit.SECONDS));

            owner.getRegistry().unregisterService(testService);

            assertTrue("round=" + i + ". Remove failed", latches[1].await(60, TimeUnit.SECONDS));

            for (Node node : nodes) {
                final Set<URI> services = node.getRegistry().getServices();
                assertEquals("round=" + i + ". Service retained", 0, services.size());
            }

        }
    }

    public static class Node {
        private final MultipointDiscoveryAgent agent;
        private final DiscoveryRegistry registry;

        public Node(String p, DiscoveryListener listener, int... peers) throws Exception {
            this.agent = new MultipointDiscoveryAgent();
            final Properties props = new Properties();
            props.put("port", p);

            List<String> uris = new ArrayList<String>(peers.length);
            for (int port : peers) {
                uris.add("localhost:"+port);
            }

            props.put("initialServers", Join.join(",", uris));
            agent.init(props);

            this.registry = new DiscoveryRegistry(agent);
            this.registry.addDiscoveryListener(listener);
            agent.start();
        }

        public MultipointDiscoveryAgent getAgent() {
            return agent;
        }

        public DiscoveryRegistry getRegistry() {
            return registry;
        }
    }
}
