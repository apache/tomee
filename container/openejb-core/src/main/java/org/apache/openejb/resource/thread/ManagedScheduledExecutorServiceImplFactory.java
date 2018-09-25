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
package org.apache.openejb.resource.thread;

import org.apache.openejb.threads.impl.ManagedScheduledExecutorServiceImpl;
import org.apache.openejb.threads.impl.ManagedThreadFactoryImpl;
import org.apache.openejb.threads.reject.CURejectHandler;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.enterprise.concurrent.ManagedThreadFactory;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ManagedScheduledExecutorServiceImplFactory {
    private int core = 5;
    private int max = 25;
    private Duration keepAlive = new Duration("5 second");
    private String threadFactory = ManagedThreadFactoryImpl.class.getName();

    public ManagedScheduledExecutorServiceImpl create() {
        return new ManagedScheduledExecutorServiceImpl(createScheduledExecutorService());
    }

    private ScheduledExecutorService createScheduledExecutorService() {
        ManagedThreadFactory managedThreadFactory;
        try {
            managedThreadFactory = ThreadFactories.findThreadFactory(threadFactory);
        } catch (final Exception e) {
            Logger.getInstance(LogCategory.OPENEJB, ManagedScheduledExecutorServiceImplFactory.class).warning("Unable to create configured thread factory: " + threadFactory, e);
            managedThreadFactory = new ManagedThreadFactoryImpl();
        }

        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(core, managedThreadFactory, CURejectHandler.INSTANCE);
        if (max < core) {
            max = core;
        }
        scheduledThreadPoolExecutor.setMaximumPoolSize(max);
        scheduledThreadPoolExecutor.setKeepAliveTime(keepAlive.getTime(), keepAlive.getUnit());
        return scheduledThreadPoolExecutor;
    }

    public void setCore(final int core) {
      this.core = core;
  }

  public void setMax(final int max) {
      this.max = max;
  }

  public void setKeepAlive(final Duration keepAlive) {
      this.keepAlive = keepAlive;
  }

  public void setThreadFactory(final String threadFactory) {
      this.threadFactory = threadFactory;
  }
}
