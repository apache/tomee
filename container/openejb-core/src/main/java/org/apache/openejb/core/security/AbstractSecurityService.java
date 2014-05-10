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

import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ThreadContextListener;
import org.apache.openejb.core.security.jaas.GroupPrincipal;
import org.apache.openejb.core.security.jacc.BasicJaccProvider;
import org.apache.openejb.core.security.jacc.BasicPolicyConfiguration;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.CallerPrincipal;
import org.apache.openejb.spi.SecurityService;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContext;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.Policy;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This security service chooses a UUID as its token as this can be serialized
 * to clients, is mostly secure, and can be deserialized in a client vm without
 * addition openejb-core classes.
 */
public abstract class AbstractSecurityService implements SecurityService<UUID>, ThreadContextListener, BasicPolicyConfiguration.RoleResolver {

    private static final Map<Object, Identity> identities = new ConcurrentHashMap<Object, Identity>();
    protected static final ThreadLocal<Identity> clientIdentity = new ThreadLocal<Identity>();
    protected String defaultUser = "guest";
    private String realmName = "PropertiesLogin";
    protected Subject defaultSubject;
    protected SecurityContext defaultContext;

    public AbstractSecurityService() {
        this(BasicJaccProvider.class.getName());
    }

    public AbstractSecurityService(final String jaccProvider) {
        System.setProperty(JaccProvider.class.getName(), jaccProvider);

        installJacc();

        ThreadContext.addThreadContextListener(this);

        // set the default subject and the default context
        updateSecurityContext();

        SystemInstance.get().setComponent(BasicPolicyConfiguration.RoleResolver.class, this);
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(final String realmName) {
        this.realmName = realmName;
    }

    /**
     * @return the defaultUser
     */
    public String getDefaultUser() {
        return defaultUser;
    }

    /**
     * @param defaultUser the defaultUser to set
     */
    public void setDefaultUser(final String defaultUser) {
        this.defaultUser = defaultUser;

        // set the default subject and the default context for the new default user
        updateSecurityContext();
    }

    // update the current subject and security context
    private void updateSecurityContext() {
        defaultSubject = createSubject(defaultUser, defaultUser);
        defaultContext = new SecurityContext(defaultSubject);
    }

    @Override
    public void init(final Properties props) throws Exception {
    }

    @Override
    public UUID login(final String username, final String password) throws LoginException {
        return login(realmName, username, password);
    }

    @Override
    public Set<String> getLogicalRoles(final Principal[] principals, final Set<String> logicalRoles) {
        final LinkedHashSet<String> roles = new LinkedHashSet<String>(principals.length);
        for (final Principal principal : principals) {
            final String name = principal.getName();
            if (logicalRoles.contains(name)) {
                roles.add(name);
            }
        }
        return roles;
    }

    @Override
    public void contextEntered(final ThreadContext oldContext, final ThreadContext newContext) {
        final String moduleID = newContext.getBeanContext().getModuleID();
        PolicyContext.setContextID(moduleID);

        Subject runAsSubject = getRunAsSubject(newContext.getBeanContext());
        if (oldContext != null && runAsSubject == null) {
            runAsSubject = getRunAsSubject(oldContext.getBeanContext());
        }

        SecurityContext securityContext = oldContext != null ? oldContext.get(SecurityContext.class) : null;
        if (runAsSubject != null) {

            securityContext = new SecurityContext(runAsSubject);

        } else if (securityContext == null) {

            final Identity identity = clientIdentity.get();
            if (identity != null) {
                securityContext = new SecurityContext(identity.subject);
            } else {
                securityContext = defaultContext;
            }
        }

        newContext.set(SecurityContext.class, securityContext);
    }

    protected Subject getRunAsSubject(final BeanContext callingBeanContext) {
        if (callingBeanContext == null) {
            return null;
        }
        return createRunAsSubject(callingBeanContext.getRunAsUser(), callingBeanContext.getRunAs());
    }

    protected Subject createRunAsSubject(final String runAsUser, final String runAsRole) {
        return createSubject(runAsUser, runAsRole);
    }

    @Override
    public void contextExited(final ThreadContext exitedContext, final ThreadContext reenteredContext) {
        if (reenteredContext == null) {
            PolicyContext.setContextID(null);
        } else {
            PolicyContext.setContextID(reenteredContext.getBeanContext().getModuleID());
        }
    }

    protected UUID registerSubject(final Subject subject) {
        final Identity identity = new Identity(subject);
        final UUID token = identity.getToken();
        identities.put(token, identity);
        return token;
    }

    @Override
    public void logout(final UUID securityIdentity) throws LoginException {
        final Identity identity = identities.get(securityIdentity);
        if (identity == null) {
            throw new LoginException("Identity is not currently logged in: " + securityIdentity);
        }
        identities.remove(securityIdentity);
    }

    protected void unregisterSubject(final Object securityIdentity) {
        identities.remove(securityIdentity);
    }

    @Override
    public void associate(final UUID securityIdentity) throws LoginException {
        if (clientIdentity.get() != null) {
            throw new LoginException("Thread already associated with a client identity.  Refusing to overwrite.");
        }
        if (securityIdentity == null) {
            throw new NullPointerException("The security token passed in is null");
        }

        // The securityIdentity token must associated with a logged in Identity
        final Identity identity = identities.get(securityIdentity);
        if (identity == null) {
            throw new LoginException("Identity is not currently logged in: " + securityIdentity);
        }

        clientIdentity.set(identity);
    }

    @Override
    public UUID disassociate() {
        try {
            final Identity identity = clientIdentity.get();
            return identity == null ? null : identity.getToken();
        } finally {
            clientIdentity.remove();
        }
    }

    @Override
    public boolean isCallerInRole(final String role) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }

        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final SecurityContext securityContext = threadContext.get(SecurityContext.class);

        final Set<Group> grps = securityContext.subject.getPrincipals(Group.class);
        for (final Group grp : grps) {
            if (grp.getName().equals(role)) {
                return true;
            }
        }
        final Set<GroupPrincipal> grpsp = securityContext.subject.getPrincipals(GroupPrincipal.class);
        for (final GroupPrincipal grp : grpsp) {
            if (grp.getName().equals(role)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Principal getCallerPrincipal() {
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final SecurityContext securityContext = threadContext.get(SecurityContext.class);
        final Set<Principal> principals = securityContext.subject.getPrincipals();

        if (!principals.isEmpty()) {
            for (final Principal principal : principals) {
                if (principal.getClass().isAnnotationPresent(CallerPrincipal.class)) {
                    return principal;
                }
            }
            return principals.iterator().next();
        }
        return null;
    }

    @Override
    public boolean isCallerAuthorized(final Method method, final InterfaceType type) {
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext beanContext = threadContext.getBeanContext();
        try {
            final String ejbName = beanContext.getEjbName();
            String name = type == null ? null : type.getSpecName();
            if ("LocalBean".equals(name) || "LocalBeanHome".equals(name)) {
                name = null;
            }
            final Identity currentIdentity = clientIdentity.get();
            final SecurityContext securityContext;
            if(currentIdentity == null) {
                securityContext= threadContext.get(SecurityContext.class);
            } else {
                securityContext = new SecurityContext(currentIdentity.getSubject());
            }
            securityContext.acc.checkPermission(new EJBMethodPermission(ejbName, name, method));
        } catch (final AccessControlException e) {
            return false;
        }
        return true;
    }

    protected static void installJacc() {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        final String providerKey = "javax.security.jacc.PolicyConfigurationFactory.provider";
        try {
            if (System.getProperty(providerKey) == null) {
                System.setProperty(providerKey, JaccProvider.Factory.class.getName());
                final ClassLoader cl = JaccProvider.Factory.class.getClassLoader();
                Thread.currentThread().setContextClassLoader(cl);
            }

            // Force the loading of the javax.security.jacc.PolicyConfigurationFactory.provider
            // Hopefully it will be cached thereafter and ClassNotFoundExceptions thrown
            // from the equivalent call in JaccPermissionsBuilder can be avoided.
            PolicyConfigurationFactory.getPolicyConfigurationFactory();
        } catch (final Exception e) {
            throw new IllegalStateException("Could not install JACC Policy Configuration Factory: " + System.getProperty(providerKey), e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

        final String policyProvider = SystemInstance.get().getOptions().get("javax.security.jacc.policy.provider", JaccProvider.Policy.class.getName());
        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            final Class policyClass = Class.forName(policyProvider, true, classLoader);
            final Policy policy = (Policy) policyClass.newInstance();
            policy.refresh();
            Policy.setPolicy(policy);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not install JACC Policy Provider: " + policyProvider, e);
        }
    }

    protected Subject createSubject(final String name, final String groupName) {
        if (name == null) {
            return null;
        }

        final User user = new User(name);
        final Group group = new Group(groupName);
        group.addMember(user);

        final HashSet<Principal> principals = new HashSet<Principal>();
        principals.add(user);
        principals.add(group);

        return new Subject(true, principals, new HashSet(), new HashSet());
    }

    @Override
    public Object currentState() {
        return clientIdentity.get();
    }

    @Override
    public void setState(final Object o) {
        if (Identity.class.isInstance(o)) {
            clientIdentity.set(Identity.class.cast(o));
        }
    }

    protected static final class SecurityContext {

        public final Subject subject;
        public final AccessControlContext acc;

        @SuppressWarnings("unchecked")
        public SecurityContext(final Subject subject) {
            this.subject = subject;
            this.acc = (AccessControlContext) Subject.doAsPrivileged(subject, new PrivilegedAction() {
                @Override
                public Object run() {
                    return AccessController.getContext();
                }
            }, null);
        }
    }

    protected static class Identity implements Serializable {

        private final Subject subject;
        private final UUID token;

        public Identity(final Subject subject) {
            this.subject = subject;
            this.token = UUID.randomUUID();
        }

        public Identity(final Subject subject, final UUID token) {
            this.subject = subject;
            this.token = token;
        }

        public Subject getSubject() {
            return subject;
        }

        public UUID getToken() {
            return token;
        }
    }

    public static class Group implements java.security.acl.Group {

        private final List<Principal> members = new ArrayList<Principal>();
        private final String name;

        public Group(final String name) {
            this.name = name;
        }

        @Override
        public boolean addMember(final Principal user) {
            return members.add(user);
        }

        @Override
        public boolean removeMember(final Principal user) {
            return members.remove(user);
        }

        @Override
        public boolean isMember(final Principal member) {
            return members.contains(member);
        }

        @Override
        public Enumeration<? extends Principal> members() {
            return Collections.enumeration(members);
        }

        @Override
        public String getName() {
            return name;
        }
    }

    @CallerPrincipal // to force it to be before group in getCallerPrincipal, otherwise we aren't deterministic
    public static class User implements Principal {

        private final String name;

        public User(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
