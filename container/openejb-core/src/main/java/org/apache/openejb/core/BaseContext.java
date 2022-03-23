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
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.ivm.IntraVmArtifact;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.timer.TimerServiceImpl;
import org.apache.openejb.core.timer.Timers;
import org.apache.openejb.core.transaction.EjbUserTransaction;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.threads.task.CUTask;

import jakarta.ejb.EJBContext;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerService;
import jakarta.interceptor.InvocationContext;
import javax.naming.Context;
import javax.naming.NamingException;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.security.Identity;
import java.security.Principal;
import java.util.Collection;
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

    protected BaseContext(final SecurityService securityService) {
        this(securityService, new EjbUserTransaction());
    }

    protected BaseContext(final SecurityService securityService, final UserTransaction userTransaction) {
        this.securityService = securityService;
        this.userTransaction = new UserTransactionWrapper(userTransaction);
    }

    private boolean isAsyncOperation(final ThreadContext threadContext) {
        if (threadContext.getCurrentOperation() == null
            && threadContext.get(CUTask.Context.class) != null) {
            return true;
        }
        return false;
    }

    protected abstract void check(ThreadContext context, Call call);

    protected IllegalStateException illegal(final Call call, final Operation operation) {
        return new IllegalStateException(call + " cannot be called in " + operation);
    }

    public Map<String, Object> getContextData() {
        doCheck(Call.getContextData);
        return ThreadContext.getThreadContext().get(InvocationContext.class).getContextData();
    }

    public void doCheck(final Call call) {
        final ThreadContext context = ThreadContext.getThreadContext();
        if (!isAsyncOperation(context)) {
            check(context, call);
        }
    }

    public EJBHome getEJBHome() {
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext di = threadContext.getBeanContext();

        return di.getEJBHome();
    }

    public EJBLocalHome getEJBLocalHome() {
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext di = threadContext.getBeanContext();

        return di.getEJBLocalHome();
    }

    public Principal getCallerPrincipal() {
        doCheck(Call.getCallerPrincipal);
        Principal callerPrincipal = getCallerPrincipal(securityService);
        if (callerPrincipal == null) {
            callerPrincipal = UnauthenticatedPrincipal.INSTANCE;
        }
        return callerPrincipal;
    }

    protected Principal getCallerPrincipal(final SecurityService securityService) {
        return securityService.getCallerPrincipal();
    }

    @Override
    public boolean isCallerInRole(final String s) {
        doCheck(Call.isCallerInRole);
        return isCallerInRole(securityService, s);
    }

    protected boolean isCallerInRole(final SecurityService securityService, final String roleName) {
        doCheck(Call.isCallerInRole);

        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext di = threadContext.getBeanContext();
        final String roleLink = di.getSecurityRoleReference(roleName);

        return securityService.isCallerInRole(roleLink);
    }

    @Override
    public UserTransaction getUserTransaction() throws IllegalStateException {
        doCheck(Call.getUserTransaction);
        return getUserTransaction(userTransaction);
    }

    public UserTransaction getUserTransaction(final UserTransaction userTransaction) throws IllegalStateException {

        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext di = threadContext.getBeanContext();

        if (di.isBeanManagedTransaction()) {
            return userTransaction;
        } else {
            throw new IllegalStateException("container-managed transaction beans can not access the UserTransaction");
        }
    }

    public void setRollbackOnly() throws IllegalStateException {
        doCheck(Call.setRollbackOnly);
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext di = threadContext.getBeanContext();

        if (di.isBeanManagedTransaction()) {
            throw new IllegalStateException("bean-managed transaction beans can not access the setRollbackOnly() method");
        }

        final TransactionPolicy txPolicy = threadContext.getTransactionPolicy();
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
        doCheck(Call.getRollbackOnly);
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext di = threadContext.getBeanContext();

        if (di.isBeanManagedTransaction()) {
            throw new IllegalStateException("bean-managed transaction beans can not access the getRollbackOnly() method: deploymentId=" + di.getDeploymentID());
        }

        final TransactionPolicy txPolicy = threadContext.getTransactionPolicy();
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
        doCheck(Call.getTimerService);

        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext beanContext = threadContext.getBeanContext();
        final EjbTimerService timerService = beanContext.getEjbTimerService();
        if (timerService == null) {
            throw new IllegalStateException("This ejb does not support timers " + beanContext.getDeploymentID());
        } else if (!timerService.isStarted()) {
            try {
                timerService.start();
            } catch (final OpenEJBException e) {
                throw new IllegalStateException(e);
            }
        }
        return new TimerServiceImpl(timerService, threadContext.getPrimaryKey(), beanContext.getEjbTimeout()) {
            @Override
            public Collection<Timer> getAllTimers() throws IllegalStateException, EJBException {
                return Timers.all(); // allowed here
            }
        };
    }

    public boolean isTimerMethodAllowed() {
        return true;
    }

    public boolean isUserTransactionAccessAllowed() {
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext di = threadContext.getBeanContext();

        doCheck(Call.UserTransactionMethod);
        return di.isBeanManagedTransaction();
    }


    public final Properties getEnvironment() {
        throw new UnsupportedOperationException();
    }

    public final Identity getCallerIdentity() {
        throw new UnsupportedOperationException();
    }

    public final boolean isCallerInRole(final Identity identity) {
        throw new UnsupportedOperationException();
    }

    public Object lookup(final String name) {
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext beanContext = threadContext.getBeanContext();
        Context jndiEnc = beanContext.getJndiEnc();
        try {
            jndiEnc = (Context) jndiEnc.lookup("comp/env");
            return jndiEnc.lookup(name);
        } catch (final NamingException | RuntimeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public class UserTransactionWrapper implements UserTransaction {
        private final UserTransaction userTransaction;

        public UserTransactionWrapper(final UserTransaction userTransaction) {
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

        public void setTransactionTimeout(final int i) throws SystemException {
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
