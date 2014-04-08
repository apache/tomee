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
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @version $Rev$ $Date$
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class MulticastTool {

    private static final CommandParser cmd = new CommandParser() {
        @Override
        protected void init() {
            category("Options");
            opt('h', "host").type(String.class).value("239.255.3.2")
                .description("Address of the multicast channel");

            opt('p', "port").type(int.class).value(6142)
                .description("Port of the multicast channel");

            opt("date-format").type(String.class).value("HH:mm:ss")
                .description("Date format to use for log lines");

            category("Sending");

            opt('s', "send").type(String.class)
                .description("Optional message to broadcast to the channel");
            opt('r', "rate").type(long.class).value(1000)
                .description("Resend every N milliseconds. Zero sends just once");

            category("Advanced");

            opt("broadcast").type(boolean.class).description("java.net.MulticastSocket#setBroadcast");
            opt("loopback-mode").type(boolean.class).description("java.net.MulticastSocket#setLoopbackMode");
            opt("receive-buffer-size").type(int.class).description("java.net.MulticastSocket#setReceiveBufferSize");
            opt("reuse-address").type(boolean.class).description("java.net.MulticastSocket#setReuseAddress");
            opt("send-buffer-size").type(int.class).description("java.net.MulticastSocket#setSendBufferSize");
            opt("so-timeout").type(int.class).description("java.net.MulticastSocket#setSoTimeout");
            opt("time-to-live").type(int.class).description("java.net.MulticastSocket#setTimeToLive");
            opt("traffic-class").type(int.class).description("java.net.MulticastSocket#setTrafficClass");
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

    private static final int BUFF_SIZE = 8192;

    public static void main(final String[] array) throws Exception {

        final CommandParser.Arguments arguments;
        try {
            arguments = cmd.parse(array);
        } catch (CommandParser.HelpException e) {
            System.exit(0);
            throw new Exception(); // never reached, but keeps compiler happy
        } catch (CommandParser.InvalidOptionsException e) {
            System.exit(1);
            throw new Exception(); // never reached, but keeps compiler happy
        }

        final Options options = arguments.options();

        final SimpleDateFormat format = new SimpleDateFormat(options.get("date-format", "HH:mm:ss"));

        final String host = options.get("host", "239.255.3.2");
        final int port = options.get("port", 6142);

        final InetAddress inetAddress = InetAddress.getByName(host);

        final InetSocketAddress address = new InetSocketAddress(inetAddress, port);

        final MulticastSocket multicast = new MulticastSocket(port);
        multicast.joinGroup(inetAddress);

        if (options.has("reuse-address")) {
            multicast.setReuseAddress(options.get("reuse-address", false));
        }
        if (options.has("broadcast")) {
            multicast.setBroadcast(options.get("broadcast", false));
        }
        if (options.has("loopback-mode")) {
            multicast.setLoopbackMode(options.get("loopback-mode", false));
        }
        if (options.has("send-buffer-size")) {
            multicast.setSendBufferSize(options.get("send-buffer-size", 0));
        }
        if (options.has("receive-buffer-size")) {
            multicast.setReceiveBufferSize(options.get("receive-buffer-size", 0));
        }
        if (options.has("so-timeout")) {
            multicast.setSoTimeout(options.get("so-timeout", 0));
        }
        if (options.has("time-to-live")) {
            multicast.setTimeToLive(options.get("time-to-live", 0));
        }
        if (options.has("traffic-class")) {
            multicast.setTrafficClass(options.get("traffic-class", 0));
        }

        System.out.println("Connected");
        print("host", host);
        print("port", port);
        System.out.println();

        System.out.println("Socket");
        print("broadcast", multicast.getBroadcast());
        print("loopback-mode", multicast.getLoopbackMode());
        print("receive-buffer-size", multicast.getReceiveBufferSize());
        print("reuse-address", multicast.getReuseAddress());
        print("send-buffer-size", multicast.getSendBufferSize());
        print("so-timeout", multicast.getSoTimeout());
        print("time-to-live", multicast.getTimeToLive());
        print("traffic-class", multicast.getTrafficClass());
        System.out.println();

        if (options.has("send")) {
            final String send = options.get("send", "");
            final long rate = options.get("rate", 1000);

            System.out.println("Sending");
            print("send", send);
            print("rate", rate);
            System.out.println();

            final Send message = new Send(address, multicast, send);

            if (rate > 0) {
                final Timer timer = new Timer("Multicast Send", true);
                timer.scheduleAtFixedRate(message, 0, rate);
            } else {
                message.run();
                message.close();
            }
        }

        System.out.println("Listening....");

        final byte[] buf = new byte[BUFF_SIZE];
        final DatagramPacket packet = new DatagramPacket(buf, 0, buf.length);

        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                multicast.receive(packet);
                if (packet.getLength() > 0) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(format.format(new Date()));
                    sb.append(" - ");
                    sb.append(packet.getAddress().getHostAddress());
                    sb.append(" - ");
                    final String str = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    sb.append(str);
                    System.out.println(sb.toString());
                }
            } catch (SocketTimeoutException e) {
                final StringBuilder sb = new StringBuilder();
                sb.append(format.format(new Date()));
                sb.append(" - ");
                sb.append("ERROR");
                sb.append(" - ");
                sb.append(e.getMessage());
                System.out.println(sb.toString());
            }
        }
    }

    private static void print(final String name, final Object value) {
        System.out.printf(" %-20s: %s", name, value);
        System.out.println();
    }

    static class Send extends TimerTask {

        private final MulticastSocket multicast;
        private final String text;
        private final SocketAddress address;

        public Send(final SocketAddress address, final MulticastSocket multicast, final String text) {
            this.address = address;
            this.multicast = multicast;
            this.text = text;
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                close();
            } finally {
                super.finalize();
            }
        }

        private void close() {
            try {
                multicast.close();
            } catch (Throwable e) {
                //Ignore
            }
        }

        @Override
        public void run() {
            try {
                final byte[] data = text.getBytes();
                final DatagramPacket packet = new DatagramPacket(data, 0, data.length, address);
                multicast.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean cancel() {
            close();
            return super.cancel();
        }
    }

}
