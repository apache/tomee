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

package org.apache.openejb.core;

import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.InternalErrorException;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.core.ivm.IntraVmProxy;
import org.apache.openejb.core.managed.ManagedObjectHandler;
import org.apache.openejb.core.singleton.SingletonEjbObjectHandler;
import org.apache.openejb.core.stateful.StatefulEjbObjectHandler;
import org.apache.openejb.core.stateless.StatelessEjbObjectHandler;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.proxy.LocalBeanProxyFactory;
import org.apache.openejb.util.proxy.ProxyManager;

import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBObject;
import jakarta.ejb.SessionContext;
import jakarta.transaction.UserTransaction;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public abstract class BaseSessionContext extends BaseContext implements SessionContext {
    protected BaseSessionContext(final SecurityService securityService) {
        super(securityService);
    }

    public BaseSessionContext(final SecurityService securityService, final UserTransaction userTransaction) {
        super(securityService, userTransaction);
    }

    public boolean wasCancelCalled() {
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext di = threadContext.getBeanContext();
        final Method runningMethod = threadContext.get(Method.class);
        if (runningMethod == null) {
            throw new IllegalStateException("No running method");
        }
        if (di.isAsynchronous(runningMethod)) {
            if (runningMethod.getReturnType() == void.class) {
                throw new IllegalStateException("Current running method " + runningMethod.getName() + " is an asynchronous method, but its return type is void :" + di.getDestinationId());
            }
            return ThreadContext.isAsynchronousCancelled();
        }
        throw new IllegalStateException("Current running method " + runningMethod.getName() + " is not an asynchronous method :" + di.getDestinationId());
    }

    public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
        doCheck(Call.getEJBLocalObject);
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext di = threadContext.getBeanContext();

        if (di.getLocalHomeInterface() == null) {
            throw new IllegalStateException("Bean does not have an EJBLocalObject interface: " + di.getDeploymentID());
        }

        return (EJBLocalObject) EjbObjectProxyHandler.createProxy(di, threadContext.getPrimaryKey(), InterfaceType.EJB_LOCAL, di.getLocalInterface());
    }

    public EJBObject getEJBObject() throws IllegalStateException {
        doCheck(Call.getEJBObject);
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext di = threadContext.getBeanContext();
        if (di.getHomeInterface() == null) {
            throw new IllegalStateException("Bean does not have an EJBObject interface: " + di.getDeploymentID());
        }

        return (EJBObject) EjbObjectProxyHandler.createProxy(di, threadContext.getPrimaryKey(), InterfaceType.EJB_OBJECT, di.getRemoteInterface());
    }

    public Object getBusinessObject(final Class interfce) {
        doCheck(Call.getBusinessObject);
        if (interfce == null) {
            throw new IllegalStateException("Interface argument cannot me null.");
        }

        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext di = threadContext.getBeanContext();

        final InterfaceType interfaceType = di.getInterfaceType(interfce);
        final BeanType type = di.getComponentType();

        if (interfaceType == null) {
            throw new IllegalStateException("Component has no such interface: " + interfce.getName());
        }

        if (!interfaceType.isBusiness()) {
            throw new IllegalStateException("Interface is not a business interface for this bean: " + interfce.getName());
        }

        try {
            final EjbObjectProxyHandler handler;
            switch (di.getComponentType()) {
                case STATEFUL: {
                    handler = new StatefulEjbObjectHandler(di, threadContext.getPrimaryKey(), interfaceType, new ArrayList<>(), interfce);
                    break;
                }
                case STATELESS: {
                    handler = new StatelessEjbObjectHandler(di, threadContext.getPrimaryKey(), interfaceType, new ArrayList<>(), interfce);
                    break;
                }
                case SINGLETON: {
                    handler = new SingletonEjbObjectHandler(di, threadContext.getPrimaryKey(), interfaceType, new ArrayList<>(), interfce);
                    break;
                }
                case MANAGED: {
                    handler = new ManagedObjectHandler(di, threadContext.getPrimaryKey(), interfaceType, new ArrayList<>(), interfce);
                    break;
                }
                default:
                    throw new IllegalStateException("Bean is not a session bean: " + di.getComponentType());
            }

            if (InterfaceType.LOCALBEAN.equals(interfaceType)) {
                return LocalBeanProxyFactory.constructProxy(di.get(BeanContext.ProxyClass.class).getProxy(), handler);
            } else {
                final List<Class> interfaces = new ArrayList<>(di.getInterfaces(interfaceType));
                interfaces.add(Serializable.class);
                interfaces.add(IntraVmProxy.class);
                if (BeanType.STATEFUL.equals(type) || BeanType.MANAGED.equals(type)) {
                    interfaces.add(BeanContext.Removable.class);
                }
                return ProxyManager.newProxyInstance(interfaces.toArray(new Class[interfaces.size()]), handler);
            }
        } catch (final IllegalAccessException iae) {
            throw new InternalErrorException("Could not create IVM proxy for " + interfce.getName() + " interface", iae);
        }
    }

    public Class getInvokedBusinessInterface() {
        doCheck(Call.getInvokedBusinessInterface);
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final Class invokedInterface = threadContext.getInvokedInterface();
        final InterfaceType type = threadContext.getBeanContext().getInterfaceType(invokedInterface);
        if (!type.isBusiness()) {
            throw new IllegalStateException("The EJB spec requires us to cripple the use of this method for anything but business interface proxy.  But FYI, your invoked interface is: " + invokedInterface.getName());
        }

        if (invokedInterface == null) {
            throw new IllegalStateException("Business interface not set into ThreadContext.");
        }
        return invokedInterface;
    }
}
