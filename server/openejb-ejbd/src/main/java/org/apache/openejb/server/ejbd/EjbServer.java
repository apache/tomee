/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.ejbd;

import org.apache.openejb.ProxyInfo;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.server.ServiceException;

import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBMetaData;
import jakarta.ejb.EJBObject;
import jakarta.ejb.Handle;
import jakarta.ejb.HomeHandle;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EjbServer implements org.apache.openejb.server.ServerService, org.apache.openejb.spi.ApplicationServer {

    protected KeepAliveServer keepAlive;
    protected EjbDaemon server;

    public EjbServer() {

    }

    @Override
    public void init(final Properties props) throws Exception {
        server = new EjbDaemon();
        if (props.getProperty("name") == null) { // to let server be component aware
            props.setProperty("name", getName());
        }
        server.init(props);
        keepAlive = new KeepAliveServer(this, server.isGzip());
    }

    @Override
    public void start() throws ServiceException {
        if (null != keepAlive) {
            keepAlive.start();
        } else {
            logNotInitialized();
        }
    }

    private void logNotInitialized() {
        Logger.getLogger(EjbServer.class.getName()).log(Level.WARNING, "EjbServer was not initialized");
    }

    @Override
    public void stop() throws ServiceException {
        if (null != keepAlive) {
            keepAlive.stop();
        } else {
            logNotInitialized();
        }
    }

    @Override
    public String getName() {
        return "ejbd";
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public void service(final Socket socket) throws ServiceException, IOException {
        if (null != keepAlive) {
            keepAlive.service(socket);
        } else {
            logNotInitialized();
        }
    }

    @Override
    public void service(final InputStream inputStream, final OutputStream outputStream) throws ServiceException, IOException {

        ServerFederation.setApplicationServer(server);
        final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            server.service(inputStream, outputStream);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    @Override
    public String getIP() {
        return "";
    }

    @Override
    public EJBMetaData getEJBMetaData(final ProxyInfo info) {
        return server.getEJBMetaData(info);
    }

    @Override
    public Handle getHandle(final ProxyInfo info) {
        return server.getHandle(info);
    }

    @Override
    public HomeHandle getHomeHandle(final ProxyInfo info) {
        return server.getHomeHandle(info);
    }

    @Override
    public EJBObject getEJBObject(final ProxyInfo info) {
        return server.getEJBObject(info);
    }

    @Override
    public Object getBusinessObject(final ProxyInfo info) {
        return server.getBusinessObject(info);
    }

    @Override
    public EJBHome getEJBHome(final ProxyInfo info) {
        return server.getEJBHome(info);
    }
}
