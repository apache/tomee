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
package org.apache.openjpa.jdbc.kernel.exps;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.openjpa.jdbc.kernel.JDBCStoreQuery;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.exps.AbstractExpressionVisitor;
import org.apache.openjpa.kernel.exps.Constant;
import org.apache.openjpa.kernel.exps.Context;
import org.apache.openjpa.kernel.exps.Expression;
import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.kernel.exps.Subquery;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UnsupportedException;

/**
 * Turns parsed queries into selects.
 *
 * @author Abe White
 * @nojavadoc
 */
public class SelectConstructor
    implements Serializable {

    private boolean _extent = false;
    private Select _subselect = null;
    private static final Localizer _loc = Localizer.forPackage(SelectConstructor.class);

    /**
     * Return true if we know the select to have on criteria; to be an extent.
     * Note that even if this method returns false, {@link #evaluate} may still
     * return null if we haven't cached whether the query is an extent yet.
     */
    public boolean isExtent() {
        return _extent;
    }

    public void setSubselect(Select subselect) {
        _subselect = subselect;
    }

    /**
     * Evaluate the expression, returning a new select and filling in any
     * associated expression state. Use {@link #select} to then select the data.
     * 
     * @param ctx fill with execution context
     * @param state will be filled with expression state
     */
    public Select evaluate(ExpContext ctx, Select parent, String alias, 
        QueryExpressions exps, QueryExpressionsState state) {
        // already know that this query is equivalent to an extent?
        Select sel;
        if (_extent) {
            sel = ctx.store.getSQLFactory().newSelect();
            sel.setAutoDistinct((exps.distinct & exps.DISTINCT_AUTO) != 0);
            return sel;
        }

        // create a new select and initialize it with the joins needed for
        // the criteria of this query
        sel = newSelect(ctx, parent, alias, exps, state);
        sel.setTablePerClassMeta(ctx.tpcMeta);

        // create where clause; if there are no where conditions and
        // no ordering or projections, we return null to signify that this
        // query should be treated like an extent
        Select inner = sel.getFromSelect();
        SQLBuffer where = buildWhere((inner != null) ? inner : sel, ctx, 
            state.filter, exps.filter);
        if (where == null && exps.projections.length == 0
            && exps.ordering.length == 0
            && (sel.getJoins() == null || sel.getJoins().isEmpty())) {
            _extent = true;
            sel.setAutoDistinct((exps.distinct & exps.DISTINCT_AUTO) != 0);
            return sel;
        }

        // now set sql criteria; it goes on the inner select if present
        if (inner != null)
            inner.where(where);
        else
            sel.where(where);

        // apply grouping and having.  this does not select the grouping
        // columns, just builds the GROUP BY clauses.  we don't build the
        // ORDER BY clauses yet because if we decide to add this select
        // to a union, the ORDER BY values get aliased differently
        if (exps.having != null) {
            Exp havingExp = (Exp) exps.having;
            SQLBuffer buf = new SQLBuffer(ctx.store.getDBDictionary());
            havingExp.appendTo(sel, ctx, state.having, buf);
            sel.having(buf);
        }
        for (int i = 0; i < exps.grouping.length; i++)
            ((Val) exps.grouping[i]).groupBy(sel, ctx, state.grouping[i]);
        
        if (exps.projections.length == 1) {
            Val val = (Val) exps.projections[0];
            if (val instanceof Count && ((Count)val).isCountDistinctMultiCols()) {
                Select newSel = ctx.store.getSQLFactory().newSelect();
                newSel.select("COUNT(*)", val);
                newSel.setExpectedResultCount(1, true);
                newSel.setFromSelect(sel);
                sel.setExpectedResultCount(0, true);
                sel = newSel;
            }
        }        
        return sel;
    }

    /**
     * Return a new select with expressions initialized.
     */
    private Select newSelect(ExpContext ctx, Select parent,
        String alias, QueryExpressions exps, QueryExpressionsState state) {
        Select subselect = JDBCStoreQuery.getThreadLocalSelect(_subselect);
        Select sel = parent != null ? subselect
            : ctx.store.getSQLFactory().newSelect();
        sel.setAutoDistinct((exps.distinct & exps.DISTINCT_AUTO) != 0);
        sel.setJoinSyntax(ctx.fetch.getJoinSyntax());
        sel.setParent(parent, alias);

        Context[] qryCtx = JDBCStoreQuery.getThreadLocalContext();
        Context lctx = null;
        for (int i = 0; i < qryCtx.length; i++) {
            if (qryCtx[i].cloneFrom == exps.ctx()) {
                lctx = qryCtx[i];
                break;
            }
        }

        if (sel.ctx() == null)
            sel.setContext(lctx);

        if (parent == null && lctx.getSubselContexts() != null) {
            // this is the case subselect was created before parent got created
            List<Context> subselCtxs = lctx.getSubselContexts();
            for (Context subselCtx : subselCtxs) {
                Select subsel = (Select) subselCtx.getSelect();
                Subquery subquery = subselCtx.getSubquery();
                subsel.setParent(sel, subquery.getCandidateAlias());
            }
        }
        if (HasContainsExpressionVisitor.hasContains(exps.filter)) {
            sel.setHasSubselect(true);
        }
        initialize(sel, ctx, exps, state);

        if (!sel.getAutoDistinct()) {
            if ((exps.distinct & exps.DISTINCT_TRUE) != 0)
                sel.setDistinct(true);
            else if ((exps.distinct & exps.DISTINCT_FALSE) != 0)
                sel.setDistinct(false);
        } else if (exps.projections.length > 0) {
            if (!sel.isDistinct() && (exps.distinct & exps.DISTINCT_TRUE) != 0){
                // if the select is not distinct but the query is, force
                // the select to be distinct
                sel.setDistinct(true);
            } else if (sel.isDistinct()) {
                // when aggregating data or making a non-distinct projection
                // from a distinct select, we have to select from a tmp
                // table formed by a distinct subselect in the from clause;
                // this subselect selects the pks of the candidate (to
                // get unique candidate values) and needed field values and
                // applies the where conditions; the outer select applies
                // ordering, grouping, etc
                boolean agg = exps.isAggregate();
                boolean candidate = ProjectionExpressionVisitor.
                    hasCandidateProjections(exps.projections);
                if (agg || (candidate 
                    && (exps.distinct & exps.DISTINCT_TRUE) == 0)) {
                    DBDictionary dict = ctx.store.getDBDictionary();
                    dict.assertSupport(dict.supportsSubselect,
                        "SupportsSubselect");

                    Select inner = sel;
                    sel = ctx.store.getSQLFactory().newSelect();
                    sel.setParent(parent, alias);
                    sel.setDistinct(agg
                        && (exps.distinct & exps.DISTINCT_TRUE) != 0);
                    sel.setFromSelect(inner);

                // auto-distincting happens to get unique candidate instances
                // back; don't auto-distinct if the user isn't selecting 
                // candidate data
                } else if (!candidate 
                    && (exps.distinct & exps.DISTINCT_TRUE) == 0) 
                    sel.setDistinct(false);
            }
        }
        return sel;
    }

    /**
     * Initialize all expressions.
     */
    private void initialize(Select sel, ExpContext ctx, QueryExpressions exps, 
        QueryExpressionsState state) {
        Map contains = null;
        if (HasContainsExpressionVisitor.hasContains(exps.filter)
            || HasContainsExpressionVisitor.hasContains(exps.having))
            contains = new HashMap(7);

        // initialize filter and having expressions
        Exp filterExp = (Exp) exps.filter;
        state.filter = filterExp.initialize(sel, ctx, contains);
        Exp havingExp = (Exp) exps.having;
        if (havingExp != null)
            state.having = havingExp.initialize(sel, ctx, contains);

        // get the top-level joins and null the expression's joins
        // at the same time so they aren't included in the where/having SQL
        Joins filterJoins = state.filter.joins;
        Joins havingJoins = (state.having == null) ? null : state.having.joins;
        Joins joins = sel.and(filterJoins, havingJoins);

        // initialize result values
        if (exps.projections.length > 0) {
            state.projections = new ExpState[exps.projections.length];
            Val resultVal;
            for (int i = 0; i < exps.projections.length; i++) {
                resultVal = (Val) exps.projections[i];
                if (!ctx.store.getDBDictionary().supportsParameterInSelect && resultVal instanceof Lit) {
                    ((Lit)resultVal).setRaw(true);
                }
                // have to join through to related type for pc object 
                // projections; this ensures that we have all our joins cached
                state.projections[i] = resultVal.initialize(sel, ctx, 
                    Val.JOIN_REL | Val.FORCE_OUTER);
                if (exps.projections.length > 1 && resultVal instanceof Count) {
                    if (((Count)resultVal).isCountDistinctMultiCols())
                        throw new UnsupportedException(_loc.get("count-distinct-multi-col-only"));
                }
                joins = sel.and(joins, state.projections[i].joins);
            }
        }

        // initialize grouping
        if (exps.grouping.length > 0) {
            state.grouping = new ExpState[exps.grouping.length];
            Val groupVal;
            for (int i = 0; i < exps.grouping.length; i++) {
                groupVal = (Val) exps.grouping[i];
                // have to join through to related type for pc object groupings;
                // this ensures that we have all our joins cached
                state.grouping[i] = groupVal.initialize(sel, ctx, Val.JOIN_REL);
                joins = sel.and(joins, state.grouping[i].joins);
            }
        }

        // initialize ordering
        if (exps.ordering.length > 0) {
            state.ordering = new ExpState[exps.ordering.length];
            Val orderVal;
            for (int i = 0; i < exps.ordering.length; i++) {
                orderVal = (Val) exps.ordering[i];
                if (contains(orderVal, exps.grouping)) 
                    state.ordering[i] = orderVal.initialize(sel, ctx, Val.JOIN_REL);
                else
                    state.ordering[i] = orderVal.initialize(sel, ctx, 0);
                    
                joins = sel.and(joins, state.ordering[i].joins);
            }
        }
        sel.where(joins);
    }
    
    private boolean contains(Val orderVal, Value[] grouping) {
        for (int i = 0; i < grouping.length; i++) {
            Val groupVal = (Val) grouping[i];
            if (orderVal.equals(groupVal))
                return true;
        }
        return false;
    }

    /**
     * Create the where sql.
     */
    private SQLBuffer buildWhere(Select sel, ExpContext ctx, ExpState state, 
        Expression filter) {
        // create where buffer
        SQLBuffer where = new SQLBuffer(ctx.store.getDBDictionary());
        where.append("(");
        Exp filterExp = (Exp) filter;
        filterExp.appendTo(sel, ctx, state, where);

        if (where.sqlEquals("(") || where.sqlEquals("(1 = 1"))
            return null;
        return where.append(")");
    }

    /**
     * Select the data for this query.
     */
    public void select(Select sel, ExpContext ctx, ClassMapping mapping,
        boolean subclasses, QueryExpressions exps, QueryExpressionsState state,
        int eager) {
        Select inner = sel.getFromSelect();
        Val val;
        Joins joins = null;
        boolean isCountDistinctMultiCols = false;

        if (sel.getSubselectPath() != null)
            joins = sel.newJoins().setSubselect(sel.getSubselectPath());

        // build ordering clauses before select so that any eager join
        // ordering gets applied after query ordering
        for (int i = 0; i < exps.ordering.length; i++)
            ((Val) exps.ordering[i]).orderBy(sel, ctx, state.ordering[i],
                exps.ascending[i]);

        // if no result string set, select matching objects like normal
        if (exps.projections.length == 0 && sel.getParent() == null) {
            int subs = (subclasses) ? Select.SUBS_JOINABLE : Select.SUBS_NONE;
            sel.selectIdentifier(mapping, subs, ctx.store, ctx.fetch, eager);
        } else if (exps.projections.length == 0) {
            // subselect for objects; we really just need the primary key values
            sel.select(mapping.getPrimaryKeyColumns(), joins);
        } else {
            if (exps.projections.length == 1) {
                val = (Val) exps.projections[0];
                if (val instanceof Count && ((Count)val).isCountDistinctMultiCols()) {
                    isCountDistinctMultiCols = true;
                    if (sel.getParent() != null)
                        throw new UnsupportedException(_loc.get("count-distinct-multi-col-subselect-unsupported"));
                }
            }            

            // if we have an inner select, we need to select the candidate
            // class' pk columns to guarantee unique instances
            if (inner != null && !isCountDistinctMultiCols)
                inner.select(mapping.getPrimaryKeyColumns(), joins);

            // select each result value; no need to pass on the eager mode since
            // under projections we always use EAGER_NONE
            boolean pks = sel.getParent() != null;
            for (int i = 0; i < exps.projections.length; i++) {
                val = (Val) exps.projections[i];
                if (inner != null) {
                    if (!isCountDistinctMultiCols)
                        val.selectColumns(inner, ctx, state.projections[i], pks);
                    else
                        val.select(inner, ctx, state.projections[i], pks);
                } else
                    val.select(sel, ctx, state.projections[i], pks);
            }

            // make sure having columns are selected since it is required by 
            // some DBs.  put them last so they don't affect result processing
            if (exps.having != null && inner != null)
                ((Exp) exps.having).selectColumns(inner, ctx, state.having, 
                    true);
        }

        // select ordering columns, since it is required by some DBs.  put them
        // last so they don't affect result processing
        for (int i = 0; i < exps.ordering.length; i++) {
            val = (Val) exps.ordering[i];
            if (inner != null)
                val.selectColumns(inner, ctx, state.ordering[i], true);
            val.select(sel, ctx, state.ordering[i], true);
        }

        // add conditions limiting the projections to the proper classes; if
        // this isn't a projection or a subq then they will already be added
        if (exps.projections.length > 0 || sel.getParent() != null) {
            ctx.store.loadSubclasses(mapping);
            mapping.getDiscriminator().addClassConditions((inner != null) 
                ? inner : sel, subclasses, joins);
        }
    }

    /**
     * Used to check whether a query's result projections are on the candidate.
     */
    private static class ProjectionExpressionVisitor
        extends AbstractExpressionVisitor {

        private boolean _candidate = false;
        private int _level = 0;

        public static boolean hasCandidateProjections(Value[] projs) {
            ProjectionExpressionVisitor v = new ProjectionExpressionVisitor();
            for (int i = 0; i < projs.length; i++) {
                projs[i].acceptVisit(v);
                if (v._candidate)
                    return true;
            }
            return false;
        }

        public void enter(Value val) {
            if (!_candidate) {
                _candidate = (_level == 0 && val instanceof Constant)
                    || (val instanceof PCPath 
                    && !((PCPath) val).isVariablePath());
            }
            _level++;
        }

        public void exit(Value val) {
            _level--;
        }
    }
}
