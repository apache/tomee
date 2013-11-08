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
package org.apache.openjpa.jdbc.sql;

import java.sql.Types;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.exps.FilterValue;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.lib.identifier.IdentifierUtil;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.StoreException;

/**
 * Dictionary for Borland Interbase.
 */
public class InterbaseDictionary
    extends DBDictionary {

    private static final Localizer _loc = Localizer.forPackage
        (InterbaseDictionary.class);

    public InterbaseDictionary() {
        platform = "Borland Interbase";
        validationSQL = "SELECT 1 FROM RDB$DATABASE";
        supportsDeferredConstraints = false;

        useGetStringForClobs = true;
        useSetStringForClobs = true;
        useGetBytesForBlobs = true;
        useSetBytesForBlobs = true;

        // the JDBC driver claims 31, but that causes exceptions
        maxTableNameLength = 30;

        bigintTypeName = "NUMERIC(18,0)";
        integerTypeName = "INTEGER";
        doubleTypeName = "DOUBLE PRECISION";
        charTypeName = "CHAR(1)";
        blobTypeName = "BLOB";
        clobTypeName = "BLOB SUB_TYPE 1";
        bitTypeName = "SMALLINT";
        smallintTypeName = "SMALLINT";
        tinyintTypeName = "SMALLINT";

        // no support for lower-casing or finding the length of strings
        // (although it can be added to the database; see
        // http://bdn.borland.com/article/0,1410,27563,00.html )
        toLowerCaseFunction = null;
        stringLengthFunction = null;
    }

    @Override
    protected String getTableNameForMetadata(String tableName) {
        return getTableNameForMetadata(DBIdentifier.newTable(tableName));
    }

    @Override
    protected String getTableNameForMetadata(DBIdentifier tableName) {
        if (DBIdentifier.isNull(tableName)) {
            return IdentifierUtil.PERCENT;
        }
        return super.getTableNameForMetadata(tableName);
    }

    @Override
    protected String getColumnNameForMetadata(String columnName) {
        return getColumnNameForMetadata(DBIdentifier.newColumn(columnName));
    }

    @Override
    protected String getColumnNameForMetadata(DBIdentifier columnName) {
        if (DBIdentifier.isNull(columnName)) {
            return IdentifierUtil.PERCENT;
        }
        return super.getColumnNameForMetadata(columnName);
    }

    @Override
    protected String appendSize(Column col, String typeName) {
        if (col.isPrimaryKey() && col.getType() == Types.VARCHAR) {
            // reduce size of varchar primary key cols proportional to the
            // number of cols, because interbase caps the total pk size
            int numKeys = 1;
            if (col.getTable() != null
                && col.getTable().getPrimaryKey() != null)
                numKeys = col.getTable().getPrimaryKey().getColumns().length;
            col.setSize(Math.min(col.getSize(), 200 / numKeys));
        } else if (col.getType() == Types.VARCHAR && col.getSize() > 200
            && col.getTable() != null) {
            // indexed varchar cols have to be <= 250 chars
            Index[] idx = col.getTable().getIndexes();
            for (int i = 0; i < idx.length; i++) {
                if (idx[i].containsColumn(col)) {
                    col.setSize(Math.min(col.getSize(), 200));
                    break;
                }
            }
        }
        return super.appendSize(col, typeName);
    }

    @Override
    public void indexOf(SQLBuffer buf, FilterValue str, FilterValue find,
        FilterValue start) {
        throw new StoreException(_loc.get("indexof-not-supported", platform));
    }

    @Override
    public void substring(SQLBuffer buf, FilterValue str, FilterValue start,
        FilterValue end) {
        throw new StoreException(_loc.get("substring-not-supported",
            platform));
    }

    @Override
    public String[] getDropColumnSQL(Column column) {
        // Interbase uses "ALTER TABLE DROP <COLUMN_NAME>" rather than the
        // usual "ALTER TABLE DROP COLUMN <COLUMN_NAME>"
        return new String[]{ "ALTER TABLE "
            + getFullName(column.getTable(), false) + " DROP " + getColumnDBName(column) };
    }
}
