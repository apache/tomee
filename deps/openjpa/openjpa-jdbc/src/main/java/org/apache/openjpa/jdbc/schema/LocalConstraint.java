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

import java.util.ArrayList;
import java.util.List;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.InvalidStateException;

/**
 * Constraint over local table columns, as opposed to a foreign key which
 * spans tables. Column APIs can represent a full constraint or a partial
 * constraint, aligning with {@link java.sql.DatabaseMetaData}.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
public abstract class LocalConstraint
    extends Constraint {

    private static final Localizer _loc = Localizer.forPackage
        (LocalConstraint.class);

    private List<Column> _colList = null;
    private Column[] _cols = null;

    /**
     * Default constructor.
     */
    public LocalConstraint() {
    }

    /**
     * Constructor.
     *
     * @param name the name of the constraint, if any
     * @param table the table of the constraint
     * @deprecated
     */
    public LocalConstraint(String name, Table table) {
        super(name, table);
    }

    public LocalConstraint(DBIdentifier name, Table table) {
        super(name, table);
    }
/**
     * Called when the constraint is removed from its table.
     */
    void remove() {
        // remove all columns
        for (Column c : _cols) {
            c.removeConstraint(this);
        }
        setColumns(null);
        super.remove();
    }

    /**
     * Return all the columns the constraint spans.
     */
    public Column[] getColumns() {
        if (_cols == null)
            _cols = (_colList == null) ? Schemas.EMPTY_COLUMNS
                : (Column[]) _colList.toArray(new Column[_colList.size()]);
        return _cols;
    }

    /**
     * Set the columns the constraint spans.
     */
    public void setColumns(Column[] cols) {
        Column[] cur = getColumns();
        for (int i = 0; i < cur.length; i++)
            removeColumn(cur[i]);

        if (cols != null)
            for (int i = 0; i < cols.length; i++)
                addColumn(cols[i]);
    }

    /**
     * Add a column to the constraint.
     */
    public void addColumn(Column col) {
    	if (col == null)
            throw new InvalidStateException(_loc.get("table-mismatch",
                col == null ? null : col.getTable(),
                col == null ? null : getTable()));
    	
        if (_colList == null)
            _colList = new ArrayList<Column>(3);
        else if (_colList.contains(col))
            return;

        _colList.add(col);
        _cols = null;
        col.addConstraint(this);
    }

    /**
     * Remove a column from the constraint.
     *
     * @return true if the column was removed, false if not part of the
     * primary key
     */
    public boolean removeColumn(Column col) {
        if (col == null || _colList == null)
            return false;
        if (_colList.remove(col)) {
            _cols = null;
            col.removeConstraint(this);
            return true;
        }
        return false;
    }

    /**
     * Return true if the pk includes the given column.
     */
    public boolean containsColumn(Column col) {
        if (col == null || _colList == null)
            return false;
        return _colList.contains(col);
    }

    /**
     * Ref all columns in this constraint.
     */
    public void refColumns() {
        Column[] cols = getColumns();
        for (int i = 0; i < cols.length; i++)
            cols[i].ref();
    }

    /**
     * Deref all columns in this constraint.
     */
    public void derefColumns() {
        Column[] cols = getColumns();
        for (int i = 0; i < cols.length; i++)
            cols[i].deref();
    }

    /**
     * Return true if the given columns match the columns of this constraint.
     */
    public boolean columnsMatch(Column[] ocols) {
        Column[] cols = getColumns();
        if (cols.length != ocols.length)
            return false;
        for (int i = 0; i < ocols.length; i++)
            if (!hasColumn(cols, ocols[i]))
                return false;
        return true;
    }

    /**
     * Return whether the given column exists in the array.
     */
    private static boolean hasColumn(Column[] cols, Column col) {
        for (int i = 0; i < cols.length; i++)
            if (cols[i].getQualifiedPath().equals(col.getQualifiedPath()))
                return true;
        return false;
    }

    /**
     * Return true if the columns of this constraint matches that of
     * the given one. The constraints are not compared on name.
     */
    protected boolean equalsLocalConstraint(LocalConstraint lc) {
        if (lc == this)
            return true;
        if (lc == null)
            return false;
        return columnsMatch(lc.getColumns());
    }
}
