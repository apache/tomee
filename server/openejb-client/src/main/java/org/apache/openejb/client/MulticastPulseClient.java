package org.apache.openejb.client;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class MulticastPulseClient extends MulticastConnectionFactory {

    private static final String SERVER = "OpenEJB.MCP.Server:";
    private static final String CLIENT = "OpenEJB.MCP.Client:";
    private static final String EMPTY = "NoService";

    private static final Set<URI> badUri = new HashSet<URI>();

    /**
     * @param uri Connection URI
     * @return Connection
     * @throws IOException              or error
     * @throws IllegalArgumentException On undefined error
     */
    @Override
    public Connection getConnection(final URI uri) throws IOException {
        final Map<String, String> params;
        try {
            params = URIs.parseParamters(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid multicast uri " + uri.toString(), e);
        }

        final Set<String> schemes = getSet(params, "schemes", this.getDefaultSchemes());
        final String group = getString(params, "group", "default");
        final long timeout = getLong(params, "timeout", 250);

        final Set<URI> uriSet;
        try {
            uriSet = MulticastPulseClient.discoverURIs(group, schemes, uri.getHost(), uri.getPort(), timeout);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to find an ejb server via the MulticastPulse URI: " + uri);
        }

        for (final URI serviceURI : uriSet) {

            if (badUri.contains(serviceURI)) {
                continue;
            }

            try {
                //Strip serverhost and group and try to connect
                return ConnectionManager.getConnection(URI.create(URI.create(serviceURI.getSchemeSpecificPart()).getSchemeSpecificPart()));
            } catch (IOException e) {
                badUri.add(serviceURI);
            }
        }

        throw new IllegalArgumentException("Unable to connect an ejb server via the MulticastPulse URI: " + uri);
    }

    /**
     * Clear the list of bad URIs that may have been collected (if any).
     */
    public static void clearBadUris() {
        badUri.clear();
    }

    /**
     * Get a list of URIs discovered for the provided request.
     * <p/>
     * Returned URIs are of the format 'mp-{serverhost}:group:scheme://servicehost:port'.
     * The serverhost is prefixed with 'mp-' in case the serverhost is an IP-Address, as RFC 2396 defines scheme must begin with a 'letter'
     *
     * @param forGroup Specific case sensitive group name or * for all
     * @param schemes  Acceptable scheme list
     * @param host     Multicast host address
     * @param port     Multicast port
     * @param timeout  Time to wait for a server response, at least 50ms
     * @return A URI set, possibly empty
     * @throws Exception On error
     */
    public static synchronized Set<URI> discoverURIs(final String forGroup, final Set<String> schemes, final String host, final int port, long timeout) throws Exception {

        if (timeout < 50) {
            timeout = 50;
        }

        if (null == forGroup || forGroup.isEmpty()) {
            throw new Exception("Specify a valid group or *");
        }

        if (null == schemes || schemes.isEmpty()) {
            throw new Exception("Specify at least one scheme, 'ejbd' for example");
        }

        if (null == host || host.isEmpty()) {
            throw new Exception("Specify a valid host name");
        }

        if (port < 1 || port > 65535) {
            throw new Exception("Specify a valid port between 1 and 65535");
        }

        final InetAddress ia;

        try {
            ia = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new Exception(host + " is not a valid address", e);
        }

        if (null == ia || !ia.isMulticastAddress()) {
            throw new Exception(host + " is not a valid multicast address");
        }

        final byte[] bytes = (MulticastPulseClient.CLIENT + forGroup).getBytes(Charset.forName("utf8"));
        final DatagramPacket request = new DatagramPacket(bytes, bytes.length, new InetSocketAddress(ia, port));


        final AtomicBoolean running = new AtomicBoolean(true);
        final MulticastSocket client = MulticastPulseClient.getSocket(ia, port);
        final Timer timer = new Timer(true);

        final Set<URI> set = new TreeSet<URI>(new Comparator<URI>() {
            @Override
            public int compare(URI u1, URI u2) {

                //Ignore server hostname
                final String serverHost = u1.getScheme();
                u1 = URI.create(u1.getSchemeSpecificPart());
                u2 = URI.create(u2.getSchemeSpecificPart());

                //Ignore scheme (ejb,ejbs,etc.)
                u1 = URI.create(u1.getSchemeSpecificPart());
                u2 = URI.create(u2.getSchemeSpecificPart());

                if (u1.getHost().equals(serverHost)) {
                    //If the service host is the same as the server host
                    //then keep it at the top of the list
                    return -1;
                }

                //Compare URI hosts
                int i = u1.getHost().compareTo(u2.getHost());

                if (i == 0) {
                    i = u1.compareTo(u2);
                }

                return i;
            }
        });

        //Start a thread that listens for multicast packets on our channel.
        //This needs to start 'before' we pulse a request.
        final Thread t = new Thread() {
            @Override
            public void run() {

                final DatagramPacket response = new DatagramPacket(new byte[2048], 2048);

                while (running.get()) {
                    try {

                        client.receive(response);

                        final SocketAddress sa = response.getSocketAddress();

                        if (null != sa && (sa instanceof InetSocketAddress)) {

                            int len = response.getLength();
                            if (len > 2048) {
                                len = 2048;
                            }

                            String s = new String(response.getData(), 0, len);

                            if (s.startsWith(MulticastPulseClient.SERVER)) {

                                s = (s.replace(MulticastPulseClient.SERVER, ""));
                                final String group = s.substring(0, s.indexOf(':'));
                                s = s.substring(group.length() + 1);

                                if (!"*".equals(forGroup) && !forGroup.equals(group)) {
                                    continue;
                                }

                                final String services = s.substring(0, s.indexOf('|'));
                                s = s.substring(services.length() + 1);

                                final String[] serviceList = services.split("\\|");
                                final String[] hosts = s.split(",");

                                for (String svc : serviceList) {

                                    if (EMPTY.equals(svc)) {
                                        continue;
                                    }

                                    final URI serviceUri;
                                    try {
                                        serviceUri = URI.create(svc);
                                    } catch (Throwable e) {
                                        continue;
                                    }

                                    if (schemes.contains(serviceUri.getScheme())) {

                                        //Just because multicast was received on this host is does not mean the service is on the same
                                        //We can however use this to identify an individual machine and group
                                        final String serverHost = ((InetSocketAddress) response.getSocketAddress()).getAddress().getHostAddress();

                                        final String serviceHost = serviceUri.getHost();
                                        if (MulticastPulseClient.isLocalAddress(serviceHost, false)) {
                                            if (!MulticastPulseClient.isLocalAddress(serverHost, false)) {
                                                //A local service is only available to a local client
                                                continue;
                                            }
                                        }

                                        svc = ("mp-" + serverHost + ":" + group + ":" + svc);

                                        try {
                                            if (svc.contains("0.0.0.0")) {
                                                for (final String h : hosts) {
                                                    set.add(URI.create(svc.replace("0.0.0.0", ipFormat(h))));
                                                }
                                            } else if (svc.contains("[::]")) {
                                                for (final String h : hosts) {
                                                    set.add(URI.create(svc.replace("[::]", ipFormat(h))));
                                                }
                                            } else {
                                                //Just add as is
                                                set.add(URI.create(svc));
                                            }
                                        } catch (Throwable e) {
                                            //Ignore
                                        }
                                    }
                                }
                            }
                        }

                    } catch (Throwable e) {
                        //Ignore
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();

        //Kill the thread after timeout
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                running.set(false);

                try {
                    client.leaveGroup(ia);
                } catch (Throwable e) {
                    //Ignore
                }
                try {
                    client.close();
                } catch (Throwable e) {
                    //Ignore
                }
                t.interrupt();
            }
        }, timeout);

        //Pulse the server
        final MulticastSocket ms = MulticastPulseClient.getSocket(ia, port);
        ms.send(request);

        //Wait for thread to complete
        t.join();

        return set;
    }

    /**
     * Is the provided host a local host
     *
     * @param host            The host to test
     * @param wildcardIsLocal Should 0.0.0.0 or [::] be deemed as local
     * @return True is the host is a local host else false
     */
    public static boolean isLocalAddress(final String host, final boolean wildcardIsLocal) {

        final InetAddress addr;
        try {
            addr = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            return false;
        }

        // Check if the address is a valid special local or loop back
        if ((wildcardIsLocal && addr.isAnyLocalAddress()) || addr.isLoopbackAddress()) {
            return true;
        }

        // Check if the address is defined on any local interface
        try {
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (SocketException e) {
            return false;
        }
    }

    private static String ipFormat(final String h) throws UnknownHostException {

        final InetAddress ia = Inet6Address.getByName(h);
        if (ia instanceof Inet6Address) {
            return "[" + ia.getHostAddress() + "]";
        } else {
            return h;
        }
    }

    public static MulticastSocket getSocket(final InetAddress ia, final int port) throws Exception {

        MulticastSocket ms = null;

        try {
            ms = new MulticastSocket(port);
            final NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(InetAddress.getLocalHost().getHostName()));
            ms.setNetworkInterface(ni);
            ms.setSoTimeout(0);
            if (!ms.getBroadcast()) {
                ms.setBroadcast(true);
            }
            ms.joinGroup(ia);

        } catch (Throwable e) {

            if (null != ms) {
                try {
                    ms.close();
                } catch (Throwable t) {
                    //Ignore
                }
            }

            throw new Exception("Failed to create a MultiPulse socket", e);
        }

        return ms;
    }

    private static final CommandParser cmd = new CommandParser() {
        @Override
        protected void init() {
            category("Options");

            opt('g', "group").type(String.class).value("*")
                    .description("Group name");

            opt('h', "host").type(String.class).value("239.255.3.2")
                    .description("Multicast address");

            opt('p', "port").type(int.class).value(6142)
                    .description("Multicast port");

            opt('t', "timeout").type(int.class).value(1000)
                    .description("Pulse back timeout");
        }

        @Override
        protected List<String> validate(Arguments arguments) {
            return super.validate(arguments);
        }

        @Override
        protected List<String> usage() {
            return super.usage();
        }
    };

    public static void main(final String[] args) throws Exception {

        final CommandParser.Arguments arguments;
        try {
            arguments = cmd.parse(args);
        } catch (CommandParser.HelpException e) {
            System.exit(0);
            throw new Exception(); // never reached, but keeps compiler happy
        } catch (CommandParser.InvalidOptionsException e) {
            System.exit(1);
            throw new Exception(); // never reached, but keeps compiler happy
        }

        final Options options = arguments.options();

        final String discover = options.get("group", "*");
        final String mchost = options.get("host", "239.255.3.2");
        final int mcport = options.get("port", 6142);
        final int timeout = options.get("timeout", 1500);
        final AtomicBoolean running = new AtomicBoolean(true);

        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running.get()) {

                    Set<URI> uriSet = null;
                    try {
                        uriSet = MulticastPulseClient.discoverURIs(
                                discover,
                                new HashSet<String>(Arrays.asList("ejbd", "ejbds", "http", "https")),
                                mchost,
                                mcport,
                                timeout);
                    } catch (Throwable e) {
                        System.err.println(e.getMessage());
                    }

                    if (uriSet != null && uriSet.size() > 0) {

                        for (URI uri : uriSet) {

                            final String server = uri.getScheme().replace("mp-", "");
                            uri = URI.create(uri.getSchemeSpecificPart());

                            final String group = uri.getScheme();
                            uri = URI.create(uri.getSchemeSpecificPart());

                            final String host = uri.getHost();
                            final int port = uri.getPort();

                            if (MulticastPulseClient.isLocalAddress(host, false) && !MulticastPulseClient.isLocalAddress(server, false)) {
                                System.out.println(server + ":" + group + " - " + uri.toASCIIString() + " is not a local service");
                                continue;
                            }

                            boolean b = false;
                            final Socket s = new Socket();
                            try {
                                s.connect(new InetSocketAddress(host, port), 1000);
                                b = true;
                            } catch (Throwable e) {
                                //Ignore
                            } finally {
                                try {
                                    s.close();
                                } catch (Throwable e) {
                                    //Ignore
                                }
                            }

                            System.out.println(server + ":" + group + " - " + uri.toASCIIString() + " is reachable: " + b);
                        }
                    } else {
                        System.out.println("### Failed to discover server: " + discover);
                    }

                    System.out.println(".");

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        //Ignore
                    }
                }
            }
        }, "MulticastPulseClient Test");

        t.setDaemon(true);
        t.start();

        System.in.read();

        running.set(false);
        t.interrupt();
    }
}
