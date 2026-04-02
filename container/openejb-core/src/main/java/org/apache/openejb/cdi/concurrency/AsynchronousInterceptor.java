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
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.concurrent.ZonedTrigger;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.apache.openejb.core.ivm.naming.NamingException;
import org.apache.openejb.resource.thread.ManagedExecutorServiceImplFactory;
import org.apache.openejb.resource.thread.ManagedScheduledExecutorServiceImplFactory;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;

@Interceptor
@Asynchronous
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 5)
public class AsynchronousInterceptor {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, AsynchronousInterceptor.class);

    public static final String MP_ASYNC_ANNOTATION_NAME = "org.eclipse.microprofile.faulttolerance.Asynchronous";

    // ensure validation logic required by the spec only runs once per invoked Method
    private final Map<Method, Exception> validationCache = new ConcurrentHashMap<>();

    @AroundInvoke
    public Object aroundInvoke(final InvocationContext ctx) throws Exception {
        final Exception exception = validationCache.computeIfAbsent(ctx.getMethod(), this::validate);
        if (exception != null) {
            throw exception;
        }

        final Asynchronous asynchronous = ctx.getMethod().getAnnotation(Asynchronous.class);
        final Schedule[] schedules = asynchronous.runAt();

        if (schedules.length > 0) {
            return aroundInvokeScheduled(ctx, asynchronous, schedules);
        }

        return aroundInvokeOneShot(ctx, asynchronous);
    }

    private Object aroundInvokeOneShot(final InvocationContext ctx, final Asynchronous asynchronous) throws Exception {
        final ManagedExecutorService mes;
        try {
            mes = ManagedExecutorServiceImplFactory.lookup(asynchronous.executor());
        } catch (final NamingException | IllegalArgumentException e) {
            throw new RejectedExecutionException("Cannot lookup ManagedExecutorService", e);
        }

        final CompletableFuture<Object> future = mes.newIncompleteFuture();
        mes.execute(() -> {
            try {
                Asynchronous.Result.setFuture(future);
                final CompletionStage<?> result = (CompletionStage<?>) ctx.proceed();
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
            } catch (final Exception e) {
                future.completeExceptionally(e);
                Asynchronous.Result.setFuture(null);
            }
        });

        return ctx.getMethod().getReturnType() == Void.TYPE ? null : future;
    }

    private Object aroundInvokeScheduled(final InvocationContext ctx, final Asynchronous asynchronous,
                                          final Schedule[] schedules) throws Exception {
        final ManagedScheduledExecutorService mses;
        try {
            mses = ManagedScheduledExecutorServiceImplFactory.lookup(asynchronous.executor());
        } catch (final IllegalArgumentException e) {
            throw new RejectedExecutionException("Cannot lookup ManagedScheduledExecutorService", e);
        }

        final ZonedTrigger trigger = ScheduleHelper.toTrigger(schedules);
        final boolean isVoid = ctx.getMethod().getReturnType() == Void.TYPE;

        // A single CompletableFuture represents ALL executions in the schedule.
        // Each execution gets Asynchronous.Result.setFuture() called before ctx.proceed()
        // so the bean method can call Asynchronous.Result.getFuture() / complete().
        // The schedule stops when the future is completed, cancelled, or an exception is thrown.
        final CompletableFuture<Object> outerFuture = mses.newIncompleteFuture();

        mses.schedule((Callable<Object>) () -> {
            try {
                Asynchronous.Result.setFuture(outerFuture);
                final Object result = ctx.proceed();

                if (isVoid) {
                    // For void methods, the bean may call Asynchronous.Result.complete("value")
                    // to signal completion. If it didn't complete the future, the schedule continues.
                    Asynchronous.Result.setFuture(null);
                    return null;
                }

                if (result instanceof CompletionStage<?> cs) {
                    if (result == outerFuture) {
                        // Bean returned the container-provided future (via Asynchronous.Result.getFuture()).
                        // It may have been completed by Asynchronous.Result.complete() inside the method.
                        Asynchronous.Result.setFuture(null);
                    } else {
                        cs.whenComplete((val, err) -> {
                            if (err != null) {
                                outerFuture.completeExceptionally(err);
                            } else if (val != null) {
                                outerFuture.complete(val);
                            }
                            Asynchronous.Result.setFuture(null);
                        });
                    }
                } else if (result != null) {
                    outerFuture.complete(result);
                    Asynchronous.Result.setFuture(null);
                }
            } catch (final Exception e) {
                outerFuture.completeExceptionally(e);
                Asynchronous.Result.setFuture(null);
            }
            return null;
        }, trigger);

        return isVoid ? null : outerFuture;
    }

    private Exception validate(final Method method) {
        if (hasMpAsyncAnnotation(method.getAnnotations()) || hasMpAsyncAnnotation(method.getDeclaringClass().getAnnotations())) {
            return new UnsupportedOperationException("Combining " + Asynchronous.class.getName()
                    + " and " + MP_ASYNC_ANNOTATION_NAME + " on the same method/class is not supported");
        }

        final Asynchronous asynchronous = method.getAnnotation(Asynchronous.class);
        if (asynchronous == null) {
            return new UnsupportedOperationException("Asynchronous annotation must be placed on a method");
        }

        final Class<?> returnType = method.getReturnType();
        if (returnType != Void.TYPE && returnType != CompletableFuture.class && returnType != CompletionStage.class) {
            return new UnsupportedOperationException("Asynchronous annotation must be placed on a method that returns either void, CompletableFuture or CompletionStage");
        }

        return null;
    }

    private boolean hasMpAsyncAnnotation(final Annotation[] declaredAnnotations) {
        return Arrays.stream(declaredAnnotations)
                .map(it -> it.annotationType().getName())
                .anyMatch(it -> it.equals(MP_ASYNC_ANNOTATION_NAME));
    }
}
