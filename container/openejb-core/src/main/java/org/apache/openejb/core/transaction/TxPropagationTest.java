/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.transaction;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;
import org.apache.geronimo.transaction.manager.TransactionImpl;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@RunWith(ApplicationComposer.class)
public class TxPropagationTest {
    private static final Logger logger = Logger.getLogger(TxPropagationTest.class.getName());
    private static final Set<String> TX_IDS = Collections.synchronizedSet(new HashSet<>());
    private static final CountDownLatch LATCH = new CountDownLatch(10);
    private static final AtomicInteger ERRORS = new AtomicInteger(0);

    @EJB
    private WorkBean workBean;

    @Test
    public void test() throws Exception {
        workBean.runAsyncTasksAndWait();

        Assert.assertTrue(LATCH.await(2, TimeUnit.SECONDS));
        Assert.assertEquals(10, TX_IDS.size());
        Assert.assertEquals(0, ERRORS.get());
    }

    @Module
    @Classes(cdi = true, value = {WorkBean.class, TaskWorker.class, WorkScheduler.class})
    public EjbJar jar() {
        return new EjbJar("tx-prop-test");
    }

    @Stateless
    public static class WorkScheduler {
        @Resource
        private ManagedExecutorService executorService;

        @EJB
        private TaskWorker worker;

        public void doWork() {
            try {
                worker.runTask();

                Future<String> f = executorService.submit(() -> {
                    logger.info("Work scheduler is in a separate thread");
                    return "OK";
                });

                try {
                    f.get(1000, TimeUnit.MILLISECONDS);
                } finally {
                    if (!f.isDone()) {
                        f.cancel(true);
                        logger.severe("Cancelling tread.");
                    }
                }
            } catch (Throwable t) {
                ERRORS.incrementAndGet();
                throw new RuntimeException(t);
            }
        }

    }

    @Stateless
    public static class WorkBean {
        @Resource
        private ManagedExecutorService executorService;

        @Inject
        private WorkScheduler scheduler;

        public void runAsyncTasksAndWait() throws Exception {
            Thread.sleep(500);
            for (int i = 1; i <= 10; i++) {
                final List<Future<String>> tasks = executorService.invokeAll(Set.of(() -> {
                    try {
                        scheduler.doWork();
                    } catch (Exception e) {
                        ERRORS.incrementAndGet();
                    }
                    return "OK";
                }));
            }
        }
    }

    @Stateless
    @Lock(LockType.READ)
    public static class TaskWorker {
        public void runTask() {
            try {
                final TransactionImpl txImpl = (TransactionImpl) SystemInstance.get().getComponent(TransactionManager.class).getTransaction();
                String xid = txImpl.getTransactionKey().toString();
                TX_IDS.add(xid);
                Thread.sleep(500);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            } finally {
                LATCH.countDown();
            }
        }
    }


}
