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
package org.apache.openejb.server;

import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 */
public class ServiceDaemon implements ServerService {

    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB_SERVER, ServiceDaemon.class);

    private ServerService next;

    private SocketListener socketListener;

    private int timeout;

    private InetAddress address;

    private int port;

    private String name;

    boolean stop = true;

    private int backlog;
    private String ip;


    public ServiceDaemon(ServerService next) {
        this.next = next;
    }

    public ServiceDaemon(ServerService next, int port, String ip) {
        this.port = port;
        this.ip = ip;
        this.address = getAddress(ip);
        this.next = next;
    }

    private static InetAddress getAddress(String host){
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(host);
        }
    }

    public static int getInt(Properties p, String property, int defaultValue){
        String value = p.getProperty(property);
        try {
            if (value != null) return Integer.parseInt(value);
            else return defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

   public void setSoTimeout(int timeout) throws SocketException {
        this.timeout = timeout;
        if (socketListener != null) {
            socketListener.setSoTimeout(timeout);
        }
    }

    public int getSoTimeout() throws IOException {
        if (socketListener == null) return 0;
        return socketListener.getSoTimeout();
    }

    /**
     * Gets the inetAddress number that the
     * daemon is listening on.
     */
    public InetAddress getAddress() {
        return address;
    }

    public void init(Properties props) throws Exception {

        ip = props.getProperty("bind");

        address = getAddress(ip);

        port = getInt(props, "port", 0);

        int threads = getInt(props, "threads", 100);

        backlog = getInt(props, "backlog", threads);

        next.init(props);
    }

    public void start() throws ServiceException {
        synchronized (this) {
            // Don't bother if we are already started/starting
            if (socketListener != null) {
                return;
            }

            next.start();

            ServerSocket serverSocket;
            try {
                serverSocket = new ServerSocket(port, backlog, address);
                port = serverSocket.getLocalPort();
                serverSocket.setSoTimeout(timeout);
            } catch (Exception e) {
                throw new ServiceException("Service failed to open socket", e);
            }

            socketListener = new SocketListener(next, serverSocket);
            Thread thread = new Thread(socketListener);
            thread.setName("service." + name + "@" + socketListener.hashCode());
            thread.setDaemon(true);
            thread.start();

        }
    }

    public void stop() throws ServiceException {

        synchronized (this) {
            if (socketListener != null) {
                socketListener.stop();
                socketListener = null;
            }
            next.stop();
        }
    }

    public String getIP() {
        return ip;
    }

    /**
     * Gets the port number that the
     * daemon is listening on.
     */
    public int getPort() {
        return port;
    }

    public void service(Socket socket) throws ServiceException, IOException {
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
    }

    public String getName() {
        return next.getName();
    }

    private static class SocketListener implements Runnable {
        private ServerService serverService;
        private ServerSocket serverSocket;
        private boolean stopped;

        public SocketListener(ServerService serverService, ServerSocket serverSocket) {
            this.serverService = serverService;
            this.serverSocket = serverSocket;
            stopped = false;
        }

        public synchronized void stop() {
            stopped = true;
        }

        private synchronized boolean shouldStop() {
            return stopped;
        }

        public void run() {
            while (!shouldStop()) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                    socket.setTcpNoDelay(true);
                    if (!shouldStop()) {
                        // the server service is responsible
                        // for closing the socket.
                        serverService.service(socket);
                    }
                } catch (SocketTimeoutException e) {
                    // we don't really care
                    // log.debug("Socket timed-out",e);
                } catch (Throwable e) {
                    log.error("Unexpected error", e);
                }
            }

            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ioException) {
                    log.debug("Error cleaning up socked", ioException);
                }
                serverSocket = null;
            }
            serverService = null;
        }

        public void setSoTimeout(int timeout) throws SocketException {
            serverSocket.setSoTimeout(timeout);
        }

        public int getSoTimeout() throws IOException {
            return serverSocket.getSoTimeout();
        }
    }
}
