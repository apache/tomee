package org.apache.openejb.resource.jdbc.managed;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

public class ManagedXADataSource extends ManagedDataSource {
    private final XADataSource xaDataSource;

    public ManagedXADataSource(final DataSource ds) {
        super(ds);
        xaDataSource = (XADataSource) ds;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new ManagedXAConnection(xaDataSource.getXAConnection().getConnection());
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return new ManagedXAConnection(xaDataSource.getXAConnection(username, password).getConnection());
    }
}
