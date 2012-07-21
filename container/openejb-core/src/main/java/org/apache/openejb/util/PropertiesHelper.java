package org.apache.openejb.util;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

public final class PropertiesHelper {
    private PropertiesHelper() {
        // no-op
    }

    public static String propertiesToString(final Properties p) {
        if (p == null) {
            return "";
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            p.store(baos, "");
        } catch (Exception ignored) {
            // no-op
        }
        return new String(baos.toByteArray());
    }
}
