package org.apache.openejb.resource.jdbc.managed.local;

import org.apache.openejb.OpenEJB;

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
    private static final Map<Transaction, Connection> CONNECTION_BY_TX = new ConcurrentHashMap<Transaction, Connection>();

    protected Connection delegate;
    private final TransactionManager transactionManager;
    private Transaction currentTransaction;


    public ManagedConnection(final Connection connection, final TransactionManager txMgr) {
        delegate = connection;
        transactionManager = txMgr;
    }

    public XAResource getXAResource() throws SQLException {
        return new LocalXAResource(delegate);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        // first some Object method management
        final String mtdName = method.getName();
        if ("toString".equals(mtdName)) {
            return "ManagedConnection{" + delegate + "}";
        }
        if ("hashCode".equals(mtdName)) {
            return delegate.hashCode();
        }
        if ("equals".equals(mtdName)) {
            return delegate.equals(args[0]);
        }

        // here the real logic starts
        try {
            final Transaction transaction = transactionManager.getTransaction();

            if (transaction == null) { // shouldn't be possible
                return method.invoke(delegate, args);
            }

            // if we have a tx check it is the same this connection is linked to
            if (currentTransaction != null) {
                if (isUnderTransaction(currentTransaction.getStatus())) {
                    if (currentTransaction != transaction) {
                        throw new SQLException("Connection can not be used while enlisted in another transaction");
                    }
                    return invokeUnderTransaction(delegate, method, args);
                } else {
                    close(delegate);
                }
            }

            // get the already bound connection to the current transaction
            // or enlist this one in the tx
            int status = transaction.getStatus();
            if (isUnderTransaction(status)) {
                final Connection connection = CONNECTION_BY_TX.get(transaction);
                if (connection != delegate) {
                    if (connection != null) { // use already existing one
                        delegate.close(); // return to pool
                        delegate = connection;
                    } else {
                        CONNECTION_BY_TX.put(transaction, delegate);
                        currentTransaction = transaction;
                        try {
                            transaction.enlistResource(getXAResource());
                        } catch (RollbackException ignored) {
                            // no-op
                        } catch (SystemException e) {
                            throw new SQLException("Unable to enlist connection the transaction", e);
                        }

                        transaction.registerSynchronization(new ClosingSynchronization(delegate));

                        delegate.setAutoCommit(false);
                    }
                }

                return invokeUnderTransaction(delegate, method, args);
            }

            return method.invoke(delegate, args);
        } catch (InvocationTargetException ite) {
            throw ite.getTargetException();
        }


    }

    private static Object invokeUnderTransaction(final Connection delegate, final Method method, final Object[] args) throws Exception {
        final String mtdName = method.getName();
        if ("setAutoCommit".equals(mtdName)
                || "commit".equals(mtdName)
                || "rollback".equals(mtdName)
                || "setSavepoint".equals(mtdName)
                || "setReadOnly".equals(mtdName)) {
            throw forbiddenCall(mtdName);
        }
        if ("close".equals(mtdName)) {
            // will be done later
            // we need to delay it in case of rollback
            return noop();
        }
        return method.invoke(delegate, args);
    }

    // just to give a better semantiv to a trivial operation
    private static Object noop() {
        return null;
    }

    private static boolean isUnderTransaction(int status) {
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
        } catch (SQLException e) {
            // no-op
        }
    }

    private static class ClosingSynchronization implements Synchronization {
        private final Connection connection;

        public ClosingSynchronization(final Connection delegate) {
            connection = delegate;
        }

        @Override
        public void beforeCompletion() {
            // no-op
        }

        @Override
        public void afterCompletion(int status) {
            close(connection);
            try {
                final Transaction tx = OpenEJB.getTransactionManager().getTransaction();
                CONNECTION_BY_TX.remove(tx);
            } catch (SystemException ignored) {
                // no-op
            }
        }
    }
}
