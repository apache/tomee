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

import java.beans.Introspector;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class IntrospectionSupport {

    public static boolean getProperties(final Object target, final Map<String, String> props,
                                        String optionPrefix) {

        boolean rc = false;
        if (target == null) {
            throw new IllegalArgumentException("target was null.");
        }
        if (props == null) {
            throw new IllegalArgumentException("props was null.");
        }

        if (optionPrefix == null) {
            optionPrefix = "";
        }

        final Class clazz = target.getClass();
        final Method[] methods = clazz.getMethods();
        for (final Method method : methods) {
            String name = method.getName();
            final Class type = method.getReturnType();
            final Class[] params = method.getParameterTypes();
            if (name.startsWith("get") && params.length == 0 && type != null
                    && isSettableType(type)) {

                try {

                    final Object value = method.invoke(target, new Object[]{});
                    if (value == null) {
                        continue;
                    }

                    final String strValue = convertToString(value, type);
                    if (strValue == null) {
                        continue;
                    }

                    name = name.substring(3, 4).toLowerCase()
                            + name.substring(4);
                    props.put(optionPrefix + name, strValue);
                    rc = true;

                } catch (final Throwable ignore) {
                    // no-op
                }

            }
        }

        return rc;
    }

    public static boolean setProperties(final Object target, final Map props,
                                        final String optionPrefix) {
        boolean rc = false;
        if (target == null) {
            throw new IllegalArgumentException("target was null.");
        }
        if (props == null) {
            throw new IllegalArgumentException("props was null.");
        }

        for (final Iterator iter = props.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            if (name.startsWith(optionPrefix)) {
                final Object value = props.get(name);
                name = name.substring(optionPrefix.length());
                if (setProperty(target, name, value)) {
                    iter.remove();
                    rc = true;
                }
            }
        }
        return rc;
    }

    public static Map extractProperties(final Map props, final String optionPrefix) {
        if (props == null) {
            throw new IllegalArgumentException("props was null.");
        }

        final HashMap<String, Object> rc = new HashMap<>(props.size());

        for (final Iterator iter = props.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            if (name.startsWith(optionPrefix)) {
                final Object value = props.get(name);
                name = name.substring(optionPrefix.length());
                rc.put(name, value);
                iter.remove();
            }
        }

        return rc;
    }

    public static boolean setProperties(final Object target, final Map props) {
        boolean rc = false;

        if (target == null) {
            throw new IllegalArgumentException("target was null.");
        }
        if (props == null) {
            throw new IllegalArgumentException("props was null.");
        }

        for (final Iterator iter = props.entrySet().iterator(); iter.hasNext(); ) {
            final Map.Entry entry = (Entry) iter.next();
            if (setProperty(target, (String) entry.getKey(), entry.getValue())) {
                iter.remove();
                rc = true;
            }
        }

        return rc;
    }

    private static boolean setProperty(final Object target, final String name, final Object value) {
        try {
            final Class clazz = target.getClass();
            final Method setter = findSetterMethod(clazz, name);
            if (setter == null) {
                return false;
            }

            // If the type is null or it matches the needed type, just use the
            // value directly
            if (value == null
                || value.getClass() == setter.getParameterTypes()[0]) {
                setter.invoke(target, new Object[]{value});
            } else {
                // We need to convert it
                setter.invoke(target, new Object[]{convert(value, setter
                    .getParameterTypes()[0])});
            }
            return true;
        } catch (final Throwable ignore) {
            return false;
        }
    }

    private static Object convert(final Object value, final Class type)
        throws URISyntaxException {
        final PropertyEditor editor = PropertyEditorManager.findEditor(type);
        if (editor != null) {
            editor.setAsText(value.toString());
            return editor.getValue();
        }
        if (type == URI.class) {
            return URLs.uri(value.toString());
        }
        return null;
    }

    private static String convertToString(final Object value, final Class type)
        throws URISyntaxException {
        final PropertyEditor editor = PropertyEditorManager.findEditor(type);
        if (editor != null) {
            editor.setValue(value);
            return editor.getAsText();
        }
        if (type == URI.class) {
            return ((URI) value).toString();
        }
        return null;
    }

    private static Method findSetterMethod(final Class clazz, String name) {
        // Build the method name.
        name = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
        final Method[] methods = clazz.getMethods();
        for (final Method method : methods) {
            final Class[] params = method.getParameterTypes();
            if (method.getName().equals(name) && params.length == 1
                    && isSettableType(params[0])) {
                return method;
            }
        }
        return null;
    }

    private static boolean isSettableType(final Class clazz) {
        if (PropertyEditorManager.findEditor(clazz) != null) {
            return true;
        }
        if (clazz == URI.class) {
            return true;
        }
        if (clazz == Boolean.class) {
            return true;
        }
        return false;
    }

    public static String toString(final Object target) {
        return toString(target, Object.class);
    }

    public static String toString(final Object target, final Class stopClass) {
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        addFields(target, target.getClass(), stopClass, map);
        final StringBuilder buffer = new StringBuilder(simpleName(target.getClass()));
        buffer.append(" {");
        final Set entrySet = map.entrySet();
        boolean first = true;
        for (Object o : entrySet) {
            final Entry entry = (Entry) o;
            if (first) {
                first = false;
            } else {
                buffer.append(", ");
            }
            buffer.append(entry.getKey());
            buffer.append(" = ");
            appendToString(buffer, entry.getValue());
        }
        buffer.append("}");
        return buffer.toString();
    }

    protected static void appendToString(final StringBuilder buffer, final Object value) {
        buffer.append(value);
    }

    public static String simpleName(final Class clazz) {
        String name = clazz.getName();
        final int p = name.lastIndexOf('.');
        if (p >= 0) {
            name = name.substring(p + 1);
        }
        return name;
    }

    private static void addFields(final Object target, final Class startClass,
                                  final Class stopClass, final LinkedHashMap<String, Object> map) {

        if (startClass != stopClass) {
            addFields(target, startClass.getSuperclass(), stopClass, map);
        }

        final Field[] fields = startClass.getDeclaredFields();
        for (final Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())
                    || Modifier.isTransient(field.getModifiers())
                    || Modifier.isPrivate(field.getModifiers())) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object o = field.get(target);
                if (o != null && o.getClass().isArray()) {
                    try {
                        o = Arrays.asList((Object[]) o);
                    } catch (final Throwable e) {
                        // no-op
                    }
                }
                map.put(field.getName(), o);
            } catch (final Throwable e) {
                e.printStackTrace();
            }
        }

    }

    public static Class getPropertyType(Class clazz, final String propertyName) throws NoSuchFieldException {
        do {
            try {
                return clazz.getDeclaredField(propertyName).getType();
            } catch (final NoSuchFieldException e) {
                //look at superclass
            }
            for (final Method method : clazz.getDeclaredMethods()) {
                if (method.getReturnType() == void.class && method.getParameterTypes().length == 1) {
                    final String methodName = method.getName();
                    if (methodName.startsWith("set")) {
                        final String type = Introspector.decapitalize(methodName.substring(3));
                        if (propertyName.equals(type)) {
                            return method.getParameterTypes()[0];
                        }
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null);

        throw new NoSuchFieldException(propertyName);
    }
}
