/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.spring;

import org.apache.openejb.core.transaction.TransactionPolicyFactory;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.SystemException;
import org.apache.openejb.ApplicationException;
import org.springframework.transaction.PlatformTransactionManager;

public class SpringTransactionPolicyFactory implements TransactionPolicyFactory {
    private final PlatformTransactionManager transactionManager;

    public SpringTransactionPolicyFactory(PlatformTransactionManager transactionManager) {
        if (transactionManager == null) throw new NullPointerException("transactionManager is null");
        this.transactionManager = transactionManager;
    }

    public TransactionPolicy createTransactionPolicy(TransactionType type) throws SystemException, ApplicationException {
        SpringTransactionPolicy policy;
        if (type == TransactionType.BeanManaged) {
            policy = new SpringBeanTransactionPolicy(transactionManager);
        } else {
            policy = new SpringTransactionPolicy(transactionManager, type);
        }
        return policy;
    }
}
