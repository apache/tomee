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
import java.util.Map;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.ReflectingPersistenceCapable;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.FieldStrategy;
import org.apache.openjpa.jdbc.meta.Strategy;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.meta.ValueMappingInfo;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.jdbc.sql.RowImpl;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.MetaDataException;

/**
 * Base class for map mappings. Handles managing the secondary table
 * used to hold map keys and values and loading. Subclasses must implement
 * abstract methods and insert/update behavior as well as overriding
 * {@link FieldStrategy#toDataStoreValue},
 * {@link FieldStrategy#toKeyDataStoreValue},
 * {@link FieldStrategy#joinRelation}, and
 * {@link FieldStrategy#joinKeyRelation} if necessary.
 *
 * @author Abe White
 */
public abstract class MapTableFieldStrategy
    extends ContainerFieldStrategy
    implements LRSMapFieldStrategy {

    private static final Localizer _loc = Localizer.forPackage
        (MapTableFieldStrategy.class);

    public FieldMapping getFieldMapping() {
        return field;
    }

    public ClassMapping[] getIndependentKeyMappings(boolean traverse) {
        return (traverse) ? field.getKeyMapping().getIndependentTypeMappings()
            : ClassMapping.EMPTY_MAPPINGS;
    }

    public ClassMapping[] getIndependentValueMappings(boolean traverse) {
        return (traverse) ? field.getElementMapping().
            getIndependentTypeMappings() : ClassMapping.EMPTY_MAPPINGS;
    }

    public ForeignKey getJoinForeignKey(ClassMapping cls) {
        return field.getJoinForeignKey();
    }

    public Object deriveKey(JDBCStore store, Object value) {
        return null;
    }

    public Object deriveValue(JDBCStore store, Object key) {
        return null;
    }

    /**
     * Invokes {@link FieldStrategy#joinKeyRelation} by default.
     */
    public Joins joinKeyRelation(Joins joins, ClassMapping key) {
        return joinKeyRelation(joins, false, false);
    }

    /**
     * Invokes {@link FieldStrategy#joinRelation} by default.
     */
    public Joins joinValueRelation(Joins joins, ClassMapping val) {
        return joinRelation(joins, false, false);
    }

    public void map(boolean adapt) {
        if (field.getTypeCode() != JavaTypes.MAP)
            throw new MetaDataException(_loc.get("not-map", field));
        if (field.getKey().getValueMappedBy() != null)
            throw new MetaDataException(_loc.get("mapped-by-key", field));

        // Non-default mapping Uni-/OneToMany/ForeignKey allows schema components
        if (field.isUni1ToMFK())  
            return;
        if (field.isBiMTo1JT())
            field.setBi1MJoinTableInfo();
        field.getValueInfo().assertNoSchemaComponents(field, !adapt);
    }
    
    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        Row row = rm.getAllRows(field.getTable(), Row.ACTION_DELETE);
        row.whereForeignKey(field.getJoinForeignKey(), sm);
        rm.flushAllRows(row);
    }

    public int supportsSelect(Select sel, int type, OpenJPAStateManager sm,
        JDBCStore store, JDBCFetchConfiguration fetch) {
        return 0;
    }

    public void load(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch)
        throws SQLException {
        if (field.isLRS()) {
            sm.storeObjectField(field.getIndex(), new LRSProxyMap(this));
            return;
        }

        // select all and load into a normal proxy
        Joins[] joins = new Joins[2];
        Result[] res = getResults(sm, store, fetch,
            JDBCFetchConfiguration.EAGER_PARALLEL, joins, false);
        try {
            Map map = (Map) sm.newProxy(field.getIndex());
            Object key, val;
            while (res[0].next()) {
                if (res[1] != res[0] && !res[1].next())
                    break;

                key = loadKey(sm, store, fetch, res[0], joins[0]);
                val = loadValue(sm, store, fetch, res[1], joins[1]);
                map.put(key, val);
            }
            sm.storeObject(field.getIndex(), map);
        } finally {
            res[0].close();
            if (res[1] != res[0])
                res[1].close();
        }
    }

    public Object loadKeyProjection(JDBCStore store,
        JDBCFetchConfiguration fetch, Result res, Joins joins)
        throws SQLException {
        return loadKey(null, store, fetch, res, joins);
    }

    public Object loadProjection(JDBCStore store, JDBCFetchConfiguration fetch,
        Result res, Joins joins)
        throws SQLException {
        return loadValue(null, store, fetch, res, joins);
    }

    public Joins join(Joins joins, boolean forceOuter) {
        return field.join(joins, forceOuter, true);
    }

    public Joins joinKey(Joins joins, boolean forceOuter) {
        return field.join(joins, forceOuter, true);
    }

    public ForeignKey getJoinForeignKey() {
        return field.getJoinForeignKey();
    }

    protected ClassMapping[] getIndependentElementMappings(boolean traverse) {
        return ClassMapping.EMPTY_MAPPINGS;
    }
    
    protected void handleMappedByForeignKey(boolean adapt){
        boolean criteria = field.getValueInfo().getUseClassCriteria();
        // check for named inverse
        FieldMapping mapped = field.getMappedByMapping();
        if (mapped != null) {
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
                ForeignKey fk = mapped.getForeignKey(
                        field.getDefiningMapping());
                field.setForeignKey(fk);
                field.setJoinForeignKey(fk);
            } else if (mapped.getElement().getTypeCode() == JavaTypes.PC) {
                if (isTypeUnjoinedSubclass(mapped.getElementMapping()))
                    throw new MetaDataException(_loc.get
                        ("mapped-inverse-unjoined", field.getName(),
                            field.getDefiningMapping(), mapped));

                // warn the user about making the collection side the owner
                Log log = field.getRepository().getLog();
                if (log.isInfoEnabled())
                    log.info(_loc.get("coll-owner", field, mapped));
                ValueMapping elem = mapped.getElementMapping();
                ForeignKey fk = elem.getForeignKey();
                field.setJoinForeignKey(fk);
                field.getElementMapping().setForeignKey(
                        mapped.getJoinForeignKey());
            } else
                throw new MetaDataException(_loc.get("not-inv-relation",
                    field, mapped));

            field.setUseClassCriteria(criteria);
            return;
        } else {
            // Uni-/OneToMany/ForeingKey
            ValueMapping val = field.getElementMapping();
            val.getValueInfo().setColumns(field.getValueInfo().getColumns());
            if (val.getTypeMapping().isMapped()) {
                ValueMappingInfo vinfo = val.getValueInfo();
                ForeignKey fk = vinfo.getTypeJoin(val, DBIdentifier.NULL, false, adapt);
                val.setForeignKey(fk);
                val.setColumnIO(vinfo.getColumnIO());
            } else
                RelationStrategies.mapRelationToUnmappedPC(val, "value", adapt);

            val.mapConstraints("value", adapt);
            
            return;
        }
/*
        // this is necessary to support openjpa 3 mappings, which didn't
        // differentiate between secondary table joins and relations built
        // around an inverse key: check to see if we're mapped as a secondary
        // table join but we're in the table of the related type, and if so
        // switch our join mapping info to our value mapping info
        String tableName = field.getMappingInfo().getTableName();
        Table table = field.getTypeMapping().getTable();
        ValueMappingInfo vinfo = field.getValueInfo();
        if (tableName != null && table != null
            && (tableName.equalsIgnoreCase(table.getName())
            || tableName.equalsIgnoreCase(table.getFullName()))) {
            vinfo.setJoinDirection(MappingInfo.JOIN_INVERSE);
            vinfo.setColumns(field.getMappingInfo().getColumns());
            field.getMappingInfo().setTableName(null);
            field.getMappingInfo().setColumns(null);
        }
*/        
    }

    protected boolean isTypeUnjoinedSubclass(ValueMapping mapped) {
        ClassMapping def = field.getDefiningMapping();
        for (; def != null; def = def.getJoinablePCSuperclassMapping())
            if (def == mapped.getTypeMapping())
                return false;
        return true;
    }

    protected boolean populateKey(Row row, OpenJPAStateManager valsm,
            Object obj, StoreContext ctx, RowManager rm, JDBCStore store)
            throws SQLException {
        ClassMapping meta = (ClassMapping)valsm.getMetaData();
        FieldMapping fm = getFieldMapping(meta);
        if (fm == null) 
            return false;
        Map mapObj = (Map)valsm.fetchObject(fm.getIndex());
        Collection<Map.Entry> entrySets = mapObj.entrySet();
        boolean found = false;
        for (Map.Entry entry : entrySets) {
            Object value = entry.getValue();
            if (obj instanceof ReflectingPersistenceCapable)
               obj = ((ReflectingPersistenceCapable)obj).getManagedInstance(); 
            if (value == obj) {
                Row newRow = (Row) ((RowImpl)row).clone();
                Object keyObj = entry.getKey();
                Strategy strat = fm.getStrategy();
                if (strat instanceof HandlerRelationMapTableFieldStrategy) {
                    HandlerRelationMapTableFieldStrategy hrStrat = 
                        (HandlerRelationMapTableFieldStrategy) strat;
                    hrStrat.setKey(keyObj, store, newRow);
                } else if (keyObj instanceof PersistenceCapable) {
                    OpenJPAStateManager keysm = 
                        RelationStrategies.getStateManager(entry.getKey(), ctx);
                    ValueMapping key = fm.getKeyMapping();
                    if (keysm != null) 
                        key.setForeignKey(newRow, keysm);
                    else
                        key.setForeignKey(newRow, null);
                } 
                rm.flushSecondaryRow(newRow);
                found = true;
            }
        }
        if (found)
            return true;
        return false;        
    }

    private FieldMapping getFieldMapping(ClassMapping meta) {
        FieldMapping[] fields = meta.getFieldMappings();
        for (int i = 0; i < fields.length; i++) {
            ValueMapping val = fields[i].getValueMapping();
            if (fields[i].getMappedByMetaData() == field && 
                    val.getDeclaredTypeCode() == JavaTypes.MAP)
                return fields[i];
        }
        return null;
    }
}
