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

import jakarta.security.jacc.PolicyConfiguration;
import jakarta.security.jacc.PolicyConfigurationFactory;
import jakarta.security.jacc.PolicyContextException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.security.Permissions;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.security.auth.Subject;

import org.apache.openejb.core.security.jacc.BasicPolicyConfiguration;
import org.apache.openejb.loader.SystemInstance;

/**
 * @version $Rev$ $Date$
 */
public abstract class JaccProvider {

    private static final String FACTORY_NAME = JaccProvider.class.getName();
    private static final String DEFAULT_CONTEXT_ID = "";
    private static final String BOOTSTRAP_CONTEXT_ID = "openejb";
    private static final Map<String, jakarta.security.jacc.Policy> POLICIES = new ConcurrentHashMap<>();
    private static final jakarta.security.jacc.Policy DEFAULT_POLICY = new DefaultPolicy();
    private static JaccProvider jaccProvider;

    public static JaccProvider get() {
        return jaccProvider;
    }

    public static void set(final JaccProvider provider) {
        // todo add a security check
        jaccProvider = provider;
    }

    /**
     * This static method uses a system property to find and instantiate (via a
     * public constructor) a provider specific factory implementation class.
     * The name of the provider specific factory implementation class is
     * obtained from the value of the system property,<p>
     * <code>org.apache.openejb.security.JaccProvider</code>.
     * PolicyConfigurationFactory implementation class.
     *
     * @throws ClassNotFoundException when the class named by the system
     *                                property could not be found including because the value of the system
     *                                property has not be set.
     * @throws PolicyContextException if the implementation throws a checked
     *                                exception that has not been accounted for by the
     *                                getPolicyConfigurationFactory method signature. The exception thrown by
     *                                the implementation class will be encapsulated (during construction) in
     *                                the thrown PolicyContextException
     */
    public static void install() throws ClassNotFoundException, PolicyContextException {
        if (jaccProvider != null) {
            return;
        }

        final String[] factoryClassName = {null};
        try {
            jaccProvider = (JaccProvider) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    factoryClassName[0] = System.getProperty(FACTORY_NAME);

                    if (factoryClassName[0] == null) {
                        throw new ClassNotFoundException("Property " + FACTORY_NAME + " not set");
                    }
                    final Thread currentThread = Thread.currentThread();
                    final ClassLoader tccl = currentThread.getContextClassLoader();
                    return Class.forName(factoryClassName[0], true, tccl).newInstance();
                }
            });
        } catch (final PrivilegedActionException pae) {
            if (pae.getException() instanceof ClassNotFoundException) {
                throw (ClassNotFoundException) pae.getException();
            } else if (pae.getException() instanceof InstantiationException) {
                throw new ClassNotFoundException(factoryClassName[0] + " could not be instantiated");
            } else if (pae.getException() instanceof IllegalAccessException) {
                throw new ClassNotFoundException("Illegal access to " + factoryClassName);
            }
            throw new PolicyContextException(pae.getException());
        }
    }


    public static class Factory extends PolicyConfigurationFactory {
        public Factory() throws PolicyContextException, ClassNotFoundException {
            install();
        }

        public PolicyConfiguration getPolicyConfiguration(final String contextID, final boolean remove) throws PolicyContextException {
            return JaccProvider.get().getPolicyConfiguration(contextID, remove);
        }

        @Override
        public PolicyConfiguration getPolicyConfiguration(final String contextID) {
            return JaccProvider.get().getPolicyConfiguration(contextID);
        }

        @Override
        public PolicyConfiguration getPolicyConfiguration() {
            return get().getPolicyConfiguration();
        }

        public boolean inService(final String contextID) throws PolicyContextException {
            return JaccProvider.get().inService(contextID);
        }
    }

    public static class PolicyFactory extends jakarta.security.jacc.PolicyFactory {
        public PolicyFactory() throws PolicyContextException, ClassNotFoundException {
            install();
        }

        @Override
        public jakarta.security.jacc.Policy getPolicy(final String contextID) {
            final String key = normalizeContextID(contextID);
            final jakarta.security.jacc.Policy contextPolicy = POLICIES.get(key);
            if (contextPolicy != null) {
                return contextPolicy;
            }

            final jakarta.security.jacc.Policy defaultContextPolicy = POLICIES.get(DEFAULT_CONTEXT_ID);
            if (defaultContextPolicy != null) {
                return defaultContextPolicy;
            }

            // During early web-app bootstrap the context id can still be "openejb",
            // while request-time checks use the Catalina web context id.
            final jakarta.security.jacc.Policy bootstrapPolicy = POLICIES.get(BOOTSTRAP_CONTEXT_ID);
            if (bootstrapPolicy != null) {
                return bootstrapPolicy;
            }

            if (POLICIES.size() == 1) {
                return POLICIES.values().iterator().next();
            }

            return DEFAULT_POLICY;
        }

        @Override
        public void setPolicy(final String contextID, final jakarta.security.jacc.Policy policy) {
            final String key = normalizeContextID(contextID);
            if (policy == null) {
                POLICIES.remove(key);
            } else {
                POLICIES.put(key, policy);
            }
        }
    }

    public static boolean isSentinelPolicy(final jakarta.security.jacc.Policy policy) {
        return policy instanceof DefaultPolicy;
    }

    private static final class DefaultPolicy implements jakarta.security.jacc.Policy {
        boolean isSentinel() {
            return true;
        }

        @Override
        public boolean implies(final Permission permissionToBeChecked, final Subject subject) {
            final JaccProvider provider = get();
            if (provider == null) {
                return false;
            }

            final Principal[] principals = subject == null ? new Principal[0] : subject.getPrincipals().toArray(new Principal[0]);
            final ProtectionDomain protectionDomain = new ProtectionDomain(
                    new CodeSource(null, (Certificate[]) null), null, null, principals);

            return provider.implies(protectionDomain, permissionToBeChecked);
        }

        @Override
        public boolean isExcluded(final Permission permission) {
            final PolicyConfiguration configuration = currentConfiguration();
            if (configuration == null) {
                return false;
            }
            final PermissionCollection excluded = configuration.getExcludedPermissions();
            return excluded != null && excluded.implies(permission);
        }

        @Override
        public boolean isUnchecked(final Permission permission) {
            final PolicyConfiguration configuration = currentConfiguration();
            if (configuration == null) {
                return false;
            }
            final PermissionCollection unchecked = configuration.getUncheckedPermissions();
            return unchecked != null && unchecked.implies(permission);
        }

        @Override
        public boolean impliesByRole(final Permission permission, final Subject subject) {
            final PolicyConfiguration configuration = currentConfiguration();
            if (configuration == null || subject == null) {
                return false;
            }

            final Map<String, PermissionCollection> perRolePermissions = configuration.getPerRolePermissions();
            for (final String role : rolesOf(subject, perRolePermissions.keySet())) {
                final PermissionCollection permissions = perRolePermissions.get(role);
                if (permissions != null && permissions.implies(permission)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public PermissionCollection getPermissionCollection(final Subject subject) {
            final PolicyConfiguration configuration = currentConfiguration();
            if (configuration == null) {
                return new Permissions();
            }

            final PermissionCollection excluded = copyOf(configuration.getExcludedPermissions());
            final PermissionCollection permissions = new ExcludingPermissionCollection(excluded);

            addAll(permissions, configuration.getUncheckedPermissions());

            final Map<String, PermissionCollection> perRolePermissions = configuration.getPerRolePermissions();
            if (subject != null) {
                for (final String role : rolesOf(subject, perRolePermissions.keySet())) {
                    addAll(permissions, perRolePermissions.get(role));
                }
            }
            permissions.setReadOnly();
            return permissions;
        }

        private static Set<String> rolesOf(final Subject subject, final Set<String> declaredRoles) {
            final Principal[] principals = subject.getPrincipals().toArray(new Principal[0]);
            if (principals.length == 0) {
                return Collections.emptySet();
            }

            final BasicPolicyConfiguration.RoleResolver roleResolver =
                    SystemInstance.get().getComponent(BasicPolicyConfiguration.RoleResolver.class);
            if (roleResolver == null) {
                return Collections.emptySet();
            }
            return roleResolver.getLogicalRoles(principals, declaredRoles);
        }

        private static void addAll(final PermissionCollection target, final PermissionCollection source) {
            if (source == null) {
                return;
            }
            final Enumeration<Permission> elements = source.elements();
            while (elements.hasMoreElements()) {
                target.add(elements.nextElement());
            }
        }

        private static PermissionCollection copyOf(final PermissionCollection source) {
            final Permissions copy = new Permissions();
            addAll(copy, source);
            copy.setReadOnly();
            return copy;
        }

        private static PolicyConfiguration currentConfiguration() {
            final JaccProvider provider = get();
            if (provider == null) {
                return null;
            }

            final PolicyConfiguration configuration = provider.getPolicyConfiguration();
            try {
                return configuration != null && configuration.inService() ? configuration : null;
            } catch (final PolicyContextException e) {
                return null;
            }
        }

        private static final class ExcludingPermissionCollection extends PermissionCollection {
            private static final long serialVersionUID = 1L;

            private final PermissionCollection excluded;
            private final Permissions granted = new Permissions();

            private ExcludingPermissionCollection(final PermissionCollection excluded) {
                this.excluded = excluded;
            }

            @Override
            public void add(final Permission permission) {
                if (isReadOnly()) {
                    throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
                }
                granted.add(permission);
            }

            @Override
            public boolean implies(final Permission permission) {
                return !excluded.implies(permission) && granted.implies(permission);
            }

            @Override
            public Enumeration<Permission> elements() {
                return granted.elements();
            }
        }
    }

    private static String normalizeContextID(final String contextID) {
        return contextID == null ? DEFAULT_CONTEXT_ID : contextID;
    }

    public abstract PolicyConfiguration getPolicyConfiguration(String contextID, boolean remove) throws PolicyContextException;

    public abstract PolicyConfiguration getPolicyConfiguration(String contextID);

    public abstract PolicyConfiguration getPolicyConfiguration();

    public abstract boolean inService(String contextID) throws PolicyContextException;

    public abstract PermissionCollection getPermissions(CodeSource codesource);

    public abstract void refresh();

    public abstract boolean implies(ProtectionDomain domain, Permission permission);

    public abstract boolean hasAccessToWebResource(final String resource, final String... methods);
}
