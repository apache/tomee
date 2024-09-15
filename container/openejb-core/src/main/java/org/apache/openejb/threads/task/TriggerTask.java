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

import jakarta.enterprise.concurrent.LastExecution;
import jakarta.enterprise.concurrent.SkippedException;
import jakarta.enterprise.concurrent.Trigger;
import org.apache.openejb.threads.impl.ContextServiceImpl;
import org.apache.openejb.threads.impl.ManagedScheduledExecutorServiceImpl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public abstract class TriggerTask<T> extends CUTask<T> {
    protected final ManagedScheduledExecutorServiceImpl executorService;
    protected final Trigger trigger;
    protected final Date initiallyScheduled;
    protected final String id;
    protected final AtomicReference<Future<T>> futureRef;

    protected volatile LastExecution lastExecution;
    protected volatile boolean skipped;
    protected volatile boolean done;
    protected volatile boolean cancelled;

    private volatile T result;
    private volatile Date nextRun;

    protected TriggerTask(final Object original, final ContextServiceImpl contextService, final ManagedScheduledExecutorServiceImpl es, final Trigger trigger,
                          final Date taskScheduledTime, final String id, final AtomicReference<Future<T>> ref) {
        super(original, contextService);
        this.executorService = es;
        this.trigger = trigger;
        this.initiallyScheduled = taskScheduledTime;
        this.id = id;
        this.futureRef = ref;
    }

    public void scheduleNextRun() {
        synchronized (this) {
            if (cancelled) {
                return;
            }

            this.nextRun = trigger.getNextRunTime(lastExecution, initiallyScheduled);
            if (nextRun == null) {
                done = true;
                return;
            }

            final ScheduledFuture<T> future = executorService.getDelegate().schedule(this::invokeExecute, millisUntilNextRun(), TimeUnit.MILLISECONDS);

            futureRef.set(future);
            taskSubmitted(future, executorService, delegate);
        }
    }

    private T execute() throws Exception {
        final long wait = millisUntilNextRun();
        if (wait > 0) {
            Thread.sleep(wait);
        }

        final ZonedDateTime runStart = ZonedDateTime.now();
        try {
            skipped = trigger.skipRun(lastExecution, initiallyScheduled);
            if (!skipped) {
                result = doInvoke();
                taskDone(future, executor, delegate, null);
                lastExecution = new LastExecutionImpl(id, result, nextRun, runStart, ZonedDateTime.now());
            } else {
                result = null;
                lastExecution = new LastExecutionImpl(id, result, nextRun, null, null);
            }
        } catch (final RuntimeException re) {
            final SkippedException skippedException = new SkippedException(re);
            taskAborted(skippedException);
            throw skippedException;
        }

        scheduleNextRun();
        return result;
    }

    public T invokeExecute() throws Exception {
        return invoke(this::execute);
    }

    protected long millisUntilNextRun() {
        if (nextRun == null) {
            return 0;
        }

        return nextRun.getTime() - ManagedScheduledExecutorServiceImpl.nowMs();
    }

    protected abstract T doInvoke() throws Exception;

    public String getId() {
        return id;
    }

    public boolean isDone() {
        return done;
    }

    public T getResult() {
        return result;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public LastExecution getLastExecution() {
        return lastExecution;
    }

    public void cancelScheduling() {
        synchronized (this) {
            this.cancelled = true;
            this.done = true;
        }
    }

    private static class LastExecutionImpl implements LastExecution {
        private final String identityName;
        private final Object result;

        private final ZonedDateTime scheduledStart;
        private final ZonedDateTime runStart;
        private final ZonedDateTime runEnd;

        public LastExecutionImpl(final String identityName, final Object result,
                                 final Date scheduledStart, final ZonedDateTime runStart, final ZonedDateTime runEnd) {
            this.identityName = identityName;
            this.result = result;
            this.scheduledStart = scheduledStart.toInstant().atZone(ZoneId.systemDefault());
            this.runStart = runStart;
            this.runEnd = runEnd;
        }

        @Override
        public String getIdentityName() {
            return identityName;
        }

        @Override
        public Object getResult() {
            return result;
        }

        @Override
        public ZonedDateTime getScheduledStart(final ZoneId zone) {
            return scheduledStart.withZoneSameInstant(zone);
        }

        @Override
        public ZonedDateTime getRunStart(final ZoneId zone) {
            if (runStart == null) {
                return null;
            }

            return runStart.withZoneSameInstant(zone);
        }

        @Override
        public ZonedDateTime getRunEnd(final ZoneId zone) {
            if (runEnd == null) {
                return null;
            }

            return runEnd.withZoneSameInstant(zone);
        }

        @Override
        public String toString() {
            return "LastExecutionImpl{" +
                (identityName != null ? "identityName='" + identityName + "', " : "") +
                "result=" + result +
                ", scheduledStart=" + scheduledStart +
                ", runStart=" + runStart +
                ", runEnd=" + runEnd +
                '}';
        }
    }

}
