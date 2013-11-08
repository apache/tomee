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

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.openjpa.kernel.exps.AggregateListener;
import org.apache.openjpa.kernel.exps.Constant;
import org.apache.openjpa.kernel.exps.FilterListener;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.lib.util.OrderedMap;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;

/**
 * A query execution context.
 *
 * @author Abe White
 * @since 0.4.0
 */
public interface QueryContext {

    /**
     * Return the query for this context. Note that the query will be
     * unavailable in remote contexts, and this method may throw an exception
     * to that effect.
     */
    public Query getQuery();

    /**
     * The persistence context for the query.
     */
    public StoreContext getStoreContext();

    /**
     * Return the fetch configuration for this query.
     */
    public FetchConfiguration getFetchConfiguration();

    /**
     * Returns the operation that this query will be expected to perform.
     *
     * @see QueryOperations
     * @since 0.4.0
     */
    public int getOperation();

    /**
     * The query language.
     */
    public String getLanguage();

    /**
     * The query string.
     */
    public String getQueryString();

    /**
     * Return the candidate collection, or <code>null</code> if an
     * extent was specified instead of a collection.
     */
    public Collection<?> getCandidateCollection();

    /**
     * Return the class of the objects that this query will return,
     * or <code>null</code> if this information is not available / not relevant.
     */
    public Class<?> getCandidateType();

    /**
     * Whether query results will include subclasses of the candidate class.
     */
    public boolean hasSubclasses();

    /**
     * Set the candidate type.
     */
    public void setCandidateType(Class<?> cls, boolean subs);

    /**
     * Whether the query has been marked read-only.
     */
    public boolean isReadOnly();

    /**
     * Whether the query has been marked read-only.
     */
    public void setReadOnly(boolean readOnly);

    /**
     * The unique flag.
     */
    public boolean isUnique();

    /**
     * Specify that the query will return only 1
     * result, rather than a collection. The execute method will return null
     * if the query result size is 0.
     *
     * @since 0.3.0
     */
    public void setUnique(boolean unique);
    
    /**
     * Affirms if this query results are distinct instance(s).
     * 
     * @since 2.0.0
     */
    public boolean isDistinct();


    /**
     * Scope of a mapping from the result data to its object representation.
     */
    public Class<?> getResultMappingScope();

    /**
     * Name of a mapping from the result data to its object representation.
     */
    public String getResultMappingName();

    /**
     * Name and scope of a mapping from the result data to its object
     * representation.
     */
    public void setResultMapping(Class<?> scope, String name);

    /**
     * Returns the result class that has been set through
     * {@link #setResultType}, or null if none.
     */
    public Class<?> getResultType();

    /**
     * Specify the type of object in which the result of evaluating this query.
     *
     * @since 0.3.0
     */
    public void setResultType(Class<?> cls);

    /**
     * Return the 0-based start index for the returned results.
     */
    public long getStartRange();

    /**
     * Return the 0-based exclusive end index for the returned results,
     * or {@link Long#MAX_VALUE} for no limit.
     */
    public long getEndRange();

    /**
     * Set the range of results to return.
     *
     * @param start 0-based inclusive start index
     * @param end 0-based exclusive end index, or
     * {@link Long#MAX_VALUE} for no limit
     * @since 0.3.2
     */
    public void setRange(long start, long end);

    /**
     * The parameter declaration.
     */
    public String getParameterDeclaration();

    /**
     * Declared parameters, for query languages that use them.
     */
    public void declareParameters(String params);

    /**
     * Return a map of parameter name to type for this query. The returned
     * map will iterate in the order that the parameters were declared or,
     * if they're implicit, used.
     */
    public OrderedMap<Object,Class<?>> getOrderedParameterTypes();
    
    /**
     * Return a map of parameter name to type for this query. The returned
     * map will iterate in the order that the parameters were declared or,
     * if they're implicit, used.
     */
    @Deprecated
    public LinkedMap getParameterTypes();

    /**
     * If this query is a bulk update, return a map of the
     * {@link FieldMetaData}s to {@link Constant}s.
     */
    public Map<FieldMetaData, Value> getUpdates();

    /**
     * Whether to ignore changes in the current transaction.
     */
    public boolean getIgnoreChanges();

    /**
     * Return the query's compilation state.
     */
    public Object getCompilation();

    /**
     * If this query is not a projection but places candidate results into a
     * result class under an alias, return that alias.
     */
    public String getAlias();

    /**
     * If this query is a projection, return the projection aliases.
     */
    public String[] getProjectionAliases();

    /**
     * If this query is a projection, return the projection types.
     */
    public Class<?>[] getProjectionTypes();

    /**
     * Return true if the query is an aggregate.
     */
    public boolean isAggregate();

    /**
     * Return true if the query uses grouping.
     */
    public boolean hasGrouping();

    /**
     * Return the classes that affect this query.
     */
    public ClassMetaData[] getAccessPathMetaDatas();

    /**
     * Return the filter listener for the given tag, or null.
     */
    public FilterListener getFilterListener(String tag);

    /**
     * Return the filter listener for the given tag, or null.
     */
    public AggregateListener getAggregateListener(String tag);

    /**
     * The set of filter listeners.
     */
    public Collection<FilterListener> getFilterListeners();

    /**
     * The set of aggregate listeners.
     */
    public Collection<AggregateListener> getAggregateListeners();

    /**
     * Helper method to delete the objects found by executing a query on
     * the given executor.
     */
    public Number deleteInMemory(StoreQuery q, StoreQuery.Executor ex, 
        Object[] params);

    /**
     * Helper method to update the objects found by executing a query on
     * the given executor.
     */
    public Number updateInMemory(StoreQuery q, StoreQuery.Executor ex, 
        Object[] params);

    /**
     * Helper method to instantiate the class with the given name, taking
     * into account the query's candidate package, automatic imports, and
     * the given imports (if any). Returns null if the type cannot be found.
     */
    public Class<?> classForName(String name, String[] imports);

    /**
     * Synchronize on the query's internal lock.
     */
    public void lock (); 

    /**
     * Unlock the query's internal lock.
     */
    public void unlock();
}
