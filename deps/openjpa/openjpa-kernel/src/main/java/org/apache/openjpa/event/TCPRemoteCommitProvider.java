/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.event;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Collections;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.GeneralException;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.Serialization;
import java.util.concurrent.locks.ReentrantLock;

import serp.util.Strings;

/**
 * TCP-based implementation of {@link RemoteCommitProvider} that
 * listens for object modifications and propagates those changes to
 * other RemoteCommitProviders over TCP sockets.
 *
 * @author Brian Leair
 * @author Patrick Linskey
 * @since 0.2.5.0
 */
public class TCPRemoteCommitProvider
    extends AbstractRemoteCommitProvider
    implements Configurable {

    private static final int DEFAULT_PORT = 5636;

    private static final Localizer s_loc = Localizer.forPackage
        (TCPRemoteCommitProvider.class);
    private static long s_idSequence = System.currentTimeMillis();

    //	A map of listen ports to listeners in this JVM. We might
    //	want to look into allowing same port, different interface --
    //	that is not currently possible in a single JVM.
    private static final Map s_portListenerMap = new HashMap();

    private long _id;
    private byte[] _localhost;
    private int _port = DEFAULT_PORT;
    private int _maxActive = 2;
    private int _maxIdle = 2;
    private int _recoveryTimeMillis = 15000;
    private TCPPortListener _listener;
    private BroadcastQueue _broadcastQueue = new BroadcastQueue();
    private final List _broadcastThreads = Collections.synchronizedList(
        new LinkedList());

    private ArrayList _addresses = new ArrayList();
    private ReentrantLock _addressesLock;

    public TCPRemoteCommitProvider()
        throws UnknownHostException {
        // obtain a unique ID.
        synchronized (TCPRemoteCommitProvider.class) {
            _id = s_idSequence++;
        }

        // cache the local IP address.
        _localhost = InetAddress.getLocalHost().getAddress();
        _addressesLock = new ReentrantLock();
        setNumBroadcastThreads(2);
    }

    /**
     * The port that this provider should listen on.
     */
    public int getPort() {
        return _port;
    }

    /**
     * The port that this provider should listen on. Set once only.
     */
    public void setPort(int port) {
        _port = port;
    }

    /**
     * The number of milliseconds to wait before retrying
     * to reconnect to a peer after it becomes unreachable.
     */
    public void setRecoveryTimeMillis(int recoverytime) {
        _recoveryTimeMillis = recoverytime;
    }

    /**
     * The number of milliseconds to wait before retrying
     * to reconnect to a peer after it becomes unreachable.
     */
    public int getRecoveryTimeMillis() {
        return _recoveryTimeMillis;
    }

    /**
     * The maximum number of sockets that this provider can
     * simetaneously open to each peer in the cluster.
     */
    public void setMaxActive(int maxActive) {
        _maxActive = maxActive;
    }

    /**
     * The maximum number of sockets that this provider can
     * simetaneously open to each peer in the cluster.
     */
    public int getMaxActive() {
        return _maxActive;
    }

    /**
     * The number of idle sockets that this provider can keep open
     * to each peer in the cluster.
     */
    public void setMaxIdle(int maxIdle) {
        _maxIdle = maxIdle;
    }

    /**
     * The number of idle sockets that this provider can keep open
     * to each peer in the cluster.
     */
    public int getMaxIdle() {
        return _maxIdle;
    }

    /**
     * The number of worker threads that are used for
     * transmitting packets to peers in the cluster.
     */
    public void setNumBroadcastThreads(int numBroadcastThreads) {
        synchronized (_broadcastThreads) {
            int cur = _broadcastThreads.size();
            if (cur > numBroadcastThreads) {
                // Notify the extra worker threads so they stop themselves
                // Threads will not end until they send another pk.
                for (int i = numBroadcastThreads; i < cur; i++) {
                    BroadcastWorkerThread worker = (BroadcastWorkerThread)
                        _broadcastThreads.remove(0);
                    worker.setRunning(false);
                }
            } else if (cur < numBroadcastThreads) {
                // Create additional worker threads
                for (int i = cur; i < numBroadcastThreads; i++) {
                    BroadcastWorkerThread wt = new BroadcastWorkerThread();
                    wt.setDaemon(true);
                    wt.start();
                    _broadcastThreads.add(wt);
                }
            }
        }
    }

    /**
     * The number of worker threads that are used for
     * transmitting packets to peers in the cluster.
     */
    public int getNumBroadcastThreads() {
        return _broadcastThreads.size();
    }

    /**
     * Sets the list of addresses of peers to which this provider will
     * send events to. The peers are semicolon-separated <code>names</code>
     * list in the form of "myhost1:portA;myhost2:portB".
     */
    public void setAddresses(String names)
        throws UnknownHostException {
        // NYI. Could look for equivalence of addresses and avoid
        // changing those that didn't change.

        _addressesLock.lock();
        try {
            for (Iterator iter = _addresses.iterator(); iter.hasNext();) {
                ((HostAddress) iter.next()).close();
            }
            String[] toks = Strings.split(names, ";", 0);
            _addresses = new ArrayList(toks.length);

            InetAddress localhost = InetAddress.getLocalHost();
            String localhostName = localhost.getHostName();

            for (int i = 0; i < toks.length; i++) {
                String host = toks[i];
                String hostname;
                int tmpPort;
                int colon = host.indexOf(':');
                if (colon != -1) {
                    hostname = host.substring(0, colon);
                    tmpPort = Integer.parseInt(host.substring(colon + 1));
                } else {
                    hostname = host;
                    tmpPort = DEFAULT_PORT;
                }
                InetAddress tmpAddress = AccessController
                    .doPrivileged(J2DoPrivHelper.getByNameAction(hostname)); 

                // bleair: For each address we would rather make use of
                // the jdk1.4 isLinkLocalAddress () || isLoopbackAddress ().
                // (Though in practice on win32 they don't work anyways!)
                // Instead we will check hostname. Not perfect, but
                // it will match often enough (people will typically
                // use the DNS machine names and be cutting/pasting.)
                if (localhostName.equals(hostname)) {
                    // This string matches the hostname for for ourselves, we
                    // don't actually need to send ourselves messages.
                    if (log.isTraceEnabled()) {
                        log.trace(s_loc.get("tcp-address-asself",
                            tmpAddress.getHostName() + ":" + tmpPort));
                    }
                } else {
                    HostAddress newAddress = new HostAddress(host);
                    _addresses.add(newAddress);
                    if (log.isTraceEnabled()) {
                        log.trace(s_loc.get("tcp-address-set",
                            newAddress._address.getHostName() + ":"
                                + newAddress._port));
                    }
                }
            }
        } catch (PrivilegedActionException pae) {
            throw (UnknownHostException) pae.getException();
        } finally {
            _addressesLock.unlock();
        }
    }

    // ---------- Configurable implementation ----------

    /**
     * Subclasses that need to perform actions in
     * {@link Configurable#endConfiguration} must invoke this method.
     */
    public void endConfiguration() {
        super.endConfiguration();
        synchronized (s_portListenerMap) {
            // see if a listener exists for this port.
            _listener = (TCPPortListener) s_portListenerMap.get
                (String.valueOf(_port));

            if (_listener == null ||
                (!_listener.isRunning() && _listener._port == _port)) {
                try {
                    _listener = new TCPPortListener(_port, log);
                    _listener.listen();
                    s_portListenerMap.put(String.valueOf(_port), _listener);
                } catch (Exception e) {
                    throw new GeneralException(s_loc.get("tcp-init-exception",
                        String.valueOf(_port)), e).setFatal(true);
                }
            } else if (_listener.isRunning()) {
                if (_listener._port != _port) {
                    // this really shouldn't be able to happen.
                    throw new GeneralException(s_loc.get
                        ("tcp-not-equal", String.valueOf(_port))).
                        setFatal(true);
                }
            } else
                throw new InternalException(s_loc.get("tcp-listener-broken"));
            _listener.addProvider(this);
        }

        _addressesLock.lock();
        try {
            HostAddress curAddress;
            for (Iterator iter = _addresses.iterator();
                iter.hasNext();) {
                curAddress = (HostAddress) iter.next();
                curAddress.setMaxActive(_maxActive);
                curAddress.setMaxIdle(_maxIdle);
            }
        }
        finally {
            _addressesLock.unlock();
        }
    }

    // ---------- RemoteCommitProvider implementation ----------

    // pre 3.3.4	= <no version number transmitted>
    // 3.3 Preview 	= 0x1428acfd;
    // 3.4 			= 0x1428acff;
    private static final long PROTOCOL_VERSION = 0x1428acff;

    public void broadcast(RemoteCommitEvent event) {
        try {
            // build a packet notifying other JVMs of object changes.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeLong(PROTOCOL_VERSION);
            oos.writeLong(_id);
            oos.writeInt(_port);
            oos.writeObject(_localhost);
            oos.writeObject(event);
            oos.flush();

            byte[] bytes = baos.toByteArray();
            baos.close();
            if (_broadcastThreads.isEmpty())
                sendUpdatePacket(bytes);
            else
                _broadcastQueue.addPacket(bytes);
        } catch (IOException ioe) {
            if (log.isWarnEnabled())
                log.warn(s_loc.get("tcp-payload-create-error"), ioe);
        }
    }

    /**
     * Sends a change notification packet to other machines in this
     * provider cluster.
     */
    private void sendUpdatePacket(byte[] bytes) {
        _addressesLock.lock();
        try {
            for (Iterator iter = _addresses.iterator(); iter.hasNext();)
                ((HostAddress) iter.next()).sendUpdatePacket(bytes);
        } finally {
            _addressesLock.unlock();
        }
    }

    public void close() {
        if (_listener != null)
            _listener.removeProvider(this);

        // Remove Broadcast Threads then close sockets.
        _broadcastQueue.close();

        // Wait for _broadcastThreads to get cleaned up.
        while(!_broadcastThreads.isEmpty()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                // Ignore.
            }
        }
        
        _addressesLock.lock();
        try {
            for (Iterator iter = _addresses.iterator(); iter.hasNext();)
                ((HostAddress) iter.next()).close();
        } finally {
            _addressesLock.unlock();
        }
    }

    /**
     * Utility class to hold messages to be sent. This
     * allows calls to broadcast () to return without
     * waiting for the send to complete.
     */
    private static class BroadcastQueue {

        private LinkedList _packetQueue = new LinkedList();
        private boolean _closed = false;

        public synchronized void close() {
            _closed = true;
            notifyAll();
        }

        public synchronized boolean isClosed() {
            return _closed;
        }

        public synchronized void addPacket(byte[] bytes) {
            _packetQueue.addLast(bytes);
            notify();
        }

        /**
         * @return the bytes defining the packet to process, or
         * <code>null</code> if the queue is empty.
         */
        public synchronized byte[] removePacket()
            throws InterruptedException {
            // only wait if the queue is still open. This allows processing
            // of events in the queue to continue, while avoiding sleeping
            // during shutdown.
            while (!_closed && _packetQueue.isEmpty())
                wait();
            if (_packetQueue.isEmpty())
                return null;
            else
                return (byte[]) _packetQueue.removeFirst();
        }
    }

    /**
     * Threads to broadcast packets placed in the {@link BroadcastQueue}.
     */
    private class BroadcastWorkerThread
        extends Thread {

        private boolean _keepRunning = true;

        public void run() {
            while (_keepRunning) {
                try {
                    // This will block until there is a packet to send, or
                    // until the queue is closed.
                    byte[] bytes = _broadcastQueue.removePacket();
                    if (bytes != null)
                        sendUpdatePacket(bytes);
                    else if (_broadcastQueue.isClosed())
                        _keepRunning = false;
                } catch (InterruptedException e) {
                    // End the thread.
                    break;
                }
            }
            remove();
        }

        public void setRunning(boolean keepRunning) {
            _keepRunning = keepRunning;
        }
        
        private void remove() {
            _broadcastThreads.remove(this);
        }
    }

    /**
     * Responsible for listening for incoming packets and processing them.
     */
    private static class TCPPortListener
        implements Runnable {

        private final Log _log;
        private ServerSocket _receiveSocket;
        private Thread _acceptThread;
        private Set _receiverThreads = new HashSet();
        private final Set _providers = new HashSet();

        /**
         * Cache the local IP address
         */
        private byte[] _localhost;

        /**
         * The port that this listener should listen on. Configured
         * by TCPRemoteCommitProvider.
         */
        private int _port;

        /**
         * Should be set to <code>true</code> once the listener is listening.
         */
        private boolean _isRunning = false;

        /**
         * Construct a new TCPPortListener configured to use the specified port.
         */
        private TCPPortListener(int port, Log log)
            throws IOException {
            _port = port;
            _log = log;
            try {
                _receiveSocket = AccessController
                    .doPrivileged(J2DoPrivHelper.newServerSocketAction(_port));
            } catch (PrivilegedActionException pae) {
                throw (IOException) pae.getException();
            }
            _localhost = InetAddress.getLocalHost().getAddress();

            if (_log.isTraceEnabled())
                _log.info(s_loc.get("tcp-start-listener",
                    String.valueOf(_port)));
        }

        private void listen() {
            _acceptThread = new Thread(this);
            _acceptThread.setDaemon(true);
            _acceptThread.start();
        }

        /**
         * All providers added here will be notified of any incoming
         * provider messages. There will be one of these per
         * BrokerFactory in a given JVM.
         * {@link TCPRemoteCommitProvider#endConfiguration} invokes
         * <code>addProvider</code> with <code>this</code> upon
         * completion of configuration.
         */
        private void addProvider(TCPRemoteCommitProvider provider) {
            synchronized (_providers) {
                _providers.add(provider);
            }
        }

        /**
         * Remove a provider from the list of providers to notify of
         * commit events.
         */
        private synchronized void removeProvider
            (TCPRemoteCommitProvider provider) {
            synchronized (_providers) {
                _providers.remove(provider);

                // if the provider list is empty, shut down the thread.
                if (_providers.size() == 0) {
                    _isRunning = false;
                    try {
                        _receiveSocket.close();
                    } catch (IOException ioe) {
                        if (_log.isWarnEnabled())
                            _log.warn(s_loc.get("tcp-close-error"), ioe);
                    }
                    _acceptThread.interrupt();
                }
            }
        }

        private boolean isRunning() {
            synchronized (_providers) {
                return _isRunning;
            }
        }

        public void run() {
            synchronized (_providers) {
                _isRunning = true;
            }

            Socket s = null;
            while (_isRunning) {
                try {
                    s = null;
                    // Block, waiting to accept new connection from a peer
                    s = AccessController.doPrivileged(J2DoPrivHelper
                        .acceptAction(_receiveSocket));
                    if (_log.isTraceEnabled()) {
                        _log.trace(s_loc.get("tcp-received-connection",
                            s.getInetAddress().getHostAddress()
                                + ":" + s.getPort()));
                    }
                    ReceiveSocketHandler sh = new ReceiveSocketHandler(s);
                    Thread receiverThread = new Thread(sh);
                    receiverThread.setDaemon(true);
                    receiverThread.start();
                    _receiverThreads.add(receiverThread);
                } catch (Exception e) {
                    if (e instanceof PrivilegedActionException)
                        e = ((PrivilegedActionException) e).getException();
                    if (!(e instanceof SocketException) || _isRunning)
                        if (_log.isWarnEnabled())
                            _log.warn(s_loc.get("tcp-accept-error"), e);

                    // Nominal case (InterruptedException) because close ()
                    // calls _acceptThread.interrupt ();
                    try {
                        if (s != null)
                            s.close();
                    } catch (Exception ee) {
                        if (_log.isWarnEnabled())
                            _log.warn(s_loc.get("tcp-close-error"), e);
                    }
                }
            }

            // We are done listening. Interrupt any worker threads.
            Thread worker;
            for (Iterator iter = _receiverThreads.iterator();
                iter.hasNext();) {
                worker = (Thread) iter.next();
                // FYI, the worker threads are blocked
                // reading from the socket's InputStream. InputStreams
                // aren't interruptable, so this interrupt isn't
                // really going to be delivered until something breaks
                // the InputStream.
                worker.interrupt();
            }
            synchronized (_providers) {
                try {
                    if (_isRunning)
                        _receiveSocket.close();
                } catch (Exception e) {
                    if (_log.isWarnEnabled())
                        _log.warn(s_loc.get("tcp-close-error"), e);
                }
                _isRunning = false;
                if (_log.isTraceEnabled())
                    _log.trace(s_loc.get("tcp-close-listener",
                        _port + ""));
            }
        }

        /**
         * Utility class that acts as a worker thread to receive Events
         * from broadcasters.
         */
        private class ReceiveSocketHandler
            implements Runnable {

            private InputStream _in;
            private Socket _s;

            private ReceiveSocketHandler(Socket s) {
                // We are the receiving end and we don't send any messages
                // back to the broadcaster. Turn off Nagle's so that
                // we will send ack packets without waiting.
                _s = s;
                try {
                    _s.setTcpNoDelay(true);
                    _in = new BufferedInputStream(s.getInputStream());
                } catch (IOException ioe) {
                    if (_log.isInfoEnabled())
                        _log.info(s_loc.get("tcp-socket-option-error"), ioe);
                    _s = null;
                } catch (Exception e) {
                    if (_log.isWarnEnabled())
                        _log.warn(s_loc.get("tcp-receive-error"), e);
                    _s = null;
                }
            }

            public void run() {
                if (_s == null)
                    return;
                while (_isRunning && _s != null) {
                    try {
                        // This will block our thread, waiting to read
                        // the next Event-object-message.
                        handle(_in);
                    } catch (EOFException eof) {
                        // EOFException raised when peer is properly
                        // closing its end.
                        if (_log.isTraceEnabled()) {
                            _log.trace(s_loc.get("tcp-close-socket",
                                _s.getInetAddress().getHostAddress()
                                    + ":" + _s.getPort()));
                        }
                        break;
                    } catch (Throwable e) {
                        if (_log.isWarnEnabled())
                            _log.warn(s_loc.get("tcp-receive-error"), e);
                        break;
                    }
                }
                // We are done receiving on this socket and this worker
                // thread is terminating.
                try {
                    _in.close();
                    if (_s != null)
                        _s.close();
                } catch (IOException e) {
                    _log.warn(s_loc.get("tcp-close-socket-error",
                        _s.getInetAddress().getHostAddress() + ":"
                            + _s.getPort()), e);
                }
            }

            /**
             * Process an {@link InputStream} containing objects written
             * by {@link TCPRemoteCommitProvider#broadcast(RemoteCommitEvent)}.
             */
            private void handle(InputStream in)
                throws IOException, ClassNotFoundException {
                // This will block waiting for the next
                ObjectInputStream ois = 
                    new Serialization.ClassResolvingObjectInputStream(in);

                long protocolVersion = ois.readLong();
                if (protocolVersion != PROTOCOL_VERSION) {
                    if (_log.isWarnEnabled()) {
                        _log.warn(s_loc.get("tcp-wrong-version-error",
                            _s.getInetAddress().getHostAddress() + ":"
                                + _s.getPort()));
                        return;
                    }
                }

                long senderId = ois.readLong();
                int senderPort = ois.readInt();
                byte[] senderAddress = (byte[]) ois.readObject();
                RemoteCommitEvent rce = (RemoteCommitEvent) ois.readObject();
                if (_log.isTraceEnabled()) {
                    _log.trace(s_loc.get("tcp-received-event",
                        _s.getInetAddress().getHostAddress() + ":"
                            + _s.getPort()));
                }

                boolean fromSelf = senderPort == _port &&
                    Arrays.equals(senderAddress, _localhost);
                TCPRemoteCommitProvider provider;
                synchronized (_providers) {
                    // bleair: We're iterating, but currenlty there can really
                    // only be a single provider.
                    for (Iterator iter = _providers.iterator();
                        iter.hasNext();) {
                        provider = (TCPRemoteCommitProvider) iter.next();
                        if (senderId != provider._id || !fromSelf)
                            provider.eventManager.fireEvent(rce);
                    }
                }
            }
        }
    }

    /**
     * Utility class to store an InetAddress and an int. Not using
     * InetSocketAddress because it's a JDK1.4 API. This also
     * provides a wrapper around the socket(s) associated with this address.
     */
    private class HostAddress {

        private InetAddress _address;
        private int _port;
        private long _timeLastError; // millis
        private boolean _isAvailable; // is peer thought to be up
        private int _infosIssued = 0; // limit log entries

        private GenericObjectPool _socketPool; // reusable open sockets

        /**
         * Construct a new host address from a string of the form
         * "host:port" or of the form "host".
         */
        private HostAddress(String host)
            throws UnknownHostException {
            int colon = host.indexOf(':');
            try {
                if (colon != -1) {
                    _address = AccessController
                        .doPrivileged(J2DoPrivHelper.getByNameAction(host
                            .substring(0, colon)));
                    _port = Integer.parseInt(host.substring(colon + 1));
                } else {
                    _address = AccessController
                        .doPrivileged(J2DoPrivHelper.getByNameAction(host));
                    _port = DEFAULT_PORT;
                }
            } catch (PrivilegedActionException pae) {
                throw (UnknownHostException) pae.getException();
            }
            // -1 max wait == as long as it takes
            _socketPool = new GenericObjectPool
                (new SocketPoolableObjectFactory(), _maxActive,
                    GenericObjectPool.WHEN_EXHAUSTED_BLOCK, -1);
            _isAvailable = true;
        }

        private void setMaxActive(int maxActive) {
            _socketPool.setMaxActive(maxActive);
        }

        private void setMaxIdle(int maxIdle) {
            _socketPool.setMaxIdle(maxIdle);
        }

        public void close() {
            // Close the pool of sockets to this peer. This
            // will close all sockets in the pool.
            try {
                _socketPool.close();
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn(s_loc.get("tcp-close-pool-error"), e);
                }
            }
        }

        private void sendUpdatePacket(byte[] bytes) {
            if (!_isAvailable) {
                long now = System.currentTimeMillis();
                if (now - _timeLastError < _recoveryTimeMillis)
                    // Not enough time has passed since the last error
                    return;
            }
            Socket s = null;
            try {
                s = getSocket();
                OutputStream os = s.getOutputStream();
                os.write(bytes);
                os.flush();

                if (log.isTraceEnabled()) {
                    log.trace(s_loc.get("tcp-sent-update",
                        _address.getHostAddress() + ":" + _port,
                        String.valueOf(s.getLocalPort())));
                }
                _isAvailable = true;
                _infosIssued = 0;
                // Return the socket to the pool; the socket is
                // still good.
                returnSocket(s);
            } catch (Exception e) {
                // There has been a problem sending to the peer.
                // The OS socket that was being used is can no longer
                // be used.
                if (s != null)
                    this.closeSocket(s);
                this.clearAllSockets();

                if (_isAvailable) {
                    // Log a warning, the peer was up and has now gone down
                    if (log.isWarnEnabled()) {
                        log.warn(s_loc.get("tcp-send-error",
                            _address.getHostAddress() + ":" + _port), e);
                    }
                    _isAvailable = false;
                    // Once enough time has passed we will log another warning
                    _timeLastError = System.currentTimeMillis();
                } else {
                    long now = System.currentTimeMillis();
                    if (now - _timeLastError > _recoveryTimeMillis) {
                        if (_infosIssued < 5) {
                            // Enough time has passed, and peer is still down
                            _timeLastError = System.currentTimeMillis();
                            // We were trying to reestablish the connection,
                            // but we failed again. Log a message, but
                            // lower severity. This log will occur periodically
                            // for 5 times until the peer comes back.
                            if (log.isInfoEnabled()) {
                                log.info(s_loc.get("tcp-send-still-error",
                                    _address.getHostAddress() + ":"
                                        + _port), e);
                            }
                            _infosIssued++;
                        }
                    }
                }
            }
        }

        private Socket getSocket()
            throws Exception {
            return (Socket) _socketPool.borrowObject();
        }

        private void returnSocket(Socket s)
            throws Exception {
            _socketPool.returnObject(s);
        }

        private void clearAllSockets() {
            _socketPool.clear();
        }

        private void closeSocket(Socket s) {
            // All sockets come from the pool.
            // This socket is no longer usable, so delete it from the
            // pool.
            try {
                _socketPool.invalidateObject(s);
            } catch (Exception e) {
            }
        }

        /**
         * Factory for pooled sockets.
         */
        private class SocketPoolableObjectFactory
            implements PoolableObjectFactory {

            public Object makeObject()
                throws IOException {
                try {
                    Socket s = AccessController
                        .doPrivileged(J2DoPrivHelper.newSocketAction(_address,
                            _port));
                    if (log.isTraceEnabled()) {
                        log.trace(s_loc.get("tcp-open-connection", _address
                            + ":" + _port, "" + s.getLocalPort()));
                    }
                    return s;
                } catch (PrivilegedActionException pae) {
                    throw (IOException) pae.getException();
                }
            }

            public void destroyObject(Object obj) {
                // silentClose ().
                try {
                    Socket s = (Socket) obj;
                    if (log.isTraceEnabled())
                        log.trace(s_loc.get("tcp-close-sending-socket",
                            _address + ":" + _port, "" + s.getLocalPort()));
                    s.close();
                } catch (Exception e) {
                    log.warn(s_loc.get("tcp-close-socket-error",
                        _address.getHostAddress() + ":" + _port), e);
                }
            }

            public boolean validateObject(Object obj) {
                return true;
            }

            public void activateObject (Object value)
			{
			}

			public void passivateObject (Object value)
			{
			}
		}
	}
}
