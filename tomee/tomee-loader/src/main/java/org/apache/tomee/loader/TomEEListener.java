package org.apache.tomee.loader;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

// simply a facade for OpenEJBListener and ServerListener
public class TomEEListener implements LifecycleListener {
    private static final Logger LOGGER = Logger.getLogger(TomEEListener.class.getName());

    private static final Method delegateMethod;
    static {
        Method mtd = null;
        try {
            mtd = LifecycleListener.class.getMethod("lifecycleEvent", LifecycleEvent.class);
        } catch (Exception e) {
            LOGGER.severe("can't get lifecycleEvent method from LifecycleListener");
        }
        delegateMethod = mtd;
    }

    private final Object delegate;

    public TomEEListener() {
        Object instance = null;
        if (tomeeLibAreInTomcatLibs()) {
            // done by reflection to avoid direct lib dep
            try {
                instance = TomEEListener.class.getClassLoader()
                                .loadClass("org.apache.tomee.catalina.ServerListener")
                                .newInstance();
            } catch (Exception e) {
                LOGGER.severe("can't instantiate ServerListener");
            }
        }

        if (instance == null) {
            instance = new OpenEJBListener();
        }

        delegate = instance;
    }

    private boolean tomeeLibAreInTomcatLibs() {
        final File lib = new File(System.getProperty("catalina.home"), "lib");
        if (lib.exists()) {
            final File[] files = lib.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return name != null && name.startsWith("openejb-core") && name.endsWith(".jar");
                }
            });
            return files != null && files.length > 0; // == 1 in fact
        }
        return false;
    }



    @Override
    public void lifecycleEvent(final LifecycleEvent lifecycleEvent) {
        try {
            delegateMethod.invoke(delegate, lifecycleEvent);
        } catch (Exception e) {
            LOGGER.severe("error invoking " + delegateMethod.getName() + " for " + lifecycleEvent);
        }
    }
}
