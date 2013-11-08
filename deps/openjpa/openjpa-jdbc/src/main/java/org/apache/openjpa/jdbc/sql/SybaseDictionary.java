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

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;
import org.apache.openjpa.jdbc.kernel.exps.FilterValue;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.schema.PrimaryKey;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.schema.Unique;
import org.apache.openjpa.lib.jdbc.DelegatingConnection;
import org.apache.openjpa.lib.util.ConcreteClassGenerator;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.StoreException;

/**
 * Dictionary for Sybase.
 *  The main point of interest is that by default, every table
 * that is created will have a unique column named "UNQ_INDEX" of
 * the "IDENTITY" type. OpenJPA will not ever utilize this column. However,
 * due to internal Sybase restrictions, this column is required
 * in order to support pessimistic (datastore) locking, since Sybase
 * requires that any tables in a "SELECT ... FOR UPDATE" clause have
 * a unique index that is <strong>not</strong> included in the list
 * of columns, as described in the
 * <a href="http://www.sybase.com/detail/1,6904,1023075,00.html"
 * >Sybase documentation</a>. This behavior can be surpressed by setting the
 * dictionary property <code>CreateIdentityColumn=false</code>. The
 * name of the unique column can be changed by setting the property
 * <code>IdentityColumnName=COLUMN_NAME</code>.
 *  A good Sybase type reference is can be found <a
 * href="http://www.ispirer.com/doc/sqlways36/sybase/syb_dtypes.html">here</a>.
 */
public class SybaseDictionary
    extends AbstractSQLServerDictionary {

    private static Localizer _loc = Localizer.forPackage
        (SybaseDictionary.class);

    private static Constructor<SybaseConnection> sybaseConnectionImpl;
    
    public static String RIGHT_TRUNCATION_ON_SQL = "set string_rtruncation on";
    public static String NUMERIC_TRUNCATION_OFF_SQL = "set arithabort numeric_truncation off";
    
    static {
        try {
            sybaseConnectionImpl = ConcreteClassGenerator.getConcreteConstructor(SybaseConnection.class, 
                    Connection.class);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * If true, then whenever the <code>schematool</code> creates a
     * table, it will append an additional IDENTITY column to the
     * table's creation SQL. This is so Sybase will be able to
     * perform <code>SELECT...FOR UPDATE</code> statements.
     */
    public boolean createIdentityColumn = true;

    /**
     * If {@link #createIdentityColumn} is true, then the
     * <code>identityColumnName</code> will be the name of the
     * additional unique column that will be created.
     */
    public String identityColumnName = "UNQ_INDEX";

    /**
     * If true, Sybase will ignore numeric truncation on insert or
     * update operations.  Otherwise, the operation will fail. The default
     * value, false is in accordance with SQL92.
     */
    public boolean ignoreNumericTruncation = false;
    
    public SybaseDictionary() {
        platform = "Sybase";
        schemaCase = SCHEMA_CASE_PRESERVE;
        forUpdateClause = "FOR UPDATE AT ISOLATION SERIALIZABLE";

        supportsLockingWithDistinctClause = false;
        supportsNullTableForGetColumns = false;
        requiresAliasForSubselect = true;
        requiresAutoCommitForMetaData = true;

        maxTableNameLength = 30;
        maxColumnNameLength = 30;
        maxIndexNameLength = 30;
        maxConstraintNameLength = 30;

        bigintTypeName = "NUMERIC(38)";
        bitTypeName = "TINYINT";

        // Sybase doesn't understand "X CROSS JOIN Y", but it does understand
        // the equivalent "X JOIN Y ON 1 = 1"
        crossJoinClause = "JOIN";
        requiresConditionForCrossJoin = true;

        // these tables should not be reflected on
        systemTableSet.addAll(Arrays.asList(new String[]{
            "IJDBC_FUNCTION_ESCAPES", "JDBC_FUNCTION_ESCAPES",
            "SPT_IJDBC_CONVERSION", "SPT_IJDBC_MDA", "SPT_IJDBC_TABLE_TYPES",
            "SPT_JDBC_CONVERSION", "SPT_JDBC_TABLE_TYPES", "SPT_JTEXT",
            "SPT_LIMIT_TYPES", "SPT_MDA", "SPT_MONITOR", "SPT_VALUES",
            "SYBLICENSESLOG",
        }));

        // reserved words specified at:
        // http://manuals.sybase.com/onlinebooks/group-as/asg1250e/refman/@Generic__BookTextView/26603
        reservedWordSet.addAll(Arrays.asList(new String[]{
            "ARITH_OVERFLOW", "BREAK", "BROWSE", "BULK", "CHAR_CONVERT",
            "CHECKPOINT", "CLUSTERED", "COMPUTE", "CONFIRM", "CONTROLROW",
            "DATABASE", "DBCC", "DETERMINISTIC", "DISK DISTINCT", "DUMMY", 
            "DUMP", "ENDTRAN", "ERRLVL", "ERRORDATA", "ERROREXIT", "EXCLUSIVE",
            "EXIT", "EXP_ROW_SIZE", "FILLFACTOR", "FUNC", "FUNCTION",
            "HOLDLOCK", "IDENTITY_GAP", "IDENTITY_INSERT", "IDENTITY_START",
            "IF", "INDEX", "INOUT", "INSTALL", "INTERSECT", "JAR", "KILL",
            "LINENO", "LOAD", "LOCK", "MAX_ROWS_PER_PAGE", "MIRROR",
            "MIRROREXIT", "MODIFY", "NEW", "NOHOLDLOCK", "NONCLUSTERED",
            "NUMERIC_TRUNCATION", "OFF", "OFFSETS", "ONCE", "ONLINE", "OUT",
            "OVER", "PARTITION", "PERM", "PERMANENT", "PLAN", "PRINT", "PROC",
            "PROCESSEXIT", "PROXY_TABLE", "QUIESCE", "RAISERROR", "READ",
            "READPAST", "READTEXT", "RECONFIGURE", "REFERENCES REMOVE", "REORG",
            "REPLACE", "REPLICATION", "RESERVEPAGEGAP", "RETURN", "RETURNS",
            "ROLE", "ROWCOUNT", "RULE", "SAVE", "SETUSER", "SHARED",
            "SHUTDOWN", "SOME", "STATISTICS", "STRINGSIZE", "STRIPE",
            "SYB_IDENTITY", "SYB_RESTREE", "SYB_TERMINATE", "TEMP", "TEXTSIZE",
            "TRAN", "TRIGGER", "TRUNCATE", "TSEQUAL", "UNPARTITION", "USE",
            "USER_OPTION", "WAITFOR", "WHILE", "WRITETEXT",
        }));
        
        // Sybase does not allow reserved words to be used as column names. 
        invalidColumnWordSet.addAll(reservedWordSet);

        // Sybase does not support foreign key delete/update action NULL,
        // DEFAULT, CASCADE
        supportsNullDeleteAction = false;
        supportsDefaultDeleteAction = false;
        supportsCascadeDeleteAction = false;
        supportsNullUpdateAction = false;
        supportsDefaultUpdateAction = false;
        supportsCascadeUpdateAction = false;
        
        fixedSizeTypeNameSet.remove("NUMERIC");
    }

    @Override
    public int getJDBCType(int metaTypeCode, boolean lob) {
        switch (metaTypeCode) {
            // the default mapping for BYTE is a TINYINT, but Sybase's TINYINT
            // type can't handle the complete range for a Java byte
            case JavaTypes.BYTE:
            case JavaTypes.BYTE_OBJ:
                return getPreferredType(Types.SMALLINT);
            default:
                return super.getJDBCType(metaTypeCode, lob);
        }
    }

    @Override
    public void setBigInteger(PreparedStatement stmnt, int idx, BigInteger val,
        Column col)
        throws SQLException {
        // setBigDecimal doesn't work here: in one case, a stored value
        // of 7799438514924349440 turns into 7799438514924349400
        // setObject gets around this in the Sybase JDBC drivers
        setObject(stmnt, idx, new BigDecimal(val), Types.BIGINT, col);
    }

    @Override
    public String[] getCreateTableSQL(Table table) {
        if (!createIdentityColumn)
            return super.getCreateTableSQL(table);

        StringBuilder buf = new StringBuilder();
        buf.append("CREATE TABLE ").append(getFullName(table, false)).
            append(" (");

        Column[] cols = table.getColumns();
        
        boolean hasIdentity = false;

        for (int i = 0; i < cols.length; i++) {
            // can only have one identity column
            if (cols[i].isAutoAssigned()) {
                hasIdentity = true;
            }

            // The column may exist if dropping and recreating a table.
            if(cols[i].getIdentifier().getName().equals(identityColumnName)) {
                hasIdentity=true;
                // column type may be lost when recreating - reset to NUMERIC
                if(cols[i].getType() != Types.NUMERIC) { // should check if compatible 
                    cols[i].setType(Types.NUMERIC);
                }
            }
            
            buf.append(i == 0 ? "" : ", ");
            buf.append(getDeclareColumnSQL(cols[i], false));
        }

        // add an identity column if we do not already have one
        if (!hasIdentity)
            buf.append(", ").append(identityColumnName).
                append(" NUMERIC IDENTITY UNIQUE");

        PrimaryKey pk = table.getPrimaryKey();
        if (pk != null)
            buf.append(", ").append(getPrimaryKeyConstraintSQL(pk));

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
    protected String getDeclareColumnSQL(Column col, boolean alter) {
        StringBuilder buf = new StringBuilder();
        buf.append(getColumnDBName(col)).append(" ");
        buf.append(getTypeName(col));

        // can't add constraints to a column we're adding after table
        // creation, cause some data might already be inserted
        if (!alter) {
            if (col.getDefaultString() != null && !col.isAutoAssigned())
                buf.append(" DEFAULT ").append(col.getDefaultString());
            if (col.isAutoAssigned())
                buf.append(" IDENTITY");
        }

        if (col.isNotNull())
            buf.append(" NOT NULL");
        else if (!col.isPrimaryKey()) {
            // sybase forces you to explicitly specify that
            // you will allow NULL values
            buf.append(" NULL");
        }

        return buf.toString();
    }

    @Override
    public String[] getDropColumnSQL(Column column) {
        // Sybase uses "ALTER TABLE DROP <COLUMN_NAME>" rather than the
        // usual "ALTER TABLE DROP COLUMN <COLUMN_NAME>"
        return new String[]{ "ALTER TABLE "
            + getFullName(column.getTable(), false) + " DROP " + getColumnDBName(column) };
    }

    @Override
    public void refSchemaComponents(Table table) {
        // note that we use getColumns() rather than getting the column by name
        // because under some circumstances this method is called under the
        // dynamic schema factory, where getting a column by name creates
        // that column
        Column[] cols = table.getColumns();
        for (int i = 0; i < cols.length; i++)
            if (identityColumnName.equalsIgnoreCase(cols[i].getIdentifier().getName()))
                cols[i].ref();
    }

    @Override
    public void endConfiguration() {
        super.endConfiguration();

        // warn about jdbc compliant flag
        String url = conf.getConnectionURL();
        if (!StringUtils.isEmpty(url)
            && url.toLowerCase().indexOf("jdbc:sybase:tds") != -1
            && url.toLowerCase().indexOf("be_as_jdbc_compliant_as_possible=")
            == -1) {
            log.warn(_loc.get("sybase-compliance", url));
        }
    }

    @Override
    public Connection decorate(Connection conn)
        throws SQLException {
        conn = super.decorate(conn);
        Connection savedConn = conn;
        
//        if(ignoreConnectionSetup) { 
//            if(conn instanceof DelegatingConnection) { 
//                conn = ((DelegatingConnection)conn).getInnermostDelegate();
//            }
//        }
        
        // In order for Sybase to raise the truncation exception when the 
        // string length is greater than the column length for Char, VarChar, 
        // Binary, VarBinary, the "set string_rtruncation on" must be executed. 
        // This setting is effective for the duration of current connection.
        if (setStringRightTruncationOn) {
            PreparedStatement stmnt = prepareStatement(conn, RIGHT_TRUNCATION_ON_SQL);        
            stmnt.execute();
            stmnt.close();
        }
        
        // By default, Sybase will fail to insert or update if a numeric
        // truncation occurs as a result of, for example, loss of decimal
        // precision.  This setting specifies that the operation should not 
        // fail if a numeric truncation occurs.
        if (ignoreNumericTruncation) {
            PreparedStatement stmnt = prepareStatement(conn, NUMERIC_TRUNCATION_OFF_SQL);        
            stmnt.execute();
            stmnt.close();            
        }
        
        
        return ConcreteClassGenerator.newInstance(sybaseConnectionImpl, savedConn);
    }
    
    /**
     * Helper method obtains a string value from a given column in a ResultSet. Strings provided are column names,
     * jdbcName will be tried first if an SQLException occurs we'll try the sybase name.
     */
    protected String getStringFromResultSet(ResultSet rs, String jdbcName, String sybaseName) throws SQLException {
        try { 
            return rs.getString(jdbcName);
        }
        catch(SQLException sqle) { 
            // if the generic JDBC identifier isn't found an SQLException will be thrown
            // try the Sybase specific id
            return rs.getString(sybaseName);
        }
    }
    /**
     * Helper method obtains a boolean value from a given column in a ResultSet. Strings provided are column names,
     * jdbcName will be tried first if an SQLException occurs we'll try the sybase name.
     */
    protected boolean getBooleanFromResultSet(ResultSet rs, String jdbcName, String sybaseName) throws SQLException {
        try { 
            return rs.getBoolean(jdbcName);
        }
        catch(SQLException sqle) {
            // if the generic JDBC identifier isn't found an SQLException will be thrown
            // try the Sybase specific id
            return rs.getBoolean(sybaseName);
        }
    }

    /**
     * Create a new primary key from the information in the schema metadata.
     */
    protected PrimaryKey newPrimaryKey(ResultSet pkMeta)
        throws SQLException {
        PrimaryKey pk = new PrimaryKey();
        pk.setSchemaIdentifier(fromDBName(getStringFromResultSet(pkMeta, "TABLE_SCHEM", "table_owner"),
            DBIdentifierType.SCHEMA));
        pk.setTableIdentifier(fromDBName(getStringFromResultSet(pkMeta, "TABLE_NAME", "table_name"),
            DBIdentifierType.TABLE));
        pk.setColumnIdentifier(fromDBName(getStringFromResultSet(pkMeta, "COLUMN_NAME", "column_name"),
            DBIdentifierType.COLUMN));
        pk.setIdentifier(fromDBName(getStringFromResultSet(pkMeta, "PK_NAME", "index_name"),
            DBIdentifierType.CONSTRAINT));
        return pk;
    }

    /**
     * Create a new index from the information in the index metadata.
     */
    protected Index newIndex(ResultSet idxMeta)
        throws SQLException {
        Index idx = new Index();
        idx.setSchemaIdentifier(fromDBName(getStringFromResultSet(idxMeta, "TABLE_SCHEM", "table_owner"),
            DBIdentifierType.SCHEMA));
        idx.setTableIdentifier(fromDBName(getStringFromResultSet(idxMeta, "TABLE_NAME", "table_name"),
            DBIdentifierType.TABLE));
        idx.setColumnIdentifier(fromDBName(getStringFromResultSet(idxMeta, "COLUMN_NAME", "column_name"),
            DBIdentifierType.COLUMN));
        idx.setIdentifier(fromDBName(getStringFromResultSet(idxMeta, "INDEX_NAME", "index_name"),
            DBIdentifierType.INDEX));
        idx.setUnique(!getBooleanFromResultSet(idxMeta, "NON_UNIQUE", "non_unique"));
        return idx;
    }
    
    public boolean isFatalException(int subtype, SQLException ex) {
        if (subtype == StoreException.LOCK) {
            SQLException next = ex.getNextException();
            if("JZ0TO".equals(next.getSQLState())) {
                return false; // query timeout
            }
        }
        return super.isFatalException(subtype, ex); 
    }

    /**
     * Connection wrapper to cache the {@link Connection#getCatalog} result,
     * which takes a very long time with the Sybase Connection (and
     * which we frequently invoke).
     */
    protected abstract static class SybaseConnection
        extends DelegatingConnection {

        private String _catalog = null;

        public SybaseConnection(Connection conn) {
            super(conn);
        }

        public String getCatalog()
            throws SQLException {
            if (_catalog == null)
                _catalog = super.getCatalog();
            return _catalog;
        }

        public void setAutoCommit(boolean autocommit)
            throws SQLException {
            // the sybase jdbc driver demands that the Connection always
            // be rolled back before autocommit status changes. Failure to
            // do so will yield "SET CHAINED command not allowed within
            // multi-statement transaction." exceptions
            try {
                super.setAutoCommit(autocommit);
            } catch (SQLException e) {
                // failed for some reason: try rolling back and then
                // setting autocommit again.
                if (autocommit)
                    super.commit();
                else
                    super.rollback();
                super.setAutoCommit(autocommit);
            }
        }
    }
    
    @Override
    public String getIsNullSQL(String colAlias, int colType)  {
        switch(colType) {
            case Types.BLOB:
            case Types.CLOB:
                return String.format("datalength(%s) = 0", colAlias);
        }
        return super.getIsNullSQL(colAlias, colType);
    }
    
    @Override
    public String getIsNotNullSQL(String colAlias, int colType) { 
        switch(colType) { 
            case Types.BLOB: 
            case Types.CLOB:
                return String.format("datalength(%s) != 0", colAlias);
        }
        return super.getIsNotNullSQL(colAlias, colType);
    }

    @Override
    public String getIdentityColumnName() {
        return identityColumnName;       
    }

    public void indexOf(SQLBuffer buf, FilterValue str, FilterValue find,
        FilterValue start) {
        buf.append("(CHARINDEX(");
        find.appendTo(buf);
        buf.append(", ");
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
