/*
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
package org.apache.openejb.cdi.transactional;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TxNever;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionalException;
import java.rmi.RemoteException;

@Interceptor
@Transactional(Transactional.TxType.NEVER)
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 200)
public class NeverInterceptor extends InterceptorBase {
    @AroundInvoke
    public Object intercept(final InvocationContext ic) throws Exception {
        try {
            return super.intercept(ic);
        } catch (final RemoteException re) {
            throw new TransactionalException(re.getMessage(), new InvalidTransactionException(re.getMessage()));
        }
    }

    @Override
    protected TransactionPolicy getPolicy() throws SystemException, ApplicationException {
        return new TxNever(getTransactionManager());
    }

    @Override
    protected boolean doesForbidUtUsage() {
        return false;
    }
}
