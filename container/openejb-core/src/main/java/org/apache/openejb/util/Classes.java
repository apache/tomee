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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class Classes {

    private static final Map<Class<?>, Class<?>> primitiveWrappers = new HashMap<Class<?>, Class<?>>();
    private static final HashMap<String, Class> primitives = new HashMap<String, Class>();

    static {
        primitives.put("boolean", boolean.class);
        primitives.put("byte", byte.class);
        primitives.put("char", char.class);
        primitives.put("short", short.class);
        primitives.put("int", int.class);
        primitives.put("long", long.class);
        primitives.put("float", float.class);
        primitives.put("double", double.class);

        primitiveWrappers.put(boolean.class, Boolean.class);
        primitiveWrappers.put(byte.class, Byte.class);
        primitiveWrappers.put(char.class, Character.class);
        primitiveWrappers.put(double.class, Double.class);
        primitiveWrappers.put(float.class, Float.class);
        primitiveWrappers.put(int.class, Integer.class);
        primitiveWrappers.put(long.class, Long.class);
        primitiveWrappers.put(short.class, Short.class);
    }

    public static Class forName(String string, final ClassLoader classLoader) throws ClassNotFoundException {
        int arrayDimentions = 0;
        while (string.endsWith("[]")) {
            string = string.substring(0, string.length() - 2);
            arrayDimentions++;
        }

        Class clazz = primitives.get(string);

        if (clazz == null) {
            clazz = Class.forName(string, true, classLoader);
        }

        if (arrayDimentions == 0) {
            return clazz;
        }
        return Array.newInstance(clazz, new int[arrayDimentions]).getClass();
    }

    public static String packageName(final Class clazz) {
        return packageName(clazz.getName());
    }

    public static String packageName(final String clazzName) {
        final int i = clazzName.lastIndexOf('.');
        if (i > 0) {
            return clazzName.substring(0, i);
        } else {
            return "";
        }
    }

    public static List<String> getSimpleNames(final Class... classes) {
        final List<String> list = new ArrayList<>();
        for (final Class aClass : classes) {
            list.add(aClass.getSimpleName());
        }

        return list;
    }

    public static Class<?> deprimitivize(Class<?> fieldType) {
        return fieldType = fieldType.isPrimitive() ? primitiveWrappers.get(fieldType) : fieldType;
    }

    /**
     * Creates a list of the specified class and all its parent classes
     *
     * @param clazz
     * @return
     */
    public static List<Class<?>> ancestors(Class clazz) {
        final ArrayList<Class<?>> ancestors = new ArrayList<>();

        while (clazz != null && !clazz.equals(Object.class)) {
            ancestors.add(clazz);
            clazz = clazz.getSuperclass();
        }

        return ancestors;
    }
}
