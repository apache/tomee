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

package org.apache.openejb.core.entity;

import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.InternalErrorException;
import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.core.ivm.IntraVmProxy;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.proxy.ProxyManager;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import java.util.ArrayList;

/**
 * @version $Rev$ $Date$
 */
public class EntityContext extends BaseContext implements javax.ejb.EntityContext {

    public EntityContext(final SecurityService securityService) {
        super(securityService);
    }

    public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
        doCheck(Call.getEJBLocalObject);

        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext di = threadContext.getBeanContext();

        if (di.getLocalInterface() == null) {
            throw new IllegalStateException("EJB " + di.getDeploymentID() + " does not have a local interface");
        }

        final EjbObjectProxyHandler handler = new EntityEjbObjectHandler(di, threadContext.getPrimaryKey(), InterfaceType.EJB_LOCAL, new ArrayList<>(), di.getLocalInterface());

        try {
            final Class[] interfaces = new Class[]{di.getLocalInterface(), IntraVmProxy.class};
            return (EJBLocalObject) ProxyManager.newProxyInstance(interfaces, handler);
        } catch (final IllegalAccessException iae) {
            throw new InternalErrorException("Could not create IVM proxy for " + di.getLocalInterface() + " interface", iae);
        }
    }

    public EJBObject getEJBObject() throws IllegalStateException {
        doCheck(Call.getEJBObject);

        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext di = threadContext.getBeanContext();

        if (di.getRemoteInterface() == null) {
            throw new IllegalStateException("EJB " + di.getDeploymentID() + " does not have a remote interface");
        }

        final EjbObjectProxyHandler handler = new EntityEjbObjectHandler(di.getContainer().getBeanContext(di.getDeploymentID()), threadContext.getPrimaryKey(), InterfaceType.EJB_OBJECT, new ArrayList<>(), di.getRemoteInterface());
        try {
            final Class[] interfaces = new Class[]{di.getRemoteInterface(), IntraVmProxy.class};
            return (EJBObject) ProxyManager.newProxyInstance(interfaces, handler);
        } catch (final IllegalAccessException iae) {
            throw new InternalErrorException("Could not create IVM proxy for " + di.getRemoteInterface() + " interface", iae);
        }
    }

    public Object getPrimaryKey() throws IllegalStateException {
        doCheck(Call.getPrimaryKey);
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        return threadContext.getPrimaryKey();
    }

    public void check(final ThreadContext context, final Call call) {
        final Operation operation = context.getCurrentOperation();
        switch (call) {
            case getUserTransaction:
            case getContextData:
                throw illegal(call, operation);
            case getPrimaryKey:
            case getEJBLocalObject:
            case getEJBObject:
                switch (operation) {
                    case SET_CONTEXT:
                    case UNSET_CONTEXT:
                    case CREATE:
                    case FIND:
                    case HOME:
                        throw illegal(call, operation);
                    default:
                        return;
                }
            case isCallerInRole:
            case getCallerPrincipal:
            case setRollbackOnly:
            case getRollbackOnly:
                switch (ThreadContext.getThreadContext().getCurrentOperation()) {
                    case SET_CONTEXT:
                    case UNSET_CONTEXT:
                    case ACTIVATE:
                    case PASSIVATE:
                        throw illegal(call, operation);
                    default:
                        return;
                }
            case getTimerService:
                switch (operation) {
                    case SET_CONTEXT:
                    case UNSET_CONTEXT:
                    case FIND:
                        throw illegal(call, operation);
                    default:
                        return;
                }
            case timerMethod:
                switch (operation) {
                    case SET_CONTEXT:
                    case UNSET_CONTEXT:
                    case CREATE:
                    case FIND:
                    case HOME:
                    case ACTIVATE:
                    case PASSIVATE:
                        throw illegal(call, operation);
                    default:
                        return;

                }
        }
    }
}
