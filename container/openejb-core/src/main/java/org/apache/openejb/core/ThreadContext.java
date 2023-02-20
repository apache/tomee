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

package org.apache.openejb.core;

import org.apache.openejb.BeanContext;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadContext {

    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
    private static final ThreadLocal<ThreadContext> threadStorage = new ThreadLocal<>();
    private static final CopyOnWriteArraySet<ThreadContextListener> listeners = new CopyOnWriteArraySet<>();
    private static final ThreadLocal<AtomicBoolean> asynchronousCancelled = new ThreadLocal<>();

    public static ThreadContext getThreadContext() {
        return threadStorage.get();
    }

    public static ThreadContext clear() {
        final ThreadContext oldContext = threadStorage.get();
        threadStorage.set(null);
        return oldContext;
    }


    public static ThreadContext enter(final ThreadContext newContext) {
        if (newContext == null) {
            throw new NullPointerException("newContext is null");
        }

        // set the thread context class loader
        final Thread thread = Thread.currentThread();
        newContext.oldClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(newContext.beanContext.getClassLoader());

        // update thread local
        final ThreadContext oldContext = threadStorage.get();
        threadStorage.set(newContext);

        // notify listeners
        for (final ThreadContextListener listener : listeners) {
            try {
                listener.contextEntered(oldContext, newContext);
            } catch (final Throwable e) {
                log.warning("ThreadContextListener threw an exception", e);
            }
        }

        // return old context so it can be used for exit call below
        return oldContext;
    }

    public static void exit(final ThreadContext oldContext) {
        final ThreadContext exitingContext = threadStorage.get();
        if (exitingContext == null) {
            throw new IllegalStateException("No existing context");
        }

        // set the thread context class loader back
        Thread.currentThread().setContextClassLoader(exitingContext.oldClassLoader);
        exitingContext.oldClassLoader = null;

        // update thread local
        threadStorage.set(oldContext);

        // notify listeners
        for (final ThreadContextListener listener : listeners) {
            try {
                listener.contextExited(exitingContext, oldContext);
            } catch (final Throwable e) {
                log.debug("ThreadContextListener threw an exception", e);
            }
        }
    }

    public static void initAsynchronousCancelled(final AtomicBoolean initializeValue) {
        asynchronousCancelled.set(initializeValue);
    }

    public static boolean isAsynchronousCancelled() {
        return asynchronousCancelled.get().get();
    }

    public static void removeAsynchronousCancelled() {
        asynchronousCancelled.remove();
    }

    public static void addThreadContextListener(final ThreadContextListener listener) {
        listeners.add(listener);
    }

    public static void removeThreadContextListener(final ThreadContextListener listener) {
        listeners.remove(listener);
    }

    private final BeanContext beanContext;
    private final Object primaryKey;
    private final Map<Class, Object> data = Collections.synchronizedMap(new HashMap<>());
    private ClassLoader oldClassLoader;
    private Operation currentOperation;
    private Class invokedInterface;
    private TransactionPolicy transactionPolicy;

    /**
     * A boolean which keeps track of whether to discard the bean instance after the method invocation.
     * The boolean would be set to true in case of exceptions which mandate bean discard.
     */
    private boolean discardInstance;

    public ThreadContext(final BeanContext beanContext, final Object primaryKey) {
        this(beanContext, primaryKey, null);
    }

    public ThreadContext(final BeanContext beanContext, final Object primaryKey, final Operation operation) {
        if (beanContext == null) {
            throw new NullPointerException("deploymentInfo is null");
        }
        this.beanContext = beanContext;
        this.primaryKey = primaryKey;
        this.currentOperation = operation;
    }

    public ThreadContext(final ThreadContext that) {
        this.beanContext = that.beanContext;
        this.primaryKey = that.primaryKey;
        this.data.putAll(that.data);
        this.oldClassLoader = that.oldClassLoader;
    }

    public BeanContext getBeanContext() {
        return beanContext;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public Operation getCurrentOperation() {
        return currentOperation;
    }

    public void setCurrentOperation(final Operation operation) {
        currentOperation = operation;
    }

    public Class getInvokedInterface() {
        return invokedInterface;
    }

    public void setInvokedInterface(final Class invokedInterface) {
        this.invokedInterface = invokedInterface;
    }

    public TransactionPolicy getTransactionPolicy() {
        return transactionPolicy;
    }

    public void setTransactionPolicy(final TransactionPolicy transactionPolicy) {
        this.transactionPolicy = transactionPolicy;
    }

    public BaseContext.State[] getCurrentAllowedStates() {
        return null;
    }

    public BaseContext.State[] setCurrentAllowedStates(final BaseContext.State[] newAllowedStates) {
        return null;
    }

    @SuppressWarnings({"unchecked"})
    public <T> T get(final Class<T> type) {
        return (T) data.get(type);
    }

    @SuppressWarnings({"unchecked"})
    public <T> T set(final Class<T> type, final T value) {
        return (T) data.put(type, value);
    }

    @SuppressWarnings({"unchecked"})
    public <T> T remove(final Class<T> type) {
        return (T) data.remove(type);
    }

    public boolean isDiscardInstance() {
        return discardInstance;
    }

    public void setDiscardInstance(final boolean discardInstance) {
        this.discardInstance = discardInstance;
    }

    @Override
    public String toString() {
        return "ThreadContext{" +
            "beanContext=" + beanContext.getId() +
            ", primaryKey=" + primaryKey +
            ", data=" + data.size() +
            ", oldClassLoader=" + oldClassLoader +
            ", currentOperation=" + currentOperation +
            ", invokedInterface=" + invokedInterface +
            ", transactionPolicy=" + transactionPolicy +
            ", discardInstance=" + discardInstance +
            '}';
    }
}
