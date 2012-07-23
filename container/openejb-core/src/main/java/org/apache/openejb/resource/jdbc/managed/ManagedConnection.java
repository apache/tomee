package org.apache.openejb.resource.jdbc.managed;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.resource.jdbc.managed.local.LocalXAResource;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

public class ManagedConnection implements InvocationHandler {
    protected final Connection delegate;
    protected final XAResource xaResource;
    private boolean previousAutoCommit;

    protected ManagedConnection(final Connection connection, final XAResource resource) {
        delegate = connection;
        xaResource = resource;
    }

    public ManagedConnection(final Connection connection) {
        this(connection, new LocalXAResource(connection));
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final TransactionManager transactionManager = OpenEJB.getTransactionManager();
        final Transaction transaction = transactionManager.getTransaction();
        if (transaction == null) { // shouldn't be possible
            return method.invoke(delegate, args);
        }

        int status = transaction.getStatus();
        if (status != Status.STATUS_ACTIVE && status != Status.STATUS_MARKED_ROLLBACK) {
            return method.invoke(delegate, args);
        }

        // TODO: manage it properly
        previousAutoCommit = delegate.getAutoCommit();
        delegate.setAutoCommit(false);

        final String mtdName = method.getName();
        if ("setAutoCommit".equals(mtdName)) {
            throw new SQLException("TODO");
        } else if ("commit".equals(mtdName)) {
            throw new SQLException("TODO");
        } else if ("rollback".equals(mtdName)) {
            throw new SQLException("TODO");
        } else if ("setReadOnly".equals(mtdName)) {
            throw new SQLException("TODO");
        }

        // TODO: manage share connection

        transaction.registerSynchronization(new ClosingSynchronization(delegate));

        return method.invoke(delegate, args);
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
            try {
                connection.close();
            } catch (SQLException e) {
                // no-op
            }
        }
    }
}
