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
package org.apache.openjpa.jdbc.kernel;

import java.sql.ResultSet;
import java.sql.Connection;
import java.util.Collection;
import java.util.Set;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.sql.JoinSyntaxes;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.meta.FieldMetaData;

/**
 * JDBC extensions to OpenJPA's {@link FetchConfiguration}.
 *
 * @author Abe White
 * @since 0.3.0
 */
public interface JDBCFetchConfiguration
    extends FetchConfiguration, EagerFetchModes, LRSSizes, JoinSyntaxes {

    /**
     * Return the eager fetch mode. Defaults to the
     * <code>openjpa.jdbc.EagerFetchMode</code> setting.
     */
    public int getEagerFetchMode();

    /**
     * Set the eager fetch mode. Defaults to the
     * <code>openjpa.jdbc.EagerFetchMode</code> setting.
     */
    public JDBCFetchConfiguration setEagerFetchMode(int mode);

    /**
     * Return the subclass fetch mode. Defaults to the
     * <code>openjpa.jdbc.SubclassFetchMode</code> setting.
     */
    public int getSubclassFetchMode();

    /**
     * Return the effective subclass fetch mode for the given type.
     */
    public int getSubclassFetchMode(ClassMapping cls);

    /**
     * Set the subclass fetch mode. Defaults to the
     * <code>openjpa.jdbc.SubclassFetchMode</code> setting.
     */
    public JDBCFetchConfiguration setSubclassFetchMode(int mode);

    /**
     * The result set type to use as a constant from {@link ResultSet}.
     * Defaults to the <code>openjpa.jdbc.ResultSetType</code> setting.
     */
    public int getResultSetType();

    /**
     * The result set type to use as a constant from {@link ResultSet}.
     * Defaults to the <code>openjpa.jdbc.ResultSetType</code> setting.
     */
    public JDBCFetchConfiguration setResultSetType(int type);

    /**
     * The fetch direction to use as a constant from {@link ResultSet}.
     * Defaults to the <code>openjpa.jdbc.FetchDirection</code> setting.
     */
    public int getFetchDirection();

    /**
     * The fetch direction to use as a constant from {@link ResultSet}.
     * Defaults to the <code>openjpa.jdbc.FetchDirection</code> setting.
     */
    public JDBCFetchConfiguration setFetchDirection(int direction);

    /**
     * The large result set size mode to use.
     * Defaults to the <code>openjpa.jdbc.LRSSize</code> setting.
     */
    public int getLRSSize();

    /**
     * The large result set size mode to use.
     * Defaults to the <code>openjpa.jdbc.LRSSize</code> setting.
     */
    public JDBCFetchConfiguration setLRSSize(int lrsSize);

    /**
     * The join syntax to use.
     */
    public int getJoinSyntax();

    /**
     * The join syntax to use.
     */
    public JDBCFetchConfiguration setJoinSyntax(int syntax);

    /**
     * Returns the names of the joins that this component will use
     * when loading objects. Defaults to the empty set.  This set is not
     * thread safe.
     *
     * @since 0.4.0.0
     */
    public Set<String> getJoins();

    /**
     * Return true if the given fully-qualified join has been added.
     *
     * @since 0.4.0.0
     */
    public boolean hasJoin(String field);

    /**
     * Adds <code>field</code> to the set of fully-qualified field names to
     * eagerly join when loading objects. Each class can have at most
     * one to-many eagerly joined fields.
     *
     * @since 0.4.0.0
     */
    public JDBCFetchConfiguration addJoin(String field);

    /**
     * Adds <code>fields</code> to the set of fully-qualified field names to
     * eagerly join when loading objects. Each class can have at most
     * one to-many eagerly joined fields.
     *
     * @since 0.4.0.0
     */
    public JDBCFetchConfiguration addJoins(Collection<String> fields);

    /**
     * Removes <code>field</code> to the set of fully-qualified field names to
     * eagerly join when loading objects.
     *
     * @since 0.4.0.0
     */
    public JDBCFetchConfiguration removeJoin(String field);

    /**
     * Removes <code>fields</code> from the set of fully-qualified
     * field names to eagerly join when loading objects.
     *
     * @since 0.4.0.0
     */
    public JDBCFetchConfiguration removeJoins(Collection<String> fields);

    /**
     * Clears the set of field names to join when loading data.
     *
     * @since 0.4.0.0
     */
    public JDBCFetchConfiguration clearJoins();

    /**
     * <p>The isolation level for queries issued to the database. This overrides
     * the persistence-unit-wide <code>openjpa.jdbc.TransactionIsolation</code>
     * value.</p>
     *
     * <p>Must be one of {@link Connection#TRANSACTION_NONE},
     * {@link Connection#TRANSACTION_READ_UNCOMMITTED},
     * {@link Connection#TRANSACTION_READ_COMMITTED},
     * {@link Connection#TRANSACTION_REPEATABLE_READ},
     * {@link Connection#TRANSACTION_SERIALIZABLE},
     * or -1 for the default connection level specified by the context in
     * which this fetch configuration is being used.</p>
     *
     * @since 0.9.7
     */
    public int getIsolation();

    /**
     * <p>The isolation level for queries issued to the database. This overrides
     * the persistence-unit-wide <code>openjpa.jdbc.TransactionIsolation</code>
     * value.</p>
     *
     * <p>Must be one of {@link Connection#TRANSACTION_NONE},
     * {@link Connection#TRANSACTION_READ_UNCOMMITTED},
     * {@link Connection#TRANSACTION_READ_COMMITTED},
     * {@link Connection#TRANSACTION_REPEATABLE_READ},
     * {@link Connection#TRANSACTION_SERIALIZABLE},
     * or -1 for the default connection level specified by the context in
     * which this fetch configuration is being used.</p>
     *
     * @since 0.9.7
     */
    public JDBCFetchConfiguration setIsolation(int level);

    /**
     * Convenience method to cast traversal to store-specific type.
     */
    public JDBCFetchConfiguration traverseJDBC(FieldMetaData fm);

    /**
     * Returns the names of the inner fetch joins that this component will use
     * when loading objects. Defaults to the empty set.  This set is not
     * thread safe.
     *
     * @since 1.0.3
     */
    public Set<String> getFetchInnerJoins();

    /**
     * Return true if the given fully-qualified inner fetch join has been added.
     *
     * @since 1.0.3
     */
    public boolean hasFetchInnerJoin(String field);

    /**
     * Adds <code>field</code> to the set of fully-qualified field names to
     * eagerly join when loading objects. Each class can have at most
     * one to-many eagerly joined fields.
     *
     * @since 1.0.3
     */
    public JDBCFetchConfiguration addFetchInnerJoin(String field);

    /**
     * Adds <code>fields</code> to the set of fully-qualified field names to
     * eagerly join when loading objects. Each class can have at most
     * one to-many eagerly joined fields.
     *
     * @since 1.0.3
     */
    public JDBCFetchConfiguration addFetchInnerJoins(Collection<String> fields);
    
    /**
     * Affirms if foreign key for a relation field will be pre-fetched as part of the owning object irrespective of
     * whether the field is included in the default fetch group of this fetch configuration. <br>
     * By default, foreign key for a relation field is pre-fetched as part of the owning object <em>only</em> if the
     * field in included in the default fetch group of this fetch configuration.
     * 
     * @since 2.2.0
     */
    public boolean getIgnoreDfgForFkSelect();

    /**
     * Affirms if foreign key for a relation field will be pre-fetched as part of the owning object irrespective of
     * whether the field is included in the default fetch group of this fetch configuration. <br>
     * By default, foreign key for a relation field is pre-fetched as part of the owning object <em>only</em> if the
     * field in included in the default fetch group of this fetch configuration.
     * 
     * @since 2.2.0
     */
    public void setIgnoreDfgForFkSelect(boolean b);
}
