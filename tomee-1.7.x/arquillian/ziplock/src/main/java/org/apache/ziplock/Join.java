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
package org.apache.ziplock;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Join {

    public static final MethodCallback METHOD_CALLBACK = new MethodCallback();

    public static final ClassCallback CLASS_CALLBACK = new ClassCallback();

    public static String join(final String delimiter, final Collection collection) {
        if (collection.size() == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (final Object obj : collection) {
            sb.append(obj).append(delimiter);
        }
        return sb.substring(0, sb.length() - delimiter.length());
    }

    public static String join(final String delimiter, final Object... collection) {
        if (collection.length == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (final Object obj : collection) {
            sb.append(obj).append(delimiter);
        }
        return sb.substring(0, sb.length() - delimiter.length());
    }

    public static <T> String join(final String delimiter, final NameCallback<T> nameCallback, final T... collection) {
        if (collection.length == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (final T obj : collection) {
            sb.append(nameCallback.getName(obj)).append(delimiter);
        }
        return sb.substring(0, sb.length() - delimiter.length());
    }

    public static <T> String join(final String delimiter, final NameCallback<T> nameCallback, final Collection<T> collection) {
        if (collection.size() == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (final T obj : collection) {
            sb.append(nameCallback.getName(obj)).append(delimiter);
        }
        return sb.substring(0, sb.length() - delimiter.length());
    }

    public static <T> List<String> strings(final Collection<T> collection, final NameCallback<T> callback) {
        final List<String> list = new ArrayList<String>();

        for (final T t : collection) {
            final String name = callback.getName(t);
            list.add(name);
        }

        return list;
    }

    public interface NameCallback<T> {
        String getName(T object);
    }

    public static class FileCallback implements NameCallback<File> {

        public String getName(final File file) {
            return file.getName();
        }
    }

    public static class MethodCallback implements NameCallback<Method> {

        public String getName(final Method method) {
            return method.getName();
        }
    }

    public static class ClassCallback implements NameCallback<Class<?>> {

        public String getName(final Class<?> cls) {
            return cls.getName();
        }
    }
}
