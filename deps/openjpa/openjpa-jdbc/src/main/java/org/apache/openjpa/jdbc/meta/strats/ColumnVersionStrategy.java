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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Comparator;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.VersionMappingInfo;
import org.apache.openjpa.jdbc.meta.strats.AbstractVersionStrategy;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.MixedLockLevels;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StoreManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.MetaDataException;

/**
 * Uses a one or more column(s) and corresponding version object.
 *
 * @author Marc Prud'hommeaux
 * @author Pinaki Poddar
 */
public abstract class ColumnVersionStrategy
    extends AbstractVersionStrategy {

    private static final Localizer _loc = Localizer.forPackage
        (ColumnVersionStrategy.class);

    /**
     * Return the code from {@link JavaTypes} for the version values this
     * strategy uses. This method is only used during mapping installation.
     */
    protected abstract int getJavaType();
    
    /**
     * Return the code from {@link JavaTypes} for the version value this given
     * column index uses. Only used if the version strategy employs more than
     * one column. 
     */
    protected int getJavaType(int i) {
        throw new AbstractMethodError(_loc.get(
                "multi-column-version-unsupported",getAlias()).toString());
    }
    
    /**
     * Return the next version given the current one, which may be null.
     */
    protected abstract Object nextVersion(Object version);

    /**
     * Compare the two versions. Defaults to assuming the version objects
     * implement {@link Comparable}.
     *
     * @see Comparator#compare
     */
    protected int compare(Object v1, Object v2) {
        if (v1 == v2)
            return 0;
        if (v1 == null)
            return -1;
        if (v2 == null)
            return 1;
        
        if (v1.getClass().isArray()) {
        	if (!v2.getClass().isArray())
        		throw new InternalException();
        	return compare((Object[])v1, (Object[])v2);
        }
        if (v1.getClass() != v2.getClass()) {
            if (v1 instanceof Number && !(v1 instanceof BigDecimal))
                v1 = new BigDecimal(((Number) v1).doubleValue());

            if (v2 instanceof Number && !(v2 instanceof BigDecimal))
                v2 = new BigDecimal(((Number) v2).doubleValue());
        }

        return ((Comparable) v1).compareTo(v2);
    }


	/**
	 * Compare each element of the given arrays that must be of equal size. 
     * The given array values represent version values and the result designate
	 * whether first version is earlier, same or later than the second one.
	 * 
	 * @return If any element of a1 is later than corresponding element of
     * a2 then returns 1 i.e. the first version is later than the second
     * version. If each element of a1 is equal to corresponding element of a2
     * then return 0 i.e. the first version is same as the second version.
	 * else return a negative number i.e. the first version is earlier than 
	 * the second version.
	 */
	protected int compare(Object[] a1, Object[] a2) {
		if (a1.length != a2.length)
	    	throw new InternalException();
		int total = 0;
		for (int i = 0; i < a1.length; i++) {
			int c =  compare(a1[i], a2[i]);
			if (c > 0) 
				return 1;
			total += c;
		}
		return total;
	}
	
    public void map(boolean adapt) {
        ClassMapping cls = vers.getClassMapping();
        if (cls.getJoinablePCSuperclassMapping() != null
            || cls.getEmbeddingMetaData() != null)
            throw new MetaDataException(_loc.get("not-base-vers", cls));

        VersionMappingInfo info = vers.getMappingInfo();
        info.assertNoJoin(vers, true);
        info.assertNoForeignKey(vers, !adapt);
        info.assertNoUnique(vers, false);
        if (info.getColumns().size() > 1) {
        	Column[] templates = new Column[info.getColumns().size()];
        	for (int i = 0; i < info.getColumns().size(); i++) {
                templates[i] = new Column();
        		Column infoColumn = (Column)info.getColumns().get(i);
        		templates[i].setTableIdentifier(infoColumn.getTableIdentifier());
        		templates[i].setType(infoColumn.getType());
        		templates[i].setSize(infoColumn.getSize());
                templates[i].setDecimalDigits(infoColumn.getDecimalDigits());
        		templates[i].setJavaType(getJavaType(i));
        		templates[i].setIdentifier(infoColumn.getIdentifier());
        	}
        	Column[] cols = info.getColumns(vers, templates, adapt);
        	for (int i = 0; i < cols.length; i++)
        		cols[i].setVersionStrategy(this);
        	vers.setColumns(cols);
        	vers.setColumnIO(info.getColumnIO());
        } else {
           Column tmplate = new Column();
           tmplate.setJavaType(getJavaType());
           DBDictionary dict = vers.getMappingRepository().getDBDictionary();
           DBIdentifier versName = DBIdentifier.newColumn("versn", dict != null ? dict.delimitAll() : false);
           tmplate.setIdentifier(versName);

           Column[] cols = info.getColumns(vers, new Column[]{ tmplate },
                   adapt);
           cols[0].setVersionStrategy(this);
           vers.setColumns(cols);
           vers.setColumnIO(info.getColumnIO());

           Index idx = info.getIndex(vers, cols, adapt);
           vers.setIndex(idx);
        }
    }

    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        Column[] cols = vers.getColumns();
        ColumnIO io = vers.getColumnIO();
        Object initial = nextVersion(null);
        for (int i = 0; i < cols.length; i++) {
            Row row = rm.getRow(cols[i].getTable(), Row.ACTION_INSERT, sm,
                    true);
            if (io.isInsertable(i, initial == null))
                row.setObject(cols[i], getColumnValue(initial, i));
        }
        // set initial version into state manager
        Object nextVersion;
        nextVersion = initial;
        sm.setNextVersion(nextVersion);
    }

    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        Column[] cols = vers.getColumns();
        if (cols == null || cols.length == 0 ||
            !sm.isDirty() && !sm.isVersionUpdateRequired())
            return;

        Object curVersion = sm.getVersion();
        Object nextVersion = nextVersion(curVersion);


        // set where and update conditions on row
        for (int i = 0; i < cols.length; i++) {
            Row row = rm.getRow(cols[i].getTable(), Row.ACTION_UPDATE, sm,
                    true);
            row.setFailedObject(sm.getManagedInstance());
            if (curVersion != null && sm.isVersionCheckRequired()) {
                row.whereObject(cols[i], getColumnValue(curVersion, i));
                if (isSecondaryColumn(cols[i], sm)) {
                	ForeignKey[] fks = cols[i].getTable().getForeignKeys();
                	for (ForeignKey fk : fks) {
                		row.whereForeignKey(fk, sm);
                	}
                }
            }
            if (vers.getColumnIO().isUpdatable(i, nextVersion == null))
                row.setObject(cols[i], getColumnValue(nextVersion, i));
        }

        if (nextVersion != null)
            sm.setNextVersion(nextVersion);
    }

    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        Column[] cols = vers.getColumns();

        Object curVersion = sm.getVersion();
        Object cur;
        for (int i = 0; i < cols.length; i++) {
            Row row = rm.getRow(cols[i].getTable(),
            	Row.ACTION_DELETE, sm, true);
            row.setFailedObject(sm.getManagedInstance());
            cur = getColumnValue(curVersion, i);
            // set where and update conditions on row
            if (cur != null) {
                row.whereObject(cols[i], cur);
                if (isSecondaryColumn(cols[i], sm)) {
                	ForeignKey[] fks = cols[i].getTable().getForeignKeys();
                	for (ForeignKey fk : fks) {
                		row.whereForeignKey(fk, sm);
                	}
                }
            }
        }
    }

    public boolean select(Select sel, ClassMapping mapping) {
        sel.select(vers.getColumns());
        return true;
    }
    
    public Object load(OpenJPAStateManager sm, JDBCStore store, Result res) 
        throws SQLException {
        return this.load(sm, store, res, null);
    }

    public Object load(OpenJPAStateManager sm, JDBCStore store, Result res, Joins joins)
        throws SQLException {
        // typically if one version column is in the result, they all are, so
        // optimize by checking for the first one before doing any real work
        Column[] cols = vers.getColumns();
        if (!res.contains(cols[0], joins)) {
            return null;
        }

        Object version = populateFromResult(res, joins);
        
        // OPENJPA-662 Allow a null StateManager because this method may just be
        // invoked to get the result of projection query
        if (sm != null) {
        	sm.setVersion(version);
        }
        return version;
    }

    public boolean checkVersion(OpenJPAStateManager sm, JDBCStore store,
        boolean updateVersion)
        throws SQLException {
        Column[] cols = vers.getColumns();
        Select sel = store.getSQLFactory().newSelect();
        sel.select(cols);
        sel.wherePrimaryKey(sm.getObjectId(), vers.getClassMapping(), store);

        // No need to lock version field (i.e. optimistic), except when version update is required (e.g. refresh) 
        JDBCFetchConfiguration fetch = store.getFetchConfiguration();
        if (!updateVersion && fetch.getReadLockLevel() >= MixedLockLevels.LOCK_PESSIMISTIC_READ) {
            fetch = (JDBCFetchConfiguration) fetch.clone();
            fetch.setReadLockLevel(MixedLockLevels.LOCK_NONE);
        }
        Result res = sel.execute(store, fetch);
        try {
            if (!res.next())
                return false;

            Object memVersion = sm.getVersion();
            Object dbVersion  = populateFromResult(res, null);
            boolean refresh   = compare(memVersion, dbVersion) < 0;

            if (updateVersion)
                sm.setVersion(dbVersion);
            return !refresh;
        } finally {
            res.close();
        }
    }

    public int compareVersion(Object v1, Object v2) {
        if (v1 == v2)
            return StoreManager.VERSION_SAME;
        if (v1 == null || v2 == null)
            return StoreManager.VERSION_DIFFERENT;

        int cmp = compare(v1, v2);
        if (cmp < 0)
            return StoreManager.VERSION_EARLIER;
        if (cmp > 0)
            return StoreManager.VERSION_LATER;
        return StoreManager.VERSION_SAME;
    }
        
    /**
     * Populate values of a version object from the given result.
     * 
     * @return a single Object or an array depending on whether using a single
     * or multiple columns being used for representation.
    */
    Object populateFromResult(Result res, Joins joins) throws SQLException {
        if (res == null)
 		return null;
    	
        Column[] cols = vers.getColumns();
        Object[] values = new Object[cols.length];
        for (int i = 0; i < cols.length; i++) {
            values[i] = res.getObject(cols[i], null, joins);
        }
        return (cols.length == 1) ? values[0] : values;
    }
    
    Object getColumnValue(Object o, int idx) {
    	if (o == null) 
    		return null;
    	if (o.getClass().isArray())
    		return Array.get(o, idx);
    	return o;
    }
    
    boolean isSecondaryColumn(Column col, OpenJPAStateManager sm) {
    	ClassMapping mapping = (ClassMapping)sm.getMetaData();
    	while (mapping != null) {
    		if (mapping.getTable() == col.getTable())
    			return false;
    		else
    			mapping = mapping.getPCSuperclassMapping();
    	}
    	return true;
    }
}
