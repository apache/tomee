/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.spi;

import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * Transaction related service.
 * @version $Rev$ $Date$
 */
public interface TransactionService
{
    /**
     * Get {@link TransactionManager} instance from container. 
     * @return the TransactionManager or <code>null</code> if none is registered.
     */
    public TransactionManager getTransactionManager();

    /**
     * Get the transaction context of the calling thread
     * @return the Transaction or <code>null</code> if no TransactionManager is used.
     */
    public Transaction getTransaction();
    
    /**
     * Gets user transaction instance.
     * @return user transaction object
     */
    public UserTransaction getUserTransaction();
    
    /**
     * Register transaction synch.
     * @param phase transaction phase
     * @param observer observer 
     * @param event event
     */
    public void registerTransactionSynchronization(TransactionPhase phase,
                                                   ObserverMethod<? super Object> observer,
                                                   Object event) throws Exception;
}