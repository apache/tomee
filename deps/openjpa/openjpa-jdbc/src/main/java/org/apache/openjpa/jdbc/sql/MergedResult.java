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
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.util.UnsupportedException;

/**
 * Result that merges multiple result delegates. Support exists for
 * maintaining ordering of the internally-held results, provided that each
 * of the individual results is itself ordered.
 *
 * @author Abe White
 */
public class MergedResult
    implements Result {

    private static final byte NEXT = 0;
    private static final byte CURRENT = 1;
    private static final byte DONE = 2;

    private final Result[] _res;
    private final byte[] _status;
    private final ResultComparator _comp;
    private final Object[] _order;
    private int _idx = 0;
    private boolean _pushedBack = false;

    /**
     * Constructor; supply delegates.
     */
    public MergedResult(Result[] res) {
        this(res, null);
    }

    /**
     * Constructor; supply delegates and comparator for ordering results.
     */
    public MergedResult(Result[] res, ResultComparator comp) {
        _res = res;
        _comp = comp;
        _order = (comp == null) ? null : new Object[res.length];
        _status = (comp == null) ? null : new byte[res.length];
    }

    public Object getEager(FieldMapping key) {
        return _res[_idx].getEager(key);
    }

    public void putEager(FieldMapping key, Object res) {
        _res[_idx].putEager(key, res);
    }

    public Joins newJoins() {
        return _res[_idx].newJoins();
    }

    public void close() {
        for (int i = 0; i < _res.length; i++)
            _res[i].close();
    }

    public void setLocking(boolean locking) {
        _res[_idx].setLocking(locking);
    }

    public boolean isLocking() {
        return _res[_idx].isLocking();
    }

    public boolean supportsRandomAccess()
        throws SQLException {
        return false;
    }

    public boolean absolute(int row)
        throws SQLException {
        throw new UnsupportedException();
    }

    public boolean next()
        throws SQLException {
        if (_pushedBack) {
            _pushedBack = false;
            return true;
        }

        if (_comp == null) {
            while (!_res[_idx].next()) {
                if (_idx == _res.length - 1)
                    return false;
                _idx++;
            }
            return true;
        }

        // ordering is involved; extract order values from each result
        boolean hasValue = false;
        for (int i = 0; i < _status.length; i++) {
            switch (_status[i]) {
                case NEXT:
                    if (_res[i].next()) {
                        hasValue = true;
                        _status[i] = CURRENT;
                        _order[i] = _comp.getOrderingValue(_res[i], i);
                    } else
                        _status[i] = DONE;
                    break;
                case CURRENT:
                    hasValue = true;
                    break;
            }
        }

        // all results exhausted
        if (!hasValue)
            return false;

        // for all results with values, find the 'least' one according to
        // the comparator
        int least = -1;
        Object orderVal = null;
        for (int i = 0; i < _order.length; i++) {
            if (_status[i] != CURRENT)
                continue;
            if (least == -1 || _comp.compare(_order[i], orderVal) < 0) {
                least = i;
                orderVal = _order[i];
            }
        }

        // make the current result the one with the least value, and clear
        // the cached value for that result
        _idx = least;
        _order[least] = null;
        _status[least] = NEXT;
        return true;
    }

    public void pushBack()
        throws SQLException {
        _pushedBack = true;
    }

    public int size()
        throws SQLException {
        int size = 0;
        for (int i = 0; i < _res.length; i++)
            size += _res[i].size();
        return size;
    }

    public boolean contains(Object obj)
        throws SQLException {
        return _res[_idx].contains(obj);
    }

    public boolean containsAll(Object[] objs)
        throws SQLException {
        return _res[_idx].containsAll(objs);
    }

    public boolean contains(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].contains(col, joins);
    }

    public boolean containsAll(Column[] cols, Joins joins)
        throws SQLException {
        return _res[_idx].containsAll(cols, joins);
    }

    public ClassMapping getBaseMapping() {
        return _res[_idx].getBaseMapping();
    }

    public void setBaseMapping(ClassMapping mapping) {
        _res[_idx].setBaseMapping(mapping);
    }

    public FieldMapping getMappedByFieldMapping() {
        return _res[_idx].getMappedByFieldMapping();
    }

    public void setMappedByFieldMapping(FieldMapping fieldMapping) {
        _res[_idx].setMappedByFieldMapping(fieldMapping);
    }

    public Object getMappedByValue() {
        return _res[_idx].getMappedByValue();
    }

    public void setMappedByValue(Object mappedByValue) {
        _res[_idx].setMappedByValue(mappedByValue);
    }

    public int indexOf() {
        return _res[_idx].indexOf();
    }

    public Object load(ClassMapping mapping, JDBCStore store,
        JDBCFetchConfiguration fetch)
        throws SQLException {
        return _res[_idx].load(mapping, store, fetch);
    }

    public Object load(ClassMapping mapping, JDBCStore store,
        JDBCFetchConfiguration fetch, Joins joins)
        throws SQLException {
        return _res[_idx].load(mapping, store, fetch, joins);
    }

    public Array getArray(Object obj)
        throws SQLException {
        return _res[_idx].getArray(obj);
    }

    public InputStream getAsciiStream(Object obj)
        throws SQLException {
        return _res[_idx].getAsciiStream(obj);
    }

    public BigDecimal getBigDecimal(Object obj)
        throws SQLException {
        return _res[_idx].getBigDecimal(obj);
    }

    public BigInteger getBigInteger(Object obj)
        throws SQLException {
        return _res[_idx].getBigInteger(obj);
    }

    public InputStream getBinaryStream(Object obj)
        throws SQLException {
        return _res[_idx].getBinaryStream(obj);
    }

    public InputStream getLOBStream(JDBCStore store, Object obj)
        throws SQLException {
        return _res[_idx].getLOBStream(store, obj);
    }
    
    public Blob getBlob(Object obj)
        throws SQLException {
        return _res[_idx].getBlob(obj);
    }

    public boolean getBoolean(Object obj)
        throws SQLException {
        return _res[_idx].getBoolean(obj);
    }

    public byte getByte(Object obj)
        throws SQLException {
        return _res[_idx].getByte(obj);
    }

    public byte[] getBytes(Object obj)
        throws SQLException {
        return _res[_idx].getBytes(obj);
    }

    public Calendar getCalendar(Object obj)
        throws SQLException {
        return _res[_idx].getCalendar(obj);
    }

    public char getChar(Object obj)
        throws SQLException {
        return _res[_idx].getChar(obj);
    }

    public Reader getCharacterStream(Object obj)
        throws SQLException {
        return _res[_idx].getCharacterStream(obj);
    }

    public Clob getClob(Object obj)
        throws SQLException {
        return _res[_idx].getClob(obj);
    }

    public Date getDate(Object obj)
        throws SQLException {
        return _res[_idx].getDate(obj);
    }

    public java.sql.Date getDate(Object obj, Calendar cal)
        throws SQLException {
        return _res[_idx].getDate(obj, cal);
    }

    public double getDouble(Object obj)
        throws SQLException {
        return _res[_idx].getDouble(obj);
    }

    public float getFloat(Object obj)
        throws SQLException {
        return _res[_idx].getFloat(obj);
    }

    public int getInt(Object obj)
        throws SQLException {
        return _res[_idx].getInt(obj);
    }

    public Locale getLocale(Object obj)
        throws SQLException {
        return _res[_idx].getLocale(obj);
    }

    public long getLong(Object obj)
        throws SQLException {
        return _res[_idx].getLong(obj);
    }

    public Number getNumber(Object obj)
        throws SQLException {
        return _res[_idx].getNumber(obj);
    }

    public Object getObject(Object obj, int metaType, Object arg)
        throws SQLException {
        return _res[_idx].getObject(obj, metaType, arg);
    }

    public Object getSQLObject(Object obj, Map map)
        throws SQLException {
        return _res[_idx].getSQLObject(obj, map);
    }

    public Ref getRef(Object obj, Map map)
        throws SQLException {
        return _res[_idx].getRef(obj, map);
    }

    public short getShort(Object obj)
        throws SQLException {
        return _res[_idx].getShort(obj);
    }

    public String getString(Object obj)
        throws SQLException {
        return _res[_idx].getString(obj);
    }

    public Time getTime(Object obj, Calendar cal)
        throws SQLException {
        return _res[_idx].getTime(obj, cal);
    }

    public Timestamp getTimestamp(Object obj, Calendar cal)
        throws SQLException {
        return _res[_idx].getTimestamp(obj, cal);
    }

    public Array getArray(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getArray(col, joins);
    }

    public InputStream getAsciiStream(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getAsciiStream(col, joins);
    }

    public BigDecimal getBigDecimal(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getBigDecimal(col, joins);
    }

    public BigInteger getBigInteger(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getBigInteger(col, joins);
    }

    public InputStream getBinaryStream(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getBinaryStream(col, joins);
    }

    public Blob getBlob(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getBlob(col, joins);
    }

    public boolean getBoolean(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getBoolean(col, joins);
    }

    public byte getByte(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getByte(col, joins);
    }

    public byte[] getBytes(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getBytes(col, joins);
    }

    public Calendar getCalendar(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getCalendar(col, joins);
    }

    public char getChar(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getChar(col, joins);
    }

    public Reader getCharacterStream(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getCharacterStream(col, joins);
    }

    public Clob getClob(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getClob(col, joins);
    }

    public Date getDate(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getDate(col, joins);
    }

    public java.sql.Date getDate(Column col, Calendar cal, Joins joins)
        throws SQLException {
        return _res[_idx].getDate(col, cal, joins);
    }

    public double getDouble(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getDouble(col, joins);
    }

    public float getFloat(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getFloat(col, joins);
    }

    public int getInt(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getInt(col, joins);
    }

    public Locale getLocale(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getLocale(col, joins);
    }

    public long getLong(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getLong(col, joins);
    }

    public Number getNumber(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getNumber(col, joins);
    }

    public Object getObject(Column col, Object arg, Joins joins)
        throws SQLException {
        return _res[_idx].getObject(col, arg, joins);
    }

    public Object getSQLObject(Column col, Map map, Joins joins)
        throws SQLException {
        return _res[_idx].getSQLObject(col, map, joins);
    }

    public Ref getRef(Column col, Map map, Joins joins)
        throws SQLException {
        return _res[_idx].getRef(col, map, joins);
    }

    public short getShort(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getShort(col, joins);
    }

    public String getString(Column col, Joins joins)
        throws SQLException {
        return _res[_idx].getString(col, joins);
    }

    public Time getTime(Column col, Calendar cal, Joins joins)
        throws SQLException {
        return _res[_idx].getTime(col, cal, joins);
    }

    public Timestamp getTimestamp(Column col, Calendar cal, Joins joins)
        throws SQLException {
        return _res[_idx].getTimestamp(col, cal, joins);
    }

    public boolean wasNull()
        throws SQLException {
        return _res[_idx].wasNull();
    }

    public void startDataRequest(Object mapping) {
        for (int i = 0; i < _res.length; i++)
            _res[i].startDataRequest(mapping);
    }

    public void endDataRequest() {
        for (int i = 0; i < _res.length; i++)
            _res[i].endDataRequest();
    }

    /**
     * Comparator for ordering result rows.
     */
    public static interface ResultComparator
        extends Comparator {

        /**
         * Return the ordering value of the current row of the given result.
         */
        public Object getOrderingValue(Result res, int idx);
    }
}
