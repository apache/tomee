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
import org.apache.openejb.server.ServiceException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class MulticastDiscoveryAgentTest extends TestCase {

    //public void testNothing(){}

    public void test() throws Exception {
        final MulticastDiscoveryAgent[] agents = {agent("red"), agent("green"), agent("yellow"), agent("blue")};

        final MulticastSearch multicast = new MulticastSearch();
        final Filter filter = new Filter();

        System.out.println("uri = " + multicast.search(filter));
        System.out.println("uri = " + multicast.search(filter));
        System.out.println("uri = " + multicast.search(filter));
        System.out.println("uri = " + multicast.search(filter));

        Thread.sleep(2000);
        System.out.println("--");

        for (final MulticastDiscoveryAgent agent : agents) {
            Thread.sleep(2000);
            System.out.println("--");
            agent.stop();
        }

        for (final MulticastDiscoveryAgent agent : agents) {
            Thread.sleep(2000);
            System.out.println("--");
            agent.start();
        }

        Thread.sleep(2000);
        multicast.close();
    }

    private static class Filter implements MulticastSearch.Filter {

        private final Set<URI> seen = new HashSet<URI>();

        @Override
        public boolean accept(final URI service) {
            if (seen.contains(service))
                return false;
            seen.add(service);
            return true;
        }
    }

    private MulticastDiscoveryAgent agent(final String id) throws IOException, URISyntaxException, ServiceException {
        final MulticastDiscoveryAgent agent = new MulticastDiscoveryAgent();
        agent.init(new Properties());
        agent.setDiscoveryListener(new MyDiscoveryListener(id));
        agent.registerService(new URI("ejb:ejbd://" + id + ":4201"));
        agent.start();
        return agent;
    }

    private static class MyDiscoveryListener implements DiscoveryListener {

        private final String id;

        public MyDiscoveryListener(String id) {
            id += "        ";
            id = id.substring(0, 8);
            this.id = id;
        }

        @Override
        public void serviceAdded(final URI service) {
            System.out.println(id + "add " + service.toString());
        }

        @Override
        public void serviceRemoved(final URI service) {
            System.out.println(id + "remove " + service.toString());
        }
    }

}
