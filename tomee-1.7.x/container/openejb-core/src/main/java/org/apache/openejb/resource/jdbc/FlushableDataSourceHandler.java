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

import org.apache.openejb.resource.jdbc.managed.local.ManagedDataSource;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.sql.CommonDataSource;
import java.io.Flushable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FlushableDataSourceHandler implements InvocationHandler {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, FlushableDataSourceHandler.class);

    private final FlushConfig config;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private volatile CommonDataSource delegate;

    public FlushableDataSourceHandler(final CommonDataSource original, final FlushConfig config) {
        this.config = config;
        this.delegate = original;
    }

    private void createANewDelegate() {
        final CommonDataSource old = delegate;
        try {
            this.delegate = DataSourceFactory.create(config.name, config.configuredManaged, config.impl, config.definition, config.maxWaitTime, config.timeBetweenEvictionRuns, config.minEvictableIdleTime);
        } catch (final Exception e) {
            LOGGER.error("Can't recreate the datasource, keeping old one", e);
            this.delegate = old;
            return;
        }

        if (DataSourceFactory.knows(old)) {
            try {
                DataSourceFactory.destroy(old);
            } catch (final Throwable t) {
                //Ignore
            }

            if (ManagedDataSource.class.isInstance(old)) {
                ManagedDataSource.class.cast(old).clean();
            }
        }
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            if ("hashCode".equals(method.getName())) {
                return hashCode();
            }
        }
        if (Flushable.class == method.getDeclaringClass()) {
            final Lock l = lock.writeLock();
            l.lock();
            try {
                createANewDelegate();
                if (Flushable.class.isInstance(delegate)) {
                    Flushable.class.cast(delegate).flush();
                }
            } finally {
                l.unlock();
            }
            return null;
        }

        final Lock l = lock.readLock();
        l.lock();
        try {
            return method.invoke(delegate, args);
        } catch (final InvocationTargetException ite) {
            throw ite.getCause();
        } finally {
            l.unlock();
        }
    }

    public CommonDataSource getDelegate() {
        return delegate;
    }

    public static class FlushConfig {
        public final String name;
        public final boolean configuredManaged;
        public final Class impl;
        public final String definition;
        public final Duration maxWaitTime;
        public final Duration timeBetweenEvictionRuns;
        public final Duration minEvictableIdleTime;

        public FlushConfig(final String name, final boolean configuredManaged, final Class impl, final String definition, final Duration maxWaitTime, final Duration timeBetweenEvictionRuns, final Duration minEvictableIdleTime) {
            this.name = name;
            this.impl = impl;
            this.configuredManaged = configuredManaged;
            this.definition = definition;
            this.maxWaitTime = maxWaitTime;
            this.timeBetweenEvictionRuns = timeBetweenEvictionRuns;
            this.minEvictableIdleTime = minEvictableIdleTime;
        }
    }
}
