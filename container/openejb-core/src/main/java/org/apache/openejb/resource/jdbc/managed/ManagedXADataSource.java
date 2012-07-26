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
package org.apache.openejb.resource.jdbc.managed;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

public class ManagedXADataSource extends ManagedDataSource {
    private static final Class<?>[] CONNECTION_CLASS = new Class<?>[] { XAConnection.class };

    private final XADataSource xaDataSource;

    public ManagedXADataSource(final DataSource ds) {
        super(ds);
        xaDataSource = (XADataSource) ds;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return managed(xaDataSource.getXAConnection().getConnection());
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return managed(xaDataSource.getXAConnection(username, password).getConnection());
    }

    private Connection managed(final Connection connection) throws SQLException {
        return (Connection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), CONNECTION_CLASS, new ManagedXAConnection(connection));
    }
}
