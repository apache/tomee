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
package org.apache.openejb.threads;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(ApplicationComposer.class)
public class CompletableFutureTest {

    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(CompletableFutureTest.Bean.class).localBean();
    }

    @Resource
    private ManagedExecutorService es;

    @Test
    public void testSupplyAsync() throws ExecutionException, InterruptedException {
        assertNotNull(es);

        final String msg = "Test Result";

        final Supplier<String> supplier = () -> msg;
        final CompletableFuture<String> completableFuture = es.supplyAsync(supplier);

        assertFalse(completableFuture.isDone());

        final String result = completableFuture.get();

        assertTrue(completableFuture.isDone());
        assertEquals(msg, result);
    }

    @Test
    public void testSupplyAsyncAndCompleteExceptionally() throws InterruptedException {
        assertNotNull(es);

        final Supplier<String> supplier = () -> {
            throw new RuntimeException("Intentional Exception");
        };

        final CompletableFuture<String> completableFuture = es.supplyAsync(supplier);

        try {
            completableFuture.get();
            fail("Expected ExecutionException not thrown");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof RuntimeException);
            assertEquals("Intentional Exception", e.getCause().getMessage());
        }
    }

    @Test
    public void testRunAsync() throws ExecutionException, InterruptedException {
        assertNotNull(es);

        final AtomicInteger ai = new AtomicInteger(1);
        final Runnable runnable = ai::decrementAndGet;
        final CompletableFuture<Void> completableFuture = es.runAsync(runnable);

        assertFalse(completableFuture.isDone());

        completableFuture.get();

        assertTrue(completableFuture.isDone());
        assertEquals(0, ai.get());
    }

    @Singleton
    public static class Bean {

    }
}
