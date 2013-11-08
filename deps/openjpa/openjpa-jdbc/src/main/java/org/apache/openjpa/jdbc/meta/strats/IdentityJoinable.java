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

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Joinable;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.util.Id;

/**
 * {@link Joinable} for the datastore identity column.
 *
 * @author Abe White
 */
class IdentityJoinable
    implements Joinable {

    private final ClassMapping mapping;

    /**
     * Constructor; supply datastore identity mapping.
     */
    public IdentityJoinable(ClassMapping mapping) {
        this.mapping = mapping;
    }

    public int getFieldIndex() {
        return -1;
    }

    public Object getPrimaryKeyValue(Result res, Column[] cols, ForeignKey fk,
        JDBCStore store, Joins joins)
        throws SQLException {
        Column col = cols[0];
        if (fk != null)
            col = fk.getColumn(col);
        long id = res.getLong(col);
        if (id == 0 && res.wasNull())
            return null;
        return id;
    }

    public Column[] getColumns() {
        return mapping.getPrimaryKeyColumns();
    }

    public Object getJoinValue(Object val, Column col, JDBCStore store) {
        return val;
    }

    public Object getJoinValue(OpenJPAStateManager sm, Column col,
        JDBCStore store) {
        Id id = (Id) sm.getObjectId();
        return (id == null) ? null : id.getIdObject();
    }

    public void setAutoAssignedValue(OpenJPAStateManager sm, JDBCStore store,
        Column col, Object autogen) {
        long id = ((Number) autogen).longValue();
        sm.setObjectId(store.newDataStoreId(id, (ClassMapping)
            sm.getMetaData(), true));
    }
}
