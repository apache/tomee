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
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CriteriaQueryBuilder {

    private CriteriaQueryBuilder() {
    }

    public static <T> TypedQuery<T> buildFind(final EntityManager em, final Class<T> entityClass,
                                               final MethodNameParser.ParsedQuery parsed,
                                               final Object[] args) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<T> cq = cb.createQuery(entityClass);
        final Root<T> root = cq.from(entityClass);
        cq.select(root);

        final int paramIdx = applyConditions(cb, root, cq, parsed.conditions(), args, 0);
        applyOrderClauses(cb, root, cq, parsed.orderClauses());
        applySortParameters(cb, root, cq, args);

        final TypedQuery<T> query = em.createQuery(cq);
        applyPagination(query, args);
        return query;
    }

    public static <T> TypedQuery<Long> buildCount(final EntityManager em, final Class<T> entityClass,
                                                   final MethodNameParser.ParsedQuery parsed,
                                                   final Object[] args) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        final Root<T> root = cq.from(entityClass);
        cq.select(cb.count(root));

        applyConditions(cb, root, cq, parsed.conditions(), args, 0);

        return em.createQuery(cq);
    }

    public static <T> long buildDelete(final EntityManager em, final Class<T> entityClass,
                                      final MethodNameParser.ParsedQuery parsed,
                                      final Object[] args) {
        // Use JPQL DELETE instead of CriteriaDelete for broader JPA provider compatibility
        final StringBuilder jpql = new StringBuilder("DELETE FROM ")
            .append(entityClass.getSimpleName()).append(" e");

        final List<MethodNameParser.Condition> conditions = parsed.conditions();
        if (!conditions.isEmpty()) {
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
                    case LIKE -> jpql.append("e.").append(c.property()).append(" LIKE :").append(paramName);
                    case CONTAINS -> jpql.append("e.").append(c.property()).append(" LIKE :").append(paramName);
                    case STARTS_WITH -> jpql.append("e.").append(c.property()).append(" LIKE :").append(paramName);
                    case ENDS_WITH -> jpql.append("e.").append(c.property()).append(" LIKE :").append(paramName);
                    case NOT -> jpql.append("e.").append(c.property()).append(" <> :").append(paramName);
                    case NULL -> jpql.append("e.").append(c.property()).append(" IS NULL");
                    case NOT_NULL -> jpql.append("e.").append(c.property()).append(" IS NOT NULL");
                    case IN -> jpql.append("e.").append(c.property()).append(" IN :").append(paramName);
                    default -> jpql.append("e.").append(c.property()).append(" = :").append(paramName);
                }
            }
        }

        final jakarta.persistence.Query query = em.createQuery(jpql.toString());

        // Bind parameters
        int paramIdx = 0;
        for (int i = 0; i < conditions.size(); i++) {
            final MethodNameParser.Condition c = conditions.get(i);
            final int paramCount = c.operator().parameterCount();
            if (paramCount > 0) {
                Object value = args[paramIdx++];
                if (c.operator() == MethodNameParser.Operator.CONTAINS) {
                    value = "%" + value + "%";
                } else if (c.operator() == MethodNameParser.Operator.STARTS_WITH) {
                    value = value + "%";
                } else if (c.operator() == MethodNameParser.Operator.ENDS_WITH) {
                    value = "%" + value;
                }
                query.setParameter("p" + i, value);
            }
        }

        return (long) query.executeUpdate();
    }

    @SuppressWarnings("unchecked")
    private static <T> int applyConditions(final CriteriaBuilder cb, final Root<T> root,
                                           final CriteriaQuery<?> cq,
                                           final List<MethodNameParser.Condition> conditions,
                                           final Object[] args, final int paramIdx) {
        final Predicate predicate = buildPredicate(cb, root, conditions, args, paramIdx);
        if (predicate != null) {
            cq.where(predicate);
        }
        // Calculate how many params were consumed
        int nextParamIdx = paramIdx;
        for (final MethodNameParser.Condition c : conditions) {
            nextParamIdx += c.operator().parameterCount();
        }
        return nextParamIdx;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> Predicate buildPredicate(final CriteriaBuilder cb, final Root<T> root,
                                                final List<MethodNameParser.Condition> conditions,
                                                final Object[] args, final int paramIdx) {
        int currentIdx = paramIdx;
        Predicate predicate = null;

        for (final MethodNameParser.Condition condition : conditions) {
            final Path<?> path = root.get(condition.property());
            final Predicate clause;

            switch (condition.operator()) {
                case EQUAL:
                    clause = cb.equal(path, args[currentIdx++]);
                    break;
                case GREATER_THAN:
                    clause = cb.greaterThan((Expression<Comparable>) path, (Comparable) args[currentIdx++]);
                    break;
                case GREATER_THAN_EQUAL:
                    clause = cb.greaterThanOrEqualTo((Expression<Comparable>) path, (Comparable) args[currentIdx++]);
                    break;
                case LESS_THAN:
                    clause = cb.lessThan((Expression<Comparable>) path, (Comparable) args[currentIdx++]);
                    break;
                case LESS_THAN_EQUAL:
                    clause = cb.lessThanOrEqualTo((Expression<Comparable>) path, (Comparable) args[currentIdx++]);
                    break;
                case LIKE:
                    clause = cb.like((Expression<String>) path, (String) args[currentIdx++]);
                    break;
                case STARTS_WITH:
                    clause = cb.like((Expression<String>) path, args[currentIdx++] + "%");
                    break;
                case ENDS_WITH:
                    clause = cb.like((Expression<String>) path, "%" + args[currentIdx++]);
                    break;
                case CONTAINS:
                    clause = cb.like((Expression<String>) path, "%" + args[currentIdx++] + "%");
                    break;
                case BETWEEN:
                    clause = cb.between((Expression<Comparable>) path, (Comparable) args[currentIdx++], (Comparable) args[currentIdx++]);
                    break;
                case IN:
                    final Object inArg = args[currentIdx++];
                    if (inArg instanceof Collection<?> col) {
                        clause = path.in(col);
                    } else {
                        clause = path.in(inArg);
                    }
                    break;
                case NOT:
                    clause = cb.notEqual(path, args[currentIdx++]);
                    break;
                case NULL:
                    clause = cb.isNull(path);
                    break;
                case NOT_NULL:
                    clause = cb.isNotNull(path);
                    break;
                case TRUE:
                    clause = cb.isTrue((Expression<Boolean>) path);
                    break;
                case FALSE:
                    clause = cb.isFalse((Expression<Boolean>) path);
                    break;
                default:
                    clause = cb.equal(path, args[currentIdx++]);
            }

            if (predicate == null) {
                predicate = clause;
            } else if (condition.connector() == MethodNameParser.Connector.OR) {
                predicate = cb.or(predicate, clause);
            } else {
                predicate = cb.and(predicate, clause);
            }
        }

        return predicate;
    }

    private static <T> void applyOrderClauses(final CriteriaBuilder cb, final Root<T> root,
                                               final CriteriaQuery<?> cq,
                                               final List<MethodNameParser.OrderClause> orderClauses) {
        if (orderClauses.isEmpty()) {
            return;
        }
        final List<Order> orders = new ArrayList<>();
        for (final MethodNameParser.OrderClause clause : orderClauses) {
            if (clause.ascending()) {
                orders.add(cb.asc(root.get(clause.property())));
            } else {
                orders.add(cb.desc(root.get(clause.property())));
            }
        }
        cq.orderBy(orders);
    }

    @SuppressWarnings("unchecked")
    private static <T> void applySortParameters(final CriteriaBuilder cb, final Root<T> root,
                                                 final CriteriaQuery<?> cq, final Object[] args) {
        if (args == null) {
            return;
        }
        final List<Order> orders = new ArrayList<>();
        for (final Object arg : args) {
            if (arg instanceof Sort<?> sort) {
                if (sort.isAscending()) {
                    orders.add(cb.asc(root.get(sort.property())));
                } else {
                    orders.add(cb.desc(root.get(sort.property())));
                }
            } else if (arg instanceof jakarta.data.Order<?> order) {
                for (final Object s : order.sorts()) {
                    final Sort<?> sort = (Sort<?>) s;
                    if (sort.isAscending()) {
                        orders.add(cb.asc(root.get(sort.property())));
                    } else {
                        orders.add(cb.desc(root.get(sort.property())));
                    }
                }
            }
        }
        if (!orders.isEmpty()) {
            cq.orderBy(orders);
        }
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
