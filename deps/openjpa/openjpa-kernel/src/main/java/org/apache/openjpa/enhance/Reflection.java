/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.enhance;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.ReferenceMap;
import org.apache.openjpa.lib.util.Reflectable;
import org.apache.openjpa.lib.util.Localizer.Message;
import org.apache.openjpa.lib.util.concurrent.ConcurrentReferenceHashMap;
import org.apache.openjpa.util.GeneralException;
import org.apache.openjpa.util.UserException;

/**
 * Reflection utilities used to support and augment enhancement.  Used both
 * at enhancement time and at runtime.
 *
 * @author Abe White
 */
public class Reflection {

    private static final Localizer _loc = Localizer.forPackage
        (Reflection.class);

    // Weak HashMap caches of getter/setter/beanProperty methods
    private static Map<Class<?>, Map<String, Method>> getterMethodCache = 
        new ConcurrentReferenceHashMap(ReferenceMap.WEAK, ReferenceMap.HARD);
    private static Map<Class<?>, Map<String, Method>> setterMethodCache = 
        new ConcurrentReferenceHashMap(ReferenceMap.WEAK, ReferenceMap.HARD);
    private static Map<Class<?>, Set<String>> beanPropertiesNameCache = 
        new ConcurrentReferenceHashMap(ReferenceMap.WEAK, ReferenceMap.HARD);
    
    private static Method getGetterMethod(Class<?> cls, String prop) {
        Method rtnMethod = null;
        Map<String, Method> clsMap = getterMethodCache.get(cls);
        if (clsMap != null) {
            rtnMethod = clsMap.get(prop);
        }
        return rtnMethod;
    }

    private static void setGetterMethod(Class<?> cls, String prop,
        Method method) {
        Map<String, Method> clsMap = getterMethodCache.get(cls);
        if (clsMap == null) {
            clsMap = new ConcurrentReferenceHashMap(ReferenceMap.HARD,
                ReferenceMap.WEAK);
            getterMethodCache.put(cls, clsMap);
        }
        clsMap.put(prop, method);
    }

    private static Method getSetterMethod(Class<?> cls, String prop) {
        Method rtnMethod = null;
        Map<String, Method> clsMap = setterMethodCache.get(cls);
        if (clsMap != null) {
            rtnMethod = clsMap.get(prop);
        }
        return rtnMethod;
    }

    private static void setSetterMethod(Class<?> cls, String prop,
        Method method) {
        Map<String, Method> clsMap = setterMethodCache.get(cls);
        if (clsMap == null) {
            clsMap = new ConcurrentReferenceHashMap(ReferenceMap.HARD,
                ReferenceMap.WEAK);
            setterMethodCache.put(cls, clsMap);
        }
        clsMap.put(prop, method);
    }

    /**
     * Return the getter method matching the given property name, optionally
     * throwing an exception if none.
     */
    public static Method findGetter(Class cls, String prop, boolean mustExist) {
        Method m = getGetterMethod(cls, prop);
        if (m != null) {
            return m;
        }
        String capProp = StringUtils.capitalize(prop);
        try {
            // this algorithm searches for a get<prop> or is<prop> method in
            // a breadth-first manner.
            for (Class c = cls; c != null && c != Object.class;
                c = c.getSuperclass()) {
                m = getDeclaredMethod(c, "get" + capProp, null);
                if (m != null) {
                    setGetterMethod(cls, prop, m);
                    return m;
                } else {
                    m = getDeclaredMethod(c, "is" + capProp, null);
                    if (m != null && (m.getReturnType() == boolean.class
                        || m.getReturnType() == Boolean.class)) {
                        setGetterMethod(cls, prop, m);
                        return m;
                    }
                }
            }
        } catch (Exception e) {
            throw new GeneralException(e);
        }

        if (mustExist)
            throw new UserException(_loc.get("bad-getter", cls, prop));
        return null;
    }

    /**
     * Return the setter method matching the given property name, optionally
     * throwing an exception if none.  The property must also have a getter.
     */
    public static Method findSetter(Class cls, String prop, boolean mustExist) {
        Method getter = findGetter(cls, prop, mustExist);
        return (getter == null) ? null 
            : findSetter(cls, prop, getter.getReturnType(), mustExist);
    }

    /**
     * Return the setter method matching the given property name, optionally
     * throwing an exception if none.
     */
    public static Method findSetter(Class cls, String prop, Class param,
        boolean mustExist) {
        Method m = getSetterMethod(cls, prop);
        if (m != null) {
            return m;
        }
        String name = "set" + StringUtils.capitalize(prop);
        try {
            for (Class c = cls; c != null && c != Object.class;
                c = c.getSuperclass()) {
                m = getDeclaredMethod(c, name, param);
                if (m != null) {
                    setSetterMethod(cls, prop, m);
                    return m;
                }
            }
        } catch (Exception e) {
            throw new GeneralException(e);
        }

        if (mustExist)
            throw new UserException(_loc.get("bad-setter", cls, prop));
        return null;
    }

    /**
     * Invokes <code>cls.getDeclaredMethods()</code>, and returns the method
     * that matches the <code>name</code> and <code>param</code> arguments.
     * Avoids the exception thrown by <code>Class.getDeclaredMethod()</code>
     * for performance reasons. <code>param</code> may be null. Additionally,
     * if there are multiple methods with different return types, this will
     * return the method defined in the least-derived class.
     *
     * @since 0.9.8
     */
    static Method getDeclaredMethod(Class cls, String name,
        Class param) {
        Method[] methods = (Method[]) AccessController.doPrivileged(
            J2DoPrivHelper.getDeclaredMethodsAction(cls));
        Method candidate = null;
        for (int i = 0 ; i < methods.length; i++) {
    	    if (name.equals(methods[i].getName())) {
                Class[] methodParams = methods[i].getParameterTypes();
                if (param == null && methodParams.length == 0)
                    candidate = mostDerived(methods[i], candidate);
                else if (param != null && methodParams.length == 1
                    && param.equals(methodParams[0]))
                    candidate = mostDerived(methods[i], candidate);
            }
        }
        return candidate;
    }

    static Method mostDerived(Method meth1, Method meth2) {
        if (meth1 == null)
            return meth2;
        if (meth2 == null)
            return meth1;
        
        Class cls2 = meth2.getDeclaringClass();
        Class cls1 = meth1.getDeclaringClass();

        if (cls1.equals(cls2)) {
            Class ret1 = meth1.getReturnType();
            Class ret2 = meth2.getReturnType();
            if (ret1.isAssignableFrom(ret2))
                return meth2;
            else if (ret2.isAssignableFrom(ret1))
                return meth1;
            else
                throw new IllegalArgumentException(
                    _loc.get("most-derived-unrelated-same-type", meth1, meth2)
                        .getMessage());
        } else {
            if (cls1.isAssignableFrom(cls2))
                return meth2;
            else if (cls2.isAssignableFrom(cls1))
                return meth1;
            else
                throw new IllegalArgumentException(
                    _loc.get("most-derived-unrelated", meth1, meth2)
                        .getMessage());
        }
    }

    /**
     * Return the field with the given name, optionally throwing an exception
     * if none.
     */
    public static Field findField(Class cls, String name, boolean mustExist) {
        try {
            Field f;
            for (Class c = cls; c != null && c != Object.class;
                c = c.getSuperclass()) {
                f = getDeclaredField(c, name);
                if (f != null)
                    return f;
            }
        } catch (Exception e) {
            throw new GeneralException(e);
        }

        if (mustExist)
            throw new UserException(_loc.get("bad-field", cls, name));
        return null;
    }

    /**
     * Invokes <code>cls.getDeclaredFields()</code>, and returns the field
     * that matches the <code>name</code> argument.  Avoids the exception
     * thrown by <code>Class.getDeclaredField()</code> for performance reasons.
     *
     * @since 0.9.8
     */
    private static Field getDeclaredField(Class cls, String name) {
        Field[] fields = AccessController.doPrivileged(
            J2DoPrivHelper.getDeclaredFieldsAction(cls));
        for (int i = 0 ; i < fields.length; i++) {
    	    if (name.equals(fields[i].getName()))
		        return fields[i];
        }
        return null;
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static Object get(Object target, Field field) {
        if (target == null || field == null)
            return null;
        makeAccessible(field, field.getModifiers());
        try {
            return field.get(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("get-field", target, field));
        }
    }
    
    /**
     * Get the value of the given named field or a corresponding getter method.
     * 
     * @return null if the field does not exist and mustExist is set to false or
     * the given target is null.
     * 
     * @exception UserException if mustExist is true and the field or getter 
     * method is non-existent
     */
    public static Object getValue(Object obj, String prop, boolean mustExist) {
    	if (obj == null)
    		return null;
    	Class cls = obj.getClass();
    	Field field = findField(cls, prop, false);
    	if (field != null)
    		return get(obj, field);
    	Method getter = findGetter(cls, prop, false);
    	if (getter != null)
    		return get(obj, getter);
        if (mustExist)
            throw new UserException(_loc.get("bad-field", cls, prop));
        return null; // should not reach
    }

    /**
     * Make the given member accessible if it isn't already.
     */
    private static void makeAccessible(AccessibleObject ao, int mods) {
        try {
            if (!Modifier.isPublic(mods) && !ao.isAccessible())
                AccessController.doPrivileged(J2DoPrivHelper
                    .setAccessibleAction(ao, true));
        } catch (SecurityException se) {
            throw new UserException(_loc.get("reflect-security", ao)).
                setFatal(true);
        }
    }

    /**
     * Wrap the given reflection exception as a runtime exception.
     */
    private static RuntimeException wrapReflectionException(Throwable t, Message message) {
        if (t instanceof InvocationTargetException)
            t = ((InvocationTargetException) t).getTargetException();  
        t.initCause(new IllegalArgumentException(message.getMessage()));
        if (t instanceof RuntimeException)
            return (RuntimeException) t;
        return new GeneralException(t);
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static boolean getBoolean(Object target, Field field) {
        if (target == null || field == null)
            return false;
        makeAccessible(field, field.getModifiers());
        try {
            return field.getBoolean(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("get-field", target, field));
        }
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static byte getByte(Object target, Field field) {
        if (target == null || field == null)
            return (byte) 0;
        makeAccessible(field, field.getModifiers());
        try {
            return field.getByte(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("get-field", target, field));
        }
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static char getChar(Object target, Field field) {
        if (target == null || field == null)
            return (char) 0;
        makeAccessible(field, field.getModifiers());
        try {
            return field.getChar(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("get-field", target, field));
        }
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static double getDouble(Object target, Field field) {
        if (target == null || field == null)
            return 0D;
        makeAccessible(field, field.getModifiers());
        try {
            return field.getDouble(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("get-field", target, field));
        }
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static float getFloat(Object target, Field field) {
        if (target == null || field == null)
            return 0F;
        makeAccessible(field, field.getModifiers());
        try {
            return field.getFloat(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("get-field", target, field));
        }
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static int getInt(Object target, Field field) {
        if (target == null || field == null)
            return 0;
        makeAccessible(field, field.getModifiers());
        try {
            return field.getInt(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("get-field", target, field));
        }
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static long getLong(Object target, Field field) {
        if (target == null || field == null)
            return 0L;
        makeAccessible(field, field.getModifiers());
        try {
            return field.getLong(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("get-field", target, field));
        }
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static short getShort(Object target, Field field) {
        if (target == null || field == null)
            return (short) 0;
        makeAccessible(field, field.getModifiers());
        try {
            return field.getShort(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("get-field", target, field));
        }
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static Object get(Object target, Method getter) {
        if (target == null || getter == null)
            return null;
        makeAccessible(getter, getter.getModifiers());
        try {
            return getter.invoke(target, (Object[]) null);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("get-method", target, getter));
        }
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static boolean getBoolean(Object target, Method getter) {
        Object o = get(target, getter);
        return (o == null) ? false : ((Boolean) o).booleanValue();
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static byte getByte(Object target, Method getter) {
        Object o = get(target, getter);
        return (o == null) ? (byte) 0 : ((Number) o).byteValue();
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static char getChar(Object target, Method getter) {
        Object o = get(target, getter);
        return (o == null) ? (char) 0 : ((Character) o).charValue();
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static double getDouble(Object target, Method getter) {
        Object o = get(target, getter);
        return (o == null) ? 0D : ((Number) o).doubleValue();
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static float getFloat(Object target, Method getter) {
        Object o = get(target, getter);
        return (o == null) ? 0F : ((Number) o).floatValue();
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static int getInt(Object target, Method getter) {
        Object o = get(target, getter);
        return (o == null) ? 0 : ((Number) o).intValue();
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static long getLong(Object target, Method getter) {
        Object o = get(target, getter);
        return (o == null) ? 0L : ((Number) o).longValue();
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static short getShort(Object target, Method getter) {
        Object o = get(target, getter);
        return (o == null) ? (short) 0 : ((Number) o).shortValue();
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, Object value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.set(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("set-field", new Object[]{target, field, value, 
                    value == null ? "" : value.getClass()}));
        }
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, boolean value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.setBoolean(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("set-field", new Object[]{target, field, value, "boolean"}));
        }
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, byte value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.setByte(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("set-field", new Object[]{target, field, value, "byte"}));
        }
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, char value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.setChar(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("set-field", new Object[]{target, field, value, "char"}));
        }
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, double value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.setDouble(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("set-field", new Object[]{target, field, value, "double"}));
        }
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, float value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.setFloat(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("set-field", new Object[]{target, field, value, "float"}));
        }
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, int value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.setInt(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("set-field", new Object[]{target, field, value, "int"}));
        }
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, long value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.setLong(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("set-field", new Object[]{target, field, value, "long"}));
        }
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, short value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.setShort(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("set-field", new Object[]{target, field, value, "short"}));
        }
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, Object value, Field field) {
        set(target, field, value);
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, boolean value, Field field) {
        set(target, field, value);
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, byte value, Field field) {
        set(target, field, value);
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, char value, Field field) {
        set(target, field, value);
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, double value, Field field) {
        set(target, field, value);
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, float value, Field field) {
        set(target, field, value);
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, int value, Field field) {
        set(target, field, value);
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, long value, Field field) {
        set(target, field, value);
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, short value, Field field) {
        set(target, field, value);
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, Object value) {
        if (target == null || setter == null)
            return;
        makeAccessible(setter, setter.getModifiers());
        try {
            setter.invoke(target, new Object[] { value });
        } catch (Throwable t) {
            throw wrapReflectionException(t, _loc.get("set-method", new Object[]{target, setter, value, 
                    value == null ? "" : value.getClass()}));
        }
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, boolean value) {
        set(target, setter, (value) ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, byte value) {
        set(target, setter, Byte.valueOf(value));
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, char value) {
        set(target, setter, Character.valueOf(value));
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, double value) {
        set(target, setter, new Double(value));
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, float value) {
        set(target, setter, new Float(value));
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, int value) {
        set(target, setter, Integer.valueOf(value));
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, long value) {
        set(target, setter, Long.valueOf(value));
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, short value) {
        set(target, setter, Short.valueOf(value));
    }
    
    /**
     * Gets all bean-style property names of the given Class or its superclass.
     * A bean-style property 'abc' exists in Class C iff C has declared 
     * following pair of methods:
     *   public void setAbc(Y y) or public C setAbc(Y y)
     *   public Y getAbc();
     *   
     * If a getter property is annotated with {@link Reflectable}, then
     * it is ignored.
     *   
     */
    public static Set<String> getBeanStylePropertyNames(Class<?> c) {
        if (c == null)
            return Collections.emptySet();
        Set<String> result = beanPropertiesNameCache.get(c);
        if (result != null) {
            return result;
        }
        Method[] methods = c.getMethods();
        if (methods == null || methods.length < 2)
            return Collections.emptySet();
        result = new TreeSet<String>();
        for (Method m : methods) {
            if (m.getName().startsWith("get")) {
                if (!canReflect(m))
                    continue;
                String prop = StringUtils.capitalize(m.getName()
                    .substring("get".length()));
                Class<?> rtype = m.getReturnType();
                try {
                  Method setter = c.getMethod("set"+prop, new Class<?>[]{rtype});
                  if (setter.getReturnType() == void.class || 
                      setter.getReturnType().isAssignableFrom(c))
                  result.add(prop);
                } catch (NoSuchMethodException e) {
                    
                }
            }
        }
        beanPropertiesNameCache.put(c, result);
        return result;
    }
    
    /**
     * Gets all public field names of the given Class.
     *   
     */
    public static Set<String> getPublicFieldNames(Class c) {
        if (c == null)
            return Collections.EMPTY_SET;
        Field[] fields = c.getFields();
        if (fields == null || fields.length == 0)
            return Collections.EMPTY_SET;
        Set<String> result = new TreeSet<String>();
        for (Field f : fields) {
            if (canReflect(f))
                result.add(f.getName());
        }
        return result;
    }
    
    /**
     * Gets values of all field f the given class such that f exactly 
     * match the given modifiers and are of given type (Object implies any type)
     * unless f is annotated as {@link Reflectable}. 
     *   
     */
    public static <T> Set<T> getFieldValues(Class c, int mods, Class<T> t){
        if (c == null)
            return Collections.EMPTY_SET;
        Field[] fields = c.getFields();
        if (fields == null || fields.length == 0)
            return Collections.EMPTY_SET;
        Set<T> result = new TreeSet<T>();
        for (Field f : fields) {
            if (mods == f.getModifiers() 
            && (t == Object.class || t.isAssignableFrom(f.getType()))
            && canReflect(f)) {
                try {
                    result.add((T)f.get(null));
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                }
            }
        }
        return result;
    }

    /**
     * Affirms if the given member is selected for reflection. The decision is
     * based on the following truth table on both the class-level and 
     * member-level annotation (null annotation represents MAYBE) 
     * 
     * Class   member  
     * MAYBE   MAYBE   YES
     * MAYBE   YES     YES
     * MAYBE   NO      NO
     *
     * YES     MAYBE   YES
     * YES     YES     YES
     * YES     NO      NO
     *
     * NO      YES     YES
     * NO      MAYBE   NO
     * NO      NO      NO 
     * 
    */   
    static boolean canReflect(Reflectable cls, Reflectable member) {
        if (cls == null || cls.value()) {
            return member == null || member.value() == true;
        } else {
            return member != null && member.value() == true;
        }
    }
    
    /**
     * Affirms if the original declaration the given field is annotated
     * for reflection. 
     */
    static boolean canReflect(Field field) {
        Class cls = field.getDeclaringClass();
        return canReflect((Reflectable)cls.getAnnotation(Reflectable.class), 
            field.getAnnotation(Reflectable.class));
    }
    
    /**
     * Affirms if the original declaration the given method is annotated
     * for reflection. 
     */
    static boolean canReflect(Method method) {
        Class cls = getDeclaringClass(method);
        if (cls != method.getDeclaringClass())
            method = getDeclaringMethod(cls, method);
        return canReflect((Reflectable)cls.getAnnotation(Reflectable.class), 
            method.getAnnotation(Reflectable.class));
    }
    
    /**
     * Gets the declaring class of the given method signature but also checks
     * if the method is declared in an interface. If yes, then returns the
     * interface. 
     */
    public static Class getDeclaringClass(Method m) {
        if (m == null)
            return null;
        Class cls = m.getDeclaringClass();
        Class[] intfs =  cls.getInterfaces();
        for (Class intf : intfs) {
            if (getDeclaringMethod(intf, m) != null)
                cls = intf;
        }
        return cls;
    }
    
    /**
     * Gets the method in the given class that has the same signature of the
     * given method, if exists. Otherwise, null.
     */
    public static Method getDeclaringMethod(Class c, Method m) {
        try {
            Method m0 = c.getMethod(m.getName(), m.getParameterTypes());
            return m0;
        } catch (Exception e) {
            return null;
        }
    }    
}
