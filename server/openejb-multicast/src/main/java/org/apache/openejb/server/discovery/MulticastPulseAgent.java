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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
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
    private static final Executor executor = Executors.newSingleThreadExecutor();

    public static final String SERVER = "OpenEJB.MCP.Server:";
    public static final String CLIENT = "OpenEJB.MCP.Client:";
    public static final String EMPTY = "NoService";

    private final Set<URI> uriSet = new HashSet<URI>();
    private AtomicBoolean running = new AtomicBoolean(false);
    private Thread listenerThread = null;
    private MulticastSocket socket = null;
    private InetSocketAddress address = null;

    private String multicast = "239.255.3.2";
    private String group = "default";
    private int port = 6142;
    private DatagramPacket response = null;
    private DiscoveryListener listener = null;
    private boolean loopbackOnly = true;

    /**
     * This agent listens for a client pulse on a defined multicast channel.
     * On receipt of a valid pulse the agent responds with its own pulse for
     * a defined amount of time. A client can deliver a pulse as often as
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

        this.multicast = p.getProperty("bind", this.multicast);
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

        final String hosts = getHosts();
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

        final byte[] bytes = (sb.toString()).getBytes(Charset.forName("utf8"));
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

            this.socket = getSocket(this.multicast, this.port);
            final MulticastSocket ms = this.socket;

            this.listenerThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    final DatagramPacket request = new DatagramPacket(new byte[2048], 2048);

                    while (MulticastPulseAgent.this.running.get()) {

                        try {
                            ms.receive(request);
                            final SocketAddress sa = request.getSocketAddress();

                            if (null != sa) {

                                String s = new String(request.getData(), 0, request.getLength());

                                if (s.startsWith(CLIENT)) {

                                    s = (s.replace(CLIENT, ""));

                                    final String client = ((InetSocketAddress) sa).getAddress().getHostAddress();

                                    if (MulticastPulseAgent.this.group.equals(s) || "*".equals(s)) {

                                        if (MulticastPulseAgent.this.loopbackOnly) {
                                            //We only have local services, so make sure the request is from a local source else ignore it
                                            if (!MulticastPulseAgent.isLocalAddress(client, false)) {
                                                log.debug(String.format("Ignoring remote client %1$s pulse request for group: %2$s - No remote services available", client, s));
                                                continue;
                                            }
                                        }

                                        log.debug(String.format("Answering client %1$s pulse request for group: %2$s", client, s));
                                        ms.send(MulticastPulseAgent.this.response);
                                    } else {
                                        log.debug(String.format("Ignoring client %1$s pulse request for group: %2$s", client, s));
                                    }
                                }
                            }

                        } catch (Throwable e) {
                            //Ignore
                        }
                    }

                    try {
                        ms.close();
                    } catch (Throwable e) {
                        //Ignore
                    }
                }
            }, "MultiPulse Listener");

            this.listenerThread.setDaemon(true);
            this.listenerThread.start();
        }
    }

    @Override
    public void stop() throws ServiceException {
        if (this.running.getAndSet(false)) {

            if (null != this.listenerThread) {

                this.listenerThread.interrupt();

                try {
                    listenerThread.join(5000);
                } catch (InterruptedException e) {
                    //Ignore
                }
            }

            if (null != this.socket) {
                try {
                    this.socket.close();
                } catch (Throwable e) {
                    //Ignore
                } finally {
                    this.socket = null;
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

    public static MulticastSocket getSocket(final String multicastAddress, final int port) throws ServiceException {

        final InetAddress ia;

        try {
            ia = InetAddress.getByName(multicastAddress);
        } catch (UnknownHostException e) {
            throw new ServiceException(multicastAddress + " is not a valid address", e);
        }

        if (null == ia || !ia.isMulticastAddress()) {
            throw new ServiceException(multicastAddress + " is not a valid multicast address");
        }

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

            log.debug(String.format("Created MulticastSocket for '%1$s:%2$s' on network adapter: %3$s", multicastAddress, port, ni));

        } catch (Throwable e) {
            log.error("getSocket", e);

            if (null != ms) {
                try {
                    ms.close();
                } catch (Throwable t) {
                    //Ignore
                }
            }

            throw new ServiceException("Failed to create a multicast socket", e);
        }

        return ms;
    }

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

    private static String getHosts() {

        final Set<String> hosts = new HashSet<String>();

        try {
            final InetAddress localhost = InetAddress.getLocalHost();
            hosts.add(localhost.getHostAddress());
            //Multi-homed
            final InetAddress[] all = InetAddress.getAllByName(localhost.getCanonicalHostName());
            for (final InetAddress ip : all) {

                if (ip.isLinkLocalAddress() || ip.isMulticastAddress()) {
                    continue;
                }

                hosts.add(ip.getHostAddress());
            }
        } catch (UnknownHostException e) {
            log.warning("Failed to list machine hosts", e);
        }

        final StringBuilder sb = new StringBuilder();
        for (String host : hosts) {
            final String lc = host.toLowerCase();
            if (!"localhost".equals(lc) && !"::1".equals(lc) && !"127.0.0.1".equals(lc)) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(host);
            }
        }

        return sb.toString();
    }
}
