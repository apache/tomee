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
package org.apache.openjpa.jdbc.meta.strats;

import java.sql.SQLException;

import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.FieldStrategy;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * Interface implemented by map strategies so that they can
 * support large result set maps.
 *
 * @author Abe White
 */
public interface LRSMapFieldStrategy
    extends FieldStrategy {

    /**
     * The owning field mapping.
     */
    public FieldMapping getFieldMapping();

    /**
     * Return all independent mappings to which this strategy must join in
     * order to access map keys, or empty array if none.
     *
     * @see ValueMapping#getIndependentTypeMappings
     * @see ClassMapping#EMPTY_MAPPINGS
     */
    public ClassMapping[] getIndependentKeyMappings(boolean traverse);

    /**
     * Return all independent mappings to which this strategy must join in
     * order to access map values, or empty array if none.
     *
     * @see ValueMapping#getIndependentTypeMappings
     * @see ClassMapping#EMPTY_MAPPINGS
     */
    public ClassMapping[] getIndependentValueMappings(boolean traverse);

    /**
     * Return the foreign key used to join to the owning field for the given
     * mapping from either {@link #getIndependentKeyMappings} or
     * {@link #getIndependentValueMappings} (or null).
     */
    public ForeignKey getJoinForeignKey(ClassMapping cls);

    /**
     * Return the columns holding data for a map key for the given key mapping
     * from {@link #getIndependentKeyMappings} or
     * {@link #getIndependentValueMappings} (or null).
     */
    public Column[] getKeyColumns(ClassMapping cls);

    /**
     * Return the columns holding data for a map value for the given value
     * mapping from {@link #getIndependentKeyMappings} or
     * {@link #getIndependentValueMappings} (or null).
     */
    public Column[] getValueColumns(ClassMapping cls);

    /**
     * Implement this method to select the keys of this field.
     * Elements of the result will be loaded with {@link #loadKey}.
     * This method is only used if the key is not derived from the value.
     */
    public void selectKey(Select sel, ClassMapping key, OpenJPAStateManager sm,
        JDBCStore store, JDBCFetchConfiguration fetch, Joins joins);

    /**
     * Load a key from the given result.
     * This method is only used if the key is not derived from the value.
     */
    public Object loadKey(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res, Joins joins)
        throws SQLException;

    /**
     * Some mapping derive keys from map values. Other mappings may return null.
     */
    public Object deriveKey(JDBCStore store, Object value);

    /**
     * Some mapping derive values from map keys. Other mappings may return null.
     */
    public Object deriveValue(JDBCStore store, Object key);

    /**
     * Implement this method to select the values of this field.
     * Elements of the result will be loaded with {@link #loadValue}.
     */
    public void selectValue(Select sel, ClassMapping val,
        OpenJPAStateManager sm, JDBCStore store, JDBCFetchConfiguration fetch, 
        Joins joins);

    /**
     * Load a value from the given result.
     */
    public Object loadValue(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res, Joins joins)
        throws SQLException;

    /**
     * Return results containing all keys and values for this map. If only
     * one result is needed, set both array indexes to the same result
     * instance. Also fill in the key and value joins in the given array.
     * The results will be loaded with the {@link #loadKey} or
     * {@link #deriveKey} and {@link #loadValue} methods.
     */
    public Result[] getResults(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, int eagerMode, Joins[] joins, boolean lrs)
        throws SQLException;

    /**
     * Join this value's table to the table for the given key mapping
     * from {@link #getIndependentKeyMappings} (or null).
     */
    public Joins joinKeyRelation(Joins joins, ClassMapping key);

    /**
     * Join this value's table to the table for the given value mapping
     * from {@link #getIndependentValueMappings} (or null).
     */
    public Joins joinValueRelation(Joins joins, ClassMapping val);
}
