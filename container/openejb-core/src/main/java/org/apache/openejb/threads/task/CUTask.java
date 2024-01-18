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
package org.apache.openejb.threads.task;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.threads.impl.ContextServiceImpl;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

public abstract class CUTask<T> extends ManagedTaskListenerTask implements Comparable<Object> {

    // only updated in container startup phase, no concurrency possible, don't use it at runtime!
    private static volatile ContainerListener[] CONTAINER_LISTENERS = new ContainerListener[0];

    public static void addContainerListener(final ContainerListener cl) {
        final ContainerListener[] array = new ContainerListener[CONTAINER_LISTENERS.length + 1];
        if (CONTAINER_LISTENERS.length > 0) {
            System.arraycopy(CONTAINER_LISTENERS, 0, array, 0, CONTAINER_LISTENERS.length);
        }
        array[CONTAINER_LISTENERS.length] = cl;
        CONTAINER_LISTENERS = array;
    }

    private final ContextServiceImpl contextService;
    private final ContextServiceImpl.Snapshot snapshot;
    private final Object[] containerListenerStates;

    public CUTask(final Object task, final ContextServiceImpl contextService) {
        super(task);
        this.contextService = contextService;

        snapshot = contextService.snapshot(null);

        if (CONTAINER_LISTENERS.length > 0) {
            containerListenerStates = new Object[CONTAINER_LISTENERS.length];
            for (int i = 0; i < CONTAINER_LISTENERS.length; i++) {
                containerListenerStates[i] = CONTAINER_LISTENERS[i].onCreation();
            }
        } else {
            containerListenerStates = null;
        }
    }

    protected T invoke(final Callable<T> call) throws Exception {
        final Object[] oldStates;
        if (CONTAINER_LISTENERS.length > 0) {
            oldStates = new Object[CONTAINER_LISTENERS.length];
            for (int i = 0; i < CONTAINER_LISTENERS.length; i++) {
                oldStates[i] = CONTAINER_LISTENERS[i].onStart(containerListenerStates[i]);
            }
        } else {
            oldStates = null;
        }

        ContextServiceImpl.State state = null;

        if (contextService != null && snapshot != null) {
            state = contextService.enter(snapshot);
        }


        Throwable throwable = null;
        try {
            taskStarting(future, executor, delegate); // do it in try to avoid issues if an exception is thrown
            return call.call();
        } catch (final Throwable t) {
            throwable = t;
            taskAborted(throwable);
            return rethrow(t);
        } finally {
            try {
                taskDone(future, executor, delegate, throwable);
            } finally {
                if (CONTAINER_LISTENERS.length > 0) {
                    for (int i = 0; i < CONTAINER_LISTENERS.length; i++) {
                        CONTAINER_LISTENERS[i].onEnd(oldStates[i]);
                    }
                }
                if (contextService != null && state != null) {
                    contextService.exit(state);
                }
            }
        }
    }

    private T rethrow(final Throwable t) throws Exception {
        if (Exception.class.isInstance(t)) {
            throw Exception.class.cast(t);
        } else if (Error.class.isInstance(t)) {
            throw Error.class.cast(t);
        }
        throw new OpenEJBRuntimeException(t.getMessage(), t);
    }

    /*
     * As the above is refactored to use ThreadContextProviders to align with the Jakarta EE 10 API,
     * this is really just something that the TomEERealm can push exit tasks to the currently
     * running Context.
     */
    public static final class Context {
        public static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

        private Context previous = null;

        private Collection<Runnable> exitTasks;

        public void enter() {
            if (previous != null) {
                throw new IllegalStateException("Can't enter a context twice, create a new one, and call enter() on that.");
            }

            this.previous = CURRENT.get();
            CURRENT.set(this);
        }

        public void exit() {
            Collection<RuntimeException> errors = null;

            // exit tasks are designed to be in execution added post tasks so execution them before next ones
            // ie inversed ordered compared to init phase
            if (exitTasks != null) {
                for (final Runnable r : exitTasks) {
                    try {
                        r.run();
                    } catch (final RuntimeException re) {
                        if (errors == null) {
                            errors = new ArrayList<>();
                        }
                        errors.add(re);
                        Logger.getInstance(LogCategory.OPENEJB, CUTask.class).warning(re.getMessage(), re);
                    }
                }
            }


            CURRENT.set(previous);
            previous = null;
        }

        public void pushExitTask(final Runnable runnable) {
            if (exitTasks == null) {
                exitTasks = new ArrayList<>(2);
            }
            exitTasks.add(runnable);
        }
    }

    @Override
    public int compareTo(final Object o) {
        return Comparable.class.isInstance(delegate) ? Comparable.class.cast(delegate).compareTo(o) : -1;
    }

    public interface ContainerListener<T> {
        T onCreation();
        T onStart(T state);
        void onEnd(T oldState);
    }
}
