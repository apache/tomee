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

package org.apache.openjpa.ee;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.GeneralException;

/*
 * AbstractManagedRuntime.java
 *
 * Created on August 2, 2007, 2:38 PM
 *
 */
public abstract class AbstractManagedRuntime implements ManagedRuntime {

    private static Localizer _loc =
        Localizer.forPackage(AbstractManagedRuntime.class);
    /**
     * Returns a transaction key that can be used to associate transactions
     * and Brokers.
     * The default implementation returns the Transaction associated 
     * with the current thread's transaction.
     * @return the transaction key
     */
    public Object getTransactionKey() throws Exception, SystemException {
        return getTransactionManager().getTransaction();
    }

    /**
     * <P>
     * Do a unit of work which will execute outside of the current managed
     * transaction. The default implementation suspends the transaction prior to
     * execution, and resumes the transaction afterwards.
     * </P>
     * 
     * @param runnable
     *            The runnable wrapper for the work that will be done. The
     *            runnable object should be fully initialized with any state
     *            needed to execute.
     * 
     * @throws NotSupportedException
     *            if the current transaction can not be obtained, or an error 
     *            occurs when suspending or resuming the transaction.
     */
    public void doNonTransactionalWork(Runnable runnable) throws 
            NotSupportedException {
        TransactionManager tm = null;
        Transaction transaction = null;
        
        try { 
            tm = getTransactionManager(); 
        }
        catch(Exception e) {
            NotSupportedException nse =
                new NotSupportedException(e.getMessage());
            nse.initCause(e);
            throw nse;
        }
        try {
            transaction = tm.suspend();
        } catch (Exception e) {
            NotSupportedException nse = new NotSupportedException(  
                    _loc.get("exc-suspend-tran", e.getClass()).getMessage());
            nse.initCause(e);
            throw nse;
        }
        
        runnable.run();
        
        try {
            tm.resume(transaction);
        } catch (Exception e) {
            try {
                transaction.setRollbackOnly();
            }
            catch(SystemException se2) {
                throw new GeneralException(se2);
            }
            NotSupportedException nse =
                new NotSupportedException(
                        _loc.get("exc-resume-tran", e.getClass()).getMessage());
            nse.initCause(e);
            throw nse;
        } 

    }
}
