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
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.openjpa.lib.util.Closeable;

/**
 * Wrapper around an existing statement. Subclasses can override the
 * methods whose behavior they mean to change. The <code>equals</code> and
 * <code>hashCode</code> methods pass through to the base underlying data
 * store statement.
 *
 * @author Abe White
 */
public class DelegatingPreparedStatement
    implements PreparedStatement, Closeable {
    
    private /*final*/ PreparedStatement _stmnt;
    private /*final*/ DelegatingPreparedStatement _del;
    private /*final*/ Connection _conn;

    public DelegatingPreparedStatement(PreparedStatement stmnt, Connection conn) {
    	initialize(stmnt, conn);
    }
    
    public void initialize(PreparedStatement stmnt, Connection conn) {
        _conn = conn;
        _stmnt = stmnt;
        if (_stmnt instanceof DelegatingPreparedStatement)
            _del = (DelegatingPreparedStatement) _stmnt;
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
    public PreparedStatement getDelegate() {
        return _stmnt;
    }

    /**
     * Return the base underlying data store statement.
     */
    public PreparedStatement getInnermostDelegate() {
        return (_del == null) ? _stmnt : _del.getInnermostDelegate();
    }

    public int hashCode() {
        return getInnermostDelegate().hashCode();
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other instanceof DelegatingPreparedStatement)
            other = ((DelegatingPreparedStatement) other).
                getInnermostDelegate();
        return getInnermostDelegate().equals(other);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("prepstmnt ").append(hashCode());
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

    public ResultSet executeQuery() throws SQLException {
        return executeQuery(true);
    }

    /**
     * Execute the query, with the option of not wrapping it in a
     * {@link DelegatingResultSet}, which is the default.
     */
    protected ResultSet executeQuery(boolean wrap) throws SQLException {
        ResultSet rs;
        if (_del != null)
            rs = _del.executeQuery(false);
        else
            rs = _stmnt.executeQuery();
        return wrapResult(rs, wrap);
    }

    public int executeUpdate() throws SQLException {
        return _stmnt.executeUpdate();
    }

    public void setNull(int i1, int i2) throws SQLException {
        _stmnt.setNull(i1, i2);
    }

    public void setBoolean(int i, boolean b) throws SQLException {
        _stmnt.setBoolean(i, b);
    }

    public void setByte(int i, byte b) throws SQLException {
        _stmnt.setByte(i, b);
    }

    public void setShort(int i, short s) throws SQLException {
        _stmnt.setShort(i, s);
    }

    public void setInt(int i1, int i2) throws SQLException {
        _stmnt.setInt(i1, i2);
    }

    public void setLong(int i, long l) throws SQLException {
        _stmnt.setLong(i, l);
    }

    public void setFloat(int i, float f) throws SQLException {
        _stmnt.setFloat(i, f);
    }

    public void setDouble(int i, double d) throws SQLException {
        _stmnt.setDouble(i, d);
    }

    public void setBigDecimal(int i, BigDecimal bd) throws SQLException {
        _stmnt.setBigDecimal(i, bd);
    }

    public void setString(int i, String s) throws SQLException {
        _stmnt.setString(i, s);
    }

    public void setBytes(int i, byte[] b) throws SQLException {
        _stmnt.setBytes(i, b);
    }

    public void setDate(int i, Date d) throws SQLException {
        _stmnt.setDate(i, d);
    }

    public void setTime(int i, Time t) throws SQLException {
        _stmnt.setTime(i, t);
    }

    public void setTimestamp(int i, Timestamp t) throws SQLException {
        _stmnt.setTimestamp(i, t);
    }

    public void setAsciiStream(int i1, InputStream is, int i2)
        throws SQLException {
        _stmnt.setAsciiStream(i1, is, i2);
    }

    /**
     * @deprecated 
     */
    public void setUnicodeStream(int i1, InputStream is, int i2)
        throws SQLException {
        _stmnt.setUnicodeStream(i1, is, i2);
    }

    public void setBinaryStream(int i1, InputStream is, int i2)
        throws SQLException {
        _stmnt.setBinaryStream(i1, is, i2);
    }

    public void clearParameters() throws SQLException {
        _stmnt.clearParameters();
    }

    public void setObject(int i1, Object o, int i2, int i3)
        throws SQLException {
        _stmnt.setObject(i1, o, i2, i3);
    }

    public void setObject(int i1, Object o, int i2) throws SQLException {
        _stmnt.setObject(i1, o, i2);
    }

    public void setObject(int i, Object o) throws SQLException {
        _stmnt.setObject(i, o);
    }

    public boolean execute() throws SQLException {
        return _stmnt.execute();
    }

    public void addBatch() throws SQLException {
        _stmnt.addBatch();
    }

    public void setCharacterStream(int i1, Reader r, int i2)
        throws SQLException {
        _stmnt.setCharacterStream(i1, r, i2);
    }

    public void setRef(int i, Ref r) throws SQLException {
        _stmnt.setRef(i, r);
    }

    public void setBlob(int i, Blob b) throws SQLException {
        _stmnt.setBlob(i, b);
    }

    public void setClob(int i, Clob c) throws SQLException {
        _stmnt.setClob(i, c);
    }

    public void setArray(int i, Array a) throws SQLException {
        _stmnt.setArray(i, a);
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return _stmnt.getMetaData();
    }

    public void setDate(int i, Date d, Calendar c) throws SQLException {
        _stmnt.setDate(i, d, c);
    }

    public void setTime(int i, Time t, Calendar c) throws SQLException {
        _stmnt.setTime(i, t, c);
    }

    public void setTimestamp(int i, Timestamp t, Calendar c)
        throws SQLException {
        _stmnt.setTimestamp(i, t, c);
    }

    public void setNull(int i1, int i2, String s) throws SQLException {
        _stmnt.setNull(i1, i2, s);
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

    public void setURL(int i, URL url) throws SQLException {
        _stmnt.setURL(i, url);
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        return _stmnt.getParameterMetaData();
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

    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        _stmnt.setAsciiStream(parameterIndex, x, length);
    }

    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        _stmnt.setAsciiStream(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        _stmnt.setBinaryStream(parameterIndex, x, length);
    }

    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        _stmnt.setBinaryStream(parameterIndex, x);
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        _stmnt.setBlob(parameterIndex, inputStream, length);
    }

    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        _stmnt.setBlob(parameterIndex, inputStream);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        _stmnt.setCharacterStream(parameterIndex, reader, length);
    }

    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        _stmnt.setCharacterStream(parameterIndex, reader);
    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        _stmnt.setClob(parameterIndex, reader, length);
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        _stmnt.setClob(parameterIndex, reader);
    }

    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        _stmnt.setNCharacterStream(parameterIndex, value, length);
    }

    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        _stmnt.setNCharacterStream(parameterIndex, value);
    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        _stmnt.setNClob(parameterIndex, value);
    }

    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        _stmnt.setNClob(parameterIndex, reader, length);
    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        _stmnt.setNClob(parameterIndex, reader);
    }

    public void setNString(int parameterIndex, String value) throws SQLException {
        _stmnt.setNString(parameterIndex, value);
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        _stmnt.setRowId(parameterIndex, x);
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        _stmnt.setSQLXML(parameterIndex, xmlObject);
    }

    public boolean isClosed() throws SQLException {
        return _stmnt.isClosed();
    }

    public boolean isPoolable() throws SQLException {
        return _stmnt.isPoolable();
    }

    public void setPoolable(boolean poolable) throws SQLException {
        _stmnt.setPoolable(poolable);
    }
    
    // Java 7 methods follow
    
    public boolean isCloseOnCompletion() throws SQLException{
    	throw new UnsupportedOperationException();
    }
    
    public void closeOnCompletion() throws SQLException{
    	throw new UnsupportedOperationException();
    }
}
