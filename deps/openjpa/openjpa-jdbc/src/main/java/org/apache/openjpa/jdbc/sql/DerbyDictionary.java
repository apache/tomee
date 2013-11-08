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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.DataSource;

import org.apache.openjpa.util.StoreException;

/**
 * Dictionary for Apache Derby (formerly Cloudscape).
 */
public class DerbyDictionary
    extends AbstractDB2Dictionary {

    /**
     * If true, we will shutdown the embedded database when closing
     * the DataSource.
     */
    public boolean shutdownOnClose = true;
    
    public DerbyDictionary() {
        platform = "Apache Derby";
        validationSQL = "VALUES(1)";
        stringLengthFunction = "LENGTH({0})";
        substringFunctionName = "SUBSTR";
        toUpperCaseFunction = "UPPER(CAST({0} AS VARCHAR(" + varcharCastLength + ")))";
        toLowerCaseFunction = "LOWER(CAST({0} AS VARCHAR(" + varcharCastLength + ")))";

        // Derby name length restriction has been relaxed 
        //http://www.archivum.info/derby-dev@db.apache.org/2004-12/msg00270.html
        maxConstraintNameLength = 128;
        maxIndexNameLength = 128;
        maxColumnNameLength = 128;
        maxTableNameLength = 128;

        useGetBytesForBlobs = true;
        useSetBytesForBlobs = true;

        allowsAliasInBulkClause = false;
        supportsDeferredConstraints = false;
        supportsParameterInSelect = false;
        supportsSelectForUpdate = true;
        supportsDefaultDeleteAction = false;
        requiresCastForMathFunctions = true;
        requiresCastForComparisons = true;
        supportsSimpleCaseExpression = false;
        supportsNullUniqueColumn = false;
        
        supportsComments = true;

        fixedSizeTypeNameSet.addAll(Arrays.asList(new String[]{
            "BIGINT", "INTEGER", "TEXT"
        }));
        reservedWordSet.addAll(Arrays.asList(new String[]{
            "BOOLEAN", "CALL", "ENDEXEC", "EXPLAIN", "FUNCTION",
            "GET_CURRENT_CONNECTION", "INOUT", "LONGINT", "LTRIM", "NONE",
            "NVARCHAR", "OFF", "OUT", "RTRIM", "SUBSTR", "XML", "XMLEXISTS",
            "XMLPARSE", "XMLSERIALIZE",
        }));

        // reservedWordSet subset that CANNOT be used as valid column names
        // (i.e., without surrounding them with double-quotes)
        invalidColumnWordSet.addAll(Arrays.asList(new String[] {
            "ADD", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "AS", "ASC",
            "ASSERTION", "AT", "AUTHORIZATION", "AVG", "BEGIN", "BETWEEN", 
            "BIT", "BOOLEAN", "BOTH", "BY", "CALL", "CASCADE", "CASCADED", 
            "CASE", "CAST", "CHAR", "CHARACTER", "CHARACTER_LENGTH", "CHECK",
            "CLOSE", "COALESCE", "COLLATE", "COLLATION", "COLUMN", "COMMIT", "CONNECT",
            "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONTINUE", "CONVERT",
            "CORRESPONDING", "CREATE", "CURRENT", "CURRENT_DATE", "CURRENT_ROLE",
            "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR",
            "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE",
            "DEFERRED", "DELETE", "DESC", "DESCRIBE", "DIAGNOSTICS", 
            "DISCONNECT", "DISTINCT", "DOUBLE", "DROP", "ELSE", "END", 
            "END-EXEC", "ESCAPE", "EXCEPT", "EXCEPTION", "EXEC", "EXECUTE",
            "EXISTS", "EXPLAIN", "EXTERNAL", "FALSE", "FETCH", "FIRST", "FLOAT",
            "FOR", "FOREIGN", "FOUND", "FROM", "FULL", "FUNCTION", "GET", 
            "GETCURRENTCONNECTION", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "HAVING", "HOUR",
            "IDENTITY", "IMMEDIATE", "IN", "INDICATOR", "INITIALLY", "INNER",
            "INOUT", "INPUT", "INSENSITIVE", "INSERT", "INT", "INTEGER",
            "INTERSECT", "INTO", "IS", "ISOLATION", "JOIN", "KEY", "LAST",
            "LEADING", "LEFT", "LIKE", "LOWER", "LTRIM", "MATCH", "MAX", "MIN",
            "MINUTE", "NATIONAL", "NATURAL", "NCHAR", "NEXT", "NO", "NONE", "NOT", 
            "NULL", "NULLIF", "NUMERIC", "NVARCHAR", "OF", "ON", "ONLY", "OPEN",
            "OPTION", "OR", "ORDER", "OUT", "OUTER", "OUTPUT", "OVER", "OVERLAPS", 
            "PAD", "PARTIAL", "PREPARE", "PRESERVE", "PRIMARY", "PRIOR",
            "PRIVILEGES", "PROCEDURE", "PUBLIC", "READ", "REAL", "REFERENCES",
            "RELATIVE", "RESTRICT", "REVOKE", "RIGHT", "ROLLBACK", "ROWS",
            "ROW_NUMBER", "RTRIM", "SCHEMA", "SCROLL", "SECOND", "SELECT", "SESSION_USER",
            "SET", "SMALLINT", "SOME", "SPACE", "SQL", "SQLCODE", "SQLERROR",
            "SQLSTATE", "SUBSTR", "SUBSTRING", "SUM", "SYSTEM_USER", "TABLE",
            "TEMPORARY", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING",
            "TRANSACTION", "TRANSLATE", "TRANSLATION", "TRIM", "TRUE", "UNION",
            "UNIQUE", "UNKNOWN", "UPDATE", "UPPER", "USER", "USING", "VALUES",
            "VARCHAR", "VARYING", "VIEW", "WHENEVER", "WHERE", "WITH", "WORK",
            "WRITE", "XML", "XMLEXISTS", "XMLPARSE", "XMLQUERY", "XMLSERIALIZE", "YEAR",
        }));
    }
    
    @Override
    public void connectedConfiguration(Connection conn) throws SQLException {
    	super.connectedConfiguration(conn);
    	if (versionEqualOrLaterThan(10, 5)) {
    		supportsSelectStartIndex = true;
    		supportsSelectEndIndex   = true;
    	}
    }

    /**
     * Appends a range to the given buffer.
     * <br>
     * A range query is never appended to a subselct clause.
     * <br>
     * If this dictionary supports {@link DBDictionary#supportsSelectStartIndex offset} 
     * and {@link DBDictionary#supportsSelectEndIndex limit} on queries then the
     * syntax is <pre>
     * [ OFFSET {start} ROWS ]
	 * [ FETCH NEXT {end-start} ROWS ONLY ]
     * </pre>
     * Otherwise, the offset is not used and the syntax is <pre>
     * [ FETCH FIRST {end} ROWS ONLY ]
     * </pre>
     * @param buf the SQL buffer to be appended
     * @param start starting offset. {@code 0} means offset is not used.
     * @param end number of rows to be fetched. {@code Long.MAX_VALUE} means no limit.
     * @param subselect flags if the buffer represents a SQL Subquery clause 
     */
    protected void appendSelectRange(SQLBuffer buf, long start, long end, boolean subselect) {
        // do not generate FETCH FIRST clause for subselect
    	if (subselect) 
    		return;
    	if (supportsSelectStartIndex && supportsSelectEndIndex) {
	    	if (isUsingOffset(start))
	    		buf.append(" OFFSET ").append(Long.toString(start)).append(" ROWS ");
	    	if (isUsingLimit(end)) {
	    		long rowCount = end - start;
	    		buf.append(" FETCH NEXT ").append(Long.toString(rowCount)).append(" ROWS ONLY");
	    	}
    	} else if (isUsingLimit(end)) {
             buf.append(" FETCH FIRST ").append(Long.toString(end)).append(" ROWS ONLY");
    	}
    }

    public void closeDataSource(DataSource dataSource) {
        super.closeDataSource(dataSource);

        if (!shutdownOnClose)
            return;

        // as well as closing the DataSource, we also need to
        // shut down the instance if we are using an embedded database, which
        // can only be done by connecting to the same URL with the
        // ";shutdown=true" string appended to the end
        // see: http://db.apache.org/derby/docs/dev/devguide/tdevdvlp40464.html
        if (conf != null && conf.getConnectionDriverName() != null &&
            conf.getConnectionDriverName().indexOf("EmbeddedDriver") != -1) {
            try {
                DriverManager.getConnection(conf.getConnectionURL()
                    + ";shutdown=true");
            } catch (SQLException e) {
                // we actually expect a SQLException to be thrown here:
                // Derby strangely uses that as a mechanism to report
                // a successful shutdown
            }
        }
    }
    
    @Override
    public boolean isFatalException(int subtype, SQLException ex) {
        int errorCode = ex.getErrorCode();
        if ((subtype == StoreException.LOCK ||
             subtype == StoreException.QUERY) && errorCode <= 30000) {
            return false;
        }
        return super.isFatalException(subtype, ex);
    }
    
	/**
	 * Applies range calculation on the actual number of rows selected by a
	 * {@code COUNT(*)} query. A range query may use either only the limit or
	 * both offset and limit based on database dictionary support and
	 * accordingly the number of rows in the result set needs to be modified.
	 * 
	 * @param select
	 * @param count
	 * @return
	 */

	public int applyRange(Select select, int count) {
		long start = select.getStartIndex();
		long end = select.getEndIndex();
		if (supportsSelectStartIndex) {
			if (start > 0)
				count -= start;
			if (end != Long.MAX_VALUE) {
				long size = end - start;
				count = (int) Math.min(count, size);
			}
		}
		return count;
	}
}
