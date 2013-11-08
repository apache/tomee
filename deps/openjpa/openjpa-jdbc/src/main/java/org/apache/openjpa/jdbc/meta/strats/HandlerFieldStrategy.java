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

import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.Embeddable;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.Joinable;
import org.apache.openjpa.jdbc.meta.ValueHandler;
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
import org.apache.openjpa.util.UserException;

/**
 * Mapping for a single-valued field that delegates to a {@link ValueHandler}.
 *
 * @author Abe White
 * @since 0.4.0
 */
public class HandlerFieldStrategy
    extends AbstractFieldStrategy
    implements Joinable, Embeddable {

    private static final Object NULL = new Object();

    private static final Localizer _loc = Localizer.forPackage
        (HandlerFieldStrategy.class);

    protected Column[] _cols = null;
    protected ColumnIO _io = null;
    protected Object[] _args = null;
    protected boolean _load = false;
    protected boolean _lob = false;

    public void map(boolean adapt) {
        if (field.getHandler() == null)
            throw new MetaDataException(_loc.get("no-handler", field));
        assertNotMappedBy();

        // map join key (if any)
        field.mapJoin(adapt, false);
        field.getKeyMapping().getValueInfo().assertNoSchemaComponents
            (field.getKey(), !adapt);
        field.getElementMapping().getValueInfo().assertNoSchemaComponents
            (field.getElement(), !adapt);

        _io = new ColumnIO();
        _cols = HandlerStrategies.map(field, field.getName(), _io, adapt);
        if (field.getValueStrategy() == ValueStrategies.AUTOASSIGN) {
            // first see if any columns already marked autoassign; if not mark
            // them all
            boolean marked = false;
            for (int i = 0; !marked && i < _cols.length; i++)
                if (_cols[i].isAutoAssigned())
                    marked = true;
            if (!marked)
                for (int i = 0; i < _cols.length; i++)
                    _cols[i].setAutoAssigned(true);
        }

        // add primary key columns to table pk if logical
        field.mapPrimaryKey(adapt);
        PrimaryKey pk = field.getTable().getPrimaryKey();
        if (field.isPrimaryKey() && pk != null && (adapt || pk.isLogical()))
            for (int i = 0; i < _cols.length; i++)
                pk.addColumn(_cols[i]);

        // set joinable
        if (!field.getHandler().objectValueRequiresLoad(field))
            for (int i = 0; i < _cols.length; i++)
                field.getDefiningMapping().setJoinable(_cols[i], this);
    }

    public void initialize() {
        _load = field.getHandler().objectValueRequiresLoad(field);
        if (_load)
            field.setUsesIntermediate(true);
        for (int i = 0; !_lob && i < _cols.length; i++)
            _lob = _cols[i].isLob();

        Object args = field.getHandler().getResultArgument(field);
        if (args == null)
            _args = null;
        else if (_cols.length == 1)
            _args = new Object[]{ args };
        else
            _args = (Object[]) args;
    }

    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        if (field.getColumnIO().isInsertable(0, false)) {
            Row row = field.getRow(sm, store, rm, Row.ACTION_INSERT);
            if (row != null) {
                Object value = sm.fetch(field.getIndex());
                if (!HandlerStrategies.set(field, value, store, row, _cols,
                    _io, field.getNullValue() == FieldMapping.NULL_NONE))
                    if (field.getValueStrategy() != ValueStrategies.AUTOASSIGN)
                        throw new UserException(_loc.get("cant-set-value", row
                            .getFailedObject(), field, value));
            }
        }
    }

    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        if (field.getColumnIO().isUpdatable(0, false)) {
            Row row = field.getRow(sm, store, rm, Row.ACTION_UPDATE);
            if (row != null) {
                Object value = sm.fetch(field.getIndex());
                if (!HandlerStrategies.set(field, value, store, row, _cols,
                    _io, field.getNullValue() == FieldMapping.NULL_NONE))
                    if (field.getValueStrategy() != ValueStrategies.AUTOASSIGN)
                        throw new UserException(_loc.get("cant-set-value", row
                            .getFailedObject(), field, value));
            }
        }
    }

    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        field.deleteRow(sm, store, rm);
    }

    public int supportsSelect(Select sel, int type, OpenJPAStateManager sm,
        JDBCStore store, JDBCFetchConfiguration fetch) {
        if ((type == Select.TYPE_JOINLESS && sel.isSelected(field.getTable()))
            || (_load && type == Select.TYPE_TWO_PART))
            return 1;
        return 0;
    }

    public int select(Select sel, OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, int eagerMode) {
        if (_cols.length == 0)
            return -1;

        if (sm != null && sm.getIntermediate(field.getIndex()) != null)
            return -1;
        if (_lob && !field.isPrimaryKey() && (sel.isDistinct() ||
                eagerMode == JDBCFetchConfiguration.EAGER_NONE))
            return -1;
        sel.select(_cols, field.join(sel));
        return 1;
    }

    public void load(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res)
        throws SQLException {
        if (_cols.length == 0 || !res.containsAll(_cols))
            return;

        Object val = HandlerStrategies.loadDataStore(field, res, null, _cols);
        if (!_load)
            sm.store(field.getIndex(), field.getHandler().
                toObjectValue(field, val));
        else {
            if (val == null)
                val = NULL;
            sm.setIntermediate(field.getIndex(), val);
        }
    }

    public void load(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch)
        throws SQLException {
        // even if no columns, allow a handler to load a generated value
        if (_cols.length == 0) {
            if (_load)
                sm.store(field.getIndex(), field.getHandler().
                    toObjectValue(field, null, sm, store, fetch));
            else
                sm.store(field.getIndex(), field.getHandler().
                    toObjectValue(field, null));
            return;
        }

        // load cached intermediate value?
        if (_load) {
            Object ds = sm.getIntermediate(field.getIndex());
            if (ds != null) {
                if (ds == NULL)
                    ds = null;
                sm.store(field.getIndex(), field.getHandler().
                    toObjectValue(field, ds, sm, store, fetch));
                return;
            }
        }

        Select sel = store.getSQLFactory().newSelect();
        sel.select(_cols);
        field.wherePrimaryKey(sel, sm, store);

        Result res = sel.execute(store, fetch);
        Object val = null;
        try {
            if (res.next())
                val = HandlerStrategies.loadDataStore(field, res, null, _cols);
        } finally {
            res.close();
        }

        loadEmbedded(sm, store, fetch, val);
    }

    public Object toDataStoreValue(Object val, JDBCStore store) {
        return HandlerStrategies.toDataStoreValue(field, val, _cols, store);
    }

    public void appendIsNull(SQLBuffer sql, Select sel, Joins joins) {
        joins = join(joins, false);
        for (int i = 0; i < _cols.length; i++) {
            if (i > 0) {
                sql.append(" AND ");
            }
            // Some databases do not allow IS NULL for every column type - let the DBDictionary decide.
            DBDictionary dict = sel.getDictionary();
            sql.append(dict.getIsNullSQL(sel.getColumnAlias(_cols[i], joins), _cols[i].getType()));
        }
    }

    public void appendIsNotNull(SQLBuffer sql, Select sel, Joins joins) {
        joins = join(joins, false);
        if (_cols.length > 1) {
            sql.append("(");
        }
        for (int i = 0; i < _cols.length; i++) {
            if (i > 0) { 
                sql.append(" OR "); 
            }
            // Some databases do not allow IS NOT NULL for every column type - let the DBDictionary decide.
            DBDictionary dict = sel.getDictionary();
            sql.append(dict.getIsNotNullSQL(sel.getColumnAlias(_cols[i], joins), _cols[i].getType()));
        }
        if (_cols.length > 1)
            sql.append(")");
    }

    public Joins join(Joins joins, boolean forceOuter) {
        return field.join(joins, forceOuter, false);
    }

    public Joins joinRelation(Joins joins, boolean forceOuter,
        boolean traverse) {
        if (traverse)
            HandlerStrategies.assertJoinable(field);
        return joins;
    }

    public Object loadProjection(JDBCStore store, JDBCFetchConfiguration fetch,
        Result res, Joins joins)
        throws SQLException {
        return HandlerStrategies.loadObject(field, null, store, fetch, res,
            joins, _cols, _load);
    }

    public boolean isVersionable() {
        return !_lob && !field.isJoinOuter()
            && field.getHandler().isVersionable(field);
    }

    public void where(OpenJPAStateManager sm, JDBCStore store, RowManager rm,
        Object prevValue)
        throws SQLException {
        Row row = field.getRow(sm, store, rm, Row.ACTION_UPDATE);
        if (row != null)
            HandlerStrategies.where(field, prevValue, store, row, _cols);
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
        Column col;
        Object val = null;
        if (cols.length == 1) {
            col = cols[0];
            if (fk != null){
                col = fk.getColumn(col);
            }
            
            //OJ-1793: Get the args from the handler and first check to see if the
            //args are null.  If they aren't null then use the first element in the args
            //array rather than passing into 'getObject' the entire args array.  This is
            //akin to what is done in the 'else if' leg below.
            Object[] args = (Object[]) field.getHandler().getResultArgument(field);            
            val = res.getObject(col, (args == null) ? null : args[0],
                    joins);
        } else if (cols.length > 1) {
            Object[] vals = new Object[cols.length];
            Object[] args = (Object[]) field.getHandler().
                getResultArgument(field);
            for (int i = 0; i < vals.length; i++) {
                col = cols[i];
                if (fk != null)
                    col = fk.getColumn(col);
                vals[i] = res.getObject(col, (args == null) ? null : args[i],
                    joins);
            }
            val = vals;
        }
        return field.getHandler().toObjectValue(field, val);
    }

    public Column[] getColumns() {
        return _cols;
    }

    public Object[] getResultArguments() {
        return _args;
    }

    public Object getJoinValue(Object fieldVal, Column col, JDBCStore store) {
        Object val = HandlerStrategies.toDataStoreValue(field, fieldVal,
            _cols, store);
        if (val == null || _cols.length < 2)
            return val;

        for (int i = 0; i < _cols.length; i++)
            if (_cols[i] == col)
                return ((Object[]) val)[i];
        throw new InternalException();
    }

    public Object getJoinValue(OpenJPAStateManager sm, Column col,
        JDBCStore store) {
        return getJoinValue(sm.fetch(field.getIndex()), col, store);
    }

    public void setAutoAssignedValue(OpenJPAStateManager sm, JDBCStore store,
        Column col, Object autoInc) {
        Object data;
        if (_cols.length == 1)
            data = JavaTypes.convert(autoInc, col.getJavaType());
        else {
            // multiple columns; have to get current value, replace this col's
            // value with the given one, and reset
            data = field.getHandler().toDataStoreValue(field,
                sm.fetch(field.getIndex()), store);
            if (data == null)
                data = new Object[_cols.length];
            for (int i = 0; i < _cols.length; i++) {
                if (_cols[i] == col) {
                    ((Object[]) data)[i] = JavaTypes.convert(autoInc, 
                        col.getJavaType());
                    break;
                }
            }
        }

        Object val = field.getHandler().toObjectValue(field, data);
        sm.store(field.getIndex(), val);
    }

    /////////////////////////////
    // Embeddable implementation
    /////////////////////////////

    public ColumnIO getColumnIO() {
        return _io;
    }

    public Object toEmbeddedDataStoreValue(Object val, JDBCStore store) {
        // don't use HandlerStrategies.toDataStoreValue b/c we want relation ids
        // to be represented by state managers, not the serialized id value
        val = field.getHandler().toDataStoreValue(field, val, store);
        if (val == null && _cols.length > 1)
            return new Object[_cols.length];
        return val;
    }

    public Object toEmbeddedObjectValue(Object val) {
        if (!_load)
            return field.getHandler().toObjectValue(field, val);
        return UNSUPPORTED;
    }

    public void loadEmbedded(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Object val)
        throws SQLException {
        if (val == null && _cols.length > 1)
            val = new Object[_cols.length];
        if (_load)
            sm.store(field.getIndex(), field.getHandler().
                toObjectValue(field, val, sm, store, fetch));
        else
            sm.store(field.getIndex(), field.getHandler().
                toObjectValue(field, val));
    }
}
