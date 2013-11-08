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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;
import org.apache.openjpa.jdbc.kernel.exps.FilterValue;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Index;

/**
 * Dictionary for Pointbase Embedded.
 */
public class PointbaseDictionary
    extends DBDictionary {

    public PointbaseDictionary() {
        platform = "Pointbase Embedded";
        supportsDeferredConstraints = false;
        supportsMultipleNontransactionalResultSets = false;
        requiresAliasForSubselect = true;

        supportsLockingWithDistinctClause = false;
        supportsLockingWithMultipleTables = false;
        supportsLockingWithDistinctClause = false;

        bitTypeName = "TINYINT";
        blobTypeName = "BLOB(1M)";
        longVarbinaryTypeName = "BLOB(1M)";
        charTypeName = "CHARACTER{0}";
        clobTypeName = "CLOB(1M)";
        doubleTypeName = "DOUBLE PRECISION";
        floatTypeName = "FLOAT";
        bigintTypeName = "BIGINT";
        integerTypeName = "INTEGER";
        realTypeName = "REAL";
        smallintTypeName = "SMALLINT";
        tinyintTypeName = "TINYINT";

        // there is no build-in function for getting the last generated
        // key in Pointbase; using MAX will have to suffice
        supportsAutoAssign = true;
        lastGeneratedKeyQuery = "SELECT MAX({0}) FROM {1}";
        autoAssignTypeName = "BIGINT IDENTITY";
        
        // OpenJPA-2045: NAME has been removed from common reserved words to
        // only specific dictionaries
        reservedWordSet.add("NAME");
    }

    public int getPreferredType(int type) {
        switch (type) {
            case Types.LONGVARCHAR:
                return Types.CLOB;
            default:
                return super.getPreferredType(type);
        }
    }

    @Override
    public Column[] getColumns(DatabaseMetaData meta, String catalog,
        String schemaName, String tableName, String columnName, Connection conn)
        throws SQLException {
        return getColumns(meta, DBIdentifier.newCatalog(catalog), DBIdentifier.newSchema(schemaName),
            DBIdentifier.newTable(tableName), DBIdentifier.newColumn(columnName), conn);
    }

    @Override
    public Column[] getColumns(DatabaseMetaData meta, DBIdentifier catalog,
        DBIdentifier schemaName, DBIdentifier tableName, DBIdentifier columnName, Connection conn)
        throws SQLException {
        Column[] cols = super.getColumns(meta, catalog, schemaName, tableName,
            columnName, conn);

        // pointbase reports the type for a CLOB field as VARCHAR: override it
        for (int i = 0; cols != null && i < cols.length; i++)
            if (cols[i].getTypeIdentifier().getName().toUpperCase().startsWith("CLOB"))
                cols[i].setType(Types.CLOB);
        return cols;
    }

    @Override
    public String getFullName(Index index) {
        return toDBName(getNamingUtil().append(DBIdentifierType.INDEX, 
            getFullIdentifier(index.getTable(), false), index.getIdentifier()));
    }

    public void substring(SQLBuffer buf, FilterValue str, FilterValue start,
        FilterValue length) {
        // SUBSTRING in Pointbase is of the form:
        // SELECT SUBSTRING(SOME_COLUMN FROM 1 FOR 5)
        buf.append("SUBSTRING(");
        str.appendTo(buf);
        buf.append(" FROM ");
        start.appendTo(buf);
        if (length != null) {
            buf.append(" FOR ");
            length.appendTo(buf);
        }
        buf.append(")");
    }

    public void indexOf(SQLBuffer buf, FilterValue str, FilterValue find,
        FilterValue start) {
        buf.append("(POSITION(");
        find.appendTo(buf);
        buf.append(" IN ");
        if (start != null)
            substring(buf, str, start, null);
        else
            str.appendTo(buf);
        buf.append(")");
        if (start != null) {
            buf.append(" - 1 + ");
            start.appendTo(buf);
        }
        buf.append(")");
    }
}
