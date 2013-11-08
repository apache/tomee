/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.persistence.jdbc;

import java.sql.ResultSet;

import junit.framework.TestCase;
import org.apache.openjpa.jdbc.kernel.EagerFetchModes;
import org.apache.openjpa.jdbc.kernel.LRSSizes;
import org.apache.openjpa.jdbc.sql.JoinSyntaxes;

public class TestJDBCEnumToKernelConstantMappings
    extends TestCase {

    public void testEagerFetchModes() {
        assertEquals(EagerFetchModes.EAGER_NONE,
            FetchMode.NONE.toKernelConstant());
        assertEquals(FetchMode.NONE,
            FetchMode.fromKernelConstant(
                EagerFetchModes.EAGER_NONE));
        assertEquals(FetchMode.NONE.toKernelConstant(),
            FetchMode.NONE.ordinal());

        assertEquals(EagerFetchModes.EAGER_JOIN,
            FetchMode.JOIN.toKernelConstant());
        assertEquals(FetchMode.JOIN,
            FetchMode.fromKernelConstant(
                EagerFetchModes.EAGER_JOIN));
        assertEquals(FetchMode.JOIN.toKernelConstant(),
            FetchMode.JOIN.ordinal());

        assertEquals(EagerFetchModes.EAGER_PARALLEL,
            FetchMode.PARALLEL.toKernelConstant());
        assertEquals(FetchMode.PARALLEL,
            FetchMode.fromKernelConstant(
                EagerFetchModes.EAGER_PARALLEL));
        assertEquals(FetchMode.PARALLEL.toKernelConstant(),
            FetchMode.PARALLEL.ordinal());

        assertEquals(getConstantCount(EagerFetchModes.class),
            FetchMode.values().length);
    }

    public void testLRSSizeType() {
        assertEquals(LRSSizes.SIZE_UNKNOWN,
            LRSSizeAlgorithm.UNKNOWN.toKernelConstant());
        assertEquals(LRSSizeAlgorithm.UNKNOWN,
            LRSSizeAlgorithm.fromKernelConstant(
                LRSSizes.SIZE_UNKNOWN));
        assertEquals(LRSSizeAlgorithm.UNKNOWN.toKernelConstant(),
            LRSSizeAlgorithm.UNKNOWN.ordinal());

        assertEquals(LRSSizes.SIZE_LAST,
            LRSSizeAlgorithm.LAST.toKernelConstant());
        assertEquals(LRSSizeAlgorithm.LAST,
            LRSSizeAlgorithm.fromKernelConstant(
                LRSSizes.SIZE_LAST));
        assertEquals(LRSSizeAlgorithm.LAST.toKernelConstant(),
            LRSSizeAlgorithm.LAST.ordinal());

        assertEquals(LRSSizes.SIZE_QUERY,
            LRSSizeAlgorithm.QUERY.toKernelConstant());
        assertEquals(LRSSizeAlgorithm.QUERY,
            LRSSizeAlgorithm.fromKernelConstant(
                LRSSizes.SIZE_QUERY));
        assertEquals(LRSSizeAlgorithm.QUERY.toKernelConstant(),
            LRSSizeAlgorithm.QUERY.ordinal());

        assertEquals(getConstantCount(LRSSizes.class),
            LRSSizeAlgorithm.values().length);
    }

    public void testJoinSyntaxType() {
        assertEquals(JoinSyntaxes.SYNTAX_SQL92,
            JoinSyntax.SQL92.toKernelConstant());
        assertEquals(JoinSyntax.SQL92,
            JoinSyntax.fromKernelConstant(
                JoinSyntaxes.SYNTAX_SQL92));
        assertEquals(JoinSyntax.SQL92.toKernelConstant(),
            JoinSyntax.SQL92.ordinal());

        assertEquals(JoinSyntaxes.SYNTAX_TRADITIONAL,
            JoinSyntax.TRADITIONAL.toKernelConstant());
        assertEquals(JoinSyntax.TRADITIONAL,
            JoinSyntax.fromKernelConstant(
                JoinSyntaxes.SYNTAX_TRADITIONAL));
        assertEquals(JoinSyntax.TRADITIONAL.toKernelConstant(),
            JoinSyntax.TRADITIONAL.ordinal());

        assertEquals(JoinSyntaxes.SYNTAX_DATABASE,
            JoinSyntax.DATABASE.toKernelConstant());
        assertEquals(JoinSyntax.DATABASE,
            JoinSyntax.fromKernelConstant(
                JoinSyntaxes.SYNTAX_DATABASE));
        assertEquals(JoinSyntax.DATABASE.toKernelConstant(),
            JoinSyntax.DATABASE.ordinal());

        assertEquals(getConstantCount(JoinSyntaxes.class),
            JoinSyntax.values().length);
    }

    public void testResultSetType() {
        assertEquals(ResultSet.TYPE_FORWARD_ONLY,
            ResultSetType.FORWARD_ONLY.toKernelConstant());
        assertEquals(ResultSetType.FORWARD_ONLY,
            ResultSetType.fromKernelConstant(
                ResultSet.TYPE_FORWARD_ONLY));

        assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSetType.SCROLL_INSENSITIVE.toKernelConstant());
        assertEquals(ResultSetType.SCROLL_INSENSITIVE,
            ResultSetType.fromKernelConstant(
                ResultSet.TYPE_SCROLL_INSENSITIVE));

        assertEquals(ResultSet.TYPE_SCROLL_SENSITIVE,
            ResultSetType.SCROLL_SENSITIVE.toKernelConstant());
        assertEquals(ResultSetType.SCROLL_SENSITIVE,
            ResultSetType.fromKernelConstant(
                ResultSet.TYPE_SCROLL_SENSITIVE));

        assertEquals(3, ResultSetType.values().length);
    }

    public void testFetchDirection() {
        assertEquals(ResultSet.FETCH_FORWARD,
            FetchDirection.FORWARD.toKernelConstant());
        assertEquals(FetchDirection.FORWARD,
            FetchDirection.fromKernelConstant(
                ResultSet.FETCH_FORWARD));

        assertEquals(ResultSet.FETCH_REVERSE,
            FetchDirection.REVERSE.toKernelConstant());
        assertEquals(FetchDirection.REVERSE,
            FetchDirection.fromKernelConstant(
                ResultSet.FETCH_REVERSE));

        assertEquals(ResultSet.FETCH_UNKNOWN,
            FetchDirection.UNKNOWN.toKernelConstant());
        assertEquals(FetchDirection.UNKNOWN,
            FetchDirection.fromKernelConstant(
                ResultSet.FETCH_UNKNOWN));

        assertEquals(3, FetchDirection.values().length);
    }


    private int getConstantCount(Class cls) {
        return cls.getDeclaredFields().length;
    }
}
