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
import org.apache.openejb.util.OpenEjbVersion;
import org.apache.openejb.server.control.StandaloneServer;
import org.junit.Assert;
import org.junit.Test;

import jakarta.ejb.EJBException;
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

public class RandomConnectionStrategyTest {

    private static final Logger logger = Logger.getLogger("org.apache.openejb.client");

    private static final int CLIENT_DELAY = 60;

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
//        System.setProperty("version", OpenEjbVersion.get().getVersion());
        System.setProperty("openejb.client.connection.strategy", "roundrobin");

        final File zip = Repository.getArtifact("org.apache.tomee", "openejb-standalone", "zip");
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
            root.getJvmOpts().add("-Dopenejb.classloader.forced-load=org.apache.openejb");
            root.ignoreOut();
            root.setProperty("name", name);
            root.setProperty("openejb.extract.configuration", "false");

            final StandaloneServer.ServerService multipoint = root.getServerService("multipoint");
            multipoint.setBind("localhost");
            multipoint.setPort(getNextAvailablePort());
            multipoint.setDisabled(false);
            multipoint.set("discoveryName", name);

            logger.info("Starting Root server");

            // Wait for it to start before continuing, otherwise test may fail in slower machines.
            root.start(1, TimeUnit.MINUTES);
        }

        final Services services = new Services();
        Client.addEventObserver(services);

        final Map<String, StandaloneServer> servers = new HashMap<>();
        for (final String name : new String[]{"red", "green", "blue"}) {

            final File home = new File(dir, name);
            Files.mkdir(home);
            Zips.unzip(zip, home, true);

            final StandaloneServer server = new StandaloneServer(home, home);
            server.killOnExit();
            server.ignoreOut();
            server.setProperty("name", name);
            server.setProperty("openejb.extract.configuration", "false");
            server.getJvmOpts().add("-Dopenejb.classloader.forced-load=org.apache.openejb");

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
        System.setProperty("openejb.client.connection.strategy", "random");

        logger.info("Beginning Test");

        final Properties environment = new Properties();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        environment.put(Context.PROVIDER_URL, "ejbd://localhost:" + servers.values().iterator().next().getServerService("ejbd").getPort() + "/provider");

        final InitialContext context = new InitialContext(environment);
        final Calculator bean = (Calculator) context.lookup("CalculatorBeanRemote");

        // Lets restart one server. This will change the cluster configuration so when we call
        // 'bean' business methods new ClusterMetaDataUpdated event will be triggered
        servers.get("red").kill();
        servers.get("red").start(1, TimeUnit.MINUTES);

        for (final Map.Entry<String, StandaloneServer> entry : servers.entrySet()) {
            final String name = entry.getKey();
            final StandaloneServer server = entry.getValue();
            final URI serverURI = server.getContext().get(URI.class);

            logger.info("Waiting for updated list");
            services.assertServices(CLIENT_DELAY, TimeUnit.SECONDS, new CalculatorCallable(bean), 500);

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
        } catch (final EJBException e) {
            logger.info(String.format("Pass.  Request resulted in %s: %s", e.getCause().getClass().getSimpleName(), e.getMessage()));
            // good
        }


        for (final Map.Entry<String, StandaloneServer> entry : servers.entrySet()) {
            final String name = entry.getKey();
            final StandaloneServer server = entry.getValue();
            final URI serverURI = server.getContext().get(URI.class);

            logger.info(String.format("Starting %s server", name));

            server.start(1, TimeUnit.MINUTES);
            services.add(serverURI);

            logger.info("Waiting for updated list");
            services.assertServices(CLIENT_DELAY, TimeUnit.SECONDS, new CalculatorCallable(bean), 500);

            logger.info("Asserting balance");
            assertBalance(bean, services.get().size());
        }
    }

    private void assertBalance(final Calculator bean, final int size) {
        final int expectedInvocations = 1000;
        final double percent = 0.10;
        final int totalInvocations = size * expectedInvocations;

        // Verify the work reached all servers
        final Set<Map.Entry<String, AtomicInteger>> entries = invoke(bean, totalInvocations).entrySet();

        Assert.assertEquals("Bad number of invocations for the bean \"" + bean.name() + "\".", size, entries.size());

        // And each server got a minimum of %10 percent of the traffic
        for (final Map.Entry<String, AtomicInteger> entry : entries) {

            final int actualInvocations = entry.getValue().get();

            Assert.assertTrue(String.format("%s out of %s is too low", actualInvocations, expectedInvocations), actualInvocations > expectedInvocations * percent);
        }
    }


    private Map<String, AtomicInteger> invoke(final Calculator bean, final int max) {
        final Map<String, AtomicInteger> invocations = new HashMap<>();
        for (int i = 0; i < max; i++) {
            final String name = bean.name();

            if (!invocations.containsKey(name)) {
                invocations.put(name, new AtomicInteger());
                logger.info(String.format("Tracking new Server %s", name));
            }
            invocations.get(name).incrementAndGet();
        }

        for (final Map.Entry<String, AtomicInteger> entry : invocations.entrySet()) {
            logger.info(String.format("Server %s invoked %s times", entry.getKey(), entry.getValue()));
        }

        return invocations;
    }

    public static class Services {
        static final Logger logger = Logger.getLogger(Services.class.getName());

        private final ReentrantLock lock = new ReentrantLock();
        private final Condition condition = lock.newCondition();

        private final Set<URI> expected = new HashSet<>();

        Services() {
        }

        public Set<URI> get() {
            return expected;
        }

        public boolean add(final URI uri) {
            return expected.add(uri);
        }

        boolean remove(final URI o) {
            return expected.remove(o);
        }

        public void observe(@Observes final ClusterMetaDataUpdated updated) {
            final URI[] locations = updated.getClusterMetaData().getLocations();
            final Set<URI> found = new HashSet<>(Arrays.asList(locations));

            if (expected.equals(found)) {
                lock.lock();
                try {
                    condition.signal();
                } finally {
                    lock.unlock();
                }
            }
        }

        public Set<URI> diff(final Set<URI> a, final Set<URI> b) {
            final Set<URI> diffs = new HashSet<>();
            for (final URI uri : b) {
                if (!a.contains(uri)) diffs.add(uri);
            }

            return diffs;
        }

        public void assertServices(final long timeout, final TimeUnit unit, final Callable callable) {
            assertServices(timeout, unit, callable, 10);
        }

        void assertServices(final long timeout, final TimeUnit unit, final Callable callable, final int delay) {
            final ClientThread client = new ClientThread(callable);
            client.delay(delay);
            client.start();
            try {
                Assert.assertTrue(String.format("services failed to come online: waited %s %s", timeout, unit), await(timeout, unit));
            } catch (final InterruptedException e) {
                Thread.interrupted();
                Assert.fail("Interrupted");
            } finally {
                client.stop();
            }
        }

        boolean await(final long timeout, final TimeUnit unit) throws InterruptedException {
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

        CalculatorCallable(final Calculator bean) {
            this.bean = bean;
        }

        @Override
        public Object call() throws Exception {
            return bean.name();
        }
    }
}
