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
import java.lang.reflect.Modifier;

public final class Reflections {
    private Reflections() {
        // no-op
    }

    public static Method findMethod(final String name, final Class<?> type, final Class<?>... args) {
        try {
            return type.getMethod(name, args);
        } catch (final NoSuchMethodException e) {
            throw new IllegalArgumentException("Can't find public method " + name + " in " + type.getName());
        }
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
            } catch (final NoSuchMethodException nsme) {
                // no-op
            } catch (final Exception e) {
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
        set(instance.getClass(), instance, field, value);
    }

    public static void set(final Class<?> inClazz, final Object instance, final String field, final Object value) {
        Class<?> clazz = inClazz;
        while (clazz != null) {
            try {
                final Field f = clazz.getDeclaredField(field);
                final boolean acc = f.isAccessible();
                f.setAccessible(true);
                Field modifiersField = null;
                final int modifiers = f.getModifiers();
                final boolean isFinal = Modifier.isFinal(modifiers);
                try {
                    if (isFinal) {
                        modifiersField = Field.class.getDeclaredField("modifiers");
                        modifiersField.setAccessible(true);
                        modifiersField.setInt(f, modifiers & ~Modifier.FINAL);
                    }
                    f.set(instance, value);
                    return;
                } finally {
                    if (isFinal && modifiersField != null) {
                        modifiersField.setInt(f, modifiers);
                    }
                    f.setAccessible(acc);
                }
            } catch (final NoSuchFieldException nsfe) {
                // no-op
            } catch (final Exception e) {
                throw new IllegalArgumentException(e);
            }

            clazz = clazz.getSuperclass();
        }
    }

    public static Object get(final Object instance, final String field) {
        return get(instance.getClass(), instance, field);
    }

    public static Object get(final Class<?> aClass, final Object instance, final String field) {
        Class<?> clazz = aClass;
        while (clazz != null) {
            try {
                final Field f = clazz.getDeclaredField(field);
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }
                return f.get(instance);
            } catch (final NoSuchFieldException nsfe) {
                // no-op
            } catch (final Exception e) {
                throw new IllegalArgumentException(e);
            }

            clazz = clazz.getSuperclass();
        }
        throw new OpenEJBRuntimeException(new NoSuchFieldException(field));
    }
}
