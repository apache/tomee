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

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.ServicePool;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Exceptions;
import org.apache.openejb.client.KeepAliveStyle;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
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

    private final AtomicBoolean stop = new AtomicBoolean();
    private final KeepAliveTimer keepAliveTimer;
    private Timer timer;

    public KeepAliveServer() {
        this(new EjbServer());
    }

    public KeepAliveServer(ServerService service) {
        this.service = service;

        keepAliveTimer = new KeepAliveTimer();

        timer = new Timer("KeepAliveTimer", true);
        timer.scheduleAtFixedRate(keepAliveTimer, timeout, timeout / 2);
    }


    public class KeepAliveTimer extends TimerTask {

        // Doesn't need to be a map.  Could be a set if Session.equals/hashCode only referenced the Thread.
        private final Map<Thread, Session> sessions = new ConcurrentHashMap<Thread, Session>();

        private BlockingQueue<Runnable> queue;

        public void run() {
            if (!stop.get()) {
                closeInactiveSessions();
            }
        }

        private void closeInactiveSessions() {
            BlockingQueue<Runnable> queue = getQueue();
            if (queue == null) return;

            int backlog = queue.size();
            if (backlog <= 0) return;

            long now = System.currentTimeMillis();

            for (Session session : sessions.values()) {

                if (session.usage.tryLock()) {
                    try {
                        if (now - session.lastRequest > timeout) {
                            try {
                                backlog--;
                                session.socket.close();
                            } catch (IOException e) {
                                logger.info("Error closing socket.", e);
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
            for (Session session : sessions.values()) {
                if (session.usage.tryLock()) {
                    try {
                        session.socket.close();
                    } catch (IOException e) {
                        logger.info("Error closing socket.", e);
                    } finally {
                        removeSession(session);
                        session.usage.unlock();
                    }
                } else {
                    logger.debug("Allowing graceful shutdown of " + session.socket.getInetAddress());
                }
            }
        }

        private BlockingQueue<Runnable> getQueue() {
            if (queue == null) {
                // this can be null if timer fires before service is fully initialized
                ServicePool incoming = SystemInstance.get().getComponent(ServicePool.class);
                if (incoming == null) return null;
                ThreadPoolExecutor threadPool = incoming.getThreadPool();
                queue = threadPool.getQueue();
            }
            return queue;
        }

        public Session addSession(Session session) {
            return sessions.put(session.thread, session);
        }

        public Session removeSession(Session session) {
            return sessions.remove(session.thread);
        }
    }

    private class Session {

        private final Thread thread;
        private final Lock usage = new ReentrantLock();

        // only used inside the Lock
        private long lastRequest;

        // only used inside the Lock
        private final Socket socket;

        public Session(Socket socket) {
            this.socket = socket;
            this.lastRequest = System.currentTimeMillis();
            this.thread = Thread.currentThread();
        }

        public void service(Socket socket) throws ServiceException, IOException {
            keepAliveTimer.addSession(this);

            int i = -1;

            try {
                InputStream in = new BufferedInputStream(socket.getInputStream());
                OutputStream out = new BufferedOutputStream(socket.getOutputStream());

                while (!stop.get()) {
                    try {
                        i = in.read();
                    } catch (SocketException e) {
                        // Socket closed.
                        break;
                    }
                    if (i == -1){
                        // client hung up
                        break;
                    }
                    KeepAliveStyle style = KeepAliveStyle.values()[i];

                    try {
                        usage.lock();

                        switch(style){
                            case PING_PING: {
                                in.read();
                            }
                            break;
                            case PING_PONG: {
                                out.write(style.ordinal());
                                out.flush();
                            }
                        }

                        service.service(new Input(in), new Output(out));
                        out.flush();
                    } finally {
                        this.lastRequest = System.currentTimeMillis();
                        usage.unlock();
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e){
                throw new IOException("Unexpected byte " + i);
            } catch (InterruptedIOException e) {
                Thread.interrupted();
            } finally {
                keepAliveTimer.removeSession(this);
            }
        }
    }


    public void service(Socket socket) throws ServiceException, IOException {
        Session session = new Session(socket);
        session.service(socket);
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
    }

    public String getIP() {
        return service.getIP();
    }

    public String getName() {
        return service.getName();
    }

    public int getPort() {
        return service.getPort();
    }

    public void start() throws ServiceException {
        stop.set(false);

//        service.start();
    }


    public void stop() throws ServiceException {
        stop.set(true);
        keepAliveTimer.closeSessions();
//        service.stop();
    }

    public void init(Properties props) throws Exception {
        service.init(props);
    }

    public class Input extends java.io.FilterInputStream {

        public Input(InputStream in) {
            super(in);
        }

        public void close() throws IOException {
        }
    }

    public class Output extends java.io.FilterOutputStream {
        public Output(OutputStream out) {
            super(out);
        }

        public void close() throws IOException {
            flush();
        }
    }

}
