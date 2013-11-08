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

import org.apache.openjpa.kernel.*;
import org.apache.openjpa.util.*;
import org.apache.openjpa.meta.*;
import org.apache.openjpa.jdbc.meta.*;
import org.apache.openjpa.jdbc.kernel.*;
import org.apache.openjpa.jdbc.schema.*;
import org.apache.openjpa.jdbc.sql.*;

/**
 * <p>Mapping for a collection of values in a separate table controlled by a
 * {@link ValueHandler}.</p>
 *
 * @author Abe White
 * @since 0.4.0, 1.1.0
 */
public class HandlerCollectionTableFieldStrategy
    extends StoreCollectionFieldStrategy
    implements LRSCollectionFieldStrategy {

    private static final Localizer _loc = Localizer.forPackage
        (HandlerCollectionTableFieldStrategy.class);

    private Column[] _cols = null;
    private ColumnIO _io = null;
    private boolean _load = false;
    private boolean _lob = false;
    private boolean _embed = false;

    public FieldMapping getFieldMapping() {
        return field;
    }

    public ClassMapping[] getIndependentElementMappings(boolean traverse) {
        return ClassMapping.EMPTY_MAPPINGS;
    }

    public Column[] getElementColumns(ClassMapping elem) {
        return _cols;
    }

    public ForeignKey getJoinForeignKey(ClassMapping elem) {
        return field.getJoinForeignKey();
    }

    public void selectElement(Select sel, ClassMapping elem, JDBCStore store,
        JDBCFetchConfiguration fetch, int eagerMode, Joins joins) {
        sel.select(_cols, joins);
    }

    public Object loadElement(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res, Joins joins)
        throws SQLException {
        return HandlerStrategies.loadObject(field.getElementMapping(),
            sm, store, fetch, res, joins, _cols, _load);
    }

    protected Joins join(Joins joins, ClassMapping elem) {
        return join(joins, false);
    }

    public Joins joinElementRelation(Joins joins, ClassMapping elem) {
        return joinRelation(joins, false, false);
    }

    protected Proxy newLRSProxy() {
        return new LRSProxyCollection(this);
    }

    public void map(boolean adapt) {
        if (field.getTypeCode() != JavaTypes.COLLECTION
            && field.getTypeCode() != JavaTypes.ARRAY)
            throw new MetaDataException(_loc.get("not-coll", field));

        assertNotMappedBy();
        field.getValueInfo().assertNoSchemaComponents(field, !adapt);
        field.getKeyMapping().getValueInfo().assertNoSchemaComponents
            (field.getKey(), !adapt);

        ValueMapping elem = field.getElementMapping();
        if (elem.getHandler() == null)
            throw new MetaDataException(_loc.get("no-handler", elem));

        field.mapJoin(adapt, true);
        _io = new ColumnIO();
        _cols = HandlerStrategies.map(elem, "element", _io, adapt);

        FieldMappingInfo finfo = field.getMappingInfo();
        Column orderCol = finfo.getOrderColumn(field, field.getTable(), adapt);
        field.setOrderColumn(orderCol);
        field.setOrderColumnIO(finfo.getColumnIO());
        field.mapPrimaryKey(adapt);
    }

    public void initialize() {
        for (int i = 0; !_lob && i < _cols.length; i++)
            _lob = _cols[i].isLob();

        ValueMapping elem = field.getElementMapping();
        _embed = elem.getEmbeddedMetaData() != null;
        _load = elem.getHandler().objectValueRequiresLoad(elem);
    }

    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        insert(sm, store, rm, sm.fetchObject(field.getIndex()));
    }

    private void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm,
        Object vals)
        throws SQLException {
        Collection coll;
        if (field.getTypeCode() == JavaTypes.ARRAY)
            coll = JavaTypes.toList(vals, field.getElement().getType(),
                false);
        else
            coll = (Collection) vals;
        if (coll == null || coll.isEmpty())
            return;

        Row row = rm.getSecondaryRow(field.getTable(), Row.ACTION_INSERT);
        row.setForeignKey(field.getJoinForeignKey(), field.getJoinColumnIO(),
            sm);

        StoreContext ctx = sm.getContext();
        ValueMapping elem = field.getElementMapping();
        Column order = field.getOrderColumn();
        boolean setOrder = field.getOrderColumnIO().isInsertable(order, false);
        int idx = 0;
        for (Iterator itr = coll.iterator(); itr.hasNext(); idx++) {
            Object val = itr.next();
            HandlerStrategies.set(elem, val, store, row, _cols,
                _io, true);
            StateManagerImpl esm = (StateManagerImpl)ctx.getStateManager(val);
            if (esm != null) {
                boolean isEmbedded = esm.isEmbedded();
                Collection rels = new ArrayList();
                if (isEmbedded) {
                    getRelations(esm, rels, ctx);
                    Map<ClassMapping,Integer> targets = new HashMap<ClassMapping,Integer>();
                    for (Object rel : rels) {
                        StateManagerImpl relSm = (StateManagerImpl)rel;
                        ClassMapping cm =(ClassMapping) relSm.getMetaData();
                        if(!targets.containsKey(cm)){
                            targets.put(cm, 0);
                        }
                        Integer n = targets.get(cm);
                        elem.setForeignKey(row, (StateManagerImpl)rel, n);
                        n++;
                        targets.put(cm, n);
                    }
                }
            }
            if (setOrder)
                row.setInt(order, idx);
            rm.flushSecondaryRow(row);
        }
    }
    
    private void getRelations(StateManagerImpl sm, Collection rels, 
        StoreContext ctx) {
        FieldMetaData[] fields = sm.getMetaData().getFields();
        for (int i = 0; i < fields.length; i++) {
            Object obj = sm.fetch(i);
            StateManagerImpl esm = (StateManagerImpl)ctx.getStateManager(obj);
            if (esm != null) {
                if (!esm.isEmbedded())
                    rels.add(esm);
                else
                    getRelations(esm, rels, ctx);
            }    
        }
    }    

    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        Object obj = sm.fetchObject(field.getIndex());
        ChangeTracker ct = null;
        if (obj instanceof Proxy) {
            Proxy proxy = (Proxy) obj;
            if (Proxies.isOwner(proxy, sm, field.getIndex()))
                ct = proxy.getChangeTracker();
        }

        Column order = field.getOrderColumn();

        // if no fine-grained change tracking or if an item was removed
        // from an ordered collection, delete and reinsert
        if (ct == null || !ct.isTracking() ||
            (order != null && !ct.getRemoved().isEmpty())) {
            delete(sm, store, rm);
            insert(sm, store, rm, obj);
            return;
        }

        // delete the removes
        ValueMapping elem = field.getElementMapping();
        Collection rem = ct.getRemoved();
        if (!rem.isEmpty()) {
            Row delRow = rm.getSecondaryRow(field.getTable(),
                Row.ACTION_DELETE);
            delRow.whereForeignKey(field.getJoinForeignKey(), sm);
            for (Iterator itr = rem.iterator(); itr.hasNext();) {
                HandlerStrategies.where(elem, itr.next(), store, delRow,
                    _cols);
                rm.flushSecondaryRow(delRow);
            }
        }

        // insert the adds
        Collection add = ct.getAdded();
        if (!add.isEmpty()) {
            Row addRow = rm.getSecondaryRow(field.getTable(),
                Row.ACTION_INSERT);
            addRow.setForeignKey(field.getJoinForeignKey(),
                field.getJoinColumnIO(), sm);

            int seq = ct.getNextSequence();
            boolean setOrder = field.getOrderColumnIO().isInsertable(order,
                false);
            for (Iterator itr = add.iterator(); itr.hasNext(); seq++) {
                HandlerStrategies.set(elem, itr.next(), store, addRow, _cols,
                    _io, true);
                if (setOrder)
                    addRow.setInt(order, seq);
                rm.flushSecondaryRow(addRow);
            }
            if (order != null)
                ct.setNextSequence(seq);
        }
    }

    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        Row row = rm.getAllRows(field.getTable(), Row.ACTION_DELETE);
        row.whereForeignKey(field.getJoinForeignKey(), sm);
        rm.flushAllRows(row);
    }

    public int supportsSelect(Select sel, int type, OpenJPAStateManager sm,
        JDBCStore store, JDBCFetchConfiguration fetch) {
        // can't do any combined select with lobs, since they don't allow
        // select distinct.  cant select eager parallel on embedded, because
        // during parallel result processing the owning sm won't be available
        // for each elem
        if (_lob || (_embed && type == Select.EAGER_PARALLEL))
            return 0;
        return super.supportsSelect(sel, type, sm, store, fetch);
    }

    public Object toDataStoreValue(Object val, JDBCStore store) {
        return HandlerStrategies.toDataStoreValue(field.getElementMapping(),
            val, _cols, store);
    }

    public Joins join(Joins joins, boolean forceOuter) {
        return field.join(joins, forceOuter, true);
    }

    public Joins joinRelation(Joins joins, boolean forceOuter,
        boolean traverse) {
        if (traverse)
            HandlerStrategies.assertJoinable(field.getElementMapping());
        return joins;
    }
}
