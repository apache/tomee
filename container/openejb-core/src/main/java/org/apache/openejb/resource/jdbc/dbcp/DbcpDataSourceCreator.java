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

package org.apache.openejb.resource.jdbc.dbcp;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.jdbc.managed.local.ManagedDataSource;
import org.apache.openejb.resource.jdbc.managed.xa.ManagedXADataSource;
import org.apache.openejb.resource.jdbc.pool.PoolDataSourceCreator;
import org.apache.openejb.resource.jdbc.pool.XADataSourceResource;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.util.Properties;

// just a sample showing how to implement a datasourcecreator
// this one will probably not be used since dbcp has already the integration we need
public class DbcpDataSourceCreator extends PoolDataSourceCreator {
    @Override
    public DataSource pool(final String name, final DataSource ds, final Properties properties) {
        return build(DbcpDataSource.class, new DbcpDataSource(name, ds), properties);
    }

    @Override
    public DataSource managed(final String name, final CommonDataSource ds) {
        final TransactionManager transactionManager = OpenEJB.getTransactionManager();
        if (ds instanceof XADataSource) {
            return new ManagedXADataSource(ds, transactionManager, SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class));
        }
        return new ManagedDataSource(DataSource.class.cast(ds), transactionManager, SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class));
    }

    @Override
    public CommonDataSource pool(final String name, final String driver, final Properties properties) {
        properties.setProperty("name", name);

        final String xa = String.class.cast(properties.remove("XaDataSource"));
        if (xa == null && !properties.containsKey("JdbcDriver")) {
            properties.setProperty("driverClassName", driver);
        }

        final BasicDataSource ds = build(BasicDataSource.class, properties);
        ds.setDriverClassName(driver);
        if (xa != null) {
            ds.setDelegate(XADataSourceResource.proxy(Thread.currentThread().getContextClassLoader(), xa));
        }
        return ds;
    }

    @Override
    protected void doDestroy(final CommonDataSource dataSource) throws Throwable {
        ((org.apache.commons.dbcp2.BasicDataSource) dataSource).close();
    }

    @Override
    protected <T> T build(final Class<T> clazz, final Properties properties) {
        final T object = super.build(clazz, properties);
        setDriverLoader(object);
        return object;
    }

    @Override
    protected <T> T build(final Class<T> clazz, final Object instance, final Properties properties) {
        final T object = super.build(clazz, instance, properties);
        setDriverLoader(object);
        return object;
    }

    private <T> void setDriverLoader(final T object) {
        if (org.apache.commons.dbcp2.BasicDataSource.class.isInstance(object)) {
            final org.apache.commons.dbcp2.BasicDataSource basicDataSource = (org.apache.commons.dbcp2.BasicDataSource) object;
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            basicDataSource.setDriverClassLoader(contextClassLoader);
        }
    }
}
