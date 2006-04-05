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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openejb.EjbInvocation;

import javax.transaction.RollbackException;

/**
 * RequiresNew
 *
 * The Container must invoke an enterprise Bean method whose transaction
 * attribute is set to RequiresNew with a new transaction context.
 *
 * If the client invokes the enterprise Bean's method while the client is not
 * associated with a transaction context, the container automatically starts a
 * new transaction before delegating a method call to the enterprise Bean
 * business method. The Container automatically enlists all the resource
 * managers accessed by the business method with the transaction. If the
 * business method invokes other enterprise beans, the Container passes the
 * transaction context with the invocation. The Container attempts to commit
 * the transaction when the business method has completed. The container
 * performs the commit protocol before the method result is sent to the client.
 *
 * If a client calls with a transaction context, the container suspends the
 * association of the transaction context with the current thread before
 * starting the new transaction and invoking the business method. The container
 * resumes the suspended transaction association after the business method and
 * the new transaction have been completed.
 *
 */
final class TxRequiresNew implements TransactionPolicy {
    private static final Log log = LogFactory.getLog(TxRequiresNew.class);

    public InvocationResult invoke(Interceptor interceptor, EjbInvocation ejbInvocation, TransactionContextManager transactionContextManager) throws Throwable {
        TransactionContext callerContext = transactionContextManager.getContext();

        if (callerContext != null) {
            callerContext.suspend();
        }
        try {
            TransactionContext beanContext = transactionContextManager.newContainerTransactionContext();
            ejbInvocation.setTransactionContext(beanContext);
            try {
                InvocationResult result = interceptor.invoke(ejbInvocation);
                return result;
            } catch (RollbackException re) {
                throw re;
            } catch (Throwable t) {
                try {
                    beanContext.setRollbackOnly();
                } catch (Exception e) {
                    log.warn("Unable to roll back", e);
                }
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
        return "RequiresNew";
    }

    private Object readResolve() {
        return ContainerPolicy.RequiresNew;
    }
}
