/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIESOR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.persistence.osgi;

// DO NOT import any org.osgi packages or classes here!

/**
 * OSGi helper class and methods.
 * All routines MUST use reflection, so we don't have any hard-coded
 * runtime depends on OSGi classes for Java SE and EE users.
 *
 * @version $Rev$ $Date$
 */
public class BundleUtils {

    public static boolean runningUnderOSGi() {
        try {
            @SuppressWarnings("unused")
            Class<?> c = Class.forName("org.osgi.framework.Bundle");
            c = Class.forName("org.osgi.framework.BundleActivator");
            c = Class.forName("org.osgi.framework.BundleContext");
            c = Class.forName("org.osgi.framework.ServiceRegistration");
            return true;
        } catch (ClassNotFoundException e) {
            // no-op - catch, eat and return false below
        } catch (NoClassDefFoundError e) {
            // no-op - catch, eat and return false below
        }
        return false;
    }

    /* (non-Javadoc)
     * OPENJPA-1491 Allow us to use the OSGi Bundle's ClassLoader instead of the application one.
     * Uses reflection so we don't have any runtime depends on OSGi classes for Java SE and EE users.
     */
    public static ClassLoader getBundleClassLoader() {
        ClassLoader cl = null;
        if (runningUnderOSGi()) {
            try {
                Class<?> c = Class.forName("org.apache.openjpa.persistence.osgi.PersistenceActivator");
                cl = (ClassLoader) c.getMethod("getBundleClassLoader").invoke(null);
            } catch (Throwable t) {
                // fail-fast
                throw new RuntimeException(t);
            }
        }
        return cl;
    }

}
