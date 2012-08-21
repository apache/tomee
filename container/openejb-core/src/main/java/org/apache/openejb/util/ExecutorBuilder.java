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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.util;

import org.apache.openejb.loader.Options;
import org.apache.openejb.util.executor.OfferRejectedExecutionHandler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
public class ExecutorBuilder {

    private int size = 10;
    private String prefix = "Pool";
    private ThreadFactory threadFactory;
    private RejectedExecutionHandler rejectedExecutionHandler;

    public ExecutorBuilder() {
    }

    public ExecutorBuilder size(int size) {
        this.size = size;
        return this;
    }

    public ExecutorBuilder prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public ExecutorBuilder threadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    public ExecutorBuilder rejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
        this.rejectedExecutionHandler = rejectedExecutionHandler;
        return this;
    }

    public ThreadPoolExecutor build(Options options) {
        final int corePoolSize = options.get(prefix + ".CorePoolSize", size);

        // Default setting is for a fixed pool size, MaximumPoolSize==CorePoolSize
        final int maximumPoolSize = Math.max(options.get(prefix + ".MaximumPoolSize", corePoolSize), corePoolSize);

        // Default QueueSize is bounded using the MaximumPoolSize
        final int size = options.get(prefix + ".QueueSize", maximumPoolSize);

        // Keep Threads inactive threads alive for 60 seconds by default
        final Duration keepAliveTime = options.get(prefix + ".KeepAliveTime", new Duration(60, TimeUnit.SECONDS));

        // All threads can be timed out by default
        final boolean allowCoreThreadTimeout = options.get(prefix + ".AllowCoreThreadTimeOut", true);

        // If the user explicitly set the QueueSize to 0, we default QueueType to SYNCHRONOUS
        final QueueType defaultQueueType = (size == 0) ? QueueType.SYNCHRONOUS : QueueType.LINKED;
        final BlockingQueue queue = options.get(prefix + ".QueueType", defaultQueueType).create(options, prefix, size);

        ThreadFactory factory = this.threadFactory;
        if (factory == null) {
            factory = new DaemonThreadFactory(prefix);
        }

        RejectedExecutionHandler handler = this.rejectedExecutionHandler;
        if (handler == null) {
            final Duration duration = options.get(prefix + ".OfferTimeout", new Duration(30, TimeUnit.SECONDS));
            handler = new OfferRejectedExecutionHandler(duration);
        }

        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize
                , maximumPoolSize
                , keepAliveTime.getTime()
                , keepAliveTime.getUnit() != null ? keepAliveTime.getUnit() : TimeUnit.SECONDS
                , queue
                , factory
                , handler
        );

        threadPoolExecutor.allowCoreThreadTimeOut(allowCoreThreadTimeout);

        return threadPoolExecutor;
    }

    /**
     * @version $Rev$ $Date$
     */
    public static enum QueueType {
        ARRAY,
        LINKED,
        PRIORITY,
        SYNCHRONOUS;

        public BlockingQueue create(Options options, final String prefix, final int queueSize) {
            switch (this) {
                case ARRAY: {
                    return new ArrayBlockingQueue(queueSize);
                }
                case LINKED: {
                    return new LinkedBlockingQueue(queueSize);
                }
                case PRIORITY: {
                    return new PriorityBlockingQueue();
                }
                case SYNCHRONOUS: {
                    return new SynchronousQueue(options.get(prefix + ".QueueFair", false));
                }
                default: {
                    // The Options class will throw an error if the user supplies an unknown enum string
                    // The only way we can reach this is if we add a new QueueType element and forget to
                    // implement it in the above switch statement.
                    throw new IllegalArgumentException("Unknown QueueType type: " + this);
                }
            }
        }
    }
}
