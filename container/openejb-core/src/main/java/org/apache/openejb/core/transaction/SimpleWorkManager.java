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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkAdapter;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkEvent;
import static javax.resource.spi.work.WorkEvent.WORK_ACCEPTED;
import static javax.resource.spi.work.WorkEvent.WORK_COMPLETED;
import static javax.resource.spi.work.WorkEvent.WORK_REJECTED;
import static javax.resource.spi.work.WorkEvent.WORK_STARTED;
import javax.resource.spi.work.WorkException;
import static javax.resource.spi.work.WorkException.INTERNAL;
import static javax.resource.spi.work.WorkException.START_TIMED_OUT;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkRejectedException;

import static org.apache.openejb.core.transaction.SimpleWorkManager.WorkType.DO;
import static org.apache.openejb.core.transaction.SimpleWorkManager.WorkType.SCHEDULE;
import static org.apache.openejb.core.transaction.SimpleWorkManager.WorkType.START;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

public class SimpleWorkManager implements WorkManager {
    public enum WorkType {
        DO, START, SCHEDULE
    }

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, SimpleWorkManager.class);

    /**
     * All work is performed by this executor
     */
    private Executor executor;

    public SimpleWorkManager(Executor executor) {
        if (executor == null) throw new NullPointerException("executor is null");
        this.executor = executor;
    }

    public void doWork(Work work) throws WorkException {
        if (work == null) throw new NullPointerException("work is null");
        doWork(work, INDEFINITE, null, null);
    }

    public void doWork(Work work, long startTimeout, ExecutionContext executionContext, WorkListener workListener) throws WorkException {
        if (work == null) throw new NullPointerException("work is null");
        executeWork(DO, work, startTimeout, executionContext, workListener);
    }

    public long startWork(Work work) throws WorkException {
        if (work == null) throw new NullPointerException("work is null");
        return startWork(work, INDEFINITE, null, null);
    }

    public long startWork(Work work, long startTimeout, ExecutionContext executionContext, WorkListener workListener) throws WorkException {
        if (work == null) throw new NullPointerException("work is null");
        return executeWork(START, work, startTimeout, executionContext, workListener);
    }

    public void scheduleWork(Work work) throws WorkException {
        if (work == null) throw new NullPointerException("work is null");
        scheduleWork(work, INDEFINITE, null, null);
    }

    public void scheduleWork(Work work, long startTimeout, ExecutionContext executionContext, WorkListener workListener) throws WorkException {
        if (work == null) throw new NullPointerException("work is null");
        executeWork(SCHEDULE, work, startTimeout, executionContext, workListener);
    }

    private long executeWork(WorkType workType, Work work, long startTimeout, ExecutionContext executionContext, WorkListener workListener) throws WorkException {
        // assure we have a work listener
        if (workListener == null) workListener = new LoggingWorkListener(workType);

        // reject work with an XID
        if (executionContext != null && executionContext.getXid() != null) {
            WorkRejectedException workRejectedException = new WorkRejectedException("SimpleWorkManager can not import an XID", WorkException.TX_RECREATE_FAILED);
            workListener.workRejected(new WorkEvent(this, WORK_REJECTED, work, workRejectedException));
            throw workRejectedException;
        }

        // accecpt all other work
        workListener.workAccepted(new WorkEvent(this, WORK_ACCEPTED, work, null));

        // execute work
        Worker worker = new Worker(work, workListener, startTimeout);
        executor.execute(worker);

        if (workType == DO) {
            // wait for completion
            try {
                worker.waitForCompletion();
            } catch (InterruptedException e) {
                WorkException workException = new WorkException("Work submission thread was interrupted", e);
                workException.setErrorCode(INTERNAL);
                throw workException;
            }

            // if work threw an exception, rethrow it
            WorkException workCompletedException = worker.getWorkException();
            if (workCompletedException != null) {
                throw workCompletedException;
            }
        } else if (workType == START) {
            // wait for work to start
            try {
                worker.waitForStart();
            } catch (InterruptedException e) {
                WorkException workException = new WorkException("Work submission thread was interrupted", e);
                workException.setErrorCode(INTERNAL);
                throw workException;
            }

            // if work threw a rejection exception, rethrow it (it is the exception for timeout) 
            WorkException workCompletedException = worker.getWorkException();
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

        public Worker(Work work, WorkListener workListener, long startTimeout) {
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
                    workException = new WorkRejectedException("Work not started within specified timeout " + startTimeout + "ms", START_TIMED_OUT);
                    workListener.workRejected(new WorkEvent(this, WORK_REJECTED, work, workException, startTimeout));
                    return;
                }

                // notify listener that work has been started
                workListener.workStarted(new WorkEvent(SimpleWorkManager.this, WORK_STARTED, work, null));

                // officially started
                started.countDown();

                // execute the real work
                workException = null;
                try {
                    work.run();
                } catch (Throwable e) {
                    workException = new WorkCompletedException(e);
                } finally {
                    // notify listener that work completed (with an optional exception)
                    workListener.workCompleted(new WorkEvent(SimpleWorkManager.this, WORK_COMPLETED, work, workException));
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

    private static class LoggingWorkListener extends WorkAdapter {
        private final WorkType workType;

        private LoggingWorkListener(WorkType workType) {
            this.workType = workType;
        }

        public void workRejected(WorkEvent event) {
            // Don't log doWork or startWork since exception is propagated to caller
            if (workType == DO || workType == START) {
                return;
            }
            WorkException exception = event.getException();
            if (exception != null) {
                if (WorkException.START_TIMED_OUT.equals(exception.getErrorCode())) {
                    logger.error(exception.getMessage());
                }
            }
        }

        public void workCompleted(WorkEvent event) {
            // Don't log doWork since exception is propagated to caller
            if (workType == DO) {
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
