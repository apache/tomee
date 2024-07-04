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

import org.apache.openejb.threads.impl.ContextServiceImpl;
import org.apache.openejb.threads.impl.ContextServiceImplFactory;
import org.apache.openejb.threads.impl.ManagedScheduledExecutorServiceImpl;
import org.apache.openejb.threads.impl.ManagedThreadFactoryImpl;
import org.apache.openejb.threads.reject.CURejectHandler;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.enterprise.concurrent.ManagedThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ManagedScheduledExecutorServiceImplFactory {
    private int core = 5;
    private String threadFactory = ManagedThreadFactoryImpl.class.getName();

    private String context;

    public ManagedScheduledExecutorServiceImpl create(final ContextServiceImpl contextService) {
        return new ManagedScheduledExecutorServiceImpl(createScheduledExecutorService(), contextService);
    }
    public ManagedScheduledExecutorServiceImpl create() {
        return new ManagedScheduledExecutorServiceImpl(createScheduledExecutorService(), ContextServiceImplFactory.lookupOrDefault(context));
    }

    private ScheduledExecutorService createScheduledExecutorService() {
        ManagedThreadFactory managedThreadFactory;
        try {
            managedThreadFactory = ThreadFactories.findThreadFactory(threadFactory);
        } catch (final Exception e) {
            Logger.getInstance(LogCategory.OPENEJB, ManagedScheduledExecutorServiceImplFactory.class).warning("Unable to create configured thread factory: " + threadFactory, e);
            managedThreadFactory = new ManagedThreadFactoryImpl();
        }

        return new ScheduledThreadPoolExecutor(core, managedThreadFactory, CURejectHandler.INSTANCE);
    }

    public void setCore(final int core) {
        this.core = core;
    }

    public void setThreadFactory(final String threadFactory) {
        this.threadFactory = threadFactory;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
