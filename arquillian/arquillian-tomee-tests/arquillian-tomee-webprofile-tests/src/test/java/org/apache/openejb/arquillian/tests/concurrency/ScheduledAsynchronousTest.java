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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

/**
 * Arquillian integration test for {@code @Asynchronous(runAt = @Schedule(...))}
 * — the scheduled recurring async method feature introduced in Jakarta Concurrency 3.1.
 */
@RunWith(Arquillian.class)
public class ScheduledAsynchronousTest {

    @Inject
    private ScheduledBean scheduledBean;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "ScheduledAsynchronousTest.war")
                .addClasses(ScheduledBean.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test
    public void scheduledVoidMethodExecutesRepeatedly() throws Exception {
        scheduledBean.everySecondVoid();

        final boolean reached = ScheduledBean.VOID_LATCH.await(10, TimeUnit.SECONDS);
        assertTrue("Scheduled void method should have been invoked at least 3 times, count: "
                + ScheduledBean.VOID_COUNTER.get(), reached);
    }

    @Test
    public void scheduledReturningMethodExecutes() throws Exception {
        final CompletableFuture<String> future = scheduledBean.everySecondReturning();

        final boolean reached = ScheduledBean.RETURNING_LATCH.await(10, TimeUnit.SECONDS);
        assertTrue("Scheduled returning method should have been invoked, count: "
                + ScheduledBean.RETURNING_COUNTER.get(), reached);
    }

    @ApplicationScoped
    public static class ScheduledBean {
        static final AtomicInteger VOID_COUNTER = new AtomicInteger();
        static final CountDownLatch VOID_LATCH = new CountDownLatch(3);

        static final AtomicInteger RETURNING_COUNTER = new AtomicInteger();
        static final CountDownLatch RETURNING_LATCH = new CountDownLatch(1);

        @Asynchronous(runAt = @Schedule(cron = "* * * * * *"))
        public void everySecondVoid() {
            VOID_COUNTER.incrementAndGet();
            VOID_LATCH.countDown();
        }

        @Asynchronous(runAt = @Schedule(cron = "* * * * * *"))
        public CompletableFuture<String> everySecondReturning() {
            RETURNING_COUNTER.incrementAndGet();
            RETURNING_LATCH.countDown();
            return Asynchronous.Result.complete("done");
        }
    }
}
