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
package org.apache.openejb.util.reflection;

import org.apache.openejb.OpenEJBRuntimeException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class Reflections {
    private Reflections() {
        // no-op
    }

    public static Object invokeByReflection(final Object obj, final String mtdName, final Class<?>[] paramTypes, final Object[] args) {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            boolean acc = true;
            Method mtd = null;
            try {
                mtd = clazz.getDeclaredMethod(mtdName, paramTypes);
                acc = mtd.isAccessible();
                mtd.setAccessible(true);
                return mtd.invoke(obj, args);
            } catch (NoSuchMethodException nsme) {
                // no-op
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            } finally {
                if (mtd != null) {
                    mtd.setAccessible(acc);
                }
            }

            clazz = clazz.getSuperclass();
        }
        throw new IllegalArgumentException(new NoSuchMethodException(obj.getClass().getName() + " ." + mtdName));
    }

    public static void set(final Object instance, final String field, final Object value) {
        Class<?> clazz = instance.getClass();
        while (clazz != null) {
            try {
                final Field f = clazz.getDeclaredField(field);
                boolean acc = f.isAccessible();
                f.setAccessible(true);
                try {
                    f.set(instance, value);
                    return;
                } finally {
                    f.setAccessible(acc);
                }
            } catch (NoSuchFieldException nsfe) {
                // no-op
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }

            clazz = clazz.getSuperclass();
        }
    }

    public static Object get(final Object instance, final String field) {
        Class<?> clazz = instance.getClass();
        while (clazz != null) {
            try {
                final Field f = clazz.getDeclaredField(field);
                boolean acc = f.isAccessible();
                f.setAccessible(true);
                try {
                    return f.get(instance);
                } finally {
                    f.setAccessible(acc);
                }
            } catch (NoSuchFieldException nsfe) {
                // no-op
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }

            clazz = clazz.getSuperclass();
        }
        throw new OpenEJBRuntimeException(new NoSuchFieldException(field));
    }
}
