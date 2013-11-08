/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.ee;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

/**
 * This interface must be implemented by concrete plugins to application
 * servers in order to integrate the OpenJPA runtime in a managed environment.
 *
 * @author Abe White
 */
public interface ManagedRuntime {

    /**
     * Return the TransactionManager for the managed runtime. This
     * manager is used to register synchronization listeners, to
     * map transactional PersistenceManagers to the current transaction,
     * and possibly to enlist XA resources.
     */
    public TransactionManager getTransactionManager()
        throws Exception;

    /** 
     * Sets the rollback only flag on the current transaction. If the
     * TransactionManager is capable of tracking the cause of the
     * rollback-only flag, it will also pass along cause information.
     *  
     * @param  cause  the Throwable that caused the transaction to be
     *                marked for rollback, or null of none is known
     */
    public void setRollbackOnly(Throwable cause)
        throws Exception;

    /** 
     * Returns the Throwable that caused the current transaction to be
     * marked for rollback, provided that any exists.
     *
     * @return the Throwable cause, or null if none
     */
    public Throwable getRollbackCause()
        throws Exception;

    /**
     * Returns a transaction key that can be used to associate transactions
     * and Brokers.
     * @return the transaction key
     */
    public Object getTransactionKey()
        throws Exception, SystemException;

    /**
     * <P>
     * Do a unit of work which will execute outside of the current managed
     * transaction.
     * </P>
     * <P>
     * If the runnable object encounters an exception it should be wrapped in a
     * RuntimeException and thrown back to the caller
     * </P>
     * 
     * @param runnable
     *            The runnable wrapper for the work that will be done. The
     *            runnable object should be fully initialized with any state
     *            needed to execute.
     * 
     * @throws NotSupportedException
     *             if the transaction can not be suspended.
     */
    public void doNonTransactionalWork(Runnable runnable)
            throws NotSupportedException;
  
}
