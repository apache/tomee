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
package org.apache.openejb.junit.jee.transaction;

import org.apache.openejb.core.transaction.JtaTransactionPolicyFactory;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.loader.SystemInstance;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import jakarta.transaction.TransactionManager;
import java.lang.reflect.Method;

public class TransactionRule implements TestRule {
    @Override
    public Statement apply(final Statement base, final Description description) {
        final Method mtd = getMethod(description.getTestClass(), description.getMethodName());
        final Transaction tx = mtd.getAnnotation(Transaction.class);
        if (tx != null) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    final TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
                    final JtaTransactionPolicyFactory factory = new JtaTransactionPolicyFactory(transactionManager);
                    final TransactionPolicy policy = factory.createTransactionPolicy(TransactionType.RequiresNew);
                    try {
                        base.evaluate();
                    } finally {
                        if (tx.rollback()) {
                            policy.setRollbackOnly();
                        }
                        policy.commit();
                    }
                }
            };
        } else {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    base.evaluate();
                }
            };
        }
    }

    private static Method getMethod(final Class<?> testClass, final String methodName) {
        try {
            return testClass.getMethod(methodName);
        } catch (final NoSuchMethodException e) {
            for (final Method mtd : testClass.getMethods()) {
                if (methodName.equals(mtd.getName())) {
                    return mtd;
                }
            }
            return null;
        }
    }
}
