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

import jakarta.enterprise.concurrent.ManageableThread;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.ri.sp.PseudoSecurityService;
import org.apache.openejb.spi.SecurityService;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ManagedThreadFactoryVirtualTest {

    @BeforeClass
    public static void setup() {
        SystemInstance.get().setComponent(SecurityService.class, new PseudoSecurityService());
    }

    @AfterClass
    public static void reset() {
        SystemInstance.reset();
    }

    @Test
    public void platformThreadImplementsManageableThread() {
        final ContextServiceImpl contextService = ContextServiceImplFactory.newDefaultContextService();
        final ManagedThreadFactoryImpl factory = new ManagedThreadFactoryImpl("test-", null, contextService, false);

        final Thread thread = factory.newThread(() -> {});
        assertNotNull(thread);
        assertTrue("Platform thread should implement ManageableThread",
                thread instanceof ManageableThread);
    }

    @Test
    public void virtualThreadDoesNotImplementManageableThread() {
        Assume.assumeTrue("Virtual threads require Java 21+", VirtualThreadHelper.isSupported());

        final ContextServiceImpl contextService = ContextServiceImplFactory.newDefaultContextService();
        final ManagedThreadFactoryImpl factory = new ManagedThreadFactoryImpl("test-vt-", null, contextService, true);

        final Thread thread = factory.newThread(() -> {});
        assertNotNull(thread);
        // Spec 3.4.4: virtual threads do NOT implement ManageableThread
        assertFalse("Virtual thread must NOT implement ManageableThread",
                thread instanceof ManageableThread);
    }

    @Test
    public void virtualThreadExecutesTask() {
        Assume.assumeTrue("Virtual threads require Java 21+", VirtualThreadHelper.isSupported());

        final ContextServiceImpl contextService = ContextServiceImplFactory.newDefaultContextService();
        final ManagedThreadFactoryImpl factory = new ManagedThreadFactoryImpl("test-vt-", null, contextService, true);

        final CountDownLatch latch = new CountDownLatch(1);
        final Thread thread = factory.newThread(latch::countDown);
        thread.start();

        try {
            assertTrue("Virtual thread should execute the task",
                    latch.await(5, TimeUnit.SECONDS));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted");
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void virtualFactoryRejectsForkJoinPool() {
        Assume.assumeTrue("Virtual threads require Java 21+", VirtualThreadHelper.isSupported());

        final ContextServiceImpl contextService = ContextServiceImplFactory.newDefaultContextService();
        final ManagedThreadFactoryImpl factory = new ManagedThreadFactoryImpl("test-vt-", null, contextService, true);

        factory.newThread(new ForkJoinPool());
    }

    @Test
    public void isVirtualReflectsConstructorParam() {
        final ContextServiceImpl contextService = ContextServiceImplFactory.newDefaultContextService();

        final ManagedThreadFactoryImpl platformFactory = new ManagedThreadFactoryImpl("p-", null, contextService, false);
        assertFalse(platformFactory.isVirtual());

        final ManagedThreadFactoryImpl virtualFactory = new ManagedThreadFactoryImpl("v-", null, contextService, true);
        assertTrue(virtualFactory.isVirtual());
    }
}
