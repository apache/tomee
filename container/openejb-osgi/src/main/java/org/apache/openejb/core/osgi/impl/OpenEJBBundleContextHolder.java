package org.apache.openejb.core.osgi.impl;

import org.osgi.framework.BundleContext;

public class OpenEJBBundleContextHolder {
    private static BundleContext openejbBundleContext;

    public static BundleContext get() {
        return openejbBundleContext;
    }

    public static void set(BundleContext openejbBundleContext) {
        OpenEJBBundleContextHolder.openejbBundleContext = openejbBundleContext;
    }
}
