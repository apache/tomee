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
public class TestMixedLockManagerFindPermutation extends SequencedActionsTest {
    public void setUp() {
        setUp(LockEmployee.class
            , "openjpa.LockManager", "mixed"
        );
        commonSetUp();
    }

    /* ======== Thread 1 : Read Lock ============*/
    public void testFindReadRead() {
        commonFindTest(
            "testFind(Read,Commit/Read,Commit)",
            LockModeType.READ, Act.CommitTx, 1, null, 
            LockModeType.READ, Act.CommitTx, 1, ExpectingOptimisticLockExClass);
        commonFindTest(
            "testFind(Read,Commit/Read,Rollback)",
            LockModeType.READ, Act.CommitTx, 1, null,
            LockModeType.READ, Act.RollbackTx, 1, null);
    }
    
    public void testFindReadWrite() {
        commonFindTest(
            "testFind(Read,Commit/Write,Commit)",
            LockModeType.READ, Act.CommitTx, 1, null,
            LockModeType.WRITE, Act.CommitTx, 1, ExpectingOptimisticLockExClass);
        commonFindTest(
            "testFind(Read,Commit/Write,Rollback)",
            LockModeType.READ, Act.CommitTx, 1, null,
            LockModeType.WRITE, Act.RollbackTx, 1, null);
    }
    
    public void testFindReadPessimisticRead() {
        commonFindTest(
            "testFind(Read,Commit/PessimisticRead,Commit)",
            LockModeType.READ, Act.CommitTx, 1, ExpectingOptimisticLockExClass, 
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 1, null);
        commonFindTest(
            "testFind(Read,Commit/PessimisticRead,Rollback)",
            LockModeType.READ, Act.CommitTx, 1, null,
            LockModeType.PESSIMISTIC_READ, Act.RollbackTx, 1, null);
    }
    
    public void testFindReadPessimisticWrite() {
        commonFindTest(
            "testFind(Read,Commit/PessimisticWrite,Commit)",
            LockModeType.READ, Act.CommitTx, 1, ExpectingOptimisticLockExClass, 
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 1, null);
        commonFindTest(
            "testFind(Read,Commit/PessimisticWrite,Rollback)",
            LockModeType.READ, Act.CommitTx, 1, null,
            LockModeType.PESSIMISTIC_WRITE, Act.RollbackTx, 1, null);
    }
    
    public void testFindReadPessimisticForceInc() {
        commonFindTest(
            "testFind(Read,Commit/PessimisticForceInc,Commit)",
            LockModeType.READ, Act.CommitTx, 1, ExpectingOptimisticLockExClass, 
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 1, null);
        commonFindTest(
            "testFind(Read,Commit/PessimisticForceInc,Rollback)",
            LockModeType.READ, Act.CommitTx, 1, null,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.RollbackTx, 1, null);
    }
    
    /* ======== Thread 1 : Write Lock ============*/
    public void testFindWriteRead() {
        commonFindTest(
            "testFind(Write,Commit/Read,Commit)",
            LockModeType.WRITE, Act.CommitTx, 1, null, 
            LockModeType.READ, Act.CommitTx, 1, ExpectingOptimisticLockExClass);
        commonFindTest(
            "testFind(Write,Commit/Read,Rollback)",
            LockModeType.WRITE, Act.CommitTx, 1, null,
            LockModeType.READ, Act.RollbackTx, 1, null);
    }
    
    public void testFindWriteWrite() {
        commonFindTest(
            "testFind(Write,Commit/Write,Commit)",
            LockModeType.WRITE, Act.CommitTx, 1, null,
            LockModeType.WRITE, Act.CommitTx, 1, ExpectingOptimisticLockExClass);
        commonFindTest(
            "testFind(Write,Commit/Write,Rollback)",
            LockModeType.WRITE, Act.CommitTx, 1, null,
            LockModeType.WRITE, Act.RollbackTx, 1, null);
    }
    
    public void testFindWritePessimisticRead() {
        commonFindTest(
            "testFind(Write,Commit/PessimisticRead,Commit)",
            LockModeType.WRITE, Act.CommitTx, 1, ExpectingOptimisticLockExClass, 
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 1, null);
        commonFindTest(
            "testFind(Write,Commit/PessimisticRead,Rollback)",
            LockModeType.WRITE, Act.CommitTx, 1, null,
            LockModeType.PESSIMISTIC_READ, Act.RollbackTx, 1, null);
    }
    
    public void testFindWritePessimisticWrite() {
        commonFindTest(
            "testFind(Write,Commit/PessimisticWrite,Commit)",
            LockModeType.WRITE, Act.CommitTx, 1, ExpectingOptimisticLockExClass, 
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 1, null);
        commonFindTest(
            "testFind(Write,Commit/PessimisticWrite,Rollback)",
            LockModeType.WRITE, Act.CommitTx, 1, null,
            LockModeType.PESSIMISTIC_WRITE, Act.RollbackTx, 1, null);
    }
    
    public void testFindWritePessimisticForceInc() {
        commonFindTest(
            "testFind(Write,Commit/PessimisticForceInc,Commit)",
            LockModeType.WRITE, Act.CommitTx, 1, ExpectingOptimisticLockExClass, 
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 1, null);
        commonFindTest(
            "testFind(Write,Commit/PessimisticForceInc,Rollback)",
            LockModeType.WRITE, Act.CommitTx, 1, null,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.RollbackTx, 1, null);
    }
    
    /* ======== Thread 1 : PessimisticRead Lock ============*/
    public void testFindPessimisticReadRead() {
        commonFindTest(
            "testFind(PessimisticRead,Commit/Read,Commit)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 1, null, 
            LockModeType.READ, Act.CommitTx, 1, ExpectingOptimisticLockExClass);
        commonFindTest(
            "testFind(PessimisticRead,Commit/Read,Rollback)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 1, null,
            LockModeType.READ, Act.RollbackTx, 1, null);
    }
    
    public void testFindPessimisticReadWrite() {
        commonFindTest(
            "testFind(PessimisticRead,Commit/Write,Commit)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 1, null,
            LockModeType.WRITE, Act.CommitTx, 1, ExpectingOptimisticLockExClass);
        commonFindTest(
            "testFind(PessimisticRead,Commit/Write,Rollback)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 1, null,
            LockModeType.WRITE, Act.RollbackTx, 1, null);
    }
    
    public void testFindPessimisticReadPessimisticRead() {
        commonFindTest(
            "testFind(PessimisticRead,Commit/PessimisticRead,Commit)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 1, null, 
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 1, null);
        commonFindTest(
            "testFind(PessimisticRead,Commit/PessimisticRead,Rollback)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 1, null,
            LockModeType.PESSIMISTIC_READ, Act.RollbackTx, 0, null); 
    }
    
    public void testFindPessimisticReadPessimisticWrite() {
        commonFindTest(
            "testFind(PessimisticRead,Commit/PessimisticWrite,Commit)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 1, null, 
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 1, null); 
        commonFindTest(
            "testFind(PessimisticRead,Commit/PessimisticWrite,Rollback)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 1, null,
            LockModeType.PESSIMISTIC_WRITE, Act.RollbackTx, 0, null);
    }
    
    public void testFindPessimisticReadPessimisticForceInc() {
        commonFindTest(
            "testFind(PessimisticRead,Commit/PessimisticForceInc,Commit)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 1, null, 
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 1, null);
        commonFindTest(
            "testFind(PessimisticRead,Commit/PessimisticForceInc,Rollback)",
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 1, null,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.RollbackTx, 0, null);
    }
    
    /* ======== Thread 1 : Pessimsitic Write Lock ============*/
    public void testFindPessimsiticWriteRead() {
        commonFindTest(
            "testFind(PessimsiticWrite,Commit/Read,Commit)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 1, null, 
            LockModeType.READ, Act.CommitTx, 1, ExpectingOptimisticLockExClass);
        commonFindTest(
            "testFind(PessimsiticWrite,Commit/Read,Rollback)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 1, null,
            LockModeType.READ, Act.RollbackTx, 1, null);
    }
    
    public void testFindPessimsiticWriteWrite() {
        commonFindTest(
            "testFind(PessimsiticWrite,Commit/Write,Commit)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 1, null,
            LockModeType.WRITE, Act.CommitTx, 1, ExpectingOptimisticLockExClass);
        commonFindTest(
            "testFind(PessimsiticWrite,Commit/Write,Rollback)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 1, null,
            LockModeType.WRITE, Act.RollbackTx, 1, null);
    }
    
    public void testFindPessimsiticWritePessimisticRead() {
        commonFindTest(
            "testFind(PessimsiticWrite,Commit/PessimisticRead,Commit)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 1, null, 
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 1, null); 
        commonFindTest(
            "testFind(PessimsiticWrite,Commit/PessimisticRead,Rollback)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 1, null,
            LockModeType.PESSIMISTIC_READ, Act.RollbackTx, 0, null); 
    }
    
    public void testFindPessimsiticWritePessimisticWrite() {
        commonFindTest(
            "testFind(PessimsiticWrite,Commit/PessimisticWrite,Commit)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 1, null, 
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 1, null); 
        commonFindTest(
            "testFind(PessimsiticWrite,Commit/PessimisticWrite,Rollback)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 1, null,
            LockModeType.PESSIMISTIC_WRITE, Act.RollbackTx, 0, null); 
    }
    
    public void testFindPessimsiticWritePessimisticForceInc() {
        commonFindTest(
            "testFind(PessimsiticWrite,Commit/PessimisticForceInc,Commit)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 1, null, 
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 1, null); 
        commonFindTest(
            "testFind(PessimsiticWrite,Commit/PessimisticForceInc,Rollback)",
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 1, null,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.RollbackTx, 0, null);
    }
    
    /* ======== Thread 1 : Pessimsitic Force Increment Lock ============*/
    public void testFindPessimsiticForceIncRead() {
        commonFindTest(
            "testFind(PessimsiticForceInc,Commit/Read,Commit)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 1, null, 
            LockModeType.READ, Act.CommitTx, 1, ExpectingOptimisticLockExClass);
        commonFindTest(
            "testFind(PessimsiticForceInc,Commit/Read,Rollback)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 1, null,
            LockModeType.READ, Act.RollbackTx, 1, null);
    }
    
    public void testFindPessimsiticForceIncWrite() {
        commonFindTest(
            "testFind(PessimsiticForceInc,Commit/Write,Commit)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 1, null,
            LockModeType.WRITE, Act.CommitTx, 1, ExpectingOptimisticLockExClass);
        commonFindTest(
            "testFind(PessimsiticForceInc,Commit/Write,Rollback)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 1, null,
            LockModeType.WRITE, Act.RollbackTx, 1, null);
    }
    
    public void testFindPessimsiticForceIncPessimisticRead() {
        commonFindTest(
            "testFind(PessimsiticForceInc,Commit/PessimisticRead,Commit)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 1, null, 
            LockModeType.PESSIMISTIC_READ, Act.CommitTx, 1, null); 
        commonFindTest(
            "testFind(PessimsiticForceInc,Commit/PessimisticRead,Rollback)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 1, null,
            LockModeType.PESSIMISTIC_READ, Act.RollbackTx, 0, null); 
    }
    
    public void testFindPessimsiticForceIncPessimisticWrite() {
        commonFindTest(
            "testFind(PessimsiticForceInc,Commit/PessimisticWrite,Commit)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 1, null, 
            LockModeType.PESSIMISTIC_WRITE, Act.CommitTx, 1, null);
        commonFindTest(
            "testFind(PessimsiticForceInc,Commit/PessimisticWrite,Rollback)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 1, null,
            LockModeType.PESSIMISTIC_WRITE, Act.RollbackTx, 0, null); 
    }
    
    public void testFindPessimsiticForceIncPessimisticForceInc() {
        commonFindTest(
            "testFind(PessimsiticForceInc,Commit/PessimisticForceInc,Commit)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 1, null, 
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 1, null); 
        commonFindTest(
            "testFind(PessimsiticForceInc,Commit/PessimisticForceInc,Rollback)",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.CommitTx, 1, null,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, Act.RollbackTx, 0, null);
    }

    private void commonFindTest( String testName, 
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
            
        Object[][] threadMain = {
                {Act.CreateEm},
                {Act.Find},
                {Act.SaveVersion},
                {Act.TestEmployee, 1, Default_FirstName},
                {Act.CloseEm},
                {Act.Sleep, 100},

                {Act.CreateEm},
                {Act.StartTx},
                {Act.FindWithLock, 1, t1Lock},
                {Act.SaveVersion},
                {Act.TestException},
                
                {Act.NewThread, 1 },
                {Act.StartThread, 1 },
                
                {Act.Wait},
                {Act.UpdateEmployee},                
                {t1IsCommit},
                
                {Act.Notify, 1},

                {Act.Find},
                
                {Act.WaitAllChildren},
                {Act.Find},
                {Act.TestEmployee, 1, null, t1VersionInc},
                {Act.TestException, 0, t1Exceptions },
                {Act.TestException, 1, t2Exceptions },
                {Act.CloseEm}
            };

            Object[][] thread1 = {
                {Act.CreateEm},
                {Act.StartTx},
                {Act.FindWithLock, 1, t2Lock},
                {Act.SaveVersion},
                {Act.TestException},
                {Act.Notify, 0},
                
                {Act.Wait},
                {Act.Sleep, 2000},  // sleep for at least 2 sec so that the entity name updated with timestamp 
                                    // in each of the 2 threads are different. 
                {Act.UpdateEmployee},
                
                {t2IsCommit},
                {Act.Sleep, 1000},
                {Act.Find},
                {Act.TestEmployee, 1, null, t2VersionInc},
                {Act.CloseEm}
            };
            launchActionSequence(testName, parameters, threadMain, thread1);
    }
}
