/**
 *
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.xbean.recipe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import org.apache.xbean.ClassLoading;

/**
 * @version $Rev: 6688 $ $Date: 2005-12-29T02:08:29.200064Z $
 */
public class ObjectRecipe implements Recipe {
    private final String type;
    private final String factoryMethod;
    private final String[] constructorArgNames;
    private final Class[] constructorArgTypes;
    private final LinkedHashMap properties;

    public ObjectRecipe(Class type) {
        this(type.getName(), null, null, null, null);
    }

    public ObjectRecipe(Class type, String factoryMethod) {
        this(type.getName(), factoryMethod, null, null, null);
    }

    public ObjectRecipe(Class type, Map properties) {
        this(type.getName(), null, null, null, properties);
    }

    public ObjectRecipe(Class type, String[] constructorArgNames, Class[] constructorArgTypes) {
        this(type.getName(), null, constructorArgNames, constructorArgTypes, null);
    }

    public ObjectRecipe(Class type, String factoryMethod, String[] constructorArgNames, Class[] constructorArgTypes) {
        this(type.getName(), factoryMethod, constructorArgNames, constructorArgTypes, null);
    }

    public ObjectRecipe(String type, String factoryMethod, String[] constructorArgNames, Class[] constructorArgTypes, Map properties) {
        this.type = type;
        this.factoryMethod = factoryMethod;
        if (constructorArgNames != null) {
            this.constructorArgNames = constructorArgNames;
        } else {
            this.constructorArgNames = new String[0];
        }
        if (constructorArgTypes != null) {
            this.constructorArgTypes = constructorArgTypes;
        } else {
            this.constructorArgTypes = new Class[0];
        }
        if (properties != null) {
            this.properties = new LinkedHashMap(properties);
            setAllProperties(properties);
        } else {
            this.properties = new LinkedHashMap();
        }
    }

    public Object getProperty(String name) {
        if (name == null) throw new NullPointerException("name is null");
        Object value = properties.get(name);
        return value;
    }

    public void setProperty(String name, Object value) {
        if (name == null) throw new NullPointerException("name is null");
        if (!RecipeHelper.isSimpleType(value)) {
            value = new ValueRecipe(value);
        }
        properties.put(name, value);
    }

    public void setAllProperties(Map map) {
        if (map == null) throw new NullPointerException("map is null");
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            setProperty(name, value);
        }
    }

    public Object create(ClassLoader classLoader) throws ConstructionException {
        // load the type class
        Class typeClass = null;
        try {
            typeClass = ClassLoading.loadClass(type, classLoader);
        } catch (ClassNotFoundException e) {
            throw new ConstructionException("Type class could not be found: " + type);
        }

        // verify that is is a class we can construct
        if (!Modifier.isPublic(typeClass.getModifiers())) {
            throw new ConstructionException("Class is not public: " + ClassLoading.getClassName(typeClass, true));
        }
        if (Modifier.isInterface(typeClass.getModifiers())) {
            throw new ConstructionException("Class is an interface: " + ClassLoading.getClassName(typeClass, true));
        }
        if (Modifier.isAbstract(typeClass.getModifiers())) {
            throw new ConstructionException("Class is abstract: " + ClassLoading.getClassName(typeClass, true));
        }

        // get object values for all recipe properties
        Map propertyValues = new LinkedHashMap(properties);
        for (Iterator iterator = propertyValues.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object value = entry.getValue();
            if (value instanceof Recipe) {
                Recipe recipe = ((Recipe) value);
                value = recipe.create(classLoader);
                entry.setValue(value);
            }
        }

        // get the constructor parameters
        Object[] parameters = extractConstructorArgs(propertyValues);

        // create the instance
        Object instance = createInstance(typeClass, parameters);

        // set remaining properties
        for (Iterator iterator = propertyValues.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String propertyName = (String) entry.getKey();
            Object propertyValue = entry.getValue();
            Method setter = findSetter(typeClass, propertyName, propertyValue);
            try {
                setter.invoke(instance, new Object[]{propertyValue});
            } catch (Exception e) {
                throw new ConstructionException("Error setting property: " + setter);
            }
        }
        return instance;
    }

    private Object[] extractConstructorArgs(Map propertyValues) {
        Object[] parameters = new Object[constructorArgNames.length];
        for (int i = 0; i < constructorArgNames.length; i++) {
            String name = constructorArgNames[i];
            Class type = constructorArgTypes[i];

            Object value;
            if (propertyValues.containsKey(name)) {
                value = propertyValues.remove(name);
                if (!isInstance(type, value)) {
                    throw new ConstructionException("Invalid constructor parameter type: " +
                            "name=" + name + ", " +
                            "index=" + i + ", " +
                            "expected=" + ClassLoading.getClassName(type, true) + ", " +
                            "actual=" + ClassLoading.getClassName(value, true));
                }
            } else {
                value = getDefaultValue(type);
            }


            parameters[i] = value;
        }
        return parameters;
    }

    private static Object getDefaultValue(Class type) {
        if (type.equals(Boolean.TYPE)) {
            return Boolean.FALSE;
        } else if (type.equals(Character.TYPE)) {
            return new Character((char) 0);
        } else if (type.equals(Byte.TYPE)) {
            return new Byte((byte) 0);
        } else if (type.equals(Short.TYPE)) {
            return new Short((short) 0);
        } else if (type.equals(Integer.TYPE)) {
            return new Integer(0);
        } else if (type.equals(Long.TYPE)) {
            return new Long(0);
        } else if (type.equals(Float.TYPE)) {
            return new Float(0);
        } else if (type.equals(Double.TYPE)) {
            return new Double(0);
        }
        return null;
    }

    private Object createInstance(Class typeClass, Object[] parameters) {
        if (factoryMethod != null) {
            Method method = selectFactory(typeClass);
            try {
                Object object = method.invoke(null, parameters);
                return object;
            } catch (Exception e) {
                Throwable t = e;
                if (e instanceof InvocationTargetException) {
                    InvocationTargetException invocationTargetException = (InvocationTargetException) e;
                    if (invocationTargetException.getCause() != null) {
                        t = invocationTargetException.getCause();
                    }
                }
                throw new ConstructionException("Error invoking factory method: " + method, t);
            }
        } else {
            Constructor constructor = selectConstructor(typeClass);

            try {
                Object object = constructor.newInstance(parameters);
                return object;
            } catch (Exception e) {
                Throwable t = e;
                if (e instanceof InvocationTargetException) {
                    InvocationTargetException invocationTargetException = (InvocationTargetException) e;
                    if (invocationTargetException.getCause() != null) {
                        t = invocationTargetException.getCause();
                    }
                }
                throw new ConstructionException("Error invoking constructor: " + constructor, t);
            }
        }
    }

    private Method selectFactory(Class typeClass) {
        try {
            Method method = typeClass.getMethod(factoryMethod, constructorArgTypes);

            if (!Modifier.isPublic(method.getModifiers())) {
                // this will never occur since private methods are not returned from
                // getMethod, but leave this here anyway, just to be safe
                throw new ConstructionException("Factory method is not public: " + method);
            }

            if (!Modifier.isStatic(method.getModifiers())) {
                throw new ConstructionException("Factory method is not static: " + method);
            }

            if (method.getReturnType().equals(Void.TYPE)) {
                throw new ConstructionException("Factory method does not return anything: " + method);
            }

            if (method.getReturnType().isPrimitive()) {
                throw new ConstructionException("Factory method returns a primitive type: " + method);
            }

            return method;
        } catch (NoSuchMethodException e) {
            // try to find a matching private method
            Method[] methods = typeClass.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getName().equals(factoryMethod) && isAssignableFrom(constructorArgTypes, method.getParameterTypes())) {
                    if (!Modifier.isPublic(method.getModifiers())) {
                        throw new ConstructionException("Factory method is not public: " + method);
                    }
                }
            }

            StringBuffer buffer = new StringBuffer("Unable to find a valid factory method: ");
            buffer.append("public static Object ").append(ClassLoading.getClassName(typeClass, true)).append(".");
            buffer.append(factoryMethod).append(toParameterList(constructorArgTypes));
            throw new ConstructionException(buffer.toString());
        }
    }

    private Constructor selectConstructor(Class typeClass) {
        try {
            Constructor constructor = typeClass.getConstructor(constructorArgTypes);

            if (!Modifier.isPublic(constructor.getModifiers())) {
                // this will never occur since private constructors are not returned from
                // getConstructor, but leave this here anyway, just to be safe
                throw new ConstructionException("Constructor is not public: " + constructor);
            }

            return constructor;
        } catch (NoSuchMethodException e) {
            // try to find a matching private method
            Constructor[] constructors = typeClass.getDeclaredConstructors();
            for (int i = 0; i < constructors.length; i++) {
                Constructor constructor = constructors[i];
                if (isAssignableFrom(constructorArgTypes, constructor.getParameterTypes())) {
                    if (!Modifier.isPublic(constructor.getModifiers())) {
                        throw new ConstructionException("Constructor is not public: " + constructor);
                    }
                }
            }

            StringBuffer buffer = new StringBuffer("Unable to find a valid constructor: ");
            buffer.append("constructor= public ").append(ClassLoading.getClassName(typeClass, true));
            buffer.append(toParameterList(constructorArgTypes));
            throw new ConstructionException(buffer.toString());
        }
    }

    private String toParameterList(Class[] parameterTypes) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        for (int i = 0; i < parameterTypes.length; i++) {
            Class type = parameterTypes[i];
            if (i > 0) buffer.append(", ");
            buffer.append(ClassLoading.getClassName(type, true));
        }
        buffer.append(")");
        return buffer.toString();
    }

    public static Method findSetter(Class typeClass, String propertyName, Object propertyValue) {
        if (propertyName == null) throw new NullPointerException("name is null");
        if (propertyName.length() == 0) throw new IllegalArgumentException("name is an empty string");

        String setterName = "set" + Character.toUpperCase(propertyName.charAt(0));
        if (propertyName.length() > 0) {
            setterName += propertyName.substring(1);
        }

        int matchLevel = 0;
        ConstructionException missException = null;

        List methods = new ArrayList(Arrays.asList(typeClass.getMethods()));
        methods.addAll(Arrays.asList(typeClass.getDeclaredMethods()));
        for (Iterator iterator = methods.iterator(); iterator.hasNext();) {
            Method method = (Method) iterator.next();
            if (method.getName().equals(setterName)) {
                if (method.getParameterTypes().length == 0) {
                    if (matchLevel < 1) {
                        matchLevel = 1;
                        missException = new ConstructionException("Setter takes no parameters: " + method);
                    }
                    continue;
                }

                if (method.getParameterTypes().length > 1) {
                    if (matchLevel < 1) {
                        matchLevel = 1;
                        missException = new ConstructionException("Setter takes more then one parameter: " + method);
                    }
                    continue;
                }

                if (method.getReturnType() != Void.TYPE) {
                    if (matchLevel < 2) {
                        matchLevel = 2;
                        missException = new ConstructionException("Setter returns a value: " + method);
                    }
                    continue;
                }

                if (Modifier.isAbstract(method.getModifiers())) {
                    if (matchLevel < 3) {
                        matchLevel = 3;
                        missException = new ConstructionException("Setter is abstract: " + method);
                    }
                    continue;
                }

                if (!Modifier.isPublic(method.getModifiers())) {
                    if (matchLevel < 4) {
                        matchLevel = 4;
                        missException = new ConstructionException("Setter is not public: " + method);
                    }
                    continue;
                }

                if (Modifier.isStatic(method.getModifiers())) {
                    if (matchLevel < 4) {
                        matchLevel = 4;
                        missException = new ConstructionException("Setter is static: " + method);
                    }
                    continue;
                }

                Class methodParameterType = method.getParameterTypes()[0];
                if (methodParameterType.isPrimitive() && propertyValue == null) {
                    if (matchLevel < 6) {
                        matchLevel = 6;
                        missException = new ConstructionException("Null can not be assigned to " +
                                ClassLoading.getClassName(methodParameterType, true) + ": " + method);
                    }
                    continue;
                }

                if (!isInstance(methodParameterType, propertyValue)) {
                    if (matchLevel < 5) {
                        matchLevel = 5;
                        missException = new ConstructionException(ClassLoading.getClassName(propertyValue, true) + " can not be assigned to " +
                                ClassLoading.getClassName(methodParameterType, true) + ": " + method);
                    }
                    continue;
                }
                return method;
            }

        }

        if (missException != null) {
            throw missException;
        } else {
            StringBuffer buffer = new StringBuffer("Unable to find a valid setter method: ");
            buffer.append("public void ").append(ClassLoading.getClassName(typeClass, true)).append(".");
            buffer.append(setterName).append("(").append(ClassLoading.getClassName(propertyValue, true)).append(")");
            throw new ConstructionException(buffer.toString());
        }
    }

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

    public static boolean isAssignableFrom(Class expected, Class actual) {
        if (expected.isPrimitive()) {
            // verify actual is the correct wrapper type
            if (expected.equals(boolean.class)) {
                return actual.equals(Boolean.class);
            } else if (expected.equals(char.class)) {
                return actual.equals(Character.class);
            } else if (expected.equals(byte.class)) {
                return actual.equals(Byte.class);
            } else if (expected.equals(short.class)) {
                return actual.equals(Short.class);
            } else if (expected.equals(int.class)) {
                return actual.equals(Integer.class);
            } else if (expected.equals(long.class)) {
                return actual.equals(Long.class);
            } else if (expected.equals(float.class)) {
                return actual.equals(Float.class);
            } else if (expected.equals(double.class)) {
                return actual.equals(Double.class);
            } else {
                throw new AssertionError("Invalid primitve type: " + expected);
            }
        }

        return expected.isAssignableFrom(actual);
    }

    public static boolean isAssignableFrom(Class[] expectedTypes, Class[] actualTypes) {
        if (expectedTypes.length != actualTypes.length) {
            return false;
        }
        for (int i = 0; i < expectedTypes.length; i++) {
            Class expectedType = expectedTypes[i];
            Class actualType = actualTypes[i];
            if (!isAssignableFrom(expectedType,  actualType)) {
                 return false;
            }
        }
        return true;
    }
}
