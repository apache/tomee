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

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.EjbInvocation;

/**
 * @version $Revision$ $Date$
 */
public class BeanPolicy implements TransactionPolicy {
    private static final Log log = LogFactory.getLog(BeanPolicy.class);
    public static final BeanPolicy INSTANCE = new BeanPolicy();

    public InvocationResult invoke(Interceptor interceptor, EjbInvocation ejbInvocation, TransactionManager transactionManager) throws Throwable {
        Transaction clientContext = transactionManager.suspend();
        try {
            try {
                InvocationResult result = interceptor.invoke(ejbInvocation);
                if (transactionManager.getTransaction() != null) {
                    throw new UncommittedTransactionException();
                }
                return result;
            } catch (Throwable t) {
                if (transactionManager.getTransaction() != null) {
                    try {
                        transactionManager.rollback();
                    } catch (Exception e) {
                        log.warn("Unable to roll back", e);
                    }
                }
                throw t;
            }
        } finally {
            if (clientContext != null) {
                transactionManager.resume(clientContext);
            }
        }
    }

    public String toString() {
        return "BeanManaged";
    }

    protected Object readResolve() {
        return INSTANCE;
    }
}
