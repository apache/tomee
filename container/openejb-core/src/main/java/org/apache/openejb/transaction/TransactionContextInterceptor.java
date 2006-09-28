/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.transaction;

import javax.transaction.TransactionManager;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.openejb.EjbInvocation;

/**
 * @version $Revision$ $Date$
 */
public class TransactionContextInterceptor implements Interceptor {
    private final Interceptor next;
    private final TransactionManager transactionManager;

    public TransactionContextInterceptor(Interceptor next, TransactionManager transactionManager) {
        this.next = next;
        this.transactionManager = transactionManager;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        EjbInvocation ejbInvocation = (EjbInvocation) invocation;

        if (ejbInvocation.getEjbTransactionData() != null) {
            throw new IllegalStateException("Invocation is already associated with a transaction");
        }

        EjbTransactionContext ejbTransactionContext = EjbTransactionContext.get(transactionManager.getTransaction());

        boolean succeeded = false;
        try {
            ejbInvocation.setEjbTransactionData(ejbTransactionContext);
            InvocationResult result = next.invoke(ejbInvocation);
            succeeded = true;
            return result;
        } finally {
            ejbInvocation.setEjbTransactionData(null);
            ejbTransactionContext.complete(succeeded);
        }
    }
}
