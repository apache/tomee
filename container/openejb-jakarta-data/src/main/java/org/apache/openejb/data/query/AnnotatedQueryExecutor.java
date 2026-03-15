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

import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class AnnotatedQueryExecutor {

    private AnnotatedQueryExecutor() {
    }

    public static Object execute(final EntityManager em, final Method method, final Object[] args,
                                  final Class<?> entityClass) {
        final Query queryAnnotation = method.getAnnotation(Query.class);
        final String jpql = expandJdqlToJpql(queryAnnotation.value(), entityClass, em);

        final jakarta.persistence.Query query = em.createQuery(jpql);

        // Bind parameters
        if (args != null) {
            final Parameter[] parameters = method.getParameters();
            int ordinal = 1;
            for (int i = 0; i < parameters.length; i++) {
                if (CriteriaQueryBuilder.isSpecialParameter(parameters[i].getType())) {
                    continue;
                }
                final Param param = parameters[i].getAnnotation(Param.class);
                if (param != null) {
                    query.setParameter(param.value(), args[i]);
                } else {
                    query.setParameter(ordinal++, args[i]);
                }
            }
        }

        CriteriaQueryBuilder.applyPagination(query, args);

        return adaptResult(method, query);
    }

    @SuppressWarnings("unchecked")
    private static Object adaptResult(final Method method, final jakarta.persistence.Query query) {
        final Class<?> returnType = method.getReturnType();

        if (returnType.equals(void.class) || returnType.equals(Void.class)) {
            query.executeUpdate();
            return null;
        }

        if (returnType.equals(long.class) || returnType.equals(Long.class)
            || returnType.equals(int.class) || returnType.equals(Integer.class)) {
            return query.getSingleResult();
        }

        if (returnType.equals(boolean.class) || returnType.equals(Boolean.class)) {
            return !query.getResultList().isEmpty();
        }

        if (List.class.isAssignableFrom(returnType) || Collection.class.isAssignableFrom(returnType)) {
            return query.getResultList();
        }

        if (Stream.class.isAssignableFrom(returnType)) {
            return query.getResultList().stream();
        }

        if (Optional.class.isAssignableFrom(returnType)) {
            final List<?> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        }

        if (returnType.isArray()) {
            final List<?> results = query.getResultList();
            return results.toArray();
        }

        // Single result
        final List<?> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Expands JDQL shorthand to full JPQL.
     * JDQL allows queries starting with FROM, WHERE, or ORDER BY without SELECT.
     * JDQL also allows unqualified field references which need the entity alias prefix.
     */
    static String expandJdqlToJpql(final String jdql, final Class<?> entityClass, final EntityManager em) {
        final String trimmed = jdql.stripLeading();
        final String upper = trimmed.toUpperCase();
        final String entityName = entityClass.getSimpleName();
        boolean expanded = false;
        String result;

        if (upper.startsWith("WHERE ") || upper.startsWith("WHERE(")) {
            result = "SELECT e FROM " + entityName + " e " + trimmed;
            expanded = true;
        } else if (upper.startsWith("FROM ")) {
            final String afterFrom = trimmed.substring(5).stripLeading();
            final int spaceIdx = afterFrom.indexOf(' ');
            if (spaceIdx > 0) {
                final String fromEntity = afterFrom.substring(0, spaceIdx);
                final String rest = afterFrom.substring(spaceIdx).stripLeading();
                if (rest.toUpperCase().startsWith("WHERE ")
                    || rest.toUpperCase().startsWith("ORDER ")
                    || rest.toUpperCase().startsWith("GROUP ")
                    || rest.toUpperCase().startsWith("HAVING ")) {
                    result = "SELECT e FROM " + fromEntity + " e " + rest;
                    expanded = true;
                } else {
                    result = "SELECT e " + trimmed;
                    expanded = false;
                }
            } else {
                result = "SELECT e " + trimmed;
                expanded = false;
            }
        } else if (upper.startsWith("ORDER BY ")) {
            result = "SELECT e FROM " + entityName + " e " + trimmed;
            expanded = true;
        } else {
            result = jdql;
        }

        // For JDQL-expanded queries, qualify unqualified field references with the entity alias "e."
        if (expanded && em != null) {
            result = qualifyFieldReferences(result, entityClass, em);
        }

        return result;
    }

    /**
     * Qualifies unqualified field references in JPQL with the entity alias "e.".
     * Uses JPA metamodel to discover entity attribute names.
     */
    private static String qualifyFieldReferences(final String jpql, final Class<?> entityClass, final EntityManager em) {
        final Set<String> fieldNames = new HashSet<>();
        try {
            final EntityType<?> entityType = em.getMetamodel().entity(entityClass);
            entityType.getAttributes().forEach(attr -> fieldNames.add(attr.getName()));
        } catch (final Exception e) {
            return jpql;
        }

        if (fieldNames.isEmpty()) {
            return jpql;
        }

        String result = jpql;
        for (final String field : fieldNames) {
            // Replace unqualified field references (not already preceded by "e." or another alias)
            // Match field name that is:
            // - preceded by a non-alphanumeric, non-dot char (or start)
            // - followed by a non-alphanumeric char (or end)
            // - not already preceded by a dot (alias qualification)
            final Pattern pattern = Pattern.compile("(?<![.\\w])" + Pattern.quote(field) + "(?![\\w])");
            final Matcher matcher = pattern.matcher(result);
            final StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                // Check if preceded by a dot (already qualified)
                final int start = matcher.start();
                if (start > 0 && result.charAt(start - 1) == '.') {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(field));
                } else {
                    matcher.appendReplacement(sb, "e." + Matcher.quoteReplacement(field));
                }
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }

        return result;
    }

}
