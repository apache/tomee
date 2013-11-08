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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.exps.FilterValue;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.schema.Sequence;
import org.apache.openjpa.jdbc.schema.Unique;
import org.apache.openjpa.lib.identifier.IdentifierUtil;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.StoreException;
import org.apache.openjpa.util.UnsupportedException;

/**
 * Dictionary for Firebird. Supports Firebird versions 1.5, 2.0 and 2.1.
 */
public class FirebirdDictionary
    extends DBDictionary {

    public int firebirdVersion = 0;
    public int indexedVarcharMaxSizeFB15 = 252;
    public String rangeSyntax = null;
    protected long maxRowNumberInRange = 16000000000L;

    protected String alterSequenceSQLFB15 = "SET GENERATOR {0} TO {1}";
    protected String alterSequenceSQLFB20 =
        "ALTER SEQUENCE {0} RESTART WITH {1}";
    protected String createSequenceSQLFB15 = "CREATE GENERATOR {0}";
    protected String createSequenceSQLFB20 = "CREATE SEQUENCE {0}";
    protected String dropSequenceSQLFB15 = "DROP GENERATOR ";
    protected String alterSequenceSQL = alterSequenceSQLFB20;
    protected String createSequenceSQL = createSequenceSQLFB20;

    public static final int FB_VERSION_15 = 15;
    public static final int FB_VERSION_20 = 20;
    public static final int FB_VERSION_21 = 21;

    public static final String RANGE_SYNTAX_FIRST_SKIP = "firstskip";
    public static final String RANGE_SYNTAX_ROWS = "rows";

    private static final Localizer _loc =
        Localizer.forPackage(FirebirdDictionary.class);

    @SuppressWarnings("unchecked")
    public FirebirdDictionary() {
        platform = "Firebird";
        validationSQL = "SELECT 1 FROM RDB$DATABASE";
        supportsDeferredConstraints = false;

        useGetStringForClobs = true;
        useSetStringForClobs = true;
        useGetBytesForBlobs = true;
        useSetBytesForBlobs = true;

        maxTableNameLength = 31;
        maxColumnNameLength = 31;
        maxConstraintNameLength = 31;
        maxIndexNameLength = 31;

        supportsSelectStartIndex = true;
        supportsSelectEndIndex = true;

        supportsMultipleNontransactionalResultSets = false;

        // On Firebird 2 the recommended syntax is "SELECT NEXT VALUE FOR {0} FROM RDB$DATABASE".
        // However, that syntax allows incrementing the sequence value by 1 only.
        nextSequenceQuery = "SELECT GEN_ID({0}, {1}) FROM RDB$DATABASE";
        sequenceSQL =
            "SELECT NULL AS SEQUENCE_SCHEMA, RDB$GENERATOR_NAME "
                + "AS SEQUENCE_NAME FROM RDB$GENERATORS "
                + "WHERE (RDB$SYSTEM_FLAG IS NULL OR RDB$SYSTEM_FLAG = 0) ";
        sequenceNameSQL = "AND RDB$GENERATOR_NAME = ?";

        // A rough sum of reserved words in Firebird 1.0 - 2.1.
        reservedWordSet.addAll(Arrays.asList(new String[] { "ACTIVE", "ADMIN",
            "AFTER", "ASCENDING", "AUTO", "AUTODDL", "BASED", "BASENAME",
            "BASE_NAME", "BEFORE", "BIGINT", "BLOB", "BLOBEDIT", "BUFFER",
            "CACHE", "CHECK_POINT_LEN", "CHECK_POINT_LENGTH", "COMPILETIME",
            "COMPUTED", "CLOSE", "CONDITIONAL", "CONTAINING", "CSTRING",
            "CURRENT_CONNECTION", "CURRENT_ROLE", "CURRENT_TRANSACTION",
            "DATABASE", "DB_KEY", "DEBUG", "DESCENDING", "DO", "ECHO", "EDIT",
            "ENTRY_POINT", "EVENT", "EXIT", "EXTERN", "FILE", "FILTER",
            "FREE_IT", "FUNCTION", "GDSCODE", "GENERATOR", "GEN_ID", "GLOBAL",
            "GOTO", "GROUP_COMMIT_WAIT", "GROUP_COMMIT_WAIT_TIME", "HELP",
            "IF", "INACTIVE", "INDEX", "INIT", "INPUT_TYPE", "ISQL",
            "LC_MESSAGES", "LC_TYPE", "LEV", "LOGFILE", "LOG_BUFFER_SIZE",
            "LOG_BUF_SIZE", "LONG", "MANUAL", "MAXIMUM", "MAXIMUM_SEGMENT",
            "MAX_SEGMENT", "MERGE", "MESSAGE", "MINUTE", "MODULE_NAME",
            "NOAUTO", "NUM_LOG_BUFS", "NUM_LOG_BUFFERS", "OUTPUT_TYPE",
            "OVERFLOW", "PAGE", "PAGELENGTH", "PAGES", "PAGE_SIZE",
            "PARAMETER", "PASSWORD", "PLAN", "POST_EVENT", "PROCEDURE",
            "PROTECTED", "QUIT", "RAW_PARTITIONS", "RDB$DB_KEY",
            "RECORD_VERSION", "RECREATE", "RELEASE", "RESERV", "RESERVING",
            "RETAIN", "RETURN", "RETURNING_VALUES", "RETURNS", "ROLE",
            "RUNTIME", "SAVEPOINT", "SEGMENT", "SHADOW", "SHARED", "SHELL",
            "SHOW", "SINGULAR", "SNAPSHOT", "SORT", "SQLWARNING", "STABILITY",
            "START", "STARTING", "STARTS", "STATEMENT", "STATIC", "STATISTICS",
            "SUB_TYPE", "SUSPEND", "TERMINATOR", "TRIGGER", "VARIABLE",
            "VERSION", "WAIT", "WEEKDAY", "WHILE" }));

        binaryTypeName = "BLOB";
        bitTypeName = "SMALLINT";
        charTypeName = "CHAR(1)";
        clobTypeName = "BLOB SUB_TYPE 1";
        doubleTypeName = "DOUBLE PRECISION";
        floatTypeName = "DOUBLE PRECISION";
        longVarbinaryTypeName = "BLOB";
        longVarcharTypeName = "BLOB SUB_TYPE 1";
        realTypeName = "FLOAT";
        smallintTypeName = "SMALLINT";
        tinyintTypeName = "SMALLINT";
        varbinaryTypeName = "BLOB";

        supportsLockingWithDistinctClause = false;
        supportsLockingWithMultipleTables = false;
        supportsLockingWithOuterJoin = false;
        supportsLockingWithInnerJoin = false;
        forUpdateClause = "FOR UPDATE WITH LOCK";
        supportsQueryTimeout = false;
    }

    /**
     * Determine Firebird version and configure itself accordingly.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void connectedConfiguration(Connection conn) throws SQLException {
        super.connectedConfiguration(conn);
        firebirdVersion = determineFirebirdVersion(conn);
        determineRangeSyntax();

        if (firebirdVersion == FB_VERSION_21)
            selectWordSet.add("WITH");
        if (!(firebirdVersion == FB_VERSION_21)) {
            crossJoinClause = "JOIN";
            requiresConditionForCrossJoin = true;
        }
        if (firebirdVersion == FB_VERSION_15) {
            stringLengthFunction = "STRLEN({0})";
            trimLeadingFunction = "LTRIM({0})";
            trimTrailingFunction = "RTRIM({0})";
            trimBothFunction = "LTRIM(RTRIM({0}))";
            alterSequenceSQL = alterSequenceSQLFB15;
            createSequenceSQL = createSequenceSQLFB15;
        }
    }

    /**
     * Use either <code>FIRST &lt;p&gt; SKIP &lt;q&gt;</code> or
     * <code>ROWS &lt;m&gt; TO &lt;n&gt;</code> syntax. If <code>ROWS</code>
     * variant is used and <code>end</code> equals {@link Long#MAX_VALUE}, a
     * constant is used as <code>&lt;n&gt;</code> value.
     */
    @Override
    protected void appendSelectRange(SQLBuffer buf, long start, long end,
        boolean subselect) {
        if (RANGE_SYNTAX_FIRST_SKIP.equals(rangeSyntax)) {
            if (end != Long.MAX_VALUE)
                buf.append(" FIRST ").appendValue(end - start);
            if (start != 0)
                buf.append(" SKIP ").appendValue(start);
            return;
        }

        buf.append(" ROWS ");
        if (start == 0) {
            buf.appendValue(end);
            return;
        }

        buf.appendValue(start + 1).append(" TO ");
        if (end == Long.MAX_VALUE)
            buf.appendValue(maxRowNumberInRange);
        else
            buf.appendValue(end);
    }

    /**
     * Determine Firebird version either by using JDBC 3 methods or, if they
     * are not available, by parsing the value returned by
     * {@linkplain DatabaseMetaData#getDatabaseProductVersion()}. User can
     * override Firebird version.
     */
    protected int determineFirebirdVersion(Connection con)
        throws SQLException {
        // Let user override firebirdVersion.
        if (firebirdVersion != 0)
            return firebirdVersion;

        DatabaseMetaData metaData = con.getMetaData();
        int maj = 0;
        int min = 0;
        if (isJDBC3) {
            maj = metaData.getDatabaseMajorVersion();
            min = metaData.getDatabaseMinorVersion();
        } else {
            try {
                // The product version looks like
                // "LI-V2.1.1.17910 Firebird 2.1,LI-V2.1.1.17910 Firebird
                // 2.1/tcp (hostname)/P10" or
                // "WI-V1.5.5.4926 Firebird 1.52WI-V1.5.5.4926 Firebird 1.5/tcp
                // (hostname)/P10"
                String productVersion = metaData.getDatabaseProductVersion();
                Pattern p = Pattern.compile(".*-V(\\d)\\.(\\d)\\..*",
                    Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(productVersion);
                m.matches();
                String majString = m.group(1);
                String minString = m.group(2);
                maj = Integer.parseInt(majString);
                min = Integer.parseInt(minString);
            } catch (Exception e) {
                // We don't understand the version format.
                if (log.isWarnEnabled())
                    log.warn(e.toString(), e);
            }
        }
        if (maj < 2)
            return FB_VERSION_15;
        if (maj == 2 && min == 0)
            return FB_VERSION_20;
        return FB_VERSION_21;
    }

    /**
     * Determine range syntax to be used depending on Firebird version.
     * User can override range syntax.
     */
    protected void determineRangeSyntax() {
        // Let user override rangeSyntax.
        if (rangeSyntax == null)
            rangeSyntax =
                (firebirdVersion == FB_VERSION_15) ? RANGE_SYNTAX_FIRST_SKIP
                    : RANGE_SYNTAX_ROWS;

        if (RANGE_SYNTAX_FIRST_SKIP.equals(rangeSyntax))
            rangePosition = RANGE_PRE_DISTINCT;
        else
            rangePosition = RANGE_POST_SELECT;
    }

    /**
     * Return <code>&lt;value&gt; AS &lt;type&gt;</code>.
     */
    @Override
    public String getPlaceholderValueString(Column col) {
        return super.getPlaceholderValueString(col) + " AS "
            + getTypeName(col);
    }

    /**
     * Return <code>%</code> if <code>tableName</code> is <code>null</code>,
     * otherwise delegate to super implementation.
     */
    @Override
    protected String getTableNameForMetadata(String tableName) {
        return (tableName == null) ? IdentifierUtil.PERCENT : 
            getTableNameForMetadata(DBIdentifier.newTable(tableName));
    }

    protected String getTableNameForMetadata(DBIdentifier tableName) {
        if (DBIdentifier.isNull(tableName)) {
            return IdentifierUtil.PERCENT;
        }
        return super.getTableNameForMetadata(tableName);
    }

    /**
     * Return <code>%</code> if <code>columnName</code> is <code>null</code>,
     * otherwise delegate to super implementation.
     */
    @Override
    protected String getColumnNameForMetadata(String columnName) {
        return (columnName == null) ? "%" : super
            .getColumnNameForMetadata(columnName);
    }

    /**
     * Return
     * <code>ALTER TABLE &lt;table name&gt; DROP &lt;col name&gt;</code>.
     */
    @Override
    public String[] getDropColumnSQL(Column column) {
        return new String[] { "ALTER TABLE "
            + getFullName(column.getTable(), false) + " DROP " + getColumnDBName(column) };
    }

    /**
     * Return either
     * <code>CREATE SEQUENCE &lt;sequence name&gt;</code> or
     * <code>CREATE GENERATOR &lt;sequence name&gt;</code>.
     * If initial value of sequence is set, return also
     * an appropriate <code>ALTER</code> statement.
     */
    @Override
    public String[] getCreateSequenceSQL(Sequence seq) {
        String seqName =
            checkNameLength(getFullName(seq), maxTableNameLength,
                "long-seq-name");
        String createSeq =
            MessageFormat.format(createSequenceSQL, new Object[] { seqName });
        if (seq.getInitialValue() == 0)
            return new String[] { createSeq };

        // Use String.valueOf to get rid of possible number formatting.
        String alterSeq =
            MessageFormat.format(alterSequenceSQL, new Object[] { seqName,
                String.valueOf(seq.getInitialValue()) });
        return new String[] { createSeq, alterSeq };
    }

    /**
     * Return Firebird-specific statement to select the list of sequences.
     */
    @Override
    protected String getSequencesSQL(String schemaName, String sequenceName) {
        return getSequencesSQL(DBIdentifier.newSchema(schemaName), DBIdentifier.newSequence(sequenceName));
    }

    @Override
    protected String getSequencesSQL(DBIdentifier schemaName, DBIdentifier sequenceName) {
        StringBuilder buf = new StringBuilder(sequenceSQL);
        if (!DBIdentifier.isNull(sequenceName)) {
            buf.append(sequenceNameSQL);
        }
        return buf.toString();
    }

    /**
     * Call super implementation and trim sequence name. This is because of
     * trailing spaces problem: <code>RDB$GENERATORS.RDB$GENERATOR_NAME</code>
     * is <code>CHAR(31)</code> and using <code>RTRIM</code> UDF function on
     * Firebird 1.5 surprisingly returns a string right-padded with spaces up
     * to the length of 255.
     */
    @Override
    protected Sequence newSequence(ResultSet sequenceMeta) throws SQLException {
        Sequence seq = super.newSequence(sequenceMeta);
        seq.setIdentifier(DBIdentifier.trim(seq.getIdentifier()));
        return seq;
    }

    /**
     * On Firebird 1.5 return
     * <code>DROP GENERATOR &lt;sequence name&gt;</code>.
     * On Firebird 2.0 and later delegate to the super implementation.
     */
    @Override
    public String[] getDropSequenceSQL(Sequence seq) {
        if (firebirdVersion == FB_VERSION_15)
            return new String[] { dropSequenceSQLFB15 + getFullName(seq) };
        return super.getDropSequenceSQL(seq);
    }

    /**
     * On Firebird 2.1 return <code>POSITION(&lt;find&gt;, &lt;str&gt; [, &lt;start&gt;])<code>.
     * On older versions throw {@link UnsupportedException} - no suitable function exists.
     */
    @Override
    public void indexOf(SQLBuffer buf, FilterValue str, FilterValue find,
        FilterValue start) {
        if (firebirdVersion < FB_VERSION_21) {
            throw new UnsupportedException(_loc.get("function-not-supported", getClass(), "LOCATE"));
        }
        buf.append("POSITION(");
        find.appendTo(buf);
        buf.append(", ");
        str.appendTo(buf);
        if (start != null) {
            buf.append(", ");
            buf.append("CAST(");
            start.appendTo(buf);
            buf.append(" AS INTEGER)");
        }
        buf.append(")");
    }

    /**
     * Use
     * <code>SUBSTRING(&lt;col name&gt; FROM &lt;m&gt; FOR &lt;n&gt;)</code>.
     * Parameters are inlined because neither parameter binding nor expressions
     * are accepted by Firebird here. As a result, an
     * {@link UnsupportedException} is thrown when something else than a
     * constant is used in <code>start</code> or <code>length</code>.
     */
    @Override
    public void substring(SQLBuffer buf, FilterValue str, FilterValue start,
        FilterValue length) {
        buf.append(substringFunctionName).append("(");
        str.appendTo(buf);
        buf.append(" FROM ");
        if (start.getValue() instanceof Number) {
            long startLong = toLong(start);
            buf.append(Long.toString(startLong));
        } else {
            throw new UnsupportedException(_loc.get("function-not-supported",
                getClass(), substringFunctionName + " with non-constants"));
        }
        if (length != null) {
            buf.append(" FOR ");
            if (length.getValue() instanceof Number) {
                long lengthLong = toLong(length);
                buf.append(Long.toString(lengthLong));
            } else {
                throw new UnsupportedException(_loc.get(
                    "function-not-supported", getClass(), substringFunctionName
                        + " with non-constants"));
            }
        }
        buf.append(")");
    }

    /**
     * On Firebird 1.5 reduce the size of indexed <code>VARCHAR</code> column
     * to 252 or a value specified by user. 252 is the maximum Firebird 1.5 can
     * handle for one-column indexes. On Firebird 2.0 and later delegate to the
     * super implementation.
     */
    @Override
    protected String appendSize(Column col, String typeName) {
        if (firebirdVersion != FB_VERSION_15)
            return super.appendSize(col, typeName);

        if (col.getType() == Types.VARCHAR
            && col.getSize() > indexedVarcharMaxSizeFB15
            && col.getTable() != null) {

            if (col.isPrimaryKey()) {
                col.setSize(indexedVarcharMaxSizeFB15);
                return super.appendSize(col, typeName);
            }
            Index[] indexes = col.getTable().getIndexes();
            for (Index index : indexes) {
                if (index.containsColumn(col)) {
                    col.setSize(indexedVarcharMaxSizeFB15);
                    return super.appendSize(col, typeName);
                }
            }
            Unique[] uniques = col.getTable().getUniques();
            for (Unique unique : uniques) {
                if (unique.containsColumn(col)) {
                    col.setSize(indexedVarcharMaxSizeFB15);
                    return super.appendSize(col, typeName);
                }
            }
            ForeignKey[] foreignKeys = col.getTable().getForeignKeys();
            for (ForeignKey foreignKey : foreignKeys) {
                if (foreignKey.containsColumn(col)) {
                    col.setSize(indexedVarcharMaxSizeFB15);
                    return super.appendSize(col, typeName);
                }
            }
        }
        return super.appendSize(col, typeName);
    }

    /**
     * Use error code as SQL state returned by Firebird is ambiguous.
     */
    @Override
    protected int matchErrorState(Map<Integer,Set<String>> errorStates, SQLException ex) {
        String errorState = ""+ex.getErrorCode();
        for (Map.Entry<Integer,Set<String>> states : errorStates.entrySet()) {
            if (states.getValue().contains(errorState))
                return states.getKey();
        }
        return StoreException.GENERAL;
    }
}
