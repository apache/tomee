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

package org.apache.openejb.core.transaction;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.SystemException;

import jakarta.transaction.TransactionManager;

public class JtaTransactionPolicyFactory implements TransactionPolicyFactory {
    private final TransactionManager transactionManager;

    public JtaTransactionPolicyFactory(final TransactionManager transactionManager) {
        if (transactionManager == null) {
            throw new NullPointerException("transactionManager is null");
        }
        this.transactionManager = transactionManager;
    }

    public TransactionPolicy createTransactionPolicy(final TransactionType type) throws SystemException, ApplicationException {
        switch (type) {
            case Required:
                return new TxRequired(transactionManager);
            case RequiresNew:
                return new TxRequiresNew(transactionManager);
            case Supports:
                return new TxSupports(transactionManager);
            case NotSupported:
                return new TxNotSupported(transactionManager);
            case Mandatory:
                return new TxMandatory(transactionManager);
            case Never:
                return new TxNever(transactionManager);
            case BeanManaged:
                return new TxBeanManaged(transactionManager);
            default:
                throw new SystemException(new IllegalArgumentException("Unknown transaction type " + type));
        }
    }
}
