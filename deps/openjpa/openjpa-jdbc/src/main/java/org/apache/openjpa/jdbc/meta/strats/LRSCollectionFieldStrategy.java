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
 * Interface implemented by collection strategies so that they can
 * support large result set collections.
 *
 * @author Abe White
 */
public interface LRSCollectionFieldStrategy
    extends FieldStrategy {

    /**
     * The owning field mapping.
     */
    public FieldMapping getFieldMapping();

    /**
     * Return all independent mappings to which this strategy must join in
     * order to access collection elements, or empty array if none.
     *
     * @param traverse whether we're traversing through to the related type
     * @see ValueMapping#getIndependentTypeMappings
     * @see ClassMapping#EMPTY_MAPPINGS
     */
    public ClassMapping[] getIndependentElementMappings(boolean traverse);

    /**
     * Return the foreign key used to join to the owning field for the given
     * element mapping from {@link #getIndependentElementMappings} (or null).
     */
    public ForeignKey getJoinForeignKey(ClassMapping elem);

    /**
     * Return the columns holding the data for a collection element for the
     * given element mapping from {@link #getIndependentElementMappings}
     * (or null).
     */
    public Column[] getElementColumns(ClassMapping elem);

    /**
     * Implement this method to select the elements of this field for the
     * given element mapping from {@link #getIndependentElementMappings}
     * (or null). Elements of the result will be loaded with
     * {@link #loadElement}.
     */
    public void selectElement(Select sel, ClassMapping elem,
        JDBCStore store, JDBCFetchConfiguration fetch, int eagerMode,
        Joins joins);

    /**
     * Load an element of the collection. The given state manager might be
     * null if the load is for a projection or for processing eager parallel
     * results.
     */
    public Object loadElement(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res, Joins joins)
        throws SQLException;

    /**
     * Join this value's table to the table for the given element mapping
     * from {@link #getIndependentElementMappings} (or null).
     *
     * @see FieldMapping#joinRelation
     */
    public Joins joinElementRelation(Joins joins, ClassMapping elem);
}
