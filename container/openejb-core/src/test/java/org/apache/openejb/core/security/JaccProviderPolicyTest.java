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
package org.apache.openejb.core.security;

import jakarta.security.jacc.Policy;
import jakarta.security.jacc.PolicyConfiguration;
import jakarta.security.jacc.PolicyContext;
import org.apache.openejb.core.security.jacc.BasicJaccProvider;
import org.apache.openejb.core.security.jacc.BasicPolicyConfiguration;
import org.apache.openejb.loader.SystemInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.security.auth.Subject;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JaccProviderPolicyTest {

    private static final String CONTEXT_ID = "JaccProviderPolicyTest";

    private JaccProvider previousProvider;
    private BasicPolicyConfiguration.RoleResolver previousRoleResolver;
    private BasicJaccProvider provider;
    private Policy policy;
    private String previousContextId;

    @Before
    public void setUp() throws Exception {
        previousProvider = JaccProvider.get();
        previousRoleResolver = SystemInstance.get().getComponent(BasicPolicyConfiguration.RoleResolver.class);
        previousContextId = PolicyContext.getContextID();

        provider = new BasicJaccProvider();
        JaccProvider.set(provider);
        SystemInstance.get().setComponent(BasicPolicyConfiguration.RoleResolver.class,
                (principals, roles) -> principals.length == 0 ? Collections.emptySet() : new HashSet<>(roles));
        PolicyContext.setContextID(CONTEXT_ID);
        policy = new JaccProvider.PolicyFactory().getPolicy(CONTEXT_ID);
    }

    @After
    public void tearDown() {
        PolicyContext.setContextID(previousContextId);
        JaccProvider.set(previousProvider);
        if (previousRoleResolver == null) {
            SystemInstance.get().removeComponent(BasicPolicyConfiguration.RoleResolver.class);
        } else {
            SystemInstance.get().setComponent(BasicPolicyConfiguration.RoleResolver.class, previousRoleResolver);
        }
    }

    @Test
    public void onlyInServiceConfigurationParticipatesInDecisions() throws Exception {
        final Permission excluded = new RuntimePermission("excluded");
        final Permission unchecked = new RuntimePermission("unchecked");
        final Permission byRole = new RuntimePermission("byRole");
        final Subject subject = subject("caller");

        final PolicyConfiguration configuration = provider.getPolicyConfiguration(CONTEXT_ID, true);
        configuration.addToExcludedPolicy(excluded);
        configuration.addToUncheckedPolicy(unchecked);
        configuration.addToRole("user", byRole);

        assertConfigurationHidden(excluded, unchecked, byRole, subject);

        configuration.commit();
        assertTrue(provider.inService(CONTEXT_ID));
        assertTrue(policy.isExcluded(excluded));
        assertTrue(policy.isUnchecked(unchecked));
        assertTrue(policy.impliesByRole(byRole, subject));
        assertTrue(policy.getPermissionCollection(subject).implies(unchecked));
        assertTrue(policy.getPermissionCollection(subject).implies(byRole));

        provider.getPolicyConfiguration(CONTEXT_ID, false);
        assertFalse(provider.inService(CONTEXT_ID));
        assertConfigurationHidden(excluded, unchecked, byRole, subject);

        configuration.commit();
        configuration.delete();
        assertConfigurationHidden(excluded, unchecked, byRole, subject);
    }

    @Test
    public void exclusionsTakePrecedenceInPermissionCollection() throws Exception {
        final PolicyConfiguration configuration = provider.getPolicyConfiguration(CONTEXT_ID, true);
        configuration.addToUncheckedPolicy(new RuntimePermission("unchecked.*"));
        configuration.addToRole("user", new RuntimePermission("role.*"));
        configuration.addToExcludedPolicy(new RuntimePermission("unchecked.blocked"));
        configuration.addToExcludedPolicy(new RuntimePermission("role.blocked"));
        configuration.commit();

        final PermissionCollection permissions = policy.getPermissionCollection(subject("caller"));
        assertTrue(permissions.implies(new RuntimePermission("unchecked.allowed")));
        assertTrue(permissions.implies(new RuntimePermission("role.allowed")));
        assertFalse(permissions.implies(new RuntimePermission("unchecked.blocked")));
        assertFalse(permissions.implies(new RuntimePermission("role.blocked")));
    }

    private void assertConfigurationHidden(final Permission excluded, final Permission unchecked,
                                           final Permission byRole, final Subject subject) {
        assertFalse(policy.isExcluded(excluded));
        assertFalse(policy.isUnchecked(unchecked));
        assertFalse(policy.impliesByRole(byRole, subject));
        assertFalse(policy.getPermissionCollection(subject).implies(unchecked));
        assertFalse(policy.getPermissionCollection(subject).implies(byRole));
    }

    private static Subject subject(final String name) {
        final Set<Principal> principals = new HashSet<>();
        principals.add(() -> name);
        return new Subject(true, principals, Collections.emptySet(), Collections.emptySet());
    }
}
