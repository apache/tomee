package org.apache.openejb.util;

import org.apache.openejb.loader.SystemInstance;
import org.apache.xbean.finder.ResourceFinder;

import java.io.IOException;
import java.util.List;

public final class EventHelper {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, EventHelper.class);

    private EventHelper() {
        // no-op
    }

    public static void installExtensions(final ResourceFinder finder) {
        try {
            final List<Class<?>> classes = finder.findAvailableClasses("org.apache.openejb.extension");
            for (Class<?> clazz : classes) {
                try {
                    final Object object = clazz.newInstance();
                    SystemInstance.get().addObserver(object);
                } catch (Throwable t) {
                    LOGGER.error("Extension construction failed" + clazz.getName(), t);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Extension scanning of 'META-INF/org.apache.openejb.extension' files failed", e);
        }
    }
}
