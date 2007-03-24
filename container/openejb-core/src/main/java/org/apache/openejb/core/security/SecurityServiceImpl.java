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
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ThreadContextListener;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.EJBRoleRefPermission;
import javax.security.jacc.PolicyContext;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.Permission;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * @version $Rev$ $Date$
 */
public class SecurityServiceImpl implements SecurityService, ThreadContextListener {
    static private final Map<Object, Identity> identities = new java.util.concurrent.ConcurrentHashMap();

    private final String defaultUser = "guest";
    private final Subject defaultSubject;
    private final SecurityContext defaultContext;

    public SecurityServiceImpl() {
        String path = System.getProperty("java.security.auth.login.config");
        if (path == null) {
            try {
                File conf = SystemInstance.get().getBase().getDirectory("conf");
                File loginConfig = new File(conf, "login.config");
                if (loginConfig.exists()) {
                    path = conf.getAbsolutePath();
                    System.setProperty("java.security.auth.login.config", path);
                }
            } catch (IOException e) {
            }
        }

        if (path == null) {
            URL resource = this.getClass().getClassLoader().getResource("login.config");
            if (resource != null) {
                path = resource.getFile();
                System.setProperty("java.security.auth.login.config", path);
            }
        }

        ThreadContext.addThreadContextListener(this);
        PolicyConfigurationFactoryImpl.install();

        try {
            defaultSubject = getSubject(defaultUser);
            defaultContext = new SecurityContext(defaultSubject);
        } catch (LoginException e) {
            throw new IllegalStateException("Unable to create default subject: " + defaultUser, e);
        }
    }

    public Object login(String username, String password) throws LoginException {
        LoginContext context = new LoginContext("PropertiesLogin", new UsernamePasswordCallbackHandler(username, password));
        context.login();

        Subject subject = context.getSubject();

        Identity identity = new Identity(subject);
        Serializable token = identity.getToken();
        identities.put(token, identity);
        return token;
    }

    private Subject getSubject(String name) throws LoginException {
        LoginContext context = new LoginContext("PropertiesLogin", new Handler(name));

        context.login();

        return context.getSubject();
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
        PolicyContext.setContextID(newContext.getDeploymentInfo().getModuleID());

        CoreDeploymentInfo deploymentInfo = newContext.getDeploymentInfo();

        SecurityContext securityContext = (oldContext != null) ? oldContext.get(SecurityContext.class) : null;

        if (deploymentInfo.getRunAs() != null) {

            String runAsRole = deploymentInfo.getRunAs();

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

    /**
     * TODO
     *
     * @param runAsRole
     * @return the role converted to a subject
     */
    private Subject resolve(String runAsRole) {
        try {
            return getSubject(runAsRole);
        } catch (LoginException e) {
            throw new IllegalStateException("RunAs user does not exist:" + runAsRole, e);
        }
    }


    public void contextExited(ThreadContext exitedContext, ThreadContext reenteredContext) {
        if (reenteredContext == null) {
            PolicyContext.setContextID(null);
        } else {
            PolicyContext.setContextID(reenteredContext.getDeploymentInfo().getModuleID());
        }
    }


    public Subject getCurrentSubject() {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        SecurityContext securityContext = threadContext.get(SecurityContext.class);

        return securityContext.subject;
    }

    private static ThreadLocal<Subject> clientIdentity = new ThreadLocal<Subject>();

    public void associate(Object securityIdentity) throws LoginException {
        if (securityIdentity == null) return;
        
        Identity identity = identities.get(securityIdentity);
        if (identity == null) throw new LoginException("Identity does not exist: " + securityIdentity);

        clientIdentity.set(identity.subject);

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

    public boolean isCallerAuthorized(Method method, InterfaceType type) {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        SecurityContext securityContext = threadContext.get(SecurityContext.class);

        try {

            String ejbName = threadContext.getDeploymentInfo().getEjbName();

            String name = type.getName();

            Permission permission = new EJBMethodPermission(ejbName, name, method);

            if (permission != null) securityContext.acc.checkPermission(permission);

        } catch (AccessControlException e) {
            return false;
        }
        return true;
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

    public void init(Properties props) throws Exception {
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

    final static class Handler implements CallbackHandler {
        private final String user;

        private Handler(String user) {
            this.user = user;
        }

        String getUser() {
            return user;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        }
    }

}
