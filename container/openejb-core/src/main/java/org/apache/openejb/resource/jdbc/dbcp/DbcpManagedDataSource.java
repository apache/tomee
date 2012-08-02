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

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DataSourceConnectionFactory;
import org.apache.commons.dbcp.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp.managed.LocalXAConnectionFactory;
import org.apache.commons.dbcp.managed.TransactionRegistry;
import org.apache.commons.dbcp.managed.XAConnectionFactory;
import org.apache.openejb.resource.jdbc.DataSourceHelper;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.lang.reflect.Field;
import java.sql.SQLException;

public class DbcpManagedDataSource extends BasicManagedDataSource {

    private final DataSource ds;

    public DbcpManagedDataSource(final String name, final DataSource dataSource) {
        super(name);
        this.ds = dataSource;
    }

    @Override
    public void setJdbcUrl(String url) {
        try {
            DataSourceHelper.setUrl(this.ds, url);
        } catch (Throwable e1) {
            super.setUrl(url);
        }
    }

    @Override
    protected ConnectionFactory createConnectionFactory() throws SQLException {

        if (this.ds instanceof XADataSource) {

            // Create the XAConectionFactory using the XA data source
            XADataSource xaDataSourceInstance = (XADataSource) this.ds;
            XAConnectionFactory xaConnectionFactory = new DataSourceXAConnectionFactory(getTransactionManager(), xaDataSourceInstance, username, password);
            setTransactionRegistry(xaConnectionFactory.getTransactionRegistry());
            return xaConnectionFactory;

        } else {

            // If xa data source is not specified a DriverConnectionFactory is created and wrapped with a LocalXAConnectionFactory
            ConnectionFactory connectionFactory = new DataSourceConnectionFactory(this.ds, username, password);
            XAConnectionFactory xaConnectionFactory = new LocalXAConnectionFactory(getTransactionManager(), connectionFactory);
            setTransactionRegistry(xaConnectionFactory.getTransactionRegistry());
            return xaConnectionFactory;
        }
    }

    public void setTransactionRegistry(TransactionRegistry registry) {
        try {
            final Field field = org.apache.commons.dbcp.managed.BasicManagedDataSource.class.getDeclaredField("transactionRegistry");
            field.setAccessible(true);
            field.set(this, registry);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }
}
