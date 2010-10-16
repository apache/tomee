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

import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.DiscoveryListener;

/**
 * @version $Rev$ $Date$
 */
public class MulticastDiscoveryAgentTest extends TestCase {

    public void testNothing(){}

    public void _test() throws Exception {
        MulticastDiscoveryAgent[] agents = {agent("red"),agent("green"),agent("yellow"),agent("blue")};

        MulticastSearch multicast = new MulticastSearch();
        Filter filter = new Filter();

        System.out.println("uri = " + multicast.search(filter));
        System.out.println("uri = " + multicast.search(filter));
        System.out.println("uri = " + multicast.search(filter));
        System.out.println("uri = " + multicast.search(filter));

        Thread.sleep(2000);
        System.out.println("--");


        for (MulticastDiscoveryAgent agent : agents) {
            Thread.sleep(10000);
            System.out.println("--");
            agent.stop();
        }

        for (MulticastDiscoveryAgent agent : agents) {
            Thread.sleep(10000);
            System.out.println("--");
            agent.start();
        }

        Thread.sleep(10000);


    }

    private static class Filter implements MulticastSearch.Filter {
        private final Set<URI> seen = new HashSet<URI>();
        public boolean accept(URI service) {
            if (seen.contains(service)) return false;
            seen.add(service);
            return true;
        }
    }

    private MulticastDiscoveryAgent agent(String id) throws IOException, URISyntaxException, ServiceException {
        MulticastDiscoveryAgent agent = new MulticastDiscoveryAgent();
        agent.init(new Properties());
        agent.setDiscoveryListener(new MyDiscoveryListener(id));
        agent.registerService(new URI("ejbd://"+id+":4201"));
        agent.start();
        return agent;
    }

    private static class MyDiscoveryListener implements DiscoveryListener {
        private final String id;

        public MyDiscoveryListener(String id) {
            id += "        ";
            id = id.substring(0,8);
            this.id = id;
        }

        public void serviceAdded(URI service) {
            System.out.println(id + "add " + service.toString());
        }

        public void serviceRemoved(URI service) {
            System.out.println(id + "remove " + service.toString());
        }
    }

}
