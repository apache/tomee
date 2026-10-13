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
package org.apache.openejb.arquillian.tests.security;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The user database holds SHA-256 digests and is bound to a UserDatabaseRealm with a
 * MessageDigestCredentialHandler (nested in a LockOutRealm), so the identity store has to
 * verify the cleartext password against the digest.
 */
@RunWith(Arquillian.class)
public class TomcatUserIdentityStoreDigestTest extends AbstractTomcatUserIdentityStoreTest {

    private static final String DIGEST = "8b5a996ea6720136098b2ef354a195b696c56782e99b30407fe753a3beb559e0";

    @Deployment(testable = false)
    public static WebArchive app() {
        return war("digest", DigestUserDatabaseConfig.class);
    }

    @Test
    public void cleartextPassword() throws Exception {
        assertAuthenticated("digest", "digest-secret");
    }

    @Test
    public void storedDigestIsNotAPassword() throws Exception {
        assertUnauthorized("digest", DIGEST);
        assertUnauthorized("digest", DIGEST.toUpperCase());
    }

    @Test
    public void wrongPassword() throws Exception {
        assertUnauthorized("digest", "wrong");
        assertUnauthorized("digest", "");
    }

    @Test
    public void unknownUser() throws Exception {
        assertUnauthorized("plain", "plain-secret");
    }

    @Test
    public void noCredentials() throws Exception {
        assertUnauthorized(null, null);
    }
}
