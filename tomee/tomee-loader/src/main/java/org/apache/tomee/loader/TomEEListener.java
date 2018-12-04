/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.loader;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.util.logging.Level;
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
        } catch (final Exception e) {
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
            } catch (final Exception e) {
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
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "error invoking {0} for {1}", new Object[]{delegateMethod.getName(), lifecycleEvent});
        }
    }
}
