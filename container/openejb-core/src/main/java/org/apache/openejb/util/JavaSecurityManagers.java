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
package org.apache.openejb.util;

import jakarta.security.jacc.PolicyContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

// WARN: don't add any logger or container dependency, it should stay self contained
public final class JavaSecurityManagers {
    private static final PrivilegedAction<Properties> GET_SYSTEM_PROPERTIES = System::getProperties;

    private JavaSecurityManagers() {
        // no-op
    }

    public static String getSystemProperty(final String key) {
        return System.getSecurityManager() == null ?
                System.getProperty(key) :
                AccessController.doPrivileged(new PrivilegedAction<String>() {
                    @Override
                    public String run() {
                        return System.getProperty(key);
                    }
                });
    }

    public static String getSystemProperty(final String key, final String or) {
        return System.getSecurityManager() == null ?
                System.getProperty(key, or) :
                AccessController.doPrivileged(new PrivilegedAction<String>() {
                    @Override
                    public String run() {
                        return System.getProperty(key, or);
                    }
                });
    }

    public static Properties getSystemProperties() {
        return System.getSecurityManager() == null ?
                System.getProperties() :
                AccessController.doPrivileged(GET_SYSTEM_PROPERTIES);
    }

    public static void removeSystemProperty(final String key) {
        if (System.getSecurityManager() == null) {
            System.clearProperty(key);
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    System.clearProperty(key);
                    return null;
                }
            });
        }
    }

    public static void setSystemProperty(final String key, final Object value) {
        if (System.getSecurityManager() == null) {
            if (String.class.isInstance(value)) {
                System.setProperty(key, String.class.cast(value));
            } else {
                System.getProperties().put(key, value);
            }
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    if (String.class.isInstance(value)) {
                        System.setProperty(key, String.class.cast(value));
                    } else {
                        System.getProperties().put(key, value);
                    }
                    return null;
                }
            });
        }
    }

    public static void setContextID(final String moduleID) {
        if (System.getSecurityManager() == null) {
            PolicyContext.setContextID(moduleID);
        } else {
            AccessController.doPrivileged(new PrivilegedAction<String>() {
                @Override
                public String run() {
                    PolicyContext.setContextID(moduleID);
                    return null;
                }
            });
        }
    }
}
