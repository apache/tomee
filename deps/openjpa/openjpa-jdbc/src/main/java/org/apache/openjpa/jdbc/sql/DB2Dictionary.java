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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Date;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.kernel.exps.FilterValue;
import org.apache.openjpa.jdbc.kernel.exps.Lit;
import org.apache.openjpa.jdbc.kernel.exps.Param;
import org.apache.openjpa.jdbc.kernel.exps.Val;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.schema.Schema;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.MixedLockLevels;
import org.apache.openjpa.kernel.exps.Literal;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.StoreException;
import org.apache.openjpa.util.UnsupportedException;
import org.apache.openjpa.util.UserException;

import serp.util.Strings;

/**
 * Dictionary for IBM DB2 database.
 */
public class DB2Dictionary
    extends AbstractDB2Dictionary {
    private static final Localizer _loc = Localizer.forPackage
        (DB2Dictionary.class);

    public static final String VENDOR_IBM = "ibm";
    public String optimizeClause = "optimize for";
    public String rowClause = "row";
    protected int db2ServerType = 0;
    public static final int db2ISeriesV5R3OrEarlier = 1;
    public static final int db2UDBV81OrEarlier = 2;
    public static final int db2ZOSV8xOrLater = 3;
    public static final int db2UDBV82OrLater = 4;
    public static final int db2ISeriesV5R4OrLater = 5;
    protected static final String forUpdate = "FOR UPDATE";
    protected static final String withURClause = "WITH UR";
    protected static final String withCSClause = "WITH CS";
    protected static final String withRSClause = "WITH RS";
    protected static final String withRRClause = "WITH RR";
    protected static final String useKeepShareLockClause     = "USE AND KEEP SHARE LOCKS";
    protected static final String useKeepUpdateLockClause    = "USE AND KEEP UPDATE LOCKS";
    protected static final String useKeepExclusiveLockClause = "USE AND KEEP EXCLUSIVE LOCKS";
    protected static final String forReadOnlyClause = "FOR READ ONLY";
    protected static final String defaultSequenceSQL 
        = "SELECT SEQSCHEMA AS SEQUENCE_SCHEMA, SEQNAME AS SEQUENCE_NAME FROM SYSCAT.SEQUENCES";
    static final String SYSDUMMY = "SYSIBM.SYSDUMMY1";

    
    private int defaultBatchLimit = 100;
    public boolean appendExtendedExceptionText = true;
    
    /**
     * Affirms whether this dictionary uses {@code ROWNUM} feature.
     * {@code ROWNUM} feature is used to construct {@code SQL SELECT} query 
     * that uses an offset or limits the number of resultant rows.
     * <br>
     * By default, this flag is set to {@code false}.
     */
    public boolean supportsRowNum = false;
    
    public DB2Dictionary() {
        platform = "DB2";
        validationSQL = "SELECT DISTINCT(CURRENT TIMESTAMP) FROM SYSIBM.SYSTABLES";
        supportsSelectEndIndex = true;

        nextSequenceQuery = "VALUES NEXTVAL FOR {0}";

        sequenceSQL = defaultSequenceSQL;
        sequenceSchemaSQL = "SEQSCHEMA = ?";
        sequenceNameSQL = "SEQNAME = ?";
        characterColumnSize = 254;

        binaryTypeName = "BLOB(1M)";
        longVarbinaryTypeName = "BLOB(1M)";
        varbinaryTypeName = "BLOB(1M)";
        clobTypeName = "CLOB(1M)";
        longVarcharTypeName = "LONG VARCHAR";
        datePrecision = MICRO;
        storeCharsAsNumbers = false;

        fixedSizeTypeNameSet.addAll(Arrays.asList(new String[]{
            "LONG VARCHAR FOR BIT DATA", "LONG VARCHAR", "LONG VARGRAPHIC",
        }));
        systemSchemas = "SYSCAT,SYSIBM,SYSSTAT,SYSIBMADM,SYSTOOLS";
        maxConstraintNameLength = 18;
        maxIndexNameLength = 128;
        maxColumnNameLength = 30;
        supportsDeferredConstraints = false;
        supportsDefaultDeleteAction = false;
        supportsAlterTableWithDropColumn = false;
        supportsLockingWithOrderClause = true;
        supportsNullUniqueColumn = false;

        supportsNullTableForGetColumns = false;
        requiresCastForMathFunctions = true;
        requiresCastForComparisons = true;

        reservedWordSet.addAll(Arrays.asList(new String[]{
            "AFTER", "ALIAS", "ALLOW", "APPLICATION", "ASSOCIATE", "ASUTIME",
            "AUDIT", "AUX", "AUXILIARY", "BEFORE", "BINARY", "BUFFERPOOL",
            "CACHE", "CALL", "CALLED", "CAPTURE", "CARDINALITY", "CCSID",
            "CLUSTER", "COLLECTION", "COLLID", "COMMENT", "CONCAT",
            "CONDITION", "CONTAINS", "COUNT_BIG", "CURRENT_LC_CTYPE",
            "CURRENT_PATH", "CURRENT_SERVER", "CURRENT_TIMEZONE", "CYCLE",
            "DATABASE", "DAYS", "DB2GENERAL", "DB2GENRL", "DB2SQL",
            "DBINFO", "DEFAULTS", "DEFINITION", "DETERMINISTIC", "DISALLOW",
            "DO", "DSNHATTR", "DSSIZE", "DYNAMIC", "EACH", "EDITPROC", "ELSEIF",
            "ENCODING", "END-EXEC1", "ERASE", "EXCLUDING", "EXIT", "FENCED",
            "FIELDPROC", "FILE", "FINAL", "FREE", "FUNCTION", "GENERAL",
            "GENERATED", "GRAPHIC", "HANDLER", "HOLD", "HOURS", "IF",
            "INCLUDING", "INCREMENT", "INDEX", "INHERIT", "INOUT", "INTEGRITY",
            "ISOBID", "ITERATE", "JAR", "JAVA", "LABEL", "LC_CTYPE", "LEAVE",
            "LINKTYPE", "LOCALE", "LOCATOR", "LOCATORS", "LOCK", "LOCKMAX",
            "LOCKSIZE", "LONG", "LOOP", "MAXVALUE", "MICROSECOND",
            "MICROSECONDS", "MINUTES", "MINVALUE", "MODE", "MODIFIES", "MONTHS",
            "NEW", "NEW_TABLE", "NOCACHE", "NOCYCLE", "NODENAME", "NODENUMBER",
            "NOMAXVALUE", "NOMINVALUE", "NOORDER", "NULLS", "NUMPARTS", "OBID",
            "OLD", "OLD_TABLE", "OPTIMIZATION", "OPTIMIZE", "OUT", "OVERRIDING",
            "PACKAGE", "PARAMETER", "PART", "PARTITION", "PATH", "PIECESIZE",
            "PLAN", "PRIQTY", "PROGRAM", "PSID", "QUERYNO", "READS", "RECOVERY",
            "REFERENCING", "RELEASE", "RENAME", "REPEAT", "RESET", "RESIGNAL",
            "RESTART", "RESULT", "RESULT_SET_LOCATOR", "RETURN", "RETURNS",
            "ROUTINE", "ROW", "RRN", "RUN", "SAVEPOINT", "SCRATCHPAD",
            "SECONDS", "SECQTY", "SECURITY", "SENSITIVE", "SIGNAL", "SIMPLE",
            "SOURCE", "SPECIFIC", "SQLID", "STANDARD", "START", "STATIC",
            "STAY", "STOGROUP", "STORES", "STYLE", "SUBPAGES", "SYNONYM",
            "SYSFUN", "SYSIBM", "SYSPROC", "SYSTEM", "TABLESPACE", "TRIGGER",
            "UNDO", "UNTIL", "VALIDPROC", "VARIABLE", "VARIANT", "VCAT",
            "VOLUMES", "WHILE", "WLM", "YEARS",
        }));
        
        // reservedWordSet subset that CANNOT be used as valid column names
        // (i.e., without surrounding them with double-quotes)
        invalidColumnWordSet.addAll(Arrays.asList(new String[] {
            "CONSTRAINT", "END-EXEC", "END-EXEC1", 
        }));


        super.setBatchLimit(defaultBatchLimit);
        
        selectWordSet.add("WITH");
    }

    public boolean supportsRandomAccessResultSet(Select sel,
        boolean forUpdate) {
        return !forUpdate
            && super.supportsRandomAccessResultSet(sel, forUpdate);
    }

    protected void appendSelectRange(SQLBuffer buf, long start, long end,
        boolean subselect) {
        // appends the literal range string, since DB2 is unable to handle
        // a bound parameter for it
        // do not generate FETCH FIRST clause for subselect
        if (!subselect)
            buf.append(" FETCH FIRST ").append(Long.toString(end)).
                append(" ROWS ONLY");
    }

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
            Class<?> c = ((Lit) val).getType();
            int javaTypeCode = JavaTypes.getTypeCode(c);
            int jdbcTypeCode = getJDBCType(javaTypeCode, false);
            String typeName = getTypeName(jdbcTypeCode);
            selectSQL.append(" AS " + typeName);

            // if the literal is a string, use the default char col size
            // in the cast statement.
            if (String.class.equals(c))
                selectSQL.append("(" + getCastStringColumnSize(val) + ")");

            selectSQL.append(")");
        }
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

    public Connection decorate(Connection conn)
        throws SQLException {
        // some versions of the DB2 driver seem to default to
        // READ_UNCOMMITTED, which will prevent locking from working
        // (multiple SELECT ... FOR UPDATE statements are allowed on
        // the same instance); if we have not overridden the
        // transaction isolation in the configuration, default to
        // TRANSACTION_READ_COMMITTED
        conn = super.decorate(conn);

        if (conf.getTransactionIsolationConstant() == -1 &&
                conn.getTransactionIsolation() <
                Connection.TRANSACTION_READ_COMMITTED)
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

        return conn;
    }

    public void connectedConfiguration(Connection conn) throws SQLException {
        super.connectedConfiguration(conn);

        DatabaseMetaData metaData = conn.getMetaData();

        String driverName = metaData.getDriverName();
        if (driverName != null && driverName.startsWith("IBM DB2"))
            driverVendor = VENDOR_IBM;
        else
            driverVendor = VENDOR_OTHER;

        
        // Determine the type of DB2 database
        // First check for AS/400
        getProductVersionMajorMinorForISeries();

        if (versionLaterThan(0)) {
            if (isDB2ISeriesV5R3OrEarlier())
                db2ServerType = db2ISeriesV5R3OrEarlier;
            else if (isDB2ISeriesV5R4OrLater())
                db2ServerType = db2ISeriesV5R4OrLater;
        }
        
        if (db2ServerType == 0) {
            if (isJDBC3) {
                setMajorVersion(metaData.getDatabaseMajorVersion());
                setMinorVersion(metaData.getDatabaseMinorVersion());
            }
            else
                getProductVersionMajorMinor();

            // Determine the type of DB2 database for ZOS & UDB
            if (isDB2UDBV81OrEarlier())
                db2ServerType = db2UDBV81OrEarlier;
            else if (isDB2ZOSV8xOrLater())
                db2ServerType = db2ZOSV8xOrLater;
            else if (isDB2UDBV82OrLater())
                db2ServerType = db2UDBV82OrLater;
        }

        // verify that database product is supported
        if (db2ServerType == 0 || getMajorVersion() < 0)
            throw new UnsupportedException(_loc.get("db-not-supported",
                new Object[] {databaseProductName, databaseProductVersion }));
        if (versionEqualOrLaterThan(9, 2)) {
            supportsLockingWithMultipleTables = true;
            supportsLockingWithInnerJoin = true;
            supportsLockingWithOuterJoin = true;
            forUpdateClause = "WITH RR USE AND KEEP UPDATE LOCKS";
            
            supportsXMLColumn = versionEqualOrLaterThan(9, 0);
            
        }

        // platform specific settings
        switch (db2ServerType) {
        case db2UDBV82OrLater:
            lastGeneratedKeyQuery = "SELECT IDENTITY_VAL_LOCAL() FROM " + SYSDUMMY;
            break;
        case  db2ZOSV8xOrLater:
            // DB2 Z/OS 
            characterColumnSize = 255;
            lastGeneratedKeyQuery = "SELECT IDENTITY_VAL_LOCAL() FROM " + SYSDUMMY;
            nextSequenceQuery = "SELECT NEXTVAL FOR {0} FROM " + SYSDUMMY;
            // allow users to set a non default sequenceSQL. 
            if (defaultSequenceSQL.equals(sequenceSQL)){            	
	            sequenceSQL = "SELECT SCHEMA AS SEQUENCE_SCHEMA, "
	                + "NAME AS SEQUENCE_NAME FROM SYSIBM.SYSSEQUENCES";
	            
                if (log.isTraceEnabled())
                    log.trace(_loc.get("sequencesql-override", new Object[] {defaultSequenceSQL, sequenceSQL}));
            }
            sequenceSchemaSQL = "SCHEMA = ?";
            sequenceNameSQL = "NAME = ?";
            if (getMajorVersion() == 8) {
                // DB2 Z/OS Version 8: no bigint support, hence map Java
                // long to decimal
                bigintTypeName = "DECIMAL(31,0)";
            }
            break;
        case db2ISeriesV5R3OrEarlier:
        case db2ISeriesV5R4OrLater:
            lastGeneratedKeyQuery = "SELECT IDENTITY_VAL_LOCAL() FROM " + SYSDUMMY;
            nextSequenceQuery = "SELECT NEXTVAL FOR {0} FROM " + SYSDUMMY;
            validationSQL = "SELECT DISTINCT(CURRENT TIMESTAMP) FROM "
                + "QSYS2.SYSTABLES";
            // allow users to set a non default sequenceSQL.
            if (defaultSequenceSQL.equals(sequenceSQL)){            
	            sequenceSQL = "SELECT SEQUENCE_SCHEMA, "
	                + "SEQUENCE_NAME FROM QSYS2.SYSSEQUENCES";
	            
                if (log.isTraceEnabled())
                    log.trace(_loc.get("sequencesql-override", new Object[] {defaultSequenceSQL, sequenceSQL}));
            }
            sequenceSchemaSQL = "SEQUENCE_SCHEMA = ?";
            sequenceNameSQL = "SEQUENCE_NAME = ?";
            // V5R4 and earlier systems do not support retrieval of generated keys
            if (isDB2ISeriesV5R4OrEarlier()) {
            	supportsGetGeneratedKeys = false;
            }
            
            break;
        }
    }
    

    public boolean supportsIsolationForUpdate() {
        return true;
    }

    /**
     * Get the update clause for the query based on the
     * isolationLevel hints if it is for update.
     * It also handles the UR hint when it is not for update.
     */
    protected String getForUpdateClause(JDBCFetchConfiguration fetch,
        boolean isForUpdate, Select sel) {
        int isolationLevel;
        // For db2UDBV81OrEarlier and db2ISeriesV5R3OrEarlier:
        // "optimize for" clause appears before "for update" clause.
        StringBuffer forUpdateString = new StringBuffer(getOptimizeClause(sel));
        // Determine the isolationLevel; the fetch
        // configuration data overrides the persistence.xml value
        if (fetch != null && fetch.getIsolation() != -1)
            isolationLevel = fetch.getIsolation();
        else
            isolationLevel = conf.getTransactionIsolationConstant();

        if (fetch != null && fetch.getReadLockLevel() >= 
            MixedLockLevels.LOCK_PESSIMISTIC_WRITE)
            isolationLevel = Connection.TRANSACTION_SERIALIZABLE;

        if (isForUpdate) {
            switch (db2ServerType) {
            case db2ISeriesV5R3OrEarlier:
            case db2UDBV81OrEarlier:
                if (isolationLevel == Connection.TRANSACTION_SERIALIZABLE)
                    forUpdateString.append(" ").append(forUpdateClause);
                else
                    forUpdateString.append(" ").append(forUpdate).append(" ")
                        .append(withRSClause);
                break;
            case db2ZOSV8xOrLater:
            case db2UDBV82OrLater:
                if (isolationLevel == Connection.TRANSACTION_SERIALIZABLE) {
                    forUpdateString.append(" ").append(forReadOnlyClause)
                        .append(" ").append(withRRClause).append(" ").append(
                            useKeepUpdateLockClause);
                } else {
                    forUpdateString.append(" ").append(forReadOnlyClause)
                        .append(" ").append(withRSClause).append(" ").append(
                            useKeepUpdateLockClause);
                }
                break;
            case db2ISeriesV5R4OrLater:
                if (isolationLevel == Connection.TRANSACTION_SERIALIZABLE) {
                    forUpdateString.append(" ").append(forReadOnlyClause)
                        .append(" ").append(withRRClause).append(" ").append(
                            useKeepExclusiveLockClause);
                } else {
                    forUpdateString.append(" ").append(forReadOnlyClause)
                        .append(" ").append(withRSClause).append(" ").append(
                            useKeepExclusiveLockClause);
                }
                break;
            }
        } else {
        	if ( fetch != null && fetch.getIsolation() == Connection.TRANSACTION_READ_UNCOMMITTED 
        			&& sel.getParent() == null) { // i.e. not a subquery
	            forUpdateString.append(" ").append(forReadOnlyClause)
	            .append(" ").append(withURClause);
	    	}
        }
   
        return forUpdateString.toString();
    }

    public boolean isDB2UDBV82OrLater() {
        return (databaseProductVersion.indexOf("SQL") != -1
             || databaseProductName.indexOf("DB2/") != -1)
             && versionEqualOrLaterThan(8, 2);
    }

    public boolean isDB2ZOSV8xOrLater() {
       return (databaseProductVersion.indexOf("DSN") != -1
            || databaseProductName.indexOf("DB2/") == -1)
            && versionLaterThan(7);
    }

    public boolean isDB2ISeriesV5R3OrEarlier() {
       return databaseProductName.indexOf("AS") != -1
           && versionEqualOrEarlierThan(5, 3); 
    }

    public boolean isDB2ISeriesV5R4OrLater() {
       return databaseProductName.indexOf("AS") != -1
           && versionEqualOrLaterThan(5, 4);
    }

    public boolean isDB2ISeriesV5R4OrEarlier() {
        return databaseProductName.indexOf("AS") != -1
            && versionEqualOrEarlierThan(5, 4);
     }

    public boolean isDB2UDBV81OrEarlier() {
        return (databaseProductVersion.indexOf("SQL") != -1 
            || databaseProductName.indexOf("DB2/") != -1) 
            && versionEqualOrEarlierThan(8,1);
    }

    /** Get the version Major/Minor for the ISeries
     */
    private void getProductVersionMajorMinorForISeries() {
        // ISeries    DBProdName                 DB2 UDB for AS/400
        //   (Toolbox)DBProdVersion              05.04.0000 V5R4m0
        // ISeries                               DB2 UDB for AS/400
        //   (Native)                            V5R4M0
        // new jcc    DBProdVersion              QSQ05040 or QSQ06010
        if (databaseProductName.indexOf("AS") != -1) {
            // default to V5R4
            setMajorVersion(5);
            setMinorVersion(4);
            int index = databaseProductVersion.indexOf('V');
            if (index != -1) {
                String s = databaseProductVersion.substring(index);
                s = s.toUpperCase();

                StringTokenizer stringtokenizer = new StringTokenizer(s, "VRM"
                    , false);
                if (stringtokenizer.countTokens() == 3) {
                    String s1 = stringtokenizer.nextToken();
                    setMajorVersion(Integer.parseInt(s1));
                    String s2 =  stringtokenizer.nextToken();
                    setMinorVersion(Integer.parseInt(s2));
                }
            } else {
                index = databaseProductVersion.indexOf('0');
                if (index != -1) {
                    String s = databaseProductVersion.substring(index);
                    s = s.toUpperCase();

                    StringTokenizer stringtokenizer = new StringTokenizer(s, "0"
                        , false);                    
                    if (stringtokenizer.countTokens() == 2) {
                        String s1 = stringtokenizer.nextToken();
                        setMajorVersion(Integer.parseInt(s1));
                        String s2 =  stringtokenizer.nextToken();
                        setMinorVersion(Integer.parseInt(s2));
                    }
                }
            }
        }
    }
    
    private void getProductVersionMajorMinor() {
        // In case JDBC driver version is lower than 3
        // use following info to determine Major and Minor 
        //                        CLI    vs      JCC
        // ZDBV8 DBProdName       DB2            DB2
        //       DBProdVersion    08.01.0005     DSN08015
        // ZDBV9                  DB2            DB2
        //                        09.01.0005     DSN09015
        // WinV9                  DB2/NT         DB2/NT
        //                        09.01.0000     SQL09010
        // SolarisV9                             DB2/SUN64
        //                                       SQL0901
        // Linux                  DB2/LINUX      DB2/LINUX
        //                        09.01.0000     SQL0901
        if (databaseProductVersion.indexOf("09") != -1) {
            setMajorVersion(9);
            if (databaseProductVersion.indexOf("01") != -1) {
                setMinorVersion(1);
            }
        } else if (databaseProductVersion.indexOf("08") != -1) {
        	setMajorVersion(8);
        	setMinorVersion(2);
            if (databaseProductVersion.indexOf("01") != -1) {
            	setMinorVersion(1);
            }
        }
    }

    protected String getOptimizeClause(Select sel) {
        if (sel != null && sel.getExpectedResultCount() > 0) {
            StringBuilder buf = new StringBuilder();
            buf.append(" ").append(optimizeClause).append(" ")
                .append(String.valueOf(sel.getExpectedResultCount()))
                .append(" ").append(rowClause);
            return buf.toString();
        }

        return "";
    }

    public OpenJPAException newStoreException(String msg, SQLException[] causes, Object failed) {
        if (appendExtendedExceptionText == true && causes != null && causes.length > 0) {
            msg = appendExtendedExceptionMsg(msg, causes[0]);
        }
        return super.newStoreException(msg, causes, failed);
    }

    /**
     *  Append exception information from SQLCA to the existing
     *  exception message
     */
    private String appendExtendedExceptionMsg(String msg, SQLException sqle){
       final String GETSQLCA ="getSqlca";
       try {
            Method sqlcaM2 = sqle.getNextException().getClass()
                             .getMethod(GETSQLCA,null);
            Object sqlca = sqlcaM2.invoke(sqle.getNextException(),
                                          new Object[] {});
            Method  getSqlErrpMethd = sqlca.getClass().
            getMethod("getSqlErrp", null);
            Method  getSqlWarnMethd = sqlca.getClass().
            getMethod("getSqlWarn", null);
            Method  getSqlErrdMethd = sqlca.getClass().
            getMethod("getSqlErrd", null);
            StringBuilder errdStr = new StringBuilder();

            int[] errds = (int[]) getSqlErrdMethd.invoke(sqlca, new Object[]{});
            for (int i = 0; i < errds.length; i++)
                errdStr.append(errdStr.length() > 0 ? ", " : "").
                    append(errds[i]);
            StringBuilder exceptionMsg = new StringBuilder();
            exceptionMsg.append("SQLCA OUTPUT");
            exceptionMsg.append("[Errp=");
            exceptionMsg.append(getSqlErrpMethd.invoke(sqlca, new Object[]{}));
            exceptionMsg.append(", Errd=");
            exceptionMsg.append(errdStr);

            String Warn = new String((char[]) getSqlWarnMethd.
                    invoke(sqlca, new Object[]{}));
            if (Warn.trim().length() != 0) {
                exceptionMsg.append(", Warn=");
                exceptionMsg.append(Warn);
                exceptionMsg.append("]");
            } else {
                exceptionMsg.append("]");
            }
            msg = msg.concat(exceptionMsg.toString());
            
            // for batched execution failures, SQLExceptions are nested
            SQLException sqle2 = sqle.getNextException();
            while (sqle2 != null) {                
                msg = msg.concat("\n" + sqle2.getMessage());
                sqle2 = sqle2.getNextException();
            }
            
            return msg;
        } catch (Throwable t) {
            return sqle.getMessage();
        }
    }

    public int getDb2ServerType() {
        return db2ServerType;
    }
    
    protected void appendLength(SQLBuffer buf, int type) {
        if (type == Types.VARCHAR)
            buf.append("(").append(Integer.toString(characterColumnSize)).
                append(")");
    }

    /**
     * If this dictionary supports XML type,
     * use this method to append xml predicate.
     * 
     * @param buf the SQL buffer to write the comparison
     * @param op the comparison operation to perform
     * @param lhs the left hand side of the comparison
     * @param rhs the right hand side of the comparison
     * @param lhsxml indicates whether the left operand maps to xml
     * @param rhsxml indicates whether the right operand maps to xml
     */
    public void appendXmlComparison(SQLBuffer buf, String op, FilterValue lhs,
        FilterValue rhs, boolean lhsxml, boolean rhsxml) {
        super.appendXmlComparison(buf, op, lhs, rhs, lhsxml, rhsxml);
        if (lhsxml && rhsxml)
            appendXmlComparison2(buf, op, lhs, rhs);
        else if (lhsxml)
            appendXmlComparison1(buf, op, lhs, rhs);
        else 
            appendXmlComparison1(buf, op, rhs, lhs);
    }

    /**
     * Append an xml comparison predicate.
     *
     * @param buf the SQL buffer to write the comparison
     * @param op the comparison operation to perform
     * @param lhs the left hand side of the comparison (maps to xml column)
     * @param rhs the right hand side of the comparison
     */
    private void appendXmlComparison1(SQLBuffer buf, String op, 
            FilterValue lhs, FilterValue rhs) {
        boolean castrhs = false;
        Class<?> rc = Filters.wrap(rhs.getType());
        int type = 0;
        if (rhs.isConstant()) {
            type = getJDBCType(JavaTypes.getTypeCode(rc), false);
            castrhs = true;
        }
        
        appendXmlExists(buf, lhs);

        buf.append(" ").append(op).append(" ");
        
        buf.append("$");
        if (castrhs)
            buf.append("Parm");
        else
            rhs.appendTo(buf);
        
        buf.append("]' PASSING ");
        appendXmlVar(buf, lhs);
        buf.append(", ");
        
        if (castrhs)
            appendCast(buf, rhs, type);
        else
            rhs.appendTo(buf);
        
        buf.append(" AS \"");
        if (castrhs)
            buf.append("Parm");
        else
            rhs.appendTo(buf);
        buf.append("\")");
    }
    
    /**
     * Append an xml comparison predicate. (both operands map to xml column)
     *
     * @param buf the SQL buffer to write the comparison
     * @param op the comparison operation to perform
     * @param lhs the left hand side of the comparison (maps to xml column)
     * @param rhs the right hand side of the comparison (maps to xml column)
     */
    private void appendXmlComparison2(SQLBuffer buf, String op, 
            FilterValue lhs, FilterValue rhs) {
        appendXmlExists(buf, lhs);
        
        buf.append(" ").append(op).append(" ");
        
        buf.append("$").append(rhs.getColumnAlias(
            rhs.getFieldMapping().getColumns()[0])).
            append("/*/");
        rhs.appendTo(buf);
        
        buf.append("]' PASSING ");
        appendXmlVar(buf, lhs);
        buf.append(", ");
        appendXmlVar(buf, rhs);
        buf.append(")");
    }
    
    private void appendXmlVar(SQLBuffer buf, FilterValue val) {
        buf.append(val.getColumnAlias(
            val.getFieldMapping().getColumns()[0])).
            append(" AS ").
            append("\"").append(val.getColumnAlias(
            val.getFieldMapping().getColumns()[0])).
            append("\"");        
    }
    
    private void appendXmlExists(SQLBuffer buf, FilterValue val) {
        buf.append("XMLEXISTS('");
        buf.append("$").append(val.getColumnAlias(
            val.getFieldMapping().getColumns()[0])).
            append("/*[");
        val.appendTo(buf);        
    }
    
    /**
     * add CAST for a scalar function where operand is a param
     * 
     * @param func original string
     * @param target substring to look for
     * @param asString 
     * @return updated string (func)
     */
    private String addCastAsString(String func, String target, 
            String asString) {
        String fstring = func;
        if (func.indexOf(target) != -1)
            fstring = Strings.replace(
                func, target, "CAST(" + target + asString + ")");
        return fstring;
    }

    /**
     * add CAST for a function operator where operand is a param
     * 
     * @param func function name
     * @param val type
     * @return updated string (func)
     */
    public String addCastAsType(String func, Val val) {
        String fstring = null;
        String type = getTypeName(getJDBCType(JavaTypes.getTypeCode(val
            .getType()), false));
        if (String.class.equals(val.getType()))
            type = type + "(" + getCastStringColumnSize(val) + ")";
        fstring = "CAST(? AS " + type + ")";
        return fstring;
    }

    /**
     * Return the batch limit. If the batchLimit is -1, change it to 100 for
     * best performance
     */
    public int getBatchLimit() {
        int limit = super.getBatchLimit();
        if (limit == UNLIMITED) {
            limit = defaultBatchLimit;
            if (log.isTraceEnabled())
                log.trace(_loc.get("batch_unlimit", String.valueOf(limit)));
        }
        return limit;
    }

    /**
     * Return the correct CAST function syntax
     * 
     * @param val operand of cast
     * @param func original string
     * @return a String with the correct CAST function syntax
     */
    public String getCastFunction(Val val, String func) {
        if (val instanceof Lit || val instanceof Param) {
            if (func.indexOf("VARCHAR") == -1) {
                func = addCastAsString(func, "{0}", " AS VARCHAR(" + varcharCastLength + ")");
            }
        }
        return func;
    }

    /**
     * Return the correct CAST function syntax
     * 
     * @param val operand of cast
     * @param func original string
     * @param col database column
     * @return a String with the correct CAST function syntax
     */
    public String getCastFunction(Val val, String func, Column col) {
        boolean doCast = false;
        if (val instanceof Lit || val instanceof Param) {
            doCast = true;
        }
        // cast anything not already a VARCHAR to VARCHAR
        if (col.getType() != Types.VARCHAR) {
            doCast = true;
        }
        if (doCast == true) {
            if (func.indexOf("VARCHAR") == -1) {
                func = addCastAsString(func, "{0}", " AS VARCHAR(" + varcharCastLength + ")");
            }
        }
        return func;
    }

    public void indexOf(SQLBuffer buf, FilterValue str, FilterValue find,
            FilterValue start) {
        if (find.getValue() != null) { // non constants
            buf.append("LOCATE(CAST((");
            find.appendTo(buf);
            buf.append(") AS VARCHAR(1000)), ");
        } else {
            // this is a constant
            buf.append("LOCATE(");
            find.appendTo(buf);
            buf.append(", ");
        }
        if (str.getValue() != null) {
            buf.append("CAST((");
            str.appendTo(buf);
            buf.append(") AS VARCHAR(1000))");
        } else {
            str.appendTo(buf);
        }
        if (start != null) {
            if (start.getValue() != null) {
                buf.append(", CAST((");
                start.appendTo(buf);
                buf.append(") AS INTEGER)");
            } else {
                buf.append(", ");
                start.appendTo(buf);
            }
        }
        buf.append(")");
    }
    
    /** 
     * Cast the specified value to the specified type.
     *
     * @param buf the buffer to append the cast to
     * @param val the value to cast
     * @param type the type of the case, e.g. {@link Types#NUMERIC}
     */
    public void appendCast(SQLBuffer buf, FilterValue val, int type) {

        // Convert the cast function: "CAST({0} AS {1})"
        int firstParam = castFunction.indexOf("{0}");
        String pre = castFunction.substring(0, firstParam); // "CAST("
        String mid = castFunction.substring(firstParam + 3);
        int secondParam = mid.indexOf("{1}");
        String post;
        if (secondParam > -1) {
            post = mid.substring(secondParam + 3); // ")"
            mid = mid.substring(0, secondParam); // " AS "
        } else
            post = "";

        // No need to add CAST if the value is a constant
        if (val instanceof Lit || val instanceof Param) {
            buf.append(pre);
            val.appendTo(buf);
            buf.append(mid);
            buf.append(getTypeName(type));
            appendLength(buf, type);
            buf.append(post);
        } else {
            val.appendTo(buf);
            String sqlString = buf.getSQL(false);
            if (sqlString.endsWith("?")) {
                // case "(?" - convert to "CAST(? AS type"
                String typeName = getTypeName(type);
                if (String.class.equals(val.getType()))
                    typeName = typeName + "(" + getCastStringColumnSize(val) + ")";
                String str = "CAST(? AS " + typeName + ")";
                buf.replaceSqlString(sqlString.length() - 1,
                        sqlString.length(), str);
            }
        }
    }

    /**
     * Create an index if necessary for some database tables
     */
    public void createIndexIfNecessary(Schema schema, String table,
        Column pkColumn) {
        createIndexIfNecessary(schema, DBIdentifier.newTable(table), 
            pkColumn);
    }

    public void createIndexIfNecessary(Schema schema, DBIdentifier table,
            Column pkColumn) {
        if (db2ServerType == db2ZOSV8xOrLater) {
            // build the index for the sequence tables
            // the index name will be the fully qualified table name + _IDX
            Table tab = schema.getTable(table);
            DBIdentifier fullIdxId = tab.getFullIdentifier().clone();
            DBIdentifier unQualifiedName = DBIdentifier.append(fullIdxId.getUnqualifiedName(), "IDX");
            fullIdxId.setName(getValidIndexName(unQualifiedName, tab));
            Index idx = tab.addIndex(fullIdxId);
            idx.setUnique(true);
            idx.addColumn(pkColumn);
        }
    }
    
    @Override
    public boolean isFatalException(int subtype, SQLException ex) {
        String errorState = ex.getSQLState();
        int errorCode = ex.getErrorCode();
        if (errorCode == -952 && "57014".equals(errorState))
            return false;
        /*
         * Check if this Exception was generated by a lock timeout expiration.
         * The following criteria are used to determine this:
         * 
         * DB2 LUW Infocenter: SQLSTATE=57033 with reason code "80" indicates
         * the statement failed due to timeout. DB2 for z/OS Stored Procedures:
         * Through the CALL and Beyond, page 188: An ErrorCode of -913 with
         * SQLERR 00C9008E means a timeout has occurred.
         */
        if (subtype == StoreException.LOCK && "57033".equals(errorState)
            && ((ex.getMessage().indexOf("80") != -1) 
                || (errorCode == -913 && ex.getMessage().contains("00C9008E")))) {
            return false;
        } 
        if ((subtype == StoreException.QUERY && "57014".equals(errorState) &&
            (errorCode == -952 || errorCode == -905))) {
            return false;
        }
        return super.isFatalException(subtype, ex);
    }
    
    @Override
    protected void setDelimitedCase(DatabaseMetaData metaData) {
        delimitedCase = SCHEMA_CASE_PRESERVE;
    }
    
    /**
     * The Type 2 JDBC Driver may throw an SQLException when provided a non-
     * zero timeout if we're connected to Z/OS. The SQLException should be
     * logged but not thrown.
     */
    @Override
    public void setQueryTimeout(PreparedStatement stmnt, int timeout)
        throws SQLException {
        if (db2ServerType == db2ZOSV8xOrLater) { 
            try { 
                super.setQueryTimeout(stmnt, timeout);
            }
            catch (SQLException e) {
                if (log.isTraceEnabled()) {
                    log.trace(_loc.get("error-setting-query-timeout", timeout,
                        e.getMessage()), e);
                }
            }
        }
        else { 
            super.setQueryTimeout(stmnt, timeout);
        }
    }

    /**
     * Set the given value as a parameter to the statement.
     */
    public void setBytes(PreparedStatement stmnt, int idx, byte[] val,
        Column col)
        throws SQLException {
        // for DB2, if the column was defined as CHAR for BIT DATA, then
        // we want to use the setBytes in stead of the setBinaryStream
        if (useSetBytesForBlobs 
                || (!DBIdentifier.isNull(col.getTypeIdentifier()) && 
                col.getTypeIdentifier().getName().contains("BIT DATA"))) {
            stmnt.setBytes(idx, val);
        } else {
            setBinaryStream(stmnt, idx, new ByteArrayInputStream(val), val.length, col);
        }
    }

    /**
     * Convert the specified column of the SQL ResultSet to the proper
     * java type.
     */
    public byte[] getBytes(ResultSet rs, int column)
        throws SQLException {
        if (useGetBytesForBlobs) {
            return rs.getBytes(column);
        }
        if (useGetObjectForBlobs) {
            return (byte[]) rs.getObject(column);
        }

        // At this point we don't have any idea if the DB2 column was defined as
        //     a blob or if it was defined as CHAR for BIT DATA.
        // First try as a blob, if that doesn't work, then try as CHAR for BIT DATA
        // If that doesn't work, then go ahead and throw the first exception
        try {
            Blob blob = getBlob(rs, column);
            if (blob == null) {
                return null;
            }
            
            int length = (int) blob.length();
            if (length == 0) {
                return null;
            }
            
            return blob.getBytes(1, length);
        }
        catch (SQLException e) {
            try {
                return rs.getBytes(column);
            }
            catch (SQLException e2) {
                throw e;                
            }
        }
    }

    private int getCastStringColumnSize(Object val) {
        int colSize = characterColumnSize;
        if (val instanceof Lit) {
            String literal = (String) ((Lit) val).getValue();
            if (literal != null) {
                int literalLen = literal.length();
                if (literalLen > characterColumnSize) {
                    colSize = literalLen;
                }
            }
        }
        return colSize;
    }

    @Override
    public void insertBlobForStreamingLoad(Row row, Column col, 
            JDBCStore store, Object ob, Select sel) throws SQLException {
        if (ob != null) {
            row.setBinaryStream(col, (InputStream)ob, -1);
        } else {
            row.setNull(col);
        }
    }

    @Override
    public void insertClobForStreamingLoad(Row row, Column col, Object ob)
    throws SQLException {
        if (ob != null) {
            row.setCharacterStream(col, (Reader)ob, -1);
        } else {
            row.setNull(col);
        }
    }

    @Override
    public void updateBlob(Select sel, JDBCStore store, InputStream is)
        throws SQLException {
        //NO-OP
    }

    @Override
    public void updateClob(Select sel, JDBCStore store, Reader reader)
        throws SQLException {
        //NO-OP
    }

    /**
     * Set the given date value as a parameter to the statement.
     */
    public void setDate(PreparedStatement stmnt, int idx, Date val, Column col)
        throws SQLException {
        // When column metadata is not available, DB2 on z/OS does not like the value produced
        // by the default dictionary - java.util.Date is converted to java.sql.Timestamp.
        if (db2ServerType == db2ZOSV8xOrLater) {
            if (col == null && val != null && "java.util.Date".equals(val.getClass().getName())) {
                setDate(stmnt, idx, new java.sql.Date(val.getTime()), null, col);
                return;
            }
        }
        super.setDate(stmnt, idx, val, col);
    }

    public int getDB2MajorVersion() {
        return getMajorVersion();
    }

    public int getDB2MinorVersion() {
        return getMinorVersion();
    }
    
    public String getDefaultSchemaName()  {
        if (defaultSchemaName == null) {
            Connection conn = null;
            Statement stmnt = null;
            ResultSet rs = null;
            try {
                String str = "SELECT CURRENT SCHEMA FROM " + SYSDUMMY;
                conn = getConnection(); 
                if (conn != null) {
                    stmnt = conn.createStatement();
                    rs = stmnt.executeQuery(str);
                    if (rs.next()) {
                        String currSchema = rs.getString(1);
                        if (currSchema != null) {
                            setDefaultSchemaName(currSchema.trim());
                        }
                    }
                } else {
                    if (log.isTraceEnabled()) {
                        log.trace(_loc.get("can_not_get_current_schema", "Unable to obtain a datasource"));
                    }
                }
            } catch (SQLException e) {
                if (log.isTraceEnabled()) {
                    log.trace(_loc.get("can_not_get_current_schema", e.getMessage()));
                }
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException se) {
                        // ignore
                    }
                }
                if (stmnt != null) {
                    try {
                        stmnt.close();
                    } catch (SQLException se) {
                        // ignore
                    }
                }
                if (conn != null) { 
                    try { 
                        conn.close(); 
                    }
                    catch(SQLException se) { 
                        // ignore
                    }
                }
            }
        }
        return defaultSchemaName;
    }

    /**
     * Obtain a connection from the configuration. Tries to use the jta-data-source first but falls back on the
     * non-jta-data-source if no jta-data-source has been defined.
     * 
     * In practice this method is only called by getDefaultSchemaName which in turn is only used by the schema tool.
     * 
     * @throws SQLException If neither datasource is available.
     * @return A connection which may be used to obtain the default schema name. 
     */
    private Connection getConnection() throws SQLException {
    	DataSource  ds = null;
    	try {
            // try to obtain a connection from the primary datasource 
            ds = conf.getDataSource(null);
    	} catch (UserException uex) {
    	}
    	if (ds==null) {
        	try {
                // use datasource 2 if available
                ds = conf.getDataSource2(null);
        	} catch (UserException uex2) {
        	}
        }
        if (ds != null) {
            return ds.getConnection();
        }
        return null;
    }
    
    @Override
    protected SQLBuffer toSelect(SQLBuffer select, JDBCFetchConfiguration fetch,
        SQLBuffer tables, SQLBuffer where, SQLBuffer group,
        SQLBuffer having, SQLBuffer order,
        boolean distinct, boolean forUpdate, long start, long end,
        Select sel) {
    	if (!supportsRowNum) {
    		return super.toSelect(select, fetch, tables, where, group, having, order,
    		        distinct, forUpdate, start, end, sel);
    	}
        // if no range, use standard select
        if (!isUsingRange(start, end)) {
            return super.toSelect(select, fetch, tables, where, group, having,
                order, distinct, forUpdate, 0, Long.MAX_VALUE, sel);
        }
        
        // if no skip, ordering, or distinct can use rownum directly
        SQLBuffer buf = new SQLBuffer(this);
        if (!requiresSubselectForRange(start, end, distinct, order)) {
            if (where != null && !where.isEmpty())
                buf.append(where).append(" AND ");
            buf.append("ROWNUM <= ").appendValue(end);
            return super.toSelect(select, fetch, tables, buf, group, having,
                order, distinct, forUpdate, 0, Long.MAX_VALUE, sel);
        }

        // if there is ordering, skip, or distinct we have to use subselects
        SQLBuffer newsel = super.toSelect(select, fetch, tables, where,
            group, having, order, distinct, forUpdate, 0, Long.MAX_VALUE,
            sel);

        // if no skip, can use single nested subselect
        if (!isUsingOffset(start)) {
            buf.append(getSelectOperation(fetch) + " * FROM (");
            buf.append(newsel);
            buf.append(") WHERE ROWNUM <= ").appendValue(end);
            return buf;
        }

        // with a skip, we have to use a double-nested subselect to put
        // where conditions on the rownum
        buf.append(getSelectOperation(fetch))
           .append(" * FROM (SELECT r.*, ROWNUM RNUM FROM (");
        buf.append(newsel);
        buf.append(") r");
        if (isUsingLimit(end))
            buf.append(" WHERE ROWNUM <= ").appendValue(end);
        buf.append(") WHERE RNUM > ").appendValue(start);
        return buf;
    }
    
    /**
     * Return true if the select with the given parameters needs a
     * subselect to apply a range.
     */
    private boolean requiresSubselectForRange(long start, long end, boolean distinct, SQLBuffer order) {
    	if (!isUsingRange(start, end))
    		return false;
        return isUsingOffset(start) || distinct || isUsingOrderBy(order);
    }


}
