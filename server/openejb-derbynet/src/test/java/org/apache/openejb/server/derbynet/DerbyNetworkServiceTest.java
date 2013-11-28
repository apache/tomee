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


import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceFinder;
import org.apache.openejb.server.SimpleServiceManager;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class DerbyNetworkServiceTest {

    private static final long RETRY_TIMEOUT = 10000;

    private void connectSocket(int port) {
        try {
            try {
                new Socket("localhost", port);
            } catch (ConnectException e) {
                // OK it didn't fully started yet. Wait a bit and try it again.
                Thread.sleep(RETRY_TIMEOUT);
                new Socket("localhost", port);
            }
        } catch (Exception e) {
            Assert.fail("Impossible to connect using port\"" + port + "\". Message: " + e.getMessage());
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
                final Map<String, Properties> services = new HashMap<String, Properties>();
                services.put("derbynet", properties);
                return services;
            }
        });

        serviceManager.init();
        serviceManager.start(false);
        connectSocket(port);
        serviceManager.stop();
    }
}
