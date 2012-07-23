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
