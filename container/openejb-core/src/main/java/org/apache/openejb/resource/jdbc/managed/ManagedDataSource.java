package org.apache.openejb.resource.jdbc.managed;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

public class ManagedDataSource implements DataSource {
    protected final DataSource delegate;

    public ManagedDataSource(final DataSource ds) {
        delegate = ds;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new ManagedConnection(delegate.getConnection());
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return new ManagedConnection(delegate.getConnection(username, password));
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
}
