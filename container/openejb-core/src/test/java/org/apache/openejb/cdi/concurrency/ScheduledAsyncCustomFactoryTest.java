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
import jakarta.enterprise.concurrent.ManageableThread;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.apache.openejb.threads.impl.ManagedThreadFactoryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.InitialContext;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Verifies that scheduled {@code @Asynchronous} firings honor the requested
 * {@link ManagedScheduledExecutorService}'s thread factory: the firing thread
 * is produced by the same {@link ManagedThreadFactoryImpl} as the primary pool
 * (same naming prefix, {@link ManageableThread} shape), not a stranger pool such
 * as the default MSES. Locks down clause A of Concurrency 3.1 §3.1 (firings run
 * with the requested executor's thread factory / virtual / priority).
 */
@RunWith(ApplicationComposer.class)
public class ScheduledAsyncCustomFactoryTest {

    private static final String MSES_JNDI = "java:module/custom/factoryMSES";

    @Inject
    private FactoryBean bean;

    @Module
    public EnterpriseBean ejb() {
        return new SingletonBean(DummyEjb.class).localBean();
    }

    @Module
    public Class<?>[] beans() {
        return new Class<?>[]{FactoryBean.class};
    }

    @Test
    public void scheduledFiringUsesCustomExecutorsThreadFactory() throws Exception {
        final ManagedScheduledExecutorService custom = InitialContext.doLookup(MSES_JNDI);

        // Collect thread ids from the custom MSES's primary pool by submitting short-lived
        // probes that each block until a release latch fires. Capturing several probes
        // forces the primary pool to spawn each of its worker threads concurrently.
        final int primarySamples = 3;
        final Set<Long> primaryThreadIds = ConcurrentHashMap.newKeySet();
        final CountDownLatch primaryStarted = new CountDownLatch(primarySamples);
        final CountDownLatch release = new CountDownLatch(1);
        final Set<Future<?>> primaryProbes = new HashSet<>();
        for (int i = 0; i < primarySamples; i++) {
            primaryProbes.add(custom.submit(() -> {
                primaryThreadIds.add(Thread.currentThread().getId());
                primaryStarted.countDown();
                try {
                    release.await(5, TimeUnit.SECONDS);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }
        assertTrue("Primary-pool probes should start", primaryStarted.await(5, TimeUnit.SECONDS));
        release.countDown();
        for (final Future<?> f : primaryProbes) {
            f.get(5, TimeUnit.SECONDS);
        }
        assertFalse("Primary pool should have reported at least one worker thread id", primaryThreadIds.isEmpty());

        final AtomicInteger counter = new AtomicInteger();
        final CompletableFuture<ThreadSnapshot> future = bean.capture(counter);
        final ThreadSnapshot firing = future.get(15, TimeUnit.SECONDS);

        assertNotNull("Scheduled firing must complete", firing);
        assertFalse("Firing must run on the secondary pool, not the primary — the custom executor's "
                        + "primary core-size is capped by maxAsync (firing thread id=" + firing.threadId
                        + ", primary ids=" + primaryThreadIds + ")",
                primaryThreadIds.contains(firing.threadId));
        assertTrue("Firing thread must come from the custom MSES's ManagedThreadFactory "
                        + "(expected name prefix '" + ManagedThreadFactoryImpl.DEFAULT_PREFIX
                        + "', got '" + firing.threadName + "')",
                firing.threadName.startsWith(ManagedThreadFactoryImpl.DEFAULT_PREFIX));
        assertTrue("Firing thread must be a ManageableThread produced by ManagedThreadFactoryImpl "
                        + "(got class " + firing.threadClass + ")",
                firing.manageable);
    }

    @ManagedScheduledExecutorDefinition(name = MSES_JNDI, maxAsync = 3)
    @ApplicationScoped
    public static class FactoryBean {

        @Asynchronous(executor = MSES_JNDI, runAt = @Schedule(cron = "* * * * * *"))
        public CompletableFuture<ThreadSnapshot> capture(final AtomicInteger counter) {
            counter.incrementAndGet();
            final Thread t = Thread.currentThread();
            final ThreadSnapshot snap = new ThreadSnapshot(
                    t.getId(),
                    t.getName(),
                    t.getClass().getName(),
                    t instanceof ManageableThread);
            final CompletableFuture<ThreadSnapshot> future = Asynchronous.Result.getFuture();
            future.complete(snap);
            return future;
        }
    }

    public record ThreadSnapshot(long threadId, String threadName, String threadClass, boolean manageable) {
    }

    @jakarta.ejb.Singleton
    public static class DummyEjb {
    }
}
