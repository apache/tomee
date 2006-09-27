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
package org.apache.openejb.entity;

import java.rmi.NoSuchObjectException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.NoSuchObjectLocalException;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.transaction.EjbTransactionContext;
import org.apache.openejb.EjbDeployment;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.EntityEjbDeployment;
import org.apache.openejb.NotReentrantException;
import org.apache.openejb.NotReentrantLocalException;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.cache.InstancePool;

/**
 * Simple Instance Interceptor that does not cache instances in the ready state
 * but passivates between each invocation.
 *
 * @version $Revision$ $Date$
 */
public final class EntityInstanceInterceptor implements Interceptor {
    private final Interceptor next;

    public EntityInstanceInterceptor(Interceptor next) {
        this.next = next;
    }

    public InvocationResult invoke(final Invocation invocation) throws Throwable {
        EjbInvocation ejbInvocation = (EjbInvocation) invocation;
        EjbDeployment deployment = ejbInvocation.getEjbDeployment();
        if (!(deployment instanceof EntityEjbDeployment)) {
            throw new IllegalArgumentException("EntityInstanceInterceptor can only be used with a EntityEjbDeploymentContext: " + deployment.getClass().getName());
        }

        EntityEjbDeployment entityEjbDeploymentContext = ((EntityEjbDeployment) deployment);
        String containerId = entityEjbDeploymentContext.getContainerId();
        InstancePool pool = entityEjbDeploymentContext.getInstancePool();

        EjbTransactionContext ejbTransactionContext = ejbInvocation.getEjbTransactionData();
        Object id = ejbInvocation.getId();

        // get the context
        EntityInstanceContext ctx = null;

        // if we have an id then check if there is already a context associated with the transaction
        if (id != null) {
            ctx = (EntityInstanceContext) ejbTransactionContext.getContext(containerId, id);
            // if we have a dead context, the cached context was discarded, so we need clean it up and get a new one
            if (ctx != null && ctx.isDead()) {
                ejbTransactionContext.unassociate(ctx);
                ctx = null;
            }
        }

        // if we didn't find an existing context, create a new one.
        if (ctx == null) {
            ctx = (EntityInstanceContext) pool.acquire();
            ctx.setId(id);
            ctx.setPool(pool);
            ctx.setEjbTransactionData(ejbTransactionContext);
        }

        // set the instanct into the invocation
        ejbInvocation.setEJBInstanceContext(ctx);

        // check reentrancy
        if (!entityEjbDeploymentContext.isReentrant() && ctx.isInCall()) {
            if (ejbInvocation.getType().isLocal()) {
                throw new NotReentrantLocalException("" + containerId);
            } else {
                throw new NotReentrantException("" + containerId);
            }
        }

        // associates the context with the transaction, this may result an a load that throws
        // and NoSuchEntityException, which needs to be converted to the a NoSuchObject[Local]Exception
        EJBInstanceContext oldContext = null;
        try {
            oldContext = ejbTransactionContext.beginInvocation(ctx);
        } catch (NoSuchEntityException e) {
            if (ejbInvocation.getType().isLocal()) {
                throw new NoSuchObjectLocalException().initCause(e);
            } else {
                throw new NoSuchObjectException(e.getMessage());
            }
        }

        // send the invocation down the chain
        try {
            InvocationResult result = next.invoke(invocation);
            return result;
        } catch (Throwable t) {
            // we must kill the instance when a system exception is thrown
            ctx.die();
            // id may have been set during create
            if (id == null) {
                id = ctx.getId();
            }
            // if we have an id unassociate the context
            if (id != null) {
                ejbTransactionContext.unassociate(containerId, id);
            }
            throw t;
        } finally {
            ejbTransactionContext.endInvocation(oldContext);
            ejbInvocation.setEJBInstanceContext(null);
        }
    }
}
