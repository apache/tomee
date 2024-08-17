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
package org.apache.openejb.cdi.concurrency;

import jakarta.annotation.Priority;
import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import javax.naming.InitialContext;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.RejectedExecutionException;

@Interceptor
@Asynchronous
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 5)
public class AsynchronousInterceptor {

    @AroundInvoke
    public Object aroundInvoke(final InvocationContext ctx) throws Exception {
        // TODO validate no MP Async annotation
        Asynchronous asynchronous = ctx.getMethod().getAnnotation(Asynchronous.class);
        if (asynchronous == null) {
            throw new UnsupportedOperationException("Asynchronous annotation must be placed on method");
        }

        Method method = ctx.getMethod();
        Class<?> returnType = method.getReturnType();
        if (returnType != Void.TYPE && returnType != CompletableFuture.class && returnType != CompletionStage.class) {
            throw new UnsupportedOperationException("Asynchronous annotation must be placed on method that returns either void, CompletableFuture or CompletionStage");
        }

        Object executor = InitialContext.doLookup(asynchronous.executor());
        if (!(executor instanceof ManagedExecutorService mes)) {
            throw new RejectedExecutionException("Cannot lookup ManagedExecutorService '%s', got: %s".formatted(asynchronous.executor(), executor));
        }

        CompletableFuture<Object> future = mes.newIncompleteFuture();
        mes.execute(() -> {
            try {
                Asynchronous.Result.setFuture(future);
                CompletionStage<?> result = (CompletionStage<?>) ctx.proceed();
                if (result == null || result == future) {
                    future.complete(result);

                    Asynchronous.Result.setFuture(null);
                    return;
                }

                result.whenComplete((resultInternal, err) -> {
                    if (resultInternal != null) {
                        future.complete(resultInternal);
                    } else if (err != null) {
                        future.completeExceptionally(err);
                    }

                    Asynchronous.Result.setFuture(null);
                });
            } catch (Exception e) {
                future.completeExceptionally(e);
                Asynchronous.Result.setFuture(null);
            }
        });

        return method.getReturnType() == Void.TYPE ? null : future;
    }
}
