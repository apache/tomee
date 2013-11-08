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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.JavaSQLTypes;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.meta.JavaTypes;


/**
 * Base {@link Result} implementation wrapped around a result set.
 * Column objects, column names, or column indexes (as <code>Number</code>
 * instances) can be used to retrieve result set data.
 *
 * @author Abe White
 */
public class ResultSetResult
    extends AbstractResult {

    private final Connection _conn;
    private final Statement _stmnt;
    private final ResultSet _rs;
    private final DBDictionary _dict;
    private boolean _closeConn = true;
    private int _row = -1;
    private int _size = -1;

    // optional; used to deserialize blobs containing refs to persistent objs
    private JDBCStore _store = null;

    /**
     * Constructor.
     */
    public ResultSetResult(Connection conn, Statement stmnt,
        ResultSet rs, DBDictionary dict) {
        if (stmnt == null)
            try {
                stmnt = rs.getStatement();
            } catch (Throwable t) {
            }

        _conn = conn;
        _stmnt = stmnt;
        _rs = rs;
        _dict = dict;
    }

    /**
     * Constructor.
     */
    public ResultSetResult(Connection conn, Statement stmnt,
        ResultSet rs, JDBCStore store) {
        this(conn, stmnt, rs, store.getDBDictionary());
        setStore(store);
    }

    /**
     * Constructor.
     */
    public ResultSetResult(Connection conn,
        ResultSet rs, DBDictionary dict) {
        _conn = conn;
        _stmnt = null;
        _rs = rs;
        _dict = dict;
    }

    /**
     * JDBC 2 constructor. Relies on being able to retrieve the statement
     * from the result set, and the connection from the statement.
     */
    public ResultSetResult(ResultSet rs, DBDictionary dict)
        throws SQLException {
        _stmnt = rs.getStatement();
        _conn = _stmnt.getConnection();
        _rs = rs;
        _dict = dict;
    }

    /**
     * JDBC 2 constructor. Relies on being able to retrieve the statement
     * from the result set, and the connection from the statement.
     */
    public ResultSetResult(ResultSet rs, JDBCStore store)
        throws SQLException {
        this(rs, store.getDBDictionary());
        setStore(store);
    }

    /**
     * Return the statement that produced this result.
     */
    public Statement getStatement() {
        return _stmnt;
    }

    /**
     * Return the backing result set.
     */
    public ResultSet getResultSet() {
        return _rs;
    }

    /**
     * Return the dictionary in use.
     */
    public DBDictionary getDBDictionary() {
        return _dict;
    }

    /**
     * Optional store manager used to deserialize blobs containing
     * references to persistent objects.
     */
    public JDBCStore getStore() {
        return _store;
    }

    /**
     * Optional store manager used to deserialize blobs containing
     * references to persistent objects.
     */
    public void setStore(JDBCStore store) {
        _store = store;
    }

    /**
     * Whether to close the backing connection when this result is closed.
     * Defaults to true.
     */
    public boolean getCloseConnection() {
        return _closeConn;
    }

    /**
     * Whether to close the backing connection when this result is closed.
     * Defaults to true.
     */
    public void setCloseConnection(boolean closeConn) {
        _closeConn = closeConn;
    }

    public void close() {
        super.close();
        try {
            _rs.close();
        } catch (SQLException se) {
        }
        if (_stmnt != null)
            try {
                _stmnt.close();
            } catch (SQLException se) {
            }
        if (_closeConn)
            try {
                _conn.close();
            } catch (SQLException se) {
            }
    }

    public boolean supportsRandomAccess()
        throws SQLException {
        return _rs.getType() != ResultSet.TYPE_FORWARD_ONLY;
    }

    protected boolean absoluteInternal(int row)
        throws SQLException {
        if (row == ++_row)
            return _rs.next();

        // random access
        _rs.absolute(row + 1);
        if (_rs.getRow() == 0) {
            _row = -1;
            return false;
        }
        _row = row;
        return true;
    }

    protected boolean nextInternal()
        throws SQLException {
        _row++;
        return _rs.next();
    }

    public int size()
        throws SQLException {
        if (_size == -1) {
            _rs.last();
            _size = _rs.getRow();
            if (_row == -1)
                _rs.beforeFirst();
            else
                _rs.absolute(_row + 1);
        }
        return _size;
    }

    protected boolean containsInternal(Object obj, Joins joins)
        throws SQLException {
        return ((Number) translate(obj, joins)).intValue() > 0;
    }

    protected Array getArrayInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getArray(_rs, ((Number) obj).intValue());
    }

    protected InputStream getAsciiStreamInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getAsciiStream(_rs, ((Number) obj).intValue());
    }

    protected BigDecimal getBigDecimalInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getBigDecimal(_rs, ((Number) obj).intValue());
    }

    protected Number getNumberInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getNumber(_rs, ((Number) obj).intValue());
    }

    protected BigInteger getBigIntegerInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getBigInteger(_rs, ((Number) obj).intValue());
    }

    protected InputStream getBinaryStreamInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getBinaryStream(_rs, ((Number) obj).intValue());
    }

    protected Blob getBlobInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getBlob(_rs, ((Number) obj).intValue());
    }

    protected boolean getBooleanInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getBoolean(_rs, ((Number) obj).intValue());
    }

    protected byte getByteInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getByte(_rs, ((Number) obj).intValue());
    }

    protected byte[] getBytesInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getBytes(_rs, ((Number) obj).intValue());
    }

    protected Calendar getCalendarInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getCalendar(_rs, ((Number) obj).intValue());
    }

    protected char getCharInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getChar(_rs, ((Number) obj).intValue());
    }

    protected Reader getCharacterStreamInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getCharacterStream(_rs, ((Number) obj).intValue());
    }

    protected Clob getClobInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getClob(_rs, ((Number) obj).intValue());
    }

    protected Date getDateInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getDate(_rs, ((Number) obj).intValue());
    }

    protected java.sql.Date getDateInternal(Object obj, Calendar cal,
        Joins joins)
        throws SQLException {
        return _dict.getDate(_rs, ((Number) obj).intValue(), cal);
    }

    protected double getDoubleInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getDouble(_rs, ((Number) obj).intValue());
    }

    protected float getFloatInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getFloat(_rs, ((Number) obj).intValue());
    }

    protected int getIntInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getInt(_rs, ((Number) obj).intValue());
    }

    protected Locale getLocaleInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getLocale(_rs, ((Number) obj).intValue());
    }

    protected long getLongInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getLong(_rs, ((Number) obj).intValue());
    }

    protected Object getStreamInternal(JDBCStore store, Object obj,
        int metaTypeCode, Object arg, Joins joins) throws SQLException {
        return getLOBStreamInternal(store, obj, joins);
    }
    
    protected Object getObjectInternal(Object obj, int metaTypeCode,
        Object arg, Joins joins)
        throws SQLException {
        if (metaTypeCode == -1 && obj instanceof Column)
            metaTypeCode = ((Column) obj).getJavaType();

        boolean isClob = (obj instanceof Column) ? ((Column) obj).getType() == Types.CLOB && !((Column) obj).isXML()
                : false;
        obj = translate(obj, joins);
        
        Object val = null;
        switch (metaTypeCode) {
            case JavaTypes.BOOLEAN:
            case JavaTypes.BOOLEAN_OBJ:
                val = (getBooleanInternal(obj, joins)) ? Boolean.TRUE
                    : Boolean.FALSE;
                break;
            case JavaTypes.BYTE:
            case JavaTypes.BYTE_OBJ:
                val = new Byte(getByteInternal(obj, joins));
                break;
            case JavaTypes.CHAR:
            case JavaTypes.CHAR_OBJ:
                val = new Character(getCharInternal(obj, joins));
                break;
            case JavaTypes.DOUBLE:
            case JavaTypes.DOUBLE_OBJ:
                val = new Double(getDoubleInternal(obj, joins));
                break;
            case JavaTypes.FLOAT:
            case JavaTypes.FLOAT_OBJ:
                val = new Float(getFloatInternal(obj, joins));
                break;
            case JavaTypes.INT:
            case JavaTypes.INT_OBJ:
                val = getIntInternal(obj, joins);
                break;
            case JavaTypes.LONG:
            case JavaTypes.LONG_OBJ:
                val = getLongInternal(obj, joins);
                break;
            case JavaTypes.SHORT:
            case JavaTypes.SHORT_OBJ:
                val = new Short(getShortInternal(obj, joins));
                break;
            case JavaTypes.STRING:
                return getStringInternal(obj, joins, isClob);
            case JavaTypes.OBJECT:
                return _dict
                    .getBlobObject(_rs, ((Number) obj).intValue(), _store);
            case JavaTypes.DATE:
                return getDateInternal(obj, joins);
            case JavaTypes.CALENDAR:
                return getCalendarInternal(obj, joins);
            case JavaTypes.BIGDECIMAL:
                return getBigDecimalInternal(obj, joins);
            case JavaTypes.NUMBER:
                return getNumberInternal(obj, joins);
            case JavaTypes.BIGINTEGER:
                return getBigIntegerInternal(obj, joins);
            case JavaTypes.LOCALE:
                return getLocaleInternal(obj, joins);
            case JavaSQLTypes.SQL_ARRAY:
                return getArrayInternal(obj, joins);
            case JavaSQLTypes.ASCII_STREAM:
                return getAsciiStreamInternal(obj, joins);
            case JavaSQLTypes.BINARY_STREAM:
                return getBinaryStreamInternal(obj, joins);
            case JavaSQLTypes.BLOB:
                return getBlobInternal(obj, joins);
            case JavaSQLTypes.BYTES:
                return getBytesInternal(obj, joins);
            case JavaSQLTypes.CHAR_STREAM:
                return getCharacterStreamInternal(obj, joins);
            case JavaSQLTypes.CLOB:
                return getClobInternal(obj, joins);
            case JavaSQLTypes.SQL_DATE:
                return getDateInternal(obj, (Calendar) arg, joins);
            case JavaSQLTypes.SQL_OBJECT:
                return getSQLObjectInternal(obj, (Map) arg, joins);
            case JavaSQLTypes.REF:
                return getRefInternal(obj, (Map) arg, joins);
            case JavaSQLTypes.TIME:
                return getTimeInternal(obj, (Calendar) arg, joins);
            case JavaSQLTypes.TIMESTAMP:
                return getTimestampInternal(obj, (Calendar) arg, joins);
            default:
                if (obj instanceof Column) {
                    Column col = (Column) obj;
                    if (col.getType() == Types.BLOB
                        || col.getType() == Types.VARBINARY) {
                        return _dict
                            .getBlobObject(_rs, col.getIndex(), _store);
                    }
                }
                return _dict.getObject(_rs, ((Number) obj).intValue(), null);
        }
        return (_rs.wasNull()) ? null : val;
    }

    protected Object getSQLObjectInternal(Object obj, Map map, Joins joins)
        throws SQLException {
        return _dict.getObject(_rs, ((Number) obj).intValue(), map);
    }

    protected Ref getRefInternal(Object obj, Map map, Joins joins)
        throws SQLException {
        return _dict.getRef(_rs, ((Number) obj).intValue(), map);
    }

    protected short getShortInternal(Object obj, Joins joins)
        throws SQLException {
        return _dict.getShort(_rs, ((Number) obj).intValue());
    }

    protected String getStringInternal(Object obj, Joins joins, boolean isClobString)
        throws SQLException {
        if (isClobString) {
            return _dict.getClobString(_rs, ((Number) obj).intValue());
        }
        return _dict.getString(_rs, ((Number) obj).intValue());
    }

    protected Time getTimeInternal(Object obj, Calendar cal, Joins joins)
        throws SQLException {
        return _dict.getTime(_rs, ((Number) obj).intValue(), cal);
    }

    protected Timestamp getTimestampInternal(Object obj, Calendar cal,
        Joins joins)
        throws SQLException {
        return _dict.getTimestamp(_rs, ((Number) obj).intValue(), cal);
    }

    public boolean wasNull()
        throws SQLException {
        return _rs.wasNull();
    }

    protected Object translate(Object obj, Joins joins)
        throws SQLException {
        if (obj instanceof Number)
            return obj;
        return findObject(obj, joins);
    }

    /**
     * Return the 1-based result set index for the given column or id, or a
     * non-positive number if the column is not contained in this result.
     */
    protected int findObject(Object obj, Joins joins)
        throws SQLException {
        try {
          String s1 = obj.toString();
          DBIdentifier sName = DBIdentifier.newColumn(obj.toString());
          return getResultSet().findColumn(_dict.convertSchemaCase(sName));
        } catch (SQLException se) {
            _dict.log.trace(se.getMessage());
            return 0;
        }
    }
  
    protected InputStream getLOBStreamInternal(JDBCStore store, Object obj,
        Joins joins) throws SQLException {
        return _dict.getLOBStream(store, _rs, ((Number) obj).intValue());
    }
}
