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
import org.junit.Assert;
import org.junit.Test;

import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class SpeedTest extends FailoverTest {

    @Test
    public void test() throws Exception {

        // To run in an IDE, uncomment and update this line
        //System.setProperty("version", "4.0.0-beta-3-SNAPSHOT");

        final File zip = Repository.getArtifact("org.apache.openejb", "openejb-standalone", "zip");
        final File app = Repository.getArtifact("org.apache.openejb.itests", "failover-ejb", "jar");

        final File dir = Files.tmpdir();

        final StandaloneServer root = createMultipointServer(zip, dir, "root");
        root.setOut(System.out);
        root.start(1, TimeUnit.MINUTES);

        final List<StandaloneServer> servers = new ArrayList<StandaloneServer>();

        for (String name : new String[]{"red", "green", "blue", "yellow", "orange"}) {

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
            ejbd.setDisabled(false);
            ejbd.setPort(getAvailablePort());
            ejbd.setThreads(5);

            final StandaloneServer.ServerService multipoint = server.getServerService("multipoint");
            multipoint.setPort(getAvailablePort());
            multipoint.setDisabled(false);
            multipoint.set("discoveryName", name);
            multipoint.set("initialServers", "localhost:"+root.getServerService("multipoint").getPort());

            server.start(1, TimeUnit.MINUTES);

            servers.add(server);
        }

        Collections.reverse(servers);

        System.setProperty("openejb.client.requestretry", "true");

        final Properties environment = new Properties();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        environment.put(Context.PROVIDER_URL, "failover:ejbd://localhost:" + servers.get(0).getServerService("ejbd").getPort());

        final InitialContext context = new InitialContext(environment);
        final Calculator bean = (Calculator) context.lookup("CalculatorBeanRemote");

        // Hotspotting doesn't kick in with just a few invocations
        // Later we'll be doing thousands of invocations and that will
        // bring the invoke time down significantly

        // With this test we're looking for issues with failed servers
        // causing delays even though they should have been removed from
        // the clients invocation list

        final long slowest = invoke(bean, 5);
        System.out.printf("Base invocation speed: %s microseconds", slowest);
        System.out.println();

        for (StandaloneServer server : servers) {
            final long speed = invoke(bean, 10000);
            Assert.assertTrue(String.format("Average invocation time %s microseconds higher than pre-hotspot time of %s microseconds", speed, slowest), speed < slowest);
            server.kill();
        }

        System.out.println("All servers destroyed");

        try {
            System.out.println(String.format("Average invocation time %s microseconds", invoke(bean, 10000)));
            Assert.fail("Server should be destroyed");
        } catch (EJBException e) {
            // good
        }

        final StandaloneServer server = servers.get(0);
        server.start(1, TimeUnit.MINUTES);

        final long speed = invoke(bean, 10000);
        Assert.assertTrue(String.format("Average invocation time %s microseconds higher than pre-hotspot time of %s microseconds", speed, slowest), speed < slowest);

    }
}
