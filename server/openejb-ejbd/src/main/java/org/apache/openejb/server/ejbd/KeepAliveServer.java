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
package org.apache.openejb.server.ejbd;

import org.apache.openejb.client.KeepAliveStyle;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.ServicePool;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @version $Rev$ $Date$
 */
public class KeepAliveServer implements ServerService {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_SERVER.createChild("keepalive"), KeepAliveServer.class);
    private final ServerService service;
    private final long timeout = (1000 * 3);

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ConcurrentHashMap<Thread, Session> sessions = new ConcurrentHashMap<Thread, Session>();
    private BlockingQueue<Runnable> threadQueue;
    private Timer timer;

    public KeepAliveServer() {
        this(new EjbServer());
    }

    public KeepAliveServer(final ServerService service) {
        this.service = service;
    }

    private void closeInactiveSessions() {

        if (!this.running.get()) return;

        final BlockingQueue<Runnable> queue = getQueue();
        if (queue == null) return;

        int backlog = queue.size();
        if (backlog <= 0) return;

        final long now = System.currentTimeMillis();

        final List<Session> current = new ArrayList<Session>();
        current.addAll(this.sessions.values());

        for (final Session session : current) {

            if (session.usage.tryLock()) {
                try {
                    if (now - session.lastRequest > timeout) {
                        try {
                            backlog--;
                            session.socket.close();
                        } catch (IOException e) {
                            if (logger.isWarningEnabled()) {
                                logger.warning("closeInactiveSessions: Error closing socket. Debug for StackTrace");
                            } else if (logger.isDebugEnabled()) {
                                logger.debug("closeInactiveSessions: Error closing socket.", e);
                            }
                        } finally {
                            removeSession(session);
                        }
                    }
                } finally {
                    session.usage.unlock();
                }
            }

            if (backlog <= 0) return;
        }
    }

    public void closeSessions() {

        // Close the ones we can
        final List<Session> current = new ArrayList<Session>();
        current.addAll(this.sessions.values());

        for (final Session session : current) {
            if (session.usage.tryLock()) {
                try {
                    session.socket.close();
                } catch (IOException e) {
                    if (logger.isWarningEnabled()) {
                        logger.warning("closeSessions: Error closing socket. Debug for StackTrace");
                    } else if (logger.isDebugEnabled()) {
                        logger.debug("closeSessions: Error closing socket.", e);
                    }
                } finally {
                    removeSession(session);
                    session.usage.unlock();
                }
            } else if (logger.isDebugEnabled()) {
                logger.debug("Allowing graceful shutdown of " + session.socket.getInetAddress());
            }
        }
    }

    private BlockingQueue<Runnable> getQueue() {
        if (this.threadQueue == null) {
            // this can be null if timer fires before service is fully initialized
            final ServicePool incoming = SystemInstance.get().getComponent(ServicePool.class);
            if (incoming == null) return null;
            final ThreadPoolExecutor threadPool = incoming.getThreadPool();
            this.threadQueue = threadPool.getQueue();
        }
        return this.threadQueue;
    }

    public Session addSession(final Session session) {
        return this.sessions.put(session.thread, session);
    }

    public Session removeSession(final Session session) {
        return this.sessions.remove(session.thread);
    }

    public class KeepAliveTimer extends TimerTask {

        private final KeepAliveServer kas;

        public KeepAliveTimer(final org.apache.openejb.server.ejbd.KeepAliveServer kas) {
            this.kas = kas;
        }

        @Override
        public void run() {
            this.kas.closeInactiveSessions();
        }
    }

    private class Session {

        private final Thread thread;
        private final KeepAliveServer kas;
        private final Lock usage = new ReentrantLock();

        // only used inside the Lock
        private long lastRequest;

        // only used inside the Lock
        private final Socket socket;

        public Session(final KeepAliveServer kas, final Socket socket) {
            this.kas = kas;
            this.socket = socket;
            this.lastRequest = System.currentTimeMillis();
            this.thread = Thread.currentThread();
        }

        public void service(final Socket socket) throws ServiceException, IOException {
            this.kas.addSession(this);

            int i = -1;

            try {
                final InputStream in = new BufferedInputStream(socket.getInputStream());
                final OutputStream out = new BufferedOutputStream(socket.getOutputStream());

                while (running.get()) {
                    try {
                        i = in.read();
                    } catch (SocketException e) {
                        // Socket closed.
                        break;
                    }
                    if (i == -1) {
                        // client hung up
                        break;
                    }
                    final KeepAliveStyle style = KeepAliveStyle.values()[i];

                    try {
                        usage.lock();

                        switch (style) {
                            case PING_PING: {
                                in.read();
                                break;
                            }

                            case PING_PONG: {
                                out.write(style.ordinal());
                                out.flush();
                                break;
                            }
                        }

                        service.service(new Input(in), new Output(out));
                        out.flush();
                    } finally {
                        this.lastRequest = System.currentTimeMillis();
                        usage.unlock();
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IOException("Unexpected byte " + i);
            } catch (InterruptedIOException e) {
                Thread.interrupted();
            } finally {
                this.kas.removeSession(this);
            }
        }
    }


    @Override
    public void service(final Socket socket) throws ServiceException, IOException {
        final Session session = new Session(this, socket);
        session.service(socket);
    }

    @Override
    public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
    }

    @Override
    public String getIP() {
        return service.getIP();
    }

    @Override
    public String getName() {
        return service.getName();
    }

    @Override
    public int getPort() {
        return service.getPort();
    }

    @Override
    public void start() throws ServiceException {
        if (!this.running.getAndSet(true)) {
            this.timer = new Timer("KeepAliveTimer", true);
            this.timer.scheduleAtFixedRate(new KeepAliveTimer(this), this.timeout, (this.timeout / 2));
        }
    }

    @Override
    public void stop() throws ServiceException {
        if (this.running.getAndSet(false)) {
            try {
                this.closeSessions();
            } catch (Throwable e) {
                //Ignore
            }
            this.timer.cancel();
        }
    }

    @Override
    public void init(final Properties props) throws Exception {
        service.init(props);
    }

    public class Input extends java.io.FilterInputStream {

        public Input(final InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
        }
    }

    public class Output extends java.io.FilterOutputStream {
        public Output(final OutputStream out) {
            super(out);
        }

        @Override
        public void close() throws IOException {
            flush();
        }
    }

}
