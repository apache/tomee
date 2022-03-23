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
package org.apache.openejb.junit5.jee.transaction;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.core.transaction.JtaTransactionPolicyFactory;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.junit.jee.transaction.Transaction;
import org.apache.openejb.loader.SystemInstance;
import org.junit.jupiter.api.extension.*;

import jakarta.transaction.TransactionManager;
import java.lang.reflect.Method;

public class TransactionExtension implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        final Method mtd = extensionContext.getTestMethod()
                .orElseThrow(() -> new OpenEJBRuntimeException("Could not get test method from extension context."));
        final Transaction tx = mtd.getAnnotation(Transaction.class);
        if (tx != null) {
            final TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
            final JtaTransactionPolicyFactory factory = new JtaTransactionPolicyFactory(transactionManager);
            final TransactionPolicy policy = factory.createTransactionPolicy(TransactionType.RequiresNew);
            if (tx.rollback()) {
                policy.setRollbackOnly();
            }
            policy.commit();
        }
    }
}
