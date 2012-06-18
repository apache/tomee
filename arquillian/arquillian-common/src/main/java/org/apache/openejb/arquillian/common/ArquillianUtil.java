package org.apache.openejb.arquillian.common;

public final class ArquillianUtil {
    private static final String OPENEJB_ADAPTER_SYSTEM_PROP = "openejb.arquillian.adapter";
    private static final String TOMEE_ADAPTER_SYSTEM_PROP = "tomee.arquillian.adapter";

    private ArquillianUtil() {
        // no-op
    }

    public static boolean isCurrentAdapter(final String name) {
        String adapter = System.getProperty(OPENEJB_ADAPTER_SYSTEM_PROP);
        if (adapter == null) {
            adapter = System.getProperty(TOMEE_ADAPTER_SYSTEM_PROP);
        }
        return adapter == null || name.equals(adapter);
    }
}
