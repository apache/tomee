/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.transaction;

import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.openejb.EjbInvocation;

import javax.ejb.TransactionRolledbackLocalException;
import javax.transaction.TransactionRolledbackException;

/**
 * Supports
 *
 * The Container invokes an enterprise Bean method whose transaction attribute
 * is set to Supports as follows.
 *
 * - If the client calls with a transaction context, the Container performs
 *   the same steps as described in the Required case.
 *
 * - If the client calls without a transaction context, the Container performs
 *   the same steps as described in the NotSupported case.
 *
 * The Supports transaction attribute must be used with caution. This is
 * because of the different transactional semantics provided by the two
 * possible modes of execution. Only the enterprise beans that will
 * execute correctly in both modes should use the Supports transaction
 * attribute.
 *
 */
final class TxSupports implements TransactionPolicy {
    public InvocationResult invoke(Interceptor interceptor, EjbInvocation ejbInvocation, TransactionContextManager transactionContextManager) throws Throwable {
        TransactionContext callerContext = transactionContextManager.getContext();
        if (callerContext != null && callerContext.isInheritable()) {
            try {
                ejbInvocation.setTransactionContext(callerContext);
                return interceptor.invoke(ejbInvocation);
            } catch (Throwable t){
                callerContext.setRollbackOnly();
                if (ejbInvocation.getType().isLocal()) {
                    throw new TransactionRolledbackLocalException().initCause(t);
                } else {
                    // can't set an initCause on a TransactionRolledbackException
                    throw new TransactionRolledbackException(t.getMessage());
                }
            } finally {
                ejbInvocation.setTransactionContext(null);
            }
        }

        if (callerContext != null) {
            callerContext.suspend();
        }
        try {
            TransactionContext beanContext = transactionContextManager.newUnspecifiedTransactionContext();
            ejbInvocation.setTransactionContext(beanContext);
            try {
                InvocationResult result = interceptor.invoke(ejbInvocation);
                return result;
            } catch (Throwable t) {
                beanContext.setRollbackOnly();
                throw t;
            } finally {
                beanContext.commit();
            }
        } finally {
            ejbInvocation.setTransactionContext(null);
            transactionContextManager.setContext(callerContext);
            if (callerContext != null) {
                callerContext.resume();
            }
        }
    }
    public String toString() {
        return "Supports";
    }

    private Object readResolve() {
        return ContainerPolicy.Supports;
    }
}
