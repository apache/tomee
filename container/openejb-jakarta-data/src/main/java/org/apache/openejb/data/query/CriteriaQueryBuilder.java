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

import jakarta.data.Limit;
import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public final class CriteriaQueryBuilder {

    private CriteriaQueryBuilder() {
    }

    // -- JPQL building (cacheable, no args dependency) --

    public static String buildFindJpql(final Class<?> entityClass,
                                       final MethodNameParser.ParsedQuery parsed) {
        final StringBuilder jpql = new StringBuilder("SELECT e FROM ")
            .append(entityClass.getSimpleName()).append(" e");

        appendWhereClause(jpql, parsed.conditions());
        appendOrderByClause(jpql, parsed.orderClauses());

        return jpql.toString();
    }

    public static String buildCountJpql(final Class<?> entityClass,
                                        final MethodNameParser.ParsedQuery parsed) {
        final StringBuilder jpql = new StringBuilder("SELECT COUNT(e) FROM ")
            .append(entityClass.getSimpleName()).append(" e");

        appendWhereClause(jpql, parsed.conditions());

        return jpql.toString();
    }

    public static String buildDeleteJpql(final Class<?> entityClass,
                                         final MethodNameParser.ParsedQuery parsed) {
        final StringBuilder jpql = new StringBuilder("DELETE FROM ")
            .append(entityClass.getSimpleName()).append(" e");

        appendWhereClause(jpql, parsed.conditions());

        return jpql.toString();
    }

    // -- Query execution (per-call, binds args) --

    @SuppressWarnings("unchecked")
    public static <T> TypedQuery<T> executeFind(final EntityManager em, final Class<T> entityClass,
                                                 final String jpql,
                                                 final MethodNameParser.ParsedQuery parsed,
                                                 final Object[] args) {
        final String effectiveJpql = appendDynamicSort(jpql, args);
        final TypedQuery<T> query = em.createQuery(effectiveJpql, entityClass);
        if (!bindConditionParameters(query, parsed.conditions(), args)) {
            // Empty IN collection — return a query that yields no results
            query.setMaxResults(0);
            return query;
        }
        applyPagination(query, args);
        return query;
    }

    public static TypedQuery<Long> executeCount(final EntityManager em,
                                                 final String jpql,
                                                 final MethodNameParser.ParsedQuery parsed,
                                                 final Object[] args) {
        final TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        if (!bindConditionParameters(query, parsed.conditions(), args)) {
            return em.createQuery("SELECT 0L", Long.class);
        }
        return query;
    }

    public static long executeDelete(final EntityManager em,
                                     final String jpql,
                                     final MethodNameParser.ParsedQuery parsed,
                                     final Object[] args) {
        final jakarta.persistence.Query query = em.createQuery(jpql);
        if (!bindConditionParameters(query, parsed.conditions(), args)) {
            return 0L;
        }
        return (long) query.executeUpdate();
    }

    // -- Shared helpers --

    private static void appendWhereClause(final StringBuilder jpql,
                                           final List<MethodNameParser.Condition> conditions) {
        if (conditions.isEmpty()) {
            return;
        }
        jpql.append(" WHERE ");
        for (int i = 0; i < conditions.size(); i++) {
            final MethodNameParser.Condition c = conditions.get(i);
            if (i > 0) {
                jpql.append(c.connector() == MethodNameParser.Connector.OR ? " OR " : " AND ");
            }
            final String paramName = "p" + i;
            switch (c.operator()) {
                case EQUAL -> jpql.append("e.").append(c.property()).append(" = :").append(paramName);
                case GREATER_THAN -> jpql.append("e.").append(c.property()).append(" > :").append(paramName);
                case GREATER_THAN_EQUAL -> jpql.append("e.").append(c.property()).append(" >= :").append(paramName);
                case LESS_THAN -> jpql.append("e.").append(c.property()).append(" < :").append(paramName);
                case LESS_THAN_EQUAL -> jpql.append("e.").append(c.property()).append(" <= :").append(paramName);
                case LIKE, CONTAINS, STARTS_WITH, ENDS_WITH ->
                    jpql.append("e.").append(c.property()).append(" LIKE :").append(paramName);
                case NOT -> jpql.append("e.").append(c.property()).append(" <> :").append(paramName);
                case NULL -> jpql.append("e.").append(c.property()).append(" IS NULL");
                case NOT_NULL -> jpql.append("e.").append(c.property()).append(" IS NOT NULL");
                case IN -> jpql.append("e.").append(c.property()).append(" IN :").append(paramName);
                case BETWEEN -> jpql.append("e.").append(c.property()).append(" BETWEEN :").append(paramName)
                    .append("a AND :").append(paramName).append("b");
                case TRUE -> jpql.append("e.").append(c.property()).append(" = TRUE");
                case FALSE -> jpql.append("e.").append(c.property()).append(" = FALSE");
                default -> jpql.append("e.").append(c.property()).append(" = :").append(paramName);
            }
        }
    }

    private static void appendOrderByClause(final StringBuilder jpql,
                                             final List<MethodNameParser.OrderClause> orderClauses) {
        if (orderClauses.isEmpty()) {
            return;
        }
        jpql.append(" ORDER BY ");
        for (int i = 0; i < orderClauses.size(); i++) {
            if (i > 0) {
                jpql.append(", ");
            }
            final MethodNameParser.OrderClause clause = orderClauses.get(i);
            jpql.append("e.").append(clause.property()).append(clause.ascending() ? " ASC" : " DESC");
        }
    }

    /**
     * Binds args to named parameters. Returns false if an IN parameter has an empty
     * collection (the query should return empty results without execution).
     */
    private static boolean bindConditionParameters(final jakarta.persistence.Query query,
                                                    final List<MethodNameParser.Condition> conditions,
                                                    final Object[] args) {
        int paramIdx = 0;
        for (int i = 0; i < conditions.size(); i++) {
            final MethodNameParser.Condition c = conditions.get(i);
            final int paramCount = c.operator().parameterCount();
            if (paramCount == 1) {
                Object value = args[paramIdx++];
                if (c.operator() == MethodNameParser.Operator.IN
                    && value instanceof java.util.Collection<?> col && col.isEmpty()) {
                    return false; // empty IN — query cannot match anything
                }
                if (c.operator() == MethodNameParser.Operator.CONTAINS) {
                    value = "%" + value + "%";
                } else if (c.operator() == MethodNameParser.Operator.STARTS_WITH) {
                    value = value + "%";
                } else if (c.operator() == MethodNameParser.Operator.ENDS_WITH) {
                    value = "%" + value;
                }
                query.setParameter("p" + i, value);
            } else if (paramCount == 2) {
                query.setParameter("p" + i + "a", args[paramIdx++]);
                query.setParameter("p" + i + "b", args[paramIdx++]);
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static String appendDynamicSort(final String jpql, final Object[] args) {
        if (args == null) {
            return jpql;
        }
        final List<String> sortClauses = new java.util.ArrayList<>();
        for (final Object arg : args) {
            if (arg instanceof Sort<?> sort) {
                sortClauses.add("e." + sort.property() + (sort.isAscending() ? " ASC" : " DESC"));
            } else if (arg instanceof jakarta.data.Order<?> order) {
                for (final Object s : order.sorts()) {
                    final Sort<?> sort = (Sort<?>) s;
                    sortClauses.add("e." + sort.property() + (sort.isAscending() ? " ASC" : " DESC"));
                }
            }
        }
        if (sortClauses.isEmpty()) {
            return jpql;
        }
        // Dynamic sort overrides any static ORDER BY in the cached JPQL
        final String upperJpql = jpql.toUpperCase();
        final String baseJpql;
        final int orderByIdx = upperJpql.lastIndexOf("ORDER BY");
        if (orderByIdx > 0) {
            baseJpql = jpql.substring(0, orderByIdx).stripTrailing();
        } else {
            baseJpql = jpql;
        }
        return baseJpql + " ORDER BY " + String.join(", ", sortClauses);
    }

    static boolean isSpecialParameter(final Class<?> type) {
        return jakarta.data.Limit.class.isAssignableFrom(type)
            || jakarta.data.Sort.class.isAssignableFrom(type)
            || jakarta.data.Order.class.isAssignableFrom(type)
            || jakarta.data.page.PageRequest.class.isAssignableFrom(type);
    }

    public static void applyPagination(final jakarta.persistence.Query query, final Object[] args) {
        if (args == null) {
            return;
        }
        for (final Object arg : args) {
            if (arg instanceof Limit limit) {
                query.setFirstResult((int) (limit.startAt() - 1));
                query.setMaxResults(limit.maxResults());
            } else if (arg instanceof PageRequest pageRequest) {
                query.setFirstResult((int) ((pageRequest.page() - 1) * pageRequest.size()));
                query.setMaxResults(pageRequest.size());
            }
        }
    }
}
