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

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Arquillian integration test for virtual thread support in
 * {@code @ManagedThreadFactoryDefinition(virtual = true)}.
 * Requires Java 21+ — skipped on earlier JVMs.
 */
@RunWith(Arquillian.class)
public class VirtualThreadTest {

    private static boolean isVirtualThreadSupported() {
        try {
            Thread.class.getMethod("ofVirtual");
            return true;
        } catch (final NoSuchMethodException e) {
            return false;
        }
    }

    @Inject
    private VirtualThreadBean bean;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "VirtualThreadTest.war")
                .addClasses(VirtualThreadBean.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test
    public void virtualThreadFactoryCreatesThread() throws Exception {
        Assume.assumeTrue("Virtual threads require Java 21+", isVirtualThreadSupported());

        assertNotNull("VirtualThreadBean should be injected", bean);
        final Thread thread = bean.createVirtualThread();
        assertNotNull("Virtual thread should be created", thread);

        // Virtual threads are not ManageableThread (spec 3.4.4)
        assertFalse("Virtual thread should NOT implement ManageableThread",
                thread instanceof jakarta.enterprise.concurrent.ManageableThread);
    }

    @Test
    public void virtualThreadExecutesTask() throws Exception {
        Assume.assumeTrue("Virtual threads require Java 21+", isVirtualThreadSupported());

        final boolean completed = bean.runOnVirtualThread();
        assertTrue("Task should complete on virtual thread", completed);
    }

    @Test
    public void platformThreadFactoryStillWorks() throws Exception {
        // This test should always run — verifies non-virtual path is unbroken
        assertNotNull("VirtualThreadBean should be injected", bean);
        final boolean completed = bean.runOnPlatformThread();
        assertTrue("Task should complete on platform thread", completed);
    }

    @ManagedThreadFactoryDefinition(
            name = "java:comp/env/concurrent/VirtualThreadFactory",
            virtual = true
    )
    @ManagedThreadFactoryDefinition(
            name = "java:comp/env/concurrent/PlatformThreadFactory",
            virtual = false
    )
    @ApplicationScoped
    public static class VirtualThreadBean {

        @Resource(lookup = "java:comp/env/concurrent/VirtualThreadFactory")
        private ManagedThreadFactory virtualFactory;

        @Resource(lookup = "java:comp/env/concurrent/PlatformThreadFactory")
        private ManagedThreadFactory platformFactory;

        public Thread createVirtualThread() {
            return virtualFactory.newThread(() -> {});
        }

        public boolean runOnVirtualThread() throws InterruptedException {
            final CountDownLatch latch = new CountDownLatch(1);
            final Thread thread = virtualFactory.newThread(latch::countDown);
            thread.start();
            return latch.await(5, TimeUnit.SECONDS);
        }

        public boolean runOnPlatformThread() throws InterruptedException {
            final CountDownLatch latch = new CountDownLatch(1);
            final Thread thread = platformFactory.newThread(latch::countDown);
            thread.start();
            return latch.await(5, TimeUnit.SECONDS);
        }
    }
}
