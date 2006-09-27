package org.apache.openejb.client;

import java.util.Properties;

public interface ConnectionFactory {

    public void init(Properties props);

    public Connection getConnection(ServerMetaData server) throws java.io.IOException;

}

