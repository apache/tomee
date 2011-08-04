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

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @version $Rev$ $Date$
 */
public class Join {

    public static final MethodCallback METHOD_CALLBACK = new MethodCallback();

    public static final ClassCallback CLASS_CALLBACK = new ClassCallback();

    public static String join(String delimiter, Collection collection) {
        if(collection.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object obj : collection) {
            sb.append(obj).append(delimiter);
        }
        return  sb.substring(0, sb.length()-delimiter.length());
    }

    public static String join(String delimiter, Object... collection) {
        if(collection.length ==0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object obj : collection) {
            sb.append(obj).append(delimiter);
        }
        return  sb.substring(0, sb.length()-delimiter.length());
    }

    public static <T> String join(String delimiter, NameCallback<T> nameCallback, T... collection) {
        if (collection.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (T obj : collection) {
            sb.append(nameCallback.getName(obj)).append(delimiter);
        }
        return sb.substring(0, sb.length() - delimiter.length());
    }

    public static <T> String join(String delimiter, NameCallback<T> nameCallback, Collection<T> collection) {
        if (collection.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (T obj : collection) {
            sb.append(nameCallback.getName(obj)).append(delimiter);
        }
        return sb.substring(0, sb.length() - delimiter.length());
    }

    public static interface NameCallback<T> {

        public String getName(T object);
    }

    public static class MethodCallback implements NameCallback<Method> {

        public String getName(Method method) {
            return method.getName();
        }
    }

    public static class ClassCallback implements NameCallback<Class<?>> {

        public String getName(Class<?> cls) {
            return cls.getName();
        }
    }
}
