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

import java.io.Serializable;
import java.sql.SQLException;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.util.MetaDataException;

/**
 * Interface implemented by all mapping strategies.
 *
 * @author Abe White
 * @since 0.4.0
 */
public interface Strategy 
    extends Serializable {

    /**
     * Return the alias of this strategy. For custom strategies, return the
     * full class name.
     */
    public String getAlias();

    /**
     * Map the owning mapping using this strategy.
     *
     * @param adapt if true, use the owning mapping's raw mapping info
     * to set its ORM data; if false, ORM data will already be set
     * @throws MetaDataException if unable to map
     */
    public void map(boolean adapt);

    /**
     * Perform caching and other initialization operations. This method is
     * called after {@link #map}, and after all related components have been
     * mapped as well.
     */
    public void initialize();

    /**
     * Set values for the mapping into the proper rows. For class mappings,
     * this method	will be called only after the corresponding method has
     * been called for all fields of this mapping.
     */
    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException;

    /**
     * Set values for the mapping into the proper rows.
     *
     * @see #insert
     */
    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException;

    /**
     * Set the where values appropriately to delete the proper instance,
     * and set all relations on non-secondary tables as updates. This allows
     * foreign key analysis.
     *
     * @see #insert
     */
    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException;

    /**
     * Return {@link Boolean#FALSE} if this mapping does not customize the
     * insert process, {@link Boolean#TRUE} if it does, or null if it does
     * customize the insert, but also relies on the standard insert method
     * being called. Implement the {@link #customInsert} method
     * to implement the custom insertion behavior.
     */
    public Boolean isCustomInsert(OpenJPAStateManager sm, JDBCStore store);

    /**
     * Return {@link Boolean#FALSE} if this mapping does not customize the
     * update process, {@link Boolean#TRUE} if it does, or null if it does
     * customize the update, but also relies on the standard update method
     * being called. Implement the {@link #customUpdate} method
     * to override the default update behavior.
     */
    public Boolean isCustomUpdate(OpenJPAStateManager sm, JDBCStore store);

    /**
     * Return {@link Boolean#FALSE} if this mapping does not customize the
     * delete process, {@link Boolean#TRUE} if it does, or null if it does
     * customize the delete, but also relies on the standard delete method
     * being called. Implement the {@link #customDelete} method
     * to override the default deletion behavior.
     */
    public Boolean isCustomDelete(OpenJPAStateManager sm, JDBCStore store);

    /**
     * Override this method to customize flushing this mapping. For classes,
     * this method must also flush all fields. For fields, this method
     * is called after the owning object is inserted, so if this field is in
     * a row with other fields, that row will already exist.
     */
    public void customInsert(OpenJPAStateManager sm, JDBCStore store)
        throws SQLException;

    /**
     * Override this method to customize flushing this mapping. For classes,
     * this method must also flush all fields.
     */
    public void customUpdate(OpenJPAStateManager sm, JDBCStore store)
        throws SQLException;

    /**
     * Override this method to customize flushing this mapping. For classes,
     * this method must also flush all fields. For fields, this method
     * will be called after the owning object is deleted.
     */
    public void customDelete(OpenJPAStateManager sm, JDBCStore store)
        throws SQLException;
}
