/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

public abstract class TomEELogConfigurer extends LogFactory {
    public static void configureLogs() {
        if (Boolean.getBoolean("tomee.skip-tomcat-log")) {
            return;
        }

        final Thread thread = Thread.currentThread();
        try {
            final ClassLoader tccl = thread.getContextClassLoader(); // this is in classpath not StandardClassLoader so use reflection
            final Class<?> logger = tccl.loadClass("org.apache.openejb.util.Logger");
            final Method m = logger.getDeclaredMethod("delegateClass");
            final String clazz = (String) m.invoke(null);
            final LogFactory factory;
            if ("org.apache.openejb.util.Log4jLogStreamFactory".equals(clazz)) {
                factory = LogFactory.class.cast(tccl.loadClass("org.apache.tomee.loader.log.Log4jLogFactory").newInstance());
            } else if ("org.apache.openejb.util.Slf4jLogStreamFactory".equals(clazz)) {
                factory = LogFactory.class.cast(tccl.loadClass("org.apache.tomee.loader.log.Slf4jLogFactory").newInstance());
            } else {
                factory = null;
            }
            if (factory != null) {
                final LogFactory oldFactory = getFactory();
                final Collection<String> names = new ArrayList<String>(oldFactory.getNames());
                oldFactory.getNames().clear();
                oldFactory.release();
                setSingleton(factory);
                reload(factory, tccl, names);
            }
        } catch (final Throwable th) {
            System.err.println(th.getClass().getName() + ": " + th.getMessage());
        }
    }

    private static void reload(final LogFactory factory, final ClassLoader tccl, final Collection<String> names) {
        for (final String name : names) {
            try {
                final Field f = Class.forName(name, false, tccl).getDeclaredField("log");
                if (!Log.class.equals(f.getType())) {
                    continue;
                }

                final boolean acc = f.isAccessible();
                f.setAccessible(true);

                final Log newValue = factory.getInstance(name);
                final int modifiers = f.getModifiers();
                if (Modifier.isFinal(modifiers)) {
                    final Field modifiersField = Field.class.getDeclaredField("modifiers");
                    modifiersField.setAccessible(true);
                    modifiersField.setInt(f, modifiers & ~Modifier.FINAL);

                    f.set(null, newValue);

                    modifiersField.setInt(f, modifiers & Modifier.FINAL);
                } else {
                    f.set(null, newValue);
                }

                f.setAccessible(acc);
            } catch (final Throwable e) {
                // no-op
            }
        }
    }

    private TomEELogConfigurer() {
        // no-op
    }
}
