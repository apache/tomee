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
import org.apache.openejb.util.Logger;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAResource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Wrapper;
import java.util.Objects;

public class ManagedConnection implements InvocationHandler {
    private final TransactionManager transactionManager;
    private final Key key;
    private final TransactionSynchronizationRegistry registry;
    protected XAResource xaResource;
    protected Connection delegate;
    protected XAConnection xaConnection;
    private Transaction currentTransaction;
    private boolean closed;

    public ManagedConnection(final CommonDataSource ds,
                             final TransactionManager txMgr,
                             final TransactionSynchronizationRegistry txRegistry,
                             final String user, final String password) {
        transactionManager = txMgr;
        registry = txRegistry;
        closed = false;
        key = new Key(ds, user, password);
    }

    public XAResource getXAResource() throws SQLException {
        if (xaResource == null) {
            newConnection();
        }
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
            InvocationHandler handler;
            return args[0] == this || ((handler = unwrapHandler(args[0])) == this) || (delegate != null && delegate.equals(unwrapDelegate(args[0], handler)));
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
                if ("close".equals(mtdName)) {
                    if (delegate == null) { // no need to get a connection
                        return close();
                    }

                    closeConnection(true);
                    return null;
                }
                if ("isClosed".equals(mtdName) && closed) {
                    return true;
                }
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
                return invokeUnderTransaction(method, args);
            }

            // get the already bound connection to the current transaction or enlist this one in the tx
            final int transactionStatus = transaction.getStatus();
            if (isUnderTransaction(transactionStatus)) {
                Connection connection = Connection.class.cast(registry.getResource(key));
                if (connection == null && delegate == null) {
                    newConnection();

                    currentTransaction = transaction;
                    try {
                        if (!transaction.enlistResource(getXAResource())) {
                            closeConnection(true);
                            throw new SQLException("Unable to enlist connection in transaction: enlistResource returns 'false'.");
                        }
                    } catch (final RollbackException ignored) {
                        // no-op
                    } catch (final SystemException e) {
                        throw new SQLException("Unable to enlist connection the transaction", e);
                    }

                    registry.putResource(key, delegate);
                    transaction.registerSynchronization(new ClosingSynchronization());

                    if (xaConnection == null) {
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
                    }
                } else if (delegate == null) { // shouldn't happen
                    delegate = connection;
                }

                return invokeUnderTransaction(method, args);
            }

            if ("isClosed".equals(mtdName) && closed) {
                return true;
            }
            if ("close".equals(mtdName)) { // let it be handled by the ClosingSynchronisation since we have a tx there
                return close();
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

    private InvocationHandler unwrapHandler(final Object arg) {
        if (arg == null || !Proxy.isProxyClass(arg.getClass())) {
            return null;
        }
        return Proxy.getInvocationHandler(arg);
    }

    private Object unwrapDelegate(final Object arg, final InvocationHandler handler) {
        return handler != null && ManagedConnection.class.isInstance(handler) ? ManagedConnection.class.cast(handler).delegate : arg;
    }

    protected Object newConnection() throws SQLException {
        final Object connection = DataSource.class.isInstance(key.ds) ?
                (key.user == null ? DataSource.class.cast(key.ds).getConnection() : DataSource.class.cast(key.ds).getConnection(key.user, key.pwd)) :
                (key.user == null ? XADataSource.class.cast(key.ds).getXAConnection() : XADataSource.class.cast(key.ds).getXAConnection(key.user, key.pwd));
        if (XAConnection.class.isInstance(connection)) {
            xaConnection = XAConnection.class.cast(connection);
            xaResource = xaConnection.getXAResource();
            delegate = xaConnection.getConnection();
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

    private Object invokeUnderTransaction(final Method method, final Object[] args) throws Exception {
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

    private class ClosingSynchronization implements Synchronization {
        @Override
        public void beforeCompletion() {
            // no-op
        }

        @Override
        public void afterCompletion(final int status) {
            closeConnection(true);
        }
    }

    private void closeConnection(final boolean force) {
        if (!force && closed) {
            return;
        }
        try {
            if (xaConnection != null) { // handles the underlying connection
                xaConnection.close();
            } else if (delegate != null && !delegate.isClosed()) {
                delegate.close();
            }
        } catch (final SQLException e) {
            // no-op
        } finally {
            close(); // set the flag
        }
    }

    private static final class Key {
        private final CommonDataSource ds;
        private final String user;
        private final String pwd;
        private final int hash;

        private Key(final CommonDataSource ds, final String user, final String pwd) {
            this.ds = ds;
            this.user = user;
            this.pwd = pwd;

            int result = ds.hashCode();
            result = 31 * result + (user != null ? user.hashCode() : 0);
            result = 31 * result + (pwd != null ? pwd.hashCode() : 0);
            hash = result;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Key key = Key.class.cast(o);
            return (ds == key.ds || ds.equals(key.ds)) &&
                    Objects.equals(user, key.user) &&
                    Objects.equals(pwd, key.pwd);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
