package org.apache.openejb.resource.jdbc;

import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ResourceAdapter;
import javax.security.auth.Subject;
import java.sql.DriverManager;
import java.sql.Connection;

public class BasicManagedConnectionFactory implements javax.resource.spi.ManagedConnectionFactory, java.io.Serializable {
    private final String jdbcDriver;
    private final String jdbcUrl;
    private final String defaultUserName;
    private final String defaultPassword;
    private java.io.PrintWriter logWriter;
    private final int hashCode;
    private final JdbcManagedConnectionFactory managedConnectionFactory;

    public BasicManagedConnectionFactory(JdbcManagedConnectionFactory factory, String jdbcDriver, String jdbcUrl, String defaultUserName, String defaultPassword) {
        this.managedConnectionFactory = factory;
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
        this.defaultUserName = defaultUserName;
        this.defaultPassword = defaultPassword;
        hashCode = jdbcDriver.hashCode() ^ jdbcUrl.hashCode() ^ defaultUserName.hashCode() ^ defaultPassword.hashCode();
    }

    public Object createConnectionFactory() throws javax.resource.ResourceException {
        throw new javax.resource.NotSupportedException("This connector must be used with an application server connection manager");
    }

    public Object createConnectionFactory(ConnectionManager cxManager) throws javax.resource.ResourceException {
        return new JdbcConnectionFactory(managedConnectionFactory, cxManager, jdbcUrl, jdbcDriver, defaultPassword, defaultUserName);
    }

    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws javax.resource.ResourceException {
        try {
            JdbcConnectionRequestInfo request = (JdbcConnectionRequestInfo) connectionRequestInfo;
            Connection connection = DriverManager.getConnection(jdbcUrl, request.getUserName(), request.getPassword());
            return new JdbcManagedConnection(managedConnectionFactory, connection, request);
        } catch (java.sql.SQLException e) {
            throw (EISSystemException) new EISSystemException("Could not obtain a physical JDBC connection from the DriverManager").initCause(e);
        }
    }

    public boolean equals(Object object) {
        if (!(object instanceof BasicManagedConnectionFactory)) {
            return false;
        }
        BasicManagedConnectionFactory that = (BasicManagedConnectionFactory) object;
        return jdbcDriver.equals(that.jdbcDriver) && jdbcUrl.equals(that.jdbcUrl) && defaultUserName.equals(that.defaultUserName) && defaultPassword.equals(that.defaultPassword);
    }

    public java.io.PrintWriter getLogWriter() {
        return logWriter;
    }

    public int hashCode() {
        return hashCode;
    }

    public ManagedConnection matchManagedConnections(java.util.Set connectionSet, javax.security.auth.Subject subject, ConnectionRequestInfo connectionInfo) throws javax.resource.ResourceException {
        if (!(connectionInfo instanceof JdbcConnectionRequestInfo)) {
            return null;
        }

        JdbcManagedConnection[] connections = (JdbcManagedConnection[]) connectionSet.toArray(new JdbcManagedConnection[]{});
        int i = 0;
        for (; i < connections.length && !connections[i].getRequestInfo().equals(connectionInfo); i++) {
        }
        return (i < connections.length) ? connections[i] : null;
    }

    public void setLogWriter(java.io.PrintWriter out) {
        logWriter = out;
    }

    public ResourceAdapter getResourceAdapter() {
        return null;
    }

    public void setResourceAdapter(ResourceAdapter ra) {

    }

}
