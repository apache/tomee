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

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.exps.FilterValue;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.PrimaryKey;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.schema.Unique;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.ReferentialIntegrityException;

/**
 * Dictionary for HyperSQL (HSQLDB) database.
 */
public class HSQLDictionary extends DBDictionary {

    /**
     * Sets whether HSQL should use "CREATED CACHED TABLE" rather than
     * "CREATE TABLE", which allows disk-based database operations.
     */
    public boolean cacheTables = false;

    private int dbMajorVersion;
    private int dbMinorVersion;
    private int violation_of_unique_index_or_constraint;

    private SQLBuffer _oneBuffer = new SQLBuffer(this).append("1");

    public HSQLDictionary() {
        platform = "HSQL";
        validationSQL = "CALL 1";
        concatenateFunction = "CONCAT({0},{1})";
        closePoolSQL = "SHUTDOWN";

        supportsAutoAssign = true;
        lastGeneratedKeyQuery = "CALL IDENTITY()";
        autoAssignClause = "IDENTITY";
        autoAssignTypeName = "INTEGER";
        nextSequenceQuery = "SELECT NEXT VALUE FOR {0} FROM"
            + " INFORMATION_SCHEMA.SYSTEM_SEQUENCES";
        crossJoinClause = "JOIN";
        requiresConditionForCrossJoin = true;
        stringLengthFunction = "LENGTH({0})";
        trimLeadingFunction = "LTRIM({0})";
        trimTrailingFunction = "RTRIM({0})";
        trimBothFunction = "LTRIM(RTRIM({0}))";

        supportsSelectForUpdate = false;
        supportsSelectStartIndex = true;
        supportsSelectEndIndex = true;
        supportsDeferredConstraints = false;

        doubleTypeName = "NUMERIC";

        supportsNullTableForGetPrimaryKeys = true;
        supportsNullTableForGetIndexInfo = true;

        requiresCastForMathFunctions = true;
        requiresCastForComparisons = true;

        reservedWordSet.addAll(Arrays.asList(new String[]{
            "BEFORE", "BIGINT", "BINARY", "CACHED", "DATETIME", "LIMIT",
            "LONGVARBINARY", "LONGVARCHAR", "OBJECT", "OTHER",
            "SAVEPOINT", "TEMP", "TEXT", "TRIGGER", "TINYINT",
            "VARBINARY", "VARCHAR_IGNORECASE",
        }));
        
        fixedSizeTypeNameSet.addAll(Arrays.asList(new String[]{
            "TEXT"
        }));
    }

    /**
     * Determine HSQLDB version and configure itself accordingly.
     */
    @Override
    public void connectedConfiguration(Connection conn) throws SQLException {
        super.connectedConfiguration(conn);

        determineHSQLDBVersion(conn) ;

        if (dbMajorVersion == 1) {
            blobTypeName = "VARBINARY";
            useGetObjectForBlobs = true;
            rangePosition = RANGE_PRE_DISTINCT;
            // HSQL 1.8.0 does support schema names in the table ("schema.table"),
            // but doesn't support it for columns references ("schema.table.column")
            useSchemaName = false;
        }
        if (dbMajorVersion > 1 && dbMinorVersion > 0) {
            nextSequenceQuery += " LIMIT 1";
        }
        String packageName;
        String fieldName;
        if (dbMajorVersion > 1) {
            // default value for "X_23505"
            violation_of_unique_index_or_constraint = 104;
            packageName = "org.hsqldb.error.ErrorCode";
            fieldName = "X_23505";
        } else {
            // default value for "VIOLATION_OF_UNIQUE_INDEX"
            violation_of_unique_index_or_constraint = 9; 
            packageName = "org.hsqldb.Trace";
            fieldName = "VIOLATION_OF_UNIQUE_INDEX";
        }
        try {
            Class<?> cls = Class.forName(packageName);
            Field fld = cls.getField(fieldName);
            violation_of_unique_index_or_constraint = fld.getInt(null);
        } catch (Exception e) {
        }
    }

    /**
     * Determine HSQLDB version either by using JDBC 3 method or, if it
     * is not available, by parsing the value returned by
     * {@linkplain DatabaseMetaData#getDatabaseProductVersion()}.
     */
    protected void determineHSQLDBVersion(Connection con) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();

        if (isJDBC3) {
            dbMajorVersion = metaData.getDatabaseMajorVersion();
            dbMinorVersion = metaData.getDatabaseMinorVersion();
        } else {
            // String is like "2.0.0"
            String productVersion = metaData.getDatabaseProductVersion();
            String[] version = productVersion.split("\\.") ;
            dbMajorVersion = Integer.parseInt(version[0]) ;
            dbMinorVersion = Integer.parseInt(version[1]);
        }
    }

    @Override
    public int getJDBCType(int metaTypeCode, boolean lob) {
        int type = super.getJDBCType(metaTypeCode, lob);
        switch (type) {
            case Types.BIGINT:
                if (metaTypeCode == JavaTypes.BIGINTEGER)
                    return Types.NUMERIC;
                break;
        }
        return type;
    }

    @Override
    public int getPreferredType(int type) {
        if (dbMajorVersion > 1) {
            return super.getPreferredType(type);
        }
        switch (type) {
            case Types.CLOB:
                return Types.VARCHAR;
            case Types.BLOB:
                return Types.VARBINARY;
            default:
                return super.getPreferredType(type);
        }
    }

    @Override
    public String[] getAddPrimaryKeySQL(PrimaryKey pk) {
        return new String[0];
    }

    @Override
    public String[] getDropPrimaryKeySQL(PrimaryKey pk) {
        return new String[0];
    }

    @Override
    public String[] getAddColumnSQL(Column column) {
        return new String[]{ "ALTER TABLE "
            + getFullName(column.getTable(), false)
            + " ADD COLUMN " + getDeclareColumnSQL(column, true) };
    }

    @Override
    public String[] getCreateTableSQL(Table table) {
        StringBuilder buf = new StringBuilder();
        buf.append("CREATE ");
        if (cacheTables)
            buf.append("CACHED ");
        buf.append("TABLE ").append(getFullName(table, false)).append(" (");

        Column[] cols = table.getColumns();
        for (int i = 0; i < cols.length; i++) {
            if (i > 0)
                buf.append(", ");
            buf.append(getDeclareColumnSQL(cols[i], false));
        }

        PrimaryKey pk = table.getPrimaryKey();
        String pkStr;
        if (pk != null) {
            pkStr = getPrimaryKeyConstraintSQL(pk);
            if (!StringUtils.isEmpty(pkStr))
                buf.append(", ").append(pkStr);
        }

        Unique[] unqs = table.getUniques();
        String unqStr;
        for (int i = 0; i < unqs.length; i++) {
            unqStr = getUniqueConstraintSQL(unqs[i]);
            if (unqStr != null)
                buf.append(", ").append(unqStr);
        }

        buf.append(")");
        return new String[]{ buf.toString() };
    }

    @Override
    protected String getPrimaryKeyConstraintSQL(PrimaryKey pk) {
        Column[] cols = pk.getColumns();
        if (cols.length == 1 && cols[0].isAutoAssigned())
            return null;
        return super.getPrimaryKeyConstraintSQL(pk);
    }

    public boolean isSystemIndex(String name, Table table) {
        return name.toUpperCase().startsWith("SYS_");
    }

    @Override
    public boolean isSystemIndex(DBIdentifier name, Table table) {
        if (DBIdentifier.isNull(name)) {
            return false;
        }
        return name.getName().toUpperCase().startsWith("SYS_");
    }

    @Override
    protected String getSequencesSQL(String schemaName, String sequenceName) {
        return getSequencesSQL(DBIdentifier.newSchema(schemaName), DBIdentifier.newSequence(sequenceName));
    }

    @Override
    protected String getSequencesSQL(DBIdentifier schemaName, DBIdentifier sequenceName) {
        StringBuilder buf = new StringBuilder();
        buf.append("SELECT SEQUENCE_SCHEMA, SEQUENCE_NAME FROM ").
            append("INFORMATION_SCHEMA.SYSTEM_SEQUENCES");
        if (!DBIdentifier.isNull(schemaName) || !DBIdentifier.isNull(sequenceName))
            buf.append(" WHERE ");
        if (!DBIdentifier.isNull(schemaName)) {
            buf.append("SEQUENCE_SCHEMA = ?");
            if (!DBIdentifier.isNull(sequenceName))
                buf.append(" AND ");
        }
        if (!DBIdentifier.isNull(sequenceName))
            buf.append("SEQUENCE_NAME = ?");
        return buf.toString();
    }

    @Override
    public SQLBuffer toOperation(String op, SQLBuffer selects,
        SQLBuffer from, SQLBuffer where, SQLBuffer group, SQLBuffer having,
        SQLBuffer order, boolean distinct, long start, long end,
        String forUpdateClause) {
        // hsql requires ordering when limit is used
        if ((start != 0 || end != Long.MAX_VALUE)
            && (order == null || order.isEmpty()))
            order = _oneBuffer;
        return super.toOperation(op, selects, from, where, group, having,
            order, distinct, start, end, forUpdateClause);
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

        for (int i = 0; cols != null && i < cols.length; i++)
            if ("BOOLEAN".equalsIgnoreCase(cols[i].getTypeIdentifier().getName()))
                cols[i].setType(Types.BIT);
        return cols;
    }

    @Override
    public void setDouble(PreparedStatement stmnt, int idx, double val,
        Column col)
        throws SQLException {
        // HSQL has a bug where it cannot store a double if it is
        // exactly the same as Long.MAX_VALUE or MIN_VALUE
        if (val == Long.MAX_VALUE || val == Long.MIN_VALUE) {
            stmnt.setLong(idx, (long) val);
        } else  {
            super.setDouble(stmnt, idx, val, col);
        }
    }

    @Override
    public void setBigDecimal(PreparedStatement stmnt, int idx, BigDecimal val,
        Column col)
        throws SQLException {
        // hsql can't compare a BigDecimal equal to any other type, so try
        // to set type based on column
        int type = (val == null || col == null) ? JavaTypes.BIGDECIMAL
            : col.getJavaType();
        switch (type) {
            case JavaTypes.DOUBLE:
            case JavaTypes.DOUBLE_OBJ:
                setDouble(stmnt, idx, val.doubleValue(), col);
                break;
            case JavaTypes.FLOAT:
            case JavaTypes.FLOAT_OBJ:
                setDouble(stmnt, idx, val.floatValue(), col);
                break;
            default:
                super.setBigDecimal(stmnt, idx, val, col);
        }
    }

    @Override
    protected void appendSelectRange(SQLBuffer buf, long start, long end,
        boolean subselect) {
        if (dbMajorVersion > 1) {
            if (start != 0)
                buf.append(" OFFSET ").appendValue(start);
            if (end != Long.MAX_VALUE)
                buf.append(" LIMIT ").appendValue(end - start);
            return;
        }
        // HSQL doesn't parameters in range
        buf.append(" LIMIT ").append(String.valueOf(start)).append(" ");
        if (end == Long.MAX_VALUE)
            buf.append(String.valueOf(0));
        else
            buf.append(String.valueOf(end - start));
    }

    @Override
    public void indexOf(SQLBuffer buf, FilterValue str, FilterValue find,
        FilterValue start) {
        buf.append("LOCATE(");
        find.appendTo(buf);
        buf.append(", ");
        str.appendTo(buf);
        if (start != null) {
            buf.append(", ");
            start.appendTo(buf);
        }
        buf.append(")");
    }

    @Override
    public String getPlaceholderValueString(Column col) {
        String type = getTypeName(col.getType());
        int idx = type.indexOf("{0}");
        if (idx != -1) {
            String pre = type.substring(0, idx);
            if (type.length() > idx + 3)
                type = pre + type.substring(idx + 3);
            else
                type = pre;
        }
        return "NULL AS " + type;
    }

    @Override
    public OpenJPAException newStoreException(String msg, SQLException[] causes,
        Object failed) {
        OpenJPAException ke = super.newStoreException(msg, causes, failed);
        if (ke instanceof ReferentialIntegrityException
            && causes[0].getErrorCode() == -violation_of_unique_index_or_constraint) {
            ((ReferentialIntegrityException) ke).setIntegrityViolation
                (ReferentialIntegrityException.IV_UNIQUE);
        }
        return ke;
    }
}
