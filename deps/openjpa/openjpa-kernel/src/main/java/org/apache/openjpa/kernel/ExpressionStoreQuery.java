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
package org.apache.openjpa.kernel;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.datacache.DataCache;
import org.apache.openjpa.kernel.exps.AbstractExpressionVisitor;
import org.apache.openjpa.kernel.exps.AggregateListener;
import org.apache.openjpa.kernel.exps.Constant;
import org.apache.openjpa.kernel.exps.ExpressionFactory;
import org.apache.openjpa.kernel.exps.ExpressionParser;
import org.apache.openjpa.kernel.exps.FilterListener;
import org.apache.openjpa.kernel.exps.InMemoryExpressionFactory;
import org.apache.openjpa.kernel.exps.Path;
import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.kernel.exps.Resolver;
import org.apache.openjpa.kernel.exps.StringContains;
import org.apache.openjpa.kernel.exps.Subquery;
import org.apache.openjpa.kernel.exps.Val;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.kernel.exps.WildcardMatch;
import org.apache.openjpa.lib.rop.ListResultObjectProvider;
import org.apache.openjpa.lib.rop.RangeResultObjectProvider;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.OrderedMap;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.InvalidStateException;
import org.apache.openjpa.util.UnsupportedException;
import org.apache.openjpa.util.UserException;

/**
 * Implementation of an expression-based query, which can handle
 * String-based query expressions such as JPQL and JDOQL.
 * This implementation is suitable for in-memory operation.
 * Override the following methods to also support datastore operation:
 * <ul>
 * <li>Override {@link #supportsDataStoreExecution} to return
 * <code>true</code>.</li>
 * <li>Override {@link #executeQuery}, {@link #executeDelete}, and
 * {@link #executeUpdate} to execute the query against the data store.
 * Keep in mind that the parameters passed to this method might be in use
 * by several threads in different query instances. Thus components like
 * the expression factory must either be thread safe, or this method must
 * synchronize on them.</li>
 * <li>Override {@link #getDataStoreActions} to return a representation of
 * the actions that will be taken on the data store. For use in visual
 * tools.</li>
 * <li>Override {@link #getExpressionFactory} to return a factory for creating
 * expressions in the datastore's language. The factory must be cachable.</li>
 * </ul>
 *
 * @author Abe White
 */
public class ExpressionStoreQuery
    extends AbstractStoreQuery {

    private static final Localizer _loc = Localizer.forPackage
        (ExpressionStoreQuery.class);

    // maintain support for a couple of deprecated extensions
    private static final FilterListener[] _listeners = new FilterListener[]{
        new StringContains(), new WildcardMatch(),
    };

    protected final ExpressionParser _parser;
    protected transient Object _parsed;

    /**
     * Construct a query with a parser for the language.
     */
    public ExpressionStoreQuery(ExpressionParser parser) {
        _parser = parser;
    }

    /**
     * Resolver used in parsing.
     */
    public Resolver getResolver() {
        return new Resolver() {
            public Class classForName(String name, String[] imports) {
                return ctx.classForName(name, imports);
            }

            public FilterListener getFilterListener(String tag) {
                return ctx.getFilterListener(tag);
            }

            public AggregateListener getAggregateListener(String tag) {
                return ctx.getAggregateListener(tag);
            }

            public OpenJPAConfiguration getConfiguration() {
                return ctx.getStoreContext().getConfiguration();
            }

            public QueryContext getQueryContext() {
                return ctx;
            }
        };
    }

    /**
     * Allow direct setting of parsed state for facades that do parsing.
     * The facade should call this method twice: once with the query string,
     * and again with the parsed state.
     */
    public boolean setQuery(Object query) {
        _parsed = query;
        return true;
    }

    public FilterListener getFilterListener(String tag) {
        for (int i = 0; i < _listeners.length; i++)
            if (_listeners[i].getTag().equals(tag))
                return _listeners[i];
        return null;
    }

    public Object newCompilation() {
        if (_parsed != null)
            return _parsed;
        return _parser.parse(ctx.getQueryString(), this);
    }

    public Object getCompilation() {
        return _parsed;
    }
    
    public void populateFromCompilation(Object comp) {
        _parser.populate(comp, this);
    }

    public void invalidateCompilation() {
        _parsed = null;
    }

    public boolean supportsInMemoryExecution() {
        return true;
    }

    public Executor newInMemoryExecutor(ClassMetaData meta, boolean subs) {
        return new InMemoryExecutor(this, meta, subs, _parser,  
        		ctx.getCompilation(), new InMemoryExpressionFactory());
    }

    public Executor newDataStoreExecutor(ClassMetaData meta, boolean subs) {
        return new DataStoreExecutor(this, meta, subs, _parser,
            ctx.getCompilation());
    }

    ////////////////////////
    // Methods for Override
    ////////////////////////

    /**
     * Execute the given expression against the given candidate extent.
     *
     * @param ex current executor
     * @param base the base type the query should match
     * @param types the independent candidate types
     * @param subclasses true if subclasses should be included in the results
     * @param facts the expression factory used to build the query for
     * each base type
     * @param parsed the parsed query values
     * @param params parameter values, or empty array
     * @param range result range
     * @return a provider for matching objects
     */
    protected ResultObjectProvider executeQuery(Executor ex,
        ClassMetaData base, ClassMetaData[] types, boolean subclasses,
        ExpressionFactory[] facts, QueryExpressions[] parsed, Object[] params,
        Range range) {
        throw new UnsupportedException();
    }

    /**
     * Execute the given expression against the given candidate extent
     * and delete the instances.
     *
     * @param ex current executor
     * @param base the base type the query should match
     * @param types the independent candidate types
     * @param subclasses true if subclasses should be included in the results
     * @param facts the expression factory used to build the query for
     * each base type
     * @param parsed the parsed query values
     * @param params parameter values, or empty array
     * @return a number indicating the number of instances deleted,
     * or null to execute the delete in memory
     */
    protected Number executeDelete(Executor ex, ClassMetaData base,
        ClassMetaData[] types, boolean subclasses, ExpressionFactory[] facts,
        QueryExpressions[] parsed, Object[] params) {
        return null;
    }

    /**
     * Execute the given expression against the given candidate extent
     * and updates the instances.
     *
     * @param ex current executor
     * @param base the base type the query should match
     * @param types the independent candidate types
     * @param subclasses true if subclasses should be included in the results
     * @param facts the expression factory used to build the query for
     * each base type
     * @param parsed the parsed query values
     * @param params parameter values, or empty array
     * @return a number indicating the number of instances updated,
     * or null to execute the update in memory.
     */
    protected Number executeUpdate(Executor ex, ClassMetaData base,
        ClassMetaData[] types, boolean subclasses, ExpressionFactory[] facts,
        QueryExpressions[] parsed, Object[] params) {
        return null;
    }

    /**
     * Return the commands that will be sent to the datastore in order
     * to execute the query, typically in the database's native language.
     *
     * @param base the base type the query should match
     * @param types the independent candidate types
     * @param subclasses true if subclasses should be included in the results
     * @param facts the expression factory used to build the query for
     * each base type
     * @param parsed the parsed query values
     * @param params parameter values, or empty array
     * @param range result range
     * @return a textual description of the query to execute
     */
    protected String[] getDataStoreActions(ClassMetaData base,
        ClassMetaData[] types, boolean subclasses, ExpressionFactory[] facts,
        QueryExpressions[] parsed, Object[] params, Range range) {
        return StoreQuery.EMPTY_STRINGS;
    }

    /**
     * Return the assignable types for the given metadata whose expression
     * trees must be compiled independently.
     */
    protected ClassMetaData[] getIndependentExpressionCandidates
        (ClassMetaData type, boolean subclasses) {
        return new ClassMetaData[]{ type };
    }

    /**
     * Return an {@link ExpressionFactory} to use to create an expression to
     * be executed against an extent. Each factory will be used to compile
     * one filter only. The factory must be cachable.
     */
    protected ExpressionFactory getExpressionFactory(ClassMetaData type) {
        throw new UnsupportedException();
    }

    /**
     * Provides support for queries that hold query information
     * in a {@link QueryExpressions} instance.
     *
     * @author Marc Prud'hommeaux
     */
    public static abstract class AbstractExpressionExecutor
        extends AbstractExecutor
        implements Executor {

        /**
         * Return the query expressions for one candidate type, or die if none.
         */
        private QueryExpressions assertQueryExpression() {
            QueryExpressions[] exp = getQueryExpressions();
            if (exp == null || exp.length < 1)
                throw new InvalidStateException(_loc.get("no-expressions"));

            return exp[0];
        }

        /**
         * Throw proper exception if given value is a collection/map/array.
         */
        protected void assertNotContainer(Value val, StoreQuery q) {
            // variables represent container elements, not the container itself
            if (val.isVariable())
                return;

            Class<?> type;
            if (val instanceof Path) {
                FieldMetaData fmd = ((Path) val).last();
                type = (fmd == null) ? val.getType() : fmd.getDeclaredType();
            } else
                type = val.getType();

            switch (JavaTypes.getTypeCode(type)) {
                case JavaTypes.ARRAY:
                case JavaTypes.COLLECTION:
                case JavaTypes.MAP:
                    throw new UserException(_loc.get("container-projection",
                        q.getContext().getQueryString()));
            }
        }

        public final void validate(StoreQuery q) {
            QueryExpressions exps = assertQueryExpression();    
            ValidateGroupingExpressionVisitor.validate(q.getContext(), exps); 
        }
        

        public void getRange(StoreQuery q, Object[] params, Range range) {
            QueryExpressions exps = assertQueryExpression();
            if (exps.range.length == 0)
                return;

            if (exps.range.length == 2 
                && exps.range[0] instanceof Constant
                && exps.range[1] instanceof Constant) {
                try {
                    range.start = ((Number) ((Constant) exps.range[0]).
                        getValue(params)).longValue();
                    range.end = ((Number) ((Constant) exps.range[1]).
                        getValue(params)).longValue();
                    return;
                } catch (ClassCastException cce) {
                    // fall through to exception below
                } catch (NullPointerException npe) {
                    // fall through to exception below
                }
            }
            throw new UserException(_loc.get("only-range-constants",
                q.getContext().getQueryString()));
        }

        public final Class<?> getResultClass(StoreQuery q) {
            return assertQueryExpression().resultClass;
        }
        
        public final ResultShape<?> getResultShape(StoreQuery q) {
            return assertQueryExpression().shape;
        }

        public final boolean[] getAscending(StoreQuery q) {
            return assertQueryExpression().ascending;
        }

        public final String getAlias(StoreQuery q) {
            return assertQueryExpression().alias;
        }

        public final String[] getProjectionAliases(StoreQuery q) {
            return assertQueryExpression().projectionAliases;
        }
        
        public Class<?>[] getProjectionTypes(StoreQuery q) {
            return null;
        }

        public final int getOperation(StoreQuery q) {
            return assertQueryExpression().operation;
        }

        public final boolean isAggregate(StoreQuery q) {
            return assertQueryExpression().isAggregate();
        }
        
        public final boolean isDistinct(StoreQuery q) {
            return assertQueryExpression().isDistinct();
        }

        public final boolean hasGrouping(StoreQuery q) {
            return assertQueryExpression().grouping.length > 0;
        }

        public final OrderedMap<Object,Class<?>> getOrderedParameterTypes(StoreQuery q) {
            return assertQueryExpression().parameterTypes;
        }

        /**
         * Creates a Object[] from the values of the given user parameters.
         */
        public Object[] toParameterArray(StoreQuery q, Map<?,?> userParams) {
            if (userParams == null || userParams.isEmpty())
                return StoreQuery.EMPTY_OBJECTS;

            OrderedMap<?,Class<?>> paramTypes = getOrderedParameterTypes(q);
            Object[] arr = new Object[userParams.size()];
            int base = positionalParameterBase(userParams.keySet());
            for(Entry<?, Class<?>> entry : paramTypes.entrySet()){
                Object key = entry.getKey();
                int idx = (key instanceof Integer) 
                    ? ((Integer)key).intValue() - base 
                    : paramTypes.indexOf(key);
                if (idx >= arr.length || idx < 0)
                        throw new UserException(_loc.get("gap-query-param", 
                            new Object[]{q.getContext().getQueryString(), key, 
                            userParams.size(), userParams}));
                Object value = userParams.get(key);
                validateParameterValue(key, value, (Class)entry.getValue());
                arr[idx] = value;
            }
            return arr;
        }
        
        /**
         * Return the base (generally 0 or 1) to use for positional parameters.
         */
        private static int positionalParameterBase(Collection params) {
            int low = Integer.MAX_VALUE;
            Object obj;
            int val;
            for (Iterator itr = params.iterator(); itr.hasNext();) {
                obj = itr.next();
                if (!(obj instanceof Number))
                    return 0; // use 0 base when params are mixed types

                val = ((Number) obj).intValue();
                if (val == 0)
                    return val;
                if (val < low)
                    low = val;
            }
            return low;
        }
        
        private static void validateParameterValue(Object key, Object value, 
            Class expected) {
            if (expected == null)
                return;
            
            if (value == null) {
                if (expected.isPrimitive()) 
                    throw new UserException(_loc.get("null-primitive-param", 
                        key, expected));
            } else {
                Class actual = value.getClass();
                boolean strict = true;
                if (!Filters.canConvert(actual, expected, strict)) 
                    throw new UserException(_loc.get("param-value-mismatch", 
                        new Object[]{key, expected, value, actual}));
            }
        }
        
        public final Map getUpdates(StoreQuery q) {
            return assertQueryExpression().updates;
        }

        public final ClassMetaData[] getAccessPathMetaDatas(StoreQuery q) {
            QueryExpressions[] exps = getQueryExpressions();
            if (exps.length == 1)
                return exps[0].accessPath;

            List<ClassMetaData> metas = null;
            for (int i = 0; i < exps.length; i++)
                metas = Filters.addAccessPathMetaDatas(metas,
                    exps[i].accessPath);
            if (metas == null)
                return StoreQuery.EMPTY_METAS;
            return (ClassMetaData[]) metas.toArray
                (new ClassMetaData[metas.size()]);
        }

        public boolean isPacking(StoreQuery q) {
            return false;
        }
        
        /**
         * Throws an exception if select or having clauses contain 
         * non-aggregate, non-grouped paths.
         */
        private static class ValidateGroupingExpressionVisitor 
            extends AbstractExpressionVisitor {

            private final QueryContext _ctx;
            private boolean _grouping = false;
            private Set _grouped = null;
            private Value _agg = null;

            /**
             * Throw proper exception if query does not meet validation.
             */
            public static void validate(QueryContext ctx, 
                QueryExpressions exps) {
                if (exps.grouping.length == 0)
                    return;

                ValidateGroupingExpressionVisitor visitor = 
                    new ValidateGroupingExpressionVisitor(ctx);
                visitor._grouping = true;
                for (int i = 0; i < exps.grouping.length; i++)
                    exps.grouping[i].acceptVisit(visitor);
                visitor._grouping = false;
                if (exps.having != null) {
                    Class cls = exps.having.getClass();
                    if (cls.getName().endsWith("Expression"))
                        cls = cls.getSuperclass();
                    Object value2 = null;
                    Method getValue2 = null;
                    try {
                        getValue2 = cls.getMethod("getValue2");
                        getValue2.setAccessible(true);
                        value2 = getValue2.invoke(exps.having, (Object[]) null);
                    } catch (NoSuchMethodException name) {
                        // skip
                    } catch (IllegalAccessException iae) {
                        // skip
                    } catch (InvocationTargetException ite) {
                        // skip
                    } 
                    if (value2 != null && value2 instanceof Subquery)
                        ;  // complex having with subquery, validation is performed by DBMS
                    else
                        exps.having.acceptVisit(visitor);
                }
                for (int i = 0; i < exps.projections.length; i++)
                    exps.projections[i].acceptVisit(visitor);
            }

            public ValidateGroupingExpressionVisitor(QueryContext ctx) {
                _ctx = ctx;
            }

            public void enter(Value val) {
                if (_grouping) {
                    if (val instanceof Path) {
                        if (_grouped == null)
                            _grouped = new HashSet();
                        _grouped.add(val);
                    }
                } else if (_agg == null) {
                    if (val.isAggregate()) 
                        _agg = val;
                    else if (val instanceof Path 
                        && (_grouped == null || !_grouped.contains(val))) {
                        throw new UserException(_loc.get("bad-grouping",
                            _ctx.getCandidateType(), _ctx.getQueryString())); 
                    }
                }
            }

            public void exit(Value val) {
                if (val == _agg)
                    _agg = null;
            }
        }
    }

    /**
     * Runs the expression query in memory.
     */
    public static class InMemoryExecutor
        extends AbstractExpressionExecutor
        implements Executor, Serializable {

        private final ClassMetaData _meta;
        private final boolean _subs;
        private final InMemoryExpressionFactory _factory;
        private final QueryExpressions[] _exps;
        private final Class[] _projTypes;

        public InMemoryExecutor(ExpressionStoreQuery q,
            ClassMetaData candidate, boolean subclasses,
            ExpressionParser parser, Object parsed, InMemoryExpressionFactory factory) {
            _meta = candidate;
            _subs = subclasses;
            _factory = factory;

            _exps = new QueryExpressions[] {
                parser.eval(parsed, q, _factory, _meta)
            };
            if (_exps[0].projections.length == 0)
                _projTypes = StoreQuery.EMPTY_CLASSES;
            else {
                AssertNoVariablesExpressionVisitor novars = new
                    AssertNoVariablesExpressionVisitor(q.getContext());
                _projTypes = new Class[_exps[0].projections.length];
                for (int i = 0; i < _exps[0].projections.length; i++) {
                    _projTypes[i] = _exps[0].projections[i].getType();
                    assertNotContainer(_exps[0].projections[i], q);
                    _exps[0].projections[i].acceptVisit(novars);
                }
                for (int i = 0; i < _exps[0].grouping.length; i++)
                    _exps[0].grouping[i].acceptVisit(novars);
            }
        }

        public QueryExpressions[] getQueryExpressions() {
            return _exps;
        }

        public ResultObjectProvider executeQuery(StoreQuery q,
            Object[] params, Range range) {
            // execute in memory for candidate collection;
            // also execute in memory for transactional extents
            Collection coll = q.getContext().getCandidateCollection();
            Iterator itr;
            if (coll != null)
                itr = coll.iterator();
            else
                itr = q.getContext().getStoreContext().
                    extentIterator(_meta.getDescribedType(), _subs,
                        q.getContext().getFetchConfiguration(),
                        q.getContext().getIgnoreChanges());

            // find matching objects
            List results = new ArrayList();
            StoreContext ctx = q.getContext().getStoreContext();
            try {
                Object obj;
                while (itr.hasNext()) {
                    obj = itr.next();
                    if (_factory.matches(_exps[0], _meta, _subs, obj, ctx,
                        params))
                        results.add(obj);
                }
            }
            finally {
                ImplHelper.close(itr);
            }

            // group results
            results = _factory.group(_exps[0], results, ctx, params);

            // apply having to filter groups
            if (_exps[0].having != null) {
                List matches = new ArrayList(results.size());
                Collection c;
                itr = results.iterator();
                while (itr.hasNext()) {
                    c = (Collection) itr.next();
                    if (_factory.matches(_exps[0], c, ctx, params))
                        matches.add(c);
                }
                results = matches;
            }

            // apply projections, order results, and filter duplicates
            results = _factory.project(_exps[0], results, ctx, params);
            results = _factory.order(_exps[0], results, ctx, params);
            results = _factory.distinct(_exps[0], coll == null, results);

            ResultObjectProvider rop = new ListResultObjectProvider(results);
            if (range.start != 0 || range.end != Long.MAX_VALUE)
                rop = new RangeResultObjectProvider(rop, range.start,range.end);
            return rop;
        }

        public String[] getDataStoreActions(StoreQuery q, Object[] params,
            Range range) {
            // in memory queries have no datastore actions to perform
            return StoreQuery.EMPTY_STRINGS;
        }

        public Object getOrderingValue(StoreQuery q, Object[] params,
            Object resultObject, int orderIndex) {
            // if this is a projection, then we have to order on something
            // we selected
            if (_exps[0].projections.length > 0) {
                String ordering = _exps[0].orderingClauses[orderIndex];
                for (int i = 0; i < _exps[0].projectionClauses.length; i++)
                    if (ordering.equals(_exps[0].projectionClauses[i]))
                        return ((Object[]) resultObject)[i];

                throw new InvalidStateException(_loc.get
                    ("merged-order-with-result", q.getContext().getLanguage(),
                        q.getContext().getQueryString(), ordering));
            }

            // use the parsed ordering expression to extract the ordering value
            Val val = (Val) _exps[0].ordering[orderIndex];
            return val.evaluate(resultObject, resultObject, q.getContext().
                getStoreContext(), params);
        }

        public Class[] getProjectionTypes(StoreQuery q) {
            return _projTypes;
        }

        /**
         * Throws an exception if a variable is found.
         */
        private static class AssertNoVariablesExpressionVisitor 
            extends AbstractExpressionVisitor {

            private final QueryContext _ctx;

            public AssertNoVariablesExpressionVisitor(QueryContext ctx) {
                _ctx = ctx;
            }

            public void enter(Value val) {
                if (!val.isVariable())
                    return;
                throw new UnsupportedException(_loc.get("inmem-agg-proj-var", 
                    _ctx.getCandidateType(), _ctx.getQueryString()));
            }
        }
    }

    /**
     * The DataStoreExecutor executes the query against the
     * implementation's overridden {@link #executeQuery} method.
     *
     * @author Marc Prud'hommeaux
     */
    public static class DataStoreExecutor
        extends AbstractExpressionExecutor
        implements Executor, Serializable {

        private ClassMetaData _meta;
        private ClassMetaData[] _metas;
        private boolean _subs;
        private ExpressionParser _parser;
        private ExpressionFactory[] _facts;
        private QueryExpressions[] _exps;
        private Class[] _projTypes;
        private Value[] _inMemOrdering;

        public DataStoreExecutor(ExpressionStoreQuery q,
            ClassMetaData meta, boolean subclasses,
            ExpressionParser parser, Object parsed) {
            _metas = q.getIndependentExpressionCandidates(meta, subclasses);
            if (_metas.length == 0)
                throw new UserException(_loc.get("query-unmapped", meta));
            _meta = meta;
            _subs = subclasses;
            _parser = parser;

            _facts = new ExpressionFactory[_metas.length];
            for (int i = 0; i < _facts.length; i++)
                _facts[i] = q.getExpressionFactory(_metas[i]);

            _exps = new QueryExpressions[_metas.length];
            for (int i = 0; i < _exps.length; i++)
                _exps[i] = parser.eval(parsed, q, _facts[i], _metas[i]);

            if (_exps[0].projections.length == 0)
                _projTypes = StoreQuery.EMPTY_CLASSES;
            else {
                _projTypes = new Class[_exps[0].projections.length];
                for (int i = 0; i < _exps[0].projections.length; i++) {
                    assertNotContainer(_exps[0].projections[i], q);
                    _projTypes[i] = _exps[0].projections[i].getType();
                }
            }
        }

        public QueryExpressions[] getQueryExpressions() {
            return _exps;
        }

        public ResultObjectProvider executeQuery(StoreQuery q,
            Object[] params, Range range) {
            range.lrs &= !isAggregate(q) && !hasGrouping(q);
            return ((ExpressionStoreQuery) q).executeQuery(this, _meta, _metas,
                _subs, _facts, _exps, params, range);
        }

        public Number executeDelete(StoreQuery q, Object[] params) {
            try {
                Number num =
                    ((ExpressionStoreQuery) q).executeDelete(this, _meta, _metas, _subs, _facts, _exps, params);
                if (num == null)
                    return q.getContext().deleteInMemory(q, this, params);
                return num;
            } finally {
                for (ClassMetaData cmd : getAccessPathMetaDatas(q)) {
                    DataCache cache = cmd.getDataCache();
                    if (cache != null && cache.getEvictOnBulkUpdate()) {
                        cache.removeAll(cmd.getDescribedType(), true);
                    }
                }
            }
        }

        public Number executeUpdate(StoreQuery q, Object[] params) {
            try {
                Number num =
                    ((ExpressionStoreQuery) q).executeUpdate(this, _meta, _metas, _subs, _facts, _exps, params);
                if (num == null)
                    return q.getContext().updateInMemory(q, this, params);
                return num;
            } finally {
                for (ClassMetaData cmd : getAccessPathMetaDatas(q)) {
                    DataCache cache = cmd.getDataCache();
                    if (cache != null && cache.getEvictOnBulkUpdate()) {
                        cache.removeAll(cmd.getDescribedType(), true);
                    }
                }
            }
        }

        public String[] getDataStoreActions(StoreQuery q, Object[] params,
            Range range) {
            return ((ExpressionStoreQuery) q).getDataStoreActions(_meta,
                _metas, _subs, _facts, _exps, params, range);
        }

        public Object getOrderingValue(StoreQuery q, Object[] params,
            Object resultObject, int orderIndex) {
            // if this is a projection, then we have to order on something
            // we selected
            if (_exps[0].projections.length > 0) {
                String ordering = _exps[0].orderingClauses[orderIndex];
                for (int i = 0; i < _exps[0].projectionClauses.length; i++)
                    if (ordering.equals(_exps[0].projectionClauses[i]))
                        return ((Object[]) resultObject)[i];

                throw new InvalidStateException(_loc.get
                    ("merged-order-with-result", q.getContext().getLanguage(),
                        q.getContext().getQueryString(), ordering));
            }

            // need to parse orderings?
            synchronized (this) {
                if (_inMemOrdering == null) {
                    ExpressionFactory factory = new InMemoryExpressionFactory();
                    _inMemOrdering = _parser.eval(_exps[0].orderingClauses,
                        (ExpressionStoreQuery) q, factory, _meta);
                }
                if (_inMemOrdering == null)
                    _inMemOrdering = _exps[0].ordering;
            }

            // use the parsed ordering expression to extract the ordering value
            Val val = (Val) _inMemOrdering[orderIndex];
            return val.evaluate(resultObject, resultObject,
                q.getContext().getStoreContext(), params);
        }

        public Class[] getProjectionTypes(StoreQuery q) {
            return _projTypes;
		}
	}
}
