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

import org.apache.openejb.threads.impl.ManagedScheduledExecutorServiceImpl;

import jakarta.enterprise.concurrent.LastExecution;
import jakarta.enterprise.concurrent.SkippedException;
import jakarta.enterprise.concurrent.Trigger;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class TriggerTask<T> extends CUTask<T> {
    protected final ManagedScheduledExecutorServiceImpl executorService;
    protected final Trigger trigger;
    protected final Date scheduledTime;
    protected final String id;
    protected final AtomicReference<Future<T>> futureRef;

    protected LastExecution lastExecution;
    protected volatile boolean skipped;

    protected volatile boolean done;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private volatile T result;

    protected TriggerTask(final Object original, final ManagedScheduledExecutorServiceImpl es, final Trigger trigger,
                          final Date taskScheduledTime, final String id, final AtomicReference<Future<T>> ref) {
        super(original);
        this.executorService = es;
        this.trigger = trigger;
        this.scheduledTime = taskScheduledTime;
        this.id = id;
        this.futureRef = ref;
    }

    public T invoke() throws Exception {
        return invoke(new Callable<T>() {
            @Override
            public T call() throws Exception {
                final long wait = nextDelay(trigger.getNextRunTime(lastExecution, scheduledTime));
                if (wait > 0) {
                    Thread.sleep(wait);
                } // else if wait < 0 then ??

                final Date now = new Date();
                try {
                    final boolean skip = trigger.skipRun(lastExecution, now);
                    if (!skip) {
                        result = doInvoke();
                        taskDone(future, executor, delegate, null);
                        lastExecution = new LastExecutionImpl(id, result, scheduledTime, now, new Date());
                    } else {
                        result = null;
                        skipped = true;
                        running.set(false);
                    }
                } catch (final RuntimeException re) {
                    final SkippedException skippedException = new SkippedException(re);
                    taskAborted(skippedException);
                    throw skippedException;
                }

                final ScheduledFuture<T> future = executorService.schedule(this, trigger.getNextRunTime(lastExecution, scheduledTime).getTime() - ManagedScheduledExecutorServiceImpl.nowMs(), TimeUnit.MILLISECONDS);
                futureRef.set(future);
                taskSubmitted(future, executorService, delegate);

                return result;
            }
        });
    }

    protected long nextDelay(final Date next) {
        return next.getTime() - ManagedScheduledExecutorServiceImpl.nowMs();
    }

    protected abstract T doInvoke() throws Exception;

    public String getId() {
        return id;
    }

    public void stop() {
        running.set(false);
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

    private static class LastExecutionImpl implements LastExecution {
        private final String identityName;
        private final Object result;
        private final Date scheduledStart;
        private final Date runStart;
        private final Date runEnd;

        public LastExecutionImpl(final String identityName, final Object result, final Date scheduledStart,
                                 final Date runStart, final Date runEnd) {
            this.identityName = identityName;
            this.result = result;
            this.scheduledStart = scheduledStart;
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
        public Date getScheduledStart() {
            return scheduledStart;
        }

        @Override
        public Date getRunStart() {
            return runStart;
        }

        @Override
        public Date getRunEnd() {
            return runEnd;
        }

        @Override
        public String toString() {
            return "LastExecutionImpl{" +
                (identityName != null ? "identityName='" + identityName + "\', " : "") +
                "result=" + result +
                ", scheduledStart=" + scheduledStart +
                ", runStart=" + runStart +
                ", runEnd=" + runEnd +
                '}';
        }
    }

}
