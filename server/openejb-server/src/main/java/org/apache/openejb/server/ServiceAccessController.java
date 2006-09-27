package org.apache.openejb.server;

import java.io.*;
import java.net.*;
import java.util.*;

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
