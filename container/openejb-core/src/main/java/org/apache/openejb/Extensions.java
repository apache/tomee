/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.finder.ResourceFinder;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The Extensions API mimics the equivalent CDI Extension/@Observes API
 *
 * Via the Extensions/@Obverves API it is possible to listen for any number of
 * internal events fired by OpenEJB/TomEE during the life of the server and
 * deployment of applications.
 *
 * Extensions are any java class that have one or more @Observes methods using the following format:
 *
 *  - public void <any-name>(@Observes <any-type> event)
 *
 * Extensions can be registered in any number of ways:
 *
 * 1. Via a META-INF/org.apache.openejb.extension text file containing the name of exactly one
 * 2. Via a <Service> tag in the server's openejb.xml or tomee.xml configuration file
 * 3. Via a <Service> tag in a META-INF/resources.xml in the application
 * 4. Directly calling SystemInstance#addObserver(Object)
 *
 * @see org.apache.openejb.observer.ObserverManager
 * @see org.apache.openejb.observer.Observes
 * @see org.apache.openejb.loader.SystemInstance#fireEvent(Object)
 * @see org.apache.openejb.loader.SystemInstance#addObserver(Object)
 *
 */
public final class Extensions {

    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, Extensions.class);

    private Extensions() {
        // no-op
    }

    public static Collection<Class<?>> findExtensions(final ResourceFinder finder) {
        try {
            return finder.findAvailableClasses("org.apache.openejb.extension");
        } catch (final IOException e) {
            LOGGER.error("Extension scanning of 'META-INF/org.apache.openejb.extension' files failed", e);
            return Collections.emptySet();
        }
    }

    public static void installExtensions(final ResourceFinder finder) {
        try {
            final List<Class<?>> classes = finder.findAvailableClasses("org.apache.openejb.extension");
            addExtensions(classes);
        } catch (final IOException e) {
            LOGGER.error("Extension scanning of 'META-INF/org.apache.openejb.extension' files failed", e);
        }
    }

    public static void addExtensions(final ClassLoader loader, final Collection<String> classes) {
        for (final String clazz : classes) {
            try {
                final Object object = loader.loadClass(clazz).newInstance();
                SystemInstance.get().addObserver(object);
            } catch (final Throwable t) {
                LOGGER.error("Extension construction failed" + clazz, t);
            }
        }
    }

    public static void addExtensions(final Collection<Class<?>> classes) {
        for (final Class<?> clazz : classes) {
            try {
                final Object object = clazz.newInstance();
                SystemInstance.get().addObserver(object);
            } catch (final Throwable t) {
                LOGGER.error("Extension construction failed" + clazz.getName(), t);
            }
        }
    }
}
