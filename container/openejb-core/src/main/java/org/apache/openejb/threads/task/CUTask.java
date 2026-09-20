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
import org.apache.openejb.threads.impl.ContextServiceImpl;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class CUTask<T> extends ManagedTaskListenerTask implements Comparable<Object>, Serializable {

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

    protected final ContextServiceImpl contextService;
    private final ContextServiceImpl.Snapshot snapshot;
    private final Object[] containerListenerStates;

    public CUTask(final Object task, final ContextServiceImpl contextService) {
        this(task, contextService, (Map<String, String>) null);
    }

    public CUTask(final Object task, final ContextServiceImpl contextService, Map<String, String> props) {
        this(task, contextService, contextService.snapshot(props));
    }

    /**
     * Uses a snapshot captured earlier, on the thread the context is taken from.
     * {@link ContextService#currentContextExecutor()} captures when the executor is created rather
     * than when a task is submitted to it.
     */
    public CUTask(final Object task, final ContextServiceImpl contextService, final ContextServiceImpl.Snapshot snapshot) {
        super(task);
        this.contextService = contextService;
        this.snapshot = snapshot;
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
        // one per invocation rather than one per task: a contextual proxy runs its task more than
        // once, and may run it on several threads at the same time
        final Context invocationContext = new Context();

        // read once: the array is replaced when a listener is registered, and the teardown below
        // walks the listeners that were started
        final ContainerListener[] listeners = CONTAINER_LISTENERS;
        final Object[] oldStates = listeners.length > 0 ? new Object[listeners.length] : null;
        int started = 0;

        ContextServiceImpl.State state = null;
        boolean entered = false;
        Throwable throwable = null;

        // establishing the context can fail, see TOMEE-4699. Keep it inside the try so that the task
        // listener is notified and the thread is cleaned up in that case as well.
        try {
            invocationContext.enter();
            entered = true;

            for (int i = 0; i < listeners.length; i++) {
                oldStates[i] = listeners[i].onStart(containerListenerStates[i]);
                started = i + 1;
            }

            if (contextService != null && snapshot != null) {
                state = contextService.enter(snapshot);
            }

            taskStarting(future, executor, delegate);
            return call.call();
        } catch (final Throwable t) {
            throwable = t;
            taskAborted(throwable);
            return rethrow(t);
        } finally {
            try {
                taskDone(future, executor, delegate, throwable);
            } finally {
                for (int i = 0; i < started; i++) {
                    listeners[i].onEnd(oldStates[i]);
                }
                if (contextService != null && state != null) {
                    contextService.exit(state);
                }
                if (entered) {
                    invocationContext.exit();
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
    public static final class Context implements Serializable {
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
            // ie inverse order compared to init phase
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

    public interface ContainerListener<T> extends Serializable {
        T onCreation();
        T onStart(T state);
        void onEnd(T oldState);
    }
}
