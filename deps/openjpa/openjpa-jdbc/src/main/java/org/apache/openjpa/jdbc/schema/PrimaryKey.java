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
package org.apache.openjpa.jdbc.schema;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;

/**
 * Represents a table primary key. It can also represent a partial key,
 * aligning with the key information available from
 * {@link java.sql.DatabaseMetaData}.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
public class PrimaryKey
    extends LocalConstraint {

    private boolean _logical = false;

    /**
     * Default constructor.
     */
    public PrimaryKey() {
    }

    /**
     * Constructor.
     *
     * @param name the name of the primary key, if any
     * @param table the table of the primary key
     * @deprecated
     */
    public PrimaryKey(String name, Table table) {
        super(name, table);
    }

    public PrimaryKey(DBIdentifier name, Table table) {
        super(name, table);
    }

    public boolean isLogical() {
        return _logical;
    }

    public void setLogical(boolean logical) {
        _logical = logical;
    }

    void remove() {
        // check all foreign keys in the schema group, removing ones that
        // reference this primary key
        Table table = getTable();
        if (table != null && table.getSchema() != null
            && table.getSchema().getSchemaGroup() != null) {
            ForeignKey[] fks = table.getSchema().getSchemaGroup().
                findExportedForeignKeys(this);
            for (int i = 0; i < fks.length; i++)
                fks[i].getTable().removeForeignKey(fks[i]);
        }
        super.remove();
    }

    public void addColumn(Column col) {
        super.addColumn(col);
        col.setPrimaryKey(true);
        if (!_logical)
            col.setNotNull(true);
    }

    /**
     * Return true if the structure of this primary key matches that of
     * the given one (same table, same columns).
     */
    public boolean equalsPrimaryKey(PrimaryKey pk) {
        return equalsLocalConstraint(pk);
    }
}
