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

import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomee.loader.TomcatHelper;
import org.apache.tomee.security.cdi.TomcatUserIdentityStoreDefinition;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import jakarta.security.enterprise.identitystore.IdentityStorePermission;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Collections.emptySet;

@ApplicationScoped
public class TomEEDefaultIdentityStore implements IdentityStore {

    @Inject
    private Supplier<TomcatUserIdentityStoreDefinition> definitionSupplier;
    private TomcatUserIdentityStoreDefinition definition;

    private UserDatabase userDatabase;

    @PostConstruct
    private void init() throws Exception {
        definition = definitionSupplier.get();

        final StandardServer server = TomcatHelper.getServer();
        final NamingResourcesImpl resources = server.getGlobalNamingResources();
        final ContextResource userDataBaseResource = resources.findResource(definition.resource());
        userDatabase = (UserDatabase) server.getGlobalNamingContext().lookup(userDataBaseResource.getName());
    }

    @Override
    public CredentialValidationResult validate(final Credential credential) {
        if (!(credential instanceof UsernamePasswordCredential)) {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }

        final UsernamePasswordCredential usernamePasswordCredential = (UsernamePasswordCredential) credential;
        final User user = getUser(usernamePasswordCredential.getCaller());

        if (user == null) {
            return CredentialValidationResult.INVALID_RESULT;
        }

        // deal with hashed passwords in tomcat-users.xml
        if (user.getPassword().equals(usernamePasswordCredential.getPasswordAsString())) {
            Set<String> groups = emptySet();
            if (validationTypes().contains(ValidationType.PROVIDE_GROUPS)) {
                groups = new HashSet<>(getUserRoles(user));
            }

            return new CredentialValidationResult(usernamePasswordCredential.getCaller(), groups);
        }

        return CredentialValidationResult.NOT_VALIDATED_RESULT;
    }

    private User getUser(final String callerPrincipal) {
        return userDatabase.findUser(callerPrincipal);
    }

    @Override
    public Set<String> getCallerGroups(final CredentialValidationResult validationResult) {

        final SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(new IdentityStorePermission("getGroups"));
        }

        final User user = getUser(validationResult.getCallerPrincipal().getName());
        return getUserRoles(user);
    }

    private Set<String> getUserRoles(final User user) {
        if (user == null) {
            return emptySet();
        }
        final Set<String> roles = new HashSet<>();
        user.getRoles().forEachRemaining(role -> roles.add(role.getRolename()));
        return roles;
    }
}
