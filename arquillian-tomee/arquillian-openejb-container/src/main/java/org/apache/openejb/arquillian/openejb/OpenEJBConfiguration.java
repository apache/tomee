package org.apache.openejb.arquillian.openejb;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

public class OpenEJBConfiguration implements ContainerConfiguration {
    @Override
    public void validate() throws ConfigurationException {
        // no-op
    }
}
