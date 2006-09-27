package org.apache.openejb.spi;

import java.io.PrintWriter;
import java.util.Properties;

import javax.resource.spi.ManagedConnectionFactory;

import org.apache.openejb.OpenEJBException;

public interface ConnectionManagerFactory {
    public void setLogWriter(PrintWriter logger);

    public void setProperties(Properties props);

    public OpenEJBConnectionManager createConnectionManager(
            String name, ConnectionManagerConfig config,
            ManagedConnectionFactory factory) throws OpenEJBException;
}