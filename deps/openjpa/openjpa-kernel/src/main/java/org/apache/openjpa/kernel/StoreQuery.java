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
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.openjpa.kernel.exps.AggregateListener;
import org.apache.openjpa.kernel.exps.Constant;
import org.apache.openjpa.kernel.exps.FilterListener;
import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.lib.util.OrderedMap;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;

/**
 * Component that executes queries against the datastore. For
 * expression-based queries, consider subclassing
 * {@link ExpressionStoreManagerQuery}.
 *
 * @author Abe White
 * @since 0.4.0
 */
public interface StoreQuery
    extends QueryOperations, Serializable {

    // linkedmap doesn't allow a size of 0, so use 1
    // This is deprecated
    public static final LinkedMap EMPTY_PARAMS = new LinkedMap(1);
    public static final OrderedMap<Object, Class<?>> EMPTY_ORDERED_PARAMS = new OrderedMap<Object, Class<?>>();
    public static final ClassMetaData[] EMPTY_METAS = new ClassMetaData[0];
    public static final String[] EMPTY_STRINGS = new String[0];
    public static final Object[] EMPTY_OBJECTS = new Object[0];
    public static final Class<?>[] EMPTY_CLASSES = new Class[0];
    public static final boolean[] EMPTY_BOOLEANS = new boolean[0];

    /**
     * Return the query context that has been set.
     */
    public QueryContext getContext();

    /**
     * Set the current query context. This will be called before use.
     */
    public void setContext(QueryContext ctx);

    /**
     * This is invoked when the user or a facade creates a new query with
     * an object that the system does not recognize. Return true if
     * the object is recognized by the store, false otherwise.
     */
    public boolean setQuery(Object query);

    /**
     * Return the standard filter listener for the given tag, or null.
     */
    public FilterListener getFilterListener(String tag);

    /**
     * Return the standard filter listener for the given tag, or null.
     */
    public AggregateListener getAggregateListener(String tag);

    /**
     * Create a new key for caching compiled query information. May be null.
     */
    public Object newCompilationKey();

    /**
     * Create a new compilation for this query. May be null.
     */
    public Object newCompilation();
    
    /**
     * Get the current compilation for this query. If it is null, do not create. 
     */
    public Object getCompilation();

    /**
     * Populate internal data from compilation.
     */
    public void populateFromCompilation(Object comp);

    /**
     * Invalidate any internal compilation state.
     */
    public void invalidateCompilation();

    /**
     * True if this query supports datastore execution, false if it
     * can only run in memory.
     */
    public boolean supportsDataStoreExecution();

    /**
     * True if this query supports in-memory execution, false if it
     * can only run against the datastore.
     */
    public boolean supportsInMemoryExecution();

    /**
     * Return an executor for in-memory execution of this query.
     * Executors must be cachable and thread safe. If this class returns
     * true from {@link #supportsAbstractExecutors}, the given metadata
     * will always be for the candidate class of this query, or possibly
     * null if the candidate class is not itself persistence capable (like
     * an interface or abstract base class). Otherwise, the given type will
     * be a mapped class.
     *
     * @param subs whether to include dependent mapped subclasses in the
     * results; independent subclasses should never be included
     */
    public Executor newInMemoryExecutor(ClassMetaData meta, boolean subs);

    /**
     * Return an executor for datastore execution of this query.
     * Executors must be cachable and thread safe. If this class returns
     * true from {@link #supportsAbstractExecutors}, the given metadata
     * will always be for the candidate class of this query, or possibly
     * null if the candidate class is not itself persistence capable (like
     * an interface or abstract base class). Otherwise, the given type will
     * be a mapped class.
     *
     * @param subs whether to include dependent mapped subclasses in the
     * results; independent subclasses should never be included
     */
    public Executor newDataStoreExecutor(ClassMetaData meta, boolean subs);

    /**
     * Return true if this query supports execution against abstract or
     * interface types. Returns false by default, meaning we will only
     * request executors for persistent classes. In this case, we will
     * automatically combine the results of the executors for all
     * implementing classes if we execute a query for an interface for
     * abstract type.
     */
    public boolean supportsAbstractExecutors();

    /**
     * Whether this query requires a candidate class.
     */
    public boolean requiresCandidateType();

    /**
     * Whether this query requires parameters to be declared.
     */
    public boolean requiresParameterDeclarations();

    /**
     * Whether this query supports declared parameters.
     */
    public boolean supportsParameterDeclarations();

    public Object evaluate(Object value, Object ob, Object[] params, 
        OpenJPAStateManager sm);

    /**
     * A query result range.
     */
    public static class Range {
        public long start = 0L;
        public long end = Long.MAX_VALUE;
        public boolean lrs = false;

        public Range() {
        }

        public Range(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }

    /**
     * An executor provides a uniform interface to the mechanism for executing
     * either an in-memory or datastore query. In the common case, the
     * {@link #executeQuery} method will be called before other methods,
     * though this is not guaranteed.
     *
     * @author Marc Prud'hommeaux
     */
    public static interface Executor {

        /**
         * Return the result of executing this query with the given parameter
         * values. If this query is a projection and this executor does not
         * pack results itself, each element of the returned result object
         * provider should be an object array containing the projection values.
         *
         * @param lrs true if the query result should be treated as a
         * large result set, assuming the query is not an
         * aggregate and does not have grouping
         * @see #isPacking
         */
        public ResultObjectProvider executeQuery(StoreQuery q, Object[] params,
            Range range);

        /**
         * Deleted the objects that result from the execution of the
         * query, retuning the number of objects that were deleted.
         */
        public Number executeDelete(StoreQuery q, Object[] params);

        /**
         * Updates the objects that result from the execution of the
         * query, retuning the number of objects that were updated.
         */
        public Number executeUpdate(StoreQuery q, Object[] params);

        /**
         * Return a description of the commands that will be sent to
         * the datastore in order to execute the query.
         */
        public String[] getDataStoreActions(StoreQuery q, Object[] params,
            Range range);

        /**
         * Validate components of query.
         */
        public void validate(StoreQuery q);

        /**
         * Mutate the given range to set any range information stored in 
         * the query string and/or parameters.
         */
        public void getRange(StoreQuery q, Object[] params, Range range);

        /**
         * Extract the value of the <code>orderIndex</code>th ordering
         * expression in {@link Query#getOrderingClauses} from the
         * given result object. The result object will be an object from
         * the result object provider returned from {@link #executeQuery}.
         * This method is used when several result lists have to be merged
         * in memory. If this exeuctor's parent query supports executors on
         * abstract or interface classes, this method will not be used.
         *
         * @see StoreQuery#supportsAbstractExecutors
         */
        public Object getOrderingValue(StoreQuery q, Object[] params,
            Object resultObject, int orderIndex);

        /**
         * Return the ordering direction for all ordering clauses, or empty
         * array if none.
         */
        public boolean[] getAscending(StoreQuery q);

        /**
         * Return true if this executor packs projections into the result
         * class itself. Executors for query languages that allow projections
         * without result clauses must return true and perform the result
         * packing themselves.
         */
        public boolean isPacking(StoreQuery q);

        /**
         * If this is not a projection but the candidate results are placed
         * into a result class with an alias, return that alias.
         */
        public String getAlias(StoreQuery q);

        /**
         * Return the alias for each projection element, or empty array
         * if not a projection.
         */
        public String[] getProjectionAliases(StoreQuery q);

        /**
         * Return the expected types of the projections used by this query,
         * or an empty array if not a projection.
         */
        public Class<?>[] getProjectionTypes(StoreQuery q);

        /**
         * Return an array of all persistent classes used in this query, or
         * empty array if unknown.
         */
        public ClassMetaData[] getAccessPathMetaDatas(StoreQuery q);

        /**
         * Returns the operation this executor is meant to execute.
         *
         * @see QueryOperations
         */
        public int getOperation(StoreQuery q);

        /**
         * Return true if the compiled query is an aggregate.
         */
        public boolean isAggregate(StoreQuery q);
        
        public boolean isDistinct(StoreQuery q);

        /**
         * Whether the compiled query has grouping.
         */
        public boolean hasGrouping(StoreQuery q);

        /**
         * Return a map of parameter names to types. The returned
         * {@link Map#entrySet}'s {@link Iterator} must return values in the
         * order in which they were declared or used.
         */
        public OrderedMap<Object, Class<?>> getOrderedParameterTypes(StoreQuery q);
        
        /**
         * Return a map of parameter names to types. The returned
         * {@link Map#entrySet}'s {@link Iterator} must return values in the
         * order in which they were declared or used.<br>
         * 
         * <B>Warning</B>: Deprecated. Use {@linkplain #getOrderedParameterTypes(StoreQuery)} instead.
         */
        @Deprecated
        public LinkedMap getParameterTypes(StoreQuery q);
        
        /**
         * Return an array from the given user parameter values.
         * The array ordering is same as what this executor expects for its 
         * executeXXX() methods as its Object[] parameters.
         * If the given userParams is null or empty return an empty array
         * rather than null.
         * 
         * @return array with parameter values ordered in the same way as this
         * receiver's executeXXX() method expects.
         * 
         * @since 2.0.0
         */
        public Object[] toParameterArray(StoreQuery q, Map<?,?> userParams);

        /**
         * Returns the result class, if any.
         */
        public Class<?> getResultClass(StoreQuery q);
        
        public ResultShape<?> getResultShape(StoreQuery q);

        /**
         * Return a map of {@link FieldMetaData} to update
         * {@link Constant}s, in cases where this query is for a bulk update.
	 	 */
		public Map<FieldMetaData,Value> getUpdates (StoreQuery q);
		
        /**
         * Return the parsed query expressions for our candidate types.
         * The expressions are available only after query has been parsed.
         * 
         * @since 2.0.0
         */
		public QueryExpressions[] getQueryExpressions();
	}
}
