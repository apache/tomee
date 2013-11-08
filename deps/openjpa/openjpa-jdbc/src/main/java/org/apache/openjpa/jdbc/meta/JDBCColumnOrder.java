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

import java.util.Comparator;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Select;

/**
 * Order by a synthetic order column.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
class JDBCColumnOrder
    implements JDBCOrder {

    private Column _col = null;
    private ColumnIO _io = null;

    /**
     * The synthetic column.
     */
    public Column getColumn() {
        return _col;
    }

    /**
     * The synthetic column.
     */
    public void setColumn(Column col) {
        _col = col;
    }

    /**
     * I/O restrictions for the synthetic column.
     */
    public ColumnIO getColumnIO() {
        return (_io == null) ? ColumnIO.UNRESTRICTED : _io;
    }

    /**
     * I/O restrictions for the synthetic column.
     */
    public void setColumnIO(ColumnIO io) {
        _io = io;
    }

    /**
     * @deprecated
     */
    public String getName() {
        return (_col == null) ? "" : _col.getName();
    }

    public DBIdentifier getIdentifier() {
        return (_col == null) ? DBIdentifier.newColumn("") : _col.getIdentifier();
    }

    public boolean isAscending() {
        return true;
    }

    public Comparator<?> getComparator() {
        return null;
    }

    public boolean isInRelation() {
        return false;
    }

    public void order(Select sel, ClassMapping elem, Joins joins) {
        if (_col != null)
            sel.orderBy(_col, true, joins, true);
    }
}
