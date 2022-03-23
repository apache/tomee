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
package org.apache.openejb.threads.task;

import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedTask;
import jakarta.enterprise.concurrent.ManagedTaskListener;
import java.util.concurrent.Future;

public abstract class ManagedTaskListenerTask implements ManagedTaskListener {
    private final ManagedTaskListener listener;

    protected Future<?> future;
    protected final Object delegate;
    protected ManagedExecutorService executor;

    protected ManagedTaskListenerTask(final Object task) {
        if (ManagedTask.class.isInstance(task)) {
            listener = ManagedTask.class.cast(task).getManagedTaskListener();
        } else {
            listener = NoopManagedTaskListener.INSTANCE;
        }
        this.delegate = task;
    }

    @Override
    public void taskSubmitted(final Future<?> future, final ManagedExecutorService executor, final Object task) {
        this.future = future;
        this.executor = executor;

        if (listener != null) {
            listener.taskSubmitted(future, executor, task);
        }
    }

    @Override
    public void taskAborted(final Future<?> future, final ManagedExecutorService executor, final Object task, final Throwable exception) {
        if (listener != null) {
            // use saved values since called with null excepted for the exception
            listener.taskAborted(this.future, this.executor, this.delegate, exception);
        }
    }

    @Override
    public void taskDone(final Future<?> future, final ManagedExecutorService executor, final Object task, final Throwable exception) {
        if (listener != null) {
            listener.taskDone(future, executor, task, exception);
        }
    }

    @Override
    public void taskStarting(final Future<?> future, final ManagedExecutorService executor, final Object task) {
        if (listener != null) {
            listener.taskStarting(future, executor, task);
        }
    }

    public void taskAborted(final Throwable skippedException) {
        taskAborted(future, executor, delegate, skippedException);
    }

    public Object getDelegate() {
        return delegate;
    }

    private static class NoopManagedTaskListener implements ManagedTaskListener {
        private static final NoopManagedTaskListener INSTANCE = new NoopManagedTaskListener();

        @Override
        public void taskSubmitted(final Future<?> future, final ManagedExecutorService executor, final Object task) {
            // no-op
        }

        @Override
        public void taskAborted(final Future<?> future, final ManagedExecutorService executor, final Object task, final Throwable exception) {
            // no-op
        }

        @Override
        public void taskDone(final Future<?> future, final ManagedExecutorService executor, final Object task, final Throwable exception) {
            // no-op
        }

        @Override
        public void taskStarting(final Future<?> future, final ManagedExecutorService executor, final Object task) {
            // no-op
        }
    }
}
