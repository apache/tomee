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

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * Reflection-based helper for Java 21+ virtual thread APIs.
 * All methods use reflection to avoid compile-time dependency on Java 21.
 * On Java 17, {@link #isSupported()} returns {@code false} and the creation
 * methods throw {@link UnsupportedOperationException}.
 */
public final class VirtualThreadHelper {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, VirtualThreadHelper.class);

    private static final boolean SUPPORTED;
    private static final Method OF_VIRTUAL;
    private static final Method BUILDER_NAME;
    private static final Method BUILDER_FACTORY;
    private static final Method BUILDER_UNSTARTED;
    private static final Method EXECUTORS_NEW_THREAD_PER_TASK;

    static {
        boolean supported = false;
        Method ofVirtual = null;
        Method builderName = null;
        Method builderFactory = null;
        Method builderUnstarted = null;
        Method executorsNewThreadPerTask = null;

        try {
            ofVirtual = Thread.class.getMethod("ofVirtual");
            final Object builder = ofVirtual.invoke(null);

            // Use the public interface Thread.Builder (not the internal impl class)
            // to look up methods — avoids module access issues
            final Class<?> builderInterface = Class.forName("java.lang.Thread$Builder");
            final Class<?> ofVirtualInterface = Class.forName("java.lang.Thread$Builder$OfVirtual");

            // Thread.Builder.OfVirtual.name(String, long) — declared on Builder
            builderName = builderInterface.getMethod("name", String.class, long.class);
            // Thread.Builder.factory()
            builderFactory = builderInterface.getMethod("factory");
            // Thread.Builder.unstarted(Runnable)
            builderUnstarted = builderInterface.getMethod("unstarted", Runnable.class);

            // Executors.newThreadPerTaskExecutor(ThreadFactory)
            executorsNewThreadPerTask = java.util.concurrent.Executors.class
                    .getMethod("newThreadPerTaskExecutor", ThreadFactory.class);

            supported = true;
            LOGGER.info("Virtual thread support detected (Java 21+)");
        } catch (final ReflectiveOperationException | SecurityException e) {
            LOGGER.debug("Virtual threads not available: " + e.getMessage());
        }

        SUPPORTED = supported;
        OF_VIRTUAL = ofVirtual;
        BUILDER_NAME = builderName;
        BUILDER_FACTORY = builderFactory;
        BUILDER_UNSTARTED = builderUnstarted;
        EXECUTORS_NEW_THREAD_PER_TASK = executorsNewThreadPerTask;
    }

    private VirtualThreadHelper() {
        // utility
    }

    /**
     * Returns {@code true} if virtual threads are available (Java 21+).
     */
    public static boolean isSupported() {
        return SUPPORTED;
    }

    /**
     * Creates an unstarted virtual thread with the given name prefix and task.
     *
     * @throws UnsupportedOperationException if virtual threads are not available
     */
    public static Thread newVirtualThread(final String namePrefix, final long index, final Runnable task) {
        if (!SUPPORTED) {
            throw new UnsupportedOperationException("Virtual threads require Java 21+");
        }

        try {
            final Object builder = OF_VIRTUAL.invoke(null);
            final Object namedBuilder = BUILDER_NAME.invoke(builder, namePrefix, index);
            return (Thread) BUILDER_UNSTARTED.invoke(namedBuilder, task);
        } catch (final ReflectiveOperationException e) {
            throw new UnsupportedOperationException("Failed to create virtual thread", e);
        }
    }

    /**
     * Creates a {@link ThreadFactory} that produces virtual threads with the given name prefix.
     *
     * @throws UnsupportedOperationException if virtual threads are not available
     */
    public static ThreadFactory newVirtualThreadFactory(final String namePrefix) {
        if (!SUPPORTED) {
            throw new UnsupportedOperationException("Virtual threads require Java 21+");
        }

        try {
            final Object builder = OF_VIRTUAL.invoke(null);
            final Object namedBuilder = BUILDER_NAME.invoke(builder, namePrefix, 0L);
            return (ThreadFactory) BUILDER_FACTORY.invoke(namedBuilder);
        } catch (final ReflectiveOperationException e) {
            throw new UnsupportedOperationException("Failed to create virtual thread factory", e);
        }
    }

    /**
     * Creates a thread-per-task executor backed by virtual threads.
     *
     * @throws UnsupportedOperationException if virtual threads are not available
     */
    public static ExecutorService newVirtualThreadPerTaskExecutor(final ThreadFactory factory) {
        if (!SUPPORTED) {
            throw new UnsupportedOperationException("Virtual threads require Java 21+");
        }

        try {
            return (ExecutorService) EXECUTORS_NEW_THREAD_PER_TASK.invoke(null, factory);
        } catch (final ReflectiveOperationException e) {
            throw new UnsupportedOperationException("Failed to create virtual thread executor", e);
        }
    }
}
