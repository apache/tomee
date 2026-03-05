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
package org.apache.openejb.data.test;

import org.apache.openejb.data.query.MethodNameParser;
import org.apache.openejb.data.query.MethodNameParser.Action;
import org.apache.openejb.data.query.MethodNameParser.Connector;
import org.apache.openejb.data.query.MethodNameParser.Operator;
import org.apache.openejb.data.query.MethodNameParser.ParsedQuery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MethodNameParserExtendedTest {

    // -- Or connector --

    @Test
    void parseFindByWithOrConnector() {
        final ParsedQuery result = MethodNameParser.parse("findByNameOrAge");
        assertNotNull(result);
        assertEquals(Action.FIND, result.action());
        assertEquals(2, result.conditions().size());
        assertEquals("name", result.conditions().get(0).property());
        assertEquals("age", result.conditions().get(1).property());
        assertEquals(Connector.OR, result.conditions().get(1).connector());
    }

    @Test
    void parseFindByWithMixedConnectors() {
        final ParsedQuery result = MethodNameParser.parse("findByNameAndAgeOrEmail");
        assertNotNull(result);
        assertEquals(3, result.conditions().size());
        assertEquals("name", result.conditions().get(0).property());
        assertEquals("age", result.conditions().get(1).property());
        assertEquals(Connector.AND, result.conditions().get(1).connector());
        assertEquals("email", result.conditions().get(2).property());
        assertEquals(Connector.OR, result.conditions().get(2).connector());
    }

    // -- All operators --

    @Test
    void parseFindByStartsWith() {
        final ParsedQuery result = MethodNameParser.parse("findByNameStartsWith");
        assertNotNull(result);
        assertEquals(Operator.STARTS_WITH, result.conditions().get(0).operator());
        assertEquals(1, result.conditions().get(0).operator().parameterCount());
    }

    @Test
    void parseFindByEndsWith() {
        final ParsedQuery result = MethodNameParser.parse("findByNameEndsWith");
        assertNotNull(result);
        assertEquals(Operator.ENDS_WITH, result.conditions().get(0).operator());
    }

    @Test
    void parseFindByLike() {
        final ParsedQuery result = MethodNameParser.parse("findByNameLike");
        assertNotNull(result);
        assertEquals(Operator.LIKE, result.conditions().get(0).operator());
    }

    @Test
    void parseFindByNot() {
        final ParsedQuery result = MethodNameParser.parse("findByNameNot");
        assertNotNull(result);
        assertEquals(Operator.NOT, result.conditions().get(0).operator());
        assertEquals(1, result.conditions().get(0).operator().parameterCount());
    }

    @Test
    void parseFindByNotNull() {
        final ParsedQuery result = MethodNameParser.parse("findByEmailNotNull");
        assertNotNull(result);
        assertEquals(Operator.NOT_NULL, result.conditions().get(0).operator());
        assertEquals(0, result.conditions().get(0).operator().parameterCount());
    }

    @Test
    void parseFindByIn() {
        final ParsedQuery result = MethodNameParser.parse("findByNameIn");
        assertNotNull(result);
        assertEquals(Operator.IN, result.conditions().get(0).operator());
        assertEquals(1, result.conditions().get(0).operator().parameterCount());
    }

    @Test
    void parseFindByTrue() {
        final ParsedQuery result = MethodNameParser.parse("findByActiveTrue");
        assertNotNull(result);
        assertEquals(Operator.TRUE, result.conditions().get(0).operator());
        assertEquals(0, result.conditions().get(0).operator().parameterCount());
    }

    @Test
    void parseFindByFalse() {
        final ParsedQuery result = MethodNameParser.parse("findByActiveFalse");
        assertNotNull(result);
        assertEquals(Operator.FALSE, result.conditions().get(0).operator());
        assertEquals(0, result.conditions().get(0).operator().parameterCount());
    }

    @Test
    void parseFindByGreaterThanEqual() {
        final ParsedQuery result = MethodNameParser.parse("findByAgeGreaterThanEqual");
        assertNotNull(result);
        assertEquals(Operator.GREATER_THAN_EQUAL, result.conditions().get(0).operator());
    }

    @Test
    void parseFindByLessThan() {
        final ParsedQuery result = MethodNameParser.parse("findByAgeLessThan");
        assertNotNull(result);
        assertEquals(Operator.LESS_THAN, result.conditions().get(0).operator());
    }

    // -- OrderBy variations --

    @Test
    void parseFindByWithOrderByDesc() {
        final ParsedQuery result = MethodNameParser.parse("findByNameOrderByAgeDesc");
        assertNotNull(result);
        assertEquals(1, result.conditions().size());
        assertEquals("name", result.conditions().get(0).property());
        assertEquals(1, result.orderClauses().size());
        assertEquals("age", result.orderClauses().get(0).property());
        assertFalse(result.orderClauses().get(0).ascending());
    }

    @Test
    void parseFindByWithMultipleOrderBy() {
        final ParsedQuery result = MethodNameParser.parse("findByAgeGreaterThanOrderByNameAscAgeDesc");
        assertNotNull(result);
        assertEquals(1, result.conditions().size());
        assertEquals(2, result.orderClauses().size());
        assertEquals("name", result.orderClauses().get(0).property());
        assertTrue(result.orderClauses().get(0).ascending());
        assertEquals("age", result.orderClauses().get(1).property());
        assertFalse(result.orderClauses().get(1).ascending());
    }

    @Test
    void parseOrderByWithoutConditions() {
        final ParsedQuery result = MethodNameParser.parse("findByNameOrderByNameAsc");
        assertNotNull(result);
        assertEquals(1, result.conditions().size());
        assertEquals(1, result.orderClauses().size());
        assertTrue(result.orderClauses().get(0).ascending());
    }

    // -- Multiple conditions with operators --

    @Test
    void parseFindByMultipleConditionsWithOperators() {
        final ParsedQuery result = MethodNameParser.parse("findByNameContainsAndAgeGreaterThan");
        assertNotNull(result);
        assertEquals(2, result.conditions().size());
        assertEquals("name", result.conditions().get(0).property());
        assertEquals(Operator.CONTAINS, result.conditions().get(0).operator());
        assertEquals("age", result.conditions().get(1).property());
        assertEquals(Operator.GREATER_THAN, result.conditions().get(1).operator());
    }

    @Test
    void parseFindByNullAndNotNull() {
        final ParsedQuery result = MethodNameParser.parse("findByEmailNullAndNameNotNull");
        assertNotNull(result);
        assertEquals(2, result.conditions().size());
        assertEquals(Operator.NULL, result.conditions().get(0).operator());
        assertEquals(Operator.NOT_NULL, result.conditions().get(1).operator());
    }

    // -- Delete variations --

    @Test
    void parseDeleteByMultipleConditions() {
        final ParsedQuery result = MethodNameParser.parse("deleteByNameAndAge");
        assertNotNull(result);
        assertEquals(Action.DELETE, result.action());
        assertEquals(2, result.conditions().size());
    }

    @Test
    void parseDeleteByWithOperator() {
        final ParsedQuery result = MethodNameParser.parse("deleteByAgeLessThan");
        assertNotNull(result);
        assertEquals(Action.DELETE, result.action());
        assertEquals(Operator.LESS_THAN, result.conditions().get(0).operator());
    }

    // -- Count / Exists variations --

    @Test
    void parseCountByWithOperator() {
        final ParsedQuery result = MethodNameParser.parse("countByAgeGreaterThan");
        assertNotNull(result);
        assertEquals(Action.COUNT, result.action());
        assertEquals(Operator.GREATER_THAN, result.conditions().get(0).operator());
    }

    @Test
    void parseExistsByWithMultipleConditions() {
        final ParsedQuery result = MethodNameParser.parse("existsByNameAndEmail");
        assertNotNull(result);
        assertEquals(Action.EXISTS, result.action());
        assertEquals(2, result.conditions().size());
    }

    // -- Non-matching --

    @Test
    void parseUnknownPrefixReturnsNull() {
        assertNull(MethodNameParser.parse("listByName"));
        assertNull(MethodNameParser.parse("searchByName"));
        assertNull(MethodNameParser.parse("removeByName"));
        assertNull(MethodNameParser.parse(""));
        assertNull(MethodNameParser.parse("count"));
        assertNull(MethodNameParser.parse("find"));
    }

    // -- Operator.fromString coverage --

    @Test
    void operatorFromStringUnknownDefaultsToEqual() {
        assertEquals(Operator.EQUAL, Operator.fromString("Unknown"));
        assertEquals(Operator.EQUAL, Operator.fromString(""));
    }

    // -- Operator.parameterCount full coverage --

    @Test
    void operatorParameterCountForAllOperators() {
        assertEquals(1, Operator.EQUAL.parameterCount());
        assertEquals(1, Operator.GREATER_THAN.parameterCount());
        assertEquals(1, Operator.GREATER_THAN_EQUAL.parameterCount());
        assertEquals(1, Operator.LESS_THAN.parameterCount());
        assertEquals(1, Operator.LESS_THAN_EQUAL.parameterCount());
        assertEquals(1, Operator.LIKE.parameterCount());
        assertEquals(1, Operator.STARTS_WITH.parameterCount());
        assertEquals(1, Operator.ENDS_WITH.parameterCount());
        assertEquals(1, Operator.CONTAINS.parameterCount());
        assertEquals(1, Operator.IN.parameterCount());
        assertEquals(1, Operator.NOT.parameterCount());
        assertEquals(2, Operator.BETWEEN.parameterCount());
        assertEquals(0, Operator.NULL.parameterCount());
        assertEquals(0, Operator.NOT_NULL.parameterCount());
        assertEquals(0, Operator.TRUE.parameterCount());
        assertEquals(0, Operator.FALSE.parameterCount());
    }

    // -- ParsedQuery immutability --

    @Test
    void parsedQueryConditionsAreImmutable() {
        final ParsedQuery result = MethodNameParser.parse("findByName");
        assertNotNull(result);
        assertThrows(UnsupportedOperationException.class, () -> result.conditions().clear());
    }

    @Test
    void parsedQueryOrderClausesAreImmutable() {
        final ParsedQuery result = MethodNameParser.parse("findByNameOrderByAgeAsc");
        assertNotNull(result);
        assertThrows(UnsupportedOperationException.class, () -> result.orderClauses().clear());
    }

    // -- Edge cases --

    @Test
    void parseFindByWithBetweenAndAnd() {
        final ParsedQuery result = MethodNameParser.parse("findByAgeBetweenAndName");
        assertNotNull(result);
        assertEquals(2, result.conditions().size());
        assertEquals(Operator.BETWEEN, result.conditions().get(0).operator());
        assertEquals(Operator.EQUAL, result.conditions().get(1).operator());
    }
}
