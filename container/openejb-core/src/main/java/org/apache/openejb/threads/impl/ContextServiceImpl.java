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

import jakarta.enterprise.concurrent.spi.ThreadContextProvider;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.threads.task.CUTask;

import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedTask;
import jakarta.transaction.Transaction;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
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

public class ContextServiceImpl implements ContextService {

    private static final HashMap<String, String> EMPTY_PROPS = new HashMap<String, String>();

    private final List<ThreadContextProvider> propagated = new ArrayList<>();
    private final List<ThreadContextProvider> cleared = new ArrayList<>();
    private final List<ThreadContextProvider> unchanged = new ArrayList<>();

    public List<ThreadContextProvider> getPropagated() {
        return propagated;
    }

    public List<ThreadContextProvider> getCleared() {
        return cleared;
    }

    public List<ThreadContextProvider> getUnchanged() {
        return unchanged;
    }

    @Override
    public <R> Callable<R> contextualCallable(final Callable<R> callable) {
        return createContextualProxy(callable, Callable.class);
    }

    @Override
    public <T, U> BiConsumer<T, U> contextualConsumer(final BiConsumer<T, U> biConsumer) {
        return createContextualProxy(biConsumer, BiConsumer.class);
    }

    @Override
    public <T> Consumer<T> contextualConsumer(final Consumer<T> consumer) {
        return createContextualProxy(consumer, Consumer.class);
    }

    @Override
    public <T, U, R> BiFunction<T, U, R> contextualFunction(final BiFunction<T, U, R> biFunction) {
        return createContextualProxy(biFunction, BiFunction.class);
    }

    @Override
    public <T, R> Function<T, R> contextualFunction(final Function<T, R> function) {
        return createContextualProxy(function, Function.class);
    }

    @Override
    public Runnable contextualRunnable(final Runnable runnable) {
        return createContextualProxy(runnable, Runnable.class);
    }

    @Override
    public <R> Supplier<R> contextualSupplier(final Supplier<R> supplier) {
        return createContextualProxy(supplier, Supplier.class);
    }

    @Override
    public <T> T createContextualProxy(final T instance, final Class<T> intf) {
        return intf.cast(createContextualProxy(instance, new Class<?>[]{intf}));
    }

    @Override
    public Object createContextualProxy(final Object instance, final Class<?>... interfaces) {
        return createContextualProxy(instance, EMPTY_PROPS, interfaces);
    }

    @Override
    public <T> T createContextualProxy(final T instance, final Map<String, String> executionProperties, final Class<T> intf) {
        return intf.cast(createContextualProxy(instance, executionProperties, new Class<?>[]{intf}));
    }

    @Override
    public Object createContextualProxy(final Object instance, final Map<String, String> executionProperties, final Class<?>... interfaces) {
        return Proxy.newProxyInstance(instance.getClass().getClassLoader(), interfaces, new CUHandler(instance, executionProperties));
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
        return createContextualProxy(completableFuture, CompletableFuture.class);
    }

    @Override
    public <T> CompletionStage<T> withContextCapture(final CompletionStage<T> completionStage) {
        return createContextualProxy(completionStage, CompletionStage.class);
    }

    private final class CUHandler extends CUTask<Object> implements InvocationHandler, Serializable {
        private final Object instance;
        private final Map<String, String> properties;
        private final boolean suspendTx;

        private CUHandler(final Object instance, final Map<String, String> props) {
            super(instance);
            this.instance = instance;
            this.properties = props;
            this.suspendTx = ManagedTask.SUSPEND.equals(props.get(ManagedTask.TRANSACTION));
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }

            final Transaction suspendedTx;
            if (suspendTx) {
                suspendedTx = OpenEJB.getTransactionManager().suspend();
            } else {
                suspendedTx = null;
            }

            try {
                return invoke(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        return method.invoke(instance, args);
                    }
                });
            } finally {
                if (suspendedTx != null) {
                    OpenEJB.getTransactionManager().resume(suspendedTx);
                }
            }
        }
    }

}
