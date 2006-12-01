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
package org.apache.openejb.slsb;

import javax.ejb.SessionContext;
import javax.ejb.Timer;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.timer.PersistentTimer;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.openejb.CallbackMethod;
import org.apache.openejb.ConnectionTrackingInterceptor;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.EJBInterfaceType;
import org.apache.openejb.EjbCallbackInvocation;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.EjbInvocationImpl;
import org.apache.openejb.ExtendedEjbDeployment;
import org.apache.openejb.StatelessEjbContainer;
import org.apache.openejb.SystemExceptionInterceptor;
import org.apache.openejb.NoConnectionEnlistingInterceptor;
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
public class DefaultStatelessEjbContainer implements StatelessEjbContainer {
    private static final Log log = LogFactory.getLog(DefaultStatelessEjbContainer.class);
    private final Interceptor invocationChain;
    private final Interceptor callbackChain;
    private final PersistentTimer transactedTimer;
    private final PersistentTimer nontransactionalTimer;
    private final TransactionManager transactionManager;
    private final UserTransaction userTransaction;

    public DefaultStatelessEjbContainer(
            TransactionManager transactionManager,
            TrackedConnectionAssociator trackedConnectionAssociator,
            PersistentTimer transactionalTimer,
            PersistentTimer nontransactionalTimer,
            boolean securityEnabled,
            boolean doAsCurrentCaller,
            boolean useContextHandler) throws Exception {

        this.transactionManager = transactionManager;
        this.userTransaction = new CoreUserTransaction(transactionManager);
        this.transactedTimer = transactionalTimer;
        this.nontransactionalTimer = nontransactionalTimer;

        //
        // build the normal invocation processing chain (built in reverse order)
        //

        // last interceptor is always the dispatcher
        Interceptor invocationChain = new DispatchInterceptor();

        // add the web services handler interceptor
        invocationChain = new HandlerChainInterceptor(invocationChain);

        // JNDI ENC interceptor
        invocationChain = new ComponentContextInterceptor(invocationChain);

        // Resource Adapter connection reassociation interceptor
        if (trackedConnectionAssociator != null) {
            invocationChain = new ConnectionTrackingInterceptor(invocationChain, trackedConnectionAssociator);
        }

        // Interceptor that changes security identity to that of the caller
        if (doAsCurrentCaller) {
            invocationChain = new EJBIdentityInterceptor(invocationChain);
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

        // create the user transaction if bean managed
        invocationChain = new StatelessInstanceInterceptor(invocationChain);

        // transaction interceptor
        invocationChain = new TransactionContextInterceptor(invocationChain, transactionManager);
        invocationChain = new TransactionPolicyInterceptor(invocationChain, transactionManager);

        //make sure tm notifications don't enlist any connections from the caller's connection context in a new tx
        //or targets connections in callers tx.
        if (trackedConnectionAssociator != null) {
            invocationChain = new NoConnectionEnlistingInterceptor(invocationChain, trackedConnectionAssociator);
        }

        // logs system exceptions
        invocationChain = new SystemExceptionInterceptor(invocationChain);

        // sets the invocation subject when the invocation has no subject associated
        invocationChain = new DefaultSubjectInterceptor(invocationChain);
        this.invocationChain = invocationChain;

        //
        // Callback chain is used for ejb state change callbacks
        //

        // last interceptor is always the callback interceptor
        Interceptor callbackChain = new StatelessCallbackInterceptor();

        // add the web services handler interceptor
        callbackChain = new HandlerChainInterceptor(callbackChain);

        // JNDI ENC interceptor
        callbackChain = new ComponentContextInterceptor(callbackChain);

        // Resource Adapter connection reassociation interceptor
        if (trackedConnectionAssociator != null) {
            callbackChain = new ConnectionTrackingInterceptor(callbackChain, trackedConnectionAssociator);
        }

        // Interceptor that changes security identity to that of the caller
        if (doAsCurrentCaller) {
            callbackChain = new EJBIdentityInterceptor(callbackChain);
        }
        this.callbackChain = callbackChain;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public UserTransaction getUserTransaction() {
        return userTransaction;
    }

    public PersistentTimer getTransactedTimer() {
        return transactedTimer;
    }

    public PersistentTimer getNontransactedTimer() {
        return nontransactionalTimer;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        return invocationChain.invoke(invocation);
    }

    public void setContext(EJBInstanceContext instanceContext, int methodIndex, SessionContext sessionContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.SET_CONTEXT, instanceContext, new Object[]{sessionContext});
        callbackChain.invoke(invocation);
    }

    public void ejbCreate(EJBInstanceContext instanceContext, int methodIndex) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.CREATE, instanceContext);
        callbackChain.invoke(invocation);
    }

    public void ejbRemove(EJBInstanceContext instanceContext, int methodIndex) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.REMOVE, instanceContext);
        callbackChain.invoke(invocation);
    }

    public void timeout(ExtendedEjbDeployment deployment, Object id, Timer timer, int ejbTimeoutIndex) {
        EjbInvocation invocation = new EjbInvocationImpl(EJBInterfaceType.TIMEOUT, id, ejbTimeoutIndex, new Object[]{timer});
        invocation.setEjbDeployment(deployment);

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(deployment.getClassLoader());
        try {
            invoke(invocation);
        } catch (Throwable throwable) {
            log.warn("Timer invocation failed", throwable);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }
}
