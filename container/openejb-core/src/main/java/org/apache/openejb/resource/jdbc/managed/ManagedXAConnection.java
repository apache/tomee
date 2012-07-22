package org.apache.openejb.resource.jdbc.managed;

import javax.sql.XAConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class ManagedXAConnection extends ManagedConnection {
    public ManagedXAConnection(final Connection connection) throws SQLException {
        super(connection, ((XAConnection) connection).getXAResource());
    }
}
