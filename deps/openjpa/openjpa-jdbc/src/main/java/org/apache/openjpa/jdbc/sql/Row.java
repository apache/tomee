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
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.openjpa.jdbc.meta.RelationId;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * Logical representation of a table row for insert/update/delete. The
 * {@link org.apache.openjpa.jdbc.kernel.UpdateManager} is responsible for
 * implementing rows to do something useful when the values are set.
 *
 * @author Abe White
 */
public interface Row {

    /**
     * Symbolic constant reserved for situations when a row operation
     * is unknown.
     */
    public static final int ACTION_UNKNOWN = -1;

    /**
     * Mark the row for update.
     */
    public static final int ACTION_UPDATE = 0;

    /**
     * Mark the row for inserttion.
     */
    public static final int ACTION_INSERT = 1;

    /**
     * Mark the row for deletion.
     */
    public static final int ACTION_DELETE = 2;

    /**
     * Return the table for this row.
     */
    public Table getTable();

    /**
     * Return the action for this row.
     */
    public int getAction();

    /**
     * Return the failed object to include in optimistic lock exceptions.
     */
    public Object getFailedObject();

    /**
     * Set the failed object to include in the optimistic lock exception
     * that will be thrown if this update results in an update count of 0
     * when executed. Leave null to avoid checking the update count.
     */
    public void setFailedObject(Object failed);

    /**
     * Whether this row has information set on it.
     */
    public boolean isValid();

    /**
     * Whether this row has information set on it.
     */
    public void setValid(boolean valid);

    /**
     * Return the instance that controls this row. The
     * {@link #setPrimaryKey} method does not necessarily have to be called
     * to know the owning instance, nor does this row's table have to have
     * an actual primary key.
     */
    public OpenJPAStateManager getPrimaryKey();

    /**
     * Set the primary key to represent the given object.
     */
    public void setPrimaryKey(OpenJPAStateManager sm)
        throws SQLException;

    /**
     * Set the primary key to represent the given object.
     *
     * @param io information on which columns are settable; may be null
     */
    public void setPrimaryKey(ColumnIO io, OpenJPAStateManager sm)
        throws SQLException;

    /**
     * Set the primary key equality criteria for this row.
     */
    public void wherePrimaryKey(OpenJPAStateManager sm)
        throws SQLException;

    /**
     * Set the value of the given foreign key to the given object.
     * If the related type uses table-per-class mappings, the foreign key may
     * be targeted at an independent superclass table.
     */
    public void setForeignKey(ForeignKey fk, OpenJPAStateManager sm)
        throws SQLException;

    /**
     * Set the value of the given foreign key to the given object.
     * If the related type uses table-per-class mappings, the foreign key may
     * be targeted at an independent superclass table.
     *
     * @param io information on which columns are settable; may be null
     */
    public void setForeignKey(ForeignKey fk, ColumnIO io,
        OpenJPAStateManager sm)
        throws SQLException;

    /**
     * Set the foreign key equality criteria to link to the given object.
     * If the related type uses table-per-class mappings, the foreign key may
     * be targeted at an independent superclass table.
     */
    public void whereForeignKey(ForeignKey fk, OpenJPAStateManager sm)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setArray(Column col, Array val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setAsciiStream(Column col, InputStream val, int length)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setBigDecimal(Column col, BigDecimal val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setBigInteger(Column col, BigInteger val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setBinaryStream(Column col, InputStream val, int length)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setBlob(Column col, Blob val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setBoolean(Column col, boolean val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setByte(Column col, byte val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setBytes(Column col, byte[] val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setCalendar(Column col, Calendar val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setChar(Column col, char val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setCharacterStream(Column col, Reader val, int length)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setClob(Column col, Clob val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setDate(Column col, Date val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setDate(Column col, java.sql.Date val, Calendar cal)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setDouble(Column col, double val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setFloat(Column col, float val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setInt(Column col, int val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setLong(Column col, long val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setLocale(Column col, Locale val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setNull(Column col)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     *
     * @param overrideDefault whether to set this column to null even if this
     * is an insert and the column has a default
     */
    public void setNull(Column col, boolean overrideDefault)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setNumber(Column col, Number val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     *
     * @param col the column being set
     * @param val the value for the column
     */
    public void setObject(Column col, Object val)
        throws SQLException;

    /**
     * Set a DB understood value for the given column.
     * The value will not be parameterized and instead be inserted as raw SQL.
     */
    public void setRaw(Column col, String value)
        throws SQLException;

    /**
     * Set the value of the given column to the identity of given instance,
     * using the given callback to create the column value. This method is
     * used for mappings that store some serialized form of id values, but must
     * make sure that the related object's id is assigned (which might
     * require an insert if the instance uses auto-increment) before it is
     * serialized.
     */
    public void setRelationId(Column col, OpenJPAStateManager sm,
        RelationId rel)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setShort(Column col, short val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setString(Column col, String val)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setTime(Column col, Time val, Calendar cal)
        throws SQLException;

    /**
     * Set the value of the given column in this row.
     */
    public void setTimestamp(Column col, Timestamp val, Calendar cal)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereArray(Column col, Array val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereAsciiStream(Column col, InputStream val, int length)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereBigDecimal(Column col, BigDecimal val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereBigInteger(Column col, BigInteger val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereBinaryStream(Column col, InputStream val, int length)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereBlob(Column col, Blob val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereBoolean(Column col, boolean val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereByte(Column col, byte val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereBytes(Column col, byte[] val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereCalendar(Column col, Calendar val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereChar(Column col, char val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereCharacterStream(Column col, Reader val, int length)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereClob(Column col, Clob val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereDate(Column col, Date val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereDate(Column col, java.sql.Date val, Calendar cal)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereDouble(Column col, double val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereFloat(Column col, float val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereInt(Column col, int val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereLong(Column col, long val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereLocale(Column col, Locale val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereNull(Column col)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereNumber(Column col, Number val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     *
     * @param col the column being set
     * @param val the value for the column
     */
    public void whereObject(Column col, Object val)
        throws SQLException;

    /**
     * Set a DB understood where condition for the given column.
     * The value will not be parameterized and instead be inserted as raw SQL.
     */
    public void whereRaw(Column col, String value)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereShort(Column col, short val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereString(Column col, String val)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereTime(Column col, Time val, Calendar cal)
        throws SQLException;

    /**
     * Set an equality condition on the value of the given column in this row.
     */
    public void whereTimestamp(Column col, Timestamp val, Calendar cal)
        throws SQLException;
}
