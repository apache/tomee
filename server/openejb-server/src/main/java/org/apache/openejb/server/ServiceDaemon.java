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

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.StringTemplate;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.URI;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

/**
 */
public class ServiceDaemon implements ServerService {

    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB_SERVER, ServiceDaemon.class);

    private ServerService next;

    private SocketListener socketListener;

    private int timeout = 1000;

    private InetAddress address;

    private int port;

    private String name;

    boolean stop = true;

    private int backlog;

    private String ip;

    private boolean secure;
    private StringTemplate discoveryUriFormat;
    private URI uri;
    private Properties props;

    public ServiceDaemon(ServerService next) {
        this.next = next;
    }

    public ServiceDaemon(ServerService next, int port, String ip) {
        this.port = port;
        this.ip = ip;
        this.address = getAddress(ip);
        this.next = next;
    }

    public static InetAddress getAddress(String host){
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(host);
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

        this.props = props;
        
        String formatString = props.getProperty("discovery");
        if (formatString != null){
            discoveryUriFormat = new StringTemplate(formatString);
        }

        ip = props.getProperty("bind");

        address = getAddress(ip);

        Options options = new Options(props);

        port = options.get("port", 0);

        int threads = options.get("threads", 100);

        backlog = options.get("backlog", threads);

        secure = options.get("secure", false);

        timeout = options.get("timeout", timeout);

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
                if (secure) {
                    ServerSocketFactory factory = SSLServerSocketFactory.getDefault();
                    serverSocket = factory.createServerSocket(port, backlog, address);
                    final String[] enabledCipherSuites = { "SSL_DH_anon_WITH_RC4_128_MD5" };
                    ((SSLServerSocket) serverSocket).setEnabledCipherSuites(enabledCipherSuites);
                } else {
                    serverSocket = new ServerSocket(port, backlog, address);
                }

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

            DiscoveryAgent agent = SystemInstance.get().getComponent(DiscoveryAgent.class);
            if (agent != null && discoveryUriFormat != null) {
                Map<String,String> map = new HashMap<String,String>();

                // add all the properties that were used to construct this service
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    map.put(entry.getKey().toString(), entry.getValue().toString());
                }

                map.put("port", Integer.toString(port));

                String address = ip;

                if ("0.0.0.0".equals(address)) {
                    try {
                        address = InetAddress.getLocalHost().getHostAddress();
                    } catch (UnknownHostException e) {
                        log.error("Failed to resolve 0.0.0.0 to a routable address", e);
                    }
                }

                map.put("host", address);
                map.put("bind", address);
                String uriString = discoveryUriFormat.apply(map);
                try {
                    uri = new URI(uriString);
                    agent.registerService(uri);
                } catch (Exception e) {
                    log.error("Cannot register service '" + getName() + "' with DiscoveryAgent.", e);
                }
            }


        }
    }

    public void stop() throws ServiceException {

        synchronized (this) {
            DiscoveryAgent agent = SystemInstance.get().getComponent(DiscoveryAgent.class);
            if (agent != null && discoveryUriFormat != null && uri != null) {
                try {
                    agent.unregisterService(uri);
                } catch (IOException e) {
                    log.error("Cannot unregister service '" + getName() + "' with DiscoveryAgent.", e);
                }
            }
            next.stop();
            if (socketListener != null) {
                socketListener.stop();
                socketListener = null;
            }
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
        private final ServerService serverService;
        private final ServerSocket serverSocket;
        private AtomicBoolean stop = new AtomicBoolean();
        private Lock lock = new ReentrantLock();

        public SocketListener(ServerService serverService, ServerSocket serverSocket) {
            this.serverService = serverService;
            this.serverSocket = serverSocket;
        }

        public void stop() {
            stop.set(true);
            try {
                if (lock.tryLock(10, TimeUnit.SECONDS)){
                    serverSocket.close();
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
            } catch (IOException e) {
            }
        }

        public void run() {
            while (!stop.get()) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                    socket.setTcpNoDelay(true);
                    if (!stop.get()) {
                        // the server service is responsible
                        // for closing the socket.
                        try {
                            lock.lock();
                            serverService.service(socket);
                        } finally {
                            lock.unlock();
                        }
                    }

                    // Sockets are consumed in other threads
                    // and should never be closed here
                    // It's up to the consumer of the socket
                    // to close it.
                } catch (SocketTimeoutException e) {
                    // we don't really care
                    // log.debug("Socket timed-out",e);
                } catch (SocketException e) {
                    if (!stop.get()){
                        log.error("Socket error", e);
                    }
                } catch (Throwable e) {
                    log.error("Unexpected error", e);
                }
            }

            try {
                serverSocket.close();
            } catch (IOException ioException) {
                log.debug("Error cleaning up socked", ioException);
            }
        }

        public void setSoTimeout(int timeout) throws SocketException {
            serverSocket.setSoTimeout(timeout);
        }

        public int getSoTimeout() throws IOException {
            return serverSocket.getSoTimeout();
        }
    }
}
