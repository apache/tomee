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
package org.apache.openejb.junit;

import org.apache.openejb.BeanContext;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.transaction.EjbTransactionUtil;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionType;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.transaction.Transactional;
import java.lang.reflect.Method;

public class TransactionRule extends BeanContextBaseRule implements TestRule {
    private final boolean rollback;

    public TransactionRule() {
        this(null, true);
    }

    public TransactionRule(final boolean rollback) {
        this(null, rollback);
    }

    public TransactionRule(final Object o, final boolean rollback) {
        super(o);
        this.rollback = rollback;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final TransactionAttribute annotation = description.getAnnotation(TransactionAttribute.class);
                final Transactional annotation2 = description.getAnnotation(Transactional.class);
                if (annotation == null && annotation2 == null) {
                    base.evaluate();
                    return;
                }

                final BeanContext beanContext = getBeanContext();
                final Method method = beanContext.getManagedClass().getMethod(description.getMethodName());

                final TransactionType transactionType = TransactionType.get(annotation == null ?
                        TransactionAttributeType.valueOf(annotation2.value().name()) : annotation.value());
                beanContext.getMethodContext(method)
                    .setTransactionType(
                            transactionType);

                ThreadContext tc = ThreadContext.getThreadContext();
                final boolean tcCreated;
                if (tc == null) {
                    tcCreated = true;
                    tc = ThreadContext.enter(new ThreadContext(beanContext, null));
                } else {
                    tcCreated = false;
                }
                final TransactionPolicy policy = EjbTransactionUtil.createTransactionPolicy(transactionType, tc);
                try {
                    base.evaluate();
                } finally {
                    if (rollback) {
                        policy.setRollbackOnly();
                    }
                    EjbTransactionUtil.afterInvoke(policy, tc);
                    if (tcCreated) {
                        ThreadContext.exit(tc);
                    }
                }
            }
        };
    }
}
