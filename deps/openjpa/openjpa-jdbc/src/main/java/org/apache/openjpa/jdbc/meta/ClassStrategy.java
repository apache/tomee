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
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.lib.rop.ResultObjectProvider;

/**
 * Mapping from a class to a relational schema.
 *
 * @author Abe White
 * @since 0.4.0
 */
public interface ClassStrategy
    extends Strategy {

    /**
     * Set the class mapping using this strategy. This will be called before
     * use.
     */
    public void setClassMapping(ClassMapping owner);

    /**
     * Return true if the this class' primary key columns correspond to the
     * base class' primary key columns used to construct oid values. Base
     * classes always return true. Classes that join to the base class table,
     * though, may not always join using the same columns the base class
     * uses for oid values, or may not use all the columns. When performing a
     * select, we will join down to the most-derived class that is identified
     * by oid values. We cannot use non-primary key field values for joining
     * during selects, because the field values to join on might be the ones
     * we're trying to select! Similarly, we can only reconstruct oid values
     * for selected objects using classes whose primary keys store oid values.
     *
     * @param hasAll if true, there must be a primary key column for every
     * base class primary key column; if false the primary key
     * must only match a subset of the base class primary key columns
     */
    public boolean isPrimaryKeyObjectId(boolean hasAll);

    /**
     * Join the mapping and its superclass.
     *
     * @param toThis if false, inner join to the superclass table; if
     * true, outer join from the superclass table to this table
     */
    public Joins joinSuperclass(Joins joins, boolean toThis);

    /**
     * Return true if this strategy can perform the given select from
     * the given <code>base</code> mapping.
     * The given state manager may be null if selecting multiple instances.
     */
    public boolean supportsEagerSelect(Select sel, OpenJPAStateManager sm,
        JDBCStore store, ClassMapping base, JDBCFetchConfiguration fetch);

    /**
     * Implement this method to customize obtaining a result containing all
     * instances of this class. Return null for standard loading.
     */
    public ResultObjectProvider customLoad(JDBCStore store, boolean subclasses,
        JDBCFetchConfiguration fetch, long startIdx, long endIdx)
        throws SQLException;

    /**
     * Implement this method to load the state of a given object, without
     * a previous {@link Result}. Return true if this method handles the
     * load. If the object does not exist in the data store, simply take no
     * action on it (but still return true). Return false to use default
     * loading.
     *
     * @param state if non-null, then you must initialize the state
     * and persistent object of the given state manager
     * (after determining the actual class of the object
     * from the database, if there are possible persistent
     * subclasses); initialization looks like this: <code>
     * sm.initialize (pcClass, state)</code>
     */
    public boolean customLoad(OpenJPAStateManager sm, JDBCStore store,
        PCState state, JDBCFetchConfiguration fetch)
        throws SQLException, ClassNotFoundException;

    /**
     * Implement this method to customize loading from a {@link Result}
     * into an instance. Return true if this mapping handles the
     * load; false if normal loading should proceed after calling this method.
     */
    public boolean customLoad(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result result)
        throws SQLException;
}
