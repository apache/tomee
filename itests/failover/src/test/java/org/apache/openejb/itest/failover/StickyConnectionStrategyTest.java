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

import org.apache.openejb.client.RemoteInitialContextFactory;
import org.apache.openejb.itest.failover.ejb.Calculator;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.Zips;
import org.apache.openejb.util.OpenEjbVersion;
import org.apache.openejb.server.control.StandaloneServer;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Assert;
import org.junit.Test;

import jakarta.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * In some situations it can be desirable to have
 * one dedicated multipoint root server which does
 * no other function other than to serve as a central
 * hub for making multipoint introductions.
 *
 * This dedicate root server will not serve applications
 * and not be added to the list of servers that can
 * service EJB requests.
 */
public class StickyConnectionStrategyTest {

    static final Logger logger = Logger.getLogger("org.apache.openejb.client");

    static {
        final ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.FINER);
        logger.addHandler(consoleHandler);
        logger.setLevel(Level.FINER);
        logger.setUseParentHandlers(false);

        //Set a high socket timeout for tests
        System.setProperty("openejb.client.connection.socket.timeout", System.getProperty("openejb.client.connection.socket.timeout", "5000"));
    }

    @Test
    public void test() throws Exception {

        // To run in an IDE, uncomment and update this line
        System.setProperty("version", OpenEjbVersion.get().getVersion());

        final File zip = Repository.getArtifact("org.apache.tomee", "openejb-standalone", "zip");
        final File app = Repository.getArtifact("org.apache.openejb.itests", "failover-ejb", "jar");

        final File dir = Files.tmpdir();

        final StandaloneServer root;
        {
            final StandaloneServer root1;
            final String name = "root";
            final File home = new File(dir, name);

            Files.mkdir(home);
            Zips.unzip(zip, home, true);

            root1 = new StandaloneServer(home, home);
            root1.killOnExit();
            root1.ignoreOut();
            root1.getJvmOpts().add("-Dopenejb.classloader.forced-load=org.apache.openejb");
            root1.setProperty("name", name);
            root1.setProperty("openejb.extract.configuration", "false");

            final StandaloneServer.ServerService multipoint = root1.getServerService("multipoint");
            multipoint.setBind("localhost");
            multipoint.setPort(getAvailablePort());
            multipoint.setDisabled(false);
            multipoint.set("discoveryName", name);
            root = root1;

            logger.info("Starting Root server");
            root.start();
        }


        final Map<String, StandaloneServer> servers = new HashMap<String, StandaloneServer>();
        for (final String name : new String[]{"red", "green", "blue"}) {
            final File home = new File(dir, name);
            Files.mkdir(home);
            Zips.unzip(zip, home, true);

            final StandaloneServer server = new StandaloneServer(home, home);
            server.killOnExit();
            server.ignoreOut();
            server.getJvmOpts().add("-Dopenejb.classloader.forced-load=org.apache.openejb");
            server.setProperty("name", name);
            server.setProperty("openejb.extract.configuration", "false");

            IO.copy(app, Files.path(home, "apps", "itest.jar"));
            IO.copy(IO.read("<openejb><Deployments dir=\"apps/\"/></openejb>"), Files.path(home, "conf", "openejb.xml"));

            final StandaloneServer.ServerService ejbd = server.getServerService("ejbd");
            ejbd.setBind("localhost");
            ejbd.setDisabled(false);
            ejbd.setPort(getAvailablePort());
            ejbd.setThreads(5);
            ejbd.set("discovery", "ejb:ejbd://{bind}:{port}/" + name);

            final StandaloneServer.ServerService multipoint = server.getServerService("multipoint");
            multipoint.setPort(getAvailablePort());
            multipoint.setDisabled(false);
            multipoint.set("discoveryName", name);
            multipoint.set("initialServers", "localhost:" + root.getServerService("multipoint").getPort());

            servers.put(name, server);

            logger.info(String.format("Starting %s server", name));

            server.start(1, TimeUnit.MINUTES);

            invoke(name, server);
        }

        System.setProperty("openejb.client.requestretry", "true");

        logger.info("Beginning Test");

        final Properties environment = new Properties();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        environment.put(Context.PROVIDER_URL, "ejbd://localhost:" + servers.values().iterator().next().getServerService("ejbd").getPort() + "/provider");

        final InitialContext context = new InitialContext(environment);
        final Calculator bean = (Calculator) context.lookup("CalculatorBeanRemote");


        String previous = null;
        for (final StandaloneServer ignored : servers.values()) {

            logger.info("Looping");

            // What server are we talking to now?
            final String name = bean.name();

            logger.info("Sticky request to " + name);

            // The root should not be serving apps
            assertFalse("root".equals(name));

            // Should not be the same server we were talking with previously (we killed that server)
            if (previous != null) assertFalse(name.equals(previous));
            previous = name;

            final int i = 1000;

            logger.info(String.format("Performing %s invocations, expecting %s to be used for each invocation.", i, name));

            // Should be the same server for the next N calls
            invoke(bean, i, name);

            logger.info("Shutting down " + name);

            // Now let's kill that server
            servers.get(name).kill();
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


        // Let's start a server again and invocations should now succeed
        final Iterator<StandaloneServer> iterator = servers.values().iterator();
        iterator.next();

        final StandaloneServer server = iterator.next();

        logger.info(String.format("Starting %s server", server.getProperties().get("name")));

        server.start(1, TimeUnit.MINUTES);

        logger.info("Performing one more invocation");

        assertEquals(5, bean.sum(2, 3));
    }

    private void invoke(final String name, final StandaloneServer server) throws NamingException {
        final Properties environment = new Properties();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        environment.put(Context.PROVIDER_URL, "ejbd://localhost:" + server.getServerService("ejbd").getPort() + "/" + name);

        final InitialContext context = new InitialContext(environment);
        final Calculator bean = (Calculator) context.lookup("CalculatorBeanRemote");
        assertEquals(name, bean.name());
    }

    private long invoke(final Calculator bean, final int max, final String expectedName) {

        long total = 0;

        for (int i = 0; i < max; i++) {
            final long start = System.nanoTime();
            final String name = bean.name();
            Assert.assertEquals(expectedName, name);
            total += System.nanoTime() - start;
        }

        return TimeUnit.NANOSECONDS.toMicros(total / max);
    }

    private long invoke(final Calculator bean, final int max) {

        long total = 0;

        for (int i = 0; i < max; i++) {
            final long start = System.nanoTime();
            Assert.assertEquals(3, bean.sum(1, 2));
            total += System.nanoTime() - start;
        }

        return TimeUnit.NANOSECONDS.toMicros(total / max);
    }

    private int getAvailablePort() {
        return NetworkUtil.getNextAvailablePort();
    }
}
