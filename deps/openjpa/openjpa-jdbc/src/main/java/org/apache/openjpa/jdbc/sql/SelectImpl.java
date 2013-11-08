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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.commons.collections.iterators.EmptyIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.kernel.EagerFetchModes;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCLockManager;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.kernel.JDBCStoreManager;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.Joinable;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.meta.strats.RelationStrategies;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.kernel.exps.Context;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.ApplicationIds;
import org.apache.openjpa.util.Id;
import org.apache.openjpa.util.InternalException;

/**
 * Standard {@link Select} implementation. Usage note: though this class
 * implements {@link Joins}, it should not be used for joining directly.
 * Instead, use the return value of {@link #newJoins}.
 *
 * @author Abe White
 * @nojavadoc
 */
public class SelectImpl
    implements Select, PathJoins {

    private static final int NONAUTO_DISTINCT = 2 << 0;
    private static final int DISTINCT = 2 << 1;
    private static final int NOT_DISTINCT = 2 << 2;
    private static final int IMPLICIT_DISTINCT = 2 << 3;
    private static final int TO_MANY = 2 << 4;
    private static final int AGGREGATE = 2 << 5;
    private static final int LOB = 2 << 6;
    private static final int OUTER = 2 << 7;
    private static final int LRS = 2 << 8;
    private static final int EAGER_TO_ONE = 2 << 9;
    private static final int EAGER_TO_MANY = 2 << 10;
    private static final int RECORD_ORDERED = 2 << 11;
    private static final int GROUPING = 2 << 12;
    private static final int FORCE_COUNT = 2 << 13;

    private static final String[] TABLE_ALIASES = new String[16];
    private static final String[] ORDER_ALIASES = new String[16];
    private static final Object[] NULL_IDS = new Object[16];
    private static final Object[] PLACEHOLDERS = new Object[50];

    private static final Localizer _loc = Localizer.forPackage(Select.class);

    static {
        for (int i = 0; i < TABLE_ALIASES.length; i++)
            TABLE_ALIASES[i] = "t" + i;
        for (int i = 0; i < ORDER_ALIASES.length; i++)
            ORDER_ALIASES[i] = "o" + i;
        for (int i = 0; i < NULL_IDS.length; i++)
            NULL_IDS[i] = new NullId();
        for (int i = 0; i < PLACEHOLDERS.length; i++)
            PLACEHOLDERS[i] = new Placeholder();
    }

    private final JDBCConfiguration _conf;
    private final DBDictionary _dict;

    // map of variable + relation path + table keys to the correct alias index:
    // each relation path/table combination should have a unique alias because
    // it represents a separate object; for example, if a Person class has a
    // 'parent' field representing another Person and also has an 'address'
    // field of type Address:
    // 'address.street' should map to a different table alias than
    // 'parent.address.street' for the purposes of comparisons
    private Map _aliases = null;

    // map of indexes to table aliases like 'TABLENAME t0'
    private SortedMap _tables = null;

    // combined list of selected ids and map of each id to its alias
    protected final Selects _selects = newSelects();
    private List _ordered = null;
    private List _grouped = null;

    // flags
    private int _flags = 0;
    private int _joinSyntax = 0;
    private long _startIdx = 0;
    private long _endIdx = Long.MAX_VALUE;
    private int _nullIds = 0;
    private int _orders = 0;
    private int _placeholders = 0;
    private int _expectedResultCount = 0;

    // query clauses
    private SQLBuffer _ordering = null;
    private SQLBuffer _where = null;
    private SQLBuffer _grouping = null;
    private SQLBuffer _having = null;
    private SQLBuffer _full = null;

    // joins to add to the end of our where clause, and joins to prepend to
    // all selects (see select(classmapping) method)
    private SelectJoins _joins = null;
    private Stack _preJoins = null;

    // map of joins+keys to eager selects and global set of eager keys; the
    // same key can't be used more than once
    private Map _eager = null;
    private Set _eagerKeys = null;

    // subselect support
    private List<SelectImpl> _subsels = null;
    private SelectImpl _parent = null;
    private String _subPath = null;
    private boolean _hasSub = false;

    // from select if this select selects from a tmp table created by another
    private SelectImpl _from = null;
    protected SelectImpl _outer = null;

    // JPQL Query context this select is associated with
    private Context _ctx = null;

    // A path navigation is begin with this schema alias
    private String _schemaAlias = null;
    private ClassMapping _tpcMeta = null;
    private List _joinedTables = null;
    private List _exJoinedTables = null;
    
    public ClassMapping getTablePerClassMeta() {
        return _tpcMeta;
    }
    public void setTablePerClassMeta(ClassMapping meta) {
        _tpcMeta = meta;
    }
    
    public void setJoinedTableClassMeta(List meta) {
        _joinedTables = meta;
    }

    public List getJoinedTableClassMeta() {
        return _joinedTables;
    }
    
    public void setExcludedJoinedTableClassMeta(List meta) {
        _exJoinedTables = meta;
    }

    public List getExcludedJoinedTableClassMeta() {
        return _exJoinedTables;
    }
    
     
    /**
     * Helper method to return the proper table alias for the given alias index.
     */
    static String toAlias(int index) {
        if (index == -1)
            return null;
        if (index < TABLE_ALIASES.length)
            return TABLE_ALIASES[index];
        return "t" + index;
    }

    /**
     * Helper method to return the proper order alias for the given order
     * column index.
     */
    public static String toOrderAlias(int index) {
        if (index == -1)
            return null;
        if (index < ORDER_ALIASES.length)
            return ORDER_ALIASES[index];
        return "o" + index;
    }

    /**
     * Constructor. Supply configuration.
     */
    public SelectImpl(JDBCConfiguration conf) {
        _conf = conf;
        _dict = _conf.getDBDictionaryInstance();
        _joinSyntax = _dict.joinSyntax;
        _selects._dict = _dict;
    }

    public void setContext(Context context) {
        if (_ctx == null) {
            _ctx = context;
            _ctx.setSelect(this);
        }
    }

    public Context ctx() {
        return _ctx;
    }

    public void setSchemaAlias(String schemaAlias) {
        _schemaAlias = schemaAlias;
    }

    /////////////////////////////////
    // SelectExecutor implementation
    /////////////////////////////////

    public JDBCConfiguration getConfiguration() {
        return _conf;
    }

    public SQLBuffer toSelect(boolean forUpdate, JDBCFetchConfiguration fetch) {
        _full = _dict.toSelect(this, forUpdate, fetch);
        return _full;
    }
    
    public SQLBuffer getSQL() {
        return _full;
    }

    public SQLBuffer toSelectCount() {
        return _dict.toSelectCount(this);
    }

    public boolean getAutoDistinct() {
        return (_flags & NONAUTO_DISTINCT) == 0;
    }

    public void setAutoDistinct(boolean val) {
        if (val)
            _flags &= ~NONAUTO_DISTINCT;
        else
            _flags |= NONAUTO_DISTINCT;
    }

    public boolean isDistinct() {
        return (_flags & NOT_DISTINCT) == 0 && ((_flags & DISTINCT) != 0
            || ((_flags & NONAUTO_DISTINCT) == 0
            && (_flags & IMPLICIT_DISTINCT) != 0));
    }

    public void setDistinct(boolean distinct) {
        // need two flags in case set not_distinct, then a to-many join happens
        // and distinct flag gets set automatically
        if (distinct) {
            _flags |= DISTINCT;
            _flags &= ~NOT_DISTINCT;
        } else {
            _flags |= NOT_DISTINCT;
            _flags &= ~DISTINCT;
        }
    }

    public boolean isLRS() {
        return (_flags & LRS) != 0;
    }

    public void setLRS(boolean lrs) {
        if (lrs)
            _flags |= LRS;
        else
            _flags &= ~LRS;
    }

    public int getExpectedResultCount() {
        // if the count isn't forced and we have to-many eager joins that could
        // throw the count off, don't pay attention to it
        if ((_flags & FORCE_COUNT) == 0 && hasEagerJoin(true))
            return 0;
        return _expectedResultCount;
    }

    public void setExpectedResultCount(int expectedResultCount, boolean force) {
        _expectedResultCount = expectedResultCount;
        if (force)
            _flags |= FORCE_COUNT;
        else 
            _flags &= ~FORCE_COUNT;
    }

    public int getJoinSyntax() {
        return _joinSyntax;
    }

    public void setJoinSyntax(int joinSyntax) {
        _joinSyntax = joinSyntax;
    }

    public boolean supportsRandomAccess(boolean forUpdate) {
        return _dict.supportsRandomAccessResultSet(this, forUpdate);
    }

    public boolean supportsLocking() {
        return _dict.supportsLocking(this);
    }

    public boolean hasMultipleSelects() {
        if (_eager == null)
            return false;
        Map.Entry entry;
        for (Iterator itr = _eager.entrySet().iterator(); itr.hasNext();) {
            entry = (Map.Entry) itr.next();
            if (entry.getValue() != this)
                return true;
        }
        return false;
    }

    public int getCount(JDBCStore store)
        throws SQLException {
        Connection conn = null;
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        try {
            SQLBuffer sql = toSelectCount();
            conn = store.getNewConnection();
            stmnt = prepareStatement(conn, sql, null, 
                ResultSet.TYPE_FORWARD_ONLY, 
                ResultSet.CONCUR_READ_ONLY, false);
            _dict.setQueryTimeout(stmnt,
                    store.getFetchConfiguration().getQueryTimeout());
            rs = executeQuery(conn, stmnt, sql, false, store);
            int count =  getCount(rs);
             
            return _dict.applyRange(this, count);
        } finally {
            if (rs != null)
                try { rs.close(); } catch (SQLException se) {}
            if (stmnt != null)
                try { stmnt.close(); } catch (SQLException se) {}
            if (conn != null)
                try { conn.close(); } catch (SQLException se) {}
        }
    }

    public Result execute(JDBCStore store, JDBCFetchConfiguration fetch)
        throws SQLException {
        if (fetch == null)
            fetch = store.getFetchConfiguration();
        return execute(store.getContext(), store, fetch,
            fetch.getReadLockLevel());
    }

    public Result execute(JDBCStore store, JDBCFetchConfiguration fetch,
        int lockLevel)
        throws SQLException {
        if (fetch == null)
            fetch = store.getFetchConfiguration();
        return execute(store.getContext(), store, fetch, lockLevel);
    }

    /**
     * Execute this select in the context of the given store manager. The
     * context is passed in separately for profiling purposes.
     */
    protected Result execute(StoreContext ctx, JDBCStore store, 
        JDBCFetchConfiguration fetch, int lockLevel)
        throws SQLException {
        boolean forUpdate = false;
        if (!isAggregate() && _grouping == null) {
            JDBCLockManager lm = store.getLockManager();
            if (lm != null)
                forUpdate = lm.selectForUpdate(this, lockLevel);
        }

        logEagerRelations();
        SQLBuffer sql = toSelect(forUpdate, fetch);
        boolean isLRS = isLRS();
        int rsType = (isLRS && supportsRandomAccess(forUpdate))
            ? -1 : ResultSet.TYPE_FORWARD_ONLY;
        Connection conn = store.getConnection();
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        try {
            if (isLRS) 
                stmnt = prepareStatement(conn, sql, fetch, rsType, -1, true); 
            else
                stmnt = prepareStatement(conn, sql, null, rsType, -1, false);
            
            _dict.setTimeouts(stmnt, fetch, forUpdate);
            
            rs = executeQuery(conn, stmnt, sql, isLRS, store);
        } catch (SQLException se) {
            // clean up statement
            if (stmnt != null)
                try { stmnt.close(); } catch (SQLException se2) {}
            try { conn.close(); } catch (SQLException se2) {}
            throw se;
        }
        return getEagerResult(conn, stmnt, rs, store, fetch, forUpdate, sql);
    }

    /**
     * Execute our eager selects, adding the results under the same keys
     * to the given result.
     */
    private static void addEagerResults(SelectResult res, SelectImpl sel,
        JDBCStore store, JDBCFetchConfiguration fetch)
        throws SQLException {
        if (sel._eager == null)
            return;

        // execute eager selects
        Map.Entry entry;
        Result eres;
        Map eager;
        for (Iterator itr = sel._eager.entrySet().iterator(); itr.hasNext();) {
            entry = (Map.Entry) itr.next();

            // simulated batched selects for inner/outer joins; for separate
            // selects, don't pass on lock level, because they're probably
            // for relations and therefore should use default level
            if (entry.getValue() == sel)
                eres = res;
            else
                eres = ((SelectExecutor) entry.getValue()).execute(store,
                    fetch);

            eager = res.getEagerMap(false);
            if (eager == null) {
                eager = new HashMap();
                res.setEagerMap(eager);
            }
            eager.put(entry.getKey(), eres);
        }
    }


    /**
     * This method is to provide override for non-JDBC or JDBC-like 
     * implementation of preparing statement.
     */
    protected PreparedStatement prepareStatement(Connection conn, 
        SQLBuffer sql, JDBCFetchConfiguration fetch, int rsType, 
        int rsConcur, boolean isLRS) throws SQLException {
        if (fetch == null)
            return sql.prepareStatement(conn, rsType, rsConcur);
        else
            return sql.prepareStatement(conn, fetch, rsType, -1);
    }
    
    /**
     * This method is to provide override for non-JDBC or JDBC-like 
     * implementation of preparing statement.
     */
    public PreparedStatement prepareStatement(Connection conn, 
        String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }    
    
    /**
     * This method is to provide override for non-JDBC or JDBC-like 
     * implementation of executing query.
     */
    protected ResultSet executeQuery(Connection conn, PreparedStatement stmnt, 
        SQLBuffer sql, boolean isLRS, JDBCStore store) throws SQLException {
        return stmnt.executeQuery();
    }
    
    /**
     * This method is to provide override for non-JDBC or JDBC-like 
     * implementation of executing query.
     */
    public ResultSet executeQuery(Connection conn, PreparedStatement stmnt, 
        String sql, JDBCStore store, Object[] params, Column[] cols) 
        throws SQLException {
        return stmnt.executeQuery();
    }

    /**
     * This method is to provide override for non-JDBC or JDBC-like 
     * implementation of getting count from the result set.
     */
    protected int getCount(ResultSet rs) throws SQLException {
        rs.next();
        return rs.getInt(1);
    }
    
    /**
     * This method is to provide override for non-JDBC or JDBC-like 
     * implementation of executing eager selects.
     */
    public Result getEagerResult(Connection conn, 
        PreparedStatement stmnt, ResultSet rs, JDBCStore store, 
        JDBCFetchConfiguration fetch, boolean forUpdate, SQLBuffer sql) 
        throws SQLException {
        SelectResult res = new SelectResult(conn, stmnt, rs, _dict);
        res.setSelect(this);
        res.setStore(store);
        res.setLocking(forUpdate);
        try {
            addEagerResults(res, this, store, fetch);
        } catch (SQLException se) {
            res.close();
            throw se;
        }
        return res;
    }

    /////////////////////////
    // Select implementation
    /////////////////////////

    public int indexOf() {
        return 0;
    }

    public List getSubselects() {
        return (_subsels == null) ? Collections.EMPTY_LIST : _subsels;
    }

    public Select getParent() {
        return _parent;
    }

    public String getSubselectPath() {
        return _subPath;
    }

    public void setParent(Select parent, String path) {
        if (path != null)
            _subPath = path;
        else
            _subPath = null;

        if (parent == _parent)
            return;
        if (_parent != null)
            _parent._subsels.remove(this);

        //### right now we can't use sql92 joins with subselects, cause
        //### I can't figure out what to do when the subselect has a join
        //### with an alias also present in the outer select... you don't want
        //### the join to appear in the FROM clause of the subselect cause
        //### then it re-aliases both tables in the scope of the subselect
        //### and the correlation with the outer select is lost
        _parent = (SelectImpl) parent;
        if (_parent != null) {
            if (_parent._subsels == null)
                _parent._subsels = new ArrayList(2);
            _parent._subsels.add(this);
            if (_parent._joinSyntax == JoinSyntaxes.SYNTAX_SQL92)
                _joinSyntax = JoinSyntaxes.SYNTAX_TRADITIONAL;
            else
                _joinSyntax = _parent._joinSyntax;
        }
    }
    
    public void setHasSubselect(boolean hasSub) {
        _hasSub = hasSub;
    }
    
    public boolean getHasSubselect() {
        return _hasSub;    
    }
    
    public Map getAliases() {
        return _aliases;
    }
    
    public void removeAlias(Object key) {
        _aliases.remove(key);
    }
    
    public Map getTables() {
        return _tables;
    }
    
    public void removeTable(Object key) {
        _tables.remove(key);
    }

    public Select getFromSelect() {
        return _from;
    }

    public void setFromSelect(Select sel) {
        _from = (SelectImpl) sel;
        if (_from != null)
            _from._outer = this;
    }

    public boolean hasEagerJoin(boolean toMany) {
        if (toMany)
            return (_flags & EAGER_TO_MANY) != 0;
        return (_flags & EAGER_TO_ONE) != 0;
    }

    public boolean hasJoin(boolean toMany) {
        if (toMany)
            return (_flags & TO_MANY) != 0;
        return _tables != null && _tables.size() > 1;
    }

    public boolean isSelected(Table table) {
        PathJoins pj = getJoins(null, false);
        if (_from != null)
            return _from.getTableIndex(table, pj, false) != -1;
        return getTableIndex(table, pj, false) != -1;
    }

    public Collection getTableAliases() {
        return (_tables == null) ? Collections.EMPTY_SET : _tables.values();
    }

    public List getSelects() {
        return Collections.unmodifiableList(_selects);
    }

    public List getSelectAliases() {
        return _selects.getAliases(false, _outer != null);
    }

    public List getIdentifierAliases() {
        return _selects.getAliases(true, _outer != null);
    }

    public SQLBuffer getOrdering() {
        return _ordering;
    }

    public SQLBuffer getGrouping() {
        return _grouping;
    }

    public SQLBuffer getWhere() {
        return _where;
    }

    public SQLBuffer getHaving() {
        return _having;
    }

    public void addJoinClassConditions() {
        if (_joins == null || _joins.joins() == null)
            return;

        // join set iterator allows concurrent modification
        Join j;
        for (Iterator itr = _joins.joins().iterator(); itr.hasNext();) {
            j = (Join) itr.next();
            if (j.getRelationTarget() != null) {
                j.getRelationTarget().getDiscriminator().addClassConditions
                    (this, j.getSubclasses() == SUBS_JOINABLE, 
                    j.getRelationJoins());
                j.setRelation(null, 0, null);
            }
        }
    }

    public Joins getJoins() {
        return _joins;
    }

    public Iterator getJoinIterator() {
        if (_joins == null || _joins.isEmpty())
            return EmptyIterator.INSTANCE;
        return _joins.joins().joinIterator();
    }

    public long getStartIndex() {
        return _startIdx;
    }

    public long getEndIndex() {
        return _endIdx;
    }

    public void setRange(long start, long end) {
        _startIdx = start;
        _endIdx = end;
    }

    public String getColumnAlias(Column col) {
        return getColumnAlias(col, (Joins) null);
    }

    public String getColumnAlias(Column col, Joins joins) {
        return getColumnAlias(col, getJoins(joins, false));
    }

    /**
     * Return the alias for the given column.
     */
    private String getColumnAlias(Column col, PathJoins pj) {
        return getColumnAlias(col.getIdentifier().getName(), col.getTable(), pj);
    }

    public String getColumnAlias(String col, Table table) {
        return getColumnAlias(col, table, (Joins) null);
    }

    public String getColumnAlias(String col, Table table, Joins joins) {
        return getColumnAlias(col, table, getJoins(joins, false));
    }

    /**
     * Return the alias for the give column
     */
    public String getColumnAlias(Column col, Object path) {
        Table table = col.getTable();
        String tableAlias = null;
        Iterator itr = getJoinIterator();
        while (itr.hasNext()) {
            Join join = (Join) itr.next();
            if (join != null) {
                if (join.getTable1() == table)
                    tableAlias = join.getAlias1();
                else if (join.getTable2() == table)
                    tableAlias = join.getAlias2();
                if (tableAlias != null)
                    return new StringBuilder(tableAlias).append(".").
                        append(_dict.getNamingUtil().toDBName(col.getIdentifier())).toString();
            }
        }
        throw new InternalException("Can not resolve alias for field: " +
            path.toString() + " mapped to column: " + col.getIdentifier().getName() +
            " table: "+table.getIdentifier().getName());
    }

    /**
     * Return the alias for the given column.
     */
    private String getColumnAlias(String col, Table table, PathJoins pj) {
        return getTableAlias(table, pj).append(_dict.getNamingUtil().toDBName(col)).toString();
    }
    
    private StringBuilder getTableAlias(Table table, PathJoins pj) {
        StringBuilder buf = new StringBuilder();
        if (_from != null) {
            String alias = toAlias(_from.getTableIndex(table, pj, true));
            if (_dict.requiresAliasForSubselect)
                return buf.append(FROM_SELECT_ALIAS).append(".").append(alias).
                    append("_");
            return buf.append(alias).append("_");
        }
        return buf.append(toAlias(getTableIndex(table, pj, true))).append(".");
    }

    public boolean isAggregate() {
        return (_flags & AGGREGATE) != 0;
    }

    public void setAggregate(boolean agg) {
        if (agg)
            _flags |= AGGREGATE;
        else
            _flags &= ~AGGREGATE;
    }

    public boolean isLob() {
        return (_flags & LOB) != 0;
    }

    public void setLob(boolean lob) {
        if (lob)
            _flags |= LOB;
        else
            _flags &= ~LOB;
    }

    public void clearSelects() {
        _selects.clear();
    }

    public boolean select(SQLBuffer sql, Object id) {
        return select(sql, id, null);
    }

    public boolean select(SQLBuffer sql, Object id, Joins joins) {
        if (!isGrouping())
            return select((Object) sql, id, joins);
        groupBy(sql, joins);
        return false;
    }

    /**
     * Record the select of the given SQL buffer or string.
     */
    private boolean select(Object sql, Object id, Joins joins) {
        getJoins(joins, true);
        boolean contains;
        if (id == null) {
            int idx = _selects.indexOfAlias(sql);
            contains = idx != -1;
            if (contains)
                id = _selects.get(idx);
            else
                id = nullId();
        } else
            contains = _selects.contains(id);

        if (contains)
            return false;
        _selects.setAlias(id, sql, false);
        return true;
    }

    /**
     * Returns a unique id for a SQL string whose given id is null.
     */
    private Object nullId() {
        if (_nullIds >= NULL_IDS.length)
            return new NullId();
        return NULL_IDS[_nullIds++];
    }

    public boolean select(String sql, Object id) {
        return select(sql, id, null);
    }

    public boolean select(String sql, Object id, Joins joins) {
        if (!isGrouping())
            return select((Object) sql, id, joins);
        groupBy(sql, joins);
        return true;
    }

    public void selectPlaceholder(String sql) {
        Object holder = (_placeholders >= PLACEHOLDERS.length)
            ? new Placeholder() : PLACEHOLDERS[_placeholders++];
        select(sql, holder);
    }

    /**
     * Insert a placeholder at the given index; use a negative index
     * to count from the back of the select list.
     */
    public void insertPlaceholder(String sql, int pos) {
        Object holder = (_placeholders >= PLACEHOLDERS.length)
            ? new Placeholder() : PLACEHOLDERS[_placeholders++];
        _selects.insertAlias(pos, holder, sql);
    }

    /**
     * Clear selected placeholders, and return removed select indexes.
     */
    public void clearPlaceholderSelects() {
        _selects.clearPlaceholders();
    }

    public boolean select(Column col) {
        return select(col, (Joins) null);
    }

    public boolean select(Column col, Joins joins) {
        if (!isGrouping())
            return select(col, getJoins(joins, true), false);
        groupBy(col, joins);
        return false;
    }

    public int select(Column[] cols) {
        return select(cols, null);
    }

    public int select(Column[] cols, Joins joins) {
        if (cols == null || cols.length == 0)
            return 0;
        if (isGrouping()) {
            groupBy(cols, joins);
            return 0;
        }
        PathJoins pj = getJoins(joins, true);
        int seld = 0;
        for (int i = 0; i < cols.length; i++)
            if (select(cols[i], pj, false))
                seld |= 2 << i;
        return seld;
    }

    /**
     * Select the given column after making the given joins.
     */
    private boolean select(Column col, PathJoins pj, boolean ident) {
        // we cache on column object if there are no joins so that when
        // looking up columns in the result we don't have to create a string
        // buffer for the table + column alias; if there are joins, then
        // we key on the alias
        String alias = getColumnAlias(col, pj);
        Object id;
        if (pj == null || pj.path() == null)
            id = col;
        else
            id = alias;
        if (_selects.contains(id))
            return false;

        if (col.getType() == Types.BLOB || col.getType() == Types.CLOB)
            setLob(true);
        _selects.setAlias(id, alias, ident);
        return true;
    }

    public void select(ClassMapping mapping, int subclasses,
        JDBCStore store, JDBCFetchConfiguration fetch, int eager) {
        select(mapping, subclasses, store, fetch, eager, null);
    }

    public void select(ClassMapping mapping, int subclasses,
        JDBCStore store, JDBCFetchConfiguration fetch, int eager,
        Joins joins) {
        select(this, mapping, subclasses, store, fetch, eager, joins, false);
    }

    /**
     * Select the given mapping.
     */
    void select(Select wrapper, ClassMapping mapping, int subclasses,
        JDBCStore store, JDBCFetchConfiguration fetch, int eager,
        Joins joins, boolean ident) {
        // note that this is one case where we don't want to use the result
        // of getJoins(); just use the given joins, which will either be clean
        // or the result of previous pre-joins. this way we don't push extra
        // stack stuff when no actual new joins have been made, and we don't
        // think the user wants outer joins when actually only the previous
        // joins were outer.  we do invoke getJoins(), though, to add these
        // joins (if any) to our top-level joins; otherwise it'd be possible
        // for the user to immediately do another join and select something,
        // and if we're in outer mode all these joins will get switched to outer
        // joins.  caching them as their original join type prevents that
        getJoins(joins, true);

        PathJoins pj = (PathJoins) joins;
        boolean hasJoins = pj != null && pj.isDirty();
        if (hasJoins) {
            if (_preJoins == null)
                _preJoins = new Stack();
            _preJoins.push(pj);
        }

        // if they are selecting this mapping with outer joins, then all joins
        // from this mapping should also be outer
        boolean wasOuter = (_flags & OUTER) != 0;
        if (hasJoins && !wasOuter && pj.isOuter())
            _flags |= OUTER;

        // delegate to store manager to select in same order it loads result
        ((JDBCStoreManager) store).select(wrapper, mapping, subclasses, null,
            null, fetch, eager, ident, (_flags & OUTER) != 0);

        // reset
        if (hasJoins)
            _preJoins.pop();
        if (!wasOuter && (_flags & OUTER) != 0)
            _flags &= ~OUTER;
    }

    public boolean selectIdentifier(Column col) {
        return selectIdentifier(col, (Joins) null);
    }

    public boolean selectIdentifier(Column col, Joins joins) {
        if (!isGrouping())
            return select(col, getJoins(joins, true), true);
        groupBy(col, joins);
        return false;
    }

    public int selectIdentifier(Column[] cols) {
        return selectIdentifier(cols, null);
    }

    public int selectIdentifier(Column[] cols, Joins joins) {
        if (cols == null || cols.length == 0)
            return 0;
        if (isGrouping()) {
            groupBy(cols, joins);
            return 0;
        }
        PathJoins pj = getJoins(joins, true);
        int seld = 0;
        for (int i = 0; i < cols.length; i++)
            if (select(cols[i], pj, true))
                seld |= 2 << i;
        return seld;
    }

    public void selectIdentifier(ClassMapping mapping, int subclasses,
        JDBCStore store, JDBCFetchConfiguration fetch, int eager) {
        selectIdentifier(mapping, subclasses, store, fetch, eager, null);
    }

    public void selectIdentifier(ClassMapping mapping, int subclasses,
        JDBCStore store, JDBCFetchConfiguration fetch, int eager,
        Joins joins) {
        select(this, mapping, subclasses, store, fetch, eager, joins, true);
    }

    public int selectPrimaryKey(ClassMapping mapping) {
        return selectPrimaryKey(mapping, null);
    }

    public int selectPrimaryKey(ClassMapping mapping, Joins joins) {
        return primaryKeyOperation(mapping, true, null, joins, false);
    }

    /**
     * Operate on primary key data. Return a bit mask of selected columns.
     */
    private int primaryKeyOperation(ClassMapping mapping, boolean sel,
        Boolean asc, Joins joins, boolean aliasOrder) {
        if (!sel && asc == null)
            return 0;

        // if this mapping can't select the full pk values, then join to
        // super and recurse
        ClassMapping sup;
        if (!mapping.isPrimaryKeyObjectId(true)) {
            sup = mapping.getJoinablePCSuperclassMapping();
            if (joins == null)
                joins = newJoins();
            joins = mapping.joinSuperclass(joins, false);
            return primaryKeyOperation(sup, sel, asc, joins, aliasOrder);
        }

        Column[] cols = mapping.getPrimaryKeyColumns();
        if (isGrouping()) {
            groupBy(cols, joins);
            return 0;
        }

        PathJoins pj = getJoins(joins, false);
        int seld = 0;
        for (int i = 0; i < cols.length; i++)
            if (columnOperation(cols[i], sel, asc, pj, aliasOrder))
                seld |= 2 << i;

        // if this mapping has not been used in the select yet (and therefore
        // is not joined to anything), but has an other-table superclass that
        // has been used, make sure to join to it
        boolean joined = false;
        for (sup = mapping.getJoinablePCSuperclassMapping(); sup != null;
            mapping = sup, sup = mapping.getJoinablePCSuperclassMapping()) {
            if (sup.getTable() == mapping.getTable())
                continue;

            if (mapping.getTable() != sup.getTable()
                && getTableIndex(mapping.getTable(), pj, false) == -1
                && getTableIndex(sup.getTable(), pj, false) != -1) {
                if (pj == null)
                    pj = (PathJoins) newJoins();
                pj = (PathJoins) mapping.joinSuperclass(pj, false);
                joined = true;
            } else
                break;
        }
        if (joined)
            where(pj);

        return seld;
    }

    /**
     * Perform an operation on a column.
     */
    private boolean columnOperation(Column col, boolean sel, Boolean asc,
        PathJoins pj, boolean aliasOrder) {
        String as = null;
        if (asc != null && (aliasOrder || (_flags & RECORD_ORDERED) != 0)) {
            Object id;
            if (pj == null || pj.path() == null)
                id = col;
            else
                id = getColumnAlias(col, pj);
            if ((_flags & RECORD_ORDERED) != 0) {
                if (_ordered == null)
                    _ordered = new ArrayList(5);
                _ordered.add(id);
            }
            if (aliasOrder) {
                as = toOrderAlias(_orders++);
                _selects.setSelectAs(id, as);
            }
        }

        boolean seld = sel && select(col, pj, false);
        if (asc != null) {
            String alias = (as != null) ? as : getColumnAlias(col, pj);
            appendOrdering(alias, asc.booleanValue());
        }
        return seld;
    }

    /**
     * Append ordering information to our internal buffer.
     */
    private void appendOrdering(Object orderBy, boolean asc) {
        if (_ordering == null)
            _ordering = new SQLBuffer(_dict);
        else
            _ordering.append(", ");

        if (orderBy instanceof SQLBuffer)
            _ordering.append((SQLBuffer) orderBy);
        else
            _ordering.append((String) orderBy);
        if (asc)
            _ordering.append(" ASC");
        else
            _ordering.append(" DESC");
    }

    public int orderByPrimaryKey(ClassMapping mapping, boolean asc,
        boolean sel) {
        return orderByPrimaryKey(mapping, asc, null, sel);
    }

    public int orderByPrimaryKey(ClassMapping mapping, boolean asc,
        Joins joins, boolean sel) {
        return orderByPrimaryKey(mapping, asc, joins, sel, false);
    }

    /**
     * Allow unions to set aliases on order columns.
     */
    public int orderByPrimaryKey(ClassMapping mapping, boolean asc,
        Joins joins, boolean sel, boolean aliasOrder) {
        return primaryKeyOperation(mapping, sel,
            (asc) ? Boolean.TRUE : Boolean.FALSE, joins, aliasOrder);
    }

    public boolean orderBy(Column col, boolean asc, boolean sel) {
        return orderBy(col, asc, null, sel);
    }

    public boolean orderBy(Column col, boolean asc, Joins joins, boolean sel) {
        return orderBy(col, asc, joins, sel, false);
    }

    /**
     * Allow unions to set aliases on order columns.
     */
    boolean orderBy(Column col, boolean asc, Joins joins, boolean sel,
        boolean aliasOrder) {
        return columnOperation(col, sel, (asc) ? Boolean.TRUE : Boolean.FALSE,
            getJoins(joins, true), aliasOrder);
    }

    public int orderBy(Column[] cols, boolean asc, boolean sel) {
        return orderBy(cols, asc, null, sel);
    }

    public int orderBy(Column[] cols, boolean asc, Joins joins, boolean sel) {
        return orderBy(cols, asc, joins, sel, false);
    }

    /**
     * Allow unions to set aliases on order columns.
     */
    int orderBy(Column[] cols, boolean asc, Joins joins, boolean sel,
        boolean aliasOrder) {
        PathJoins pj = getJoins(joins, true);
        int seld = 0;
        for (int i = 0; i < cols.length; i++)
            if (columnOperation(cols[i], sel,
                (asc) ? Boolean.TRUE : Boolean.FALSE, pj, aliasOrder))
                seld |= 2 << i;
        return seld;
    }

    public boolean orderBy(SQLBuffer sql, boolean asc, boolean sel, Value selAs)
    {
        return orderBy(sql, asc, (Joins) null, sel, selAs);
    }

    public boolean orderBy(SQLBuffer sql, boolean asc, Joins joins,
        boolean sel, Value selAs) {
        return orderBy(sql, asc, joins, sel, false, selAs);
    }

    /**
     * Allow unions to set aliases on order columns.
     */
    boolean orderBy(SQLBuffer sql, boolean asc, Joins joins, boolean sel,
        boolean aliasOrder, Value selAs) {
        return orderBy((Object) sql, asc, joins, sel, aliasOrder, selAs);
    }

    /**
     * Order on a SQL buffer or string.
     */
    private boolean orderBy(Object sql, boolean asc, Joins joins, boolean sel,
        boolean aliasOrder, Value selAs) {
        Object order = sql;
        if (aliasOrder) {
            order = toOrderAlias(_orders++);
            _selects.setSelectAs(sql, (String) order);
        }
        if ((_flags & RECORD_ORDERED) != 0) {
            if (_ordered == null)
                _ordered = new ArrayList(5);
            _ordered.add(selAs == null ? sql : selAs);
        }

        getJoins(joins, true);
        appendOrdering(selAs != null ? selAs.getAlias() : order, asc);
        if (sel) {
            int idx = _selects.indexOfAlias(sql);
            if (idx == -1) {
                _selects.setAlias(nullId(), sql, false);
                return true;
            }
        }
        return false;
    }

    public boolean orderBy(String sql, boolean asc, boolean sel) {
        return orderBy(sql, asc, null, sel);
    }

    public boolean orderBy(String sql, boolean asc, Joins joins, boolean sel) {
        return orderBy(sql, asc, joins, sel, false);
    }

    /**
     * Allow unions to set aliases on order columns.
     */
    boolean orderBy(String sql, boolean asc, Joins joins, boolean sel,
        boolean aliasOrder) {
        return orderBy((Object) sql, asc, joins, sel, aliasOrder, null);
    }

    public void clearOrdering() {
        _ordering = null;
        _orders = 0;
    }

    /**
     * Allow unions to record the select list indexes of items we order by.
     */
    void setRecordOrderedIndexes(boolean record) {
        if (record)
            _flags |= RECORD_ORDERED;
        else {
            _ordered = null;
            _flags &= ~RECORD_ORDERED;
        }
    }

    /**
     * Return the indexes in the select list of all items we're ordering
     * by, or null if none. For use with unions.
     */
    List getOrderedIndexes() {
        if (_ordered == null)
            return null;
        List idxs = new ArrayList(_ordered.size());
        for (int i = 0; i < _ordered.size(); i++)
            idxs.add(_selects.indexOf(_ordered.get(i)));
        return idxs;
    }

    public void wherePrimaryKey(Object oid, ClassMapping mapping,
        JDBCStore store) {
        wherePrimaryKey(oid, mapping, null, store);
    }

    /**
     * Add where conditions setting the mapping's primary key to the given
     * oid values. If the given mapping does not use oid values for its
     * primary key, we will recursively join to its superclass until we find
     * an ancestor that does.
     */
    private void wherePrimaryKey(Object oid, ClassMapping mapping, Joins joins,
        JDBCStore store) {
        // if this mapping's identifiers include something other than
        // the pk values, join to super and recurse
        if (!mapping.isPrimaryKeyObjectId(false)) {
            ClassMapping sup = mapping.getJoinablePCSuperclassMapping();
            if (joins == null)
                joins = newJoins();
            joins = mapping.joinSuperclass(joins, false);
            wherePrimaryKey(oid, sup, joins, store);
            return;
        }

        Column[] cols = mapping.getPrimaryKeyColumns();
        where(oid, mapping, cols, cols, null, null, getJoins(joins, true),
            store);
    }

    public void whereForeignKey(ForeignKey fk, Object oid,
        ClassMapping mapping, JDBCStore store) {
        whereForeignKey(fk, oid, mapping, null, store);
    }

    /**
     * Add where conditions setting the given foreign key to the given
     * oid values.
     *
     * @see #wherePrimaryKey
     */
    private void whereForeignKey(ForeignKey fk, Object oid,
        ClassMapping mapping, Joins joins, JDBCStore store) {
        // if this mapping's identifiers include something other than
        // the pk values, or if this foreign key doesn't link to only
        // identifiers, join to table and do a getPrimaryKey
        if (!mapping.isPrimaryKeyObjectId(false) || !containsAll
            (mapping.getPrimaryKeyColumns(), fk.getPrimaryKeyColumns())) {
            if (joins == null)
                joins = newJoins();
            // traverse to foreign key target mapping
            while (mapping.getTable() != fk.getPrimaryKeyTable()) {
                if (joins == null)
                    joins = newJoins();
                joins = mapping.joinSuperclass(joins, false);
                mapping = mapping.getJoinablePCSuperclassMapping();
                if (mapping == null)
                    throw new InternalException();
            }
            joins = joins.join(fk, false, false);
            wherePrimaryKey(oid, mapping, joins, store);
            return;
        }

        Column[] fromCols = fk.getColumns();
        Column[] toCols = fk.getPrimaryKeyColumns();
        Column[] constCols = fk.getConstantColumns();
        Object[] consts = fk.getConstants();
        where(oid, mapping, toCols, fromCols, consts, constCols,
            getJoins(joins, true), store);
    }

    /**
     * Internal method to flush the oid values as where conditions to the
     * given columns.
     */
    private void where(Object oid, ClassMapping mapping, Column[] toCols,
        Column[] fromCols, Object[] vals, Column[] constCols, PathJoins pj,
        JDBCStore store) {
        ValueMapping embed = mapping.getEmbeddingMapping();
        if (embed != null) {
            where(oid, embed.getFieldMapping().getDefiningMapping(),
                toCols, fromCols, vals, constCols, pj, store);
            return;
        }

        // only bother to pack pk values into array if app id
        Object[] pks = null;
        boolean relationId = RelationStrategies.isRelationId(fromCols); 
        if (!relationId && mapping.getIdentityType() == ClassMapping.ID_APPLICATION)
            pks = ApplicationIds.toPKValues(oid, mapping);

        SQLBuffer buf = new SQLBuffer(_dict);
        Joinable join;
        Object val;
        int count = 0;
        for (int i = 0; i < toCols.length; i++, count++) {
            if (pks == null) {
                val = (oid == null) ? null : relationId ? oid : ((Id) oid).getId();
            } else {
                // must be app identity; use pk index to get correct pk value
                join = mapping.assertJoinable(toCols[i]);
                val = pks[mapping.getField(join.getFieldIndex()).
                    getPrimaryKeyIndex()];
                val = join.getJoinValue(val, toCols[i], store);
            }

            if (count > 0)
                buf.append(" AND ");
            buf.append(getColumnAlias(fromCols[i], pj));
            if (val == null)
                buf.append(" IS ");
            else
                buf.append(" = ");
            buf.appendValue(val, fromCols[i]);
        }

        if (constCols != null && constCols.length > 0) {
            for (int i = 0; i < constCols.length; i++, count++) {
                if (count > 0)
                    buf.append(" AND ");
                buf.append(getColumnAlias(constCols[i], pj));

                if (vals[i] == null)
                    buf.append(" IS ");
                else
                    buf.append(" = ");
                buf.appendValue(vals[i], constCols[i]);
            }
        }

        where(buf, pj);
    }

    /**
     * Test to see if the given set of columns contains all the
     * columns in the given potential subset.
     */
    private static boolean containsAll(Column[] set, Column[] sub) {
        if (sub.length > set.length)
            return false;

        // this is obviously n^2, but the number of columns should be in
        // the 1-2 range, so no biggie
        boolean found = true;
        for (int i = 0; i < sub.length && found; i++) {
            found = false;
            for (int j = 0; j < set.length && !found; j++)
                found = sub[i] == set[j];
        }
        return found;
    }

    public void where(Joins joins) {
        if (joins != null)
            where((String) null, joins);
    }

    public void where(SQLBuffer sql) {
        where(sql, (Joins) null);
    }

    public void where(SQLBuffer sql, Joins joins) {
        where(sql, getJoins(joins, true));
    }

    /**
     * Add the given condition to the WHERE clause.
     */
    private void where(SQLBuffer sql, PathJoins pj) {
        // no need to use joins...
        if (sql == null || sql.isEmpty())
            return;

        if (_where == null)
            _where = new SQLBuffer(_dict);
        else if (!_where.isEmpty())
            _where.append(" AND ");
        _where.append(sql);
    }

    public void where(String sql) {
        where(sql, (Joins) null);
    }

    public void where(String sql, Joins joins) {
        where(sql, getJoins(joins, true));
    }

    /**
     * Add the given condition to the WHERE clause.
     */
    private void where(String sql, PathJoins pj) {
        // no need to use joins...
        if (StringUtils.isEmpty(sql))
            return;

        if (_where == null)
            _where = new SQLBuffer(_dict);
        else if (!_where.isEmpty())
            _where.append(" AND ");
        _where.append(sql);
    }

    public void having(SQLBuffer sql) {
        having(sql, (Joins) null);
    }

    public void having(SQLBuffer sql, Joins joins) {
        having(sql, getJoins(joins, true));
    }

    /**
     * Add the given condition to the HAVING clause.
     */
    private void having(SQLBuffer sql, PathJoins pj) {
        // no need to use joins...
        if (sql == null || sql.isEmpty())
            return;

        if (_having == null)
            _having = new SQLBuffer(_dict);
        else if (!_having.isEmpty())
            _having.append(" AND ");
        _having.append(sql);
    }

    public void having(String sql) {
        having(sql, (Joins) null);
    }

    public void having(String sql, Joins joins) {
        having(sql, getJoins(joins, true));
    }

    /**
     * Add the given condition to the HAVING clause.
     */
    private void having(String sql, PathJoins pj) {
        // no need to use joins...
        if (StringUtils.isEmpty(sql))
            return;

        if (_having == null)
            _having = new SQLBuffer(_dict);
        else if (!_having.isEmpty())
            _having.append(" AND ");
        _having.append(sql);
    }

    public void groupBy(SQLBuffer sql) {
        groupBy(sql, (Joins) null);
    }

    public void groupBy(SQLBuffer sql, Joins joins) {
        getJoins(joins, true);
        groupByAppend(sql.getSQL());
    }

    public void groupBy(String sql) {
        groupBy(sql, (Joins) null);
    }

    public void groupBy(String sql, Joins joins) {
        getJoins(joins, true);
        groupByAppend(sql);
    }

    public void groupBy(Column col) {
        groupBy(col, null);
    }

    public void groupBy(Column col, Joins joins) {
        PathJoins pj = getJoins(joins, true);
        groupByAppend(getColumnAlias(col, pj));
    }

    public void groupBy(Column[] cols) {
        groupBy(cols, null);
    }

    public void groupBy(Column[] cols, Joins joins) {
        PathJoins pj = getJoins(joins, true);
        for (int i = 0; i < cols.length; i++) {
            groupByAppend(getColumnAlias(cols[i], pj));
        }
    }
    
    private void groupByAppend(String sql) {
        if (_grouped == null || !_grouped.contains(sql)) {
            if (_grouping == null) {
                _grouping = new SQLBuffer(_dict);
                _grouped = new ArrayList();
            } else
                _grouping.append(", ");

            _grouping.append(sql);
            _grouped.add(sql);
        }
    }

    public void groupBy(ClassMapping mapping, int subclasses, JDBCStore store,
        JDBCFetchConfiguration fetch) {
        groupBy(mapping, subclasses, store, fetch, null);
    }

    public void groupBy(ClassMapping mapping, int subclasses, JDBCStore store,
        JDBCFetchConfiguration fetch, Joins joins) {
        // we implement this by putting ourselves into grouping mode, where
        // all select invocations are re-routed to group-by invocations instead.
        // this allows us to utilize the same select APIs of the store manager
        // and all the mapping strategies, rather than having to create 
        // equivalent APIs and duplicate logic for grouping
        boolean wasGrouping = isGrouping();
        _flags |= GROUPING;
        try {
            select(mapping, subclasses, store, fetch, 
                EagerFetchModes.EAGER_NONE, joins);
        } finally {
            if (!wasGrouping)
                _flags &= ~GROUPING;
        }
    }

    /**
     * Whether we're in group mode, where any select is changed to a group-by
     * call.
     */
    private boolean isGrouping() {
        return (_flags & GROUPING) != 0;
    }

    /**
     * Return the joins to use for column aliases, etc.
     *
     * @param joins joins given by the user
     * @return the joins to use for aliases, etc
     */
    private PathJoins getJoins(Joins joins, boolean record) {
        PathJoins pj = (PathJoins) joins;
        boolean pre = (pj == null || !pj.isDirty())
            && _preJoins != null && !_preJoins.isEmpty();
        if (pre)
            pj = (PathJoins) _preJoins.peek();

        if (pj == null || !pj.isDirty())
            pj = _joins;
        else if (!pre) {
            if ((_flags & OUTER) != 0)
                pj = (PathJoins) outer(pj);
            if (record) {
                if (!pj.isEmpty()) {
                    if (_joins == null)
                        _joins = new SelectJoins(this);
                    if (_joins.joins() == null)
                        _joins.setJoins(new JoinSet(pj.joins()));
                    else
                        _joins.joins().addAll(pj.joins());
                }
            }
        }
        return pj;
    }

    public SelectExecutor whereClone(int sels) {
        if (sels < 1)
            sels = 1;

        Select[] clones = null;
        SelectImpl sel;
        for (int i = 0; i < sels; i++) {
            sel = (SelectImpl) _conf.getSQLFactoryInstance().newSelect();
            sel._flags = _flags;
            sel._flags &= ~AGGREGATE;
            sel._flags &= ~OUTER;
            sel._flags &= ~LRS;
            sel._flags &= ~EAGER_TO_ONE;
            sel._flags &= ~EAGER_TO_MANY;
            sel._flags &= ~FORCE_COUNT;
            sel._joinSyntax = _joinSyntax;
            sel._schemaAlias = _schemaAlias;
            if (_aliases != null)
                sel._aliases = new HashMap(_aliases);
            if (_tables != null)
                sel._tables = new TreeMap(_tables);
            if (_joins != null)
                sel._joins = _joins.clone(sel);
            if (_where != null)
                sel._where = new SQLBuffer(_where);
            if (_from != null) {
                sel._from = (SelectImpl) _from.whereClone(1);
                sel._from._outer = sel;
            }
            if (_subsels != null) {
                sel._subsels = new ArrayList(_subsels.size());
                SelectImpl sub, selSub;
                for (int j = 0; j < _subsels.size(); j++) {
                    sub = (SelectImpl) _subsels.get(j);
                    selSub = (SelectImpl) sub.fullClone(1);
                    selSub._parent = sel;
                    selSub._subPath = sub._subPath;
                    sel._subsels.add(selSub);
                    if (sel._where != null)
                        sel._where.replace(sub, selSub);
                }
            }

            if (sels == 1)
                return sel;
            if (clones == null)
                clones = new Select[sels];
            clones[i] = sel;
        }
        return _conf.getSQLFactoryInstance().newUnion(clones);
    }

    public SelectExecutor fullClone(int sels) {
        if (sels < 1)
            sels = 1;

        Select[] clones = null;
        SelectImpl sel;
        for (int i = 0; i < sels; i++) {
            sel = (SelectImpl) whereClone(1);
            sel._flags = _flags;
            sel._expectedResultCount = _expectedResultCount;
            sel._selects.addAll(_selects);
            if (_ordering != null)
                sel._ordering = new SQLBuffer(_ordering);
            sel._orders = _orders;
            if (_grouping != null)
                sel._grouping = new SQLBuffer(_grouping);
            if (_having != null)
                sel._having = new SQLBuffer(_having);
            if (_from != null) {
                sel._from = (SelectImpl) _from.fullClone(1);
                sel._from._outer = sel;
            }

            if (sels == 1)
                return sel;
            if (clones == null)
                clones = new Select[sels];
            clones[i] = sel;
        }
        return _conf.getSQLFactoryInstance().newUnion(clones);
    }

    public SelectExecutor eagerClone(FieldMapping key, int eagerType,
        boolean toMany, int sels) {
        if (eagerType == EAGER_OUTER
            && _joinSyntax == JoinSyntaxes.SYNTAX_TRADITIONAL)
            return null;
        if (_eagerKeys != null && _eagerKeys.contains(key))
            return null;

        // global set of eager keys
        if (_eagerKeys == null)
            _eagerKeys = new HashSet();
        _eagerKeys.add(key);

        SelectExecutor sel;
        if (eagerType != EAGER_PARALLEL) {
            if (toMany)
                _flags |= EAGER_TO_MANY;
            else
                _flags |= EAGER_TO_ONE;
            sel = this;
        } else if (sels < 2)
            sel = parallelClone();
        else {
            Select[] clones = new Select[sels];
            for (int i = 0; i < clones.length; i++)
                clones[i] = parallelClone();
            sel = _conf.getSQLFactoryInstance().newUnion(clones);
        }

        if (_eager == null)
            _eager = new HashMap();
        _eager.put(toEagerKey(key, getJoins(null, false)), sel);
        return sel;
    }

    /**
     * Return a clone of this select for use in eager parallel selects.
     */
    private SelectImpl parallelClone() {
        SelectImpl sel = (SelectImpl) whereClone(1);
        sel._flags &= ~NONAUTO_DISTINCT;
        sel._eagerKeys = _eagerKeys;
        if (_preJoins != null && !_preJoins.isEmpty()) {
            sel._preJoins = new Stack();
            sel._preJoins.push(((SelectJoins) _preJoins.peek()).
                clone(sel));
        }
        return sel;
    }

    /**
     * Return view of eager selects. May be null.
     */
    public Map getEagerMap() {
        return _eager;
    }

    public void logEagerRelations() {
        if (_eagerKeys != null) {
            _conf.getLog(JDBCConfiguration.LOG_DIAG).trace(
                "Eager relations: "+_eagerKeys);
        }
    }

    public SelectExecutor getEager(FieldMapping key) {
        if (_eager == null || !_eagerKeys.contains(key))
            return null;
        return (SelectExecutor) _eager.get(toEagerKey(key, getJoins(null,
            false)));
    }

    /**
     * Return the eager key to use for the user-given key.
     */
    private static Object toEagerKey(FieldMapping key, PathJoins pj) {
        if (pj == null || pj.path() == null)
            return key;
        return new Key(pj.path().toString(), key);
    }

    public Joins newJoins() {
        if (_preJoins != null && !_preJoins.isEmpty()) {
            SelectJoins sj = (SelectJoins) _preJoins.peek();
            return sj.clone(this);
        }
        // return this for efficiency in case no joins end up being made
        return this;
    }

    public Joins newOuterJoins() {
        return ((PathJoins) newJoins()).setOuter(true);
    }

    public void append(SQLBuffer buf, Joins joins) {
        if (joins == null || joins.isEmpty())
            return;
        if (_joinSyntax == JoinSyntaxes.SYNTAX_SQL92)
            return;

        if (!buf.isEmpty())
            buf.append(" AND ");
        Join join = null;
        for (Iterator itr = ((PathJoins) joins).joins().joinIterator();
            itr.hasNext();) {
            join = (Join) itr.next();
            switch (_joinSyntax) {
                case JoinSyntaxes.SYNTAX_TRADITIONAL:
                    buf.append(_dict.toTraditionalJoin(join));
                    break;
                case JoinSyntaxes.SYNTAX_DATABASE:
                    buf.append(_dict.toNativeJoin(join));
                    break;
                default:
                    throw new InternalException();
            }

            if (itr.hasNext())
                buf.append(" AND ");
        }
    }

    public Joins and(Joins joins1, Joins joins2) {
        return and((PathJoins) joins1, (PathJoins) joins2, true);
    }

    public Select getSelect() {
        return null;
    }

    /**
     * Combine the given joins.
     */
    private SelectJoins and(PathJoins j1, PathJoins j2, boolean nullJoins) {
        if ((j1 == null || j1.isEmpty())
            && (j2 == null || j2.isEmpty()))
            return null;

        SelectJoins sj = new SelectJoins(this);
        if (j1 == null || j1.isEmpty()) {
            if (j2.getSelect() == this) {
                if (nullJoins)
                    sj.setJoins(j2.joins());
                else
                    sj.setJoins(new JoinSet(j2.joins()));
            }
        } else {
            JoinSet set = null;
            if (j1.getSelect() == this) {
                if (nullJoins)
                    set = j1.joins();
                else
                    set = new JoinSet(j1.joins());

                if (j2 != null && !j2.isEmpty()
                    && j2.getSelect() == this)
                    set.addAll(j2.joins());
                sj.setJoins(set);
            }
        }

        // null previous joins; all are combined into this one
        if (nullJoins && j1 != null)
            j1.nullJoins();
        if (nullJoins && j2 != null)
            j2.nullJoins();

        return sj;
    }

    public Joins or(Joins joins1, Joins joins2) {
        PathJoins j1 = (PathJoins) joins1;
        PathJoins j2 = (PathJoins) joins2;

        // if no common joins, return null; if one side of the or clause has
        // different joins than the other, then we need to use distinct
        boolean j1Empty = j1 == null || j1.isEmpty();
        boolean j2Empty = j2 == null || j2.isEmpty();
        if (j1Empty || j2Empty) {
            if (j1Empty && !j2Empty) {
                collectOuterJoins(j2);
                if (!j2.isEmpty())
                    _flags |= IMPLICIT_DISTINCT;
            } else if (j2Empty && !j1Empty) {
                collectOuterJoins(j1);
                if (!j1.isEmpty())
                    _flags |= IMPLICIT_DISTINCT;
            }
            return null;
        }

        // if all common joins, move all joins to returned instance
        SelectJoins sj = new SelectJoins(this);
        if (j1.joins().equals(j2.joins())) {
            sj.setJoins(j1.joins());
            j1.nullJoins();
            j2.nullJoins();
        } else {
            JoinSet commonJoins = new JoinSet(j1.joins());
            commonJoins.retainAll(j2.joins());
            if (!commonJoins.isEmpty()) {
                // put common joins in returned instance; remove them from
                // each given instance
                sj.setJoins(commonJoins);
                j1.joins().removeAll(commonJoins);
                j2.joins().removeAll(commonJoins);
            }
            collectOuterJoins(j1);
            collectOuterJoins(j2);

            // if one side of the or clause has different joins than the other,
            // then we need to use distinct
            if (!j1.isEmpty() || !j2.isEmpty())
                _flags |= IMPLICIT_DISTINCT;
        }
        return sj;
    }

    public Joins outer(Joins joins) {
        if (_joinSyntax == JoinSyntaxes.SYNTAX_TRADITIONAL || joins == null)
            return joins;

        // record that this is an outer join set, even if it's empty
        PathJoins pj = ((PathJoins) joins).setOuter(true);
        if (pj.isEmpty())
            return pj;

        Join join;
        Join rec;
        boolean hasJoins = _joins != null && _joins.joins() != null;
        for (Iterator itr = pj.joins().iterator(); itr.hasNext();) {
            join = (Join) itr.next();
            if (join.getType() == Join.TYPE_INNER) {
                if (!hasJoins)
                    join.setType(Join.TYPE_OUTER);
                else {
                    rec = _joins.joins().getRecordedJoin(join);
                    if (rec == null || rec.getType() == Join.TYPE_OUTER)
                        join.setType(Join.TYPE_OUTER);
                }
            }
        }
        return joins;
    }

    /**
     * Moves the joins from the given instance into our outer joins set.
     */
    private void collectOuterJoins(PathJoins pj) {
        if (_joinSyntax == JoinSyntaxes.SYNTAX_TRADITIONAL || pj == null
            || pj.isEmpty())
            return;

        if (_joins == null)
            _joins = new SelectJoins(this);

        boolean add = true;
        if (_joins.joins() == null) {
            _joins.setJoins(pj.joins());
            add = false;
        }

        Join join;
        for (Iterator itr = pj.joins().iterator(); itr.hasNext();) {
            join = (Join) itr.next();
            if (join.getType() == Join.TYPE_INNER) {
                if (join.getForeignKey() != null
                    && !_dict.canOuterJoin(_joinSyntax, join.getForeignKey())) {
                    Log log = _conf.getLog(JDBCConfiguration.LOG_JDBC);
                    if (log.isWarnEnabled())
                        log.warn(_loc.get("cant-outer-fk",
                            join.getForeignKey()));
                } else
                    join.setType(Join.TYPE_OUTER);
            }
            if (add)
                _joins.joins().add(join);
        }
        pj.nullJoins();
    }

    /**
     * Return the alias for the given table under the given joins.
     * NOTE: WE RELY ON THESE INDEXES BEING MONOTONICALLY INCREASING FROM 0
     */
    private int getTableIndex(Table table, PathJoins pj, boolean create) {
        // if we have a from select, then there are no table aliases
        if (_from != null)
            return -1;

        Integer i = null;
        Object key = table.getFullIdentifier().getName();
        if (pj != null && pj.path() != null)
            key = new Key(pj.getPathStr(), key);

        if (_ctx != null && (_parent != null || _subsels != null || _hasSub)) {
            i = findAliasForQuery(table, pj, key, create);
        }

        if (i != null)
            return i.intValue();

        // check out existing aliases
        i = findAlias(table, key);

        if (i != null)
            return i.intValue();
        if (!create)
            return -1;

        // not found; create alias
        i = aliasSize(false, null);
//        System.out.println("GetTableIndex\t"+
//                ((_parent != null) ? "Sub" :"") +
//                " created alias: "+
//                i.intValue()+ " "+ key);
        recordTableAlias(table, key, i);
        return i.intValue();
    }

    private Integer findAliasForQuery(Table table, PathJoins pj, Object key,
        boolean create) {
        Integer i = null;
        SelectImpl sel = this;
        String alias = _schemaAlias;
        if (isPathInThisContext(pj) || table.isAssociation())          
            alias = null;

        // find the context where this alias is defined
        Context ctx = (alias != null) ?
            _ctx.findContext(alias) : null;
        if (ctx != null)
            sel = (SelectImpl) ctx.getSelect();

        if (!create) 
            i = sel.findAlias(table, key);  // find in parent and in myself
        else
            i = sel.getAlias(table, key); // find in myself
        if (i != null)
            return i;
        
        if (create) { // create here
            i = sel.createAlias(table, key);
        } else if (ctx != null && ctx != ctx()) { // create in other select
            i = ((SelectImpl)ctx.getSelect()).createAlias(table, key);
        }

        return i;
    }

    private boolean isPathInThisContext(PathJoins pj) {
        // currCtx is set from Action, it is reset to null after the PCPath initialization
        Context currCtx = pj == null ? null : ((PathJoinsImpl)pj).context;
        
        // lastCtx is set to currCtx after the SelectJoins.join. pj.lastCtx and pj.path string are 
        // the last snapshot of pj. They will be used together for later table alias resolution in
        // the getColumnAlias(). 
        Context lastCtx = pj == null ? null : ((PathJoinsImpl)pj).lastContext;
        Context thisCtx = currCtx == null ? lastCtx : currCtx;
        String corrVar = pj == null ? null : pj.getCorrelatedVariable();
        
        return (pj != null && pj.path() != null && 
            (corrVar == null || (thisCtx != null && ctx() == thisCtx)));
    }
 
    private Integer getAlias(Table table, Object key) {
        Integer alias = null;
        if (_aliases != null)
            alias = (Integer) _aliases.get(key);
        return alias;
    }

    private int createAlias(Table table, Object key) {
        Integer i = ctx().nextAlias();
//        System.out.println("\t"+
//                ((_parent != null) ? "Sub" :"") +
//                "Query created alias: "+ 
//                i.intValue()+ " "+ key);
        recordTableAlias(table, key, i);
        return i.intValue();
    }

    private Integer findAlias(Table table, Object key) {
        Integer alias = null;
        if (_aliases != null) {
            alias = (Integer) _aliases.get(key);
            if (alias != null) {
                return alias;
            }
        }
        if (_parent != null) {
            alias = _parent.findAlias(table, key);
            if (alias != null) {
                return alias;
            }
        }
        return alias;
    }

    /**
     * Record the mapping of the given key to the given alias.
     */
    private void recordTableAlias(Table table, Object key, Integer alias) {
        if (_aliases == null)
            _aliases = new HashMap();
        _aliases.put(key, alias);

        String tableString = _dict.getFullName(table, false) + " "
            + toAlias(alias.intValue());
        if (_tables == null)
            _tables = new TreeMap();
        _tables.put(alias, tableString);
    }

    
    /**
     * Calculate total number of aliases.
     * 
     * From 1.2.x
     */
    private int aliasSize(boolean fromParent, SelectImpl fromSub) {
        int aliases = (fromParent || _parent == null) ? 0 : _parent.aliasSize(false, this);
        aliases += (_aliases == null) ? 0 : _aliases.size();
        if (_subsels != null) {
            for (SelectImpl sub : _subsels) {
                if (sub != fromSub)
                    aliases += sub.aliasSize(true, null);
            }
        }
        return aliases;
    }
    
    public String toString() {
        return toSelect(false, null).getSQL();
    }

    ////////////////////////////
    // PathJoins implementation
    ////////////////////////////

    public boolean isOuter() {
        return false;
    }

    public PathJoins setOuter(boolean outer) {
        return new SelectJoins(this).setOuter(true);
    }

    public boolean isDirty() {
        return false;
    }

    public StringBuilder path() {
        return null;
    }
    
    public String getPathStr() {
        return null;
    }

    public JoinSet joins() {
        return null;
    }

    public int joinCount() {
        return 0;
    }

    public void nullJoins() {
    }

    public boolean isEmpty() {
        return true;
    }

    public Joins crossJoin(Table localTable, Table foreignTable) {
        return new SelectJoins(this).crossJoin(localTable, foreignTable);
    }

    public Joins join(ForeignKey fk, boolean inverse, boolean toMany) {
        return new SelectJoins(this).join(fk, inverse, toMany);
    }

    public Joins outerJoin(ForeignKey fk, boolean inverse, boolean toMany) {
        return new SelectJoins(this).outerJoin(fk, inverse, toMany);
    }

    public Joins joinRelation(String name, ForeignKey fk, ClassMapping target,
        int subs, boolean inverse, boolean toMany) {
        return new SelectJoins(this).joinRelation(name, fk, target, subs, 
            inverse, toMany);
    }

    public Joins outerJoinRelation(String name, ForeignKey fk, 
        ClassMapping target, int subs, boolean inverse, boolean toMany) {
        return new SelectJoins(this).outerJoinRelation(name, fk, target, subs, 
            inverse, toMany);
    }

    public Joins setVariable(String var) {
        if (var == null)
            return this;
        return new SelectJoins(this).setVariable(var);
    }

    public Joins setSubselect(String alias) {
        if (alias == null)
            return this;
        return new SelectJoins(this).setSubselect(alias);
    }

    /**
     * Represents a SQL string selected with null id.
     */
    private static class NullId {
    }

    /**
     * Represents a placeholder SQL string.
     */
    private static class Placeholder {
    }
    
    public SelectImpl clone(Context ctx) {
        SelectImpl sel = (SelectImpl) _conf.getSQLFactoryInstance().newSelect();
        sel._ctx = ctx;
        if (_parent != null && _parent.ctx() != null)
            sel._parent = (SelectImpl)_parent.ctx().getSelect();
        sel._schemaAlias = _schemaAlias;
        sel._flags = _flags;
        return sel;
    }

    /**
     * Key type used for aliases.
     */
    private static class Key {

        private final String _path;
        private final Object _key;

        public Key(String path, Object key) {
            _path = path;
            _key = key;
        }

        public int hashCode() {
            return ((_path == null) ? 0  : _path.hashCode()) ^ ((_key == null) ? 0  : _key.hashCode());
        }

        public boolean equals(Object other) {
            if (other == null)
                return false;
            if (other == this)
                return true;
            if (other.getClass() != getClass())
                return false;
            Key k = (Key) other;
            if (k._key == null || k._path == null || _key == null || _path == null)
            	return false;
            return k._path.equals(_path) && k._key.equals(_key);
        }

        public String toString() {
            return _path + "|" + _key;
        }
        
        Object getKey() {
            return _key;
        }
    }

    /**
     * A {@link Result} implementation wrapped around this select.
     */
    public static class SelectResult
        extends ResultSetResult
        implements PathJoins {

        private SelectImpl _sel = null;
        private Map<Column, Object> cachedColumnAlias_ = null;

        // position in selected columns list where we expect the next load
        private int _pos = 0;
        private Stack _preJoins = null;

        /**
         * Constructor.
         */
        public SelectResult(Connection conn, Statement stmnt, ResultSet rs,
            DBDictionary dict) {
            super(conn, stmnt, rs, dict);
        }

        /**
         * Select for this result.
         */
        public SelectImpl getSelect() {
            return _sel;
        }

        /**
         * Select for this result.
         */
        public void setSelect(SelectImpl sel) {
            _sel = sel;
        }

        public Object getEager(FieldMapping key) {
            // don't bother creating key if we know we don't have any
            // eager results
            if (_sel._eager == null || !_sel._eagerKeys.contains(key))
                return null;
            Map map = SelectResult.this.getEagerMap(true);
            if (map == null)
                return null;
            return map.get(_sel.toEagerKey(key, getJoins(null)));
        }

        public void putEager(FieldMapping key, Object res) {
            Map map = SelectResult.this.getEagerMap(true);
            if (map == null) {
                map = new HashMap();
                setEagerMap(map);
            }
            map.put(_sel.toEagerKey(key, getJoins(null)), res);
        }

        public Object load(ClassMapping mapping, JDBCStore store,
            JDBCFetchConfiguration fetch, Joins joins)
            throws SQLException {
            boolean hasJoins = joins != null
                && ((PathJoins) joins).path() != null;
            if (hasJoins) {
                if (_preJoins == null)
                    _preJoins = new Stack();
                _preJoins.push(joins);
            }

            Object obj = super.load(mapping, store, fetch, joins);

            // reset
            if (hasJoins)
                _preJoins.pop();
            return obj;
        }

        public Joins newJoins() {
            PathJoins pre = getPreJoins();
            if (pre == null || pre.path() == null)
                return this;

            PathJoinsImpl pj = new PathJoinsImpl();
            pj.path = new StringBuilder(pre.path().toString());
            return pj;
        }

        protected boolean containsInternal(Object obj, Joins joins) {
            // we key directly on objs and join-less cols, or on the alias
            // for cols with joins
            PathJoins pj = getJoins(joins);
            if (pj != null && pj.path() != null) {
                Object columnAlias = getColumnAlias((Column) obj, pj);
                if (joins == null) {
                    if (cachedColumnAlias_ == null)
                        cachedColumnAlias_ = new HashMap<Column, Object>();
                    cachedColumnAlias_.put((Column) obj, columnAlias);
                }
                return columnAlias != null && _sel._selects.contains(columnAlias);
            }
            return obj != null && _sel._selects.contains(obj);
        }

        protected boolean containsAllInternal(Object[] objs, Joins joins)
            throws SQLException {
            PathJoins pj = getJoins(joins);
            Object obj;
            for (int i = 0; i < objs.length; i++) {
                if (pj != null && pj.path() != null)
                    obj = getColumnAlias((Column) objs[i], pj);
                else
                    obj = objs[i];
                if (obj == null || !_sel._selects.contains(obj))
                    return false;
            }
            return true;
        }

        public void pushBack()
            throws SQLException {
            _pos = 0;
            super.pushBack();
        }

        protected boolean absoluteInternal(int row)
            throws SQLException {
            _pos = 0;
            return super.absoluteInternal(row);
        }

        protected boolean nextInternal()
            throws SQLException {
            _pos = 0;
            return super.nextInternal();
        }

        protected int findObject(Object obj, Joins joins)
            throws SQLException {
            if (_pos == _sel._selects.size())
                _pos = 0;

            // we key directly on objs and join-less cols, or on the alias
            // for cols with joins
            PathJoins pj = getJoins(joins);
            Boolean pk = null;
            if (pj != null && pj.path() != null) {
                Column col = (Column) obj;
                pk = (col.isPrimaryKey()) ? Boolean.TRUE : Boolean.FALSE;
                if (joins == null && cachedColumnAlias_ != null) {
                    obj = cachedColumnAlias_.get(col);
                    if (obj == null)
                        obj = getColumnAlias(col, pj);
                } else {
                    obj = getColumnAlias(col, pj);
                }
                if (obj == null)
                    throw new SQLException(col.getTable() + ": "
                        + pj.path() + " (" + _sel._aliases + ")");
            }

            // we load in the same order we select, more or less...
            if (_sel._selects.get(_pos).equals(obj))
                return ++_pos;

            // if we're looking for a primary key, try back a couple places,
            // since pks might be selected in a slightly different order than
            // they are loaded back; don't change the marker position
            if (pk == null)
                pk = (obj instanceof Column && ((Column) obj).isPrimaryKey())
                    ? Boolean.TRUE : Boolean.FALSE;
            if (pk.booleanValue()) {
                for (int i = _pos - 1; i >= 0 && i >= _pos - 3; i--)
                    if (_sel._selects.get(i).equals(obj))
                        return i + 1;
            }

            // search forward on the assumption that we might be skipping
            // selects for sibling classes; advance the position if we find
            // something forward
            for (int i = _pos + 1; i < _sel._selects.size(); i++) {
                if (_sel._selects.get(i).equals(obj)) {
                    _pos = i;
                    return ++_pos;
                }
            }

            // maybe the column was selected by 2 different mappings, so it's
            // somewhere prior to the current position; in this case leave the
            // position marker at its current place cause subsequent loads will
            // still probably start from there
            for (int i = 0; i < _pos; i++)
                if (_sel._selects.get(i).equals(obj))
                    return i + 1;

            // somethings's wrong...
            throw new SQLException(obj.toString());
        }

        /**
         * Return the joins to use to find column data.
         */
        private PathJoins getJoins(Joins joins) {
            PathJoins pj = (PathJoins) joins;
            if (pj != null && pj.path() != null)
                return pj;
            return getPreJoins();
        }

        /**
         * Return the pre joins for the result, or null if none. Note that
         * we have to take the Select's pre joins into account too, since
         * batched selects can have additional pre joins on the stack even
         * on execution.
         */
        private PathJoins getPreJoins() {
            if (_preJoins != null && !_preJoins.isEmpty())
                return (PathJoins) _preJoins.peek();
            if (_sel._preJoins != null && !_sel._preJoins.isEmpty())
                return (PathJoins) _sel._preJoins.peek();
            return null;
        }

        /**
         * Return the alias used to key on the column data, considering the
         * given joins.
         */
        private String getColumnAlias(Column col, PathJoins pj) {
            String alias;
            if (_sel._from != null) {
                alias = _sel.toAlias(_sel._from.getTableIndex
                    (col.getTable(), pj, false));
                if (alias == null)
                    return null;
                if (_sel._dict.requiresAliasForSubselect)
                    return FROM_SELECT_ALIAS + "." + alias + "_" + col;
                return alias + "_" + col;
            }
            alias = _sel.toAlias(_sel.getTableIndex(col.getTable(), pj, false));
            return (alias == null) ? null : alias + "." + col;
        }

        ////////////////////////////
        // PathJoins implementation
        ////////////////////////////

        public boolean isOuter() {
            return false;
        }

        public PathJoins setOuter(boolean outer) {
            return this;
        }

        public boolean isDirty() {
            return false;
        }

        public StringBuilder path() {
            return null;
        }
        
        public String getPathStr() {
            return null;
        }

        public JoinSet joins() {
            return null;
        }

        public int joinCount() {
            return 0;
        }

        public void nullJoins() {
        }

        public boolean isEmpty() {
            return true;
        }

        public Joins crossJoin(Table localTable, Table foreignTable) {
            return this;
        }

        public Joins join(ForeignKey fk, boolean inverse, boolean toMany) {
            return this;
        }

        public Joins outerJoin(ForeignKey fk, boolean inverse, boolean toMany) {
            return this;
        }

        public Joins joinRelation(String name, ForeignKey fk, 
            ClassMapping target, int subs, boolean inverse, boolean toMany) {
            return new PathJoinsImpl().joinRelation(name, fk, target, subs, 
                inverse, toMany);
        }

        public Joins outerJoinRelation(String name, ForeignKey fk,
            ClassMapping target, int subs, boolean inverse, boolean toMany) {
            return new PathJoinsImpl().outerJoinRelation(name, fk, target, subs,
                inverse, toMany);
        }

        public Joins setVariable(String var) {
            if (var == null)
                return this;
            return new PathJoinsImpl().setVariable(var);
        }

        public Joins setSubselect(String alias) {
            if (alias == null)
                return this;
            return new PathJoinsImpl().setSubselect(alias);
        }

        public Joins setCorrelatedVariable(String var) {
            return this;
        }

        public Joins setJoinContext(Context ctx) {
            return this;
        }

        public String getCorrelatedVariable() {
            return null;
        }

        public void moveJoinsToParent() {
        }
    }

    /**
     * Base joins implementation.
     */
    private static class PathJoinsImpl
        implements PathJoins {

        protected StringBuilder path = null;
        protected String var = null;
        protected String correlatedVar = null;
        protected Context context = null;
        protected Context lastContext = null;
        protected String pathStr = null;

        public Select getSelect() {
            return null;
        }

        public boolean isOuter() {
            return false;
        }

        public PathJoins setOuter(boolean outer) {
            return this;
        }

        public boolean isDirty() {
            return var != null || path != null;
        }

        public StringBuilder path() {
            return path;
        }

        public JoinSet joins() {
            return null;
        }

        public int joinCount() {
            return 0;
        }

        public void nullJoins() {
        }

        public Joins setVariable(String var) {
            this.var = var;
            return this;
        }

        public String getVariable() {
            return var;
        }
        
        public Joins setCorrelatedVariable(String var) {
            this.correlatedVar = var;
            return this;
        }
        
        public String getCorrelatedVariable() {
            return correlatedVar;
        }

        public Joins setJoinContext(Context context) {
            this.context = context;
            return this;
         }

        public Joins setSubselect(String alias) {
            append(alias);
            return this;
        }

        public boolean isEmpty() {
            return true;
        }

        public Joins crossJoin(Table localTable, Table foreignTable) {
            append(var);
            var = null;
            return this;
        }

        public Joins join(ForeignKey fk, boolean inverse, boolean toMany) {
            append(var);
            var = null;
            return this;
        }

        public Joins outerJoin(ForeignKey fk, boolean inverse, boolean toMany) {
            append(var);
            var = null;
            return this;
        }

        public Joins joinRelation(String name, ForeignKey fk, 
            ClassMapping target, int subs, boolean inverse, boolean toMany) {
            append(name);
            append(var);
            var = null;
            return this;
        }

        public Joins outerJoinRelation(String name, ForeignKey fk,
            ClassMapping target, int subs, boolean inverse, boolean toMany) {
            append(name);
            append(var);
            var = null;
            return this;
        }

        protected void append(String str) {
            if (str != null) {
                if (path == null)
                    path = new StringBuilder(str);
                else
                    path.append('.').append(str);
                pathStr = null;
            }
        }
        
        public String getPathStr() {
            if (pathStr == null) {
                pathStr = path.toString();
            }
            return pathStr;
        }

        public String toString() {
            return "PathJoinsImpl<" + hashCode() + ">: "
                + String.valueOf(path);
        }

        public void moveJoinsToParent() {
        }
    }

    /**
     * Joins implementation.
     */
    private static class SelectJoins
        extends PathJoinsImpl 
        implements Cloneable {

        private final SelectImpl _sel;
        private JoinSet _joins = null;
        private boolean _outer = false;
        private int _count = 0;

        public SelectJoins(SelectImpl sel) {
            _sel = sel;
        }

        public Select getSelect() {
            return _sel;
        }

        public boolean isOuter() {
            return _outer;
        }

        public PathJoins setOuter(boolean outer) {
            _outer = outer;
            return this;
        }

        public boolean isDirty() {
            return super.isDirty() || !isEmpty();
        }

        public JoinSet joins() {
            return _joins;
        }

        public void setJoins(JoinSet joins) {
            _joins = joins;
            _outer = joins != null && joins.last() != null
                && joins.last().getType() == Join.TYPE_OUTER;
        }

        public int joinCount() {
            if (_joins == null)
                return _count;
            return Math.max(_count, _joins.size());
        }

        public void nullJoins() {
            if (_joins != null)
                _count = Math.max(_count, _joins.size());
            _joins = null;
        }

        public boolean isEmpty() {
            return _joins == null || _joins.isEmpty();
        }

        public Joins crossJoin(Table localTable, Table foreignTable) {
            // cross joins are for unbound variables; unfortunately we have
            // to always go DISTINCT for unbound vars because there are certain
            // cases that require it, and we can't differentiate them from the
            // cases that don't
            _sel._flags |= IMPLICIT_DISTINCT;

            if (_sel.getJoinSyntax() != JoinSyntaxes.SYNTAX_SQL92
                || _sel._from != null) {
                // don't make any joins, but update the path if a variable
                // has been set
                if (this.var != null) {
                    this.append(this.var);
                } else if (this.path == null && this.correlatedVar != null && _sel._dict.isImplicitJoin()) {
                    this.append(this.correlatedVar);
                }
                this.var = null;
                _outer = false;
                return this;
            }

            // don't let the get alias methods see that a var has been set
            // until we get past the local table
            String var = this.var;
            this.var = null;
            Context ctx = context; 
            context = null; 

            int alias1 = _sel.getTableIndex(localTable, this, true);
            this.append(var);
            this.append(correlatedVar);
            context = ctx; 
            
            int alias2 = _sel.getTableIndex(foreignTable, this, true);
            Join j = new Join(localTable, alias1, foreignTable, alias2,
                null, false);
            j.setType(Join.TYPE_CROSS);

            if (_joins == null)
                _joins = new JoinSet();
            _joins.add(j);
            setCorrelated(j);
            _outer = false;
            lastContext =  context;
            context = null;
            return this;
        }

        public Joins join(ForeignKey fk, boolean inverse, boolean toMany) {
            return join(null, fk, null, -1, inverse, toMany, false);
        }

        public Joins outerJoin(ForeignKey fk, boolean inverse, boolean toMany) {
            return join(null, fk, null, -1, inverse, toMany, true);
        }

        public Joins joinRelation(String name, ForeignKey fk, 
            ClassMapping target, int subs, boolean inverse, boolean toMany) {
            return join(name, fk, target, subs, inverse, toMany, false);
        }

        public Joins outerJoinRelation(String name, ForeignKey fk,
            ClassMapping target, int subs, boolean inverse, boolean toMany) {
            return join(name, fk, target, subs, inverse, toMany, true);
        }

        private Joins join(String name, ForeignKey fk, ClassMapping target,
            int subs, boolean inverse, boolean toMany, boolean outer) {
            // don't let the get alias methods see that a var has been set
            // until we get past the local table
            String var = this.var;
            this.var = null;
            Context ctx = context; 
            context = null; 

            // get first table alias before updating path; if there is a from
            // select then we shouldn't actually create a join object, since
            // the joins will all be done in the from select
            boolean createJoin = _sel._from == null;
            Table table1 = null;
            int alias1 = -1;
            if (createJoin) {
                boolean createIndex = true;
                table1 = (inverse) ? fk.getPrimaryKeyTable() : fk.getTable();
                if (correlatedVar != null)
                    createIndex = false;  // not to create here
                alias1 = _sel.getTableIndex(table1, this, createIndex);
            }

            // update the path with the relation name before getting pk alias
            this.append(name);
            this.append(var);
            if (var == null)
                this.append(correlatedVar);
            context = ctx; 
            
            if (toMany) {
                _sel._flags |= IMPLICIT_DISTINCT;
                _sel._flags |= TO_MANY;
            }
            _outer = outer;

            if (createJoin) {
                boolean createIndex = true;
                Table table2 = (inverse) ? fk.getTable() 
                    : fk.getPrimaryKeyTable();
                boolean created = false;
                int alias2 = -1;
                if (table2.isAssociation()) {
                    alias2 = _sel.getTableIndex(table2, this, false);
                    if (alias2 == -1)
                        createIndex = true;
                    else 
                        created = true;
                }
                else if (context == _sel.ctx()) 
                   createIndex = true;
                else if (correlatedVar != null)
                    createIndex = false;
                if (!created)
                    alias2 = _sel.getTableIndex(table2, this, createIndex);
                Join j = new Join(table1, alias1, table2, alias2, fk, inverse);
                j.setType((outer) ? Join.TYPE_OUTER : Join.TYPE_INNER);

                if (_joins == null)
                    _joins = new JoinSet();
                if (_joins.add(j) && (subs == Select.SUBS_JOINABLE 
                    || subs == Select.SUBS_NONE))
                    j.setRelation(target, subs, clone(_sel));

                setCorrelated(j);
            }
            lastContext = context;
            context = null;
            return this;
        }

        private void setCorrelated(Join j) {
            if (_sel._parent == null)
                return;

            if (_sel._aliases == null) {
                j.setIsNotMyJoin();
               return;
            }

            Object aliases[] = _sel._aliases.values().toArray();
            boolean found1 = false;
            boolean found2 = false;

            for (int i = 0; i < aliases.length; i++) {
                int alias = ((Integer)aliases[i]).intValue();
                if (alias == j.getIndex1())
                    found1 = true;
                if (alias == j.getIndex2())
                    found2 = true;
            }
                
            if (found1 && found2)
                return;
            else if (!found1 && !found2) {
                j.setIsNotMyJoin();
                return;
            }
            else {
                j.setCorrelated();
            }
        }

        public void moveJoinsToParent() {
            if (_joins == null)
                return;
           Join j = null;
           List<Join> removed = new ArrayList<Join>(5);
           for (Iterator itr = _joins.iterator(); itr.hasNext();) {
               j = (Join) itr.next();
               if (j.isNotMyJoin()) {
                   addJoinsToParent(_sel._parent, j);
                   removed.add(j);
               }
           }
           for (Join join : removed) {
               _joins.remove(join);
           }
        }

        private void addJoinsToParent(SelectImpl parent, Join join) {
            if (parent._aliases == null)
                return;
            Object aliases[] = parent._aliases.values().toArray();
            boolean found1 = false;
            boolean found2 = false;

            for (int i = 0; i < aliases.length; i++) {
                int alias = ((Integer)aliases[i]).intValue();
                if (alias == join.getIndex1())
                    found1 = true;
                if (alias == join.getIndex2())
                    found2 = true;
            }
                
            if (found1 && found2) {
                // this is my join, add join
                if (parent._joins == null) 
                    parent._joins = new SelectJoins(parent);
                SelectJoins p = parent._joins;
                if (p.joins() == null)
                    p.setJoins(new JoinSet());                
                p.joins().add(join);
            }
            else if (parent._parent != null)
                addJoinsToParent(parent._parent, join);
        }

        public SelectJoins clone(SelectImpl sel) {
            SelectJoins sj = new SelectJoins(sel);
            sj.var = var;
            if (path != null)
                sj.path = new StringBuilder(path.toString());
            if (_joins != null && !_joins.isEmpty())
                sj._joins = new JoinSet(_joins);
            sj._outer = _outer;
            return sj;
        }

        public String toString() {
            return super.toString() + " (" + _outer + "): " + _joins;
        }
    }
    
    protected Selects newSelects() {
        return new Selects();
    }
    
    public DBDictionary getDictionary() { 
        return _dict;
    }

    /**
     * Helper class to track selected columns, with fast contains method.
     * Acts as a list of select ids, with additional methods to manipulate
     * the alias of each selected id.
     */
    protected static class Selects
        extends AbstractList {

        protected List _ids = null;
        protected List _idents = null;
        protected Map _aliases = null;
        protected Map _selectAs = null;
        protected DBDictionary _dict = null;

        /**
         * Add all aliases from another instance.
         */
        public void addAll(Selects sels) {
            if (_ids == null && sels._ids != null)
                _ids = new ArrayList(sels._ids);
            else if (sels._ids != null)
                _ids.addAll(sels._ids);

            if (_idents == null && sels._idents != null)
                _idents = new ArrayList(sels._idents);
            else if (sels._idents != null)
                _idents.addAll(sels._idents);

            if (_aliases == null && sels._aliases != null)
                _aliases = new HashMap(sels._aliases);
            else if (sels._aliases != null)
                _aliases.putAll(sels._aliases);

            if (_selectAs == null && sels._selectAs != null)
                _selectAs = new HashMap(sels._selectAs);
            else if (sels._selectAs != null)
                _selectAs.putAll(sels._selectAs);
        }

        /**
         * Returns the alias of a given id.
         */
        public Object getAlias(Object id) {
            return (_aliases == null) ? null : _aliases.get(id);
        }

        /**
         * Set an alias for a given id.
         */
        public int setAlias(Object id, Object alias, boolean ident) {
            if (_ids == null) {
                _ids = new ArrayList();
                _aliases = new HashMap();
            }

            int idx;
            if (_aliases.put(id, alias) != null)
                idx = _ids.indexOf(id);
            else {
                _ids.add(id);
                idx = _ids.size() - 1;

                if (ident) {
                    if (_idents == null)
                        _idents = new ArrayList(3);
                    _idents.add(id);
                }
            }
            return idx;
        }

        /**
         * Set an alias for a given index.
         */
        public void setAlias(int idx, Object alias) {
            Object id = _ids.get(idx);
            _aliases.put(id, alias);
        }

        /**
         * Insert an alias before the given index, using negative indexes
         * to count backwards.
         */
        public void insertAlias(int idx, Object id, Object alias) {
            _aliases.put(id, alias);
            if (idx >= 0)
                _ids.add(idx, id);
            else
                _ids.add(_ids.size() + idx, id);
        }

        /**
         * Return the index of the given alias.
         */
        public int indexOfAlias(Object alias) {
            if (_aliases == null)
                return -1;
            for (int i = 0; i < _ids.size(); i++)
                if (alias.equals(_aliases.get(_ids.get(i))))
                    return i;
            return -1;
        }

        /**
         * A list representation of the aliases, in select order, with
         * AS aliases present.
         */
        public List getAliases(final boolean ident, final boolean inner) {
            if (_ids == null)
                return Collections.EMPTY_LIST;

            return new AbstractList() {
                public int size() {
                    return (ident && _idents != null) ? _idents.size()
                        : _ids.size();
                }

                public Object get(int i) {
                    Object id = (ident && _idents != null) ? _idents.get(i)
                        : _ids.get(i);
                    Object alias = _aliases.get(id);
                    if (id instanceof Column && ((Column) id).isXML())
                        alias = alias + _dict.getStringVal;
                        
                    String as = null;
                    if (inner) {
                        if (alias instanceof String)
                            as = ((String) alias).replace('.', '_');
                    } else if (_selectAs != null)
                        as = (String) _selectAs.get(id);
                    else if (id instanceof Value)
                        as = ((Value) id).getAlias();

                    if (as != null) {
                        if (ident && _idents != null)
                            return as;
                        if (alias instanceof SQLBuffer)
                            alias = new SQLBuffer((SQLBuffer) alias).
                                append(" AS ").append(as);
                        else
                            alias = alias + " AS " + as;
                    }
                    return alias;
                }
            };
        }

        /**
         * Set that a given id's alias has an AS value.
         */
        public void setSelectAs(Object id, String as) {
            if (_selectAs == null)
                _selectAs = new HashMap((int) (5 * 1.33 + 1));
            _selectAs.put(id, as);
        }

        /**
         * Clear all placeholders and select AS clauses.
         */
        public void clearPlaceholders() {
            if (_ids == null)
                return;

            Object id;
            for (Iterator itr = _ids.iterator(); itr.hasNext();) {
                id = itr.next();
                if (id instanceof Placeholder) {
                    itr.remove();
                    _aliases.remove(id);
                }
            }
        }

        public boolean contains(Object id) {
            return _aliases != null && _aliases.containsKey(id);
        }

        public Object get(int i) {
            if (_ids == null)
                throw new ArrayIndexOutOfBoundsException();
            return _ids.get(i);
        }

        public int size() {
            return (_ids == null) ? 0 : _ids.size();
        }

        public void clear() {
            _ids = null;
            _aliases = null;
            _selectAs = null;
            _idents = null;
        }
    }

    public Joins setCorrelatedVariable(String var) {
        if (var == null)
            return this;
        return new SelectJoins(this).setCorrelatedVariable(var);
    }
    
    public Joins setJoinContext(Context ctx) {
        if (ctx == null)
            return this;
        return new SelectJoins(this).setJoinContext(ctx);
    }

    public String getCorrelatedVariable() {
        return null;
    }

    public void moveJoinsToParent() {
    }
}

/**
 * Common joins interface used internally. Cannot be made an inner class
 * because the outer class (Select) has to implement it.
 */
interface PathJoins
    extends Joins {

    /**
     * Mark this as an outer joins set.
     */
    public PathJoins setOuter(boolean outer);

    /**
     * Return true if this instance has a path, any joins, or a variable.
     */
    public boolean isDirty();

    /**
     * Return the relation path traversed by these joins, or null if none.
     */
    public StringBuilder path();

    /**
     * Return the set of {@link Join} elements, or null if none.
     */
    public JoinSet joins();

    /**
     * Return the maximum number of joins contained in this instance at any
     * time.
     */
    public int joinCount();

    /**
     * Null the set of {@link Join} elements.
     */
    public void nullJoins();

    /**
     * The select owner of this join
     * @return
     */
    public Select getSelect();
    
    public String getPathStr();
}

