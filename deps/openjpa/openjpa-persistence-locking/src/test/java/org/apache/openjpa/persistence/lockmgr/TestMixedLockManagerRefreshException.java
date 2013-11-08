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
import javax.persistence.TransactionRequiredException;

/**
 * Test JPA 2.0 em.refresh(LockMode) exception behaviors with "mixed"
 * lock manager.
 */
public class TestMixedLockManagerRefreshException extends SequencedActionsTest {
    public void setUp() {
        setUp(LockEmployee.class
            , "openjpa.LockManager", "mixed"
        );
        commonSetUp();
    }

    /**
     * TransactionRequiredException if there is no transaction
     */
    public void testRefreshNoTxReqExceptions() {
        Object[][] threadMainTxReqTest = {
            {Act.CreateEm},
            {Act.Find},
            {Act.SaveVersion},
            {Act.TestEmployee, 1, Default_FirstName},
            
            {Act.Refresh, 1, LockModeType.NONE },
            {Act.TestException, 0, null },
            
            {Act.Refresh, 1, LockModeType.READ },
            {Act.TestException, 0, null },
            
            {Act.Refresh, 1, LockModeType.WRITE },
            {Act.TestException, 0, null },
            
            {Act.Refresh, 1, LockModeType.OPTIMISTIC },
            {Act.TestException, 0, null },
            
            {Act.Refresh, 1, LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, null },
            
            {Act.Refresh, 1, LockModeType.PESSIMISTIC_READ},
            {Act.TestException, 0, null },
            
            {Act.Refresh, 1, LockModeType.PESSIMISTIC_WRITE},
            {Act.TestException, 0, null },
            
            {Act.Refresh, 1, LockModeType.PESSIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, null },
            {Act.CloseEm}
        };
        launchActionSequence("testLockTxReqExceptions()",
            null, threadMainTxReqTest);
    }

    /**
     * TransactionRequiredException if there is no transaction
     */
    public void testRefreshTxReqExceptions() {
        Object[][] threadMainTxReqTest = {
            {Act.CreateEm},
            {Act.Find},
            {Act.SaveVersion},
            {Act.TestEmployee, 1, Default_FirstName},
            
            {Act.RefreshWithLock, 1, LockModeType.NONE },
            {Act.TestException, 0, null },
            
            {Act.RefreshWithLock, 1, LockModeType.READ },
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.RefreshWithLock, 1, LockModeType.WRITE },
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.RefreshWithLock, 1, LockModeType.OPTIMISTIC },
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.RefreshWithLock, 1, LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.RefreshWithLock, 1, LockModeType.PESSIMISTIC_READ},
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.RefreshWithLock, 1, LockModeType.PESSIMISTIC_WRITE},
            {Act.TestException, 0, TransactionRequiredException.class },
            
            {Act.RefreshWithLock, 1, LockModeType.PESSIMISTIC_FORCE_INCREMENT },
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
    public void testRefreshIllegalArgrumentExceptions() {
        // Test invalid entity argument throws IllegalArgumentException.
        Object[][] threadMainInvEntityIllegalArgTest = {
            {Act.CreateEm},
            {Act.Find},
            {Act.SaveVersion},
            {Act.TestEmployee, 1, Default_FirstName},
            {Act.StartTx},
      
            {Act.RefreshObject, null, LockModeType.NONE },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.RefreshObject, "null", LockModeType.NONE },
            {Act.TestException, 0, IllegalArgumentException.class },
            
            {Act.RefreshObject, null, LockModeType.READ },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.RefreshObject, "null", LockModeType.READ },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.RefreshObject, null, LockModeType.WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.RefreshObject, "null", LockModeType.WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.RefreshObject, null, LockModeType.OPTIMISTIC },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.RefreshObject, "null", LockModeType.OPTIMISTIC },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.RefreshObject, null, 
                LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.RefreshObject, "null", 
                LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.RefreshObject, null, LockModeType.PESSIMISTIC_READ },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.RefreshObject, "null", LockModeType.PESSIMISTIC_READ },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.RefreshObject, null, LockModeType.PESSIMISTIC_WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.RefreshObject, "null", LockModeType.PESSIMISTIC_WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.RefreshObject, null, 
                LockModeType.PESSIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },
            {Act.RefreshObject, "null", 
                LockModeType.PESSIMISTIC_FORCE_INCREMENT },
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
            
            {Act.Refresh, 2, LockModeType.PESSIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },
            
            {Act.Refresh, 2, LockModeType.NONE },
            {Act.TestException, 0, IllegalArgumentException.class },
          
            {Act.Refresh, 2, LockModeType.READ },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Refresh, 2, LockModeType.WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Refresh, 2, LockModeType.OPTIMISTIC },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Refresh, 2, LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Refresh, 2, LockModeType.PESSIMISTIC_READ },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Refresh, 2, LockModeType.PESSIMISTIC_WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Refresh, 2, LockModeType.PESSIMISTIC_FORCE_INCREMENT },
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
            
            {Act.Refresh, 2, LockModeType.NONE },
            {Act.TestException, 0, IllegalArgumentException.class },
        
            {Act.Refresh, 2, LockModeType.READ },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Refresh, 2, LockModeType.WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Refresh, 2, LockModeType.OPTIMISTIC },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Refresh, 2, LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Refresh, 2, LockModeType.PESSIMISTIC_READ },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Refresh, 2, LockModeType.PESSIMISTIC_WRITE },
            {Act.TestException, 0, IllegalArgumentException.class },

            {Act.Refresh, 2, LockModeType.PESSIMISTIC_FORCE_INCREMENT },
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

            {Act.Refresh, 1, LockModeType.NONE },
            {Act.TestException, 1, IllegalArgumentException.class },
      
            {Act.Refresh, 1, LockModeType.READ },
            {Act.TestException, 1, IllegalArgumentException.class },

            {Act.Refresh, 1, LockModeType.WRITE },
            {Act.TestException, 1, IllegalArgumentException.class },

            {Act.Refresh, 1, LockModeType.OPTIMISTIC },
            {Act.TestException, 1, IllegalArgumentException.class },

            {Act.Refresh, 1, LockModeType.OPTIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 1, IllegalArgumentException.class },

            {Act.Refresh, 1, LockModeType.PESSIMISTIC_READ },
            {Act.TestException, 1, IllegalArgumentException.class },

            {Act.Refresh, 1, LockModeType.PESSIMISTIC_WRITE },
            {Act.TestException, 1, IllegalArgumentException.class },

            {Act.Refresh, 1, LockModeType.PESSIMISTIC_FORCE_INCREMENT },
            {Act.TestException, 1, IllegalArgumentException.class },
            
            {Act.RollbackTx},
            {Act.CloseEm}
        };
        launchActionSequence(
            "testLockIllegalArgrumentExceptions()",
            "Test removed entity - no exception since it is still "
                + "in the context.",
            threadMainRemoveIllegalArgTest);
    }
}
