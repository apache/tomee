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

import java.util.Collection;
import javax.persistence.LockModeType;

import org.apache.openjpa.jdbc.kernel.EagerFetchModes;
import org.apache.openjpa.jdbc.kernel.LRSSizes;
import org.apache.openjpa.jdbc.sql.JoinSyntaxes;
import org.apache.openjpa.persistence.FetchPlan;

/**
 * JDBC extensions to the fetch plan.
 *
 * @since 0.4.1
 * @author Abe White
 * @author Pinaki Poddar
 * @published
 */
public interface JDBCFetchPlan
    extends FetchPlan {

    /**
     * Eager fetch mode in loading relations.
     */
    public FetchMode getEagerFetchMode();

    /**
     * Eager fetch mode in loading relations.
     */
    public JDBCFetchPlan setEagerFetchMode(FetchMode mode);

    /**
     * Eager fetch mode in loading subclasses.
     */
    public FetchMode getSubclassFetchMode();

    /**
     * Eager fetch mode in loading subclasses.
     */
    public JDBCFetchPlan setSubclassFetchMode(FetchMode mode);

    /**
     * Type of JDBC result set to use for query results.
     */
    public ResultSetType getResultSetType();

    /**
     * Type of JDBC result set to use for query results.
     */
    public JDBCFetchPlan setResultSetType(ResultSetType type);

    /**
     * Result set fetch direction.
     */
    public FetchDirection getFetchDirection();

    /**
     * Result set fetch direction.
     */
    public JDBCFetchPlan setFetchDirection(FetchDirection direction);

    /**
     * How to determine the size of a large result set.
     */
    public LRSSizeAlgorithm getLRSSizeAlgorithm();

    /**
     * How to determine the size of a large result set.
     */
    public JDBCFetchPlan setLRSSizeAlgorithm(LRSSizeAlgorithm lrsSizeAlgorithm);

    /**
     * SQL join syntax.
     */
    public JoinSyntax getJoinSyntax();

    /**
     * SQL join syntax.
     */
    public JDBCFetchPlan setJoinSyntax(JoinSyntax syntax);

    /**
     * The isolation level for queries issued to the database. This overrides
     * the persistence-unit-wide <code>openjpa.jdbc.TransactionIsolation</code>
     * value.
     *
     * @since 0.9.7
     */
    public IsolationLevel getIsolation();

    /**
     * The isolation level for queries issued to the database. This overrides
     * the persistence-unit-wide <code>openjpa.jdbc.TransactionIsolation</code>
     * value.
     *
     * @since 0.9.7
     */
    public JDBCFetchPlan setIsolation(IsolationLevel level);


    // covariant type support for return vals

    public JDBCFetchPlan addFetchGroup(String group);
    public JDBCFetchPlan addFetchGroups(Collection groups);
    public JDBCFetchPlan addFetchGroups(String... groups);
    public JDBCFetchPlan addField(Class cls, String field);
    public JDBCFetchPlan addField(String field);
    public JDBCFetchPlan addFields(Class cls, Collection fields);
    public JDBCFetchPlan addFields(Class cls, String... fields);
    public JDBCFetchPlan addFields(Collection fields);
    public JDBCFetchPlan addFields(String... fields);
    public JDBCFetchPlan clearFetchGroups();
    public JDBCFetchPlan clearFields();
    public JDBCFetchPlan removeFetchGroup(String group);
    public JDBCFetchPlan removeFetchGroups(Collection groups);
    public JDBCFetchPlan removeFetchGroups(String... groups);
    public JDBCFetchPlan removeField(Class cls, String field);
    public JDBCFetchPlan removeField(String field);
    public JDBCFetchPlan removeFields(Class cls, Collection fields);
    public JDBCFetchPlan removeFields(Class cls, String... fields);
    public JDBCFetchPlan removeFields(String... fields);
    public JDBCFetchPlan removeFields(Collection fields);
    public JDBCFetchPlan resetFetchGroups();
    public JDBCFetchPlan setQueryResultCacheEnabled(boolean cache);
    public JDBCFetchPlan setFetchBatchSize(int fetchBatchSize);
    public JDBCFetchPlan setLockTimeout(int timeout);
    public JDBCFetchPlan setMaxFetchDepth(int depth);
    public JDBCFetchPlan setReadLockMode(LockModeType mode);
    public JDBCFetchPlan setWriteLockMode(LockModeType mode);
    public JDBCFetchPlan setQueryTimeout(int timeout);

    /**
     * @deprecated use the {@link FetchMode} enum instead.
     */
    public static final int EAGER_NONE = EagerFetchModes.EAGER_NONE;

    /**
     * @deprecated use the {@link FetchMode} enum instead.
     */
    public static final int EAGER_JOIN = EagerFetchModes.EAGER_JOIN;

    /**
     * @deprecated use the {@link FetchMode} enum instead.
     */
    public static final int EAGER_PARALLEL = EagerFetchModes.EAGER_PARALLEL;

    /**
     * @deprecated use the {@link LRSSizeAlgorithm} enum instead.
     */
    public static final int SIZE_UNKNOWN = LRSSizes.SIZE_UNKNOWN;

    /**
     * @deprecated use the {@link LRSSizeAlgorithm} enum instead.
     */
    public static final int SIZE_LAST = LRSSizes.SIZE_LAST;

    /**
     * @deprecated use the {@link LRSSizeAlgorithm} enum instead.
     */
    public static final int SIZE_QUERY = LRSSizes.SIZE_QUERY;

    /**
     * @deprecated use the {@link JoinSyntax} enum instead.
     */
    public static final int SYNTAX_SQL92 = JoinSyntaxes.SYNTAX_SQL92;

    /**
     * @deprecated use the {@link JoinSyntax} enum instead.
     */
    public static final int SYNTAX_TRADITIONAL =
        JoinSyntaxes.SYNTAX_TRADITIONAL;

    /**
     * @deprecated use the {@link JoinSyntax} enum instead.
     */
    public static final int SYNTAX_DATABASE = JoinSyntaxes.SYNTAX_DATABASE;

    /**
     * @deprecated use {@link #setEagerFetchMode(FetchMode)} instead.
     */
    public JDBCFetchPlan setEagerFetchMode(int mode);

    /**
     * @deprecated use {@link #setSubclassFetchMode(FetchMode)} instead.
     */
    public JDBCFetchPlan setSubclassFetchMode(int mode);

    /**
     * @deprecated use {@link #setResultSetType(ResultSetType)} instead.
     */
    public JDBCFetchPlan setResultSetType(int mode);

    /**
     * @deprecated use {@link #setFetchDirection(FetchDirection)} instead.
     */
    public JDBCFetchPlan setFetchDirection(int direction);

    /**
     * @deprecated use {@link #getLRSSizeAlgorithm()} instead.
     */
    public int getLRSSize();

    /**
     * @deprecated use {@link #setLRSSizeAlgorithm(LRSSizeAlgorithm)} instead.
     */
    public JDBCFetchPlan setLRSSize(int lrsSizeMode);

    /**
     * @deprecated use {@link #setJoinSyntax(JoinSyntax)} instead.
     */
    public JDBCFetchPlan setJoinSyntax(int syntax);
    
    /**
     * Affirms if foreign key for a relation field will be pre-fetched as part of the owning object irrespective of
     * whether the field is included in the default fetch group of this fetch configuration. <br><br>
     * By default, foreign key for a relation field is pre-fetched as part of the owning object <em>only</em> if the
     * field in included in the default fetch group of this fetch configuration.
     * 
     * @since 2.2.0
     */
    public boolean getIgnoreDfgForFkSelect();

    /**
     * Affirms if foreign key for a relation field will be pre-fetched as part of the owning object irrespective of
     * whether the field is included in the default fetch group of this fetch configuration. <br><br>
     * By default, foreign key for a relation field is pre-fetched as part of the owning object <em>only</em> if the
     * field in included in the default fetch group of this fetch configuration.
     * 
     * @since 2.2.0
     */
    public void setIgnoreDfgForFkSelect(boolean b);
}
