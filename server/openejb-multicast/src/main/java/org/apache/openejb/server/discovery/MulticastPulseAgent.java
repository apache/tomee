package org.apache.openejb.server.discovery;

import org.apache.openejb.loader.Options;
import org.apache.openejb.server.DiscoveryAgent;
import org.apache.openejb.server.DiscoveryListener;
import org.apache.openejb.server.SelfManaging;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.OptionsLog;
import sun.net.util.IPAddressUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
public class MulticastPulseAgent implements DiscoveryAgent, ServerService, SelfManaging {

    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB_SERVER.createChild("discovery").createChild("multipulse"), MulticastPulseAgent.class);
    private static final NetworkInterface[] interfaces = getNetworkInterfaces();
    private static final ExecutorService executor = Executors.newFixedThreadPool((interfaces.length + 2) * 2);
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final int TTL = Integer.parseInt(System.getProperty("org.apache.openejb.multipulse.ttl", "32"));

    public static final String SERVER = "OpenEJB.MCP.Server:";
    public static final String CLIENT = "OpenEJB.MCP.Client:";
    public static final String EMPTY = "NoService";

    private final Set<String> ignore = new HashSet<String>();
    private final Set<URI> uriSet = new HashSet<URI>();
    private AtomicBoolean running = new AtomicBoolean(false);
    final ArrayList<Future> futures = new ArrayList<Future>();
    private MulticastSocket[] sockets = null;
    private InetSocketAddress address = null;

    private String multicast = "239.255.3.2";
    private String group = "default";
    private int port = 6142;
    private DatagramPacket response = null;
    private DiscoveryListener listener = null;
    private boolean loopbackOnly = true;

    /**
     * This agent listens for client pulses on a defined multicast channel.
     * On receipt of a valid pulse the agent responds with its own pulse for
     * a defined amount of time and rate. A client can deliver a pulse as often as
     * required until it is happy of the server response.
     * <p/>
     * Both server and client deliver crafted information payloads.
     */
    public MulticastPulseAgent() {
    }

    @Override
    public void init(final Properties p) throws Exception {
        final Options o = new Options(p);
        o.setLogger(new OptionsLog(log));

        this.ignore.add("localhost");
        this.ignore.add("::1");
        this.ignore.add("127.0.0.1");

        try {
            final String[] ignoreList = o.get("ignore", "").split(",");
            for (final String s : ignoreList) {
                if (null != s && s.trim().length() > 0) {
                    this.ignore.add(s.trim().toLowerCase());
                }
            }
        } catch (Exception e) {
            log.warning("Invalid ignore parameter. Should be a lowercase single or comma seperated list like: ignore=host1,host2");
        }

        this.multicast = o.get("bind", this.multicast);
        this.port = o.get("port", this.port);
        this.group = o.get("group", this.group);

        final InetAddress ia = InetAddress.getByName(this.multicast);
        this.address = new InetSocketAddress(ia, this.port);
        this.buildPacket();
    }

    private void buildPacket() throws SocketException {

        this.loopbackOnly = true;
        for (final URI uri : uriSet) {
            if (!isLoopback(uri.getHost())) {
                this.loopbackOnly = false;
                break;
            }
        }

        final String hosts = getHosts(this.ignore);
        final StringBuilder sb = new StringBuilder(SERVER);
        sb.append(this.group);
        sb.append(':');

        if (this.uriSet.size() > 0) {
            for (final URI uri : this.uriSet) {
                sb.append(uri.toASCIIString());
                sb.append('|');
            }
        } else {
            sb.append(EMPTY);
            sb.append('|');
        }

        sb.append(hosts);

        final byte[] bytes = (sb.toString()).getBytes(UTF8);
        this.response = new DatagramPacket(bytes, bytes.length, this.address);

        log.debug("MultiPulse packet is: " + sb);

        if (bytes.length > 2048) {
            log.warning("MultiPulse packet is larger than 2048 bytes, clients will not be able to read the packet");
        }
    }

    @Override
    public void setDiscoveryListener(final DiscoveryListener listener) {
        this.listener = listener;
    }

    @Override
    public void registerService(URI uri) throws IOException {

        uri = parseUri(uri);

        if (this.uriSet.add(uri)) {
            this.buildPacket();
            this.fireEvent(uri, true);
        }
    }

    @Override
    public void unregisterService(final URI uri) throws IOException {

        final URI tmp = parseUri(uri);

        if (this.uriSet.remove(tmp)) {
            this.fireEvent(uri, false);
        }
    }

    @Override
    public void reportFailed(final URI serviceUri) throws IOException {
        this.unregisterService(serviceUri);
    }

    /**
     * Strip the scheme
     *
     * @param uri URI to strip the scheme
     * @return Stripped URI
     */
    private URI parseUri(final URI uri) {
        return URI.create(uri.getSchemeSpecificPart());
    }

    private void fireEvent(final URI uri, final boolean add) {
        if (null != this.listener) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (add) {
                        MulticastPulseAgent.this.listener.serviceAdded(uri);
                    } else {
                        MulticastPulseAgent.this.listener.serviceRemoved(uri);
                    }
                }
            });
        }
    }

    @Override
    public void start() throws ServiceException {
        if (!this.running.getAndSet(true)) {

            try {
                this.sockets = getSockets(this.multicast, this.port);
            } catch (Exception e) {
                throw new ServiceException("Failed to get Multicast sockets", e);
            }

            final CountDownLatch latch = new CountDownLatch(this.sockets.length);
            final String mpg = MulticastPulseAgent.this.group;
            final boolean isLoopBackOnly = MulticastPulseAgent.this.loopbackOnly;
            final DatagramPacket mpr = MulticastPulseAgent.this.response;

            for (final MulticastSocket socket : this.sockets) {

                this.futures.add(executor.submit(new Runnable() {
                    @Override
                    public void run() {

                        final DatagramPacket request = new DatagramPacket(new byte[2048], 2048);
                        latch.countDown();

                        while (MulticastPulseAgent.this.running.get()) {

                            try {
                                socket.receive(request);
                                final SocketAddress sa = request.getSocketAddress();

                                if (null != sa) {

                                    final String req = new String(request.getData(), 0, request.getLength());

                                    executor.execute(new Runnable() {
                                        @Override
                                        public void run() {

                                            String s = req;

                                            if (s.startsWith(CLIENT)) {

                                                s = (s.replace(CLIENT, ""));

                                                if (mpg.equals(s) || "*".equals(s)) {

                                                    final String client = ((InetSocketAddress) sa).getAddress().getHostAddress();

                                                    if (isLoopBackOnly) {
                                                        //We only have local services, so make sure the request is from a local source else ignore it
                                                        if (!MulticastPulseAgent.isLocalAddress(client, false)) {
                                                            if (log.isDebugEnabled()) {
                                                                log.debug(String.format("Ignoring remote client %1$s pulse request for group: %2$s - No remote services available",
                                                                                        client,
                                                                                        s));
                                                            }
                                                            return;
                                                        }
                                                    }

                                                    if (log.isDebugEnabled()) {
                                                        log.debug(String.format("Answering client %1$s pulse request for group: %2$s", client, s));
                                                    }

                                                    //This is a valid client request for the server to respond on the same channel.
                                                    //Because multicast is not guaranteed we will send 3 responses per valid request at 10ms intervals.
                                                    for (int i = 0; i < 3; i++) {

                                                        try {
                                                            socket.send(mpr);
                                                        } catch (Exception e) {
                                                            if (log.isDebugEnabled()) {
                                                                log.debug("MulticastPulseAgent client error: " + e.getMessage(), e);
                                                            }
                                                        }

                                                        try {
                                                            Thread.sleep(10);
                                                        } catch (InterruptedException e) {
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });

                                }

                            } catch (Exception e) {
                                if (log.isDebugEnabled()) {
                                    log.debug("MulticastPulseAgent request error: " + e.getMessage(), e);
                                }
                            }
                        }

                        try {
                            socket.close();
                        } catch (Throwable e) {
                            //Ignore
                        }
                    }
                }));
            }

            try {
                //Give threads a reasonable amount of time to start
                latch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                this.stop();
            }
        }
    }

    @Override
    public void stop() throws ServiceException {
        if (this.running.getAndSet(false)) {

            try {
                //Iterrupt threads
                for (final Future future : this.futures) {
                    try {
                        future.cancel(true);
                    } catch (Throwable e) {
                        //Ignore
                    }
                }

                //Wait for threads to complete
                for (final Future future : this.futures) {
                    try {
                        future.get();
                    } catch (Throwable e) {
                        //Ignore
                    }
                }
            } finally {
                this.futures.clear();
            }

            if (null != this.sockets) {
                try {
                    for (final MulticastSocket s : sockets) {
                        try {
                            s.close();
                        } catch (Throwable e) {
                            //Ignore
                        }
                    }
                } finally {
                    this.sockets = null;
                }
            }
        }
    }

    @Override
    public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
        //Ignore
    }

    @Override
    public void service(final Socket socket) throws ServiceException, IOException {
        //Ignore
    }

    @Override
    public String getName() {
        return "multipulse";
    }

    @Override
    public String getIP() {
        return this.multicast;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    /**
     * Attempts to return at least one socket per valid network interface.
     * If no valid interface is found then the array will be empty.
     *
     * @param multicastAddress A valid multicast address
     * @param port             A valid multicast port
     * @return MulticastSocket[], may be empty if no valid interfaces exist
     * @throws Exception On invalid parameters
     */
    public static MulticastSocket[] getSockets(final String multicastAddress, final int port) throws Exception {

        final InetAddress ia;

        try {
            ia = InetAddress.getByName(multicastAddress);
        } catch (UnknownHostException e) {
            throw new ServiceException(multicastAddress + " is not a valid address", e);
        }

        if (null == ia || !ia.isMulticastAddress()) {
            throw new ServiceException(multicastAddress + " is not a valid multicast address");
        }

        return getSockets(ia, port);
    }

    private static MulticastSocket[] getSockets(final InetAddress ia, final int port) throws Exception {

        final ArrayList<MulticastSocket> list = new ArrayList<MulticastSocket>();

        for (final NetworkInterface ni : interfaces) {

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

                log.debug(String.format("Created MulticastSocket for '%1$s:%2$s' on network adapter: %3$s", ia.getHostName(), port, ni));

            } catch (Throwable e) {

                if (null != ms) {
                    try {
                        ms.close();
                    } catch (Throwable t) {
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

    /**
     * Is the provided host a valid loopback address
     *
     * @param host Host to test
     * @return True or false
     */
    public static boolean isLoopback(final String host) {

        final InetAddress addr;
        try {
            addr = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            return false;
        }

        return addr.isLoopbackAddress();
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

        // Check if the address is defined on any interface
        try {
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (SocketException e) {
            return false;
        }
    }

    private static String getHosts(final Set<String> ignore) {

        final Set<String> hosts = new TreeSet<String>(new Comparator<String>() {

            @Override
            public int compare(final String h1, final String h2) {

                //Sort by hostname, IPv4, IPv6

                try {
                    if (IPAddressUtil.isIPv4LiteralAddress(h1)) {
                        if (IPAddressUtil.isIPv6LiteralAddress(h2.replace("[", "").replace("]", ""))) {
                            return -1;
                        }
                    } else if (IPAddressUtil.isIPv6LiteralAddress(h1.replace("[", "").replace("]", ""))) {
                        if (IPAddressUtil.isIPv4LiteralAddress(h2)) {
                            return 1;
                        }
                    } else if (0 != h1.compareTo(h2)) {
                        return -1;
                    }
                } catch (Throwable e) {
                    //Ignore
                }

                return h1.compareTo(h2);
            }
        });

        try {
            final InetAddress localhost = InetAddress.getLocalHost();
            hosts.add(localhost.getHostAddress());
            //Multi-homed
            final InetAddress[] all = InetAddress.getAllByName(localhost.getCanonicalHostName());
            for (final InetAddress ip : all) {

                if (ip.isLinkLocalAddress() || ip.isMulticastAddress()) {
                    continue;
                }

                final String ha = ip.getHostAddress();
                if (!ha.replace("[", "").startsWith("2001:0:")) { //Filter Teredo
                    hosts.add(ha);
                    hosts.add(ip.getCanonicalHostName());
                }
            }
        } catch (UnknownHostException e) {
            log.warning("Failed to list machine hosts", e);
        }

        final StringBuilder sb = new StringBuilder();
        for (final String host : hosts) {
            final String lc = host.toLowerCase();
            if (!ignore.contains(lc)) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(host);
            }
        }

        return sb.toString();
    }
}
