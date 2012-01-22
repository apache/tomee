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
package org.apache.openejb.server.derbynet;

import org.apache.derby.drda.NetworkServerControl;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.SelfManaging;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.util.LoggingPrintWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class DerbyNetworkService implements ServerService, SelfManaging {

    private NetworkServerControl serverControl;
    private int port = 1527;
    private int threads;
    private boolean disabled;
    private InetAddress host;

    public String getIP() {
        return host.getHostAddress();
    }

    public String getName() {
        return "derbynet";
    }

    public int getPort() {
        return port;
    }

    public void init(Properties properties) throws Exception {
        Options options = new Options(properties);

        this.threads = options.get("threads", 20);
        this.port = options.get("port", 1527);
        this.disabled = options.get("disabled", false);
        
        host = InetAddress.getByName("0.0.0.0");
        System.setProperty("derby.system.home", SystemInstance.get().getBase().getDirectory().getAbsolutePath());
    }

    public void service(InputStream inputStream, OutputStream outputStream) throws ServiceException, IOException {
    }

    public void service(Socket socket) throws ServiceException, IOException {
    }

    public void start() throws ServiceException {
        if (disabled) return;
        try {
            serverControl = new NetworkServerControl(host, port);
            //serverControl.setMaxThreads(threads);

            serverControl.start(new LoggingPrintWriter("Derby"));
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public void stop() throws ServiceException {
        if (serverControl == null) {
            return;
        }
        try {
            serverControl.shutdown();
        } catch (Exception e) {
            throw new ServiceException(e);
        } finally {
            serverControl = null;
        }
    }

}
