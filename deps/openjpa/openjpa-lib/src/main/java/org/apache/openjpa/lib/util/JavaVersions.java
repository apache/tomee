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
package org.apache.openjpa.lib.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessController;

/**
 * Utilities for dealing with different Java specification versions.
 *
 * @author Abe White
 * @author Pinaki Poddar
 * 
 * @nojavadoc
 */
public class JavaVersions {

    /**
     * Java version; one of 2, 3, 4, 5, 6, or 7.
     */
    public static final int VERSION;

    private static final Class<?>[] EMPTY_CLASSES = new Class[0];

    static {
        String specVersion = AccessController.doPrivileged(
            J2DoPrivHelper.getPropertyAction("java.specification.version")); 
        if ("1.2".equals(specVersion))
            VERSION = 2;
        else if ("1.3".equals(specVersion))
            VERSION = 3;
        else if ("1.4".equals(specVersion))
            VERSION = 4;
        else if ("1.5".equals(specVersion))
            VERSION = 5;
        else if ("1.6".equals(specVersion))
            VERSION = 6;
        else
            VERSION = 7; // maybe someday...
    }

    /**
     * Collects the parameterized type declarations for a given field.
     */
    public static Class<?>[] getParameterizedTypes(Field f) {
        try {
            return collectParameterizedTypes(f.getGenericType(), f.getType());
        } catch (Exception e) {
            return EMPTY_CLASSES;
        }
    }

    /**
     * Collects the parameterized return type declarations for a given method.
     */
    public static Class<?>[] getParameterizedTypes(Method meth) {
        try {
            return collectParameterizedTypes(meth.getGenericReturnType(), meth.getReturnType());
        } catch (Exception e) {
            return EMPTY_CLASSES;
        }
    }

    /**
     * Return all parameterized classes for the given type.
     */
    private static Class<?>[] collectParameterizedTypes(Type type, Class<?> cls) throws Exception {
        if (type instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType)type).getActualTypeArguments();
            Class<?>[] clss = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                Class<?> c = extractClass(args[i]);
                if (c == null) {
                    return EMPTY_CLASSES;
                }
                clss[i] = c;
            }
            return clss;
        } else if (cls.getSuperclass() != Object.class) {
            return collectParameterizedTypes(cls.getGenericSuperclass(), cls.getSuperclass());
        } else {
            return EMPTY_CLASSES;
        }
    }
    
    /**
     * Extracts the class from the given argument, if possible. Null otherwise.
     */
    static Class<?> extractClass(Type type) throws Exception {
        if (type instanceof Class) {
            return (Class<?>)type;
        } else if (type instanceof ParameterizedType) {
            return extractClass(((ParameterizedType)type).getRawType());
        }
        return null;
    }
}
