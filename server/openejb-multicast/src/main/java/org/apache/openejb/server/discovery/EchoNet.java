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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @version $Rev$ $Date$
 */
public class EchoNet {

    public static void main(String[] args) throws Exception {

        final int INITIAL_PORT = 3000;
        int maxServers = 3;

        if (args.length > 0)
            maxServers = Integer.parseInt(args[0]);

        if (maxServers < 1) {
            System.out.println("number of servers must be greater than zero");
            return;
        }

        Server lastServer = new Server(INITIAL_PORT).start();
        for (int i=1; i<maxServers; i++) {
            Server newServer = new Server(INITIAL_PORT+i).start();
            
            if (lastServer != null) 
                newServer.connect(lastServer);

            lastServer = newServer;
        }

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
            new Thread(new Runnable() {
                public void run() {
                    try {
                        read(port, socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private static void read(int port, Socket socket) throws IOException {
        System.out.println(port + " - accept = " + socket.getRemoteSocketAddress());
        InputStream in = socket.getInputStream();
        int i = -1;
        byte[] buffer = new byte[1024];
        while ((i = in.read(buffer)) != -1) {
            buffer[i++] = (byte) '\n';
            System.out.write(buffer, 0, i);
        }
    }

    public static class Server {
        private final int port;
        private final Selector selector;
        private final URI me;

        public Server(int port) throws IOException {
            this.port = port;

            me = URI.create("conn://localhost:" + port);

            ServerSocketChannel serverChannel = ServerSocketChannel.open();

            ServerSocket serverSocket = serverChannel.socket();
            InetSocketAddress address = new InetSocketAddress(port);
            serverSocket.bind(address);
            serverChannel.configureBlocking(false);

            selector = Selector.open();

            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            println("Listening");
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

        public class Session {

            private static final int EOF = 3;

            private final SocketChannel channel;
            private final ByteBuffer read = ByteBuffer.allocate(1024);
            private final SelectionKey key;
            private final InetSocketAddress address;
            private final List<URI> listed = new ArrayList<URI>();

            private ByteBuffer write;
            private State state = State.GREETING;
            private URI uri;

            public Session(SocketChannel channel, InetSocketAddress address, URI uri) throws ClosedChannelException {
                this.channel = channel;
                this.address = address;
                this.uri = uri != null ? uri : URI.create("conn://" + address.getHostName() + ":" + address.getPort());
                this.key = channel.register(selector, 0, this);
            }

            public Session ops(int ops) {
                key.interestOps(ops);
                return this;
            }

            private void println(String s) {
                trace(s);
//                System.out.println(port + " - " + uri.getPort() + " - " + s);
            }

            public void state(int ops, State state) {
                this.state = state;
                key.interestOps(ops);

//                trace("*");
            }

            public void setURI(URI uri) {
                seen(uri);
                this.uri = uri;
            }

            private void trace(String str) {
                StringBuilder sb = new StringBuilder();
                sb.append(port);
                sb.append(" ");
                if ((key.interestOps() & OP_READ) == OP_READ) sb.append("<");
                if ((key.interestOps() & OP_WRITE) == OP_WRITE) sb.append(">");
                sb.append(" ");
                sb.append(uri.getPort());
                sb.append(" ");
                sb.append(this.state);
                sb.append(" ");
                sb.append(str);
                System.out.println(sb.toString());
            }

            public void write(URI uri) throws IOException {
                write(Arrays.asList(uri));
            }

            public void write(List<URI> uris) throws IOException {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                for (URI uri : uris) {
                    byte[] b = uri.toString().getBytes("UTF-8");
                    baos.write(b);
                    baos.write(EOF);
                }

                this.write = ByteBuffer.wrap(baos.toByteArray());
            }

            public boolean drain() throws IOException {
                this.channel.write(write);
                return write.remaining() == 0;
            }

            public String read() throws IOException {

                if (channel.read(read) == -1) throw new EOFException();

                byte[] buf = read.array();

                int end = endOfText(buf, 0, read.position());

                if (end < 0) return null;

                // Copy the string without the terminator char
                String text = new String(buf, 0, end, "UTF-8");

                int newPos = read.position() - end;
                System.arraycopy(buf, end + 1, buf, 0, newPos-1);
                
                read.position(newPos - 1);

                return text;
            }

            private int endOfText(byte[] data, int offset, int pos) {
                for (int i = offset; i < pos; i++) if (data[i] == EOF) return i;
                return -1;
            }

            @Override
            public String toString() {
                return "Session{" +
                        "uri=" + uri +
                        ", state=" + state +
                        '}';
            }

            private final long rate = 3000;

            private long last = 0;

            public void tick() throws IOException {
                if (state != State.HEARTBEAT) return;

                long now = System.currentTimeMillis();
                long delay = now - last;

                if (delay > rate) {
                    last = now;
                    write(me);
                    state(OP_READ | OP_WRITE, State.HEARTBEAT);
                }

            }
        }

        private static enum State {
            GREETING, LISTING, HEARTBEAT
        }

        List<URI> seen = new ArrayList<URI>();

        private void _run() {
            while (true) {

//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    Thread.interrupted();
//                }
                try {
                    selector.select(1000);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    break;
                }

                Set keys = selector.selectedKeys();

                Iterator iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();

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

                            Session session = new Session(client, address, null);
                            session.state(OP_READ, State.GREETING);
                        }

                        if (key.isConnectable()) {

                            // we are a client

                            Session session = (Session) key.attachment();
                            session.channel.finishConnect();

                            // when you are a client, first say high to everyone
                            // before accepting data

                            // once a server reads our address, it will send it's
                            // full list of known addresses, followed by it's own
                            // address to signal that it is done.

                            // we will initiate connections to everyone in the list
                            // who we have not yet seen.

                            // Afterward the server will only pulls its heartbeat

                            session.write(me);

                            session.state(OP_WRITE, State.GREETING);
                        }

                        if (key.isReadable()) {


                            Session session = (Session) key.attachment();

                            switch (session.state) {
                                case GREETING: { // read

                                    String message = session.read();

                                    if (message == null) break; // need to read more

                                    session.setURI(URI.create(message));

                                    session.println("welcome");

                                    ArrayList<URI> list = new ArrayList<URI>(seen);

                                    // When they read themselves on the list
                                    // they'll know it's time to list their URIs

                                    list.remove(me); // yank
                                    list.remove(session.uri); // yank
                                    list.add(session.uri); // add to the end

                                    session.write(list);

                                    session.state(OP_WRITE, State.LISTING);


                                }
                                ;
                                break;

                                case LISTING: { // read

                                    String message = null;

                                    while ((message = session.read()) != null) {

                                        URI uri = URI.create(message);

                                        session.listed.add(uri);

                                        session.println(message);

                                        // they listed me, means they want my list
                                        if (uri.equals(me)) {
                                            ArrayList<URI> list = new ArrayList<URI>(seen);

                                            for (URI reported : session.listed) {
                                                list.remove(reported);
                                            }

                                            // When they read us on the list
                                            // they'll know it's time to switch to heartbeat

                                            list.remove(session.uri);
                                            list.remove(me); // yank if in the middle
                                            list.add(me); // add to the end

                                            session.write(list);

                                            session.state(OP_WRITE, State.LISTING);

                                        } else if (uri.equals(session.uri)) {

                                            session.state(OP_READ, State.HEARTBEAT);

                                        } else if (!seen.contains(uri)) {
                                            try {
                                                connect(uri);
                                            } catch (Exception e) {
                                                println("connect failed " + uri + " - " + e.getMessage());
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                }
                                ;
                                break;

                                case HEARTBEAT: { // read

                                    String message = session.read();

                                    if (message != null) {
                                        session.println("pong");
                                    }

                                }
                                ;
                                break;
                            }

                        }

                        if (key.isWritable()) {

                            Session session = (Session) key.attachment();

                            switch (session.state) {
                                case GREETING: { // write

                                    if (session.drain()) {
                                        session.state(OP_READ, State.LISTING);
                                    }

                                }
                                ;
                                break;

                                case LISTING: { // write

                                    if (session.drain()) {

                                        // we haven't ready any URIs yet
                                        if (session.listed.size() == 0) {

                                            session.state(OP_READ, State.LISTING);

                                        } else {

                                            session.state(OP_READ, State.HEARTBEAT);

                                        }
                                    }
                                }
                                ;
                                break;

                                case HEARTBEAT: { // write

                                    if (session.drain()) {

                                        session.last = System.currentTimeMillis();

                                        session.println("ping");

                                        session.state(OP_READ, State.HEARTBEAT);

                                    }

                                }
                                ;
                                break;
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

                for (SelectionKey key : selector.keys()) {
                    Server.Session session = (Session) key.attachment();

                    try {
                        if (session != null) session.tick();
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

                Session session = new Session(socketChannel, address, uri);
                session.ops(OP_CONNECT);

                // seen - needs to get maintained as "connected"
                // TODO remove from seen
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void seen(URI uri) {
            println("seen " + uri);
            seen.add(uri);
        }

        private void println(String s) {
            System.out.println(port + " - " + s);
        }
    }
}

