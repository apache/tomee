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
package org.apache.openejb.server.security;

import junit.framework.TestCase;
import org.apache.openejb.server.security.GroupPrincipal;
import org.apache.openejb.server.security.UserPrincipal;
import org.apache.openejb.server.security.UsernamePasswordCallbackHandler;

import javax.security.auth.Subject;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
public class PropertiesLoginModuleTest extends TestCase {

    static {
        loadJassLoginConfig();
    }

    private static void loadJassLoginConfig() {
        String path = System.getProperty("java.security.auth.login.config");
        if (path == null) {
            URL resource = PropertiesLoginModuleTest.class.getClassLoader().getResource("login.config");
            if (resource != null) {
                path = resource.getFile();
                System.setProperty("java.security.auth.login.config", path);
            }
        }
        System.out.println("Path to login config: " + path);
    }

    public void testLogin() throws LoginException {
        LoginContext context = new LoginContext("PropertiesLogin", new UsernamePasswordCallbackHandler("jonathan", "secret"));
        context.login();

        Subject subject = context.getSubject();

        assertEquals("Should have three principals", 3, subject.getPrincipals().size());
        assertEquals("Should have one user principal", 1, subject.getPrincipals(UserPrincipal.class).size());
        assertEquals("Should have two group principals", 2, subject.getPrincipals(GroupPrincipal.class).size());

        context.logout();

        assertEquals("Should have zero principals", 0, subject.getPrincipals().size());
    }

    public void testBadUseridLogin() throws Exception {
        LoginContext context = new LoginContext("PropertiesLogin", new UsernamePasswordCallbackHandler("nobody", "secret"));
        try {
            context.login();
            fail("Should have thrown a FailedLoginException");
        } catch (FailedLoginException doNothing) {
        }

    }

    public void testBadPWLogin() throws Exception {
        LoginContext context = new LoginContext("PropertiesLogin", new UsernamePasswordCallbackHandler("jonathan", "badpass"));
        try {
            context.login();
            fail("Should have thrown a FailedLoginException");
        } catch (FailedLoginException doNothing) {
        }

    }
}
