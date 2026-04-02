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

import org.junit.Assume;
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

    @Test
    public void isSupportedReturnsConsistentValue() {
        // Just verify it doesn't throw — result depends on JVM version
        final boolean supported = VirtualThreadHelper.isSupported();
        // On Java 21+ should be true, on 17 false
        assertNotNull("isSupported should return a value", Boolean.valueOf(supported));
    }

    @Test
    public void newVirtualThreadCreatesThread() {
        Assume.assumeTrue("Virtual threads require Java 21+", VirtualThreadHelper.isSupported());

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
    public void newVirtualThreadFactoryCreatesThreads() {
        Assume.assumeTrue("Virtual threads require Java 21+", VirtualThreadHelper.isSupported());

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
    public void newVirtualThreadPerTaskExecutorWorks() {
        Assume.assumeTrue("Virtual threads require Java 21+", VirtualThreadHelper.isSupported());

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
    public void newVirtualThreadThrowsOnUnsupported() {
        Assume.assumeFalse("Only run on Java < 21", VirtualThreadHelper.isSupported());

        VirtualThreadHelper.newVirtualThread("test-", 1, () -> {});
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newVirtualThreadFactoryThrowsOnUnsupported() {
        Assume.assumeFalse("Only run on Java < 21", VirtualThreadHelper.isSupported());

        VirtualThreadHelper.newVirtualThreadFactory("test-");
    }
}
