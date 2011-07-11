package org.apache.openejb.server.cxf.transport.util;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.extension.ExtensionManagerBus;

/**
 * @author Romain Manni-Bucau
 */
public final class CxfUtil {
    private CxfUtil() {
        // no-op
    }

    /*
     * Ensure the bus created is unqiue and non-shared.
     * The very first bus created is set as a default bus which then can
     * be (re)used in other places.
     */
    public static Bus getBus() {
        getDefaultBus();
        return new ExtensionManagerBus();
    }

    /*
     * Ensure the Spring bus is initialized with the CXF module classloader
     * instead of the application classloader.
     */
    public static Bus getDefaultBus() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(CxfUtil.class.getClassLoader());
        try {
            return BusFactory.getDefaultBus();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }
}
