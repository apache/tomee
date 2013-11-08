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
import java.util.Map;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StoreManager;

/**
 * Handles optimistic lock versioning for a class.
 *
 * @author Abe White
 */
public interface VersionStrategy
    extends Strategy {

    /**
     * Set the version that uses this strategy. This will be called before use.
     */
    public void setVersion(Version owner);

    /**
     * Select the data for this indicator.
     *
     * @param mapping the known base class being selected; this may
     * not be the base class in the inheritance hierarchy
     * @return true if anything was selected; false otherwise
     */
    public boolean select(Select sel, ClassMapping mapping);

    /**
     * Load data.
     */
    public Object load(OpenJPAStateManager sm, JDBCStore store, Result res)
        throws SQLException;
    
    /**
     * Load data.
     */
    public Object load(OpenJPAStateManager sm, JDBCStore store, Result res, Joins joins)
        throws SQLException;

    /**
     * This method is called after data is loaded into the instance, in
     * case the version indicator works off of a state image.
     */
    public void afterLoad(OpenJPAStateManager sm, JDBCStore store);

    /**
     * Checks the version of the given state manager with the version
     * stored in memory.
     *
     * @return true if the in-memory version was up-to-date, false otherwise
     */
    public boolean checkVersion(OpenJPAStateManager sm, JDBCStore store,
        boolean updateVersion)
        throws SQLException;

    /**
     * @see StoreManager#compareVersion
     */
    public int compareVersion(Object v1, Object v2);

    /**
     * @return a Map<Column,Object> specifying how to update each version
     * column during a bulk update.
     *
     * @since 1.0.0
     */
    public Map<Column,? extends Object> getBulkUpdateValues();
}
