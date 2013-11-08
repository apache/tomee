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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Table;

/**
 * Dictionary for Empress using ODBC server combined with their
 * type 2 driver. This dictionary may issues with other
 * driver/topology configurations.
 *  Empress does not allow multiple connections to read rows
 * read in a transaction, effectively forcing pessimistic transactions
 * regardless of the Optimistic setting. To allow users to use
 * optimistic transactions in a multi-connection evironment, you
 * must set the AllowConcurrentRead setting to true in addition
 * to standard options.
 *  Empress has the following additional limitations:
 * <ul>
 * <li>Foreign keys are quite limited in Empress and it is recommended
 * that these be created by hand.</li>
 * <li>Batching can be unreliable. Using BatchLimit=0 is strongly
 * recommended.</li>
 * <li>Using AllowConcurrentRead should be accompanied by
 * SimulateLocking=true</li>
 * <li>Connections should be rolled back on return to ensure locks
 * are released (see OpenJPA default DataSource documentation.</li>
 * <li>Certain outer joins requiring parameters in a subselect is not
 * supported by Empress and may under certain configurations cause
 * size () calls on query results and LRS fields to throw an exception.</li>
 * <li>Certain aggregate functions are not supported.</li>
 * </ul>
 */
public class EmpressDictionary
    extends DBDictionary {

    /**
     * This setting inserts "BYPASS" after every "SELECT". This
     * allows for multiple transactional reads of the same row
     * from different connections at the expense of loss of
     * pessimistic locking. Defaults to false.
     */
    public boolean allowConcurrentRead = false;

    public EmpressDictionary() {
        platform = "Empress";

        validationSQL = "SELECT DISTINCT today FROM sys_tables";
        joinSyntax = SYNTAX_TRADITIONAL;
        toUpperCaseFunction = "TOUPPER({0})";
        toLowerCaseFunction = "TOLOWER({0})";

        supportsDeferredConstraints = false;
        requiresAliasForSubselect = true;
        maxTableNameLength = 28;
        maxColumnNameLength = 28;
        maxIndexNameLength = 28;
        maxConstraintNameLength = 28;
        schemaCase = SCHEMA_CASE_PRESERVE;

        useGetBytesForBlobs = true;
        useSetBytesForBlobs = true;
        useGetStringForClobs = true;
        useSetStringForClobs = true;

        clobTypeName = "TEXT";
        blobTypeName = "BULK";
        realTypeName = "FLOAT(8)";
        bigintTypeName = "DECIMAL(38)";
        timestampTypeName = "DATE";
        varcharTypeName = "CHARACTER";
        tinyintTypeName = "DOUBLE PRECISION";
        doubleTypeName = "SMALLINT";
        bitTypeName = "SMALLINT";

        fixedSizeTypeNameSet.addAll(Arrays.asList(new String[]{
            "TEXT", "BULK", "LONGFLOAT", "INTEGER64", "SHORTINTEGER",
            "LONGINTEGER",
        }));
    }

    public boolean isSystemIndex(String name, Table table) {
        return name.toUpperCase().startsWith("SYS_");
    }

    public SQLBuffer toSelect(SQLBuffer selects, JDBCFetchConfiguration fetch,
        SQLBuffer from, SQLBuffer where, SQLBuffer group,
        SQLBuffer having, SQLBuffer order,
        boolean distinct, boolean forUpdate, long startIdx, long endIdx) {
        if (!allowConcurrentRead)
            return super.toSelect(selects, fetch, from, where, group,
                having, order, distinct, forUpdate, startIdx, endIdx);

        // override to allow a "BYPASS" to be inserted post-"select"
        // depending on allowConcurrentRead setting
        SQLBuffer buf = new SQLBuffer(this);
        buf.append("SELECT BYPASS ");
        if (distinct)
            buf.append("DISTINCT ");
        buf.append(selects).append(" FROM ").append(from);

        if (where != null && !where.isEmpty())
            buf.append(" WHERE ").append(where);
        if (group != null && !group.isEmpty())
            buf.append(" GROUP BY ").append(group);
        if (having != null && !having.isEmpty())
            buf.append(" HAVING ").append(having);
        if (order != null && !order.isEmpty())
            buf.append(" ORDER BY ").append(order);
        return buf;
    }

    public String[] getDropColumnSQL(Column column) {
        // empress wants dropped columns in the form: ALTER TABLE foo
        // DELETE columnToDrop
        return new String[]{ "ALTER TABLE "
            + getFullName(column.getTable(), false) + " DELETE " + 
            getColumnDBName(column) };
    }

    public void setFloat(PreparedStatement stmnt, int idx, float val,
        Column col)
        throws SQLException {
        // empress seems to allow INFINITY to be stored, but not retrieved,
        // which can prove to be difficult to handle
        if (val == Float.POSITIVE_INFINITY) {
            val = Float.MAX_VALUE;
            storageWarning(new Float(Float.POSITIVE_INFINITY),
                new Float(val));
        } else if (val == Float.NEGATIVE_INFINITY) {
            val = Float.MIN_VALUE + 1;
            storageWarning(new Float(Float.NEGATIVE_INFINITY),
                new Float(val));
        }
        super.setFloat(stmnt, idx, val, col);
    }

    public void setDouble(PreparedStatement stmnt, int idx, double val,
        Column col)
        throws SQLException {
        // empress seems to allow INFINITY to be stored, but not retrieved,
        // which can prove to be difficult to handle
        if (val == Double.POSITIVE_INFINITY) {
            val = Double.MAX_VALUE;
            storageWarning(new Double(Double.POSITIVE_INFINITY),
                new Double(val));
        } else if (val == Double.NEGATIVE_INFINITY) {
            val = Double.MIN_VALUE + 1;
            storageWarning(new Double(Double.NEGATIVE_INFINITY),
                new Double(val));
        }
        super.setDouble(stmnt, idx, val, col);
    }
}
