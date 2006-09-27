/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
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
