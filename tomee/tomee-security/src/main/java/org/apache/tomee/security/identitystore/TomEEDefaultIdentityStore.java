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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class TomEEDefaultIdentityStore implements IdentityStore {
    private UserDatabase userDatabase;

    @PostConstruct
    private void init() throws Exception {
        final StandardServer server = TomcatHelper.getServer();
        final NamingResourcesImpl resources = server.getGlobalNamingResources();
        final ContextResource userDataBaseResource = resources.findResource("UserDatabase");
        userDatabase = (UserDatabase) server.getGlobalNamingContext().lookup(userDataBaseResource.getName());
    }

    @Override
    public CredentialValidationResult validate(final Credential credential) {
        if (credential instanceof UsernamePasswordCredential) {
            final UsernamePasswordCredential usernamePasswordCredential = (UsernamePasswordCredential) credential;
            return Optional.ofNullable(userDatabase.findUser(usernamePasswordCredential.getCaller()))
                           .filter(user -> user.getPassword().equals(usernamePasswordCredential.getPasswordAsString()))
                           .map(user -> new CredentialValidationResult(user.getUsername(), getUserRoles(user)))
                           .orElse(CredentialValidationResult.INVALID_RESULT);
        }

        return CredentialValidationResult.NOT_VALIDATED_RESULT;
    }

    @Override
    public Set<String> getCallerGroups(final CredentialValidationResult validationResult) {
        return validationResult.getCallerGroups();
    }

    private Set<String> getUserRoles(final User user) {
        final Set<String> roles = new HashSet<>();
        user.getRoles().forEachRemaining(role -> roles.add(role.getRolename()));
        return roles;
    }
}
