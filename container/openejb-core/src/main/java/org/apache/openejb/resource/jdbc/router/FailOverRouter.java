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
package org.apache.openejb.resource.jdbc.router;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class FailOverRouter extends AbstractRouter {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SERVER, FailOverRouter.class);

    public static final String DEFAULT_STRATEGY = "default";

    private final AtomicReference<DataSource> facade = new AtomicReference<DataSource>();
    private final Collection<DataSource> dataSources = new CopyOnWriteArrayList<DataSource>();

    private String delimiter = ",";
    private String strategy = DEFAULT_STRATEGY;
    private String datasourceNames = "";

    @Override
    public DataSource getDataSource() {
        return facade.get();
    }

    public void setDatasourceNames(final String datasourceNames) {
        this.datasourceNames = datasourceNames;
        initDataSources();
    }

    public void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
        initDataSources();
    }

    public void setStrategy(final String strategy) {
        if (strategy == null) {
            this.strategy = DEFAULT_STRATEGY;
        } else {
            this.strategy = strategy.toLowerCase(Locale.ENGLISH).trim();
        }
        initFacade();
    }

    private void initDataSources() {
        dataSources.clear();
        for (final String ds : datasourceNames.split(Pattern.quote(delimiter))) {
            try {
                final Object o = getOpenEJBResource(ds.trim());
                if (DataSource.class.isInstance(o)) {
                    LOGGER.debug("Found datasource '" + ds + "'");
                    dataSources.add(DataSource.class.cast(o));
                }
            } catch (final NamingException error) {
                LOGGER.error("Can't find datasource '" + ds + "'", error);
            }
        }

        initFacade();
    }

    private void initFacade() {
        Class<?> clazz = DataSource.class;
        int xads = 0;
        for (final DataSource ds : dataSources) {
            if (XADataSource.class.isInstance(ds)) {
                xads++;
            }
        }
        if (xads > 0 && xads == dataSources.size()) {
            clazz = XADataSource.class;
        }

        facade.set(DataSource.class.cast(Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{ clazz }, new FacadeHandler(dataSources, strategy))));
    }

    public Collection<DataSource> getDataSources() {
        return dataSources;
    }

    public void updateDataSources(final Collection<DataSource> ds) {
        dataSources.clear();
        dataSources.addAll(ds);
        initFacade();
    }

    private static class FacadeHandler implements InvocationHandler {
        private final Collection<DataSource> delegates;
        private final String strategy;
        private final AtomicInteger currentIdx = new AtomicInteger(0); // used by some strategies

        public FacadeHandler(final Collection<DataSource> dataSources, final String strategy) {
            this.delegates = dataSources;
            this.strategy = strategy;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (Object.class.equals(method.getDeclaringClass())) {
                if ("toString".equals(method.getName())) {
                    return "DataSourceFacade" + delegates;
                }
                if ("hashCode".equals(method.getName())) {
                    return delegates.hashCode();
                }
                if ("equals".equals(method.getName())) {
                    return method.invoke(this, args);
                }
            }

            int ex = 0;
            final Collection<DataSource> sources = sortFollowingStrategy(strategy, delegates, currentIdx);
            for (final DataSource ds : sources) {
                try {
                    if (method.getName().startsWith("set")) {
                        method.invoke(ds, args);
                    } else { // getConnection are here
                        return method.invoke(ds, args); // return the first one succeeding
                    }
                } catch (final InvocationTargetException ite) {
                    ex++;
                    if (ex == sources.size()) { // all failed so throw the exception
                        throw ite.getCause();
                    }
                }
            }

            return null;
        }
    }

    private static Collection<DataSource> sortFollowingStrategy(final String strategy, final Collection<DataSource> delegates, final AtomicInteger idx) {
        if (strategy == null) {
            return delegates;
        }

        if (DEFAULT_STRATEGY.equals(strategy) || strategy.isEmpty()) {
            return delegates;
        }

        //
        // take care next strategies can break multiple calls on the facade
        // it is only intended to be used for connection selection
        //

        if ("random".equals(strategy)) {
            final List<DataSource> ds = new ArrayList<DataSource>(delegates);
            Collections.shuffle(ds);
            return ds;
        }

        if ("reverse".equals(strategy)) {
            final List<DataSource> ds = new ArrayList<DataSource>(delegates);
            final int times = idx.incrementAndGet() % ds.size();
            for (int i = 0; i < times; i++) {
                Collections.reverse(ds);
            }
            return ds;
        }

        if (strategy.startsWith("round-robin")) {
            final int step;
            if (strategy.contains("%")) {
                step = Math.max(1, Integer.parseInt(strategy.substring(strategy.lastIndexOf("%") + 1)));
            } else {
                step = 1;
            }

            final List<DataSource> ds = new ArrayList<DataSource>(delegates);

            int currentIdx = 0;
            for (int i = 0; i < step; i++) {
                currentIdx = idx.incrementAndGet();
            }
            Collections.rotate(ds, 1 + (currentIdx % ds.size()));
            return ds;
        }

        return delegates;
    }
}
