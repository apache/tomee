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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.ReflectingPersistenceCapable;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Embeddable;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.FieldStrategy;
import org.apache.openjpa.jdbc.meta.Joinable;
import org.apache.openjpa.jdbc.meta.MappingInfo;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.meta.ValueMappingImpl;
import org.apache.openjpa.jdbc.meta.ValueMappingInfo;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.PrimaryKey;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.jdbc.sql.SelectExecutor;
import org.apache.openjpa.jdbc.sql.Union;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StateManagerImpl;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.ApplicationIds;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.MetaDataException;
import org.apache.openjpa.util.ObjectId;
import org.apache.openjpa.util.OpenJPAId;
import org.apache.openjpa.util.UnsupportedException;


/**
 * Mapping for a single-valued relation to another entity.
 *
 * @author Abe White
 * @since 0.4.0
 */
public class RelationFieldStrategy
    extends AbstractFieldStrategy
    implements Joinable, Embeddable {

    private static final Localizer _loc = Localizer.forPackage
        (RelationFieldStrategy.class);

    private Boolean _fkOid = null;
    
    public void map(boolean adapt) {
        if (field.getTypeCode() != JavaTypes.PC || field.isEmbeddedPC())
            throw new MetaDataException(_loc.get("not-relation", field));

        field.getKeyMapping().getValueInfo().assertNoSchemaComponents
            (field.getKey(), !adapt);
        if (!field.isNonDefaultMappingUsingJoinTableStrategy())
            field.getElementMapping().getValueInfo().assertNoSchemaComponents
                (field.getElement(), !adapt);
        boolean criteria = field.getValueInfo().getUseClassCriteria();

        // check for named inverse
        FieldMapping mapped = field.getMappedByMapping();
        if (mapped != null) {
            field.getMappingInfo().assertNoSchemaComponents(field, !adapt);
            field.getValueInfo().assertNoSchemaComponents(field, !adapt);
            mapped.resolve(mapped.MODE_META | mapped.MODE_MAPPING);

            if (!mapped.isMapped() || mapped.isSerialized())
                throw new MetaDataException(_loc.get("mapped-by-unmapped",
                    field, mapped));

            if (mapped.getTypeCode() == JavaTypes.PC) {
                if (mapped.getJoinDirection() == mapped.JOIN_FORWARD) {
                    field.setJoinDirection(field.JOIN_INVERSE);
                    field.setColumns(mapped.getDefiningMapping().
                        getPrimaryKeyColumns());
                } else if (isTypeUnjoinedSubclass(mapped))
                    throw new MetaDataException(_loc.get
                        ("mapped-inverse-unjoined", field.getName(),
                            field.getDefiningMapping(), mapped));

                field.setForeignKey(mapped.getForeignKey
                    (field.getDefiningMapping()));
            } else if (mapped.getElement().getTypeCode() == JavaTypes.PC) {
                if (isTypeUnjoinedSubclass(mapped.getElementMapping()))
                    throw new MetaDataException(_loc.get
                        ("mapped-inverse-unjoined", field.getName(),
                            field.getDefiningMapping(), mapped));

                // warn the user about making the collection side the owner
                Log log = field.getRepository().getLog();
                if (log.isInfoEnabled())
                    log.info(_loc.get("coll-owner", field, mapped));
                field.setForeignKey(mapped.getElementMapping().
                    getForeignKey());
            } else
                throw new MetaDataException(_loc.get("not-inv-relation",
                    field, mapped));

            field.setUseClassCriteria(criteria);
            return;
        } 

        // this is necessary to support openjpa 3 mappings, which didn't
        // differentiate between secondary table joins and relations built
        // around an inverse key: check to see if we're mapped as a secondary
        // table join but we're in the table of the related type, and if so
        // switch our join mapping info to our value mapping info
        DBIdentifier tableName = field.getMappingInfo().getTableIdentifier();
        Table table = field.getTypeMapping().getTable();
        ValueMappingInfo vinfo = field.getValueInfo();
        if (!DBIdentifier.isNull(tableName) && table != null
            && (tableName.equals(table.getIdentifier())
            || tableName.equals(table.getFullIdentifier()))) {
            vinfo.setJoinDirection(MappingInfo.JOIN_INVERSE);
            vinfo.setColumns(field.getMappingInfo().getColumns());
            field.getMappingInfo().setTableIdentifier(DBIdentifier.NULL);
            field.getMappingInfo().setColumns(null);
        }
        
        if (!field.isBiMTo1JT())
            field.mapJoin(adapt, false);
        if (field.getTypeMapping().isMapped()) {
            if (field.getMappedByIdValue() != null) 
                setMappedByIdColumns();            
             
            if (!field.isBiMTo1JT()) {
                ForeignKey fk = vinfo.getTypeJoin(field, field.getName(), true,
                    adapt);
                field.setForeignKey(fk);
            }
            field.setColumnIO(vinfo.getColumnIO());
            if (vinfo.getJoinDirection() == vinfo.JOIN_INVERSE)
                field.setJoinDirection(field.JOIN_INVERSE);
        } else
            RelationStrategies.mapRelationToUnmappedPC(field, field.getName(),
                adapt);

        field.setUseClassCriteria(criteria);
        field.mapPrimaryKey(adapt);
        PrimaryKey pk = field.getTable().getPrimaryKey();
        if (field.isPrimaryKey()) {
            Column[] cols = field.getColumns();
            if (pk != null && (adapt || pk.isLogical()))
                for (int i = 0; i < cols.length; i++)
                    pk.addColumn(cols[i]);
            for (int i = 0; i < cols.length; i++)
                field.getDefiningMapping().setJoinable(cols[i], this);
        }

        // map constraints after pk so we don't re-index / re-unique pk col
        field.mapConstraints(field.getName(), adapt);
    }

    /**
     * When there is MappedById annotation, the owner of the one-to-one/
     * many-to-one relationship will use its primary key to represent 
     * foreign key relation. No need to create a separate foreign key
     * column. 
     */
    private void setMappedByIdColumns() {
        ClassMetaData owner = field.getDefiningMetaData();
        FieldMetaData[] pks = owner.getPrimaryKeyFields();
        for (int i = 0; i < pks.length; i++) {
            FieldMapping fm = (FieldMapping) pks[i];
            ValueMappingImpl val = (ValueMappingImpl) field.getValue();
            ValueMappingInfo info = val.getValueInfo();
            if (info.getColumns().size() == 0) 
                info.setColumns(getMappedByIdColumns(fm));
        }
    }

    private List getMappedByIdColumns(FieldMapping pk) {
        ClassMetaData embeddedId = ((ValueMappingImpl)pk.getValue()).
            getEmbeddedMetaData();
        Column[] pkCols = null;
        List cols = new ArrayList();
        String mappedByIdValue = field.getMappedByIdValue();
        if (embeddedId != null) {
            FieldMetaData[] fmds = embeddedId.getFields();
            for (int i = 0; i < fmds.length; i++) {
                if ((fmds[i].getName().equals(mappedByIdValue)) ||
                    mappedByIdValue.length() == 0) {
                    if (fmds[i].getValue().getEmbeddedMetaData() != null) {
                        EmbedValueHandler.getEmbeddedIdCols(
                                (FieldMapping)fmds[i], cols);
                    } else 
                        EmbedValueHandler.getIdColumns(
                                (FieldMapping)fmds[i], cols);
                }
            }
            return cols;
        } else { // primary key is single-value
            Class pkType = pk.getDeclaredType();
            FieldMetaData[] pks = field.getValue().getDeclaredTypeMetaData().
                    getPrimaryKeyFields();
            if (pks.length != 1 || pks[0].getDeclaredType() != pkType)
                return Collections.EMPTY_LIST;
            pkCols = pk.getColumns();
            for (int i = 0; i < pkCols.length; i++)
                cols.add(pkCols[i]);
            return cols;
        }
    }

    /**
     * Return whether our defining mapping is an unjoined subclass of
     * the type of the given value.
     */
    private boolean isTypeUnjoinedSubclass(ValueMapping mapped) {
        ClassMapping def = field.getDefiningMapping();
        for (; def != null; def = def.getJoinablePCSuperclassMapping())
            if (def == mapped.getTypeMapping())
                return false;
        return true;
    }

    public void initialize() {
        field.setUsesIntermediate(true);

        ForeignKey fk = field.getForeignKey();
        if (fk == null)
            _fkOid = Boolean.TRUE;
        else if (field.getJoinDirection() != FieldMapping.JOIN_INVERSE)
            _fkOid = field.getTypeMapping().isForeignKeyObjectId(fk);
    }

    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        if (field.getMappedBy() != null)
            return;
        Row row = null;
        OpenJPAStateManager rel = RelationStrategies.getStateManager
            (sm.fetchObjectField(field.getIndex()), store.getContext());
        // Checks if the field being inserted is a MapsId field and 
        // the related object is using auto-assigned identity
        // If the above conditions are satisfied and the related instance has
        // already been inserted in the RowManger, then returns without further
        // processing
        if (sm instanceof StateManagerImpl) {
	        List<FieldMetaData> mappedByIdFields = ((StateManagerImpl)sm).getMappedByIdFields();
	        if (rel != null && ((ClassMapping)rel.getMetaData()).getTable().getAutoAssignedColumns().length > 0
	        &&  mappedByIdFields!= null && mappedByIdFields.contains(field)) {
	        	row = rm.getRow(((ClassMapping)rel.getMetaData()).getTable(), Row.ACTION_INSERT, rel, false); 
	        	if (row != null) return;
	        }
        }
        if (field.getJoinDirection() == FieldMapping.JOIN_INVERSE)
            updateInverse(sm, rel, store, rm);
        else {
            if (row == null) row =  field.getRow(sm, store, rm, Row.ACTION_INSERT);
            if (row != null && !field.isBiMTo1JT()) {
                field.setForeignKey(row, rel);
                // this is for bi-directional maps, the key and value of the 
                // map are stored in the table of the mapped-by entity  
                setMapKey(sm, rel, store, row);
            }
        }
    }
    
    private void setMapKey(OpenJPAStateManager sm, OpenJPAStateManager rel, 
        JDBCStore store, Row row) throws SQLException {
        if (rel == null)
            return;
        ClassMetaData meta = rel.getMetaData();
        FieldMapping mapField = getMapField(meta);
        
        // there is no bi-directional map field
        if (mapField == null)
            return;
        
        Map mapObj = (Map)rel.fetchObjectField(mapField.getIndex());
        Object keyObj = getMapKeyObj(mapObj, sm.getPersistenceCapable());
        ValueMapping key = mapField.getKeyMapping();
        if (!key.isEmbedded()) {
            if (keyObj instanceof PersistenceCapable) {
                OpenJPAStateManager keySm = RelationStrategies.
                    getStateManager(keyObj, store.getContext());
                // key is an entity
                ForeignKey fk = mapField.getKeyMapping().
                    getForeignKey();
                ColumnIO io = new ColumnIO();
                row.setForeignKey(fk, io, keySm);
            } 
        } else {
            // key is an embeddable or basic type
            FieldStrategy strategy = mapField.getStrategy(); 
            if (strategy instanceof  
                    HandlerRelationMapTableFieldStrategy) {
                HandlerRelationMapTableFieldStrategy strat = 
                    (HandlerRelationMapTableFieldStrategy) strategy;
                Column[] kcols = strat.getKeyColumns((ClassMapping)meta);
                ColumnIO kio = strat.getKeyColumnIO();
                HandlerStrategies.set(key, keyObj, store, row, kcols,
                        kio, true);
            }
        } 
    }
    
    private FieldMapping getMapField(ClassMetaData meta) {
        FieldMapping[] fields = ((ClassMapping)meta).getFieldMappings();
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData mappedBy = fields[i].getMappedByMetaData();
            if (fields[i].getDeclaredTypeCode() == JavaTypes.MAP &&
                mappedBy == field)  
                return fields[i];
        } 
        return null;    
    }
    
    private Object getMapKeyObj(Map mapObj, Object value) {
        if (value instanceof ReflectingPersistenceCapable)
            value = ((ReflectingPersistenceCapable)value).getManagedInstance(); 

        Set<Map.Entry> entries = mapObj.entrySet();
        for (Map.Entry entry : entries) {
            if (entry.getValue() == value)
                return entry.getKey();
        }
     
        return null;
    }

    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        if (field.getMappedBy() != null)
            return;

        OpenJPAStateManager rel = RelationStrategies.getStateManager
            (sm.fetchObjectField(field.getIndex()), store.getContext());

        if (field.getJoinDirection() == field.JOIN_INVERSE) {
            nullInverse(sm, rm);
            updateInverse(sm, rel, store, rm);
        } else {
            int action = (rel == null && 
                    field.isBidirectionalJoinTableMappingNonOwner()) ?
                    Row.ACTION_DELETE : Row.ACTION_UPDATE;
            Row row = field.getRow(sm, store, rm, action);
            if (row != null && !field.isBiMTo1JT()) {
                field.setForeignKey(row, rel);
                // this is for bi-directional maps, the key and value of the 
                // map are stored in the table of the mapped-by entity  
                setMapKey(sm, rel, store, row);
            }
            
            if (field.isBiMTo1JT()) { // also need to update the join table
                PersistenceCapable invPC = (PersistenceCapable)sm.fetchObject(
                    field.getBi_1ToM_JTField().getIndex());
                Row secondaryRow = null;
                if (invPC != null) {
                    secondaryRow = rm.getSecondaryRow(field.getBi1ToMJoinFK().getTable(),
                        Row.ACTION_INSERT);
                    secondaryRow.setForeignKey(field.getBi1ToMElemFK(), null, sm);
                    secondaryRow.setForeignKey(field.getBi1ToMJoinFK(), null, 
                        RelationStrategies.getStateManager(invPC,
                        store.getContext()));
                    rm.flushSecondaryRow(secondaryRow);
                }
            }
        }
    }

    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        if (field.getMappedBy() != null)
            return;

        if (field.getJoinDirection() == field.JOIN_INVERSE) {
            if (sm.getLoaded().get(field.getIndex())) {
                OpenJPAStateManager rel = RelationStrategies.getStateManager(sm.
                    fetchObjectField(field.getIndex()), store.getContext());
                updateInverse(sm, rel, store, rm);
            } else
                nullInverse(sm, rm);
        } else {
            field.deleteRow(sm, store, rm);

            // if our foreign key has a delete action, we need to set the
            // related object so constraints can be evaluated
            Object lastRelPc = sm.fetchObjectField(field.getIndex());
            if( lastRelPc == null) {
            	lastRelPc = sm.fetchInitialField(field.getIndex());
            }
            OpenJPAStateManager rel = RelationStrategies.getStateManager
                (lastRelPc, store.getContext());
            if (rel != null) {
                ForeignKey fk = field.getForeignKey((ClassMapping)
                    rel.getMetaData());
                if (fk.getDeleteAction() == ForeignKey.ACTION_RESTRICT ||
                    fk.getDeleteAction() == ForeignKey.ACTION_CASCADE) {
                    Row row = field.getRow(sm, store, rm, Row.ACTION_DELETE);
                    row.setForeignKey(fk, null, rel);
                    // this is for bi-directional maps, the key and value of the
                    // map are stored in the table of the mapped-by entity  
                    setMapKey(sm, rel, store, row);
                }
            }
        }
    }

    /**
     * Null inverse relations that reference the given object.
     */
    private void nullInverse(OpenJPAStateManager sm, RowManager rm)
        throws SQLException {
        if (field.getUseClassCriteria())
            return;

        ForeignKey fk = field.getForeignKey();
        ColumnIO io = field.getColumnIO();
        if (!io.isAnyUpdatable(fk, true))
            return;

        // null inverse if not already enforced by fk
        if (field.getIndependentTypeMappings().length != 1)
            throw RelationStrategies.uninversable(field);
        Row row = rm.getAllRows(fk.getTable(), Row.ACTION_UPDATE);
        row.setForeignKey(fk, io, null);
        row.whereForeignKey(fk, sm);
        rm.flushAllRows(row);
    }

    /**
     * This method updates the inverse columns of our relation
     * with the given object.
     */
    private void updateInverse(OpenJPAStateManager sm, OpenJPAStateManager rel,
        JDBCStore store, RowManager rm)
        throws SQLException {
        if (rel == null)
            return;

        ForeignKey fk = field.getForeignKey();
        ColumnIO io = field.getColumnIO();

        int action;
        if (rel.isNew() && !rel.isFlushed()) {
            if (sm.isDeleted() || !io.isAnyInsertable(fk, false))
                return;
            action = Row.ACTION_INSERT;
        } else if (rel.isDeleted()) {
            if (rel.isFlushed() || !sm.isDeleted())
                return;
            action = Row.ACTION_DELETE;
        } else {
            if (sm.isDeleted())
                sm = null;
            if (!io.isAnyUpdatable(fk, sm == null))
                return;
            action = Row.ACTION_UPDATE;
        }

        if (field.getIndependentTypeMappings().length != 1)
            throw RelationStrategies.uninversable(field);

        // get the row for the inverse object; the row might be in a secondary
        // table if there is a field controlling the foreign key
        Row row = null;
        FieldMapping[] invs = field.getInverseMappings();
        for (int i = 0; i < invs.length; i++) {
            if (invs[i].getMappedByMetaData() == field
                && invs[i].getTypeCode() == JavaTypes.PC) {
                row = invs[i].getRow(rel, store, rm, action);
                break;
            }
        }
        ClassMapping relMapping = field.getTypeMapping();
        if (row == null)
            row = rm.getRow(relMapping.getTable(), action, rel, true);

        // if this is an update, this might be the only mod to the row, so
        // make sure the where condition is set
        if (action == Row.ACTION_UPDATE
            && row.getTable() == relMapping.getTable())
            row.wherePrimaryKey(rel);

        // update the inverse pointer with our oid value
        row.setForeignKey(fk, io, sm);
    }

    public int supportsSelect(Select sel, int type, OpenJPAStateManager sm,
        JDBCStore store, JDBCFetchConfiguration fetch) {
        if (type == Select.TYPE_JOINLESS)
            return (field.getJoinDirection() != field.JOIN_INVERSE
                && sel.isSelected(field.getTable())) ? 1 : 0;
        if (type == Select.TYPE_TWO_PART)
            return 1;

        // already cached?
        if (sm != null) {
            Object oid = sm.getIntermediate(field.getIndex());
            if (store.getContext().findCached(oid, null) != null)
                return 0;
        }

        ClassMapping[] clss = field.getIndependentTypeMappings();
        switch (type) {
            case Select.EAGER_PARALLEL:
                return clss.length;
            case Select.EAGER_OUTER:
                return (clss.length == 1 && store.getDBDictionary().canOuterJoin
                    (sel.getJoinSyntax(), field.getForeignKey(clss[0]))) ? 1 :
                    0;
            case Select.EAGER_INNER:
                return (clss.length == 1) ? 1 : 0;
            default:
                return 0;
        }
    }

    public void selectEagerParallel(SelectExecutor sel,
        final OpenJPAStateManager sm, final JDBCStore store,
        final JDBCFetchConfiguration fetch, final int eagerMode) {
        final ClassMapping[] clss = field.getIndependentTypeMappings();
        if (!(sel instanceof Union))
            selectEagerParallel((Select) sel, clss[0], store, fetch, eagerMode);
        else {
            Union union = (Union) sel;
            if (fetch.getSubclassFetchMode (field.getTypeMapping()) 
                != JDBCFetchConfiguration.EAGER_JOIN)
                union.abortUnion();
            union.select(new Union.Selector() {
                public void select(Select sel, int idx) {
                    selectEagerParallel(sel, clss[idx], store, fetch,
                        eagerMode);
                }
            });
        }
    }

    /**
     * Perform an eager parallel select.
     */
    private void selectEagerParallel(Select sel, ClassMapping cls,
        JDBCStore store, JDBCFetchConfiguration fetch, int eagerMode) {
        if (field.isBiMTo1JT())
            return;
        sel.selectPrimaryKey(field.getDefiningMapping());
        // set a variable name that does not conflict with any in the query;
        // using a variable guarantees that the selected data will use different
        // aliases and joins than any existing WHERE conditions on this field
        // that might otherwise limit the relations that match
        Joins joins = sel.newJoins().setVariable("*");
        eagerJoin(joins, cls, true);
        sel.select(cls, field.getSelectSubclasses(), store, fetch, eagerMode, 
            joins);
    }

    public void selectEagerJoin(Select sel, OpenJPAStateManager sm,
        JDBCStore store, JDBCFetchConfiguration fetch, int eagerMode) {
        if (field.isBiMTo1JT()) 
            return;

        // limit the eager mode to single on recursive eager fetching b/c
        // at this point the select has been modified and an attempt to
        // clone it for a to-many eager select can result in a clone that
        // produces invalid SQL
        ClassMapping cls = field.getIndependentTypeMappings()[0];
        boolean forceInner = fetch.hasFetchInnerJoin(field.getFullName(false)) ?
                true : false;
        sel.select(cls, field.getSelectSubclasses(), store, fetch,
            JDBCFetchConfiguration.EAGER_JOIN,
            eagerJoin(sel.newJoins(), cls, forceInner));
    }

    /**
     * Add the joins needed to select/load eager data.
     */
    private Joins eagerJoin(Joins joins, ClassMapping cls, boolean forceInner) {
        boolean inverse = field.getJoinDirection() == field.JOIN_INVERSE;
        if (!inverse) {
            joins = join(joins, false);
            joins = setEmbeddedVariable(joins);
        }

        // and join into relation
        ForeignKey fk = field.getForeignKey(cls);
        if (!forceInner && field.getNullValue() != FieldMapping.NULL_EXCEPTION)
            return joins.outerJoinRelation(field.getName(), fk, field.
                getTypeMapping(), field.getSelectSubclasses(), inverse, false);
        return joins.joinRelation(field.getName(), fk, field.getTypeMapping(), 
            field.getSelectSubclasses(), inverse, false);
    }

    /**
     * If joining from an embedded owner, use variable to create a unique
     * alias in case owner contains other same-typed embedded relations.
     */
    private Joins setEmbeddedVariable(Joins joins) {
        if (field.getDefiningMetaData().getEmbeddingMetaData() == null)
            return joins;
        return joins.setVariable(field.getDefiningMetaData().
            getEmbeddingMetaData().getFieldMetaData().getName());
    }

    public int select(Select sel, OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, int eagerMode) {
        if (field.getJoinDirection() == field.JOIN_INVERSE)
            return -1;
        // already cached oid?
        if (sm != null && sm.getIntermediate(field.getIndex()) != null)
            return -1;
        if (!Boolean.TRUE.equals(_fkOid))
            return -1;
        sel.select(field.getColumns(), field.join(sel));
        return 0;
    }

    public Object loadEagerParallel(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Object res)
        throws SQLException {
        // process batched results if we haven't already
        Map rels;
        if (res instanceof Result)
            rels = processEagerParallelResult(sm, store, fetch, (Result) res);
        else
            rels = (Map) res;

        // store object for this oid in instance
        sm.storeObject(field.getIndex(), rels.remove(sm.getObjectId()));
        return rels;
    }

    /**
     * Process the given batched result.
     */
    private Map processEagerParallelResult(OpenJPAStateManager sm,
        JDBCStore store, JDBCFetchConfiguration fetch, Result res)
        throws SQLException {
        // do same joins as for load
        //### cheat: we know typical result joins only care about the relation
        //### path; thus we can ignore different mappings
        ClassMapping[] clss = field.getIndependentTypeMappings();
        Joins joins = res.newJoins().setVariable("*");
        eagerJoin(joins, clss[0], true);

        Map rels = new HashMap();
        ClassMapping owner = field.getDefiningMapping();
        ClassMapping cls;
        Object oid;
        while (res.next()) {
            cls = res.getBaseMapping();
            if (cls == null)
                cls = clss[0];
            oid = owner.getObjectId(store, res, null, true, null);
            rels.put(oid, res.load(cls, store, fetch, joins));
        }
        res.close();

        return rels;
    }

    public void loadEagerJoin(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res)
        throws SQLException {
        if (field.isBiMTo1JT())
            return;
        ClassMapping cls = field.getIndependentTypeMappings()[0];

        // for inverseEager field
        FieldMapping mappedByFieldMapping = field.getMappedByMapping();
        PersistenceCapable mappedByValue = null;

        if (mappedByFieldMapping != null) {
        	ValueMapping val = mappedByFieldMapping.getValueMapping();
        	ClassMetaData decMeta = val.getTypeMetaData();
            // eager loading a child from its toOne parent and
            // the parent has @OneToOne(mappedBy="parent") child relation.
            // By saving the mapped-by info in 'res' is to
            // avoid unneeded SQL pushdown that would otherwise gets
            // generated.
            if (decMeta != null && !sm.isEmbedded()) {
        	    mappedByValue = sm.getPersistenceCapable();
        	    res.setMappedByFieldMapping(mappedByFieldMapping);
        	    res.setMappedByValue(mappedByValue);
        	}
        }

        boolean isLocked = res.isLocking();
        try {
            if (store.getLockManager() != null)
                res.setLocking(store.getLockManager().skipRelationFieldLock());
            sm.storeObject(field.getIndex(), res.load(cls, store, fetch,
                    eagerJoin(res.newJoins(), cls, false)));
        } finally {
            res.setLocking(isLocked);
        }

        // reset mapped by is needed for OneToOne bidirectional relations
        // having a mapped-by parent to correctly set the parent-child
        // relation.
        res.setMappedByFieldMapping(null);
        res.setMappedByValue(null);
    }

    public void load(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res)
        throws SQLException {
        if (field.getJoinDirection() == field.JOIN_INVERSE)
            return;
        // cached oid?
        if (sm != null && sm.getIntermediate(field.getIndex()) != null)
            return;
        if (!Boolean.TRUE.equals(_fkOid))
            return;
        if (!res.containsAll(field.getColumns()))
            return;

        // get the related object's oid
        ClassMapping relMapping = field.getTypeMapping();
        Object oid = null;
        if (relMapping.isMapped() && !field.isBiMTo1JT()) { 
            oid = relMapping.getObjectId(store, res, field.getForeignKey(),
                    field.getPolymorphic() != ValueMapping.POLY_FALSE, null);
        } else {
            Column[] cols = field.getColumns();
            if (relMapping.getIdentityType() == ClassMapping.ID_DATASTORE) {
                long id = res.getLong(cols[0]);
                if (!res.wasNull())
                    oid = store.newDataStoreId(id, relMapping, true);
            } else { 
                // application id
                if (cols.length == 1) {
                    Object val = res.getObject(cols[0], null, null);
                    if (val != null)
                        oid = ApplicationIds.fromPKValues(new Object[]{ val },
                            relMapping);
                } else {
                    Object[] vals = new Object[cols.length];
                    for (int i = 0; i < cols.length; i++) {
                        vals[i] = res.getObject(cols[i], null, null);
                        if (vals[i] == null)
                            break;
                        if (i == cols.length - 1)
                            oid = ApplicationIds.fromPKValues(vals, relMapping);
                    }
                }
            }
        }

        if (oid == null)
            sm.storeObject(field.getIndex(), null);
        else
            sm.setIntermediate(field.getIndex(), oid);
    }

    public void load(final OpenJPAStateManager sm, final JDBCStore store,
        final JDBCFetchConfiguration fetch)
        throws SQLException {
        // check for cached oid value, or load oid if no way to join
        if (Boolean.TRUE.equals(_fkOid)) {
            Object oid = sm.getIntermediate(field.getIndex());
            if (oid != null) {
                Object val = store.find(oid, field, fetch);
                sm.storeObject(field.getIndex(), val);
                return;
            }
        }

        final ClassMapping[] rels = field.getIndependentTypeMappings();
        final int subs = field.getSelectSubclasses();
        final Joins[] resJoins = new Joins[rels.length];

        // select related mapping columns; joining from the related type
        // back to our fk table if not an inverse mapping (in which case we
        // can just make sure the inverse cols == our pk values)
        Union union = store.getSQLFactory().newUnion(rels.length);
        union.setExpectedResultCount(1, false);
        if (fetch.getSubclassFetchMode(field.getTypeMapping())
            != JDBCFetchConfiguration.EAGER_JOIN)
            union.abortUnion();
        union.select(new Union.Selector() {
            public void select(Select sel, int idx) {
                if (field.getJoinDirection() == field.JOIN_INVERSE)
                    sel.whereForeignKey(field.getForeignKey(rels[idx]),
                        sm.getObjectId(), field.getDefiningMapping(), store);
                else {
                    if (!field.isBiMTo1JT()) {
                        resJoins[idx] = sel.newJoins().joinRelation(field.getName(),
                            field.getForeignKey(rels[idx]), rels[idx],
                            field.getSelectSubclasses(), false, false);
                        field.wherePrimaryKey(sel, sm, store);
                    } else {
                        resJoins[idx] = sel.newJoins().joinRelation(null,
                            field.getBi1ToMJoinFK(), rels[idx],
                            field.getSelectSubclasses(), false, false);
                        sel.whereForeignKey(field.getBi1ToMElemFK(), sm.getObjectId(), 
                            field.getDefiningMapping(), store);
                    }
                }
                sel.select(rels[idx], subs, store, fetch, fetch.EAGER_JOIN, 
                    resJoins[idx]);
            }
        });

        Result res = union.execute(store, fetch);
        try {
            Object val = null;
            if (res.next())
                val = res.load(rels[res.indexOf()], store, fetch,
                    resJoins[res.indexOf()]);
            sm.storeObject(field.getIndex(), val);
        } finally {
            res.close();
        }
    }

    public Object toDataStoreValue(Object val, JDBCStore store) {
        return RelationStrategies.toDataStoreValue(field, val, store);
    }

    public void appendIsNull(SQLBuffer sql, Select sel, Joins joins) {
        // if no inverse, just join to mapping's table (usually a no-op
        // because it'll be in the primary table) and see if fk cols are null;
        // if inverse, then we have to do a sub-select to see if any inverse
        // objects point back to this field's owner
        if (field.getJoinDirection() != field.JOIN_INVERSE) {
            //### probably need some sort of subselect here on fk constants
            joins = join(joins, false);
            Column[] cols = field.getColumns();
            if (cols.length == 0)
                sql.append("1 <> 1");
            else
                sql.append(sel.getColumnAlias(cols[0], joins)).
                    append(" IS ").appendValue(null, cols[0]);
        } else
            testInverseNull(sql, sel, joins, true);
    }

    public void appendIsNotNull(SQLBuffer sql, Select sel, Joins joins) {
        // if no inverse, just join to mapping's table (usually a no-op
        // because it'll be in the primary table) and see if fk cols aren't
        // null; if inverse, then we have to do a sub-select to see if any
        // inverse objects point back to this field's owner
        if (field.getJoinDirection() != field.JOIN_INVERSE) {
            //### probably need some sort of subselect here on fk constants
            joins = join(joins, false);
            Column[] cols = field.getColumns();
            if (cols.length == 0)
                sql.append("1 = 1");
            else
                sql.append(sel.getColumnAlias(cols[0], joins)).
                    append(" IS NOT ").appendValue(null, cols[0]);
        } else
            testInverseNull(sql, sel, joins, false);
    }

    /**
     * Append SQL for a sub-select testing whether an inverse object exists
     * for this relation.
     */
    private void testInverseNull(SQLBuffer sql, Select sel, Joins joins,
        boolean empty) {
        DBDictionary dict = field.getMappingRepository().getDBDictionary();
        dict.assertSupport(dict.supportsSubselect, "SupportsSubselect");

        if (field.getIndependentTypeMappings().length != 1)
            throw RelationStrategies.uninversable(field);

        if (empty)
            sql.append("0 = ");
        else
            sql.append("0 < ");

        ForeignKey fk = field.getForeignKey();
        ContainerFieldStrategy.appendJoinCount(sql, sel, joins, dict, field,
            fk);
    }

    public Joins join(Joins joins, boolean forceOuter) {
        // if we're not in an inverse object table join normally, otherwise
        // already traversed the relation; just join back to owner table
        if (field.getJoinDirection() != field.JOIN_INVERSE)
            return field.join(joins, forceOuter, false);
        ClassMapping[] clss = field.getIndependentTypeMappings();
        if (clss.length != 1)
            throw RelationStrategies.uninversable(field);
        if (forceOuter)
            return joins.outerJoinRelation(field.getName(),
                field.getForeignKey(), clss[0], field.getSelectSubclasses(), 
                true, false);
        return joins.joinRelation(field.getName(), field.getForeignKey(),
            clss[0], field.getSelectSubclasses(), true, false);
    }

    public Joins joinRelation(Joins joins, boolean forceOuter,
        boolean traverse) {
        // if this is an inverse mapping it's already joined to the relation
        if (field.getJoinDirection() == field.JOIN_INVERSE)
            return joins;
        ClassMapping[] clss = field.getIndependentTypeMappings();
        if (clss.length != 1) {
            if (traverse)
                throw RelationStrategies.unjoinable(field);
            return joins;
        }

        joins = setEmbeddedVariable(joins);
        if (forceOuter)
            return joins.outerJoinRelation(field.getName(), 
                field.getForeignKey(clss[0]), clss[0], 
                field.getSelectSubclasses(), false, false);
        return joins.joinRelation(field.getName(), field.getForeignKey(clss[0]),
            clss[0], field.getSelectSubclasses(), false, false);
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
        ClassMapping relmapping = field.getTypeMapping();
        if (relmapping.getIdentityType() == ClassMapping.ID_DATASTORE) {
            Column col = cols[0];
            if (fk != null)
                col = fk.getColumn(col);   
            long id = res.getLong(col, joins);
            if (field.getObjectIdFieldTypeCode() == JavaTypes.LONG)
                return id;
            return store.newDataStoreId(id, relmapping, field.getPolymorphic() 
                != ValueMapping.POLY_FALSE);
        }

        if (relmapping.isOpenJPAIdentity())
            return ((Joinable) relmapping.getPrimaryKeyFieldMappings()[0].
                getStrategy()).getPrimaryKeyValue(res, cols, fk, store, joins);

        if (cols == getColumns() && fk == null)
            fk = field.getForeignKey();
        else
            fk = createTranslatingForeignKey(relmapping, cols, fk); 
        return relmapping.getObjectId(store, res, fk,
            field.getPolymorphic() != ValueMapping.POLY_FALSE, joins);
    }

    /**
     * Create a faux foreign key that translates between the columns to pull
     * the data from and our related type's primary key columns.
     */
    private ForeignKey createTranslatingForeignKey(ClassMapping relmapping,
        Column[] gcols, ForeignKey gfk) {
        ForeignKey fk = field.getForeignKey(); 
        Column[] cols = fk.getColumns();

        ForeignKey tfk = null;
        Column tcol;
        for (int i = 0; i < gcols.length; i++) {
            tcol = gcols[i];
            if (gfk != null)
                tcol = gfk.getColumn(tcol);
            if (tfk == null)
                tfk = new ForeignKey(DBIdentifier.NULL, tcol.getTable());
            tfk.join(tcol, fk.getPrimaryKeyColumn(cols[i]));
        }
        return tfk;
    }

    public Object getJoinValue(Object fieldVal, Column col, JDBCStore store) {
        Object o = field.getForeignKey().getConstant(col);
        if (o != null)
            return o;
        col = field.getForeignKey().getPrimaryKeyColumn(col);
        if (col == null)
            throw new InternalException();
        
        Object savedFieldVal = fieldVal;

        ClassMapping relmapping = field.getTypeMapping();
        Joinable j = field.getTypeMapping().assertJoinable(col);
        if (ImplHelper.isManageable(fieldVal) && !field.getDefiningMetaData().useIdClassFromParent())
            fieldVal = store.getContext().getObjectId(fieldVal);
        if (fieldVal instanceof OpenJPAId)
            fieldVal = ((OpenJPAId) fieldVal).getIdObject();
        if (relmapping.getObjectIdType() != null
            && relmapping.getObjectIdType().isInstance(fieldVal)) {
            Object[] pks = ApplicationIds.toPKValues(fieldVal, relmapping);
            fieldVal = pks[relmapping.getField(j.getFieldIndex()).
                getPrimaryKeyIndex()];
        } else if (relmapping.getObjectIdType() == ObjectId.class && 
            relmapping.getPrimaryKeyFieldMappings()[0].getValueMapping().isEmbedded()) {
            if (fieldVal == null)
                return j.getJoinValue(savedFieldVal, col, store);
            return j.getJoinValue(fieldVal, col, store);
        }
        return j.getJoinValue(fieldVal, col, store);
    }

    public Object getJoinValue(OpenJPAStateManager sm, Column col,
        JDBCStore store) {
        return getJoinValue(sm.fetch(field.getIndex()), col, store);
    }

    public void setAutoAssignedValue(OpenJPAStateManager sm, JDBCStore store,
        Column col, Object autoInc) {
        throw new UnsupportedException();
    }

    /////////////////////////////
    // Embeddable implementation
    /////////////////////////////

    public Column[] getColumns() {
        return field.getColumns();
    }

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
        return UNSUPPORTED;
    }

    public void loadEmbedded(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Object val)
        throws SQLException {
        ClassMapping relMapping = field.getTypeMapping();
        Object oid;
        if (val == null)
            oid = null;
        else if (relMapping.getIdentityType() == ClassMapping.ID_DATASTORE)
            oid = store.newDataStoreId(((Number) val).longValue(), relMapping,
                field.getPolymorphic() != ValueMapping.POLY_FALSE);
        else {
            Object[] pks = (getColumns().length == 1) ? new Object[]{ val }
                : (Object[]) val;
            boolean nulls = true;
            for (int i = 0; nulls && i < pks.length; i++)
                nulls = pks[i] == null;
            if (nulls)
                oid = null;
            else {
                oid = ApplicationIds.fromPKValues(pks, relMapping);
                if (field.getPolymorphic() == ValueMapping.POLY_FALSE
                    && oid instanceof OpenJPAId) {
                    ((OpenJPAId) oid).setManagedInstanceType(relMapping.
                        getDescribedType());
                }
            }
        }

        if (oid == null)
            sm.storeObject(field.getIndex(), null);
        else {
            if (JavaTypes.maybePC(field.getValue()) &&
                field.getElement().getEmbeddedMetaData() == null) {
                Object obj = store.find(oid, field, fetch);
                sm.storeObject(field.getIndex(), obj);
            } else    
                sm.setIntermediate(field.getIndex(), oid);
        }
    }
}
