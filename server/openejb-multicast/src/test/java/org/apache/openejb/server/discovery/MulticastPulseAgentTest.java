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
package org.apache.openejb.server.discovery;

import org.apache.openejb.server.DiscoveryListener;
import org.apache.openejb.util.NetworkUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;
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
import java.util.logging.Logger;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class MulticastPulseAgentTest {

    private static final Set<String> schemes = new HashSet<String>(Arrays.asList("ejbd", "ejbds", "http"));
    private static ExecutorService executor;
    private static final Charset utf8 = Charset.forName("UTF-8");
    private static final String forGroup = "*";
    private static final String host = "239.255.3.2";
    private static final int port = NetworkUtil.getNextAvailablePort();
    private static MulticastPulseAgent agent;

    @BeforeClass
    public static void beforeClass() throws Exception {

        executor = Executors.newFixedThreadPool(10);

        final Properties p = new Properties();
        p.setProperty("bind", host);
        p.setProperty("port", "" + port);

        agent = new MulticastPulseAgent();
        agent.init(p);
        agent.setDiscoveryListener(new MyDiscoveryListener("MulticastPulseAgentTest"));
        agent.registerService(new URI("ejb:ejbd://[::]:4201"));
        agent.registerService(new URI("ejb:ejbd://0.0.0.0:4201"));
        agent.registerService(new URI("ejb:http://127.0.0.1:4201"));
        agent.registerService(new URI("ejb:https://0.0.0.1:4201"));
        agent.start();

        System.out.println();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        agent.stop();
        executor.shutdownNow();
    }

    /**
     * Most of this code is identical to org.apache.openejb.client.MulticastPulseClient#discoverURIs
     * <p/>
     * The MulticastPulseClient class is not shared or available here so the test has to emulate it.
     *
     * @throws Exception On error
     */
    @Test
    public void test() throws Exception {
        if ("true".equals(System.getProperty("skipMulticastTests"))) {
            Logger.getLogger(this.getClass().getName()).warning("Skipping MulticastTest " + this.getClass().getName());
            return;
        }

        final InetAddress ia;

        try {
            ia = InetAddress.getByName(host);
        } catch (final UnknownHostException e) {
            throw new Exception(host + " is not a valid address", e);
        }

        if (null == ia || !ia.isMulticastAddress()) {
            throw new Exception(host + " is not a valid multicast address");
        }

        //Returns at least one socket per valid network interface
        final MulticastSocket[] clientSockets = MulticastPulseAgent.getSockets(host, port);

        //No point going on if we don't have sockets...
        if (clientSockets.length < 1) {
            System.out.println("Cannnot perform multipulse test without a valid interface");
            return;
        }

        final byte[] bytes = (MulticastPulseAgent.CLIENT + forGroup).getBytes(utf8);
        final DatagramPacket request = new DatagramPacket(bytes, bytes.length, new InetSocketAddress(ia, port));
        final AtomicBoolean running = new AtomicBoolean(true);
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

            private boolean isIPv4LiteralAddress(final InetAddress val) {
                return Inet4Address.class.isInstance(val);
            }

            private boolean isIPv6LiteralAddress(final InetAddress val) {
                return Inet6Address.class.isInstance(val);
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
                } catch (final Throwable e) {
                    //Ignore
                }

                return h1.compareTo(h2);
            }
        });

        final ReentrantLock setLock = new ReentrantLock();

        //Start threads that listen for multicast packets on our channel.
        //These need to start 'before' we pulse a request.
        final ArrayList<Future> futures = new ArrayList<Future>();
        final CountDownLatch latch = new CountDownLatch(clientSockets.length);

        for (final MulticastSocket socket : clientSockets) {

            futures.add(executor.submit(new Runnable() {
                @Override
                public void run() {

                    String name = "Unknown interface";
                    try {
                        name = socket.getNetworkInterface().getDisplayName();
                    } catch (final Throwable e) {
                        //Ignore
                    }
                    System.out.println("Entered MulticastPulse client thread on: " + name);

                    final DatagramPacket response = new DatagramPacket(new byte[2048], 2048);

                    latch.countDown();

                    while (running.get()) {
                        try {

                            socket.receive(response);

                            final SocketAddress sa = response.getSocketAddress();

                            if ((sa instanceof InetSocketAddress)) {

                                int len = response.getLength();
                                if (len > 2048) {
                                    len = 2048;
                                }

                                String s = new String(response.getData(), 0, len);

                                if (s.startsWith(MulticastPulseAgent.SERVER)) {

                                    s = (s.replace(MulticastPulseAgent.SERVER, ""));
                                    final String group = s.substring(0, s.indexOf(':'));
                                    s = s.substring(group.length() + 1);

                                    if (!"*".equals(forGroup) && !forGroup.equals(group)) {
                                        continue;
                                    }

                                    final String services = s.substring(0, s.lastIndexOf('|'));
                                    s = s.substring(services.length() + 1);

                                    final String[] serviceList = services.split("\\|");
                                    final String[] hosts = s.split(",");

                                    System.out.println(String.format("\n" + name + " received Server pulse:\n\tGroup: %1$s\n\tServices: %2$s\n\tServer: %3$s\n",
                                            group,
                                            services,
                                            s));

                                    for (final String svc : serviceList) {

                                        if (MulticastPulseAgent.EMPTY.equals(svc)) {
                                            continue;
                                        }

                                        final URI serviceUri;
                                        try {
                                            serviceUri = URI.create(svc);
                                        } catch (final Throwable e) {
                                            continue;
                                        }

                                        if (schemes.contains(serviceUri.getScheme())) {

                                            //Just because multicast was received on this host is does not mean the service is on the same
                                            //We can however use this to identify an individual machine and group
                                            final String serverHost = ((InetSocketAddress) response.getSocketAddress()).getAddress().getHostAddress();

                                            final String serviceHost = serviceUri.getHost();
                                            if (MulticastPulseAgent.isLocalAddress(serviceHost, false)) {
                                                if (!MulticastPulseAgent.isLocalAddress(serverHost, false)) {
                                                    //A local service is only available to a local client
                                                    continue;
                                                }
                                            }

                                            final String fullsvc = ("mp-" + serverHost + ":" + group + ":" + svc);

                                            setLock.lock();

                                            try {
                                                if (fullsvc.contains("0.0.0.0")) {
                                                    for (final String h : hosts) {
                                                        if (!h.replace("[", "").startsWith("2001:0:")) { //Filter Teredo
                                                            set.add(URI.create(fullsvc.replace("0.0.0.0", ipFormat(h))));
                                                        }
                                                    }
                                                } else if (fullsvc.contains("[::]")) {
                                                    for (final String h : hosts) {
                                                        if (!h.replace("[", "").startsWith("2001:0:")) { //Filter Teredo
                                                            set.add(URI.create(fullsvc.replace("[::]", ipFormat(h))));
                                                        }
                                                    }
                                                } else {
                                                    //Just add as is
                                                    set.add(URI.create(fullsvc));
                                                }
                                            } catch (final Throwable e) {
                                                //Ignore
                                            } finally {
                                                setLock.unlock();
                                            }
                                        } else {
                                            System.out.println("Reject service: " + serviceUri.toASCIIString() + " - Not looking for scheme: " + serviceUri.getScheme());
                                        }
                                    }
                                }
                            }

                        } catch (final Throwable e) {
                            //Ignore
                        }
                    }

                    System.out.println("Exit MulticastPulse client thread on: " + name);
                    System.out.flush();
                }
            }));
        }

        //Allow slow thread starts
        System.out.println("Wait for threads to start");
        int timeout = 5000;
        try {

            //Give threads a generous amount of time to start
            if (latch.await(15, TimeUnit.SECONDS)) {
                System.out.println("Threads have started");

                //Pulse the server - It is thread safe to use same sockets as send/receive synchronization is only on the packet
                for (final MulticastSocket socket : clientSockets) {
                    try {
                        socket.send(request);
                    } catch (final Throwable e) {
                        //Ignore
                    }
                }
            } else {
                timeout = 1;
                System.out.println("Giving up on threads");
            }

        } catch (final InterruptedException e) {
            timeout = 1;
        }

        //Kill the threads after timeout
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                running.set(false);

                for (final Future future : futures) {
                    try {
                        future.cancel(true);
                    } catch (final Throwable e) {
                        //Ignore
                    }
                }

                for (final MulticastSocket socket : clientSockets) {

                    try {
                        socket.leaveGroup(ia);
                    } catch (final Throwable e) {
                        //Ignore
                    }
                    try {
                        socket.close();
                    } catch (final Throwable e) {
                        //Ignore
                    }
                }
            }
        }, timeout);

        //Wait for threads to complete
        for (final Future future : futures) {
            try {
                future.get();
            } catch (final Throwable e) {
                //Ignore
            }
        }

        System.out.println();
        System.out.flush();

        final ArrayList<String> list = new ArrayList<String>();

        final TreeSet<URI> uris = new TreeSet<URI>(set);
        for (final URI uri : uris) {
            final String astr = uri.toASCIIString();
            System.out.println("MultiPulse discovered: " + astr);

            if (list.contains(astr)) {
                System.out.println("Duplicate uri: " + uri);
            }

            org.junit.Assert.assertTrue(!list.contains(astr));
            list.add(astr);
        }

        System.out.println("Multipulse complete");

        //If timeout == 1 assume either a cancel or the test took too long (Will not fail)
        org.junit.Assert.assertTrue(timeout == 1 || set.size() > 0);
    }

    @Test
    public void testBroadcastBadUri() throws Exception {
        if ("true".equals(System.getProperty("skipMulticastTests"))) {
            Logger.getLogger(this.getClass().getName()).warning("Skipping MulticastTest " + this.getClass().getName());
            return;
        }

        final DiscoveryListener original = agent.getDiscoveryListener();

        final CountDownLatch latch = new CountDownLatch(1);

        final DiscoveryListener listener = new DiscoveryListener() {
            @Override
            public void serviceAdded(final URI service) {
                latch.countDown();
                System.out.println("added = " + service);
            }

            @Override
            public void serviceRemoved(final URI service) {
                latch.countDown();
                System.out.println("removed = " + service);
            }
        };

        agent.setDiscoveryListener(listener);

        final String[] hosts = agent.getHosts().split(",");
        final String host = hosts[hosts.length - 1];

        boolean removed = agent.removeFromIgnore(host);
        org.junit.Assert.assertTrue("Host is already ignored", !removed);

        final Future<?> future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final InetAddress ia = getAddress(MulticastPulseAgentTest.host);

                    final byte[] bytes = (MulticastPulseAgent.CLIENT + forGroup + MulticastPulseAgent.BADURI + host).getBytes(Charset.forName("UTF-8"));
                    final DatagramPacket request = new DatagramPacket(bytes, bytes.length, new InetSocketAddress(ia, port));

                    final MulticastSocket[] multicastSockets = MulticastPulseAgent.getSockets(MulticastPulseAgentTest.host, port);

                    for (int i = 0; i < 5; i++) {
                        for (final MulticastSocket socket : multicastSockets) {

                            try {
                                socket.send(request);
                                Thread.sleep(100);
                            } catch (final Exception e) {
                                System.out.println("Failed to broadcast bad URI on: " + socket.getInterface().getHostAddress());
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (final Exception e) {
                    System.out.println("Failed to broadcast bad URI");
                    e.printStackTrace();
                }
            }
        });

        final boolean await = latch.await(20, TimeUnit.SECONDS);
        removed = agent.removeFromIgnore(host);

        agent.setDiscoveryListener(original);

        org.junit.Assert.assertTrue("Failed to remove host", removed);
        org.junit.Assert.assertTrue("Failed to unlatch", await);
    }

    private String ipFormat(final String h) throws UnknownHostException {

        final InetAddress ia = InetAddress.getByName(h);
        if (ia instanceof Inet6Address) {
            return "[" + ia.getHostAddress() + "]";
        } else {
            return h;
        }
    }

    private static InetAddress getAddress(final String host) throws Exception {
        final InetAddress ia;
        try {
            ia = InetAddress.getByName(host);
        } catch (final UnknownHostException e) {
            throw new Exception(host + " is not a valid address", e);
        }

        if (null == ia || !ia.isMulticastAddress()) {
            throw new Exception(host + " is not a valid multicast address");
        }
        return ia;
    }

    private static class MyDiscoveryListener implements DiscoveryListener {

        private final String id;

        public MyDiscoveryListener(final String id) {
            this.id = id;
        }

        @Override
        public void serviceAdded(final URI service) {
            System.out.println(id + ": add : " + service.toString());
        }

        @Override
        public void serviceRemoved(final URI service) {
            System.out.println(id + ": remove : " + service.toString());
        }
    }
}
