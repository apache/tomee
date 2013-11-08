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

import java.sql.*;
import java.util.*;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.*;
import org.apache.openjpa.jdbc.meta.*;
import org.apache.openjpa.jdbc.schema.*;
import org.apache.openjpa.jdbc.sql.*;
import org.apache.openjpa.kernel.*;
import org.apache.openjpa.lib.util.*;
import org.apache.openjpa.util.*;

/**
 * Mapping for a map of keys and values both controlled by
 * {@link ValueHandler}s.
 *
 * @author Abe White
 * @since 0.4.0, 1.1.0
 */
public class HandlerHandlerMapTableFieldStrategy
    extends MapTableFieldStrategy {

    private static final Localizer _loc = Localizer.forPackage
        (HandlerHandlerMapTableFieldStrategy.class);

    private Column[] _kcols = null;
    private ColumnIO _kio = null;
    private boolean _kload = false;
    private Column[] _vcols = null;
    private ColumnIO _vio = null;
    private boolean _vload = false;

    public Column[] getKeyColumns(ClassMapping cls) {
        return _kcols;
    }

    public Column[] getValueColumns(ClassMapping cls) {
        return _vcols;
    }

    public void selectKey(Select sel, ClassMapping cls, OpenJPAStateManager sm,
        JDBCStore store, JDBCFetchConfiguration fetch, Joins joins) {
        sel.select(_kcols, joins);
    }

    public void selectValue(Select sel, ClassMapping cls,
        OpenJPAStateManager sm,
        JDBCStore store, JDBCFetchConfiguration fetch, Joins joins) {
        sel.select(_vcols, joins);
    }

    public Result[] getResults(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, int eagerMode, Joins[] joins, boolean lrs)
        throws SQLException {
        Select sel = store.getSQLFactory().newSelect();
        sel.setLRS(lrs);
        sel.select(_kcols);
        sel.select(_vcols);
        sel.whereForeignKey(field.getJoinForeignKey(), sm.getObjectId(),
            field.getDefiningMapping(), store);
        Result res = sel.execute(store, fetch);
        return new Result[]{ res, res };
    }

    public Object loadKey(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res, Joins joins)
        throws SQLException {
        return HandlerStrategies.loadObject(field.getKeyMapping(),
            sm, store, fetch, res, joins, _kcols, _kload);
    }

    public Object loadValue(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res, Joins joins)
        throws SQLException {
        return HandlerStrategies.loadObject(field.getElementMapping(),
            sm, store, fetch, res, joins, _vcols, _vload);
    }

    public void map(boolean adapt) {
        super.map(adapt);

        ValueMapping key = field.getKeyMapping();
        if (key.getHandler() == null)
            throw new MetaDataException(_loc.get("no-handler", key));
        ValueMapping val = field.getElementMapping();
        if (val.getHandler() == null)
            throw new MetaDataException(_loc.get("no-handler", val));
        assertNotMappedBy();

        field.mapJoin(adapt, true);
        _kio = new ColumnIO();
        List columns = key.getValueInfo().getColumns(); 
        DBDictionary dict = field.getMappingRepository().getDBDictionary();
        DBIdentifier colName = dict.getValidColumnName(DBIdentifier.newColumn("key"), field.getTable());
        _kcols = HandlerStrategies.map(key, colName.getName(), _kio, adapt);

        _vio = new ColumnIO();
        _vcols = HandlerStrategies.map(val, "value", _vio, adapt);
        field.mapPrimaryKey(adapt);
    }

    public void initialize() {
        _kload = field.getKeyMapping().getHandler().
            objectValueRequiresLoad(field.getKeyMapping());
        _vload = field.getElementMapping().getHandler().
            objectValueRequiresLoad(field.getElementMapping());
    }

    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        insert(sm, store, rm, (Map) sm.fetchObject(field.getIndex()));
    }

    private void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm,
        Map map)
        throws SQLException {
        if (map == null || map.isEmpty())
            return;

        Row row = rm.getSecondaryRow(field.getTable(), Row.ACTION_INSERT);
        row.setForeignKey(field.getJoinForeignKey(), field.getJoinColumnIO(),
            sm);

        ValueMapping key = field.getKeyMapping();
        ValueMapping val = field.getElementMapping();
        Map.Entry entry;
        for (Iterator itr = map.entrySet().iterator(); itr.hasNext();) {
            entry = (Map.Entry) itr.next();
            HandlerStrategies.set(key, entry.getKey(), store, row, _kcols,
                _kio, true);
            HandlerStrategies.set(val, entry.getValue(), store, row, _vcols,
                _vio, true);
            rm.flushSecondaryRow(row);
        }
    }

    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        Map map = (Map) sm.fetchObject(field.getIndex());
        ChangeTracker ct = null;
        if (map instanceof Proxy) {
            Proxy proxy = (Proxy) map;
            if (Proxies.isOwner(proxy, sm, field.getIndex()))
                ct = proxy.getChangeTracker();
        }

        // if no fine-grained change tracking then just delete and reinsert
        if (ct == null || !ct.isTracking()) {
            delete(sm, store, rm);
            insert(sm, store, rm, map);
            return;
        }

        // delete the removes
        ValueMapping key = field.getKeyMapping();
        Collection rem = ct.getRemoved();
        if (!rem.isEmpty()) {
            Row delRow = rm.getSecondaryRow(field.getTable(),
                Row.ACTION_DELETE);
            delRow.whereForeignKey(field.getJoinForeignKey(), sm);
            for (Iterator itr = rem.iterator(); itr.hasNext();) {
                HandlerStrategies.where(key, itr.next(), store, delRow,
                    _kcols);
                rm.flushSecondaryRow(delRow);
            }
        }

        // insert the adds
        ValueMapping val = field.getElementMapping();
        Collection add = ct.getAdded();
        Object mkey;
        if (!add.isEmpty()) {
            Row addRow = rm.getSecondaryRow(field.getTable(),
                Row.ACTION_INSERT);
            addRow.setForeignKey(field.getJoinForeignKey(),
                field.getJoinColumnIO(), sm);

            for (Iterator itr = add.iterator(); itr.hasNext();) {
                mkey = itr.next();
                HandlerStrategies.set(key, mkey, store, addRow, _kcols,
                    _kio, true);
                HandlerStrategies.set(val, map.get(mkey), store, addRow,
                    _vcols, _vio, true);
                rm.flushSecondaryRow(addRow);
            }
        }

        // update the changes
        Collection change = ct.getChanged();
        if (!change.isEmpty()) {
            Row changeRow = rm.getSecondaryRow(field.getTable(),
                Row.ACTION_UPDATE);
            changeRow.whereForeignKey(field.getJoinForeignKey(), sm);

            for (Iterator itr = change.iterator(); itr.hasNext();) {
                mkey = itr.next();
                HandlerStrategies.where(key, mkey, store, changeRow, _kcols);
                HandlerStrategies.set(val, map.get(mkey), store, changeRow,
                    _vcols, _vio, true);
                rm.flushSecondaryRow(changeRow);
            }
        }
    }

    public Object toDataStoreValue(Object val, JDBCStore store) {
        return HandlerStrategies.toDataStoreValue(field.getElementMapping(),
            val, _vcols, store);
    }

    public Object toKeyDataStoreValue(Object val, JDBCStore store) {
        return HandlerStrategies.toDataStoreValue(field.getKeyMapping(), val,
            _kcols, store);
    }

    public Joins joinRelation(Joins joins, boolean forceOuter,
        boolean traverse) {
        if (traverse)
            HandlerStrategies.assertJoinable(field.getElementMapping());
        return joins;
    }

    public Joins joinKeyRelation(Joins joins, boolean forceOuter,
        boolean traverse) {
        if (traverse)
            HandlerStrategies.assertJoinable(field.getKeyMapping());
        return joins;
    }
}
