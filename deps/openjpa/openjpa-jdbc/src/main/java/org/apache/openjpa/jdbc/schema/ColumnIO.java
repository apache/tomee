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
package org.apache.openjpa.jdbc.schema;

import java.io.Serializable;

/**
 * Metadata about column I/O in a specific context. In the context of
 * a foreign key, the standard foreign key columns are indexed first, then
 * the constant columns.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
public class ColumnIO
    implements Serializable {

    public static final ColumnIO UNRESTRICTED = new ColumnIO() {
        public void setInsertable(int col, boolean insertable) {
            throw new UnsupportedOperationException();
        }

        public void setUpdatable(int col, boolean updatable) {
            throw new UnsupportedOperationException();
        }

        public void setNullInsertable(int col, boolean insertable) {
            throw new UnsupportedOperationException();
        }

        public void setNullUpdatable(int col, boolean insertable) {
            throw new UnsupportedOperationException();
        }
    };

    private int _unInsertable = 0;
    private int _unUpdatable = 0;
    private int _unNullInsertable = 0;
    private int _unNullUpdatable = 0;

    /**
     * Whether the column at the given index is insertable in this context.
     */
    public boolean isInsertable(int col, boolean nullValue) {
        return is(col, _unInsertable, _unNullInsertable, nullValue);
    }

    /**
     * Equivalent to <code>isInsertable (0, nullValue)</code>, but returns
     * false if the given column is null.
     */
    public boolean isInsertable(Column col, boolean nullValue) {
        return is(col, _unInsertable, _unNullInsertable, nullValue);
    }

    /**
     * Whether any column up to but excluding the given index is insertable.
     */
    public boolean isAnyInsertable(int col, boolean nullValue) {
        return isAny(col, _unInsertable, _unNullInsertable, nullValue);
    }

    /**
     * Equivalent to <code>isAnyInsertable (cols.length, nullValue)</code>.
     */
    public boolean isAnyInsertable(Column[] cols, boolean nullValue) {
        return isAny(cols, _unInsertable, _unNullInsertable, nullValue);
    }

    /**
     * Return true if any columns for the given key are insertable.
     */
    public boolean isAnyInsertable(ForeignKey fk, boolean nullValue) {
        return isAny(fk, _unInsertable, _unNullInsertable, nullValue)
            && (!nullValue || fk.isLogical() || isNullable(fk));
    }

    /**
     * Whether all columns up to but excluding the given index are insertable.
     */
    public boolean isAllInsertable(int col, boolean nullValue) {
        return isAll(col, _unInsertable, _unNullInsertable, nullValue);
    }

    /**
     * Equivalent to <code>isAllInsertable (cols.length, nullValue)</code>.
     */
    public boolean isAllInsertable(Column[] cols, boolean nullValue) {
        return isAll(cols, _unInsertable, _unNullInsertable, nullValue);
    }

    /**
     * Return true if all columns for the given key are insertable.
     */
    public boolean isAllInsertable(ForeignKey fk, boolean nullValue) {
        return isAll(fk, _unInsertable, _unNullInsertable, nullValue)
            && (!nullValue || fk.isLogical() || isNullable(fk));
    }

    /**
     * Whether the column at the given index is insertable in this context.
     */
    public void setInsertable(int col, boolean insertable) {
        _unInsertable = set(col, insertable, _unInsertable);
    }

    /**
     * Whether this context can insert the given column as null/default in
     * this context.
     */
    public void setNullInsertable(int col, boolean insertable) {
        _unNullInsertable = set(col, insertable, _unNullInsertable);
    }

    /**
     * Whether the column at the given index is updatable in this context.
     */
    public boolean isUpdatable(int col, boolean nullValue) {
        return is(col, _unUpdatable, _unNullUpdatable, nullValue);
    }

    /**
     * Equivalent to <code>isUpdatable (0, nullValue)</code>, but returns
     * false if the given column is null.
     */
    public boolean isUpdatable(Column col, boolean nullValue) {
        return is(col, _unUpdatable, _unNullUpdatable, nullValue);
    }

    /**
     * Whether any column up to but excluding the given index is updatable.
     */
    public boolean isAnyUpdatable(int col, boolean nullValue) {
        return isAny(col, _unUpdatable, _unNullUpdatable, nullValue);
    }

    /**
     * Equivalent to <code>isAnyUpdatable (cols.length, nullValue)</code>.
     */
    public boolean isAnyUpdatable(Column[] cols, boolean nullValue) {
        return isAny(cols, _unUpdatable, _unNullUpdatable, nullValue);
    }

    /**
     * Return true if any columns for the given key are updatable.
     */
    public boolean isAnyUpdatable(ForeignKey fk, boolean nullValue) {
        return isAny(fk, _unUpdatable, _unNullUpdatable, nullValue)
            && (!nullValue || fk.isLogical() || isNullable(fk));
    }

    /**
     * Whether all columns up to but excluding the given index are updatable.
     */
    public boolean isAllUpdatable(int col, boolean nullValue) {
        return isAll(col, _unUpdatable, _unNullUpdatable, nullValue);
    }

    /**
     * Equivalent to <code>isAllUpdatable (cols.length, nullValue)</code>.
     */
    public boolean isAllUpdatable(Column[] cols, boolean nullValue) {
        return isAll(cols, _unUpdatable, _unNullUpdatable, nullValue);
    }

    /**
     * Return true if all columns for the given key are updatable.
     */
    public boolean isAllUpdatable(ForeignKey fk, boolean nullValue) {
        return isAll(fk, _unUpdatable, _unNullUpdatable, nullValue)
            && (!nullValue || fk.isLogical() || isNullable(fk));
    }

    /**
     * Whether the column at the given index is updatable in this context.
     */
    public void setUpdatable(int col, boolean updatable) {
        _unUpdatable = set(col, updatable, _unUpdatable);
    }

    /**
     * Whether this context can set the given column to null/default in
     * this context.
     */
    public void setNullUpdatable(int col, boolean updatable) {
        _unNullUpdatable = set(col, updatable, _unNullUpdatable);
    }

    /**
     * Whether the column at the given index has the given property.
     */
    private boolean is(int col, int property, int nullProperty,
        boolean nullValue) {
        return (property & (2 << col)) == 0
            && (!nullValue || (nullProperty & (2 << col)) == 0);
    }

    /**
     * Whether the column has the given property.
     */
    private boolean is(Column col, int property, int nullProperty,
        boolean nullValue) {
        return col != null && is(0, property, nullProperty, nullValue);
    }

    /**
     * Whether any column up to but excluding the given index has the given
     * property.
     */
    private boolean isAny(int col, int property, int nullProperty,
        boolean nullValue) {
        if (col == 0)
            return false;
        if (property == 0)
            return true;
        for (int i = 0; i < col; i++)
            if (is(i, property, nullProperty, nullValue))
                return true;
        return false;
    }

    /**
     * Whether any columns have the given property.
     */
    private boolean isAny(Column[] cols, int property, int nullProperty,
        boolean nullValue) {
        return isAny(cols.length, property, nullProperty, nullValue);
    }

    /**
     * Whether any columns for the given key have the property.
     */
    private boolean isAny(ForeignKey fk, int property, int nullProperty,
        boolean nullValue) {
        return fk != null && isAny(fk.getColumns().length
            + fk.getConstantColumns().length, property, nullProperty,
            nullValue);
    }

    /**
     * Whether all columns up to but excluding the given index have the given
     * property.
     */
    private boolean isAll(int col, int property, int nullProperty,
        boolean nullValue) {
        if (col == 0)
            return false;
        if (property == 0)
            return true;
        for (int i = 0; i < col; i++)
            if (!is(i, property, nullProperty, nullValue))
                return false;
        return true;
    }

    /**
     * Whether all columns have the given property.
     */
    private boolean isAll(Column[] cols, int property, int nullProperty,
        boolean nullValue) {
        return isAll(cols.length, property, nullProperty, nullValue);
    }

    /**
     * Whether all columns for the key have the given property.
     */
    private boolean isAll(ForeignKey fk, int property, int nullProperty,
        boolean nullValue) {
        return fk != null && isAll(fk.getColumns().length
            + fk.getConstantColumns().length, property, nullProperty,
            nullValue);
    }

    /**
     * Set the given property.
     */
    private int set(int col, boolean is, int property) {
        if (is)
            return property & ~(2 << col);
        return property | (2 << col);
    }

    /**
     * Whether the given foreign key is nullable.
     */
    private boolean isNullable(ForeignKey fk) {
        Column[] cols = fk.getColumns();
        for (int i = 0; i < cols.length; i++)
            if (cols[i].isNotNull() || cols[i].isPrimaryKey())
                return false;
        return true;
    }
}
