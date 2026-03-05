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
package org.apache.openejb.data.meta;

import jakarta.data.repository.Repository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class RepositoryMetadata {

    private final Class<?> repositoryInterface;
    private final Class<?> entityClass;
    private final Class<?> keyClass;
    private final String dataStore;

    public RepositoryMetadata(final Class<?> repositoryInterface) {
        this.repositoryInterface = repositoryInterface;

        final Repository repoAnnotation = repositoryInterface.getAnnotation(Repository.class);
        this.dataStore = repoAnnotation != null ? repoAnnotation.dataStore() : "";

        final Class<?>[] types = resolveTypeArguments(repositoryInterface);
        this.entityClass = types[0];
        this.keyClass = types[1];
    }

    private static Class<?>[] resolveTypeArguments(final Class<?> repoInterface) {
        for (final Type type : repoInterface.getGenericInterfaces()) {
            if (type instanceof ParameterizedType pt) {
                final Type rawType = pt.getRawType();
                if (rawType instanceof Class<?> rawClass && isDataRepository(rawClass)) {
                    final Type[] args = pt.getActualTypeArguments();
                    if (args.length >= 2) {
                        return new Class<?>[]{(Class<?>) args[0], (Class<?>) args[1]};
                    }
                }
                // recurse into parent interfaces
                if (rawType instanceof Class<?> rawClass) {
                    final Class<?>[] result = resolveTypeArguments(rawClass);
                    if (result[0] != Object.class) {
                        return result;
                    }
                }
            } else if (type instanceof Class<?> parentInterface) {
                final Class<?>[] result = resolveTypeArguments(parentInterface);
                if (result[0] != Object.class) {
                    return result;
                }
            }
        }
        return new Class<?>[]{Object.class, Object.class};
    }

    private static boolean isDataRepository(final Class<?> clazz) {
        if (clazz.getName().startsWith("jakarta.data.repository.")) {
            return true;
        }
        for (final Class<?> iface : clazz.getInterfaces()) {
            if (isDataRepository(iface)) {
                return true;
            }
        }
        return false;
    }

    public Class<?> getRepositoryInterface() {
        return repositoryInterface;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public Class<?> getKeyClass() {
        return keyClass;
    }

    public String getDataStore() {
        return dataStore;
    }
}
