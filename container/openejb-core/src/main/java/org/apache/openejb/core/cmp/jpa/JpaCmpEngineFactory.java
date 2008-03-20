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
package org.apache.openejb.core.cmp.jpa;

import org.apache.openejb.core.cmp.CmpEngineFactory;
import org.apache.openejb.core.cmp.CmpCallback;
import org.apache.openejb.core.cmp.CmpEngine;
import org.apache.openejb.OpenEJBException;

import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

public class JpaCmpEngineFactory implements CmpEngineFactory {
    private TransactionManager transactionManager;
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;
    private CmpCallback cmpCallback;

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() {
        return transactionSynchronizationRegistry;
    }

    public void setTransactionSynchronizationRegistry(TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
    }

    public CmpCallback getCmpCallback() {
        return cmpCallback;
    }

    public void setCmpCallback(CmpCallback cmpCallback) {
        this.cmpCallback = cmpCallback;
    }

    public CmpEngine create() throws OpenEJBException {
        return new JpaCmpEngine(cmpCallback, transactionManager, transactionSynchronizationRegistry);
    }
}
