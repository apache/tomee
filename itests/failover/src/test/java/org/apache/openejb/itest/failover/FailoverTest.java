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
import org.apache.openejb.loader.IO;
import org.apache.openejb.server.control.StandaloneServer;
import org.apache.openejb.util.Files;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.Zips;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class FailoverTest {

    @Test
    public void testNothing() {
    }

    @Test @Ignore
    public void testFailover() throws Exception {

        final File zip = new File("/Users/dblevins/.m2/repository/org/apache/openejb/openejb-standalone/4.0.0-beta-3-SNAPSHOT/openejb-standalone-4.0.0-beta-3-SNAPSHOT.zip");
        final File app = new File("/Users/dblevins/.m2/repository/org/apache/openejb/itests/failover-ejb/4.0.0-beta-3-SNAPSHOT/failover-ejb-4.0.0-beta-3-SNAPSHOT.jar");

        final File dir = Files.tmpdir();

        final String[] serverNames = {"red", "green", "blue"};
//        final String[] serverNames = {"red", "green", "blue", "yellow", "orange", "purple"};
//        final String[] serverNames = {"red", "green"};

        final List<StandaloneServer> servers = new ArrayList<StandaloneServer>();

        final List<String> initialServers = new ArrayList<String>();

        for (String name : serverNames) {
            final File home = new File(dir, name);
            Files.mkdir(home);
            Zips.unzip(zip, home, true);

            final StandaloneServer server = new StandaloneServer(home, home);
            server.killOnExit();
            server.ignoreOut();
            server.getProperties().put("openejb.extract.configuration", "false");

            IO.copy(app, Files.path(home, "apps", "itest.jar"));
            IO.copy(IO.read("<openejb><Deployments dir=\"apps/\"/></openejb>"), Files.path(home, "conf", "openejb.xml"));
            /*
            server      = org.apache.openejb.server.ejbd.EjbServer
            bind        = 127.0.0.1
            port        = 4201
            disabled    = false
            threads     = 200
            backlog     = 200
            discovery   = ejb:ejbd://{bind}:{port}
             */
            final StandaloneServer.ServerService ejbd = server.getServerService("ejbd");
            ejbd.setDisabled(false);
            ejbd.setPort(getAvailablePort());
            ejbd.setThreads(5);
            ejbd.set("discovery", "ejb:ejbd://{bind}:{port}/" + name);

            /*
            server      = org.apache.openejb.server.discovery.MultipointDiscoveryAgent
            bind        = 127.0.0.1
            port        = 4212
            disabled    = true

            initialServers         =
            group                  = default
            heart_rate             = 500
            loopback_mode          = false
            max_missed_heartbeats  = 10
             */
            final StandaloneServer.ServerService multipoint = server.getServerService("multipoint");
            multipoint.setPort(getAvailablePort());
            multipoint.setDisabled(false);

            initialServers.add("localhost:" + multipoint.getPort());

            servers.add(server);
        }

        servers.get(0).setOut(System.out);

        for (StandaloneServer server : servers) {
            final StandaloneServer.ServerService multipoint = server.getServerService("multipoint");
            multipoint.set("initialServers", Join.join(",", initialServers));
        }

        for (StandaloneServer server : servers) {
            server.start(1, TimeUnit.MINUTES);
        }

        Collections.reverse(servers);

        System.setProperty("openejb.client.requestretry", "true");

        final Properties environment = new Properties();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        environment.put(Context.PROVIDER_URL, "failover:ejbd://localhost:" + servers.get(0).getServerService("ejbd").getPort());

        final InitialContext context = new InitialContext(environment);
        final Calculator bean = (Calculator) context.lookup("CalculatorBeanRemote");

        for (StandaloneServer server : servers) {
            System.out.println(String.format("Average invocation time %s microseconds", invoke(bean, 10000)));
            server.kill();
        }

        System.out.println("All servers destroyed");

        try {
            System.out.println(String.format("Average invocation time %s microseconds", invoke(bean, 10000)));
            Assert.fail("Server should be destroyed");
        } catch (EJBException e) {
            // good
        }

        for (StandaloneServer server : servers) {
            server.start(1, TimeUnit.MINUTES);
            System.out.println(String.format("Average invocation time %s microseconds", invoke(bean, 10000)));
        }

        System.out.println("DONE");
    }

    private long invoke(Calculator bean, int max) {

        long total = 0;

        for (int i = 0; i < max; i++) {
            final long start = System.nanoTime();
            Assert.assertEquals(3, bean.sum(1, 2));
            total += System.nanoTime() - start;
        }

        return TimeUnit.NANOSECONDS.toMicros(total / max);
    }

    private int getAvailablePort() {
        try {
            final ServerSocket serverSocket = new ServerSocket(0);
            final int port = serverSocket.getLocalPort();
            serverSocket.close();

            return port;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to acquire a available port");
        }
    }
}
