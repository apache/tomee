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

import javax.sql.CommonDataSource;
import java.io.Flushable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.asList;

public class ResettableDataSourceHandler implements DelegatableHandler {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, ResettableDataSourceHandler.class.getName());

    private final AtomicReference<CommonDataSource> delegate = new AtomicReference<>();
    private final RetryStrategy strategy; // TODO: add pause/exp backoff strategy
    private Set<String> retryMethods = new HashSet<>();

    public ResettableDataSourceHandler(final CommonDataSource ds, final String value, final String methods) {
        this.delegate.set(ds);

        if (!"*".equals(methods)) {
            this.retryMethods.addAll(asList(methods == null ? new String[]{"getConnection", "getXAConnection"} : methods.split(" *, *")));
        }

        final Runnable recreate = new Runnable() {
            @Override
            public void run() {
                try {
                    Flushable.class.cast(delegate.get()).flush();
                } catch (final IOException ioe) {
                    LOGGER.error("Can't flush connection pool: " + ioe.getMessage());
                }
            }
        };

        RetryStrategy tmp;
        if (value.equals("true")) {
            tmp = new CountRetryStrategy(recreate, 1);
        } else if (value.startsWith("retry(") && value.endsWith(")")) {
            tmp = new CountRetryStrategy(recreate, Integer.parseInt(value.substring("retry(".length(), value.length() - 1)));
        } else {
            try {
                tmp = new CountRetryStrategy(recreate, Integer.parseInt(value.trim()));
            } catch (final NumberFormatException nfe) {
                try {
                    tmp = RetryStrategy.class.cast(Thread.currentThread().getContextClassLoader().loadClass(value)
                        .getConstructor(Runnable.class, String.class).newInstance(recreate, value));
                } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
                    throw new IllegalArgumentException("Unknown retry strategy: " + value, e);
                }
            }
        }
        strategy = tmp;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass() && "toString".equals(method.getName())) {
            return "Resettable[" + getDelegate().toString() + "]";
        }

        Result retry = null;
        do {
            try {
                return method.invoke(getDelegate(), args);
            } catch (final InvocationTargetException ite) {
                final Throwable cause = ite.getCause();
                if (SQLException.class.isInstance(cause) && isRetryMethod(method)) {
                    retry = strategy.shouldRetry(cause, retry);
                    if (!retry.status) {
                        throw cause;
                    } else {
                        continue;
                    }
                }
                throw cause;
            }
        } while (true);
    }

    private boolean isRetryMethod(final Method method) {
        return retryMethods.isEmpty() /* wildcard */ || retryMethods.contains(method.getName());
    }

    @Override
    public CommonDataSource getDelegate() {
        return delegate.get();
    }

    public void updateDelegate(final CommonDataSource ds) {
        delegate.set(ds);
    }

    public interface RetryStrategy {
        Result shouldRetry(Throwable cause, Result previous);
    }

    public static class Result {
        private final boolean status;

        public Result(final boolean status) {
            this.status = status;
        }
    }

    private static class CountRetryStrategy implements RetryStrategy {
        private final Runnable task;
        private final int max;

        public CountRetryStrategy(final Runnable recreate, final int max) {
            this.task = recreate;
            this.max = max;
        }

        @Override
        public Result shouldRetry(final Throwable cause, final Result previous) {
            LOGGER.error("SQLException, resetting the connection pool.", cause);

            final Integer count = previous == null ? 1 : CountResult.class.cast(previous).count + 1;
            task.run();
            return new CountResult(count <= max, count);
        }
    }

    private static class CountResult extends Result {
        private int count;

        public CountResult(final boolean status, final int count) {
            super(status);
            this.count = count;
        }
    }
}
