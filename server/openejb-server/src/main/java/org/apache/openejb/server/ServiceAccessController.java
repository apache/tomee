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

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @version $Rev$ $Date$
 */
public class ServiceAccessController implements ServerService {

    ServerService next;

    InetAddress[] allowedHosts;

    public ServiceAccessController(ServerService next) {
        this.next = next;
    }

    public void init(Properties props) throws Exception {

        parseAdminIPs(props);

        next.init(props);
    }

    public void start() throws ServiceException {

        next.start();
    }

    public void stop() throws ServiceException {

        next.stop();
    }

    public void service(Socket socket) throws ServiceException, IOException {

        next.service(socket);
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException("service(in,out)");
    }


    public String getName() {
        return next.getName();
    }

    public String getIP() {
        return next.getIP();
    }

    public int getPort() {
        return next.getPort();
    }

    public void checkHostsAuthorization(InetAddress client, InetAddress server) throws SecurityException {

        boolean authorized = false;

        authorized = client.equals(server);

        for (int i = 0; i < allowedHosts.length && !authorized; i++) {
            authorized = allowedHosts[i].equals(client);
        }

        if (!authorized) {
            throw new SecurityException("Host " + client.getHostAddress() + " is not authorized to access this service.");
        }
    }

    private void parseAdminIPs(Properties props) {
        try {

            Vector addresses = new Vector();

            InetAddress[] localIps = InetAddress.getAllByName("localhost");
            for (int i = 0; i < localIps.length; i++) {
                addresses.add(localIps[i]);
            }

            String ipString = props.getProperty("only_from");
            if (ipString != null) {
                StringTokenizer st = new StringTokenizer(ipString, ",");
                while (st.hasMoreTokens()) {
                    String address = null;
                    InetAddress ip = null;
                    try {
                        address = st.nextToken();
                        ip = InetAddress.getByName(address);
                        addresses.add(ip);
                    } catch (Exception e) {

                    }
                }
            }

            allowedHosts = new InetAddress[ addresses.size() ];
            addresses.copyInto(allowedHosts);

        } catch (Exception e) {

        }
    }

}
