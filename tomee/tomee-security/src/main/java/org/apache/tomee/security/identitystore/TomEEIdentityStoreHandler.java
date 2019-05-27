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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.security.enterprise.identitystore.IdentityStoreHandler;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;
import static javax.security.enterprise.identitystore.CredentialValidationResult.NOT_VALIDATED_RESULT;
import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.PROVIDE_GROUPS;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.VALIDATE;

@ApplicationScoped
public class TomEEIdentityStoreHandler implements IdentityStoreHandler {
    @Inject
    private Instance<IdentityStore> identityStores;

    private List<IdentityStore> authenticationStores;
    private List<IdentityStore> authorizationStores;

    @PostConstruct
    private void init() {
        authenticationStores =
                identityStores.stream()
                              .filter(i -> i.validationTypes().contains(VALIDATE))
                              .sorted(Comparator.comparing(IdentityStore::priority))
                              .collect(Collectors.toList());

        authorizationStores =
                identityStores.stream()
                              .filter(i -> i.validationTypes().contains(PROVIDE_GROUPS))
                              .filter(i -> !i.validationTypes().contains(VALIDATE))
                              .sorted(Comparator.comparing(IdentityStore::priority))
                              .collect(Collectors.toList());
    }

    @Override
    public CredentialValidationResult validate(final Credential credential) {
        if (authenticationStores.isEmpty()) {
            return NOT_VALIDATED_RESULT;
        }

        CredentialValidationResult validationResult = null;
        IdentityStore authorizedStore = null;
        for (final IdentityStore identityStore : identityStores) {
            validationResult = identityStore.validate(credential);
            if (validationResult.getStatus().equals(VALID)) {
                authorizedStore = identityStore;
                break;
            }
        }

        if (authorizedStore == null) {
            return INVALID_RESULT;
        }

        final Set<String> groups = new HashSet<>();
        if (authorizedStore.validationTypes().contains(PROVIDE_GROUPS)) {
            groups.addAll(authorizedStore.getCallerGroups(validationResult));
        }

        final CredentialValidationResult authorizedValidationResult = validationResult;
        final Set<String> additionalGroups =
                authorizationStores.stream()
                                   .map(as -> as.getCallerGroups(authorizedValidationResult))
                                   .flatMap(Collection::stream)
                                   .collect(Collectors.toSet());
        groups.addAll(additionalGroups);

        return new CredentialValidationResult(authorizedValidationResult.getIdentityStoreId(),
                                              authorizedValidationResult.getCallerPrincipal(),
                                              authorizedValidationResult.getCallerDn(),
                                              authorizedValidationResult.getCallerUniqueId(),
                                              groups);
    }
}
