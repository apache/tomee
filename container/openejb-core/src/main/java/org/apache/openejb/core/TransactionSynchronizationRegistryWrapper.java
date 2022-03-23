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

package org.apache.openejb.core;

import org.apache.openejb.loader.SystemInstance;

import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;

/**
 * @version $Rev$ $Date$
 */
public class TransactionSynchronizationRegistryWrapper implements TransactionSynchronizationRegistry {
    private SystemInstance system;
    private TransactionSynchronizationRegistry registry;

    public TransactionSynchronizationRegistryWrapper() {
        final SystemInstance system = SystemInstance.get();
        this.registry = system.getComponent(TransactionSynchronizationRegistry.class);
        this.system = system;
    }

    public TransactionSynchronizationRegistry getRegistry() {
        final SystemInstance system = SystemInstance.get();
        if (system != this.system) {
            this.registry = system.getComponent(TransactionSynchronizationRegistry.class);
            this.system = system;
        }
        return registry;
    }

    public Object getResource(final Object o) {
        return getRegistry().getResource(o);
    }

    public boolean getRollbackOnly() {
        return getRegistry().getRollbackOnly();
    }

    public Object getTransactionKey() {
        return getRegistry().getTransactionKey();
    }

    public int getTransactionStatus() {
        return getRegistry().getTransactionStatus();
    }

    public void putResource(final Object o, final Object o1) {
        getRegistry().putResource(o, o1);
    }

    public void registerInterposedSynchronization(final Synchronization synchronization) {
        getRegistry().registerInterposedSynchronization(synchronization);
    }

    public void setRollbackOnly() {
        getRegistry().setRollbackOnly();
    }
}
