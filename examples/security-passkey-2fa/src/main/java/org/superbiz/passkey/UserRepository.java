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

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class UserRepository {

    private static final Map<String, String> PASSWORDS = Map.of(
            "jon", "doe",
            "iron", "man");

    private static final Map<String, Set<String>> ROLES = Map.of(
            "jon", Set.of("user"),
            "iron", Set.of("user", "admin"));

    public boolean validatePassword(final String username, final String password) {
        final String expected = PASSWORDS.get(username);
        return expected != null && expected.equals(password);
    }

    public boolean exists(final String username) {
        return PASSWORDS.containsKey(username);
    }

    public Optional<Set<String>> roles(final String username) {
        return Optional.ofNullable(ROLES.get(username));
    }
}
