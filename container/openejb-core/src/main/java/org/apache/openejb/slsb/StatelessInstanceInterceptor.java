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
package org.apache.openejb.slsb;

import javax.xml.rpc.handler.MessageContext;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.transaction.EjbTransactionContext;
import org.apache.geronimo.webservices.MessageContextInvocationKey;
import org.apache.openejb.EjbDeployment;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.StatelessEjbDeployment;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.cache.InstancePool;


/**
 * Interceptor for Stateless Session EJBs that obtains an instance
 * from a pool to execute the method.
 *
 * @version $Revision$ $Date$
 */
public final class StatelessInstanceInterceptor implements Interceptor {
    private final Interceptor next;

    public StatelessInstanceInterceptor(Interceptor next) {
        this.next = next;
    }

    public InvocationResult invoke(final Invocation invocation) throws Throwable {
        EjbInvocation ejbInvocation = (EjbInvocation) invocation;
        EjbDeployment deployment = ejbInvocation.getEjbDeployment();
        if (!(deployment instanceof StatelessEjbDeployment)) {
            throw new IllegalArgumentException("StatelessInstanceInterceptor can only be used with a StatelessEjbDeploymentContext: " + deployment.getClass().getName());
        }

        InstancePool pool = ((StatelessEjbDeployment) deployment).getInstancePool();

        // get the context
        StatelessInstanceContext ctx = (StatelessInstanceContext) pool.acquire();
        assert ctx.getInstance() != null: "Got a context with no instance assigned";
        assert !ctx.isInCall() : "Acquired a context already in an invocation";
        ctx.setPool(pool);

        // initialize the context and set it into the invocation
        ejbInvocation.setEJBInstanceContext(ctx);

        // set the webservice message context if we got one
        ctx.setMessageContext((MessageContext)invocation.get(MessageContextInvocationKey.INSTANCE));

        EjbTransactionContext ejbTransactionContext = ejbInvocation.getEjbTransactionData();
        EJBInstanceContext oldContext = ejbTransactionContext.beginInvocation(ctx);
        try {
            InvocationResult result = next.invoke(invocation);
            return result;
        } catch (Throwable t) {
            // we must kill the instance when a system exception is thrown
            ctx.die();
            throw t;
        } finally {
            ejbTransactionContext.endInvocation(oldContext);
            ejbInvocation.setEJBInstanceContext(null);
            ctx.setMessageContext(null);
        }
    }
}
