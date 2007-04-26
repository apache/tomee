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

import java.io.Serializable;
import java.io.ObjectStreamException;
import java.security.Identity;
import java.security.Principal;
import java.util.List;
import java.util.Properties;
import javax.ejb.EJBContext;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.TimerService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.timer.TimerServiceImpl;
import org.apache.openejb.core.ivm.IntraVmCopyMonitor;
import org.apache.openejb.core.ivm.IntraVmArtifact;
import org.apache.openejb.spi.SecurityService;


/**
 * @version $Rev$ $Date$
 */
public abstract class BaseContext implements EJBContext, Serializable {

    private final UserTransaction userTransaction;
    private final SecurityService securityService;
    private final TransactionManager transactionManager;

    public BaseContext(TransactionManager transactionManager, SecurityService securityService) {
        this.transactionManager = transactionManager;
        this.securityService = securityService;
        this.userTransaction = new CoreUserTransaction(transactionManager);
    }

    protected BaseContext(TransactionManager transactionManager, SecurityService securityService, UserTransaction userTransaction) {
        this.transactionManager = transactionManager;
        this.securityService = securityService;
        this.userTransaction = userTransaction;
    }

    protected static State[] states;
    
    public static State[] getStates() {
        return states;
    }
    
    protected abstract State getState();

    public EJBHome getEJBHome() {
        return getState().getEJBHome();
    }

    public EJBLocalHome getEJBLocalHome() {
        return getState().getEJBLocalHome();
    }

    public final Properties getEnvironment() {
        throw new UnsupportedOperationException();
    }

    public final Identity getCallerIdentity() {
        throw new UnsupportedOperationException();
    }

    public Principal getCallerPrincipal() {
        return getState().getCallerPrincipal(securityService);
    }

    public final boolean isCallerInRole(Identity identity) {
        throw new UnsupportedOperationException();
    }

    public boolean isCallerInRole(String roleName) {
        return getState().isCallerInRole(securityService, roleName);
    }

    public UserTransaction getUserTransaction() throws IllegalStateException {
        return getState().getUserTransaction(userTransaction);
    }

    public void setRollbackOnly() throws IllegalStateException {
        getState().setRollbackOnly(transactionManager);
    }

    public boolean getRollbackOnly() throws IllegalStateException {
        return getState().getRollbackOnly(transactionManager);
    }

    public TimerService getTimerService() throws IllegalStateException {
        return getState().getTimerService();
    }

    public Object lookup(String name) {
        try {
            return (new InitialContext()).lookup("java:comp/env/" + name);
        } catch (NamingException ne) {
            throw new IllegalArgumentException(ne);
        }
    }

    public boolean isUserTransactionAccessAllowed() {
        return getState().isUserTransactionAccessAllowed();
    }

    public boolean isMessageContextAccessAllowed() {
        return getState().isMessageContextAccessAllowed();
    }

    public boolean isJNDIAccessAllowed() {
        return getState().isJNDIAccessAllowed();
    }

    public boolean isEntityManagerFactoryAccessAllowed() {
        return getState().isEntityManagerFactoryAccessAllowed();
    }

    public boolean isEntityManagerAccessAllowed() {
        return getState().isEntityManagerAccessAllowed();
    }

    public boolean isTimerAccessAllowed() {
        return getState().isTimerAccessAllowed();
    }

    public static boolean isTimerMethodAllowed() {
        State[] currentStates = ThreadContext.getThreadContext().getCurrentAllowedStates();
        State currentState = currentStates[ThreadContext.getThreadContext().getCurrentOperation().ordinal()];
        
        return currentState.isTimerMethodAllowed();
    }
    
    public static class State {

        public EJBHome getEJBHome() {
            ThreadContext threadContext = ThreadContext.getThreadContext();
            CoreDeploymentInfo di = threadContext.getDeploymentInfo();

            return di.getEJBHome();
        }

        public EJBLocalHome getEJBLocalHome() {
            ThreadContext threadContext = ThreadContext.getThreadContext();
            CoreDeploymentInfo di = threadContext.getDeploymentInfo();

            return di.getEJBLocalHome();
        }

        public Principal getCallerPrincipal(SecurityService securityService) {
            return securityService.getCallerPrincipal();
        }

        public boolean isCallerInRole(SecurityService securityService, String roleName) {
            return securityService.isCallerInRole(roleName);
        }

        public UserTransaction getUserTransaction(UserTransaction userTransaction) throws IllegalStateException {
            ThreadContext threadContext = ThreadContext.getThreadContext();
            DeploymentInfo di = threadContext.getDeploymentInfo();

            if (di.isBeanManagedTransaction()) {
                return userTransaction;
            } else {
                throw new IllegalStateException("container-managed transaction beans can not access the UserTransaction");
            }
        }

        public void setRollbackOnly(TransactionManager transactionManager) throws IllegalStateException {
            ThreadContext threadContext = ThreadContext.getThreadContext();
            DeploymentInfo di = threadContext.getDeploymentInfo();

            if (di.isBeanManagedTransaction()) {
                throw new IllegalStateException("bean-managed transaction beans can not access the setRollbackOnly() method");
            }

            try {
                transactionManager.setRollbackOnly();
            } catch (SystemException se) {
                throw new RuntimeException("Transaction service has thrown a SystemException");
            }
        }

        public boolean getRollbackOnly(TransactionManager transactionManager) throws IllegalStateException {
            ThreadContext threadContext = ThreadContext.getThreadContext();
            DeploymentInfo di = threadContext.getDeploymentInfo();

            if (di.isBeanManagedTransaction()) {
                throw new IllegalStateException("bean-managed transaction beans can not access the getRollbackOnly() method: deploymentId=" + di.getDeploymentID());
            }

            try {
                int status = transactionManager.getStatus();
                if (status == Status.STATUS_MARKED_ROLLBACK || status == Status.STATUS_ROLLEDBACK) {
                    return true;
                } else if (status == Status.STATUS_NO_TRANSACTION) {
                    // this would be true for Supports tx attribute where no tx was propagated
                    throw new IllegalStateException("No current transaction");
                } else {
                    return false;
                }
            } catch (SystemException se) {
                throw new RuntimeException("Transaction service has thrown a SystemException");
            }
        }

        public TimerService getTimerService() throws IllegalStateException {
            ThreadContext threadContext = ThreadContext.getThreadContext();
            DeploymentInfo deploymentInfo = threadContext.getDeploymentInfo();
            EjbTimerService timerService = deploymentInfo.getEjbTimerService();
            if (timerService == null) {
                throw new IllegalStateException("This ejb does not support timers " + deploymentInfo.getDeploymentID());
            }
            return new TimerServiceImpl(timerService, threadContext.getPrimaryKey());
        }

        public boolean isTimerAccessAllowed() {
            return true;
        }

        public boolean isTimerMethodAllowed() {
            return true;
        }

        public boolean isUserTransactionAccessAllowed() {
            ThreadContext threadContext = ThreadContext.getThreadContext();
            DeploymentInfo di = threadContext.getDeploymentInfo();

            return di.isBeanManagedTransaction();
        }

        public boolean isMessageContextAccessAllowed() {
            return true;
        }

        public boolean isJNDIAccessAllowed() {
            return true;
        }

        public boolean isEntityManagerFactoryAccessAllowed() {
            return true;
        }

        public boolean isEntityManagerAccessAllowed() {
            return true;
        }

    }

    protected Object writeReplace() throws ObjectStreamException {
        return new IntraVmArtifact(this, true);
    }
}
