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
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;
import org.apache.openjpa.jdbc.kernel.exps.FilterValue;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Index;

/**
 * Dictionary for the SQL Server databases (Sybase and MS SQL Server).
 */
public abstract class AbstractSQLServerDictionary
    extends DBDictionary {

    public AbstractSQLServerDictionary() {
        reservedWordSet.addAll(Arrays.asList(new String[]{ "FILE", "INDEX" }));
        invalidColumnWordSet.addAll(reservedWordSet);
        systemTableSet.add("DTPROPERTIES");
        validationSQL = "SELECT GETDATE()";
        rangePosition = RANGE_POST_DISTINCT;

        supportsDeferredConstraints = false;
        supportsSelectEndIndex = true;
        allowsAliasInBulkClause = false;

        supportsAutoAssign = true;
        autoAssignClause = "IDENTITY";
        lastGeneratedKeyQuery = "SELECT @@IDENTITY";

        trimLeadingFunction = "LTRIM({0})";
        trimTrailingFunction = "RTRIM({0})";
        trimBothFunction = "LTRIM(RTRIM({0}))";
        concatenateFunction = "({0}+{1})";
        supportsModOperator = true;

        currentDateFunction = "GETDATE()";
        currentTimeFunction = "GETDATE()";
        currentTimestampFunction = "GETDATE()";

        useGetStringForClobs = true;
        useSetStringForClobs = true;
        useGetBytesForBlobs = true;
        useSetBytesForBlobs = true;
        binaryTypeName = "BINARY";
        blobTypeName = "IMAGE";
        longVarbinaryTypeName = "IMAGE";
        clobTypeName = "TEXT";
        longVarcharTypeName = "TEXT";
        dateTypeName = "DATETIME";
        timeTypeName = "DATETIME";
        timestampTypeName = "DATETIME";
        floatTypeName = "FLOAT(16)";
        doubleTypeName = "FLOAT(32)";
        integerTypeName = "INT";
        fixedSizeTypeNameSet.addAll(Arrays.asList(new String[]{
            "IMAGE", "TEXT", "NTEXT", "MONEY", "SMALLMONEY", "INT",
            "DOUBLE PRECISION", "DATETIME", "SMALLDATETIME",
            "EXTENDED TYPE", "SYSNAME", "SQL_VARIANT", "INDEX",
            "DATETIME2",
        }));
    }

    public Column[] getColumns(DatabaseMetaData meta, String catalog,
        String schemaName, String tableName, String colName, Connection conn)
        throws SQLException {
        Column[] cols = super.getColumns(meta, catalog, schemaName, tableName,
            colName, conn);
        for (int i = 0; cols != null && i < cols.length; i++)
            if (cols[i].getType() == Types.LONGVARCHAR)
                cols[i].setType(Types.CLOB);
        return cols;
    }

    public String getFullName(Index idx) {
        return toDBName(getNamingUtil().append(DBIdentifierType.INDEX,
            getFullIdentifier(idx.getTable(), false),idx.getIdentifier()));
    }

    public void setNull(PreparedStatement stmnt, int idx, int colType,
        Column col)
        throws SQLException {
        // SQLServer has some problems with setNull on lobs
        if (colType == Types.CLOB)
            stmnt.setString(idx, null);
        else if (colType == Types.BLOB)
            stmnt.setBytes(idx, null);
        else
            super.setNull(stmnt, idx, colType, col);
    }

    protected void appendSelectRange(SQLBuffer buf, long start, long end,
        boolean subselect) {
        // cannot use a value here, since SQLServer does not support
        // bound parameters in a "TOP" clause
        buf.append(" TOP ").append(Long.toString(end));
    }

    public void substring(SQLBuffer buf, FilterValue str, FilterValue start,
        FilterValue length) {
        if (length != null)
            super.substring(buf, str, start, length);
        else {
            buf.append("SUBSTRING(");
            str.appendTo(buf);
            buf.append(", ");
            start.appendTo(buf);
            buf.append(", LEN(");
            str.appendTo(buf);
            buf.append(")");
            buf.append(" - (");
            start.appendTo(buf);
            buf.append(" - 1))");
        }
    }
}
