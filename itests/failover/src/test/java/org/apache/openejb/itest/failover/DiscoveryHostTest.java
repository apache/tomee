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
import org.apache.openejb.util.Join;
import org.apache.openejb.util.NetworkUtil;
import org.apache.openejb.util.OpenEjbVersion;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import jakarta.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class DiscoveryHostTest {

    @Ignore
    @Test
    public void test() throws Exception {

        // To run in an IDE, uncomment and update this line
        //System.setProperty("version", OpenEjbVersion.get().getVersion());

        final File zip = Repository.getArtifact("org.apache.tomee", "openejb-standalone", "zip");
        final File app = Repository.getArtifact("org.apache.openejb.itests", "failover-ejb", "jar");

        final File dir = Files.tmpdir();

        final List<StandaloneServer> servers = new ArrayList<StandaloneServer>();

        final List<String> initialServers = new ArrayList<String>();

        for (final String name : new String[]{"red", "green", "blue"}) {
            final File home = new File(dir, name);
            Files.mkdir(home);
            Zips.unzip(zip, home, true);

            final StandaloneServer server = new StandaloneServer(home, home);
            server.killOnExit();
            server.ignoreOut();
            server.getJvmOpts().add("-Dopenejb.classloader.forced-load=org.apache.openejb");
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

            final StandaloneServer.ServerService multipoint = server.getServerService("multipoint");
            multipoint.setBind("0.0.0.0");
            multipoint.setPort(getAvailablePort());
            multipoint.setDisabled(false);
            multipoint.set("discoveryHost", "localhost");

            initialServers.add("localhost:" + multipoint.getPort());

            servers.add(server);
        }

        servers.get(0).setOut(System.out);

        for (final StandaloneServer server : servers) {
            final StandaloneServer.ServerService multipoint = server.getServerService("multipoint");
            multipoint.set("initialServers", Join.join(",", initialServers));
        }

        for (final StandaloneServer server : servers) {
            server.start(1, TimeUnit.MINUTES);
        }

        Collections.reverse(servers);

        System.setProperty("openejb.client.requestretry", "true");

        final Properties environment = new Properties();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        environment.put(Context.PROVIDER_URL, "failover:ejbd://localhost:" + servers.get(0).getServerService("ejbd").getPort());

        final InitialContext context = new InitialContext(environment);
        final Calculator bean = (Calculator) context.lookup("CalculatorBeanRemote");

        for (final StandaloneServer server : servers) {
            System.out.println(String.format("Average invocation time %s microseconds", invoke(bean, 10000)));
            server.kill();
        }

        System.out.println("All servers destroyed");

        try {
            System.out.println(String.format("Average invocation time %s microseconds", invoke(bean, 10000)));
            Assert.fail("Server should be destroyed");
        } catch (final EJBException e) {
            // good
        }

        for (final StandaloneServer server : servers) {
            server.start(1, TimeUnit.MINUTES);
            System.out.println(String.format("Average invocation time %s microseconds", invoke(bean, 10000)));
        }

        System.out.println("DONE");
    }

    private long invoke(final Calculator bean, final int max, final String expectedName) {

        long total = 0;

        for (int i = 0; i < max; i++) {
            final long start = System.nanoTime();
            final String name = bean.name();
            System.out.println(name);
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
