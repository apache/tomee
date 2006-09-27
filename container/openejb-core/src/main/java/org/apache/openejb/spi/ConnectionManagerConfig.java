package org.apache.openejb.spi;

import java.util.Properties;

public class ConnectionManagerConfig {
    public Properties properties = new Properties();
    public boolean containerManagedSignOn = true;

    public ConnectionManagerConfig() {
    }

    public ConnectionManagerConfig(Properties properties, boolean containerManagedSignOn) {
        this.properties = properties;
        this.containerManagedSignOn = containerManagedSignOn;
    }
}