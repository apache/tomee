package org.apache.openejb.server.discovery;

import org.apache.openejb.loader.Options;
import org.apache.openejb.server.DiscoveryAgent;
import org.apache.openejb.server.DiscoveryListener;
import org.apache.openejb.server.SelfManaging;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.util.DaemonThreadFactory;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.OptionsLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

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

    private static final Logger LOG = Logger.getInstance(LogCategory.OPENEJB_SERVER.createChild("discovery").createChild("multipulse"), MulticastPulseAgent.class);
    private static NetworkInterface[] interfaces = null;
    private static ExecutorService executor = null;
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final int TTL = Integer.parseInt(System.getProperty("org.apache.openejb.multipulse.ttl", "32"));

    public static final String SERVER = "OpenEJB.MCP.Server:";
    public static final String CLIENT = "OpenEJB.MCP.Client:";
    public static final String BADURI = ":BadUri:";
    public static final String EMPTY = "NoService";

    private final ReentrantLock lock = new ReentrantLock();
    private final Set<String> ignore = Collections.synchronizedSet(new HashSet<>());
    private final Set<URI> uriSet = new HashSet<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    final ArrayList<Future> futures = new ArrayList<>();
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
     * <p/>
     * The client pulse contains OpenEJB.MCP.Client:(group or *)[:BadUri:URI]
     * The server will only respond to a request for it's own group or *
     * The optional :BadUri: is used by clients to notify a server that it is sending out unreachable URI's
     * <p/>
     * The server response pulse contains OpenEJB.MCP.Server:(Service|Service)|(Comma separated host list)
     */
    public MulticastPulseAgent() {
    }

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

            executor = Executors.newFixedThreadPool(length * 3, new DaemonThreadFactory("multicast-pulse-agent-"));
        }

        return executor;
    }

    @Override
    public void init(final Properties p) throws Exception {
        final Options o = new Options(p);
        o.setLogger(new OptionsLog(LOG));

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
        } catch (final Exception e) {
            LOG.warning("Invalid ignore parameter. Should be a lowercase single host or comma seperated list of hosts to ignore like: ignore=host1,host2,ipv4,ipv6");
        }

        this.multicast = o.get("bind", this.multicast);
        this.port = o.get("port", this.port);
        this.group = o.get("group", this.group);

        final InetAddress ia = InetAddress.getByName(this.multicast);
        this.address = new InetSocketAddress(ia, this.port);
        this.buildPacket();
    }

    private void buildPacket() throws SocketException {

        final ReentrantLock l = this.lock;
        l.lock();

        try {
            this.loopbackOnly = true;
            for (final URI uri : this.uriSet) {
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

            if (LOG.isDebugEnabled()) {
                LOG.debug("MultiPulse packet is: " + sb);
            }

            if (bytes.length > 2048) {
                LOG.warning("MultiPulse packet is larger than 2048 bytes, clients will not be able to read the packet" +
                        "\n - You should define the 'ignore' property to filter out unreachable addresses: " + sb);
            }
        } finally {
            l.unlock();
        }
    }

    public DatagramPacket getResponsePacket() {
        final ReentrantLock l = this.lock;
        l.lock();

        try {
            return this.response;
        } finally {
            l.unlock();
        }
    }

    @Override
    public void setDiscoveryListener(final DiscoveryListener listener) {
        this.listener = listener;
    }

    public DiscoveryListener getDiscoveryListener() {
        return listener;
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
            final DiscoveryListener dl = this.listener;
            getExecutorService().execute(new Runnable() {
                @Override
                public void run() {
                    if (add) {
                        dl.serviceAdded(uri);
                    } else {
                        dl.serviceRemoved(uri);
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
            } catch (final Exception e) {
                throw new ServiceException("Failed to get Multicast sockets", e);
            }

            final CountDownLatch latch = new CountDownLatch(this.sockets.length);
            final String mpg = this.group;
            final boolean isLoopBackOnly = this.loopbackOnly;
            final ExecutorService executorService = getExecutorService();
            final MulticastPulseAgent agent = MulticastPulseAgent.this;

            for (final MulticastSocket socket : this.sockets) {

                final String socketKey;
                try {
                    socketKey = socket.getNetworkInterface().toString();
                } catch (final SocketException e) {
                    LOG.error("Failed to get network interface name on: " + socket, e);
                    continue;
                }

                final Sender sender = new Sender(this, socketKey, socket);
                this.futures.add(executorService.submit(sender));

                this.futures.add(executorService.submit(new Runnable() {
                    @Override
                    public void run() {

                        final DatagramPacket request = new DatagramPacket(new byte[2048], 2048);
                        latch.countDown();

                        while (agent.running.get()) {

                            try {
                                socket.receive(request);
                                final SocketAddress sa = request.getSocketAddress();

                                if (null != sa) {

                                    String req = new String(request.getData(), 0, request.getLength());

                                    if (req.startsWith(CLIENT)) {

                                        final int ix = req.indexOf(BADURI);
                                        String badUri = null;

                                        if (ix > 0) {
                                            //The client is notifying of a bad uri
                                            badUri = req.substring(ix).replace(BADURI, "");
                                            req = req.substring(0, ix).replace(CLIENT, "");
                                        } else {
                                            req = (req.replace(CLIENT, ""));
                                        }

                                        //Is this a group or global pulse request
                                        if (mpg.equals(req) || "*".equals(req)) {

                                            //Is there a bad url and is it this agent broadcasting the bad URI?
                                            if (null != badUri) {
                                                if (getHosts(agent.ignore).contains(badUri)) {
                                                    final ReentrantLock l = agent.lock;
                                                    l.lock();

                                                    try {
                                                        //Remove it and rebuild our broadcast packet
                                                        if (agent.ignore.add(badUri)) {
                                                            agent.buildPacket();
                                                            LOG.warning("This server has removed the unreachable host '" + badUri + "' from discovery, you should consider adding" +
                                                                    " this to the 'ignore' property in the multipulse.properties file");
                                                        }
                                                    } finally {
                                                        l.unlock();
                                                    }
                                                }

                                                agent.fireEvent(URI.create("OpenEJB" + BADURI + badUri), false);

                                            } else {

                                                //Normal client multicast pulse request
                                                final String client = ((InetSocketAddress) sa).getAddress().getHostAddress();

                                                if (isLoopBackOnly && !MulticastPulseAgent.isLocalAddress(client, false)) {
                                                    //We only have local services, so make sure the request is from a local source else ignore it
                                                    if (LOG.isDebugEnabled()) {
                                                        LOG.debug(String.format("Ignoring remote client %1$s pulse request for group: %2$s - No remote services available",
                                                                client,
                                                                req));
                                                    }
                                                } else {

                                                    //We have received a valid pulse request
                                                    if (LOG.isDebugEnabled()) {
                                                        LOG.debug(String.format("Answering client '%1$s' pulse request for group: '%2$s' on '%3$s'", client, req, socketKey));
                                                    }

                                                    //Renew response pulse
                                                    sender.pulseResponse();
                                                }
                                            }
                                        }
                                    }
                                }

                            } catch (final Exception e) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("MulticastPulseAgent request error: " + e.getMessage(), e);
                                }
                            }
                        }

                        try {
                            socket.close();
                        } catch (final Throwable e) {
                            //Ignore
                        }
                    }
                }));
            }

            try {
                //Give threads a reasonable amount of time to start
                latch.await(5, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
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
                    } catch (final Throwable e) {
                        //Ignore
                    }
                }

                //Wait for threads to complete
                for (final Future future : this.futures) {
                    try {
                        future.get();
                    } catch (final Throwable e) {
                        //Ignore
                    }
                }
            } finally {
                this.futures.clear();
            }

            if (null != this.sockets) {
                try {
                    for (final MulticastSocket s : this.sockets) {
                        try {
                            s.close();
                        } catch (final Throwable e) {
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
     * Lists current broadcast hosts as a comma separated list.
     * Used principally for testing.
     *
     * @return String
     */
    public String getHosts() {
        return getHosts(this.ignore);
    }

    /**
     * Remove a host from the ignore list.
     * Used principally for testing.
     *
     * @param host String
     * @return True if removed, else false
     */
    public boolean removeFromIgnore(final String host) {
        return this.ignore.remove(host);
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
        } catch (final UnknownHostException e) {
            throw new ServiceException(multicastAddress + " is not a valid address", e);
        }

        if (null == ia || !ia.isMulticastAddress()) {
            throw new ServiceException(multicastAddress + " is not a valid multicast address");
        }

        return getSockets(ia, port);
    }

    private static MulticastSocket[] getSockets(final InetAddress ia, final int port) throws Exception {

        final ArrayList<MulticastSocket> list = new ArrayList<>();

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

                LOG.debug(String.format("Created MulticastSocket for '%1$s:%2$s' on network adapter: %3$s", ia.getHostName(), port, ni));

            } catch (final Throwable e) {

                if (null != ms) {
                    try {
                        ms.close();
                    } catch (final Throwable t) {
                        //Ignore
                    }
                }
            }
        }

        return list.toArray(new MulticastSocket[list.size()]);
    }

    private static NetworkInterface[] getNetworkInterfaces() {

        final HashSet<NetworkInterface> list = new HashSet<>();

        try {
            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                final NetworkInterface next = interfaces.nextElement();

                if (next.supportsMulticast() && next.isUp()) {
                    list.add(next);
                }
            }
        } catch (final SocketException e) {
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
        } catch (final UnknownHostException e) {
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
        } catch (final UnknownHostException e) {
            return false;
        }

        // Check if the address is a valid special local or loop back
        if ((wildcardIsLocal && addr.isAnyLocalAddress()) || addr.isLoopbackAddress()) {
            return true;
        }

        // Check if the address is defined on any interface
        try {
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (final SocketException e) {
            return false;
        }
    }

    private static String getHosts(final Set<String> ignore) {

        final Set<String> hosts = new TreeSet<String>(new Comparator<String>() {
            private boolean isIPv4LiteralAddress(final InetAddress val) {
                return Inet4Address.class.isInstance(val);
            }

            private boolean isIPv6LiteralAddress(final InetAddress val) {
                return Inet6Address.class.isInstance(val);
            }

            @Override
            public int compare(final String h1, final String h2) {

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
                } catch (final Throwable e) {
                    //Ignore
                }

                return h1.compareTo(h2);
            }
        });

        try {
            final InetAddress localhost = InetAddress.getLocalHost();
            hosts.add(localhost.getHostAddress());
            //Multi-homed
            final InetAddress[] all = InetAddress.getAllByName(localhost.getHostName());
            for (final InetAddress ip : all) {

                if (ip.isLinkLocalAddress() || ip.isMulticastAddress()) {
                    continue;
                }

                final String ha = ip.getHostAddress();
                if (!ha.replace("[", "").startsWith("2001:0:")) { //Filter Teredo
                    hosts.add(ha);
                    hosts.add(ip.getHostName());
                }
            }
        } catch (final UnknownHostException e) {
            LOG.warning("Failed to list machine hosts", e);
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

    private static class Sender implements Runnable {

        private final AtomicInteger counter = new AtomicInteger(0);
        private final MulticastPulseAgent agent;
        private final String socketKey;
        private final MulticastSocket socket;

        private Sender(final MulticastPulseAgent agent, final String socketKey, final MulticastSocket socket) {
            this.agent = agent;
            this.socketKey = socketKey;
            this.socket = socket;
        }

        @Override
        public void run() {
            while (this.agent.running.get()) {

                synchronized (this.counter) {
                    try {
                        //Wait indefinitely until we are interrupted or notified
                        this.counter.wait();
                    } catch (final InterruptedException e) {
                        if (!this.agent.running.get()) {
                            break;
                        }
                    }
                }

                //Pulse a response every 10ms until our counter is 0 (at least 1 second)
                while (this.counter.decrementAndGet() > 0) {

                    try {
                        this.socket.send(this.agent.getResponsePacket());
                    } catch (final Exception e) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("MulticastPulseAgent client error: " + e.getMessage(), e);
                        }
                    }

                    try {
                        Thread.sleep(10);
                    } catch (final InterruptedException e) {
                        break;
                    }
                }
            }
        }

        /**
         * Renew the counter and notify to pulse response
         */
        private void pulseResponse() {

            synchronized (this.counter) {

                this.counter.set(100);
                this.counter.notifyAll();
            }
        }

        @Override
        public String toString() {
            return this.socketKey;
        }
    }
}