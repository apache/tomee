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

package org.apache.openejb.junit.context;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * @author quintin
 */
public class Util {
    private Util() {
    }

    /**
     * @param type
     * @param instance
     * @return true if the given object is of the specify type of class
     */
    public static boolean isInstance(Class type, Object instance) {
        if (type.isPrimitive()) {
            // for primitives the insance can't be null
            if (instance == null) {
                return false;
            }

            // verify instance is the correct wrapper type
            if (type.equals(boolean.class)) {
                return instance instanceof Boolean;
            } else if (type.equals(char.class)) {
                return instance instanceof Character;
            } else if (type.equals(byte.class)) {
                return instance instanceof Byte;
            } else if (type.equals(short.class)) {
                return instance instanceof Short;
            } else if (type.equals(int.class)) {
                return instance instanceof Integer;
            } else if (type.equals(long.class)) {
                return instance instanceof Long;
            } else if (type.equals(float.class)) {
                return instance instanceof Float;
            } else if (type.equals(double.class)) {
                return instance instanceof Double;
            } else {
                throw new AssertionError("Invalid primitve type: " + type);
            }
        }

        return instance == null || type.isInstance(instance);
    }

    /**
     * Parses an @Property value and adds it to the specified Hashtable
     *
     * @param env
     * @param value
     */
    public static void addProperty(Hashtable env, String property) {
        if (property == null || property.length() == 0) {
            throw new IllegalArgumentException("No property specified.");
        }

        if (property.charAt(0) == '=') {
            throw new IllegalArgumentException("Invalid property has no name: " + property);
        }

        String name, value;

        property = property.trim();

        int esp = property.indexOf('=');
        if (esp < 0) {
            name = property;
            value = "";
        } else {
            name = property.substring(0, esp).trim();

            if (property.length() > esp) {
                value = property.substring(esp + 1).trim();
            } else {
                value = "";
            }
        }

        env.put(name, value);
    }

    /**
     * Finds the setter method for a given field in the test class
     *
     * @param allowStatic
     * @param typeClass
     * @param propertyName
     * @param propertyValue
     * @return setter Method or NULL if not found
     */
    public static Method findSetter(Class<?> testClazz, Field field, Object propertyValue) {
        String propertyName = field.getName();
        boolean isStatic = Modifier.isStatic(field.getModifiers());

        String setterName = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);

        List<Method> methods = new ArrayList<Method>(Arrays.asList(testClazz.getMethods()));
        methods.addAll(Arrays.asList(testClazz.getDeclaredMethods()));
        Method unpreferredValidMethod = null;
        for (Method clazzMethod : methods) {
            if (clazzMethod.getName().equals(setterName)) {
                if (clazzMethod.getParameterTypes().length == 0) {
                    continue;
                }

                if (clazzMethod.getParameterTypes().length > 1) {
                    continue;
                }

                if (clazzMethod.getReturnType() != Void.TYPE) {
                    continue;
                }

                if (Modifier.isAbstract(clazzMethod.getModifiers())) {
                    continue;
                }

                Class methodParameterType = clazzMethod.getParameterTypes()[0];
                if (methodParameterType.isPrimitive() && propertyValue == null) {
                    continue;
                }

                // Method is static, but field isn't. Can't work
                if (!isStatic && Modifier.isStatic(clazzMethod.getModifiers())) {
                    continue;
                }

                if (!Modifier.isPublic(clazzMethod.getModifiers())) {
                    clazzMethod.setAccessible(true);
                }

                if (!isInstance(methodParameterType, propertyValue)) {
                    continue;
                }
                // not necessarily the BEST match
                else if (methodParameterType != propertyValue.getClass()) {
                    if (unpreferredValidMethod == null) {
                        unpreferredValidMethod = clazzMethod;
                    } else {
                        unpreferredValidMethod = getMostSpecificMethod(unpreferredValidMethod, clazzMethod);
                    }
                    continue;
                }

                return clazzMethod;
            }
        }

        return unpreferredValidMethod;
    }

    /**
     * Returns the most specific of the 2 methods. When calling this method both should
     * already match as valid calls, so the most specific will be the one where subtype
     * relation holds against the other.
     * <p/>
     * We also assume that the only difference between the method signatures is the type
     * of it's first argument, and thus that their first arguments can't be of the
     * same type.
     *
     * @param unpreferredValidMethod
     * @param clazzMethod
     * @return most specific method
     */
    public static Method getMostSpecificMethod(Method method1, Method method2) {
        Class<?> class1 = method1.getParameterTypes()[0];
        Class<?> class2 = method2.getParameterTypes()[0];

        if (class1.isAssignableFrom(class2)) {
            return method2;
        } else {
            return method1;
        }
    }
}
