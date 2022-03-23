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

import org.apache.openejb.OpenEJB;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.annotation.PostConstruct;
import javax.naming.NamingException;
import javax.sql.DataSource;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class FailOverRouter extends AbstractRouter {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SERVER, FailOverRouter.class);

    public static final String DEFAULT_STRATEGY = "default";

    private ExceptionSelector exceptionSelectorRuntime;
    private ErrorHandler errorHandlerRuntime;
    private Strategy strategyRuntime;
    private DataSource facade;
    private final List<DataSourceHolder> dataSources = new CopyOnWriteArrayList<>();

    private String delimiter = ",";
    private String strategy = DEFAULT_STRATEGY;
    private String datasourceNames = "";

    @Override
    public DataSource getDataSource() {
        return facade;
    }

    @PostConstruct
    public void init() {
        initDataSources();
        initStrategy();
        initFacade();
    }

    public void setDatasourceNames(final String datasourceNames) {
        this.datasourceNames = datasourceNames;
    }

    public void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
    }

    public void setStrategy(final String strategy) {
        this.strategy = strategy;
    }

    public void setStrategyInstance(final Strategy strategy) {
        this.strategyRuntime = strategy;
    }

    public void setExceptionSelectorInstance(final ExceptionSelector selector) {
        exceptionSelectorRuntime = selector;
    }

    public void setExceptionSelector(final String selector) {
        try {
            exceptionSelectorRuntime = "mysql".equalsIgnoreCase(selector) ?
                    new MySQLExceptionSelector() :
                    ExceptionSelector.class.cast(Thread.currentThread().getContextClassLoader()
                            .loadClass(selector.trim()).newInstance());
        } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void setErrorHandlerInstance(final ErrorHandler errorHandler) {
        errorHandlerRuntime = errorHandler;
    }

    public void setErrorHandler(final String errorHandler) {
        try {
            errorHandlerRuntime = ErrorHandler.class.cast(
                    Thread.currentThread().getContextClassLoader().loadClass(errorHandler.trim()).newInstance());
        } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void initStrategy() {
        switch (strategy) {
            case "round-robin":
                strategyRuntime = new Strategy() { // simply rotating the list each time
                    private final AtomicInteger idx = new AtomicInteger(0);

                    @Override
                    public Collection<DataSourceHolder> prepare(final Collection<DataSourceHolder> list) {
                        final int step;
                        if (strategy.contains("%")) {
                            step = Math.max(1, Integer.parseInt(strategy.substring(strategy.lastIndexOf('%') + 1)));
                        } else {
                            step = 1;
                        }

                        final List<DataSourceHolder> ds = new ArrayList<>(list);
                        int currentIdx = 0;
                        for (int i = 0; i < step; i++) {
                            currentIdx = idx.incrementAndGet();
                        }
                        Collections.rotate(ds, 1 + currentIdx % ds.size());
                        return ds;
                    }

                    @Override
                    public void used(final DataSourceHolder holder) {
                        // no-op
                    }
                };
                break;
            case "random":
                strategyRuntime = new Strategy() { // simply rotating the list each time
                    @Override
                    public Collection<DataSourceHolder> prepare(final Collection<DataSourceHolder> list) {
                        final List<DataSourceHolder> ds = new ArrayList<>(list);
                        Collections.shuffle(ds);
                        return ds;
                    }

                    @Override
                    public void used(final DataSourceHolder holder) {
                        // no-op
                    }
                };
                break;
            case "reverse":
                strategyRuntime = new Strategy() { // simply rotating the list each time
                    private final AtomicInteger idx = new AtomicInteger();

                    @Override
                    public Collection<DataSourceHolder> prepare(final Collection<DataSourceHolder> list) {
                        final List<DataSourceHolder> ds = new ArrayList<>(list);
                        final int times = idx.incrementAndGet() % ds.size();
                        for (int i = 0; i < times; i++) {
                            Collections.reverse(ds);
                        }
                        return ds;
                    }

                    @Override
                    public void used(final DataSourceHolder holder) {
                        // no-op
                    }
                };
                break;
            case "static":
                strategyRuntime = new Strategy() { // no auto adaption (if first ds is always faster for instance)
                    @Override
                    public Collection<DataSourceHolder> prepare(final Collection<DataSourceHolder> list) {
                        return list;
                    }

                    @Override
                    public void used(final DataSourceHolder holder) {
                        // no-op
                    }
                };
                break;
            case DEFAULT_STRATEGY:
            default:
                strategyRuntime = new Strategy() { // use the list and save a working item as first one
                    @Override
                    public Collection<DataSourceHolder> prepare(final Collection<DataSourceHolder> list) {
                        return list;
                    }

                    @Override
                    public void used(final DataSourceHolder holder) {
                        if (dataSources.get(0) == holder) { // no lock
                            return;
                        }
                        synchronized (this) {
                            if (dataSources.get(0) == holder) {
                                return;
                            }

                            final DataSourceHolder old = dataSources.set(0, holder); // locks
                            if (old != holder) {
                                dataSources.set(dataSources.lastIndexOf(holder), old);
                            }
                        }
                    }
                };
                break;
        }
    }

    private void initDataSources() {
        dataSources.clear();
        for (final String ds : datasourceNames.split(Pattern.quote(delimiter))) {
            try {
                final String name = ds.trim();
                final Object o = getOpenEJBResource(name);
                if (DataSource.class.isInstance(o)) {
                    LOGGER.debug("Found datasource '" + ds + "'");
                    dataSources.add(new DataSourceHolder(DataSource.class.cast(o), name));
                } else {
                    throw new IllegalArgumentException(name + " (" + o + ") is not a datasource");
                }
            } catch (final NamingException error) {
                throw new IllegalStateException(error);
            }
        }

        initFacade();
    }

    private void initFacade() {
        facade = DataSource.class.cast(Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{DataSource.class}, new FacadeHandler(dataSources, strategyRuntime, errorHandlerRuntime, exceptionSelectorRuntime)));
    }

    public Collection<DataSourceHolder> getDataSources() {
        return dataSources;
    }

    public void updateDataSources(final Collection<DataSourceHolder> ds) {
        dataSources.clear();
        dataSources.addAll(ds);
        initFacade();
    }

    private static class FacadeHandler implements InvocationHandler {
        private static final TransactionSynchronizationRegistry SYNCHRONIZATION_REGISTRY = SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class);

        private final TransactionManager transactionManager;
        private final Collection<DataSourceHolder> delegates;
        private final Strategy strategy;
        private final ErrorHandler handler;
        private final ExceptionSelector selector;

        public FacadeHandler(final Collection<DataSourceHolder> dataSources, final Strategy strategy,
                             final ErrorHandler handler, final ExceptionSelector selector) {
            this.delegates = dataSources;
            this.strategy = strategy;
            this.handler = handler;
            this.selector = selector;
            this.transactionManager = OpenEJB.getTransactionManager();
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

            final Transaction transaction = transactionManager.getTransaction();
            if (transaction != null) {
                final DataSource currentDs = DataSource.class.cast(SYNCHRONIZATION_REGISTRY.getResource(FacadeHandler.class.getName()));
                if (currentDs != null) {
                    return method.invoke(currentDs, args);
                }
            }

            int ex = 0;
            final Collection<DataSourceHolder> sources = strategy.prepare(delegates);
            final int size = sources.size();

            Object out = null;
            Map<String, Throwable> failed = null;
            DataSourceHolder used = null;
            for (final DataSourceHolder ds : sources) {
                used = ds;
                try {
                    final boolean set = method.getName().startsWith("set");
                    if (set) { // should set on all datasources because of failover which can happen but can also be bound to the tx
                        method.invoke(ds.dataSource, args);
                    } else { // getConnection methods are here
                        out = method.invoke(ds.dataSource, args);
                    }

                    if (transaction != null) { // if a tx is in progress save the datasource to use for the tx
                        SYNCHRONIZATION_REGISTRY.putResource(FacadeHandler.class.getName(), ds.dataSource);
                        break;
                    }

                    if (!set) { // if no exception and not a set all is done so return out
                        break;
                    }
                } catch (final InvocationTargetException ite) {
                    final Throwable cause = ite.getTargetException();
                    if (selector != null && !selector.shouldFailover(cause)) {
                        if (failed != null) {
                            handler.onError(failed, ds);
                        }
                        throw cause;
                    }

                    if (handler != null) {
                        if (failed == null) {
                            failed = new HashMap<>();
                        }
                        failed.put(ds.name, ite.getCause());
                    }

                    ex++;
                    if (ex == size) { // all failed so throw the exception
                        if (failed != null) {
                            handler.onError(failed, null);
                        }
                        throw ite.getCause();
                    }
                }
            }

            if (failed != null) {
                handler.onError(failed, used);
            }
            strategy.used(used);
            return out;
        }
    }

    public interface ExceptionSelector {
        boolean shouldFailover(final Throwable sqle);
    }

    public abstract class SQLExceptionSelector implements ExceptionSelector {
        @Override
        public boolean shouldFailover(final Throwable sqle) {
            return SQLException.class.isInstance(sqle) && shouldFailover(SQLException.class.cast(sqle));
        }

        abstract boolean shouldFailover(final SQLException sqle);
    }

    public class MySQLExceptionSelector extends SQLExceptionSelector {
        private final Class<?> communicationException;

        public MySQLExceptionSelector() {
            try {
                communicationException = Thread.currentThread().getContextClassLoader().loadClass("com.mysql.jdbc.CommunicationsException");
            } catch (final ClassNotFoundException e) {
                throw new IllegalArgumentException("com.mysql.jdbc.CommunicationsException not available, please use another ExceptionSelector");
            }
        }

        @Override
        public boolean shouldFailover(final SQLException ex) {
            final String sqlState = ex.getSQLState();
            return (sqlState != null && sqlState.startsWith("08")) ||
                    communicationException.isInstance(ex);
        }
    }

    public interface ErrorHandler {
        void onError(final Map<String, Throwable> errorByFailingDataSource, final DataSourceHolder finallyUsedOrNull);
    }

    public interface Strategy {
        Collection<DataSourceHolder> prepare(final Collection<DataSourceHolder> list);

        void used(DataSourceHolder holder);
    }

    public static final class DataSourceHolder {
        private final DataSource dataSource;
        private final String name;

        public DataSourceHolder(final DataSource dataSource, final String name) {
            this.dataSource = dataSource;
            this.name = name;
        }
    }
}
