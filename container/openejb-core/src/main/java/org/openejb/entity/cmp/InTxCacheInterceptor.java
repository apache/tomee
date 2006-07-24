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
package org.openejb.entity.cmp;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.openejb.transaction.EjbTransactionContext;
import org.openejb.transaction.CmpTxData;
import org.openejb.CmpEjbDeployment;
import org.openejb.EjbDeployment;
import org.openejb.EjbInvocation;

/**
 * This interceptor defines, if required, the InTxCache of the
 * TransactionContext bound to the intercepted EJBInvocation. A
 * CacheFlushStrategyFactory is used to create the CacheFlushStrategy to be
 * used under the cover of the defined InTxCache.
 *
 * @version $Revision$ $Date$
 */
public final class InTxCacheInterceptor implements Interceptor {
    private final Interceptor next;

    public InTxCacheInterceptor(Interceptor next) {
        this.next = next;
    }

    public InvocationResult invoke(final Invocation invocation) throws Throwable {
        EjbInvocation ejbInvocation = (EjbInvocation) invocation;
        EjbTransactionContext ejbTransactionContext = ejbInvocation.getEjbTransactionData();
        if (ejbTransactionContext.getCmpTxData() == null) {
            EjbDeployment deployment = ejbInvocation.getEjbDeployment();
            if (!(deployment instanceof CmpEjbDeployment)) {
                throw new IllegalArgumentException("NewInTxCacheInterceptor can only be used with an CmpEjbDeployment: " + deployment.getClass().getName());
            }

            CmpEjbDeployment cmpEjbDeploymentContext = ((CmpEjbDeployment) deployment);

            CmpTxData cmpTxData = cmpEjbDeploymentContext.getEjbCmpEngine().createCmpTxData();
            if (cmpTxData == null) throw new NullPointerException("cmpTxData is null");

            ejbTransactionContext.setCmpTxData(cmpTxData);
        }

        return next.invoke(invocation);
    }
}
