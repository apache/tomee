/**
 *
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
package org.apache.openejb.client;

import junit.framework.TestCase;

import javax.security.auth.login.FailedLoginException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientSecurityTest extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
        LoginTestUtil.initialize();
        ClientSecurity.logout();
        ClientSecurity.setIdentityResolver(null);
        System.getProperties().remove(ClientSecurity.IDENTITY_RESOLVER_STRATEGY);
    }

    public void testDefaultStrategy() {
        IdentityResolver identityResolver = ClientSecurity.getIdentityResolver();
        assertNotNull("identityResolver is null", identityResolver);
        assertTrue("identityResolver should be an instance of JaasIdentityResolver", identityResolver instanceof JaasIdentityResolver);
    }

    public void testSimpleStrategy() {
        System.setProperty(ClientSecurity.IDENTITY_RESOLVER_STRATEGY, "simple");
        IdentityResolver identityResolver = ClientSecurity.getIdentityResolver();
        assertNotNull("identityResolver is null", identityResolver);
        assertTrue("identityResolver should be an instance of ClientSecurity.SimpleIdentityResolver", identityResolver instanceof ClientSecurity.SimpleIdentityResolver);
    }

    public void testJaasStrategy() {
        System.setProperty(ClientSecurity.IDENTITY_RESOLVER_STRATEGY, "jaas");
        IdentityResolver identityResolver = ClientSecurity.getIdentityResolver();
        assertNotNull("identityResolver is null", identityResolver);
        assertTrue("identityResolver should be an instance of JaasIdentityResolver", identityResolver instanceof JaasIdentityResolver);
    }
    
    public void testLogin() throws FailedLoginException {
        // setup the server response
        LoginTestUtil.setAuthGranted();

        // attempt a login
        ClientSecurity.login("jonathan", "secret");

        // Verify stored server request
        assertTrue("serverRequest should be an instance of AuthenticationRequest", LoginTestUtil.serverRequest instanceof AuthenticationRequest);
        AuthenticationRequest authenticationRequest = (AuthenticationRequest) LoginTestUtil.serverRequest;
        assertEquals("jonathan", authenticationRequest.getPrincipal());
        assertEquals("secret", authenticationRequest.getCredentials());

        // verify client identity
        assertEquals("SecretIdentity", ClientSecurity.getIdentity());

        // verify we are using the simple client identity strategy
        assertTrue("ClientSecurity.getIdentityResolver() should be an instance of ClientSecurity.SimpleIdentityResolver",
                ClientSecurity.getIdentityResolver() instanceof ClientSecurity.SimpleIdentityResolver);

        // logout
        ClientSecurity.logout();

        // verify we are logged out
        assertNull("ClientSecurity.getIdentity() is not null", ClientSecurity.getIdentity());
    }

    private static Throwable threadException;

    public void testThreadLogin() throws Exception {
        // setup the server response
        LoginTestUtil.setAuthGranted();

        // Perform a thread scoped login using a new thread
        final CountDownLatch loginLatch = new CountDownLatch(1);
        final CountDownLatch loginVerifiedLatch = new CountDownLatch(1);
        Thread loginThread = new Thread() {
            public void run() {
                try {
                    // attempt a login
                    ClientSecurity.login("jonathan", "secret", true);

                    // Verify stored server request
                    assertTrue("serverRequest should be an instance of AuthenticationRequest", LoginTestUtil.serverRequest instanceof AuthenticationRequest);
                    AuthenticationRequest authenticationRequest = (AuthenticationRequest) LoginTestUtil.serverRequest;
                    assertEquals("jonathan", authenticationRequest.getPrincipal());
                    assertEquals("secret", authenticationRequest.getCredentials());

                    // verify client identity
                    assertEquals("SecretIdentity", ClientSecurity.getIdentity());

                    // verify we are using the simple client identity strategy
                    assertTrue("ClientSecurity.getIdentityResolver() should be an instance of ClientSecurity.SimpleIdentityResolver",
                            ClientSecurity.getIdentityResolver() instanceof ClientSecurity.SimpleIdentityResolver);

                    // notify outer thread that we are logged in
                    loginLatch.countDown();

                    // wait for outer thread to verify that it is not also logged in
                    loginVerifiedLatch.await(5, TimeUnit.SECONDS);

                    // logout
                    ClientSecurity.logout();

                    // verify we are logged out
                    assertNull("ClientSecurity.getIdentity() is not null", ClientSecurity.getIdentity());
                } catch (Throwable e) {
                    threadException = e;
                }

            }
        };
        loginThread.start();

        // wait for login thread to login
        loginLatch.await(5, TimeUnit.SECONDS);

        // verify we are not logged in
        assertNull("ClientSecurity.getIdentity() is not null", ClientSecurity.getIdentity());


        // notify the login thread that we are done with out verifications
        loginVerifiedLatch.countDown();

        // wait for login thread to finish
        loginThread.join(5000);

        if (threadException != null) {
            if (threadException instanceof Exception) throw (Exception) threadException;
            if (threadException instanceof Error) throw (Error) threadException;
            throw new Error("login thread threw an exception", threadException);
        }
    }

    public void testAuthDenied() throws Exception {
        LoginTestUtil.setAuthDenied();

        try {
            ClientSecurity.login("nobody", "secret");
            fail("Should have thrown a FailedLoginException");
        } catch (FailedLoginException doNothing) {
        }

        // verify we are not logged in
        assertNull("ClientSecurity.getIdentity() is not null", ClientSecurity.getIdentity());
    }
}
