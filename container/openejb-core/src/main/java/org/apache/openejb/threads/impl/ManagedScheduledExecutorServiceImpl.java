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
package org.apache.openejb.threads.impl;

import org.apache.openejb.threads.future.CUScheduledFuture;
import org.apache.openejb.threads.future.CUTriggerScheduledFuture;
import org.apache.openejb.threads.task.CUCallable;
import org.apache.openejb.threads.task.CURunnable;
import org.apache.openejb.threads.task.TriggerCallable;
import org.apache.openejb.threads.task.TriggerRunnable;
import org.apache.openejb.threads.task.TriggerTask;

import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedTask;
import jakarta.enterprise.concurrent.Trigger;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ManagedScheduledExecutorServiceImpl extends ManagedExecutorServiceImpl implements ManagedScheduledExecutorService {
    private final ScheduledExecutorService delegate;
    private final ContextServiceImpl contextService;

    public ManagedScheduledExecutorServiceImpl(final ScheduledExecutorService delegate, final ContextServiceImpl contextService) {
        super(delegate, contextService);
        this.delegate = delegate;
        this.contextService = contextService;
    }


    @Override
    public ScheduledFuture<?> schedule(final Runnable runnable, final Trigger trigger) {
        Objects.requireNonNull(runnable);
        final Date taskScheduledTime = new Date();
        final AtomicReference<Future<?>> futureHandle = new AtomicReference<>();
        final TriggerRunnable wrapper = new TriggerRunnable(this, contextService, runnable, new CURunnable(runnable, contextService), trigger, taskScheduledTime, getTaskId(runnable), AtomicReference.class.cast(futureHandle));
        return initTriggerScheduledFuture(AtomicReference.class.cast(futureHandle), wrapper);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(final Callable<V> vCallable, final Trigger trigger) {
        Objects.requireNonNull(vCallable);
        final Date taskScheduledTime = new Date();
        final AtomicReference<Future<V>> futureHandle = new AtomicReference<>();
        final TriggerCallable<V> wrapper = new TriggerCallable<>(this, this.contextService, vCallable, new CUCallable<>(vCallable, contextService), trigger, taskScheduledTime, getTaskId(vCallable), futureHandle);
        return initTriggerScheduledFuture(futureHandle, wrapper);
    }

    private <V> ScheduledFuture<V> initTriggerScheduledFuture(final AtomicReference<Future<V>> futureHandle, final TriggerTask<V> wrapper) {
        wrapper.scheduleNextRun();

        ScheduledFuture<V> proxy = (ScheduledFuture<V>) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{ScheduledFuture.class},
                new TriggerBasedScheduledFutureFacade(futureHandle));

        return new CUTriggerScheduledFuture<>(proxy, wrapper);
    }

    @Override
    public ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit) {
        Objects.requireNonNull(command);
        final CURunnable wrapper = new CURunnable(command, contextService);
        final ScheduledFuture<?> future = delegate.schedule(wrapper, delay, unit);
        wrapper.taskSubmitted(future, this, command);
        return new CUScheduledFuture<>(ScheduledFuture.class.cast(future), wrapper);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(final Callable<V> callable, final long delay, final TimeUnit unit) {
        Objects.requireNonNull(callable);
        final CUCallable<V> wrapper = new CUCallable<>(callable);
        final ScheduledFuture<V> future = delegate.schedule(wrapper, delay, unit);
        wrapper.taskSubmitted(future, this, callable);
        return new CUScheduledFuture<>(future, wrapper);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period, final TimeUnit unit) {
        Objects.requireNonNull(command);
        final CURunnable wrapper = new CURunnable(command, contextService);
        final ScheduledFuture<?> future = delegate.scheduleAtFixedRate(wrapper, initialDelay, period, unit);
        wrapper.taskSubmitted(future, this, command);
        return new CUScheduledFuture<>(ScheduledFuture.class.cast(future), wrapper);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay, final TimeUnit unit) {
        Objects.requireNonNull(command);
        final CURunnable wrapper = new CURunnable(command, contextService);
        final ScheduledFuture<?> future = delegate.scheduleWithFixedDelay(wrapper, initialDelay, delay, unit);
        wrapper.taskSubmitted(future, this, command);
        return new CUScheduledFuture<>(ScheduledFuture.class.cast(future), wrapper);
    }

    public static long nowMs() {
        return System.currentTimeMillis(); // need to be comparable to java.util.Date
    }

    private static String getTaskId(final Object runnable) {
        if (runnable instanceof ManagedTask managedTask) {
            Map<String, String> executionProps = managedTask.getExecutionProperties();
            if (executionProps != null) {
                return executionProps.get(ManagedTask.IDENTITY_NAME);
            }
        }

        return null;
    }

    @Override
    public ScheduledExecutorService getDelegate() {
        return delegate;
    }

    /**
     * Automatically resolves an AtomicReference
     * @param delegate
     * @param <V>
     */
    private record TriggerBasedScheduledFutureFacade<V>(AtomicReference<ScheduledFuture<V>> delegate) implements InvocationHandler {
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            try {
                return method.invoke(delegate.get(), args);
            } catch (final InvocationTargetException ite) {
                throw ite.getCause();
            }
        }
    }
}
