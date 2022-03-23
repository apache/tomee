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
package org.apache.openejb.threads;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedTask;
import jakarta.enterprise.concurrent.ManagedTaskListener;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(ApplicationComposer.class)
public class TransactionPropagationTest {
    @Module
    public Class<?>[] classes() {
        return new Class<?>[]{Starter.class};
    }

    @EJB
    private Starter starter;

    @Test
    public void testTxPropagation() {
        starter.start();
    }

    @Singleton
    public static class Starter {
        @Resource
        private ContextService cs;

        @Resource
        private TransactionManager txMgr;

        public void start() {
            cs.createContextualProxy(new IWantMyOwnTransaction(), Collections.singletonMap(ManagedTask.TRANSACTION, ManagedTask.SUSPEND), Runnable.class).run();
            try {
                cs.createContextualProxy(new IWantMyTheCallerTransaction(txMgr.getTransaction()), Collections.singletonMap(ManagedTask.TRANSACTION, ManagedTask.USE_TRANSACTION_OF_EXECUTION_THREAD), Runnable.class).run();
            } catch (final SystemException e) {
                fail(e.getMessage());
            }
        }
    }

    public static class IWantMyOwnTransaction implements Runnable {
        @Override
        public void run() {
            try {
                assertNull(OpenEJB.getTransactionManager().getTransaction());
            } catch (final SystemException e) {
                fail(e.getMessage());
            }
        }
    }

    public static class IWantMyTheCallerTransaction implements Runnable {
        private final Transaction callerTx;

        public IWantMyTheCallerTransaction(final Transaction callerTx) {
            this.callerTx = callerTx;
        }

        @Override
        public void run() {
            try {
                assertEquals(callerTx, OpenEJB.getTransactionManager().getTransaction());
            } catch (final SystemException e) {
                fail(e.getMessage());
            }
        }
    }
}
