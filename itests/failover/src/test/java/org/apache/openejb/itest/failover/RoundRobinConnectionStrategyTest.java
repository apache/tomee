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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.itest.failover;

import org.apache.openejb.client.Client;
import org.apache.openejb.client.RemoteInitialContextFactory;
import org.apache.openejb.client.event.ClusterMetaDataUpdated;
import org.apache.openejb.client.event.Observes;
import org.apache.openejb.itest.failover.ejb.Calculator;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.Zips;
import org.apache.openejb.server.control.StandaloneServer;
import org.junit.Assert;
import org.junit.Test;

import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.openejb.util.NetworkUtil.getNextAvailablePort;

public class RoundRobinConnectionStrategyTest {

    static final Logger logger = Logger.getLogger("org.apache.openejb.client");

    static {
        final ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.FINER);
        logger.addHandler(consoleHandler);
        logger.setLevel(Level.FINER);
        logger.setUseParentHandlers(false);
    }

    @Test
    public void test() throws Exception {

//        To run in an IDE, uncomment and update this line
//        System.setProperty("version", "4.0.0-beta-3-SNAPSHOT");
        System.setProperty("openejb.client.connection.strategy", "roundrobin");

        final File zip = Repository.getArtifact("org.apache.openejb", "openejb-standalone", "zip");
        final File app = Repository.getArtifact("org.apache.openejb.itests", "failover-ejb", "jar");

        final File dir = Files.tmpdir();

        final StandaloneServer root;
        {
            final String name = "root";
            final File home = new File(dir, name);

            Files.mkdir(home);
            Zips.unzip(zip, home, true);

            root = new StandaloneServer(home, home);
            root.killOnExit();
            root.ignoreOut();
            root.setProperty("name", name);
            root.setProperty("openejb.extract.configuration", "false");

            final StandaloneServer.ServerService multipoint = root.getServerService("multipoint");
            multipoint.setBind("localhost");
            multipoint.setPort(getNextAvailablePort());
            multipoint.setDisabled(false);
            multipoint.set("discoveryName", name);

            logger.info("Starting Root server");
            root.start();
        }

        final Services services = new Services();
        Client.addEventObserver(services);

        final Map<String, StandaloneServer> servers = new HashMap<String, StandaloneServer>();
        for (String name : new String[]{"red", "green", "blue"}) {

            final File home = new File(dir, name);
            Files.mkdir(home);
            Zips.unzip(zip, home, true);

            final StandaloneServer server = new StandaloneServer(home, home);
            server.killOnExit();
            server.ignoreOut();
            server.setProperty("name", name);
            server.setProperty("openejb.extract.configuration", "false");

            IO.copy(app, Files.path(home, "apps", "itest.jar"));
            IO.copy(IO.read("<openejb><Deployments dir=\"apps/\"/></openejb>"), Files.path(home, "conf", "openejb.xml"));

            final StandaloneServer.ServerService ejbd = server.getServerService("ejbd");
            ejbd.setBind("localhost");
            ejbd.setDisabled(false);
            ejbd.setPort(getNextAvailablePort());
            ejbd.setThreads(5);

            final URI uri = URI.create(String.format("ejbd://%s:%s/%s", ejbd.getBind(), ejbd.getPort(), name));
            ejbd.set("discovery", "ejb:" + uri);
            services.add(uri);
            server.getContext().set(URI.class, uri);

            final StandaloneServer.ServerService multipoint = server.getServerService("multipoint");
            multipoint.setPort(getNextAvailablePort());
            multipoint.setDisabled(false);
            multipoint.set("discoveryName", name);
            multipoint.set("initialServers", "localhost:" + root.getServerService("multipoint").getPort());

            servers.put(name, server);

            logger.info(String.format("Starting %s server", name));

            server.start(1, TimeUnit.MINUTES);
        }


        System.setProperty("openejb.client.requestretry", "true");
        System.setProperty("openejb.client.connection.strategy", "roundrobin");

        logger.info("Beginning Test");

        final Properties environment = new Properties();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        environment.put(Context.PROVIDER_URL, "ejbd://localhost:" + servers.values().iterator().next().getServerService("ejbd").getPort() + "/provider");

        final InitialContext context = new InitialContext(environment);
        final Calculator bean = (Calculator) context.lookup("CalculatorBeanRemote");

        for (Map.Entry<String, StandaloneServer> entry : servers.entrySet()) {
            final String name = entry.getKey();
            final StandaloneServer server = entry.getValue();
            final URI serverURI = server.getContext().get(URI.class);

            logger.info("Waiting for updated list");
            services.assertServices(10, TimeUnit.SECONDS, new CalculatorCallable(bean), 500);

            logger.info("Asserting balance");
            assertBalance(bean, services.get().size());

            logger.info("Shutting down " + name);
            server.kill();
            services.remove(serverURI);
        }

        logger.info("All Servers Shutdown");

        try {
            logger.info("Making one last request, expecting complete failover");

            final String name = bean.name();
            Assert.fail("Server should be destroyed: " + name);
        } catch (EJBException e) {
            logger.info(String.format("Pass.  Request resulted in %s: %s", e.getCause().getClass().getSimpleName(), e.getMessage()));
            // good
        }


        for (Map.Entry<String, StandaloneServer> entry : servers.entrySet()) {
            final String name = entry.getKey();
            final StandaloneServer server = entry.getValue();
            final URI serverURI = server.getContext().get(URI.class);

            logger.info(String.format("Starting %s server", name));

            server.start(1, TimeUnit.MINUTES);
            services.add(serverURI);

            logger.info("Waiting for updated list");
            services.assertServices(10, TimeUnit.SECONDS, new CalculatorCallable(bean), 500);

            logger.info("Asserting balance");
            assertBalance(bean, services.get().size());
        }
    }

    private void assertBalance(Calculator bean, int size) {
        final int expectedInvocations = 100;
        final int tolerance = 2;
        final int totalInvocations = size * expectedInvocations + (size * tolerance);


        // Verify the work was split up more or less evenly (within a tolerance of 2)
        for (Map.Entry<String, AtomicInteger> entry : invoke(bean, totalInvocations).entrySet()) {

            final int actualInvocations = entry.getValue().get();

            Assert.assertTrue(String.format("Expected %s invocations, received %s", expectedInvocations, actualInvocations), actualInvocations >= expectedInvocations && actualInvocations <= (expectedInvocations + tolerance + size) );
        }
    }


    private Map<String, AtomicInteger> invoke(Calculator bean, int max) {
        final Map<String, AtomicInteger> invocations = new HashMap<String, AtomicInteger>();
        for (int i = 0; i < max; i++) {
            final String name = bean.name();

            if (!invocations.containsKey(name)) {
                invocations.put(name, new AtomicInteger());
            }

            invocations.get(name).incrementAndGet();
        }

        for (Map.Entry<String, AtomicInteger> entry : invocations.entrySet()) {
            logger.info(String.format("Server %s invoked %s times", entry.getKey(), entry.getValue()));
        }

        return invocations;
    }

    public static class Services {
        static final Logger logger = Logger.getLogger(Services.class.getName());

        private final ReentrantLock lock = new ReentrantLock();
        private final Condition condition = lock.newCondition();

        private final Set<URI> expected = new HashSet<URI>();

        public Services() {
        }

        public Set<URI> get() {
            return expected;
        }

        public boolean add(URI uri) {
            return expected.add(uri);
        }

        public boolean remove(URI o) {
            return expected.remove(o);
        }

        public void observe(@Observes ClusterMetaDataUpdated updated) {
            final URI[] locations = updated.getClusterMetaData().getLocations();
            final Set<URI> found = new HashSet<URI>(Arrays.asList(locations));

            if (expected.equals(found)) {
                lock.lock();
                try {
                    condition.signal();
                } finally {
                    lock.unlock();
                }
            }
        }

        public Set<URI> diff(Set<URI> a, Set<URI> b) {
            final Set<URI> diffs = new HashSet<URI>();
            for (URI uri : b) {
                if (!a.contains(uri)) diffs.add(uri);
            }

            return diffs;
        }

        public void assertServices(long timeout, TimeUnit unit, Callable callable) {
            assertServices(timeout, unit, callable, 10);
        }

        public void assertServices(long timeout, TimeUnit unit, Callable callable, int delay) {
            final ClientThread client = new ClientThread(callable);
            client.delay(delay);
            client.start();
            try {
                Assert.assertTrue(String.format("services failed to come online: waited %s %s", timeout, unit), await(timeout, unit));
            } catch (InterruptedException e) {
                Thread.interrupted();
                Assert.fail("Interrupted");
            } finally {
                client.stop();
            }
        }

        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            lock.lock();
            try {
                return condition.await(timeout, unit);
            } finally {
                lock.unlock();
            }
        }
    }

    private static class CalculatorCallable implements Callable {
        private final Calculator bean;

        public CalculatorCallable(Calculator bean) {
            this.bean = bean;
        }

        @Override
        public Object call() throws Exception {
            return bean.name();
        }
    }
}
