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

import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.monitoring.Managed;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.PropertyPlaceHolderHelper;
import org.apache.openejb.util.StringTemplate;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("UnusedDeclaration")
@Managed
public class ServiceDaemon extends UnwrappbleServerService {

    private static final Logger LOG = Logger.getInstance(LogCategory.OPENEJB_SERVER, ServiceDaemon.class);

    @Managed
    private final ServerService next;

    private SocketListener socketListener;

    private int timeout = 0;

    private InetAddress inetAddress;

    private int port;

    private int backlog;

    private String ip;

    private boolean secure;
    private StringTemplate discoveryUriFormat;
    private URI serviceUri;
    private Properties props;
    private String[] enabledCipherSuites;

    public ServiceDaemon(final ServerService next) {
        this.next = next;
    }

    public ServiceDaemon(final ServerService next, final int port, final String ip) {
        this.port = port;
        this.ip = ip;
        this.inetAddress = getAddress(ip);
        this.next = next;
    }

    public static InetAddress getAddress(final String host) {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(host);
        }
    }

    /**
     * Gets the inetAddress number that the
     * daemon is listening on.
     * @return 
     */
    public InetAddress getInetAddress() {
        return this.inetAddress;
    }

    @Override
    public void init(final Properties props) throws Exception {

        this.props = props;

        final String formatString = props.getProperty("discovery");
        if (formatString != null) {
            this.discoveryUriFormat = new StringTemplate(formatString);
        }

        this.ip = PropertyPlaceHolderHelper.simpleValue(props.getProperty("bind"));

        this.inetAddress = getAddress(this.ip);

        final Options options = new Options(props);

        this.port = Integer.parseInt(PropertyPlaceHolderHelper.simpleValue(options.get("port", "0")));

        final int threads = options.get("threads", 100);

        this.backlog = options.get("backlog", threads);

        this.secure = options.get("secure", false);

        this.timeout = options.get("timeout", this.timeout);

        this.enabledCipherSuites = options.get("enabledCipherSuites", "SSL_DH_anon_WITH_RC4_128_MD5").split(",");

        this.next.init(props);
    }

    @Override
    public void start() throws ServiceException {
        synchronized (this) {
            // Don't bother if we are already started/starting
            if (this.socketListener != null) {
                return;
            }

            this.next.start();

            final ServerSocket serverSocket;
            try {
                if (this.secure) {
                    final ServerSocketFactory factory = SSLServerSocketFactory.getDefault();
                    serverSocket = factory.createServerSocket(this.port, this.backlog, this.inetAddress);
                    ((SSLServerSocket) serverSocket).setEnabledCipherSuites(this.enabledCipherSuites);
                } else {
                    serverSocket = new ServerSocket();
                    serverSocket.setReuseAddress(true);

                    try {
                    serverSocket.bind(new InetSocketAddress(this.inetAddress, this.port), this.backlog);

                    } catch (final BindException e) {
                        //One retry - Port may be closing
                        Thread.sleep(1000);
                        serverSocket.bind(new InetSocketAddress(this.inetAddress, this.port), this.backlog);
                }
                }

                serverSocket.setSoTimeout(this.timeout);
                int serverPort = serverSocket.getLocalPort();
                if (this.port == 0 && next.getName() != null) {
                    SystemInstance.get().getProperties().put(next.getName() + ".port", Integer.toString(serverPort));
                    this.port = serverPort;
                }

            } catch (Exception e) {
                throw new ServiceException("Service failed to open socket", e);
            }

            this.socketListener = new SocketListener(this.next, serverSocket);
            final Thread thread = new Thread(this.socketListener);
            thread.setName("Service." + this.getName() + "@" + this.socketListener.hashCode());
            thread.setDaemon(true);
            thread.start();

            final DiscoveryAgent agent = SystemInstance.get().getComponent(DiscoveryAgent.class);
            if (agent != null && this.discoveryUriFormat != null) {
                final Map<String, String> map = new HashMap<>();

                // add all the properties that were used to construct this service
                for (final Map.Entry<Object, Object> entry : this.props.entrySet()) {
                    map.put(entry.getKey().toString(), entry.getValue().toString());
                }

                map.put("port", Integer.toString(this.port));

                String address = this.ip;

                if ("0.0.0.0".equals(address)) {
                    try {
                        address = InetAddress.getLocalHost().getHostAddress();
                    } catch (UnknownHostException e) {
                        LOG.error("Failed to resolve 0.0.0.0 to a routable address", e);
                    }
                }

                map.put("host", address);
                map.put("bind", address);
                final String uriString = this.discoveryUriFormat.apply(map);
                try {
                    this.serviceUri = new URI(uriString);
                    agent.registerService(this.serviceUri);
                } catch (Exception e) {
                    LOG.error("Cannot register service '" + this.getName() + "' with DiscoveryAgent.", e);
                }
            }


        }
    }

    @Override
    public void stop() throws ServiceException {

        synchronized (this) {
            final DiscoveryAgent agent = SystemInstance.get().getComponent(DiscoveryAgent.class);
            if (agent != null && this.discoveryUriFormat != null && this.serviceUri != null) {
                try {
                    agent.unregisterService(this.serviceUri);
                } catch (IOException e) {
                    LOG.error("Cannot unregister service '" + this.getName() + "' with DiscoveryAgent.", e);
                }
            }
            this.next.stop();
            if (this.socketListener != null) {
                this.socketListener.stop();
                this.socketListener = null;
            }
        }
    }

    @Override
    public String getIP() {
        return this.ip;
    }

    /**
     * Gets the port number that the
     * daemon is listening on.
     * @return 
     */
    @Override
    @Managed
    public int getPort() {
        return this.port;
    }

    @Managed
    public String getBind() {
        return this.ip;
    }

    @Override
    public void service(final Socket socket) throws ServiceException, IOException {
    }

    @Override
    public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
    }

    @Override
    public String getName() {
        return this.next.getName();
    }

    @Override
    protected Object getDelegate() {
        return next;
    }

    private static class SocketListener implements Runnable {
        private final ServerService serverService;
        private final ServerSocket serverSocket;
        private final AtomicBoolean stop = new AtomicBoolean();
        private final Lock lock = new ReentrantLock();

        public SocketListener(final ServerService serverService, final ServerSocket serverSocket) {
            this.serverService = serverService;
            this.serverSocket = serverSocket;
        }

        public void stop() {
            this.stop.set(true);
            boolean b = false;
            final Lock l = this.lock;
            try {
                //This lock is here to try and be fair to the serverService on a shutdown
                b = l.tryLock(10, TimeUnit.SECONDS);
            } catch (Throwable e) {
                //Ignore
            } finally {

                try {
                    this.serverSocket.close();
                } catch (Throwable e) {
                    //Ignore
                } finally {
                    if (b) {
                        l.unlock();
                    }
                }
            }
        }

        @Override
        public void run() {
            while (!this.stop.get()) {
                try {
                    final Socket socket = this.serverSocket.accept();

                    if (socket.isClosed()) {
                        continue;
                    }

                    socket.setSoLinger(true, 10);
                    socket.setTcpNoDelay(true);

                    if (!this.stop.get()) {
                        // the server service is responsible
                        // for closing the socket.
                        final Lock l = this.lock;
                        l.lock();
                        try {
                            this.serverService.service(socket);
                        } finally {
                            l.unlock();
                        }
                    }

                    // Sockets are consumed in other threads
                    // and should never be closed here
                    // It's up to the consumer of the socket
                    // to close it.
                } catch (SocketTimeoutException e) {
                    // Ignore - Should not get here on serverSocket.setSoTimeout(0)
                } catch (SocketException e) {
                    if (!this.stop.get()) {
                        LOG.debug("Socket error", e);
                    }
                } catch (Throwable e) {
                    if (!this.stop.get()) {
                        LOG.debug("Unexpected error", e);
                    }
                }
            }

            try {
                this.serverSocket.close();
            } catch (Throwable e) {
                LOG.debug("Error cleaning up socked", e);
            }
        }

        public void setSoTimeout(final int timeout) throws SocketException {
            this.serverSocket.setSoTimeout(timeout);
        }

        public int getSoTimeout() throws IOException {
            return this.serverSocket.getSoTimeout();
        }

        public ServerSocket getServerSocket() {
            return this.serverSocket;
        }
    }

    @Managed
    public URI getServiceUri() {
        return this.serviceUri;
    }

    @Managed
    public boolean isSecure() {
        return this.secure;
    }

    @Managed
    private final AddressMonitor address = new AddressMonitor();

    @Managed(append = true)
    public class AddressMonitor {
        @Managed
        public String getHostName() {
            return ServiceDaemon.this.inetAddress.getHostName();
        }

        @Managed
        public String getCanonicalHostName() {
            return ServiceDaemon.this.inetAddress.getCanonicalHostName();
        }

        @Managed
        public String getHostAddress() {
            return ServiceDaemon.this.inetAddress.getHostAddress();
        }

        @Managed
        public byte[] getAddress() {
            return ServiceDaemon.this.inetAddress.getAddress();
        }
    }

    public ServerSocket getServerSocket() {
        return this.socketListener.getServerSocket();
    }

    @Managed
    private final SocketMonitor socket = new SocketMonitor();

    @Managed(append = true)
    public class SocketMonitor {
        @Managed
        public int getLocalPort() {
            return ServiceDaemon.this.getServerSocket().getLocalPort();
        }

        @Managed
        public boolean getReuseAddress() throws SocketException {
            return ServiceDaemon.this.getServerSocket().getReuseAddress();
        }

        @Managed
        public int getSoTimeout() throws IOException {
            return ServiceDaemon.this.getServerSocket().getSoTimeout();
        }

        @Managed
        public boolean isClosed() {
            return ServiceDaemon.this.getServerSocket().isClosed();
        }

        @Managed
        public boolean isBound() {
            return ServiceDaemon.this.getServerSocket().isBound();
        }

        @Managed
        public int getReceiveBufferSize() throws SocketException {
            return ServiceDaemon.this.getServerSocket().getReceiveBufferSize();
        }

        @Managed
        public void setReceiveBufferSize(final int size) throws SocketException {
            ServiceDaemon.this.getServerSocket().setReceiveBufferSize(size);
        }

        @Managed
        public void setPerformancePreferences(final int connectionTime, final int latency, final int bandwidth) {
            ServiceDaemon.this.getServerSocket().setPerformancePreferences(connectionTime, latency, bandwidth);
        }

        @Managed
        public void setReuseAddress(final boolean on) throws SocketException {
            ServiceDaemon.this.getServerSocket().setReuseAddress(on);
        }

        @Managed
        public void setSoTimeout(final int timeout) throws SocketException {
            ServiceDaemon.this.getServerSocket().setSoTimeout(timeout);
        }
    }
}
