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

package org.apache.openejb.core.timer;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.quartz.SchedulerConfigException;
import org.apache.openejb.quartz.spi.ThreadPool;
import org.apache.openejb.util.ExecutorBuilder;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version $Rev$ $Date$
 */
@SuppressWarnings("UnusedDeclaration")
public class DefaultTimerThreadPoolAdapter implements ThreadPool {

    private static final Logger logger = Logger.getInstance(LogCategory.TIMER, "org.apache.openejb.util.resources");

    public static final String OPENEJB_TIMER_POOL_SIZE = "openejb.timer.pool.size";
    public static final String OPENEJB_EJB_TIMER_POOL_AWAIT_SECONDS = "openejb.ejb-timer-pool.shutdown.timeout";

    private Executor executor;

    private String instanceId;

    private String instanceName;

    /**
     * Mock support for property: org.quartz.threadPool.threadCount
     */
    private int threadCount = Integer.parseInt(SystemInstance.get().getProperty(OPENEJB_TIMER_POOL_SIZE, "3"));

    /**
     * Mock support for property: org.quartz.threadPool.threadPriority
     */
    private int threadPriority = Thread.NORM_PRIORITY;

    private final Object threadAvailableLock = new Object();

    private boolean threadPoolExecutorUsed;

    // This is to prevent other parts of the code becoming dependent
    // on the executor produced for EJB Timers
    //
    // If we want to share an Executor across the whole system
    // for @Asynchronous and @Remote execution we should design
    // that specifically and have it explicitly created somewhere
    public static final class TimerExecutor {
        private final Executor executor;
        private final AtomicInteger references = new AtomicInteger(0);

        private TimerExecutor(final Executor executor) {
            if (executor == null) {
                throw new IllegalArgumentException("executor cannot be null");
            }
            this.executor = executor;
        }

        public TimerExecutor incr() {
            references.incrementAndGet();
            return this;
        }

        public boolean decr() {
            return references.decrementAndGet() == 0;
        }
    }

    @Override
    public int blockForAvailableThreads() {
        if (this.threadPoolExecutorUsed) {
            final ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) this.executor;
            synchronized (this.threadAvailableLock) {
                while (threadPoolExecutor.getMaximumPoolSize() - threadPoolExecutor.getActiveCount() < 1 && !threadPoolExecutor.isShutdown()) {
                    try {
                        this.threadAvailableLock.wait(500L);
                    } catch (final InterruptedException ignore) {
                        // no-op
                    }
                }
                return threadPoolExecutor.getMaximumPoolSize() - threadPoolExecutor.getActiveCount();
            }
        } else {
            return 1;
        }
    }

    @Override
    public void setInstanceId(final String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public void setInstanceName(final String instanceName) {
        this.instanceName = instanceName;
    }

    public String getInstanceId() {
        return this.instanceId;
    }

    public String getInstanceName() {
        return this.instanceName;
    }

    @Override
    public int getPoolSize() {
        if (this.threadPoolExecutorUsed) {
            return ((ThreadPoolExecutor) this.executor).getPoolSize();
        } else {
            return 1;
        }
    }

    @Override
    public synchronized void initialize() throws SchedulerConfigException {
        final TimerExecutor timerExecutor = SystemInstance.get().getComponent(TimerExecutor.class);

        if (timerExecutor != null) {
            this.executor = timerExecutor.incr().executor;
        } else {
            this.executor = new ExecutorBuilder()
                .size(threadCount)
                .prefix("EjbTimerPool")
                .build(SystemInstance.get().getOptions());

            final TimerExecutor value = new TimerExecutor(this.executor).incr();
            SystemInstance.get().setComponent(TimerExecutor.class, value);
        }

        this.threadPoolExecutorUsed = this.executor instanceof ThreadPoolExecutor;

        if (!this.threadPoolExecutorUsed) {
            logger.warning("Unrecognized ThreadPool implementation [" + this.executor.getClass().getName() + "] is used, EJB Timer service may not work correctly");
        }
    }

    @Override
    public boolean runInThread(final Runnable runnable) {
        try {
            this.executor.execute(runnable);
            return true;
        } catch (final RejectedExecutionException e) {
            logger.error("Failed to execute timer task", e);
            return false;
        }
    }

    @Override
    public synchronized void shutdown(final boolean waitForJobsToComplete) {
        if (threadPoolExecutorUsed) {
            final SystemInstance systemInstance = SystemInstance.get();
            final TimerExecutor te = systemInstance.getComponent(TimerExecutor.class);
            if (te != null) {
                if (te.executor == executor) {
                    if (te.decr()) {
                        doShutdownExecutor(waitForJobsToComplete);
                        systemInstance.removeComponent(TimerExecutor.class);
                    } else { // flush jobs, maybe not all dedicated to this threadpool if shared but shouldn't be an issue
                        final ThreadPoolExecutor tpe = ThreadPoolExecutor.class.cast(executor);
                        if (waitForJobsToComplete) {
                            final Collection<Runnable> jobs = new ArrayList<>();
                            tpe.getQueue().drainTo(jobs);
                            for (final Runnable r : jobs) {
                                try {
                                    r.run();
                                } catch (final Exception e) {
                                    logger.warning(e.getMessage(), e);
                                }
                            }
                        }
                    }
                } else {
                    doShutdownExecutor(waitForJobsToComplete);
                }
            } else {
                doShutdownExecutor(waitForJobsToComplete);
            }
        }
    }

    private void doShutdownExecutor(final boolean waitJobs) {
        final ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
        tpe.shutdown();
        if (waitJobs) {
            final int timeout = SystemInstance.get().getOptions().get(OPENEJB_EJB_TIMER_POOL_AWAIT_SECONDS, 5);
            try {
                tpe.awaitTermination(timeout, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public int getThreadCount() {
        return this.threadCount;
    }

    public void setThreadCount(final int threadCount) {
        this.threadCount = threadCount;
    }

    public int getThreadPriority() {
        return this.threadPriority;
    }

    public void setThreadPriority(final int threadPriority) {
        this.threadPriority = threadPriority;
    }

}
