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
import org.apache.openejb.server.control.StandaloneServer;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Assert;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This test verifies the situation where none of the
 * servers listed in the 'initialServers' list can be contacted.
 *
 * A server that is unable to connect to any of its peers should
 * continue trying to connect to servers on the 'initialServers'
 * list.  How long to wait between attempts is dictated by the
 * 'reconnectDelay' setting, which is 30 seconds by default.
 */
public class ReconnectDelayListSelfTest {

    static final Logger logger = Logger.getLogger("org.apache.openejb.client");

    static {
        set(logger, Level.FINER);
        set(Logger.getLogger("OpenEJB.client"), Level.FINER);
    }

    private static void set(Logger logger, Level level) {
        final ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(level);
        logger.addHandler(consoleHandler);
        logger.setLevel(level);
        logger.setUseParentHandlers(false);
    }

    private static final String MULTIPOINT = "multipoint";

    @Test
    public void test() throws Exception {

        // To run in an IDE, uncomment and update this line
        System.setProperty("version", "4.0.0-beta-3-SNAPSHOT");

        Duration reconnectDelay = new Duration("1 second");

        final File zip = Repository.getArtifact("org.apache.openejb", "openejb-standalone", "zip");
        final File app = Repository.getArtifact("org.apache.openejb.itests", "failover-ejb", "jar");

        final File dir = Files.tmpdir();

        System.setProperty("openejb.client.requestretry", "true");

        final Map<String, StandaloneServer> servers = new HashMap<String, StandaloneServer>();

        for (String name : new String[]{"red", "green", "blue"}) {
            final File home = new File(dir, name);
            Files.mkdir(home);
            Zips.unzip(zip, home, true);

            final StandaloneServer server = new StandaloneServer(home, home);
            server.killOnExit();
            server.ignoreOut();
            server.getProperties().put("name", name);
            server.getProperties().put("openejb.extract.configuration", "false");

            IO.copy(app, Files.path(home, "apps", "itest.jar"));
            IO.copy(IO.read("<openejb><Deployments dir=\"apps/\"/></openejb>"), Files.path(home, "conf", "openejb.xml"));

            final StandaloneServer.ServerService ejbd = server.getServerService("ejbd");
            ejbd.setDisabled(false);
            ejbd.setBind("0.0.0.0");
            ejbd.setPort(getAvailablePort());
            ejbd.setThreads(5);
            ejbd.set("discoveryHost", "localhost");
            ejbd.set("discovery", "ejb:ejbd://{discoveryHost}:{port}/" + name);

            final StandaloneServer.ServerService multipoint = server.getServerService(MULTIPOINT);
            multipoint.setBind("0.0.0.0");
            multipoint.setPort(getAvailablePort());
            multipoint.setDisabled(false);
            multipoint.set("discoveryHost", "localhost");
            multipoint.set("discoveryName", name);
            multipoint.set("reconnectDelay", reconnectDelay.toString());

            servers.put(name, server);
        }

        final StandaloneServer red = servers.get("red");

        // Set all the initialServers to point to RED
        for (Map.Entry<String, StandaloneServer> entry : servers.entrySet()) {
            final StandaloneServer server = entry.getValue();
            final StandaloneServer.ServerService multipoint = server.getServerService(MULTIPOINT);
            final String value = "localhost:" + red.getServerService(MULTIPOINT).getPort() + ",localhost:"+multipoint.getPort();
            multipoint.set("initialServers", value);
        }

        // Start all the servers except RED
        for (Map.Entry<String, StandaloneServer> entry : servers.entrySet()) {
            if (entry.getKey().equals("red")) continue;
            entry.getValue().start(1, TimeUnit.MINUTES);
        }

        // Verify Failover is not yet functional

        {
            // RED was never started so BLUE never found any peers

            // Lets invoke BLUE then shut it down and verify we have
            // no other peers to invoke
            final StandaloneServer blue = servers.get("blue");
            final Properties environment = new Properties();
            environment.put(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
            environment.put(Context.PROVIDER_URL, "ejbd://localhost:" + blue.getServerService("ejbd").getPort());

            final InitialContext context = new InitialContext(environment);
            final Calculator bean = (Calculator) context.lookup("CalculatorBeanRemote");

            // Invoke BLUE a few times
            invoke(bean, 10, "blue");

            // Kill BLUE
            blue.kill();

            // Invocations should now fail (and not failover)
            try {
                bean.name();
                Assert.fail("Server should be down and failover not hooked up");
            } catch (Exception e) {
                // pass
            }
        }

        // Now we start RED
        red.start(1, TimeUnit.MINUTES);

        // Wait for the reconnectDelay so GREEN can find RED
        Thread.sleep((long) (reconnectDelay.getTime(TimeUnit.MILLISECONDS) * 1.5));

        // Verify Failover is now functional

        {
            // RED was never started so GREEN never found any peers

            // Lets invoke GREEN then shut it down and verify we have
            // no other peers to invoke
            final StandaloneServer green = servers.get("green");
            final Properties environment = new Properties();
            environment.put(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
            environment.put(Context.PROVIDER_URL, "ejbd://localhost:" + green.getServerService("ejbd").getPort());

            final InitialContext context = new InitialContext(environment);
            final Calculator bean = (Calculator) context.lookup("CalculatorBeanRemote");


            // Invoke GREEN a few times
            invoke(bean, 10, "green");

            // Kill GREEN
            green.kill();

            // Invocations should now failover to RED
            invoke(bean, 10, "red");
        }
    }

    private long invoke(Calculator bean, int max, String expectedName) {

        long total = 0;

        for (int i = 0; i < max; i++) {
            final long start = System.nanoTime();
            String name = bean.name();
            System.out.println(name);
            Assert.assertEquals(expectedName, name);
            total += System.nanoTime() - start;
        }

        return TimeUnit.NANOSECONDS.toMicros(total / max);
    }

    private int getAvailablePort() {
        return NetworkUtil.getNextAvailablePort();
    }
}
