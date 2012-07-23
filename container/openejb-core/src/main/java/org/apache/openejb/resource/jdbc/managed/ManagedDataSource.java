package org.apache.openejb.resource.jdbc.managed;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.resource.jdbc.managed.local.LocalXAResource;
import org.apache.openejb.util.reflection.Reflections;

import javax.sql.DataSource;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class ManagedDataSource implements DataSource {
    private static final Class<?>[] CONNECTION_CLASS = new Class<?>[] { Connection.class };

    protected final DataSource delegate;

    public ManagedDataSource(final DataSource ds) {
        delegate = ds;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return managed(delegate.getConnection());
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return managed(delegate.getConnection(username, password));
    }

    private Connection managed(final Connection connection) {
        return (Connection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), CONNECTION_CLASS, new ManagedConnection(connection));
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
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    // @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return (Logger) Reflections.invokeByReflection(delegate, "getParentLogger", new Class<?>[0], null);
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }
}
