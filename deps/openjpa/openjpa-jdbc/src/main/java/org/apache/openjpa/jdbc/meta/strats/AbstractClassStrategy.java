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
import org.apache.openjpa.jdbc.meta.ClassStrategy;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.lib.rop.ResultObjectProvider;

/**
 * No-op strategy for easy extension.
 *
 * @author Abe White
 */
public abstract class AbstractClassStrategy
    extends AbstractStrategy
    implements ClassStrategy {

    /**
     * The owning class mapping.
     */
    protected ClassMapping cls = null;

    public void setClassMapping(ClassMapping owner) {
        cls = owner;
    }

    public boolean isPrimaryKeyObjectId(boolean hasAll) {
        return false;
    }

    public Joins joinSuperclass(Joins joins, boolean toThis) {
        return joins;
    }

    public boolean supportsEagerSelect(Select sel, OpenJPAStateManager sm,
        JDBCStore store, ClassMapping base, JDBCFetchConfiguration fetch) {
        return true;
    }

    public ResultObjectProvider customLoad(JDBCStore store, boolean subclasses,
        JDBCFetchConfiguration fetch, long startIdx, long endIdx)
        throws SQLException {
        return null;
    }

    public boolean customLoad(OpenJPAStateManager sm, JDBCStore store,
        PCState state, JDBCFetchConfiguration fetch)
        throws SQLException, ClassNotFoundException {
        return false;
    }

    public boolean customLoad(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result result)
        throws SQLException {
        return false;
    }
}
