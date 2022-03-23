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
package org.apache.openejb.cdi.transactional;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionalException;
import jakarta.transaction.UserTransaction;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static jakarta.transaction.Transactional.TxType.MANDATORY;
import static jakarta.transaction.Transactional.TxType.NEVER;
import static jakarta.transaction.Transactional.TxType.NOT_SUPPORTED;
import static jakarta.transaction.Transactional.TxType.REQUIRED;
import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(ApplicationComposer.class)
public class TransactionalTest {
    @Module
    @Classes(cdi = true, value = { TxBean.class })
    public EjbJar jar() {
        return new EjbJar();
    }

    @Inject
    private TxBean bean;

    @Test
    public void exceptionPriorityRules() {
        assertFalse(new InterceptorBase.ExceptionPriotiryRules(new Class[] {IllegalArgumentException.class}, new Class[]{IllegalArgumentException.class})
                .accept(new IllegalArgumentException(""), new Class[0]));
        assertTrue(new InterceptorBase.ExceptionPriotiryRules(new Class[] {SQLException.class}, new Class[]{SQLWarning.class})
                .accept(new SQLWarning(""), new Class[0]));
    }

    @Test
    public void dontRollbackCommits() throws SystemException {
        assertNull(OpenEJB.getTransactionManager().getTransaction());
        try {
            bean.dontRollback();
        } catch (final AnException e) {
            // expected
        }
        assertNull(OpenEJB.getTransactionManager().getTransaction());
    }

    @Test
    public void neverInTx() throws SystemException {
        assertNull(OpenEJB.getTransactionManager().getTransaction());
        try {
            bean.createTx(new Runnable() {
                @Override
                public void run() {
                    bean.never();
                }
            });
            fail();
        } catch (final TransactionalException e) {
            // expected
        }
        assertNull(OpenEJB.getTransactionManager().getTransaction());
    }

    @Test(expected = TransactionalException.class)
    public void mandatoryKO() {
        for (int i = 0; i < 2; i++) {
            bean.withoutATxIllThrowAnException();
        }
    }

    @Test
    public void mandatoryOk() throws Exception {
        for (int i = 0; i < 2; i++) {
            OpenEJB.getTransactionManager().begin();
            bean.withoutATxIllThrowAnException();
            OpenEJB.getTransactionManager().rollback();
        }
    }

    @Test
    public void requiredStartsTx() throws Exception {
        for (int i = 0; i < 2; i++) {
            bean.required(); // asserts in the method
        }
    }

    @Test
    public void utAllowedWhenThereIsNoTx() throws Exception {
        for (int i = 0; i < 2; i++) {
            bean.notSupportedUtOk();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void utForbiddenWhenThereIsATx() throws Exception {
        bean.requiredUtForbidden();
    }

    @Test
    public void rollbackException() throws Exception {
        for (int i = 0; i < 2; i++) {
            final AtomicInteger status = new AtomicInteger();
            final TransactionManager transactionManager = OpenEJB.getTransactionManager();
            transactionManager.begin();
            transactionManager.getTransaction().registerSynchronization(new Synchronization() {
                @Override
                public void beforeCompletion() {
                    // no-op
                }

                @Override
                public void afterCompletion(int state) {
                    status.set(state);
                }
            });
            try {
                bean.anException();
                fail();
            } catch (final AnException e) {
                // no-op
            }
            OpenEJB.getTransactionManager().rollback();
            assertEquals(Status.STATUS_ROLLEDBACK, status.get());
        }
    }

    @Test
    public void runtimeException() throws Exception {
        for (int i = 0; i < 2; i++) {
            final AtomicInteger status = new AtomicInteger(-1);
            try {
                bean.runtimeEx(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OpenEJB.getTransactionManager().getTransaction().registerSynchronization(new Synchronization() {
                                @Override
                                public void beforeCompletion() {
                                    // no-op
                                }

                                @Override
                                public void afterCompletion(int state) {
                                    status.set(state);
                                }
                            });
                        } catch (final RollbackException | SystemException e) {
                            fail();
                        }
                    }
                });
                fail();
            } catch (final AnException e) {
                // no-op
            }
            assertEquals(Status.STATUS_ROLLEDBACK, status.get());
        }
    }

    @Test
    public void checked() throws Exception {
        for (int i = 0; i < 2; i++) {
            final AtomicInteger status = new AtomicInteger();
            try {
                bean.checked(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OpenEJB.getTransactionManager().getTransaction().registerSynchronization(new Synchronization() {
                                @Override
                                public void beforeCompletion() {
                                    // no-op
                                }

                                @Override
                                public void afterCompletion(int state) {
                                    status.set(state);
                                }
                            });
                        } catch (final RollbackException | SystemException e) {
                            fail();
                        }
                    }
                });
                fail();
            } catch (final AnCheckedException e) {
                // no-op
            }
            assertEquals(Status.STATUS_COMMITTED, status.get());
        }
    }

    @Test
    public void runtimeChecked() throws Exception {
        for (int i = 0; i < 2; i++) {
            final AtomicInteger status = new AtomicInteger();
            try {
                bean.runtimeChecked(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OpenEJB.getTransactionManager().getTransaction().registerSynchronization(new Synchronization() {
                                @Override
                                public void beforeCompletion() {
                                    // no-op
                                }

                                @Override
                                public void afterCompletion(int state) {
                                    status.set(state);
                                }
                            });
                        } catch (final RollbackException | SystemException e) {
                            fail();
                        }
                    }
                });
                fail();
            } catch (final AnException e) {
                // no-op
            }
            assertEquals(Status.STATUS_COMMITTED, status.get());
        }
    }

    @Test
    public void classLevel() throws Exception {
        for (int i = 0; i < 2; i++) {
            final AtomicInteger status = new AtomicInteger();
            try {
                bean.classLevel(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OpenEJB.getTransactionManager().getTransaction().registerSynchronization(new Synchronization() {
                                @Override
                                public void beforeCompletion() {
                                    // no-op
                                }

                                @Override
                                public void afterCompletion(int state) {
                                    status.set(state);
                                }
                            });
                        } catch (final RollbackException | SystemException e) {
                            fail();
                        }
                    }
                });
                fail();
            } catch (final AnCheckedException e) {
                // no-op
            }
            assertEquals(Status.STATUS_COMMITTED, status.get());
        }
    }

    @Test
    public void dontRollbackException() throws Exception {
        for (int i = 0; i < 2; i++) {
            final AtomicInteger status = new AtomicInteger();
            try {
                bean.anotherException(status);
                fail();
            } catch (final AnotherException e) {
                // no-op
            }
            assertEquals(Status.STATUS_COMMITTED, status.get());
        }
    }

    @Test
    public void requiresNew() {
        final AtomicReference<Transaction> tx2 = new AtomicReference<>();
        final Transaction tx1 = bean.defaultTx(new Runnable() {
            @Override
            public void run() {
                tx2.set(bean.newTx(new Runnable() {
                    @Override
                    public void run() {
                      // no-op
                    }
                }));
            }
        });
        assertNotSame(tx1, tx2.get());
    }

    @Test
    public void rb() {
        try {
            bean.exceptionOnCompletion();
            fail();
        } catch (final IllegalArgumentException te) {
            // ok
        }
    }

    @Test
    public void tomee2051() {
        for (int i = 0; i < 2; i++) {
            final AtomicInteger status = new AtomicInteger();
            try {
                bean.tomee2051(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OpenEJB.getTransactionManager().getTransaction().registerSynchronization(new Synchronization() {
                                @Override
                                public void beforeCompletion() {
                                    // no-op
                                }

                                @Override
                                public void afterCompletion(int state) {
                                    status.set(state);
                                }
                            });
                        } catch (final RollbackException | SystemException e) {
                            fail();
                        }
                    }
                });
                fail();
            } catch (final AnException e) {
                // no-op
            }
            assertEquals(Status.STATUS_ROLLEDBACK, status.get());
        }
    }

    @Transactional(value = REQUIRED, rollbackOn = AnCheckedException.class)
    public static class TxBean {
        @Resource
        private UserTransaction ut;

        @Resource
        private TransactionManager txMgr;

        @Transactional(value = REQUIRED)
        public void required() {
            assertHasTx();
        }

        @Transactional
        public void exceptionOnCompletion() {
            TransactionSynchronizationRegistry.class.cast(txMgr)
                    .registerInterposedSynchronization(new Synchronization() {
                        @Override
                        public void beforeCompletion() {
                            throw new IllegalArgumentException();
                        }

                        @Override
                        public void afterCompletion(final int status) {
                            // no-op
                        }
                    });
        }

        @Transactional(value = REQUIRED)
        public void requiredUtForbidden() {
            assertHasTx();
            try {
                ut.begin();
            } catch (final NotSupportedException | SystemException e) {
                fail();
            }
        }

        @Transactional(value = MANDATORY)
        public void withoutATxIllThrowAnException() {
            assertHasTx();
        }

        @Transactional(value = NOT_SUPPORTED)
        public void notSupportedUtOk() {
            try {
                assertEquals(Status.STATUS_NO_TRANSACTION, OpenEJB.getTransactionManager().getStatus());
                ut.begin();
                ut.commit();
            } catch (final Exception e) {
                fail(e.getMessage());
            }
        }

        private void assertHasTx() {
            try {
                assertEquals(Status.STATUS_ACTIVE, OpenEJB.getTransactionManager().getStatus());
            } catch (final SystemException e) {
                fail("no active tx");
            }
        }

        @Transactional(dontRollbackOn = AnException.class)
        public void dontRollback() {
            throw new AnException();
        }

        @Transactional(rollbackOn = AnException.class)
        public void tomee2051(final Runnable r) throws AnException {
            r.run();
            throw new AnException();
        }

        @Transactional(value = MANDATORY, rollbackOn = AnException.class)
        public void anException() {
            throw new AnException();
        }

        @Transactional(REQUIRED)
        public void runtimeEx(Runnable runnable) {
            runnable.run();
            throw new AnException();
        }

        @Transactional(REQUIRED)
        public void classLevel(Runnable runnable) throws AnCheckedException {
            runnable.run();
            throw new AnCheckedException();
        }

        @Transactional
        public Transaction defaultTx(final Runnable runnable) {
            runnable.run();
            try {
                return txMgr.getTransaction();
            } catch (SystemException e) {
                throw new IllegalStateException(e);
            }
        }

        @Transactional(REQUIRED)
        public void checked(Runnable runnable) throws AnCheckedException {
            runnable.run();
            throw new AnCheckedException();
        }

        @Transactional(REQUIRED)
        public void createTx(Runnable runnable) {
            runnable.run();
        }

        @Transactional(NEVER)
        public void never() {
            // no-op
        }

        @Transactional(REQUIRED)
        public void runtimeChecked(Runnable runnable) throws AnException {
            runnable.run();
            throw new AnException();
        }

        @Transactional(REQUIRES_NEW)
        public Transaction newTx(final Runnable runnable) {
            runnable.run();
            try {
                return txMgr.getTransaction();
            } catch (SystemException e) {
                throw new IllegalStateException(e);
            }
        }

        @Transactional(value = REQUIRED, dontRollbackOn = AnotherException.class)
        public void anotherException(final AtomicInteger status) {
            try {
                OpenEJB.getTransactionManager().getTransaction().registerSynchronization(new Synchronization() {
                    @Override
                    public void beforeCompletion() {
                        // no-op
                    }

                    @Override
                    public void afterCompletion(final int state) {
                        status.set(state);
                    }
                });
            } catch (final RollbackException | SystemException e) {
                fail();
            }
            throw new AnotherException();
        }
    }

    public static class AnCheckedException extends Exception {
    }
    public static class AnException extends RuntimeException {
    }
    public static class AnotherException extends RuntimeException {

    }
}
