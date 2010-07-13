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
package org.apache.openejb.core;

import java.security.Principal;
import java.util.List;
import java.util.ArrayList;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.InternalErrorException;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.core.ivm.IntraVmProxy;
import org.apache.openejb.core.stateless.StatelessEjbObjectHandler;
import org.apache.openejb.core.stateful.StatefulEjbObjectHandler;
import org.apache.openejb.core.singleton.SingletonEjbObjectHandler;
import org.apache.openejb.core.managed.ManagedObjectHandler;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.proxy.ProxyManager;

/**
 * @version $Rev$ $Date$
 */
public abstract class BaseSessionContext extends BaseContext implements SessionContext {
    protected BaseSessionContext(SecurityService securityService) {
        super(securityService);
    }

    public BaseSessionContext(SecurityService securityService, UserTransaction userTransaction) {
        super(securityService, userTransaction);
    }

    public boolean wasCancelCalled() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
        check(Call.getEJBLocalObject);
        ThreadContext threadContext = ThreadContext.getThreadContext();
        DeploymentInfo di = threadContext.getDeploymentInfo();

        if (di.getLocalHomeInterface() == null) throw new IllegalStateException("Bean does not have an EJBLocalObject interface: "+di.getDeploymentID());

        return (EJBLocalObject) EjbObjectProxyHandler.createProxy(di, threadContext.getPrimaryKey(), InterfaceType.EJB_LOCAL);
    }

    public EJBObject getEJBObject() throws IllegalStateException {
        check(Call.getEJBObject);
        ThreadContext threadContext = ThreadContext.getThreadContext();
        DeploymentInfo di = threadContext.getDeploymentInfo();
        if (di.getHomeInterface() == null) throw new IllegalStateException("Bean does not have an EJBObject interface: "+di.getDeploymentID());

        return (EJBObject) EjbObjectProxyHandler.createProxy(di, threadContext.getPrimaryKey(), InterfaceType.EJB_OBJECT);
    }

    public MessageContext getMessageContext() throws IllegalStateException {
        check(Call.getMessageContext);
        ThreadContext threadContext = ThreadContext.getThreadContext();
        MessageContext messageContext = threadContext.get(MessageContext.class);
        if (messageContext == null) throw new IllegalStateException("Only calls on the service-endpoint have a MessageContext.");
        return messageContext;
    }

    public Object getBusinessObject(Class interfce) {
        check(Call.getBusinessObject);
        if (interfce == null) throw new IllegalStateException("Interface argument cannot me null.");

        ThreadContext threadContext = ThreadContext.getThreadContext();
        DeploymentInfo di = threadContext.getDeploymentInfo();

        InterfaceType interfaceType = di.getInterfaceType(interfce);

        if (interfaceType == null){
            throw new IllegalStateException("Component has no such interface: " + interfce.getName());
        }

        if (!interfaceType.isBusiness()) {
            throw new IllegalStateException("Interface is not a business interface for this bean: " + interfce.getName());
        }

        try {
            EjbObjectProxyHandler handler;
            switch(di.getComponentType()){
                case STATEFUL: {
                    handler = new StatefulEjbObjectHandler(di, threadContext.getPrimaryKey(), interfaceType, new ArrayList<Class>());
                    break;
                }
                case STATELESS: {
                    handler = new StatelessEjbObjectHandler(di, threadContext.getPrimaryKey(), interfaceType, new ArrayList<Class>());
                    break;
                }
                case SINGLETON: {
                    handler = new SingletonEjbObjectHandler(di, threadContext.getPrimaryKey(), interfaceType, new ArrayList<Class>());
                    break;
                }
                case MANAGED: {
                    handler = new ManagedObjectHandler(di, threadContext.getPrimaryKey(), interfaceType, new ArrayList<Class>());
                    break;
                }
                default: throw new IllegalStateException("Bean is not a session bean: "+di.getComponentType());
            }

            List<Class> interfaces = new ArrayList<Class>();
            interfaces.addAll(di.getInterfaces(interfaceType));
            interfaces.add(IntraVmProxy.class);
            return ProxyManager.newProxyInstance(interfaces.toArray(new Class[interfaces.size()]), handler);
        } catch (IllegalAccessException iae) {
            throw new InternalErrorException("Could not create IVM proxy for " + interfce.getName() + " interface", iae);
        }
    }

    public Class getInvokedBusinessInterface() {
        check(Call.getInvokedBusinessInterface);
        ThreadContext threadContext = ThreadContext.getThreadContext();
        Class invokedInterface = threadContext.getInvokedInterface();
        InterfaceType type = threadContext.getDeploymentInfo().getInterfaceType(invokedInterface);
        if (!type.isBusiness()) throw new IllegalStateException("The EJB spec requires us to cripple the use of this method for anything but business interface proxy.  But FYI, your invoked interface is: "+invokedInterface.getName());

        if (invokedInterface == null){
            throw new IllegalStateException("Business interface not set into ThreadContext.");
        }
        return invokedInterface;
    }
}
