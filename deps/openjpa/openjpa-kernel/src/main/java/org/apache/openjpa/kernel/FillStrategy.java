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
package org.apache.openjpa.kernel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.openjpa.enhance.Reflection;
import org.apache.openjpa.lib.util.Localizer;


/**
 * A strategy to fill data into a {@link ResultShape}.
 * <BR>
 * Available strategy implementations can fill by invoking constructor, setting array elements, direct assignment,
 * invoking put(key,value)-style method on Map or factory-constructed instance. 
 * 
 * @author Pinaki Poddar
 * 
 * @since 2.0.0
 *
 */
public interface FillStrategy<T> {
    static final Localizer _loc = Localizer.forPackage(FillStrategy.class);
    
    T fill(Object[] data, Class<?>[] types, String[] aliases);
    
    
    /**
     * Fills an array of given type. 
     *
     * @param <T> must be an array type.
     */
    public static class Array<T> implements FillStrategy<T> {
        private final Class<?> cls;
        public Array(Class<T> arrayCls) {
            if (arrayCls == null || !arrayCls.isArray())
                throw new IllegalArgumentException(_loc.get("fill-bad-array", arrayCls).getMessage());
            this.cls = arrayCls.getComponentType();
        }
        
        public T fill(Object[] values, Class<?>[] types, String[] aliases) {
            Object array = java.lang.reflect.Array.newInstance(cls, values.length);
            System.arraycopy(values, 0, array, 0, values.length);
            return (T)array;
        }
    }
    
    /**
     * Construct and populate an instance by invoking the put method 
     * with each alias as key and element of the given array of values.
     * 
     * The instance is a created by the no-argument constructor of the declaring class of the given method.
     */
    public static class Map<T> implements FillStrategy<T> {
        private final Method putMethod;
        
        public Map(Method put) {
            if (put == null || put.getParameterTypes().length != 2)
                throw new IllegalArgumentException(_loc.get("fill-bad-put", put).getMessage());
            putMethod = put;
        }
        
        public T fill(Object[] values, Class<?>[] types, String[] aliases) {
            int i = 0;
            try {
                Object map = putMethod.getDeclaringClass().newInstance();
                for (i = 0; i < values.length; i++)
                    putMethod.invoke(map, aliases[i], values[i]);
                return (T)map;
            } catch (InvocationTargetException t) {
                throw new RuntimeException(_loc.get("fill-map-error", putMethod, aliases[i], values[i]).getMessage(), 
                    t.getTargetException());
            } catch (Exception e) {
                throw new RuntimeException(_loc.get("fill-map-error", putMethod, aliases[i], values[i]).getMessage(), 
                        e);
            }
        }
        
    }
    
    /**
     * Construct and populate an instance by the given constructor and arguments.
     */
    public static class NewInstance<T> implements FillStrategy<T> {
        private Constructor<? extends T> cons;
        private Class<T> cls;
        
        public NewInstance(Constructor<? extends T> cons) {
            this.cons = cons;
        }
        
        public NewInstance(Class<T> cls) {
            this.cls = cls;
        }
        
        
        /**
         * Finds a constructor of the given class with given argument types.
         */
        <X> Constructor<X> findConstructor(Class<X> cls, Class<?>[] types) {
            try {
                return cls.getConstructor(types);
            } catch (Exception e) {
                Constructor<?>[] constructors = cls.getConstructors();
                for (Constructor<?> cons : constructors) {
                    Class<?>[] paramTypes = cons.getParameterTypes();
                    boolean match = false;
                    if (paramTypes.length == types.length) {
                        for (int i = 0; i < paramTypes.length; i++) {
                            match = paramTypes[i].isAssignableFrom(Filters.wrap(types[i]));
                            if (!match)
                                break;
                            }
                        }
                        if (match) {
                            return (Constructor<X>)cons;
                        }
                }
            }
            throw new RuntimeException(_loc.get("fill-ctor-none", cls, Arrays.toString(types)).getMessage());
        }
        
        public T fill(Object[] values, Class<?>[] types, String[] aliases) {
            if (cons == null) {
                cons = findConstructor(cls, types);
            }
            try {
                for (int i = 0; i < values.length; i++) {
                    values[i] = Filters.convert(values[i], types[i]);
                }
                return cons.newInstance(values);
            } catch (Exception e) {
                throw new RuntimeException(_loc.get("fill-ctor-error", cons, Arrays.toString(values), 
                        Arrays.toString(types)).getMessage(), e);
            }
        }
        
    }
    
    /**
     * Create and populate a bean by invoking setter methods identified by alias name with each array
     * element value as argument.
     */
    public static class Bean<T> implements FillStrategy<T> {
        private final Class<T> cls;
        private Method[] setters;
        
        public Bean(Class<T> cls) {
            this.cls = cls;
        }
    
        public T fill(Object[] values, Class<?>[] types, String[] aliases) {
            int i = 0;
            try {
                if (setters == null) {
                    setters = new Method[values.length];
                }
                T bean = cls.newInstance();
                for (i = 0; i < values.length; i++) {
                    if (setters[i] == null) {
                        setters[i] = Reflection.findSetter(cls, aliases[i], false);
                        if (setters[i] == null)
                            throw new RuntimeException(_loc.get("fill-bean-setter", cls, aliases[i]).getMessage());
                    }
                    setters[i].invoke(bean, Filters.convert(values[i], types[i]));
                }
                return bean;
            } catch (InvocationTargetException t) {
                throw new RuntimeException(_loc.get("fill-bean-error", setters[i], values[i], types[i]).getMessage(), 
                        t.getTargetException());
            } catch (Exception e) {
                throw new RuntimeException(_loc.get("fill-bean-error", setters[i], values[i], types[i]).getMessage(), 
                        e);
            }
        }
    }
    
    
    /**
     * Populate an instance by simply assigning the 0-th element of the input values.  
     */
    public static class Assign<T> implements FillStrategy<T> {
        public T fill(Object[] values, Class<?>[] types, String[] aliases) {
            try {
                return (T)values[0];
            } catch (Exception e) {
                throw new RuntimeException(_loc.get("fill-assign-error", Arrays.toString(values),
                        Arrays.toString(types)).getMessage(), e);
            }
        }
    }
    
    /**
     * Populate an instance created by given factory using a given put(key,value) method.
     * If the first argument of the given put method is integer then fill the values
     * by index else fill the values with alias key.  
     */
    public static class Factory<T> implements FillStrategy<T> {
        final ObjectFactory<T> factory;
        final Method putMethod;
        final boolean isArray;
        
        public Factory(ObjectFactory<T> factory, Method put) {
            this.factory = factory;
            this.putMethod = put;
            if (put == null || put.getParameterTypes().length != 2)
                throw new IllegalArgumentException(_loc.get("fill-factory-bad-put", put).getMessage());
            Class<?> keyType = putMethod.getParameterTypes()[0];
            this.isArray = keyType == int.class || keyType == Integer.class;
        }
        
        public T fill(Object[] values, Class<?>[] types, String[] aliases) {
            int i = 0;
            Object key = null;
            T result = factory.newInstance();
            try {
                for (i = 0; i < values.length; i++) {
                    key = isArray ? i : aliases[i];
                    putMethod.invoke(result, key, Filters.convert(values[i], types[i]));
                }
                return result;
            } catch (InvocationTargetException t) {
                throw new RuntimeException(_loc.get("fill-factory-error", new Object[]{putMethod, key, values[i], 
                        types[i]}).getMessage(), t.getTargetException());
            } catch (Exception e) {
                throw new RuntimeException(_loc.get("fill-factory-error", new Object[]{putMethod, key, values[i], 
                        types[i]}).getMessage(), e);
            }
        }
    }
}
