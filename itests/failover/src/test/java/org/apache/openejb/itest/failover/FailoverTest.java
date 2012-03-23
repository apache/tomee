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

import org.apache.openejb.itest.failover.ejb.Calculator;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.Zips;
import org.apache.openejb.server.control.StandaloneServer;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.apache.openejb.util.NetworkUtil.getNextAvailablePort;

/**
 * @version $Rev$ $Date$
 */
public class FailoverTest {
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

    protected long invoke(Calculator bean, int max) {

        long total = 0;

        for (int i = 0; i < max; i++) {
            final long start = System.nanoTime();
            Assert.assertEquals(3, bean.sum(1, 2));
            total += System.nanoTime() - start;
        }

        return TimeUnit.NANOSECONDS.toMicros(total / max);
    }

    public StandaloneServer createMultipointServer(File zip, File dir, String name) throws IOException {
        final File home = new File(dir, name);

        Files.mkdir(home);
        Zips.unzip(zip, home, true);

        StandaloneServer server = new StandaloneServer(home, home);
        server.killOnExit();
        server.ignoreOut();
        server.setProperty("name", name);
        server.setProperty("TestName", this.getClass().getName());
        server.setProperty("openejb.extract.configuration", "false");

        final StandaloneServer.ServerService multipoint = server.getServerService("multipoint");
        multipoint.setBind("localhost");
        multipoint.setPort(getNextAvailablePort());
        multipoint.setEnabled(true);
        multipoint.set("discoveryName", name);
        return server;
    }

}
