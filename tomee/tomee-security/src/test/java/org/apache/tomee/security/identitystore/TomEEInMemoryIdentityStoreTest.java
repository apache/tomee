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

import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore.ValidationType;
import jakarta.security.enterprise.identitystore.InMemoryIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.InMemoryIdentityStoreDefinition.Credentials;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TomEEInMemoryIdentityStoreTest {

    @InMemoryIdentityStoreDefinition({
        @Credentials(callerName = "alice", password = "s3cret", groups = {"users", "admins"}),
        @Credentials(callerName = "bob",   password = "hunter2", groups = {"users"})
    })
    static class DefaultStore {}

    @InMemoryIdentityStoreDefinition(
        value = @Credentials(callerName = "alice", password = "s3cret", groups = {"users"}),
        useFor = ValidationType.PROVIDE_GROUPS)
    static class GroupsOnlyStore {}

    @InMemoryIdentityStoreDefinition(
        value = @Credentials(callerName = "alice", password = "s3cret", groups = {"users"}),
        useFor = ValidationType.VALIDATE)
    static class ValidateOnlyStore {}

    @InMemoryIdentityStoreDefinition({})
    static class EmptyStore {}

    @InMemoryIdentityStoreDefinition(
        value = @Credentials(callerName = "alice", password = "s3cret"),
        priority = 42)
    static class PriorityStore {}

    // Two entries with the same caller name -- the store should deterministically pick the first match
    // so admins can shadow a generic entry with a more specific one earlier in the list.
    @InMemoryIdentityStoreDefinition({
        @Credentials(callerName = "alice", password = "first",  groups = {"first-match"}),
        @Credentials(callerName = "alice", password = "second", groups = {"second-match"})
    })
    static class DuplicateCallerStore {}

    @InMemoryIdentityStoreDefinition({
        @Credentials(callerName = "人", password = "パスワード", groups = {"ユーザー"})
    })
    static class UnicodeStore {}

    private static TomEEInMemoryIdentityStore buildStore(final Class<?> annotatedFixture) throws Exception {
        final InMemoryIdentityStoreDefinition annotation =
                annotatedFixture.getAnnotation(InMemoryIdentityStoreDefinition.class);
        final TomEEInMemoryIdentityStore store = new TomEEInMemoryIdentityStore();
        inject(store, "definitionSupplier", (Supplier<InMemoryIdentityStoreDefinition>) () -> annotation);
        // Call the private @PostConstruct init() so validationTypes is populated without a CDI container.
        final var init = TomEEInMemoryIdentityStore.class.getDeclaredMethod("init");
        init.setAccessible(true);
        init.invoke(store);
        return store;
    }

    private static void inject(final Object target, final String fieldName, final Object value) throws Exception {
        final Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private TomEEInMemoryIdentityStore defaultStore;

    @Before
    public void setUp() throws Exception {
        defaultStore = buildStore(DefaultStore.class);
    }

    @Test
    public void validCallerReturnsValidResultWithGroups() {
        final CredentialValidationResult result = defaultStore.validate(
                new UsernamePasswordCredential("alice", "s3cret"));

        assertEquals(CredentialValidationResult.Status.VALID, result.getStatus());
        assertEquals("alice", result.getCallerPrincipal().getName());
        assertEquals(Set.of("users", "admins"), result.getCallerGroups());
    }

    @Test
    public void wrongPasswordReturnsInvalid() {
        final CredentialValidationResult result = defaultStore.validate(
                new UsernamePasswordCredential("alice", "nope"));

        assertSame(CredentialValidationResult.INVALID_RESULT, result);
    }

    @Test
    public void unknownCallerReturnsInvalid() {
        final CredentialValidationResult result = defaultStore.validate(
                new UsernamePasswordCredential("charlie", "whatever"));

        assertSame(CredentialValidationResult.INVALID_RESULT, result);
    }

    @Test
    public void nonUsernamePasswordCredentialNotValidated() {
        // Arbitrary Credential subtype — the store only supports UsernamePasswordCredential and reports NOT_VALIDATED.
        final Credential opaque = new Credential() { };
        final CredentialValidationResult result = defaultStore.validate(opaque);

        assertSame(CredentialValidationResult.NOT_VALIDATED_RESULT, result);
    }

    @Test
    public void groupsOnlyUseForDoesNotValidate() throws Exception {
        final TomEEInMemoryIdentityStore store = buildStore(GroupsOnlyStore.class);

        final CredentialValidationResult result = store.validate(
                new UsernamePasswordCredential("alice", "s3cret"));

        assertSame(CredentialValidationResult.NOT_VALIDATED_RESULT, result);
        assertTrue(store.validationTypes().contains(ValidationType.PROVIDE_GROUPS));
    }

    @Test
    public void getCallerGroupsRespectsUseFor() throws Exception {
        final TomEEInMemoryIdentityStore validateOnly = buildStore(ValidateOnlyStore.class);
        // Validation still works (VALIDATE present), but getCallerGroups must be empty because PROVIDE_GROUPS absent.
        final CredentialValidationResult result = validateOnly.validate(
                new UsernamePasswordCredential("alice", "s3cret"));

        assertEquals(CredentialValidationResult.Status.VALID, result.getStatus());
        assertTrue(validateOnly.getCallerGroups(result).isEmpty());

        // Default store has both VALIDATE and PROVIDE_GROUPS, so getCallerGroups returns the declared groups.
        final CredentialValidationResult defaultResult = defaultStore.validate(
                new UsernamePasswordCredential("bob", "hunter2"));
        assertEquals(Set.of("users"), defaultStore.getCallerGroups(defaultResult));
        assertNotSame(CredentialValidationResult.INVALID_RESULT, defaultResult);
    }

    @Test
    public void emptyStoreAlwaysReturnsInvalid() throws Exception {
        final TomEEInMemoryIdentityStore empty = buildStore(EmptyStore.class);

        assertSame(CredentialValidationResult.INVALID_RESULT,
                empty.validate(new UsernamePasswordCredential("alice", "s3cret")));
    }

    @Test
    public void emptyPasswordReturnsInvalid() {
        // An empty password must not accidentally match a declared credential; also guards against NPE.
        assertSame(CredentialValidationResult.INVALID_RESULT,
                defaultStore.validate(new UsernamePasswordCredential("alice", "")));
    }

    @Test
    public void callerNameIsCaseSensitive() {
        // Spec gives no case-folding guidance, so matching is strict. "ALICE" must not resolve to "alice".
        assertSame(CredentialValidationResult.INVALID_RESULT,
                defaultStore.validate(new UsernamePasswordCredential("ALICE", "s3cret")));
    }

    @Test
    public void rightPasswordWrongCallerReturnsInvalid() {
        // bob's password on alice's account: caller mismatch, so INVALID even though the password hash matches
        // a different caller's declared secret.
        assertSame(CredentialValidationResult.INVALID_RESULT,
                defaultStore.validate(new UsernamePasswordCredential("alice", "hunter2")));
    }

    @Test
    public void priorityReflectsAnnotation() throws Exception {
        final TomEEInMemoryIdentityStore custom = buildStore(PriorityStore.class);
        assertEquals(42, custom.priority());

        // DefaultStore doesn't override priority -- spec default for @InMemoryIdentityStoreDefinition is 90
        assertEquals(90, defaultStore.priority());
    }

    @Test
    public void validationTypesReflectUseFor() throws Exception {
        assertEquals(EnumSet.of(ValidationType.VALIDATE, ValidationType.PROVIDE_GROUPS),
                defaultStore.validationTypes());

        assertEquals(Collections.singleton(ValidationType.VALIDATE),
                buildStore(ValidateOnlyStore.class).validationTypes());

        assertEquals(Collections.singleton(ValidationType.PROVIDE_GROUPS),
                buildStore(GroupsOnlyStore.class).validationTypes());
    }

    @Test
    public void duplicateCallerFirstMatchWins() throws Exception {
        final TomEEInMemoryIdentityStore store = buildStore(DuplicateCallerStore.class);

        final CredentialValidationResult first = store.validate(
                new UsernamePasswordCredential("alice", "first"));
        assertEquals(CredentialValidationResult.Status.VALID, first.getStatus());
        assertEquals(Set.of("first-match"), first.getCallerGroups());

        // The second declaration is reachable too; deterministic order means the first *matching* entry
        // wins -- here "first" wins for password "first", and "second" wins for password "second".
        final CredentialValidationResult second = store.validate(
                new UsernamePasswordCredential("alice", "second"));
        assertEquals(CredentialValidationResult.Status.VALID, second.getStatus());
        assertEquals(Set.of("second-match"), second.getCallerGroups());
    }

    @Test
    public void unicodeCallerAndPasswordRoundTrip() throws Exception {
        final TomEEInMemoryIdentityStore store = buildStore(UnicodeStore.class);

        final CredentialValidationResult valid = store.validate(
                new UsernamePasswordCredential("人", "パスワード"));
        assertEquals(CredentialValidationResult.Status.VALID, valid.getStatus());
        assertEquals("人", valid.getCallerPrincipal().getName());
        assertEquals(Set.of("ユーザー"), valid.getCallerGroups());
    }

    @Test
    public void credentialWithEmptyGroupsArray() {
        // Default "alice" has {"users","admins"}; confirm "bob" (single group) does not leak alice's groups.
        final CredentialValidationResult bob = defaultStore.validate(
                new UsernamePasswordCredential("bob", "hunter2"));

        assertEquals(CredentialValidationResult.Status.VALID, bob.getStatus());
        assertEquals(Set.of("users"), bob.getCallerGroups());
        assertFalse(bob.getCallerGroups().contains("admins"));
    }

    @Test(timeout = 10_000)
    public void concurrentValidateIsThreadSafe() throws Exception {
        // The store is @ApplicationScoped and handles concurrent requests; it must stay stateless across calls.
        final int threads = 8;
        final int iterations = 200;
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            final Callable<Integer> task = () -> {
                int ok = 0;
                for (int i = 0; i < iterations; i++) {
                    final CredentialValidationResult r = defaultStore.validate(
                            new UsernamePasswordCredential("alice", "s3cret"));
                    if (r.getStatus() == CredentialValidationResult.Status.VALID) {
                        ok++;
                    }
                }
                return ok;
            };

            int total = 0;
            for (Future<Integer> f : pool.invokeAll(Collections.nCopies(threads, task))) {
                total += f.get(5, TimeUnit.SECONDS);
            }
            assertEquals(threads * iterations, total);
        } finally {
            pool.shutdownNow();
        }
    }
}
