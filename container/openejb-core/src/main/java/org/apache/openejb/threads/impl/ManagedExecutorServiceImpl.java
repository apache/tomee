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

import org.apache.openejb.api.resource.DestroyableResource;
import org.apache.openejb.threads.future.CUFuture;
import org.apache.openejb.threads.task.CUCallable;
import org.apache.openejb.threads.task.CURunnable;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.enterprise.concurrent.ManagedExecutorService;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ManagedExecutorServiceImpl extends AbstractExecutorService implements ManagedExecutorService, DestroyableResource {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, ManagedExecutorServiceImpl.class);

    private final ExecutorService delegate;

    public ManagedExecutorServiceImpl(final ExecutorService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void shutdown() {
        throw new IllegalStateException("You can't call shutdown");
    }

    @Override
    public List<Runnable> shutdownNow() {
        throw new IllegalStateException("You can't call shutdownNow");
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }
    
    public Integer getCorePoolSize() {
        if (delegate instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) delegate).getCorePoolSize();
        } else {
            return null;
        }
    }
    
    public Integer getMaximumPoolSize() {
        if (delegate instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) delegate).getMaximumPoolSize();
        } else {
            return null;
        }
    }

    public Integer getPoolSize() {
        if (delegate instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) delegate).getPoolSize();
        } else {
            return null;
        }
    }

    public Integer getActiveCount() {
        if (delegate instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) delegate).getActiveCount();
        } else {
            return null;
        }
    }

    public Integer getLargestPoolSize() {
        if (delegate instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) delegate).getLargestPoolSize();
        } else {
            return null;
        }
    }

    public Integer getQueueSize() {
        if (delegate instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) delegate).getQueue().size();
        } else {
            return null;
        }
    }

    public Long getCompletedTaskCount() {
        if (delegate instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) delegate).getCompletedTaskCount();
        } else {
            return null;
        }
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
        return new CUFuture<>(future, wrapper);
    }

    @Override
    public <T> Future<T> submit(final Runnable task, final T result) {
        final CURunnable wrapper = new CURunnable(task);
        final Future<T> future = delegate.submit(wrapper, result);
        wrapper.taskSubmitted(future, this, task);
        return new CUFuture<>(future, wrapper);
    }

    @Override
    public Future<?> submit(final Runnable task) {
        final CURunnable wrapper = new CURunnable(task);
        final Future<?> future = delegate.submit(wrapper);
        wrapper.taskSubmitted(future, this, task);
        return new CUFuture<>(Future.class.cast(future), wrapper);
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

    @Override
    public void destroyResource() {
        final List<Runnable> runnables = delegate.shutdownNow();
        if (runnables.size() > 0) {
            LOGGER.warning(runnables.size() + " tasks to execute");
            for (final Runnable runnable : runnables) {
                try {
                    LOGGER.info("Executing " + runnable);
                    runnable.run();
                } catch (final Throwable th) {
                    LOGGER.error(th.getMessage(), th);
                }
            }
        }
    }
}
