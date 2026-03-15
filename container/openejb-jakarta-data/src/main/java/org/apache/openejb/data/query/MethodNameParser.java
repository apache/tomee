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

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MethodNameParser {

    private static final String FIND_BY = "findBy";
    private static final String DELETE_BY = "deleteBy";
    private static final String COUNT_BY = "countBy";
    private static final String EXISTS_BY = "existsBy";
    private static final String ORDER_BY = "OrderBy";

    private static final String[] OPERATORS = {
        "GreaterThanEqual", "LessThanEqual", "GreaterThan", "LessThan",
        "StartsWith", "EndsWith", "Contains", "NotNull", "Null",
        "Between", "Like", "Not", "In", "True", "False"
    };

    private MethodNameParser() {
    }

    public static ParsedQuery parse(final String methodName) {
        final Action action;
        String toParse;

        if (methodName.startsWith(FIND_BY)) {
            action = Action.FIND;
            toParse = methodName.substring(FIND_BY.length());
        } else if (methodName.startsWith(DELETE_BY)) {
            action = Action.DELETE;
            toParse = methodName.substring(DELETE_BY.length());
        } else if (methodName.startsWith(COUNT_BY)) {
            action = Action.COUNT;
            toParse = methodName.substring(COUNT_BY.length());
        } else if (methodName.startsWith(EXISTS_BY)) {
            action = Action.EXISTS;
            toParse = methodName.substring(EXISTS_BY.length());
        } else {
            return null;
        }

        // Extract OrderBy suffix
        final List<OrderClause> orderClauses = new ArrayList<>();
        final int orderByIdx = toParse.indexOf(ORDER_BY);
        if (orderByIdx >= 0) {
            final String orderPart = toParse.substring(orderByIdx + ORDER_BY.length());
            toParse = toParse.substring(0, orderByIdx);
            parseOrderBy(orderPart, orderClauses);
        }

        // Split by And/Or
        final List<Condition> conditions = new ArrayList<>();
        if (!toParse.isEmpty()) {
            parseConditions(toParse, conditions);
        }

        return new ParsedQuery(action, conditions, orderClauses);
    }

    private static void parseConditions(final String input, final List<Condition> conditions) {
        // Split by "And" and "Or" while tracking the connector
        final List<String> parts = new ArrayList<>();
        final List<Connector> connectors = new ArrayList<>();

        String remaining = input;
        while (!remaining.isEmpty()) {
            final int andIdx = findConnector(remaining, "And");
            final int orIdx = findConnector(remaining, "Or");

            if (andIdx < 0 && orIdx < 0) {
                parts.add(remaining);
                break;
            }

            if (andIdx >= 0 && (orIdx < 0 || andIdx < orIdx)) {
                parts.add(remaining.substring(0, andIdx));
                connectors.add(Connector.AND);
                remaining = remaining.substring(andIdx + 3);
            } else {
                parts.add(remaining.substring(0, orIdx));
                connectors.add(Connector.OR);
                remaining = remaining.substring(orIdx + 2);
            }
        }

        for (int i = 0; i < parts.size(); i++) {
            final String part = parts.get(i);
            final Connector connector = i < connectors.size() ? connectors.get(i) : Connector.AND;
            conditions.add(parseConditionPart(part, i == 0 ? Connector.AND : connectors.get(i - 1)));
        }
    }

    private static int findConnector(final String input, final String connector) {
        // Find "And" or "Or" that separates property conditions
        // Must not be at position 0 and must be followed by uppercase letter
        int idx = input.indexOf(connector);
        while (idx > 0) {
            if (idx + connector.length() < input.length()
                && Character.isUpperCase(input.charAt(idx + connector.length()))) {
                return idx;
            }
            idx = input.indexOf(connector, idx + 1);
        }
        return -1;
    }

    private static Condition parseConditionPart(final String part, final Connector connector) {
        for (final String op : OPERATORS) {
            if (part.endsWith(op)) {
                final String property = StringUtils.uncapitalize(part.substring(0, part.length() - op.length()));
                return new Condition(property, Operator.fromString(op), connector);
            }
        }
        // Default: equality
        return new Condition(StringUtils.uncapitalize(part), Operator.EQUAL, connector);
    }

    private static void parseOrderBy(final String orderPart, final List<OrderClause> orderClauses) {
        // OrderByNameAscAgeDesc -> [name ASC, age DESC]
        String remaining = orderPart;
        while (!remaining.isEmpty()) {
            // Try to find Asc or Desc in the middle first (followed by uppercase = another clause)
            final int ascIdx = findDirectionSuffix(remaining, "Asc");
            final int descIdx = findDirectionSuffix(remaining, "Desc");

            if (ascIdx > 0 && (descIdx < 0 || ascIdx < descIdx)) {
                final String prop = StringUtils.uncapitalize(remaining.substring(0, ascIdx));
                orderClauses.add(new OrderClause(prop, true));
                remaining = remaining.substring(ascIdx + 3);
            } else if (descIdx > 0) {
                final String prop = StringUtils.uncapitalize(remaining.substring(0, descIdx));
                orderClauses.add(new OrderClause(prop, false));
                remaining = remaining.substring(descIdx + 4);
            } else if (remaining.endsWith("Desc")) {
                final String prop = StringUtils.uncapitalize(remaining.substring(0, remaining.length() - 4));
                orderClauses.add(new OrderClause(prop, false));
                break;
            } else if (remaining.endsWith("Asc")) {
                final String prop = StringUtils.uncapitalize(remaining.substring(0, remaining.length() - 3));
                orderClauses.add(new OrderClause(prop, true));
                break;
            } else {
                // Default ascending
                orderClauses.add(new OrderClause(StringUtils.uncapitalize(remaining), true));
                break;
            }
        }
    }

    private static int findDirectionSuffix(final String input, final String suffix) {
        int idx = input.indexOf(suffix);
        while (idx > 0) {
            if (idx + suffix.length() < input.length()
                && Character.isUpperCase(input.charAt(idx + suffix.length()))) {
                return idx;
            }
            idx = input.indexOf(suffix, idx + 1);
        }
        return -1;
    }

    public enum Action {
        FIND, DELETE, COUNT, EXISTS
    }

    public enum Connector {
        AND, OR
    }

    public enum Operator {
        EQUAL,
        GREATER_THAN, GREATER_THAN_EQUAL,
        LESS_THAN, LESS_THAN_EQUAL,
        LIKE, STARTS_WITH, ENDS_WITH, CONTAINS,
        BETWEEN, IN, NOT,
        NULL, NOT_NULL,
        TRUE, FALSE;

        public static Operator fromString(final String s) {
            return switch (s) {
                case "GreaterThan" -> GREATER_THAN;
                case "GreaterThanEqual" -> GREATER_THAN_EQUAL;
                case "LessThan" -> LESS_THAN;
                case "LessThanEqual" -> LESS_THAN_EQUAL;
                case "Like" -> LIKE;
                case "StartsWith" -> STARTS_WITH;
                case "EndsWith" -> ENDS_WITH;
                case "Contains" -> CONTAINS;
                case "Between" -> BETWEEN;
                case "In" -> IN;
                case "Not" -> NOT;
                case "Null" -> NULL;
                case "NotNull" -> NOT_NULL;
                case "True" -> TRUE;
                case "False" -> FALSE;
                default -> EQUAL;
            };
        }

        public int parameterCount() {
            return switch (this) {
                case NULL, NOT_NULL, TRUE, FALSE -> 0;
                case BETWEEN -> 2;
                default -> 1;
            };
        }
    }

    public record Condition(String property, Operator operator, Connector connector) {
    }

    public record OrderClause(String property, boolean ascending) {
    }

    public record ParsedQuery(Action action, List<Condition> conditions, List<OrderClause> orderClauses) {
        public ParsedQuery {
            conditions = Collections.unmodifiableList(conditions);
            orderClauses = Collections.unmodifiableList(orderClauses);
        }
    }
}
