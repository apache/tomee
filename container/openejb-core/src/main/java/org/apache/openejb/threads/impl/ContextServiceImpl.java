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

import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedTask;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.resource.thread.ManagedExecutorServiceImplFactory;
import org.apache.openejb.threads.future.CUCompletableFuture;
import org.apache.openejb.threads.task.CUBiConsumer;
import org.apache.openejb.threads.task.CUBiFunction;
import org.apache.openejb.threads.task.CUCallable;
import org.apache.openejb.threads.task.CUConsumer;
import org.apache.openejb.threads.task.CUFunction;
import org.apache.openejb.threads.task.CURunnable;
import org.apache.openejb.threads.task.CUSupplier;
import org.apache.openejb.threads.task.CUTask;

import javax.naming.NamingException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ContextServiceImpl implements ContextService, Serializable {
    private final List<ThreadContextProvider> propagated;
    private final List<ThreadContextProvider> cleared;
    private final List<ThreadContextProvider> unchanged;

    // TODO is lost after serialization, probably need to store some reference that can be resolved again after deserializing
    private transient ManagedExecutorService mes;

    public ContextServiceImpl(List<ThreadContextProvider> propagated, List<ThreadContextProvider> cleared, List<ThreadContextProvider> unchanged) {
        this.propagated = propagated;
        this.cleared = cleared;
        this.unchanged = unchanged;
    }

    public ContextServiceImpl(ContextServiceImpl other, ManagedExecutorService mes) {
        this(other.propagated, other.cleared, other.unchanged);
        this.mes = mes;
    }

    @Override
    public <R> Callable<R> contextualCallable(final Callable<R> callable) {
        return new CUCallable<>(callable, this);
    }

    @Override
    public <T, U> BiConsumer<T, U> contextualConsumer(final BiConsumer<T, U> biConsumer) {
        return new CUBiConsumer<>(biConsumer, this);
    }

    @Override
    public <T> Consumer<T> contextualConsumer(final Consumer<T> consumer) {
        return new CUConsumer<>(consumer, this);
    }

    @Override
    public <T, U, R> BiFunction<T, U, R> contextualFunction(final BiFunction<T, U, R> biFunction) {
        return new CUBiFunction<>(biFunction, this);
    }

    @Override
    public <T, R> Function<T, R> contextualFunction(final Function<T, R> function) {
        return new CUFunction<>(function, this);
    }

    @Override
    public Runnable contextualRunnable(final Runnable runnable) {
        return new CURunnable(runnable, this);
    }

    @Override
    public <R> Supplier<R> contextualSupplier(final Supplier<R> supplier) {
        return new CUSupplier<>(supplier, this);
    }

    @Override
    public <T> T createContextualProxy(final T instance, final Class<T> intf) {
        return intf.cast(createContextualProxy(instance, new Class<?>[]{intf}));
    }

    @Override
    public Object createContextualProxy(final Object instance, final Class<?>... interfaces) {
        return createContextualProxy(instance, Map.of(), interfaces);
    }

    @Override
    public <T> T createContextualProxy(final T instance, final Map<String, String> executionProperties, final Class<T> intf) {
        return intf.cast(createContextualProxy(instance, executionProperties, new Class<?>[]{intf}));
    }

    @Override
    public Object createContextualProxy(final Object instance, final Map<String, String> executionProperties, final Class<?>... interfaces) {
        if (instance == null) {
            throw new IllegalArgumentException("Cannot create contextual proxy, instance is null");
        }

        for (Class<?> intf : interfaces) {
            if (!intf.isInstance(instance)) {
                throw new IllegalArgumentException("Cannot create contextual proxy, instance is not an instance of " + intf.getName());
            }
        }

        return Proxy.newProxyInstance(instance.getClass().getClassLoader(), interfaces, new CUHandler(instance, executionProperties, this));
    }

    @Override
    public Executor currentContextExecutor() {
        return command -> contextualRunnable(command).run();
    }

    @Override
    public Map<String, String> getExecutionProperties(final Object contextualProxy) {
        return CUHandler.class.cast(Proxy.getInvocationHandler(contextualProxy)).properties;
    }

    @Override
    public <T> CompletableFuture<T> withContextCapture(final CompletableFuture<T> completableFuture) {
        return copyInternal(completableFuture);
    }

    @Override
    public <T> CompletionStage<T> withContextCapture(final CompletionStage<T> completionStage) {
        return copyInternal(completionStage);
    }

    public Snapshot snapshot(final Map<String, String> props) {
        final List<ThreadContextSnapshot> snapshots = new ArrayList<>();

        // application context needs to be applied first

        boolean appContextPropagated;
        ThreadContextProvider appContext = find(ContextServiceDefinition.APPLICATION, propagated);
        if (appContext != null) {
            appContextPropagated = true;
        } else {
            appContext = find(ContextServiceDefinition.APPLICATION, cleared);
            appContextPropagated = false;
        }

        if (appContext != null) {
            if (appContextPropagated) {
                snapshots.add(appContext.currentContext(props));
            } else {
                snapshots.add(appContext.clearedContext(props));
            }
        }

        for (ThreadContextProvider threadContextProvider : propagated) {
            if (ContextServiceDefinition.APPLICATION.equals(threadContextProvider.getThreadContextType()))
                continue;

            final ThreadContextSnapshot snapshot = threadContextProvider.currentContext(props);
            snapshots.add(snapshot);
        }

        for (ThreadContextProvider threadContextProvider : cleared) {
            if (ContextServiceDefinition.APPLICATION.equals(threadContextProvider.getThreadContextType()))
                continue;

            final ThreadContextSnapshot snapshot = threadContextProvider.clearedContext(props);
            snapshots.add(snapshot);
        }

        return new Snapshot(snapshots);
    }

    private ThreadContextProvider find(final String name, final List<ThreadContextProvider> threadContextProviders) {
        for (final ThreadContextProvider threadContextProvider : threadContextProviders) {
            if (name.equals(threadContextProvider.getThreadContextType())) {
                return threadContextProvider;
            }
        }

        return null;
    }

    public State enter(final Snapshot snapshot) {

        final List<ThreadContextRestorer> restorers = new ArrayList<>();

        for (ThreadContextSnapshot tcs : snapshot.snapshots()) {
            try {
                restorers.add(0, tcs.begin());
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        return new State(restorers);
    }

    public void exit(final State state) {
        if (state != null) {
            final List<ThreadContextRestorer> restorers = state.restorers();
            for (ThreadContextRestorer restorer : restorers) {
                restorer.endContext();
            }
        }
    }

    private <U> CompletableFuture<U> copyInternal(CompletionStage<U> future) {
        final CUCompletableFuture<U> managedFuture = new CUCompletableFuture<>(getManagedExecutorService(), this);
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                managedFuture.complete(result);
            } else {
                managedFuture.completeExceptionally(exception);
            }
        });
        return managedFuture;
    }

    protected ManagedExecutorService getManagedExecutorService() {
        if (mes == null) {
            try {
                ManagedExecutorServiceImpl defaultMes = ManagedExecutorServiceImplFactory.lookup("java:comp/DefaultManagedExecutorService");
                mes = new ManagedExecutorServiceImpl(defaultMes.getDelegate(), this);
            } catch (NamingException e) {
                throw new OpenEJBRuntimeException(e);
            }
        }

        return mes;
    }

    private final static class CUHandler extends CUTask<Object> implements InvocationHandler, Serializable {
        private final Object instance;
        private final Map<String, String> properties;

        private CUHandler(final Object instance, final Map<String, String> props, ContextServiceImpl contextService) {
            super(instance, reconfigureContextService(contextService, props), props);

            this.instance = instance;
            this.properties = props;
        }

        private static ContextServiceImpl reconfigureContextService(ContextServiceImpl contextService, Map<String, String> props) {
            if (props == null || !props.containsKey(ManagedTask.TRANSACTION)) {
                return contextService;
            }

            ArrayList<ThreadContextProvider> propagated = new ArrayList<>(contextService.propagated);
            ArrayList<ThreadContextProvider> cleared = new ArrayList<>(contextService.cleared);
            ArrayList<ThreadContextProvider> unchanged = new ArrayList<>(contextService.unchanged);

            if (ManagedTask.SUSPEND.equals(props.get(ManagedTask.TRANSACTION))) {
                if (!cleared.contains(TxThreadContextProvider.INSTANCE)) {
                    cleared.add(TxThreadContextProvider.INSTANCE);
                }

                propagated.remove(TxThreadContextProvider.INSTANCE);
                unchanged.remove(TxThreadContextProvider.INSTANCE);
            }

            if (ManagedTask.USE_TRANSACTION_OF_EXECUTION_THREAD.equals(props.get(ManagedTask.TRANSACTION))) {
                if (!propagated.contains(TxThreadContextProvider.INSTANCE)) {
                    propagated.add(TxThreadContextProvider.INSTANCE);
                }

                cleared.remove(TxThreadContextProvider.INSTANCE);
                unchanged.remove(TxThreadContextProvider.INSTANCE);
            }

            return new ContextServiceImpl(
                    new ArrayList<>(propagated),
                    new ArrayList<>(cleared),
                    new ArrayList<>(unchanged));
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }

            return invoke(() -> method.invoke(instance, args));
        }
    }

    // Not serializable by design because e.g. ApplicationThreadContextProvider holds
    // a ClassLoader to restore which is not serializable
    public record State(List<ThreadContextRestorer> restorers) { }
    public record Snapshot(List<ThreadContextSnapshot> snapshots) implements Serializable { }
}
