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
import org.apache.openejb.core.ivm.naming.NamingException;
import org.apache.openejb.resource.thread.ManagedExecutorServiceImplFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;

@Interceptor
@Asynchronous
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 5)
public class AsynchronousInterceptor {
    public static final String MP_ASYNC_ANNOTATION_NAME = "org.eclipse.microprofile.faulttolerance.Asynchronous";

    // ensure validation logic required by the spec only runs once per invoked Method
    private final Map<Method, Exception> validationCache = new ConcurrentHashMap<>();

    @AroundInvoke
    public Object aroundInvoke(final InvocationContext ctx) throws Exception {
        Exception exception = validationCache.computeIfAbsent(ctx.getMethod(), this::validate);
        if (exception != null) {
            throw exception;
        }

        Asynchronous asynchronous = ctx.getMethod().getAnnotation(Asynchronous.class);
        ManagedExecutorService mes;
        try {
            mes = ManagedExecutorServiceImplFactory.lookup(asynchronous.executor());
        } catch (NamingException | IllegalArgumentException e) {
            throw new RejectedExecutionException("Cannot lookup ManagedExecutorService", e);
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

        return ctx.getMethod().getReturnType() == Void.TYPE ? null : future;
    }

    private Exception validate(final Method method) {
        if (hasMpAsyncAnnotation(method.getAnnotations()) || hasMpAsyncAnnotation(method.getDeclaringClass().getAnnotations())) {
            return new UnsupportedOperationException("Combining " + Asynchronous.class.getName()
                    + " and " + MP_ASYNC_ANNOTATION_NAME + " on the same method/class is not supported");
        }

        Asynchronous asynchronous = method.getAnnotation(Asynchronous.class);
        if (asynchronous == null) {
            return new UnsupportedOperationException("Asynchronous annotation must be placed on a method");
        }

        Class<?> returnType = method.getReturnType();
        if (returnType != Void.TYPE && returnType != CompletableFuture.class && returnType != CompletionStage.class) {
            return new UnsupportedOperationException("Asynchronous annotation must be placed on a method that returns either void, CompletableFuture or CompletionStage");
        }

        return null;
    }

    private boolean hasMpAsyncAnnotation(Annotation[] declaredAnnotations) {
        return Arrays.stream(declaredAnnotations)
                .map(it -> it.annotationType().getName())
                .anyMatch(it -> it.equals(MP_ASYNC_ANNOTATION_NAME));
    }
}
