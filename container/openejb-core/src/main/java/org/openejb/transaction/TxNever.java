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

import javax.transaction.TransactionManager;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.InvocationResult;
import org.openejb.EjbInvocation;

/**
 * Never
 *
 * The Container invokes an enterprise Bean method whose transaction attribute
 * is set to Never without a transaction context defined by the EJB spec.
 *
 * The client is required to call without a transaction context.
 *
 * - If the client calls with a transaction context, the Container throws the
 *   java.rmi.RemoteException exception if the client is a remote client, or
 *   the javax.ejb.EJBException if the client is a local client.
 * - If the client calls without a transaction context, the Container performs
 *   the same steps as described in the NotSupported case.
 *
 */
final class TxNever implements TransactionPolicy {
    public InvocationResult invoke(Interceptor interceptor, EjbInvocation ejbInvocation, TransactionManager transactionManager) throws Throwable {
        // If we have a transaction, throw an exception
        if (transactionManager.getTransaction() != null) {
            if (ejbInvocation.getType().isLocal()) {
                throw new TransactionNotSupportedLocalException();
            } else {
                throw new TransactionNotSupportedException();
            }
        }

        InvocationResult result = interceptor.invoke(ejbInvocation);
        return result;
    }

    public String toString() {
        return "Never";
    }

    private Object readResolve() {
        return ContainerPolicy.Never;
    }
}
