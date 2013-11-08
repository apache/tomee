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
package org.apache.openjpa.jdbc.meta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.Reflection;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.strats.NoneClassStrategy;
import org.apache.openjpa.jdbc.meta.strats.VerticalClassStrategy;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Schemas;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.util.ApplicationIds;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.MetaDataException;
import org.apache.openjpa.util.OpenJPAId;

/**
 * Specialization of metadata for relational databases.
 *
 * @author Abe White
 */
public class ClassMapping
    extends ClassMetaData
    implements ClassStrategy {

    public static final ClassMapping[] EMPTY_MAPPINGS = new ClassMapping[0];

    private static final Localizer _loc = Localizer.forPackage
        (ClassMapping.class);

    private final ClassMappingInfo _info;
    private final Discriminator _discrim;
    private final Version _version;
    private ClassStrategy _strategy = null;

    private Table _table = null;
    private ColumnIO _io = null;
    private Column[] _cols = Schemas.EMPTY_COLUMNS;
    private ForeignKey _fk = null;
    private int _subclassMode = Integer.MAX_VALUE;

    private ClassMapping[] _joinSubMaps = null;
    private ClassMapping[] _assignMaps = null;

    // maps columns to joinables
    private final Map _joinables = new ConcurrentHashMap();

    /**
     * Constructor. Supply described type and owning repository.
     */
    protected ClassMapping(Class type, MappingRepository repos) {
        super(type, repos);
        _discrim = repos.newDiscriminator(this);
        _version = repos.newVersion(this);
        _info = repos.newMappingInfo(this);
    }

    /**
     * Embedded constructor. Supply embedding value and owning repository.
     */
    protected ClassMapping(ValueMetaData vmd) {
        super(vmd);
        _discrim = getMappingRepository().newDiscriminator(this);
        _version = getMappingRepository().newVersion(this);
        _info = getMappingRepository().newMappingInfo(this);
    }

    /**
     * The class discriminator.
     */
    public Discriminator getDiscriminator() {
        return _discrim;
    }

    /**
     * The version indicator.
     */
    public Version getVersion() {
        return _version;
    }

    ///////////
    // Runtime
    ///////////

    /**
     * Return the oid value stored in the result. This implementation will
     * recurse until it finds an ancestor class who uses oid values for its
     * primary key.
     *
     * @param fk if non-null, use the local columns of the given foreign
     * key in place of this class' primary key columns
     * @see #isPrimaryKeyObjectId
     */
    public Object getObjectId(JDBCStore store, Result res, ForeignKey fk,
        boolean subs, Joins joins)
        throws SQLException {
        ValueMapping embed = getEmbeddingMapping();
        if (embed != null)
            return embed.getFieldMapping().getDefiningMapping().
                getObjectId(store, res, fk, subs, joins);

        return getObjectId(this, store, res, fk, subs, joins);
    }

    /**
     * Recursive helper for public <code>getObjectId</code> method.
     */
    private Object getObjectId(ClassMapping cls, JDBCStore store, Result res,
        ForeignKey fk, boolean subs, Joins joins)
        throws SQLException {
        if (!isPrimaryKeyObjectId(true))
            return getPCSuperclassMapping().getObjectId(cls, store, res, fk,
                subs, joins);
        if (getIdentityType() == ID_UNKNOWN)
            throw new InternalException();

        Column[] pks = getPrimaryKeyColumns();
        if (getIdentityType() == ID_DATASTORE) {
            Column col = (fk == null) ? pks[0] : fk.getColumn(pks[0]);
            long id = res.getLong(col, joins);
            return (id == 0 && res.wasNull()) ? null
                : store.newDataStoreId(id, cls, subs);
        }

        // application identity
        Object[] vals = new Object[getPrimaryKeyFields().length];
        FieldMapping fm;
        Joinable join;
        int pkIdx;
        boolean canReadDiscriminator = true;
        boolean isNullPK = true;
        for (int i = 0; i < pks.length; i++) {
            // we know that all pk column join mappings use primary key fields,
            // cause this mapping uses the oid as its primary key (we recursed
            // at the beginning of the method to ensure this)
            join = assertJoinable(pks[i]);
            fm = getFieldMapping(join.getFieldIndex());
            pkIdx = fm.getPrimaryKeyIndex();
            canReadDiscriminator &= isSelfReference(fk, join.getColumns()); 
            // could have already set value with previous multi-column joinable
            if (vals[pkIdx] == null) {
                res.startDataRequest(fm);
                vals[pkIdx] = join.getPrimaryKeyValue(res, join.getColumns(),
                    fk, store, joins);
                res.endDataRequest();
                isNullPK = isNullPK && vals[pkIdx] == null;
            }
        }
        if (isNullPK) {
            return null;
        }

        // the oid data is loaded by the base type, but if discriminator data
        // is present, make sure to use it to construct the actual oid instance
        // so that we get the correct app id class, etc
        
        // Discriminator refers to the row but the vals[] may hold data that
        // refer to another row. Then there is little point reading the disc
        // value

        ClassMapping dcls = cls;
        if (subs && canReadDiscriminator) {
            res.startDataRequest(cls.getDiscriminator());
            try {
                Class dtype = cls.getDiscriminator().getClass(store, cls, res);
                if (dtype != cls.getDescribedType())
                  dcls = cls.getMappingRepository().getMapping(dtype, 
                    store.getContext().getClassLoader(), true); 
            } catch (Exception e) {
                // intentionally ignored
            }
            res.endDataRequest();  
        }
        Object oid = ApplicationIds.fromPKValues(vals, dcls);
        if (oid instanceof OpenJPAId) {
            ((OpenJPAId) oid).setManagedInstanceType(dcls.getDescribedType(), 
                subs);
        }
        return oid;
    }
    
    boolean isSelfReference(ForeignKey fk, Column[] cols) {
    	if (fk == null)
    		return true;
    	for (Column col : cols)
    		if (fk.getColumn(col) != col)
    			return false;
    	return true;
    }

    /**
     * Return the given column value(s) for the given object. The given
     * columns will be primary key columns of this mapping, but may be in
     * any order. If there is only one column, return its value. If there
     * are multiple columns, return an object array of their values, in the
     * same order the columns are given.
     */
    public Object toDataStoreValue(Object obj, Column[] cols, JDBCStore store) {
        Object ret = (cols.length == 1) ? null : new Object[cols.length];

        // in the past we've been lenient about being able to translate objects
        // from other persistence contexts, so try to get sm directly from
        // instance before asking our context
        OpenJPAStateManager sm;
        if (ImplHelper.isManageable(obj)) {
        	PersistenceCapable pc = ImplHelper.toPersistenceCapable(obj,
                    getRepository().getConfiguration());
            sm = (OpenJPAStateManager) pc.pcGetStateManager();
            if (sm == null) {
            	ret = getValueFromUnmanagedInstance(obj, cols, true);
            } else if (sm.isDetached()) {
            	obj = store.getContext().find(sm.getObjectId(), false, null);
            	sm = store.getContext().getStateManager(obj);
            }
        } else {
            sm = store.getContext().getStateManager(obj);
        }
        if (sm == null)
            return ret;

        Object val;
        for (int i = 0; i < cols.length; i++) {
            val = assertJoinable(cols[i]).getJoinValue(sm, cols[i], store);
            if (cols.length == 1)
                ret = val;
            else
                ((Object[]) ret)[i] = val;
        }
        return ret;
    }
    
    /**
     * Return the joinable for the given column, or throw an exception if
     * none is available.
     */
    public Joinable assertJoinable(Column col) {
        Joinable join = getJoinable(col);
        if (join == null)
            throw new MetaDataException(_loc.get("no-joinable",
                col.getQualifiedPath().toString()));
        return join;
    }

    /**
     * Return the {@link Joinable} for the given column. Any column that
     * another mapping joins to must be controlled by a joinable.
     */
    public Joinable getJoinable(Column col) {
        Joinable join;
        if (getEmbeddingMetaData() != null) {
            join = getEmbeddingMapping().getFieldMapping().
                getDefiningMapping().getJoinable(col);
            if (join != null)
                return join;
        }
        ClassMapping sup = getJoinablePCSuperclassMapping();
        if (sup != null) {
            join = sup.getJoinable(col);
            if (join != null)
                return join;
        }
        return (Joinable) _joinables.get(col);
    }

    /**
     * Add the given column-to-joinable mapping.
     */
    public void setJoinable(Column col, Joinable joinable) {
        // don't let non-pk override pk
        Joinable join = (Joinable) _joinables.get(col);
        if (join == null || (join.getFieldIndex() != -1
            && getField(join.getFieldIndex()).getPrimaryKeyIndex() == -1))
            _joinables.put(col, joinable);
    }

    /**
     * Return whether the columns of the given foreign key to this mapping
     * can be used to construct an object id for this type. This is a
     * relatively expensive operation; its results should be cached.
     *
     * @return {@link Boolean#TRUE} if the foreign key contains all oid
     * columns, <code>null</code> if it contains only some columns,
     * or {@link Boolean#FALSE} if it contains non-oid columns
     */
    public Boolean isForeignKeyObjectId(ForeignKey fk) {
        // if this mapping's primary key can't construct an oid, then no way
        // foreign key can
        if (getIdentityType() == ID_UNKNOWN || !isPrimaryKeyObjectId(false))
            return Boolean.FALSE;

        // with datastore identity, it's all or nothing
        Column[] cols = fk.getPrimaryKeyColumns();
        if (getIdentityType() == ID_DATASTORE) {
            if (cols.length != 1 || cols[0] != getPrimaryKeyColumns()[0])
                return Boolean.FALSE;
            return Boolean.TRUE;
        }

        // check the join mapping for each pk column to see if it links up to
        // a primary key field
        Joinable join;
        for (int i = 0; i < cols.length; i++) {
            join = assertJoinable(cols[i]);
            if (join.getFieldIndex() != -1
                && getField(join.getFieldIndex()).getPrimaryKeyIndex() == -1)
                return Boolean.FALSE;
        }

        // if all primary key links, see whether we join to all pks
        if (isPrimaryKeyObjectId(true)
            && cols.length == getPrimaryKeyColumns().length)
            return Boolean.TRUE;
        return null;
    }

    ///////
    // ORM
    ///////

    /**
     * Raw mapping data.
     */
    public ClassMappingInfo getMappingInfo() {
        return _info;
    }

    /**
     * The strategy used to map this mapping.
     */
    public ClassStrategy getStrategy() {
        return _strategy;
    }

    /**
     * The strategy used to map this mapping. The <code>adapt</code>
     * parameter determines whether to adapt when mapping the strategy;
     * use null if the strategy should not be mapped.
     */
    public void setStrategy(ClassStrategy strategy, Boolean adapt) {
        // set strategy first so we can access it during mapping
        ClassStrategy orig = _strategy;
        _strategy = strategy;
        if (strategy != null) {
            try {
                strategy.setClassMapping(this);
                if (adapt != null)
                    strategy.map(adapt.booleanValue());
            } catch (RuntimeException re) {
                // reset strategy
                _strategy = orig;
                throw re;
            }
        }
    }

    /**
     * The mapping's primary table.
     */
    public Table getTable() {
        return _table;
    }

    /**
     * The mapping's primary table.
     */
    public void setTable(Table table) {
        _table = table;
    }

    /**
     * The columns this mapping uses to uniquely identify an object.
     * These will typically be the primary key columns or the columns this
     * class uses to link to its superclass table.
     */
    public Column[] getPrimaryKeyColumns() {
        if (getIdentityType() == ID_APPLICATION && isMapped()) {
            if (_cols.length == 0) {
                FieldMapping[] pks = getPrimaryKeyFieldMappings();
                Collection cols = new ArrayList(pks.length);
                Column[] fieldCols;
                for (int i = 0; i < pks.length; i++) {
                    fieldCols = pks[i].getColumns();
                    if (fieldCols.length == 0) {
                        _cols = new Column[0];
                        return _cols;
                    }
                    for (int j = 0; j < fieldCols.length; j++)
                        cols.add(fieldCols[j]);
                }
                _cols = (Column[]) cols.toArray(new Column[cols.size()]);
            }
        }
        return _cols;
    }

    /**
     * The columns this mapping uses to uniquely identify an object.
     * These will typically be the primary key columns or the columns this
     * class uses to link to its superclass table.
     */
    public void setPrimaryKeyColumns(Column[] cols) {
        if (cols == null)
            cols = Schemas.EMPTY_COLUMNS;
        _cols = cols;
    }

    /**
     * I/O information on the key columns / join key.
     */
    public ColumnIO getColumnIO() {
        return (_io == null) ? ColumnIO.UNRESTRICTED : _io;
    }

    /**
     * I/O information on the key columns / join key.
     */
    public void setColumnIO(ColumnIO io) {
        _io = io;
    }

    /**
     * Foreign key linking the primary key columns to the superclass table,
     * or null if none.
     */
    public ForeignKey getJoinForeignKey() {
        return _fk;
    }

    /**
     * Foreign key linking the primary key columns to the superclass table,
     * or null if none.
     */
    public void setJoinForeignKey(ForeignKey fk) {
        _fk = fk;
    }

    public void refSchemaComponents() {
        if (getEmbeddingMetaData() == null) {
            if (_table != null && _table.getPrimaryKey() != null)
                _table.getPrimaryKey().ref();
            if (_fk != null)
                _fk.ref();
            Column[] pks = getPrimaryKeyColumns();
            for (int i = 0; i < pks.length; i++)
                pks[i].ref();
        } else {
            FieldMapping[] fields = getFieldMappings();
            for (int i = 0; i < fields.length; i++)
                fields[i].refSchemaComponents();
        }
    }

    /**
     * Clear mapping information, including strategy.
     */
    public void clearMapping() {
        _strategy = null;
        _cols = Schemas.EMPTY_COLUMNS;
        _fk = null;
        _table = null;
        _info.clear();
        setResolve(MODE_MAPPING | MODE_MAPPING_INIT, false);
    }

    /**
     * Update {@link MappingInfo} with our current mapping information.
     */
    public void syncMappingInfo() {
        if (getEmbeddingMetaData() == null)
            _info.syncWith(this);
        else {
            _info.clear();
            FieldMapping[] fields = getFieldMappings();
            for (int i = 0; i < fields.length; i++)
                fields[i].syncMappingInfo();
        }
    }

    //////////////////////
    // MetaData interface
    //////////////////////

    protected void setDescribedType(Class type) {
        super.setDescribedType(type);
        // this method called from superclass constructor, so _info not yet
        // initialized
        if (_info != null)
            _info.setClassName(type.getName());
    }

    /**
     * The subclass fetch mode, as one of the eager constants in
     * {@link JDBCFetchConfiguration}.
     */
    public int getSubclassFetchMode() {
        if (_subclassMode == Integer.MAX_VALUE) {
            if (getPCSuperclass() != null)
                _subclassMode = getPCSuperclassMapping().
                    getSubclassFetchMode();
            else
                _subclassMode = FetchConfiguration.DEFAULT;
        }
        return _subclassMode;
    }

    /**
     * The subclass fetch mode, as one of the eager constants in
     * {@link JDBCFetchConfiguration}.
     */
    public void setSubclassFetchMode(int mode) {
        _subclassMode = mode;
    }

    /**
     * Convenience method to perform cast from
     * {@link ClassMetaData#getRepository}.
     */
    public MappingRepository getMappingRepository() {
        return (MappingRepository) getRepository();
    }

    /**
     * Convenience method to perform cast from
     * {@link ClassMetaData#getEmbeddingMetaData}
     */
    public ValueMapping getEmbeddingMapping() {
        return (ValueMapping) getEmbeddingMetaData();
    }

    /**
     * Returns true if this class does not use the "none" strategy (including
     * if it has a null strategy, and therefore is probably in the process of
     * being mapped).
     */
    public boolean isMapped() {
        if (!super.isMapped())
            return false;
        if (_strategy != null)
            return _strategy != NoneClassStrategy.getInstance();
        return !NoneClassStrategy.ALIAS.equals(_info.getStrategy());
    }

    /**
     * Convenience method to perform cast from
     * {@link ClassMetaData#getPCSuperclassMetaData}.
     */
    public ClassMapping getPCSuperclassMapping() {
        return (ClassMapping) getPCSuperclassMetaData();
    }

    /**
     * Convenience method to perform cast from
     * {@link ClassMetaData#getMappedPCSuperclassMetaData}.
     */
    public ClassMapping getMappedPCSuperclassMapping() {
        return (ClassMapping) getMappedPCSuperclassMetaData();
    }

    /**
     * Return the nearest mapped superclass that can join to this class.
     */
    public ClassMapping getJoinablePCSuperclassMapping() {
        ClassMapping sup = getMappedPCSuperclassMapping();
        if (sup == null)
            return null;
        if (_fk != null || _table == null || _table.equals(sup.getTable()))
            return sup;
        return null;
    }

    /**
     * Convenience method to perform cast from
     * {@link ClassMetaData#getPCSubclassMetaDatas}.
     */
    public ClassMapping[] getPCSubclassMappings() {
        return (ClassMapping[]) getPCSubclassMetaDatas();
    }

    /**
     * Convenience method to perform cast from
     * {@link ClassMetaData#getMappedPCSubclassMetaDatas}.
     */
    public ClassMapping[] getMappedPCSubclassMappings() {
        return (ClassMapping[]) getMappedPCSubclassMetaDatas();
    }

    /**
     * Return mapped subclasses that are reachable via joins.
     */
    public ClassMapping[] getJoinablePCSubclassMappings() {
        ClassMapping[] subs = getMappedPCSubclassMappings(); // checks for new
        if (_joinSubMaps == null) {
            if (subs.length == 0)
                _joinSubMaps = subs;
            else {
                List joinable = new ArrayList(subs.length);
                for (int i = 0; i < subs.length; i++)
                    if (isSubJoinable(subs[i]))
                        joinable.add(subs[i]);
                _joinSubMaps = (ClassMapping[]) joinable.toArray
                    (new ClassMapping[joinable.size()]);
            }
        }
        return _joinSubMaps;
    }

    /**
     * Return whether we can reach the given subclass via joins.
     */
    private boolean isSubJoinable(ClassMapping sub) {
        if (sub == null)
            return false;
        if (sub == this)
            return true;
        return isSubJoinable(sub.getJoinablePCSuperclassMapping());
    }

    /**
     * Returns the closest-derived list of non-inter-joinable mapped types
     * assignable to this type. May return this mapping.
     */
    public ClassMapping[] getIndependentAssignableMappings() {
        ClassMapping[] subs = getMappedPCSubclassMappings(); // checks for new
        if (_assignMaps == null) {
            // remove unmapped subs
            if (subs.length == 0) {
                if (isMapped())
                    _assignMaps = new ClassMapping[]{ this };
                else
                    _assignMaps = subs;
            } else {
                int size = (int) (subs.length * 1.33 + 2);
                Set independent = new LinkedHashSet(size);
                if (isMapped())
                    independent.add(this);
                independent.addAll(Arrays.asList(subs));

                // remove all mappings that have a superclass mapping in the set
                ClassMapping map, sup;
                List clear = null;
                for (Iterator itr = independent.iterator(); itr.hasNext();) {
                    map = (ClassMapping) itr.next();
                    sup = map.getJoinablePCSuperclassMapping();
                    if (sup != null && independent.contains(sup)) {
                        if (clear == null)
                            clear = new ArrayList(independent.size() - 1);
                        clear.add(map);
                    }
                }
                if (clear != null)
                    independent.removeAll(clear);

                _assignMaps = (ClassMapping[]) independent.toArray
                    (new ClassMapping[independent.size()]);
            }
        }
        return _assignMaps;
    }

    /**
     * Convenience method to perform cast from {@link ClassMetaData#getFields}.
     */
    public FieldMapping[] getFieldMappings() {
        return (FieldMapping[]) getFields();
    }

    /**
     * Convenience method to perform cast from
     * {@link ClassMetaData#getDeclaredFields}.
     */
    public FieldMapping[] getDeclaredFieldMappings() {
        return (FieldMapping[]) getDeclaredFields();
    }

    /**
     * Convenience method to perform cast from
     * {@link ClassMetaData#getPrimaryKeyFields}.
     */
    public FieldMapping[] getPrimaryKeyFieldMappings() {
        return (FieldMapping[]) getPrimaryKeyFields();
    }

    /**
     * Convenience method to perform cast from
     * {@link ClassMetaData#getVersionField}.
     */
    public FieldMapping getVersionFieldMapping() {
        return (FieldMapping) getVersionField();
    }

    /**
     * Convenience method to perform cast from
     * {@link ClassMetaData#getDefaultFetchGroupFields}.
     */
    public FieldMapping[] getDefaultFetchGroupFieldMappings() {
        return (FieldMapping[]) getDefaultFetchGroupFields();
    }

    /**
     * Convenience method to perform cast from
     * {@link ClassMetaData#getDefinedFields}.
     */
    public FieldMapping[] getDefinedFieldMappings() {
        return (FieldMapping[]) getDefinedFields();
    }

    /**
     * Convenience method to perform cast from
     * {@link ClassMetaData#getFieldsInListingOrder}.
     */
    public FieldMapping[] getFieldMappingsInListingOrder() {
        return (FieldMapping[]) getFieldsInListingOrder();
    }

    /**
     * Convenience method to perform cast from
     * {@link ClassMetaData#getDefinedFieldsInListingOrder}.
     */
    public FieldMapping[] getDefinedFieldMappingsInListingOrder() {
        return (FieldMapping[]) getDefinedFieldsInListingOrder();
    }

    /**
     * Convenience method to perform cast from {@link ClassMetaData#getField}.
     */
    public FieldMapping getFieldMapping(int index) {
        return (FieldMapping) getField(index);
    }

    /**
     * Convenience method to perform cast from
     * {@link ClassMetaData#getDeclaredField}.
     */
    public FieldMapping getDeclaredFieldMapping(int index) {
        return (FieldMapping) getDeclaredField(index);
    }

    /**
     * Convenience method to perform cast from {@link ClassMetaData#getField}.
     */
    public FieldMapping getFieldMapping(String name) {
        return (FieldMapping) getField(name);
    }

    /**
     * Convenience method to perform cast from
     * {@link ClassMetaData#getDeclaredField}.
     */
    public FieldMapping getDeclaredFieldMapping(String name) {
        return (FieldMapping) getDeclaredField(name);
    }

    /**
     * Convenience method to perform cast from
     * {@link ClassMetaData#getDeclaredUnmanagedFields}.
     */
    public FieldMapping[] getDeclaredUnmanagedFieldMappings() {
        return (FieldMapping[]) getDeclaredUnmanagedFields();
    }

    /**
     * Convenience method to perform cast from
     * {@link ClassMetaData#addDeclaredField}.
     */
    public FieldMapping addDeclaredFieldMapping(String name, Class type) {
        return (FieldMapping) addDeclaredField(name, type);
    }

    protected void resolveMapping(boolean runtime) {
        super.resolveMapping(runtime);

        // map class strategy; it may already be mapped by the repository before
        // the resolve process begins
        MappingRepository repos = getMappingRepository();
        if (_strategy == null)
            repos.getStrategyInstaller().installStrategy(this);
        Log log = getRepository().getLog();
        if (log.isTraceEnabled())
            log.trace(_loc.get("strategy", this, _strategy.getAlias()));

        // make sure unmapped superclass fields are defined if we're mapped;
        // also may have been done by repository already
        defineSuperclassFields(getJoinablePCSuperclassMapping() == null);

        // resolve everything that doesn't rely on any relations to avoid
        // recursion, then resolve all fields
        resolveNonRelationMappings();
        FieldMapping[] fms = getFieldMappings();
        for (int i = 0; i < fms.length; i++) {
            if (fms[i].getDefiningMetaData() == this) {
                boolean fill = getMappingRepository().getMappingDefaults().
                    defaultMissingInfo();
                ForeignKey fk = fms[i].getForeignKey();
                if (fill && fk != null && 
                    fk.getPrimaryKeyColumns().length == 0) { 
                    // set resolve mode to force this field mapping to be 
                    // resolved again. The need to resolve again occurs when 
                    // a primary key is a relation field with the foreign key
                    // annotation. In this situation, this primary key field
                    // mapping is resolved during the call to 
                    // resolveNonRelationMapping. Since it is a relation
                    // field, the foreign key will be constructed. However, 
                    // the primary key of the parent entity may not have been 
                    // resolved yet, resulting in missing information in the fk
                    fms[i].setResolve(MODE_META); 
                    if (fms[i].getStrategy() != null)
                        fms[i].getStrategy().map(false);
                }                
                fms[i].resolve(MODE_MAPPING);
            }
        }
        fms = getDeclaredUnmanagedFieldMappings();
        for (int i = 0; i < fms.length; i++)
            fms[i].resolve(MODE_MAPPING);

        // mark mapped columns
        if (_cols != null) {
            ColumnIO io = getColumnIO();
            for (int i = 0; i < _cols.length; i++) {
                if (io.isInsertable(i, false))
                    _cols[i].setFlag(Column.FLAG_DIRECT_INSERT, true);
                if (io.isUpdatable(i, false))
                    _cols[i].setFlag(Column.FLAG_DIRECT_UPDATE, true);
            }
        }
        // once columns are resolved, resolve unique constraints as they need
        // the columns be resolved 
        _info.getUniques(this, true);
    }
    
    /**
     * Resolve non-relation field mappings so that when we do relation
     * mappings they can rely on them for joins.
     */
    void resolveNonRelationMappings() {
        // make sure primary key fields are resolved first because other
        // fields might rely on them
        FieldMapping[] fms = getPrimaryKeyFieldMappings();
        for (int i = 0; i < fms.length; i++)
            fms[i].resolve(MODE_MAPPING);

        // resolve defined fields that are safe; that don't rely on other types
        // also being resolved.  don't use getDefinedFields b/c it relies on
        // whether fields are mapped, which isn't known yet
        fms = getFieldMappings();
        for (int i = 0; i < fms.length; i++)
            if (fms[i].getDefiningMetaData() == this
                && !fms[i].isTypePC() && !fms[i].getKey().isTypePC()
                && !fms[i].getElement().isTypePC())
                fms[i].resolve(MODE_MAPPING);

        _discrim.resolve(MODE_MAPPING);
        _version.resolve(MODE_MAPPING);        
    }

    protected void initializeMapping() {
        super.initializeMapping();

        FieldMapping[] fields = getDefinedFieldMappings();
        for (int i = 0; i < fields.length; i++)
            fields[i].resolve(MODE_MAPPING_INIT);
        _discrim.resolve(MODE_MAPPING_INIT);
        _version.resolve(MODE_MAPPING_INIT);
        _strategy.initialize();
    }

    protected void clearDefinedFieldCache() {
        // just make this method available to other classes in this package
        super.clearDefinedFieldCache();
    }

    protected void clearSubclassCache() {
        super.clearSubclassCache();
        _joinSubMaps = null;
        _assignMaps = null;
    }

    public void copy(ClassMetaData cls) {
        super.copy(cls);
        if (_subclassMode == Integer.MAX_VALUE)
            _subclassMode = ((ClassMapping) cls).getSubclassFetchMode();
    }

    protected boolean validateDataStoreExtensionPrefix(String prefix) {
        return "jdbc-".equals(prefix);
    }

    ////////////////////////////////
    // ClassStrategy implementation
    ////////////////////////////////

    public String getAlias() {
        return assertStrategy().getAlias();
    }

    public void map(boolean adapt) {
        assertStrategy().map(adapt);
    }

    public void initialize() {
        assertStrategy().initialize();
    }

    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        assertStrategy().insert(sm, store, rm);
    }

    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        assertStrategy().update(sm, store, rm);
    }

    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        assertStrategy().delete(sm, store, rm);
    }

    public Boolean isCustomInsert(OpenJPAStateManager sm, JDBCStore store) {
        return assertStrategy().isCustomInsert(sm, store);
    }

    public Boolean isCustomUpdate(OpenJPAStateManager sm, JDBCStore store) {
        return assertStrategy().isCustomUpdate(sm, store);
    }

    public Boolean isCustomDelete(OpenJPAStateManager sm, JDBCStore store) {
        return assertStrategy().isCustomDelete(sm, store);
    }

    public void customInsert(OpenJPAStateManager sm, JDBCStore store)
        throws SQLException {
        assertStrategy().customInsert(sm, store);
    }

    public void customUpdate(OpenJPAStateManager sm, JDBCStore store)
        throws SQLException {
        assertStrategy().customUpdate(sm, store);
    }

    public void customDelete(OpenJPAStateManager sm, JDBCStore store)
        throws SQLException {
        assertStrategy().customDelete(sm, store);
    }

    public void setClassMapping(ClassMapping owner) {
        assertStrategy().setClassMapping(owner);
    }

    public boolean isPrimaryKeyObjectId(boolean hasAll) {
        return assertStrategy().isPrimaryKeyObjectId(hasAll);
    }

    public Joins joinSuperclass(Joins joins, boolean toThis) {
        return assertStrategy().joinSuperclass(joins, toThis);
    }

    public boolean supportsEagerSelect(Select sel, OpenJPAStateManager sm,
        JDBCStore store, ClassMapping base, JDBCFetchConfiguration fetch) {
        return assertStrategy().supportsEagerSelect(sel, sm, store, base,
            fetch);
    }

    public ResultObjectProvider customLoad(JDBCStore store, boolean subclasses,
        JDBCFetchConfiguration fetch, long startIdx, long endIdx)
        throws SQLException {
        return assertStrategy().customLoad(store, subclasses, fetch,
            startIdx, endIdx);
    }

    public boolean customLoad(OpenJPAStateManager sm, JDBCStore store,
        PCState state, JDBCFetchConfiguration fetch)
        throws SQLException, ClassNotFoundException {
        return assertStrategy().customLoad(sm, store, state, fetch);
    }

    public boolean customLoad(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result result)
        throws SQLException {
        return assertStrategy().customLoad(sm, store, fetch, result);
    }
    
    private ClassStrategy assertStrategy() {
        if (_strategy == null)
            throw new InternalException();
        return _strategy;
    }
    
    /**
     * Find the field mappings that correspond to the given columns.
     * 
     * @return null if no columns are given or no field mapping uses the given
     * columns.
     */
    private List<FieldMapping> getFieldMappings(Column[] cols, boolean prime) {
    	if (cols == null || cols.length == 0)
    		return null;
    	List<FieldMapping> result = null;
    	for (Column c : cols) {
    		List<FieldMapping> fms = hasColumn(c, prime);
    		if (fms == null) continue;
			if (result == null)
				result = new ArrayList<FieldMapping>();
			for (FieldMapping fm : fms)
				if (!result.contains(fm))
					result.add(fm);
    	}
    	return result;
    }
    
    /**
     * Looks up in reverse to find the list of field mappings that include the
     * given column. Costly.
     * 
     * @return null if no field mappings carry this column. 
     */
    private List<FieldMapping> hasColumn(Column c, boolean prime) {
    	List<FieldMapping> result = null;
    	FieldMapping[] fms = (prime) ? 
    		getPrimaryKeyFieldMappings() : getFieldMappings();
    	for (FieldMapping fm : fms) {
    		Column[] cols = fm.getColumns();
    		if (contains(cols, c)) {
    			if (result == null)
    				result = new ArrayList<FieldMapping>();
    			result.add(fm);
    		}
    	}
    	return result;
    }
    
    boolean contains(Column[] cols, Column c) {
    	for (Column col : cols)
    		if (col == c)
    			return true;
    	return false;
    }
    
    /**
     * Gets the field values of the given instance for the given columns.
     * The given columns are used to identify the fields by a reverse lookup.
     *  
     * @return a single object or an array of objects based on number of 
     * fields the given columns represent.
     */
    private Object getValueFromUnmanagedInstance(Object obj, Column[] cols, 
    		boolean prime) {
    	List<FieldMapping> fms = getFieldMappings(cols, prime);
    	if (fms == null)
    		return null;
    	if (fms.size() == 1)
    		return Reflection.getValue(obj, fms.get(0).getName(), true);
    	Object[] result = new Object[fms.size()];
    	int i = 0;
    	for (FieldMapping fm : fms) {
    		result[i++] = Reflection.getValue(obj, fm.getName(), true);
    	}
    	return result;
    }

    public boolean isVerticalStrategy() {
        String strat = getMappingInfo().getHierarchyStrategy();
        if (strat != null && strat.equals(VerticalClassStrategy.ALIAS))
            return true;
        return false;
    }
}
