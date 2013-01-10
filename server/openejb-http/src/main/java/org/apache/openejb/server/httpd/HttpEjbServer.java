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

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.ejbd.EjbServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public abstract class HttpEjbServer implements ServerService {

    protected HttpServer httpServer;
    private String name;

    @Override
    public void init(final Properties props) throws Exception {
        name = props.getProperty("name");
        final EjbServer ejbServer = new EjbServer();
        final ServerServiceAdapter adapter = new ServerServiceAdapter(ejbServer);

        final SystemInstance systemInstance = SystemInstance.get();
        HttpListenerRegistry registry = systemInstance.getComponent(HttpListenerRegistry.class);
        if (registry == null) {
            registry = new HttpListenerRegistry();
            systemInstance.setComponent(HttpListenerRegistry.class, registry);
        }

        registry.addHttpListener(adapter, "/ejb/?.*");

        // register the http server
        systemInstance.setComponent(HttpServer.class, httpServer);

        httpServer.init(props);
        ejbServer.init(props);
    }

    @Override
    public void service(final Socket socket) throws ServiceException, IOException {
        httpServer.service(socket);
    }

    @Override
    public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
        httpServer.service(in, out);
    }

    @Override
    public void start() throws ServiceException {
        httpServer.start();
    }

    @Override
    public void stop() throws ServiceException {
        httpServer.stop();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPort() {
        return httpServer.getPort();
    }

    @Override
    public String getIP() {
        return httpServer.getIP();
    }
}
