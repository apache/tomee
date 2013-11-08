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
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;
import org.apache.openjpa.jdbc.kernel.exps.FilterValue;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.schema.PrimaryKey;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.ReferenceHashSet;
import org.apache.openjpa.util.StoreException;
import org.apache.openjpa.util.UnsupportedException;

/**
 * Dictionary for Informix database. Notable features:
 * <ul>
 * <li>Informix does not allow pessimistic locking on scrollable result
 * sets.</li>
 * <li>SET LOCK MODE TO WAIT N statements are issued to wait on locks. See
 * {@link #lockWaitSeconds} and {@link #lockModeEnabled}.</li>
 * <li>LOCK MODE ROW is used by default for table creation to allow the
 * maximum concurrency.</li>
 * </ul>
 */
public class InformixDictionary
    extends DBDictionary {

    public static final String VENDOR_IBM = "ibm";

    /**
     * If true, then we will issue a "SET LOCK MODE TO WAIT N"
     * statement whenever we create a {@link Connection}, in order
     * allow waiting on locks.
     */
    public boolean lockModeEnabled = false;

    /**
     * If {@link #lockModeEnabled} is <code>true</code>, then this
     * parameter specifies the number of seconds we will wait to
     * obtain a lock for inserts and pessimistic locking.
     */
    public int lockWaitSeconds = 30;

    /**
     * Informix JDBC metadata for all known drivers returns with the
     * table catalog and the table schema name swapped. A <code>true</code>
     * value for this property indicates that they should be reversed.
     */
    public boolean swapSchemaAndCatalog = true;

    protected boolean useJCC = false;
    // weak set of connections we've already executed lock mode sql on
    private final Collection _seenConnections = new ReferenceHashSet
        (ReferenceHashSet.WEAK);

    private static final Localizer _loc = Localizer.forPackage
        (InformixDictionary.class);

    public InformixDictionary() {
        platform = "Informix";
        validationSQL = "SELECT FIRST 1 CURRENT TIMESTAMP "
            + "FROM informix.systables";

        supportsAutoAssign = true;
        autoAssignTypeName = "serial";
        lastGeneratedKeyQuery = "SELECT FIRST 1 DBINFO('sqlca.sqlerrd1') "
            + "FROM informix.systables";

        // informix actually does support deferred constraints, but not
        // in the table definition; deferred constraints can be activated by
        // invoking "set constraints all deferred" on the connection, which
        // we don't do yet
        supportsDeferredConstraints = false;
        constraintNameMode = CONS_NAME_AFTER;

        // informix supports "CLOB" type, but any attempt to insert
        // into them raises: "java.sql.SQLException: Can't convert fromnull"
        useGetStringForClobs = true;
        longVarcharTypeName = "TEXT";
        clobTypeName = "TEXT";
        smallintTypeName = "INT8";
        tinyintTypeName = "INT8";
        floatTypeName = "FLOAT";
        bitTypeName = "BOOLEAN";
        blobTypeName = "BYTE";
        doubleTypeName = "NUMERIC(32,20)";
        dateTypeName = "DATE";
        timeTypeName = "DATETIME HOUR TO SECOND";
        timestampTypeName = "DATETIME YEAR TO FRACTION(3)";
        doubleTypeName = "NUMERIC(32,20)";
        floatTypeName = "REAL";
        bigintTypeName = "NUMERIC(32,0)";
        doubleTypeName = "DOUBLE PRECISION";
        fixedSizeTypeNameSet.addAll(Arrays.asList(new String[]{
            "BYTE", "DOUBLE PRECISION", "INTERVAL", "SMALLFLOAT", "TEXT",
            "INT8",
        }));
        
        // OpenJPA-2045: NAME has been removed from common reserved words to
        // only specific dictionaries
        reservedWordSet.add("NAME");

        supportsLockingWithDistinctClause = false;
        supportsLockingWithMultipleTables = false;
        supportsLockingWithOrderClause = false;

        // the informix JDBC drivers have problems with using the
        // schema name in reflection on columns and tables
        supportsSchemaForGetColumns = false;
        supportsSchemaForGetTables = false;

        // Informix doesn't support aliases in deletes if the table has an index
        allowsAliasInBulkClause = false;
        
        // Informix doesn't understand "X CROSS JOIN Y", but it does understand
        // the equivalent "X JOIN Y ON 1 = 1"
        crossJoinClause = "JOIN";
        requiresConditionForCrossJoin = true;

        concatenateFunction = "CONCAT({0},{1})";
        nextSequenceQuery = "SELECT {0}.NEXTVAL FROM SYSTABLES WHERE TABID=1";
        supportsCorrelatedSubselect = false;
        swapSchemaAndCatalog = false;
        
        // Informix does not support foreign key delete action NULL or DEFAULT
        supportsNullDeleteAction = false;
        supportsDefaultDeleteAction = false;
        
        trimSchemaName = true;
    }

    @Override
    public void connectedConfiguration(Connection conn)
        throws SQLException {
        super.connectedConfiguration(conn);

        DatabaseMetaData meta = conn.getMetaData();
        String driverName = meta.getDriverName();
        if (driverName != null) {
            if (driverName.equals("IBM DB2 JDBC Universal Driver Architecture"))
            { 
                driverVendor = VENDOR_IBM;
                useJCC = true;
                setIdentifierCase(meta);
            } 
            else if (driverName.equals("IBM Informix JDBC Driver for IBM Informix Dynamic Server")) {
                setIdentifierCase(meta);
                driverVendor = VENDOR_IBM;
            }
            else if ("Informix".equalsIgnoreCase(driverName))
                driverVendor = VENDOR_DATADIRECT;
            else
                driverVendor = VENDOR_OTHER;
        } else
            driverVendor = VENDOR_OTHER;

        if (isJDBC3) {
            conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
            if (log.isTraceEnabled())
                log.trace(_loc.get("connection-defaults", new Object[]{
                    conn.getAutoCommit(), conn.getHoldability(),
                    conn.getTransactionIsolation()}));
        }
    }
    
    private void setIdentifierCase(DatabaseMetaData meta) {
        try {
            // lower case identifiers is the default for the JCC and newer
            // Informix JDBC drivers
            if (meta.storesLowerCaseIdentifiers()) { 
                schemaCase = SCHEMA_CASE_LOWER;
            }
            else if (meta.storesMixedCaseIdentifiers()) {
                schemaCase = SCHEMA_CASE_PRESERVE;
            }
            // otherwise, use the default (upper)
        }
        catch (SQLException e) {
            getLog().warn("cannot-determine-identifier-base-case");
            if (getLog().isTraceEnabled()) {
                getLog().trace(e.toString(), e);
            }
        }
    }

    @Override
    public Column[] getColumns(DatabaseMetaData meta, String catalog,
        String schemaName, String tableName, String columnName, Connection conn)
        throws SQLException {
        return getColumns(meta, DBIdentifier.newCatalog(catalog), 
            DBIdentifier.newSchema(schemaName),DBIdentifier.newTable(tableName),
            DBIdentifier.newColumn(columnName), conn);
    }

    @Override
    public Column[] getColumns(DatabaseMetaData meta, DBIdentifier catalog,
        DBIdentifier schemaName, DBIdentifier tableName, DBIdentifier columnName, Connection conn)
        throws SQLException {
        Column[] cols = super.getColumns(meta, catalog, schemaName, tableName,
            columnName, conn);

        // treat logvarchar as clob
        for (int i = 0; cols != null && i < cols.length; i++)
            if (cols[i].getType() == Types.LONGVARCHAR)
                cols[i].setType(Types.CLOB);
        return cols;
    }

    @Override
    public Column newColumn(ResultSet colMeta)
        throws SQLException {
        Column col = super.newColumn(colMeta);
        if (swapSchemaAndCatalog)
            col.setSchemaIdentifier(fromDBName(colMeta.getString("TABLE_CAT"), DBIdentifierType.CATALOG));
        return col;
    }

    @Override
    public PrimaryKey newPrimaryKey(ResultSet pkMeta)
        throws SQLException {
        PrimaryKey pk = super.newPrimaryKey(pkMeta);
        if (swapSchemaAndCatalog)
            pk.setSchemaIdentifier(fromDBName(pkMeta.getString("TABLE_CAT"), DBIdentifierType.CATALOG));
        return pk;
    }

    @Override
    public Index newIndex(ResultSet idxMeta)
        throws SQLException {
        Index idx = super.newIndex(idxMeta);
        if (swapSchemaAndCatalog)
            idx.setSchemaIdentifier(fromDBName(idxMeta.getString("TABLE_CAT"), DBIdentifierType.CATALOG));
        return idx;
    }

    @Override
    public void setBoolean(PreparedStatement stmnt, int idx, boolean val,
        Column col)
        throws SQLException {
        // informix actually requires that a boolean be set: it cannot
        // handle a numeric argument
        stmnt.setString(idx, val ? "t" : "f");
    }

    @Override
    public String[] getCreateTableSQL(Table table) {
        String[] create = super.getCreateTableSQL(table);
        create[0] = create[0] + " LOCK MODE ROW";
        return create;
    }

    @Override
    public String[] getAddPrimaryKeySQL(PrimaryKey pk) {
        String pksql = getPrimaryKeyConstraintSQL(pk);
        if (pksql == null)
            return new String[0];
        return new String[]{ "ALTER TABLE "
            + getFullName(pk.getTable(), false) + " ADD CONSTRAINT " + pksql };
    }

    @Override
    public String[] getAddForeignKeySQL(ForeignKey fk) {
        String fksql = getForeignKeyConstraintSQL(fk);
        if (fksql == null)
            return new String[0];
        return new String[]{ "ALTER TABLE "
            + getFullName(fk.getTable(), false) + " ADD CONSTRAINT " + fksql };
    }

    @Override
    public boolean supportsRandomAccessResultSet(Select sel,
        boolean forUpdate) {
        return !forUpdate && !sel.isLob()
            && super.supportsRandomAccessResultSet(sel, forUpdate);
    }

    @Override
    public Connection decorate(Connection conn)
        throws SQLException {
        conn = super.decorate(conn);
        if (isJDBC3 && conn.getHoldability() != 
            ResultSet.HOLD_CURSORS_OVER_COMMIT) {
            conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
            if (log.isTraceEnabled()) {                    
                log.trace(_loc.get("connection-defaults", new Object[]{
                    conn.getAutoCommit(), conn.getHoldability(),
                    conn.getTransactionIsolation()}));
            }
        }

        // if we haven't already done so, initialize the lock mode of the
        // connection
        if (_seenConnections.add(conn)) {
            if (lockModeEnabled) {
                String sql = "SET LOCK MODE TO WAIT";
                if (lockWaitSeconds > 0)
                    sql = sql + " " + lockWaitSeconds;
                execute(sql, conn, true);
            }
            String sql = "SET ENVIRONMENT RETAINUPDATELOCKS 'ALL'";
            execute(sql, conn, false);
        }

        // the datadirect driver requires that we issue a rollback before using
        // each connection
        if (VENDOR_DATADIRECT.equalsIgnoreCase(driverVendor))
            try {
                conn.rollback();
            } catch (SQLException se) {
            }
        return conn;
    }
    
    private void execute(String sql, Connection conn, boolean throwExc) {
        Statement stmnt = null;
        try {
            stmnt = conn.createStatement();
            stmnt.executeUpdate(sql);
        } catch (SQLException se) {
            if (throwExc)
                throw SQLExceptions.getStore(se, this);
            else {
                if (log.isTraceEnabled())
                    log.trace(_loc.get("can-not-execute", sql));
            }
        } finally {
            if (stmnt != null)
                try {
                    stmnt.close();
                } catch (SQLException se) {
                }
        }
    }

    @Override
    public void indexOf(SQLBuffer buf, FilterValue str, FilterValue find,
        FilterValue start) {
        throw new UnsupportedException(_loc.get("function-not-supported",
                getClass(), "LOCATE"));
    }

    @Override
    public boolean needsToCreateIndex(Index idx, Table table) {
       // Informix will automatically create a unique index for the 
       // primary key, so don't create another index again
       PrimaryKey pk = table.getPrimaryKey();
       if (pk != null && idx.columnsMatch(pk.getColumns()))
           return false;
       return true;
    }
    
    public boolean useJCC() {
        return useJCC;
    }
    
    /**
     * Return DB specific schemaCase 
     */
    @Override
    public String getSchemaCase(){
        return schemaCase;
    }
        
    @Override
    public boolean isFatalException(int subtype, SQLException ex) {
        
        // SQL State of IX000 is a general purpose Informix error code
        // category, so only return Boolean.TRUE if we match SQL Codes
        // recoverable = Boolean.FALSE;
        if ((subtype == StoreException.LOCK && checkNestedErrorCodes(ex, "IX000", -154)) 
          ||(subtype == StoreException.QUERY && ex.getErrorCode() == -213)) {
            return false;
        }
        
        return super.isFatalException(subtype, ex);
    }

    /**
     * Specialized matchErrorState method for Informix.  Informix exceptions are
     * typically nested multiple levels deep.  Correct determination of the exception type requires
     * inspection of nested exceptions to determine the root cause. A list of Informix (IDS v10) error codes
     * can be found here:
     * 
     * http://publib.boulder.ibm.com/infocenter/idshelp/v10/index.jsp?topic=/com.ibm.em.doc/errors_ids100.html
     * 
     * @param errorStates classification of SQL error states by their specific nature. The keys of the
     * map represent one of the constants defined in {@link StoreException}. The value corresponding to
     * a key represent the set of SQL Error States representing specific category of database error. 
     * This supplied map is sourced from <code>sql-error-state-codes.xml</xml> and filtered the
     * error states for the current database.
     * 
     * @param ex original SQL Exception as raised by the database driver.
     * 
     * @return A constant indicating the category of error as defined in {@link StoreException}.
     */
    protected int matchErrorState(Map<Integer,Set<String>> errorStates, SQLException ex) {
        // Informix SQLState IX000 is a general SQLState that applies to many possible conditions
        // If the underlying cause is also an IX000 with error code: 
        // -107 ISAM error: record is locked. || -154 ISAM error: Lock Timeout Expired.
        // the exception type is LOCK.
        if (checkNestedErrorCodes(ex, "IX000", -107, -154)) {
           return StoreException.LOCK;
        }
        return super.matchErrorState(errorStates, ex);
    }
    
    private boolean checkNestedErrorCodes(SQLException ex, String sqlState, int...errorCodes) {
        SQLException cause = ex;
        int level = 0;
        // Query at most 5 exceptions deep to prevent infinite iteration exception loops
        // Typically, the root exception is at level 3.
        while (cause != null && level < 5) {
            String errorState = cause.getSQLState();
            if (sqlState == null || sqlState.equals(errorState)) {
                for (int ec : errorCodes) {
                    if (cause.getErrorCode() == ec) {
                        return true;
                    }
                }
            }
            cause = cause.getNextException();
            level++;
        }
        return false;
    }
}

