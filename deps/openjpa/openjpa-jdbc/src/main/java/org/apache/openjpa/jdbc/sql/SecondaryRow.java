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

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.RelationId;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * Secondary table row that tracks foreign keys to auto-inc columns.
 *
 * @author Abe White
 * @nojavadoc
 */
public class SecondaryRow
    extends RowImpl {

    private OpenJPAStateManager[] _fks = null;
    private ColumnIO[] _fkIO = null;
    private OpenJPAStateManager[] _rels = null;
    private RelationId[] _callbacks = null;

    /**
     * Constructor; supply table and action.
     */
    public SecondaryRow(Table table, int action) {
        this(table.getColumns(), action);
    }

    protected SecondaryRow(Column[] cols, int action) {
        super(cols, action);
    }

    public void setForeignKey(ForeignKey fk, OpenJPAStateManager sm)
        throws SQLException {
        setForeignKey(fk, null, sm);
    }

    public void setForeignKey(ForeignKey fk, ColumnIO io,
        OpenJPAStateManager sm)
        throws SQLException {
        if (!delayForeignKey(fk, sm)) {
            super.setForeignKey(fk, io, sm);
            return;
        }

        // force valid
        if (canSetAny(io, fk.getColumns().length
            + fk.getConstantColumns().length, false))
            setValid(true);

        // record foreig key for delayed flush
        if (_fks == null)
            _fks = new OpenJPAStateManager[getTable().getForeignKeys().length];
        _fks[fk.getIndex()] = sm;

        if (_fkIO != null)
            _fkIO[fk.getIndex()] = io;
        else if (io != null
            && ((getAction() == ACTION_INSERT
            && !io.isAllInsertable(fk, false))
            || (getAction() != ACTION_INSERT
            && !io.isAllUpdatable(fk, false)))) {
            _fkIO = new ColumnIO[_fks.length];
            _fkIO[fk.getIndex()] = io;
        }
    }

    /**
     * Record foreign keys to new auto-inc instances; flush them only when
     * we have to generate our SQL to give the instance a chance to finalize
     * its values.
     */
    private boolean delayForeignKey(ForeignKey fk, OpenJPAStateManager sm) {
        return fk.isPrimaryKeyAutoAssigned() && getAction() != ACTION_DELETE
            && sm != null && sm.isNew() && !sm.isFlushed();
    }

    public void setRelationId(Column col, OpenJPAStateManager sm,
        RelationId rel)
        throws SQLException {
        if (sm == null || sm.getObjectId() != null || !sm.isNew()
            || sm.isFlushed() || !isPrimaryKeyAutoAssigned(sm))
            super.setRelationId(col, sm, rel);
        else {
            if (_rels == null) {
                Column[] cols = getTable().getRelationIdColumns();
                _rels = new OpenJPAStateManager[cols.length];
                _callbacks = new RelationId[cols.length];
            }
            int idx = getRelationIdIndex(col);
            _rels[idx] = sm;
            _callbacks[idx] = rel;
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

    protected String generateSQL(DBDictionary dict) {
        try {
            if (_fks != null) {
                ForeignKey[] fks = getTable().getForeignKeys();
                ColumnIO io;
                for (int i = 0; i < _fks.length; i++) {
                    if (_fks[i] != null) {
                        io = (_fkIO == null) ? null : _fkIO[i];
                        super.setForeignKey(fks[i], io, _fks[i]);
                    }
                }
            }
            if (_rels != null) {
                Column[] cols = getTable().getRelationIdColumns();
                for (int i = 0; i < _rels.length; i++)
                    if (_rels[i] != null)
                        super.setRelationId(cols[i], _rels[i], _callbacks[i]);
            }
        }
        catch (SQLException se) {
            throw SQLExceptions.getStore(se, dict);
        }
        return super.generateSQL(dict);
    }

    protected RowImpl newInstance(Column[] cols, int action) {
        return new SecondaryRow(cols, action);
    }

    public void copyInto(RowImpl row, boolean whereOnly) {
        super.copyInto(row, whereOnly);
        if (_fks == null || whereOnly || row.getAction() == ACTION_DELETE
            || !(row instanceof SecondaryRow))
            return;

        SecondaryRow srow = (SecondaryRow) row;
        if (srow._fks == null)
            srow._fks = new OpenJPAStateManager[_fks.length];
        System.arraycopy(_fks, 0, srow._fks, 0, _fks.length);
        if (_fkIO != null) {
            if (srow._fkIO == null)
                srow._fkIO = new ColumnIO[_fkIO.length];
            System.arraycopy(_fkIO, 0, srow._fkIO, 0, _fkIO.length);
        }
    }
}
