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
package org.apache.openejb.client;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URI;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SocketConnectionFactory implements ConnectionFactory {

    private KeepAliveStyle keepAliveStyle = KeepAliveStyle.PING;

    private static Map<URI, Pool> connections = new ConcurrentHashMap<URI, Pool>();
    private int size = 5;
    private long timeout = 1000;

    public SocketConnectionFactory() {

        size = getSize();
        timeout = getTimeout();
    }

    private long getTimeout() {
        Properties p = System.getProperties();
        long timeout = getLong(p, "openejb.client.connectionpool.timeout", this.timeout);
        timeout = getLong(p, "openejb.client.connection.pool.timeout", timeout);
        return timeout;
    }

    private int getSize() {
        Properties p = System.getProperties();
        int size = getInt(p, "openejb.client.connectionpool.size", this.size);
        size = getInt(p, "openejb.client.connection.pool.size", size);
        return size;
    }

    public static int getInt(Properties p, String property, int defaultValue) {
        String value = p.getProperty(property);
        try {
            if (value != null) return Integer.parseInt(value);
            else return defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static long getLong(Properties p, String property, long defaultValue) {
        String value = p.getProperty(property);
        try {
            if (value != null) return Long.parseLong(value);
            else return defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Connection getConnection(URI uri) throws java.io.IOException {

        Pool pool = getPool(uri);

        SocketConnection conn = pool.get();
        if (conn == null) {
            try {
                conn = new SocketConnection(uri, pool);
                conn.open(uri);
            } catch (IOException e) {
                pool.put(null);
                throw e;
            }
        }

        try {
            conn.lock.tryLock(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.interrupted();
            pool.put(conn);
            throw new IOException("Connection busy");
        }

        OutputStream ouputStream = conn.getOuputStream();
        if (conn.socket.isClosed()) {
            pool.put(null);
            return getConnection(uri);
        }

        try {
            ouputStream.write(getKeepAliveStyle().ordinal());

            switch (getKeepAliveStyle()) {
                case PING_PING: {
                    ouputStream.flush();
                    ouputStream.write(getKeepAliveStyle().ordinal());
                    ouputStream.flush();
                }
                break;
                case PING_PONG: {
                    ouputStream.flush();
                    conn.getInputStream().read();
                }
            }
        } catch (IOException e) {
            pool.put(null);
            throw e;
        }

        return conn;
    }

    private Pool getPool(URI uri) {
        Pool pool = connections.get(uri);
        if (pool == null) {
            pool = new Pool(uri, getSize(), getTimeout());
            connections.put(uri, pool);
        }
        return pool;
    }

    public KeepAliveStyle getKeepAliveStyle() {
        String property = System.getProperty("openejb.client.keepalive");
        if (property != null) {
            property = property.toUpperCase();
            return KeepAliveStyle.valueOf(property);
        }
        return keepAliveStyle;
    }

    class SocketConnection implements Connection {

        private Socket socket = null;
        private final URI uri;

        private boolean discarded;
        private final Pool pool;
        private final Lock lock = new ReentrantLock();
        private OutputStream out;
        private BufferedInputStream in;

        public SocketConnection(URI uri, Pool pool) {
            this.uri = uri;
            this.pool = pool;
        }

        protected void open(URI uri) throws IOException {

            /*-----------------------*/
            /* Open socket to server */
            /*-----------------------*/
            try {
                if (uri.getScheme().equalsIgnoreCase("ejbds")) {
                    SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(uri.getHost(), uri.getPort());
                    // use an anonymous cipher suite so that a KeyManager or
                    // TrustManager is not needed
                    // NOTE: this assumes that the cipher suite is known. A check
                    // -should- be done first.
                    final String[] enabledCipherSuites = {"SSL_DH_anon_WITH_RC4_128_MD5"};
                    sslSocket.setEnabledCipherSuites(enabledCipherSuites);
                    socket = sslSocket;
                } else {
                    socket = new Socket(uri.getHost(), uri.getPort());
                }

                socket.setTcpNoDelay(true);
            } catch (ConnectException e) {
                throw new ConnectException("Cannot connect to server '" + uri.toString() + "'.  Check that the server is started and that the specified serverURL is correct.");

            } catch (IOException e) {
                throw new IOException("Cannot connect to server: '" + uri.toString() + "'.  Exception: " + e.getClass().getName() + " : " + e.getMessage());

            } catch (SecurityException e) {
                throw new IOException("Cannot access server: '" + uri.toString() + "' due to security restrictions in the current VM: " + e.getClass().getName() + " : " + e.getMessage());

            } catch (Throwable e) {
                throw new IOException("Cannot  connect to server: '" + uri.toString() + "' due to an unkown exception in the OpenEJB client: " + e.getClass().getName() + " : " + e.getMessage());
            }

        }

        public void discard() {
            pool.put(null);
            discarded = true;
            try {
                socket.close();
            } catch (IOException e) {
            }
            // don't bother unlocking it
            // it should never get used again
        }

        public URI getURI() {
            return uri;
        }

        public void close() throws IOException {
            if (discarded) return;

            pool.put(this);
            lock.unlock();
        }

        public InputStream getInputStream() throws IOException {
            /*----------------------------------*/
            /* Open input streams */
            /*----------------------------------*/
            try {
                if (in == null) {
                    in = new BufferedInputStream(socket.getInputStream());
                }

                return new Input(in);
            } catch (StreamCorruptedException e) {
                throw new IOException("Cannot open input stream to server, the stream has been corrupted: " + e.getClass().getName() + " : " + e.getMessage());

            } catch (IOException e) {
                throw new IOException("Cannot open input stream to server: " + e.getClass().getName() + " : " + e.getMessage());

            } catch (Throwable e) {
                throw new IOException("Cannot open output stream to server: " + e.getClass().getName() + " : " + e.getMessage());
            }
        }

        public OutputStream getOuputStream() throws IOException {
            /*----------------------------------*/
            /* Openning output streams */
            /*----------------------------------*/
            try {
                if (out == null) {
                    out = new BufferedOutputStream(socket.getOutputStream());
                }
                return new Output(out);
            } catch (IOException e) {
                throw new IOException("Cannot open output stream to server: " + e.getClass().getName() + " : " + e.getMessage());

            } catch (Throwable e) {
                throw new IOException("Cannot open output stream to server: " + e.getClass().getName() + " : " + e.getMessage());
            }
        }

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

    private static class Pool {
        private final Semaphore semaphore;
        private final Stack<SocketConnection> pool;
        private final long timeout;
        private final TimeUnit timeUnit;
        private final int size;
        private final URI uri;

        private Pool(URI uri, int size, long timeout) {
            this.uri = uri;
            this.size = size;
            this.semaphore = new Semaphore(size);
            this.pool = new Stack<SocketConnection>();
            this.timeout = timeout;
            this.timeUnit = TimeUnit.MILLISECONDS;

            Object[] objects = new Object[size];
            for (int i = 0; i < objects.length; i++) {
                pool.push(null);
            }
        }

        public SocketConnection get() throws IOException{
            try {
                if (semaphore.tryAcquire(timeout, timeUnit)) {
                    return pool.pop();
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
            }

            throw new ConnectionPoolTimeoutException("No connections available in pool (size " + size + ").  Waited for " + timeout + " seconds for a connection.");
        }

        public void put(SocketConnection connection) {
            pool.push(connection);
            semaphore.release();
        }

        @Override
        public String toString() {
            return "Pool{" +
                    "size=" + size +
                    ", available=" + semaphore.availablePermits() +
                    ", uri=" + uri +
                    '}';
        }
    }
}
