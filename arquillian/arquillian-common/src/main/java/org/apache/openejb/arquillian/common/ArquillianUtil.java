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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.common;

import org.apache.openejb.OpenEJBRuntimeException;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.shrinkwrap.api.Archive;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ArquillianUtil {
    private static final String OPENEJB_ADAPTER_SYSTEM_PROP = "openejb.arquillian.adapter";
    private static final String TOMEE_ADAPTER_SYSTEM_PROP = "tomee.arquillian.adapter";

    public static final String PREDEPLOYING_KEY = "openejb.arquillian.predeploy-archives";

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

    public static Collection<Archive<?>> toDeploy(final Properties properties) {
        final Collection<Archive<?>> list = new ArrayList<Archive<?>>();
        if (properties.containsKey(ArquillianUtil.PREDEPLOYING_KEY)) {
            final String toSplit = properties.getProperty(PREDEPLOYING_KEY).trim();
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            for (String name : toSplit.split(",")) {
                int bracket = name.indexOf(".[");
                if (bracket >= 0) {
                    int end = name.indexOf("]");
                    if (end > bracket) {
                        final String pkg = name.substring(0, bracket + 1);
                        final String classes = name.substring(bracket + 2, end);
                        for (String n : classes.split("\\|")) {
                            addClass(list, loader, pkg + n);
                        }
                        continue;
                    }
                }
                addClass(list, loader, name);
            }
        }
        return list;
    }

    private static void addClass(final Collection<Archive<?>> list, final ClassLoader loader, final String classname) {
        final String name = classname.trim();
        try {
            final Class<?> clazz = loader.loadClass(name);
            for (Method m : clazz.getMethods()) {
                final int modifiers = m.getModifiers();
                if (Object.class.equals(m.getDeclaringClass()) || !Archive.class.isAssignableFrom(m.getReturnType())
                        || !Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
                    continue;
                }

                for (Annotation a : m.getAnnotations()) {
                    if ("org.jboss.arquillian.container.test.api.Deployment".equals(a.annotationType().getName())) {
                        final Archive<?> archive = (Archive<?>) m.invoke(null);
                        list.add(archive);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    public static void undeploy(DeployableContainer<?> container, final Collection<Archive<?>> containerArchives) {
        if (containerArchives != null) {
            for (Archive<?> a  : containerArchives) {
                try {
                    container.undeploy(a);
                } catch (DeploymentException e) {
                    Logger.getLogger(container.getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }
}
