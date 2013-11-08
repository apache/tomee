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
package org.apache.openjpa.persistence.lockmgr;

import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import javax.persistence.TransactionRequiredException;

/**
 * Test JPA 2.0 em.find(LockMode) exception behaviors with "mixed" lock manager.
 */
public class TestMixedLockManagerFindException extends SequencedActionsTest {
    public void setUp() {
        setUp(LockEmployee.class
            , "openjpa.LockManager", "mixed"
        );
        commonSetUp();
    }

    /**
     * TransactionRequiredException if there is no transaction
     */
    public void testFindTxReqExceptions() {
        Object[][] threadMainTxReqTest = {
            {Act.CreateEm},
            {Act.Find},
            {Act.SaveVersion},
            {Act.TestEmployee, 1, Default_FirstName},
            
            {Act.FindWithLock, 1, LockModeType.NONE },
            {Act.TestException},
            
            {Act.FindWithLock, 1, LockModeType.READ },
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.FindWithLock, 1, LockModeType.WRITE },
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.FindWithLock, 1, LockModeType.OPTIMISTIC },
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.FindWithLock, 1, LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.FindWithLock, 1, LockModeType.PESSIMISTIC_READ},
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.FindWithLock, 1, LockModeType.PESSIMISTIC_WRITE},
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.FindWithLock, 1, LockModeType.PESSIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, TransactionRequiredException.class },
            {Act.CloseEm},
        };
        launchActionSequence("testLockTxReqExceptions()",
            null, threadMainTxReqTest);
    }

    /*
     * IllegalArgumentException if the instance is not an entity or is a
     *      detached entity
     */
    public void testFindIllegalArgrumentExceptions() {
        // Test invalid entity argument throws IllegalArgumentException.
        Object[][] threadMainInvEntityIllegalArgTest = {
            {Act.CreateEm},
            {Act.Find},
            {Act.SaveVersion},
            {Act.TestEmployee, 1, Default_FirstName},
            {Act.StartTx},
      
            {Act.FindObject, null, 0, LockModeType.NONE },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.FindObject, Object.class, 0, LockModeType.NONE },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.FindObject, LockEmployee.class, null, LockModeType.NONE },
            {Act.TestException, 0, EntityNotFoundException.class },
            {Act.FindObject, LockEmployee.class, Boolean.TRUE,
                LockModeType.NONE },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.FindObject, null, 0, LockModeType.READ },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.FindObject, Object.class, 0, LockModeType.READ },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.FindObject, LockEmployee.class, null, LockModeType.READ },
            {Act.TestException, 0, EntityNotFoundException.class },
            {Act.FindObject, LockEmployee.class, Boolean.TRUE,
                LockModeType.READ },
            {Act.TestException, 0, IllegalArgumentException.class },
            
            {Act.FindObject, null, 0, LockModeType.WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.FindObject, Object.class, 0, LockModeType.WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.FindObject, LockEmployee.class, null, LockModeType.WRITE },
            {Act.TestException, 0, EntityNotFoundException.class },
            {Act.FindObject, LockEmployee.class, Boolean.TRUE,
                LockModeType.WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },
            
            {Act.FindObject, null, 0, LockModeType.OPTIMISTIC },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.FindObject, Object.class, 0, LockModeType.OPTIMISTIC },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.FindObject, LockEmployee.class, null,
                LockModeType.OPTIMISTIC },
            {Act.TestException, 0, EntityNotFoundException.class },
            {Act.FindObject, LockEmployee.class, Boolean.TRUE,
                LockModeType.OPTIMISTIC },
            {Act.TestException, 0, IllegalArgumentException.class },
            
            {Act.FindObject, null, 0,
                LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.FindObject, Object.class, 0, 
                LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.FindObject, LockEmployee.class, null,
                LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, EntityNotFoundException.class },
            {Act.FindObject, LockEmployee.class, Boolean.TRUE,
                LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },
            
            {Act.FindObject, null, 0,
                LockModeType.PESSIMISTIC_READ },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.FindObject, Object.class, 0,
                LockModeType.PESSIMISTIC_READ },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.FindObject, LockEmployee.class, null,
                LockModeType.PESSIMISTIC_READ },
            {Act.TestException, 0, EntityNotFoundException.class },
            {Act.FindObject, LockEmployee.class, Boolean.TRUE,
                LockModeType.PESSIMISTIC_READ },
            {Act.TestException, 0, IllegalArgumentException.class },
            
            {Act.FindObject, null, 0,
                LockModeType.PESSIMISTIC_WRITE },
            {Act.TestException, 0,
                    IllegalArgumentException.class },
            {Act.FindObject, Object.class, 0,
                LockModeType.PESSIMISTIC_WRITE },
            {Act.TestException, 0,
                    IllegalArgumentException.class },
            {Act.FindObject, LockEmployee.class, null,
                LockModeType.PESSIMISTIC_WRITE },
            {Act.TestException, 0,
                    EntityNotFoundException.class },
            {Act.FindObject, LockEmployee.class, Boolean.TRUE,
                LockModeType.PESSIMISTIC_WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },
            
            {Act.FindObject, null, 0,
                LockModeType.PESSIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0,
                IllegalArgumentException.class },
            {Act.FindObject, Object.class, 0,
                LockModeType.PESSIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0,
                IllegalArgumentException.class },
            {Act.FindObject, LockEmployee.class, null,
                LockModeType.PESSIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, EntityNotFoundException.class },
            {Act.FindObject, LockEmployee.class, Boolean.TRUE, 
                LockModeType.PESSIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.CloseEm}
        };
        launchActionSequence("testLockIllegalArgrumentExceptions()",
            "Test invalid entity.", threadMainInvEntityIllegalArgTest);
    }
    
    /*
     * If a pessimistic lock mode type is specified and the entity
     * contains a version attribute, the persistence provider must
     * also perform optimistic version checks when obtaining the
     * database lock. If these checks fail, the
     * OptimisticLockException will be thrown.
     */
    public void testLockOptimisticLockExceptions() {
        commonLockOptimisticLockExceptions(
            LockModeType.NONE, true, false);
//        commonLockOptimisticLockExceptions(
//            LockModeType.READ, true, false);
//        commonLockOptimisticLockExceptions(
//            LockModeType.WRITE, true, false);
        commonLockOptimisticLockExceptions(
            LockModeType.OPTIMISTIC, true, false);
        commonLockOptimisticLockExceptions(
            LockModeType.OPTIMISTIC_FORCE_INCREMENT, true, false);
//        commonLockOptimisticLockExceptions(
//            LockModeType.PESSIMISTIC_READ, false, true);
        commonLockOptimisticLockExceptions(
            LockModeType.PESSIMISTIC_WRITE, false, true);
        commonLockOptimisticLockExceptions(
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, false, true);
    }
    
    public void commonLockOptimisticLockExceptions(LockModeType lockMode,
        boolean expectingOptLockException1, 
        boolean expectingOptLockException2) {
        Object[][] threadMainOptLockExTest = {
            {Act.CreateEm},
            {Act.Find},
            {Act.SaveVersion},
            {Act.TestEmployee, 1, Default_FirstName},
            {Act.Clear},
            
            {Act.NewThread, 1 },
            {Act.StartThread, 1 },
            {Act.Wait},
            
            {Act.StartTx},
            {Act.FindWithLock, 1, lockMode},
            {Act.UpdateEmployee},
            {Act.Notify, 1},
            {Act.Wait},
            {Act.CommitTx},
            
            {Act.WaitAllChildren},
            {Act.TestException, 0, expectingOptLockException1
                ? OptimisticLockException.class : null},
            {Act.TestException, 1, expectingOptLockException2
                    ? OptimisticLockException.class : null},
            {Act.CloseEm}
        };
        Object[][] thread1OptLockExTest = {
            {Act.CreateEm},
            {Act.StartTx},
            {Act.FindWithLock, 1, LockModeType.OPTIMISTIC},
            {Act.SaveVersion},
            {Act.UpdateEmployee},
            
            {Act.Notify, 0},
            {Act.Wait},
            
            {Act.CommitTx},
            {Act.Notify, 0},
            {Act.CloseEm}
        };        
        launchActionSequence("testLockOptimisticLockExceptions()", null,
            threadMainOptLockExTest, thread1OptLockExTest);
    }
}
