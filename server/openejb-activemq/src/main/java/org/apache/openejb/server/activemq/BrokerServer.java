/**
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
package org.apache.openejb.server.activemq;

import org.apache.activemq.broker.BrokerService;
import org.apache.openejb.server.SelfManaging;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class BrokerServer implements ServerService, SelfManaging {
    private BrokerService broker;

    private int port = 1527;
    private boolean disabled;
    private InetAddress host;

    public void init(Properties properties) throws Exception {
        String port = properties.getProperty("port", "1527");
        String bind = properties.getProperty("bind");
        String disabled = properties.getProperty("disabled");
        this.port = Integer.parseInt(port);
        this.disabled = Boolean.parseBoolean(disabled);
        host = InetAddress.getByName(bind);

        if (this.disabled) {
            return;
        }
        URI uri = new URI("tcp", null, bind, this.port, null, null, null);

        broker = new BrokerService();
        broker.setPersistent(false);
        broker.addConnector(uri);
    }

    public void start() throws ServiceException {
        if (disabled) return;
        try {
            broker.start();
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public void stop() throws ServiceException {
        if (broker == null) {
            return;
        }
        try {
            broker.stop();
        } catch (Exception e) {
            throw new ServiceException(e);
        } finally {
            broker = null;
        }
    }

    public void service(InputStream inputStream, OutputStream outputStream) throws ServiceException, IOException {
    }

    public void service(Socket socket) throws ServiceException, IOException {
    }

    public String getName() {
        return "activemq";
    }

    public String getIP() {
        return host.getHostAddress();
    }

    public int getPort() {
        return port;
    }

}
