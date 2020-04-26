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

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.context.RequestScoped;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.Objects.nonNull;


@RequestScoped
public class ManagedScheduledService {

    private static final Logger LOGGER = Logger.getLogger(ManagedScheduledService.class.getSimpleName());

    @Resource
    private ManagedScheduledExecutorService executor;

    /**
     * Execute a task after a planned delay and get the result back by using a {@link Callable}
     *
     * @param value        The value to compute
     * @param errorMessage If not null an exception with be thrown with this message
     * @return the processed result
     */
    public Future<Integer> singleFixedDelayTask(final int value,
                                                final String errorMessage) {
        LOGGER.info("longCallableTask scheduled");
        return executor.schedule(
                longCallableTask(value, 10, errorMessage), 100, TimeUnit.MILLISECONDS);
    }

    /**
     * Execute a task periodically. Although a future is returned, it will not contain a result because the
     * executor uses a runnable to perform the operations.<br>
     * If an exception happens, the task will stop and you can catch the exception with the {@link ScheduledFuture}.
     *
     * @param value          The value to compute
     * @param errorMessage   If not null an exception with be thrown with this message
     * @param countDownLatch
     * @return An object where you can cancel the periodic task and check for exceptions.
     */
    public ScheduledFuture<?> periodicFixedDelayTask(final int value,
                                                     final String errorMessage,
                                                     final CountDownLatch countDownLatch) {
        LOGGER.info("longRunnableTask scheduled");
        return executor.scheduleAtFixedRate(
                longRunnableTask(value, 10, errorMessage, countDownLatch), 0, 100, TimeUnit.MILLISECONDS);
    }

    /**
     * Will simulate a long running operation
     *
     * @param value          The value to compute
     * @param taskDurationMs the time length of the operation
     * @param errorMessage   If not null an exception with be thrown with this message
     * @return a {@link Runnable}
     */
    private Runnable longRunnableTask(final int value,
                                      final int taskDurationMs,
                                      final String errorMessage,
                                      final CountDownLatch countDownLatch) {
        return () -> {
            failOrWait(taskDurationMs, errorMessage);
            Integer result = value + 1;
            LOGGER.info("longRunnableTask complete. Value is " + result);
            // Cannot return result with a Runnable.
            countDownLatch.countDown();
        };
    }

    /**
     * Will simulate a long running operation
     *
     * @param value          The value to compute
     * @param taskDurationMs the time lenght of the operation
     * @param errorMessage   If not null an exception with be thrown with this message
     * @return a {@link Callable} with the result
     */
    private Callable<Integer> longCallableTask(final int value,
                                               final int taskDurationMs,
                                               final String errorMessage) {
        return () -> {
            LOGGER.info("longCallableTask start");
            failOrWait(taskDurationMs, errorMessage);
            LOGGER.info("longCallableTask complete");
            // We can return a result with a Callable.
            return value + 1;
        };
    }

    private void failOrWait(final int taskDurationMs,
                            final String errorMessage) {
        if (nonNull(errorMessage)) {
            LOGGER.severe("Exception will be thrown");
            throw new RuntimeException(errorMessage);
        }
        try {
            // Simulate a long processing task using TimeUnit to sleep.
            TimeUnit.MILLISECONDS.sleep(taskDurationMs);
        } catch (InterruptedException e) {
            throw new RuntimeException("Problem while waiting");
        }
    }

}
