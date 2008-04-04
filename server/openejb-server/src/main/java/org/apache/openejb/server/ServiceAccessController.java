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


import org.apache.openejb.server.auth.IPAddressPermission;
import org.apache.openejb.server.auth.ExactIPAddressPermission;
import org.apache.openejb.server.auth.ExactIPv6AddressPermission;
import org.apache.openejb.server.auth.IPAddressPermissionFactory;
import org.apache.openejb.server.auth.PermitAllPermission;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 */
public class ServiceAccessController implements ServerService {

    private final ServerService next;
    private IPAddressPermission[] hostPermissions;

    public ServiceAccessController(ServerService next) {
        this.next = next;
    }

    public void service(Socket socket) throws ServiceException, IOException {
        // Check authorization
        checkHostsAuthorization(socket.getInetAddress(), socket.getLocalAddress());

        next.service(socket);
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException("service(in,out)");
    }

    public void checkHostsAuthorization(InetAddress clientAddress, InetAddress serverAddress) throws SecurityException {
        // Check the client ip against the server ip. Hosts are
        // allowed to access themselves, so if these ips
        // match, the following for loop will be skipped.
        if (clientAddress.equals(serverAddress)) {
            return;
        }

        for (IPAddressPermission host : hostPermissions) {
            if (host.implies(clientAddress)) {
                return;
            }
        }

        throw new SecurityException("Host " + clientAddress.getHostAddress() + " is not authorized to access this service.");
    }

    private void parseAdminIPs(Properties props) throws ServiceException {
        LinkedList<IPAddressPermission> permissions = new LinkedList<IPAddressPermission>();

        String ipString = props.getProperty("only_from");

        if (ipString == null) {
            permissions.add(new PermitAllPermission());
        } else {
        	String hostname = "localhost";
            addIPAddressPermissions(permissions, hostname);

            StringTokenizer st = new StringTokenizer(ipString, " ");
            while (st.hasMoreTokens()) {
                String mask = st.nextToken();
                try {
                	permissions.add(IPAddressPermissionFactory.getIPAddressMask(mask));
                } catch (IllegalArgumentException iae) {
                	// it could be that it is a hostname not ip address
                	addIPAddressPermissions(permissions, mask);
                }
            }
        }

        hostPermissions = (IPAddressPermission[]) permissions.toArray(new IPAddressPermission[permissions.size()]);
    }

	private void addIPAddressPermissions(
			LinkedList<IPAddressPermission> permissions, String hostname)
			throws ServiceException {
		try {
		    InetAddress[] localIps = InetAddress.getAllByName(hostname);
		    for (int i = 0; i < localIps.length; i++) {
		        if (localIps[i] instanceof Inet4Address) {
		            permissions.add(new ExactIPAddressPermission(localIps[i].getAddress()));
		        } else {
		            permissions.add(new ExactIPv6AddressPermission(localIps[i].getAddress()));
		        }
		    }
		} catch (UnknownHostException e) {
		    throw new ServiceException("Could not get " + hostname + " inet address", e);
		}
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

    public String getName() {
        return next.getName();
    }

    public String getIP() {
        return next.getIP();
    }

    public int getPort() {
        return next.getPort();
    }

}
