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

import javax.security.auth.Subject;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

public class ClientLoginTest extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
        LoginTestUtil.initialize();
    }

    public void testAuthGranted() throws LoginException {
        // setup the server response
        LoginTestUtil.setAuthGranted();

        // attempt a login
        LoginContext context = new LoginContext("ClientLogin", new UsernamePasswordCallbackHandler("jonathan", "secret"));
        context.login();

        // Verify stored server request
        assertTrue("serverRequest should be an instance of AuthenticationRequest", LoginTestUtil.serverRequest instanceof AuthenticationRequest);
        AuthenticationRequest authenticationRequest = (AuthenticationRequest) LoginTestUtil.serverRequest;
        assertEquals("jonathan", authenticationRequest.getUsername());
        assertEquals("secret", authenticationRequest.getCredentials());

        // get the subject
        Subject subject = context.getSubject();

        // verify subject
        assertEquals("Should have one principal", 1, subject.getPrincipals().size());
        assertEquals("Should have one user principal", 1, subject.getPrincipals(ClientIdentityPrincipal.class).size());
        ClientIdentityPrincipal principal = subject.getPrincipals(ClientIdentityPrincipal.class).iterator().next();
        assertEquals("jonathan", principal.getName());
        assertEquals("SecretIdentity", principal.getClientIdentity());

        // logout
        context.logout();

        // verify we are logged out
        assertEquals("Should have zero principals", 0, subject.getPrincipals().size());
        assertNull("ClientSecurity.getIdentity() is not null", ClientSecurity.getIdentity());
    }

    public void testAuthDenied() throws Exception {
        LoginTestUtil.setAuthDenied();

        LoginContext context = new LoginContext("ClientLogin", new UsernamePasswordCallbackHandler("nobody", "secret"));
        try {
            context.login();
            fail("Should have thrown a FailedLoginException");
        } catch (FailedLoginException doNothing) {
        }
    }
}
