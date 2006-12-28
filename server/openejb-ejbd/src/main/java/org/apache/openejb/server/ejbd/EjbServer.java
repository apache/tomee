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
package org.apache.openejb.server.ejbd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

import org.apache.openejb.server.ServiceException;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.ProxyInfo;

import javax.ejb.EJBMetaData;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;

public class EjbServer implements org.apache.openejb.server.ServerService, org.apache.openejb.spi.ApplicationServer {

    EjbDaemon server;

    public void init(Properties props) throws Exception {
        server = EjbDaemon.getEjbDaemon();
        server.init(props);
    }

    public void start() throws ServiceException {
    }

    public void stop() throws ServiceException {
    }

    public String getName() {
        return "ejbd";
    }

    public int getPort() {
        return 0;
    }

    public void service(Socket socket) throws ServiceException, IOException {
        ServerFederation.setApplicationServer(server);
        server.service(socket);
    }

    public void service(InputStream inputStream, OutputStream outputStream) throws ServiceException, IOException {
        ServerFederation.setApplicationServer(server);
        server.service(inputStream, outputStream);
    }

    public String getIP() {
        return "";
    }

    public EJBMetaData getEJBMetaData(ProxyInfo info) {
        return server.getEJBMetaData(info);
    }

    public Handle getHandle(ProxyInfo info) {
        return server.getHandle(info);
    }

    public HomeHandle getHomeHandle(ProxyInfo info) {
        return server.getHomeHandle(info);
    }

    public EJBObject getEJBObject(ProxyInfo info) {
        return server.getEJBObject(info);
    }

    public EJBHome getEJBHome(ProxyInfo info) {
        return server.getEJBHome(info);
    }
}
