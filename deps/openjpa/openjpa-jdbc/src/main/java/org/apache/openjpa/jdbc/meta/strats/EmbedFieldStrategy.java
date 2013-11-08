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

import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.StateManager;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Embeddable;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.JavaSQLTypes;
import org.apache.openjpa.jdbc.meta.Joinable;
import org.apache.openjpa.jdbc.meta.RelationId;
import org.apache.openjpa.jdbc.meta.ValueMappingInfo;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.kernel.StateManagerImpl;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FetchGroup;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.MetaDataException;

/**
 * Mapping for an embedded persistent object.
 *
 * @author Abe White
 * @since 0.4.0
 */
public class EmbedFieldStrategy
    extends AbstractFieldStrategy   
    implements Embeddable {

    private static final int INSERT = 0;
    private static final int UPDATE = 1;
    private static final int DELETE = 2;

    private static final Localizer _loc = Localizer.forPackage
        (EmbedFieldStrategy.class);

    private boolean _synthetic = false;

    public void map(boolean adapt) {
        if (field.getEmbeddedMetaData() == null)
            throw new MetaDataException(_loc.get("not-embed", field));
        assertNotMappedBy();

        // map join key (if any)
        field.mapJoin(adapt, false);
        field.getKeyMapping().getValueInfo().assertNoSchemaComponents
            (field.getKey(), !adapt);
        field.getElementMapping().getValueInfo().assertNoSchemaComponents
            (field.getElement(), !adapt);

        ValueMappingInfo vinfo = field.getValueInfo();
        vinfo.assertNoJoin(field, true);
        vinfo.assertNoForeignKey(field, !adapt);
        vinfo.assertNoUnique(field, !adapt);
        vinfo.assertNoIndex(field, !adapt);

        // before we map the null indicator column, we need to make sure our
        // value is mapped so we can tell whether the column is synthetic
        field.getValueMapping().resolve(field.MODE_META | field.MODE_MAPPING);
        Column col = vinfo.getNullIndicatorColumn(field, field.getName(),
            field.getTable(), adapt);
        if (col != null) {
            field.setColumns(new Column[]{ col });
            field.setColumnIO(vinfo.getColumnIO());
        }

        field.mapPrimaryKey(adapt);
    }

    public void initialize() {
        Column[] cols = field.getColumns();
        if (cols.length != 1)
            _synthetic = false;
        else {
            // do any of the embedded field mappings use this column?
            // if not, consider it synthetic
            Column col = cols[0];
            boolean found = false;
            FieldMapping[] fields = field.getEmbeddedMapping().
                getFieldMappings();
            for (int i = 0; !found && i < fields.length; i++) {
                cols = fields[i].getColumns();
                for (int j = 0; j < cols.length; j++)
                    if (cols[j].equals(col))
                        found = true;
            }
            _synthetic = !found;
        }
    }

    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        Row row = field.getRow(sm, store, rm, Row.ACTION_INSERT);
        if (row == null)
            return;

        OpenJPAStateManager em = store.getContext().getStateManager
            (sm.fetchObject(field.getIndex()));
        insert(sm, em, store, rm, row);
    }

    /**
     * Insert an embedded object.
     *
     * @param owner the owning state manager
     * @param sm the embedded state manager, or null if the value is null
     * @param rm the row manager
     * @param row expected row for this embedded value
     */
    public void insert(OpenJPAStateManager owner, OpenJPAStateManager sm,
        JDBCStore store, RowManager rm, Row row)
        throws SQLException {
        OpenJPAStateManager em = sm;
        if (em == null)
            em = new NullEmbeddedStateManager(owner, field);
        rm = new EmbeddedRowManager(rm, row);
        FieldMapping[] fields = field.getEmbeddedMapping().getFieldMappings();
        for (int i = 0; i < fields.length; i++)
            if (!Boolean.TRUE.equals(fields[i].isCustomInsert(em, store)))
                if (!fields[i].isPrimaryKey())
                    fields[i].insert(em, store, rm);

        if (field.getColumnIO().isInsertable(0, true))
            setNullIndicatorColumn(sm, row);
    }

    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        OpenJPAStateManager em = store.getContext().getStateManager
            (sm.fetchObject(field.getIndex()));
        boolean newVal = em == null || em.getPCState() == PCState.ECOPY;

        Row row = null;
        if (newVal && field.getJoinForeignKey() != null
            && field.isJoinOuter()) {
            // if our record is in an outer-joined related table and we're not
            // updating an existing value, delete the old one
            Row del = rm.getRow(field.getTable(), Row.ACTION_DELETE, sm, true);
            del.whereForeignKey(field.getJoinForeignKey(), sm);
            delete(sm, null, store, rm, del);

            // insert the new value
            row = rm.getRow(field.getTable(), Row.ACTION_INSERT, sm,
                em != null);
        } else
            row = rm.getRow(field.getTable(), Row.ACTION_UPDATE, sm, true);
        if (row == null)
            return;

        if (row.getAction() == Row.ACTION_INSERT)
            insert(sm, em, store, rm, row);
        else
            update(sm, em, store, rm, row);
    }

    /**
     * Update an embedded object.
     *
     * @param owner the owning state manager
     * @param sm the embedded state manager, or null if the value is null
     * @param rm the row manager
     * @param row expected row for this embedded value
     */
    public void update(OpenJPAStateManager owner, OpenJPAStateManager sm,
        JDBCStore store, RowManager rm, Row row)
        throws SQLException {
        OpenJPAStateManager em = sm;
        if (em == null)
            em = new NullEmbeddedStateManager(owner, field);
        rm = new EmbeddedRowManager(rm, row);
        FieldMapping[] fields = field.getEmbeddedMapping().getFieldMappings();
        for (int i = 0; i < fields.length; i++)
            if (em.getDirty().get(i)
                && !em.getFlushed().get(i)
                && !Boolean.TRUE.equals(fields[i].isCustomUpdate(em, store)))
                fields[i].update(em, store, rm);

        if (field.getColumnIO().isUpdatable(0, true))
            setNullIndicatorColumn(sm, row);
    }

    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        OpenJPAStateManager em = null;
        if (sm.getLoaded().get(field.getIndex()))
            em = store.getContext().getStateManager(sm.fetchObject
                (field.getIndex()));
        Row row = field.getRow(sm, store, rm, Row.ACTION_DELETE);
        delete(sm, em, store, rm, row);
    }

    /**
     * Delete an embedded object record.
     *
     * @param owner the owning state manager
     * @param sm the embedded state manager, or null if not known
     * @param rm the row manager
     * @param row expected row for this embedded value
     */
    public void delete(OpenJPAStateManager owner, OpenJPAStateManager sm,
        JDBCStore store, RowManager rm, Row row)
        throws SQLException {
        rm = new EmbeddedRowManager(rm, row);
        if (sm == null)
            sm = new NullEmbeddedStateManager(owner, field);
        FieldMapping[] fields = field.getEmbeddedMapping().getFieldMappings();
        for (int i = 0; i < fields.length; i++)
            if (!Boolean.TRUE.equals(fields[i].isCustomDelete(sm, store)))
                fields[i].delete(sm, store, rm);
    }

    /**
     * Set the proper null indicator value into the given row.
     */
    private void setNullIndicatorColumn(OpenJPAStateManager sm, Row row)
        throws SQLException {
        if (!_synthetic)
            return;

        Column col = field.getColumns()[0];
        Object val = ((EmbeddedClassStrategy) field.getEmbeddedMapping().
            getStrategy()).getNullIndicatorValue(sm);
        if (val == null)
            row.setNull(col, true);
        else
            row.setObject(col, val);
    }

    public Boolean isCustomInsert(OpenJPAStateManager sm, JDBCStore store) {
        OpenJPAStateManager em = sm.getContext().getStateManager(sm.fetchObject
            (field.getIndex()));
        Boolean custom = isCustom(INSERT, sm, em, store);
        if (Boolean.TRUE.equals(custom) && _synthetic)
            return null;
        return custom;
    }

    public Boolean isCustomUpdate(OpenJPAStateManager sm, JDBCStore store) {
        OpenJPAStateManager em = sm.getContext().getStateManager(sm.fetchObject
            (field.getIndex()));
        Boolean custom = isCustom(UPDATE, sm, em, store);
        if (Boolean.TRUE.equals(custom) && _synthetic)
            return null;
        return custom;
    }

    public Boolean isCustomDelete(OpenJPAStateManager sm, JDBCStore store) {
        OpenJPAStateManager em = sm.getContext().getStateManager(sm.fetchObject
            (field.getIndex()));
        return isCustom(DELETE, sm, em, store);
    }

    /**
     * Return whether the given action requires custom handling for any fields.
     */
    private Boolean isCustom(int action, OpenJPAStateManager owner,
        OpenJPAStateManager sm, JDBCStore store) {
        if (sm == null)
            sm = new NullEmbeddedStateManager(owner, field);
        boolean hasCustom = false;
        boolean hasStd = false;

        FieldMapping[] fields = field.getEmbeddedMapping().getFieldMappings();
        Boolean custom = null;
        for (int i = 0; i < fields.length; i++) {
            switch (action) {
                case INSERT:
                    custom = fields[i].isCustomInsert(sm, store);
                    break;
                case UPDATE:
                    custom = fields[i].isCustomUpdate(sm, store);
                    break;
                case DELETE:
                    custom = fields[i].isCustomDelete(sm, store);
                    break;
            }

            if (!Boolean.FALSE.equals(custom))
                hasCustom = true;
            if (!Boolean.TRUE.equals(custom))
                hasStd = true;
        }

        if (hasCustom && hasStd)
            return null;
        return (hasCustom) ? Boolean.TRUE : Boolean.FALSE;
    }

    public void customInsert(OpenJPAStateManager sm, JDBCStore store)
        throws SQLException {
        OpenJPAStateManager em = store.getContext().getStateManager
            (sm.fetchObject(field.getIndex()));
        if (em == null)
            em = new NullEmbeddedStateManager(sm, field);
        FieldMapping[] fields = field.getEmbeddedMapping().getFieldMappings();
        for (int i = 0; i < fields.length; i++)
            if (!Boolean.FALSE.equals(fields[i].isCustomInsert(em, store)))
                fields[i].customInsert(em, store);
    }

    public void customUpdate(OpenJPAStateManager sm, JDBCStore store)
        throws SQLException {
        OpenJPAStateManager em = store.getContext().getStateManager
            (sm.fetchObject(field.getIndex()));
        if (em == null)
            em = new NullEmbeddedStateManager(sm, field);
        FieldMapping[] fields = field.getEmbeddedMapping().getFieldMappings();
        for (int i = 0; i < fields.length; i++)
            if (em.getDirty().get(i)
                && !em.getFlushed().get(i)
                && !Boolean.FALSE.equals(fields[i].isCustomUpdate(em, store)))
                fields[i].customUpdate(em, store);
    }

    public void customDelete(OpenJPAStateManager sm, JDBCStore store)
        throws SQLException {
        OpenJPAStateManager em = store.getContext().getStateManager
            (sm.fetchObject(field.getIndex()));
        if (em == null)
            em = new NullEmbeddedStateManager(sm, field);
        FieldMapping[] fields = field.getEmbeddedMapping().getFieldMappings();
        for (int i = 0; i < fields.length; i++)
            if (!Boolean.FALSE.equals(fields[i].isCustomDelete(em, store)))
                fields[i].customDelete(em, store);
    }

    public int supportsSelect(Select sel, int type, OpenJPAStateManager sm,
        JDBCStore store, JDBCFetchConfiguration fetch) {
        if (type == Select.TYPE_JOINLESS && sel.isSelected(field.getTable()))
            return 1;
        return 0;
    }

    public int select(Select sel, OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, int eagerMode) {
        Joins joins = field.join(sel);
        sel.select(field.getColumns(), joins); // null indicator

        // limit eager mode to join b/c at this point the select has been
        // modified and an attempt to clone it for a to-many eager select can
        // result in a clone that produces invalid SQL
        eagerMode = Math.min(eagerMode, JDBCFetchConfiguration.EAGER_JOIN);
        sel.select(field.getEmbeddedMapping(), sel.SUBS_EXACT, store,
            fetch, eagerMode, joins);
        return 1;
    }

    public void load(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res)
        throws SQLException {
        Boolean isNull = indicatesNull(res);
        if (isNull == null)
            return;

        if (Boolean.TRUE.equals(isNull)) {
            sm.storeObject(field.getIndex(), null);
            return;
        }
        
        // handling of lazy embeddables.  if the embedded field is not part of any
        // fetch group and the result does not contain any embeddable columns, 
        // do not load the embeddable.
        if (fetch.requiresFetch(field) == FetchConfiguration.FETCH_NONE &&
            !containsEmbeddedResult(fetch, res)) {
            return;
        }

        //### note: without a null indicator column, the above indicatesNull()
        //### call will always return false, meaning we always have to assume
        //### we selected the embedded object fields and load the object
        //### immediately; this will be inefficient when the embedded object
        //### was not selected after all
        StoreContext ctx = store.getContext();
        OpenJPAStateManager em = ctx.embed(null, null, sm, field);
        sm.storeObject(field.getIndex(), em.getManagedInstance());
        boolean needsLoad = loadFields(em, store, fetch, res);

        // After loading everything from result, load the rest of the
        // configured fields if anything is missing.
        if (needsLoad && 
            fetch.requiresFetch(field.getFieldMetaData()) == 
                JDBCFetchConfiguration.FETCH_LOAD) {
          em.load(fetch);
        }
    }

    /*
     * finds an eager fetch field and searches for it in the result.  
     * if the result does not contain it, assume that it contains no embeddable
     * column data.  this is a fairly safe assumption given that the entire 
     * embeddable was marked lazy.
     */
    private boolean containsEmbeddedResult(FetchConfiguration fetch, Result res) {
        FieldMapping[] fields = field.getEmbeddedMapping().getFieldMappings();
        for (int i = 0; i < fields.length; i++) {
            boolean load = (fetch.requiresFetch(fields[i]) == FetchConfiguration.FETCH_LOAD);
            if (load) {
                // check the first eager fetch field
                return checkResult(fields[i],res);
            }
        }
        // if all fields are lazy and in the default fetch group, populate the embeddable 
        // so its attributes can be loaded when accessed.
        return fetch.hasFetchGroup(FetchGroup.NAME_DEFAULT);
    }
    
    private boolean checkResult(FieldMapping fm, Result res) {
        if (fm.getStrategy() instanceof Joinable) {
            Joinable strat = (Joinable)fm.getStrategy();
            Column[] cols = strat.getColumns();
            for (Column col : cols) {
                try {
                    if (res.contains(col)) {
                        return true;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        }
        // if the field is a collection, also check for an eager result which could result from
        // a non-lazy relationship in the embeddable
        int type = fm.getTypeCode();
        if ((type == JavaTypes.ARRAY || 
             type == JavaTypes.COLLECTION || 
             type == JavaTypes.MAP)
            && res.getEager(fm) != null) {
            return true;
        }
        return false;
    }

    private boolean loadFields(OpenJPAStateManager em, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res)
        throws SQLException {
        FieldMapping[] fields = field.getEmbeddedMapping().getFieldMappings();
        Object eres, processed;
        boolean needsLoad = false;
        for (int i = 0; i < fields.length; i++) {
            eres = res.getEager(fields[i]);
            res.startDataRequest(fields[i]);
            try {
                if (eres == res)
                    fields[i].loadEagerJoin(em, store, fetch, res);
                else if (eres != null) {
                    processed =
                        fields[i].loadEagerParallel(em, store, fetch, eres);
                    if (processed != eres)
                        res.putEager(fields[i], processed);
                } else {
                    fields[i].load(em, store, fetch, res);
                }
                needsLoad = needsLoad || (!em.getLoaded().get(i) && 
                    fetch.requiresFetch(fields[i])
                        == FetchConfiguration.FETCH_LOAD);
            } finally {
                res.endDataRequest();
            }
        }
        return needsLoad;
    }

    /**
     * Return whether the given result indicates that the embedded value is
     * null.
     *
     * @return {@link Boolean#TRUE} if the value is null, {@link Boolean#FALSE}
     * if it is not, or <code>null</code> if the result does not
     * contain the embedded value
     */
    private Boolean indicatesNull(Result res)
        throws SQLException {
        Column[] cols = field.getColumns();
        if (cols.length != 1)
            return Boolean.FALSE;
        if (!res.contains(cols[0]))
            return null;

        Object obj = res.getObject(cols[0], -1, (Object) null);
        if (((EmbeddedClassStrategy) field.getEmbeddedMapping().getStrategy()).
            indicatesNull(obj))
            return Boolean.TRUE;
        return Boolean.FALSE;
    }

    public Object toDataStoreValue(Object val, JDBCStore store) {
        ClassMapping type = field.getTypeMapping();
        return type.toDataStoreValue(val, type.getPrimaryKeyColumns(), store);
    }

    public void appendIsNull(SQLBuffer sql, Select sel, Joins joins) {
        appendTestNull(sql, sel, joins, true);
    }

    public void appendIsNotNull(SQLBuffer sql, Select sel, Joins joins) {
        appendTestNull(sql, sel, joins, false);
    }

    /**
     * Append SQL to test whether the embedded object is null.
     */
    private void appendTestNull(SQLBuffer sql, Select sel, Joins joins,
        boolean isNull) {
        Column[] cols = field.getColumns();
        if (cols.length != 1) {
            if (isNull)
                sql.append("1 <> 1");
            else
                sql.append("1 = 1");
            return;
        }

        Object val;
        if (cols[0].isNotNull()) {
            if (_synthetic || cols[0].getDefaultString() == null)
                val = JavaSQLTypes.getEmptyValue(cols[0].getJavaType());
            else
                val = cols[0].getDefault();
        } else
            val = null;

        joins = join(joins, false);
        sql.append(sel.getColumnAlias(cols[0], joins));
        if (isNull && val == null)
            sql.append(" IS ");
        else if (isNull)
            sql.append(" = ");
        else if (val == null)
            sql.append(" IS NOT ");
        else
            sql.append(" <> ");
        sql.appendValue(val, cols[0]);
    }

    public Joins join(Joins joins, boolean forceOuter) {
        return field.join(joins, forceOuter, false);
    }

    /**
     * Loading embed object without instantiating owner entity
     */
    public Object loadProjection(JDBCStore store, JDBCFetchConfiguration fetch,
        Result res, Joins joins)
        throws SQLException {
        Boolean isNull = indicatesNull(res);
        if (isNull == null)
            return null;

        StoreContext ctx = store.getContext();
        // load primary key of owner entity
        Object owner = field.getDefiningMapping().getObjectId(store, res, 
            null, true, joins);
        OpenJPAStateManager em = ctx.embed(null, null, null, field);
        // set owner id
        ((StateManagerImpl) em).setOwner(owner);
        boolean needsLoad = loadFields(em, store, fetch, res);

        // After loading everything from result, load the rest of the
        // configured fields if anything is missing.
        if (needsLoad && 
            fetch.requiresFetch(field.getFieldMetaData()) == 
                JDBCFetchConfiguration.FETCH_LOAD) {
          em.load(fetch);
        }
        
        return em.getManagedInstance();
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
        //return UNSUPPORTED;
        return null;
    }

    public void loadEmbedded(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Object val)
        throws SQLException {
        if (val != null)
            sm.storeObject(field.getIndex(), val);
        else
            sm.storeObject(field.getIndex(), null);
    }
    

    /**
     * State manager that represents a null embedded object.
     * Used to update embedded fields when the embedded value is null. This
     * state manager doesn't require access to the old embedded value, because
     * all updates are flushed before all deletes, so all foreign keys will
     * be properly nulled before the related object is deleted. However, this
     * state manager should not be used if possible when the parent object is
     * being deleted; in that case having the original embedded state manager
     * is preferable so that it can set foreign key relations to order DELETE
     * statements to meet constraints.
     */
    private static class NullEmbeddedStateManager
        implements OpenJPAStateManager {

        private static final BitSet EMPTY_BITSET = new BitSet();

        private final OpenJPAStateManager _owner;
        private final ValueMetaData _vmd;
        private BitSet _full = null;

        /**
         * Constructor; supply the owner of the null/deleted embedded value,
         * and the value metadata.
         */
        public NullEmbeddedStateManager(OpenJPAStateManager owner,
            ValueMetaData vmd) {
            _owner = owner;
            _vmd = vmd;
        }

        public void initialize(Class forType, PCState state) {
            throw new InternalException();
        }

        public void load(FetchConfiguration fetch) {
            throw new InternalException();
        }

        public boolean assignObjectId(boolean flush) {
            throw new InternalException();
        }

        public Object getManagedInstance() {
            return null;
        }

        public PersistenceCapable getPersistenceCapable() {
            return null;
        }

        public ClassMetaData getMetaData() {
            return _vmd.getEmbeddedMetaData();
        }

        public OpenJPAStateManager getOwner() {
            return _owner;
        }

        public int getOwnerIndex() {
            return _vmd.getFieldMetaData().getIndex();
        }

        public boolean isEmbedded() {
            return true;
        }

        public boolean isTransactional() {
            return true;
        }

        public boolean isPersistent() {
            return true;
        }

        public boolean isNew() {
            return _owner.isNew();
        }

        public boolean isDeleted() {
            return _owner.isDeleted();
        }

        public boolean isDetached() {
            return _owner.isDetached();
        }

        public boolean isVersionUpdateRequired() {
            return _owner.isVersionUpdateRequired();
        }

        public boolean isVersionCheckRequired() {
            return _owner.isVersionCheckRequired();
        }

        public boolean isDirty() {
            return true;
        }

        public boolean isFlushed() {
            return _owner.isFlushed();
        }

        public boolean isFlushedDirty() {
            return isFlushed();
        }

        public boolean isProvisional() {
            return _owner.isProvisional();
        }

        public BitSet getLoaded() {
            // consider everything loaded
            if (_full == null) {
                FieldMetaData[] fmds = _vmd.getEmbeddedMetaData().getFields();
                _full = new BitSet(fmds.length);
                for (int i = 0; i < fmds.length; i++)
                    _full.set(i);
            }
            return _full;
        }

        public BitSet getDirty() {
            // consider everything dirty
            if (_full == null) {
                FieldMetaData[] fmds = _vmd.getEmbeddedMetaData().getFields();
                _full = new BitSet(fmds.length);
                for (int i = 0; i < fmds.length; i++)
                    _full.set(i);
            }
            return _full;
        }

        public BitSet getFlushed() {
            return EMPTY_BITSET;
        }

        public BitSet getUnloaded(FetchConfiguration fetch) {
            throw new InternalException();
        }

        public Object newProxy(int field) {
            throw new InternalException();
        }

        public Object newFieldProxy(int field) {
            throw new InternalException();
        }

        public boolean isDefaultValue(int field) {
            return true;
        }

        public StoreContext getContext() {
            return _owner.getContext();
        }

        public Object getId() {
            return _owner.getId();
        }

        public Object getObjectId() {
            return _owner.getObjectId();
        }

        public void setObjectId(Object oid) {
            throw new InternalException();
        }

        public Object getLock() {
            return null;
        }

        public void setLock(Object lock) {
            throw new InternalException();
        }

        public Object getVersion() {
            return null;
        }

        public void setVersion(Object version) {
            throw new InternalException();
        }

        public void setNextVersion(Object version) {
            throw new InternalException();
        }

        public PCState getPCState() {
            return (_owner.isDeleted()) ? PCState.EDELETED : PCState.ECOPY;
        }

        public Object getImplData() {
            return null;
        }

        public Object setImplData(Object data, boolean cacheable) {
            throw new InternalException();
        }

        public boolean isImplDataCacheable() {
            return false;
        }

        public Object getImplData(int field) {
            return null;
        }

        public Object setImplData(int field, Object data) {
            throw new InternalException();
        }

        public boolean isImplDataCacheable(int field) {
            return false;
        }

        public Object getIntermediate(int field) {
            return null;
        }

        public void setIntermediate(int field, Object value) {
            throw new InternalException();
        }

        public boolean fetchBooleanField(int field) {
            return false;
        }

        public byte fetchByteField(int field) {
            return (byte) 0;
        }

        public char fetchCharField(int field) {
            return (char) 0;
        }

        public double fetchDoubleField(int field) {
            return 0D;
        }

        public float fetchFloatField(int field) {
            return 0F;
        }

        public int fetchIntField(int field) {
            return 0;
        }

        public long fetchLongField(int field) {
            return 0L;
        }

        public Object fetchObjectField(int field) {
            return null;
        }

        public short fetchShortField(int field) {
            return (short) 0;
        }

        public String fetchStringField(int field) {
            return null;
        }

        public Object fetchField(int field, boolean transitions) {
            return null;
        }

        public void storeBooleanField(int field, boolean externalVal) {
            throw new InternalException();
        }

        public void storeByteField(int field, byte externalVal) {
            throw new InternalException();
        }

        public void storeCharField(int field, char externalVal) {
            throw new InternalException();
        }

        public void storeDoubleField(int field, double externalVal) {
            throw new InternalException();
        }

        public void storeFloatField(int field, float externalVal) {
            throw new InternalException();
        }

        public void storeIntField(int field, int externalVal) {
            throw new InternalException();
        }

        public void storeLongField(int field, long externalVal) {
            throw new InternalException();
        }

        public void storeObjectField(int field, Object externalVal) {
            throw new InternalException();
        }

        public void storeShortField(int field, short externalVal) {
            throw new InternalException();
        }

        public void storeStringField(int field, String externalVal) {
            throw new InternalException();
        }

        public void storeField(int field, Object value) {
            throw new InternalException();
        }

        public boolean fetchBoolean(int field) {
            return false;
        }

        public byte fetchByte(int field) {
            return (byte) 0;
        }

        public char fetchChar(int field) {
            return (char) 0;
        }

        public double fetchDouble(int field) {
            return 0D;
        }

        public float fetchFloat(int field) {
            return 0F;
        }

        public int fetchInt(int field) {
            return 0;
        }

        public long fetchLong(int field) {
            return 0L;
        }

        public Object fetchObject(int field) {
            return null;
        }

        public short fetchShort(int field) {
            return (short) 0;
        }

        public String fetchString(int field) {
            return null;
        }

        public Object fetch(int field) {
            return null;
        }

        public void storeBoolean(int field, boolean externalVal) {
            throw new InternalException();
        }

        public void storeByte(int field, byte externalVal) {
            throw new InternalException();
        }

        public void storeChar(int field, char externalVal) {
            throw new InternalException();
        }

        public void storeDouble(int field, double externalVal) {
            throw new InternalException();
        }

        public void storeFloat(int field, float externalVal) {
            throw new InternalException();
        }

        public void storeInt(int field, int externalVal) {
            throw new InternalException();
        }

        public void storeLong(int field, long externalVal) {
            throw new InternalException();
        }

        public void storeObject(int field, Object externalVal) {
            throw new InternalException();
        }

        public void storeShort(int field, short externalVal) {
            throw new InternalException();
        }

        public void storeString(int field, String externalVal) {
            throw new InternalException();
        }

        public void store(int field, Object value) {
            throw new InternalException();
        }

        public Object fetchInitialField(int field) {
            throw new InternalException();
        }

        public void dirty(int field) {
            throw new InternalException();
        }

        public void removed(int field, Object removed, boolean key) {
            throw new InternalException();
        }

        public boolean beforeRefresh(boolean refreshAll) {
            throw new InternalException();
        }

        public void setRemote(int field, Object value) {
            throw new InternalException();
        }

        ///////////////////////////////
        // StateManager implementation
        ///////////////////////////////

        public Object getPCPrimaryKey(Object oid, int field) {
            throw new InternalException();
        }

        public StateManager replaceStateManager(StateManager sm) {
            throw new InternalException();
        }

        public Object getGenericContext() {
            return getContext();
        }

        public void dirty(String field) {
            throw new InternalException();
        }

        public Object fetchObjectId() {
            return getObjectId();
        }

        public void accessingField(int field) {
        }

        public boolean serializing() {
            throw new InternalException();
        }

        public boolean writeDetached(ObjectOutput out) {
            throw new InternalException();
        }

        public void proxyDetachedDeserialized(int idx) {
            throw new InternalException();
        }

        public void settingBooleanField(PersistenceCapable pc, int field,
            boolean val1, boolean val2, int set) {
            throw new InternalException();
        }

        public void settingCharField(PersistenceCapable pc, int field,
            char val1, char val2, int set) {
            throw new InternalException();
        }

        public void settingByteField(PersistenceCapable pc, int field,
            byte val1, byte val2, int set) {
            throw new InternalException();
        }

        public void settingShortField(PersistenceCapable pc, int field,
            short val1, short val2, int set) {
            throw new InternalException();
        }

        public void settingIntField(PersistenceCapable pc, int field,
            int val1, int val2, int set) {
            throw new InternalException();
        }

        public void settingLongField(PersistenceCapable pc, int field,
            long val1, long val2, int set) {
            throw new InternalException();
        }

        public void settingFloatField(PersistenceCapable pc, int field,
            float val1, float val2, int set) {
            throw new InternalException();
        }

        public void settingDoubleField(PersistenceCapable pc, int field,
            double val1, double val2, int set) {
            throw new InternalException();
        }

        public void settingStringField(PersistenceCapable pc, int field,
            String val1, String val2, int set) {
            throw new InternalException();
        }

        public void settingObjectField(PersistenceCapable pc, int field,
            Object val1, Object val2, int set) {
            throw new InternalException();
        }

        public void providedBooleanField(PersistenceCapable pc, int field,
            boolean val) {
            throw new InternalException();
        }

        public void providedCharField(PersistenceCapable pc, int field,
            char val) {
            throw new InternalException();
        }

        public void providedByteField(PersistenceCapable pc, int field,
            byte val) {
            throw new InternalException();
        }

        public void providedShortField(PersistenceCapable pc, int field,
            short val) {
            throw new InternalException();
        }

        public void providedIntField(PersistenceCapable pc, int field,
            int val) {
            throw new InternalException();
        }

        public void providedLongField(PersistenceCapable pc, int field,
            long val) {
            throw new InternalException();
        }

        public void providedFloatField(PersistenceCapable pc, int field,
            float val) {
            throw new InternalException();
        }

        public void providedDoubleField(PersistenceCapable pc, int field,
            double val) {
            throw new InternalException();
        }

        public void providedStringField(PersistenceCapable pc, int field,
            String val) {
            throw new InternalException();
        }

        public void providedObjectField(PersistenceCapable pc, int field,
            Object val) {
            throw new InternalException();
        }

        public boolean replaceBooleanField(PersistenceCapable pc, int field) {
            throw new InternalException();
        }

        public char replaceCharField(PersistenceCapable pc, int field) {
            throw new InternalException();
        }

        public byte replaceByteField(PersistenceCapable pc, int field) {
            throw new InternalException();
        }

        public short replaceShortField(PersistenceCapable pc, int field) {
            throw new InternalException();
        }

        public int replaceIntField(PersistenceCapable pc, int field) {
            throw new InternalException();
        }

        public long replaceLongField(PersistenceCapable pc, int field) {
            throw new InternalException();
        }

        public float replaceFloatField(PersistenceCapable pc, int field) {
            throw new InternalException();
        }

        public double replaceDoubleField(PersistenceCapable pc, int field) {
            throw new InternalException();
        }

        public String replaceStringField(PersistenceCapable pc, int field) {
            throw new InternalException();
        }

        public Object replaceObjectField(PersistenceCapable pc, int field) {
            throw new InternalException();
        }

        @Override
        public boolean isDelayed(int field) {
            return false;
        }
        
        @Override
        public void setDelayed(int field, boolean delay) {
            throw new InternalException();
        }
        
        @Override
        public void loadDelayedField(int field) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Special row manager type that unwraps embedded objects.
     */
    private static class EmbeddedRowManager
        implements RowManager {

        private final RowManager _rm;
        private final Row _row;

        public EmbeddedRowManager(RowManager delegate, Row row) {
            _rm = delegate;
            _row = (row == null) ? null : new EmbeddedRow(row);
        }

        public boolean hasAutoAssignConstraints() {
            return false;
        }

        public Collection getInserts() {
            throw new InternalException();
        }

        public Collection getUpdates() {
            throw new InternalException();
        }

        public Collection getDeletes() {
            throw new InternalException();
        }

        public Collection getSecondaryUpdates() {
            throw new InternalException();
        }

        public Collection getSecondaryDeletes() {
            throw new InternalException();
        }

        public Collection getAllRowUpdates() {
            throw new InternalException();
        }

        public Collection getAllRowDeletes() {
            throw new InternalException();
        }

        public Row getRow(Table table, int action, OpenJPAStateManager sm,
            boolean create) {
            while (sm != null && sm.getOwner() != null)
                sm = sm.getOwner();
            if (_row != null && table == _row.getTable()
                && action == _row.getAction())
                return _row;
            return new EmbeddedRow(_rm.getRow(table, action, sm, create));
        }

        public Row getSecondaryRow(Table table, int action) {
            return new EmbeddedRow(_rm.getSecondaryRow(table, action));
        }

        public void flushSecondaryRow(Row row)
            throws SQLException {
            _rm.flushSecondaryRow(((EmbeddedRow) row).getDelegate());
        }

        public Row getAllRows(Table table, int action) {
            return new EmbeddedRow(_rm.getAllRows(table, action));
        }

        public void flushAllRows(Row row)
            throws SQLException {
            _rm.flushAllRows(((EmbeddedRow) row).getDelegate());
        }
    }

    /**
     * Special row type that unwraps embedded objects.
     */
    private static class EmbeddedRow
        implements Row {

        private final Row _row;

        public EmbeddedRow(Row delegate) {
            _row = delegate;
        }

        public Row getDelegate() {
            return _row;
        }

        public boolean isValid() {
            return _row.isValid();
        }

        public void setValid(boolean valid) {
            _row.setValid(valid);
        }

        public void setPrimaryKey(OpenJPAStateManager sm)
            throws SQLException {
            _row.setPrimaryKey(getOwner(sm));
        }

        public void setPrimaryKey(ColumnIO io, OpenJPAStateManager sm)
            throws SQLException {
            _row.setPrimaryKey(io, getOwner(sm));
        }

        public void wherePrimaryKey(OpenJPAStateManager sm)
            throws SQLException {
            _row.wherePrimaryKey(getOwner(sm));
        }

        public void setForeignKey(ForeignKey fk, OpenJPAStateManager sm)
            throws SQLException {
            _row.setForeignKey(fk, getOwner(sm));
        }

        public void setForeignKey(ForeignKey fk, ColumnIO io,
            OpenJPAStateManager sm)
            throws SQLException {
            _row.setForeignKey(fk, io, getOwner(sm));
        }

        public void whereForeignKey(ForeignKey fk, OpenJPAStateManager sm)
            throws SQLException {
            _row.whereForeignKey(fk, getOwner(sm));
        }

        public void setRelationId(Column col, OpenJPAStateManager sm,
            RelationId rel)
            throws SQLException {
            _row.setRelationId(col, getOwner(sm), rel);
        }

        private OpenJPAStateManager getOwner(OpenJPAStateManager sm) {
            while (sm != null && sm.getOwner() != null)
                sm = sm.getOwner();
            return sm;
        }

        ////////////////////////
        // Pass-through methods
        ////////////////////////

        public Table getTable() {
            return _row.getTable();
        }

        public int getAction() {
            return _row.getAction();
        }

        public Object getFailedObject() {
            return _row.getFailedObject();
        }

        public void setFailedObject(Object failed) {
            _row.setFailedObject(failed);
        }

        public OpenJPAStateManager getPrimaryKey() {
            return _row.getPrimaryKey();
        }

        public void setArray(Column col, Array val)
            throws SQLException {
            _row.setArray(col, val);
        }

        public void setAsciiStream(Column col, InputStream val, int length)
            throws SQLException {
            _row.setAsciiStream(col, val, length);
        }

        public void setBigDecimal(Column col, BigDecimal val)
            throws SQLException {
            _row.setBigDecimal(col, val);
        }

        public void setBigInteger(Column col, BigInteger val)
            throws SQLException {
            _row.setBigInteger(col, val);
        }

        public void setBinaryStream(Column col, InputStream val, int length)
            throws SQLException {
            _row.setBinaryStream(col, val, length);
        }

        public void setBlob(Column col, Blob val)
            throws SQLException {
            _row.setBlob(col, val);
        }

        public void setBoolean(Column col, boolean val)
            throws SQLException {
            _row.setBoolean(col, val);
        }

        public void setByte(Column col, byte val)
            throws SQLException {
            _row.setByte(col, val);
        }

        public void setBytes(Column col, byte[] val)
            throws SQLException {
            _row.setBytes(col, val);
        }

        public void setCalendar(Column col, Calendar val)
            throws SQLException {
            _row.setCalendar(col, val);
        }

        public void setChar(Column col, char val)
            throws SQLException {
            _row.setChar(col, val);
        }

        public void setCharacterStream(Column col, Reader val, int length)
            throws SQLException {
            _row.setCharacterStream(col, val, length);
        }

        public void setClob(Column col, Clob val)
            throws SQLException {
            _row.setClob(col, val);
        }

        public void setDate(Column col, Date val)
            throws SQLException {
            _row.setDate(col, val);
        }

        public void setDate(Column col, java.sql.Date val, Calendar cal)
            throws SQLException {
            _row.setDate(col, val, cal);
        }

        public void setDouble(Column col, double val)
            throws SQLException {
            _row.setDouble(col, val);
        }

        public void setFloat(Column col, float val)
            throws SQLException {
            _row.setFloat(col, val);
        }

        public void setInt(Column col, int val)
            throws SQLException {
            _row.setInt(col, val);
        }

        public void setLong(Column col, long val)
            throws SQLException {
            _row.setLong(col, val);
        }

        public void setLocale(Column col, Locale val)
            throws SQLException {
            _row.setLocale(col, val);
        }

        public void setNull(Column col)
            throws SQLException {
            _row.setNull(col);
        }

        public void setNull(Column col, boolean overrideDefault)
            throws SQLException {
            _row.setNull(col, overrideDefault);
        }

        public void setNumber(Column col, Number val)
            throws SQLException {
            _row.setNumber(col, val);
        }

        public void setObject(Column col, Object val)
            throws SQLException {
            _row.setObject(col, val);
        }

        public void setRaw(Column col, String val)
            throws SQLException {
            _row.setRaw(col, val);
        }

        public void setShort(Column col, short val)
            throws SQLException {
            _row.setShort(col, val);
        }

        public void setString(Column col, String val)
            throws SQLException {
            _row.setString(col, val);
        }

        public void setTime(Column col, Time val, Calendar cal)
            throws SQLException {
            _row.setTime(col, val, cal);
        }

        public void setTimestamp(Column col, Timestamp val, Calendar cal)
            throws SQLException {
            _row.setTimestamp(col, val, cal);
        }

        public void whereArray(Column col, Array val)
            throws SQLException {
            _row.whereArray(col, val);
        }

        public void whereAsciiStream(Column col, InputStream val, int length)
            throws SQLException {
            _row.whereAsciiStream(col, val, length);
        }

        public void whereBigDecimal(Column col, BigDecimal val)
            throws SQLException {
            _row.whereBigDecimal(col, val);
        }

        public void whereBigInteger(Column col, BigInteger val)
            throws SQLException {
            _row.whereBigInteger(col, val);
        }

        public void whereBinaryStream(Column col, InputStream val, int length)
            throws SQLException {
            _row.whereBinaryStream(col, val, length);
        }

        public void whereBlob(Column col, Blob val)
            throws SQLException {
            _row.whereBlob(col, val);
        }

        public void whereBoolean(Column col, boolean val)
            throws SQLException {
            _row.whereBoolean(col, val);
        }

        public void whereByte(Column col, byte val)
            throws SQLException {
            _row.whereByte(col, val);
        }

        public void whereBytes(Column col, byte[] val)
            throws SQLException {
            _row.whereBytes(col, val);
        }

        public void whereCalendar(Column col, Calendar val)
            throws SQLException {
            _row.whereCalendar(col, val);
        }

        public void whereChar(Column col, char val)
            throws SQLException {
            _row.whereChar(col, val);
        }

        public void whereCharacterStream(Column col, Reader val, int length)
            throws SQLException {
            _row.whereCharacterStream(col, val, length);
        }

        public void whereClob(Column col, Clob val)
            throws SQLException {
            _row.whereClob(col, val);
        }

        public void whereDate(Column col, Date val)
            throws SQLException {
            _row.whereDate(col, val);
        }

        public void whereDate(Column col, java.sql.Date val, Calendar cal)
            throws SQLException {
            _row.whereDate(col, val, cal);
        }

        public void whereDouble(Column col, double val)
            throws SQLException {
            _row.whereDouble(col, val);
        }

        public void whereFloat(Column col, float val)
            throws SQLException {
            _row.whereFloat(col, val);
        }

        public void whereInt(Column col, int val)
            throws SQLException {
            _row.whereInt(col, val);
        }

        public void whereLong(Column col, long val)
            throws SQLException {
            _row.whereLong(col, val);
        }

        public void whereLocale(Column col, Locale val)
            throws SQLException {
            _row.whereLocale(col, val);
        }

        public void whereNull(Column col)
            throws SQLException {
            _row.whereNull(col);
        }

        public void whereNumber(Column col, Number val)
            throws SQLException {
            _row.whereNumber(col, val);
        }

        public void whereObject(Column col, Object val)
            throws SQLException {
            _row.whereObject(col, val);
        }

        public void whereRaw(Column col, String val)
            throws SQLException {
            _row.whereRaw(col, val);
        }

        public void whereShort(Column col, short val)
            throws SQLException {
            _row.whereShort(col, val);
        }

        public void whereString(Column col, String val)
            throws SQLException {
            _row.whereString(col, val);
        }

        public void whereTime(Column col, Time val, Calendar cal)
            throws SQLException {
            _row.whereTime(col, val, cal);
        }

        public void whereTimestamp(Column col, Timestamp val, Calendar cal)
            throws SQLException {
            _row.whereTimestamp(col, val, cal);
        }
    }
}
