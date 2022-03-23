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
package org.apache.tomee.catalina.valve;

import org.apache.catalina.Contained;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.ha.ClusterValve;
import org.apache.openejb.config.sys.PropertiesAdapter;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.tomee.catalina.TomEERuntimeException;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

public class LazyValve implements ClusterValve, Lifecycle, Contained {
    private String delegateClassName;
    private String properties;
    private Container container;
    private Valve next;
    private CatalinaCluster cluster;

    private volatile Valve delegate;
    private volatile boolean init;
    private volatile boolean start;
    private volatile LifecycleState state;
    private final List<LifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<>();

    public void setDelegateClassName(String delegateClassName) {
        this.delegateClassName = delegateClassName;
    }

    public void setProperties(final String properties) {
        this.properties = properties;
    }

    private Valve instance() {
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    final Object instance;

                    ClassLoader cl = loader();
                    if (cl == null) {
                        return null;
                    }

                    final Class<?> clazz;
                    try {
                        clazz = cl.loadClass(delegateClassName);
                    } catch (final ClassNotFoundException e) {
                        throw new TomEERuntimeException(e);
                    }

                    try {
                        final ObjectRecipe recipe = new ObjectRecipe(clazz);
                        recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
                        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
                        recipe.allow(Option.FIELD_INJECTION);
                        recipe.allow(Option.PRIVATE_PROPERTIES);
                        if (properties != null) {
                            final Properties props = new PropertiesAdapter()
                                    .unmarshal(properties.trim().replaceAll("\\p{Space}*(\\p{Alnum}*)=", "\n$1="));
                            recipe.setAllProperties(props);
                        }
                        instance = recipe.create();
                    } catch (final Exception e) {
                        throw new TomEERuntimeException(e);
                    }

                    delegate = Valve.class.cast(instance);
                    delegate.setNext(next);
                    if (Contained.class.isInstance(delegate)) {
                        Contained.class.cast(delegate).setContainer(container);
                    }
                    if (Lifecycle.class.isInstance(delegate)) {
                        if (init) {
                            try {
                                final Lifecycle lifecycle = Lifecycle.class.cast(delegate);
                                for (final LifecycleListener listener : lifecycleListeners) {
                                    lifecycle.addLifecycleListener(listener);
                                }
                                lifecycle.init();
                                if (start) {
                                    lifecycle.start();
                                }
                            } catch (final LifecycleException e) {
                                // no-op
                            }
                        }
                    }
                    if (ClusterValve.class.isInstance(delegate)) {
                        ClusterValve.class.cast(delegate).setCluster(cluster);
                    }
                }
            }
        }
        return delegate;
    }

    private ClassLoader loader() {
        if (container == null || !Context.class.isInstance(container)) {
            return ParentClassLoaderFinder.Helper.get();
        }
        final Context ctx = Context.class.cast(container);
        return ctx.getLoader() != null && ctx.getLoader().getClassLoader() != null ? ctx.getLoader().getClassLoader() : null;
    }

    @Override
    public Valve getNext() {
        return next;
    }

    @Override
    public void setNext(final Valve valve) {
        this.next = valve;
        if (delegate != null) {
            delegate.setNext(next);
        }
    }

    @Override
    public void backgroundProcess() {
        if (delegate != null) {
            delegate.backgroundProcess();
        }
    }

    @Override
    public void invoke(final Request request, final Response response) throws IOException, ServletException {
        instance().invoke(request, response);
    }

    @Override
    public boolean isAsyncSupported() {
        return instance().isAsyncSupported();
    }

    @Override
    public void addLifecycleListener(final LifecycleListener listener) {
        lifecycleListeners.add(listener);
    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return lifecycleListeners.toArray(new LifecycleListener[0]);
    }

    @Override
    public void removeLifecycleListener(final LifecycleListener listener) {
        lifecycleListeners.remove(listener);
    }

    @Override
    public void init() throws LifecycleException {
        if (instance() != null && Lifecycle.class.isInstance(delegate)) {
            Lifecycle.class.cast(delegate).init();
        } else {
            init = true;
        }
        state = LifecycleState.INITIALIZED;
    }

    @Override
    public void start() throws LifecycleException {
        if (instance() != null && Lifecycle.class.isInstance(delegate)) {
            Lifecycle.class.cast(delegate).start();
        } else {
            start = true;
        }
        state = LifecycleState.STARTED;
    }

    @Override
    public void stop() throws LifecycleException {
        if (instance() != null && Lifecycle.class.isInstance(delegate)) {
            Lifecycle.class.cast(delegate).stop();
        }
        state = LifecycleState.STOPPED;
    }

    @Override
    public void destroy() throws LifecycleException {
        if (instance() != null && Lifecycle.class.isInstance(delegate)) {
            Lifecycle.class.cast(delegate).destroy();
        }
        state = LifecycleState.DESTROYED;
    }

    @Override
    public LifecycleState getState() {
        return state;
    }

    @Override
    public String getStateName() {
        return state.name();
    }

    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public void setContainer(final Container container) {
        this.container = container;
        if (delegate != null && Contained.class.isInstance(delegate)) {
            Contained.class.cast(delegate).setContainer(container);
        }
    }

    @Override
    public CatalinaCluster getCluster() {
        return cluster;
    }

    @Override
    public void setCluster(final CatalinaCluster catalinaCluster) {
        this.cluster = catalinaCluster;
    }
}
