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
package org.apache.openejb;

import java.security.Identity;
import java.security.Principal;
import java.util.Properties;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.TimerService;
import javax.security.auth.Subject;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.geronimo.security.ContextManager;

/**
 * Implementation of EJBContext that uses the State pattern to determine
 * which operations can be performed for a given EJB.
 *
 * @version $Revision$ $Date$
 */
public abstract class EJBContextImpl {
    protected final EJBInstanceContext context;
    private final UserTransaction userTransaction;
    private final TransactionManager transactionManager;
    private Subject callerSubject;
    protected EJBContextState state;

    public EJBContextImpl(EJBInstanceContext context, TransactionManager transactionManager, UserTransaction userTransaction) {
        this.context = context;
        this.userTransaction = userTransaction;
        this.transactionManager = transactionManager;
    }

    public Subject getCallerSubject() {
        return callerSubject;
    }

    public void setCallerSubject(Subject callerSubject) {
        this.callerSubject = callerSubject;
    }

    public EJBHome getEJBHome() {
        return state.getEJBHome(context);
    }

    public EJBLocalHome getEJBLocalHome() {
        return state.getEJBLocalHome(context);
    }

    public EJBObject getEJBObject() throws IllegalStateException {
        return state.getEJBObject(context);
    }

    public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
        return state.getEJBLocalObject(context);
    }

    public Principal getCallerPrincipal() {
        return state.getCallerPrincipal(callerSubject);
    }

    public boolean isCallerInRole(String s) {
        return state.isCallerInRole(s, context);
    }

    public UserTransaction getUserTransaction() {
        // handle the getUserTransaction directly as it is anoying and always allowed
        if (userTransaction == null) {
            throw new IllegalStateException("getUserTransaction is not allowed when using Container Managed Transactions");
        }
        return state.getUserTransaction(userTransaction);
    }

    public void setRollbackOnly() {
        if (userTransaction != null) {
            throw new IllegalStateException("Calls to setRollbackOnly are not allowed for EJBs with bean-managed transaction demarcation");
        }
        state.setRollbackOnly(context, transactionManager);
    }

    public boolean getRollbackOnly() {
        if (userTransaction != null) {
            throw new IllegalStateException("Calls to getRollbackOnly are not allowed for EJBs with bean-managed transaction demarcation");
        }
        return state.getRollbackOnly(context, transactionManager);
    }

    public TimerService getTimerService() {
        return state.getTimerService(context);
    }

    public Properties getEnvironment() {
        throw new EJBException("getEnvironment is no longer supported; use JNDI instead");
    }

    public Identity getCallerIdentity() {
        throw new EJBException("getCallerIdentity is no longer supported; use getCallerPrincipal instead");
    }

    public boolean isCallerInRole(Identity identity) {
        throw new EJBException("isCallerInRole(Identity role) is no longer supported; use isCallerInRole(String roleName) instead");
    }

    public abstract static class EJBContextState {
        public EJBHome getEJBHome(EJBInstanceContext context) {
            if( context.getProxyFactory() !=null ) {
                EJBHome home = context.getProxyFactory().getEJBHome();
                return home;
            }
            throw new IllegalStateException("getEJBHome is not allowed if no home interface is defined");
        }

        public EJBLocalHome getEJBLocalHome(EJBInstanceContext context) {
            if( context.getProxyFactory() !=null ) {
                EJBLocalHome localHome = context.getProxyFactory().getEJBLocalHome();
                return localHome;
            }
            throw new IllegalStateException("getEJBLocalHome is not allowed if no local localHome interface is defined");
        }

        public EJBObject getEJBObject(EJBInstanceContext context) {
            if( context.getProxyFactory() !=null ) {
                EJBObject remote = context.getProxyFactory().getEJBObject(context.getId());
                return remote;
            }
            throw new IllegalStateException("getEJBObject is not allowed if no remote interface is defined");
        }

        public EJBLocalObject getEJBLocalObject(EJBInstanceContext context) {
            if( context.getProxyFactory() !=null ) {
                EJBLocalObject local = context.getProxyFactory().getEJBLocalObject(context.getId());
                return local;
            }
            throw new IllegalStateException("getEJBLocalObject is not allowed if no local interface is defined");
        }

        public Principal getCallerPrincipal(Subject callerSubject) {
            return ContextManager.getCurrentPrincipal(callerSubject);
        }

        public boolean isCallerInRole(String s, EJBInstanceContext context) {
            if( context.getProxyFactory() !=null ) {
                return ContextManager.isCallerInRole(context.getProxyFactory().getEJBName(), s);
            }
            throw new IllegalStateException("isCallerInRole is not allowed if no local or remote interface is defined");
        }

        public UserTransaction getUserTransaction(UserTransaction userTransaction) {
            return userTransaction;
        }

        public void setRollbackOnly(EJBInstanceContext context, TransactionManager transactionManager) {
            try {
                Transaction transaction = transactionManager.getTransaction();
                if (transaction == null) {
                    throw new IllegalStateException("There is no transaction in progess.");
                }
                transaction.setRollbackOnly();
            } catch (SystemException e) {
                throw new EJBException(e);
            }
        }

        public boolean getRollbackOnly(EJBInstanceContext context, TransactionManager transactionManager) {
            try {
                int status = transactionManager.getStatus();
                return (status == Status.STATUS_MARKED_ROLLBACK ||
                        status == Status.STATUS_ROLLEDBACK ||
                        status == Status.STATUS_ROLLING_BACK);
            } catch (SystemException e) {
                throw new EJBException(e);
            }
        }

        public TimerService getTimerService(EJBInstanceContext context) {
            TimerService timerService = context.getTimerService();
            if (timerService == null) {
                //TODO is this correct?
                throw new IllegalStateException("EJB does not implement EJBTimeout");
            }
            return timerService;
        }
    }
}
