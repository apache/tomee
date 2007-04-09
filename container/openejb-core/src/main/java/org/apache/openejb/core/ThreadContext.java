/**
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

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.openejb.util.Logger;

public class ThreadContext {
    private static final Logger log = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");
    private static final ThreadLocal<ThreadContext> threadStorage = new ThreadLocal<ThreadContext>();
    private static final List<ThreadContextListener> listeners = new CopyOnWriteArrayList<ThreadContextListener>();

    public static ThreadContext getThreadContext() {
        ThreadContext threadContext = threadStorage.get();
        return threadContext;
    }

    public static ThreadContext enter(ThreadContext newContext) {
        if (newContext == null) {
            throw new NullPointerException("newContext is null");
        }

        // set the thread context class loader
        newContext.oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(newContext.deploymentInfo.getClassLoader());

        // update thread local
        ThreadContext oldContext = threadStorage.get();
        threadStorage.set(newContext);

        // notify listeners
        for (ThreadContextListener listener : listeners) {
            try {
                listener.contextEntered(oldContext, newContext);
            } catch (Throwable e) {
                log.warning("ThreadContextListener threw an exception", e);
            }
        }

        // return old context so it can be used for exit call below
        return oldContext;
    }

    public static void exit(ThreadContext oldContext) {
        ThreadContext exitingContext = threadStorage.get();
        if (exitingContext == null) {
            throw new IllegalStateException("No existing context");
        }

        // set the thread context class loader back
        Thread.currentThread().setContextClassLoader(exitingContext.oldClassLoader);
        exitingContext.oldClassLoader = null;

        // update thread local
        threadStorage.set(oldContext);

        // notify listeners
        for (ThreadContextListener listener : listeners) {
            try {
                listener.contextExited(exitingContext, oldContext);
            } catch (Throwable e) {
                log.warning("ThreadContextListener threw an exception", e);
            }
        }
    }

    public static void addThreadContextListener(ThreadContextListener listener) {
        listeners.add(listener);
    }

    public static void removeThreadContextListener(ThreadContextListener listener) {
        listeners.remove(listener);
    }

    private final CoreDeploymentInfo deploymentInfo;
    private final Object primaryKey;
    private final HashMap<Class, Object> data = new HashMap<Class, Object>();
    private ClassLoader oldClassLoader;
    private Operation currentOperation;
    private BaseContext.State[] currentAllowedStates;

    public ThreadContext(CoreDeploymentInfo deploymentInfo, Object primaryKey) {
        this(deploymentInfo, primaryKey, null);
    }

    public ThreadContext(CoreDeploymentInfo deploymentInfo, Object primaryKey, Operation operation) {
        if (deploymentInfo == null) {
            throw new NullPointerException("deploymentInfo is null");
        }
        this.deploymentInfo = deploymentInfo;
        this.primaryKey = primaryKey;
        this.currentOperation = operation;
    }

    public ThreadContext(ThreadContext that) {
        this.deploymentInfo = that.deploymentInfo;
        this.primaryKey = that.primaryKey;
        this.data.putAll(that.data);
        this.oldClassLoader = that.oldClassLoader;
    }

    public CoreDeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public Operation getCurrentOperation() {
        return currentOperation;
    }

    public void setCurrentOperation(Operation operation) {
        currentOperation = operation;
    }

    public BaseContext.State[] getCurrentAllowedStates() {
        return currentAllowedStates;
    }

    public BaseContext.State[] setCurrentAllowedStates(BaseContext.State[] newAllowedStates) {
        BaseContext.State[] oldAllowedStates = currentAllowedStates; 
        currentAllowedStates = newAllowedStates;
        return oldAllowedStates;
    }
    
    @SuppressWarnings({"unchecked"})
    public <T> T get(Class<T> type) {
        return (T)data.get(type);
    }

    @SuppressWarnings({"unchecked"})
    public <T> T set(Class<T> type, T value) {
        return (T) data.put(type, value);
    }
}
