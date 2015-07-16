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

import org.apache.openejb.OpenEJB;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.sql.CommonDataSource;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ManagedConnection implements InvocationHandler {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_RESOURCE_JDBC, ManagedConnection.class);

    private static final Map<Integer, Map<Transaction, Connection>> CONNECTION_BY_TX_BY_DS = new ConcurrentHashMap<Integer, Map<Transaction, Connection>>();

    private final TransactionManager transactionManager;
    private final LocalXAResource xaResource;
    protected Connection delegate;
    private Transaction currentTransaction;
    private boolean closed;

    private final Map<Transaction, Connection> connectionByTx;

    public ManagedConnection(final CommonDataSource ds, final Connection connection, final TransactionManager txMgr) {
        delegate = connection;
        transactionManager = txMgr;
        closed = false;
        xaResource = new LocalXAResource(delegate);
        connectionByTx = CONNECTION_BY_TX_BY_DS.get(ds.hashCode());
    }

    public XAResource getXAResource() throws SQLException {
        return xaResource;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        // first some Object method management
        final String mtdName = method.getName();
        if ("toString".equals(mtdName)) {
            return "ManagedConnection{" + delegate + "}";
        }
        if ("hashCode".equals(mtdName)) {
            return hashCode();
        }
        if ("equals".equals(mtdName)) {
            return args[0] == this || (delegate != null && delegate.equals(args[0]));
        }

        // allow to get delegate if needed by the underlying program
        if (Wrapper.class == method.getDeclaringClass() && args.length == 1 && Connection.class == args[0]) {
            if ("isWrapperFor".equals(mtdName)) {
                return true;
            }
            if ("unwrap".equals(mtdName)) {
                return delegate;
            }
        }

        // here the real logic starts
        try {
            final Transaction transaction = transactionManager.getTransaction();

            // shouldn't be used without a transaction but if so just delegate to the actual connection
            if (transaction == null) {
                if (delegate == null) {
                    newConnection();
                }
                return invoke(method, delegate, args);
            }

            // if we have a tx check it is the same this connection is linked to
            if (currentTransaction != null && isUnderTransaction(currentTransaction.getStatus())) {
                if (!currentTransaction.equals(transaction)) {
                    throw new SQLException("Connection can not be used while enlisted in another transaction");
                }
                return invokeUnderTransaction(delegate, method, args);
            }

            // get the already bound connection to the current transaction or enlist this one in the tx
            if (isUnderTransaction(transaction.getStatus())) {
                Connection connection = Connection.class.cast(registry.getResource(key));
                if (connection == null && delegate == null) {
                    newConnection();
                    connection = delegate;

                    registry.putResource(transaction, delegate);
                    currentTransaction = transaction;
                    try {
                        transaction.enlistResource(getXAResource());
                    } catch (final RollbackException ignored) {
                        // no-op
                    } catch (final SystemException e) {
                        throw new SQLException("Unable to enlist connection the transaction", e);
                    }

                    transaction.registerSynchronization(new ClosingSynchronization(delegate));

                    try {
                        setAutoCommit(false);
                    } catch (final SQLException xae) { // we are alreay in a transaction so this can't be called from a user perspective - some XA DataSource prevents it in their code
                        final String message = "Can't set auto commit to false cause the XA datasource doesn't support it, this is likely an issue";
                        final Logger logger = Logger.getInstance(LogCategory.OPENEJB_RESOURCE_JDBC, ManagedConnection.class);
                        if (logger.isDebugEnabled()) { // we don't want to print the exception by default
                            logger.warning(message, xae);
                        } else {
                            logger.warning(message);
                        }
                    }
                } else if (delegate == null) { // shouldn't happen
                    delegate = connection;
                }

                return invokeUnderTransaction(connection, method, args);
            }

            // we shouldn't come here, tempted to just throw an exception
            if (delegate == null) {
                newConnection();
            }
            return invoke(method, delegate, args);
        } catch (final InvocationTargetException ite) {
            throw ite.getTargetException();
        }
    }

    protected Object newConnection() throws SQLException {
        final Object connection = DataSource.class.isInstance(key.ds) ?
                (key.user == null ? DataSource.class.cast(key.ds).getConnection() : DataSource.class.cast(key.ds).getConnection(key.user, key.pwd)) :
                (key.user == null ? XADataSource.class.cast(key.ds).getXAConnection() : XADataSource.class.cast(key.ds).getXAConnection(key.user, key.pwd));
        if (XAConnection.class.isInstance(connection)) {
            xaConnection = XAConnection.class.cast(connection);
            delegate = xaConnection.getConnection();
            xaResource = xaConnection.getXAResource();
        } else {
            delegate = Connection.class.cast(connection);
            xaResource = new LocalXAResource(delegate);
        }
        return connection;
    }

    protected void setAutoCommit(final boolean value) throws SQLException {
        if (delegate == null) {
            newConnection();
        }
        delegate.setAutoCommit(value);
    }

    private static Object invoke(final Method method, final Connection delegate, final Object[] args) throws Throwable {
        try {
            return method.invoke(delegate, args);
        } catch (final InvocationTargetException ite) {
            throw ite.getCause();
        }
    }

    private Object invokeUnderTransaction(final Connection delegate, final Method method, final Object[] args) throws Exception {
        final String mtdName = method.getName();
        if ("setAutoCommit".equals(mtdName)
            || "commit".equals(mtdName)
            || "rollback".equals(mtdName)
            || "setSavepoint".equals(mtdName)
            || "setReadOnly".equals(mtdName)) {
            throw forbiddenCall(mtdName);
        }
        if ("close".equals(mtdName)) {
            return close();
        }
        if ("isClosed".equals(mtdName) && closed) {
            return true; // if !closed let's delegate to the underlying connection
        }
        return method.invoke(delegate, args);
    }

    // will be done later
    // we need to delay it in case of rollback
    private Object close() {
        closed = true;
        return null;
    }

    private static boolean isUnderTransaction(final int status) {
        return status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK;
    }

    private static SQLException forbiddenCall(final String mtdName) {
        return new SQLException("can't call " + mtdName + " when the connection is JtaManaged");
    }

    private static void close(final Connection connection) {
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (final SQLException e) {
            // no-op
        }
    }

    public static void pushDataSource(final CommonDataSource ds) {
        CONNECTION_BY_TX_BY_DS.put(ds.hashCode(), new ConcurrentHashMap<Transaction, Connection>());
    }

    public static void cleanDataSource(final CommonDataSource ds) {
        final Map<Transaction, Connection> map = CONNECTION_BY_TX_BY_DS.remove(ds.hashCode());
        if (map != null) {
            map.clear();
        }
    }

    private static class ClosingSynchronization implements Synchronization {
        private final Connection connection;
        private final Map<Transaction, Connection> mapToCleanup;

        public ClosingSynchronization(final Connection delegate, final Map<Transaction, Connection> connByTx) {
            connection = delegate;
            mapToCleanup = connByTx;
        }

        @Override
        public void beforeCompletion() {
            // no-op
        }

        @Override
        public void afterCompletion(final int status) {
            close(connection);
            try {
                final Transaction tx = OpenEJB.getTransactionManager().getTransaction();
                mapToCleanup.remove(tx);
            } catch (final SystemException ignored) {
                // no-op
            }
        }
    }
}
