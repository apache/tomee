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
 * Test JPA 2.0 em.refresh(LockMode) basic behaviors with "mixed" lock manager.
 */
public class TestMixedLockManagerRefreshBasic extends SequencedActionsTest {
    public void setUp() {
        setUp(LockEmployee.class
            , "openjpa.LockManager", "mixed"
            );
        commonSetUp();
    }

    public void testRefreshRead() {
        testCommon("testRefreshRead",
            LockModeType.READ, 0, 1);
    }

    public void testRefreshWrite() {
        testCommon("testRefreshWrite",
            LockModeType.WRITE, 1, 1);
    }

    public void testRefreshOptimistic() {
        testCommon("testRefreshOptimistic",
            LockModeType.OPTIMISTIC, 0, 1);
    }

    public void testRefreshOptimisticForceInc() {
        testCommon("testRefreshOptimisticForceInc",
            LockModeType.OPTIMISTIC_FORCE_INCREMENT, 1, 1);
    }

    public void testRefreshPessimisticRead() {
        testCommon("testRefreshPessimisticRead",
            LockModeType.PESSIMISTIC_READ, 0, 1);
    }

    public void testRefreshPessimisticWrite() {
        testCommon("testRefreshPessimisticWrite",
            LockModeType.PESSIMISTIC_WRITE, 0, 1);
    }

    public void testRefreshPessimisticForceInc() {
        testCommon("testRefreshPessimisticForceInc",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, 1, 1);
    }

    public void testCommon(String testName, LockModeType lockMode,
        int commitVersionIncrement, int updateCommitVersionIncrement) {
        Object[][] threadMain = {
            // Find entity, refresh with Lock, no update and commit.
            { Act.CreateEm },
            { Act.StartTx },
            { Act.Clear },
            { Act.Find, 1 },
            { Act.TestEmployee, 1, Default_FirstName},
            { Act.SaveVersion },
            { Act.RefreshWithLock, 1, lockMode },
            { Act.TestEmployee, 1, Default_FirstName},
            { Act.CommitTx },
            { Act.Clear },
            { Act.Find, 1 },
            { Act.TestEmployee, 1, Default_FirstName,
                commitVersionIncrement },

            // Find entity, refresh with Lock, update and commit.
            { Act.StartTx },
            { Act.Clear },
            { Act.Find, 1 },
            { Act.TestEmployee, 1, Default_FirstName},
            { Act.SaveVersion },
            { Act.UpdateEmployee, 1, "New Name 2"},
            { Act.TestEmployee, 1, "New Name 2"},
            { Act.RefreshWithLock, 1, lockMode },
            { Act.TestEmployee, 1, Default_FirstName, 0},
            { Act.UpdateEmployee, 1, lockMode.toString()},
            { Act.CommitTx },
            { Act.Clear },
            { Act.Find, 1 },
            { Act.TestEmployee, 1, lockMode.toString(),
                updateCommitVersionIncrement },

            // Find entity, refresh with Lock, update and rollback.
            { Act.StartTx },
            { Act.Clear },
            { Act.Find, 1},
            { Act.TestEmployee, 1, lockMode.toString()},
            { Act.SaveVersion },
            { Act.UpdateEmployee, 1, "New Name 3"},
            { Act.TestEmployee, 1, "New Name 3", 0},
            { Act.RefreshWithLock, 1, lockMode },
            { Act.TestEmployee, 1, lockMode.toString(), 0},
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
