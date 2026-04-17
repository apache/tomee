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
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.resource.thread.ManagedScheduledExecutorServiceImplFactory;
import org.apache.openejb.testing.Module;
import org.apache.openejb.threads.impl.ManagedScheduledExecutorServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Verifies that a scheduled {@code @Asynchronous} firing runs on the executor named in
 * {@link Asynchronous#executor()} rather than silently falling back to
 * {@code java:comp/DefaultManagedScheduledExecutorService}.
 */
@RunWith(ApplicationComposer.class)
public class ScheduledAsyncExecutorRoutingTest {

    @Inject
    private CustomExecutorBean bean;

    @Module
    public EnterpriseBean ejb() {
        return new SingletonBean(DummyEjb.class).localBean();
    }

    @Module
    public Class<?>[] beans() {
        return new Class<?>[]{CustomExecutorBean.class};
    }

    @Test
    public void scheduledFiringRunsOnRequestedExecutorNotDefault() throws Exception {
        final ManagedScheduledExecutorServiceImpl defaultMses =
                ManagedScheduledExecutorServiceImplFactory.lookup("java:comp/DefaultManagedScheduledExecutorService");

        // Saturate the default MSES core pool (size 5) so every worker thread is live
        // simultaneously and we capture its id. Each probe records its thread id up front,
        // then blocks on the release latch so the pool can't recycle a single worker across
        // multiple probes before we've seen them all.
        final int coreSize = 5;
        final Set<Long> defaultThreadIds = ConcurrentHashMap.newKeySet();
        final CountDownLatch started = new CountDownLatch(coreSize);
        final CountDownLatch release = new CountDownLatch(1);
        final Set<Future<?>> probes = new HashSet<>();
        for (int i = 0; i < coreSize; i++) {
            probes.add(defaultMses.submit(() -> {
                defaultThreadIds.add(Thread.currentThread().getId());
                started.countDown();
                try {
                    release.await(5, TimeUnit.SECONDS);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }
        assertTrue("All default-MSES probe tasks should start", started.await(5, TimeUnit.SECONDS));
        release.countDown();
        for (final Future<?> f : probes) {
            f.get(5, TimeUnit.SECONDS);
        }

        assertFalse("Default MSES should have a non-empty worker pool", defaultThreadIds.isEmpty());

        // Now run the scheduled method that targets the CUSTOM executor.
        final AtomicInteger counter = new AtomicInteger();
        final CompletableFuture<Long> future = bean.captureFiringThreadId(counter);
        final Long firingThreadId = future.get(15, TimeUnit.SECONDS);

        assertEquals("Method body should have fired once", 1, counter.get());
        assertFalse("Scheduled firing must run on the requested custom executor, "
                        + "not on the default MSES thread pool (firing thread id=" + firingThreadId
                        + ", default pool thread ids=" + defaultThreadIds + ")",
                defaultThreadIds.contains(firingThreadId));
    }

    @ManagedScheduledExecutorDefinition(name = "java:module/routing/customMSES")
    @ApplicationScoped
    public static class CustomExecutorBean {

        @Asynchronous(executor = "java:module/routing/customMSES",
                runAt = @Schedule(cron = "* * * * * *"))
        public CompletableFuture<Long> captureFiringThreadId(final AtomicInteger counter) {
            counter.incrementAndGet();
            final CompletableFuture<Long> future = Asynchronous.Result.getFuture();
            future.complete(Thread.currentThread().getId());
            return future;
        }
    }

    @jakarta.ejb.Singleton
    public static class DummyEjb {
    }
}
