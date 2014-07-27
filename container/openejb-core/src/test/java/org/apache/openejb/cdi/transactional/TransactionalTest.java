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

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import javax.transaction.TransactionalException;
import javax.transaction.UserTransaction;
import java.util.concurrent.atomic.AtomicInteger;

import static javax.transaction.Transactional.TxType.MANDATORY;
import static javax.transaction.Transactional.TxType.NOT_SUPPORTED;
import static javax.transaction.Transactional.TxType.REQUIRED;
import static org.junit.Assert.assertEquals;
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

    @Test(expected = TransactionalException.class)
    public void mandatoryKO() {
        bean.withoutATxIllThrowAnException();
    }

    @Test
    public void mandatoryOk() throws Exception {
        OpenEJB.getTransactionManager().begin();
        bean.withoutATxIllThrowAnException();
        OpenEJB.getTransactionManager().rollback();
    }

    @Test
    public void requiredStartsTx() throws Exception {
        bean.required(); // asserts in the method
    }

    @Test
    public void utAllowedWhenThereIsNoTx() throws Exception {
        bean.notSupportedUtOk();
    }

    @Test(expected = IllegalStateException.class)
    public void utForbiddenWhenThereIsATx() throws Exception {
        bean.requiredUtForbidden();
    }

    @Test
    public void rollbackException() throws Exception {
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
        } catch (final TransactionalException e) {
            // no-op
        }
        OpenEJB.getTransactionManager().rollback();
        assertEquals(Status.STATUS_ROLLEDBACK, status.get());
    }

    @Test
    public void dontRollbackException() throws Exception {
        final AtomicInteger status = new AtomicInteger();
        final TransactionManager transactionManager = OpenEJB.getTransactionManager();
        try {
            bean.anotherException(status);
            fail();
        } catch (final TransactionalException e) {
            // no-op
        }
        assertEquals(Status.STATUS_COMMITTED, status.get());
    }

    public static class TxBean {
        @Resource
        private UserTransaction ut;

        @Transactional(value = REQUIRED)
        public void required() {
            assertHasTx();
        }

        @Transactional(value = REQUIRED)
        public void requiredUtForbidden() {
            assertHasTx();
            try {
                ut.begin();
            } catch (final NotSupportedException e) {
                fail();
            } catch (final SystemException e) {
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

        @Transactional(value = MANDATORY, rollbackOn = AnException.class)
        public void anException() {
            throw new AnException();
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
            } catch (final RollbackException e) {
                fail();
            } catch (final SystemException e) {
                fail();
            }
            throw new AnotherException();
        }
    }

    public static class AnException extends RuntimeException {

    }
    public static class AnotherException extends RuntimeException {

    }
}
