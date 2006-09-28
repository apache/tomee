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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server;

import java.util.Properties;
import java.net.URI;
import java.io.IOException;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.SafeToolkit;

public class Server implements org.apache.openejb.spi.Service {

    private SafeToolkit toolkit = SafeToolkit.getToolkit("OpenEJB EJB Server");
    private Messages _messages = new Messages("org.apache.openejb.server");
    private Logger logger = Logger.getInstance("OpenEJB.server.remote", "org.apache.openejb.server");

    Properties props;

    static Server server;
    private ServiceManager manager;

    public static Server getServer() {
        if (server == null) {
            server = new Server();
        }

        return server;
    }

    public void init(java.util.Properties props) throws Exception {
        this.props = props;

        OpenEJB.init(props, new ServerFederation());

        if (System.getProperty("openejb.nobanner") == null) {
            System.out.println("[init] OpenEJB Remote Server");
        }

        manager = ServiceManager.getManager();
        manager.init();
    }

    public void start() throws Exception {
        manager.start();
    }

    public void stop() throws Exception {
        manager.stop();
    }

    public void addService(URI uri) {

    }

//    public void addService(ServerService )

    public static class ServerServiceFactory {
        public ServerService createService(URI location) throws IOException {
            String scheme = location.getScheme();
            
            return null;
        }
    }

}

