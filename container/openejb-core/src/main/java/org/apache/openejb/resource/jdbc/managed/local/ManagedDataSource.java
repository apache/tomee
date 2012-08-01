package org.apache.openejb.resource.jdbc.managed.local;

import org.apache.openejb.util.reflection.Reflections;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class ManagedDataSource implements DataSource {
    private static final Class<?>[] CONNECTION_CLASS = new Class<?>[] { Connection.class };

    protected final DataSource delegate;
    protected final TransactionManager transactionManager;

    public ManagedDataSource(final DataSource ds, final TransactionManager txMgr) {
        delegate = ds;
        transactionManager = txMgr;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return managed(delegate.getConnection());
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return managed(delegate.getConnection(username, password));
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

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    // @Override JDK7
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return (Logger) Reflections.invokeByReflection(delegate, "getParentLogger", new Class<?>[0], null);
    }

    private Connection managed(final Connection connection) {
        return (Connection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), CONNECTION_CLASS, new ManagedConnection(connection, transactionManager));
    }

    public DataSource getDelegate() {
        return delegate;
    }
}
