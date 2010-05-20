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

public class AdminDaemon implements ServerService {

    public void init(Properties props) throws Exception {
    }

    public void service(Socket socket) throws ServiceException, IOException {
        InputStream in = null;

        try {
            in = socket.getInputStream();

            byte requestType = (byte) in.read();

            if (requestType == -1) {
                return;
            }

            switch (requestType) {
                case RequestMethodConstants.STOP_REQUEST_Quit:
                case RequestMethodConstants.STOP_REQUEST_quit:
                case RequestMethodConstants.STOP_REQUEST_Stop:
                case RequestMethodConstants.STOP_REQUEST_stop:
                    Server server = SystemInstance.get().getComponent(Server.class);
                    server.stop();

            }

        } catch (SecurityException e) {

        } catch (Throwable e) {

        } finally {
            try {
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (Throwable t) {

            }
        }
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException("Method not implemented: service(InputStream in, OutputStream out)");
    }
    
    public void start() throws ServiceException {
    }

    public void stop() throws ServiceException {
    }

    public int getPort() {
        return 0;
    }

    public String getIP() {
        return "";
    }

    public String getName() {
        return "admin thread";
    }

}
