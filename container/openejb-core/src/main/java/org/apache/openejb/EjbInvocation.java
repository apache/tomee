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
package org.apache.openejb;

import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;

import org.apache.openejb.transaction.EjbTransactionContext;

/**
 * Specialization of Invocation to define attributes specific to the
 * invocation of an EJB. This provides a type-safe mechanism for Interceptors
 * to access EJB specific information; it is the responsibility of the
 * original to ensure the Invocation implementation supports this interface
 * if it is going to be processed by an EJBContainer.
 *
 * @version $Revision$ $Date$
 */
public interface EjbInvocation extends Invocation {

    /**
     * The index of the virtual EJB operation
     * @return the index of the EJB operation being performed
     */
    int getMethodIndex();

    /**
     * The type of invocation, indicating which interface was invoked or
     * which 'special' callback was should be invoked (e.g. ejbTimeout).
     * @return the type of invocation
     */
    EJBInterfaceType getType();

    /**
     * Any arguments to the invocation (e.g. Method parameters).
     * @return the arguments to the invocation; null indicates no arguments (equivalent to Object[0])
     */
    Object[] getArguments();

    /**
     * The identity of the instance being invoked; for example, the primary
     * key of an Entity EJB.
     * @return the identity of the instance to invoke; may be null for 'class' level operations
     */
    Object getId();

    /**
     * The context representing the actual instance to use for processing this
     * request. Is transient, not valid on the client side, and will not be
     * valid on the server side until a suitable instance has been located
     * by an Interceptor
     * @return the context representing the instance to invoke
     */
    EJBInstanceContext getEJBInstanceContext();

    /**
     * Set the instance context to use
     * @param instanceContext the instance context to use
     */
    void setEJBInstanceContext(EJBInstanceContext instanceContext);

    /**
     * Gets the transaction context to use.  Eventhough the tx context is available from a
     * thread local we carry it in the invocation context to avoid the extra tx cost.
     * @return the transaction context to use
     */
    EjbTransactionContext getEjbTransactionData();

    /**
     * Setx the transaction context to use.  Eventhough the tx context is available from a
     * thread local we carry it in the invocation context to avoid the extra tx cost.
     * @param ejbTransactionContext the transaction context to use
     */
    void setEjbTransactionData(EjbTransactionContext ejbTransactionContext);

    InvocationResult createResult(Object object);

    InvocationResult createExceptionResult(Exception exception);

    ExtendedEjbDeployment getEjbDeployment();

    void setEjbDeployment(ExtendedEjbDeployment ejbDeployment);
}
