package org.superbiz.executor;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.RequestScoped;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;

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
public class AsyncBookService {

    @Resource
    private ManagedExecutorService executor;

    @Asynchronous
    public Future<String> serviceA() {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new IOException("Simulated error"));
        return future;
    }

    @Asynchronous
    public CompletableFuture<Integer> serviceB() {
        return CompletableFuture.supplyAsync(delayedSupplier(1, 100), executor)
                .thenApply(i -> i + 1);
    }

    @Asynchronous
    public CompletableFuture<Integer> serviceB() {
        return CompletableFuture.supplyAsync(delayedWithExceptionSupplier(100, new RuntimeException("test")), executor);
    }

    private Supplier<Integer> delayedSupplier(final int value,
                                              final int delayMs) {
        return () -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                throw new RuntimeException("Problem while waiting");
            }
            return value;
        };
    }

    private CompletableFuture<Integer> delayedWithExceptionSupplier(final int delayMs,
                                                                    final Throwable t) {
        final CompletableFuture<Integer> future = new CompletableFuture<>();
        try {
            Thread.sleep(delayMs);
            future.completeExceptionally(t);
        } catch (InterruptedException e) {
            throw new RuntimeException("Problem while waiting");
        }
        return future;
    }

}
