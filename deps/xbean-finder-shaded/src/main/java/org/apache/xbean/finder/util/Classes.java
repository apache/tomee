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
package org.apache.xbean.finder.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static boolean equals(String classNameA, String classNameB) {
        return classNameA.equals(classNameB);
    }

    public static Class forName(String string, ClassLoader classLoader) throws ClassNotFoundException {
        int arrayDimentions = 0;
        while (string.endsWith("[]")){
            string = string.substring(0, string.length() - 2);
            arrayDimentions++;
        }

        Class clazz = primitives.get(string);

        if (clazz == null) clazz = Class.forName(string, true, classLoader);

        if (arrayDimentions == 0){
            return clazz;
        }
        return Array.newInstance(clazz, new int[arrayDimentions]).getClass();
    }

    public static String packageName(Class clazz){
        return packageName(clazz.getName());
    }

    public static String packageName(String clazzName){
        int i = clazzName.lastIndexOf('.');
        if (i > 0){
            return clazzName.substring(0, i);
        } else {
            return "";
        }
    }

    public static List<String> getSimpleNames(Class... classes){
        List<String> list = new ArrayList<String>();
        for (Class aClass : classes) {
            list.add(aClass.getSimpleName());
        }

        return list;
    }

    public static Class<?> deprimitivize(Class<?> fieldType) {
        return fieldType = fieldType.isPrimitive() ? primitiveWrappers.get(fieldType): fieldType;
    }
}
