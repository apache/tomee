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
import org.apache.openejb.data.query.MethodNameParser.Operator;
import org.apache.openejb.data.query.MethodNameParser.ParsedQuery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MethodNameParserTest {

    @Test
    void parseSimpleFindBy() {
        final ParsedQuery result = MethodNameParser.parse("findByName");
        assertNotNull(result);
        assertEquals(Action.FIND, result.action());
        assertEquals(1, result.conditions().size());
        assertEquals("name", result.conditions().get(0).property());
        assertEquals(Operator.EQUAL, result.conditions().get(0).operator());
    }

    @Test
    void parseFindByMultipleConditions() {
        final ParsedQuery result = MethodNameParser.parse("findByNameAndAge");
        assertNotNull(result);
        assertEquals(Action.FIND, result.action());
        assertEquals(2, result.conditions().size());
        assertEquals("name", result.conditions().get(0).property());
        assertEquals("age", result.conditions().get(1).property());
    }

    @Test
    void parseFindByWithOperator() {
        final ParsedQuery result = MethodNameParser.parse("findByAgeGreaterThan");
        assertNotNull(result);
        assertEquals(1, result.conditions().size());
        assertEquals("age", result.conditions().get(0).property());
        assertEquals(Operator.GREATER_THAN, result.conditions().get(0).operator());
    }

    @Test
    void parseFindByWithOrderBy() {
        final ParsedQuery result = MethodNameParser.parse("findByAgeGreaterThanOrderByNameAsc");
        assertNotNull(result);
        assertEquals(1, result.conditions().size());
        assertEquals("age", result.conditions().get(0).property());
        assertEquals(Operator.GREATER_THAN, result.conditions().get(0).operator());
        assertEquals(1, result.orderClauses().size());
        assertEquals("name", result.orderClauses().get(0).property());
        assertTrue(result.orderClauses().get(0).ascending());
    }

    @Test
    void parseDeleteBy() {
        final ParsedQuery result = MethodNameParser.parse("deleteByName");
        assertNotNull(result);
        assertEquals(Action.DELETE, result.action());
        assertEquals(1, result.conditions().size());
        assertEquals("name", result.conditions().get(0).property());
    }

    @Test
    void parseCountBy() {
        final ParsedQuery result = MethodNameParser.parse("countByName");
        assertNotNull(result);
        assertEquals(Action.COUNT, result.action());
    }

    @Test
    void parseExistsBy() {
        final ParsedQuery result = MethodNameParser.parse("existsByEmail");
        assertNotNull(result);
        assertEquals(Action.EXISTS, result.action());
        assertEquals("email", result.conditions().get(0).property());
    }

    @Test
    void parseFindByBetween() {
        final ParsedQuery result = MethodNameParser.parse("findByAgeBetween");
        assertNotNull(result);
        assertEquals(1, result.conditions().size());
        assertEquals(Operator.BETWEEN, result.conditions().get(0).operator());
        assertEquals(2, result.conditions().get(0).operator().parameterCount());
    }

    @Test
    void parseFindByNull() {
        final ParsedQuery result = MethodNameParser.parse("findByEmailNull");
        assertNotNull(result);
        assertEquals(1, result.conditions().size());
        assertEquals(Operator.NULL, result.conditions().get(0).operator());
        assertEquals(0, result.conditions().get(0).operator().parameterCount());
    }

    @Test
    void parseFindByContains() {
        final ParsedQuery result = MethodNameParser.parse("findByNameContains");
        assertNotNull(result);
        assertEquals(1, result.conditions().size());
        assertEquals(Operator.CONTAINS, result.conditions().get(0).operator());
    }

    @Test
    void parseNonMatchingMethod() {
        assertNull(MethodNameParser.parse("getByName"));
        assertNull(MethodNameParser.parse("save"));
    }

    @Test
    void parseFindByLessThanEqual() {
        final ParsedQuery result = MethodNameParser.parse("findByAgeLessThanEqual");
        assertNotNull(result);
        assertEquals(1, result.conditions().size());
        assertEquals(Operator.LESS_THAN_EQUAL, result.conditions().get(0).operator());
    }
}
