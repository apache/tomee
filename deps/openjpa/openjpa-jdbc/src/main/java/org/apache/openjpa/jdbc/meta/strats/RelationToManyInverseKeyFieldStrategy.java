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
import java.util.Collection;
import java.util.Iterator;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.FieldMappingInfo;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.meta.ValueMappingInfo;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.ChangeTracker;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.MetaDataException;
import org.apache.openjpa.util.Proxies;
import org.apache.openjpa.util.Proxy;

/**
 * Maps a relation to a set of other objects using an inverse
 * foreign key in the related object table.
 *
 * @author Abe White
 */
public abstract class RelationToManyInverseKeyFieldStrategy
    extends StoreCollectionFieldStrategy {

    private static final Localizer _loc = Localizer.forPackage
        (RelationToManyInverseKeyFieldStrategy.class);

    private boolean _orderInsert = false;
    private boolean _orderUpdate = false;
    private boolean _uni1MFK = false;

    protected ClassMapping[] getIndependentElementMappings(boolean traverse) {
        return field.getElementMapping().getIndependentTypeMappings();
    }

    protected ForeignKey getJoinForeignKey(ClassMapping elem) {
        return field.getElementMapping().getForeignKey(elem);
    }

    protected void selectElement(Select sel, ClassMapping elem,
        JDBCStore store, JDBCFetchConfiguration fetch, int eagerMode,
        Joins joins) {
        sel.select(elem, field.getElementMapping().getSelectSubclasses(),
            store, fetch, eagerMode, joins);
    }

    protected Object loadElement(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res, Joins joins)
        throws SQLException {
        ClassMapping elem = res.getBaseMapping();
        if (elem == null)
            elem = field.getElementMapping().getIndependentTypeMappings()[0];
        return res.load(elem, store, fetch, joins);
    }

    protected Joins join(Joins joins, ClassMapping elem) {
        ValueMapping vm = field.getElementMapping();
        ForeignKey fk = vm.getForeignKey(elem);
        ClassMapping owner = field.getDefiningMapping();
        while (fk.getPrimaryKeyTable() != owner.getTable()) {
            joins = owner.joinSuperclass(joins, false);
            owner = owner.getJoinablePCSuperclassMapping(); 
            if (owner == null)
                throw new InternalException();
        }
        return joins.joinRelation(field.getName(), fk, elem, 
            vm.getSelectSubclasses(), true, true);
    }

    protected Joins joinElementRelation(Joins joins, ClassMapping elem) {
        return joinRelation(joins, false, false);
    }

    public void map(boolean adapt) {
        OpenJPAConfiguration conf = field.getRepository().getConfiguration();
        boolean isNonDefaultMappingAllowed = field.getRepository().
            getMetaDataFactory().getDefaults().isNonDefaultMappingAllowed(conf);
        FieldMapping mapped = field.getMappedByMapping();

        // JPA 2.0 allows non-default mapping: Uni-/1-M/@JoinColumn ==> foreign key strategy
        // Bi-/1-M/@JoinColumn should result in exception 
        if (!isNonDefaultMappingAllowed || mapped != null) {
            field.getValueInfo().assertNoSchemaComponents(field, !adapt);
        }
        field.getKeyMapping().getValueInfo().assertNoSchemaComponents
            (field.getKey(), !adapt);

        ValueMapping elem = field.getElementMapping();
        if (elem.getTypeCode() != JavaTypes.PC || elem.isEmbeddedPC()
            || !elem.getTypeMapping().isMapped())
            throw new MetaDataException(_loc.get("not-elem-relation", field));

        // check for named inverse
        FieldMappingInfo finfo = field.getMappingInfo();
        ValueMappingInfo vinfo = elem.getValueInfo();
        boolean criteria = vinfo.getUseClassCriteria();
        if (mapped != null) {
            mapped.resolve(mapped.MODE_META | mapped.MODE_MAPPING);
            if (!(mapped.getStrategy() instanceof RelationFieldStrategy 
               || mapped.getHandler() instanceof UntypedPCValueHandler))
                throw new MetaDataException(_loc.get("not-inv-relation",
                    field, mapped));
            vinfo.assertNoSchemaComponents(elem, !adapt);
            elem.setForeignKey(mapped.getForeignKey
                (field.getDefiningMapping()));
            elem.setColumns(mapped.getDefiningMapping().
                getPrimaryKeyColumns());
            elem.setJoinDirection(ValueMapping.JOIN_EXPECTED_INVERSE);
            elem.setUseClassCriteria(criteria);

            ForeignKey fk = mapped.getForeignKey();
            /** Foreign key may be null if declared type of the mapped field is 
             * abstract and under table-per-class inheritance strategy will have
             * no mapped table.  
             */
            if (fk != null) {
            	field.setOrderColumn(finfo.getOrderColumn(field,
            			fk.getTable(), adapt));
            	field.setOrderColumnIO(finfo.getColumnIO());
            }
            return;
        } else { 
            if (field.getValueInfo().getColumns().size() > 0 && 
                field.getAccessType() == FieldMetaData.ONE_TO_MANY) {
                _uni1MFK = true;
            }
        }

        // map inverse foreign key in related table
        ForeignKey fk = vinfo.getInverseTypeJoin(elem, field.getName(), adapt);
        if (_uni1MFK) {
            Column[] locals = fk.getColumns();
            for (int i = 0; i < locals.length; i++)
                locals[i].setUni1MFK(true);
        }
        elem.setForeignKey(fk);
        elem.setColumnIO(vinfo.getColumnIO());
        elem.setColumns(elem.getTypeMapping().getPrimaryKeyColumns());
        elem.setJoinDirection(ValueMapping.JOIN_EXPECTED_INVERSE);
        elem.setUseClassCriteria(criteria);
        elem.mapConstraints(field.getName(), adapt);

        field.setOrderColumn(finfo.getOrderColumn(field, fk.getTable(),
            adapt));
        field.setOrderColumnIO(finfo.getColumnIO());
    }

    public void initialize() {
        Column order = field.getOrderColumn();
        _orderInsert = field.getOrderColumnIO().isInsertable(order, false);
        _orderUpdate = field.getOrderColumnIO().isUpdatable(order, false);

        ValueMapping elem = field.getElementMapping();
        Log log = field.getRepository().getLog();
        if (field.getMappedBy() == null
            && elem.getUseClassCriteria() && log.isWarnEnabled()) {
            ForeignKey fk = elem.getForeignKey();
            if (elem.getColumnIO().isAnyUpdatable(fk, false))
                log.warn(_loc.get("class-crit-owner", field));
        }
    }

    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        if (field.getMappedBy() == null || _orderInsert || _orderUpdate)
            insert(sm, rm, sm.fetchObject(field.getIndex()));
    }

    private void insert(OpenJPAStateManager sm, RowManager rm, Object vals)
        throws SQLException {
        if (field.getMappedBy() != null && !_orderInsert && !_orderUpdate)
            return;
        Collection coll = toCollection(vals);
        if (coll == null || coll.isEmpty())
            return;

        ClassMapping rel = field.getElementMapping().getTypeMapping();
        int idx = 0;
        for (Iterator itr = coll.iterator(); itr.hasNext(); idx++)
            updateInverse(sm.getContext(), itr.next(), rel, rm, sm, idx);
    }

    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        if (field.getMappedBy() != null && !_orderInsert && !_orderUpdate)
            return;

        Object obj = sm.fetchObject(field.getIndex());
        ChangeTracker ct = null;
        if (obj instanceof Proxy) {
            Proxy proxy = (Proxy) obj;
            if (Proxies.isOwner(proxy, sm, field.getIndex()))
                ct = proxy.getChangeTracker();
        }
        Column order = field.getOrderColumn();

        // if no fine-grained change tracking then just delete and reinsert
        // if no fine-grained change tracking or if an item was removed
        // from an ordered collection, delete and reinsert
        if (ct == null || !ct.isTracking() ||
            (order != null && !ct.getRemoved().isEmpty())) {
            delete(sm, store, rm);
            insert(sm, rm, obj);
            return;
        }

        // null inverse columns for deletes and update them with our oid for
        // inserts
        ClassMapping rel = field.getElementMapping().getTypeMapping();
        StoreContext ctx = store.getContext();
        if (field.getMappedBy() == null) {
            Collection rem = ct.getRemoved();
            for (Iterator itr = rem.iterator(); itr.hasNext();)
                updateInverse(ctx, itr.next(), rel, rm, null, 0);
        }

        Collection add = ct.getAdded();
        int seq = ct.getNextSequence();
        for (Iterator itr = add.iterator(); itr.hasNext(); seq++)
            updateInverse(ctx, itr.next(), rel, rm, sm, seq);
        if (order != null)
            ct.setNextSequence(seq);
    }

    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        if (field.getMappedBy() != null)
            return;

        // if nullable, null any existing inverse columns that refer to this obj
        ValueMapping elem = field.getElementMapping();
        ColumnIO io = elem.getColumnIO();
        ForeignKey fk = elem.getForeignKey();
        if (!elem.getUseClassCriteria() && io.isAnyUpdatable(fk, true)) { 
            assertInversable();
            Row row = rm.getAllRows(fk.getTable(), Row.ACTION_UPDATE);
            row.setForeignKey(fk, io, null);
            row.whereForeignKey(fk, sm);
            rm.flushAllRows(row);
            return;
        }

        if (!sm.getLoaded().get(field.getIndex()))
            return;

        // update fk on each field value row
        ClassMapping rel = field.getElementMapping().getTypeMapping();
        StoreContext ctx = store.getContext();
        Collection objs = toCollection(sm.fetchObject(field.getIndex()));
        if (objs != null && !objs.isEmpty())
            for (Iterator itr = objs.iterator(); itr.hasNext();)
                updateInverse (ctx, itr.next(), rel, rm, sm, 0);
    }

    /**
     * This method updates the inverse columns of a 1-M related object
     * with the given oid.
     */
    private void updateInverse(StoreContext ctx, Object inverse,
        ClassMapping rel, RowManager rm, OpenJPAStateManager sm, int idx)
        throws SQLException {
        OpenJPAStateManager invsm = RelationStrategies.getStateManager(inverse,
            ctx);
        if (invsm == null)
            return;

        ValueMapping elem = field.getElementMapping();
        ForeignKey fk = elem.getForeignKey();
        ColumnIO io = elem.getColumnIO();
        Column order = field.getOrderColumn();

        int action;
        boolean writeable;
        boolean orderWriteable;
        if (invsm.isNew() && !invsm.isFlushed()) {
            // no need to null inverse columns of new instance
            if (sm == null || sm.isDeleted())
                return;
            writeable = io.isAnyInsertable(fk, false);
            orderWriteable = _orderInsert;
            action = Row.ACTION_INSERT;
        } else if (invsm.isDeleted()) {
            // no need to null inverse columns of deleted instance
            if (invsm.isFlushed() || sm == null || !sm.isDeleted())
                return;
            writeable = true;
            orderWriteable = false;
            action = Row.ACTION_DELETE;
        } else {
            if (sm != null && sm.isDeleted())
                sm = null;
            writeable = io.isAnyUpdatable(fk, sm == null);
            orderWriteable = field.getOrderColumnIO().isUpdatable
                (order, sm == null);
            action = Row.ACTION_UPDATE;
        }
        if (!writeable && !orderWriteable)
            return;

        assertInversable();

        // if this is an update, this might be the only mod to the row, so
        // make sure the where condition is set
        Row row = rm.getRow(fk.getTable(), action, invsm, true);
        if (action == Row.ACTION_UPDATE)
            row.wherePrimaryKey(invsm);

        // update the inverse pointer with our oid value
        if (writeable)
            row.setForeignKey(fk, io, sm);
        if (orderWriteable)
            row.setInt(order, idx);
    }

    public Object toDataStoreValue(Object val, JDBCStore store) {
        ClassMapping cm = field.getElementMapping().getTypeMapping();
        return cm.toDataStoreValue(val, cm.getPrimaryKeyColumns(), store);
    }

    public Joins join(Joins joins, boolean forceOuter) {
        ValueMapping elem = field.getElementMapping();
        ClassMapping[] clss = elem.getIndependentTypeMappings();
        if (clss.length != 1)
            throw RelationStrategies.unjoinable(elem);
        if (forceOuter)
            return joins.outerJoinRelation(field.getName(), 
                elem.getForeignKey(clss[0]), clss[0],
                elem.getSelectSubclasses(), true, true);
        return joins.joinRelation(field.getName(), elem.getForeignKey(clss[0]),
            clss[0], elem.getSelectSubclasses(), true, true);
    }

    private void assertInversable() {
        ValueMapping elem = field.getElementMapping();
        if (elem.getIndependentTypeMappings().length != 1)
            throw RelationStrategies.uninversable(elem);
    }
}
