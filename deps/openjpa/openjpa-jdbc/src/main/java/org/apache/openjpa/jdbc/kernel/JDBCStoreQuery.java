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
package org.apache.openjpa.jdbc.kernel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.openjpa.event.LifecycleEventManager;
import org.apache.openjpa.jdbc.kernel.exps.ExpContext;
import org.apache.openjpa.jdbc.kernel.exps.GetColumn;
import org.apache.openjpa.jdbc.kernel.exps.JDBCExpressionFactory;
import org.apache.openjpa.jdbc.kernel.exps.JDBCStringContains;
import org.apache.openjpa.jdbc.kernel.exps.JDBCWildcardMatch;
import org.apache.openjpa.jdbc.kernel.exps.PCPath;
import org.apache.openjpa.jdbc.kernel.exps.QueryExpressionsState;
import org.apache.openjpa.jdbc.kernel.exps.SQLEmbed;
import org.apache.openjpa.jdbc.kernel.exps.SQLExpression;
import org.apache.openjpa.jdbc.kernel.exps.SQLValue;
import org.apache.openjpa.jdbc.kernel.exps.Val;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Discriminator;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.strats.NoneDiscriminatorStrategy;
import org.apache.openjpa.jdbc.meta.strats.VerticalClassStrategy;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.PostgresDictionary;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.SQLExceptions;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.jdbc.sql.SelectImpl;
import org.apache.openjpa.jdbc.sql.Union;
import org.apache.openjpa.kernel.ExpressionStoreQuery;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.OrderingMergedResultObjectProvider;
import org.apache.openjpa.kernel.QueryHints;
import org.apache.openjpa.kernel.exps.Constant;
import org.apache.openjpa.kernel.exps.Context;
import org.apache.openjpa.kernel.exps.ExpressionFactory;
import org.apache.openjpa.kernel.exps.ExpressionParser;
import org.apache.openjpa.kernel.exps.FilterListener;
import org.apache.openjpa.kernel.exps.Literal;
import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.lib.rop.MergedResultObjectProvider;
import org.apache.openjpa.lib.rop.RangeResultObjectProvider;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.util.UnsupportedException;
import org.apache.openjpa.util.UserException;

/**
 * JDBC query implementation.
 *
 * @author Abe White
 * @nojavadoc
 */
public class JDBCStoreQuery 
    extends ExpressionStoreQuery {

    private static final Table INVALID = new Table();

    // add all standard filter and aggregate listeners to these maps
    private static final Map _listeners = new HashMap();

    static {
        // deprecated extensions
        _listeners.put(JDBCStringContains.TAG, new JDBCStringContains());
        _listeners.put(JDBCWildcardMatch.TAG, new JDBCWildcardMatch());
        _listeners.put(SQLExpression.TAG, new SQLExpression());
        _listeners.put(SQLValue.TAG, new SQLValue());

        // jdbc-specific extensions
        _listeners.put(GetColumn.TAG, new GetColumn());
        _listeners.put(SQLEmbed.TAG, new SQLEmbed());
    }

    private final transient JDBCStore _store;
    private static ThreadLocalContext localContext = new ThreadLocalContext();

    /**
     * Constructor. Supply store manager.
     */
    public JDBCStoreQuery(JDBCStore store, ExpressionParser parser) {
        super(parser);
        _store = store;
    }

    /**
     * Return the store.
     */
    public JDBCStore getStore() {
        return _store;
    }

    public FilterListener getFilterListener(String tag) {
        return (FilterListener) _listeners.get(tag);
    }

    public Object newCompilationKey() {
        JDBCFetchConfiguration fetch = (JDBCFetchConfiguration) ctx
            .getFetchConfiguration();
        return fetch.getJoinSyntax();
    }

    public boolean supportsDataStoreExecution() {
        return true;
    }

    protected ClassMetaData[] getIndependentExpressionCandidates(
        ClassMetaData meta, boolean subclasses) {
        if (!subclasses)
            return new ClassMapping[] { (ClassMapping) meta };
        return ((ClassMapping) meta).getIndependentAssignableMappings();
    }

    protected ExpressionFactory getExpressionFactory(ClassMetaData meta) {
        JDBCExpressionFactory factory = new JDBCExpressionFactory((ClassMapping) meta);
        if (_store.getDBDictionary() instanceof PostgresDictionary)
            factory.setBooleanLiteralAsNumeric(false);
        return factory;
    }
    
    protected ResultObjectProvider executeQuery(Executor ex,
        ClassMetaData base, ClassMetaData[] metas, boolean subclasses,
        ExpressionFactory[] facts, QueryExpressions[] exps, Object[] params,
        Range range) {
        Context[] ctxs = new Context[exps.length];
        for (int i = 0; i < exps.length; i++)
            ctxs[i] = exps[i].ctx();
        localContext.set(clone(ctxs, null));
        if (metas.length > 1 && exps[0].isAggregate())
            throw new UserException(Localizer.forPackage(JDBCStoreQuery.class).
                get("mult-mapping-aggregate", Arrays.asList(metas)));

        ClassMapping[] mappings = (ClassMapping[]) metas;
        JDBCFetchConfiguration fetch = (JDBCFetchConfiguration) 
            ctx.getFetchConfiguration();
        if (exps[0].fetchPaths != null) {
            fetch.addFields(Arrays.asList(exps[0].fetchPaths));
            fetch.addJoins(Arrays.asList(exps[0].fetchPaths));
        }
        if (exps[0].fetchInnerPaths != null)
            fetch.addFetchInnerJoins(Arrays.asList(exps[0].fetchInnerPaths));

        int eager = calculateEagerMode(exps[0], range.start, range.end);
        int subclassMode = fetch.getSubclassFetchMode((ClassMapping) base);
        DBDictionary dict = _store.getDBDictionary();
        long start = (mappings.length == 1 && dict.supportsSelectStartIndex) 
            ? range.start : 0L;
        long end = (dict.supportsSelectEndIndex) ? range.end : Long.MAX_VALUE;

        QueryExpressionsState[] states = new QueryExpressionsState[exps.length];
        for (int i = 0; i < states.length; i++) {
            states[i] = new QueryExpressionsState();
            exps[i].state = states[i];
        }
        ExpContext ctx = new ExpContext(_store, params, fetch);

        // add selects with populate WHERE conditions to list
        List sels = new ArrayList(mappings.length);
        List selMappings = new ArrayList(mappings.length);
        BitSet subclassBits = new BitSet();
        BitSet nextBits = new BitSet();
        boolean unionable = createWhereSelects(sels, mappings, selMappings,
            subclasses, subclassBits, nextBits, facts, exps, states, ctx,
            subclassMode)
            && subclassMode == JDBCFetchConfiguration.EAGER_JOIN
            && start == 0
            && end == Long.MAX_VALUE;

        // we might want to use lrs settings if we can't use the range
        if (sels.size() > 1)
            start = 0L;
        boolean lrs = range.lrs || (fetch.getFetchBatchSize() >= 0 
            && (start != range.start || end != range.end));

        ResultObjectProvider[] rops = null;
        ResultObjectProvider rop = null;
        if (unionable) {
            Union union = _store.getSQLFactory().newUnion(
                (Select[]) sels.toArray(new Select[sels.size()]));
            BitSet[] paged = populateUnion(union, mappings, subclasses, facts,
                exps, states, ctx, lrs, eager, start, end);
            union.setLRS(lrs);
            rop = executeUnion(union, mappings, exps, states, ctx, paged);
        } else {
            if (sels.size() > 1)
                rops = new ResultObjectProvider[sels.size()];

            Select sel;
            BitSet paged;
            for (int i = 0, idx = 0; i < sels.size(); i++) {
                sel = (Select) sels.get(i);
                paged = populateSelect(sel, (ClassMapping) selMappings.get(i),
                    subclassBits.get(i), (JDBCExpressionFactory) facts[idx],
                    exps[idx], states[idx], ctx, lrs, eager, start, end);

                rop = executeSelect(sel, (ClassMapping) selMappings.get(i),
                    exps[idx], states[idx], ctx, paged, start, end);
                if (rops != null)
                    rops[i] = rop;

                if (nextBits.get(i))
                    idx++;
            }
        }

        if (rops != null) {
            if (exps[0].ascending.length == 0)
                rop = new MergedResultObjectProvider(rops);
            else {
                rop = new OrderingMergedResultObjectProvider(rops,
                    exps[0].ascending, ex, this, params);
            }
        }

        // need to fake result range?
        if ((rops != null && range.end != Long.MAX_VALUE) 
            || start != range.start || end != range.end)
            rop = new RangeResultObjectProvider(rop, range.start, range.end);

        localContext.remove();
        return rop;
    }

    /**
     * Select data for the given union, returning paged fields.
     */
    private BitSet[] populateUnion(Union union, final ClassMapping[] mappings,
        final boolean subclasses, final ExpressionFactory[] facts,
        final QueryExpressions[] exps, final QueryExpressionsState[] states,
        final ExpContext ctx, final boolean lrs, final int eager,
        final long start, final long end) {
        final BitSet[] paged = (exps[0].projections.length > 0) ? null
            : new BitSet[mappings.length];
        union.select(new Union.Selector() {
            public void select(Select sel, int idx) {
                BitSet bits = populateSelect(sel, mappings[idx], subclasses,
                    (JDBCExpressionFactory) facts[idx], exps[idx], states[idx],
                    ctx,  lrs, eager, start, end);
                if (paged != null)
                    paged[idx] = bits;
            }
        });
        return paged;
    }

    /**
     * Select data for the given select, returning paged fields.
     */
    private BitSet populateSelect(Select sel, ClassMapping mapping,
        boolean subclasses, JDBCExpressionFactory fact, QueryExpressions exps,
        QueryExpressionsState state, ExpContext ctx, boolean lrs, int eager,
        long start, long end) {
        sel.setLRS(lrs);
        sel.setRange(start, end);

        BitSet paged = null;
        if (exps.projections.length == 0) {
            paged = PagingResultObjectProvider.getPagedFields(sel, mapping,
                _store, ctx.fetch, eager, end - start);
            if (paged != null)
                eager = JDBCFetchConfiguration.EAGER_JOIN;
        }

        fact.getSelectConstructor().select(sel, ctx, mapping, subclasses, exps,
            state, eager);
        return paged;
    }

    /**
     * Execute the given union.
     */
    private ResultObjectProvider executeUnion(Union union,
        ClassMapping[] mappings, QueryExpressions[] exps, 
        QueryExpressionsState[] states, ExpContext ctx, BitSet[] paged) {
        if (exps[0].projections.length > 0)
            return new ProjectionResultObjectProvider(union, exps, states, ctx);

        if (paged != null)
            for (int i = 0; i < paged.length; i++)
                if (paged[i] != null)
                    return new PagingResultObjectProvider(union, mappings,
                        _store, ctx.fetch, paged, Long.MAX_VALUE);

        return new InstanceResultObjectProvider(union, mappings[0], _store,
            ctx.fetch);
    }

    /**
     * Execute the given select.
     */
    private ResultObjectProvider executeSelect(Select sel, ClassMapping mapping,
        QueryExpressions exps, QueryExpressionsState state, ExpContext ctx, 
        BitSet paged, long start, long end) {
        if (exps.projections.length > 0)
            return new ProjectionResultObjectProvider(sel, exps, state, ctx);
        if (paged != null)
            return new PagingResultObjectProvider(sel, mapping, _store, 
                ctx.fetch, paged, end - start);
        return new InstanceResultObjectProvider(sel, mapping, _store, 
            ctx.fetch);
    }

    /**
     * Generate the selects with WHERE conditions needed to execute the query
     * for the given mappings.
     */
    private boolean createWhereSelects(List sels, ClassMapping[] mappings,
        List selMappings, boolean subclasses, BitSet subclassBits,
        BitSet nextBits, ExpressionFactory[] facts, QueryExpressions[] exps,
        QueryExpressionsState[] states, ExpContext ctx, int subclassMode) {
        Number optHint = (Number) ctx.fetch.getHint
            (QueryHints.HINT_RESULT_COUNT);
        ClassMapping[] verts;
        boolean unionable = true;
        Select sel;
        for (int i = 0; i < mappings.length; i++) {
            // determine vertical mappings to select separately
            verts = getVerticalMappings(mappings[i], subclasses, exps[i],
                subclassMode);
            if (verts.length == 1 && subclasses)
                subclassBits.set(sels.size());

            Discriminator disc = mappings[i].getDiscriminator();
            if (mappings.length > 1 && disc != null && disc.getColumns().length == 0 &&
                disc.getStrategy() instanceof NoneDiscriminatorStrategy)
                ctx.tpcMeta = mappings[i];

            // create criteria select and clone for each vert mapping
            sel = ((JDBCExpressionFactory) facts[i]).getSelectConstructor().
                evaluate(ctx, null, null, exps[i], states[i]);
            if (optHint != null)
               sel.setExpectedResultCount(optHint.intValue(), true);
            else if (this.ctx.isUnique())
                sel.setExpectedResultCount(1, false);
            
            List selectFrom = getJoinedTableMeta(sel);
            int size = 0;
            if (selectFrom != null) {
                size = selectFrom.size();
                for (int j = 0; j < size; j++) {
                    ClassMapping vert = (ClassMapping)selectFrom.get(j); 
                    selMappings.add(vert);
                    if (j == size - 1) {
                        nextBits.set(sels.size());
                        sel.select(vert.getPrimaryKeyColumns(), null);
                        sels.add(sel);
                    } else {
                        SelectImpl selClone = (SelectImpl)sel.fullClone(1);
                        selClone.select(vert.getPrimaryKeyColumns(), null);
                        sels.add(selClone);
                    }
                }
            } else {
                for (int j = 0; j < verts.length; j++) {
                    selMappings.add(verts[j]);
                    if (j == verts.length - 1) {
                        nextBits.set(sels.size());
                        sels.add(sel);
                    } else
                        sels.add(sel.fullClone(1));
                }
            }
            
            // turn off unioning if a given independent mapping requires
            // multiple selects, or if we're using FROM selects
            if (verts.length > 1 || size > 1 || sel.getFromSelect() != null)
                unionable = false;
        }
        return unionable;
    }
    
    private List getJoinedTableMeta(Select sel) {
        List selectFrom = sel.getJoinedTableClassMeta();
        List exSelectFrom = sel.getExcludedJoinedTableClassMeta();
        if (exSelectFrom == null)
            return selectFrom;
        if (selectFrom == null)
            return null;
        int size = selectFrom.size();
        List retList = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            Object obj = selectFrom.get(i);
            if (!exSelectFrom.contains(obj))
                retList.add(obj);
        }
        return retList;
    }

    /**
     * Return all the vertical mappings to select separately. Depends on
     * subclass fetch mode and the type of query.
     */
    private ClassMapping[] getVerticalMappings(ClassMapping mapping,
        boolean subclasses, QueryExpressions exps, int subclassMode) {
        if (!subclasses || exps.projections.length > 0)
            return new ClassMapping[] { mapping };

        if (subclassMode != JDBCFetchConfiguration.EAGER_PARALLEL
            || !hasVerticalSubclasses(mapping))
            return new ClassMapping[] { mapping };

        List subs = new ArrayList(4);
        addSubclasses(mapping, subs);
        return (ClassMapping[]) subs.toArray(new ClassMapping[subs.size()]);
    }

    /**
     * Recursive helper to add mappings for subclasses to the given list.
     */
    private void addSubclasses(ClassMapping mapping, Collection subs) {
        // possible future optimizations:
        // - if no fields in meta or its subclasses (and not in an
        //   already-selected table) are in the current fetch
        //   configuration, stop creating new executors
        // - allow an executor to select a range of subclasses, rather
        //   than just all subclasses / no subclasses; this would
        //   allow us to do just one query per actual vertically-mapped
        //   subclass, rather than one per mapped subclass, as is happening now

        subs.add(mapping);
        if (!hasVerticalSubclasses(mapping))
            return;

        // recurse on immediate subclasses
        ClassMapping[] subMappings = mapping.getJoinablePCSubclassMappings();
        for (int i = 0; i < subMappings.length; i++)
            if (subMappings[i].getJoinablePCSuperclassMapping() == mapping)
                addSubclasses(subMappings[i], subs);
    }

    /**
     * Return whether the given class has any vertical subclasses.
     */
    private static boolean hasVerticalSubclasses(ClassMapping mapping) {
        ClassMapping[] subs = mapping.getJoinablePCSubclassMappings();
        for (int i = 0; i < subs.length; i++)
            if (subs[i].getStrategy() instanceof VerticalClassStrategy)
                return true;
        return false;
    }

    /**
     * The eager mode depends on the unique setting and range. If the range
     * produces 0 results, use eager setting of none. If it produces 1 result
     * or the query is unique, use an eager setting of single. Otherwise use
     * an eager mode of multiple.
     */
    private int calculateEagerMode(QueryExpressions exps, long start,
        long end) {
        if (exps.projections.length > 0 || start >= end)
            return EagerFetchModes.EAGER_NONE;
        if (end - start == 1 || ctx.isUnique())
            return EagerFetchModes.EAGER_JOIN;
        return EagerFetchModes.EAGER_PARALLEL;
    }

    protected Number executeDelete(Executor ex, ClassMetaData base,
        ClassMetaData[] metas, boolean subclasses, ExpressionFactory[] facts,
        QueryExpressions[] exps, Object[] params) {
        return executeBulkOperation(metas, subclasses, facts, exps,
            params, null);
    }

    protected Number executeUpdate(Executor ex, ClassMetaData base,
        ClassMetaData[] metas, boolean subclasses, ExpressionFactory[] facts,
        QueryExpressions[] exps, Object[] params) {
        return executeBulkOperation(metas, subclasses, facts, exps,
            params, exps[0].updates);
    }

    private Number executeBulkOperation(ClassMetaData[] metas,
        boolean subclasses, ExpressionFactory[] facts, QueryExpressions[] exps,
        Object[] params, Map updates) {
        Context[] ctxs = new Context[exps.length];
        for (int i = 0; i < exps.length; i++)
            ctxs[i] = exps[i].ctx();
        localContext.set(clone(ctxs, null));
        
        // we cannot execute a bulk delete statement when have mappings in
        // multiple tables, so indicate we want to use in-memory with null
        ClassMapping[] mappings = (ClassMapping[]) metas;

        // specification of the "updates" map indicates that this is
        // an update query; otherwise, this is a delete statement
        boolean isUpdate = updates != null && updates.size() > 0;

        for (int i = 0; i < mappings.length; i++) {
            if (!isSingleTableMapping(mappings[i], subclasses) && !isUpdate)
                return null;

            if (!isUpdate) {
                // if there are any delete callbacks, we need to
                // execute in-memory so the callbacks are invoked
                LifecycleEventManager mgr = ctx.getStoreContext().getBroker()
                    .getLifecycleEventManager();
                if (mgr.hasDeleteListeners(null, mappings[i]))
                    return null;
            }
        }

        JDBCFetchConfiguration fetch = (JDBCFetchConfiguration) 
            ctx.getFetchConfiguration();
        ExpContext ctx = new ExpContext(_store, params, fetch);
        DBDictionary dict = _store.getDBDictionary();
        QueryExpressionsState[] state = new QueryExpressionsState[exps.length];
        for (int i = 0; i < state.length; i++)
            state[i] = new QueryExpressionsState();

        SQLBuffer[] sql = new SQLBuffer[mappings.length];
        JDBCExpressionFactory jdbcFactory;
        Select sel;
        for (int i = 0; i < mappings.length; i++) {
            jdbcFactory = (JDBCExpressionFactory) facts[i];
            sel = jdbcFactory.getSelectConstructor().evaluate(ctx, null, null,
                exps[i], state[i]);
            jdbcFactory.getSelectConstructor().select(sel, ctx, mappings[i], 
                subclasses, exps[i], state[i], 
                JDBCFetchConfiguration.EAGER_NONE);

            // The bulk operation will return null to indicate that the database
            // does not support the request bulk delete operation; in
            // this case, we need to perform the query in-memory and
            // manually delete the instances
            if (!isUpdate)
                sql[i] = dict.toDelete(mappings[i], sel, params);
            else
                sql[i] = dict.toUpdate(mappings[i], sel, _store, params,
                    updates);

            if (sql[i] == null)
                return null;
        }

        // we need to make sure we have an active store connection
        _store.getContext().beginStore();

        Connection conn = _store.getConnection();
        long count = 0;
        try {
            PreparedStatement stmnt;
            for (int i = 0; i < sql.length; i++) {
                stmnt = null;
                try {
                    stmnt = prepareStatement(conn, sql[i]);
                    dict.setTimeouts(stmnt, fetch, true);
                    count += executeUpdate(conn, stmnt, sql[i], isUpdate);
                } catch (SQLException se) {
                    throw SQLExceptions.getStore(se, sql[i].getSQL(), 
                        _store.getDBDictionary());
                } finally {
                    if (stmnt != null)
                        try { stmnt.close(); } catch (SQLException se) {}
                }
            }
        } finally {
            try { 
            	if (conn.getAutoCommit())
            		conn.close(); 
            } catch (SQLException se) {
            	
            }
        }

        localContext.remove();
        return count;
    }

    /**
     * Whether the given mapping occupies only one table.
     */
    private boolean isSingleTableMapping(ClassMapping mapping,
        boolean subclasses) {
        ClassMapping root = mapping;
        while (root.getJoinablePCSuperclassMapping() != null)
            root = root.getJoinablePCSuperclassMapping();
        if (hasVerticalSubclasses(root))
            return false;

        // we cannot execute a bulk delete if any of the
        // field mappings for the candidates have columns
        // in any other table, since bulk deleting just from the
        // class will leave dangling relations; we might be able
        // to issue bulk deletes separately for the joins (possibly
        // using a temporary table to select the primary keys for
        // all the related tables and then issing a delete against those
        // keys), but that logic is not currently implemented
        Table table = getTable(mapping.getFieldMappings(), null);
        if (table == INVALID)
            return false;

        if (subclasses) {
            // if we are including subclasses, we also need to gather
            // all the mappings for all known subclasses
            ClassMapping[] subs = mapping.getJoinablePCSubclassMappings();
            for (int i = 0; subs != null && i < subs.length; i++) {
                table = getTable(subs[i].getDefinedFieldMappings(), table);
                if (table == INVALID)
                    return false;
            }
        }
        return true;
    }

    /**
     * Return the single table for the given fields, or INVALID if they
     * use multiple tables.
     */
    private Table getTable(FieldMapping[] fields, Table table) {
        for (int i = 0; i < fields.length; i++) {
            table = getTable(fields[i], table);
            if (table == INVALID)
                break;
        }
        return table;
    }

    /**
     * Return the table for the field if the given table hasn't been set
     * yet, or if the tables match. If the field uses a different table,
     * returns INVALID. Also returns INVALID if field is dependent.
     */
    private Table getTable(FieldMapping fm, Table table) {
        if (fm.getCascadeDelete() != ValueMetaData.CASCADE_NONE 
            && !fm.isEmbeddedPC())
            return INVALID;

        Column[] columns = fm.getColumns();
        for (int i = 0; columns != null && i < columns.length; i++) {
            if (table == null)
                table = columns[i].getTable();
            else if (table != columns[i].getTable())
                return INVALID;
        }
        if (fm.isBidirectionalJoinTableMappingOwner())
        	return INVALID;
        return table;
    }

    protected Number executeUpdate(ClassMetaData base, ClassMetaData[] metas,
        boolean subclasses, ExpressionFactory[] facts,
        QueryExpressions[] parsed, Object[] params) {
        return null;
    }

    protected String[] getDataStoreActions(ClassMetaData base,
        ClassMetaData[] metas, boolean subclasses, ExpressionFactory[] facts,
        QueryExpressions[] exps, Object[] params, Range range) {
        Context[] ctxs = new Context[exps.length];
        for (int i = 0; i < exps.length; i++)
            ctxs[i] = exps[i].ctx();
        localContext.set(clone(ctxs, null));
        ClassMapping[] mappings = (ClassMapping[]) metas;
        JDBCFetchConfiguration fetch = (JDBCFetchConfiguration) ctx.
            getFetchConfiguration();
        if (exps[0].fetchPaths != null) {
            fetch.addFields(Arrays.asList(exps[0].fetchPaths));
            fetch.addJoins(Arrays.asList(exps[0].fetchPaths));
        }
        if (exps[0].fetchInnerPaths != null)
            fetch.addFetchInnerJoins(Arrays.asList(exps[0].fetchInnerPaths));

        int eager = calculateEagerMode(exps[0], range.start, range.end);
        eager = Math.min(eager, JDBCFetchConfiguration.EAGER_JOIN);
        int subclassMode = fetch.getSubclassFetchMode((ClassMapping) base);
        DBDictionary dict = _store.getDBDictionary();
        long start = (mappings.length == 1 && dict.supportsSelectStartIndex) 
            ? range.start : 0L;
        long end = (dict.supportsSelectEndIndex) ? range.end : Long.MAX_VALUE;

        QueryExpressionsState[] states = new QueryExpressionsState[exps.length];
        for (int i = 0; i < states.length; i++)
            states[i] = new QueryExpressionsState();
        ExpContext ctx = new ExpContext(_store, params, fetch);

        // add selects with populate WHERE conditions to list
        List sels = new ArrayList(mappings.length);
        List selMappings = new ArrayList(mappings.length);
        BitSet subclassBits = new BitSet();
        BitSet nextBits = new BitSet();
        boolean unionable = createWhereSelects(sels, mappings, selMappings,
            subclasses, subclassBits, nextBits, facts, exps, states, ctx, 
            subclassMode) && subclassMode == JDBCFetchConfiguration.EAGER_JOIN;
        if (sels.size() > 1)
            start = 0L;

        if (unionable) {
            Union union = _store.getSQLFactory().newUnion(
                (Select[]) sels.toArray(new Select[sels.size()]));
            populateUnion(union, mappings, subclasses, facts, exps, states, ctx,
                false, eager, start, end);
            if (union.isUnion())
                return new String[] {union.toSelect(false, fetch).getSQL(true)};
            sels = Arrays.asList(union.getSelects());
        } else {
            Select sel;
            for (int i = 0, idx = 0; i < sels.size(); i++) {
                sel = (Select) sels.get(i);
                populateSelect(sel, (ClassMapping) selMappings.get(i),
                    subclassBits.get(i), (JDBCExpressionFactory) facts[idx],
                    exps[idx], states[idx], ctx, false, eager, start, end);
                if (nextBits.get(i))
                    idx++;
            }
        }

        String[] sql = new String[sels.size()];
        for (int i = 0; i < sels.size(); i++)
            sql[i] = ((Select) sels.get(i)).toSelect(false, fetch).getSQL(true);

        localContext.remove();
        return sql;
    }
    
    /**
     * This method is to provide override for non-JDBC or JDBC-like 
     * implementation of executing update.
     */
    protected int executeUpdate(Connection conn, PreparedStatement stmnt, 
        SQLBuffer sqlBuf, boolean isUpdate) throws SQLException {
        return stmnt.executeUpdate();
    }
            
    /**
     * This method is to provide override for non-JDBC or JDBC-like 
     * implementation of preparing statement.
     */
    protected PreparedStatement prepareStatement(Connection conn, SQLBuffer sql)
        throws SQLException {
        return sql.prepareStatement(conn);
    }    

    public Object evaluate(Object value, Object ob, Object[] params, 
        OpenJPAStateManager sm) {
        int id = 0;
        if (value instanceof org.apache.openjpa.jdbc.kernel.exps.Val)
            id = ((org.apache.openjpa.jdbc.kernel.exps.Val)value).getId();
        else
            throw new UnsupportedException(); 

        switch(id) {
        case Val.MATH_VAL:
            return handleMathVal(value, ob, params, sm);
        case Val.CONCAT_VAL:
            return handleConcatVal(value, ob, params, sm);
        case Val.SUBSTRING_VAL:
            return handleSubstringVal(value, ob, params, sm);
        case Val.ARGS_VAL:
            return handleArgsVal(value, ob, params, sm);
        case Val.LOWER_VAL:
            return handleLowerVal(value, ob, params, sm);
        case Val.UPPER_VAL:
            return handleUpperVal(value, ob, params, sm);
        case Val.LENGTH_VAL:
            return handleLengthVal(value, ob, params, sm);
        case Val.TRIM_VAL:
            return handleTrimVal(value, ob, params, sm);
        case Val.INDEXOF_VAL:
            return handleIndexOfVal(value, ob, params, sm);
        case Val.ABS_VAL:
            return handleAbsVal(value, ob, params, sm);
        case Val.SQRT_VAL:
            return handleSqrtVal(value, ob, params, sm);
        default:    
            throw new UnsupportedException();
        }
    }

    private Object handleMathVal(Object value, Object ob, Object[] params, 
        OpenJPAStateManager sm) {
        org.apache.openjpa.jdbc.kernel.exps.Math mathVal =
            (org.apache.openjpa.jdbc.kernel.exps.Math) value;
        Val value1 = mathVal.getVal1();
        Object val1 = getValue(value1, ob, params, sm);
        Class c1 = value1.getType();

        Val value2 = mathVal.getVal2();
        Object val2 = getValue(value2, ob, params, sm);
        Class c2 = value2.getType();

        String op = mathVal.getOperation();
        if (op.equals(org.apache.openjpa.jdbc.kernel.exps.Math.ADD)) 
            return Filters.add(val1, c1, val2, c2);
        else if (op.equals(
                org.apache.openjpa.jdbc.kernel.exps.Math.SUBTRACT))
            return Filters.subtract(val1, c1, val2, c2);
        else if (op.equals(
                org.apache.openjpa.jdbc.kernel.exps.Math.MULTIPLY)) 
            return Filters.multiply(val1, c1, val2, c2);
        else if (op.equals(
                org.apache.openjpa.jdbc.kernel.exps.Math.DIVIDE)) 
            return Filters.divide(val1, c1, val2, c2);
        else if (op.equals(org.apache.openjpa.jdbc.kernel.exps.Math.MOD)) 
            return Filters.mod(val1, c1, val2, c2);
        throw new UnsupportedException();
    }

    private Object handleConcatVal(Object value, Object ob, Object[] params,
        OpenJPAStateManager sm) {
        org.apache.openjpa.jdbc.kernel.exps.Concat concatVal =
            (org.apache.openjpa.jdbc.kernel.exps.Concat)value;
        Val value1 = concatVal.getVal1();
        Object val1 = getValue(value1, ob, params, sm);

        Val value2 = concatVal.getVal2();
        Object val2 = getValue(value2, ob, params, sm);
        return new StringBuilder(100).append(val1).append(val2).toString();
    }

    private Object handleSubstringVal(Object value, Object ob, Object[] params,
        OpenJPAStateManager sm) {
        org.apache.openjpa.jdbc.kernel.exps.Substring substrVal =
            (org.apache.openjpa.jdbc.kernel.exps.Substring) value;
        Val value1 = substrVal.getVal1();
        String val1 = (String) getValue(value1, ob, params, sm);

        Val value2 = substrVal.getVal2();
        Object val2 = getValue(value2, ob, params, sm);

        org.apache.openjpa.kernel.exps.Value[] valAry2 = 
            (org.apache.openjpa.kernel.exps.Value[]) val2;
        Object arg1 = getValue(valAry2[0], ob, params, sm); //starting pos
        Object arg2 = getValue(valAry2[1], ob, params, sm); // length
        int startIdx = ((Long) arg1).intValue();
        int length = ((Long) arg2).intValue();
        int endIdx = startIdx + length;
        return val1.substring(startIdx, endIdx);
    }

    private Object handleArgsVal(Object value, Object ob, Object[] params, 
        OpenJPAStateManager sm) {
        org.apache.openjpa.jdbc.kernel.exps.Args argsVal = 
            (org.apache.openjpa.jdbc.kernel.exps.Args) value;
        return argsVal.getValues();
    }

    private Object handleLowerVal(Object value, Object ob, Object[] params, 
        OpenJPAStateManager sm) {
        org.apache.openjpa.jdbc.kernel.exps.ToLowerCase lowerVal = 
            (org.apache.openjpa.jdbc.kernel.exps.ToLowerCase) value;
        Val val = lowerVal.getValue();
        return ((String) getValue(val, ob, params, sm)).toLowerCase();
    }

    private Object handleUpperVal(Object value, Object ob, Object[] params, 
        OpenJPAStateManager sm){
        org.apache.openjpa.jdbc.kernel.exps.ToUpperCase upperVal = 
            (org.apache.openjpa.jdbc.kernel.exps.ToUpperCase) value;
        Val val = upperVal.getValue();
        return ((String) getValue(val, ob, params, sm)).toUpperCase();
    }

    private Object handleLengthVal(Object value, Object ob, Object[] params, 
        OpenJPAStateManager sm){
        org.apache.openjpa.jdbc.kernel.exps.StringLength strLenVal = 
            (org.apache.openjpa.jdbc.kernel.exps.StringLength) value;
        Val val = strLenVal.getValue();
        return ((String) getValue(val, ob, params, sm)).length();
    }

    private Object handleTrimVal(Object value, Object ob, Object[] params, 
        OpenJPAStateManager sm) {
        org.apache.openjpa.jdbc.kernel.exps.Trim trimVal = 
            (org.apache.openjpa.jdbc.kernel.exps.Trim) value;
        Val val = trimVal.getVal();
        String valStr = (String) getValue(val, ob, params, sm);
        Val trimChar = trimVal.getTrimChar();
        char trimCharObj = ((String) getValue(trimChar, ob, params, sm)).
            charAt(0);
        Boolean where = trimVal.getWhere();
        if (where == null) { //trim both
            return trimLeading(trimTrailing(valStr, trimCharObj), trimCharObj);
        } else if (where.booleanValue()) { // trim leading
            return trimLeading(valStr, trimCharObj);
        } else { // trim trailing
            return trimTrailing(valStr, trimCharObj);
        }
    }

    private String trimLeading(String value, char trimChar) {
        int startIdx = 0;
        int len = value.length();
        for (int i = 0; i < len; i++) {
            if (value.charAt(i) != trimChar) {
                startIdx = i;
                break;
            }
        }
        return value.substring(startIdx);
    }

    private String trimTrailing(String value, char trimChar) {
        int endIdx = 0;
        int len = value.length();
        for (int i = len-1; i >= 0; i--) {
            if (value.charAt(i) != trimChar) {
                endIdx = i;
                break;
            }
        }
        return value.substring(0, endIdx+1);
    }

    private Object handleIndexOfVal(Object value, Object ob, Object[] params,
        OpenJPAStateManager sm) {
        org.apache.openjpa.jdbc.kernel.exps.IndexOf locateVal = 
            (org.apache.openjpa.jdbc.kernel.exps.IndexOf) value;
        String val1 = (String) getValue(locateVal.getVal1(), ob, params, sm);
        Val[] val2 = (Val[]) getValue(locateVal.getVal2(), ob, params, sm);
        String strVal = (String) getValue(val2[0], ob, params, sm);
        int idx = ((Long) getValue(val2[1], ob, params, sm)).intValue();
        return strVal.indexOf(val1, idx);
    }

    private Object handleAbsVal(Object value, Object ob, Object[] params, 
        OpenJPAStateManager sm) {
        org.apache.openjpa.jdbc.kernel.exps.Abs absVal = 
            (org.apache.openjpa.jdbc.kernel.exps.Abs) value;
        Object val = getValue(absVal.getValue(), ob, params, sm);
        Class c = val.getClass();
        if (c == Integer.class)
            return Integer.valueOf(java.lang.Math.abs(((Integer) val).intValue()));
        else if (c == Float.class)
            return Float.valueOf(java.lang.Math.abs(((Float) val).floatValue()));
        else if (c == Double.class)
            return Double.valueOf(java.lang.Math.abs(((Double) val).doubleValue()));
        else if (c == Long.class)
            return Long.valueOf(java.lang.Math.abs(((Long) val).longValue()));
        throw new UnsupportedException();
    }

    private Object handleSqrtVal(Object value, Object ob, Object[] params, 
        OpenJPAStateManager sm) {
        org.apache.openjpa.jdbc.kernel.exps.Sqrt sqrtVal = 
            (org.apache.openjpa.jdbc.kernel.exps.Sqrt) value;
        Object val = getValue(sqrtVal.getValue(), ob, params, sm);
        Class c = val.getClass();
        if (c == Integer.class)
            return Double.valueOf(java.lang.Math.sqrt(((Integer) val).doubleValue()));
        else if (c == Float.class)
            return Double.valueOf(java.lang.Math.sqrt(((Float) val).floatValue()));
        else if (c == Double.class)
            return Double.valueOf(java.lang.Math.sqrt(((Double) val).doubleValue()));
        else if (c == Long.class)
            return Double.valueOf(java.lang.Math.sqrt(((Long) val).doubleValue()));
        throw new UnsupportedException();
    }    

    private Object getValue(Object value, Object ob, Object[] params,
        OpenJPAStateManager sm) {
        if (value instanceof PCPath) {
            FieldMapping fm = (FieldMapping)((PCPath) value).last();
            return getValue(ob, fm, sm);
        } else if (value instanceof Literal) {
            return ((Literal) value).getValue();
        } else if (value instanceof Constant) {
            return ((Constant) value).getValue(params);
        } else {
            return evaluate(value, ob, params, sm);
        }
    }

    private Object getValue(Object ob, FieldMapping fmd,
        OpenJPAStateManager sm) {
        int i = fmd.getIndex();
        switch (fmd.getDeclaredTypeCode()) {
        case JavaTypes.BOOLEAN:
            return sm.fetchBooleanField(i);
        case JavaTypes.BYTE:
            return sm.fetchByteField(i);
        case JavaTypes.CHAR:
            return sm.fetchCharField(i);
        case JavaTypes.DOUBLE:
            return sm.fetchDoubleField(i);
        case JavaTypes.FLOAT:
            return sm.fetchFloatField(i);
        case JavaTypes.INT:
            return sm.fetchIntField(i);
        case JavaTypes.LONG:
            return sm.fetchLongField(i);
        case JavaTypes.SHORT:
            return sm.fetchShortField(i);
        case JavaTypes.STRING:
            return sm.fetchStringField(i);
        case JavaTypes.DATE:
        case JavaTypes.NUMBER:
        case JavaTypes.BOOLEAN_OBJ:
        case JavaTypes.BYTE_OBJ:
        case JavaTypes.CHAR_OBJ:
        case JavaTypes.DOUBLE_OBJ:
        case JavaTypes.FLOAT_OBJ:
        case JavaTypes.INT_OBJ:
        case JavaTypes.LONG_OBJ:
        case JavaTypes.SHORT_OBJ:
        case JavaTypes.BIGDECIMAL:
        case JavaTypes.BIGINTEGER:
        case JavaTypes.LOCALE:
        case JavaTypes.OBJECT:
        case JavaTypes.OID:
            return sm.fetchObjectField(i);
        default:
            throw new UnsupportedException();
        }
    }

    private static class ThreadLocalContext extends ThreadLocal<Context[]> {
        public Context[] initialValue() {
          return null;
        }
    }

    public static Context[] getThreadLocalContext() {
        return localContext.get();
    }

    public static Context getThreadLocalContext(Context orig) {
        Context[] root = localContext.get();
        for (int i = 0; i < root.length; i++) {
            Context lctx = getThreadLocalContext(root[i], orig);
            if (lctx != null)
                return lctx;
        }
        return null;
    }

    public static Select getThreadLocalSelect(Select select) {
        if (select == null)
            return null;
        Context[] lctx = JDBCStoreQuery.getThreadLocalContext();
        Context cloneFrom = select.ctx();
        for (int i = 0; i < lctx.length; i++) {
            Context cloneTo = getThreadLocalContext(lctx[i], cloneFrom);
            if (cloneTo != null)
                return (Select)cloneTo.getSelect();
        }
        return select;
    }

    public static Context getThreadLocalContext(Context lctx, Context cloneFrom) {
        if (lctx.cloneFrom == cloneFrom)
            return lctx;
        java.util.List<Context> subselCtxs = lctx.getSubselContexts();
        if (subselCtxs != null) {
            for (Context subselCtx : subselCtxs) {
                Context ctx = getThreadLocalContext(subselCtx, cloneFrom);
                if (ctx != null)
                    return ctx;
            }
        }
        return null;
    }

    private static Context[] clone(Context[] orig, Context parent) {
        Context[] newCtx = new Context[orig.length];
        for (int i = 0; i < orig.length; i++) {
            newCtx[i] = clone(orig[i], parent);
        }
        return newCtx;
    }

    private static Context clone(Context orig, Context parent) {
        Context myParent = null;
        if (parent == null) {
            Context origParent = orig.getParent();
            if (origParent != null)
                myParent = clone(orig.getParent(), null);
        } else
            myParent = parent;

        Context newCtx = new Context(orig.parsed, null, myParent);
        newCtx.from = orig.from;
        newCtx.meta = orig.meta;
        newCtx.schemaAlias = orig.schemaAlias;
        newCtx.setSchemas(orig.getSchemas());
        newCtx.setVariables(orig.getVariables());
        newCtx.cloneFrom = orig;
        Object select = orig.getSelect();
        if (select != null)
            newCtx.setSelect(((SelectImpl)select).clone(newCtx));
        newCtx.subquery = orig.subquery;
        List<Context> subsels = orig.getSubselContexts();
        if (subsels != null) {
            for (Context subsel : subsels) 
                newCtx.addSubselContext(clone(subsel, newCtx));
        }

        return newCtx;        
    }
}
