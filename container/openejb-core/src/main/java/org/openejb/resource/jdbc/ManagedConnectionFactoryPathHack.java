package org.openejb.resource.jdbc;

import org.openejb.loader.SystemInstance;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.EISSystemException;
import javax.resource.ResourceException;
import javax.security.auth.Subject;
import java.util.Set;
import java.util.Properties;
import java.io.PrintWriter;
import java.io.File;
import java.sql.DriverManager;

public class ManagedConnectionFactoryPathHack implements javax.resource.spi.ManagedConnectionFactory, java.io.Serializable {
    private final ManagedConnectionFactory factory;

    public ManagedConnectionFactoryPathHack(ManagedConnectionFactory factory) {
        this.factory = factory;
    }

    public Object createConnectionFactory(ConnectionManager connectionManager) throws ResourceException {
        return factory.createConnectionFactory(connectionManager);
    }

    public Object createConnectionFactory() throws ResourceException {
        return factory.createConnectionFactory();
    }

    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {

        Properties systemProperties = System.getProperties();
        synchronized (systemProperties) {
            String userDir = systemProperties.getProperty("user.dir");
            try {
                File base = SystemInstance.get().getBase().getDirectory();
                systemProperties.setProperty("user.dir", base.getAbsolutePath());
                return factory.createManagedConnection(subject, connectionRequestInfo);
            } finally {
                systemProperties.setProperty("user.dir", userDir);
            }
        }
    }

    public ManagedConnection matchManagedConnections(Set set, Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        return factory.matchManagedConnections(set, subject, connectionRequestInfo);
    }

    public void setLogWriter(PrintWriter printWriter) throws ResourceException {
        factory.setLogWriter(printWriter);
    }

    public PrintWriter getLogWriter() throws ResourceException {
        return factory.getLogWriter();
    }

    public int hashCode() {
        return factory.hashCode();
    }

    public boolean equals(Object o) {
        return factory.equals(o);
    }
}
