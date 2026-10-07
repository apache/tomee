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

import org.apache.catalina.Container;
import org.apache.catalina.CredentialHandler;
import org.apache.catalina.Engine;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.realm.CombinedRealm;
import org.apache.catalina.realm.UserDatabaseRealm;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Collections.emptySet;

@ApplicationScoped
public class TomEEDefaultIdentityStore implements IdentityStore {

    private static final Logger LOGGER = Logger.getInstance(LogCategory.TOMEE_SECURITY, TomEEDefaultIdentityStore.class);

    @Inject
    private Supplier<TomcatUserIdentityStoreDefinition> definitionSupplier;
    private TomcatUserIdentityStoreDefinition definition;

    private UserDatabase userDatabase;

    // null means no UserDatabaseRealm is configured for the resource: passwords are compared as plain text
    private CredentialHandler credentialHandler;

    @PostConstruct
    private void init() throws Exception {
        definition = definitionSupplier.get();

        final StandardServer server = TomcatHelper.getServer();
        final NamingResourcesImpl resources = server.getGlobalNamingResources();
        final ContextResource userDataBaseResource = resources.findResource(definition.resource());
        userDatabase = (UserDatabase) server.getGlobalNamingContext().lookup(userDataBaseResource.getName());

        credentialHandler = findCredentialHandler(server, definition.resource());
        if (credentialHandler == null) {
            LOGGER.warning("No UserDatabaseRealm found for resource '" + definition.resource()
                    + "', passwords are compared as plain text. To use digested passwords configure a UserDatabaseRealm"
                    + " with a CredentialHandler for this resource in server.xml");
        }
    }

    /**
     * Looks up the credential handler of the {@link UserDatabaseRealm} (possibly nested in a
     * {@link CombinedRealm} or {@link org.apache.catalina.realm.LockOutRealm}) of an engine or host
     * that is backed by the given global user database resource.
     */
    static CredentialHandler findCredentialHandler(final Server server, final String resourceName) {
        for (final Service service : server.findServices()) {
            final Engine engine = service.getContainer();
            if (engine == null) {
                continue;
            }

            CredentialHandler handler = findCredentialHandler(engine.getRealm(), resourceName);
            if (handler != null) {
                return handler;
            }

            for (final Container host : engine.findChildren()) {
                handler = findCredentialHandler(host.getRealm(), resourceName);
                if (handler != null) {
                    return handler;
                }
            }
        }
        return null;
    }

    private static CredentialHandler findCredentialHandler(final Realm realm, final String resourceName) {
        if (realm instanceof CombinedRealm combinedRealm) {
            for (final Realm nested : combinedRealm.getNestedRealms()) {
                final CredentialHandler handler = findCredentialHandler(nested, resourceName);
                if (handler != null) {
                    return handler;
                }
            }
            return null;
        }

        if (realm instanceof UserDatabaseRealm userDatabaseRealm
                && !userDatabaseRealm.getLocalJndiResource()
                && resourceName.equals(userDatabaseRealm.getResourceName())) {
            return userDatabaseRealm.getCredentialHandler();
        }
        return null;
    }

    @Override
    public CredentialValidationResult validate(final Credential credential) {
        if (!(credential instanceof UsernamePasswordCredential usernamePasswordCredential)) {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }

        final User user = getUser(usernamePasswordCredential.getCaller());

        if (user == null) {
            return CredentialValidationResult.INVALID_RESULT;
        }

        if (!passwordMatches(user.getPassword(), usernamePasswordCredential.getPasswordAsString())) {
            return CredentialValidationResult.INVALID_RESULT;
        }

        Set<String> groups = emptySet();
        if (validationTypes().contains(ValidationType.PROVIDE_GROUPS)) {
            groups = new HashSet<>(getUserRoles(user));
        }

        return new CredentialValidationResult(usernamePasswordCredential.getCaller(), groups);
    }

    private boolean passwordMatches(final String stored, final String supplied) {
        if (stored == null || supplied == null) {
            return false;
        }
        if (credentialHandler != null) {
            return credentialHandler.matches(supplied, stored);
        }
        return MessageDigest.isEqual(stored.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
    }

    private User getUser(final String callerPrincipal) {
        return userDatabase.findUser(callerPrincipal);
    }

    @Override
    public Set<String> getCallerGroups(final CredentialValidationResult validationResult) {
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
