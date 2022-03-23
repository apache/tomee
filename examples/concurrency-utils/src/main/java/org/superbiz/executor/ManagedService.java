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
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.RequestScoped;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static java.util.Objects.nonNull;


@RequestScoped
public class ManagedService {

    private static final Logger LOGGER = Logger.getLogger(ManagedService.class.getSimpleName());

    @Resource
    private ManagedExecutorService executor;

    /**
     * Executes an operation asynchronously, in a different thread provided by the {@link ManagedExecutorService}.
     * The computation will carry on after the return of the method.
     *
     * @param value The demo data.
     * @return A {@link CompletableFuture} that will return immediately.
     */
    public CompletableFuture<Integer> asyncTask(final int value) {
        LOGGER.info("Create asyncTask");
        return CompletableFuture
                .supplyAsync(longTask(value, 100, null), executor) // Execute asynchronously.
                .thenApply(i -> i + 1); // After the return of the task, do something else with the result.
    }

    /**
     * Executes an operation asynchronously, in a different thread provided by the {@link ManagedExecutorService}.
     * The computation will carry on after the return of the method.
     *
     * @param value The demo data.
     * @return A {@link CompletableFuture} that will return immediately.
     */
    public CompletableFuture<Integer> asyncTaskWithException(final int value) {
        LOGGER.info("Create asyncTaskWithException");
        return CompletableFuture
                .supplyAsync(longTask(value, 100, "Planned exception"), executor) // Execute asynchronously.
                .thenApply(i -> i + 1); // After the return of the task, do something else with the result.
    }

    /**
     * Method to simulate an asynchronous task. Will add 1 to the value for each invocation.
     *
     * @param value          The demo data.
     * @param taskDurationMs How long the task will take to complete. In ms.
     * @param errorMessage   Message for the exception simulating an execution problem
     * @return a {@link Supplier} function processing the new value
     */
    private Supplier<Integer> longTask(final int value,
                                       final int taskDurationMs,
                                       final String errorMessage) {
        return () -> {
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
            LOGGER.info("longTask complete");
            return value + 1;
        };
    }

}
