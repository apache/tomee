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
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ivm.ClientSecurity;
import org.apache.openejb.core.security.AbstractSecurityService;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

public abstract class CUTask<T> extends ManagedTaskListenerTask implements Comparable<Object> {
    // TODO: get rid of it as a static thing, make it owned by the executor probably
    private static final SecurityService SECURITY_SERVICE = SystemInstance.get().getComponent(SecurityService.class);

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

    private final Context initialContext;
    private final Object[] containerListenerStates;

    public CUTask(final Object task) {
        super(task);

        Object stateTmp = SECURITY_SERVICE.currentState();
        final boolean associate;
        if (stateTmp == null) {
            stateTmp = ClientSecurity.getIdentity();
            associate = stateTmp != null;
        } else {
            associate = false;
        }
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final AbstractSecurityService.SecurityContext sc = threadContext == null ? null : threadContext.get(AbstractSecurityService.SecurityContext.class);
        if (threadContext != null && threadContext.getBeanContext() != null &&
                (threadContext.getBeanContext().getRunAs() != null || threadContext.getBeanContext().getRunAsUser() != null)) {
            initialContext = new Context(
                    associate, stateTmp,
                    new AbstractSecurityService.SecurityContext(AbstractSecurityService.class.cast(SECURITY_SERVICE).getRunAsSubject(threadContext.getBeanContext())),
                    threadContext, Thread.currentThread().getContextClassLoader(), null);
        } else {
            initialContext = new Context(associate, stateTmp, sc, threadContext, Thread.currentThread().getContextClassLoader(), null);
        }
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
        initialContext.enter();
        final Object[] oldStates;
        if (CONTAINER_LISTENERS.length > 0) {
            oldStates = new Object[CONTAINER_LISTENERS.length];
            for (int i = 0; i < CONTAINER_LISTENERS.length; i++) {
                oldStates[i] = CONTAINER_LISTENERS[i].onStart(containerListenerStates[i]);
            }
        } else {
            oldStates = null;
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
                initialContext.exit();
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

    public static final class Context {
        public static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

        /*
        private static final Class<?>[] THREAD_SCOPES = new Class<?>[] {
                RequestScoped.class, SessionScoped.class, ConversationScoped.class
        };
        */

        private final Object securityServiceState;
        private final ThreadContext threadContext;
        private final ClassLoader loader;
        private final boolean associate;
        private final AbstractSecurityService.SecurityContext securityContext;
        private final Context stack;

        /* propagation of CDI context seems wrong
        private final CdiAppContextsService contextService;
        private final CdiAppContextsService.State cdiState;
        */

        private Context currentContext;
        private Collection<Runnable> exitTasks;

        private Context(final boolean associate, final Object initialSecurityServiceState,
                        final AbstractSecurityService.SecurityContext securityContext, final ThreadContext initialThreadContext,
                        final ClassLoader initialLoader, final Context stack) {
            this.associate = associate;
            this.securityServiceState = initialSecurityServiceState;
            this.securityContext = securityContext;
            this.loader = initialLoader;
            this.stack = stack;
            // copy to ensure we have a thread safe data map
            this.threadContext = initialThreadContext == null ? null : new ThreadContext(initialThreadContext);

            /* propagation of CDI context seems wrong
            final ContextsService genericContextsService = WebBeansContext.currentInstance().getContextsService();
            if (CdiAppContextsService.class.isInstance(genericContextsService)) {
                contextService = CdiAppContextsService.class.cast(genericContextsService);
                cdiState = contextService.saveState();
            } else {
                contextService = null;
                cdiState = null;
            }
            */
        }

        public void enter() {
            final Thread thread = Thread.currentThread();

            final ClassLoader oldCl = thread.getContextClassLoader();
            thread.setContextClassLoader(loader);

            final Object threadState;
            if (associate) {
                //noinspection unchecked
                try {
                    SECURITY_SERVICE.associate(securityServiceState);
                } catch (final LoginException e) {
                    throw new IllegalStateException(e);
                }
                threadState = null;
            } else {
                threadState = SECURITY_SERVICE.currentState();
                SECURITY_SERVICE.setState(securityServiceState);
            }

            final ThreadContext oldCtx;
            if (threadContext != null) { // point A
                final ThreadContext newContext = new ThreadContext(threadContext);
                newContext.set(Context.class, this);
                if (securityContext != null) {
                    newContext.set(AbstractSecurityService.ProvidedSecurityContext.class, new AbstractSecurityService.ProvidedSecurityContext(securityContext));
                }
                oldCtx = ThreadContext.enter(newContext);
            } else {
                oldCtx = null;
            }

            currentContext = new Context(associate, threadState, securityContext, oldCtx, oldCl, this);

            /* propagation of CDI context seems wrong
            if (cdiState != null) {
                contextService.restoreState(cdiState);
            }
            */

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

            if (threadContext != null) { // ensure we use the same condition as point A, see OPENEJB-2109
                try {
                    ThreadContext.exit(currentContext.threadContext);
                } catch (final RuntimeException re) {
                    if (errors == null) {
                        errors = new ArrayList<>();
                    }
                    errors.add(re);
                    Logger.getInstance(LogCategory.OPENEJB, CUTask.class).warning(re.getMessage(), re);
                }
            }

            try {
                if (!associate) {
                    SECURITY_SERVICE.setState(currentContext.securityServiceState);
                } else {
                    SECURITY_SERVICE.disassociate();
                }
            } catch (final RuntimeException re) {
                if (errors == null) {
                    errors = new ArrayList<>();
                }
                errors.add(re);
                Logger.getInstance(LogCategory.OPENEJB, CUTask.class).warning(re.getMessage(), re);
            }

            /* propagation of CDI context seems wrong
            if (currentContext.cdiState != null) {
                contextService.restoreState(currentContext.cdiState);
                contextService.removeThreadLocals();
            }
            */

            Thread.currentThread().setContextClassLoader(currentContext.loader);
            if (currentContext.stack == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(currentContext.stack);
            }
            currentContext = null;

            if (errors != null) {
                if (errors.size() == 1) {
                    throw errors.iterator().next();
                }
                throw new OpenEJBRuntimeException(Join.join("\n", Throwable::getMessage, errors));
            }
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
