/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.core.timer;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.DaemonThreadFactory;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;

/**
 * @version $Rev$ $Date$
 */
public class DefaultTimerThreadPoolAdapter implements ThreadPool {

    private static final Logger logger = Logger.getInstance(LogCategory.TIMER, "org.apache.openejb.util.resources");

    private Executor executor;

    private String instanceId;

    private String instanceName;

    private final Object threadAvailableLock = new Object();

    private boolean threadPoolExecutorUsed;

    public DefaultTimerThreadPoolAdapter() {
        executor = SystemInstance.get().getComponent(Executor.class);
        if (executor == null) {
            executor = Executors.newFixedThreadPool(10, new DaemonThreadFactory(DefaultTimerThreadPoolAdapter.class));
            SystemInstance.get().setComponent(Executor.class, executor);
        }
        threadPoolExecutorUsed = executor instanceof ThreadPoolExecutor;
        if (!threadPoolExecutorUsed) {
            logger.warning("Unrecognized ThreadPool implementation [" + executor.getClass().getName() + "] is used, EJB Timer service may not work correctly");
        }
    }

//    @Override
    public int blockForAvailableThreads() {
        if (threadPoolExecutorUsed) {
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
            synchronized (threadAvailableLock) {
                while ((threadPoolExecutor.getMaximumPoolSize() - threadPoolExecutor.getActiveCount()) < 1 && !threadPoolExecutor.isShutdown()) {
                    try {
                        threadAvailableLock.wait(500L);
                    } catch (InterruptedException ignore) {
                    }
                }
                return threadPoolExecutor.getMaximumPoolSize() - threadPoolExecutor.getActiveCount();
            }
        } else {
            return 1;
        }
    }

//    @Override
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

//    @Override
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    @Override
    public int getPoolSize() {
        if (threadPoolExecutorUsed) {
            return ((ThreadPoolExecutor) executor).getPoolSize();
        } else {
            return 1;
        }
    }

    @Override
    public void initialize() throws SchedulerConfigException {
    }

    @Override
    public boolean runInThread(Runnable runnable) {
        try {
            executor.execute(runnable);
            return true;
        } catch (RejectedExecutionException e) {
            logger.error("Fail to executor timer task", e);
            return false;
        }
    }

    @Override
    public void shutdown(boolean arg0) {
        //TODO Seems we should never try to shutdown the thread pool, as it is shared in global scope
    }

}
