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
package org.apache.openjpa.jdbc.meta;

import java.sql.SQLException;

import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.jdbc.sql.SelectExecutor;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * Maps a persistent field to the relational schema.
 *
 * @author Abe White
 * @since 0.4.0
 */
public interface FieldStrategy
    extends Strategy {

    /**
     * Set the class mapping using this strategy. This will be called before
     * use.
     */
    public void setFieldMapping(FieldMapping owner);

    /**
     * Return whether this mapping can perform the given select type.
     * Return 0 if the given type is not supported. If the given type is an
     * eager parallel type, return the number of UNIONed selects necessary
     * to fetch the data. Otherwise, return any positive number if the type
     * is supported. The given state manager may be null if selecting
     * multiple instances.
     *
     * @see Select
     */
    public int supportsSelect(Select sel, int type, OpenJPAStateManager sm,
        JDBCStore store, JDBCFetchConfiguration fetch);

    /**
     * Fill in parallel eager select for related objects.
     *
     * @see #select
     */
    public void selectEagerParallel(SelectExecutor sel, OpenJPAStateManager sm,
        JDBCStore store, JDBCFetchConfiguration fetch, int eagerMode);

    /**
     * Fill in joined select to related objects.
     *
     * @see #select
     */
    public void selectEagerJoin(Select sel, OpenJPAStateManager sm,
        JDBCStore store, JDBCFetchConfiguration fetch, int eagerMode);

    /**
     * Whether the eager joins or selects used by this field are to-many.
     */
    public boolean isEagerSelectToMany();

    /**
     * Select the virtual row columns of this mapping.
     *
     * @param sel the select to add to
     * @param sm the instance being loaded, or null if not
     * initialized yet or selecting for multiple instances
     * @param store the current store manager
     * @param fetch fetch configuration
     * @param eagerMode the eager fetch mode to use; this may be more
     * restrictive than the mode of the fetch configuration
     * @return &gt; 0 if this mapping requires the selected data
     * (if any), 0 if it selected data but does not
     * require it, or &lt; 0 if no data was selected
     */
    public int select(Select sel, OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, int eagerMode);

    /**
     * Load the batched eager result.
     *
     * @param res originally the {@link Result} to load from, but this
     * method may return a processed result form that will be
     * passed to subsequent calls
     */
    public Object loadEagerParallel(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Object res)
        throws SQLException;

    /**
     * Load the joined eager result.
     */
    public void loadEagerJoin(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res)
        throws SQLException;

    /**
     * Load virtual row data; the given result is not guaranteed to contain
     * data for this field, so the field mapping should make sure the
     * result contains its needed column data before loading.
     */
    public void load(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res)
        throws SQLException;

    /**
     * Load secondary data using a connection from the store manager.
     */
    public void load(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch)
        throws SQLException;

    ///////////
    // Queries
    ///////////

    /**
     * Translate the given external field value to its datastore equivalent.
     * If the mapping occupies multiple columns in the datastore, return an
     * object array, else return a single object. Simply returns the given
     * object by default.
     */
    public Object toDataStoreValue(Object val, JDBCStore store);

    /**
     * Translate the given key value to its datastore equivalent. If the
     * mapping occupies multiple columns in the datastore, return an object
     * array, else return a single object. Simply returns the given object by
     * default.
     */
    public Object toKeyDataStoreValue(Object val, JDBCStore store);

    /**
     * Append a where clause to the given statement checking if this value
     * is empty. Appends impossible-to-satisfy SQL by default.
     */
    public void appendIsEmpty(SQLBuffer sql, Select sel, Joins joins);

    /**
     * Append a where clause to the given statement checking if this value
     * is not empty. Appends always-satisfied SQL by default.
     */
    public void appendIsNotEmpty(SQLBuffer sql, Select sel, Joins joins);

    /**
     * Append a where clause to the given statement checking if this value
     * is null.
     */
    public void appendIsNull(SQLBuffer sql, Select sel, Joins joins);

    /**
     * Append a where clause to the given statement checking if this value
     * is not null.
     */
    public void appendIsNotNull(SQLBuffer sql, Select sel, Joins joins);

    /**
     * Append a where clause to the given statement checking the size
     * of the value.
     */
    public void appendSize(SQLBuffer sql, Select sel, Joins joins);

    /**
     * Append the ordered column alias to the given statement.
     */
    public void appendIndex(SQLBuffer sql, Select sel, Joins joins);

    /**
     * Append the entity discriminator value to the given statement.
     */
    public void appendType(SQLBuffer sql, Select sel, Joins joins);

    /**
     * Join this value to the class table. Does nothing by default.
     */
    public Joins join(Joins joins, boolean forceOuter);

    /**
     * Join the key value to the class table. Does nothing by default.
     */
    public Joins joinKey(Joins joins, boolean forceOuter);

    /**
     * Join this value's table to the table for the related first class object
     * type, if any. Does nothing by default.
     *
     * @param traverse if true, throw proper exception if it is not
     * possible for this mapping to traverse into the related type
     */
    public Joins joinRelation(Joins joins, boolean forceOuter,
        boolean traverse);

    /**
     * Join this value's table to the table for the related first class object
     * key type, if any. Does nothing by default.
     *
     * @param traverse if true, throw proper exception if it is not
     * possible for this mapping to traverse into the related type
     */
    public Joins joinKeyRelation(Joins joins, boolean forceOuter,
        boolean traverse);

    /**
     * Load this field value using the given result. The result
     * will contain the columns from {@link ValueMapping#getColumns}.
     */
    public Object loadProjection(JDBCStore store, JDBCFetchConfiguration fetch,
        Result res, Joins joins)
        throws SQLException;

    /**
     * Load this field's key value using the given result. The result will
     * contain the columns from {@link ValueMapping#getColumns}.
     */
    public Object loadKeyProjection(JDBCStore store,
        JDBCFetchConfiguration fetch, Result res, Joins joins)
        throws SQLException;

    //////////////
    // Versioning
    //////////////

    /**
     * Return true if this field can be used as part of a state image for
     * optimistic locking.
     */
    public boolean isVersionable();

    /**
     * Add a WHERE condition to the row for this field such that the field's
     * current DB value must equal the given previous value. Only versionable
     * mappings must implement this method meaningfully.
     */
    public void where(OpenJPAStateManager sm, JDBCStore store, RowManager rm,
        Object prevValue)
        throws SQLException;
}
