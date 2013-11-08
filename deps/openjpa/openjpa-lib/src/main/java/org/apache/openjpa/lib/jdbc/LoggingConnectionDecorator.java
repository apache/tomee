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
package org.apache.openjpa.lib.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.BatchUpdateException;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.J2DoPrivHelper;

/**
 * A {@link ConnectionDecorator} that creates logging connections and
 * {@link ReportingSQLException}s.
 *
 * @author Marc Prud'hommeaux
 * @nojavadoc
 */
public class LoggingConnectionDecorator implements ConnectionDecorator {

    private static final String SEP = J2DoPrivHelper.getLineSeparator();

    private static final int WARN_IGNORE = 0;
    private static final int WARN_LOG_TRACE = 1;
    private static final int WARN_LOG_INFO = 2;
    private static final int WARN_LOG_WARN = 3;
    private static final int WARN_LOG_ERROR = 4;
    private static final int WARN_THROW = 5;
    private static final int WARN_HANDLE = 6;
    private static final String[] WARNING_ACTIONS = new String[7];
    
    static {
        WARNING_ACTIONS[WARN_IGNORE] = "ignore";
        WARNING_ACTIONS[WARN_LOG_TRACE] = "trace";
        WARNING_ACTIONS[WARN_LOG_INFO] = "info";
        WARNING_ACTIONS[WARN_LOG_WARN] = "warn";
        WARNING_ACTIONS[WARN_LOG_ERROR] = "error";
        WARNING_ACTIONS[WARN_THROW] = "throw";
        WARNING_ACTIONS[WARN_HANDLE] = "handle";
    }

    private final DataSourceLogs _logs = new DataSourceLogs();
    private SQLFormatter _formatter;
    private boolean _prettyPrint;
    private int _prettyPrintLineLength = 60;
    private int _warningAction = WARN_IGNORE;
    private SQLWarningHandler _warningHandler;
    private boolean _printParameters = false;

    /**
     * If set to <code>true</code>, pretty-print SQL by running it
     * through {@link SQLFormatter#prettyPrint}. If
     * <code>false</code>, don't pretty-print, and output SQL logs in
     * a single line. Pretty-printed SQL can be easier for a human to
     * read, but is harder to parse with tools like grep.
     */
    public void setPrettyPrint(boolean prettyPrint) {
        _prettyPrint = prettyPrint;
        if (_formatter == null && _prettyPrint) {
            _formatter = new SQLFormatter();
            _formatter.setLineLength(_prettyPrintLineLength);
        } else if (!_prettyPrint)
            _formatter = null;
    }

    /**
     * @see #setPrettyPrint
     */
    public boolean getPrettyPrint() {
        return _prettyPrint;
    }

    /**
     * The number of characters to print per line when
     * pretty-printing of SQL is enabled. Defaults to 60 to provide
     * some space for any ant-related characters on the left of a
     * standard 80-character display.
     */
    public void setPrettyPrintLineLength(int length) {
        _prettyPrintLineLength = length;
        if (_formatter != null)
            _formatter.setLineLength(length);
    }

    /**
     * @see #setPrettyPrintLineLength
     */
    public int getPrettyPrintLineLength() {
        return _prettyPrintLineLength;
    }

    /**
     * <p>
     * Whether parameter values will be printed in exception messages or in trace. This is different from
     * trackParameters which controls whether OpenJPA will track parameters internally (visible while debugging and used
     * in batching).
     * </p>
     */
    public boolean getPrintParameters() {
        return _printParameters;
    }

    public void setPrintParameters(boolean printParameters) {
        _printParameters = printParameters;
    }

    /**
     * What to do with SQL warnings.
     */
    public void setWarningAction(String warningAction) {
        int index = Arrays.asList(WARNING_ACTIONS).indexOf(warningAction);
        if (index < 0)
            index = WARN_IGNORE;
        _warningAction = index;
    }

    /**
     * What to do with SQL warnings.
     */
    public String getWarningAction() {
        return WARNING_ACTIONS[_warningAction];
    }

    /**
     * What to do with SQL warnings.
     */
    public void setWarningHandler(SQLWarningHandler warningHandler) {
        _warningHandler = warningHandler;
    }

    /**
     * What to do with SQL warnings.
     */
    public SQLWarningHandler getWarningHandler() {
        return _warningHandler;
    }

    /**
     * The log to write to.
     */
    public DataSourceLogs getLogs() {
        return _logs;
    }

    public Connection decorate(Connection conn) throws SQLException {
        return newLoggingConnection(conn);
    }
         
    private LoggingConnection newLoggingConnection(Connection conn)
        throws SQLException {
        return new LoggingConnection(conn);
    }

    private SQLException wrap(SQLException sqle, Statement stmnt) {
        return wrap(sqle, stmnt, null, -1);
    }

    private SQLException wrap(SQLException sqle, String sql) {
        return wrap(sqle, null, sql, -1);
    }
    
    private SQLException wrap(SQLException sqle, Statement stmnt, String sql) {
        return wrap(sqle, stmnt, sql, -1);
    }

    private SQLException wrap(SQLException sqle, Statement stmnt, int indexOfFailedBatchObject) {
        return wrap(sqle, stmnt, null, indexOfFailedBatchObject);
    }

    /**
     * Include SQL in exception.
     */
    private SQLException wrap(SQLException sqle, Statement stmnt, String sql, int indexOfFailedBatchObject) {
        ReportingSQLException toReturn = null;

        if (sqle instanceof ReportingSQLException) {
            toReturn = (ReportingSQLException) sqle;
        } else {
            toReturn = new ReportingSQLException(sqle, stmnt, sql);
        }

        toReturn.setIndexOfFirstFailedObject(indexOfFailedBatchObject);
        return toReturn;
    }

    /**
     * Interface that allows customization of what to do when
     * {@link SQLWarning}s occur.
     */
    public static interface SQLWarningHandler {

        public void handleWarning(SQLWarning warning) throws SQLException;
    }

    /**
     * Logging connection.
     */
    protected class LoggingConnection extends DelegatingConnection {

        public LoggingConnection(Connection conn) throws SQLException {
            super(conn);
        }

        protected PreparedStatement prepareStatement(String sql, boolean wrap)
            throws SQLException {
            SQLException err = null;
            try {
                PreparedStatement stmnt = super.prepareStatement(sql, false);
                return newLoggingPreparedStatement(stmnt, sql);
            } catch (SQLException se) {
                err = wrap(se, sql);
                throw err;
            }  finally {
                handleSQLErrors(err);
            }
        }

        protected PreparedStatement prepareStatement(String sql, int rsType,
            int rsConcur, boolean wrap) throws SQLException {
            SQLException err = null;
            try {
                PreparedStatement stmnt = super.prepareStatement
                    (sql, rsType, rsConcur, false);
                return newLoggingPreparedStatement(stmnt, sql);
            } catch (SQLException se) {
                err =  wrap(se, sql);
                throw err;
            } finally {
                handleSQLErrors(err);
            }
        }

        protected Statement createStatement(boolean wrap) throws SQLException {
            SQLException err = null;
            try {
                Statement stmnt = super.createStatement(false);
                return newLoggingStatement(stmnt);
            }catch (SQLException se) {
                err = se;
                throw se;
            } finally {
                handleSQLErrors(err);
            }
        }

        protected Statement createStatement(int type, int concurrency,
            boolean wrap) throws SQLException {
            SQLException err = null;
            try {
                Statement stmnt = super.createStatement(type, concurrency, 
                    false);
                return newLoggingStatement(stmnt);
            } catch (SQLException se) {
                err = se;
                throw se;
            } finally {
                handleSQLErrors(err);
            }
        }
        
        protected CallableStatement prepareCall(String sql, boolean wrap) 
            throws SQLException {
            SQLException err = null;
            try {
                CallableStatement stmt = super.prepareCall(sql, wrap);
                return newLoggingCallableStatement(stmt, sql);
            } catch (SQLException se) {
                err = wrap(se, sql);
                throw err;
            } finally {
                handleSQLErrors(err);
            }
        }

        private LoggingPreparedStatement newLoggingPreparedStatement
            (PreparedStatement stmnt, String sql) throws SQLException {
            return new LoggingPreparedStatement(stmnt, sql);
        }
        
        private CallableStatement newLoggingCallableStatement(
            CallableStatement stmnt, String sql) throws SQLException {
            return new LoggingCallableStatement(stmnt, sql);
        }
        
        private LoggingStatement newLoggingStatement(Statement stmnt)
            throws SQLException {
            return new LoggingStatement(stmnt);
        }
        
        private LoggingDatabaseMetaData newLoggingDatabaseMetaData
            (DatabaseMetaData meta) throws SQLException {
            return new LoggingDatabaseMetaData(meta);
        }



        public void commit() throws SQLException {
            long start = System.currentTimeMillis();
            SQLException err = null;            
            try {
                super.commit();
            } catch (SQLException se) {
                err = se;
                throw se;
            } finally {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("commit", start, this);
                handleSQLErrors(err);
            }
        }

        public void rollback() throws SQLException {
            long start = System.currentTimeMillis();
            SQLException err = null;            
            try {
                super.rollback();
            } catch (SQLException se) {
                err = se;
                throw se;
            } finally {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("rollback", start, this);
                handleSQLErrors(err);
            }
        }

        public void close() throws SQLException {
            long start = System.currentTimeMillis();       
            try {
                super.close();
            } finally {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("close", start, this);
            }
        }

        public Savepoint setSavepoint() throws SQLException {
            long start = System.currentTimeMillis();
            SQLException err = null;            
            try {
                return super.setSavepoint();
            } catch (SQLException se) {
                err = se;
                throw se;
            } finally {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("savepoint", start, this);
                handleSQLErrors(err);
            }
        }

        public Savepoint setSavepoint(String name) throws SQLException {
            long start = System.currentTimeMillis();
            SQLException err = null;            
            try {
                return super.setSavepoint(name);
            } catch (SQLException se) {
                err = se;
                throw se;
            } finally {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("savepoint: " + name, start, this);
                handleSQLErrors(err);
            }
        }

        public void rollback(Savepoint savepoint) throws SQLException {
            long start = System.currentTimeMillis();
            SQLException err = null;            
            try {
                super.rollback(savepoint);
            } catch (SQLException se) {
                err = se;
                throw se;
            } finally {
                if (_logs.isJDBCEnabled()) {
                    String name = null;
                    try {
                        name = savepoint.getSavepointName();
                    } catch (SQLException sqe) {
                        name = String.valueOf(savepoint.getSavepointId());
                    }
                    _logs.logJDBC("rollback: " + name, start, this);
                }
                handleSQLErrors(err);
            }
        }

        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            long start = System.currentTimeMillis();
            SQLException err = null;            
            try {
                super.releaseSavepoint(savepoint);
            } catch (SQLException se) {
                err = se;
                throw se;
            } finally {
                if (_logs.isJDBCEnabled()) {
                    String name = null;
                    try {
                        name = savepoint.getSavepointName();
                    } catch (SQLException sqe) {
                        name = String.valueOf(savepoint.getSavepointId());
                    }
                    _logs.logJDBC("release: " + name, start, this);
                }
                handleSQLErrors(err);
            }
        }

        protected Statement createStatement(int resultSetType,
            int resultSetConcurrency, int resultSetHoldability, boolean wrap)
            throws SQLException {
            SQLException err = null;
            try {
                Statement stmnt = super.createStatement(resultSetType,
                    resultSetConcurrency, resultSetHoldability, false);
                return newLoggingStatement(stmnt);
            }catch (SQLException se) {
                err = se;
                throw se;
            } finally {
                handleSQLErrors(err);
            }
        }

        protected PreparedStatement prepareStatement(String sql,
            int resultSetType, int resultSetConcurrency,
            int resultSetHoldability, boolean wrap) throws SQLException {
            SQLException err = null;
            try {
                PreparedStatement stmnt = super.prepareStatement
                    (sql, resultSetType, resultSetConcurrency,
                        resultSetHoldability, false);
                return newLoggingPreparedStatement(stmnt, sql);
            } catch (SQLException se) {
                err = wrap(se, sql);
                throw err;
            } finally {
                handleSQLErrors(err);
            }
        }

        protected PreparedStatement prepareStatement(String sql,
            int autoGeneratedKeys, boolean wrap) throws SQLException {
            SQLException err = null;
            try {
                PreparedStatement stmnt = super.prepareStatement
                    (sql, autoGeneratedKeys, false);
                return newLoggingPreparedStatement(stmnt, sql);
            } catch (SQLException se) {
                err = wrap(se, sql);
                throw err;
            } finally {
                handleSQLErrors(err);
            }
        }

        protected PreparedStatement prepareStatement(String sql,
            int[] columnIndexes, boolean wrap) throws SQLException {
            SQLException err = null;
            try {
                PreparedStatement stmnt = super.prepareStatement
                    (sql, columnIndexes, false);
                return newLoggingPreparedStatement(stmnt, sql);
            } catch (SQLException se) {
                err = wrap(se, sql);
                throw err;
            } finally {
                handleSQLErrors(err);
            }
        }

        protected PreparedStatement prepareStatement(String sql,
            String[] columnNames, boolean wrap) throws SQLException {
            SQLException err = null;
            try {
                PreparedStatement stmnt = super.prepareStatement
                    (sql, columnNames, false);
                return newLoggingPreparedStatement(stmnt, sql);
            } catch (SQLException se) {
                err = wrap(se, sql);
                throw err;
            } finally {
                handleSQLErrors(err);
            }
        }

        protected DatabaseMetaData getMetaData(boolean wrap)
            throws SQLException {
            return newLoggingDatabaseMetaData(super.getMetaData(false));
        }

        /**
         * Log time elapsed since given start.
         */
        private void logTime(long startTime) throws SQLException {
            if (_logs.isSQLEnabled())
                _logs.logSQL("spent", startTime, this); 
        }

        /**
         * Log time elapsed since given start.
         */
        private void logSQL(Statement stmnt) throws SQLException {
            if (_logs.isSQLEnabled())
                _logs.logSQL("executing " + stmnt, this);
        }

        /**
         * Log time elapsed since given start.
         */
        private void logBatchSQL(Statement stmnt) throws SQLException {
            if (_logs.isSQLEnabled())
                _logs.logSQL("executing batch " + stmnt, this);
        }
        
        /**
         * Handle any {@link SQLWarning}s on the current {@link Connection}.
         * Chain throwed SQLWarnings to SQLException.
         * @see #handleSQLWarning(SQLWarning)
         */
        private void handleSQLErrors(SQLException err) throws SQLException {
            if (_warningAction == WARN_IGNORE)
                return;

            try {
                handleSQLWarning(getWarnings());
            } catch (SQLException warning) {
                if (err != null)
                    err.setNextException(warning);
                else
                    throw warning;
            } finally {
                clearWarnings();
            }
        }

        /**
         * Handle any {@link SQLWarning}s on the specified {@link Statement}.
         * Chain throwed SQLWarnings to SQLException.
         *
         * @see #handleSQLWarning(SQLWarning)
         */
        private void handleSQLErrors(Statement stmnt, SQLException err) 
            throws SQLException {
            if (_warningAction == WARN_IGNORE)
                return; 
            
            try {
                handleSQLWarning(stmnt.getWarnings());
            } catch (SQLException warning) {
                if (err != null)
                    err.setNextException(warning);
                else
                    throw warning;
            } finally {
                stmnt.clearWarnings();
            }
        }        
        
        /**
         * Handle any {@link SQLWarning}s on the specified {@link ResultSet}.
         * Chain throwed SQLWarnings to SQLException.
         * 
         * @see #handleSQLWarning(SQLWarning)
         */
        private void handleSQLErrors(ResultSet rs, SQLException err) 
            throws SQLException {
            if (_warningAction == WARN_IGNORE)
                return;
            
            try {
                handleSQLWarning(rs.getWarnings());
            } catch (SQLException warning){
                if (err != null)
                    err.setNextException(warning);
                else
                    throw warning;
            } finally {
                rs.clearWarnings();
            }
        }
        
        /**
         * Handle the specified {@link SQLWarning} depending on the
         * setting of the {@link #setWarningAction} attribute.
         *
         * @param warning the warning to handle
         */
        private void handleSQLWarning(SQLWarning warning) throws SQLException {
            if (warning == null)
                return;
            if (_warningAction == WARN_IGNORE)
                return;

            Log log = _logs.getJDBCLog();
            for (; warning != null; warning = warning.getNextWarning()) {
                switch (_warningAction) {
                    case WARN_LOG_TRACE:
                        if (log.isTraceEnabled())
                            log.trace(warning);
                        break;
                    case WARN_LOG_INFO:
                        if (log.isInfoEnabled())
                            log.info(warning);
                        break;
                    case WARN_LOG_WARN:
                        if (log.isWarnEnabled())
                            log.warn(warning);
                        break;
                    case WARN_LOG_ERROR:
                        if (log.isErrorEnabled())
                            log.error(warning);
                        break;
                    case WARN_THROW:
                        // just throw it as if it were a SQLException
                        throw warning;
                    case WARN_HANDLE:
                        if (_warningHandler != null)
                            _warningHandler.handleWarning(warning);
                        break;
                    default:
                        // ignore
                        break;
                }
            }
        }

        /**
         * Metadata wrapper that logs actions.
         */
        protected class LoggingDatabaseMetaData
            extends DelegatingDatabaseMetaData {

            public LoggingDatabaseMetaData(DatabaseMetaData meta) {
                super(meta, LoggingConnection.this);
            }

            public ResultSet getBestRowIdentifier(String catalog,
                String schema, String table, int scope, boolean nullable)
                throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getBestRowIdentifier: "
                        + catalog + ", " + schema + ", " + table,
                        LoggingConnection.this);
                return super.getBestRowIdentifier(catalog, schema,
                    table, scope, nullable);
            }

            public ResultSet getCatalogs() throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getCatalogs", LoggingConnection.this);
                return super.getCatalogs();
            }

            public ResultSet getColumnPrivileges(String catalog, String schema,
                String table, String columnNamePattern) throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getColumnPrivileges: "
                        + catalog + ", " + schema + ", " + table,
                        LoggingConnection.this);
                return super.getColumnPrivileges(catalog, schema,
                    table, columnNamePattern);
            }

            public ResultSet getColumns(String catalog, String schemaPattern,
                String tableNamePattern, String columnNamePattern)
                throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getColumns: "
                        + catalog + ", " + schemaPattern + ", "
                        + tableNamePattern + ", " + columnNamePattern,
                        LoggingConnection.this);
                return super.getColumns(catalog, schemaPattern,
                    tableNamePattern, columnNamePattern);
            }

            public ResultSet getCrossReference(String primaryCatalog,
                String primarySchema, String primaryTable,
                String foreignCatalog, String foreignSchema,
                String foreignTable) throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getCrossReference: "
                        + primaryCatalog + ", " + primarySchema + ", "
                        + primaryTable + ", " + foreignCatalog + ", "
                        + foreignSchema + ", " + foreignSchema,
                        LoggingConnection.this);
                return super.getCrossReference(primaryCatalog, primarySchema,
                    primaryTable, foreignCatalog, foreignSchema, foreignTable);
            }

            public ResultSet getExportedKeys(String catalog, String schema,
                String table) throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getExportedKeys: "
                        + catalog + ", " + schema + ", " + table,
                        LoggingConnection.this);
                return super.getExportedKeys(catalog, schema, table);
            }

            public ResultSet getImportedKeys(String catalog, String schema,
                String table) throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getImportedKeys: "
                        + catalog + ", " + schema + ", " + table,
                        LoggingConnection.this);
                return super.getImportedKeys(catalog, schema, table);
            }

            public ResultSet getIndexInfo(String catalog, String schema,
                String table, boolean unique, boolean approximate)
                throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getIndexInfo: "
                        + catalog + ", " + schema + ", " + table,
                        LoggingConnection.this);
                return super.getIndexInfo(catalog, schema, table, unique,
                    approximate);
            }

            public ResultSet getPrimaryKeys(String catalog, String schema,
                String table) throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getPrimaryKeys: "
                        + catalog + ", " + schema + ", " + table,
                        LoggingConnection.this);
                return super.getPrimaryKeys(catalog, schema, table);
            }

            public ResultSet getProcedureColumns(String catalog,
                String schemaPattern, String procedureNamePattern,
                String columnNamePattern) throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getProcedureColumns: "
                        + catalog + ", " + schemaPattern + ", "
                        + procedureNamePattern + ", " + columnNamePattern,
                        LoggingConnection.this);
                return super.getProcedureColumns(catalog, schemaPattern,
                    procedureNamePattern, columnNamePattern);
            }

            public ResultSet getProcedures(String catalog,
                String schemaPattern, String procedureNamePattern)
                throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getProcedures: "
                        + catalog + ", " + schemaPattern + ", "
                        + procedureNamePattern, LoggingConnection.this);
                return super.getProcedures(catalog, schemaPattern,
                    procedureNamePattern);
            }

            public ResultSet getSchemas() throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getSchemas", LoggingConnection.this);
                return super.getSchemas();
            }

            public ResultSet getTablePrivileges(String catalog,
                String schemaPattern, String tableNamePattern)
                throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getTablePrivileges", LoggingConnection.this);
                return super.getTablePrivileges(catalog, schemaPattern,
                    tableNamePattern);
            }

            public ResultSet getTables(String catalog, String schemaPattern,
                String tableNamePattern, String[] types) throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getTables: "
                        + catalog + ", " + schemaPattern + ", "
                        + tableNamePattern, LoggingConnection.this);
                return super.getTables(catalog, schemaPattern,
                    tableNamePattern, types);
            }

            public ResultSet getTableTypes() throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getTableTypes", LoggingConnection.this);
                return super.getTableTypes();
            }

            public ResultSet getTypeInfo() throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getTypeInfo", LoggingConnection.this);
                return super.getTypeInfo();
            }

            public ResultSet getUDTs(String catalog, String schemaPattern,
                String typeNamePattern, int[] types) throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getUDTs", LoggingConnection.this);
                return super.getUDTs(catalog, schemaPattern,
                    typeNamePattern, types);
            }

            public ResultSet getVersionColumns(String catalog,
                String schema, String table) throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("getVersionColumns: "
                        + catalog + ", " + schema + ", " + table,
                        LoggingConnection.this);
                return super.getVersionColumns(catalog, schema, table);
            }
        }

        /**
         * Statement wrapper that logs SQL to the parent data source and
         * remembers the last piece of SQL to be executed on it.
         */
        protected class LoggingStatement extends DelegatingStatement {

            private String _sql = null;

            public LoggingStatement(Statement stmnt) throws SQLException {
                super(stmnt, LoggingConnection.this);
            }

            private LoggingResultSet newLoggingResultSet(ResultSet rs, Statement stmnt) {
                return new LoggingResultSet(rs, stmnt);
            }

            public void appendInfo(StringBuffer buf) {
                if (_sql != null) {
                    buf.append(" ");
                    if (_formatter != null) {
                        buf.append(SEP);
                        buf.append(_formatter.prettyPrint(_sql));
                    } else {
                        buf.append(_sql);
                    }
                }
            }

            protected ResultSet wrapResult(ResultSet rs, boolean wrap) {
                if (!wrap || rs == null)
                    return super.wrapResult(rs, wrap);
                return newLoggingResultSet(rs, this);
            }

            public void cancel() throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("cancel " + this, LoggingConnection.this);
                super.cancel();
            }

            protected ResultSet executeQuery(String sql, boolean wrap)
                throws SQLException {
                _sql = sql;
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.executeQuery(sql, wrap);
                } catch (SQLException se) {               	
                    err = wrap(se, LoggingStatement.this, sql);
                    throw err;
                } finally {
                    logTime(start);
                    handleSQLErrors(LoggingStatement.this, err);
                }
            }

            public int executeUpdate(String sql) throws SQLException {
                _sql = sql;
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.executeUpdate(sql);
                } catch (SQLException se) {                	
                    err = wrap(se, LoggingStatement.this, sql);
                    throw err;
                } finally {
                    logTime(start);
                    handleSQLErrors(LoggingStatement.this, err);
                }
            }

            public boolean execute(String sql) throws SQLException {
                _sql = sql;
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.execute(sql);
                } catch (SQLException se) {
                    err = wrap(se, LoggingStatement.this, sql);
                    throw err;
                } finally {
                    logTime(start);
                    handleSQLErrors(LoggingStatement.this, err);
                }
            }

            public int executeUpdate(String sql, int i) throws SQLException {
                _sql = sql;
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.executeUpdate(sql, i);
                } catch (SQLException se) {                 
                    err = wrap(se, LoggingStatement.this, sql);
                    throw err;
                } finally {
                    logTime(start);
                    handleSQLErrors(LoggingStatement.this, err);
                }
            }

            public int executeUpdate(String sql, int[] ia) throws SQLException {
                _sql = sql;
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.executeUpdate(sql, ia);
                } catch (SQLException se) {                 
                    err = wrap(se, LoggingStatement.this, sql);
                    throw err;
                } finally {
                    logTime(start);
                    handleSQLErrors(LoggingStatement.this, err);
                }
            }

            public int executeUpdate(String sql, String[] sa) throws SQLException {
                _sql = sql;
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.executeUpdate(sql, sa);
                } catch (SQLException se) {                 
                    err = wrap(se, LoggingStatement.this, sql);
                    throw err;
                } finally {
                    logTime(start);
                    handleSQLErrors(LoggingStatement.this, err);
                }
            }

            public boolean execute(String sql, int i) throws SQLException {
                _sql = sql;
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.execute(sql, i);
                } catch (SQLException se) {
                    err = wrap(se, LoggingStatement.this, sql);
                    throw err;
                } finally {
                    logTime(start);
                    handleSQLErrors(LoggingStatement.this, err);
                }
            }

            public boolean execute(String sql, int[] ia) throws SQLException {
                _sql = sql;
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.execute(sql, ia);
                } catch (SQLException se) {
                    err = wrap(se, LoggingStatement.this, sql);
                    throw err;
                } finally {
                    logTime(start);
                    handleSQLErrors(LoggingStatement.this, err);
                }
            }

            public boolean execute(String sql, String[] sa) throws SQLException {
                _sql = sql;
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.execute(sql, sa);
                } catch (SQLException se) {
                    err = wrap(se, LoggingStatement.this, sql);
                    throw err;
                } finally {
                    logTime(start);
                    handleSQLErrors(LoggingStatement.this, err);
                }
            }
        }

        protected class LoggingPreparedStatement
            extends DelegatingPreparedStatement {

            private final String _sql;
            private List<String> _params = null;
            private List<List<String>> _paramBatch = null;
            // When batching is used, this variable contains the index into the
            // last successfully executed batched statement.
            int batchedRowsBaseIndex = 0;

            public LoggingPreparedStatement(PreparedStatement stmnt, String sql)
                throws SQLException {
                super(stmnt, LoggingConnection.this);
                _sql = sql;
            }

            private LoggingResultSet newLoggingResultSet(ResultSet rs,
                PreparedStatement stmnt) {
                return new LoggingResultSet(rs, stmnt);
            }

            protected ResultSet wrapResult(ResultSet rs, boolean wrap) {
                if (!wrap || rs == null)
                    return super.wrapResult(rs, wrap);
                return newLoggingResultSet(rs, this);
            }

            protected ResultSet executeQuery(String sql, boolean wrap)
                throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.executeQuery(sql, wrap);
                } catch (SQLException se) {
                    err = wrap(se, LoggingPreparedStatement.this, sql);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingPreparedStatement.this, err);
                }
            }

            public int executeUpdate(String sql) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.executeUpdate(sql);
                } catch (SQLException se) {
                    err =  wrap(se, LoggingPreparedStatement.this, sql);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingPreparedStatement.this, err);
                }
            }

            public boolean execute(String sql) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.execute(sql);
                } catch (SQLException se) {
                    err = wrap(se, LoggingPreparedStatement.this, sql);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingPreparedStatement.this, err);
                }
            }

            protected ResultSet executeQuery(boolean wrap) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.executeQuery(wrap);
                } catch (SQLException se) {
                    err = wrap(se, LoggingPreparedStatement.this, _sql);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingPreparedStatement.this, err);
                }
            }

            public int executeUpdate() throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.executeUpdate();
                } catch (SQLException se) {
                    err = wrap(se, LoggingPreparedStatement.this);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingPreparedStatement.this, err);
                }
            }

            public int[] executeBatch() throws SQLException {
                int indexOfFirstFailedObject = -1;

                logBatchSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    int[] toReturn = super.executeBatch();
                    //executeBatch is called any time the number of batched statements
                    //is equal to, or less than, batchLimit.  In the 'catch' block below,
                    //the logic seeks to find an index based on the current executeBatch
                    //results.  This is fine when executeBatch is only called once, but
                    //if executeBatch is called many times, the _paramsBatch will continue
                    //to grow, as such, to index into _paramsBatch, we need to take into
                    //account the number of times executeBatch is called in or der to
                    //correctly index into _paramsBatch.  To that end, each time executeBatch
                    //is called, lets get the size of _paramBatch.  This will effectively
                    //tell us the index of the last successfully executed batch statement.
                    //If an exception is caused, then we know that _paramBatch.size was
                    //the index of the LAST row to successfully execute.
                    if (_paramBatch != null){
                        batchedRowsBaseIndex = _paramBatch.size();            
                    }
                    return toReturn;
                } catch (SQLException se) {
                    // if the exception is a BatchUpdateException, and
                    // we are tracking parameters, then set the current
                    // parameter set to be the index of the failed
                    // statement so that the ReportingSQLException will
                    // show the correct param(s)
                    if (se instanceof BatchUpdateException
                        && _paramBatch != null && shouldTrackParameters()) {
                        int[] count = ((BatchUpdateException) se).
                            getUpdateCounts();
                        if (count != null && count.length <= _paramBatch.size())
                        {
                            for (int i = 0; i < count.length; i++) {
                                // -3 is Statement.STATEMENT_FAILED, but is
                                // only available in JDK 1.4+
                                if (count[i] == Statement.EXECUTE_FAILED) {
                                    indexOfFirstFailedObject = i;
                                    break;
                                }
                            }

                            // no -3 element: it may be that the server stopped
                            // processing, so the size of the count will be
                            // the index
                            //See the Javadoc for 'getUpdateCounts'; a provider
                            //may stop processing when the first failure occurs,
                            //as such, it may only return 'UpdateCounts' for the
                            //first few which pass.  As such, the failed
                            //index is 'count.length', NOT count.length+1.  That
                            //is, if the provider ONLY returns the first few that
                            //passes (i.e. say an array of [1,1] is returned) then
                            //length is 2, and since _paramBatch starts at 0, we
                            //don't want to use length+1 as that will give us the
                            //wrong index.
                            if (indexOfFirstFailedObject == -1){
                                indexOfFirstFailedObject = count.length;
                            }

                            //Finally, whatever the index is at this point, add batchedRowsBaseIndex
                            //to it to get the final index.  Recall, we need to start our index from the
                            //last batch which successfully executed.
                            indexOfFirstFailedObject += batchedRowsBaseIndex;

                            // set the current params to the saved values
                            if (indexOfFirstFailedObject < _paramBatch.size())
                                _params = (List<String>) _paramBatch.get(indexOfFirstFailedObject);
                        }
                    }
                    err = wrap(se, LoggingPreparedStatement.this, indexOfFirstFailedObject);
                    throw err;
                } finally {
                    logTime(start);
                    handleSQLErrors(LoggingPreparedStatement.this, err);
                }
            }

            public boolean execute() throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.execute();
                } catch (SQLException se) {
                    err = wrap(se, LoggingPreparedStatement.this);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingPreparedStatement.this, err);
                }
            }

            public int executeUpdate(String s, int i) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.executeUpdate(s, i);
                } catch (SQLException se) {
                    err = wrap(se, LoggingPreparedStatement.this);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingPreparedStatement.this, err);
                }
            }

            public int executeUpdate(String s, int[] ia) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.executeUpdate(s, ia);
                } catch (SQLException se) {
                    err = wrap(se, LoggingPreparedStatement.this);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingPreparedStatement.this, err);
                }
            }

            public int executeUpdate(String s, String[] sa) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.executeUpdate(s, sa);
                } catch (SQLException se) {
                    err = wrap(se, LoggingPreparedStatement.this);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingPreparedStatement.this, err);
                }
            }

            public boolean execute(String s, int i) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.execute(s, i);
                } catch (SQLException se) {
                    err = wrap(se, LoggingPreparedStatement.this);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingPreparedStatement.this, err);
                }
            }

            public boolean execute(String s, int[] ia) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.execute(s, ia);
                } catch (SQLException se) {
                    err = wrap(se, LoggingPreparedStatement.this);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingPreparedStatement.this, err);
                }
            }

            public boolean execute(String s, String[] sa) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.execute(s, sa);
                } catch (SQLException se) {
                    err = wrap(se, LoggingPreparedStatement.this);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingPreparedStatement.this, err);
                }
            }

            public void cancel() throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("cancel " + this + ": " + _sql,
                        LoggingConnection.this);

                super.cancel();
            }

            public void setNull(int i1, int i2) throws SQLException {
                setLogParameter(i1, "null", null);
                super.setNull(i1, i2);
            }

            public void setBoolean(int i, boolean b) throws SQLException {
                setLogParameter(i, b);
                super.setBoolean(i, b);
            }

            public void setByte(int i, byte b) throws SQLException {
                setLogParameter(i, b);
                super.setByte(i, b);
            }

            public void setShort(int i, short s) throws SQLException {
                setLogParameter(i, s);
                super.setShort(i, s);
            }

            public void setInt(int i1, int i2) throws SQLException {
                setLogParameter(i1, i2);
                super.setInt(i1, i2);
            }

            public void setLong(int i, long l) throws SQLException {
                setLogParameter(i, l);
                super.setLong(i, l);
            }

            public void setFloat(int i, float f) throws SQLException {
                setLogParameter(i, f);
                super.setFloat(i, f);
            }

            public void setDouble(int i, double d) throws SQLException {
                setLogParameter(i, d);
                super.setDouble(i, d);
            }

            public void setBigDecimal(int i, BigDecimal bd)
                throws SQLException {
                setLogParameter(i, "BigDecimal", bd);
                super.setBigDecimal(i, bd);
            }

            public void setString(int i, String s) throws SQLException {
                setLogParameter(i, "String", s);
                super.setString(i, s);
            }

            public void setBytes(int i, byte[] b) throws SQLException {
                setLogParameter(i, "byte[]", b);
                super.setBytes(i, b);
            }

            public void setDate(int i, Date d) throws SQLException {
                setLogParameter(i, "Date", d);
                super.setDate(i, d);
            }

            public void setTime(int i, Time t) throws SQLException {
                setLogParameter(i, "Time", t);
                super.setTime(i, t);
            }

            public void setTimestamp(int i, Timestamp t) throws SQLException {
                setLogParameter(i, "Timestamp", t);
                super.setTimestamp(i, t);
            }

            public void setAsciiStream(int i1, InputStream is, int i2)
                throws SQLException {
                setLogParameter(i1, "InputStream", is);
                super.setAsciiStream(i1, is, i2);
            }

            @Deprecated
            public void setUnicodeStream(int i1, InputStream is, int i2)
                throws SQLException {
                setLogParameter(i1, "InputStream", is);
                super.setUnicodeStream(i2, is, i2);
            }

            public void setBinaryStream(int i1, InputStream is, int i2)
                throws SQLException {
                setLogParameter(i1, "InputStream", is);
                super.setBinaryStream(i1, is, i2);
            }
            
            public void setBinaryStream(int i1, InputStream is)
            throws SQLException {
            	setLogParameter(i1, "InputStream", is);
            	super.setBinaryStream(i1, is);            	
            }
            
            public void clearParameters() throws SQLException {
                clearLogParameters(false);
                super.clearParameters();
            }

            public void setObject(int i1, Object o, int i2, int i3)
                throws SQLException {
                setLogParameter(i1, "Object", o);
                super.setObject(i1, o, i2, i3);
            }

            public void setObject(int i1, Object o, int i2)
                throws SQLException {
                setLogParameter(i1, "Object", o);
                super.setObject(i1, o, i2);
            }

            public void setObject(int i, Object o) throws SQLException {
                setLogParameter(i, "Object", o);
                super.setObject(i, o);
            }

            public void addBatch() throws SQLException {
                if (_logs.isSQLEnabled())
                    _logs.logSQL("batching " + this, LoggingConnection.this);
                long start = System.currentTimeMillis();
                try {
                    super.addBatch();
                    if (shouldTrackParameters()) {
                        // make sure our list is initialized
                        if (_paramBatch == null)
                            _paramBatch = new ArrayList<List<String>>();
                        // copy parameters since they will be re-used
                        if (_params != null) {
                            List<String> copyParms =
                                    new ArrayList<String>(_params);
                            _paramBatch.add(copyParms);
                        }
                        else
                            _paramBatch.add(null);
                    }
                }
                finally {
                    logTime(start);
                }
            }

            public void setCharacterStream(int i1, Reader r, int i2)
                throws SQLException {
                setLogParameter(i1, "Reader", r);
                super.setCharacterStream(i1, r, i2);
            }

            public void setRef(int i, Ref r) throws SQLException {
                setLogParameter(i, "Ref", r);
                super.setRef(i, r);
            }

            public void setBlob(int i, Blob b) throws SQLException {
                setLogParameter(i, "Blob", b);
                super.setBlob(i, b);
            }

            public void setClob(int i, Clob c) throws SQLException {
                setLogParameter(i, "Clob", c);
                super.setClob(i, c);
            }

            public void setArray(int i, Array a) throws SQLException {
                setLogParameter(i, "Array", a);
                super.setArray(i, a);
            }

            public ResultSetMetaData getMetaData() throws SQLException {
                return super.getMetaData();
            }

            public void setDate(int i, Date d, Calendar c) throws SQLException {
                setLogParameter(i, "Date", d);
                super.setDate(i, d, c);
            }

            public void setTime(int i, Time t, Calendar c) throws SQLException {
                setLogParameter(i, "Time", t);
                super.setTime(i, t, c);
            }

            public void setTimestamp(int i, Timestamp t, Calendar c)
                throws SQLException {
                setLogParameter(i, "Timestamp", t);
                super.setTimestamp(i, t, c);
            }

            public void setNull(int i1, int i2, String s) throws SQLException {
                setLogParameter(i1, "null", null);
                super.setNull(i1, i2, s);
            }

            public void setURL(int i, URL u) throws SQLException {
                setLogParameter(i, "URL", u);
                super.setURL(i, u);
            }

            protected void appendInfo(StringBuffer buf) {
                buf.append(" ");
                if (_formatter != null) {
                    buf.append(SEP);
                    buf.append(_formatter.prettyPrint(_sql));
                    buf.append(SEP);
                } else {
                    buf.append(_sql);
                }

                StringBuilder paramBuf = null;
                if (_params != null && !_params.isEmpty()) {
                    paramBuf = new StringBuilder();
                    for (Iterator<String> itr = _params.iterator(); itr
                        .hasNext();) {
                        if (_printParameters) {
                            paramBuf.append(itr.next());
                        } else {
                            paramBuf.append("?");
                            itr.next();
                        }
                        if (itr.hasNext()) {
                            paramBuf.append(", ");
                        }
                    }
                }

                if (paramBuf != null) {
                    if (!_prettyPrint) {
                        buf.append(" ");
                    }
                    buf.append("[params=").append(paramBuf.toString()).append("]");
                }
                super.appendInfo(buf);
            }

            private void clearLogParameters(boolean batch) {
                if (_params != null) {
                    _params.clear();
                }

                if (batch && _paramBatch != null) {
                    _paramBatch.clear();
                }
            }

            private boolean shouldTrackParameters() {
                return _printParameters || _logs.isSQLEnabled();
            }

            private void setLogParameter(int index, boolean val) {
                if (shouldTrackParameters())
                    setLogParameter(index, "(boolean) " + val);
            }

            private void setLogParameter(int index, byte val) {
                if (shouldTrackParameters())
                    setLogParameter(index, "(byte) " + val);
            }

            private void setLogParameter(int index, double val) {
                if (shouldTrackParameters())
                    setLogParameter(index, "(double) " + val);
            }

            private void setLogParameter(int index, float val) {
                if (shouldTrackParameters())
                    setLogParameter(index, "(float) " + val);
            }

            private void setLogParameter(int index, int val) {
                if (shouldTrackParameters())
                    setLogParameter(index, "(int) " + val);
            }

            private void setLogParameter(int index, long val) {
                if (shouldTrackParameters())
                    setLogParameter(index, "(long) " + val);
            }

            private void setLogParameter(int index, short val) {
                if (shouldTrackParameters())
                    setLogParameter(index, "(short) " + val);
            }

            private void setLogParameter(int index, String type, Object val) {
                if (shouldTrackParameters())
                    setLogParameter(index, "(" + type + ") " + val);
            }

            private void setLogParameter(int index, String val) {
                if (_params == null)
                    _params = new ArrayList<String>();
                while (_params.size() < index)
                    _params.add(null);
                if (val.length() > 80)
                    val = val.substring(0, 77) + "...";
                _params.set(index - 1, val);
            }
        }

        /**
         * Warning-handling result set.
         */
        protected class LoggingResultSet extends DelegatingResultSet {

            public LoggingResultSet(ResultSet rs, Statement stmnt) {
                super(rs, stmnt);
            }

            public boolean next() throws SQLException {
                SQLException err = null;
                try {
                    return super.next();
                } catch (SQLException se) {
                    err = se;
                    throw se;
                } finally {
                    handleSQLErrors(LoggingResultSet.this, err);
                }
            }

            public void close() throws SQLException {
                SQLException err = null;            	
                try {
                    super.close();
                } catch (SQLException se) {
                    err = se;
                    throw se;
                } finally {
                    handleSQLErrors(LoggingResultSet.this, err);
                }
            }

            public void beforeFirst() throws SQLException {
                SQLException err = null;            	
                try {
                    super.beforeFirst();
                } catch (SQLException se) {
                    err = se;
                    throw se;
                } finally {
                    handleSQLErrors(LoggingResultSet.this, err);
                }
            }

            public void afterLast() throws SQLException {
                SQLException err = null;            	
                try {
                    super.afterLast();
                } catch (SQLException se) {
                    err = se;
                    throw se;
                } finally {
                    handleSQLErrors(LoggingResultSet.this, err);
                }
            }

            public boolean first() throws SQLException {
                SQLException err = null;            	
                try {
                    return super.first();
                } catch (SQLException se) {
                    err = se;
                    throw se;
                } finally {
                    handleSQLErrors(LoggingResultSet.this, err);
                }
            }

            public boolean last() throws SQLException {
                SQLException err = null;            	
                try {
                    return super.last();
                } catch (SQLException se) {
                    err = se;
                    throw se;
                } finally {
                    handleSQLErrors(LoggingResultSet.this, err);
                }
            }

            public boolean absolute(int a) throws SQLException {
                SQLException err = null;            	
                try {
                    return super.absolute(a);
                } catch (SQLException se) {
                    err = se;
                    throw se;
                } finally {
                    handleSQLErrors(LoggingResultSet.this, err);
                }
            }

            public boolean relative(int a) throws SQLException {
                SQLException err = null;            	
                try {
                    return super.relative(a);
                } catch (SQLException se) {
                    err = se;
                    throw se;
                } finally {
                    handleSQLErrors(LoggingResultSet.this, err);
                }
            }

            public boolean previous() throws SQLException {
                SQLException err = null;            	
                try {
                    return super.previous();
                } catch (SQLException se) {
                    err = se;
                    throw se;
                } finally {
                    handleSQLErrors(LoggingResultSet.this, err);
                }
            }
        }
        
        /**
         * CallableStatement decorated with logging.
         * Similar to {@link LoggingPreparedStatement} but can not be extended
         * due to the existing delegation hierarchy.
         */
        protected class LoggingCallableStatement extends 
            DelegatingCallableStatement {
            private final String _sql;
            private List<String> _params = null;
            private List<List<String>> _paramBatch = null;
            //When batching is used, this variable contains the index into the last
            //successfully executed batched statement.
            int batchedRowsBaseIndex = 0;            

            public LoggingCallableStatement(CallableStatement stmt, String sql) 
                throws SQLException {
        		super(stmt, LoggingConnection.this);
        		_sql = sql;
        	}
        	
            private LoggingResultSet newLoggingResultSet(ResultSet rs,
                CallableStatement stmnt) {
                return new LoggingResultSet(rs, stmnt);
            }
            
            protected ResultSet wrapResult(ResultSet rs, boolean wrap) {
                if (!wrap || rs == null)
                    return super.wrapResult(wrap, rs);
                return newLoggingResultSet(rs, this);
            }

            protected ResultSet executeQuery(String sql, boolean wrap)
                throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.executeQuery(sql, wrap);
                } catch (SQLException se) {
                    err = wrap(se, LoggingCallableStatement.this, sql);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingCallableStatement.this, err);
                }
            }

            public int executeUpdate(String sql) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.executeUpdate(sql);
                } catch (SQLException se) {
                    err = wrap(se, LoggingCallableStatement.this, sql);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingCallableStatement.this, err);
                }
            }

            public boolean execute(String sql) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.execute(sql);
                } catch (SQLException se) {
                    err = wrap(se, LoggingCallableStatement.this, sql);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingCallableStatement.this, err);
                }
            }

            protected ResultSet executeQuery(boolean wrap) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;
                try {
                    return super.executeQuery(wrap);
                } catch (SQLException se) {
                    err = wrap(se, LoggingCallableStatement.this);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingCallableStatement.this, err);
                }
            }

            public int executeUpdate() throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;                
                try {
                    return super.executeUpdate();
                } catch (SQLException se) {
                    err = wrap(se, LoggingCallableStatement.this);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingCallableStatement.this, err);
                }
            }

            public int[] executeBatch() throws SQLException {
                int indexOfFirstFailedObject = -1;

                logBatchSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;                
                try {
                    int[] toReturn = super.executeBatch();
                    //executeBatch is called any time the number of batched statements
                    //is equal to, or less than, batchLimit.  In the 'catch' block below, 
                    //the logic seeks to find an index based on the current executeBatch 
                    //results.  This is fine when executeBatch is only called once, but 
                    //if executeBatch is called many times, the _paramsBatch will continue 
                    //to grow, as such, to index into _paramsBatch, we need to take into 
                    //account the number of times executeBatch is called in order to 
                    //correctly index into _paramsBatch.  To that end, each time executeBatch 
                    //is called, lets get the size of _paramBatch.  This will effectively 
                    //tell us the index of the last successfully executed batch statement.  
                    //If an exception is caused, then we know that _paramBatch.size was 
                    //the index of the LAST row to successfully execute.
                    if (_paramBatch != null){
                        batchedRowsBaseIndex = _paramBatch.size();                        
                    }
                    return toReturn; 
                } catch (SQLException se) {
                    // if the exception is a BatchUpdateException, and
                    // we are tracking parameters, then set the current
                    // parameter set to be the index of the failed
                    // statement so that the ReportingSQLException will
                    // show the correct param
                    if (se instanceof BatchUpdateException
                        && _paramBatch != null && shouldTrackParameters()) {
                        int[] count = ((BatchUpdateException) se).
                            getUpdateCounts();
                        if (count != null && count.length <= _paramBatch.size())
                        {
                            for (int i = 0; i < count.length; i++) {
                                // -3 is Statement.STATEMENT_FAILED, but is
                                // only available in JDK 1.4+
                                if (count[i] == Statement.EXECUTE_FAILED) {
                                    indexOfFirstFailedObject = i;
                                    break;
                                }
                            }

                            // no -3 element: it may be that the server stopped
                            // processing, so the size of the count will be
                            // the index
                            //See the Javadoc for 'getUpdateCounts'; a provider 
                            //may stop processing when the first failure occurs, 
                            //as such, it may only return 'UpdateCounts' for the 
                            //first few which pass.  As such, the failed 
                            //index is 'count.length', NOT count.length+1.  That
                            //is, if the provider ONLY returns the first few that 
                            //passes (i.e. say an array of [1,1] is returned) then
                            //length is 2, and since _paramBatch starts at 0, we 
                            //don't want to use length+1 as that will give us the 
                            //wrong index.
                            if (indexOfFirstFailedObject == -1){
                                indexOfFirstFailedObject = count.length;
                            }
                            
                            //Finally, whatever the index is at this point, add batchedRowsBaseIndex
                            //to it to get the final index.  Recall, we need to start our index from the
                            //last batch which successfully executed.
                            indexOfFirstFailedObject += batchedRowsBaseIndex;

                            // set the current params to the saved values
                            if (indexOfFirstFailedObject < _paramBatch.size()){
                                _params = (List<String>) _paramBatch.get(indexOfFirstFailedObject);
                            }
                        }
                    }
                    err = wrap(se, LoggingCallableStatement.this, indexOfFirstFailedObject);
                    throw err;
                } finally {
                    logTime(start);
                    handleSQLErrors(LoggingCallableStatement.this, err);
                }
            }

            public boolean execute() throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;                
                try {
                    return super.execute();
                } catch (SQLException se) {
                    err = wrap(se, LoggingCallableStatement.this);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingCallableStatement.this, err);
                }
            }

            public int executeUpdate(String s, int i) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;                
                try {
                    return super.executeUpdate(s, i);
                } catch (SQLException se) {
                    err = wrap(se, LoggingCallableStatement.this);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingCallableStatement.this, err);
                }
            }

            public int executeUpdate(String s, int[] ia) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;                
                try {
                    return super.executeUpdate(s, ia);
                } catch (SQLException se) {
                    err = wrap(se, LoggingCallableStatement.this);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingCallableStatement.this, err);
                }
            }

            public int executeUpdate(String s, String[] sa) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;                
                try {
                    return super.executeUpdate(s, sa);
                } catch (SQLException se) {
                    err = wrap(se, LoggingCallableStatement.this);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingCallableStatement.this, err);
                }
            }

            public boolean execute(String s, int i) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;                
                try {
                    return super.execute(s, i);
                } catch (SQLException se) {
                    err = wrap(se, LoggingCallableStatement.this);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingCallableStatement.this, err);
                }
            }

            public boolean execute(String s, int[] ia) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;                
                try {
                    return super.execute(s, ia);
                } catch (SQLException se) {
                    err = wrap(se, LoggingCallableStatement.this);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingCallableStatement.this, err);
                }
            }

            public boolean execute(String s, String[] sa) throws SQLException {
                logSQL(this);
                long start = System.currentTimeMillis();
                SQLException err = null;                
                try {
                    return super.execute(s, sa);
                } catch (SQLException se) {
                    err = wrap(se, LoggingCallableStatement.this);
                    throw err;
                } finally {
                    logTime(start);
                    clearLogParameters(true);
                    handleSQLErrors(LoggingCallableStatement.this, err);
                }
            }

            public void cancel() throws SQLException {
                if (_logs.isJDBCEnabled())
                    _logs.logJDBC("cancel " + this + ": " + _sql,
                        LoggingConnection.this);

                super.cancel();
            }

            public void setNull(int i1, int i2) throws SQLException {
                setLogParameter(i1, "null", null);
                super.setNull(i1, i2);
            }

            public void setBoolean(int i, boolean b) throws SQLException {
                setLogParameter(i, b);
                super.setBoolean(i, b);
            }

            public void setByte(int i, byte b) throws SQLException {
                setLogParameter(i, b);
                super.setByte(i, b);
            }

            public void setShort(int i, short s) throws SQLException {
                setLogParameter(i, s);
                super.setShort(i, s);
            }

            public void setInt(int i1, int i2) throws SQLException {
                setLogParameter(i1, i2);
                super.setInt(i1, i2);
            }

            public void setLong(int i, long l) throws SQLException {
                setLogParameter(i, l);
                super.setLong(i, l);
            }

            public void setFloat(int i, float f) throws SQLException {
                setLogParameter(i, f);
                super.setFloat(i, f);
            }

            public void setDouble(int i, double d) throws SQLException {
                setLogParameter(i, d);
                super.setDouble(i, d);
            }

            public void setBigDecimal(int i, BigDecimal bd)
                throws SQLException {
                setLogParameter(i, "BigDecimal", bd);
                super.setBigDecimal(i, bd);
            }

            public void setString(int i, String s) throws SQLException {
                setLogParameter(i, "String", s);
                super.setString(i, s);
            }

            public void setBytes(int i, byte[] b) throws SQLException {
                setLogParameter(i, "byte[]", b);
                super.setBytes(i, b);
            }

            public void setDate(int i, Date d) throws SQLException {
                setLogParameter(i, "Date", d);
                super.setDate(i, d);
            }

            public void setTime(int i, Time t) throws SQLException {
                setLogParameter(i, "Time", t);
                super.setTime(i, t);
            }

            public void setTimestamp(int i, Timestamp t) throws SQLException {
                setLogParameter(i, "Timestamp", t);
                super.setTimestamp(i, t);
            }

            public void setAsciiStream(int i1, InputStream is, int i2)
                throws SQLException {
                setLogParameter(i1, "InputStream", is);
                super.setAsciiStream(i1, is, i2);
            }

            @Deprecated
            public void setUnicodeStream(int i1, InputStream is, int i2)
                throws SQLException {
                setLogParameter(i1, "InputStream", is);
                super.setUnicodeStream(i2, is, i2);
            }

            public void setBinaryStream(int i1, InputStream is, int i2)
                throws SQLException {
                setLogParameter(i1, "InputStream", is);
                super.setBinaryStream(i1, is, i2);
            }

            public void clearParameters() throws SQLException {
                clearLogParameters(false);
                super.clearParameters();
            }

            public void setObject(int i1, Object o, int i2, int i3)
                throws SQLException {
                setLogParameter(i1, "Object", o);
                super.setObject(i1, o, i2, i3);
            }

            public void setObject(int i1, Object o, int i2)
                throws SQLException {
                setLogParameter(i1, "Object", o);
                super.setObject(i1, o, i2);
            }

            public void setObject(int i, Object o) throws SQLException {
                setLogParameter(i, "Object", o);
                super.setObject(i, o);
            }

            public void addBatch() throws SQLException {
                if (_logs.isSQLEnabled())
                    _logs.logSQL("batching " + this, LoggingConnection.this);
                long start = System.currentTimeMillis();
                try {
                    super.addBatch();
                    if (shouldTrackParameters()) {
                        // make sure our list is initialized
                        if (_paramBatch == null)
                            _paramBatch = new ArrayList<List<String>>();
                        // copy parameters since they will be re-used
                        if (_params != null) {
                            List<String> copyParams =
                                new ArrayList<String>(_params);
                            _paramBatch.add(copyParams);
                        }
                        else
                            _paramBatch.add(null);
                    }
                }
                finally {
                    logTime(start);
                }
            }

            public void setCharacterStream(int i1, Reader r, int i2)
                throws SQLException {
                setLogParameter(i1, "Reader", r);
                super.setCharacterStream(i1, r, i2);
            }

            public void setRef(int i, Ref r) throws SQLException {
                setLogParameter(i, "Ref", r);
                super.setRef(i, r);
            }

            public void setBlob(int i, Blob b) throws SQLException {
                setLogParameter(i, "Blob", b);
                super.setBlob(i, b);
            }

            public void setClob(int i, Clob c) throws SQLException {
                setLogParameter(i, "Clob", c);
                super.setClob(i, c);
            }

            public void setArray(int i, Array a) throws SQLException {
                setLogParameter(i, "Array", a);
                super.setArray(i, a);
            }

            public ResultSetMetaData getMetaData() throws SQLException {
                return super.getMetaData();
            }

            public void setDate(int i, Date d, Calendar c) throws SQLException {
                setLogParameter(i, "Date", d);
                super.setDate(i, d, c);
            }

            public void setTime(int i, Time t, Calendar c) throws SQLException {
                setLogParameter(i, "Time", t);
                super.setTime(i, t, c);
            }

            public void setTimestamp(int i, Timestamp t, Calendar c)
                throws SQLException {
                setLogParameter(i, "Timestamp", t);
                super.setTimestamp(i, t, c);
            }

            public void setNull(int i1, int i2, String s) throws SQLException {
                setLogParameter(i1, "null", null);
                super.setNull(i1, i2, s);
            }

            public void setURL(int i, URL u) throws SQLException {
                setLogParameter(i, "URL", u);
                super.setURL(i, u);
            }

            protected void appendInfo(StringBuffer buf) {
                buf.append(" ");
                if (_formatter != null) {
                    buf.append(SEP);
                    buf.append(_formatter.prettyPrint(_sql));
                    buf.append(SEP);
                } else {
                    buf.append(_sql);
                }

                StringBuilder paramBuf = null;
                if (_params != null && !_params.isEmpty()) {
                    paramBuf = new StringBuilder();
                    for (Iterator<String> itr = _params.iterator(); itr
                        .hasNext();) {
                        paramBuf.append(itr.next());
                        if (itr.hasNext())
                            paramBuf.append(", ");
                    }
                }

                if (paramBuf != null) {
                    if (!_prettyPrint)
                        buf.append(" ");
                    buf.append("[params=").
                        append(paramBuf.toString()).append("]");
                }
                super.appendInfo(buf);
            }

            protected void clearLogParameters(boolean batch) {
                if (_params != null)
                    _params.clear();
                if (batch && _paramBatch != null)
                    _paramBatch.clear();
            }

            private boolean shouldTrackParameters() {
                return _printParameters || _logs.isSQLEnabled();
            }

            private void setLogParameter(int index, boolean val) {
                if (shouldTrackParameters())
                    setLogParameter(index, "(boolean) " + val);
            }

            private void setLogParameter(int index, byte val) {
                if (shouldTrackParameters())
                    setLogParameter(index, "(byte) " + val);
            }

            private void setLogParameter(int index, double val) {
                if (shouldTrackParameters())
                    setLogParameter(index, "(double) " + val);
            }

            private void setLogParameter(int index, float val) {
                if (shouldTrackParameters())
                    setLogParameter(index, "(float) " + val);
            }

            private void setLogParameter(int index, int val) {
                if (shouldTrackParameters())
                    setLogParameter(index, "(int) " + val);
            }

            private void setLogParameter(int index, long val) {
                if (shouldTrackParameters())
                    setLogParameter(index, "(long) " + val);
            }

            private void setLogParameter(int index, short val) {
                if (shouldTrackParameters())
                    setLogParameter(index, "(short) " + val);
            }

            private void setLogParameter(int index, String type, Object val) {
                if (shouldTrackParameters())
                    setLogParameter(index, "(" + type + ") " + val);
            }

            private void setLogParameter(int index, String val) {
                if (_params == null)
                    _params = new ArrayList<String>();
                while (_params.size() < index)
                    _params.add(null);
                if (val.length() > 80)
                    val = val.substring(0, 77) + "...";
                _params.set(index - 1, val);
            }
        }
    }   
}
