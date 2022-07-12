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

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DataSourceConnectionFactory;
import org.apache.commons.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp2.managed.LocalXAConnectionFactory;
import org.apache.commons.dbcp2.managed.TransactionRegistry;
import org.apache.commons.dbcp2.managed.XAConnectionFactory;
import org.apache.openejb.resource.jdbc.DataSourceHelper;

import java.lang.reflect.Field;
import java.sql.SQLException;
import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

public class DbcpManagedDataSource extends BasicManagedDataSource {

    private final CommonDataSource ds;

    public DbcpManagedDataSource(final String name, final CommonDataSource dataSource) {
        super(name);
        this.ds = dataSource;
        if (XADataSource.class.isInstance(dataSource)) {
            setXaDataSourceInstance(XADataSource.class.cast(ds));
        }
    }

    @Override
    public void setJdbcUrl(final String url) {
        try {
            DataSourceHelper.setUrl(this.ds, url);
        } catch (final Throwable e1) {
            super.setUrl(url);
        }
    }

    @Override
    protected ConnectionFactory createConnectionFactory() throws SQLException {
        if (ds instanceof XADataSource) {

            // Create the XAConectionFactory using the XA data source
            final XADataSource xaDataSourceInstance = (XADataSource) ds;
            final XAConnectionFactory xaConnectionFactory = new DataSourceXAConnectionFactory(getTransactionManager(), xaDataSourceInstance, getUsername(), getPassword());
            setTransactionRegistry(xaConnectionFactory, new DbcpTransactionRegistry(getTransactionManager()));
            setTransactionRegistry(xaConnectionFactory.getTransactionRegistry());
            return xaConnectionFactory;

        }

        // If xa data source is not specified a DriverConnectionFactory is created and wrapped with a LocalXAConnectionFactory
        final ConnectionFactory connectionFactory = new DataSourceConnectionFactory(DataSource.class.cast(ds), getUsername(), getPassword());
        final XAConnectionFactory xaConnectionFactory = new LocalXAConnectionFactory(getTransactionManager(), connectionFactory);
        setTransactionRegistry(xaConnectionFactory, new DbcpTransactionRegistry(getTransactionManager()));
        setTransactionRegistry(xaConnectionFactory.getTransactionRegistry());
        return xaConnectionFactory;
    }

    public void setTransactionRegistry(final TransactionRegistry registry) {
        try {
            final Field field = org.apache.commons.dbcp2.managed.BasicManagedDataSource.class.getDeclaredField("transactionRegistry");
            field.setAccessible(true);
            field.set(this, registry);
        } catch (final Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    private void setTransactionRegistry(XAConnectionFactory xaConnectionFactory, final TransactionRegistry registry) {
        try {
            final Field field = xaConnectionFactory.getClass().getDeclaredField("transactionRegistry");
            field.setAccessible(true);
            field.set(xaConnectionFactory, registry);
        } catch (final Throwable e) {
            throw new IllegalStateException(e);
        }
    }
}
