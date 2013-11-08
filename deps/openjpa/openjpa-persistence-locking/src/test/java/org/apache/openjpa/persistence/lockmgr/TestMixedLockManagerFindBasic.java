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

/**
 * Test JPA 2.0 em.find(LockMode) behaviors with "mixed" lock manager.
 */
public class TestMixedLockManagerFindBasic extends SequencedActionsTest {
    public void setUp() {
        setUp(LockEmployee.class
            , "openjpa.LockManager", "mixed"
            );
        commonSetUp();
    }

    public void testFindRead() {
        testCommon("testFindRead",
            LockModeType.READ, 0, 1);
    }

    public void testFindWrite() {
        testCommon("testFindWrite",
            LockModeType.WRITE, 1, 1);
    }

    public void testFindOptimistic() {
        testCommon("testFindOptimistic",
            LockModeType.OPTIMISTIC, 0, 1);
    }

    public void testFindOptimisticForceInc() {
        testCommon("testFindOptimisticForceInc",
            LockModeType.OPTIMISTIC_FORCE_INCREMENT, 1, 1);
    }

    public void testFindPessimisticRead() {
        testCommon("testFindPessimisticRead",
            LockModeType.PESSIMISTIC_READ, 0, 1);
    }

    public void testFindPessimisticWrite() {
        testCommon("testFindPessimisticWrite",
            LockModeType.PESSIMISTIC_WRITE, 0, 1);
    }

    public void testFindPessimisticForceInc() {
        testCommon("testFindPessimisticForceInc",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, 1, 1);
    }

    public void testCommon(String testName, LockModeType lockMode,
        int commitVersionIncrement, int updateCommitVersionIncrement) {
        Object[][] threadMain = {
            // Find entity, no transaction, no update.
            { Act.CreateEm },
            { Act.Clear },
            { Act.Find, 1 },
            { Act.TestEmployee, 1, Default_FirstName},

            // Find entity with lLock, no update and commit.
            { Act.StartTx },
            { Act.Clear },
            { Act.FindWithLock, 1, lockMode },
            { Act.TestEmployee, 1, Default_FirstName},
            { Act.SaveVersion },
            { Act.CommitTx },
            { Act.Clear },
            { Act.Find, 1 },
            { Act.TestEmployee, 1, Default_FirstName,
                commitVersionIncrement },

            // Find entity with lock, update and commit.
            { Act.StartTx },
            { Act.Clear },
            { Act.FindWithLock, 1, lockMode },
            { Act.SaveVersion },
            { Act.TestEmployee, 1, Default_FirstName},
            { Act.UpdateEmployee, 1, lockMode.toString() },
            { Act.CommitTx },
            { Act.Clear },
            { Act.Find, 1 },
            { Act.TestEmployee, 1, lockMode.toString(),
                updateCommitVersionIncrement },

            // Find entity with lock, update but rollback.
            { Act.StartTx },
            { Act.Clear },
            { Act.FindWithLock, 1, lockMode },
            { Act.SaveVersion },
            { Act.TestEmployee, 1, lockMode.toString()},
            { Act.UpdateEmployee, 1, lockMode.toString() + " Again" },
            { Act.RollbackTx },
            { Act.Clear },
            { Act.Find, 1 },
            { Act.TestEmployee, 1, lockMode.toString(), 0 },
            { Act.CloseEm }
        };

        launchActionSequence(testName, "LockMode=" + lockMode, threadMain);
    }
}
