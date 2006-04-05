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

/**
 * NotSupported
 *
 * The Container invokes an enterprise Bean method whose transaction attribute
 * is set to NotSupported with an unspecified transaction context.
 *
 * If a client calls with a transaction context, the container suspends the
 * association of the transaction context with the current thread before
 * invoking the enterprise bean's business method. The container resumes the
 * suspended association when the business method has completed. The suspended
 * transaction context of the client is not passed to the resource managers or
 * other enterprise Bean objects that are invoked from the business method.
 *
 * If the business method invokes other enterprise beans, the Container passes
 * no transaction context with the invocation.
 *
 * Refer to Subsection 17.6.5 for more details of how the Container can
 * implement this case.
 *
 */
final class TxNotSupported implements TransactionPolicy {
    public InvocationResult invoke(Interceptor interceptor, EjbInvocation ejbInvocation, TransactionContextManager transactionContextManager) throws Throwable {
        TransactionContext callerContext = transactionContextManager.getContext();
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
        return "NotSupported";
    }
    private Object readResolve() {
        return ContainerPolicy.NotSupported;
    }
}
