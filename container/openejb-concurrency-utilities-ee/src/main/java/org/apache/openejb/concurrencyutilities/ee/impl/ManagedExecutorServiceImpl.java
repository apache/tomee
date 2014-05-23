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
package org.apache.openejb.concurrencyutilities.ee.impl;

import org.apache.openejb.concurrencyutilities.ee.future.CUFuture;
import org.apache.openejb.concurrencyutilities.ee.task.CUCallable;
import org.apache.openejb.concurrencyutilities.ee.task.CURunnable;
import org.apache.openejb.util.Duration;

import javax.enterprise.concurrent.ManagedExecutorService;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ManagedExecutorServiceImpl extends AbstractExecutorService implements ManagedExecutorService {
    private final ExecutorService delegate;
    private final Duration waitAtShutdown;

    public ManagedExecutorServiceImpl(final ExecutorService delegate, final Duration waitAtShutdown) {
        this.delegate = delegate;
        this.waitAtShutdown = waitAtShutdown;
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
        try { // wait a bit to let task a chance to be done
            delegate.awaitTermination(waitAtShutdown.getTime(), waitAtShutdown.getUnit());
        } catch (final InterruptedException e) {
            // no-op
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        final CUCallable<T> wrapper = new CUCallable<T>(task);
        final Future<T> future = delegate.submit(wrapper);
        wrapper.taskSubmitted(future, this, task);
        return new CUFuture<T>(future, wrapper);
    }

    @Override
    public <T> Future<T> submit(final Runnable task, final T result) {
        final CURunnable wrapper = new CURunnable(task);
        final Future<T> future = delegate.submit(wrapper, result);
        wrapper.taskSubmitted(future, this, task);
        return new CUFuture<T>(future, wrapper);
    }

    @Override
    public Future<?> submit(final Runnable task) {
        final CURunnable wrapper = new CURunnable(task);
        final Future<?> future = delegate.submit(wrapper);
        wrapper.taskSubmitted(future, this, task);
        return new CUFuture<Void>(CUFuture.class.cast(future), wrapper);
    }

    @Override
    public void execute(final Runnable command) {
        final CURunnable wrapper = new CURunnable(command);
        delegate.execute(wrapper);
        wrapper.taskSubmitted(null, this, command);
    }

    public ExecutorService getDelegate() {
        return delegate;
    }
}
