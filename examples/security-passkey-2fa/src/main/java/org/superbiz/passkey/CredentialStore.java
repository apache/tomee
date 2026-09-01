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
package org.superbiz.passkey;

import com.webauthn4j.credential.CredentialRecord;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class CredentialStore {

    public static final class Entry {
        private final String username;
        private final byte[] credentialId;
        private CredentialRecord record;

        private Entry(final String username, final byte[] credentialId, final CredentialRecord record) {
            this.username = username;
            this.credentialId = credentialId;
            this.record = record;
        }

        public String getUsername() {
            return username;
        }

        public byte[] getCredentialId() {
            return credentialId;
        }

        public CredentialRecord getRecord() {
            return record;
        }
    }

    private final Map<String, Entry> byCredentialId = new ConcurrentHashMap<>();

    public void save(final String username, final byte[] credentialId, final CredentialRecord record) {
        byCredentialId.put(key(credentialId), new Entry(username, credentialId, record));
    }

    public Optional<Entry> find(final byte[] credentialId) {
        return Optional.ofNullable(byCredentialId.get(key(credentialId)));
    }

    public List<byte[]> credentialIds(final String username) {
        final List<byte[]> ids = new ArrayList<>();
        for (final Entry entry : byCredentialId.values()) {
            if (entry.username.equals(username)) {
                ids.add(entry.credentialId);
            }
        }
        return ids;
    }

    public boolean hasCredentials(final String username) {
        return !credentialIds(username).isEmpty();
    }

    public void updateRecord(final byte[] credentialId, final CredentialRecord updated) {
        final Entry entry = byCredentialId.get(key(credentialId));
        if (entry != null) {
            entry.record = updated;
        }
    }

    private static String key(final byte[] credentialId) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(credentialId);
    }
}
