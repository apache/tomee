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
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.RequestScoped;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.nonNull;


@RequestScoped
public class ManagedScheduledService {

    @Resource
    private ManagedScheduledExecutorService executor;

    public Future<Integer> singleFixedDelayTask(final int value, final String errorMessage) {
        System.out.println("longCallableTask scheduled");
        return executor.schedule(longCallableTask(value, 10, errorMessage), 100, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> periodicFixedDelayTask(final int value, final String errorMessage) {
        System.out.println("longRunnableTask scheduled");
        return executor.scheduleAtFixedRate(longRunnableTask(value, 10, errorMessage), 0, 100, TimeUnit.MILLISECONDS);
    }

    private Runnable longRunnableTask(final int value,
                                      final int taskDurationMs,
                                      final String errorMessage) {
        return () -> {
            if (nonNull(errorMessage)) {
                System.out.println("Exception will be thrown");
                throw new RuntimeException(errorMessage);
            }

            Integer result = value + 1;
            System.out.println("longRunnableTask complete. Value is " + result);
            // Cannot return result with a Runnable.
        };
    }

    private Callable<Integer> longCallableTask(final int value,
                                               final int taskDurationMs,
                                               final String errorMessage) {
        return () -> {
            System.out.println("longCallableTask start");
            if (nonNull(errorMessage)) {
                System.out.println("Exception will be thrown");
                throw new RuntimeException(errorMessage);
            }

            try {
                // Simulate a long processing task using TimeUnit to sleep.
                TimeUnit.MILLISECONDS.sleep(taskDurationMs);
            } catch (InterruptedException e) {
                throw new RuntimeException("Problem while waiting");
            }
            System.out.println("longCallableTask complete");
            // We can return a result with a Callable.
            return value + 1;
        };
    }

}
