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
package org.apache.openejb.threads.impl;

import jakarta.enterprise.concurrent.ManageableThread;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import org.apache.openejb.threads.task.CURunnable;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;

public class ManagedThreadFactoryImpl implements ManagedThreadFactory {
    private static final AtomicInteger ID = new AtomicInteger();

    private final String prefix;

    public ManagedThreadFactoryImpl() {
        this.prefix = "managed-thread-";
    }

    public ManagedThreadFactoryImpl(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(final Runnable r) {
        final CURunnable wrapper = new CURunnable(r);
        final Thread thread = new ManagedThread(wrapper);
        thread.setDaemon(true);
        thread.setName(prefix + ID.incrementAndGet());
        thread.setContextClassLoader(ManagedThreadFactoryImpl.class.getClassLoader()); // ensure we use container loader as main context classloader to avoid leaks
        return thread;
    }

    @Override
    public ForkJoinWorkerThread newThread(final ForkJoinPool pool) {
        return new ManagedForkJoinWorkerThread(pool);
    }

    public static class ManagedThread extends Thread implements ManageableThread {
        public ManagedThread(final Runnable r) {
            super(r);
        }

        @Override
        public boolean isShutdown() {
            return !isAlive();
        }
    }

    public static class ManagedForkJoinWorkerThread extends ForkJoinWorkerThread {

        /**
         * Creates a ForkJoinWorkerThread operating in the given pool.
         *
         * @param pool the pool this thread works in
         * @throws NullPointerException if pool is null
         */
        protected ManagedForkJoinWorkerThread(ForkJoinPool pool) {
            super(pool);
        }
    }
}
