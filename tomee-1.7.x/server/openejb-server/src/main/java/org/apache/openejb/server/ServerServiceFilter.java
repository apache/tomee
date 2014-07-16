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
package org.apache.openejb.server;

import org.apache.openejb.monitoring.Managed;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

/**
 * TODO: Make this the superclass of the appropriate ServerService implementations
 * @version $Rev$ $Date$
 */
public class ServerServiceFilter implements ServerService {

    @Managed
    private final ServerService service;

    public ServerServiceFilter(final ServerService service) {
        this.service = service;
    }

    @Override
    public String getIP() {
        return service.getIP();
    }

    @Override
    public String getName() {
        return service.getName();
    }

    @Override
    public int getPort() {
        return service.getPort();
    }

    @Override
    public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
        service.service(in, out);
    }

    @Override
    public void service(final Socket socket) throws ServiceException, IOException {
        service.service(socket);
    }

    @Override
    public void start() throws ServiceException {
        service.start();
    }

    @Override
    public void stop() throws ServiceException {
        service.stop();
    }

    @Override
    public void init(final Properties props) throws Exception {
        service.init(props);
    }
}
