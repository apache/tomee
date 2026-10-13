/*
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
package org.apache.openejb.server.admin;

import org.apache.openejb.client.RequestType;
import org.apache.openejb.config.RemoteServer;
import org.apache.openejb.loader.AdminShutdownSecret;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.Server;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdminDaemonTest {

    private File base;
    private ServerSocket serverSocket;
    private CountingServer server;
    private String oldHome;
    private String oldBase;

    @Before
    public void setUp() throws Exception {
        base = Files.tmpdir();
        oldHome = System.setProperty("openejb.home", base.getAbsolutePath());
        oldBase = System.setProperty("openejb.base", base.getAbsolutePath());
        SystemInstance.reset();
        serverSocket = new ServerSocket(0, 10, InetAddress.getLoopbackAddress());
    }

    @After
    public void tearDown() throws Exception {
        serverSocket.close();
        restore("openejb.home", oldHome);
        restore("openejb.base", oldBase);
        SystemInstance.reset();
        Files.delete(base);
    }

    private static void restore(final String key, final String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }

    @Test
    public void secretFileIsOwnerOnlyOnPosix() throws Exception {
        Assume.assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
        newDaemon(new Properties(), true);

        final File secretFile = new File(new File(base, "conf"), AdminShutdownSecret.FILE_NAME);
        assertEquals("rw-------", PosixFilePermissions.toString(java.nio.file.Files.getPosixFilePermissions(secretFile.toPath())));
    }

    @Test
    public void secretWrittenWithoutPosixPermissions() throws Exception {
        final AdminDaemon daemon = newDaemon(new Properties(), true, new AdminDaemon() {
            @Override
            boolean supportsPosix(final Path path) {
                return false;
            }
        });

        final File secretFile = new File(new File(base, "conf"), AdminShutdownSecret.FILE_NAME);
        assertTrue(secretFile.isFile());
        assertFalse(IO.slurp(secretFile).trim().isEmpty());

        Stop.stop(InetAddress.getLoopbackAddress().getHostAddress(), serverSocket.getLocalPort());
        accept(daemon);
        assertEquals(1, server.stops.get());
    }

    @Test
    public void stopRequiresGeneratedSecret() throws Exception {
        final AdminDaemon daemon = newDaemon(new Properties(), true);

        final File secretFile = new File(new File(base, "conf"), AdminShutdownSecret.FILE_NAME);
        assertTrue(secretFile.isFile());

        send(daemon, new byte[]{RequestType.STOP_REQUEST_Stop.getCode()});
        assertEquals(0, server.stops.get());

        send(daemon, "Stop".getBytes(StandardCharsets.US_ASCII));
        assertEquals(0, server.stops.get());

        send(daemon, request('Q', IO.slurp(secretFile).trim() + "x"));
        assertEquals(0, server.stops.get());

        // the stop client reads the secret written by the server
        Stop.stop(InetAddress.getLoopbackAddress().getHostAddress(), serverSocket.getLocalPort());
        accept(daemon);
        assertEquals(1, server.stops.get());

        daemon.stop();
        assertFalse(secretFile.exists());
    }

    @Test
    public void remoteServerSendsGeneratedSecret() throws Exception {
        final AdminDaemon daemon = newDaemon(new Properties(), true);

        final Properties properties = new Properties();
        properties.setProperty("openejb.home", base.getAbsolutePath());
        properties.setProperty(RemoteServer.SERVER_SHUTDOWN_HOST, InetAddress.getLoopbackAddress().getHostAddress());
        properties.setProperty(RemoteServer.SERVER_SHUTDOWN_PORT, Integer.toString(serverSocket.getLocalPort()));
        properties.setProperty(RemoteServer.SERVER_SHUTDOWN_COMMAND, "Q");
        assertTrue(new RemoteServer(properties, 1, false).stop());
        accept(daemon);
        assertEquals(1, server.stops.get());
    }

    @Test
    public void configuredSecret() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty(AdminShutdownSecret.PROPERTY, "changeit");
        final AdminDaemon daemon = newDaemon(properties, true);

        assertFalse(new File(new File(base, "conf"), AdminShutdownSecret.FILE_NAME).exists());

        send(daemon, request('S', "other"));
        assertEquals(0, server.stops.get());

        send(daemon, request('Q', "changeit\0"));
        assertEquals(1, server.stops.get());
    }

    @Test
    public void refuseStopWithoutConfDirectory() throws Exception {
        final AdminDaemon daemon = newDaemon(new Properties(), false);

        send(daemon, new byte[]{RequestType.STOP_REQUEST_Stop.getCode()});
        Stop.stop(InetAddress.getLoopbackAddress().getHostAddress(), serverSocket.getLocalPort());
        accept(daemon);
        assertEquals(0, server.stops.get());
    }

    @Test
    public void secretCanBeDisabled() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty(AdminShutdownSecret.REQUIRED_PROPERTY, "false");
        final AdminDaemon daemon = newDaemon(properties, true);

        send(daemon, new byte[]{RequestType.STOP_REQUEST_stop.getCode()});
        assertEquals(1, server.stops.get());
    }

    private AdminDaemon newDaemon(final Properties properties, final boolean withConf) throws Exception {
        return newDaemon(properties, withConf, new AdminDaemon());
    }

    private AdminDaemon newDaemon(final Properties properties, final boolean withConf, final AdminDaemon daemon) throws Exception {
        if (withConf) {
            Files.mkdir(new File(base, "conf"));
        }
        for (final String key : properties.stringPropertyNames()) {
            SystemInstance.get().setProperty(key, properties.getProperty(key));
        }
        server = new CountingServer();
        SystemInstance.get().setComponent(Server.class, server);

        daemon.init(new Properties());
        return daemon;
    }

    private static byte[] request(final char code, final String secret) {
        return (code + secret).getBytes(StandardCharsets.UTF_8);
    }

    private void send(final AdminDaemon daemon, final byte[] request) throws Exception {
        try (Socket client = new Socket(InetAddress.getLoopbackAddress(), serverSocket.getLocalPort())) {
            final OutputStream out = client.getOutputStream();
            out.write(request);
            out.flush();
            client.shutdownOutput();
            accept(daemon);
        }
    }

    private void accept(final AdminDaemon daemon) throws Exception {
        daemon.service(serverSocket.accept());
    }

    private static class CountingServer extends Server {
        private final AtomicInteger stops = new AtomicInteger();

        @Override
        public void stop() {
            stops.incrementAndGet();
        }
    }
}
