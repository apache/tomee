/**
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

import org.apache.openejb.InterfaceType;
import org.apache.openejb.util.ConfUtils;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ThreadContextListener;
import org.apache.openejb.core.security.jaas.UsernamePasswordCallbackHandler;
import org.apache.openejb.core.security.jacc.BasicJaccProvider;
import org.apache.openejb.spi.SecurityService;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.EJBRoleRefPermission;
import javax.security.jacc.PolicyContext;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.Permission;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.Policy;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * @version $Rev$ $Date$
 */
public class SecurityServiceImpl implements SecurityService, ThreadContextListener {
    static private final Map<Object, Identity> identities = new java.util.concurrent.ConcurrentHashMap();
    static private final ThreadLocal<Subject> clientIdentity = new ThreadLocal<Subject>();

    private final String defaultUser = "guest";
    private final Subject defaultSubject;

    private final SecurityContext defaultContext;
    private String realmName = "PropertiesLogin";

    public SecurityServiceImpl() {

        System.setProperty(JaccProvider.class.getName(), BasicJaccProvider.class.getName());

        installJaas();

        installJacc();

        ThreadContext.addThreadContextListener(this);

        defaultSubject = createSubject(defaultUser);
        defaultContext = new SecurityContext(defaultSubject);

        try {
            // Perform a login attempt (which should fail)
            // simply to excercise the initialize code of any
            // LoginModules that are configured.
            // They should have a chance to perform any special
            // boot-time code that they may need.
            login("","");
        } catch (Throwable e) {
        }
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public void init(Properties props) throws Exception {
    }

    public Object login(String username, String password) throws LoginException {
        return login(realmName, username, password);
    }

    public Object login(String realmName, String username, String password) throws LoginException {
        if (realmName == null){
            realmName = this.realmName;
        }
        LoginContext context = new LoginContext(realmName, new UsernamePasswordCallbackHandler(username, password));
        context.login();

        Subject subject = context.getSubject();

        Identity identity = new Identity(subject);
        Serializable token = identity.getToken();
        identities.put(token, identity);
        return token;
    }

    private final static class SecurityContext {

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

    public void contextEntered(ThreadContext oldContext, ThreadContext newContext) {
        String moduleID = newContext.getDeploymentInfo().getModuleID();
        PolicyContext.setContextID(moduleID);

        CoreDeploymentInfo callingDeploymentInfo = (oldContext != null)? oldContext.getDeploymentInfo(): null;

        SecurityContext securityContext = (oldContext != null) ? oldContext.get(SecurityContext.class) : null;

        if (callingDeploymentInfo != null && callingDeploymentInfo.getRunAs() != null) {

            String runAsRole = callingDeploymentInfo.getRunAs();

            Subject runAs = resolve(runAsRole);

            securityContext = new SecurityContext(runAs);

        } else if (securityContext == null) {

            Subject subject = clientIdentity.get();

            if (subject != null){
                securityContext = new SecurityContext(subject);
            } else {
                securityContext = defaultContext;
            }

        }

        newContext.set(SecurityContext.class, securityContext);

    }

    public void contextExited(ThreadContext exitedContext, ThreadContext reenteredContext) {
        if (reenteredContext == null) {
            PolicyContext.setContextID(null);
        } else {
            PolicyContext.setContextID(reenteredContext.getDeploymentInfo().getModuleID());
        }
    }

    private Subject resolve(String runAsRole) {
        return createSubject(runAsRole);
    }



    public Subject getCurrentSubject() {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        SecurityContext securityContext = threadContext.get(SecurityContext.class);

        return securityContext.subject;
    }

    public void associate(Object securityIdentity) throws LoginException {
        if (securityIdentity == null) return;

        Identity identity = identities.get(securityIdentity);
        if (identity == null) throw new LoginException("Identity does not exist: " + securityIdentity);

        clientIdentity.set(identity.subject);

    }

    private static class Identity {
        private final Subject subject;
        private final UUID token;

        public Identity(Subject subject) {
            this.subject = subject;
            this.token = UUID.randomUUID();
        }

        public Subject getSubject() {
            return subject;
        }

        public Serializable getToken() {
            return token;
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
        for (Principal principal : principals) {
            return principal;
        }
        return null;
    }

    public boolean isCallerAuthorized(Method method, InterfaceType typee) {
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

    public Object getSecurityIdentity() {
        return null;
    }

    public void setSecurityIdentity(Object securityIdentity) {
    }

    public <T> T translateTo(Object securityIdentity, Class<T> type) {
        return null;
    }

    public boolean isCallerAuthorized(Object securityIdentity, Collection<String> roleNames) {
        return true;
    }

    private static void installJaas() {
        String path = System.getProperty("java.security.auth.login.config");

        if (path != null) {
            return;
        }

        URL loginConfig = ConfUtils.getConfResource("login.config");

        System.setProperty("java.security.auth.login.config", loginConfig.toExternalForm());
    }

    private static void installJacc() {
        final String providerKey = "javax.security.jacc.PolicyConfigurationFactory.provider";
        if (System.getProperty(providerKey) == null){
            System.setProperty(providerKey, JaccProvider.Factory.class.getName()) ;
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

    private Subject createSubject(String name) {
        SecurityServiceImpl.User user = new SecurityServiceImpl.User(name);
        SecurityServiceImpl.Group group = new SecurityServiceImpl.Group(name);
        group.addMember(user);

        HashSet<Principal> principals = new HashSet<Principal>();
        principals.add(user);
        principals.add(group);

        return new Subject(true, principals, new HashSet(), new HashSet());
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
