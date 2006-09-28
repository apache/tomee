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
package org.apache.openejb.entity.cmp;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.transaction.EjbTransactionContext;
import org.apache.openejb.transaction.CmpTxData;
import org.apache.openejb.CmpEjbDeployment;
import org.apache.openejb.EjbDeployment;
import org.apache.openejb.EjbInvocation;

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
