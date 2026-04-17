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
import jakarta.enterprise.concurrent.LastExecution;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.concurrent.ZonedTrigger;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.apache.openejb.core.ivm.naming.NamingException;
import org.apache.openejb.resource.thread.ManagedExecutorServiceImplFactory;
import org.apache.openejb.resource.thread.ManagedScheduledExecutorServiceImplFactory;
import org.apache.openejb.threads.impl.ContextServiceImpl;
import org.apache.openejb.threads.impl.ManagedExecutorServiceImpl;
import org.apache.openejb.threads.impl.ManagedScheduledExecutorServiceImpl;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Interceptor
@Asynchronous
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 5)
public class AsynchronousInterceptor {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, AsynchronousInterceptor.class);

    public static final String MP_ASYNC_ANNOTATION_NAME = "org.eclipse.microprofile.faulttolerance.Asynchronous";
    private static final ScheduledAsyncInvoker SCHEDULED_ASYNC_INVOKER = new ScheduledAsyncInvoker();

    // ensure validation logic required by the spec only runs once per invoked Method
    private final Map<Method, Exception> validationCache = new ConcurrentHashMap<>();

    @AroundInvoke
    public Object aroundInvoke(final InvocationContext ctx) throws Exception {
        final Exception exception = validationCache.computeIfAbsent(ctx.getMethod(), this::validate);
        if (exception != null) {
            throw exception;
        }

        if (SCHEDULED_ASYNC_INVOKER.isReentry(ctx)) {
            return ctx.proceed();
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
        // Per spec, the executor attribute may reference either a ManagedScheduledExecutorService
        // or a plain ManagedExecutorService. When a plain MES is referenced, fall back to the
        // default MSES for scheduling capability but preserve the MES's context service.
        final ManagedScheduledExecutorServiceImpl mses = resolveMses(asynchronous.executor());

        final ZonedTrigger trigger = ScheduleHelper.toTrigger(schedules);
        final boolean isVoid = ctx.getMethod().getReturnType() == Void.TYPE;
        final ContextServiceImpl ctxService = (ContextServiceImpl) mses.getContextService();
        final ContextServiceImpl.Snapshot snapshot = ctxService.snapshot(null);

        // Run scheduled firings on the requested executor so the user's thread factory,
        // priorities, and virtual-thread settings apply.
        final ScheduledExecutorService triggerDelegate = mses.getDelegate();

        // A single CompletableFuture represents ALL executions in the schedule.
        // Per spec: "A single future represents the completion of all executions in the schedule."
        // The schedule continues until:
        //   - the method returns a non-null result value
        //   - the method raises an exception
        //   - the future is completed (via Asynchronous.Result.complete()) or cancelled
        final CompletableFuture<Object> outerFuture = mses.newIncompleteFuture();
        final AtomicReference<ScheduledFuture<?>> scheduledRef = new AtomicReference<>();
        final AtomicReference<LastExecution> lastExecutionRef = new AtomicReference<>();

        final ScheduledAsyncInvoker.Invocation scheduledInvocation = SCHEDULED_ASYNC_INVOKER.capture(ctx);

        scheduleNextExecution(triggerDelegate, snapshot, ctxService, trigger, outerFuture,
                scheduledInvocation, isVoid, scheduledRef, lastExecutionRef);

        // Cancel the underlying scheduled task when the future completes externally
        // (e.g. Asynchronous.Result.complete() or cancel())
        outerFuture.whenComplete((final Object val, final Throwable err) -> {
            final ScheduledFuture<?> sf = scheduledRef.get();
            if (sf != null) {
                sf.cancel(false);
            }
            scheduledInvocation.release();
        });

        return isVoid ? null : outerFuture;
    }

    private ManagedScheduledExecutorServiceImpl resolveMses(final String executorName) {
        try {
            return ManagedScheduledExecutorServiceImplFactory.lookup(executorName);
        } catch (final IllegalArgumentException e) {
            // The executor might be a plain ManagedExecutorService — verify it exists,
            // then use the default MSES for scheduling with the MES's context service
            try {
                final ManagedExecutorServiceImpl plainMes = ManagedExecutorServiceImplFactory.lookup(executorName);
                final ContextServiceImpl mesContextService = (ContextServiceImpl) plainMes.getContextService();
                final ManagedScheduledExecutorServiceImpl defaultMses =
                        ManagedScheduledExecutorServiceImplFactory.lookup("java:comp/DefaultManagedScheduledExecutorService");
                return new ManagedScheduledExecutorServiceImpl(defaultMses.getDelegate(), mesContextService);
            } catch (final Exception fallbackEx) {
                throw new RejectedExecutionException("Cannot lookup executor for scheduled async method", e);
            }
        }
    }

    private void scheduleNextExecution(final ScheduledExecutorService delegate, final ContextServiceImpl.Snapshot snapshot,
                                       final ContextServiceImpl ctxService, final ZonedTrigger trigger,
                                       final CompletableFuture<Object> future,
                                       final ScheduledAsyncInvoker.Invocation scheduledInvocation,
                                       final boolean isVoid, final AtomicReference<ScheduledFuture<?>> scheduledRef,
                                       final AtomicReference<LastExecution> lastExecutionRef) {
        final ZonedDateTime taskScheduledTime = ZonedDateTime.now();
        final ZonedDateTime nextRun = trigger.getNextRunTime(lastExecutionRef.get(), taskScheduledTime);
        if (nextRun == null || future.isDone()) {
            return;
        }

        final long delayMs = Duration.between(ZonedDateTime.now(), nextRun).toMillis();

        final ScheduledFuture<?> sf = delegate.schedule(() -> {
            if (future.isDone()) {
                return;
            }

            final ContextServiceImpl.State state = ctxService.enter(snapshot);
            try {
                if (trigger.skipRun(lastExecutionRef.get(), nextRun)) {
                    // Skipped — reschedule for the next run
                    scheduleNextExecution(delegate, snapshot, ctxService, trigger, future,
                            scheduledInvocation, isVoid, scheduledRef, lastExecutionRef);
                    return;
                }

                final ZonedDateTime runStart = ZonedDateTime.now();
                Asynchronous.Result.setFuture(future);

                final Object result = scheduledInvocation.proceed();
                final ZonedDateTime runEnd = ZonedDateTime.now();

                // Track last execution for trigger computation
                lastExecutionRef.set(new SimpleLastExecution(taskScheduledTime, runStart, runEnd, result));

                if (isVoid) {
                    Asynchronous.Result.setFuture(null);
                    scheduleNextExecution(delegate, snapshot, ctxService, trigger, future,
                            scheduledInvocation, isVoid, scheduledRef, lastExecutionRef);
                    return;
                }

                // Per spec: non-null return value stops the schedule
                if (result != null) {
                    if (result instanceof CompletionStage<?> cs && result != future) {
                        cs.whenComplete((final Object val, final Throwable err) -> {
                            if (err != null) {
                                future.completeExceptionally(err);
                            } else {
                                future.complete(val);
                            }
                        });
                    }
                    Asynchronous.Result.setFuture(null);
                    // Don't reschedule — method returned non-null
                    return;
                }

                Asynchronous.Result.setFuture(null);
                // null return: schedule continues
                scheduleNextExecution(delegate, snapshot, ctxService, trigger, future,
                        scheduledInvocation, isVoid, scheduledRef, lastExecutionRef);
            } catch (final java.lang.reflect.InvocationTargetException e) {
                future.completeExceptionally(e.getCause() != null ? e.getCause() : e);
                Asynchronous.Result.setFuture(null);
            } catch (final Exception e) {
                future.completeExceptionally(e);
                Asynchronous.Result.setFuture(null);
            } finally {
                ctxService.exit(state);
            }
        }, Math.max(0, delayMs), TimeUnit.MILLISECONDS);

        scheduledRef.set(sf);
    }

    /**
     * Simple {@link LastExecution} implementation for tracking execution history
     * within the manual trigger loop.
     */
    private record SimpleLastExecution(ZonedDateTime scheduledStart, ZonedDateTime runStart,
                                       ZonedDateTime runEnd, Object result) implements LastExecution {
        @Override
        public String getIdentityName() {
            return null;
        }

        @Override
        public Object getResult() {
            return result;
        }

        @Override
        public Date getScheduledStart() {
            return Date.from(scheduledStart.toInstant());
        }

        @Override
        public ZonedDateTime getScheduledStart(final ZoneId zone) {
            return scheduledStart.withZoneSameInstant(zone);
        }

        @Override
        public Date getRunStart() {
            return Date.from(runStart.toInstant());
        }

        @Override
        public ZonedDateTime getRunStart(final ZoneId zone) {
            return runStart.withZoneSameInstant(zone);
        }

        @Override
        public Date getRunEnd() {
            return Date.from(runEnd.toInstant());
        }

        @Override
        public ZonedDateTime getRunEnd(final ZoneId zone) {
            return runEnd.withZoneSameInstant(zone);
        }
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
