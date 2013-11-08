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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.exps.FilterValue;
import org.apache.openjpa.jdbc.kernel.exps.Lit;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.schema.PrimaryKey;
import org.apache.openjpa.jdbc.schema.Schema;
import org.apache.openjpa.jdbc.schema.SchemaGroup;
import org.apache.openjpa.jdbc.schema.Sequence;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.schema.Unique;
import org.apache.openjpa.kernel.exps.Literal;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.StoreException;
import org.apache.openjpa.util.UserException;

/**
 * Dictionary for SolidDB database.
 */
public class SolidDBDictionary
    extends DBDictionary {

    /**
     * Sets whether tables are to be located in-memory or on disk.
     * Creating in-memory tables should append "STORE MEMORY" to the 
     * "CREATE TABLE" statement. Creating disk-based tables should 
     * append "STORE DISK". Since cursor hold over commit can not apply 
     * to M-tables (which will cause SOLID Table Error 13187: The cursor 
     * cannot continue accessing M-tables after the transaction has committed 
     * or aborted. The statement must be re-executed.), the default is 
     * STORE DISK.
     * The default concurrency control mechanism depends on the table type:
     *    Disk-based tables (D-tables) are by default optimistic.
     *    Main-memory tables (M-tables) are always pessimistic.
     * Since OpenJPA applications expects lock waits (as usually is done with 
     * normal pessimistic databases), the server should be set to the pessimistic mode. 
     * The optimistic mode is about not waiting for the locks at all. That increases 
     * concurrency but requires more programming. The pessimistic mode with the 
     * READ COMMITTED isolation level (default) should get as much concurrency as one 
     * might need. The pessimistic locking mode can be set in solid.ini:  
     *    [General]
     *        Pessimistic=yes
     *    
     * 
     */
    public boolean storeIsMemory = false;
    
    /**
     * If true, then simulate auto-assigned values in SolidDB by
     * using a trigger that inserts a sequence value into the
     * primary key value when a row is inserted.
     */
    public boolean useTriggersForAutoAssign = true;

    /**
     * The global sequence name to use for auto-assign simulation.
     */
    public String autoAssignSequenceName = null;

    /**
     * Flag to use OpenJPA 0.3 style naming for auto assign sequence name and
     * trigger name for backwards compatibility.
     */
    public boolean openjpa3GeneratedKeyNames = false;
    
    /**
     * Possible values for LockingMode are "PESSIMISTIC" and "OPTIMISTIC"
     */
    public String lockingMode = null;


    private static final Localizer _loc = Localizer.forPackage
        (SolidDBDictionary.class);

    public SolidDBDictionary() {
        platform = "SolidDB";
        bitTypeName = "TINYINT";
        blobTypeName = "LONG VARBINARY";
        booleanTypeName = "TINYINT";
        clobTypeName = "LONG VARCHAR";
        doubleTypeName = "DOUBLE PRECISION";
        
        allowsAliasInBulkClause = false;
        useGetStringForClobs = true;
        useSetStringForClobs = true;
        supportsDeferredConstraints = false;
        supportsNullUniqueColumn = false;
        
        concatenateFunction = "CONCAT({0},{1})";
        stringLengthFunction = "LENGTH({0})";
        trimLeadingFunction = "LTRIM({0})";
        trimTrailingFunction = "RTRIM({0})";
        trimBothFunction = "TRIM({0})";

        currentDateFunction = "CURDATE()";
        currentTimeFunction = "CURTIME()";
        currentTimestampFunction = "NOW()";
        lastGeneratedKeyQuery = "SELECT {0}.CURRENT";
        sequenceSQL = "SELECT SEQUENCE_SCHEMA, SEQUENCE_NAME FROM SYS_SEQUENCES";
        sequenceSchemaSQL = "SEQSCHEMA = ?";
        sequenceNameSQL = "SEQNAME = ?";
        
        reservedWordSet.addAll(Arrays.asList(new String[]{
            "BIGINT", "BINARY", "DATE", "TIME", 
            "TINYINT", "VARBINARY"
        }));
    }

    @Override
    public void endConfiguration() {
        super.endConfiguration();
        if (useTriggersForAutoAssign) {
            supportsAutoAssign = true;
        }
    }
    
    @Override
    public String[] getCreateTableSQL(Table table, SchemaGroup group) {
        StringBuilder buf = new StringBuilder();
        buf.append("CREATE TABLE ").append(getFullName(table, false)).append(" (");
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

        buf.append(") STORE ");
        if (storeIsMemory)
            buf.append("MEMORY");
        else
            buf.append("DISK");
        
        String[] create = null;
        if (lockingMode != null) {
            StringBuilder buf1 = new StringBuilder();
            if (lockingMode.equalsIgnoreCase("PESSIMISTIC")) { 
                buf1.append("ALTER TABLE ").append(getFullName(table, false)).
                    append(" SET PESSIMISTIC");
            } else if (lockingMode.equalsIgnoreCase("OPTIMISTIC")){
                buf1.append("ALTER TABLE ").append(getFullName(table, false)).
                    append(" SET OPTIMISTIC");
            } else 
                throw new UserException(_loc.get("invalid-locking-mode", lockingMode));
            
            create = new String[2];
            create[0] = buf.toString();
            create[1] = buf1.toString();
        } else {
            create = new String[1];
            create[0] = buf.toString();
        }
        
        if (!useTriggersForAutoAssign)
            return create;

        List seqs = null;
        String seq, trig;
        for (int i = 0; cols != null && i < cols.length; i++) {
            if (!cols[i].isAutoAssigned())
                continue;
            if (seqs == null)
                seqs = new ArrayList(4);

            seq = getAutoGenSeqName(cols[i]);
            if (sequenceExists(table.getSchemaIdentifier().getName(), seq, group))
                seqs.add("DROP SEQUENCE " + seq);
            seqs.add("CREATE SEQUENCE " + seq);
            
            if (openjpa3GeneratedKeyNames)
                trig = getOpenJPA3GeneratedKeyTriggerName(cols[i]);
            else
                trig = getGeneratedKeyTriggerName(cols[i]);

            // create the trigger that will insert new values into
            // the table whenever a row is created
            // CREATE TRIGGER TRIG01 ON table1 
            //     BEFORE INSERT 
            //     REFERENCING NEW COL1 AS NEW_COL1
            // BEGIN
            //     EXEC SEQUENCE seq1 NEXT INTO NEW_COL1;
            // END;

            seqs.add("CREATE TRIGGER " + trig
                + " ON " + toDBName(table.getIdentifier())
                + " BEFORE INSERT REFERENCING NEW " + toDBName(cols[i].getIdentifier())
                + " AS NEW_COL1 BEGIN EXEC SEQUENCE " + seq + " NEXT INTO NEW_COL1; END");
            
        }
        if (seqs == null)
            return create;

        // combine create table sql and create sequences sql
        String[] sql = new String[create.length + seqs.size()];
        System.arraycopy(create, 0, sql, 0, create.length);
        for (int i = 0; i < seqs.size(); i++)
            sql[create.length + i] = (String) seqs.get(i);
        return sql;
    }

    protected boolean sequenceExists(String schemaName, String seqName, SchemaGroup group) {
        Schema[] schemas = group.getSchemas();
        for (int i = 0; i < schemas.length; i++) {
            String dbSchemaName = schemas[i].getIdentifier().getName();
            if (schemaName != null && !schemaName.equalsIgnoreCase(dbSchemaName))
                continue;
                  
            Sequence[] seqs = schemas[i].getSequences();
            for (int j = 0; j < seqs.length; j++) {
                String dbSeqName = seqs[j].getName();
                if (dbSeqName != null && dbSeqName.equalsIgnoreCase(seqName))
                    return true;
            }
        }
        return false;
    }
    
    /**
     * Trigger name for simulating auto-assign values on the given column.
     */
    protected String getGeneratedKeyTriggerName(Column col) {
        // replace trailing _SEQ with _TRG
        String seqName = getGeneratedKeySequenceName(col);
        return seqName.substring(0, seqName.length() - 3) + "TRG";
    }

    /**
     * Returns a OpenJPA 3-compatible name for an auto-assign sequence.
     */
    protected String getOpenJPA3GeneratedKeySequenceName(Column col) {
        Table table = col.getTable();
        DBIdentifier sName = DBIdentifier.preCombine(table.getIdentifier(), "SEQ");
        return toDBName(getNamingUtil().makeIdentifierValid(sName, table.getSchema().
            getSchemaGroup(), maxTableNameLength, true));
    }

    /**
     * Returns a OpenJPA 3-compatible name for an auto-assign trigger.
     */
    protected String getOpenJPA3GeneratedKeyTriggerName(Column col) {
        Table table = col.getTable();        
        DBIdentifier sName = DBIdentifier.preCombine(table.getIdentifier(), "TRIG");
        return toDBName(getNamingUtil().makeIdentifierValid(sName, table.getSchema().
            getSchemaGroup(), maxTableNameLength, true));
    }

    protected String getAutoGenSeqName(Column col) {
        String seqName = autoAssignSequenceName;
        if (seqName == null) {
            if (openjpa3GeneratedKeyNames)
                seqName = getOpenJPA3GeneratedKeySequenceName(col);
            else
                seqName = getGeneratedKeySequenceName(col);
        }
        return seqName;
    }

    @Override
    protected String getGenKeySeqName(String query, Column col) {
        return MessageFormat.format(query, new Object[]{getAutoGenSeqName(col)});        
    }
    
    @Override
    public String convertSchemaCase(DBIdentifier objectName) {
        if (objectName != null && objectName.getName() == null)
            return "";
        return super.convertSchemaCase(objectName);
    }
    
    @Override
    public void substring(SQLBuffer buf, FilterValue str, FilterValue start,
            FilterValue length) {
        if (length != null) {
            super.substring(buf, str, start, length);
        } else {
            buf.append(substringFunctionName).append("(");
            str.appendTo(buf);
            buf.append(", ");
            if (start.getValue() instanceof Number) {
                buf.append(Long.toString(toLong(start)));
            } else {
                start.appendTo(buf);
            }
            buf.append(", ");
            if (start.getValue() instanceof Number) {
                long startLong = toLong(start);
                long endLong = Integer.MAX_VALUE; //2G
                buf.append(Long.toString(endLong - startLong));
            } else {
                buf.append(Integer.toString(Integer.MAX_VALUE));
                buf.append(" - (");
                start.appendTo(buf);
                buf.append(")");
            }
            buf.append(")");
        }
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
    public boolean isSystemIndex(DBIdentifier name, Table table) {
        // names starting with "$$" are reserved for SolidDB internal use
        String strName = DBIdentifier.isNull(name) ? null : name.getName();
        boolean startsWith$$ = false;
        if (strName != null) {
            startsWith$$ = name.isDelimited() ? strName.startsWith("\"$$") :
                strName.startsWith("$$");
        }
        return super.isSystemIndex(name, table) || startsWith$$; 
    }

    @Override
    public boolean isSystemSequence(DBIdentifier name, DBIdentifier schema,
            boolean targetSchema) {
        if (super.isSystemSequence(name, schema, targetSchema))
            return true;
        String schemaName = DBIdentifier.isNull(schema) ? null : schema.getName();
        boolean startsWith_SYSTEM = schema.isDelimited() ? schemaName.startsWith("\"_SYSTEM") :
            schemaName.startsWith("_SYSTEM");
        
        String seqName = DBIdentifier.isNull(name) ? null : name.getName();
        boolean startsWithSYS_SEQ_ = name.isDelimited() ? seqName.startsWith("\"SYS_SEQ_") :
            seqName.startsWith("SYS_SEQ_");
        if (startsWith_SYSTEM && startsWithSYS_SEQ_)
            return true;
        return false;
    }

    @Override
    public void setBigDecimal(PreparedStatement stmnt, int idx, BigDecimal val,
            Column col) throws SQLException {
        int type = (val == null || col == null) ? JavaTypes.BIGDECIMAL
                : col.getJavaType();
        switch (type) {
        case JavaTypes.DOUBLE:
        case JavaTypes.DOUBLE_OBJ:
            setDouble(stmnt, idx, val.doubleValue(), col);
            break;
        case JavaTypes.FLOAT:
        case JavaTypes.FLOAT_OBJ:
            setFloat(stmnt, idx, val.floatValue(), col);
            break;
        case JavaTypes.LONG:
        case JavaTypes.LONG_OBJ:
            setLong(stmnt, idx, val.longValue(), col);
            break;
        default:
            super.setBigDecimal(stmnt, idx, val, col);
        }
    }

    @Override
    public void setDouble(PreparedStatement stmnt, int idx, double val,
            Column col) throws SQLException {
        int type = (col == null) ? JavaTypes.DOUBLE
                : col.getJavaType();
        switch (type) {
        case JavaTypes.DOUBLE:
        case JavaTypes.DOUBLE_OBJ:
            super.setDouble(stmnt, idx, val, col);
            break;
        case JavaTypes.FLOAT:
        case JavaTypes.FLOAT_OBJ:
            setFloat(stmnt, idx, Double.valueOf(val).floatValue(), col);
            break;
        case JavaTypes.LONG:
        case JavaTypes.LONG_OBJ:
            setLong(stmnt, idx, Double.valueOf(val).longValue(), col);
            break;
        }
    }
    
    @Override
    public boolean needsToCreateIndex(Index idx, Table table, Unique[] uniques) {
       // SolidDB will automatically create a unique index for the 
       // constraint, so don't create another index again
       PrimaryKey pk = table.getPrimaryKey();
       if (pk != null && idx.columnsMatch(pk.getColumns()))
           return false;
       
       // If table1 has constraints on column (a, b), an explicit index on (a)
       // will cause duplicate index error from SolidDB
       Column[] icols = idx.getColumns();
       boolean isDuplicate = false;
       boolean mayBeDuplicate = false;
       for (int i = 0; i < uniques.length; i++) {
           Column[] ucols = uniques[i].getColumns();
           if (ucols.length < icols.length)
               continue;
           for (int j = 0, k = 0; j < ucols.length && k < icols.length; j++, k++) {
               if (mayBeDuplicate && ucols[j].getQualifiedPath().equals(icols[k].getQualifiedPath())) {
                   if (k == icols.length - 1) {
                       isDuplicate = true;
                   } else {
                       mayBeDuplicate = true;
                   }
               } else
                   mayBeDuplicate = false;
           }
           if (isDuplicate)
               break;
       }
       return isDuplicate;
    }

    @Override
    protected String getSequencesSQL(String schemaName, String sequenceName) {
        return getSequencesSQL(DBIdentifier.newSchema(schemaName), 
            DBIdentifier.newSequence(sequenceName));
    }
    
    @Override
    protected String getSequencesSQL(DBIdentifier schemaName, DBIdentifier sequenceName) {
        StringBuilder buf = new StringBuilder();
        buf.append(sequenceSQL);
        if (!DBIdentifier.isNull(schemaName) || !DBIdentifier.isNull(sequenceName))
            buf.append(" WHERE ");
        if (!DBIdentifier.isNull(schemaName)) {
            buf.append(sequenceSchemaSQL);
            if (!DBIdentifier.isNull(sequenceName))
                buf.append(" AND ");
        }
        if (!DBIdentifier.isNull(sequenceName))
            buf.append(sequenceNameSQL);
        return buf.toString();
    }

    @Override
    protected void appendSelect(SQLBuffer selectSQL, Object alias, Select sel,
            int idx) {
        // if this is a literal value, add a cast...
        Object val = sel.getSelects().get(idx);
        boolean toCast = (val instanceof Lit) && 
        ((Lit)val).getParseType() != Literal.TYPE_DATE && 
        ((Lit)val).getParseType() != Literal.TYPE_TIME &&
        ((Lit)val).getParseType() != Literal.TYPE_TIMESTAMP;

        if (toCast) 
            selectSQL.append("CAST(");

            // ... and add the select per super's behavior...
        super.appendSelect(selectSQL, alias, sel, idx);

        // ... and finish the cast
        if (toCast) {
            Class c = ((Lit) val).getType();
            int javaTypeCode = JavaTypes.getTypeCode(c);
            int jdbcTypeCode = getJDBCType(javaTypeCode, false);
            String typeName = getTypeName(jdbcTypeCode);
            selectSQL.append(" AS " + typeName);

            // if the literal is a string, use the default char col size
            // in the cast statement.
            if (String.class.equals(c))
                selectSQL.append("(" + characterColumnSize + ")");

            selectSQL.append(")");
        }
    }

    /**
     * Solid does not support deferred referential integrity checking.
     */
    @Override
    protected ForeignKey newForeignKey(ResultSet fkMeta)
    throws SQLException {
        ForeignKey fk = super.newForeignKey(fkMeta);
        fk.setDeferred(false);
        return fk;
    }

    @Override
    public boolean isFatalException(int subtype, SQLException ex) {
        String errorState = ex.getSQLState();
        int errorCode = ex.getErrorCode();
        if (subtype == StoreException.LOCK && errorCode == 14529 && "HY000".equals(errorState))
            return false;
        return super.isFatalException(subtype, ex);
    }
}
