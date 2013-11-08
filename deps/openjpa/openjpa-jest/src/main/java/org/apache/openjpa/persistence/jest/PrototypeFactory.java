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

package org.apache.openjpa.persistence.jest;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.openjpa.kernel.Filters;

/**
 * A factory for a specific type of objects registered by a key.
 * The client registers a type indexed by name. 
 * The client can get a new instance of the registered type. 
 * The requested registered type <em>not</em> necessarily have to have a no-arg
 * constructor. The constructor arguments can be passed during 
 * {@link #newInstance(Class, Object...) new instance} request. Based on the
 * arguments, a matching constructor, if any, is located and invoked.
 * 
 * <K> type of key for this registry
 * <T> base type of the objects to construct 
 * 
 * @author Pinaki Poddar
 *
 */
public class PrototypeFactory<K,T> {
    private Map<K, Class<? extends T>> _registry = new TreeMap<K, Class<? extends T>>();
    
    /**
     * Register the given class with the given key.
     * 
     * @param key a non-null key.
     * @param prototype a type.
     */
    public void register(K key, Class<? extends T> prototype) {
        _registry.put(key, prototype);
    }
    
    /**
     * Create a new instance of the type {@linkplain #register(Object, Class) registered} before
     * with the given key, if any.
     * The given arguments are used to identify a constructor of the registered type and 
     * passed to the constructor of the registered type.
     * 
     * @param key a key to identify a registered type.
     * @param args arguments to pass to the constructor of the type.
     * 
     * @return null if no type has been registered against the given key.  
     */
    public T newInstance(K key, Object... args) {
        return _registry.containsKey(key) ? newInstance(_registry.get(key), args) : null;
    }
    
    /**
     * Gets the keys registered in this factory.
     * 
     * @return immutable set of registered keys.
     */
    public Set<K> getRegisteredKeys() {
        return Collections.unmodifiableSet(_registry.keySet());
    }
    
    private T newInstance(Class<? extends T> type, Object... args) {
        try {
            return findConstructor(type, getConstructorParameterTypes(args)).newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
    
    Class<?>[] getConstructorParameterTypes(Object... args) {
        if (args == null || args.length == 0) {
            return new Class<?>[0];
        }
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i] == null ? Object.class : args[i].getClass();
        }
        return types;
    }
    
    /**
     * Finds a constructor of the given class with given argument types.
     */
    Constructor<? extends T> findConstructor(Class<? extends T> cls, Class<?>[] types) {
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
                        return (Constructor<? extends T>)cons;
                    }
            }
        }
        throw new RuntimeException();//_loc.get("fill-ctor-none", cls, Arrays.toString(types)).getMessage());
    }
}
