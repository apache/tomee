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
package org.apache.openejb.resource.jdbc;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import javax.sql.CommonDataSource;
import java.io.Flushable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FlushableDataSourceHandler implements DelegatableHandler {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, FlushableDataSourceHandler.class);
    public static final String[] FACTORY_ARGS = new String[]{
        "ServiceId", "JtaManaged", "JdbcDriver", "Definition", "MaxWaitTime", "TimeBetweenEvictionRuns", "MinEvictableIdleTime", "OpenEJBResourceClasspath"
    };

    private final FlushConfig config;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private AtomicReference<CommonDataSource> delegate = new AtomicReference<>();
    private final ResettableDataSourceHandler resettableHandler;

    public FlushableDataSourceHandler(final CommonDataSource original, final FlushConfig config, final ResettableDataSourceHandler resettableHandler) {
        this.config = config;
        this.delegate.set(original);
        this.resettableHandler = resettableHandler;
    }

    private void createANewDelegate() {
        final CommonDataSource old = delegate.get();
        try {
            final ObjectRecipe recipe = new ObjectRecipe(DataSourceFactory.class.getName(), "create", FACTORY_ARGS);
            recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
            recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
            recipe.allow(Option.NAMED_PARAMETERS);
            recipe.allow(Option.PRIVATE_PROPERTIES);
            recipe.setAllProperties(config.properties);

            recipe.setProperty("resettableHandler", resettableHandler);
            recipe.setProperty("flushableHandler", this);

            updateDataSource(CommonDataSource.class.cast(recipe.create()));
        } catch (final Exception e) {
            LOGGER.error("Can't recreate the datasource, keeping old one", e);
            return;
        }

        if (DataSourceFactory.knows(old)) {
            try {
                DataSourceFactory.destroy(old);
            } catch (final Throwable t) {
                //Ignore
            }
        }
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final CommonDataSource actualDelegate = delegate.get();
        if (Object.class == method.getDeclaringClass()) {
            if ("hashCode".equals(method.getName())) {
                return hashCode();
            }
            if ("toString".equals(method.getName())) {
                return "Flushable[" + actualDelegate.toString() + "]";
            }
        }
        if (Flushable.class == method.getDeclaringClass()) {
            final Lock l = lock.writeLock();
            l.lock();
            try {
                createANewDelegate();
                if (Flushable.class.isInstance(actualDelegate)) { // these sanity could be enhanced
                    if (!Proxy.isProxyClass(actualDelegate.getClass()) ||
                            // reset implies flush so we need to check both
                            (!FlushableDataSourceHandler.class.isInstance(Proxy.getInvocationHandler(actualDelegate)) &&
                            !ResettableDataSourceHandler.class.isInstance(Proxy.getInvocationHandler(actualDelegate)))) {
                        Flushable.class.cast(actualDelegate).flush();
                    }
                }
            } finally {
                l.unlock();
            }
            return null;
        }

        final Lock l = lock.readLock();
        l.lock();
        try {
            return method.invoke(getDelegate(), args);
        } catch (final InvocationTargetException ite) {
            throw ite.getCause();
        } finally {
            l.unlock();
        }
    }

    @Override
    public CommonDataSource getDelegate() {
        return delegate.get();
    }

    public void updateDataSource(final CommonDataSource ds) { // order is important, check DataSourceFactory
        CommonDataSource current = ds;
        while (Proxy.isProxyClass(current.getClass())) {
            final InvocationHandler handler = Proxy.getInvocationHandler(current);
            if (FlushableDataSourceHandler.class.isInstance(handler) ||
                ResettableDataSourceHandler.class.isInstance(handler)) {
                current = DelegatableHandler.class.cast(handler).getDelegate();
            } else {
                break;
            }
        }
        delegate.set(current);
    }

    public static class FlushConfig {
        public final Map<String, Object> properties;

        public FlushConfig(final Map<String, Object> properties) {
            this.properties = properties;
        }
    }
}
