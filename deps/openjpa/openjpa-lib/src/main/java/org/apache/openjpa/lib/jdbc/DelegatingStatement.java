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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import org.apache.openjpa.lib.util.Closeable;

/**
 * Wrapper around an existing statement. Subclasses can override the
 * methods whose behavior they mean to change. The <code>equals</code> and
 * <code>hashCode</code> methods pass through to the base underlying data
 * store statement.
 *
 * @author Abe White
 */
public class DelegatingStatement implements Statement, Closeable {

    private Statement _stmnt;
    private DelegatingStatement _del;
    private Connection _conn;

    public DelegatingStatement(Statement stmnt, Connection conn) {
    	initialize(stmnt, conn);
    }
    
    public void initialize(Statement stmnt, Connection conn) {
        _conn = conn;
        _stmnt = stmnt;
        if (stmnt instanceof DelegatingStatement)
            _del = (DelegatingStatement) stmnt;
        else
            _del = null;
    }

    protected ResultSet wrapResult(ResultSet rs, boolean wrap) {
        if (!wrap || rs == null)
            return rs;
        return new DelegatingResultSet(rs, this);
    }

    /**
     * Return the wrapped statement.
     */
    public Statement getDelegate() {
        return _stmnt;
    }

    /**
     * Return the base underlying data store statement.
     */
    public Statement getInnermostDelegate() {
        return (_del == null) ? _stmnt : _del.getInnermostDelegate();
    }

    public int hashCode() {
        return getInnermostDelegate().hashCode();
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other instanceof DelegatingStatement)
            other = ((DelegatingStatement) other).getInnermostDelegate();
        return getInnermostDelegate().equals(other);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("stmnt ").append(hashCode());
        appendInfo(buf);
        return buf.toString();
    }

    protected void appendInfo(StringBuffer buf) {
        if (_del != null)
            _del.appendInfo(buf);
    }

    public ResultSet executeQuery(String str) throws SQLException {
        return executeQuery(str, true);
    }

    /**
     * Execute the query, with the option of not wrapping it in a
     * {@link DelegatingResultSet}, which is the default.
     */
    protected ResultSet executeQuery(String sql, boolean wrap)
        throws SQLException {
        ResultSet rs;
        if (_del != null)
            rs = _del.executeQuery(sql, false);
        else
            rs = _stmnt.executeQuery(sql);
        return wrapResult(rs, wrap);
    }

    public int executeUpdate(String str) throws SQLException {
        return _stmnt.executeUpdate(str);
    }

    public boolean execute(String str) throws SQLException {
        return _stmnt.execute(str);
    }

    public void close() throws SQLException {
        _stmnt.close();
    }

    public int getMaxFieldSize() throws SQLException {
        return _stmnt.getMaxFieldSize();
    }

    public void setMaxFieldSize(int i) throws SQLException {
        _stmnt.setMaxFieldSize(i);
    }

    public int getMaxRows() throws SQLException {
        return _stmnt.getMaxRows();
    }

    public void setMaxRows(int i) throws SQLException {
        _stmnt.setMaxRows(i);
    }

    public void setEscapeProcessing(boolean bool) throws SQLException {
        _stmnt.setEscapeProcessing(bool);
    }

    public int getQueryTimeout() throws SQLException {
        return _stmnt.getQueryTimeout();
    }

    public void setQueryTimeout(int i) throws SQLException {
        _stmnt.setQueryTimeout(i);
    }

    public void cancel() throws SQLException {
        _stmnt.cancel();
    }

    public SQLWarning getWarnings() throws SQLException {
        return _stmnt.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        _stmnt.clearWarnings();
    }

    public void setCursorName(String str) throws SQLException {
        _stmnt.setCursorName(str);
    }

    public ResultSet getResultSet() throws SQLException {
        return getResultSet(true);
    }

    /**
     * Get the last result set, with the option of not wrapping it in a
     * {@link DelegatingResultSet}, which is the default.
     */
    protected ResultSet getResultSet(boolean wrap) throws SQLException {
        ResultSet rs;
        if (_del != null)
            rs = _del.getResultSet(false);
        else
            rs = _stmnt.getResultSet();
        return wrapResult(rs, wrap);
    }

    public int getUpdateCount() throws SQLException {
        return _stmnt.getUpdateCount();
    }

    public boolean getMoreResults() throws SQLException {
        return _stmnt.getMoreResults();
    }

    public void setFetchDirection(int i) throws SQLException {
        _stmnt.setFetchDirection(i);
    }

    public int getFetchDirection() throws SQLException {
        return _stmnt.getFetchDirection();
    }

    public void setFetchSize(int i) throws SQLException {
        _stmnt.setFetchSize(i);
    }

    public int getFetchSize() throws SQLException {
        return _stmnt.getFetchSize();
    }

    public int getResultSetConcurrency() throws SQLException {
        return _stmnt.getResultSetConcurrency();
    }

    public int getResultSetType() throws SQLException {
        return _stmnt.getResultSetType();
    }

    public void addBatch(String str) throws SQLException {
        _stmnt.addBatch(str);
    }

    public void clearBatch() throws SQLException {
        _stmnt.clearBatch();
    }

    public int[] executeBatch() throws SQLException {
        return _stmnt.executeBatch();
    }

    public Connection getConnection() throws SQLException {
        return _conn;
    }

    // JDBC 3 methods follow.

    public boolean getMoreResults(int i) throws SQLException {
        return _stmnt.getMoreResults(i);
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return _stmnt.getGeneratedKeys();
    }

    public int executeUpdate(String s, int i) throws SQLException {
        return _stmnt.executeUpdate(s, i);
    }

    public int executeUpdate(String s, int[] ia) throws SQLException {
        return _stmnt.executeUpdate(s, ia);
    }

    public int executeUpdate(String s, String[] sa) throws SQLException {
        return _stmnt.executeUpdate(s, sa);
    }

    public boolean execute(String s, int i) throws SQLException {
        return _stmnt.execute(s, i);
    }

    public boolean execute(String s, int[] ia) throws SQLException {
        return _stmnt.execute(s, ia);
    }

    public boolean execute(String s, String[] sa) throws SQLException {
        return _stmnt.execute(s, sa);
    }

    public int getResultSetHoldability() throws SQLException {
        return _stmnt.getResultSetHoldability();
    }

    // JDBC 4 methods follow.

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(getDelegate().getClass());
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (isWrapperFor(iface))
            return (T) getDelegate();
        else
            return null;
    }

    public boolean isClosed() throws SQLException {
        return _stmnt.isClosed();
    }

    public void setPoolable(boolean b) throws SQLException {
        _stmnt.setPoolable(b);
    }

    public boolean isPoolable() throws SQLException {
        return _stmnt.isPoolable();
    }
    
    // Java 7 methods follow
    
    public boolean isCloseOnCompletion() throws SQLException{
    	throw new UnsupportedOperationException();
    }
    
    public void closeOnCompletion() throws SQLException{
    	throw new UnsupportedOperationException();
    }
}
