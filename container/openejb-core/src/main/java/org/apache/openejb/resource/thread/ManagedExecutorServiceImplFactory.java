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

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.threads.impl.ContextServiceImpl;
import org.apache.openejb.threads.impl.ManagedExecutorServiceImpl;
import org.apache.openejb.threads.impl.ManagedThreadFactoryImpl;
import org.apache.openejb.threads.reject.CURejectHandler;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.enterprise.concurrent.ManagedThreadFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class ManagedExecutorServiceImplFactory {
    private int core = 5;
    private int max = 25;
    private Duration keepAlive = new Duration("5 second");
    private int queue = 15;
    private String threadFactory;

    private String context;

    public ManagedExecutorServiceImpl create() {
        return new ManagedExecutorServiceImpl(createExecutorService(), findContextService());
    }

    private ExecutorService createExecutorService() {
        final BlockingQueue<Runnable> blockingQueue;
        if (queue <= 0) {
            blockingQueue = new LinkedBlockingQueue<>();
        } else {
            blockingQueue = new ArrayBlockingQueue<>(queue);
        }

        ManagedThreadFactory managedThreadFactory;
        try {
            managedThreadFactory = "org.apache.openejb.threads.impl.ManagedThreadFactoryImpl".equals(threadFactory) ?
                    new ManagedThreadFactoryImpl() :
                    ThreadFactories.findThreadFactory(threadFactory);
        } catch (final Exception e) {
            Logger.getInstance(LogCategory.OPENEJB, ManagedExecutorServiceImplFactory.class).warning("Can't create configured thread factory: " + threadFactory, e);
            managedThreadFactory = new ManagedThreadFactoryImpl();
        }

        return new ThreadPoolExecutor(core, max, keepAlive.getTime(), keepAlive.getUnit(), blockingQueue, managedThreadFactory, CURejectHandler.INSTANCE);
    }

    private ContextServiceImpl findContextService() {
        if (context == null || context.trim().isEmpty()) {
            throw new IllegalArgumentException("Please specify a context service to be used with the managed executor");
        }

        try {
            final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            final Context context = containerSystem.getJNDIContext();
            final Object obj = context.lookup("openejb/Resource/" + this.context);
            if (!(obj instanceof ContextServiceImpl)) {
                throw new IllegalArgumentException("Resource with id " + context
                        + " is not a ContextService, but is " + obj.getClass().getName());
            }
            return (ContextServiceImpl) obj;
        } catch (final NamingException e) {
            throw new IllegalArgumentException("Unknown context service " + context);
        }
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

    public void setQueue(final int queue) {
        this.queue = queue;
    }

    public void setThreadFactory(final String threadFactory) {
        this.threadFactory = threadFactory;
    }

    public String getContext() {
        return context;
    }

    public void setContext(final String context) {
        this.context = context;
    }
}
