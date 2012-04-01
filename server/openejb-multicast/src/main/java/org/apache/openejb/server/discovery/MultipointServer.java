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

import org.apache.openejb.monitoring.Event;
import org.apache.openejb.monitoring.Managed;
import org.apache.openejb.server.ServerRuntimeException;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @version $Rev$ $Date$
 */
@Managed
public class MultipointServer {
    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB_SERVER.createChild("discovery").createChild("multipoint"), MultipointServer.class);

    private static final URI END_LIST = URI.create("end:list");

    private final int port;

    private final URI me;

    private final Set<URI> roots = new LinkedHashSet<URI>();

    private final Event runs = new Event();

    private final Event heartbeats = new Event();

    private final Event reconnects = new Event();
    private final Event sessionsCreated = new Event();

    /**
     * Only used for toString to make debugging easier
     */
    private final String name;

    private final Tracker tracker;

    private final LinkedList<URI> connect = new LinkedList<URI>();
    private final Map<URI, Session> connections = new HashMap<URI, Session>();

    private long joined = 0;

    private long reconnectDelay;

    private ServerSocketChannel serverChannel;

    private final Selector selector;

    private final Lock lock = new ReentrantLock();
    private final Condition started = lock.newCondition();
    private final Condition stopped = lock.newCondition();

    public MultipointServer(int port, Tracker tracker) throws IOException {
        this("localhost", "localhost", port, tracker, randomColor(), true, Collections.EMPTY_SET, new Duration(30, TimeUnit.SECONDS));
    }

    public MultipointServer(String bindHost, String broadcastHost, int port, Tracker tracker, String name, boolean debug, Set<URI> roots, Duration reconnectDelay) throws IOException {
        if (tracker == null) throw new NullPointerException("tracker cannot be null");
        if (bindHost == null) throw new NullPointerException("host cannot be null");

        if (broadcastHost == null) broadcastHost = bindHost;
        if (reconnectDelay == null) reconnectDelay = new Duration(30, TimeUnit.SECONDS);

        this.tracker = tracker;
        this.name = name;

        if (roots != null) {
            this.roots.addAll(roots);
        }

        this.reconnectDelay = reconnectDelay.getTime(TimeUnit.NANOSECONDS);

        final String format = String.format("MultipointServer(bindHost=%s, discoveryHost=%s, port=%s, name=%s, debug=%s, roots=%s, reconnectDelay='%s')",
                bindHost,
                broadcastHost,
                port,
                name,
                debug,
                this.roots.size(),
                reconnectDelay.toString());

        log.debug(format);


        selector = Selector.open();

        final InetSocketAddress address = new InetSocketAddress(bindHost, port);
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        final ServerSocket serverSocket = serverChannel.socket();
        serverSocket.bind(address);
        this.port = serverSocket.getLocalPort();

        if (name != null) {
            me = URI.create("conn://" + broadcastHost + ":" + this.port + "/" + name);
        } else {
            me = URI.create("conn://" + broadcastHost + ":" + this.port);
        }

        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        println("Broadcasting");
    }

    public URI getMe() {
        return me;
    }

    public Set<URI> getRoots() {
        return roots;
    }

    public Event getRuns() {
        return runs;
    }

    public Event getHeartbeats() {
        return heartbeats;
    }

    public Event getReconnects() {
        return reconnects;
    }

    public Event getSessionsCreated() {
        return sessionsCreated;
    }

    public String getName() {
        return name;
    }

    public long getJoined() {
        return joined;
    }

    public List<URI> getSessions() {
        return new ArrayList<URI>(connections.keySet());
    }

    public List<URI> getConnectionsQueued() {
        return new ArrayList<URI>(connect);
    }

    public long getReconnectDelay() {
        return reconnectDelay;
    }

    public int getPort() {
        return port;
    }

    /**
     * Attempt to connect back to the network if
     *  - We aren't already connected
     *  - We aren't already attempting to connect
     *  - It has been a while since we last tried (reconnectDelay)
     */
    private void rejoin() {
        if (connections.size() > 0) return;
        if (connect.size() > 0) return;
        if (System.nanoTime() - joined <= reconnectDelay) return;

        log.info("MultipointReconnect{initialServers=" + roots.size() + "}");

        reconnects.record();

        for (URI root : roots) {
            connect(root);
        }

        this.joined = System.nanoTime();
    }
    public MultipointServer start() {
        if (running.compareAndSet(false, true)) {

            String multipointServer = Join.join(".", "MultipointServer", name, port);
            log.info("MultipointServer Starting : Thread '" + multipointServer + "'");

            Thread thread = new Thread(new Runnable() {
                public void run() {
                    signal(started);
                    try {
                        _run();
                    } finally {
                        signal(stopped);
                    }
                }
            });
            thread.setName(multipointServer);
            thread.start();

            await(started, 10, TimeUnit.SECONDS);
        }
        return this;
    }

    private void signal(Condition condition) {
        lock.lock();
        try {
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    private void await(Condition condition, long time, TimeUnit unit) {
        lock.lock();
        try {
            condition.await(time, unit);
        } catch (InterruptedException e) {
            Thread.interrupted();
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        running.set(false);
        try {
            serverChannel.close();
        } catch (IOException e) {
            throw new CloseException(e);
        } finally {
            await(stopped, 10, TimeUnit.SECONDS);
        }
    }

    public static class CloseException extends RuntimeException {
        public CloseException(Throwable cause) {
            super(cause);
        }
    }

    public class Session {

        private static final int EOF = 3;

        private final SocketChannel channel;
        private final ByteBuffer read = ByteBuffer.allocate(1024);
        private final SelectionKey key;
        private final List<URI> listed = new ArrayList<URI>();
        private final long created = System.currentTimeMillis();

        private ByteBuffer write;

        @Managed
        private State state = State.OPEN;
        private URI uri;
        public boolean hangup;
        private final boolean client;

        public Session(SocketChannel channel, InetSocketAddress address, URI uri) throws ClosedChannelException {
            this.channel = channel;
            this.client = uri != null;
            this.uri = uri != null ? uri : URI.create("conn://" + address.getHostName() + ":" + address.getPort());
            this.key = channel.register(selector, 0, this);
            sessionsCreated.record();
            log.info("Constructing " + this);
        }

        public Session ops(int ops) {
            key.interestOps(ops);
            return this;
        }

        public long getCreated() {
            return created;
        }

        public void state(int ops, State state) {
//            trace("transition "+state +"  "+ops);
            if (this.state != state) {
                if (log.isDebugEnabled()) {
                    log.debug(message(state.name()));
                }
            }
            this.state = state;
            if (ops > 0) key.interestOps(ops);
        }

        public void setURI(URI uri) {
            this.uri = uri;
        }

        private void trace(String str) {
//            println(message(str));

            if (log.isDebugEnabled()) {
                log.debug(message(str));
//                new Exception().fillInStackTrace().printStackTrace();
            }
        }

        private void info(String str) {
//            println(message(str));

            if (log.isInfoEnabled()) {
                log.info(message(str));
            }
        }

        private String message(String str) {
            final StringBuilder sb = new StringBuilder();
            sb.append(name);
            sb.append(":");
            sb.append(port);
            sb.append(" ");
            if (key.isValid()) {
                if ((key.interestOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) sb.append("<");
                if ((key.interestOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) sb.append(">");
                if ((key.interestOps() == 0)) sb.append("-");
            } else {
                sb.append(":");
            }
            sb.append(" ");
            sb.append(uri.getPort());
            sb.append(" ");
            sb.append(this.state);
            sb.append(" ");
            sb.append(str);
            String x = sb.toString();
            return x;
        }

        public void write(URI uri) throws IOException {
            write(Arrays.asList(uri));
        }

        public void write(Collection<?> uris) throws IOException {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            for (Object uri : uris) {
                final String s = uri.toString();
                final byte[] b = s.getBytes("UTF-8");
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

            final byte[] buf = read.array();

            final int end = endOfText(buf, 0, read.position());

            if (end < 0) return null;

            // Copy the string without the terminator char
            final String text = new String(buf, 0, end, "UTF-8");

            final int newPos = read.position() - end;
            System.arraycopy(buf, end + 1, buf, 0, newPos - 1);
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
                    ", created=" + created +
                    ", state=" + state +
                    ", owner=" + port +
                    ", s=" + (client ? channel.socket().getPort() : channel.socket().getLocalPort()) +
                    ", c=" + (!client ? channel.socket().getPort() : channel.socket().getLocalPort()) +
                    ", " + (client ? "client" : "server") +
                    '}';
        }

        private long last = 0;

        public void tick() throws IOException {
            if (state != State.HEARTBEAT) return;

            final long now = System.currentTimeMillis();
            final long delay = now - last;

            if (delay >= tracker.getHeartRate()) {
                last = now;
                heartbeat();
            }

        }

        private void heartbeat() throws IOException {
            heartbeats.record();

            final Set<String> strings = tracker.getRegisteredServices();
//            for (String string : strings) {
//                trace(string);
//            }
            write(strings);
            state(SelectionKey.OP_READ | SelectionKey.OP_WRITE, State.HEARTBEAT);
        }
    }

    private static enum State {
        OPEN, GREETING, LISTING, HEARTBEAT, CLOSED
    }

    private final AtomicBoolean running = new AtomicBoolean();

    private void _run() {

        // The selectorTimeout ensures that even when there are no IO events,
        // this loop will "wake up" and execute at least as frequently as the
        // expected heartrate.
        //
        // We initiate WRITE events (the heartbeats we send) in this loop, so that
        // detail is critical.
        long selectorTimeout = tracker.getHeartRate();

        // For reliability purposes we will actually adjust the selectorTimeout
        // on each iteration of the loop, shrinking it down just a little to a
        // account for the execution time of the loop itself.

        int failed = 0;
        while (running.get()) {
            runs.record();

            final long start = System.nanoTime();

            try {
                selector.select(selectorTimeout);
                failed = 0;
            } catch (IOException ex) {
                if (failed++ > 100) {
                    log.fatal("Too many Multipoint Failures.  Terminating service.", ex);
                    return;
                }
                log.error("Multipoint Failure.", ex);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }

            final Set keys = selector.selectedKeys();
            final Iterator iterator = keys.iterator();
            while (iterator.hasNext()) {
                final SelectionKey key = (SelectionKey) iterator.next();
                iterator.remove();

                try {
                    if (key.isAcceptable()) doAccept(key);

                    if (key.isConnectable()) doConnect(key);

                    if (key.isReadable()) doRead(key);

                    if (key.isWritable()) doWrite(key);

                } catch (CancelledKeyException ex) {
                    synchronized (connect) {
                        final Session session = (Session) key.attachment();
                        if (session.state != State.CLOSED) {
                            close(key);
                        }
                    }
                } catch (ClosedChannelException ex) {
                    synchronized (connect) {
                        final Session session = (Session) key.attachment();
                        if (session.state != State.CLOSED) {
                            close(key);
                        }
                    }
                } catch (IOException ex) {
                    final Session session = (Session) key.attachment();
                    session.trace(ex.getClass().getSimpleName() + ": " + ex.getMessage());
                    close(key);
                }

            }

            // This loop can generate WRITE keys (the heartbeats we send)
            for (SelectionKey key : selector.keys()) {
                final Session session = (Session) key.attachment();

                try {
                    if (session != null && session.state == State.HEARTBEAT) session.tick();
                } catch (IOException ex) {
                    close(key);
                }
            }

            // Here is where we actually will expire missing services
            tracker.checkServices();

            // Fill 'connections' list if we are fully disconnected
            rejoin();

            // Connect to anyone in the 'connections' list
            initiateConnections();

            // Adjust selector timeout so we execute in even increments
            // This keeps the heartbeat and rejoin regular
            selectorTimeout = adjustedSelectorTimeout(start);
        }
    }

    private long adjustedSelectorTimeout(long start) {
        final long end = System.nanoTime();
        final long elapsed = TimeUnit.NANOSECONDS.toMillis(end - start);
        final long heartRate = tracker.getHeartRate();

        return Math.max(1, heartRate - elapsed);
    }

    private void initiateConnections() {
        synchronized (connect) {
            while (connect.size() > 0) {

                final URI uri = connect.removeFirst();

                if (connections.containsKey(uri)) continue;

                final int port = uri.getPort();
                final String host = uri.getHost();

                try {
                    println("open " + uri);

                    // Create a non-blocking NIO channel
                    final SocketChannel socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(false);

                    final InetSocketAddress address = new InetSocketAddress(host, port);

                    socketChannel.connect(address);

                    final Session session = new Session(socketChannel, address, uri);
                    session.ops(SelectionKey.OP_CONNECT);
                    session.trace("client");
                    connections.put(session.uri, session);

                    // seen - needs to get maintained as "connected"
                    // TODO remove from seen
                } catch (IOException e) {
                    throw new ServerRuntimeException(e);
                }
            }
        }
    }

    private void doWrite(SelectionKey key) throws IOException {
        final Session session = (Session) key.attachment();

        switch (session.state) {
            case GREETING: { // write

                // Only CLIENTs write a GREETING message
                // As we are a client, the first thing we do
                // is READ the server's LIST

                if (session.drain()) {
                    session.state(SelectionKey.OP_READ, State.LISTING);
                }

            }
            break;

            case LISTING: { // write

                if (session.drain()) {

                    if (session.client) {
                        // CLIENTs list last, so at this point we've read
                        // the server's list and have written ours

//                        session.trace("DONE WRITING");

                        session.state(SelectionKey.OP_READ, State.HEARTBEAT);

                    } else {
                        // SERVERs always write their list first, so at this
                        // point we switch to LIST READ mode

                        session.state(SelectionKey.OP_READ, State.LISTING);

                    }
                }
            }
            break;

            case HEARTBEAT: { // write

                if (session.drain()) {

                    session.last = System.currentTimeMillis();

//                    session.trace("send");

                    session.state(SelectionKey.OP_READ, State.HEARTBEAT);

                }

            }
            break;
        }
    }

    private void doRead(SelectionKey key) throws IOException {
        final Session session = (Session) key.attachment();

        switch (session.state) {
            case GREETING: { // read

                // This state is only reachable as a SERVER
                // The client connected and said hello by sending
                // its URI to let us know who they are

                // Once this is read, the client will expect us
                // to send our full list of URIs followed by the
                // "end" address.

                // So we switch to WRITE LISTING and they switch
                // to READ LISTING

                // Then we will switch to READ LISTING and they
                // will switch to WRITE LISTING

                final String message = session.read();

                if (message == null) break; // need to read more

                session.setURI(URI.create(message));

                connected(session);

                session.trace("welcome");

                final ArrayList<URI> list = connections();

                // When they read themselves on the list
                // they'll know it's time to list their URIs

                list.remove(me); // yank
                list.add(END_LIST); // add to the end

                session.write(list);

                session.state(SelectionKey.OP_WRITE, State.LISTING);

                session.trace("STARTING");


            }
            break;

            case LISTING: { // read

                String message = null;

                while ((message = session.read()) != null) {

//                    session.trace(message);

                    final URI uri = URI.create(message);

                    if (END_LIST.equals(uri)) {

                        if (session.client) {

                            final ArrayList<URI> list = connections();

                            for (URI reported : session.listed) {
                                list.remove(reported);
                            }

                            // When they read us on the list
                            // they'll know it's time to switch to heartbeat

                            list.remove(session.uri);
                            list.add(END_LIST);

                            session.write(list);

                            session.state(SelectionKey.OP_WRITE, State.LISTING);

                        } else {

                            // We are a SERVER in this relationship, so we will have already
                            // listed our known peers by this point.  From here we switch to
                            // heartbeating

                            // heartbeat time
                            if (session.hangup) {
                                session.state(0, State.CLOSED);
                                session.trace("hangup");
                                hangup(key);

                            } else {

                                session.trace("DONE READING");

                                session.state(SelectionKey.OP_READ, State.HEARTBEAT);

                            }

                        }

                        break;

                    } else {

                        session.listed.add(uri);

                        try {
                            connect(uri);
                        } catch (Exception e) {
                            println("connect failed " + uri + " - " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }

            }
            break;

            case HEARTBEAT: { // read

                String message = null;
                while ((message = session.read()) != null) {
//                    session.trace(message);
                    tracker.processData(message);
                }
            }
            break;
        }
    }

    private void doConnect(SelectionKey key) throws IOException {
        // we are a client

        final Session session = (Session) key.attachment();
        session.channel.finishConnect();
        session.trace("connected");

        // when you are a client, first say high to everyone
        // before accepting data

        // once a server reads our address, it will send it's
        // full list of known addresses, followed by the "end"
        // address to signal that it is done.

        // we will then send our full list of known addresses,
        // followed by the "end" address to signal we are done.

        // Afterward the server will only pulls its heartbeat

        // separately, we will initiate connections to everyone
        // in the list who we have not yet seen.

        // WRITE our GREETING
        session.write(me);

        session.state(SelectionKey.OP_WRITE, State.GREETING);
    }

    private void doAccept(SelectionKey key) throws IOException {
        // we are a server

        // when you are a server, we must first listen for the
        // address of the client before sending data.

        // once they send us their address, we will send our
        // full list of known addresses, followed by the "end"
        // address to signal that we are done.

        // Afterward we will only pulls our heartbeat

        final ServerSocketChannel server = (ServerSocketChannel) key.channel();
        final SocketChannel client = server.accept();
        final InetSocketAddress address = (InetSocketAddress) client.socket().getRemoteSocketAddress();

        client.configureBlocking(false);

        final Session session = new Session(client, address, null);
        session.trace("accept");
        session.state(SelectionKey.OP_READ, State.GREETING);
    }

    private ArrayList<URI> connections() {
        synchronized (connect) {
            final ArrayList<URI> list = new ArrayList<URI>(connections.keySet());
            list.addAll(connect);
            return list;
        }
    }

    private void close(SelectionKey key) {
        final Session session = (Session) key.attachment();


        if (session.hangup) {
            // This was a duplicate connection and was closed
            // do not remove this URI from the 'connections'
            // map as this particular session is not in that
            // map -- only the good session that will not be
            // closed is in there.
            log.info("Hungup " + session);
            session.trace("hungup");
        } else {
            log.info("Closed " + session);
            session.trace("closed");
            synchronized (connect) {
                connections.remove(session.uri);
            }
        }

        session.state(0, State.CLOSED);
        hangup(key);
    }

    private void hangup(SelectionKey key) {
        key.cancel();
        try {
            key.channel().close();
        } catch (IOException cex) {
        }
    }


    public void connect(MultipointServer s) throws Exception {
        connect(s.port);
    }

    public void connect(int port) throws Exception {
        connect(URI.create("conn://localhost:" + port));
    }

    public void connect(URI uri) {
        if (me.equals(uri)) return;

        synchronized (connect) {
            if (!connections.containsKey(uri) && !connect.contains(uri)) {
                log.debug("Queuing{uri=" + uri + "}");
                connect.addLast(uri);
            }
        }
    }

    private void connected(Session session) {

        synchronized (connect) {
            Session duplicate = connections.get(session.uri);
//            Session duplicate = null;

            if (duplicate != null) {


                session.trace("duplicate");

                // At this point we know we have two sockets open
                // to the client, this can happen in two different ways
                //
                //  1. one created by them and one created by us.
                //  2. two created by them and none created by us.
                //
                // For case #1, we will both have detected this situation
                // and know it needs fixing.  Only one of us can hangup
                //
                // For case #2, the client was likely disconnected and
                // is calling back.

                final Session[] sessions = {session, duplicate};

                if (!sessions[0].client && !sessions[1].client) {
                    // Case 1 -- Client is calling back
                    Arrays.sort(sessions, new Comparator<Session>() {
                        @Override
                        public int compare(Session a, Session b) {
                            return (int) (b.created - a.created);
                        }
                    });
                } else {
                    // Case 2 -- We called each other at the same time

                    Arrays.sort(sessions, new Comparator<Session>() {
                    // Goal: Keep the connection with the lowest port number
                    ///
                    // Low vs high is not very significant.  The critical
                    // part is that they both choose the same connection.
                    //
                    // Port numbers are seen on both sides.  There are two
                    // ports (one client and one server) for each connection.
                    //
                    // Both sides will agree to kill the connection with the
                    // lowest server port.  If those are the same, then both
                    // sides will agree to kill the connection with the lowest
                    // client port.  If those are the same, we still close a
                    // connection and hope for the best.  If both connections
                    // are killed we will try again next time another node
                    // lists the server and we notice we are not connected.
                    //
                    public int compare(Session a, Session b) {
                        int serverRank = server(a) - server(b);
                        if (serverRank != 0) return serverRank;
                        return client(a) - client(b);
                    }

                    private int server(Session a) {
                        final Socket socket = a.channel.socket();
                        return a.client ? socket.getPort() : socket.getLocalPort();
                    }

                    private int client(Session a) {
                        final Socket socket = a.channel.socket();
                        return !a.client ? socket.getPort() : socket.getLocalPort();
                    }
                });
                }

                session = sessions[0];
                duplicate = sessions[1];

                session.trace(session + "@" + session.hashCode() + " KEEP");
                duplicate.trace(duplicate + "@" + duplicate.hashCode() + " KILL");

                duplicate.hangup = true;
            }

            if (session.state == State.GREETING) {
                session.info(session + "@" + session.hashCode() + " DISCOVERED");
            }
            connections.put(session.uri, session);
        }
    }

    private void println(String s) {
//        if (debug && s.matches(".*(Listening|DONE|KEEP|KILL)")) {
//            System.out.format("%1$tH:%1$tM:%1$tS.%1$tL - %2$s\n", System.currentTimeMillis(), s);
//        }
    }

    @Override
    public String toString() {
        return "MultipointServer{" +
                "name='" + name + '\'' +
                ", me=" + me +
                '}';
    }

    public static String randomColor() {
        String[] colors = {
                "almond",
                "amber",
                "amethyst",
                "apple",
                "apricot",
                "aqua",
                "aquamarine",
                "ash",
                "azure",
                "banana",
                "beige",
                "black",
                "blue",
                "brick",
                "bronze",
                "brown",
                "burgundy",
                "carrot",
                "charcoal",
                "cherry",
                "chestnut",
                "chocolate",
                "chrome",
                "cinnamon",
                "citrine",
                "cobalt",
                "copper",
                "coral",
                "cornflower",
                "cotton",
                "cream",
                "crimson",
                "cyan",
                "ebony",
                "emerald",
                "forest",
                "fuchsia",
                "ginger",
                "gold",
                "goldenrod",
                "gray",
                "green",
                "grey",
                "indigo",
                "ivory",
                "jade",
                "jasmine",
                "khaki",
                "lava",
                "lavender",
                "lemon",
                "lilac",
                "lime",
                "macaroni",
                "magenta",
                "magnolia",
                "mahogany",
                "malachite",
                "mango",
                "maroon",
                "mauve",
                "mint",
                "moonstone",
                "navy",
                "ocean",
                "olive",
                "onyx",
                "orange",
                "orchid",
                "papaya",
                "peach",
                "pear",
                "pearl",
                "periwinkle",
                "pine",
                "pink",
                "pistachio",
                "platinum",
                "plum",
                "prune",
                "pumpkin",
                "purple",
                "quartz",
                "raspberry",
                "red",
                "rose",
                "rosewood",
                "ruby",
                "salmon",
                "sapphire",
                "scarlet",
                "sienna",
                "silver",
                "slate",
                "strawberry",
                "tan",
                "tangerine",
                "taupe",
                "teal",
                "titanium",
                "topaz",
                "turquoise",
                "umber",
                "vanilla",
                "violet",
                "watermelon",
                "white",
                "yellow"
        };

        final Random random = new Random();
        long l = random.nextLong();

        if (l < 0) l *= -1;

        final long index = l % colors.length;
        final String s = colors[(int) index];

        return s;
    }
}
