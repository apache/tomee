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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @version $Rev$ $Date$
 */
public class MulticastTool {

    private static final int BUFF_SIZE = 8192;

    public static void main(String[] array) throws Exception {

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

        String send = null;

        long rate = 1000;

        String host = "239.255.3.2";
        int port = 6142;
        Integer ttl = null;
        Boolean loopbackmode = null;
        Integer socketTimeout = null;

        Iterator<String> args = Arrays.asList(array).iterator();
        while (args.hasNext()) {
            String arg = args.next();

            if (arg.equals("--host") || arg.equals("-h")) {
                host = args.next();
            } else if (arg.equals("--port") || arg.equals("-p")) {
                port = Integer.parseInt(args.next());
            } else if (arg.equals("--rate") || arg.equals("-r")) {
                rate = new Long(args.next());
            } else if (arg.equals("--ttl")) {
                ttl = new Integer(args.next());
            } else if (arg.equals("--send") || arg.equals("-s")) {
                send = args.next();
            } else if (arg.equals("--timeout") || arg.equals("-t")) {
                socketTimeout = new Integer(args.next());
            } else if (arg.equals("--loopback") || arg.equals("-l")) {
                loopbackmode = new Boolean(args.next());
            } else {
                throw new IllegalArgumentException(arg);
            }
        }

        InetAddress inetAddress = InetAddress.getByName(host);

        InetSocketAddress address = new InetSocketAddress(inetAddress, port);

        MulticastSocket multicast = new MulticastSocket(port);
        multicast.joinGroup(inetAddress);

        if (ttl != null) {
            multicast.setTimeToLive(ttl);
        }

        if (socketTimeout != null) {
            multicast.setSoTimeout(socketTimeout);
        }

        if (loopbackmode != null) {
            multicast.setLoopbackMode(loopbackmode);
        }

        System.out.print("Connecting to multicast group: ");
        System.out.print(host);
        System.out.print(":");
        System.out.println(multicast.getLocalPort());

        print("LoopbackMode", multicast.getLoopbackMode());
        print("TimeToLive", multicast.getTimeToLive());
        print("SoTimeout", multicast.getSoTimeout());

        System.out.println("-------------------------------");

        if (send != null) {
            Timer timer = new Timer("Multicast Send", true);
            timer.scheduleAtFixedRate(new Send(address, multicast, send), 0, rate);
        }

        byte[] buf = new byte[BUFF_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, 0, buf.length);

        while (true) {
            try {
                multicast.receive(packet);
                if (packet.getLength() > 0) {
                    InetAddress a = packet.getAddress();
                    System.out.print(format.format(new Date()));
                    System.out.print(" - ");
                    System.out.print(a.getHostAddress());
                    System.out.print(" - ");
                    String str = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    System.out.println(str);
                }
            } catch (SocketTimeoutException e) {
            }
        }
    }

    private static void print(String name, Object value) {
        System.out.print(name);
        System.out.print(":");
        System.out.println(value);
    }

    static class Send extends TimerTask {
        private final MulticastSocket multicast;
        private final String text;
        private final SocketAddress address;

        public Send(SocketAddress address, MulticastSocket multicast, String text) {
            this.address = address;
            this.multicast = multicast;
            this.text = text;
        }

        public void run() {
            try {
                byte[] data = text.getBytes();
                DatagramPacket packet = new DatagramPacket(data, 0, data.length, address);
                multicast.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
