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
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class FailOverRouter extends AbstractRouter {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SERVER, FailOverRouter.class);
    private final AtomicReference<DataSource> facade = new AtomicReference<DataSource>();
    private final Collection<DataSource> dataSources = new CopyOnWriteArrayList<DataSource>();
    private String delimiter = ",";
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
                new Class<?>[]{ clazz }, new FacadeHandler(dataSources))));
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

        public FacadeHandler(final Collection<DataSource> dataSources) {
            this.delegates = dataSources;
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
            for (final DataSource ds : delegates) {
                try {
                    if (method.getName().startsWith("set")) {
                        method.invoke(ds, args);
                    } else { // getConnection are here
                        return method.invoke(ds, args); // return the first one succeeding
                    }
                } catch (final InvocationTargetException ite) {
                    ex++;
                    if (ex == delegates.size()) { // all failed so throw the exception
                        throw ite.getCause();
                    }
                }
            }

            return null;
        }
    }
}
