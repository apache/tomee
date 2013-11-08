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
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * Represents a value that can be joined to. Any column that a user
 * joins to must be "owned" by an entity that implements this interface.
 * The system maps columns to joinables to be able to decompose oids and
 * field values into individual join values on a per-column basis. This
 * allows us to support joins to only some of the columns of a mapping, and
 * to be loose with the ordering of foreign key columns relative to the
 * ordering of the joined-to columns. Having a separate interface for
 * joinables also allows us to perform tricks such as a vertically-mapped
 * application identity subclass transparently transforming columns of its
 * foreign key to the corresponding primary key fields in the base class.
 *
 * @author Abe White
 */
public interface Joinable 
    extends Serializable {

    /**
     * Return the field index of this joinable, or -1 if not a field.
     */
    public int getFieldIndex();

    /**
     * Return the value for this joinable from the given result, using the
     * given columns. If the given foreign key is non-null, use the foreign
     * key's columns by translating the given columns through
     * {@link ForeignKey#getColumn}.
     */
    public Object getPrimaryKeyValue(Result res, Column[] cols, ForeignKey fk,
        JDBCStore store, Joins joins)
        throws SQLException;

    /**
     * The columns managed by this joinable.
     */
    public Column[] getColumns();

    /**
     * Return the join value of the given column.
     *
     * @param val the value of the field for this joinable
     * @param col the column of this joinable whose value to return
     */
    public Object getJoinValue(Object val, Column col, JDBCStore store);

    /**
     * Return the join value of the given column.
     *
     * @param sm the instance from which to get the value
     * @param col the column whose value to return
     */
    public Object getJoinValue(OpenJPAStateManager sm, Column col,
        JDBCStore store);

    /**
     * Use the given auto-assigned value to set this join value's field
     * on the given instance.
     */
    public void setAutoAssignedValue(OpenJPAStateManager sm, JDBCStore store,
        Column col, Object autogen);
}
