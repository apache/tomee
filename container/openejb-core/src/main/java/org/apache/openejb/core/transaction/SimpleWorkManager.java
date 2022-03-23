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

package org.apache.openejb.core.transaction;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.resource.spi.work.ExecutionContext;
import jakarta.resource.spi.work.Work;
import jakarta.resource.spi.work.WorkAdapter;
import jakarta.resource.spi.work.WorkCompletedException;
import jakarta.resource.spi.work.WorkEvent;
import jakarta.resource.spi.work.WorkException;
import jakarta.resource.spi.work.WorkListener;
import jakarta.resource.spi.work.WorkManager;
import jakarta.resource.spi.work.WorkRejectedException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

public class SimpleWorkManager implements WorkManager {
    public enum WorkType {
        DO, START, SCHEDULE
    }

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, SimpleWorkManager.class);

    /**
     * All work is performed by this executor
     */
    private final Executor executor;

    public SimpleWorkManager(final Executor executor) {
        if (executor == null) {
            throw new NullPointerException("executor is null");
        }
        this.executor = executor;
    }

    public void doWork(final Work work) throws WorkException {
        if (work == null) {
            throw new NullPointerException("work is null");
        }
        doWork(work, INDEFINITE, null, null);
    }

    public void doWork(final Work work, final long startTimeout, final ExecutionContext executionContext, final WorkListener workListener) throws WorkException {
        if (work == null) {
            throw new NullPointerException("work is null");
        }
        executeWork(WorkType.DO, work, startTimeout, executionContext, workListener);
    }

    public long startWork(final Work work) throws WorkException {
        if (work == null) {
            throw new NullPointerException("work is null");
        }
        return startWork(work, INDEFINITE, null, null);
    }

    public long startWork(final Work work, final long startTimeout, final ExecutionContext executionContext, final WorkListener workListener) throws WorkException {
        if (work == null) {
            throw new NullPointerException("work is null");
        }
        return executeWork(WorkType.START, work, startTimeout, executionContext, workListener);
    }

    public void scheduleWork(final Work work) throws WorkException {
        if (work == null) {
            throw new NullPointerException("work is null");
        }
        scheduleWork(work, INDEFINITE, null, null);
    }

    public void scheduleWork(final Work work, final long startTimeout, final ExecutionContext executionContext, final WorkListener workListener) throws WorkException {
        if (work == null) {
            throw new NullPointerException("work is null");
        }
        executeWork(WorkType.SCHEDULE, work, startTimeout, executionContext, workListener);
    }

    private long executeWork(final WorkType workType, final Work work, final long startTimeout, final ExecutionContext executionContext, WorkListener workListener) throws WorkException {
        // assure we have a work listener
        if (workListener == null) {
            workListener = new LoggingWorkListener(workType);
        }

        // reject work with an XID
        if (executionContext != null && executionContext.getXid() != null) {
            final WorkRejectedException workRejectedException = new WorkRejectedException("SimpleWorkManager can not import an XID", WorkException.TX_RECREATE_FAILED);
            workListener.workRejected(new WorkEvent(this, WorkEvent.WORK_REJECTED, work, workRejectedException));
            throw workRejectedException;
        }

        // accecpt all other work
        workListener.workAccepted(new WorkEvent(this, WorkEvent.WORK_ACCEPTED, work, null));

        // execute work
        final Worker worker = new Worker(work, workListener, startTimeout);
        executor.execute(worker);

        if (workType == WorkType.DO) {
            // wait for completion
            try {
                worker.waitForCompletion();
            } catch (final InterruptedException e) {
                final WorkException workException = new WorkException("Work submission thread was interrupted", e);
                workException.setErrorCode(WorkException.INTERNAL);
                throw workException;
            }

            // if work threw an exception, rethrow it
            final WorkException workCompletedException = worker.getWorkException();
            if (workCompletedException != null) {
                throw workCompletedException;
            }
        } else if (workType == WorkType.START) {
            // wait for work to start
            try {
                worker.waitForStart();
            } catch (final InterruptedException e) {
                final WorkException workException = new WorkException("Work submission thread was interrupted", e);
                workException.setErrorCode(WorkException.INTERNAL);
                throw workException;
            }

            // if work threw a rejection exception, rethrow it (it is the exception for timeout) 
            final WorkException workCompletedException = worker.getWorkException();
            if (workCompletedException instanceof WorkRejectedException) {
                throw workCompletedException;
            }
        }

        return worker.getStartDelay();
    }

    private class Worker implements Runnable {
        private final Work work;
        private final WorkListener workListener;
        private final long startTimeout;
        private final long created = System.currentTimeMillis();
        private final CountDownLatch started = new CountDownLatch(1);
        private final CountDownLatch completed = new CountDownLatch(1);
        private long startDelay = UNKNOWN;
        private WorkException workException;

        public Worker(final Work work, final WorkListener workListener, final long startTimeout) {
            this.work = work;
            this.workListener = workListener;
            if (startTimeout <= 0) {
                this.startTimeout = INDEFINITE;
            } else {
                this.startTimeout = startTimeout;
            }
        }

        public void run() {
            try {
                // check if we have started within the specified limit
                startDelay = System.currentTimeMillis() - created;
                if (startDelay > startTimeout) {
                    workException = new WorkRejectedException("Work not started within specified timeout " + startTimeout + "ms", WorkException.START_TIMED_OUT);
                    workListener.workRejected(new WorkEvent(this, WorkEvent.WORK_REJECTED, work, workException, startTimeout));
                    return;
                }

                // notify listener that work has been started
                workListener.workStarted(new WorkEvent(SimpleWorkManager.this, WorkEvent.WORK_STARTED, work, null));

                // officially started
                started.countDown();

                // execute the real work
                workException = null;
                try {
                    work.run();
                } catch (final Throwable e) {
                    workException = new WorkCompletedException(e);
                } finally {
                    // notify listener that work completed (with an optional exception)
                    workListener.workCompleted(new WorkEvent(SimpleWorkManager.this, WorkEvent.WORK_COMPLETED, work, workException));
                }
            } finally {
                // assure that threads waiting for start are released
                started.countDown();

                // Done
                completed.countDown();
            }
        }

        public long getStartDelay() {
            return startDelay;
        }

        public WorkException getWorkException() {
            return workException;
        }

        public void waitForStart() throws InterruptedException {
            started.await();
        }

        public void waitForCompletion() throws InterruptedException {
            completed.await();
        }
    }

    private static final class LoggingWorkListener extends WorkAdapter {
        private final WorkType workType;

        private LoggingWorkListener(final WorkType workType) {
            this.workType = workType;
        }

        public void workRejected(final WorkEvent event) {
            // Don't log doWork or startWork since exception is propagated to caller
            if (workType == WorkType.DO || workType == WorkType.START) {
                return;
            }
            final WorkException exception = event.getException();
            if (exception != null) {
                if (WorkException.START_TIMED_OUT.equals(exception.getErrorCode())) {
                    logger.error(exception.getMessage());
                }
            }
        }

        public void workCompleted(final WorkEvent event) {
            // Don't log doWork since exception is propagated to caller
            if (workType == WorkType.DO) {
                return;
            }

            Throwable cause = event.getException();
            if (cause != null && cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (cause != null) {
                logger.error(event.getWork().toString(), cause);
            }
        }
    }
}
