/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.security;

import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.core.ThreadContextListener;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.security.jacc.BasicJaccProvider;
import org.apache.openejb.core.security.jacc.BasicPolicyConfiguration;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.loader.SystemInstance;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.EJBRoleRefPermission;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.PolicyConfigurationFactory;
import java.security.AccessControlContext;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.security.Principal;
import java.security.AccessControlException;
import java.security.Permission;
import java.security.Policy;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Method;

/**
 * This security service chooses a UUID as its token as this can be serialized
 * to clients, is mostly secure, and can be deserialized in a client vm without
 * addition openejb-core classes.
 */
public abstract class AbstractSecurityService implements SecurityService<UUID>, ThreadContextListener, BasicPolicyConfiguration.RoleResolver {
    static private final Map<Object, Identity> identities = new ConcurrentHashMap<Object, Identity>();
    static protected final ThreadLocal<Identity> clientIdentity = new ThreadLocal<Identity>();
    protected final String defaultUser = "guest";
    protected final Subject defaultSubject;
    protected final SecurityContext defaultContext;
    private String realmName = "PropertiesLogin";

    public AbstractSecurityService() {
        System.setProperty(JaccProvider.class.getName(), BasicJaccProvider.class.getName());

        installJacc();

        ThreadContext.addThreadContextListener(this);

        defaultSubject = createSubject(defaultUser);
        defaultContext = new SecurityContext(defaultSubject);

        SystemInstance.get().setComponent(BasicPolicyConfiguration.RoleResolver.class, this);
    }


    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public void init(Properties props) throws Exception {
    }

    public UUID login(String username, String password) throws LoginException {
        return login(realmName, username, password);
    }

    public Set<String> getLogicalRoles(Principal[] principals, Set<String> logicalRoles) {
        LinkedHashSet<String> roles = new LinkedHashSet<String>(principals.length);
        for (Principal principal : principals) {
            String name = principal.getName();
            if (logicalRoles.contains(name)) {
                roles.add(name);
            }
        }
        return roles;
    }

    public void contextEntered(ThreadContext oldContext, ThreadContext newContext) {
        String moduleID = newContext.getDeploymentInfo().getModuleID();
        PolicyContext.setContextID(moduleID);

        SecurityContext securityContext = (oldContext != null) ? oldContext.get(SecurityContext.class) : null;

        CoreDeploymentInfo callingDeploymentInfo = (oldContext != null)? oldContext.getDeploymentInfo(): null;
        Subject runAsSubject = getRunAsSubject(callingDeploymentInfo);
        if (runAsSubject != null) {

            securityContext = new SecurityContext(runAsSubject);

        } else if (securityContext == null) {

            Identity identity = clientIdentity.get();
            if (identity != null){
                securityContext = new SecurityContext(identity.subject);
            } else {
                securityContext = defaultContext;
            }
        }

        newContext.set(SecurityContext.class, securityContext);
    }

    protected Subject getRunAsSubject(CoreDeploymentInfo callingDeploymentInfo) {
        if (callingDeploymentInfo == null) return null;

        String runAsRole = callingDeploymentInfo.getRunAs();
        Subject runAs = createRunAsSubject(runAsRole);
        return runAs;
    }

    protected Subject createRunAsSubject(String runAsRole) {
        return createSubject(runAsRole);
    }

    public void contextExited(ThreadContext exitedContext, ThreadContext reenteredContext) {
        if (reenteredContext == null) {
            PolicyContext.setContextID(null);
        } else {
            PolicyContext.setContextID(reenteredContext.getDeploymentInfo().getModuleID());
        }
    }

    protected UUID registerSubject(Subject subject) {
        Identity identity = new Identity(subject);
        UUID token = identity.getToken();
        identities.put(token, identity);
        return token;
    }

    public void logout(UUID securityIdentity) throws LoginException {
        Identity identity = identities.get(securityIdentity);
        if (identity == null) throw new LoginException("Identity is not currently logged in: " + securityIdentity);
        identities.remove(securityIdentity);
    }

    protected void unregisterSubject(Object securityIdentity) {
        identities.remove(securityIdentity);
    }

    public void associate(UUID securityIdentity) throws LoginException {
        if (clientIdentity.get() != null) throw new LoginException("Thread already associated with a client identity.  Refusing to overwrite.");
        if (securityIdentity == null) throw new NullPointerException("The security token passed in is null");

        // The securityIdentity token must associated with a logged in Identity
        Identity identity = identities.get(securityIdentity);
        if (identity == null) throw new LoginException("Identity is not currently logged in: " + securityIdentity);

        clientIdentity.set(identity);
    }

    public UUID disassociate() {
        try {
            Identity identity = clientIdentity.get();
            return (identity == null)? null: identity.getToken();
        } finally {
            clientIdentity.remove();
        }
    }

    public boolean isCallerInRole(String role) {
        if (role == null) throw new IllegalArgumentException("Role must not be null");

        ThreadContext threadContext = ThreadContext.getThreadContext();
        SecurityContext securityContext = threadContext.get(SecurityContext.class);

        try {
            CoreDeploymentInfo deployment = threadContext.getDeploymentInfo();

            securityContext.acc.checkPermission(new EJBRoleRefPermission(deployment.getEjbName(), role));
        } catch (AccessControlException e) {
            return false;
        }
        return true;
    }

    public Principal getCallerPrincipal() {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        SecurityContext securityContext = threadContext.get(SecurityContext.class);
        Set<Principal> principals = securityContext.subject.getPrincipals();
        if (!principals.isEmpty()) {
            return principals.iterator().next();
        }
        return null;
    }

    public boolean isCallerAuthorized(Method method, InterfaceType interfaceType) {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        SecurityContext securityContext = threadContext.get(SecurityContext.class);

        try {

            CoreDeploymentInfo deploymentInfo = threadContext.getDeploymentInfo();

            String ejbName = deploymentInfo.getEjbName();

            InterfaceType type = deploymentInfo.getInterfaceType(method.getDeclaringClass());

            String name = (type == null)? null: type.getSpecName();

            Permission permission = new EJBMethodPermission(ejbName, name, method);

            if (permission != null) securityContext.acc.checkPermission(permission);

        } catch (AccessControlException e) {
            return false;
        }
        return true;
    }

    protected static void installJacc() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        final String providerKey = "javax.security.jacc.PolicyConfigurationFactory.provider";
        try {
            if (System.getProperty(providerKey) == null) {
                System.setProperty(providerKey, JaccProvider.Factory.class.getName());
                ClassLoader cl = JaccProvider.Factory.class.getClassLoader();
                Thread.currentThread().setContextClassLoader(cl);
            }

            // Force the loading of the javax.security.jacc.PolicyConfigurationFactory.provider
            // Hopefully it will be cached thereafter and ClassNotFoundExceptions thrown
            // from the equivalent call in JaccPermissionsBuilder can be avoided.
            PolicyConfigurationFactory.getPolicyConfigurationFactory();
        } catch (Exception e) {
            throw new IllegalStateException("Could not install JACC Policy Configuration Factory: " + System.getProperty(providerKey), e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }


        String policyProvider = System.getProperty("javax.security.jacc.policy.provider", JaccProvider.Policy.class.getName());
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class policyClass = Class.forName(policyProvider, true, classLoader);
            Policy policy = (Policy) policyClass.newInstance();
            policy.refresh();
            Policy.setPolicy(policy);
        } catch (Exception e) {
            throw new IllegalStateException("Could not install JACC Policy Provider: "+policyProvider, e);
        }
    }

    protected Subject createSubject(String name) {
        if (name == null) return null;

        User user = new User(name);
        Group group = new Group(name);
        group.addMember(user);

        HashSet<Principal> principals = new HashSet<Principal>();
        principals.add(user);
        principals.add(group);

        return new Subject(true, principals, new HashSet(), new HashSet());
    }

    protected final static class SecurityContext {

        private final Subject subject;
        private final AccessControlContext acc;

        public SecurityContext(Subject subject) {
            this.subject = subject;
            this.acc = (AccessControlContext) Subject.doAsPrivileged(subject, new PrivilegedAction() {
                public Object run() {
                    return AccessController.getContext();
                }
            }, null);
        }
    }

    protected static class Identity {
        private final Subject subject;
        private final UUID token;

        public Identity(Subject subject) {
            this.subject = subject;
            this.token = UUID.randomUUID();
        }

        public Identity(Subject subject, UUID token) {
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

        public Group(String name) {
            this.name = name;
        }

        public boolean addMember(Principal user) {
            return members.add(user);
        }

        public boolean removeMember(Principal user) {
            return members.remove(user);
        }

        public boolean isMember(Principal member) {
            return members.contains(member);
        }

        public Enumeration<? extends Principal> members() {
            return Collections.enumeration(members);
        }

        public String getName() {
            return name;
        }
    }

    public static class User implements Principal {
        private final String name;

        public User(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
