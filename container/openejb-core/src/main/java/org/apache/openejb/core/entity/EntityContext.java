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
package org.apache.openejb.core.entity;

import java.security.Principal;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.TimerService;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.InternalErrorException;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.core.ivm.IntraVmProxy;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.proxy.ProxyManager;


/**
 * @version $Rev$ $Date$
 */
public class EntityContext extends BaseContext implements javax.ejb.EntityContext {

    public EntityContext(TransactionManager transactionManager, SecurityService securityService) {
        super(transactionManager, securityService);
    }

    protected EntityContext(TransactionManager transactionManager, SecurityService securityService, UserTransaction userTransaction) {
        super(transactionManager, securityService, userTransaction);
    }

    public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
        return ((javax.ejb.EntityContext) getState()).getEJBLocalObject();
    }

    public EJBObject getEJBObject() throws IllegalStateException {
        return ((javax.ejb.EntityContext) getState()).getEJBObject();
    }

    public Object getPrimaryKey() throws IllegalStateException {
        return ((javax.ejb.EntityContext) getState()).getPrimaryKey();
    }

    private static class EntityState extends State {

        public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
            ThreadContext threadContext = ThreadContext.getThreadContext();
            DeploymentInfo di = threadContext.getDeploymentInfo();

            EjbObjectProxyHandler handler = new EntityEjbObjectHandler((RpcContainer) di.getContainer(), threadContext.getPrimaryKey(), di.getDeploymentID(), InterfaceType.EJB_LOCAL);
            handler.setLocal(true);
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

            EjbObjectProxyHandler handler = new EntityEjbObjectHandler((RpcContainer) di.getContainer(), threadContext.getPrimaryKey(), di.getDeploymentID(), InterfaceType.EJB_OBJECT);
            try {
                Class[] interfaces = new Class[]{di.getRemoteInterface(), IntraVmProxy.class};
                return (EJBObject) ProxyManager.newProxyInstance(interfaces, handler);
            } catch (IllegalAccessException iae) {
                throw new InternalErrorException("Could not create IVM proxy for " + di.getLocalInterface() + " interface", iae);
            }
        }

        public Object getPrimaryKey() throws IllegalStateException {
            ThreadContext threadContext = ThreadContext.getThreadContext();
            return threadContext.getPrimaryKey();
        }
    }

    protected final static EntityState CONTEXT = new EntityState() {

        public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public EJBObject getEJBObject() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public Object getPrimaryKey() throws IllegalStateException {
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

        public boolean isResourceManagerAccessAllowed() {
            return false;
        }

        public boolean isEnterpriseBeanAccessAllowed() {
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
    };

    protected final static EntityState CREATE = new EntityState() {

        public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public EJBObject getEJBObject() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public Object getPrimaryKey() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public UserTransaction getUserTransaction(UserTransaction userTransaction) throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean isUserTransactionAccessAllowed() {
            return false;
        }

        public boolean isMessageContextAccessAllowed() {
            return false;
        }

        public boolean isTimerAccessAllowed() {
            return false;
        }
    };

    protected final static EntityState LIFECYCLE_BUSINESS_TIMEOUT = new EntityState() {

        public UserTransaction getUserTransaction(UserTransaction userTransaction) throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean isUserTransactionAccessAllowed() {
            return false;
        }

        public boolean isMessageContextAccessAllowed() {
            return false;
        }
    };

    protected final static EntityState FIND = new EntityState() {

        public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public EJBObject getEJBObject() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public Object getPrimaryKey() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public UserTransaction getUserTransaction(UserTransaction userTransaction) throws IllegalStateException {
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

        public boolean isTimerAccessAllowed() {
            return false;
        }
    };

    protected final static EntityState HOME = new EntityState() {

        public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public EJBObject getEJBObject() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public Object getPrimaryKey() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public UserTransaction getUserTransaction(UserTransaction userTransaction) throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean isUserTransactionAccessAllowed() {
            return false;
        }

        public boolean isMessageContextAccessAllowed() {
            return false;
        }

        public boolean isTimerAccessAllowed() {
            return false;
        }
    };

    protected final static EntityState ACTIVATE_PASSIVATE = new EntityState() {

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

        public boolean isUserTransactionAccessAllowed() {
            return false;
        }

        public boolean isMessageContextAccessAllowed() {
            return false;
        }

        public boolean isResourceManagerAccessAllowed() {
            return false;
        }

        public boolean isEnterpriseBeanAccessAllowed() {
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
    };

    static {
        states[Operation.SET_CONTEXT.ordinal()] = CONTEXT;
        states[Operation.UNSET_CONTEXT.ordinal()] = CONTEXT;
        states[Operation.CREATE.ordinal()] = CREATE;
        states[Operation.POST_CREATE.ordinal()] = LIFECYCLE_BUSINESS_TIMEOUT;
        states[Operation.REMOVE.ordinal()] = LIFECYCLE_BUSINESS_TIMEOUT;
        states[Operation.FIND.ordinal()] = FIND;
        states[Operation.HOME.ordinal()] = HOME;
        states[Operation.ACTIVATE.ordinal()] = ACTIVATE_PASSIVATE;
        states[Operation.PASSIVATE.ordinal()] = ACTIVATE_PASSIVATE;
        states[Operation.LOAD.ordinal()] = LIFECYCLE_BUSINESS_TIMEOUT;
        states[Operation.STORE.ordinal()] = LIFECYCLE_BUSINESS_TIMEOUT;
        states[Operation.BUSINESS.ordinal()] = LIFECYCLE_BUSINESS_TIMEOUT;
        states[Operation.TIMEOUT.ordinal()] = LIFECYCLE_BUSINESS_TIMEOUT;
    }
}
