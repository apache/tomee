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

import java.util.Arrays;

import javax.persistence.LockModeType;

/**
 * Test JPA 2.0 LockMode type permutation behaviors with "mixed" lock manager.
 */
public class TestMixedLockManagerRefreshPermutation 
    extends SequencedActionsTest {
    public void setUp() {
        setUp(LockEmployee.class
            , "openjpa.LockManager", "mixed"
        );
        commonSetUp();
    }

    /* ======== Thread 1 : Read Lock ============*/
    public void testRefreshReadRead() {
        commonRefreshTest(
            "testRefresh(Read,Commit/Read,Commit)",
            LockModeType.READ, Act.CommitTx, 2, null, 
            LockModeType.READ, Act.CommitTx, 2, ExpectingOptimisticLockExClass);
        commonRefreshTest(
            "testRefresh(Read,Commit/Read,Rollback)",
            LockModeType.READ, Act.CommitTx, 2, null,
            LockModeType.READ, Act.RollbackTx, 2, null);
    }
    
    public void testRefreshReadWrite() {
        commonRefreshTest(
            "testRefresh(Read,Commit/Write,Commit)",
            LockModeType.READ, Act.CommitTx, 2, null,
            LockModeType.WRITE, Act.CommitTx, 2,
                ExpectingOptimisticLockExClass);
        commonRefreshTest(
            "testRefresh(Read,Commit/Write,Rollback)",
            LockModeType.READ, Act.CommitTx, 2, null,
            LockModeType.WRITE, Act.RollbackTx, 2, null);
    }
    
    public void testRefreshReadPessimisticRead() {
        commonRefreshTest(
            "testRefresh(Read,Commit/PessimisticRead,Commit)",
            LockModeType.READ, Act.CommitTx, 2, null, // thread 2 tmo  
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 2, null);
        commonRefreshTest(
            "testRefresh(Read,Commit/PessimisticRead,Rollback)",
            LockModeType.READ, Act.CommitTx, 2, null,
            LockModeType.PESSIMISTIC_READ, Act.RollbackTx, 2, null);
    }
    
    public void testRefreshReadPessimisticWrite() {
        commonRefreshTest(
            "testRefresh(Read,Commit/PessimisticWrite,Commit)",
            LockModeType.READ, Act.CommitTx, 2, null, 
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 2, null);
        commonRefreshTest(
            "testRefresh(Read,Commit/PessimisticWrite,Rollback)",
            LockModeType.READ, Act.CommitTx, 2, null,
            LockModeType.PESSIMISTIC_WRITE, Act.RollbackTx, 2, null);
    }
    
    public void testRefreshReadPessimisticForceInc() {
        commonRefreshTest(
            "testRefresh(Read,Commit/PessimisticForceInc,Commit)",
            LockModeType.READ, Act.CommitTx, 2, ExpectingOptimisticLockExClass, 
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 2, null);
        commonRefreshTest(
            "testRefresh(Read,Commit/PessimisticForceInc,Rollback)",
            LockModeType.READ, Act.CommitTx, 2, null,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.RollbackTx, 2, null);
    }
    
    /* ======== Thread 1 : Write Lock ============*/
    public void testRefreshWriteRead() {
        commonRefreshTest(
            "testRefresh(Write,Commit/Read,Commit)",
            LockModeType.WRITE, Act.CommitTx, 2, null, 
            LockModeType.READ, Act.CommitTx, 2, ExpectingOptimisticLockExClass);
        commonRefreshTest(
            "testRefresh(Write,Commit/Read,Rollback)",
            LockModeType.WRITE, Act.CommitTx, 2, null,
            LockModeType.READ, Act.RollbackTx, 2, null);
    }
    
    public void testRefreshWriteWrite() {
        commonRefreshTest(
            "testRefresh(Write,Commit/Write,Commit)",
            LockModeType.WRITE, Act.CommitTx, 2, null,
            LockModeType.WRITE, Act.CommitTx, 2, ExpectingOptimisticLockExClass);
        commonRefreshTest(
            "testRefresh(Write,Commit/Write,Rollback)",
            LockModeType.WRITE, Act.CommitTx, 2, null,
            LockModeType.WRITE, Act.RollbackTx, 2, null);
    }
    
    public void testRefreshWritePessimisticRead() {
        commonRefreshTest(
            "testRefresh(Write,Commit/PessimisticRead,Commit)",
            LockModeType.WRITE, Act.CommitTx, 2, null, 
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 2, null);
        commonRefreshTest(
            "testRefresh(Write,Commit/PessimisticRead,Rollback)",
            LockModeType.WRITE, Act.CommitTx, 2, null,
            LockModeType.PESSIMISTIC_READ, Act.RollbackTx, 2, null);
    }
    
    public void testRefreshWritePessimisticWrite() {
        commonRefreshTest(
            "testRefresh(Write,Commit/PessimisticWrite,Commit)",
            LockModeType.WRITE, Act.CommitTx, 2, null, 
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 2, null);
        commonRefreshTest(
            "testRefresh(Write,Commit/PessimisticWrite,Rollback)",
            LockModeType.WRITE, Act.CommitTx, 2, null,
            LockModeType.PESSIMISTIC_WRITE, Act.RollbackTx, 2, null);
    }
    
    public void testRefreshWritePessimisticForceInc() {
        commonRefreshTest(
            "testRefresh(Write,Commit/PessimisticForceInc,Commit)",
            LockModeType.WRITE, Act.CommitTx, 2, ExpectingOptimisticLockExClass,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 2, null);
        commonRefreshTest(
            "testRefresh(Write,Commit/PessimisticForceInc,Rollback)",
            LockModeType.WRITE, Act.CommitTx, 2, null,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.RollbackTx, 2, null);
    }
    
    /* ======== Thread 1 : PessimisticRead Lock ============*/
    public void testRefreshPessimisticReadRead() {
        commonRefreshTest(
            "testRefresh(PessimisticRead,Commit/Read,Commit)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 2, null, 
            LockModeType.READ, Act.CommitTx, 2, ExpectingOptimisticLockExClass);
        commonRefreshTest(
            "testRefresh(PessimisticRead,Commit/Read,Rollback)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 2, null,
            LockModeType.READ, Act.RollbackTx, 2, null);
    }
    
    public void testRefreshPessimisticReadWrite() {
        commonRefreshTest(
            "testRefresh(PessimisticRead,Commit/Write,Commit)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 2, null,
            LockModeType.WRITE, Act.CommitTx, 2, ExpectingOptimisticLockExClass);
        commonRefreshTest(
            "testRefresh(PessimisticRead,Commit/Write,Rollback)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 2, null,
            LockModeType.WRITE, Act.RollbackTx, 2, null);
    }
    
    public void testRefreshPessimisticReadPessimisticRead() {
        commonRefreshTest(
            "testRefresh(PessimisticRead,Commit/PessimisticRead,Commit)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 2, null, 
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 2, null);
        commonRefreshTest(
            "testRefresh(PessimisticRead,Commit/PessimisticRead,Rollback)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 2, null,
            LockModeType.PESSIMISTIC_READ, Act.RollbackTx, 2, null);
    }
    
    public void testRefreshPessimisticReadPessimisticWrite() {
        commonRefreshTest(
            "testRefresh(PessimisticRead,Commit/PessimisticWrite,Commit)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 2, null, 
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 2, null); 
        commonRefreshTest(
            "testRefresh(PessimisticRead,Commit/PessimisticWrite,Rollback)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 2, null,
            LockModeType.PESSIMISTIC_WRITE, Act.RollbackTx, 2, null);
    }
    
    public void testRefreshPessimisticReadPessimisticForceInc() {
        commonRefreshTest(
            "testRefresh(PessimisticRead,Commit/PessimisticForceInc,Commit)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 3, null, 
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 3, null);
        commonRefreshTest(
            "testRefresh(PessimisticRead,Commit/PessimisticForceInc,Rollback)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 2, null,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.RollbackTx, 2, null);
    }
    
    /* ======== Thread 1 : Pessimistic Write Lock ============*/
    public void testRefreshPessimsiticWriteRead() {
        commonRefreshTest(
            "testRefresh(PessimsiticWrite,Commit/Read,Commit)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 2, null, 
            LockModeType.READ, Act.CommitTx, 2, ExpectingOptimisticLockExClass);
        commonRefreshTest(
            "testRefresh(PessimsiticWrite,Commit/Read,Rollback)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 2, null,
            LockModeType.READ, Act.RollbackTx, 2, null);
    }
    
    public void testRefreshPessimsiticWriteWrite() {
        commonRefreshTest(
            "testRefresh(PessimsiticWrite,Commit/Write,Commit)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 2, null,
            LockModeType.WRITE, Act.CommitTx, 2, ExpectingOptimisticLockExClass);
        commonRefreshTest(
            "testRefresh(PessimsiticWrite,Commit/Write,Rollback)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 2, null,
            LockModeType.WRITE, Act.RollbackTx, 2, null);
    }
    
    public void testRefreshPessimsiticWritePessimisticRead() {
        commonRefreshTest(
            "testRefresh(PessimsiticWrite,Commit/PessimisticRead,Commit)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 2, null, 
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 2, null); 
        commonRefreshTest(
            "testRefresh(PessimsiticWrite,Commit/PessimisticRead,Rollback)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 2, null,
            LockModeType.PESSIMISTIC_READ, Act.RollbackTx, 2, null); 
    }
    
    public void testRefreshPessimsiticWritePessimisticWrite() {
        commonRefreshTest(
            "testRefresh(PessimsiticWrite,Commit/PessimisticWrite,Commit)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 2, null, 
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 2, null);
        commonRefreshTest(
            "testRefresh(PessimsiticWrite,Commit/PessimisticWrite,Rollback)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 2, null,
            LockModeType.PESSIMISTIC_WRITE, Act.RollbackTx, 2, null); 
    }
    
    public void testRefreshPessimsiticWritePessimisticForceInc() {
        commonRefreshTest(
            "testRefresh(PessimsiticWrite,Commit/PessimisticForceInc,Commit)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 3, null, 
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 3, null); 
        commonRefreshTest(
            "testRefresh(PessimsiticWrite,Commit/PessimisticForceInc,Rollback)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 2, null,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.RollbackTx, 2, null);
    }
    
    /* ======== Thread 1 : Pessimistic Force Increment Lock ============*/
    public void testRefreshPessimsiticForceIncRead() {
        commonRefreshTest(
            "testRefresh(PessimsiticForceInc,Commit/Read,Commit)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 2, null, 
            LockModeType.READ, Act.CommitTx, 2, ExpectingOptimisticLockExClass);
        commonRefreshTest(
            "testRefresh(PessimsiticForceInc,Commit/Read,Rollback)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 2, null,
            LockModeType.READ, Act.RollbackTx, 2, null);
    }
    
    public void testRefreshPessimsiticForceIncWrite() {
        commonRefreshTest(
            "testRefresh(PessimsiticForceInc,Commit/Write,Commit)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 2, null,
            LockModeType.WRITE, Act.CommitTx, 2, ExpectingOptimisticLockExClass);
        commonRefreshTest(
            "testRefresh(PessimsiticForceInc,Commit/Write,Rollback)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 2, null,
            LockModeType.WRITE, Act.RollbackTx, 2, null);
    }
    
    public void testRefreshPessimsiticForceIncPessimisticRead() {
        commonRefreshTest(
            "testRefresh(PessimsiticForceInc,Commit/PessimisticRead,Commit)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 2, null, 
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 2, null); 
        commonRefreshTest(
            "testRefresh(PessimsiticForceInc,Commit/PessimisticRead,Rollback)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 2, null,
            LockModeType.PESSIMISTIC_READ, Act.RollbackTx, 2, null); 
    }
    
    public void testRefreshPessimsiticForceIncPessimisticWrite() {
        commonRefreshTest(
            "testRefresh(PessimsiticForceInc,Commit/PessimisticWrite,Commit)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 2, null, 
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 2, null); 
        commonRefreshTest(
            "testRefresh(PessimsiticForceInc,Commit/PessimisticWrite,Rollback)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 2, null,
            LockModeType.PESSIMISTIC_WRITE, Act.RollbackTx, 2, null);
    }
    
    public void testRefreshPessimsiticForceIncPessimisticForceInc() {
        commonRefreshTest(
            "testRefresh(PessimsiticForceInc,Commit/PessimisticForceInc,Commit)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 3, null, 
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 2, null);
        commonRefreshTest(
            "testRefresh(PessimsiticForceInc,Commit/PessimisticForceInc,Rollback)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 2, null,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.RollbackTx, 2, null); 
    }

    private void commonRefreshTest( String testName, 
        LockModeType t1Lock, Act t1IsCommit, int t1VersionInc, 
            Class<?>[] t1Exceptions, 
        LockModeType t2Lock, Act t2IsCommit, int t2VersionInc,
            Class<?>[] t2Exceptions ) {
        String[] parameters = new String[] {
            "Thread 1: lock= " + t1Lock + ", isCommit= " + t1IsCommit +
                ", versionInc= +" + t1VersionInc +
                ", expectedEx= " + Arrays.toString(t1Exceptions),
            "Thread 2: lock= " + t2Lock + ", isCommit= " + t2IsCommit + 
                ", versionInc= +" + t2VersionInc +
                ", expectedEx= " + Arrays.toString(t2Exceptions)};
            
        String t1Message1 = "Refresh in Thread 0";
        String t1Message2 = "Refresh in Thread 0 Again";
        String t2Message1 = "Refresh in Thread 1";
        
        Object[][] threadMain = {
                {Act.CreateEm},
                {Act.Find},
                {Act.SaveVersion},
                {Act.TestEmployee, 1, Default_FirstName},
                
                {Act.NewThread, 1 },
                {Act.StartThread, 1 },
                {Act.Wait},
      
                {Act.StartTx},
                {Act.UpdateEmployee, 1, t1Message1},
                {Act.TestEmployee, 1, t1Message1},
                {Act.CommitTx},
                {Act.TestException},
                {Act.Notify, 1},
                {Act.Wait},
        
                {Act.StartTx},
                {Act.RefreshWithLock, 1, t1Lock},
                {Act.TestLockMode, 1, t1Lock},
                {Act.TestEmployee, 1, t1Message1},
                {Act.UpdateEmployee, 1, t1Message2},
                {Act.TestEmployee, 1, t1Message2},
        
                {t1IsCommit},
                
                {Act.Notify, 1},

                {Act.WaitAllChildren},
                {Act.Find},
                {Act.TestEmployee, 1, null, t1VersionInc},
        
                {Act.TestException, 0, t1Exceptions },
                {Act.TestException, 1, t2Exceptions },
                {Act.CloseEm}
            };

            Object[][] thread1 = {
                {Act.CreateEm},
                {Act.Find, 1},
                {Act.SaveVersion},
                {Act.TestEmployee, 1, Default_FirstName},
                {Act.TestException},
                {Act.Notify, 0},
                {Act.Wait},
                
                {Act.StartTx},
                {Act.UpdateEmployee, 1, t2Message1},
                {Act.TestEmployee, 1, t2Message1},
                {Act.RefreshWithLock, 1, t2Lock },
                {Act.TestLockMode, 1, t2Lock},
                {Act.TestEmployee, 1, t1Message1},
        
                {Act.Notify, 0},
                {Act.Wait},
                {t2IsCommit},
        
                {Act.CloseEm}
            };
            launchActionSequence(testName, parameters, threadMain, thread1);
    }
}
