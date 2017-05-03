package org.apache.openejb.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public static final String ORG_APACHE_OPENEJB_MULTIPULSE_TTL = "org.apache.openejb.multipulse.ttl";
    public static final String ORG_APACHE_OPENEJB_MULTIPULSE_URI_LIMIT = "org.apache.openejb.multipulse.uri.limit";

    private static final Logger log = Logger.getLogger("OpenEJB.client");
    private static final String SERVER = "OpenEJB.MCP.Server:";
    private static final String CLIENT = "OpenEJB.MCP.Client:";
    private static final String BADURI = ":BadUri:";
    private static final String EMPTY = "NoService";
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final int TTL = Integer.parseInt(System.getProperty(ORG_APACHE_OPENEJB_MULTIPULSE_TTL, "32"));
    private static final int LIMIT = Integer.parseInt(System.getProperty(ORG_APACHE_OPENEJB_MULTIPULSE_URI_LIMIT, "50000"));
    private static final Map<URI, Set<URI>> knownUris = new HashMap<URI, Set<URI>>();
    private static NetworkInterface[] interfaces = getNetworkInterfaces();
    private static ExecutorService executor = null;

    private static synchronized NetworkInterface[] getInterfaces() {
        if (null == interfaces) {
            interfaces = getNetworkInterfaces();
        }

        return interfaces;
    }

    private static synchronized ExecutorService getExecutorService() {

        if (null == executor) {

            int length = getInterfaces().length;
            if (length < 1) {
                length = 1;
            }

            executor = Executors.newFixedThreadPool(length * 2);
        }

        return executor;
    }

    /**
     * @param uri Connection URI
     * @return Connection
     * @throws IOException              or error
     * @throws IllegalArgumentException On undefined error
     */
    @Override
    public Connection getConnection(final URI uri) throws IOException {

        if (knownUris.size() >= LIMIT) {
            //This is here just as a brake to prevent DOS or OOME.
            //There is no way we should have more than this number of unique MutliPulse URI's in a LAN
            throw new IllegalArgumentException("Unique MultiPulse URI limit of " +
                LIMIT +
                " reached. Increase using the system property '" +
                ORG_APACHE_OPENEJB_MULTIPULSE_URI_LIMIT +
                "'");
        }

        Set<URI> uriSet = knownUris.get(uri);

        if (null == uriSet || uriSet.isEmpty()) {

            final Map<String, String> params = getUriParameters(uri);

            final Set<String> schemes = getSet(params, "schemes", this.getDefaultSchemes());
            final String group = getString(params, "group", "default");
            final long timeout = getLong(params, "timeout", 250);

            try {
                uriSet = MulticastPulseClient.discoverURIs(group, schemes, uri.getHost(), uri.getPort(), timeout);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to find an ejb server via the MultiPulse URI: " + uri);
            }

            knownUris.put(uri, uriSet);
        }

        for (final URI serviceURI : uriSet) {

            //Strip serverhost and group and try to connect
            final URI tryUri = URI.create(URI.create(serviceURI.getSchemeSpecificPart()).getSchemeSpecificPart());

            try {
                return ConnectionManager.getConnection(tryUri);
            } catch (Exception e) {

                uriSet.remove(serviceURI);

                if (java.net.SocketTimeoutException.class.isInstance(e) || SocketException.class.isInstance(e)) {
                    //Notify server that this URI is not reachable
                    MulticastPulseClient.broadcastBadUri(getString(getUriParameters(uri), "group", "default"), tryUri, uri.getHost(), uri.getPort());
                }

                if (log.isLoggable(Level.FINE)) {
                    log.fine("Failed connection to: " + serviceURI);
                }
            }
        }

        throw new IOException("Unable to connect an ejb server via the MultiPulse URI: " + uri);
    }

    private static Map<String, String> getUriParameters(final URI uri) {
        final Map<String, String> params;
        try {
            params = URIs.parseParamters(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid MultiPulse uri " + uri.toString(), e);
        }
        return params;
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
    public static Set<URI> discoverURIs(final String forGroup, final Set<String> schemes, final String host, final int port, long timeout) throws Exception {

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

        final InetAddress ia = getAddress(host);

        final byte[] bytes = (MulticastPulseClient.CLIENT + forGroup).getBytes(UTF8);
        final DatagramPacket request = new DatagramPacket(bytes, bytes.length, new InetSocketAddress(ia, port));

        final AtomicBoolean running = new AtomicBoolean(true);
        final List<Future> futures = Collections.synchronizedList(new ArrayList<Future>());

        MulticastSocket[] clientSockets = null;

        try {
            clientSockets = MulticastPulseClient.getSockets(ia, port);
            final MulticastSocket[] clientSocketsFinal = clientSockets;

            final Timer timer = new Timer(true);

            final Set<URI> set = new TreeSet<URI>(new Comparator<URI>() {
                @Override
                public int compare(final URI uri1, final URI uri2) {

                    //Ignore server hostname
                    URI u1 = URI.create(uri1.getSchemeSpecificPart());
                    URI u2 = URI.create(uri2.getSchemeSpecificPart());

                    //Ignore scheme (ejb,ejbs,etc.)
                    u1 = URI.create(u1.getSchemeSpecificPart());
                    u2 = URI.create(u2.getSchemeSpecificPart());

                    //Compare URI hosts
                    int i = compare(u1.getHost(), u2.getHost());
                    if (i != 0) {
                        i = uri1.compareTo(uri2);
                    }

                    return i;
                }

                private int compare(final String h1, final String h2) {

                    //Sort by hostname, IPv4, IPv6

                    try {
                        InetAddress address1 = null;
                        InetAddress address2 = null;
                        try {
                            address1 = InetAddress.getByName(h1);
                            address2 = InetAddress.getByName(h2);
                        } catch(final UnknownHostException e) {
                            // no-op
                        }

                        if (isIPv4LiteralAddress(address1)) {
                            if (isIPv6LiteralAddress(address2)) {
                                return -1;
                            }
                        } else if (isIPv6LiteralAddress(address1)) {
                            if (isIPv4LiteralAddress(address2)) {
                                return 1;
                            }
                        } else if (0 != h1.compareTo(h2)) {
                            return -1;
                        }
                    } catch (Exception e) {
                        //Ignore
                    }

                    return h1.compareTo(h2);
                }

                private boolean isIPv4LiteralAddress(final InetAddress val) {
                    return Inet4Address.class.isInstance(val);
                }

                private boolean isIPv6LiteralAddress(final InetAddress val) {
                    return Inet6Address.class.isInstance(val);
                }
            });

            final ReentrantLock setLock = new ReentrantLock();

            //Start threads that listen for multicast packets on our channel.
            //These need to start 'before' we pulse a request.
            final CountDownLatch latchListeners = new CountDownLatch(clientSocketsFinal.length);

            for (final MulticastSocket socket : clientSocketsFinal) {

                futures.add(getExecutorService().submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final DatagramPacket response = new DatagramPacket(new byte[2048], 2048);
                            latchListeners.countDown();

                            while (running.get()) {
                                try {

                                    socket.receive(response);

                                    final SocketAddress sa = response.getSocketAddress();

                                    if (null != sa && (sa instanceof InetSocketAddress)) {

                                        int len = response.getLength();
                                        if (len > 2048) {

                                            if (log.isLoggable(Level.FINE)) {
                                                log.log(Level.FINE, "Truncating multipulse length {0} to 2048", new Object[]{len});
                                            }
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

                                            final String services = s.substring(0, s.lastIndexOf('|'));
                                            s = s.substring(services.length() + 1);

                                            final String[] serviceList = services.split("\\|");
                                            final String[] hosts = s.split(",");

                                            for (final String svc : serviceList) {

                                                if (EMPTY.equals(svc)) {
                                                    continue;
                                                }

                                                final URI serviceUri;
                                                try {
                                                    serviceUri = URI.create(svc);
                                                } catch (Exception e) {
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

                                                    final String svcfull = ("mp-" + serverHost + ":" + group + ":" + svc);

                                                    setLock.lock();

                                                    try {
                                                        if (svcfull.contains("0.0.0.0")) {
                                                            for (final String h : hosts) {
                                                                if (!h.replace("[", "").startsWith("2001:0:")) { //Filter Teredo
                                                                    set.add(URI.create(svcfull.replace("0.0.0.0", ipFormat(h))));
                                                                }
                                                            }
                                                        } else if (svcfull.contains("[::]")) {
                                                            for (final String h : hosts) {
                                                                if (!h.replace("[", "").startsWith("2001:0:")) { //Filter Teredo
                                                                    set.add(URI.create(svcfull.replace("[::]", ipFormat(h))));
                                                                }
                                                            }
                                                        } else {
                                                            //Just add as is
                                                            set.add(URI.create(svcfull));
                                                        }
                                                    } catch (Exception e) {
                                                        //Ignore
                                                    } finally {
                                                        setLock.unlock();
                                                    }
                                                }
                                            }
                                        }
                                    }

                                } catch (Exception e) {
                                    //Ignore
                                }
                            }
                        } finally {
                            try {
                                socket.leaveGroup(ia);
                            } catch (Exception e) {
                                //Ignore
                            }
                            try {
                                socket.close();
                            } catch (Exception e) {
                                //Ignore
                            }
                        }
                    }
                }));
            }

            try {
                //Give listener threads a reasonable amount of time to start
                if (latchListeners.await(clientSocketsFinal.length * 2, TimeUnit.SECONDS)) {

                    //Start pulsing client request every 10ms - This will ensure we have at least 4 client pulses within our minimum timeout
                    //This pulse is designed to tell a listening server to wake up and pulse back a response
                    futures.add(0, getExecutorService().submit(new Runnable() {
                        @Override
                        public void run() {
                            while (running.get()) {
                                //Pulse to listening servers - It is thread safe to use same sockets as send/receive synchronization is only on the packet
                                for (final MulticastSocket socket : clientSocketsFinal) {

                                    if (running.get()) {
                                        try {
                                            socket.send(request);
                                        } catch (Exception e) {
                                            //Ignore
                                        }
                                    } else {
                                        break;
                                    }
                                }

                                if (running.get()) {
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        break;
                                    }
                                }
                            }
                        }
                    }));
                } else {
                    timeout = 1;
                }

            } catch (InterruptedException e) {
                //Terminate as quickly as possible
                timeout = 1;
            }

            //Kill the threads after timeout
            timer.schedule(new TimerTask() {
                @Override
                public void run() {

                    running.set(false);

                    try {
                        for (final Future future : futures) {
                            future.cancel(true);
                        }
                    } catch (ConcurrentModificationException e) {
                        //Ignore
                    }

                }
            }, timeout);

            //Wait for threads to complete
            for (final Future future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    //Ignore
                }
            }

            setLock.lock();
            try {
                return new TreeSet<URI>(set);
            } finally {
                setLock.unlock();
            }
        } finally {

            //Just to be sure we are clean
            for (final Future future : futures) {
                try {
                    future.cancel(true);
                } catch (Exception e) {
                    //Ignore
                }
            }

            futures.clear();

            for (final MulticastSocket socket : clientSockets) {

                try {
                    socket.leaveGroup(ia);
                } catch (Exception e) {
                    //Ignore
                }
                try {
                    socket.close();
                } catch (Exception e) {
                    //Ignore
                }
            }
        }
    }

    private static InetAddress getAddress(final String host) throws Exception {
        final InetAddress ia;
        try {
            ia = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new Exception(host + " is not a valid address", e);
        }

        if (null == ia || !ia.isMulticastAddress()) {
            throw new Exception(host + " is not a valid multicast address");
        }
        return ia;
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

        final InetAddress ia = InetAddress.getByName(h);
        if (ia instanceof Inet6Address) {
            return "[" + ia.getHostAddress() + "]";
        } else {
            return h;
        }
    }

    public static MulticastSocket[] getSockets(final InetAddress ia, final int port) throws Exception {

        final ArrayList<MulticastSocket> list = new ArrayList<MulticastSocket>();

        for (final NetworkInterface ni : getInterfaces()) {

            MulticastSocket ms = null;

            try {

                ms = new MulticastSocket(port);
                ms.setNetworkInterface(ni);
                ms.setSoTimeout(0);
                ms.setTimeToLive(TTL);
                if (!ms.getBroadcast()) {
                    ms.setBroadcast(true);
                }
                ms.joinGroup(ia);

                list.add(ms);

            } catch (Exception e) {

                if (null != ms) {
                    try {
                        ms.close();
                    } catch (Exception t) {
                        //Ignore
                    }
                }

            }
        }

        return list.toArray(new MulticastSocket[list.size()]);
    }

    private static NetworkInterface[] getNetworkInterfaces() {

        final HashSet<NetworkInterface> list = new HashSet<NetworkInterface>();

        try {
            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                final NetworkInterface next = interfaces.nextElement();

                if (next.supportsMulticast() && next.isUp()) {
                    list.add(next);
                }
            }
        } catch (SocketException e) {
            //Ignore
        }

        return list.toArray(new NetworkInterface[list.size()]);
    }

    private static final CommandParser cmd = new CommandParser() {
        @Override
        protected void init() {
            category("Options");

            opt('g', "group").type(String.class).value("*").description("Group name");

            opt('h', "host").type(String.class).value("239.255.3.2").description("Multicast address");

            opt('p', "port").type(int.class).value(6142).description("Multicast port");

            opt('t', "timeout").type(int.class).value(1000).description("Pulse back timeout");
        }

        @Override
        protected List<String> validate(final Arguments arguments) {
            return super.validate(arguments);
        }

        @Override
        protected List<String> usage() {
            return super.usage();
        }
    };

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
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

        System.out.println(String.format("Using discovery options group=%1$s, host=%2$s, port=%3$s, timeout=%4$s", discover, mchost, mcport, timeout));
        System.out.println();

        final AtomicBoolean running = new AtomicBoolean(true);

        final Thread t = new Thread(new Runnable() {
            @SuppressWarnings("UseOfSystemOutOrSystemErr")
            @Override
            public void run() {
                while (running.get()) {

                    Set<URI> uriSet = null;
                    try {
                        uriSet = MulticastPulseClient.discoverURIs(discover, new HashSet<String>(Arrays.asList("ejbd", "ejbds", "http", "https")), mchost, mcport, timeout);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }

                    final int size = uriSet.size();
                    if (uriSet != null && size > 0) {

                        final int st = (timeout / size);

                        for (final URI uri : uriSet) {

                            final String server = uri.getScheme().replace("mp-", "");
                            URI uriSub = URI.create(uri.getSchemeSpecificPart());

                            final String group = uriSub.getScheme();
                            uriSub = URI.create(uriSub.getSchemeSpecificPart());

                            final String host = uriSub.getHost();
                            final int port = uriSub.getPort();

                            if (MulticastPulseClient.isLocalAddress(host, false) && !MulticastPulseClient.isLocalAddress(server, false)) {
                                System.out.println(server + ":" + group + " - " + uriSub.toASCIIString() + " is not a local service");
                                continue;
                            }

                            System.out.print(server + ":" + group + " - " + uriSub.toASCIIString() + " is reachable: ");

                            boolean b = false;
                            final Socket s = new Socket();
                            try {
                                s.connect(new InetSocketAddress(host, port), st);
                                b = true;
                            } catch (Exception e) {
                                if (java.net.SocketTimeoutException.class.isInstance(e) || SocketException.class.isInstance(e)) {
                                    MulticastPulseClient.broadcastBadUri(group, uriSub, mchost, mcport);
                                    System.out.print("" + e + " : ");
                                }
                            } finally {
                                try {
                                    s.close();
                                } catch (Exception e) {
                                    //Ignore
                                }
                            }

                            System.out.println(b);
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

        //noinspection ResultOfMethodCallIgnored
        System.in.read();

        running.set(false);
        t.interrupt();
    }

    /**
     * Asynchronous attempt to broadcast a bad URI on our channel.
     * Hopefully the culprit server will hear this and stop sending it.
     *
     * @param uri Bad URI to broadcast
     */
    private static void broadcastBadUri(final String group, final URI uri, final String host, final int port) {

        getExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final InetAddress ia = getAddress(host);

                    final byte[] bytes = (MulticastPulseClient.CLIENT + group + MulticastPulseClient.BADURI + uri.getHost()).getBytes(UTF8);
                    final DatagramPacket request = new DatagramPacket(bytes, bytes.length, new InetSocketAddress(ia, port));

                    final MulticastSocket[] multicastSockets = MulticastPulseClient.getSockets(ia, port);

                    for (final MulticastSocket socket : multicastSockets) {

                        try {
                            socket.send(request);
                        } catch (Exception e) {
                            log.log(Level.WARNING, "Failed to broadcast bad URI: " + uri + " on: " + socket.getInterface().getHostAddress(), e);
                        }
                    }
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to broadcast bad URI: " + uri, e);
                }
            }
        });
    }
}
