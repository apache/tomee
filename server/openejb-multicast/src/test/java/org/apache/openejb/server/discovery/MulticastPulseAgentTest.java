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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Copyright (c) ORPRO Vision GmbH.
 * Author: Andy Gumbrecht
 * Date: 11.06.12
 */
public class MulticastPulseAgentTest {

    private static final Set<String> schemes = new HashSet<String>(Arrays.asList("ejbd", "ejbds", "http"));

    @Test
    public void test() throws Exception {

        final String group = "*";
        final String host = "239.255.3.2";
        final int port = 6142;

        final Properties p = new Properties();
        p.setProperty("bind", host);
        p.setProperty("port", "" + 6142);

        MulticastPulseAgent agent = new MulticastPulseAgent();
        agent.init(p);
        agent.setDiscoveryListener(new MyDiscoveryListener("test"));
        agent.registerService(new URI("ejb:ejbd://[::]:4201"));
        agent.registerService(new URI("ejb:ejbd://0.0.0.0:4201"));
        agent.registerService(new URI("ejb:http://127.0.0.1:4201"));
        agent.registerService(new URI("ejb:https://0.0.0.1:4201"));
        agent.start();

        final byte[] bytes = (MulticastPulseAgent.CLIENT + group).getBytes(Charset.forName("utf8"));
        final InetAddress ia = InetAddress.getByName(host);
        DatagramPacket dp = new DatagramPacket(bytes, bytes.length, new InetSocketAddress(ia, port));

        final AtomicBoolean running = new AtomicBoolean(true);
        final AtomicBoolean passed = new AtomicBoolean(false);
        final MulticastSocket client = MulticastPulseAgent.getSocket(host, port);
        final Timer timer = new Timer(true);
        final Set<URI> set = new TreeSet<URI>(new Comparator<URI>() {
            @Override
            public int compare(URI u1, URI u2) {

                u1 = URI.create(u1.getSchemeSpecificPart());
                u2 = URI.create(u2.getSchemeSpecificPart());

                int i = u1.getHost().compareTo(u2.getHost());

                if (i == 0) {
                    i = u1.compareTo(u2);
                }

                return i;
            }
        });

        //Start a thread that listens for multicast packets
        final Thread t = new Thread() {
            @Override
            public void run() {

                while (running.get()) {
                    try {

                        final DatagramPacket dgp = new DatagramPacket(new byte[512], 512);

                        client.receive(dgp);

                        final SocketAddress sa = dgp.getSocketAddress();

                        if (null != sa) {

                            String s = new String(dgp.getData()).trim();
                            if (s.startsWith(MulticastPulseAgent.SERVER)) {

                                s = (s.replace(MulticastPulseAgent.SERVER, ""));
                                final String group = s.substring(0, s.indexOf(':'));
                                s = s.substring(group.length() + 1);

                                final String services = s.substring(0, s.lastIndexOf('|'));
                                s = s.substring(services.length() + 1);

                                final String[] service = services.split("\\|");
                                final String[] hosts = s.split(",");

                                System.out.println(String.format("Client received Server pulse:\n\t%1$s\n\t%2$s\n\t%3$s\n", group, services, s));

                                for (String svc : service) {

                                    if (MulticastPulseAgent.EMPTY.equals(svc)) {
                                        continue;
                                    }

                                    URI test = null;
                                    try {
                                        test = URI.create(svc);
                                    } catch (Throwable e) {
                                        continue;
                                    }

                                    if (schemes.contains(test.getScheme())) {

                                        svc = (group + ":" + svc);

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

                                running.set(false);
                                timer.cancel();
                                passed.set(true);
                            }
                        }

                    } catch (Throwable e) {
                        //Ignore
                    }
                }

                System.out.println("Exit MulticastPulse client thread");
            }
        };
        t.setDaemon(true);
        t.start();

        if (running.get()) {
            //Kill the thread after timeout
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    running.set(false);
                    client.close();
                    t.interrupt();
                    System.out.println("Interrupted MultiPulse client");
                }
            }, 1000);
        }

        //Pulse the server
        final MulticastSocket ms = MulticastPulseAgent.getSocket(host, port);
        ms.send(dp);

        //Wait for thread to die
        t.join();

        agent.stop();

        for (URI uri : set) {
            System.out.println(uri.toASCIIString());
        }

        org.junit.Assert.assertTrue(passed.get());
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
