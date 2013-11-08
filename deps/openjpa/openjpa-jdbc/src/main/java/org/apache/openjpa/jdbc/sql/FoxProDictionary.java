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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.schema.PrimaryKey;

/**
 * Dictionary for Visual FoxPro via DataDirect SequeLink
 * and DataDirect ODBC FoxPro driver. This will not
 * work with any other combination of JDBC/ODBC server and ODBC driver.
 *  FoxPro has the following limitations:
 * <ul>
 * <li>Primary Keys and indexes cannot be created via JDBC</li>
 * <li>Only has fixed-length char fields: all strings must be
 * trimmed in result sets</li>
 * <li>Does not have sufficient support for foreign keys</li>
 * <li>ODBC driver cannot handle certain Aggregate functions.</li>
 * <li>Locking is extremeley unreliable. Multiple clients
 * accessing single datastore may result in concurrency
 * data validity errors.</li>
 * </ul>
 */
public class FoxProDictionary
    extends DBDictionary {

    public FoxProDictionary() {
        platform = "Visual FoxPro";
        joinSyntax = SYNTAX_TRADITIONAL;

        supportsForeignKeys = false;
        supportsDeferredConstraints = false;
        maxTableNameLength = 30;
        maxColumnNameLength = 30;
        maxIndexNameLength = 8;
        maxConstraintNameLength = 8;

        binaryTypeName = "GENERAL";
        blobTypeName = "GENERAL";
        longVarbinaryTypeName = "GENERAL";
        clobTypeName = "MEMO";
        longVarcharTypeName = "MEMO";
        dateTypeName = "TIMESTAMP";
        timeTypeName = "TIMESTAMP";
        varcharTypeName = "CHARACTER{0}";
        bigintTypeName = "DOUBLE";
        numericTypeName = "INTEGER";
        smallintTypeName = "INTEGER";
        bitTypeName = "NUMERIC(1)";
        integerTypeName = "INTEGER";
        tinyintTypeName = "INTEGER";
        decimalTypeName = "DOUBLE";
        doubleTypeName = "DOUBLE";
        realTypeName = "DOUBLE";
        floatTypeName = "NUMERIC(19,16)";

        // the max character literal length is actually 254, but for primary
        // keys, it is 240; default to that length so users can add PKs later
        characterColumnSize = 240;
        
        // OpenJPA-2045: NAME has been removed from common reserved words to
        // only specific dictionaries
        reservedWordSet.add("NAME");
    }

    @Override
    public String getString(ResultSet rs, int column)
        throws SQLException {
        // foxpro doesn't auto-truncate values.
        String str = rs.getString(column);
        if (str != null)
            str = str.trim();
        return str;
    }

    @Override
    public void setNull(PreparedStatement stmnt, int idx, int colType,
        Column col)
        throws SQLException {
        // ensure that blob/clob is handled with safe methods.
        switch (colType) {
            case Types.BLOB:
                stmnt.setBytes(idx, null);
                break;
            case Types.CLOB:
                stmnt.setString(idx, null);
                break;
            default:
                super.setNull(stmnt, idx, colType, col);
        }
    }

    @Override
    protected String appendSize(Column col, String typeName) {
        // foxpro does not like unsized column declarations.
        if (col.getSize() == 0) {
            if ("CHARACTER".equals(typeName))
                col.setSize(240);
            else if ("NUMERIC".equals(typeName))
                col.setSize(19);
        }
        return super.appendSize(col, typeName);
    }

    @Override
    protected String getPrimaryKeyConstraintSQL(PrimaryKey pk) {
        // this foxpro driver combination does not support primary keys
        return null;
    }

    @Override
    public String[] getCreateIndexSQL(Index index) {
        // foxpro JDBC access does not allow the creation of indexes
        return new String[0];
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
        try {
            Column[] cols = super.getColumns(meta, catalog, schemaName,
                tableName, columnName, conn);
            for (int i = 0; cols != null && i < cols.length; i++) {
                // foxpro returns an odd type "11" code for DATETIME fields
                if (cols[i].getType() == 11)
                    cols[i].setType(Types.TIMESTAMP);
                    // MEMO maps to LONGVARCHAR during reverse analysis
                else if ("MEMO".equals(cols[i].getTypeIdentifier().getName()))
                    cols[i].setType(Types.CLOB);
            }
            return cols;
        } catch (SQLException se) {
            // foxpro throws an exception if the table specified in the
            // column list is not found
            if (se.getErrorCode() == 562)
                return null;
            throw se;
        }
    }

    @Override
    public PrimaryKey[] getPrimaryKeys(DatabaseMetaData meta, String catalog,
        String schemaName, String tableName, Connection conn)
        throws SQLException {
        // this combination does not reliably return PK information
        return null;
    }

    @Override
    public PrimaryKey[] getPrimaryKeys(DatabaseMetaData meta, DBIdentifier catalog,
        DBIdentifier schemaName, DBIdentifier tableName, Connection conn)
        throws SQLException {
        // this combination does not reliably return PK information
        return null;
    }
}
