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
import org.apache.openejb.core.ivm.IntraVmArtifact;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.timer.TimerServiceImpl;
import org.apache.openejb.core.transaction.EjbUserTransaction;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.spi.SecurityService;

import javax.ejb.EJBContext;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.TimerService;
import javax.interceptor.InvocationContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.security.Identity;
import java.security.Principal;
import java.util.Map;
import java.util.Properties;


/**
 * @version $Rev$ $Date$
 */
public abstract class BaseContext implements EJBContext, Serializable {

    public static enum Call {
        getEJBObject, getEJBLocalObject, isCallerInRole, setRollbackOnly, getCallerPrincipal, getRollbackOnly, getTimerService, getUserTransaction, getBusinessObject, timerMethod, getInvokedBusinessInterface, UserTransactionMethod, getMessageContext, getPrimaryKey, getContextData

    }

    protected final SecurityService securityService;
    protected final UserTransaction userTransaction;

    protected BaseContext(SecurityService securityService) {
        this(securityService, new EjbUserTransaction());
    }

    protected BaseContext(SecurityService securityService, UserTransaction userTransaction) {
        this.securityService = securityService;
        this.userTransaction = new UserTransactionWrapper(userTransaction);
    }

    public abstract void check(Call call);

    protected IllegalStateException illegal(Call call, Operation operation) {
        return new IllegalStateException(call + " cannot be called in " + operation);
    }

    public Map<String, Object> getContextData() {
        check(Call.getContextData);
        return ThreadContext.getThreadContext().get(InvocationContext.class).getContextData();
    }

    public EJBHome getEJBHome() {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        BeanContext di = threadContext.getBeanContext();

        return di.getEJBHome();
    }

    public EJBLocalHome getEJBLocalHome() {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        BeanContext di = threadContext.getBeanContext();

        return di.getEJBLocalHome();
    }

    public Principal getCallerPrincipal() {
        check(Call.getCallerPrincipal);
        Principal callerPrincipal = getCallerPrincipal(securityService);
        if (callerPrincipal == null) callerPrincipal = UnauthenticatedPrincipal.INSTANCE;
        return callerPrincipal;
    }

    protected Principal getCallerPrincipal(SecurityService securityService) {
        return securityService.getCallerPrincipal();
    }

    @Override
    public boolean isCallerInRole(String s) {
        check(Call.isCallerInRole);
        return isCallerInRole(securityService, s);
    }

    protected boolean isCallerInRole(SecurityService securityService, String roleName) {
        check(Call.isCallerInRole);
        return securityService.isCallerInRole(roleName);
    }

    @Override
    public UserTransaction getUserTransaction() throws IllegalStateException {
        check(Call.getUserTransaction);
        return getUserTransaction(userTransaction);
    }

    public UserTransaction getUserTransaction(UserTransaction userTransaction) throws IllegalStateException {

        ThreadContext threadContext = ThreadContext.getThreadContext();
        BeanContext di = threadContext.getBeanContext();

        if (di.isBeanManagedTransaction()) {
            return userTransaction;
        } else {
            throw new IllegalStateException("container-managed transaction beans can not access the UserTransaction");
        }
    }

    public void setRollbackOnly() throws IllegalStateException {
        check(Call.setRollbackOnly);
        ThreadContext threadContext = ThreadContext.getThreadContext();
        BeanContext di = threadContext.getBeanContext();

        if (di.isBeanManagedTransaction()) {
            throw new IllegalStateException("bean-managed transaction beans can not access the setRollbackOnly() method");
        }

        TransactionPolicy txPolicy = threadContext.getTransactionPolicy();
        if (txPolicy == null) {
            throw new IllegalStateException("ThreadContext does not contain a TransactionEnvironment");
        }
        if (txPolicy.getTransactionType() == TransactionType.Never
                || txPolicy.getTransactionType() == TransactionType.NotSupported
                || txPolicy.getTransactionType() == TransactionType.Supports) {
            throw new IllegalStateException("setRollbackOnly accessible only from MANDATORY, REQUIRED, or REQUIRES_NEW");
        }
        txPolicy.setRollbackOnly();
    }

    public boolean getRollbackOnly() throws IllegalStateException {
        check(Call.getRollbackOnly);
        ThreadContext threadContext = ThreadContext.getThreadContext();
        BeanContext di = threadContext.getBeanContext();

        if (di.isBeanManagedTransaction()) {
            throw new IllegalStateException("bean-managed transaction beans can not access the getRollbackOnly() method: deploymentId=" + di.getDeploymentID());
        }

        TransactionPolicy txPolicy = threadContext.getTransactionPolicy();
        if (txPolicy == null) {
            throw new IllegalStateException("ThreadContext does not contain a TransactionEnvironment");
        }
        if (txPolicy.getTransactionType() == TransactionType.Never
                || txPolicy.getTransactionType() == TransactionType.NotSupported
                || txPolicy.getTransactionType() == TransactionType.Supports) {
            throw new IllegalStateException("getRollbackOnly accessible only from MANDATORY, REQUIRED, or REQUIRES_NEW");
        }
        return txPolicy.isRollbackOnly();
    }

    public TimerService getTimerService() throws IllegalStateException {
        check(Call.getTimerService);

        ThreadContext threadContext = ThreadContext.getThreadContext();
        BeanContext beanContext = threadContext.getBeanContext();
        EjbTimerService timerService = beanContext.getEjbTimerService();
        if (timerService == null) {
            throw new IllegalStateException("This ejb does not support timers " + beanContext.getDeploymentID());
        }
        return new TimerServiceImpl(timerService, threadContext.getPrimaryKey(), beanContext.getEjbTimeout());
    }

    public boolean isTimerMethodAllowed() {
        return true;
    }

    public boolean isUserTransactionAccessAllowed() {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        BeanContext di = threadContext.getBeanContext();

        check(Call.UserTransactionMethod);
        return di.isBeanManagedTransaction();
    }


    public final Properties getEnvironment() {
        throw new UnsupportedOperationException();
    }

    public final Identity getCallerIdentity() {
        throw new UnsupportedOperationException();
    }

    public final boolean isCallerInRole(Identity identity) {
        throw new UnsupportedOperationException();
    }

    public Object lookup(String name) {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        BeanContext beanContext = threadContext.getBeanContext();
        Context jndiEnc = beanContext.getJndiEnc();
        try {
            jndiEnc = (Context) jndiEnc.lookup("comp/env");
            return jndiEnc.lookup(name);
        } catch (NamingException e) {
            throw new IllegalArgumentException(e);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public class UserTransactionWrapper implements UserTransaction {
        private UserTransaction userTransaction;

        public UserTransactionWrapper(UserTransaction userTransaction) {
            this.userTransaction = userTransaction;
        }

        public void begin() throws NotSupportedException, SystemException {
            if (!isUserTransactionAccessAllowed()) {
                throw new IllegalStateException();
            }
            userTransaction.begin();
        }

        public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
            if (!isUserTransactionAccessAllowed()) {
                throw new IllegalStateException();
            }
            userTransaction.commit();
        }

        public int getStatus() throws SystemException {
            if (!isUserTransactionAccessAllowed()) {
                throw new IllegalStateException();
            }
            return userTransaction.getStatus();
        }

        public void rollback() throws IllegalStateException, SecurityException, SystemException {
            if (!isUserTransactionAccessAllowed()) {
                throw new IllegalStateException();
            }
            userTransaction.rollback();
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            if (!isUserTransactionAccessAllowed()) {
                throw new IllegalStateException();
            }
            userTransaction.setRollbackOnly();
        }

        public void setTransactionTimeout(int i) throws SystemException {
            if (!isUserTransactionAccessAllowed()) {
                throw new IllegalStateException();
            }
            userTransaction.setTransactionTimeout(i);
        }
    }

    public static class State {
    }

    protected Object writeReplace() throws ObjectStreamException {
        return new IntraVmArtifact(this, true);
    }
}
