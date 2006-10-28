/**
 *
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
package org.apache.openejb.server.httpd;

import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.ejbd.EjbServer;
import org.apache.openejb.loader.SystemInstance;

import java.util.Properties;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @version $Revision$ $Date$
 */
public class HttpEjbServer implements ServerService {

    private HttpServer httpServer;
    private String name;

    public void init(Properties props) throws Exception {
        name = props.getProperty("name");
        EjbServer ejbServer = new EjbServer();
        ServerServiceAdapter adapter = new ServerServiceAdapter(ejbServer);

        HttpListenerRegistry registry = new HttpListenerRegistry();
        SystemInstance.get().setComponent(HttpListenerRegistry.class, registry);
        registry.addHttpListener(adapter, "/ejb/.*");

        httpServer = new HttpServer(registry);
        httpServer.init(props);
        ejbServer.init(props);
    }


    public void service(Socket socket) throws ServiceException, IOException {
        httpServer.service(socket);
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        httpServer.service(in, out);
    }

    public void start() throws ServiceException {
        httpServer.start();
    }

    public void stop() throws ServiceException {
        httpServer.stop();
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return httpServer.getPort();
    }

    public String getIP() {
        return httpServer.getIP();
    }
}
