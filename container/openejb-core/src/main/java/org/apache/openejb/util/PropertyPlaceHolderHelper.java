package org.apache.openejb.util;

import java.util.Map;
import java.util.Properties;
import org.apache.openejb.loader.SystemInstance;

public final class PropertyPlaceHolderHelper {
    private static final String PREFIX = "${";
    private static final String SUFFIX = "}";

    private PropertyPlaceHolderHelper() {
        // no-op
    }

    public static String value(final String key) {
        if (key == null || !key.startsWith(PREFIX) || !key.endsWith(SUFFIX)) {
            return key;
        }

        final String value = SystemInstance.get().getOptions().get(key.substring(2, key.length() - 1), key);
        if (!value.equals(key) && value.startsWith("java:")) {
            return value.substring(5);
        }
        return value;
    }

    public static Properties holds(final Properties properties) {
        final Properties updated = new Properties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            updated.setProperty(entry.getKey().toString(), value(entry.getValue().toString()));
        }
        return updated;
    }
}
