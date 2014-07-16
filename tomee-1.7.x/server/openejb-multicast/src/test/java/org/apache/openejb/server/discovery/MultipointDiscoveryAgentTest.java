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
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ManagedMBean;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.server.DiscoveryListener;
import org.apache.openejb.server.DiscoveryRegistry;
import org.apache.openejb.util.Join;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @version $Rev$ $Date$
 */
public class MultipointDiscoveryAgentTest extends TestCase {

//    static {
//        final FilteredHandler consoleHandler = new FilteredHandler(new ConsoleHandler(), new LogRecordFilter() {
//            @Override
//            public boolean accept(LogRecord record) {
//                return Thread.currentThread().getName().contains("red");
//            }
//        });
//        consoleHandler.setLevel(Level.FINEST);
//
//        final Logger logger = Logger.getLogger("OpenEJB.server.discovery");
//        logger.addHandler(consoleHandler);
//        logger.setLevel(Level.FINEST);
//        logger.setUseParentHandlers(false);
//    }

    public void test() throws Exception {
//        System.setProperty("logging.level.OpenEJB.server.discovery", "debug");

        final URI testService = new URI("green://localhost:0");

        final String[] names = {"red"};
        final int PEERS = names.length;

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
        final Node root = new Node(0, listener, "root");

        nodes.add(root);

        for (String name : names) {
            final Node node = new Node(0, listener, name, root.getURI());
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

    public void _debug() throws Exception {
        System.setProperty("log4j.category.OpenEJB.server.discovery", "debug");

        System.setProperty("log4j.appender.C.layout", "org.apache.log4j.PatternLayout");
        System.setProperty("log4j.appender.C.layout.ConversionPattern", "%d - %m%n");

        final URI greenService = new URI("green://localhost:5555");
        final Node green = new Node(5555, new Listener("green"), true, "green", 100, 2);

        green.getRegistry().registerService(greenService);

//        launch(green, "blue", 4444);
//        launch(green, "red", 6666);
//        launch(green, "yellow", 8888);
        final Node orange = launch(green, "orange", 7777);

        Thread.sleep(500000);

        orange.getAgent().stop();

        Thread.sleep(5000);

    }

    private Node launch(Node green, String color, int port) throws Exception {
        final URI orangeService = new URI(color + "://localhost:"+ port);
        final Node orange = new Node(port, new Listener(color), color, green.getURI());
        orange.getRegistry().registerService(orangeService);
        Thread.sleep(100);
        return orange;
    }

    public static class Node {
        private final MultipointDiscoveryAgent agent;
        private final DiscoveryRegistry registry;
        private final String name;

        public Node(int p, DiscoveryListener listener, String name, URI... uris) throws Exception {
            this(p, listener, false, name, 100, 2, uris);
        }

        public Node(int p, DiscoveryListener listener, boolean debug, String name, int heartRate, int maxMissHeartBeats, URI... uris) throws Exception {
            this.agent = new MultipointDiscoveryAgent(debug, name);
            this.name = name;
            final Properties props = new Properties();
            props.put("port", p+"");

            props.put("initialServers", Join.join(",", uris));
            props.put("max_missed_heartbeats", "1");
            props.put("heart_rate", "" + heartRate);
            props.put("max_missed_heartbeats", "" + maxMissHeartBeats);
            agent.init(props);

            this.registry = new DiscoveryRegistry(agent);
            this.registry.addDiscoveryListener(listener);


            agent.start();

//            final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
//            jmxName.set("type", "ServerService");
//            jmxName.set("name", "multipoint");
//            jmxName.set("port", Integer.toString(agent.getPort()));
//
//            MBeanServer server = LocalMBeanServer.get();
//            try {
//                final ObjectName objectName = jmxName.build();
//                server.registerMBean(new ManagedMBean(agent), objectName);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

        }

        public MultipointDiscoveryAgent getAgent() {
            return agent;
        }

        public DiscoveryRegistry getRegistry() {
            return registry;
        }

        public int getPort() {
            return agent.getPort();
        }

        public URI getURI() {
            return URI.create("conn://localhost:" + getPort() + "/" + name);
        }
    }

    private static class Listener implements DiscoveryListener {
        private final String name;

        public String getName() {
            return name;
        }

        private Listener(String name) {
            this.name = name;
        }

        public void serviceAdded(URI service) {
//            System.out.printf("[%s] added = %s\n", name, service);
        }

        public void serviceRemoved(URI service) {
//            System.out.printf("[%s] removed = %s\n", name, service);
        }

        @Override
        public String toString() {
            return name;
        }
    }


    public static interface LogRecordFilter {
        public boolean accept(LogRecord record);
    }

    public static class FilteredHandler extends Handler {

        private final Handler handler;
        private final LogRecordFilter filter;

        public FilteredHandler(Handler handler, LogRecordFilter filter) {
            this.handler = handler;
            this.filter = filter;
        }

        @Override
        public void publish(LogRecord record) {
            handler.publish(record);
//            if (filter.accept(record)) {
//            }
        }

        @Override
        public void flush() {
            handler.flush();
        }

        @Override
        public void close() throws SecurityException {
            handler.close();
        }

        @Override
        public void setFormatter(Formatter newFormatter) throws SecurityException {
            handler.setFormatter(newFormatter);
        }

        @Override
        public Formatter getFormatter() {
            return handler.getFormatter();
        }

        @Override
        public void setEncoding(String encoding) throws SecurityException, UnsupportedEncodingException {
            handler.setEncoding(encoding);
        }

        @Override
        public String getEncoding() {
            return handler.getEncoding();
        }

        @Override
        public void setFilter(Filter newFilter) throws SecurityException {
            handler.setFilter(newFilter);
        }

        @Override
        public Filter getFilter() {
            return handler.getFilter();
        }

        @Override
        public void setErrorManager(ErrorManager em) {
            handler.setErrorManager(em);
        }

        @Override
        public ErrorManager getErrorManager() {
            return handler.getErrorManager();
        }

        @Override
        public void setLevel(Level newLevel) throws SecurityException {
            handler.setLevel(newLevel);
        }

        @Override
        public Level getLevel() {
            return handler.getLevel();
        }

        @Override
        public boolean isLoggable(LogRecord record) {
            return handler.isLoggable(record);
        }
    }
}
