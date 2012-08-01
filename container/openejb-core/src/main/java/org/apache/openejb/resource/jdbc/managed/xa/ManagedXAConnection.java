package org.apache.openejb.resource.jdbc.managed.xa;

import org.apache.openejb.resource.jdbc.managed.local.ManagedConnection;

import javax.sql.XAConnection;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.sql.Connection;
import java.sql.SQLException;

public class ManagedXAConnection extends ManagedConnection {
    public ManagedXAConnection(final Connection connection, final TransactionManager txMgr) throws SQLException {
        super(connection, txMgr);
    }

    @Override
    public XAResource getXAResource() throws SQLException {
        return ((XAConnection) delegate).getXAResource();
    }
}
