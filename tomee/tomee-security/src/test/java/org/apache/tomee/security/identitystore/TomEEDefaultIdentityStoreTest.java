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
package org.apache.tomee.security.identitystore;

import org.apache.catalina.CredentialHandler;
import org.apache.catalina.Realm;
import org.apache.catalina.User;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.realm.LockOutRealm;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.catalina.realm.MessageDigestCredentialHandler;
import org.apache.catalina.realm.UserDatabaseRealm;
import org.apache.catalina.users.MemoryUserDatabase;
import org.junit.Before;
import org.junit.Test;

import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class TomEEDefaultIdentityStoreTest {

    private MemoryUserDatabase database;
    private TomEEDefaultIdentityStore store;

    @Before
    public void setUp() throws Exception {
        database = new MemoryUserDatabase("test");
        final User alice = database.createUser("alice", "s3cret", null);
        alice.addRole(database.createRole("admin", null));

        store = new TomEEDefaultIdentityStore();
        setField("userDatabase", database);
    }

    @Test
    public void validPassword() {
        final CredentialValidationResult result = store.validate(new UsernamePasswordCredential("alice", "s3cret"));

        assertEquals(CredentialValidationResult.Status.VALID, result.getStatus());
        assertEquals("alice", result.getCallerPrincipal().getName());
        assertEquals(Set.of("admin"), result.getCallerGroups());
    }

    @Test
    public void wrongPassword() {
        assertInvalid(store.validate(new UsernamePasswordCredential("alice", "wrong")));
        assertInvalid(store.validate(new UsernamePasswordCredential("alice", "s3cre")));
        assertInvalid(store.validate(new UsernamePasswordCredential("alice", "s3cret ")));
        assertInvalid(store.validate(new UsernamePasswordCredential("alice", "")));
    }

    @Test
    public void plainTextCredentialHandler() throws Exception {
        setField("credentialHandler", new MessageDigestCredentialHandler());

        assertEquals(CredentialValidationResult.Status.VALID,
                store.validate(new UsernamePasswordCredential("alice", "s3cret")).getStatus());
        assertInvalid(store.validate(new UsernamePasswordCredential("alice", "wrong")));
    }

    @Test
    public void digestedPassword() throws Exception {
        final MessageDigestCredentialHandler handler = new MessageDigestCredentialHandler();
        handler.setAlgorithm("SHA-256");
        final String digest = handler.mutate("s3cret");
        database.createUser("carol", digest, null);
        setField("credentialHandler", handler);

        final CredentialValidationResult result = store.validate(new UsernamePasswordCredential("carol", "s3cret"));
        assertEquals(CredentialValidationResult.Status.VALID, result.getStatus());
        assertEquals("carol", result.getCallerPrincipal().getName());

        assertInvalid(store.validate(new UsernamePasswordCredential("carol", digest)));
        assertInvalid(store.validate(new UsernamePasswordCredential("carol", "wrong")));
    }

    @Test
    public void saltedDigestedPassword() throws Exception {
        final MessageDigestCredentialHandler handler = new MessageDigestCredentialHandler();
        handler.setAlgorithm("SHA-256");
        handler.setSaltLength(16);
        handler.setIterations(100);
        final String digest = handler.mutate("s3cret");
        database.createUser("carol", digest, null);
        setField("credentialHandler", handler);

        assertEquals(CredentialValidationResult.Status.VALID,
                store.validate(new UsernamePasswordCredential("carol", "s3cret")).getStatus());
        assertInvalid(store.validate(new UsernamePasswordCredential("carol", digest)));
    }

    @Test
    public void unknownUser() {
        assertEquals(CredentialValidationResult.INVALID_RESULT,
                store.validate(new UsernamePasswordCredential("bob", "s3cret")));
    }

    @Test
    public void nullStoredPassword() throws Exception {
        database.createUser("nopass", null, null);

        assertInvalid(store.validate(new UsernamePasswordCredential("nopass", "")));
        assertInvalid(store.validate(new UsernamePasswordCredential("nopass", "anything")));

        setField("credentialHandler", new MessageDigestCredentialHandler());
        assertInvalid(store.validate(new UsernamePasswordCredential("nopass", "")));
        assertInvalid(store.validate(new UsernamePasswordCredential("nopass", "anything")));
    }

    @Test
    public void unsupportedCredential() {
        assertEquals(CredentialValidationResult.NOT_VALIDATED_RESULT, store.validate(new Credential() {
        }));
    }

    @Test
    public void credentialHandlerOfEngineRealm() {
        final CredentialHandler handler = new MessageDigestCredentialHandler();
        final UserDatabaseRealm realm = userDatabaseRealm("UserDatabase", handler);
        final StandardServer server = server(realm, null);

        assertSame(handler, TomEEDefaultIdentityStore.findCredentialHandler(server, "UserDatabase"));
        assertNull(TomEEDefaultIdentityStore.findCredentialHandler(server, "OtherDatabase"));
    }

    @Test
    public void credentialHandlerOfNestedRealm() {
        final CredentialHandler handler = new MessageDigestCredentialHandler();
        final CredentialHandler other = new MessageDigestCredentialHandler();
        final LockOutRealm lockOut = new LockOutRealm();
        lockOut.addRealm(new MemoryRealm());
        lockOut.addRealm(userDatabaseRealm("OtherDatabase", other));
        lockOut.addRealm(userDatabaseRealm("UserDatabase", handler));

        final StandardServer server = server(lockOut, null);

        assertSame(handler, TomEEDefaultIdentityStore.findCredentialHandler(server, "UserDatabase"));
        assertSame(other, TomEEDefaultIdentityStore.findCredentialHandler(server, "OtherDatabase"));
    }

    @Test
    public void credentialHandlerOfHostRealm() {
        final CredentialHandler handler = new MessageDigestCredentialHandler();
        final StandardServer server = server(new MemoryRealm(), userDatabaseRealm("UserDatabase", handler));

        assertSame(handler, TomEEDefaultIdentityStore.findCredentialHandler(server, "UserDatabase"));
    }

    @Test
    public void noRealmForResource() {
        final UserDatabaseRealm local = userDatabaseRealm("UserDatabase", new MessageDigestCredentialHandler());
        local.setLocalJndiResource(true);

        assertNull(TomEEDefaultIdentityStore.findCredentialHandler(server(null, null), "UserDatabase"));
        assertNull(TomEEDefaultIdentityStore.findCredentialHandler(server(new MemoryRealm(), null), "UserDatabase"));
        assertNull(TomEEDefaultIdentityStore.findCredentialHandler(server(local, null), "UserDatabase"));
    }

    private static UserDatabaseRealm userDatabaseRealm(final String resourceName, final CredentialHandler handler) {
        final UserDatabaseRealm realm = new UserDatabaseRealm();
        realm.setResourceName(resourceName);
        realm.setCredentialHandler(handler);
        return realm;
    }

    private static StandardServer server(final Realm engineRealm, final Realm hostRealm) {
        final StandardEngine engine = new StandardEngine();
        engine.setName("Catalina");
        engine.setRealm(engineRealm);

        final StandardHost host = new StandardHost();
        host.setName("localhost");
        host.setRealm(hostRealm);
        engine.addChild(host);

        final StandardService service = new StandardService();
        service.setContainer(engine);

        final StandardServer server = new StandardServer();
        server.addService(service);
        return server;
    }

    private void setField(final String name, final Object value) throws Exception {
        final Field field = TomEEDefaultIdentityStore.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(store, value);
    }

    private static void assertInvalid(final CredentialValidationResult result) {
        assertEquals(CredentialValidationResult.Status.INVALID, result.getStatus());
    }
}
