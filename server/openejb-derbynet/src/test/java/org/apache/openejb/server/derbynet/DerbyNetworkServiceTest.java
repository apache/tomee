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
package org.apache.openejb.server.derbynet;


import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceFinder;
import org.apache.openejb.server.SimpleServiceManager;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class DerbyNetworkServiceTest {

    private static final long RETRY_TIMEOUT = 250;
    private long timeoutLeftover = 10000;

    private void waitForDerby(int port) {
        try {
            Socket socket = new Socket("localhost", port);
            socket.close();
        } catch (IOException e) {
            timeoutLeftover -= RETRY_TIMEOUT;
            if (timeoutLeftover < 0) {
                Assert.fail("Impossible to connect using port\"" + port + "\". Message: " + e.getMessage());
            }
            try {
                Thread.sleep(RETRY_TIMEOUT);
            } catch (InterruptedException ignore) {
                // no-op
            }
            waitForDerby(port);
        }
    }

    private void assertConnection(int port) throws ClassNotFoundException, SQLException {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        final String connectionStr = "jdbc:derby://localhost:" + port + "/testDB;create=true;user=tomee;password=tomee";
        final Connection conn = DriverManager.getConnection(connectionStr);
        try {
            final Statement stmt = conn.createStatement();
            final ResultSet rs = stmt.executeQuery("values(1)"); // Derby doesn't like "SELECT 1" as validation query
            boolean valueFound = false;
            while (rs.next()) {
                valueFound = true;
                Assert.assertEquals("1", rs.getString(1));
            }
            Assert.assertTrue("No value found.", valueFound);
            stmt.close();
        } finally {
            conn.close();
        }
    }

    @Test
    public void test() throws Exception {
        final int port = NetworkUtil.getNextAvailablePort();
        final SimpleServiceManager serviceManager = new SimpleServiceManager(new ServiceFinder() {
            @Override
            public Map<String, Properties> mapAvailableServices(Class interfase) throws IOException, ClassNotFoundException {
                final Properties properties = new Properties();
                properties.setProperty("server", DerbyNetworkService.class.getName());
                properties.setProperty("port", port + "");
                properties.setProperty("disabled", "false");
                properties.put(ServerService.class, DerbyNetworkService.class);
                properties.put(
                    "derby.system.home",
                    new File(SystemInstance.get().getBase().getDirectory(), "target").getAbsolutePath()
                );
                final Map<String, Properties> services = new HashMap<String, Properties>();
                services.put("derbynet", properties);
                return services;
            }
        });
        serviceManager.init();
        serviceManager.start(false);
        waitForDerby(port);
        assertConnection(port);
        serviceManager.stop();
    }
}
