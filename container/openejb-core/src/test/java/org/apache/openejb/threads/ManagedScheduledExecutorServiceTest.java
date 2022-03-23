/**
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

import org.apache.openejb.resource.thread.ManagedScheduledExecutorServiceImplFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.ri.sp.PseudoSecurityService;
import org.apache.openejb.spi.SecurityService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.enterprise.concurrent.LastExecution;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.Trigger;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ManagedScheduledExecutorServiceTest {
    @BeforeClass
    public static void forceSecurityService() {
        SystemInstance.get().setComponent(SecurityService.class, new PseudoSecurityService());
    }

    @AfterClass
    public static void reset() {
        SystemInstance.reset();
    }

    @Test
    public void triggerCallableSchedule() throws Exception {
        final ManagedScheduledExecutorService es = new ManagedScheduledExecutorServiceImplFactory().create();
        final CountDownLatch counter = new CountDownLatch(5);
        final FutureAwareCallable callable = new FutureAwareCallable(counter);

        final Future<Long> future = es.schedule((Callable<Long>) callable,
            new Trigger() {
                @Override
                public Date getNextRunTime(final LastExecution lastExecutionInfo, final Date taskScheduledTime) {
                    if (lastExecutionInfo == null) {
                        return new Date();
                    }
                    return new Date(System.currentTimeMillis() + 100
                                    /*lastExecutionInfo.getRunEnd().getTime() + 100  // can already be passed */);
                }

                @Override
                public boolean skipRun(final LastExecution lastExecutionInfo, final Date scheduledRunTime) {
                    return false;
                }
            }
        );


        assertFalse(future.isDone());
        assertFalse(future.isCancelled());

        counter.await(1, TimeUnit.SECONDS);

        assertEquals("Future was not called", 0L, future.get().longValue());

        future.cancel(true);
        assertEquals("Counter did not count down in time", 0L, counter.getCount());

        assertTrue(future.isDone());
        assertTrue(future.isCancelled());
    }

    @Test
    public void triggerRunnableSchedule() throws Exception {
        final ManagedScheduledExecutorService es = new ManagedScheduledExecutorServiceImplFactory().create();
        final CountDownLatch counter = new CountDownLatch(5);
        final FutureAwareCallable callable = new FutureAwareCallable(counter);

        final ScheduledFuture<?> future = es.schedule(Runnable.class.cast(callable),
            new Trigger() {
                @Override
                public Date getNextRunTime(final LastExecution lastExecutionInfo, final Date taskScheduledTime) {
                    if (lastExecutionInfo == null) {
                        return new Date();
                    }
                    return new Date(lastExecutionInfo.getRunEnd().getTime() + 200);
                }

                @Override
                public boolean skipRun(final LastExecution lastExecutionInfo, final Date scheduledRunTime) {
                    return false;
                }
            }
        );

        assertFalse(future.isDone());
        assertFalse(future.isCancelled());

        //Should easily get 5 invocations within 1 second
        counter.await(3, TimeUnit.SECONDS);

        future.cancel(true);
        assertEquals("Counter did not count down in time", 0L, counter.getCount());

        final boolean done = future.isDone();
        assertTrue(done);
        final boolean cancelled = future.isCancelled();
        assertTrue(cancelled);
    }

    @Test
    public void simpleSchedule() throws Exception {
        final ManagedScheduledExecutorService es = new ManagedScheduledExecutorServiceImplFactory().create();
        final long start = System.currentTimeMillis();
        final ScheduledFuture<Long> future = es.schedule(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                Thread.sleep(4000);
                return System.currentTimeMillis();
            }
        }, 2, TimeUnit.SECONDS);
        assertEquals(6, TimeUnit.MILLISECONDS.toSeconds(future.get() - start), 1);
    }

    protected static class FutureAwareCallable implements Callable<Long>, Runnable {
        private final CountDownLatch counter;

        public FutureAwareCallable(final CountDownLatch counter) {
            this.counter = counter;
        }

        @Override
        public Long call() throws Exception {
            this.run();
            return counter.getCount();
        }

        @Override
        public void run() {
            counter.countDown();
        }
    }
}
