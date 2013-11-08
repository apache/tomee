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

import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import javax.persistence.TransactionRequiredException;

/**
 * Test JPA 2.0 em.lock(LockMode) exception behaviors with "mixed" lock manager.
 */
public class TestMixedLockManagerLockException extends SequencedActionsTest {
    public void setUp() {
        setUp(LockEmployee.class
            , "openjpa.LockManager", "mixed"
        );
        commonSetUp();
    }

    /**
     * TransactionRequiredException if there is no transaction
     */
    public void testLockTxReqExceptions() {
        Object[][] threadMainTxReqTest = {
            {Act.CreateEm},
            {Act.Find},
            {Act.SaveVersion},
            {Act.TestEmployee, 1, Default_FirstName},
            
            {Act.Lock, 1, LockModeType.NONE },
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.Lock, 1, LockModeType.READ },
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.Lock, 1, LockModeType.WRITE },
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.Lock, 1, LockModeType.OPTIMISTIC },
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.Lock, 1, LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.Lock, 1, LockModeType.PESSIMISTIC_READ},
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.Lock, 1, LockModeType.PESSIMISTIC_WRITE},
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.Lock, 1, LockModeType.PESSIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, TransactionRequiredException.class },
            {Act.CloseEm}
        };
        
        launchActionSequence("testLockTxReqExceptions()",
            null, threadMainTxReqTest);
    }

    /*
     * IllegalArgumentException if the instance is not an entity or is a
     *      detached entity
     */
    public void testLockIllegalArgrumentExceptions() {
        // Test invalid entity argument throws IllegalArgumentException.
        Object[][] threadMainInvEntityIllegalArgTest = {
            {Act.CreateEm},
            {Act.Find},
            {Act.SaveVersion},
            {Act.TestEmployee, 1, Default_FirstName},
            {Act.StartTx},
      
            {Act.LockObject, null, LockModeType.NONE },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.LockObject, "null", LockModeType.NONE },
            {Act.TestException, 0, IllegalArgumentException.class },
            
            {Act.LockObject, null, LockModeType.READ },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.LockObject, "null", LockModeType.READ },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.LockObject, null, LockModeType.WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.LockObject, "null", LockModeType.WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.LockObject, null, LockModeType.OPTIMISTIC },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.LockObject, "null", LockModeType.OPTIMISTIC },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.LockObject, null, LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.LockObject, "null", LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.LockObject, null, LockModeType.PESSIMISTIC_READ },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.LockObject, "null", LockModeType.PESSIMISTIC_READ },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.LockObject, null, LockModeType.PESSIMISTIC_WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.LockObject, "null", LockModeType.PESSIMISTIC_WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.LockObject, null, LockModeType.PESSIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.LockObject, "null", LockModeType.PESSIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.CloseEm}
       };
        launchActionSequence("testLockIllegalArgrumentExceptions()",
            "Test invalid entity.", threadMainInvEntityIllegalArgTest);
        
        // Test detached entity argument throws IllegalArgumentException.
        Object[][] threadMainDetachEntityIllegalArgTest = {
            {Act.CreateEm},
            {Act.Find},
            {Act.SaveVersion},
            {Act.TestEmployee, 1, Default_FirstName},
            {Act.StartTx},
            {Act.Detach, 1, 2},
            
            {Act.Lock, 2, LockModeType.PESSIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },
            
            {Act.Lock, 2, LockModeType.NONE },
            {Act.TestException, 0, IllegalArgumentException.class },
          
            {Act.Lock, 2, LockModeType.READ },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Lock, 2, LockModeType.WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Lock, 2, LockModeType.OPTIMISTIC },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Lock, 2, LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Lock, 2, LockModeType.PESSIMISTIC_READ },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Lock, 2, LockModeType.PESSIMISTIC_WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Lock, 2, LockModeType.PESSIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.CloseEm}
        };
        launchActionSequence("testLockIllegalArgrumentExceptions()",
            "Test detached entity.", threadMainDetachEntityIllegalArgTest);

        // Test detached argument from serialized entity throws 
        //  IllegalArgumentException.
        Object[][] threadMainDetachSerializeIllegalArgTest = {
            {Act.CreateEm},
            {Act.Find},
            {Act.SaveVersion},
            {Act.TestEmployee, 1, Default_FirstName},
            {Act.StartTx},
            {Act.DetachSerialize, 1, 2},
            
            {Act.Lock, 2, LockModeType.NONE },
            {Act.TestException, 0, IllegalArgumentException.class },
        
            {Act.Lock, 2, LockModeType.READ },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Lock, 2, LockModeType.WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Lock, 2, LockModeType.OPTIMISTIC },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Lock, 2, LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Lock, 2, LockModeType.PESSIMISTIC_READ },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Lock, 2, LockModeType.PESSIMISTIC_WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Lock, 2, LockModeType.PESSIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.CloseEm}
        };        
        launchActionSequence("testLockIllegalArgrumentExceptions()",
            "Test detached entity using serialization.",
            threadMainDetachSerializeIllegalArgTest);
        
        Object[][] threadMainRemoveIllegalArgTest = {
            {Act.CreateEm},
            {Act.Find},
            {Act.SaveVersion},
            {Act.TestEmployee, 1, Default_FirstName},
            {Act.StartTx},
            {Act.Remove},

            {Act.Lock, 1, LockModeType.NONE },
            {Act.TestException},
      
            {Act.Lock, 1, LockModeType.READ },
            {Act.TestException},

            {Act.Lock, 1, LockModeType.WRITE },
            {Act.TestException},

            {Act.Lock, 1, LockModeType.OPTIMISTIC },
            {Act.TestException},

            {Act.Lock, 1, LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException},

            {Act.Lock, 1, LockModeType.PESSIMISTIC_READ },
            {Act.TestException},

            {Act.Lock, 1, LockModeType.PESSIMISTIC_WRITE },
            {Act.TestException},

            {Act.Lock, 1, LockModeType.PESSIMISTIC_FORCE_INCREMENT },
            {Act.TestException},
            
            {Act.RollbackTx},
            {Act.CloseEm}
        };
        launchActionSequence(
            "testLockIllegalArgrumentExceptions()",
            "Test removed entity - no exception since it is still "
                +"in the context.",
            threadMainRemoveIllegalArgTest);
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
            LockModeType.NONE, false);
        commonLockOptimisticLockExceptions(
            LockModeType.READ, false);
        commonLockOptimisticLockExceptions(
            LockModeType.WRITE, false);
//        commonLockOptimisticLockExceptions(
//            LockModeType.OPTIMISTIC, false);
//        commonLockOptimisticLockExceptions(
//            LockModeType.OPTIMISTIC_FORCE_INCREMENT, false);
        commonLockOptimisticLockExceptions(
            LockModeType.PESSIMISTIC_READ, true);
//        commonLockOptimisticLockExceptions(
//            LockModeType.PESSIMISTIC_WRITE, true);
        commonLockOptimisticLockExceptions(
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, true);
    }
    
    public void commonLockOptimisticLockExceptions(LockModeType lockMode,
        boolean expectingOptLockException) {
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
            {Act.Find},
            {Act.Notify, 1},
            
            {Act.Wait},
            {Act.Lock, 1, lockMode},
            {Act.WaitAllChildren},
            {Act.TestException, 0, expectingOptLockException
                ? OptimisticLockException.class : null},
            
            {Act.RollbackTx},
            {Act.CloseEm}
        };
        Object[][] thread1OptLockExTest = {
            {Act.CreateEm},
            {Act.StartTx},
            {Act.Find},
            {Act.SaveVersion},
            
            {Act.Notify, 0},
            {Act.Wait},
            {Act.UpdateEmployee},
            
            {Act.CommitTx},
            {Act.Notify, 0},
            {Act.CloseEm}
        };        
        launchActionSequence("testLockOptimisticLockExceptions()", null,
            threadMainOptLockExTest, thread1OptLockExTest);
    }
}
