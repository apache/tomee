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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SortPropertyValidationTest {

    @Test
    void acceptsSimpleAttributeNames() {
        assertEquals("name", CriteriaQueryBuilder.validateSortProperty("name"));
        assertEquals("priority", CriteriaQueryBuilder.validateSortProperty("priority"));
        assertEquals("_internal$Field2", CriteriaQueryBuilder.validateSortProperty("_internal$Field2"));
    }

    @Test
    void acceptsDottedAttributePaths() {
        assertEquals("address.city", CriteriaQueryBuilder.validateSortProperty("address.city"));
        assertEquals("owner.address.zipCode", CriteriaQueryBuilder.validateSortProperty("owner.address.zipCode"));
    }

    @Test
    void rejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> CriteriaQueryBuilder.validateSortProperty(null));
    }

    @Test
    void rejectsEmptyAndMalformedPaths() {
        assertThrows(IllegalArgumentException.class, () -> CriteriaQueryBuilder.validateSortProperty(""));
        assertThrows(IllegalArgumentException.class, () -> CriteriaQueryBuilder.validateSortProperty(".name"));
        assertThrows(IllegalArgumentException.class, () -> CriteriaQueryBuilder.validateSortProperty("name."));
        assertThrows(IllegalArgumentException.class, () -> CriteriaQueryBuilder.validateSortProperty("a..b"));
        assertThrows(IllegalArgumentException.class, () -> CriteriaQueryBuilder.validateSortProperty("1name"));
    }

    @Test
    void rejectsPropertiesContainingExpressions() {
        assertThrows(IllegalArgumentException.class, () -> CriteriaQueryBuilder.validateSortProperty(
            "(SELECT u.password FROM Users u WHERE u.admin = TRUE)"));
        assertThrows(IllegalArgumentException.class, () -> CriteriaQueryBuilder.validateSortProperty(
            "name, (CASE WHEN (SELECT COUNT(u) FROM Users u) > 0 THEN 1 ELSE 2 END)"));
        assertThrows(IllegalArgumentException.class, () -> CriteriaQueryBuilder.validateSortProperty("name ASC, e.secret"));
        assertThrows(IllegalArgumentException.class, () -> CriteriaQueryBuilder.validateSortProperty("name'"));
        assertThrows(IllegalArgumentException.class, () -> CriteriaQueryBuilder.validateSortProperty("name "));
    }
}
