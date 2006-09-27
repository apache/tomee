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
package org.apache.openejb.entity.cmp;

import javax.ejb.EntityContext;
import javax.ejb.Timer;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.timer.PersistentTimer;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.openejb.CallbackMethod;
import org.apache.openejb.CmpEjbContainer;
import org.apache.openejb.CmpEjbDeployment;
import org.apache.openejb.ConnectionTrackingInterceptor;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.EJBInterfaceType;
import org.apache.openejb.EjbCallbackInvocation;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.EjbInvocationImpl;
import org.apache.openejb.ExtendedEjbDeployment;
import org.apache.openejb.SystemExceptionInterceptor;
import org.apache.openejb.core.CoreUserTransaction;
import org.apache.openejb.dispatch.DispatchInterceptor;
import org.apache.openejb.entity.EntityCallbackInterceptor;
import org.apache.openejb.entity.EntityInstanceInterceptor;
import org.apache.openejb.naming.ComponentContextInterceptor;
import org.apache.openejb.security.EJBIdentityInterceptor;
import org.apache.openejb.security.EjbRunAsInterceptor;
import org.apache.openejb.security.EjbSecurityInterceptor;
import org.apache.openejb.security.PolicyContextHandlerEJBInterceptor;
import org.apache.openejb.transaction.EjbTransactionContext;
import org.apache.openejb.transaction.TransactionPolicyInterceptor;
import org.apache.openejb.transaction.TransactionContextInterceptor;


/**
 * @version $Revision$ $Date$
 */
public class DefaultCmpEjbContainer implements CmpEjbContainer {
    private static final Log log = LogFactory.getLog(DefaultCmpEjbContainer.class);
    private final Interceptor invocationChain;
    private final Interceptor callbackChain;
    private final PersistentTimer transactedTimer;
    private final PersistentTimer nontransactionalTimer;
    private final TransactionManager transactionManager;
    private final UserTransaction userTransaction;

    public DefaultCmpEjbContainer(
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

        Interceptor invocationChain = new DispatchInterceptor();
        if (doAsCurrentCaller) {
            invocationChain = new EJBIdentityInterceptor(invocationChain);
        }

        if (securityEnabled) {
            invocationChain = new EjbSecurityInterceptor(invocationChain);
        }
        invocationChain = new EjbRunAsInterceptor(invocationChain);
        if (useContextHandler) {
            invocationChain = new PolicyContextHandlerEJBInterceptor(invocationChain);
        }
        invocationChain = new ComponentContextInterceptor(invocationChain);
        if (trackedConnectionAssociator != null) {
            invocationChain = new ConnectionTrackingInterceptor(invocationChain, trackedConnectionAssociator);
        }
        invocationChain = new EntityInstanceInterceptor(invocationChain);
        invocationChain = new InTxCacheInterceptor(invocationChain);
        invocationChain = new TransactionContextInterceptor(invocationChain, transactionManager);
        invocationChain = new TransactionPolicyInterceptor(invocationChain, transactionManager);
        invocationChain = new SystemExceptionInterceptor(invocationChain);
        this.invocationChain = invocationChain;

        //
        // Callback chain is used for ejb state change callbacks
        //

        Interceptor callbackChain = new EntityCallbackInterceptor();
        if (doAsCurrentCaller) {
            callbackChain = new EJBIdentityInterceptor(callbackChain);
        }
        callbackChain = new ComponentContextInterceptor(callbackChain);
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

    public void setContext(EJBInstanceContext instanceContext, EntityContext entityContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.SET_CONTEXT, instanceContext, new Object[]{entityContext});
        callbackChain.invoke(invocation);
    }

    public void unsetContext(EJBInstanceContext instanceContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.UNSET_CONTEXT, instanceContext);
        callbackChain.invoke(invocation);
    }

    public void ejbActivate(EJBInstanceContext instanceContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.ACTIVATE, instanceContext);
        callbackChain.invoke(invocation);
    }

    public void ejbPassivate(EJBInstanceContext instanceContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.PASSIVATE, instanceContext);
        callbackChain.invoke(invocation);
    }

    public void load(EJBInstanceContext instanceContext, EjbTransactionContext ejbTransactionContext) throws Throwable {
        CmpEjbDeployment deployment = (CmpEjbDeployment) instanceContext.getDeployment();
        deployment.getEjbCmpEngine().beforeLoad((CmpInstanceContext) instanceContext);

        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.LOAD, instanceContext);
        invocation.setEjbTransactionData(ejbTransactionContext);
        callbackChain.invoke(invocation);
    }

    public void store(EJBInstanceContext instanceContext, EjbTransactionContext ejbTransactionContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.STORE, instanceContext);
        invocation.setEjbTransactionData(ejbTransactionContext);
        callbackChain.invoke(invocation);

        CmpEjbDeployment deployment = (CmpEjbDeployment) instanceContext.getDeployment();
        deployment.getEjbCmpEngine().afterStore((CmpInstanceContext) instanceContext);
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
