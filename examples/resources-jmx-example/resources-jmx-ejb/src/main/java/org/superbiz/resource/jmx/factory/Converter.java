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
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.superbiz.resource.jmx.factory;


import java.beans.PropertyEditor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Can convert anything with a:
 * - PropertyEditor
 * - Constructor that accepts String
 * - public static method that returns itself and takes a String
 *
 * @version $Revision$ $Date$
 */
public class Converter {

    private Converter() {
    }

    public static Object convert(final Object value, Class<?> targetType, final String name) {
        if (value == null) {
            if (targetType.equals(Boolean.TYPE)) {
                return false;
            }
            return value;
        }

        final Class<? extends Object> actualType = value.getClass();

        if (targetType.isPrimitive()) {
            targetType = PrimitiveTypes.valueOf(targetType.toString().toUpperCase()).getWraper();
        }

        if (targetType.isAssignableFrom(actualType)) {
            return value;
        }

        if (Number.class.isAssignableFrom(actualType) && Number.class.isAssignableFrom(targetType)) {
            return value;
        }

        if (!(value instanceof String)) {
            final String message = String.format("Expected type '%s' for '%s'. Found '%s'", targetType.getName(), name, actualType.getName());
            throw new IllegalArgumentException(message);
        }

        final String stringValue = (String) value;

        if (Enum.class.isAssignableFrom(targetType)) {
            final Class<? extends Enum> enumType = (Class<? extends Enum>) targetType;
            try {
                return Enum.valueOf(enumType, stringValue);
            } catch (final IllegalArgumentException e) {
                try {
                    return Enum.valueOf(enumType, stringValue.toUpperCase());
                } catch (final IllegalArgumentException e1) {
                    return Enum.valueOf(enumType, stringValue.toLowerCase());
                }
            }
        }

        try {
            // Force static initializers to run
            Class.forName(targetType.getName(), true, targetType.getClassLoader());
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        }

        final PropertyEditor editor = Editors.get(targetType);

        if (editor == null) {
            final Object result = create(targetType, stringValue);
            if (result != null) {
                return result;
            }
        }

        if (editor == null) {
            final String message = String.format("Cannot convert to '%s' for '%s'. No PropertyEditor", targetType.getName(), name);
            throw new IllegalArgumentException(message);
        }

        editor.setAsText(stringValue);
        return editor.getValue();
    }

    private static Object create(final Class<?> type, final String value) {
        try {
            final Constructor<?> constructor = type.getConstructor(String.class);
            return constructor.newInstance(value);
        } catch (final NoSuchMethodException e) {
            // fine
        } catch (final Exception e) {
            final String message = String.format("Cannot convert string '%s' to %s.", value, type);
            throw new IllegalArgumentException(message, e);
        }

        for (final Method method : type.getMethods()) {
            if (isInvalidMethod(type, method)) {
                continue;
            }

            try {
                return method.invoke(null, value);
            } catch (final Exception e) {
                final String message = String.format("Cannot convert string '%s' to %s.", value, type);
                throw new IllegalStateException(message, e);
            }
        }

        return null;
    }

    private static boolean isInvalidMethod(Class<?> type, Method method) {
        if (!Modifier.isStatic(method.getModifiers()) ||
                !Modifier.isPublic(method.getModifiers()) ||
                !method.getReturnType().equals(type) ||
                !method.getParameterTypes()[0].equals(String.class) ||
                method.getParameterTypes().length != 1) {
            return true;
        }
        return false;
    }
}