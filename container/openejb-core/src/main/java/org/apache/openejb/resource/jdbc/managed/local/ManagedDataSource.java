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

package org.apache.openejb.resource.jdbc.managed.local;

import org.apache.openejb.util.LogCategory;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.io.ObjectStreamException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class ManagedDataSource implements DataSource, Serializable {
    private static final org.apache.openejb.util.Logger LOGGER = org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB_RESOURCE_JDBC, ManagedDataSource.class);
    private static final Class<?>[] CONNECTION_CLASS = new Class<?>[]{Connection.class};

    protected final CommonDataSource delegate;
    protected final TransactionManager transactionManager;
    protected final TransactionSynchronizationRegistry registry;
    protected final int hashCode;

    protected ManagedDataSource(final CommonDataSource ds, final TransactionManager txMgr, final TransactionSynchronizationRegistry txRegistry, final int hc) {
        delegate = ds;
        hashCode = hc;
        transactionManager = txMgr;
        registry = txRegistry;
    }

    public ManagedDataSource(final DataSource ds, final TransactionManager txMgr,  final TransactionSynchronizationRegistry txRegistry) {
        this(ds, txMgr, txRegistry, ds.hashCode());
    }

    @Override
    public Connection getConnection() throws SQLException {
        return managed(null, null);
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return managed(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return DataSource.class.isInstance(delegate) ? DataSource.class.cast(delegate).unwrap(iface) : null;
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return DataSource.class.isInstance(delegate) && DataSource.class.cast(delegate).isWrapperFor(iface);
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    private Connection managed(final String u, final String p) {
        final Connection resource = getTxConnection(delegate, u, p, transactionManager, registry);
        if (resource != null) {
            return resource;
        }

        return (Connection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), CONNECTION_CLASS,
                new ManagedConnection(delegate, transactionManager, registry, u, p));
    }

    protected static Connection getTxConnection(final CommonDataSource delegate, final String u, final String p, final TransactionManager transactionManager, final TransactionSynchronizationRegistry registry) {
        try {
            final Transaction transaction = transactionManager.getTransaction();
            if (transaction != null && ManagedConnection.isUnderTransaction(transaction.getStatus())) {
                final Object resource = registry.getResource(new Key(delegate, u, p));
                if (Connection.class.isInstance(resource)) {
                    return Connection.class.cast(resource);
                }
            }
        } catch (SystemException e) {
            // we wouldn't expect this to happen, but lets log it and fall through to the previous behaviour
            LOGGER.warning("Attempting to get the current transaction failed with an error: " + e.getMessage(), e);
        }
        return null;
    }

    public CommonDataSource getDelegate() {
        return delegate;
    }

    @Override
    public boolean equals(final Object o) {
        return CommonDataSource.class.isInstance(o) && hashCode == o.hashCode();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    Object writeReplace() throws ObjectStreamException {
        if (Serializable.class.isInstance(delegate)) {
            return delegate; // we don't care about the wrapping delegate will do another lookup when unserialized so it is magic :)
        }
        throw new ObjectStreamException(delegate + " not serializable") {};
    }
}
