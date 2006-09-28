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
package org.apache.openejb.sfsb;

import javax.ejb.EnterpriseBean;
import javax.ejb.SessionContext;
import javax.ejb.SessionSynchronization;
import javax.ejb.Timer;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.timer.PersistentTimer;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.openejb.CallbackMethod;
import org.apache.openejb.ConnectionTrackingInterceptor;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.EjbCallbackInvocation;
import org.apache.openejb.ExtendedEjbDeployment;
import org.apache.openejb.StatefulEjbContainer;
import org.apache.openejb.SystemExceptionInterceptor;
import org.apache.openejb.core.CoreUserTransaction;
import org.apache.openejb.dispatch.DispatchInterceptor;
import org.apache.openejb.naming.ComponentContextInterceptor;
import org.apache.openejb.security.DefaultSubjectInterceptor;
import org.apache.openejb.security.EJBIdentityInterceptor;
import org.apache.openejb.security.EjbRunAsInterceptor;
import org.apache.openejb.security.EjbSecurityInterceptor;
import org.apache.openejb.security.PolicyContextHandlerEJBInterceptor;
import org.apache.openejb.transaction.TransactionPolicyInterceptor;
import org.apache.openejb.transaction.TransactionContextInterceptor;


/**
 * @version $Revision$ $Date$
 */
public class DefaultStatefulEjbContainer implements StatefulEjbContainer {
    private final Interceptor invocationChain;
    private final Interceptor callbackChain;
    private final TransactionManager transactionManager;
    private final UserTransaction userTransaction;

    public DefaultStatefulEjbContainer(TransactionManager transactionManager,
                                       TrackedConnectionAssociator trackedConnectionAssociator,
                                       boolean securityEnabled,
                                       boolean doAsCurrentCaller,
                                       boolean useContextHandler) throws Exception {

        this.transactionManager = transactionManager;
        this.userTransaction = new CoreUserTransaction(transactionManager);

        //
        // build the normal invocation processing chain (built in reverse order)
        //

        // last interceptor is always the dispatcher
        Interceptor invocationChain = new DispatchInterceptor();

        // Interceptor that changes security identity to that of the caller
        if (doAsCurrentCaller) {
            invocationChain = new EJBIdentityInterceptor(invocationChain);
        }

        // JNDI ENC interceptor
        invocationChain = new ComponentContextInterceptor(invocationChain);

        // Resource Adapter connection reassociation interceptor
        if (trackedConnectionAssociator != null) {
            invocationChain = new ConnectionTrackingInterceptor(invocationChain, trackedConnectionAssociator);
        }

        // security permission check interceptor
        if (securityEnabled) {
            invocationChain = new EjbSecurityInterceptor(invocationChain);
        }

        // Sets the run as subject which is used when this ejb calls another ejb
        invocationChain = new EjbRunAsInterceptor(invocationChain);

        // Sets the jacc security policy for this ejb
        if (useContextHandler) {
            invocationChain = new PolicyContextHandlerEJBInterceptor(invocationChain);
        }

        invocationChain = new StatefulInstanceInterceptor(invocationChain, transactionManager);

        // transaction interceptor
        invocationChain = new TransactionContextInterceptor(invocationChain, transactionManager);
        invocationChain = new TransactionPolicyInterceptor(invocationChain, transactionManager);

        // logs system exceptions
        invocationChain = new SystemExceptionInterceptor(invocationChain);

        // sets the invocation subject when the invocation has no subject associated
        invocationChain = new DefaultSubjectInterceptor(invocationChain);
        this.invocationChain = invocationChain;

        //
        // Callback chain is used for ejb state change callbacks
        //

        // last interceptor is always the callback interceptor
        Interceptor callbackChain = new StatefulCallbackInterceptor();

        // Interceptor that changes security identity to that of the caller
        if (doAsCurrentCaller) {
            callbackChain = new EJBIdentityInterceptor(callbackChain);
        }

        // JNDI ENC interceptor
        callbackChain = new ComponentContextInterceptor(callbackChain);

        // Resource Adapter connection reassociation interceptor
        if (trackedConnectionAssociator != null) {
            callbackChain = new ConnectionTrackingInterceptor(callbackChain, trackedConnectionAssociator);
        }
        this.callbackChain = new SystemExceptionInterceptor(callbackChain);
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public UserTransaction getUserTransaction() {
        return userTransaction;
    }

    public PersistentTimer getTransactedTimer() {
        throw new UnsupportedOperationException("Stateful session beans do not support timers");
    }

    public PersistentTimer getNontransactedTimer() {
        throw new UnsupportedOperationException("Stateful session beans do not support timers");
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        return invocationChain.invoke(invocation);
    }

    public void setContext(EJBInstanceContext instanceContext, SessionContext sessionContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.SET_CONTEXT, instanceContext, new Object[]{sessionContext});
        callbackChain.invoke(invocation);
    }

    public void activate(EJBInstanceContext instanceContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.ACTIVATE, instanceContext);
        callbackChain.invoke(invocation);
    }

    public void passivate(EJBInstanceContext instanceContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.PASSIVATE, instanceContext);
        callbackChain.invoke(invocation);
    }

    public void afterBegin(EJBInstanceContext instanceContext) throws Throwable {
        EnterpriseBean instance = instanceContext.getInstance();
        if (instance instanceof SessionSynchronization) {
            EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.AFTER_BEGIN, instanceContext);
            callbackChain.invoke(invocation);
        }
    }

    public void beforeCommit(EJBInstanceContext instanceContext) throws Throwable {
        EnterpriseBean instance = instanceContext.getInstance();
        if (instance instanceof SessionSynchronization) {
            EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.BEFORE_COMPLETION, instanceContext);
            callbackChain.invoke(invocation);
        }
    }

    public void afterCommit(EJBInstanceContext instanceContext, boolean committed) throws Throwable {
        EnterpriseBean instance = instanceContext.getInstance();
        if (instance instanceof SessionSynchronization) {
            EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.AFTER_COMPLETION, instanceContext, new Object[]{new Boolean(committed)});
            callbackChain.invoke(invocation);
        }
    }

    public void timeout(ExtendedEjbDeployment deployment, Object id, Timer timer, int ejbTimeoutIndex) {
        throw new UnsupportedOperationException("Stateful EJBs do not support timeout");
    }


}
