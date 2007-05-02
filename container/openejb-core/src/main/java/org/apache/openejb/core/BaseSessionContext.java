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

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.List;
import java.util.ArrayList;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.InternalErrorException;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.core.ivm.IntraVmProxy;
import org.apache.openejb.core.stateless.StatelessEjbObjectHandler;
import org.apache.openejb.core.stateful.StatefulEjbObjectHandler;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.proxy.ProxyManager;


/**
 * @version $Rev$ $Date$
 */
public abstract class BaseSessionContext extends BaseContext implements SessionContext {

    public BaseSessionContext(TransactionManager transactionManager, SecurityService securityService) {
        super(transactionManager, securityService);
    }

    public BaseSessionContext(TransactionManager transactionManager, SecurityService securityService, UserTransaction userTransaction) {
        super(transactionManager, securityService, userTransaction);
    }

    public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
        return ((SessionState) getState()).getEJBLocalObject();
    }

    public EJBObject getEJBObject() throws IllegalStateException {
        return ((SessionState) getState()).getEJBObject();
    }

    public MessageContext getMessageContext() throws IllegalStateException {
        return ((SessionState) getState()).getMessageContext();
    }

    public Object getBusinessObject(Class aClass) {
        return ((SessionState) getState()).getBusinessObject(aClass);
    }

    public Class getInvokedBusinessInterface() {
        return ((SessionState) getState()).getInvokedBusinessInterface();
    }

    protected static class SessionState extends State {

        public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
            ThreadContext threadContext = ThreadContext.getThreadContext();
            DeploymentInfo di = threadContext.getDeploymentInfo();

            EjbObjectProxyHandler handler = new StatelessEjbObjectHandler(di, threadContext.getPrimaryKey(), InterfaceType.EJB_LOCAL, new ArrayList<Class>());
            try {
                Class[] interfaces = new Class[]{di.getLocalInterface(), IntraVmProxy.class};
                return (EJBLocalObject) ProxyManager.newProxyInstance(interfaces, handler);
            } catch (IllegalAccessException iae) {
                throw new InternalErrorException("Could not create IVM proxy for " + di.getLocalInterface() + " interface", iae);
            }
        }

        public EJBObject getEJBObject() throws IllegalStateException {
            ThreadContext threadContext = ThreadContext.getThreadContext();
            DeploymentInfo di = threadContext.getDeploymentInfo();

            EjbObjectProxyHandler handler = new StatelessEjbObjectHandler(di, threadContext.getPrimaryKey(), InterfaceType.EJB_OBJECT, new ArrayList<Class>());
            try {
                Class[] interfaces = new Class[]{di.getRemoteInterface(), IntraVmProxy.class};
                return (EJBObject) ProxyManager.newProxyInstance(interfaces, handler);
            } catch (IllegalAccessException iae) {
                throw new InternalErrorException("Could not create IVM proxy for " + di.getLocalInterface() + " interface", iae);
            }
        }

        public MessageContext getMessageContext() throws IllegalStateException {
            ThreadContext threadContext = ThreadContext.getThreadContext();
            MessageContext messageContext = threadContext.get(MessageContext.class);
            if (messageContext == null) throw new IllegalStateException("Only calls on the service-endpoint have a MessageContext.");
            return messageContext;
        }

        public Object getBusinessObject(Class interfce) {
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
                    default: throw new IllegalStateException("Bean is not a session bean: "+di.getComponentType());
                }

                List<Class> interfaces = new ArrayList<Class>();
                interfaces.addAll(di.getInterfaces(interfaceType));
                interfaces.add(IntraVmProxy.class);
                return ProxyManager.newProxyInstance(interfaces.toArray(new Class[]{}), handler);
            } catch (IllegalAccessException iae) {
                throw new InternalErrorException("Could not create IVM proxy for " + interfce.getName() + " interface", iae);
            }
        }

        public Class getInvokedBusinessInterface() {
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

    /**
     * Dependency injection methods (e.g., setSessionContext)
     */
    public static class InjectionSessionState extends SessionState {

        public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public EJBObject getEJBObject() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public MessageContext getMessageContext() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public Object getBusinessObject(Class interfce) {
            throw new IllegalStateException();
        }

        public Class getInvokedBusinessInterface() {
            throw new IllegalStateException();
        }

        public Principal getCallerPrincipal(SecurityService securityService) {
            throw new IllegalStateException();
        }

        public boolean isCallerInRole(SecurityService securityService, String roleName) {
            throw new IllegalStateException();
        }

        public UserTransaction getUserTransaction(UserTransaction userTransaction) throws IllegalStateException {
            throw new IllegalStateException();
        }

        public void setRollbackOnly(TransactionManager transactionManager) throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean getRollbackOnly(TransactionManager transactionManager) throws IllegalStateException {
            throw new IllegalStateException();
        }

        public TimerService getTimerService() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean isUserTransactionAccessAllowed() {
            return false;
        }

        public boolean isMessageContextAccessAllowed() {
            return false;
        }

        public boolean isEntityManagerFactoryAccessAllowed() {
            return false;
        }

        public boolean isEntityManagerAccessAllowed() {
            return false;
        }

        public boolean isTimerAccessAllowed() {
            return false;
        }
    }

    /**
     * PostConstruct, Pre-Destroy lifecycle callback interceptor methods
     */
    public static class LifecycleSessionState extends SessionState {

        public MessageContext getMessageContext() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public Class getInvokedBusinessInterface() {
            throw new IllegalStateException();
        }

        public Principal getCallerPrincipal(SecurityService securityService) {
            throw new IllegalStateException();
        }

        public boolean isCallerInRole(SecurityService securityService, String roleName) {
            throw new IllegalStateException();
        }

        public void setRollbackOnly(TransactionManager transactionManager) throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean getRollbackOnly(TransactionManager transactionManager) throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean isUserTransactionAccessAllowed() {
            return false;
        }

        public boolean isMessageContextAccessAllowed() {
            return false;
        }

        public boolean isJNDIAccessAllowed() {
            return false;
        }

        public boolean isEntityManagerFactoryAccessAllowed() {
            return false;
        }

        public boolean isEntityManagerAccessAllowed() {
            return false;
        }

        public boolean isTimerAccessAllowed() {
            return false;
        }

        public boolean isTimerMethodAllowed() {
            return false;
        }
    }

    /**
     * Business method from business interface or component interface; business
     * method interceptor method
     */
    public static class BusinessSessionState extends SessionState {

        public MessageContext getMessageContext() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean isMessageContextAccessAllowed() {
            return false;
        }
    }

    /**
     * Timeout callback method
     */
    public static class TimeoutSessionState extends SessionState {

        public Class getInvokedBusinessInterface() {
            throw new IllegalStateException();
        }

        public MessageContext getMessageContext() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean isMessageContextAccessAllowed() {
            return false;
        }
    }

}
