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
package org.apache.openejb.transaction;

import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.interceptor.Interceptor;
import org.apache.openejb.EjbInvocation;

import javax.ejb.TransactionRolledbackLocalException;
import javax.transaction.TransactionRolledbackException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

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
    public InvocationResult invoke(Interceptor interceptor, EjbInvocation ejbInvocation, TransactionManager transactionManager) throws Throwable {
        Transaction callerTransaction = transactionManager.getTransaction();
        if (callerTransaction != null) {
            try {
                InvocationResult result = interceptor.invoke(ejbInvocation);
                return result;
            } catch (Throwable t){
                transactionManager.setRollbackOnly();
                if (ejbInvocation.getType().isLocal()) {
                    throw new TransactionRolledbackLocalException().initCause(t);
                } else {
                    // can't set an initCause on a TransactionRolledbackException
                    throw new TransactionRolledbackException(t.getMessage());
                }
            }
        } else {
            InvocationResult result = interceptor.invoke(ejbInvocation);
            return result;
        }
    }

    public String toString() {
        return "Supports";
    }

    private Object readResolve() {
        return ContainerPolicy.Supports;
    }
}
