package org.apache.openejb.spi;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;

public interface OpenEJBConnectionManager extends ConnectionManager {
    public void associateConnections(Object[] connections) throws ResourceException;

    public void disposeConnections(Object[] connections) throws ResourceException;
}