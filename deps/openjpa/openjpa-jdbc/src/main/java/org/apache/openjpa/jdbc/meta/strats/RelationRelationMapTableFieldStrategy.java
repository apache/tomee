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

import org.apache.openjpa.lib.util.*;
import org.apache.openjpa.meta.*;
import org.apache.openjpa.kernel.*;
import org.apache.openjpa.util.*;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.jdbc.meta.*;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.*;
import org.apache.openjpa.jdbc.schema.*;
import org.apache.openjpa.jdbc.sql.*;

/**
 * <p>Mapping for a map whose keys and values are both relations to other
 * persistent objects.</p>
 *
 * @author Abe White
 * @since 0.4.0, 1.1.0
 */
public class RelationRelationMapTableFieldStrategy
    extends MapTableFieldStrategy {

    private static final Localizer _loc = Localizer.forPackage
        (RelationRelationMapTableFieldStrategy.class);

    private String _keyRelationName = null;

    public Column[] getKeyColumns(ClassMapping cls) {
        return field.getKeyMapping().getColumns();
    }

    public Column[] getValueColumns(ClassMapping cls) {
        return field.getElementMapping().getColumns();
    }

    public void selectKey(Select sel, ClassMapping key, OpenJPAStateManager sm,
        JDBCStore store, JDBCFetchConfiguration fetch, Joins joins) {
        sel.select(key, field.getKeyMapping().getSelectSubclasses(),
            store, fetch, JDBCFetchConfiguration.EAGER_NONE, joins);
    }

    public void selectValue(Select sel, ClassMapping val,
        OpenJPAStateManager sm, JDBCStore store, JDBCFetchConfiguration fetch, 
        Joins joins) {
        sel.select(val, field.getElementMapping().getSelectSubclasses(),
            store, fetch, JDBCFetchConfiguration.EAGER_NONE, joins);
    }

    public Result[] getResults(final OpenJPAStateManager sm,
        final JDBCStore store, final JDBCFetchConfiguration fetch,
        final int eagerMode, final Joins[] resJoins, boolean lrs)
        throws SQLException {
        ValueMapping key = field.getKeyMapping();
        final ClassMapping[] keys = key.getIndependentTypeMappings();
        Union kunion = store.getSQLFactory().newUnion(keys.length);
        if (fetch.getSubclassFetchMode(key.getTypeMapping())
            != JDBCFetchConfiguration.EAGER_JOIN)
            kunion.abortUnion();
        kunion.setLRS(lrs);
        kunion.select(new Union.Selector() {
            public void select(Select sel, int idx) {
                ForeignKey joinFK = null;
                if (field.isUni1ToMFK()) {
                    ValueMapping val = field.getElementMapping();
                    ValueMappingInfo vinfo = val.getValueInfo();
                    Table table = vinfo.getTable(val);
                    joinFK = field.getMappingInfo().getJoinForeignKey(field, table, true);
                } else {
                    joinFK = field.getJoinForeignKey();
                }
                
                sel.whereForeignKey(joinFK,
                    sm.getObjectId(), field.getDefiningMapping(), store);

                // order before select in case we're faking union with
                // multiple selects; order vals used to merge results
                Joins joins = joinKeyRelation(sel.newJoins(), keys[idx]);
                sel.orderBy(field.getKeyMapping().getColumns(), true, true);
                sel.select(keys[idx], field.getKeyMapping().
                    getSelectSubclasses(), store, fetch, eagerMode, joins);

                //### cheat: result joins only care about the relation path;
                //### thus we can use first mapping of union only
                if (idx == 0)
                    resJoins[0] = joins;
            }
        });

        ValueMapping val = field.getElementMapping();
        final ClassMapping[] vals = val.getIndependentTypeMappings();
        Union vunion = store.getSQLFactory().newUnion(vals.length);
        if (fetch.getSubclassFetchMode(val.getTypeMapping())
            != JDBCFetchConfiguration.EAGER_JOIN)
            vunion.abortUnion();
        vunion.setLRS(lrs);
        vunion.select(new Union.Selector() {
            public void select(Select sel, int idx) {
                if (field.isUni1ToMFK()) {
                    sel.orderBy(field.getKeyMapping().getColumns(), true, true);
                    sel.select(vals[idx], field.getElementMapping().
                        getSelectSubclasses(), store, fetch, eagerMode, null);
                    sel.whereForeignKey(field.getElementMapping().getForeignKey(),
                        sm.getObjectId(), field.getElementMapping().getDeclaredTypeMapping(), store);
                    
                } else {
                    sel.whereForeignKey(field.getJoinForeignKey(),
                        sm.getObjectId(), field.getDefiningMapping(), store);

                    // order before select in case we're faking union with
                    // multiple selects; order vals used to merge results
                    Joins joins = joinValueRelation(sel.newJoins(), vals[idx]);
                    sel.orderBy(field.getKeyMapping().getColumns(), true, true);
                    sel.select(vals[idx], field.getElementMapping().
                        getSelectSubclasses(), store, fetch, eagerMode, joins);

                    //### cheat: result joins only care about the relation path;
                    //### thus we can use first mapping of union only
                    if (idx == 0)
                        resJoins[1] = joins;
                }
            }
        });

        Result kres = null;
        Result vres = null;
        try {
            kres = kunion.execute(store, fetch);
            vres = vunion.execute(store, fetch);
            return new Result[]{ kres, vres };
        } catch (SQLException se) {
            if (kres != null)
                kres.close();
            if (vres != null)
                vres.close();
            throw se;
        }
    }

    public Object loadKey(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res, Joins joins)
        throws SQLException {
        ClassMapping key = res.getBaseMapping();
        if (key == null)
            key = field.getKeyMapping().getIndependentTypeMappings()[0];
        return res.load(key, store, fetch, joins);
    }

    public Object loadValue(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res, Joins joins)
        throws SQLException {
        ClassMapping val = res.getBaseMapping();
        if (val == null)
            val = field.getElementMapping().getIndependentTypeMappings()[0];
        return res.load(val, store, fetch, joins);
    }

    public Joins joinKeyRelation(Joins joins, ClassMapping key) {
        ValueMapping vm = field.getKeyMapping();
        return joins.joinRelation(_keyRelationName, vm.getForeignKey(key), key,
            vm.getSelectSubclasses(), false, false);
    }

    public Joins joinValueRelation(Joins joins, ClassMapping val) {
        ValueMapping vm = field.getElementMapping();
        ForeignKey fk = vm.getForeignKey(val);
        if (fk == null)
            return joins;
        return joins.joinRelation(field.getName(), fk, val,
            vm.getSelectSubclasses(), false, false);
    }

    public void map(boolean adapt) {
        super.map(adapt);

        ValueMapping key = field.getKeyMapping();
        if (key.getTypeCode() != JavaTypes.PC || key.isEmbeddedPC())
            throw new MetaDataException(_loc.get("not-relation", key));
        ValueMapping val = field.getElementMapping();
        if (val.getTypeCode() != JavaTypes.PC || val.isEmbeddedPC())
            throw new MetaDataException(_loc.get("not-relation", val));
        FieldMapping mapped = field.getMappedByMapping();
        DBDictionary dict = field.getMappingRepository().getDBDictionary();
        DBIdentifier keyName = null;
        if (field.isUni1ToMFK() || (!field.isBiMTo1JT() && mapped != null)) { 
            handleMappedByForeignKey(adapt);
            keyName = dict.getValidColumnName(DBIdentifier.newColumn("vkey"), field.getTable());
        } else if (field.isBiMTo1JT() || mapped == null) { 
            field.mapJoin(adapt, true);
            mapTypeJoin(val, DBIdentifier.newColumn("value"), adapt);
            keyName = dict.getValidColumnName(DBIdentifier.newColumn("key"), field.getTable());
        }
        mapTypeJoin(key, keyName, adapt);

        field.mapPrimaryKey(adapt);
    }

    /**
     * Map the given value's join to its persistent type.
     */
    private void mapTypeJoin(ValueMapping vm, DBIdentifier name, boolean adapt) {
        if (vm.getTypeMapping().isMapped()) {
            ValueMappingInfo vinfo = vm.getValueInfo();
            ForeignKey fk = vinfo.getTypeJoin(vm, name, false, adapt);
            vm.setForeignKey(fk);
            vm.setColumnIO(vinfo.getColumnIO());
        } else
            RelationStrategies.mapRelationToUnmappedPC(vm, name, adapt);
        vm.mapConstraints(name, adapt);
    }

    public void initialize() {
        _keyRelationName = field.getName() + ":key";
    }

    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        insert(sm, rm, (Map) sm.fetchObject(field.getIndex()), store);
    }

    private void insert(OpenJPAStateManager sm, RowManager rm, Map map, 
        JDBCStore store)
        throws SQLException {
        if (map == null || map.isEmpty())
            return;
        
        if (!field.isBiMTo1JT() && field.getMappedBy() != null)
            return;

        Row row = null;
        if (!field.isUni1ToMFK()) {
            row = rm.getSecondaryRow(field.getTable(), Row.ACTION_INSERT);
            row.setForeignKey(field.getJoinForeignKey(), field.getJoinColumnIO(),
                sm);
        }
        ValueMapping key = field.getKeyMapping();
        ValueMapping val = field.getElementMapping();
        StoreContext ctx = sm.getContext();
        OpenJPAStateManager keysm, valsm;
        Map.Entry entry;
        for (Iterator itr = map.entrySet().iterator(); itr.hasNext();) {
            entry = (Map.Entry) itr.next();
            keysm = RelationStrategies.getStateManager(entry.getKey(), ctx);
            valsm = RelationStrategies.getStateManager(entry.getValue(), ctx);
            if (field.isUni1ToMFK()){
                row = rm.getRow(field.getElementMapping().getDeclaredTypeMapping().getTable(),
                    Row.ACTION_UPDATE, valsm, true);
                row.wherePrimaryKey(valsm);
                val.setForeignKey(row, sm);
            } else {
                val.setForeignKey(row, valsm);
            }
            key.setForeignKey(row, keysm);
            
            // so far, we populated the key/value of each
            // map element owned by the entity.
            // In the case of ToMany, and both sides
            // use Map to represent the relation,
            // we need to populate the key value of the owner
            // from the view point of the owned side
            PersistenceCapable obj = sm.getPersistenceCapable();
            if (!populateKey(row, valsm, obj, ctx, rm, store))
                if (!field.isUni1ToMFK())
                    rm.flushSecondaryRow(row);
        }
    }

    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        if (field.getMappedBy() != null && !field.isBiMTo1JT())
            return;
        
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
            insert(sm, rm, map, store);
            return;
        }

        ValueMapping key = field.getKeyMapping();
        ValueMapping val = field.getElementMapping();
        StoreContext ctx = store.getContext();
        OpenJPAStateManager keysm, valsm;

        // update the changes; note that we have to model changes as
        // delete-then-insert if we have a foreign key action, because
        // secondary row updates aren't part of the constraint graph
        Collection change = ct.getChanged();
        boolean canChange = val.getForeignKey().isLogical();
        Object mkey;
        if (canChange && !change.isEmpty()) {
            Row changeRow = null;
            if (!field.isUni1ToMFK()) {
                changeRow = rm.getSecondaryRow(field.getTable(),
                    Row.ACTION_UPDATE);
                changeRow.whereForeignKey(field.getJoinForeignKey(), sm);
            }
            for (Iterator itr = change.iterator(); itr.hasNext();) {
                mkey = itr.next();
                Object mval = map.get(mkey);
                if (mval == null) {
                    Set<Map.Entry> entries = map.entrySet();
                    for (Map.Entry entry : entries) {
                        if (entry.getKey().equals(mkey))
                            mval = entry.getValue();
                    }
                }
                if (mval == null)
                    continue;
                keysm = RelationStrategies.getStateManager(mkey, ctx);
                valsm = RelationStrategies.getStateManager(mval, ctx);
                key.whereForeignKey(changeRow, keysm);
                if (field.isUni1ToMFK()){
                    changeRow = rm.getRow(field.getElementMapping().getDeclaredTypeMapping().getTable(),
                        Row.ACTION_UPDATE, valsm, true);
                    changeRow.wherePrimaryKey(valsm);
                    val.setForeignKey(changeRow, sm);
                } else {
                    val.setForeignKey(changeRow, valsm);
                    rm.flushSecondaryRow(changeRow);
                }
            }
        }

        // delete the removes
        Collection rem = ct.getRemoved();
        if (!rem.isEmpty() || (!canChange && !change.isEmpty())) {
            Row delRow = null;
            if (!field.isUni1ToMFK()) {
                delRow = rm.getSecondaryRow(field.getTable(),
                    Row.ACTION_DELETE);
                delRow.whereForeignKey(field.getJoinForeignKey(), sm);
            }

            for (Iterator itr = rem.iterator(); itr.hasNext();) {
                Object pc = itr.next();
                if (field.isUni1ToMFK()){
                    updateSetNull(sm, rm, pc);
                } else {
                    keysm = RelationStrategies.getStateManager(pc, ctx);
                    key.whereForeignKey(delRow, keysm);
                    rm.flushSecondaryRow(delRow);
                }
            }
            if (!canChange && !change.isEmpty()) {
                for (Iterator itr = change.iterator(); itr.hasNext();) {
                    Object pc = itr.next();
                    if (field.isUni1ToMFK()){
                        updateSetNull(sm, rm, pc);
                    } else { 
                        keysm = RelationStrategies.getStateManager(pc, ctx);
                        key.whereForeignKey(delRow, keysm);
                        rm.flushSecondaryRow(delRow);
                    }
                }
            }
        }

        // insert the adds
        Collection add = ct.getAdded();
        if (!add.isEmpty() || (!canChange && !change.isEmpty())) {
            Row addRow = null;
            if (!field.isUni1ToMFK()) {
                addRow = rm.getSecondaryRow(field.getTable(),
                    Row.ACTION_INSERT);
                addRow.setForeignKey(field.getJoinForeignKey(),
                    field.getJoinColumnIO(), sm);
            }
            for (Iterator itr = add.iterator(); itr.hasNext();) {
                mkey = itr.next();
                Object mval = map.get(mkey);
                if (mval == null) {
                    Set<Map.Entry> entries = map.entrySet();
                    for (Map.Entry entry : entries) {
                        if (entry.getKey().equals(mkey))
                            mval = entry.getValue();
                    }
                }
                if (mval == null)
                    continue;
                keysm = RelationStrategies.getStateManager(mkey, ctx);
                valsm = RelationStrategies.getStateManager(mval, ctx);
                if (field.isUni1ToMFK()){
                    addRow = rm.getRow(field.getElementMapping().getDeclaredTypeMapping().getTable(),
                        Row.ACTION_UPDATE, valsm, true);
                    addRow.wherePrimaryKey(valsm);
                    key.setForeignKey(addRow, keysm);
                    val.setForeignKey(addRow, sm);
                } else {
                    key.setForeignKey(addRow, keysm);
                    val.setForeignKey(addRow, valsm);
                    rm.flushSecondaryRow(addRow);
                }
            }
            if (!canChange && !change.isEmpty()) {
                for (Iterator itr = change.iterator(); itr.hasNext();) {
                    mkey = itr.next();
                    Object mval = map.get(mkey);
                    if (mval == null) {
                        Set<Map.Entry> entries = map.entrySet();
                        for (Map.Entry entry : entries) {
                            if (entry.getKey().equals(mkey))
                                mval = entry.getValue();
                        }
                    }
                    if (mval == null)
                        continue;
                    keysm = RelationStrategies.getStateManager(mkey, ctx);
                    valsm = RelationStrategies.getStateManager(mval, ctx);
                    if (field.isUni1ToMFK()){
                        addRow = rm.getRow(field.getElementMapping().getDeclaredTypeMapping().getTable(),
                            Row.ACTION_UPDATE, valsm, true);
                        addRow.wherePrimaryKey(valsm);
                        key.setForeignKey(addRow, keysm);
                        val.setForeignKey(addRow, sm);
                    } else {
                        key.setForeignKey(addRow, keysm);
                        val.setForeignKey(addRow, valsm);
                        rm.flushSecondaryRow(addRow);
                    }
                }
            }
        }
    }

    public Joins joinRelation(Joins joins, boolean forceOuter,
        boolean traverse) {
        ValueMapping val = field.getElementMapping();
        ClassMapping[] clss = val.getIndependentTypeMappings();
        if (clss.length != 1) {
            if (traverse)
                throw RelationStrategies.unjoinable(val);
            return joins;
        }
        ForeignKey fk = val.getForeignKey(clss[0]);
        if (fk == null)
            return joins;
        if (forceOuter)
            return joins.outerJoinRelation(field.getName(),
                fk, clss[0], val.getSelectSubclasses(),
                false, false);
        return joins.joinRelation(field.getName(),
            fk, clss[0], val.getSelectSubclasses(),
            false, false);
    }

    public Joins joinKeyRelation(Joins joins, boolean forceOuter,
        boolean traverse) {
        ValueMapping key = field.getKeyMapping();
        ClassMapping[] clss = key.getIndependentTypeMappings();
        if (clss.length != 1) {
            if (traverse)
                throw RelationStrategies.unjoinable(key);
            return joins;
        }
        if (forceOuter)
            return joins.outerJoinRelation(field.getName(),
                key.getForeignKey(clss[0]), clss[0], key.getSelectSubclasses(),
                false, false);
        return joins.joinRelation(_keyRelationName,
            key.getForeignKey(clss[0]), clss[0], key.getSelectSubclasses(), 
            false, false);
    }

    public Object toDataStoreValue(Object val, JDBCStore store) {
        return RelationStrategies.toDataStoreValue(field.getElementMapping(),
            val, store);
    }

    public Object toKeyDataStoreValue(Object val, JDBCStore store) {
        return RelationStrategies.toDataStoreValue(field.getKeyMapping(),
            val, store);
    }
    
    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        if (field.isUni1ToMFK()) {
            Map mapObj = (Map)sm.fetchObject(field.getIndex());
            updateSetNull(sm, store, rm, mapObj.keySet());
            return;
        }    
        super.delete(sm, store, rm);
    }
    
    private void updateSetNull(OpenJPAStateManager sm, JDBCStore store, RowManager rm,
        Set rem) throws SQLException {
        for (Iterator itr = rem.iterator(); itr.hasNext();) {
            Object mkey = itr.next();
            updateSetNull(sm, rm, mkey);
        }
    }
    
    private void updateSetNull(OpenJPAStateManager sm, RowManager rm, Object mkey) 
        throws SQLException {
        StoreContext ctx = sm.getContext();
        ValueMapping key = field.getKeyMapping();
        ValueMapping val = field.getElementMapping();
        OpenJPAStateManager keysm = RelationStrategies.getStateManager(mkey, ctx);
        Row delRow = rm.getRow(field.getElementMapping().getDeclaredTypeMapping().getTable(),
                Row.ACTION_UPDATE, sm, true);
        ValueMappingInfo vinfo = field.getElementMapping().getValueInfo();
        Table table = vinfo.getTable(val);
        ForeignKey joinFK = field.getMappingInfo().getJoinForeignKey(field, table, true);
        delRow.whereForeignKey(joinFK, sm);
        delRow.whereForeignKey(key.getForeignKey(), keysm);
        val.setForeignKey(delRow, null);
        key.setForeignKey(delRow, null);
    }
}
