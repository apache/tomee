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

import org.apache.openejb.data.meta.MethodMetadata;
import org.apache.openejb.data.query.MethodNameParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MethodMetadataTest {

    @Test
    void builtinStrategyWithNullParsedQuery() {
        final MethodMetadata meta = new MethodMetadata(MethodMetadata.Strategy.BUILTIN, null);
        assertEquals(MethodMetadata.Strategy.BUILTIN, meta.getStrategy());
        assertNull(meta.getParsedQuery());
    }

    @Test
    void methodNameStrategyWithParsedQuery() {
        final MethodNameParser.ParsedQuery parsed = MethodNameParser.parse("findByName");
        final MethodMetadata meta = new MethodMetadata(MethodMetadata.Strategy.METHOD_NAME, parsed);
        assertEquals(MethodMetadata.Strategy.METHOD_NAME, meta.getStrategy());
        assertNotNull(meta.getParsedQuery());
        assertEquals(MethodNameParser.Action.FIND, meta.getParsedQuery().action());
    }

    @Test
    void annotatedQueryStrategy() {
        final MethodMetadata meta = new MethodMetadata(MethodMetadata.Strategy.ANNOTATED_QUERY, null);
        assertEquals(MethodMetadata.Strategy.ANNOTATED_QUERY, meta.getStrategy());
    }

    @Test
    void findAnnotationStrategy() {
        final MethodMetadata meta = new MethodMetadata(MethodMetadata.Strategy.FIND_ANNOTATION, null);
        assertEquals(MethodMetadata.Strategy.FIND_ANNOTATION, meta.getStrategy());
    }

    @Test
    void allStrategyValuesExist() {
        assertEquals(4, MethodMetadata.Strategy.values().length);
        assertNotNull(MethodMetadata.Strategy.valueOf("BUILTIN"));
        assertNotNull(MethodMetadata.Strategy.valueOf("ANNOTATED_QUERY"));
        assertNotNull(MethodMetadata.Strategy.valueOf("FIND_ANNOTATION"));
        assertNotNull(MethodMetadata.Strategy.valueOf("METHOD_NAME"));
    }
}
