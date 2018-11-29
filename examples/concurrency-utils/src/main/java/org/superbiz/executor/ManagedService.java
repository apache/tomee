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

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.RequestScoped;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static java.util.Objects.nonNull;


@RequestScoped
public class ManagedService {

    @Resource
    private ManagedExecutorService executor;

    /**
     * Executes an opperation asynchronously, in a different thread provided by the {@link ManagedExecutorService}.
     * The computation will carry on after the return of the method.
     *
     * @param value The demo data.
     * @return A {@link CompletableFuture} that will return immediately.
     */
    public CompletableFuture<Integer> asyncTask(final int value) {
        return CompletableFuture
                .supplyAsync(delayedTask(value, 100, null), executor) // Execute asynchronously.
                .thenApply(i -> i + 1); // After the return of the task, do something else with the result.
    }

    /**
     * Executes an opperation asynchronously, in a different thread provided by the {@link ManagedExecutorService}.
     * The computation will carry on after the return of the method.
     *
     * @param value The demo data.
     * @return A {@link CompletableFuture} that will return immediately.
     */
    public CompletableFuture<Integer> asyncTaskWithException(final int value) {
        return CompletableFuture
                .supplyAsync(delayedTask(value, 100, "Planned exception"), executor) // Execute asynchronously.
                .thenApply(i -> i + 1); // After the return of the task, do something else with the result.
    }

    /**
     * Method to simulate an asynchronous task. Will add 1 to the value for each invocation.
     *
     * @param value        The demo data.
     * @param delayMs      How long the task will take to complete. In ms.
     * @param errorMessage Message for the exception simulating an execution problem
     * @return
     */
    private Supplier<Integer> delayedTask(final int value,
                                          final int delayMs,
                                          final String errorMessage) {
        return () -> {
            if (nonNull(errorMessage)) {
                System.out.println("Exception will be thrown");
                throw new RuntimeException(errorMessage);
            }

            try {
                // simulate long processing task
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                throw new RuntimeException("Problem while waiting");
            }
            System.out.println("delayedTask complete");
            return value + 1;
        };
    }

}
