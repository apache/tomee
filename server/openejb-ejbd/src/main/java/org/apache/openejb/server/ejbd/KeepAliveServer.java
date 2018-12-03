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

import org.apache.openejb.client.FlushableGZIPOutputStream;
import org.apache.openejb.client.KeepAliveStyle;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.ServicePool;
import org.apache.openejb.server.Unwrappable;
import org.apache.openejb.server.context.RequestInfos;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;

/**
 * @version $Rev$ $Date$
 */
public class KeepAliveServer implements ServerService {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_SERVER.createChild("keepalive"), KeepAliveServer.class);
    private final ServerService service;
    private final long timeout = (1000 * 10);

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ConcurrentHashMap<Thread, Session> sessions = new ConcurrentHashMap<>();
    private BlockingQueue<Runnable> threadQueue;
    private Timer timer;
    private final boolean gzip;

    @SuppressWarnings("deprecation")
    public KeepAliveServer() {
        this(new EjbServer());
    }

    @Deprecated
    public KeepAliveServer(final ServerService service) {
        this(service, false);
    }

    public KeepAliveServer(final ServerService service, final boolean gzip) {
        this.service = service;
        this.gzip = gzip;
    }

    private void closeInactiveSessions() {

        if (!this.running.get()) {
            return;
        }

        final BlockingQueue<Runnable> queue = this.getQueue();
        if (queue == null) {
            return;
        }

        int backlog = queue.size();
        if (backlog <= 0) {
            return;
        }

        final long now = System.currentTimeMillis();

        final List<Session> current = new ArrayList<Session>();
        current.addAll(this.sessions.values());

        for (final Session session : current) {

            final Lock l = session.lock;

            if (l.tryLock()) {
                try {
                    if (now - session.lastRequest.get() > this.timeout) {

                        backlog--;

                        try {
                            session.close();
                        } catch (Throwable e) {
                            //Ignore
                        } finally {
                            this.removeSession(session);
                        }
                    }
                } finally {
                    l.unlock();
                }
            }

            if (backlog <= 0) {
                return;
            }
        }
    }

    public void closeSessions() {

        // Close the ones we can
        final List<Session> current = new ArrayList<>();
        current.addAll(this.sessions.values());

        for (final Session session : current) {

            final Lock l = session.lock;

            if (l.tryLock()) {
                try {
                    session.close();
                } catch (Throwable e) {
                    //Ignore
                } finally {
                    this.removeSession(session);
                    l.unlock();
                }
            } else if (logger.isDebugEnabled()) {
                try {
                    logger.debug("Allowing graceful shutdown of " + session.socket.getInetAddress());
                } catch (Throwable e) {
                    //Ignore
                }
            }
        }

        this.sessions.clear();
    }

    private BlockingQueue<Runnable> getQueue() {
        if (this.threadQueue == null) {
            // this can be null if timer fires before service is fully initialized
            final ServicePool incoming = Unwrappable.class.isInstance(service) ? Unwrappable.class.cast(service).unwrap(ServicePool.class) : null;
            if (incoming == null) {
                return null;
            }

            this.threadQueue = incoming.getThreadPool().getQueue();
        }
        return this.threadQueue;
    }

    /**
     *
     * @param session
     * @return
     */
    public Session addSession(final Session session) {
        return this.sessions.put(session.thread, session);
    }

    /**
     *
     * @param session
     * @return
     */
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
        private final Lock lock = new ReentrantLock();

        // only used inside the Lock
        private final AtomicLong lastRequest;
        private final Socket socket;
        private InputStream in = null;
        private OutputStream out = null;

        private Session(final KeepAliveServer kas, final Socket socket) {
            this.kas = kas;
            this.socket = socket;
            this.lastRequest = new AtomicLong(System.currentTimeMillis());
            this.thread = Thread.currentThread();
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                this.close();
            } finally {
                super.finalize();
            }
        }

        private void service() throws ServiceException, IOException {
            this.kas.addSession(this);

            int i = -1;

            try {

                final Lock l1 = this.lock;
                l1.lock();

                try {
                    if (!KeepAliveServer.this.gzip) {
                        in = new BufferedInputStream(socket.getInputStream());
                        out = new BufferedOutputStream(socket.getOutputStream());
                    } else {
                        in = new GZIPInputStream(new BufferedInputStream(socket.getInputStream()));
                        out = new BufferedOutputStream(new FlushableGZIPOutputStream(socket.getOutputStream()));
                    }
                } finally {
                    l1.unlock();
                }

                while (KeepAliveServer.this.running.get()) {
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

                    final Lock l2 = this.lock;
                    l2.lock();

                    try {

                        switch (style) {
                            case PING_PING: {
                                i = in.read();
                                break;
                            }
                            case PING_PONG: {
                                out.write(style.ordinal());
                                out.flush();
                                break;
                            }
                        }

                        try {
                            KeepAliveServer.this.service.service(new Input(in), new Output(out));
                            out.flush();
                        } catch (SocketException e) {
                            // Socket closed.
                            break;
                        }
                    } finally {
                        this.lastRequest.set(System.currentTimeMillis());
                        l2.unlock();
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IOException("Unexpected byte " + i);
            } catch (InterruptedIOException e) {
                Thread.interrupted();
            } finally {

                close();

                this.kas.removeSession(this);
            }
        }

        private void close() {
            if (null != in) {
                try {
                    in.close();
                } catch (Throwable e) {
                    //ignore
                }
            }

            if (null != out) {
                try {
                    out.close();
                } catch (Throwable e) {
                    //ignore
                }
            }

            if (null != socket) {
                try {
                    socket.close();
                } catch (Throwable e) {
                    //ignore
                }
            }
        }
    }

    @Override
    public void service(final Socket socket) throws ServiceException, IOException {
        RequestInfos.initRequestInfo(socket);
        try {
            new Session(this, socket).service();
        } finally {
            RequestInfos.clearRequestInfo();
        }
    }

    @Override
    public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
    }

    @Override
    public String getIP() {
        return this.service.getIP();
    }

    @Override
    public String getName() {
        return this.service.getName();
    }

    @Override
    public int getPort() {
        return this.service.getPort();
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
            try {
                this.timer.cancel();
            } catch (Throwable e) {
                //Ignore
            }
        }
    }

    @Override
    public void init(final Properties props) throws Exception {
        this.service.init(props);
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
            this.flush();
        }
    }

}
