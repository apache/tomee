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
import org.apache.openjpa.jdbc.meta.FieldMapping;
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
import org.apache.openjpa.util.MetaDataException;

/**
 * Direct mapping from a string value to a column.
 *
 * @author Abe White
 * @since 0.4.0
 */
public class StringFieldStrategy
    extends AbstractFieldStrategy
    implements Joinable, Embeddable {

    private static final Localizer _loc = Localizer.forPackage
        (StringFieldStrategy.class);

    public void map(boolean adapt) {
        if (field.getTypeCode() != JavaTypes.STRING)
            throw new MetaDataException(_loc.get("not-string", field));
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

    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        String str = (String) toDataStoreValue(sm.fetchString
            (field.getIndex()), store);
        if (field.getColumnIO().isInsertable(0, str == null)) {
            Row row = field.getRow(sm, store, rm, Row.ACTION_INSERT);
            if (row != null)
                row.setString(field.getColumns()[0], str);
        }
    }

    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        String str = (String) toDataStoreValue(sm.fetchString
            (field.getIndex()), store);
        if (field.getColumnIO().isUpdatable(0, str == null)) {
            Row row = field.getRow(sm, store, rm, Row.ACTION_UPDATE);
            if (row != null)
                row.setString(field.getColumns()[0], str);
        }
    }

    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        field.deleteRow(sm, store, rm);
    }

    public Object toDataStoreValue(Object val, JDBCStore store) {
        if (val != null)
            return val;

        if (field.getNullValue() != FieldMapping.NULL_DEFAULT)
            return null;
        if (field.getColumns()[0].getDefaultString() != null)
            return null;
        // honor the user's null-value=default
        return "";
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
        if (res.contains(col))
            sm.storeString(field.getIndex(), res.getString(col));
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
        return res.getString(field.getColumns()[0], joins);
    }

    public boolean isVersionable() {
        return true;
    }

    public void where(OpenJPAStateManager sm, JDBCStore store, RowManager rm,
        Object prevValue)
        throws SQLException {
        Row row = field.getRow(sm, store, rm, Row.ACTION_UPDATE);
        if (row == null)
            return;

        Column col = field.getColumns()[0];
        if (prevValue == null)
            row.whereNull(col);
        else
            row.whereString(col, prevValue.toString());
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
        return res.getString(col, joins);
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
        String str = (autoInc == null) ? null : autoInc.toString();
        sm.storeString(field.getIndex(), str);
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

    public Object toEmbeddedDataStoreValue(Object val, JDBCStore store) {
        return toDataStoreValue(val, store);
    }

    public Object toEmbeddedObjectValue(Object val) {
        return val;
    }

    public void loadEmbedded(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Object val)
        throws SQLException {
        sm.storeString(field.getIndex(), (String) val);
    }
}
