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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.security;

import java.rmi.AccessException;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.Permission;
import javax.ejb.AccessLocalException;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.security.ContextManager;
import org.apache.openejb.EJBContextImpl;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.ExtendedEjbDeployment;


/**
 * An interceptor that performs the JACC EJB security check before continuing
 * on w/ the interceptor stack call.
 *
 * @version $Revision$ $Date$
 */
public final class EjbSecurityInterceptor implements Interceptor {
    private final Interceptor next;


    public EjbSecurityInterceptor(Interceptor next) {
        this.next = next;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        EjbInvocation ejbInvocation = ((EjbInvocation) invocation);
        ExtendedEjbDeployment deployment = ejbInvocation.getEjbDeployment();

        if (!deployment.isSecurityEnabled()) {
            return next.invoke(invocation);
        }

        EJBContextImpl context = ejbInvocation.getEJBInstanceContext().getEJBContextImpl();

        Subject oldCaller = context.getCallerSubject();
        Subject subject = ContextManager.getCurrentCaller();
        String oldPolicyContextID = PolicyContext.getContextID();
        try {
            PolicyContext.setContextID(deployment.getPolicyContextId());
            AccessControlContext accessContext = ContextManager.getCurrentContext();
            if (accessContext != null) {
                PermissionManager permissionManager = deployment.getPermissionManager();
                Permission permission = permissionManager.getPermission(ejbInvocation.getType(), ejbInvocation.getMethodIndex());
                if (permission != null) accessContext.checkPermission(permission);
            }

            context.setCallerSubject(subject);

            return next.invoke(invocation);
        } catch (AccessControlException e) {
            if (ejbInvocation.getType().isLocal()) {
                throw new AccessLocalException(e.getMessage());
            } else {
                throw new AccessException(e.getMessage());
            }
        } finally {
            PolicyContext.setContextID(oldPolicyContextID);
            context.setCallerSubject(oldCaller);
        }
    }
}
