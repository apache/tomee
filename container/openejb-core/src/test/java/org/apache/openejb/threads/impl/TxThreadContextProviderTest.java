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
package org.apache.openejb.threads.impl;

import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.ri.sp.PseudoSecurityService;
import org.apache.openejb.spi.SecurityService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * TOMEE-4731: clearing the transaction context must not fail when there is nothing to resume
 * or when the container (and its TransactionManager) is already gone.
 */
public class TxThreadContextProviderTest {
    private TransactionManager tm;

    @Before
    public void setUp() throws Exception {
        SystemInstance.reset();
        SystemInstance.get().setComponent(SecurityService.class, new PseudoSecurityService());
        tm = new GeronimoTransactionManager();
        SystemInstance.get().setComponent(TransactionManager.class, tm);
    }

    @After
    public void tearDown() throws Exception {
        if (tm.getTransaction() != null) {
            tm.rollback();
        }
        SystemInstance.reset();
    }

    @Test
    public void beginWithoutTransactionIsANoOp() {
        final ThreadContextRestorer restorer = TxThreadContextProvider.TxThreadContextClearingSnapshot.INSTANCE.begin();
        assertSame(ThreadContextProviderUtil.NOOP_RESTORER, restorer);
    }

    @Test
    public void endContextToleratesAGoneTransactionManager() throws Exception {
        tm.begin();
        final Transaction transaction = tm.getTransaction();
        final ThreadContextRestorer restorer = TxThreadContextProvider.TxThreadContextClearingSnapshot.INSTANCE.begin();
        assertNull(tm.getTransaction());

        // simulates the container shutting down while the context is still active
        SystemInstance.reset();

        try {
            restorer.endContext();
        } finally {
            transaction.rollback();
        }
    }

    @Test
    public void suspendedTransactionIsResumed() throws Exception {
        tm.begin();
        final Transaction transaction = tm.getTransaction();
        assertNotNull(transaction);

        final ThreadContextRestorer restorer = TxThreadContextProvider.TxThreadContextClearingSnapshot.INSTANCE.begin();
        assertNull("the transaction is cleared while the context is active", tm.getTransaction());

        restorer.endContext();
        assertSame("and resumed afterwards", transaction, tm.getTransaction());
    }

    @Test
    public void managedThreadTerminatingAfterShutdownDoesNotFail() throws Exception {
        final ManagedThreadFactoryImpl factory = new ManagedThreadFactoryImpl(
            "tomee-4731-", null, ContextServiceImplFactory.newDefaultContextService());

        final CountDownLatch running = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final AtomicReference<Throwable> uncaught = new AtomicReference<>();

        final Thread thread = factory.newThread(() -> {
            running.countDown();
            try {
                release.await();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        thread.setUncaughtExceptionHandler((t, e) -> uncaught.set(e));
        thread.start();

        assertTrue(running.await(1, TimeUnit.MINUTES));

        // the container shuts down while the worker thread is still alive
        SystemInstance.reset();

        release.countDown();
        thread.join(TimeUnit.MINUTES.toMillis(1));
        assertFalse(thread.isAlive());
        assertNull("the thread must terminate cleanly but got " + uncaught.get(), uncaught.get());
    }
}
