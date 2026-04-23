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
package org.apache.openejb.arquillian.tests.concurrency;

import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Arquillian test that mirrors the TCK pattern of using
 * {@code @ManagedScheduledExecutorDefinition} with a custom JNDI name
 * and {@code @Asynchronous(executor="java:module/...", runAt=@Schedule(...))}.
 *
 * <p>This verifies that {@code java:module/} and {@code java:app/} scoped
 * executor lookups work for scheduled async methods.</p>
 */
@RunWith(Arquillian.class)
public class ScheduledAsyncCustomExecutorTest {

    @Inject
    private ScheduledBeanWithCustomExecutor bean;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "ScheduledAsyncCustomExecutorTest.war")
                .addClasses(ScheduledBeanWithCustomExecutor.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test
    public void scheduledWithModuleScopedExecutor() throws Exception {
        final AtomicInteger counter = new AtomicInteger();
        final CompletableFuture<Integer> future = bean.scheduledWithModuleExecutor(2, counter);

        assertNotNull("Future should be returned", future);
        final Integer result = future.get(15, TimeUnit.SECONDS);
        assertEquals("Should complete after 2 runs", Integer.valueOf(2), result);
    }

    @Test
    public void scheduledWithAppScopedExecutor() throws Exception {
        final AtomicInteger counter = new AtomicInteger();
        final CompletableFuture<Integer> future = bean.scheduledWithAppExecutor(1, counter);

        assertNotNull("Future should be returned", future);
        final Integer result = future.get(15, TimeUnit.SECONDS);
        assertEquals("Should complete after 1 run", Integer.valueOf(1), result);
    }

    @ManagedScheduledExecutorDefinition(name = "java:module/concurrent/TestScheduledExecutor")
    @ManagedScheduledExecutorDefinition(name = "java:app/concurrent/TestAppScheduledExecutor")
    @ApplicationScoped
    public static class ScheduledBeanWithCustomExecutor {

        @Asynchronous(executor = "java:module/concurrent/TestScheduledExecutor",
                       runAt = @Schedule(cron = "* * * * * *"))
        public CompletableFuture<Integer> scheduledWithModuleExecutor(final int runs, final AtomicInteger counter) {
            final int count = counter.incrementAndGet();
            if (count < runs) {
                return null;
            }
            final CompletableFuture<Integer> future = Asynchronous.Result.getFuture();
            future.complete(count);
            return future;
        }

        @Asynchronous(executor = "java:app/concurrent/TestAppScheduledExecutor",
                       runAt = @Schedule(cron = "* * * * * *"))
        public CompletableFuture<Integer> scheduledWithAppExecutor(final int runs, final AtomicInteger counter) {
            final int count = counter.incrementAndGet();
            if (count < runs) {
                return null;
            }
            final CompletableFuture<Integer> future = Asynchronous.Result.getFuture();
            future.complete(count);
            return future;
        }
    }
}
