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
package org.apache.openejb.threads.future;

import org.apache.openejb.threads.task.ManagedTaskListenerTask;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CUFuture<V, C> implements Future<V>, Comparable<C> {
    protected final Future<V> delegate;
    protected final ManagedTaskListenerTask listener;

    public CUFuture(final Future<V> delegate, final ManagedTaskListenerTask listener) {
        this.delegate = delegate;
        this.listener = listener;
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        final boolean result = delegate.cancel(mayInterruptIfRunning);
        listener.taskAborted(new CancellationException());
        return result;
    }

    @Override
    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
        return delegate.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return delegate.get();
    }

    @Override
    public V get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.get(timeout, unit);
    }

    @Override
    public int compareTo(final C o) {
        return Comparable.class.isInstance(delegate) ? Comparable.class.cast(delegate).compareTo(o) : -1;
    }
}
