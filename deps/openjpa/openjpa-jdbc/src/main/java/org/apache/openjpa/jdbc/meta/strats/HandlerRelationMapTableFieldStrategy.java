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
 * <p>Mapping for a map whose keys are controlled by a {@link ValueHandler}
 * and whose values are relations to other persistent objects.</p>
 *
 * @author Abe White
 * @since 0.4.0, 1.1.0
 */
public class HandlerRelationMapTableFieldStrategy
    extends MapTableFieldStrategy {

    private static final Localizer _loc = Localizer.forPackage
        (HandlerRelationMapTableFieldStrategy.class);

    private Column[] _kcols = null;
    private ColumnIO _kio = null;
    private boolean _kload = false;

    public Column[] getKeyColumns(ClassMapping cls) {
        return _kcols;
    }

    public ColumnIO getKeyColumnIO() {
        return _kio;
    }

    public Column[] getValueColumns(ClassMapping cls) {
        return field.getElementMapping().getColumns();
    }

    public void selectKey(Select sel, ClassMapping key, OpenJPAStateManager sm,
        JDBCStore store, JDBCFetchConfiguration fetch, Joins joins) {
        sel.select(_kcols, joins);
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
        ValueMapping elem = field.getElementMapping();
        final ClassMapping[] vals = elem.getIndependentTypeMappings();
        Union union = store.getSQLFactory().newUnion(vals.length);
        if (fetch.getSubclassFetchMode(elem.getTypeMapping())
            != JDBCFetchConfiguration.EAGER_JOIN)
            union.abortUnion();
        union.setLRS(lrs);
        union.select(new Union.Selector() {
            public void select(Select sel, int idx) {
                sel.select(_kcols);
                if (field.isUni1ToMFK()) {
                    sel.whereForeignKey(field.getElementMapping().getForeignKey(),
                        sm.getObjectId(), field.getElementMapping().getDeclaredTypeMapping(), store);
                    sel.select(vals[idx], field.getElementMapping().
                            getSelectSubclasses(), store, fetch, eagerMode, null);
                } else {
                    sel.whereForeignKey(field.getJoinForeignKey(),
                        sm.getObjectId(), field.getDefiningMapping(), store);
                    Joins joins = joinValueRelation(sel.newJoins(), vals[idx]);
                    sel.select(vals[idx], field.getElementMapping().
                        getSelectSubclasses(), store, fetch, eagerMode, joins);

                    //### cheat: result joins only care about the relation path;
                    //### thus we can use first mapping of union only
                    if (idx == 0)
                        resJoins[1] = joins;
               }
            }
        });
        Result res = union.execute(store, fetch);
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
        ClassMapping val = res.getBaseMapping();
        if (val == null)
            val = field.getElementMapping().getIndependentTypeMappings()[0];
        return res.load(val, store, fetch, joins);
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
        if (key.getHandler() == null)
            throw new MetaDataException(_loc.get("no-handler", key));
        ValueMapping val = field.getElementMapping();
        if (val.getTypeCode() != JavaTypes.PC || val.isEmbeddedPC())
            throw new MetaDataException(_loc.get("not-relation", val));
        
        FieldMapping mapped = field.getMappedByMapping();
        if (field.isUni1ToMFK() || (!field.isBiMTo1JT() && mapped != null)) { 
            // map to the owner table
            handleMappedByForeignKey(adapt);
        } else if (field.isBiMTo1JT() || mapped == null) { 
            // map to a separate table
            field.mapJoin(adapt, true);
            if (val.getTypeMapping().isMapped()) {
                ValueMappingInfo vinfo = val.getValueInfo();
                ForeignKey fk = vinfo.getTypeJoin(val, "value", false, adapt);
                val.setForeignKey(fk);
                val.setColumnIO(vinfo.getColumnIO());
            } else
                RelationStrategies.mapRelationToUnmappedPC(val, "value", adapt);

            val.mapConstraints("value", adapt);
        }
        _kio = new ColumnIO();
        DBDictionary dict = field.getMappingRepository().getDBDictionary();
        _kcols = HandlerStrategies.map(key, 
            dict.getValidColumnName(DBIdentifier.newColumn("key"), field.getTable()).getName(), _kio, adapt);

        field.mapPrimaryKey(adapt);
    }

    public void initialize() {
        _kload = field.getKeyMapping().getHandler().
            objectValueRequiresLoad(field.getKeyMapping());
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
        StoreContext ctx = store.getContext();
        OpenJPAStateManager valsm;
        Map.Entry entry;
        for (Iterator itr = map.entrySet().iterator(); itr.hasNext();) {
            entry = (Map.Entry) itr.next();
            valsm = RelationStrategies.getStateManager(entry.getValue(),
                ctx);
            if (field.isUni1ToMFK()){
                row = rm.getRow(field.getElementMapping().getDeclaredTypeMapping().getTable(),
                    Row.ACTION_UPDATE, valsm, true);
                row.wherePrimaryKey(valsm);
                val.setForeignKey(row, sm);
            } else {
                val.setForeignKey(row, valsm);
            }
            HandlerStrategies.set(key, entry.getKey(), store, row, _kcols,
                    _kio, true);
            
            // So far we populated the key/value of each
            // map element owned by the entity.
            // In the case of ToMany, and both sides
            // use Map to represent the relation,
            // we need to populate the key value of the owner
            // from the view point of the owned side
            PersistenceCapable obj = sm.getPersistenceCapable();
            if (!populateKey(row, valsm, obj, ctx, rm, store)) {
                if (!field.isUni1ToMFK())
                    rm.flushSecondaryRow(row);
            }
        }
    }
    
    public void setKey(Object keyObj, JDBCStore store, Row row) 
        throws SQLException {
        ValueMapping key = field.getKeyMapping();
        HandlerStrategies.set(key, keyObj, store, row, _kcols,
            _kio, true);
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
            insert(sm, store, rm, map);
            return;
        }

        ValueMapping key = field.getKeyMapping();
        ValueMapping val = field.getElementMapping();
        StoreContext ctx = store.getContext();
        OpenJPAStateManager valsm;

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
                valsm = RelationStrategies.getStateManager(map.get(mkey), ctx);
                if (field.isUni1ToMFK()){
                    changeRow = rm.getRow(field.getElementMapping().getDeclaredTypeMapping().getTable(),
                        Row.ACTION_UPDATE, valsm, true);
                    changeRow.wherePrimaryKey(valsm);
                    val.setForeignKey(changeRow, sm);
                } else {
                    val.setForeignKey(changeRow, valsm);
                }
                
                HandlerStrategies.where(key, mkey, store, changeRow, _kcols);
                if (!field.isUni1ToMFK())
                    rm.flushSecondaryRow(changeRow);
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
                mkey = itr.next();
                if (field.isUni1ToMFK()){
                    updateSetNull(sm, mkey, store, rm);
                } else {
                    HandlerStrategies.where(key, mkey, store, delRow, _kcols);
                    rm.flushSecondaryRow(delRow);
                }
            }
            if (!canChange && !change.isEmpty()) {
                for (Iterator itr = change.iterator(); itr.hasNext();) {
                    mkey = itr.next();
                    if (field.isUni1ToMFK()){
                        updateSetNull(sm, mkey, store, rm);
                    } else { 
                        HandlerStrategies.where(key, mkey, store, delRow,  _kcols);
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
                valsm = RelationStrategies.getStateManager(map.get(mkey), ctx);
                if (field.isUni1ToMFK()){
                    addRow = rm.getRow(field.getElementMapping().getDeclaredTypeMapping().getTable(),
                        Row.ACTION_UPDATE, valsm, true);
                    addRow.wherePrimaryKey(valsm);
                    val.setForeignKey(addRow, sm);
                } else {
                    val.setForeignKey(addRow, valsm);
                }
                
                HandlerStrategies.set(key, mkey, store, addRow, _kcols,
                    _kio, true);
                if (!field.isUni1ToMFK())
                    rm.flushSecondaryRow(addRow);
            }
            if (!canChange && !change.isEmpty()) {
                for (Iterator itr = change.iterator(); itr.hasNext();) {
                    mkey = itr.next();
                    valsm = RelationStrategies.getStateManager(map.get(mkey), ctx);
                    if (field.isUni1ToMFK()){
                        addRow = rm.getRow(field.getElementMapping().getDeclaredTypeMapping().getTable(),
                            Row.ACTION_UPDATE, valsm, true);
                        addRow.wherePrimaryKey(valsm);
                        val.setForeignKey(addRow, sm);
                    } else {
                        val.setForeignKey(addRow, valsm);
                    }
                    
                    HandlerStrategies.set(key, mkey, store, addRow, _kcols,
                        _kio, true);
                    if (!field.isUni1ToMFK())
                        rm.flushSecondaryRow(addRow);
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
        if (traverse)
            HandlerStrategies.assertJoinable(field.getKeyMapping());
        return joins;
    }

    public Object toDataStoreValue(Object val, JDBCStore store) {
        return RelationStrategies.toDataStoreValue(field.getElementMapping(),
            val, store);
    }

    public Object toKeyDataStoreValue(Object val, JDBCStore store) {
        return HandlerStrategies.toDataStoreValue(field.getKeyMapping(), val,
            _kcols, store);
    }
    
    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        if ((field.getMappedBy() != null && !field.isBiMTo1JT()))
            return;
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
            updateSetNull(sm, mkey, store, rm);
        }
    }

    private void updateSetNull(OpenJPAStateManager sm, Object mkey, 
        JDBCStore store, RowManager rm) throws SQLException {
        ValueMapping key = field.getKeyMapping();
        ValueMapping val = field.getElementMapping();
        StoreContext ctx = store.getContext();
        ValueMappingInfo vinfo = field.getElementMapping().getValueInfo();
        Table table = vinfo.getTable(val);
        ForeignKey joinFK = field.getMappingInfo().getJoinForeignKey(field, table, true);
        Row delRow = rm.getRow(field.getElementMapping().getDeclaredTypeMapping().getTable(),
            Row.ACTION_UPDATE, sm, true);
        delRow.whereForeignKey(joinFK, sm);
        val.setForeignKey(delRow, null);
        HandlerStrategies.set(key, null, store, delRow, _kcols, _kio, true);
        HandlerStrategies.where(key, mkey, store, delRow, _kcols);
    }
}
