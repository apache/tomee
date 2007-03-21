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
package org.apache.openejb.server.security;

import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.core.ThreadContextListener;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.CoreDeploymentInfo;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.EJBRoleRefPermission;
import javax.ejb.AccessLocalException;
import java.util.Set;
import java.security.AccessControlContext;
import java.security.Permission;
import java.security.AccessControlException;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.security.Principal;
import java.lang.reflect.Method;
import java.rmi.AccessException;

/**
 * @version $Rev$ $Date$
 */
public class SecurityServiceImpl implements ThreadContextListener {

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

        SecurityContext securityContext = oldContext.get(SecurityContext.class);

        if (deploymentInfo.getRunAsSubject() != null){
            securityContext = new SecurityContext(deploymentInfo.getRunAsSubject());
        } else if (securityContext == null){
            // TODO: Get the Subject from the JAAS LoginModule
            Subject subject = null;

            securityContext = new SecurityContext(subject);
        }

        newContext.set(SecurityContext.class, securityContext);

    }


    public void contextExited(ThreadContext exitedContext, ThreadContext reenteredContext) {
        PolicyContext.setContextID(reenteredContext.getDeploymentInfo().getModuleID());
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

    public void checkPermission(Method method, InterfaceType type) throws Throwable {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        SecurityContext securityContext = threadContext.get(SecurityContext.class);

        try {

            String ejbName = threadContext.getDeploymentInfo().getEjbName();
            Permission permission = new EJBMethodPermission(ejbName, type.getName(), method);

            if (permission != null) securityContext.acc.checkPermission(permission);

        } catch (AccessControlException e) {
            boolean isLocal = false;// TODO: This check should go in the proxy handler
            if (isLocal) {
                throw new AccessLocalException(e.getMessage());
            } else {
                throw new AccessException(e.getMessage());
            }
        }
    }
}
