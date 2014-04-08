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
import org.apache.openejb.util.ExecutorBuilder;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private int threadCount = 3;

    /**
     * Mock support for property: org.quartz.threadPool.threadPriority
     */
    private int threadPriority = Thread.NORM_PRIORITY;

    private final Object threadAvailableLock = new Object();

    private final boolean threadPoolExecutorUsed;

    public DefaultTimerThreadPoolAdapter() {
        final TimerExecutor timerExecutor = SystemInstance.get().getComponent(TimerExecutor.class);

        if (timerExecutor != null) {
            this.executor = timerExecutor.executor;
        } else {
            this.executor = new ExecutorBuilder()
                    .size(3)
                    .prefix("EjbTimerPool")
                    .build(SystemInstance.get().getOptions());

            SystemInstance.get().setComponent(TimerExecutor.class, new TimerExecutor(this.executor));
        }

        this.threadPoolExecutorUsed = (this.executor instanceof ThreadPoolExecutor);

        if (!this.threadPoolExecutorUsed) {
            logger.warning("Unrecognized ThreadPool implementation [" + this.executor.getClass().getName() + "] is used, EJB Timer service may not work correctly");
        }
    }

    // This is to prevent other parts of the code becoming dependent
    // on the executor produced for EJB Timers
    //
    // If we want to share an Executor across the whole system
    // for @Asynchronous and @Remote execution we should design
    // that specifically and have it explicitly created somewhere
    public final static class TimerExecutor {
        private final Executor executor;

        private TimerExecutor(Executor executor) {
            if (executor == null) throw new IllegalArgumentException("executor cannot be null");
            this.executor = executor;
        }
    }

    @Override
    public int blockForAvailableThreads() {
        if (this.threadPoolExecutorUsed) {
            final ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) this.executor;
            synchronized (this.threadAvailableLock) {
                while ((threadPoolExecutor.getMaximumPoolSize() - threadPoolExecutor.getActiveCount()) < 1 && !threadPoolExecutor.isShutdown()) {
                    try {
                        this.threadAvailableLock.wait(500L);
                    } catch (InterruptedException ignore) {
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
    public void initialize() throws SchedulerConfigException {
    }

    @Override
    public boolean runInThread(final Runnable runnable) {
        try {
            this.executor.execute(runnable);
            return true;
        } catch (RejectedExecutionException e) {
            logger.error("Failed to execute timer task", e);
            return false;
        }
    }

    @Override
    public void shutdown(final boolean arg0) {
        if (threadPoolExecutorUsed) {
            final ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
            tpe.shutdown();
            if (arg0) {
                int timeout = SystemInstance.get().getOptions().get(OPENEJB_EJB_TIMER_POOL_AWAIT_SECONDS, 5);
                try {
                    tpe.awaitTermination(timeout, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
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
