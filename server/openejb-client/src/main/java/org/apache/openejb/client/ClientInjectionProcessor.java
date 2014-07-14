/**
 *
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
package org.apache.openejb.client;

import javax.naming.Context;
import javax.naming.NamingException;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientInjectionProcessor<T> {

    private static final Logger logger = Logger.getLogger("OpenEJB.client");

    private final Class<? extends T> beanClass;
    private final ClassLoader classLoader;
    private final List<Injection> injections;
    private final List<CallbackMetaData> postConstructCallbacks;
    private final List<CallbackMetaData> preDestroyCallbacks;
    private final Context context;
    private T instance;
    private boolean allowStatic;

    public ClientInjectionProcessor(final Class<? extends T> beanClass,
                                    final List<Injection> injections,
                                    final List<CallbackMetaData> postConstructMethods,
                                    final List<CallbackMetaData> preDestroyMethods,
                                    final Context context) {
        this.beanClass = beanClass;
        classLoader = beanClass.getClassLoader();
        this.injections = injections;
        this.postConstructCallbacks = postConstructMethods;
        this.preDestroyCallbacks = preDestroyMethods;
        this.context = context;
    }

    public void allowStatic() {
        allowStatic = true;
    }

    public T createInstance() throws Exception {
        if (instance == null) {
            construct();
        }
        return instance;
    }

    public T getInstance() {
        return instance;
    }

    private void construct() {
        final Map<Injection, Object> values = new HashMap<Injection, Object>();
        for (final Injection injection : injections) {
            // only process injections for this class
            final Class<?> targetClass = loadClass(injection.getTargetClass());
            if (targetClass == null) {
                continue;
            }
            if (!targetClass.isAssignableFrom(beanClass)) {
                continue;
            }

            try {
                final String jndiName = injection.getJndiName();
                final Object object = context.lookup("java:comp/env/" + jndiName);
                values.put(injection, object);
            } catch (NamingException e) {
                logger.warning("Injection data not found in JNDI context: jndiName='" +
                    injection.getJndiName() +
                    "', target=" +
                    injection.getTargetClass() +
                    "/" +
                    injection.getName());
            }
        }

        try {
            instance = beanClass.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Error while creating bean " + beanClass.getName(), e);
        }

        final List<String> unsetProperties = new ArrayList<String>();
        for (final Map.Entry<Injection, Object> entry : values.entrySet()) {
            final Injection injection = entry.getKey();
            final Object value = entry.getValue();

            final Class<?> targetClass = loadClass(injection.getTargetClass());
            if (targetClass == null || !targetClass.isAssignableFrom(beanClass)) {
                continue;
            }

            if (!setProperty(targetClass, injection.getName(), value)) {
                unsetProperties.add(injection.getName());
            }
        }

        if (unsetProperties.size() > 0) {
            for (final Object property : unsetProperties) {
                logger.warning("Injection: Unable to set property '" + property + "' in class " + beanClass.getName());
            }
        }
    }

    public void postConstruct() throws Exception {
        if (instance == null) {
            throw new IllegalStateException("Instance has not been constructed");
        }
        if (postConstructCallbacks == null) {
            return;
        }

        for (final Method postConstruct : toMethod(postConstructCallbacks)) {
            try {
                postConstruct.invoke(instance);
            } catch (Exception e) {
                e = unwrap(e);
                throw new Exception("Error while calling post construct method", e);
            }
        }
    }

    public void preDestroy() {
        if (instance == null) {
            return;
        }
        if (preDestroyCallbacks == null) {
            return;
        }
        for (final Method preDestroy : toMethod(preDestroyCallbacks)) {
            try {
                preDestroy.invoke(instance);
            } catch (Exception e) {
                e = unwrap(e);
                logger.log(Level.SEVERE, "Error while calling pre destroy method", e);
            }
        }
    }

    private List<Method> toMethod(final List<CallbackMetaData> callbacks) {
        final List<String> methodsNotFound = new ArrayList<String>(1);
        final List<Method> methods = new ArrayList<Method>(callbacks.size());
        for (final CallbackMetaData callback : callbacks) {
            final Method method = toMethod(callback);
            if (method != null) {
                methods.add(method);
            } else {
                methodsNotFound.add(callback.toString());
            }
        }
        if (!methodsNotFound.isEmpty()) {
            throw new IllegalStateException("Callback methods not found " + methodsNotFound);
        }
        return methods;
    }

    private Method toMethod(final CallbackMetaData callback) {
        try {
            final String className = callback.getClassName();
            final Class<?> clazz = classLoader.loadClass(className);
            return clazz.getDeclaredMethod(callback.getMethod());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean setProperty(final Class clazz, final String name, Object propertyValue) {
        final Method method = findSetter(clazz, name, propertyValue);
        if (method != null) {
            try {
                propertyValue = convert(method.getParameterTypes()[0], propertyValue);
                method.invoke(instance, propertyValue);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        final Field field = findField(clazz, name, propertyValue);
        if (field != null) {
            try {
                propertyValue = convert(field.getType(), propertyValue);
                field.set(instance, propertyValue);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    public Method findSetter(final Class typeClass, final String propertyName, final Object propertyValue) {
        if (propertyName == null) {
            throw new NullPointerException("name is null");
        }
        if (propertyName.length() == 0) {
            throw new IllegalArgumentException("name is an empty string");
        }

        String setterName = "set" + Character.toUpperCase(propertyName.charAt(0));
        if (propertyName.length() > 0) {
            setterName += propertyName.substring(1);
        }

        final List<Method> methods = new ArrayList<Method>(Arrays.asList(typeClass.getMethods()));
        methods.addAll(Arrays.asList(typeClass.getDeclaredMethods()));
        for (final Method method : methods) {
            if (method.getName().equals(setterName)) {
                if (method.getParameterTypes().length == 0) {
                    continue;
                }

                if (method.getParameterTypes().length > 1) {
                    continue;
                }

                if (method.getReturnType() != Void.TYPE) {
                    continue;
                }

                if (Modifier.isAbstract(method.getModifiers())) {
                    continue;
                }

                if (!allowStatic && Modifier.isStatic(method.getModifiers())) {
                    continue;
                }

                final Class methodParameterType = method.getParameterTypes()[0];
                if (methodParameterType.isPrimitive() && propertyValue == null) {
                    continue;
                }

                if (!isInstance(methodParameterType, propertyValue) && !isConvertable(methodParameterType, propertyValue)) {
                    continue;
                }

                if (!Modifier.isPublic(method.getModifiers())) {
                    setAccessible(method);
                }

                return method;
            }

        }
        return null;
    }

    public Field findField(final Class typeClass, final String propertyName, final Object propertyValue) {
        if (propertyName == null) {
            throw new NullPointerException("name is null");
        }
        if (propertyName.length() == 0) {
            throw new IllegalArgumentException("name is an empty string");
        }

        final List<Field> fields = new ArrayList<Field>(Arrays.asList(typeClass.getDeclaredFields()));
        Class parent = typeClass.getSuperclass();
        while (parent != null) {
            fields.addAll(Arrays.asList(parent.getDeclaredFields()));
            parent = parent.getSuperclass();
        }

        for (final Field field : fields) {
            if (field.getName().equals(propertyName)) {

                if (!allowStatic && Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                final Class fieldType = field.getType();
                if (fieldType.isPrimitive() && propertyValue == null) {
                    continue;
                }

                if (!isInstance(fieldType, propertyValue) && !isConvertable(fieldType, propertyValue)) {
                    continue;
                }

                if (!Modifier.isPublic(field.getModifiers())) {
                    setAccessible(field);
                }

                return field;
            }

        }
        return null;
    }

    private static void setAccessible(final AccessibleObject accessibleObject) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                accessibleObject.setAccessible(true);
                return null;
            }
        });
    }

    private static boolean isInstance(final Class type, final Object instance) {
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

    private static boolean isConvertable(final Class type, final Object propertyValue) {
        return (propertyValue instanceof String && findEditor(type) != null);
    }

    private Object convert(final Class type, Object value) {
        if (type == Object.class || !(value instanceof String)) {
            return value;
        }

        final String stringValue = (String) value;
        final PropertyEditor editor = findEditor(type);
        if (editor != null) {
            editor.setAsText(stringValue);
            value = editor.getValue();
        }
        return value;
    }

    /**
     * Locate a property editor for qiven class of object.
     *
     * @param type The target object class of the property.
     * @return The resolved editor, if any.  Returns null if a suitable editor
     * could not be located.
     */
    private static PropertyEditor findEditor(final Class type) {
        if (type == null) {
            throw new NullPointerException("type is null");
        }

        // try to locate this directly from the editor manager first.
        final PropertyEditor editor = PropertyEditorManager.findEditor(type);

        // we're outta here if we got one.
        if (editor != null) {
            return editor;
        }

        // nothing found
        return null;
    }

    private Class<?> loadClass(final String targetClass) {
        try {
            return classLoader.loadClass(targetClass);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static Exception unwrap(Exception e) {
        if (e instanceof InvocationTargetException && e.getCause() instanceof Exception) {
            e = (Exception) e.getCause();
        }
        return e;
    }
}
