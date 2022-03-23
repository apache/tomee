package org.superbiz.executor;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.context.RequestScoped;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

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
@RequestScoped
public class ThreadFactoryService {

    private static final Logger LOGGER = Logger.getLogger(ThreadFactoryService.class.getSimpleName());

    @Resource
    private ManagedThreadFactory factory;

    /**
     * Happy path.
     *
     * @param longTask to compute
     */
    public void asyncTask(final LongTask longTask) throws InterruptedException {
        LOGGER.info("Create asyncTask");

        final Thread thread = factory.newThread(longTask);
        thread.setName("pretty asyncTask");
        thread.start();
    }

    /**
     * Example where we have to stop a thread.
     *
     * @param longTask
     * @throws InterruptedException
     */
    public void asyncHangingTask(final Runnable longTask) {
        LOGGER.info("Create asyncHangingTask");

        final Thread thread = factory.newThread(longTask);
        thread.setName("pretty asyncHangingTask");
        thread.start();

        if (thread.isAlive()) {
            // This will cause any wait in the thread to resume.
            // This will call the InterruptedException block in the longRunnableTask method.
            thread.interrupt();
        }
    }

    /**
     * Runnable rung task simulating a lengthy operation.
     * In the other test classes we use anonymous classes.
     * It's useful to have a "real" class in this case to be able to access the result of the operation.
     */
    public static class LongTask implements Runnable {
        private final int value;
        private final long taskDurationMs;
        private final CountDownLatch countDownLatch;
        private int result;
        private AtomicBoolean isTerminated = new AtomicBoolean(false);

        public LongTask(final int value,
                        final long taskDurationMs,
                        final CountDownLatch countDownLatch) {
            this.value = value;
            this.taskDurationMs = taskDurationMs;
            this.countDownLatch = countDownLatch;
        }

        public int getResult() {
            return result;
        }

        public boolean getIsTerminated() {
            return isTerminated.get();
        }

        @Override
        public void run() {
            try {
                // Simulate a long processing task using TimeUnit to sleep.
                TimeUnit.MILLISECONDS.sleep(taskDurationMs);
            } catch (InterruptedException e) {
                isTerminated.set(true);
                countDownLatch.countDown();
                throw new RuntimeException("Problem while waiting");
            }

            result = value + 1;
            LOGGER.info("longRunnableTask complete. Value is " + result);
            countDownLatch.countDown();
            // Cannot return result with a Runnable. Must store and access it later.
        }
    }
}
