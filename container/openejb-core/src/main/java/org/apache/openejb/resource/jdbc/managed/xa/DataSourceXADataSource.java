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

package org.apache.openejb.resource.jdbc.managed.xa;

import org.apache.openejb.resource.jdbc.managed.local.ManagedDataSource;

import javax.sql.CommonDataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceXADataSource extends ManagedDataSource {
    private final XADataSource xaDataSource;

    public DataSourceXADataSource(final CommonDataSource ds, final TransactionManager txMgr, final TransactionSynchronizationRegistry registry) {
        super(CommonDataSourceAdapter.wrap(ds), txMgr, registry, ds.hashCode());
        xaDataSource = XADataSource.class.cast(ds);
    }

    @Override
    public Connection getConnection() throws SQLException {
        final XAConnection xaConnection = xaDataSource.getXAConnection();
        return xaConnection.getConnection();
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        final XAConnection xaConnection = xaDataSource.getXAConnection(username, password);
        return xaConnection.getConnection();
    }
}
