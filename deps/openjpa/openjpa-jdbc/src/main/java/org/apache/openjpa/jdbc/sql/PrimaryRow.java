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

import java.sql.SQLException;

import org.apache.commons.lang.ObjectUtils;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.RelationId;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.InvalidStateException;

/**
 * Primary table row that tracks foreign keys and auto-inc columns.
 *
 * @author Abe White
 * @nojavadoc
 */
public class PrimaryRow
    extends RowImpl {

    // VALID flag in superclass uses 2 << 0
    private static final byte PK_SET = 2 << 1;
    private static final byte PK_WHERE = 2 << 2;
    private static final byte DEPENDENT = 2 << 4;

    private static final Localizer _loc = Localizer.forPackage
        (PrimaryRow.class);

    private OpenJPAStateManager _pk = null;
    private ColumnIO _pkIO = null;
    private OpenJPAStateManager[] _fkSet = null;
    private ColumnIO[] _fkIO = null;
    private OpenJPAStateManager[] _fkWhere = null;
    private OpenJPAStateManager[] _relSet = null;
    private RelationId[] _callbacks = null;
    private Object _failed = null;
    private int _idx = -1;

    /**
     * Constructor; supply table and action.
     */
    public PrimaryRow(Table table, int action, OpenJPAStateManager owner) {
        this(table.getColumns(), action, owner);
    }

    protected PrimaryRow(Column[] cols, int action, OpenJPAStateManager owner) {
        super(cols, action);
        _pk = owner;
    }

    /**
     * Mark this row as dependent on some other row.
     */
    public boolean isDependent() {
        return (flags & DEPENDENT) > 0;
    }

    /**
     * Mark this row as dependent on some other row.
     */
    public void setDependent(boolean dependent) {
        if (dependent)
            flags |= DEPENDENT;
        else
            flags &= ~DEPENDENT;
    }

    /**
     * The index of this row in ordered row list.
     */
    public int getIndex() {
        return _idx;
    }

    /**
     * The index of this row in ordered row list.
     */
    public void setIndex(int idx) {
        _idx = idx;
    }

    public Object getFailedObject() {
        return _failed;
    }

    public void setFailedObject(Object failed) {
        _failed = failed;
    }

    public OpenJPAStateManager getPrimaryKey() {
        return _pk;
    }

    public void setPrimaryKey(OpenJPAStateManager sm)
        throws SQLException {
        setPrimaryKey(null, sm);
    }

    public void setPrimaryKey(ColumnIO io, OpenJPAStateManager sm) {
        _pk = sm;
        flags |= PK_SET;
        _pkIO = io;

        // force valid
        setValid(true);
    }

    public void wherePrimaryKey(OpenJPAStateManager sm)
        throws SQLException {
        _pk = sm;
        flags |= PK_WHERE;

        // force valid
        if (getAction() == ACTION_DELETE)
            setValid(true);
    }

    /**
     * Return the I/O information for the given set foreign key.
     */
    public ColumnIO getForeignKeyIO(ForeignKey fk) {
        return (_fkIO == null) ? null : _fkIO[fk.getIndex()];
    }

    /**
     * Return the value for the given foreign key. Values not needed for
     * constraint analyses are not recorded.
     */
    public OpenJPAStateManager getForeignKeySet(ForeignKey fk) {
        return (_fkSet == null) ? null : _fkSet[fk.getIndex()];
    }

    /**
     * Return the value for the given foreign key. Values not needed for
     * constraint analyses are not recorded.
     */
    public OpenJPAStateManager getForeignKeyWhere(ForeignKey fk) {
        return (_fkWhere == null) ? null : _fkWhere[fk.getIndex()];
    }

    public void setForeignKey(ForeignKey fk, OpenJPAStateManager sm)
        throws SQLException {
        setForeignKey(fk, null, sm);
    }

    public void setForeignKey(ForeignKey fk, ColumnIO io,
        OpenJPAStateManager sm)
        throws SQLException {
        if (!delayForeignKey(fk, sm, true))
            super.setForeignKey(fk, io, sm);
        else
            recordForeignKey(fk, io, sm, true);
    }

    public void whereForeignKey(ForeignKey fk, OpenJPAStateManager sm)
        throws SQLException {
        if (!delayForeignKey(fk, sm, false))
            super.whereForeignKey(fk, sm);
        else
            recordForeignKey(fk, null, sm, false);
    }

    public void clearForeignKey(ForeignKey fk)
        throws SQLException {
        super.clearForeignKey(fk);
        if (_fkSet != null)
            _fkSet[fk.getIndex()] = null;
        if (_fkIO != null)
            _fkIO[fk.getIndex()] = null;
    }

    /**
     * If this is a delete, delay foreign keys to other deleted objects if the 
     * key is restricted or cascade. If this is an update or insert, delay 
     * foreign keys to other inserts if the key is not logical. If the foreign 
     * key is to a new record and the columns are auto-inc, record it.
     */
    private boolean delayForeignKey(ForeignKey fk, OpenJPAStateManager sm,
        boolean set) {
        if (sm == null)
            return false;

        if (getAction() == ACTION_DELETE)
            return sm.isDeleted() && !fk.isDeferred()
                && (fk.getDeleteAction() == ForeignKey.ACTION_RESTRICT ||
                    fk.getDeleteAction() == ForeignKey.ACTION_CASCADE);

        if (!sm.isNew() || sm.isFlushed())
            return false;
        if (!fk.isDeferred() && !fk.isLogical())
            return true;
        if (fk.isPrimaryKeyAutoAssigned())
            return true;
        return false;
    }

    /**
     * Record a delayed foreign key.
     */
    private void recordForeignKey(ForeignKey fk, ColumnIO io,
        OpenJPAStateManager sm, boolean set) {
        if (set) {
            // force valid
            if (canSetAny(io, fk.getColumns().length
                + fk.getConstantColumns().length, false))
                setValid(true);

            if (_fkSet == null)
                _fkSet = new OpenJPAStateManager[getTable().
                    getForeignKeys().length];
            _fkSet[fk.getIndex()] = sm;

            if (_fkIO != null)
                _fkIO[fk.getIndex()] = io;
            else if (io != null && ((getAction() == ACTION_INSERT
                && !io.isAllInsertable(fk, false))
                || (getAction() != ACTION_INSERT
                && !io.isAllUpdatable(fk, false)))) {
                _fkIO = new ColumnIO[_fkSet.length];
                _fkIO[fk.getIndex()] = io;
            }
        } else {
            // force valid
            if (getAction() == ACTION_DELETE)
                setValid(true);

            if (_fkWhere == null)
                _fkWhere = new OpenJPAStateManager[getTable().
                    getForeignKeys().length];
            _fkWhere[fk.getIndex()] = sm;
        }
    }

    /**
     * Return the recorded value for the given relation id column. Only
     * values that are dependent on a new, unflushed auto-assigned instance
     * are recorded.
     */
    public OpenJPAStateManager getRelationIdSet(Column col) {
        return (_relSet == null) ? null : _relSet[getRelationIdIndex(col)];
    }

    /**
     * Return the recorded callbacks for the given relation id column. Only
     * values that are dependent on a new, unflushed auto-assigned instance
     * are recorded.
     */
    public RelationId getRelationIdCallback(Column col) {
        return (_callbacks == null) ? null
            : _callbacks[getRelationIdIndex(col)];
    }

    public void setRelationId(Column col, OpenJPAStateManager sm,
        RelationId rel)
        throws SQLException {
        if (sm == null || sm.getObjectId() != null || !sm.isNew()
            || sm.isFlushed() || !isPrimaryKeyAutoAssigned(sm))
            super.setRelationId(col, sm, rel);
        else {
            if (_relSet == null) {
                Column[] cols = getTable().getRelationIdColumns();
                _relSet = new OpenJPAStateManager[cols.length];
                _callbacks = new RelationId[cols.length];
            }
            int idx = getRelationIdIndex(col);
            _relSet[idx] = sm;
            _callbacks[idx] = rel;
        }
    }

    public void clearRelationId(Column col)
        throws SQLException {
        super.clearRelationId(col);
        if (_relSet != null) {
            int idx = getRelationIdIndex(col);
            _relSet[idx] = null;
            _callbacks[idx] = null;
        }
    }

    /**
     * Return the index into our relation id array of the value for the
     * given column.
     */
    private int getRelationIdIndex(Column col) {
        Column[] cols = getTable().getRelationIdColumns();
        for (int i = 0; i < cols.length; i++)
            if (cols[i] == col)
                return i;
        return -1;
    }

    /**
     * Return true if any primary key columns of the given instance are
     * auto-assigned.
     */
    private static boolean isPrimaryKeyAutoAssigned(OpenJPAStateManager sm) {
        ClassMapping cls = (ClassMapping) sm.getMetaData();
        while (cls.getJoinablePCSuperclassMapping() != null)
            cls = cls.getJoinablePCSuperclassMapping();
        Column[] cols = cls.getPrimaryKeyColumns();
        for (int i = 0; i < cols.length; i++)
            if (cols[i].isAutoAssigned())
                return true;
        return false;
    }

    protected void setObject(Column col, Object val, int metaType,
        boolean overrideDefault)
        throws SQLException {
        // make sure we're not setting two different values
    	// unless the given column is an implicit relationship and value
    	// changes from logical default to non-default
        Object prev = getSet(col);
        if (prev != null) {
            if (prev == NULL)
                prev = null;
            if (!rowValueEquals(prev, val)) {
            	if (allowsUpdate(col, prev, val)) {
            		super.setObject(col, val, metaType, overrideDefault);
            	} else if (!isDefaultValue(val)) {
            		throw new InvalidStateException(_loc.get("diff-values",
            				new Object[]{ col.getFullDBIdentifier().getName(),
                            (prev == null) ? null : prev.getClass(), prev,
                            (val == null) ? null : val.getClass(), val })).
            				setFatal(true);
            	} else {
            	    // since not allow to update and the new value is 0 or null,
            	    // just return.
            	    return;
            	}
            }
        }
        super.setObject(col, val, metaType, overrideDefault);
    }
    
    /**
     * Allow the given column value to be updated only if old or current value
     * is a default value or was not set and the column is not a primary key.
     */
    boolean allowsUpdate(Column col, Object old, Object cur) {
    	return ((!col.isPrimaryKey() && col.isImplicitRelation()) ||
    	   col.isUni1MFK()) && (isDefaultValue(old));
    }
    
    boolean isDefaultValue(Object val) {
    	return val == null || val == NULL
    	    || (val instanceof Number && ((Number)val).longValue() == 0);
    }

    /**
     * Return true if the two values should be considered equal.
     */
    private static boolean rowValueEquals(Object o1, Object o2) {
        if (ObjectUtils.equals(o1, o2))
            return true;

        // check for numeric equality (bug #1151)
        return o1 instanceof Number && o2 instanceof Number
            && ((Number) o1).doubleValue() == ((Number) o2).doubleValue();
    }

    protected String generateSQL(DBDictionary dict) {
        try {
            if ((flags & PK_SET) > 0)
                super.setPrimaryKey(_pkIO, _pk);
            if ((flags & PK_WHERE) > 0)
                super.wherePrimaryKey(_pk);
            if (_fkSet != null) {
                ForeignKey[] fks = getTable().getForeignKeys();
                ColumnIO io;
                for (int i = 0; i < _fkSet.length; i++) {
                    if (_fkSet[i] != null) {
                        io = (_fkIO == null) ? null : _fkIO[i];
                        super.setForeignKey(fks[i], io, _fkSet[i]);
                    }
                }
            }
            if (_relSet != null) {
                Column[] cols = getTable().getRelationIdColumns();
                for (int i = 0; i < _relSet.length; i++)
                    if (_relSet[i] != null)
                        super.setRelationId(cols[i], _relSet[i], _callbacks[i]);
            }
            if (_fkWhere != null) {
                ForeignKey[] fks = getTable().getForeignKeys();
                for (int i = 0; i < _fkWhere.length; i++)
                    if (_fkWhere[i] != null)
                        super.whereForeignKey(fks[i], _fkWhere[i]);
            }
        }
        catch (SQLException se) {
            throw SQLExceptions.getStore(se, dict);
        }
        return super.generateSQL(dict);
    }

    protected RowImpl newInstance(Column[] cols, int action) {
        return new PrimaryRow(cols, action, _pk);
    }

    public void copyInto(RowImpl row, boolean whereOnly) {
        super.copyInto(row, whereOnly);
        if (!(row instanceof PrimaryRow))
            return;

        PrimaryRow prow = (PrimaryRow) row;
        prow._pk = _pk;
        prow._pkIO = _pkIO;
        if ((flags & PK_WHERE) > 0)
            prow.flags |= PK_WHERE;
        if (!whereOnly && (flags & PK_SET) > 0)
            prow.flags |= PK_SET;

        if (_fkWhere != null) {
            if (prow._fkWhere == null)
                prow._fkWhere = new OpenJPAStateManager[_fkWhere.length];
            System.arraycopy(_fkWhere, 0, prow._fkWhere, 0, _fkWhere.length);
        }
        if (!whereOnly && _fkSet != null) {
            if (prow._fkSet == null)
                prow._fkSet = new OpenJPAStateManager[_fkSet.length];
            System.arraycopy(_fkSet, 0, prow._fkSet, 0, _fkSet.length);
            if (_fkIO != null) {
                if (prow._fkIO == null)
                    prow._fkIO = new ColumnIO[_fkIO.length];
                System.arraycopy(_fkIO, 0, prow._fkIO, 0, _fkIO.length);
            }
        }
        if (!whereOnly && _relSet != null) {
            if (prow._relSet == null) {
                prow._relSet = new OpenJPAStateManager[_relSet.length];
                prow._callbacks = new RelationId[_callbacks.length];
            }
            System.arraycopy(_relSet, 0, prow._relSet, 0, _relSet.length);
            System.arraycopy(_callbacks, 0, prow._callbacks, 0,
                _callbacks.length);
        }
    }
    
    public String toString() {
    	StringBuilder buf = new StringBuilder();
    	buf.append("PrimaryRow[");
    	switch (getAction()) {
	    	case ACTION_UPDATE: buf.append("UPDATE"); break;
	    	case ACTION_INSERT: buf.append("INSERT"); break;
	    	case ACTION_DELETE: buf.append("DELETE"); break;
	    	default: buf.append("UNKNOWN");
    	}
    	buf.append(" ").append(getTable().getName()).append("]: ");
    	buf.append(_pk);
    	return buf.toString();
    }
}
