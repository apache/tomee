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
import javax.inject.Inject;
import javax.naming.ldap.LdapContext;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.security.enterprise.identitystore.LdapIdentityStoreDefinition;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

// todo create an LDAPtive version of it for pooling and other capabilities
@ApplicationScoped
public class TomEELDAPIdentityStore implements IdentityStore {

    @Inject
    private Supplier<LdapIdentityStoreDefinition> definitionSupplier;
    private LdapIdentityStoreDefinition definition;

    private Set<ValidationType> validationTypes;

    private LdapContext ldapContext;

    @PostConstruct
    private void init() throws Exception {
        definition = definitionSupplier.get();
        validationTypes = new HashSet<>(asList(definition.useFor()));
        ldapContext = lookup(definition.url(), definition.bindDn(), definition.bindDnPassword());
    }

    @Override
    public CredentialValidationResult validate(final Credential credential) {
        if (!(credential instanceof UsernamePasswordCredential)) {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }

        return CredentialValidationResult.INVALID_RESULT;
    }

    @Override
    public Set<String> getCallerGroups(final CredentialValidationResult validationResult) {
        return emptySet();
    }

    @Override
    public int priority() {
        return definition.priority();
    }

    @Override
    public Set<ValidationType> validationTypes() {
        return validationTypes;
    }

    public static LdapContext lookup(final String url, final String bindDn, final String bindDnPassword) {
        return null;
    }
}
