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
package org.apache.openjpa.persistence.event;

import java.util.Collection;
import java.util.Collections;


import org.apache.openjpa.persistence.event.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import junit.framework.AssertionFailedError;

import org.apache.openjpa.event.TransactionEvent;
import org.apache.openjpa.event.TransactionListener;
import org.apache.openjpa.persistence.CallbackMode;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;

@AllowFailure(message="surefire excluded")
public class TestEvents
    extends AbstractTestCase {

    private TransactionEventListenerTestImpl transactionListener;

    public TestEvents(String s) {
        super(s, "eventcactusapp");
    }

    public void setUp() {
        transactionListener = new TransactionEventListenerTestImpl();
        deleteAll(RuntimeTest1.class);
    }

    public void testCommit() {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        ((OpenJPAEntityManagerSPI) pm)
            .addTransactionListener(transactionListener);

        assertEquals(0, transactionListener.status);

        RuntimeTest1 t1 = new RuntimeTest1("foo", 5);
        startTx(pm);
        assertEquals(TransactionEventListenerTestImpl.STARTED,
            transactionListener.status);

        pm.persist(t1);

        endTx(pm);
        assertEquals(TransactionEventListenerTestImpl.STARTED |
            TransactionEventListenerTestImpl.COMMIT_BEGUN |
            TransactionEventListenerTestImpl.COMMITTED,
            transactionListener.status);
    }

    public void testRollback() {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        ((OpenJPAEntityManagerSPI) pm)
            .addTransactionListener(transactionListener);

        assertEquals(0, transactionListener.status);

        RuntimeTest1 t1 = new RuntimeTest1("foo", 5);
        startTx(pm);
        assertEquals(TransactionEventListenerTestImpl.STARTED,
            transactionListener.status);

        pm.persist(t1);

        rollbackTx(pm);
        assertEquals(TransactionEventListenerTestImpl.STARTED |
            TransactionEventListenerTestImpl.ROLLEDBACK,
            transactionListener.status);
    }

    public void testObjectChanges() {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        ((OpenJPAEntityManagerSPI) pm)
            .addTransactionListener(transactionListener);

        // add a couple new object
        RuntimeTest1 t1 = new RuntimeTest1("t1", 0);
        RuntimeTest1 t2 = new RuntimeTest1("t2", 1);
        startTx(pm);
        pm.persist(t1);
        pm.persist(t2);
        endTx(pm);

        // now do some modifications
        transactionListener.status = 0;
        startTx(pm);
        RuntimeTest1 t3 = new RuntimeTest1("t3", 3);
        pm.persist(t3);
        t1.setStringField("baz");
        pm.remove(t2);
        endTx(pm);

        assertEquals(3, transactionListener.trans.size());
        assertTrue(transactionListener.trans.contains(t1));
        assertTrue(transactionListener.trans.contains(t2));
        assertTrue(transactionListener.trans.contains(t3));
    }

    public void testIgnoreCallbackModeExceptionConsumed() {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        ((OpenJPAEntityManagerSPI) pm)
            .addTransactionListener(transactionListener);
        //FIXME need to find an alternative
        ((OpenJPAEntityManagerSPI) pm)
            .setTransactionListenerCallbackMode(CallbackMode.IGNORE);
        transactionListener.exception = transactionListener.EXCEPTION;

        RuntimeTest1 t1 = new RuntimeTest1("foo", 5);
        startTx(pm);
        assertEquals(TransactionEventListenerTestImpl.STARTED,
            transactionListener.status);
        pm.persist(t1);
        endTx(pm);
        endEm(pm);

        assertEquals(TransactionEventListenerTestImpl.STARTED |
            TransactionEventListenerTestImpl.COMMIT_BEGUN |
            TransactionEventListenerTestImpl.COMMITTED,
            transactionListener.status);
    }

    public void testExceptionCausesRollback() {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        ((OpenJPAEntityManagerSPI) pm)
            .addTransactionListener(transactionListener);
        transactionListener.exception = transactionListener.EXCEPTION;

        RuntimeTest1 t1 = new RuntimeTest1("foo", 5);
        startTx(pm);
        assertEquals(TransactionEventListenerTestImpl.STARTED,
            transactionListener.status);
        pm.persist(t1);

        try {
            endTx(pm);
            fail("Commit should have caused exception.");
        } catch (AssertionFailedError afe) {
            bug(1139, afe, "Listener exceptions being swallowed");
            return;
        } catch (Exception je) {
            assertEquals("xxx", je.getMessage());
        }
        assertTrue(!(isActiveTx(pm)));
        endEm(pm);

        assertEquals(TransactionEventListenerTestImpl.STARTED |
            TransactionEventListenerTestImpl.COMMIT_BEGUN |
            TransactionEventListenerTestImpl.ROLLEDBACK,
            transactionListener.status);
    }

    public void testExceptionAfterCommitThrown() {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        ((OpenJPAEntityManagerSPI) pm)
            .addTransactionListener(transactionListener);
        transactionListener.exception = transactionListener.EXCEPTION_AFTER;

        RuntimeTest1 t1 = new RuntimeTest1("foo", 5);
        startTx(pm);
        assertEquals(TransactionEventListenerTestImpl.STARTED,
            transactionListener.status);
        pm.persist(t1);

        try {
            endTx(pm);
            fail("Commit should have caused exception.");
        } catch (AssertionFailedError afe) {
            bug(1139, afe, "Listener exceptions being swallowed");
        } catch (Exception je) {
            assertEquals("xxx", je.getMessage());
        }
        assertFalse(isActiveTx(pm));
        endEm(pm);

        assertEquals(TransactionEventListenerTestImpl.STARTED |
            TransactionEventListenerTestImpl.COMMIT_BEGUN |
            TransactionEventListenerTestImpl.COMMITTED,
            transactionListener.status);
    }

    private static class TransactionEventListenerTestImpl
        implements TransactionListener {

        static int STARTED = 1;
        static int COMMITTED = 2;
        static int ROLLEDBACK = 4;
        static int COMMIT_BEGUN = 8;
        static int EXCEPTION = 1;
        static int EXCEPTION_AFTER = 2;

        int exception;
        int status;
        Collection trans = Collections.EMPTY_LIST;

        public void afterBegin(TransactionEvent event) {
            status |= STARTED;
        }

        public void beforeFlush(TransactionEvent event) {
        }

        public void afterFlush(TransactionEvent event) {
        }

        public void beforeCommit(TransactionEvent event) {
            status |= COMMIT_BEGUN;
            trans = event.getTransactionalObjects();
            if (exception == EXCEPTION)
                throw new RuntimeException("xxx");
        }

        public void afterCommit(TransactionEvent event) {
            status |= COMMITTED;
            if (exception == EXCEPTION_AFTER)
                throw new RuntimeException("xxx");
        }

        public void afterRollback(TransactionEvent event) {
            status |= ROLLEDBACK;
        }

        public void afterStateTransitions(TransactionEvent event) {
        }

        public void afterCommitComplete(TransactionEvent event) {
        }

        public void afterRollbackComplete(TransactionEvent event) {
        }
    }
}
