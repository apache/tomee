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
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

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

    public static <T> Object execute(final EntityManager em, final Method method, final Object[] args,
                                     final Class<T> entityClass) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<T> cq = cb.createQuery(entityClass);
        final Root<T> root = cq.from(entityClass);
        cq.select(root);

        final Parameter[] parameters = method.getParameters();
        final List<Predicate> predicates = new ArrayList<>();

        for (int i = 0; i < parameters.length; i++) {
            if (isSpecialParameter(parameters[i].getType())) {
                continue;
            }
            final By by = parameters[i].getAnnotation(By.class);
            if (by != null) {
                predicates.add(cb.equal(root.get(by.value()), args[i]));
            } else {
                // Use parameter name as property name
                predicates.add(cb.equal(root.get(parameters[i].getName()), args[i]));
            }
        }

        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        // Handle @OrderBy annotation(s) on the method
        final OrderBy[] orderBys = method.getAnnotationsByType(OrderBy.class);
        if (orderBys != null && orderBys.length > 0) {
            final List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
            for (final OrderBy ob : orderBys) {
                if (ob.descending()) {
                    orders.add(cb.desc(root.get(ob.value())));
                } else {
                    orders.add(cb.asc(root.get(ob.value())));
                }
            }
            cq.orderBy(orders);
        }

        final TypedQuery<T> query = em.createQuery(cq);
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

    private static boolean isSpecialParameter(final Class<?> type) {
        return jakarta.data.Limit.class.isAssignableFrom(type)
            || jakarta.data.Sort.class.isAssignableFrom(type)
            || jakarta.data.Order.class.isAssignableFrom(type)
            || jakarta.data.page.PageRequest.class.isAssignableFrom(type);
    }
}
