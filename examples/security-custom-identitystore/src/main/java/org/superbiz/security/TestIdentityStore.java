/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import java.util.Collections;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static jakarta.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;

@ApplicationScoped
public class TestIdentityStore implements IdentityStore {

    public CredentialValidationResult validate(Credential credential) {

        if (!(credential instanceof UsernamePasswordCredential)) {
            return INVALID_RESULT;
        }

        final UsernamePasswordCredential usernamePasswordCredential = (UsernamePasswordCredential) credential;
        if (usernamePasswordCredential.compareTo("jon", "doe")) {
            return new CredentialValidationResult("jon", new HashSet<>(asList("foo", "bar")));
        }

        if (usernamePasswordCredential.compareTo("iron", "man")) {
            return new CredentialValidationResult("iron", new HashSet<>(Collections.singletonList("avengers")));
        }

        return INVALID_RESULT;
    }

}