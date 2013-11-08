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
package org.apache.openjpa.jdbc.meta.strats;

import java.sql.SQLException;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.Embeddable;
import org.apache.openjpa.jdbc.meta.Joinable;
import org.apache.openjpa.jdbc.meta.ValueMappingInfo;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.PrimaryKey;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueStrategies;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.MetaDataException;

/**
 * Direct mapping from a primitive value to a column.
 *
 * @author Abe White
 * @since 0.4.0
 */
public class PrimitiveFieldStrategy
    extends AbstractFieldStrategy
    implements Joinable, Embeddable {

    private static final Object NULL = new Object();

    private static final Localizer _loc = Localizer.forPackage
        (PrimitiveFieldStrategy.class);

    private boolean _stateImage = false;

    public void map(boolean adapt) {
        if (field.isSerialized() || !field.getType().isPrimitive())
            throw new MetaDataException(_loc.get("not-primitive", field));
        assertNotMappedBy();

        // map join key, if any
        field.mapJoin(adapt, false);
        field.getKeyMapping().getValueInfo().assertNoSchemaComponents
            (field.getKey(), !adapt);
        field.getElementMapping().getValueInfo().assertNoSchemaComponents
            (field.getElement(), !adapt);

        ValueMappingInfo vinfo = field.getValueInfo();
        vinfo.assertNoJoin(field, true);
        vinfo.assertNoForeignKey(field, !adapt);

        // Determine whether to delimit the base field name
        DBDictionary dict = field.getMappingRepository().getDBDictionary();
        DBIdentifier fieldName = DBIdentifier.newColumn(field.getName(), dict != null ? dict.delimitAll() : false);
        // get value columns
        Column tmpCol = new Column();
        tmpCol.setIdentifier(fieldName);
        tmpCol.setJavaType(field.getTypeCode());

        Column[] cols = vinfo.getColumns(field, fieldName,
            new Column[]{ tmpCol }, field.getTable(), adapt);
        if (field.getValueStrategy() == ValueStrategies.AUTOASSIGN)
            cols[0].setAutoAssigned(true);
        if (vinfo.isImplicitRelation())
        	for (int i = 0; i < cols.length; i++)
        		cols[i].setImplicitRelation(true);
        field.setColumns(cols);
        field.setColumnIO(vinfo.getColumnIO());
        field.mapConstraints(fieldName, adapt);

        // add primary key columns to table pk if logical
        field.mapPrimaryKey(adapt);
        PrimaryKey pk = field.getTable().getPrimaryKey();
        if (field.isPrimaryKey() && pk != null && (adapt || pk.isLogical()))
            pk.addColumn(cols[0]);

        // set joinable
        field.getDefiningMapping().setJoinable(field.getColumns()[0], this);
    }

    public void initialize() {
        // record whether we're using a state image indicator, which requires
        // that we do special null checks when loading primitives
        _stateImage = field.getDefiningMapping().getVersion().getStrategy().
            getAlias().equals(StateComparisonVersionStrategy.ALIAS);
        if (_stateImage)
            field.setUsesImplData(null);
    }

    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        if (!field.getColumnIO().isInsertable(0, false))
            return;
        Row row = field.getRow(sm, store, rm, Row.ACTION_INSERT);
        if (row != null)
            update(sm, row);
    }

    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        if (!field.getColumnIO().isUpdatable(0, false))
            return;
        Row row = field.getRow(sm, store, rm, Row.ACTION_UPDATE);
        if (row != null)
            update(sm, row);
    }

    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        field.deleteRow(sm, store, rm);
    }

    /**
     * Set the value of the owning field into the given row.
     */
    private void update(OpenJPAStateManager sm, Row row)
        throws SQLException {
        Column col = field.getColumns()[0];
        switch (field.getTypeCode()) {
            case JavaTypes.BOOLEAN:
                row.setBoolean(col, sm.fetchBoolean(field.getIndex()));
                break;
            case JavaTypes.BYTE:
                row.setByte(col, sm.fetchByte(field.getIndex()));
                break;
            case JavaTypes.CHAR:
                row.setChar(col, sm.fetchChar(field.getIndex()));
                break;
            case JavaTypes.DOUBLE:
                row.setDouble(col, sm.fetchDouble(field.getIndex()));
                break;
            case JavaTypes.FLOAT:
                row.setFloat(col, sm.fetchFloat(field.getIndex()));
                break;
            case JavaTypes.INT:
                row.setInt(col, sm.fetchInt(field.getIndex()));
                break;
            case JavaTypes.LONG:
                row.setLong(col, sm.fetchLong(field.getIndex()));
                break;
            case JavaTypes.SHORT:
                row.setShort(col, sm.fetchShort(field.getIndex()));
                break;
            default:
                throw new InternalException();
        }
    }

    public int supportsSelect(Select sel, int type, OpenJPAStateManager sm,
        JDBCStore store, JDBCFetchConfiguration fetch) {
        if (type == Select.TYPE_JOINLESS && sel.isSelected(field.getTable()))
            return 1;
        return 0;
    }

    public int select(Select sel, OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, int eagerMode) {
        sel.select(field.getColumns()[0], field.join(sel));
        return 1;
    }

    public void load(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res)
        throws SQLException {
        Column col = field.getColumns()[0];
        if (!res.contains(col))
            return;

        int idx = field.getIndex();
        boolean checkNull = _stateImage && !field.isJoinOuter();
        switch (field.getTypeCode()) {
            case JavaTypes.BOOLEAN:
                sm.storeBoolean(idx, res.getBoolean(col));
                break;
            case JavaTypes.BYTE:
                sm.storeByte(idx, res.getByte(col));
                break;
            case JavaTypes.CHAR:
                sm.storeChar(idx, res.getChar(col));
                break;
            case JavaTypes.DOUBLE:
                sm.storeDouble(idx, res.getDouble(col));
                checkNull = false;
                break;
            case JavaTypes.FLOAT:
                sm.storeFloat(idx, res.getFloat(col));
                checkNull = false;
                break;
            case JavaTypes.INT:
                sm.storeInt(idx, res.getInt(col));
                break;
            case JavaTypes.LONG:
                sm.storeLong(idx, res.getLong(col));
                break;
            case JavaTypes.SHORT:
                sm.storeShort(idx, res.getShort(col));
                break;
            default:
                throw new InternalException();
        }

        // we're using state image versioning, so record that the actual db
        // value was null so we add the correct OL check on update
        if (checkNull && res.wasNull())
            sm.setImplData(field.getIndex(), NULL);
    }

    public void appendIsNull(SQLBuffer sql, Select sel, Joins joins) {
        joins = join(joins, false);
        sql.append(sel.getColumnAlias(field.getColumns()[0], joins)).
            append(" IS ").appendValue(null, field.getColumns()[0]);
    }

    public void appendIsNotNull(SQLBuffer sql, Select sel, Joins joins) {
        joins = join(joins, false);
        sql.append(sel.getColumnAlias(field.getColumns()[0], joins)).
            append(" IS NOT ").appendValue(null, field.getColumns()[0]);
    }

    public Joins join(Joins joins, boolean forceOuter) {
        return field.join(joins, forceOuter, false);
    }

    public Object loadProjection(JDBCStore store, JDBCFetchConfiguration fetch,
        Result res, Joins joins)
        throws SQLException {
        return res.getObject(field.getColumns()[0], null, joins);
    }

    public boolean isVersionable() {
        if (field.isJoinOuter())
            return false;
        switch (field.getTypeCode()) {
            case JavaTypes.BOOLEAN:
            case JavaTypes.BYTE:
            case JavaTypes.CHAR:
            case JavaTypes.INT:
            case JavaTypes.LONG:
            case JavaTypes.SHORT:
                return true;
            default:
                return false;
        }
    }

    public void where(OpenJPAStateManager sm, JDBCStore store, RowManager rm,
        Object prevValue)
        throws SQLException {
        Row row = field.getRow(sm, store, rm, Row.ACTION_UPDATE);
        if (row == null)
            return;

        // for primitives loaded as default vals, check to see if was null in
        // the database when loaded == remove the impl data at the same time
        // to be sure we don't think the value is null after the commit
        Column col = field.getColumns()[0];
        if (sm.setImplData(field.getIndex(), null) == NULL)
            row.whereNull(col);
        else
            row.whereObject(col, prevValue);
    }

    ///////////////////////////
    // Joinable implementation
    ///////////////////////////

    public int getFieldIndex() {
        return field.getIndex();
    }

    public Object getPrimaryKeyValue(Result res, Column[] cols, ForeignKey fk,
        JDBCStore store, Joins joins)
        throws SQLException {
        Column col = cols[0];
        if (fk != null)
            col = fk.getColumn(col);
        return JavaTypes.convert(res.getObject(col, null, joins),
            field.getTypeCode());
    }

    public Column[] getColumns() {
        return field.getColumns();
    }

    public Object getJoinValue(Object fieldVal, Column col, JDBCStore store) {
        return fieldVal;
    }

    public Object getJoinValue(OpenJPAStateManager sm, Column col,
        JDBCStore store) {
        return sm.fetch(field.getIndex());
    }

    public void setAutoAssignedValue(OpenJPAStateManager sm, JDBCStore store,
        Column col, Object autoInc) {
        int idx = field.getIndex();
        switch (field.getTypeCode()) {
            case JavaTypes.BOOLEAN:
                if (autoInc == null)
                    sm.storeBoolean(idx, false);
                else if (autoInc instanceof Boolean)
                    sm.storeBoolean(idx, ((Boolean) autoInc).booleanValue());
                else
                    sm.storeBoolean(idx, ((Number) autoInc).intValue() != 0);
                break;
            case JavaTypes.BYTE:
                if (autoInc == null)
                    sm.storeByte(idx, (byte) 0);
                else
                    sm.storeByte(idx, ((Number) autoInc).byteValue());
                break;
            case JavaTypes.CHAR:
                if (autoInc == null)
                    sm.storeChar(idx, (char) 0);
                else if (autoInc instanceof Character)
                    sm.storeChar(idx, ((Character) autoInc).charValue());
                else if (autoInc instanceof String)
                    sm.storeChar(idx, ((String) autoInc).charAt(0));
                else
                    sm.storeChar(idx, (char) ((Number) autoInc).intValue());
                break;
            case JavaTypes.DOUBLE:
                if (autoInc == null)
                    sm.storeDouble(idx, 0D);
                else
                    sm.storeDouble(idx, ((Number) autoInc).doubleValue());
                break;
            case JavaTypes.FLOAT:
                if (autoInc == null)
                    sm.storeFloat(idx, 0F);
                else
                    sm.storeFloat(idx, ((Number) autoInc).floatValue());
                break;
            case JavaTypes.INT:
                if (autoInc == null)
                    sm.storeInt(idx, 0);
                else
                    sm.storeInt(idx, ((Number) autoInc).intValue());
                break;
            case JavaTypes.LONG:
                if (autoInc == null)
                    sm.storeLong(idx, 0L);
                else
                    sm.storeLong(idx, ((Number) autoInc).longValue());
                break;
            case JavaTypes.SHORT:
                if (autoInc == null)
                    sm.storeShort(idx, (short) 0);
                else
                    sm.storeShort(idx, ((Number) autoInc).shortValue());
                break;
            default:
                throw new InternalException();
        }
    }

    /////////////////////////////
    // Embeddable implementation
    /////////////////////////////

    public ColumnIO getColumnIO() {
        return field.getColumnIO();
    }

    public Object[] getResultArguments() {
        return null;
    }

    public Object toEmbeddedObjectValue(Object val) {
        return val;
    }

    public Object toEmbeddedDataStoreValue(Object val, JDBCStore store) {
        return toDataStoreValue(val, store);
    }

    public void loadEmbedded(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Object val)
        throws SQLException {
        sm.store(field.getIndex(), val);
    }
}
