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

import jakarta.enterprise.concurrent.SkippedException;
import org.apache.openejb.threads.task.TriggerTask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Delegates isDone calls to TriggerTask and throws SkippedExceptions in get methods if task execution has been skipped
 * @param <V>
 */
public class CUTriggerScheduledFuture<V> extends CUScheduledFuture<V> {
    public CUTriggerScheduledFuture(ScheduledFuture<V> delegate, TriggerTask<V> task) {
        super(delegate, task);
    }

    @Override
    public boolean isDone() {
        return super.isDone() && ((TriggerTask<V>) listener).isDone();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        ((TriggerTask<V>) listener).cancelScheduling();
        return super.cancel(mayInterruptIfRunning);
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        V result = super.get();
        if (((TriggerTask<V>) listener).isSkipped()) {
            throw new SkippedException();
        }

        return result;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        V result = super.get(timeout, unit);
        if (((TriggerTask<V>) listener).isSkipped()) {
            throw new SkippedException();
        }

        return result;
    }
}
