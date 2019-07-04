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

import org.apache.openejb.resource.jdbc.managed.local.Key;
import org.apache.openejb.resource.jdbc.managed.local.ManagedConnection;
import org.apache.openejb.resource.jdbc.managed.local.ManagedDataSource;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.CommonDataSource;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

public class ManagedXADataSource extends ManagedDataSource {

    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_RESOURCE_JDBC, ManagedXADataSource.class);
    private static final Class<?>[] CONNECTION_CLASS = new Class<?>[]{Connection.class};

    private final TransactionManager txMgr;

    public ManagedXADataSource(final CommonDataSource ds, final TransactionManager txMgr, final TransactionSynchronizationRegistry registry) {
        super(ds, txMgr, registry, ds.hashCode());
        this.txMgr = txMgr; // ObjectRecipe and our logic will setTxMgr but we want the original one (wrapper)
    }

    @Override
    public Connection getConnection() throws SQLException {
        return managedXA(null, null);
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return managedXA(username, password);
    }

    private Connection managedXA(final String u, final String p) throws SQLException {
        final Connection resource = getTxConnection(delegate, u, p, transactionManager, registry);
        if (resource != null) {
            return resource;
        }
        return Connection.class.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), CONNECTION_CLASS,
                new ManagedXAConnection(delegate, txMgr, registry, u, p)));
    }
}
