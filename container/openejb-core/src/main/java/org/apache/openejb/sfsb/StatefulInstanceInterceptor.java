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

import java.rmi.NoSuchObjectException;
import javax.ejb.NoSuchObjectLocalException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.transaction.EjbTransactionContext;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.NotReentrantException;
import org.apache.openejb.NotReentrantLocalException;
import org.apache.openejb.StatefulEjbDeployment;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.cache.InstanceCache;

/**
 * Interceptor for Stateful Session EJBs that acquires an instance for execution.
 * For create methods it creates a new context using the factory, and for every
 * thing else it gets the context from either the main or transaction cache.
 *
 * @version $Revision$ $Date$
 */
public final class StatefulInstanceInterceptor implements Interceptor {
    private final Interceptor next;
    private final TransactionManager transactionManager;

    public StatefulInstanceInterceptor(Interceptor next, TransactionManager transactionManager) {
        this.next = next;
        this.transactionManager = transactionManager;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        EjbInvocation ejbInvocation = (EjbInvocation) invocation;
        StatefulEjbDeployment deployment = (StatefulEjbDeployment) ejbInvocation.getEjbDeployment();
        if (!(deployment instanceof StatefulEjbDeployment)) {
            throw new IllegalArgumentException("StatefulInstanceInterceptor can only be used with a StatefulEjbDeploymentContext: " + deployment.getClass().getName());
        }

        // initialize the context and set it into the invocation
        StatefulInstanceContext ctx = getInstanceContext((StatefulEjbDeployment) deployment, ejbInvocation);
        ejbInvocation.setEJBInstanceContext(ctx);

        // resume the preexisting transaction context
        if (ctx.getPreexistingTransaction() != null) {
            Transaction preexistingContext = ctx.getPreexistingTransaction();
            transactionManager.resume(preexistingContext);
            ctx.setPreexistingTransaction(null);
        }

        // check reentrancy
        if (ctx.isInCall()) {
            if (ejbInvocation.getType().isLocal()) {
                throw new NotReentrantLocalException("Stateful session beans do not support reentrancy: " + deployment.getContainerId());
            } else {
                throw new NotReentrantException("Stateful session beans do not support reentrancy: " + deployment.getContainerId());
            }
        }

        EjbTransactionContext ejbTransactionContext = ejbInvocation.getEjbTransactionData();
        EJBInstanceContext oldInstanceContext = ejbTransactionContext.beginInvocation(ctx);
        try {
            // invoke next
            InvocationResult invocationResult = next.invoke(invocation);

            // if we have a BMT still associated with the thread, suspend it and save it off for the next invocation
            if (deployment.isBeanManagedTransactions()) {
                ctx.setPreexistingTransaction(transactionManager.suspend());
            }

            return invocationResult;
        } catch (Throwable t) {
            // we must kill the instance when a system exception is thrown
            ctx.die();

            throw t;
        } finally {
            ejbTransactionContext.endInvocation(oldInstanceContext);
            ejbInvocation.setEJBInstanceContext(null);
        }
    }

    private StatefulInstanceContext getInstanceContext(StatefulEjbDeployment deployment, EjbInvocation ejbInvocation) throws Throwable {
        Object id = ejbInvocation.getId();
        InstanceCache instanceCache = deployment.getInstanceCache();

        StatefulInstanceContext ctx;
        if (id == null) {
            // we don't have an id so we are a create method
            ctx = (StatefulInstanceContext) deployment.getInstanceFactory().createInstance();
            assert ctx.getInstance() != null: "Got a context with no instance assigned";
            id = ctx.getId();
            ctx.setCache(instanceCache);
            instanceCache.putActive(id, ctx);
        } else {
            // first check the transaction cache
            EjbTransactionContext ejbTransactionContext = ejbInvocation.getEjbTransactionData();
            ctx = (StatefulInstanceContext) ejbTransactionContext.getContext(deployment.getContainerId(), id);
            if (ctx == null) {
                // next check the main cache
                ctx = (StatefulInstanceContext) instanceCache.get(id);
                if (ctx == null) {
                    // bean is no longer cached or never existed
                    if (ejbInvocation.getType().isLocal()) {
                        throw new NoSuchObjectLocalException(id.toString());
                    } else {
                        throw new NoSuchObjectException(id.toString());
                    }
                }
            }

            if (ctx.isDead()) {
                if (ejbInvocation.getType().isLocal()) {
                    throw new NoSuchObjectLocalException("Instance has been removed or threw a system exception: id=" + id.toString());
                } else {
                    throw new NoSuchObjectException("Instance has been removed or threw a system exception: id=" + id.toString());
                }
            }
        }
        return ctx;
    }
}
