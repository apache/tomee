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

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import jakarta.security.enterprise.identitystore.InMemoryIdentityStoreDefinition;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Collections.emptySet;

/**
 * Jakarta Security 4.0 {@link InMemoryIdentityStoreDefinition}
 * implementation. Credentials are declared directly on the annotation and matched with constant-time byte
 * comparison (both password and caller name) to avoid leaking timing information about which callers are
 * present.
 */
@ApplicationScoped
public class TomEEInMemoryIdentityStore implements IdentityStore {

    @Inject
    private Supplier<InMemoryIdentityStoreDefinition> definitionSupplier;

    private InMemoryIdentityStoreDefinition definition;
    private Set<ValidationType> validationTypes;

    @PostConstruct
    private void init() {
        definition = definitionSupplier.get();
        validationTypes = Set.copyOf(Arrays.asList(definition.useFor()));
    }

    @Override
    public CredentialValidationResult validate(final Credential credential) {
        if (!(credential instanceof UsernamePasswordCredential usernamePasswordCredential)) {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }

        // Spec: if VALIDATE is not declared, this store should not authenticate callers.
        if (!validationTypes.contains(ValidationType.VALIDATE)) {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }

        final char[] suppliedPasswordChars = usernamePasswordCredential.getPassword().getValue();
        // Encode char[] -> UTF-8 bytes without going through String, so the plaintext
        // password never gets interned/retained outside of a scrubbable buffer.
        final byte[] suppliedPassword = toUtf8Bytes(suppliedPasswordChars);
        final byte[] suppliedCaller = toUtf8Bytes(usernamePasswordCredential.getCaller());

        try {
            InMemoryIdentityStoreDefinition.Credentials match = null;
            // Walk every entry once, regardless of a hit, so runtime stays roughly independent
            // of which (or how many) callers are declared and where the match occurs.
            for (final InMemoryIdentityStoreDefinition.Credentials entry : definition.value()) {
                final byte[] declaredPassword = entry.password().getBytes(StandardCharsets.UTF_8);
                final byte[] declaredCaller = entry.callerName().getBytes(StandardCharsets.UTF_8);
                // MessageDigest.isEqual is time-constant in the length of its second argument, so
                // both comparisons leak at most the declared (public, annotation-known) lengths.
                final boolean passwordMatches = MessageDigest.isEqual(declaredPassword, suppliedPassword);
                final boolean callerMatches = MessageDigest.isEqual(declaredCaller, suppliedCaller);
                if (passwordMatches && callerMatches && match == null) {
                    match = entry;
                }
            }

            if (match == null) {
                return CredentialValidationResult.INVALID_RESULT;
            }

            final Set<String> groups = new HashSet<>(Arrays.asList(match.groups()));
            return new CredentialValidationResult(match.callerName(), groups);
        } finally {
            Arrays.fill(suppliedPassword, (byte) 0);
        }
    }

    @Override
    public Set<String> getCallerGroups(final CredentialValidationResult validationResult) {
        if (!validationTypes.contains(ValidationType.PROVIDE_GROUPS)) {
            return emptySet();
        }
        return validationResult.getCallerGroups();
    }

    @Override
    public int priority() {
        return definition.priority();
    }

    @Override
    public Set<ValidationType> validationTypes() {
        return validationTypes;
    }

    private static byte[] toUtf8Bytes(final char[] chars) {
        if (chars == null || chars.length == 0) {
            return new byte[0];
        }
        // Direct char[] -> UTF-8 bytes via the charset encoder, bypassing String so the
        // plaintext never lands in the String pool or a long-lived heap slot.
        final ByteBuffer buffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        final byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    private static byte[] toUtf8Bytes(final String value) {
        if (value == null) {
            return new byte[0];
        }
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
