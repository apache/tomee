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
package org.apache.openejb.data.query;

import jakarta.data.repository.By;
import jakarta.data.repository.OrderBy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class FindAnnotationExecutor {

    private FindAnnotationExecutor() {
    }

    /**
     * Builds the JPQL string for a @Find-annotated method. Cacheable — depends only on method metadata.
     */
    public static <T> String buildJpql(final Method method, final Class<T> entityClass) {
        final StringBuilder jpql = new StringBuilder("SELECT e FROM ")
            .append(entityClass.getSimpleName()).append(" e");

        final Parameter[] parameters = method.getParameters();
        final List<String> conditions = new ArrayList<>();

        for (int i = 0; i < parameters.length; i++) {
            if (CriteriaQueryBuilder.isSpecialParameter(parameters[i].getType())) {
                continue;
            }
            final By by = parameters[i].getAnnotation(By.class);
            final String property = by != null ? by.value() : parameters[i].getName();
            conditions.add("e." + property + " = :p" + i);
        }

        if (!conditions.isEmpty()) {
            jpql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        // Handle @OrderBy annotation(s) on the method
        final OrderBy[] orderBys = method.getAnnotationsByType(OrderBy.class);
        if (orderBys != null && orderBys.length > 0) {
            jpql.append(" ORDER BY ");
            for (int i = 0; i < orderBys.length; i++) {
                if (i > 0) {
                    jpql.append(", ");
                }
                jpql.append("e.").append(orderBys[i].value())
                    .append(orderBys[i].descending() ? " DESC" : " ASC");
            }
        }

        return jpql.toString();
    }

    /**
     * Executes a @Find-annotated method using the given (cached) JPQL string.
     */
    public static <T> Object execute(final EntityManager em, final Method method, final Object[] args,
                                     final Class<T> entityClass, final String jpql) {
        final TypedQuery<T> query = em.createQuery(jpql, entityClass);

        // Bind parameters
        final Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (CriteriaQueryBuilder.isSpecialParameter(parameters[i].getType())) {
                continue;
            }
            query.setParameter("p" + i, args[i]);
        }

        CriteriaQueryBuilder.applyPagination(query, args);

        return adaptResult(method, query);
    }

    @SuppressWarnings("unchecked")
    private static <T> Object adaptResult(final Method method, final TypedQuery<T> query) {
        final Class<?> returnType = method.getReturnType();

        if (List.class.isAssignableFrom(returnType) || Collection.class.isAssignableFrom(returnType)) {
            return query.getResultList();
        }

        if (Stream.class.isAssignableFrom(returnType)) {
            return query.getResultList().stream();
        }

        if (Optional.class.isAssignableFrom(returnType)) {
            final List<T> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        }

        // Single result
        final List<T> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
}
