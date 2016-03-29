/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.jul.handler.rotating;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.LogManager;

// Note: don't touch this class while not needed to avoid to trigger the executor service init
// mainly there to avoid all handlers to have their own threads
class BackgroundTaskRunner {
    private static final ExecutorService EXECUTOR_SERVICE;
    private static final boolean SYNCHRONOUS;

    static {
        final LogManager logManager = LogManager.getLogManager();
        SYNCHRONOUS = Boolean.parseBoolean(getProperty(logManager, BackgroundTaskRunner.class.getName() + ".synchronous"));
        if (SYNCHRONOUS) {
            EXECUTOR_SERVICE = null;
        } else {

            final String threadCount = getProperty(logManager, BackgroundTaskRunner.class.getName() + ".threads");
            final String shutdownTimeoutStr = getProperty(logManager, BackgroundTaskRunner.class.getName() + ".shutdownTimeout");
            final Duration shutdownTimeout = new Duration(shutdownTimeoutStr == null ? "30 seconds" : shutdownTimeoutStr);
            EXECUTOR_SERVICE = Executors.newFixedThreadPool(Integer.parseInt(threadCount == null ? "2" : threadCount), new ThreadFactory() {
                private final ThreadGroup group;
                private final AtomicInteger threadNumber = new AtomicInteger(1);
                private final String namePrefix = "org.apache.tomee.jul.handler.rotating.BackgroundTaskRunner-";

                {
                    final SecurityManager s = System.getSecurityManager();
                    group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
                }

                @Override
                public Thread newThread(final Runnable r) {
                    final Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
                    if (!t.isDaemon()) {
                        t.setDaemon(true);
                    }
                    if (t.getPriority() != Thread.NORM_PRIORITY) {
                        t.setPriority(Thread.NORM_PRIORITY);
                    }
                    return t;
                }
            });

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    EXECUTOR_SERVICE.shutdown();
                    try {
                        EXECUTOR_SERVICE.awaitTermination(shutdownTimeout.asMillis(), TimeUnit.MILLISECONDS);
                    } catch (final InterruptedException e) {
                        Thread.interrupted();
                    }
                }
            });
        }
    }

    private static String getProperty(final LogManager logManager, final String key) {
        final String val = logManager.getProperty(key);
        return val != null ? val : System.getProperty(key);
    }

    static void push(final Runnable runnable) {
        if (SYNCHRONOUS) {
            runnable.run();
        } else {
            EXECUTOR_SERVICE.submit(runnable);
        }
    }
}
