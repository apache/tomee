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
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
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
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SERVER, DerbyNetworkService.class);

    private NetworkServerControl serverControl;
    private int port = 1527;
    private boolean disabled;
    private InetAddress host;
    private boolean verbose = false;

    @Override
    public String getIP() {
        return host.getHostAddress();
    }

    @Override
    public String getName() {
        return "derbynet";
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void init(final Properties properties) throws Exception {
        final Options options = new Options(properties);
        this.port = options.get("port", 1527);
        this.disabled = options.get("disabled", false);
        this.verbose = options.get("verbose", false);
        this.host = InetAddress.getByName("0.0.0.0");
        System.setProperty(
            "derby.system.home",
            options.get("derby.system.home", SystemInstance.get().getBase().getDirectory().getAbsolutePath())
        );
    }

    @Override
    public void service(final InputStream inputStream, final OutputStream outputStream) throws ServiceException, IOException {
        // no-op
    }

    @Override
    public void service(final Socket socket) throws ServiceException, IOException {
        // no-op
    }

    @Override
    public void start() throws ServiceException {
        if (this.disabled) {
            return;
        }
        try {
            this.serverControl = new NetworkServerControl(host, port);
            this.serverControl.start(new LoggingPrintWriter("Derby"));
            if (verbose) {
                LOGGER.info("Starting openejb-derbynet with derby " + serverControl.getRuntimeInfo() + " " + serverControl.getSysinfo());
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void stop() throws ServiceException {
        if (this.serverControl == null) {
            return;
        }
        try {
            this.serverControl.shutdown();
        } catch (Exception e) {
            throw new ServiceException(e);
        } finally {
            this.serverControl = null;
        }
    }

}
