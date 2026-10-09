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
package org.apache.openejb.server;

import junit.framework.TestCase;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.auth.IPAddressPermission;
import org.apache.openejb.server.auth.IPAddressPermissionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

public class ServiceAccessControllerTest extends TestCase {

    private static final byte[] LOOPBACK = {127, 0, 0, 1};
    private static final byte[] SERVER = {10, 0, 0, 5};
    private static final byte[] LISTED = {121, 122, 123, 124};

    @Override
    protected void tearDown() throws Exception {
        SystemInstance.reset();
        super.tearDown();
    }

    public void testOnlyFromDoesNotAdmitUnlistedLocalhost() throws Exception {
        final ServiceAccessController controller = newController("121.122.123.124");

        assertRejected(controller, LOOPBACK, LOOPBACK);
        assertRejected(controller, LOOPBACK, SERVER);
    }

    public void testOnlyFromDoesNotAdmitUnlistedSelfAddress() throws Exception {
        final ServiceAccessController controller = newController("121.122.123.124");

        assertRejected(controller, SERVER, SERVER);
    }

    public void testOnlyFromAdmitsListedHost() throws Exception {
        final ServiceAccessController controller = newController("121.122.123.124");

        controller.checkHostsAuthorization(InetAddress.getByAddress(LISTED), InetAddress.getByAddress(SERVER));
    }

    public void testOnlyFromLocalhostAdmitsLoopback() throws Exception {
        final ServiceAccessController controller = newController("localhost");

        for (final InetAddress local : InetAddress.getAllByName("localhost")) {
            controller.checkHostsAuthorization(local, local);
        }
        assertRejected(controller, LISTED, LOOPBACK);
    }

    public void testNoOnlyFromPermitsAll() throws Exception {
        final ServiceAccessController controller = newController(null);

        controller.checkHostsAuthorization(InetAddress.getByAddress(new byte[]{(byte) 198, 51, 100, 7}), InetAddress.getByAddress(SERVER));
    }

    public void testImplicitLocalAccessRestoresLegacyBehavior() throws Exception {
        SystemInstance.get().setProperty(ServiceAccessController.IMPLICIT_LOCAL_ACCESS, "true");
        final ServiceAccessController controller = newController("121.122.123.124");

        controller.checkHostsAuthorization(InetAddress.getByAddress(LOOPBACK), InetAddress.getByAddress(SERVER));
        controller.checkHostsAuthorization(InetAddress.getByAddress(SERVER), InetAddress.getByAddress(SERVER));
        assertRejected(controller, new byte[]{121, 122, 123, 125}, SERVER);
    }

    private static ServiceAccessController newController(final String onlyFrom) throws Exception {
        final ServiceAccessController controller = new ServiceAccessController(new MockServerService());
        final Properties properties = new Properties();
        if (onlyFrom != null) {
            properties.put("only_from", onlyFrom);
        }
        controller.init(properties);
        return controller;
    }

    private static void assertRejected(final ServiceAccessController controller, final byte[] client, final byte[] server) throws Exception {
        try {
            controller.checkHostsAuthorization(InetAddress.getByAddress(client), InetAddress.getByAddress(server));
            fail("client should have been rejected");
        } catch (final SecurityException expected) {
            // ok
        }
    }

    public void testWrongExactIPAddressPermission1() throws Exception {
        try {
            IPAddressPermissionFactory.getIPAddressMask("121.122.123.a");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testWrongExactIPAddressPermission2() throws Exception {
        try {
            IPAddressPermissionFactory.getIPAddressMask("121.122.123.256");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testExactIPAddressPermission() throws Exception {
        final IPAddressPermission permission = IPAddressPermissionFactory.getIPAddressMask("121.122.123.124");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[]{121, 122, 123, 124})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[]{121, 122, 123, 125})));
    }

    public void testWrongStartWithIPAddressPermission1() throws Exception {
        try {
            IPAddressPermissionFactory.getIPAddressMask("121.0.123.0");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testStartWithIPAddressPermission() throws Exception {
        final IPAddressPermission permission = IPAddressPermissionFactory.getIPAddressMask("121.122.0.0");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[]{121, 122, 123, 124})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[]{121, 123, 123, 124})));
    }

    public void testFactorizedIPAddressPermission() throws Exception {
        IPAddressPermission permission = IPAddressPermissionFactory.getIPAddressMask("121.122.123.{1,2,3}");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[]{121, 122, 123, 1})));
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[]{121, 122, 123, 2})));
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[]{121, 122, 123, 3})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[]{121, 122, 123, 4})));

        permission = IPAddressPermissionFactory.getIPAddressMask("121.122.{1,2,3}");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[]{121, 122, 1, 1})));
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[]{121, 122, 2, 2})));
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[]{121, 122, 3, 3})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[]{121, 122, 4, 3})));
    }

    public void testNetmaskIPAddressPermission() throws Exception {
        IPAddressPermission permission = IPAddressPermissionFactory.getIPAddressMask("121.122.123.254/31");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[]{121, 122, 123, (byte) 254})));
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[]{121, 122, 123, (byte) 255})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[]{121, 122, 123, (byte) 253})));

        permission = IPAddressPermissionFactory.getIPAddressMask("121.122.123.254/255.255.255.254");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[]{121, 122, 123, (byte) 254})));
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[]{121, 122, 123, (byte) 255})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[]{121, 122, 123, (byte) 253})));
    }

    public void testExactIPv6AddressPermission() throws Exception {
        final IPAddressPermission permission = IPAddressPermissionFactory.getIPAddressMask("101:102:103:104:105:106:107:108");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[]{1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, 1, 8})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[]{1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, 1, 9})));
    }

    public void testNetmaskIPv6AddressPermission() throws Exception {
        IPAddressPermission permission = IPAddressPermissionFactory.getIPAddressMask("101:102:103:104:105:106:107:FFFE/127");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[]{1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, (byte) 255, (byte) 254})));
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[]{1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, (byte) 255, (byte) 255})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[]{1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, (byte) 255, (byte) 253})));

        permission = IPAddressPermissionFactory.getIPAddressMask("101:102:103:104:105:106:107:FFFE/FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFE");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[]{1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, (byte) 255, (byte) 254})));
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[]{1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, (byte) 255, (byte) 255})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[]{1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, (byte) 255, (byte) 253})));
    }

    //    public void testServiceOKWithInit() throws Exception {
    //        Properties properties = new Properties();
    //        properties.put("only_from", "121.122.{56,57}");
    //
    //        MockServerService mockServerService = new MockServerService();
    //        ServiceAccessController controller = new ServiceAccessController(mockServerService);
    //        controller.init(properties);
    //
    //        executeTestServiceOK(mockServerService, controller);
    //    }
    //
    //    public void testServiceNOKWithInit() throws Exception {
    //        Properties properties = new Properties();
    //        properties.put("only_from", "121.122.{56,57}");
    //
    //        MockServerService mockServerService = new MockServerService();
    //        ServiceAccessController controller = new ServiceAccessController(mockServerService);
    //        controller.init(properties);
    //
    //        executeTestServiceOK(mockServerService, controller);
    //    }

    private void executeTestServiceOK(final MockServerService mockServerService, final ServiceAccessController controller) throws ServiceException, IOException {
        MockSocket mockSocket = new MockSocket(InetAddress.getByAddress(new byte[]{121, 122, 56, 123}));
        controller.service(mockSocket);
        assertSame(mockSocket, mockServerService.socket);

        mockSocket = new MockSocket(InetAddress.getByAddress(new byte[]{121, 122, 57, 123}));
        controller.service(mockSocket);
        assertSame(mockSocket, mockServerService.socket);
    }

    private void executeTestServiceNOK(final ServiceAccessController controller) throws ServiceException, IOException {
        final MockSocket mockSocket = new MockSocket(InetAddress.getByAddress(new byte[]{121, 122, 58, 123}));
        try {
            controller.service(mockSocket);
            fail();
        } catch (SecurityException e) {
        }
    }

    private static class MockSocket extends Socket {

        private final InetAddress address;

        private MockSocket(final InetAddress address) {
            this.address = address;
        }

        @Override
        public InetAddress getInetAddress() {
            return address;
        }
    }

    private static class MockServerService implements ServerService {

        private Socket socket;

        @Override
        public void init(final Properties props) throws Exception {
        }

        @Override
        public void start() throws ServiceException {
            throw new AssertionError();
        }

        @Override
        public void stop() throws ServiceException {
            throw new AssertionError();
        }

        @Override
        public String getIP() {
            throw new AssertionError();
        }

        @Override
        public int getPort() {
            throw new AssertionError();
        }

        @Override
        public void service(final Socket socket) throws ServiceException, IOException {
            this.socket = socket;
        }

        @Override
        public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
        }

        @Override
        public String getName() {
            throw new AssertionError();
        }
    }
}