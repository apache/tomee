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
package org.apache.openjpa.jdbc.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.kernel.exps.Context;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.UnsupportedException;
import org.apache.openjpa.util.UserException;

/**
 * A logical union made up of multiple distinct selects whose results are
 * combined in memory.
 *
 * @author Abe White
 */
public class LogicalUnion
    implements Union {

    private static final Localizer _loc = Localizer.forPackage
        (LogicalUnion.class);

    protected final UnionSelect[] sels;
    protected final DBDictionary dict;
    protected final ClassMapping[] mappings;
    protected final BitSet desc = new BitSet();
    private boolean _distinct = true;
   

    /**
     * Constructor.
     *
     * @param conf system configuration
     * @param sels the number of SQL selects to union together
     */
    public LogicalUnion(JDBCConfiguration conf, int sels) {
        this(conf, sels, null);
    }

    /**
     * Constructor used to seed the internal selects.
     */
    public LogicalUnion(JDBCConfiguration conf, Select[] seeds) {
        this(conf, seeds.length, seeds);
    }

    /**
     * Delegate constructor.
     */
    protected LogicalUnion(JDBCConfiguration conf, int sels, Select[] seeds) {
        if (sels == 0)
            throw new InternalException("sels == 0");

        dict = conf.getDBDictionaryInstance();
        mappings = new ClassMapping[sels];
        this.sels = new UnionSelect[sels];

        SelectImpl seed;
        for (int i = 0; i < sels; i++) {
            seed = (seeds == null)
                ? (SelectImpl) conf.getSQLFactoryInstance().newSelect()
                : (SelectImpl) seeds[i];
            this.sels[i] = newUnionSelect(seed, i);
        }
    }

    /**
     * Create a new union select with the given delegate and union position.
     */
    protected UnionSelect newUnionSelect(SelectImpl seed, int pos) {
        return new UnionSelect(seed, pos);
    }

    public Select[] getSelects() {
        return sels;
    }
   
    public boolean isUnion() {
        return false;
    }

    public void abortUnion() {
    }

    public String getOrdering() {
        return null;
    }

    public JDBCConfiguration getConfiguration() {
        return sels[0].getConfiguration();
    }

    public DBDictionary getDBDictionary() {
        return dict;
    }

    public SQLBuffer toSelect(boolean forUpdate, JDBCFetchConfiguration fetch) {
        return dict.toSelect(sels[0], forUpdate, fetch);
    }
    
    public SQLBuffer getSQL() {
        return sels.length == 1 ? sels[0].getSQL() : null;
    }

    public SQLBuffer toSelectCount() {
        return dict.toSelectCount(sels[0]);
    }

    public boolean getAutoDistinct() {
        return sels[0].getAutoDistinct();
    }

    public void setAutoDistinct(boolean distinct) {
        for (int i = 0; i < sels.length; i++)
            sels[i].setAutoDistinct(distinct);
    }

    public boolean isDistinct() {
        return _distinct;
    }

    public void setDistinct(boolean distinct) {
        _distinct = distinct;
    }

    public boolean isLRS() {
        return sels[0].isLRS();
    }

    public void setLRS(boolean lrs) {
        for (int i = 0; i < sels.length; i++)
            sels[i].setLRS(lrs);
    }

    public int getExpectedResultCount() {
        return sels[0].getExpectedResultCount();
    }
    
    public void setExpectedResultCount(int expectedResultCount,
        boolean force) {
        for (int i = 0; i < sels.length; i++)
            sels[i].setExpectedResultCount(expectedResultCount, force);
    }

    public int getJoinSyntax() {
        return sels[0].getJoinSyntax();
    }

    public void setJoinSyntax(int syntax) {
        for (int i = 0; i < sels.length; i++)
            sels[i].setJoinSyntax(syntax);
    }

    public boolean supportsRandomAccess(boolean forUpdate) {
        if (sels.length == 1)
            return sels[0].supportsRandomAccess(forUpdate);
        return false;
    }

    public boolean supportsLocking() {
        if (sels.length == 1)
            return sels[0].supportsLocking();
        for (int i = 0; i < sels.length; i++)
            if (!sels[i].supportsLocking())
                return false;
        return true;
    }

    public boolean hasMultipleSelects() {
        if (sels != null && sels.length > 1)
            return true;
        return sels[0].hasMultipleSelects();
    }

    public int getCount(JDBCStore store)
        throws SQLException {
        int count = 0;
        for (int i = 0; i < sels.length; i++)
            count += sels[i].getCount(store);
        return count;
    }

    public Result execute(JDBCStore store, JDBCFetchConfiguration fetch)
        throws SQLException {
        if (fetch == null)
            fetch = store.getFetchConfiguration();
        return execute(store, fetch, fetch.getReadLockLevel());
    }

    public Result execute(JDBCStore store, JDBCFetchConfiguration fetch,
        int lockLevel)
        throws SQLException {
        if (fetch == null)
            fetch = store.getFetchConfiguration();

        if (sels.length == 1) {
            Result res = sels[0].execute(store, fetch, lockLevel);
            ((AbstractResult) res).setBaseMapping(mappings[0]);
            return res;
        }

        if (getExpectedResultCount() == 1) {
            AbstractResult res;
            for (int i = 0; i < sels.length; i++) {
                res = (AbstractResult) sels[i].execute(store, fetch,
                    lockLevel);
                res.setBaseMapping(mappings[i]);
                res.setIndexOf(i);

                // if we get to the last select, just return its result
                if (i == sels.length - 1)
                    return res;

                // return the first result that has a row
                try {
                    if (!res.next())
                        res.close();
                    else {
                        res.pushBack();
                        return res;
                    }
                }
                catch (SQLException se) {
                    res.close();
                    throw se;
                }
            }
        }

        // create a single result from each select in our fake union, merging
        // them as needed
        AbstractResult[] res = new AbstractResult[sels.length];
        List[] orderIdxs = null;
        try {
            List l;
            for (int i = 0; i < res.length; i++) {
                res[i] = (AbstractResult) sels[i].execute(store, fetch,
                    lockLevel);
                res[i].setBaseMapping(mappings[i]);
                res[i].setIndexOf(i);

                l = sels[i].getSelectedOrderIndexes();
                if (l != null) {
                    if (orderIdxs == null)
                        orderIdxs = new List[sels.length];
                    orderIdxs[i] = l;
                }
            }
        } catch (SQLException se) {
            for (int i = 0; res[i] != null; i++)
                res[i].close();
            throw se;
        }

        // if multiple selects have ordering, use a comparator to collate
        ResultComparator comp = null;
        if (orderIdxs != null)
            comp = new ResultComparator(orderIdxs, desc, dict);
        return new MergedResult(res, comp);
    }

    public void select(Union.Selector selector) {
        for (int i = 0; i < sels.length; i++)
            selector.select(sels[i], i);
    }

    public String toString() {
        return toSelect(false, null).getSQL();
    }

    /**
     * A callback used to create the selects in a SQL union.
     */
    public static interface Selector {

        /**
         * Populate the <code>i</code>th select in the union.
         */
        public void select(Select sel, int i);
    }

    /**
     * A select that is part of a logical union.
     */
    public class UnionSelect
        implements Select {

        protected final SelectImpl sel;
        protected final int pos;
        protected int orders = 0;
        protected List orderIdxs = null;
       
        public UnionSelect(SelectImpl sel, int pos) {
            this.sel = sel;
            this.pos = pos;
            sel.setRecordOrderedIndexes(true);
        }

        /**
         * Delegate select.
         */
        public SelectImpl getDelegate() {
            return sel;
        }

        /**
         * Return the indexes of the data in the select clause this query is
         * ordered by.
         */
        public List getSelectedOrderIndexes() {
            if (orderIdxs == null)
                orderIdxs = sel.getOrderedIndexes();
            return orderIdxs;
        }

        public JDBCConfiguration getConfiguration() {
            return sel.getConfiguration();
        }

        public int indexOf() {
            return pos;
        }

        public SQLBuffer toSelect(boolean forUpdate,
            JDBCFetchConfiguration fetch) {
            return sel.toSelect(forUpdate, fetch);
        }
        
        public SQLBuffer getSQL() {
            return sel.getSQL();
        }

        public SQLBuffer toSelectCount() {
            return sel.toSelectCount();
        }

        public boolean getAutoDistinct() {
            return sel.getAutoDistinct();
        }

        public void setAutoDistinct(boolean distinct) {
            sel.setAutoDistinct(distinct);
        }

        public boolean isDistinct() {
            return sel.isDistinct();
        }

        public void setDistinct(boolean distinct) {
            sel.setDistinct(distinct);
        }

        public boolean isLRS() {
            return sel.isLRS();
        }

        public void setLRS(boolean lrs) {
            sel.setLRS(lrs);
        }

        public int getJoinSyntax() {
            return sel.getJoinSyntax();
        }

        public void setJoinSyntax(int joinSyntax) {
            sel.setJoinSyntax(joinSyntax);
        }

        public boolean supportsRandomAccess(boolean forUpdate) {
            return sel.supportsRandomAccess(forUpdate);
        }

        public boolean supportsLocking() {
            return sel.supportsLocking();
        }

        public boolean hasMultipleSelects() {
            return sel.hasMultipleSelects();
        }

        public int getCount(JDBCStore store)
            throws SQLException {
            return sel.getCount(store);
        }

        public Result execute(JDBCStore store, JDBCFetchConfiguration fetch)
            throws SQLException {
            return sel.execute(store, fetch);
        }

        public Result execute(JDBCStore store, JDBCFetchConfiguration fetch,
            int lockLevel)
            throws SQLException {
            return sel.execute(store, fetch, lockLevel);
        }

        public List getSubselects() {
            return Collections.EMPTY_LIST;
        }

        public Select getParent() {
            return null;
        }

        public String getSubselectPath() {
            return null;
        }

        public void setParent(Select parent, String path) {
            throw new UnsupportedException(_loc.get("union-element"));
        }

        public void setHasSubselect(boolean hasSub) {
            sel.setHasSubselect(hasSub);
        }
        
        public boolean getHasSubselect() {
            return sel.getHasSubselect();    
        }
        
        public Select getFromSelect() {
            return null;
        }

        public void setFromSelect(Select sel) {
            throw new UnsupportedException(_loc.get("union-element"));
        }

        public boolean hasEagerJoin(boolean toMany) {
            return sel.hasEagerJoin(toMany);
        }

        public boolean hasJoin(boolean toMany) {
            return sel.hasJoin(toMany);
        }

        public boolean isSelected(Table table) {
            return sel.isSelected(table);
        }

        public Collection getTableAliases() {
            return sel.getTableAliases();
        }

        public List getSelects() {
            return sel.getSelects();
        }

        public List getSelectAliases() {
            return sel.getSelectAliases();
        }

        public List getIdentifierAliases() {
            return sel.getIdentifierAliases();
        }

        public SQLBuffer getOrdering() {
            return sel.getOrdering();
        }

        public SQLBuffer getGrouping() {
            return sel.getGrouping();
        }

        public SQLBuffer getWhere() {
            return sel.getWhere();
        }

        public SQLBuffer getHaving() {
            return sel.getHaving();
        }

        public void addJoinClassConditions() {
            sel.addJoinClassConditions();
        }

        public Joins getJoins() {
            return sel.getJoins();
        }

        public Iterator getJoinIterator() {
            return sel.getJoinIterator();
        }

        public long getStartIndex() {
            return sel.getStartIndex();
        }

        public long getEndIndex() {
            return sel.getEndIndex();
        }

        public void setRange(long start, long end) {
            sel.setRange(start, end);
        }

        public String getColumnAlias(Column col) {
            return sel.getColumnAlias(col);
        }

        public String getColumnAlias(Column col, Joins joins) {
            return sel.getColumnAlias(col, joins);
        }

        public String getColumnAlias(Column col, Object alias) {
            return sel.getColumnAlias(col, alias);
        }

        public String getColumnAlias(String col, Table table) {
            return sel.getColumnAlias(col, table);
        }

        public String getColumnAlias(String col, Table table, Joins joins) {
            return sel.getColumnAlias(col, table, joins);
        }

        public boolean isAggregate() {
            return sel.isAggregate();
        }

        public void setAggregate(boolean agg) {
            sel.setAggregate(agg);
        }

        public boolean isLob() {
            return sel.isLob();
        }

        public void setLob(boolean lob) {
            sel.setLob(lob);
        }

        public void selectPlaceholder(String sql) {
            sel.selectPlaceholder(sql);
        }

        public void clearSelects() {
            sel.clearSelects();
        }

        public boolean select(SQLBuffer sql, Object id) {
            return sel.select(sql, id);
        }

        public boolean select(SQLBuffer sql, Object id, Joins joins) {
            return sel.select(sql, id, joins);
        }

        public boolean select(String sql, Object id) {
            return sel.select(sql, id);
        }

        public boolean select(String sql, Object id, Joins joins) {
            return sel.select(sql, id, joins);
        }

        public boolean select(Column col) {
            return sel.select(col);
        }

        public boolean select(Column col, Joins joins) {
            return sel.select(col, joins);
        }

        public int select(Column[] cols) {
            return sel.select(cols);
        }

        public int select(Column[] cols, Joins joins) {
            return sel.select(cols, joins);
        }

        public void select(ClassMapping mapping, int subclasses,
            JDBCStore store, JDBCFetchConfiguration fetch, int eager) {
            select(mapping, subclasses, store, fetch, eager, null, false);
        }

        public void select(ClassMapping mapping, int subclasses,
            JDBCStore store, JDBCFetchConfiguration fetch, int eager,
            Joins joins) {
            select(mapping, subclasses, store, fetch, eager, joins, false);
        }

        private void select(ClassMapping mapping, int subclasses,
            JDBCStore store, JDBCFetchConfiguration fetch, int eager,
            Joins joins, boolean identifier) {
            // if this is the first (primary) mapping selected for this
            // SELECT, record it so we can figure out what the result type is
            // since the discriminator might not be selected
            if (mappings[pos] == null)
                mappings[pos] = mapping;

            sel.select(this, mapping, subclasses, store, fetch, eager,
                joins, identifier);
        }

        public boolean selectIdentifier(Column col) {
            return sel.selectIdentifier(col);
        }

        public boolean selectIdentifier(Column col, Joins joins) {
            return sel.selectIdentifier(col, joins);
        }

        public int selectIdentifier(Column[] cols) {
            return sel.selectIdentifier(cols);
        }

        public int selectIdentifier(Column[] cols, Joins joins) {
            return sel.selectIdentifier(cols, joins);
        }

        public void selectIdentifier(ClassMapping mapping, int subclasses,
            JDBCStore store, JDBCFetchConfiguration fetch, int eager) {
            select(mapping, subclasses, store, fetch, eager, null, true);
        }

        public void selectIdentifier(ClassMapping mapping, int subclasses,
            JDBCStore store, JDBCFetchConfiguration fetch, int eager,
            Joins joins) {
            select(mapping, subclasses, store, fetch, eager, joins, true);
        }

        public int selectPrimaryKey(ClassMapping mapping) {
            return sel.selectPrimaryKey(mapping);
        }

        public int selectPrimaryKey(ClassMapping mapping, Joins joins) {
            return sel.selectPrimaryKey(mapping, joins);
        }

        public int orderByPrimaryKey(ClassMapping mapping, boolean asc,
            boolean select) {
            return orderByPrimaryKey(mapping, asc, null, select);
        }

        public int orderByPrimaryKey(ClassMapping mapping, boolean asc,
            Joins joins, boolean select) {
            ClassMapping pks = mapping;
            while (!pks.isPrimaryKeyObjectId(true))
                pks = pks.getJoinablePCSuperclassMapping();
            Column[] cols = pks.getPrimaryKeyColumns();
            recordOrderColumns(cols, asc);
            return sel.orderByPrimaryKey(mapping, asc, joins, select,
                isUnion());
        }

        /**
         * Record that we're ordering by a SQL expression.
         */
        protected void recordOrder(Object ord, boolean asc) {
            if (ord == null)
                return;
            orderIdxs = null;

            int idx = orders++;
            if (desc.get(idx) && asc)
                throw new UserException(_loc.get("incompat-ordering"));
            if (!asc)
                desc.set(idx);
        }

        /**
         * Record that we're ordering by the given columns.
         */
        protected void recordOrderColumns(Column[] cols, boolean asc) {
            for (int i = 0; i < cols.length; i++)
                recordOrder(cols[i], asc);
        }

        public boolean orderBy(Column col, boolean asc, boolean select) {
            return orderBy(col, asc, null, select);
        }

        public boolean orderBy(Column col, boolean asc, Joins joins,
            boolean select) {
            recordOrder(col, asc);
            return sel.orderBy(col, asc, joins, select, isUnion());
        }

        public int orderBy(Column[] cols, boolean asc, boolean select) {
            return orderBy(cols, asc, null, select);
        }

        public int orderBy(Column[] cols, boolean asc, Joins joins,
            boolean select) {
            recordOrderColumns(cols, asc);
            return sel.orderBy(cols, asc, joins, select, isUnion());
        }

        public boolean orderBy(SQLBuffer sql, boolean asc, boolean select,
            Value selAs) {
            return orderBy(sql, asc, null, select, selAs);
        }

        public boolean orderBy(SQLBuffer sql, boolean asc, Joins joins,
            boolean select, Value selAs) {
            recordOrder(sql.getSQL(false), asc);
            return sel.orderBy(sql, asc, joins, select, isUnion(), selAs);
        }

        public boolean orderBy(String sql, boolean asc, boolean select) {
            return orderBy(sql, asc, null, select);
        }

        public boolean orderBy(String sql, boolean asc, Joins joins,
            boolean select) {
            recordOrder(sql, asc);
            return sel.orderBy(sql, asc, joins, select, isUnion());
        }

        public void clearOrdering() {
            sel.clearOrdering();
        }

        public void wherePrimaryKey(Object oid, ClassMapping mapping,
            JDBCStore store) {
            sel.wherePrimaryKey(oid, mapping, store);
        }

        public void whereForeignKey(ForeignKey fk, Object oid,
            ClassMapping mapping, JDBCStore store) {
            sel.whereForeignKey(fk, oid, mapping, store);
        }

        public void where(Joins joins) {
            sel.where(joins);
        }

        public void where(SQLBuffer sql) {
            sel.where(sql);
        }

        public void where(SQLBuffer sql, Joins joins) {
            sel.where(sql, joins);
        }

        public void where(String sql) {
            sel.where(sql);
        }

        public void where(String sql, Joins joins) {
            sel.where(sql, joins);
        }

        public void having(SQLBuffer sql) {
            sel.having(sql);
        }

        public void having(SQLBuffer sql, Joins joins) {
            sel.having(sql, joins);
        }

        public void having(String sql) {
            sel.having(sql);
        }

        public void having(String sql, Joins joins) {
            sel.having(sql, joins);
        }

        public void groupBy(SQLBuffer sql) {
            sel.groupBy(sql);
        }

        public void groupBy(SQLBuffer sql, Joins joins) {
            sel.groupBy(sql, joins);
        }

        public void groupBy(String sql) {
            sel.groupBy(sql);
        }

        public void groupBy(String sql, Joins joins) {
            sel.groupBy(sql, joins);
        }

        public void groupBy(Column col) {
            sel.groupBy(col);
        }

        public void groupBy(Column col, Joins joins) {
            sel.groupBy(col, joins);
        }

        public void groupBy(Column[] cols) {
            sel.groupBy(cols);
        }

        public void groupBy(Column[] cols, Joins joins) {
            sel.groupBy(cols, joins);
        }

        public void groupBy(ClassMapping mapping, int subclasses, 
            JDBCStore store, JDBCFetchConfiguration fetch) {
            sel.groupBy(mapping, subclasses, store, fetch);
        }

        public void groupBy(ClassMapping mapping, int subclasses, 
            JDBCStore store, JDBCFetchConfiguration fetch, Joins joins) {
            sel.groupBy(mapping, subclasses, store, fetch, joins);
        }

        public SelectExecutor whereClone(int sels) {
            return sel.whereClone(sels);
        }

        public SelectExecutor fullClone(int sels) {
            return sel.fullClone(sels);
        }

        public SelectExecutor eagerClone(FieldMapping key, int eagerType,
            boolean toMany, int sels) {
            SelectExecutor ex = sel.eagerClone(key, eagerType, toMany, sels);
            return (ex == sel) ? this : ex;
        }

        public SelectExecutor getEager(FieldMapping key) {
            SelectExecutor ex = sel.getEager(key);
            return (ex == sel) ? this : ex;
        }

        public Joins newJoins() {
            return sel.newJoins();
        }

        public Joins newOuterJoins() {
            return sel.newOuterJoins();
        }

        public void append(SQLBuffer buf, Joins joins) {
            sel.append(buf, joins);
        }

        public Joins and(Joins joins1, Joins joins2) {
            return sel.and(joins1, joins2);
        }

        public Joins or(Joins joins1, Joins joins2) {
            return sel.or(joins1, joins2);
        }

        public Joins outer(Joins joins) {
            return sel.outer(joins);
        }

        public String toString() {
            return sel.toString();
        }

        public int getExpectedResultCount() {
            return sel.getExpectedResultCount();
        }

        public void setExpectedResultCount(int expectedResultCount, 
            boolean force) {
            sel.setExpectedResultCount(expectedResultCount, force);
        }

        public void setContext(Context context) {
            sel.setContext(context);
        }

        public Context ctx() {
            return sel.ctx();
        }

        public void setSchemaAlias(String schemaAlias) {
            sel.setSchemaAlias(schemaAlias);
        }

        public void logEagerRelations() {
            sel.logEagerRelations();            
        }
        public void setTablePerClassMeta(ClassMapping meta) {            
        }

        public ClassMapping getTablePerClassMeta() {
            return sel.getTablePerClassMeta();
        }
        
        public void setJoinedTableClassMeta(List meta) {
            sel.setJoinedTableClassMeta(meta);
        }

        public List getJoinedTableClassMeta() {
            return sel.getJoinedTableClassMeta();
        }
        
        public void setExcludedJoinedTableClassMeta(List meta) {
            sel.setExcludedJoinedTableClassMeta(meta);
        }

        public List getExcludedJoinedTableClassMeta() {
            return sel.getExcludedJoinedTableClassMeta();
        }
        
        public DBDictionary getDictionary() { 
            return dict; 
        }
    }

    /**
     * Comparator for collating ordered results when faking a union.
     */
    private static class ResultComparator
        implements MergedResult.ResultComparator {

        private final List[] _orders;
        private final BitSet _desc;
        private final DBDictionary _dict;

        public ResultComparator(List[] orders, BitSet desc, DBDictionary dict) {
            _orders = orders;
            _desc = desc;
            _dict = dict;
        }

        public Object getOrderingValue(Result res, int idx) {
            // if one value just return it
            ResultSet rs = ((ResultSetResult) res).getResultSet();
            if (_orders[idx].size() == 1)
                return getOrderingValue(rs, _orders[idx].get(0));

            // return array of values
            Object[] vals = new Object[_orders[idx].size()];
            for (int i = 0; i < vals.length; i++)
                vals[i] = getOrderingValue(rs, _orders[idx].get(i));
            return vals;
        }

        /**
         * Extract value at given index from result set.
         */
        private Object getOrderingValue(ResultSet rs, Object i) {
            try {
                return _dict.getObject(rs, ((Integer) i).intValue() + 1, null);
            } catch (SQLException se) {
                throw SQLExceptions.getStore(se, _dict);
            }
        }

        public int compare(Object o1, Object o2) {
            if (o1 == o2)
                return 0;
            if (o1 == null)
                return (_desc.get(0)) ? -1 : 1;
            if (o2 == null)
                return (_desc.get(0)) ? 1 : -1;

            int cmp;
            if (!(o1 instanceof Object[])) {
                if (!(o2 instanceof Object[])) {
                    cmp = ((Comparable) o1).compareTo(o2);
                    return (_desc.get(0)) ? -cmp : cmp;
                }

                cmp = ((Comparable) o1).compareTo(((Object[]) o2)[0]);
                if (cmp != 0)
                    return (_desc.get(0)) ? -cmp : cmp;
                return -1;
            }

            if (!(o2 instanceof Object[])) {
                cmp = ((Comparable) ((Object[]) o1)[0]).compareTo(o2);
                if (cmp != 0)
                    return (_desc.get(0)) ? -cmp : cmp;
                return 1;
            }

            Object[] a1 = (Object[]) o1;
            Object[] a2 = (Object[]) o2;
            for (int i = 0; i < a1.length; i++) {
                cmp = ((Comparable) a1[i]).compareTo(a2[i]);
                if (cmp != 0)
                    return (_desc.get(i)) ? -cmp : cmp;
            }
            return a1.length - a2.length;
        }
    }
}
