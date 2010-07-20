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
package org.apache.openejb.server.admin;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.Server;
import org.apache.openejb.client.RequestMethodConstants;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

public class AdminDaemon implements ServerService {

    @Override
    public void init(Properties props) throws Exception {
    }

    @Override
    public void service(Socket socket) throws ServiceException, IOException {
        InputStream in = null;

        try {
            in = socket.getInputStream();

            byte requestType = (byte) in.read();

            switch (requestType) {
                case -1:
                    return;
                case RequestMethodConstants.STOP_REQUEST_Quit:
                case RequestMethodConstants.STOP_REQUEST_quit:
                case RequestMethodConstants.STOP_REQUEST_Stop:
                case RequestMethodConstants.STOP_REQUEST_stop:
                    Server server = SystemInstance.get().getComponent(Server.class);
                    server.stop();
                    break;
                default:
                    //If this turns up in the logs then it is time to take action
                    Logger.getInstance(LogCategory.OPENEJB_SERVER, AdminDaemon.class).warning("Invalid Server Socket request: " + requestType);
                    break;
            }

        } catch (Throwable e) {
            Logger.getInstance(LogCategory.OPENEJB_SERVER, AdminDaemon.class).warning("Server Socket request failed", e);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (Throwable t) {
                    //Ignore
                }
            }

            if (null != socket) {
                try {
                    socket.close();
                } catch (Throwable t) {
                    //Ignore
                }
            }
        }
    }

    @Override
    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException("Method not implemented: service(InputStream in, OutputStream out)");
    }

    @Override
    public void start() throws ServiceException {
    }

    @Override
    public void stop() throws ServiceException {
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public String getIP() {
        return "";
    }

    @Override
    public String getName() {
        return "admin thread";
    }
}
