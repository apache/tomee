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
package org.apache.openjpa.jdbc.kernel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.jdbc.sql.RowImpl;
import org.apache.openjpa.jdbc.sql.SQLExceptions;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StateManagerImpl;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ValueStrategies;
import org.apache.openjpa.util.ApplicationIds;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.OptimisticException;

/**
 * Basic prepared statement manager implementation.
 *
 * @author Abe White
 */
public class PreparedStatementManagerImpl 
    implements PreparedStatementManager {

    private final static Localizer _loc = Localizer
        .forPackage(PreparedStatementManagerImpl.class);

    protected final JDBCStore _store;
    protected final Connection _conn;
    protected final DBDictionary _dict;
    protected transient Log _log = null;

    // track exceptions
    protected final Collection<Exception> _exceptions = new LinkedList<Exception>();

    /**
     * Constructor. Supply connection.
     */
    public PreparedStatementManagerImpl(JDBCStore store, Connection conn) {
        _store = store;
        _dict = store.getDBDictionary();
        _conn = conn;
        if (store.getConfiguration() != null)
            _log = store.getConfiguration().getLog(JDBCConfiguration.LOG_JDBC);
    }

    public Collection<Exception> getExceptions() {
        return _exceptions;
    }

    public void flush(RowImpl row) {
        try {
            if (!row.isFlushed())
                flushInternal(row);
        } catch (SQLException se) {
            _exceptions.add(SQLExceptions.getStore(se, _dict));
        } catch (OpenJPAException ke) {
            _exceptions.add(ke);
        }
    }

    /**
     * Flush the given row.
     */
    protected void flushInternal(RowImpl row) throws SQLException {
        flushAndUpdate(row);
    }

    /**
     * Flush the given row immediately. 
     */
    protected void flushAndUpdate(RowImpl row)
    throws SQLException {
        Column[] autoAssign = getAutoAssignColumns(row);
        String[] autoAssignColNames = getAutoAssignColNames(autoAssign, row);

        // prepare statement
        String sql = row.getSQL(_dict);
        PreparedStatement stmnt = prepareStatement(sql, autoAssignColNames);

        // setup parameters and execute statement
        if (stmnt != null)
            row.flush(stmnt, _dict, _store);
        try {
            int count = executeUpdate(stmnt, sql, row);
            if (count != 1) {
                logSQLWarnings(stmnt);
                Object failed = row.getFailedObject();
                if (failed != null)
                    _exceptions.add(new OptimisticException(failed));
                else if (row.getAction() == Row.ACTION_INSERT)
                    throw new SQLException(_loc.get(
                        "update-failed-no-failed-obj", String.valueOf(count),
                        sql).getMessage());
            }
            if (autoAssignColNames != null)
                populateAutoAssignCols(stmnt, autoAssign, autoAssignColNames,
                    row);
            else {
                StateManagerImpl sm = (StateManagerImpl)row.getPrimaryKey();
                if (sm != null) {
                    ClassMapping meta = (ClassMapping)sm.getMetaData();
                    if (hasGeneratedKey(meta)) {
                        sm.setObjectId(ApplicationIds.create(
                            sm.getPersistenceCapable(), meta));
                    }
                }
            }
        } catch (SQLException se) {
            throw SQLExceptions.getStore(se, row.getFailedObject(), _dict);
        } finally {
            if (stmnt != null) {
                try {
                    stmnt.close();
                } catch (SQLException se) {
                }
            }
        }
    }
    
    private boolean hasGeneratedKey(ClassMapping meta) {
        FieldMapping[] pks = meta.getPrimaryKeyFieldMappings();
        for (int i = 0; i < pks.length; i++) {
            ClassMapping pkMeta = pks[i].getTypeMapping(); 
            if (pkMeta != null) {
                return hasGeneratedKey(pkMeta);
            } else if (pks[i].getValueStrategy() == ValueStrategies.AUTOASSIGN)
                return true;
        }
        return false;
    }

    /** 
     * This method will only be called when there is auto assign columns.
     * If database supports getGeneratedKeys, the keys will be obtained
     * from the result set associated with the stmnt. If not, a separate 
     * sql to select the key will be issued from DBDictionary. 
     */
    protected List<Object> populateAutoAssignCols(PreparedStatement stmnt, 
        Column[] autoAssign, DBIdentifier[] autoAssignColNames, RowImpl row) 
        throws SQLException {
        List<Object> vals = null;
        if (_dict.supportsGetGeneratedKeys) {
            // set auto assign values to id col
            vals = getGeneratedKeys(stmnt, autoAssignColNames);
        }
        setObjectId(vals, autoAssign, autoAssignColNames, row);
        return vals;
    }

    protected List<Object> populateAutoAssignCols(PreparedStatement stmnt, 
        Column[] autoAssign, String[] autoAssignColNames, RowImpl row) 
        throws SQLException {
        return populateAutoAssignCols(stmnt, autoAssign, 
            DBIdentifier.toArray(autoAssignColNames, DBIdentifierType.COLUMN), row);
    }
    
    protected void setObjectId(List vals, Column[] autoAssign,
        String[] autoAssignColNames, RowImpl row) 
        throws SQLException{
        setObjectId(vals, autoAssign, DBIdentifier.toArray(autoAssignColNames, DBIdentifierType.COLUMN), row);
    }
    
    protected void setObjectId(List vals, Column[] autoAssign,
        DBIdentifier[] autoAssignColNames, RowImpl row) 
        throws SQLException{
        OpenJPAStateManager sm = row.getPrimaryKey();
        ClassMapping mapping = (ClassMapping) sm.getMetaData();
        Object val = null;
        for (int i = 0; i < autoAssign.length; i++) {
            if (_dict.supportsGetGeneratedKeys && vals != null && 
                vals.size() > 0)
                val = vals.get(i);
            else
                val = _dict.getGeneratedKey(autoAssign[i], _conn);
            mapping.assertJoinable(autoAssign[i]).setAutoAssignedValue(sm,
                _store, autoAssign[i], val);
        }
        sm.setObjectId(
            ApplicationIds.create(sm.getPersistenceCapable(), mapping));
    }

    /**
     * This method will only be called when the database supports
     * getGeneratedKeys.
     */
    protected List<Object> getGeneratedKeys(PreparedStatement stmnt, 
        String[] autoAssignColNames) 
        throws SQLException {
        return getGeneratedKeys(stmnt, DBIdentifier.toArray(autoAssignColNames, DBIdentifierType.COLUMN));
    }

    protected List<Object> getGeneratedKeys(PreparedStatement stmnt, 
        DBIdentifier[] autoAssignColNames) 
        throws SQLException {
        ResultSet rs = stmnt.getGeneratedKeys();
        List<Object> vals = new ArrayList<Object>();
        while (rs.next()) {
            for (int i = 0; i < autoAssignColNames.length; i++)
                vals.add(rs.getObject(i + 1));
        }
        rs.close();
        return vals;
    }

    protected Column[] getAutoAssignColumns(RowImpl row) {
        Column[] autoAssign = null;
        if (row.getAction() == Row.ACTION_INSERT)
            autoAssign = row.getTable().getAutoAssignedColumns();
        return autoAssign;
    }

    protected String[] getAutoAssignColNames(Column[] autoAssign, RowImpl row) {
        String[] autoAssignColNames = null;
        if (autoAssign != null && autoAssign.length > 0
            && row.getPrimaryKey() != null) {
            autoAssignColNames = new String[autoAssign.length];
            for (int i = 0; i < autoAssign.length; i++)
                autoAssignColNames[i] =
                    _dict.convertSchemaCase(autoAssign[i].getIdentifier());
        }
        return autoAssignColNames;
    }

    public void flush() {
    }
    
    /**
     * This method is to provide override for non-JDBC or JDBC-like 
     * implementation of executing update.
     */
    protected int executeUpdate(PreparedStatement stmnt, String sql, 
        RowImpl row) throws SQLException {
        return stmnt.executeUpdate();
    }

    /**
     * This method is to provide override for non-JDBC or JDBC-like 
     * implementation of preparing statement.
     */
    protected PreparedStatement prepareStatement(String sql) 
        throws SQLException {
        return prepareStatement(sql, null);
    }    
    /**
     * This method is to provide override for non-JDBC or JDBC-like 
     * implementation of preparing statement.
     */
    protected PreparedStatement prepareStatement(String sql, 
        String[] autoAssignColNames)
        throws SQLException {
        // pass in AutoAssignColumn names
        if (autoAssignColNames != null && _dict.supportsGetGeneratedKeys) 
            return _conn.prepareStatement(sql, autoAssignColNames);
        else
            return _conn.prepareStatement(sql);
    }
    
    /**
     * Provided the JDBC log category is logging warnings, this method will 
     * log any SQL warnings that result from the execution of a SQL statement. 
     */
    protected void logSQLWarnings(PreparedStatement stmt) {
        logSQLWarnings((Statement)stmt);
    }

    protected void logSQLWarnings(Statement stmt) {
        if (stmt != null && _log != null && _log.isTraceEnabled()) {
            try {
                SQLWarning warn = stmt.getWarnings();
                while (warn != null) {
                    _log.trace(_loc.get("sql-warning", warn.getMessage()));
                    warn = warn.getNextWarning();
                }
            } catch (SQLException e) {}
        }
    }
}
