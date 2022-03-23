package org.superbiz.executor;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.executor.ThreadFactoryService.LongTask;

import jakarta.inject.Inject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/*
 * This demonstrates used managed threads inside a container.
 * <br>
 * We use CountDownLatch to demonstrate the thread management because it's faster and
 * safer that simply using a sleep and hope the other thread has completed.
 */
@RunWith(Arquillian.class)
public class ThreadFactoryServiceTest {

    private static final Logger LOGGER = Logger.getLogger(ThreadFactoryServiceTest.class.getSimpleName());

    @Inject
    private ThreadFactoryService factoryService;

    @Deployment()
    public static final WebArchive app() {
        return ShrinkWrap.create(WebArchive.class, "example.war")
                .addClasses(ThreadFactoryService.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void asyncTask() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final LongTask longTask = new LongTask(1, 50, countDownLatch);
        factoryService.asyncTask(longTask);

        countDownLatch.await(200, TimeUnit.MILLISECONDS); // With the countdown latch we don't block unnecessarily.
        LOGGER.info("task was completed");

        assertEquals(2, longTask.getResult());
    }

    @Test
    public void asyncHangingTask() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final LongTask longTask = new LongTask(1, 1000000, countDownLatch);

        factoryService.asyncHangingTask(longTask);

        countDownLatch.await(200, TimeUnit.MILLISECONDS);
        LOGGER.info("task should have been interrupted and its operation not completed.");

        assertTrue(longTask.getIsTerminated());
    }
}