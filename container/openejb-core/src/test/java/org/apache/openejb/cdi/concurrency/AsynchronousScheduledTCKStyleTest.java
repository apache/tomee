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
package org.apache.openejb.cdi.concurrency;

import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests that mirror the Concurrency TCK's ReqBean pattern:
 * - Class-level @Asynchronous (one-shot)
 * - Method-level @Asynchronous(runAt=@Schedule(...)) (scheduled)
 * - Uses Asynchronous.Result.getFuture() inside the bean method
 * - Various return behaviors: NULL, COMPLETE_RESULT, COMPLETE_EXCEPTIONALLY, INCOMPLETE
 */
@RunWith(ApplicationComposer.class)
public class AsynchronousScheduledTCKStyleTest {

    @Inject
    private ScheduledReqBean reqBean;

    @Module
    public EnterpriseBean ejb() {
        return new SingletonBean(DummyEjb.class).localBean();
    }

    @Module
    public Class<?>[] beans() {
        return new Class<?>[]{ScheduledReqBean.class};
    }

    /**
     * TCK: testScheduledAsynchCompletedResult
     * Method returns null until runs==count, then completes the future via Asynchronous.Result.
     */
    @Test
    public void scheduledCompletedResult() throws Exception {
        final AtomicInteger counter = new AtomicInteger();
        final CompletableFuture<Integer> future = reqBean.scheduledEverySecond(2, ReturnType.COMPLETE_RESULT, counter);

        assertNotNull("Future should be returned by interceptor", future);

        // Wait for completion — method completes the future on the 2nd invocation
        final Integer result = future.get(15, TimeUnit.SECONDS);
        assertEquals("Should have run exactly 2 times", Integer.valueOf(2), result);
    }

    /**
     * TCK: testScheduledAsynchCompletedFuture
     * Method returns INCOMPLETE future (doesn't complete it). Schedule runs once, future stays incomplete.
     */
    @Test
    public void scheduledIncompleteFuture() throws Exception {
        final AtomicInteger counter = new AtomicInteger();
        final CompletableFuture<Integer> future = reqBean.scheduledEverySecond(1, ReturnType.INCOMPLETE, counter);

        assertNotNull("Future should be returned by interceptor", future);

        // The future should NOT complete — it's INCOMPLETE
        try {
            future.get(3, TimeUnit.SECONDS);
            fail("Should have timed out — future is incomplete");
        } catch (final TimeoutException e) {
            // expected
        }

        assertFalse("Future should not be done", future.isDone());
        assertFalse("Future should not be cancelled", future.isCancelled());

        // Should have executed exactly once
        assertEquals("Schedule should have executed exactly once", 1, counter.get());

        future.cancel(true);
    }

    /**
     * TCK: testScheduledAsynchCompletedExceptionally
     * Method completes the future exceptionally.
     */
    @Test
    public void scheduledCompletedExceptionally() throws Exception {
        final AtomicInteger counter = new AtomicInteger();
        final CompletableFuture<Integer> future = reqBean.scheduledEverySecond(1, ReturnType.COMPLETE_EXCEPTIONALLY, counter);

        assertNotNull("Future should be returned by interceptor", future);

        try {
            future.get(15, TimeUnit.SECONDS);
            fail("Should have completed exceptionally");
        } catch (final Exception e) {
            assertTrue("Future should be done", future.isDone());
            assertTrue("Future should be completed exceptionally", future.isCompletedExceptionally());
        }
    }

    /**
     * TCK: testScheduledAsynchVoidReturn
     * Void method with @Asynchronous(runAt=...) — should execute repeatedly.
     */
    @Test
    public void scheduledVoidReturn() throws Exception {
        final AtomicInteger counter = new AtomicInteger();
        reqBean.scheduledVoidEverySecond(3, counter);

        // Wait for 3 executions
        final long start = System.currentTimeMillis();
        while (counter.get() < 3 && System.currentTimeMillis() - start < 15000) {
            Thread.sleep(200);
        }
        assertTrue("Void method should have executed at least 3 times, got: " + counter.get(),
                counter.get() >= 3);
    }

    /**
     * TCK: testScheduledAsynchWithInvalidJNDIName
     * Method with invalid executor JNDI name should throw RejectedExecutionException.
     */
    @Test
    public void scheduledWithInvalidJNDIName() throws Exception {
        try {
            reqBean.scheduledInvalidExecutor();
            fail("Should have thrown an exception for invalid executor");
        } catch (final jakarta.ejb.EJBException | java.util.concurrent.RejectedExecutionException e) {
            // expected — invalid JNDI name
        }
    }

    // --- Bean ---

    public enum ReturnType {
        NULL, COMPLETE_RESULT, COMPLETE_EXCEPTIONALLY, INCOMPLETE, THROW_EXCEPTION
    }

    @Asynchronous
    @RequestScoped
    public static class ScheduledReqBean {

        @Asynchronous(runAt = @Schedule(cron = "* * * * * *"))
        public CompletableFuture<Integer> scheduledEverySecond(final int runs, final ReturnType type,
                                                                final AtomicInteger counter) {
            final int count = counter.incrementAndGet();

            // Return null until we've reached the target number of runs
            if (runs != count) {
                return null;
            }

            final CompletableFuture<Integer> future = Asynchronous.Result.getFuture();

            switch (type) {
                case NULL:
                    return null;
                case COMPLETE_EXCEPTIONALLY:
                    future.completeExceptionally(new Exception("Expected exception"));
                    return future;
                case COMPLETE_RESULT:
                    future.complete(count);
                    return future;
                case INCOMPLETE:
                    return future; // don't complete it
                case THROW_EXCEPTION:
                    throw new RuntimeException("Expected exception");
                default:
                    return null;
            }
        }

        @Asynchronous(runAt = @Schedule(cron = "* * * * * *"))
        public void scheduledVoidEverySecond(final int runs, final AtomicInteger counter) {
            final int count = counter.incrementAndGet();
            if (count >= runs) {
                Asynchronous.Result.getFuture().complete(null);
            }
        }

        @Asynchronous(executor = "java:comp/env/invalid/executor",
                       runAt = @Schedule(cron = "* * * * * *"))
        public CompletableFuture<String> scheduledInvalidExecutor() {
            throw new UnsupportedOperationException("Should not reach here with invalid executor");
        }
    }

    @jakarta.ejb.Singleton
    public static class DummyEjb {
    }
}
