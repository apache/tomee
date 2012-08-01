package org.apache.openejb.resource.jdbc.managed.xa;

import org.apache.openejb.resource.jdbc.managed.local.ManagedDataSource;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

public class ManagedXADataSource extends ManagedDataSource {
    private static final Class<?>[] CONNECTION_CLASS = new Class<?>[] { XAConnection.class };

    private final XADataSource xaDataSource;

    public ManagedXADataSource(final DataSource ds, final TransactionManager txMgr) {
        super(ds, txMgr);
        xaDataSource = (XADataSource) ds;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return managedXA(xaDataSource.getXAConnection().getConnection());
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return managedXA(xaDataSource.getXAConnection(username, password).getConnection());
    }

    private Connection managedXA(final Connection connection) throws SQLException {
        return (Connection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), CONNECTION_CLASS, new ManagedXAConnection(connection, transactionManager));
    }
}
