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
import org.apache.openejb.util.DaemonThreadFactory;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
public class DefaultTimerThreadPoolAdapter implements ThreadPool {

    private static final Logger logger = Logger.getInstance(LogCategory.TIMER, "org.apache.openejb.util.resources");

    private Executor executor;

    private String instanceId;

    private String instanceName;

    private final Object threadAvailableLock = new Object();

    private final boolean threadPoolExecutorUsed;

    public DefaultTimerThreadPoolAdapter() {
        this.executor = SystemInstance.get().getComponent(Executor.class);

        if (this.executor == null) {
            final int size = Integer.parseInt(SystemInstance.get().getProperty("openejb.timer.pool.size", "2"));
            this.executor = new ThreadPoolExecutor(2
                    , size
                    , 60L
                    , TimeUnit.SECONDS
                    , new LinkedBlockingQueue<Runnable>(size)
                    , new DaemonThreadFactory(DefaultTimerThreadPoolAdapter.class)
                    , new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(final Runnable r, final ThreadPoolExecutor tpe) {
                    try {
                        tpe.getQueue().put(r);
                    } catch (InterruptedException e) {
                        throw new RejectedExecutionException("Interrupted waiting for executor slot");
                    }
                }
            }
            );
            ((ThreadPoolExecutor) this.executor).allowCoreThreadTimeOut(true);
            SystemInstance.get().setComponent(Executor.class, this.executor);
        }

        this.threadPoolExecutorUsed = (this.executor instanceof ThreadPoolExecutor);

        if (!this.threadPoolExecutorUsed) {
            logger.warning("Unrecognized ThreadPool implementation [" + this.executor.getClass().getName() + "] is used, EJB Timer service may not work correctly");
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
        //TODO Seems we should never try to shutdown the thread pool, as it is shared in global scope
    }

}
