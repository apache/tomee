package org.apache.openejb.api.resource;

import java.util.Properties;

// see properties-provider for resources/services
public interface PropertiesResourceProvider {
    Properties provides();
}
