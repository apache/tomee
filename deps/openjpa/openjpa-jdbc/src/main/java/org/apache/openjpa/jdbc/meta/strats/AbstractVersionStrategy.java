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
import java.util.Collections;
import java.util.Map;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Version;
import org.apache.openjpa.jdbc.meta.VersionStrategy;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StoreManager;

/**
 * No-op strategy for easy extension.
 *
 * @author Abe White
 */
public abstract class AbstractVersionStrategy
    extends AbstractStrategy
    implements VersionStrategy {

    /**
     * The owning version.
     */
    protected Version vers = null;

    public void setVersion(Version owner) {
        vers = owner;
    }

    public boolean select(Select sel, ClassMapping mapping) {
        return false;
    }

    public Object load(OpenJPAStateManager sm, JDBCStore store, Result res)
        throws SQLException {
    	return null;
    }

    public Object load(OpenJPAStateManager sm, JDBCStore store, Result res, Joins joins)
        throws SQLException {
        return null;
    }
    
    public void afterLoad(OpenJPAStateManager sm, JDBCStore store) {
    }

    public boolean checkVersion(OpenJPAStateManager sm, JDBCStore store,
        boolean updateVersion)
        throws SQLException {
        return !updateVersion;
    }

    public int compareVersion(Object v1, Object v2) {
        return StoreManager.VERSION_SAME;
    }

    public Map getBulkUpdateValues() {
        return Collections.EMPTY_MAP;
    }
}
