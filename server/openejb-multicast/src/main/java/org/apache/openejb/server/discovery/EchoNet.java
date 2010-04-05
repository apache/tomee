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
package org.apache.openejb.server.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @version $Rev$ $Date$
 */
public class EchoNet {

    public static void main(String[] args) throws Exception {
        listen(3333);

        Server a = new Server(4444).start();
        Server b = new Server(5555).start();
//        Server c = new Server(6666).start();
        a.connect(3333);
//        b.connect(3333);
        a.connect(b);
//        b.connect(a);
//        b.connect(c);
//        c.connect(a);
//        c.connect(b);


        new CountDownLatch(1).await();
    }

    private static void listen(final int port) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    runServer(port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void runServer(final int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            final Socket socket = serverSocket.accept();
//            new Thread(new Runnable() {
//                public void run() {
//                    try {
//                        read(port, socket);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
        }
    }

    private static void read(int port, Socket socket) throws IOException {
//        System.out.println(port + " - accept = " + socket.getRemoteSocketAddress());
        InputStream in = socket.getInputStream();
        int i = -1;
        byte[] buffer = new byte[1024];
        while ((i = in.read(buffer)) != -1) {
            buffer[i++] = (byte)'\n';
//            System.out.write(buffer, 0, i);
        }
    }

    public static class Server {
        private int port;
        private Selector selector;

        private final URI me;

        public Server(int port) {
            this.port = port;

            me = URI.create("conn://localhost:" + port);

            ServerSocketChannel serverChannel;
            try {
                serverChannel = ServerSocketChannel.open();
                ServerSocket ss = serverChannel.socket();
                InetSocketAddress address = new InetSocketAddress(port);
                ss.bind(address);
                serverChannel.configureBlocking(false);

                selector = Selector.open();

                serverChannel.register(selector, SelectionKey.OP_ACCEPT);

                println("Listening");

            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
        }

        public Server start() {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    _run();
                }


            });
            thread.setName("Server." + port);
            thread.start();
            return this;
        }


        private void printOps(SelectionKey key) {
            StringBuilder sb = new StringBuilder(key.hashCode() + "  opts: ");
            if (key.isAcceptable()) sb.append("a");
            if (key.isConnectable()) sb.append("c");
            if (key.isReadable()) sb.append("r");
            if (key.isWritable()) sb.append("w");
            println(sb.toString());
        }

        public class Session {
            private final LinkedList<ByteBuffer> buffers = new LinkedList<ByteBuffer>();

            private final ByteBuffer read = ByteBuffer.allocate(1024);
            private final ByteBuffer write = ByteBuffer.allocate(1024);

            public SelectionKey key;

            public InetSocketAddress address;

            private State state = State.GREETING;
            private final boolean server;
            private Iterator<URI> listing;
            private List<URI> listed = new ArrayList<URI>();
            public URI uri;

            public Session(boolean server) {
                this.server = server;
            }

            public ByteBuffer pop() {
                try {
                    return buffers.removeFirst();
                } finally {
                    if (buffers.size() == 0) key.interestOps(OP_READ);
                }
            }

            private void println(String s) {
                trace(s);
//                System.out.println(port + " - " + uri.getPort() + " - " + s);
            }

            public void state(int ops, State state) {
                this.state = state;
                key.interestOps(ops);

                trace("*");
            }

            private void trace(String str) {
                StringBuilder sb = new StringBuilder();
                sb.append(port);
                sb.append(" ");
                if ((key.interestOps() & OP_READ) == OP_READ) sb.append("<");
                if ((key.interestOps() & OP_WRITE) == OP_WRITE) sb.append(">");
//                if ((ops & OP_READ) == OP_READ) sb.append("(r)");
//                if ((ops & OP_WRITE) == OP_WRITE) sb.append("(w)");
                sb.append(" ");
                sb.append(uri.getPort());
                sb.append(" ");
                sb.append(this.state);
                sb.append(" ");
                sb.append(str);
                System.out.println(sb.toString());
            }

        }

        private static enum State {
            GREETING, LISTING, HEARTBEAT
        }

        List<URI> seen = new ArrayList<URI>();

        private void _run() {
            while (true) {

                try {
//                    if (me.getPort() != 5555) Thread.sleep(1000);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
                try {
                    selector.select();
//                    selector.select(2000);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    break;
                }

                Set keys = selector.selectedKeys();

//                println("selection " + keys.size() + "  " + keys);

                Iterator iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();

//                    printOps(key);


                    try {
                        if (key.isAcceptable()) {

                            // we are a server

                            // when you are a server, we must first listen for the
                            // address of the client before sending data.

                            // once they send us their address, we will send our
                            // full list of known addresses, followed by our own
                            // address to signal that we are done.

                            // Afterward we will only pulls our heartbeat

                            ServerSocketChannel server = (ServerSocketChannel) key.channel();
                            SocketChannel client = server.accept();
                            InetSocketAddress address = (InetSocketAddress) client.socket().getRemoteSocketAddress();

                            println("accept " + address.getPort());
                            client.configureBlocking(false);

                            register(client, address, OP_READ, true);
                        }

                        if (key.isConnectable()) {

                            // we are a client

                            SocketChannel channel = (SocketChannel) key.channel();
                            channel.finishConnect();

                            // when you are a client, first say high to everyone
                            // before accepting data

                            // once a server reads our address, it will send it's
                            // full list of known addresses, followed by it's own
                            // address to signal that it is done.

                            // we will initiate connections to everyone in the list
                            // who we have not yet seen.

                            // Afterward the server will only pulls its heartbeat

                            key.interestOps(OP_WRITE);

                        }

                        if (key.isReadable()) {

                            SocketChannel client = (SocketChannel) key.channel();
                            Session session = (Session) key.attachment();

                            ByteBuffer output = session.read;

                            output.clear();

                            int i = client.read(output);

                            String message = new String(output.array(), output.arrayOffset(), output.position());

                            if (message.length() == 0) {
                                session.println(" --- ");
                                return;
                            } else {
//                                session.println(message);
                            }

                            URI uri = URI.create(message);

                            switch (session.state) {
                                case GREETING: { // read
                                    session.uri = uri;

                                    session.println("welcome");

                                    session.state(OP_WRITE, State.LISTING);

                                    ArrayList<URI> list = new ArrayList<URI>(seen);

                                    // When they read themselves on the list
                                    // they'll know it's time to list their URIs

                                    list.remove(me); // yank
                                    list.remove(uri); // yank
                                    list.add(uri); // add to the end

                                    session.listing = list.iterator();

                                }; break;

                                case LISTING: { // read

                                    session.listed.add(uri);

                                    session.println(message);

                                    // they listed me, means they want my list
                                    if (uri.equals(me)) {

                                        // switch to write
                                        session.state(OP_WRITE, State.LISTING);

                                        ArrayList<URI> list = new ArrayList<URI>(seen);

                                        for (URI reported : session.listed) {
                                            list.remove(reported);
                                        }
                                        
                                        // When they read us on the list
                                        // they'll know it's time to switch to heartbeat

                                        list.remove(session.uri);
                                        list.remove(me); // yank if in the middle
                                        list.add(me); // add to the end

                                        session.listing = list.iterator();
                                    } else if (uri.equals(session.uri)) {

                                        session.state(OP_WRITE | OP_READ, State.HEARTBEAT);

                                    } else if (!seen.contains(uri)) {
                                        try {
                                            connect(uri);
                                        } catch (Exception e) {
                                            println("connect failed " + uri + " - " + e.getMessage());
                                            e.printStackTrace();
                                        }
                                    } else {
                                        session.println("ambiguous");

                                        session.state(OP_WRITE | OP_READ, State.HEARTBEAT);
                                    }
                                }; break;

                                case HEARTBEAT: { // read

                                    session.println("pong");

                                }; break;
                            }

                        }

                        if (key.isWritable()) {

                            SocketChannel client = (SocketChannel) key.channel();
                            Session session = (Session) key.attachment();

                            switch (session.state) {
                                case GREETING: { // write

                                    session.println("hello");

                                    ByteBuffer output = session.write;

                                    output.clear();

                                    output.put(me.toString().getBytes());

                                    output.flip();

                                    client.write(output);

                                    session.state(OP_READ, State.LISTING);

                                }; break;

                                case LISTING: { // write

                                    URI uri = session.listing.next();

                                    ByteBuffer output = session.write;

                                    output.clear();

                                    session.println(uri.toString());

                                    output.put(uri.toString().getBytes());

                                    output.flip();

                                    client.write(output);

                                    if (!session.listing.hasNext()) {

                                        // We've just signaled them to
                                        // go next and list their URIs
                                        if (uri.equals(session.uri)) {

                                            session.state(OP_READ, State.LISTING);

                                        } else if (uri.equals(me)) {
                                            // we've clearly signaled them that
                                            // we are done and do not expect
                                            // to read any URLs

                                            session.state(OP_WRITE |OP_READ, State.HEARTBEAT);

                                        } else {

                                            session.println("ambiguous state, switching to heartbeat");

                                            session.state(OP_WRITE |OP_READ, State.HEARTBEAT);

                                        }
                                    }
                                }; break;

                                case HEARTBEAT: { // write

                                    ByteBuffer output = session.write;

                                    output.clear();

                                    output.put(me.toString().getBytes());

                                    output.flip();

                                    client.write(output);

                                    session.println("ping");

                                }; break;
                            }
                        }

                    } catch (IOException ex) {
                        key.cancel();
                        try {
                            key.channel().close();
                        } catch (IOException cex) {
                        }
                    }

                }

            }
        }

        private void push(ByteBuffer output, Session session) {
            for (Session fellow : clients.values()) {
                // Don't write to the originator
                if (fellow == session) continue;
                fellow.buffers.addLast(output);
                fellow.key.interestOps(OP_READ | OP_WRITE);
            }
        }


        public void connect(Server s) throws Exception {
            connect(s.port);
        }

        public void connect(int port) throws Exception {
            connect(URI.create("conn://localhost:" + port));
        }

        public void connect(URI uri) throws Exception {
            if (me.equals(uri)) return;
            
            int port = uri.getPort();
            String host = uri.getHost();

            try {
                println("open " + uri);

                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);

                InetSocketAddress address = new InetSocketAddress(host, port);

                socketChannel.connect(address);

                Session session = register(socketChannel, address, SelectionKey.OP_CONNECT, false);
                session.uri = uri;

                // seen - needs to get maintained as "connected"
                // TODO remove from seen
                seen.add(uri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            println("++ " + port);
        }

        private final Map<SocketAddress, Session> clients = new ConcurrentHashMap<SocketAddress, Session>();
//        private final List<Session> clients = new ArrayList<Session>();

        private Session register(SocketChannel client, InetSocketAddress address, int ops, boolean server) throws IOException {
//            println("Registering " + address);
            Session session = new Session(server);
            session.key = client.register(selector, ops, session);
            session.address = address;
            session.uri = URI.create("conn://" + address.getHostName() + ":" + address.getPort());

//            clients.add(session);
//            Session duplicate = clients.put(address, session);
//            if (duplicate != null) {
//                println("duplicate = " + duplicate + "  " + address);
//                duplicate.key.channel().close();
//                println("closed duplicate " + address);
//            }
//
//            for (SocketAddress node : clients.keySet()) {
//                println("node " + node);
//            }

            return session;
        }

        private void println(String s) {
            System.out.println(port + " - " + s);
        }
    }
}

