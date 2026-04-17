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
package org.apache.openejb.threads.impl;

import org.apache.openejb.junit.jre.EnabledForJreRange;
import org.apache.openejb.junit.jre.JreConditionRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class VirtualThreadHelperTest {

    @Rule
    public final JreConditionRule jreCondition = new JreConditionRule();

    @Test
    @EnabledForJreRange(min = 21)
    public void newVirtualThreadCreatesThread() {
        final CountDownLatch latch = new CountDownLatch(1);
        final Thread thread = VirtualThreadHelper.newVirtualThread("test-vt-", 1, latch::countDown);

        assertNotNull(thread);
        assertFalse("Thread should not be started yet", thread.isAlive());

        thread.start();
        try {
            assertTrue("Virtual thread should complete", latch.await(5, TimeUnit.SECONDS));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted waiting for virtual thread");
        }
    }

    @Test
    @EnabledForJreRange(min = 21)
    public void newVirtualThreadFactoryCreatesThreads() {
        final ThreadFactory factory = VirtualThreadHelper.newVirtualThreadFactory("test-vtf-");
        assertNotNull(factory);

        final CountDownLatch latch = new CountDownLatch(1);
        final Thread thread = factory.newThread(latch::countDown);
        assertNotNull(thread);

        thread.start();
        try {
            assertTrue("Factory-created virtual thread should complete", latch.await(5, TimeUnit.SECONDS));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted waiting for virtual thread");
        }
    }

    @Test
    @EnabledForJreRange(min = 21)
    public void newVirtualThreadPerTaskExecutorWorks() {
        final ThreadFactory factory = VirtualThreadHelper.newVirtualThreadFactory("test-vtpe-");
        final ExecutorService executor = VirtualThreadHelper.newVirtualThreadPerTaskExecutor(factory);
        assertNotNull(executor);

        final CountDownLatch latch = new CountDownLatch(3);
        executor.execute(latch::countDown);
        executor.execute(latch::countDown);
        executor.execute(latch::countDown);

        try {
            assertTrue("All tasks should complete on virtual threads", latch.await(5, TimeUnit.SECONDS));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted waiting for virtual thread executor");
        } finally {
            executor.shutdown();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    @EnabledForJreRange(max = 20)
    public void newVirtualThreadThrowsOnUnsupported() {
        VirtualThreadHelper.newVirtualThread("test-", 1, () -> {});
    }

    @Test(expected = UnsupportedOperationException.class)
    @EnabledForJreRange(max = 20)
    public void newVirtualThreadFactoryThrowsOnUnsupported() {
        VirtualThreadHelper.newVirtualThreadFactory("test-");
    }
}
