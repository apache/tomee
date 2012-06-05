package org.superbiz.deltaspike.config;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.apache.deltaspike.core.impl.config.BaseConfigSource;
import org.apache.deltaspike.core.util.PropertyFileUtils;

public class MyConfigSource extends BaseConfigSource {
    private static final Logger LOGGER = Logger.getLogger(MyConfigSource.class.getName());
    private static final String MY_CONF_FILE_NAME = "my-app-config.properties";

    private final Properties properties;

    public MyConfigSource() {
        final Enumeration<URL> in;
        try {
            in = Thread.currentThread().getContextClassLoader().getResources(MY_CONF_FILE_NAME);
        } catch (IOException e) {
            throw new IllegalArgumentException("can't find " + MY_CONF_FILE_NAME, e);
        }

        properties = new Properties();

        while (in.hasMoreElements()) {
            final Properties currentProps = PropertyFileUtils.loadProperties(in.nextElement());
            for (Map.Entry<Object, Object> key : currentProps.entrySet()) { // some check
                if (properties.containsKey(key.getKey().toString())) {
                    LOGGER.warning("found " + key.getKey() + " multiple times, only one value will be available.");
                }
            }
            properties.putAll(currentProps);
        }

        initOrdinal(401); // before other sources
    }

    @Override
    public String getPropertyValue(String key) {
        return properties.getProperty(key);
    }

    @Override
    public String getConfigName() {
        return MY_CONF_FILE_NAME;
    }
}
