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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.DatagramPacket;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Copyright (c) ORPRO Vision GmbH.
 * Author: Andy Gumbrecht
 * Date: 11.06.12
 */
public class MulticastPulseAgentTest {

    private static final Set<String> schemes = new HashSet<String>(Arrays.asList("ejbd", "ejbds", "http"));
    private static ExecutorService executor;
    private static final Charset utf8 = Charset.forName("UTF-8");
    private static final String forGroup = "*";
    private static final String host = "239.255.3.2";
    private static final int port = 6142;
    private static MulticastPulseAgent agent;

    @BeforeClass
    public static void beforeClass() throws Exception {

        executor = Executors.newFixedThreadPool(10);

        final Properties p = new Properties();
        p.setProperty("bind", host);
        p.setProperty("port", "" + 6142);

        agent = new MulticastPulseAgent();
        agent.init(p);
        agent.setDiscoveryListener(new MyDiscoveryListener("test"));
        agent.registerService(new URI("ejb:ejbd://[::]:4201"));
        agent.registerService(new URI("ejb:ejbd://0.0.0.0:4201"));
        agent.registerService(new URI("ejb:http://127.0.0.1:4201"));
        agent.registerService(new URI("ejb:https://0.0.0.1:4201"));
        agent.start();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        agent.stop();
        executor.shutdownNow();
    }

    @Test
    public void test() throws Exception {

        final InetAddress ia;

        try {
            ia = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new Exception(host + " is not a valid address", e);
        }

        if (null == ia || !ia.isMulticastAddress()) {
            throw new Exception(host + " is not a valid multicast address");
        }

        final byte[] bytes = (MulticastPulseAgent.CLIENT + forGroup).getBytes(utf8);
        final DatagramPacket request = new DatagramPacket(bytes, bytes.length, new InetSocketAddress(ia, port));

        final AtomicBoolean running = new AtomicBoolean(true);

        final MulticastSocket[] clientSockets = MulticastPulseAgent.getSockets(host, port);
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

        //Start threads that listen for multicast packets on our channel.
        //These need to start 'before' we pulse a request.
        final ArrayList<Future> futures = new ArrayList<Future>();

        for (final MulticastSocket socket : clientSockets) {

            futures.add(executor.submit(new Runnable() {
                @Override
                public void run() {

                    final DatagramPacket response = new DatagramPacket(new byte[2048], 2048);

                    while (running.get()) {
                        try {

                            socket.receive(response);

                            final SocketAddress sa = response.getSocketAddress();

                            if (null != sa && (sa instanceof InetSocketAddress)) {

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

                                    System.out.println(String.format("Client received Server pulse:\n\t%1$s\n\t%2$s\n\t%3$s\n", group, services, s));

                                    for (String svc : serviceList) {

                                        if (MulticastPulseAgent.EMPTY.equals(svc)) {
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
                                            if (MulticastPulseAgent.isLocalAddress(serviceHost, false)) {
                                                if (!MulticastPulseAgent.isLocalAddress(serverHost, false)) {
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
                                        } else {
                                            System.out.println("Reject service: " + serviceUri.toASCIIString());
                                        }
                                    }
                                }
                            }

                        } catch (Throwable e) {
                            //Ignore
                        }
                    }

                    System.out.println("Exit MulticastPulse client thread");
                }
            }));
        }

        //Pulse the server - It is thread safe to use same sockets as send/receive synchronization is only on the packet
        for (final MulticastSocket socket : clientSockets) {
            try {
                socket.send(request);
            } catch (Throwable e) {
                //Ignore
            }
        }

        //Kill the threads after timeout
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                running.set(false);

                for (Future future : futures) {
                    try {
                        future.cancel(true);
                    } catch (Throwable e) {
                        //Ignore
                    }
                }

                for (final MulticastSocket socket : clientSockets) {

                    try {
                        socket.leaveGroup(ia);
                    } catch (Throwable e) {
                        //Ignore
                    }
                    try {
                        socket.close();
                    } catch (Throwable e) {
                        //Ignore
                    }
                }
            }
        }, 1500);

        //Wait for threads to complete
        for (final Future future : futures) {
            try {
                future.get();
            } catch (Throwable e) {
                //Ignore
            }
        }

        futures.clear();

        for (final URI uri : set) {
            System.out.println(uri.toASCIIString());
        }

        org.junit.Assert.assertTrue(set.size() > 0);
    }

    private String ipFormat(final String h) throws UnknownHostException {

        final InetAddress ia = Inet6Address.getByName(h);
        if (ia instanceof Inet6Address) {
            return "[" + ia.getHostAddress() + "]";
        } else {
            return h;
        }
    }

    private static class MyDiscoveryListener implements DiscoveryListener {
        private final String id;

        public MyDiscoveryListener(String id) {
            id += "        ";
            id = id.substring(0, 8);
            this.id = id;
        }

        @Override
        public void serviceAdded(URI service) {
            System.out.println(id + "add " + service.toString());
        }

        @Override
        public void serviceRemoved(URI service) {
            System.out.println(id + "remove " + service.toString());
        }
    }
}
