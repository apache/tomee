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
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
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
import java.util.Map;

import org.apache.openjpa.lib.util.Closeable;

/**
 * {@link CallableStatement} that delegates to an internal statement.
 *
 * @author Abe White
 */
public class DelegatingCallableStatement
    implements CallableStatement, Closeable {

    private final CallableStatement _stmnt;
    private final DelegatingCallableStatement _del;
    private final Connection _conn;

    public DelegatingCallableStatement(CallableStatement stmnt,
        Connection conn) {
        _conn = conn;
        _stmnt = stmnt;
        if (_stmnt instanceof DelegatingCallableStatement)
            _del = (DelegatingCallableStatement) _stmnt;
        else
            _del = null;
    }

    protected ResultSet wrapResult(boolean wrap, ResultSet rs) {
        if (!wrap)
            return rs;

        // never wrap null
        if (rs == null)
            return null;

        return new DelegatingResultSet(rs, this);
    }

    /**
     * Return the wrapped statement.
     */
    public CallableStatement getDelegate() {
        return _stmnt;
    }

    /**
     * Return the base underlying data store statement.
     */
    public CallableStatement getInnermostDelegate() {
        return (_del == null) ? _stmnt : _del.getInnermostDelegate();
    }

    public int hashCode() {
        return getInnermostDelegate().hashCode();
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other instanceof DelegatingCallableStatement)
            other = ((DelegatingCallableStatement) other).
                getInnermostDelegate();
        return getInnermostDelegate().equals(other);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("callstmnt ").append(hashCode());
        appendInfo(buf);
        return buf.toString();
    }

    protected void appendInfo(StringBuffer buf) {
        if (_del != null)
            _del.appendInfo(buf);
    }

    public ResultSet executeQuery(String str) throws SQLException {
        return executeQuery(true);
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

        return wrapResult(wrap, rs);
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

        return wrapResult(wrap, rs);
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

        return wrapResult(wrap, rs);
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

    /////////////////////////////
    // CallableStatement methods
    /////////////////////////////

    public void registerOutParameter(int i1, int i2) throws SQLException {
        _stmnt.registerOutParameter(i1, i2);
    }

    public void registerOutParameter(int i1, int i2, int i3)
        throws SQLException {
        _stmnt.registerOutParameter(i1, i2, i3);
    }

    public boolean wasNull() throws SQLException {
        return _stmnt.wasNull();
    }

    public String getString(int i) throws SQLException {
        return _stmnt.getString(i);
    }

    public boolean getBoolean(int i) throws SQLException {
        return _stmnt.getBoolean(i);
    }

    public byte getByte(int i) throws SQLException {
        return _stmnt.getByte(i);
    }

    public short getShort(int i) throws SQLException {
        return _stmnt.getShort(i);
    }

    public int getInt(int i) throws SQLException {
        return _stmnt.getInt(i);
    }

    public long getLong(int i) throws SQLException {
        return _stmnt.getLong(i);
    }

    public float getFloat(int i) throws SQLException {
        return _stmnt.getFloat(i);
    }

    public double getDouble(int i) throws SQLException {
        return _stmnt.getDouble(i);
    }

    /**
     * @deprecated use <code>getBigDecimal(int parameterIndex)</code> or
     *             <code>getBigDecimal(String parameterName)</code>
     */
    public BigDecimal getBigDecimal(int a, int b) throws SQLException {
        return _stmnt.getBigDecimal(a, b);
    }

    public byte[] getBytes(int i) throws SQLException {
        return _stmnt.getBytes(i);
    }

    public Date getDate(int i) throws SQLException {
        return _stmnt.getDate(i);
    }

    public Time getTime(int i) throws SQLException {
        return _stmnt.getTime(i);
    }

    public Timestamp getTimestamp(int i) throws SQLException {
        return _stmnt.getTimestamp(i);
    }

    public Object getObject(int i) throws SQLException {
        return _stmnt.getObject(i);
    }

    public BigDecimal getBigDecimal(int i) throws SQLException {
        return _stmnt.getBigDecimal(i);
    }

    public Object getObject(int i, Map<String,Class<?>> m) throws SQLException {
        return _stmnt.getObject(i, m);
    }

    public Ref getRef(int i) throws SQLException {
        return _stmnt.getRef(i);
    }

    public Blob getBlob(int i) throws SQLException {
        return _stmnt.getBlob(i);
    }

    public Clob getClob(int i) throws SQLException {
        return _stmnt.getClob(i);
    }

    public Array getArray(int i) throws SQLException {
        return _stmnt.getArray(i);
    }

    public Date getDate(int i, Calendar c) throws SQLException {
        return _stmnt.getDate(i, c);
    }

    public Time getTime(int i, Calendar c) throws SQLException {
        return _stmnt.getTime(i, c);
    }

    public Timestamp getTimestamp(int i, Calendar c) throws SQLException {
        return _stmnt.getTimestamp(i, c);
    }

    public void registerOutParameter(int i1, int i2, String s)
        throws SQLException {
        _stmnt.registerOutParameter(i1, i2, s);
    }

    // JDBC 3 methods follow.

    public void registerOutParameter(String s, int i) throws SQLException {
        _stmnt.registerOutParameter(s, i);
    }

    public void registerOutParameter(String s, int i1, int i2)
        throws SQLException {
        _stmnt.registerOutParameter(s, i1, i2);
    }

    public void registerOutParameter(String s1, int i, String s2)
        throws SQLException {
        _stmnt.registerOutParameter(s1, i, s2);
    }

    public URL getURL(int i) throws SQLException {
        return _stmnt.getURL(i);
    }

    public void setURL(String a, URL b) throws SQLException {
        _stmnt.setURL(a, b);
    }

    public URL getURL(String a) throws SQLException {
        return _stmnt.getURL(a);
    }

    public void setNull(String a, int b) throws SQLException {
        _stmnt.setNull(a, b);
    }

    public void setBoolean(String a, boolean b) throws SQLException {
        _stmnt.setBoolean(a, b);
    }

    public void setByte(String a, byte b) throws SQLException {
        _stmnt.setByte(a, b);
    }

    public void setShort(String a, short b) throws SQLException {
        _stmnt.setShort(a, b);
    }

    public void setInt(String a, int b) throws SQLException {
        _stmnt.setInt(a, b);
    }

    public void setLong(String a, long b) throws SQLException {
        _stmnt.setLong(a, b);
    }

    public void setFloat(String a, float b) throws SQLException {
        _stmnt.setFloat(a, b);
    }

    public void setDouble(String a, double b) throws SQLException {
        _stmnt.setDouble(a, b);
    }

    public void setBigDecimal(String a, BigDecimal b) throws SQLException {
        _stmnt.setBigDecimal(a, b);
    }

    public void setString(String a, String b) throws SQLException {
        _stmnt.setString(a, b);
    }

    public void setBytes(String a, byte[] b) throws SQLException {
        _stmnt.setBytes(a, b);
    }

    public void setDate(String a, Date b) throws SQLException {
        _stmnt.setDate(a, b);
    }

    public void setTime(String a, Time b) throws SQLException {
        _stmnt.setTime(a, b);
    }

    public void setTimestamp(String a, Timestamp b) throws SQLException {
        _stmnt.setTimestamp(a, b);
    }

    public void setAsciiStream(String a, InputStream b, int c)
        throws SQLException {
        _stmnt.setAsciiStream(a, b, c);
    }

    public void setBinaryStream(String a, InputStream b, int c)
        throws SQLException {
        _stmnt.setBinaryStream(a, b, c);
    }

    public void setObject(String a, Object b, int c, int d)
        throws SQLException {
        _stmnt.setObject(a, b, c, d);
    }

    public void setObject(String a, Object b, int c) throws SQLException {
        _stmnt.setObject(a, b, c);
    }

    public void setObject(String a, Object b) throws SQLException {
        _stmnt.setObject(a, b);
    }

    public void setCharacterStream(String a, Reader b, int c)
        throws SQLException {
        _stmnt.setCharacterStream(a, b, c);
    }

    public void setDate(String a, Date b, Calendar c) throws SQLException {
        _stmnt.setDate(a, b, c);
    }

    public void setTime(String a, Time b, Calendar c) throws SQLException {
        _stmnt.setTime(a, b, c);
    }

    public void setTimestamp(String a, Timestamp b, Calendar c)
        throws SQLException {
        _stmnt.setTimestamp(a, b, c);
    }

    public void setNull(String a, int b, String c) throws SQLException {
        _stmnt.setNull(a, b, c);
    }

    public String getString(String a) throws SQLException {
        return _stmnt.getString(a);
    }

    public boolean getBoolean(String a) throws SQLException {
        return _stmnt.getBoolean(a);
    }

    public byte getByte(String a) throws SQLException {
        return _stmnt.getByte(a);
    }

    public short getShort(String a) throws SQLException {
        return _stmnt.getShort(a);
    }

    public int getInt(String a) throws SQLException {
        return _stmnt.getInt(a);
    }

    public long getLong(String a) throws SQLException {
        return _stmnt.getLong(a);
    }

    public float getFloat(String a) throws SQLException {
        return _stmnt.getFloat(a);
    }

    public double getDouble(String a) throws SQLException {
        return _stmnt.getDouble(a);
    }

    public byte[] getBytes(String a) throws SQLException {
        return _stmnt.getBytes(a);
    }

    public Date getDate(String a) throws SQLException {
        return _stmnt.getDate(a);
    }

    public Time getTime(String a) throws SQLException {
        return _stmnt.getTime(a);
    }

    public Timestamp getTimestamp(String a) throws SQLException {
        return _stmnt.getTimestamp(a);
    }

    public Object getObject(String a) throws SQLException {
        return _stmnt.getObject(a);
    }

    public BigDecimal getBigDecimal(String a) throws SQLException {
        return _stmnt.getBigDecimal(a);
    }

    public Object getObject(String a, Map<String, Class<?>>b) throws
            SQLException {
        return _stmnt.getObject(a, b);
    }

    public Ref getRef(String a) throws SQLException {
        return _stmnt.getRef(a);
    }

    public Blob getBlob(String a) throws SQLException {
        return _stmnt.getBlob(a);
    }

    public Clob getClob(String a) throws SQLException {
        return _stmnt.getClob(a);
    }

    public Array getArray(String a) throws SQLException {
        return _stmnt.getArray(a);
    }

    public Date getDate(String a, Calendar b) throws SQLException {
        return _stmnt.getDate(a, b);
    }

    public Time getTime(String a, Calendar b) throws SQLException {
        return _stmnt.getTime(a, b);
    }

    public Timestamp getTimestamp(String a, Calendar b) throws SQLException {
        return _stmnt.getTimestamp(a, b);
    }

    // JDBC 4 methods follow.

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(getDelegate().getClass());
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (isWrapperFor(iface))
            return (T) getDelegate();
        else
            return null;
    }

    public Reader getCharacterStream(int parameterIndex) throws SQLException {
        return _stmnt.getCharacterStream(parameterIndex);
    }

    public Reader getCharacterStream(String parameterName) throws SQLException {
        return _stmnt.getCharacterStream(parameterName);
    }

    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        return _stmnt.getNCharacterStream(parameterIndex);
    }

    public Reader getNCharacterStream(String parameterName) throws SQLException {
        return _stmnt.getNCharacterStream(parameterName);
    }

    public NClob getNClob(int parameterIndex) throws SQLException {
        return _stmnt.getNClob(parameterIndex);
    }

    public NClob getNClob(String parameterName) throws SQLException {
        return _stmnt.getNClob(parameterName);
    }

    public String getNString(int parameterIndex) throws SQLException {
        return _stmnt.getNString(parameterIndex);
    }

    public String getNString(String parameterName) throws SQLException {
        return _stmnt.getNString(parameterName);
    }

    public RowId getRowId(int parameterIndex) throws SQLException {
        return _stmnt.getRowId(parameterIndex);
    }

    public RowId getRowId(String parameterName) throws SQLException {
        return _stmnt.getRowId(parameterName);
    }

    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        return _stmnt.getSQLXML(parameterIndex);
    }

    public SQLXML getSQLXML(String parameterName) throws SQLException {
        return _stmnt.getSQLXML(parameterName);
    }

    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
        _stmnt.setAsciiStream(parameterName, x, length);
    }

    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        _stmnt.setAsciiStream(parameterName, x);
    }

    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
        _stmnt.setBinaryStream(parameterName, x, length);
    }

    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        _stmnt.setBinaryStream(parameterName, x);
    }

    public void setBlob(String parameterName, Blob x) throws SQLException {
        _stmnt.setBlob(parameterName, x);
    }

    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
        _stmnt.setBlob(parameterName, inputStream, length);
    }

    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        _stmnt.setBlob(parameterName, inputStream);
    }

    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        _stmnt.setCharacterStream(parameterName, reader, length);
    }

    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        _stmnt.setCharacterStream(parameterName, reader);
    }

    public void setClob(String parameterName, Clob x) throws SQLException {
        _stmnt.setClob(parameterName, x);
    }

    public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        _stmnt.setClob(parameterName, reader, length);
    }

    public void setClob(String parameterName, Reader reader) throws SQLException {
        _stmnt.setClob(parameterName, reader);
    }

    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
        _stmnt.setNCharacterStream(parameterName, value, length);
    }

    public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
        _stmnt.setNCharacterStream(parameterName, value);
    }

    public void setNClob(String parameterName, NClob value) throws SQLException {
        _stmnt.setNClob(parameterName, value);
    }

    public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
        _stmnt.setNClob(parameterName, reader, length);
    }

    public void setNClob(String parameterName, Reader reader) throws SQLException {
        _stmnt.setNClob(parameterName, reader);
    }

    public void setNString(String parameterName, String value) throws SQLException {
        _stmnt.setNString(parameterName, value);
    }

    public void setRowId(String parameterName, RowId x) throws SQLException {
        _stmnt.setRowId(parameterName, x);
    }

    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
        _stmnt.setSQLXML(parameterName, xmlObject);
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
    
    public <T>T getObject(String columnLabel, Class<T> type) throws SQLException{
    	throw new UnsupportedOperationException();
    }
    
    public <T>T getObject(int columnIndex, Class<T> type) throws SQLException{
    	throw new UnsupportedOperationException();
    }
    
    public boolean isCloseOnCompletion() throws SQLException{
    	throw new UnsupportedOperationException();
    }
    
    public void closeOnCompletion() throws SQLException{
    	throw new UnsupportedOperationException();
    }
}
