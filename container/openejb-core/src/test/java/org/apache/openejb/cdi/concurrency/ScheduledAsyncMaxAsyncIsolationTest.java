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
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.InitialContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Verifies Concurrency 3.1 §3.1: "Scheduled asynchronous methods are treated similar to
 * other scheduled tasks in that they are not subject to max-async constraints."
 *
 * <p>Mirrors the TCK test
 * {@code ManagedScheduledExecutorDefinitionWebTests.testScheduledAsynchIgnoresMaxAsync}:
 * a custom {@link ManagedScheduledExecutorService} is saturated with regular async
 * submissions that fill all {@code maxAsync} slots; a scheduled {@code @Asynchronous}
 * firing on the same executor must still execute and complete, rather than queueing
 * behind the saturating workload.
 */
@RunWith(ApplicationComposer.class)
public class ScheduledAsyncMaxAsyncIsolationTest {

    private static final String MSES_JNDI = "java:module/maxasync/limitedMSES";
    private static final int MAX_ASYNC = 1;

    @Inject
    private SaturatedBean bean;

    @Module
    public EnterpriseBean ejb() {
        return new SingletonBean(DummyEjb.class).localBean();
    }

    @Module
    public Class<?>[] beans() {
        return new Class<?>[]{SaturatedBean.class};
    }

    @Test
    public void scheduledFiringBypassesMaxAsync() throws Exception {
        final ManagedScheduledExecutorService mses = InitialContext.doLookup(MSES_JNDI);

        // Saturate every maxAsync slot with tasks that block until the test releases them.
        // With maxAsync=1 the underlying ScheduledThreadPoolExecutor has corePoolSize=1, so a
        // single blocking submission occupies the only worker thread.
        final CountDownLatch saturationStarted = new CountDownLatch(MAX_ASYNC);
        final CountDownLatch release = new CountDownLatch(1);
        for (int i = 0; i < MAX_ASYNC; i++) {
            mses.submit(() -> {
                saturationStarted.countDown();
                try {
                    release.await(20, TimeUnit.SECONDS);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        assertTrue("Saturating tasks should occupy all maxAsync slots before the scheduled firing is triggered",
                saturationStarted.await(5, TimeUnit.SECONDS));

        try {
            // A scheduled firing on the same executor must not be blocked by the saturating
            // workload. With a one-second cron and a 15-second deadline there is ample headroom
            // unless the firing is queued behind the maxAsync core threads.
            final AtomicInteger counter = new AtomicInteger();
            final CompletableFuture<Integer> future = bean.scheduledFire(counter);
            final Integer result = future.get(15, TimeUnit.SECONDS);

            assertNotNull("Scheduled firing must complete despite saturated maxAsync slots", result);
            assertEquals("Scheduled firing should have executed exactly once", 1, counter.get());
        } finally {
            release.countDown();
        }
    }

    @ManagedScheduledExecutorDefinition(name = MSES_JNDI, maxAsync = MAX_ASYNC)
    @ApplicationScoped
    public static class SaturatedBean {

        @Asynchronous(executor = MSES_JNDI, runAt = @Schedule(cron = "* * * * * *"))
        public CompletableFuture<Integer> scheduledFire(final AtomicInteger counter) {
            final int count = counter.incrementAndGet();
            final CompletableFuture<Integer> future = Asynchronous.Result.getFuture();
            future.complete(count);
            return future;
        }
    }

    @jakarta.ejb.Singleton
    public static class DummyEjb {
    }
}
