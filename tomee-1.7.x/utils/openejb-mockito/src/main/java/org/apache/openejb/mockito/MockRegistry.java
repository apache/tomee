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
package org.apache.openejb.mockito;

import org.apache.openejb.testing.TestInstance;
import org.apache.openejb.loader.SystemInstance;
import org.mockito.Mock;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * this class is used as a storage for mocks
 * this logic could be enhanced but currently we store mock by types/name
 *
 * Note: only injected mocks are managed
 */
public class MockRegistry {
    private static boolean initialized = false;
    private static final Map<Class<?>, Object> mockInstancesByType = new HashMap<Class<?>, Object>();
    private static final Map<String, Object> mockInstancesByName = new HashMap<String, Object>();

    public static void reset() {
        initialized = false;
        mockInstancesByType.clear();
        mockInstancesByName.clear();
    }

    public static Map<Class<?>, Object> mocksByType() {
        ensureInit();
        return mockInstancesByType;
    }

    public static Map<String, Object> mocksByName() {
        ensureInit();
        return mockInstancesByName;
    }

    private static void ensureInit() {
        if (!initialized) {
            synchronized (MockRegistry.class) {
                if (!initialized) {
                    final TestInstance instance = SystemInstance.get().getComponent(TestInstance.class);
                    if (instance != null) {
                        Class<?> current = instance.getTestClass();
                        while (!current.equals(Object.class)) {
                            for (Field f : current.getDeclaredFields()) {
                                for (Annotation annotation : f.getAnnotations()) {
                                    if (annotation.annotationType().getName().startsWith("org.mockito.")) {
                                        final boolean acc = f.isAccessible();
                                        try {
                                            f.setAccessible(true);
                                            final Object mockInstance = f.get(instance.getInstance());

                                            if (Mock.class.equals(annotation.annotationType())) {
                                                final Mock mock = (Mock) annotation;
                                                if (!"".equals(mock.name())) {
                                                    mockInstancesByName.put(mock.name(), mockInstance);
                                                } else {
                                                    mockInstancesByType.put(f.getType(), mockInstance);
                                                }
                                            } else {
                                                mockInstancesByType.put(f.getType(), mockInstance);
                                            }
                                        } catch (IllegalAccessException e) {
                                            // no-op
                                        } finally {
                                            f.setAccessible(acc);
                                        }

                                        break;
                                    }
                                }
                            }

                            current = current.getSuperclass();
                        }
                    }
                    initialized = true;
                }
            }
        }
    }
}
