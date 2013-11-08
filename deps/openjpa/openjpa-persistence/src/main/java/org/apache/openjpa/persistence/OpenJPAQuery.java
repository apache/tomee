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
package org.apache.openjpa.persistence;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.QueryFlushModes;
import org.apache.openjpa.kernel.QueryHints;
import org.apache.openjpa.kernel.QueryOperations;

/**
 * Interface implemented by OpenJPA queries.
 *
 * @since 0.4.0
 * @author Abe White
 * @published
 */
public interface OpenJPAQuery<X> extends TypedQuery<X> {

    /**
     * Hint key for specifying the number of rows to optimize for.
     */
    public static final String HINT_RESULT_COUNT = QueryHints.HINT_RESULT_COUNT;

    /**
     * The owning entity manager.
     */
    public OpenJPAEntityManager getEntityManager();

    /**
     * Query language.
     */
    public String getLanguage();

    /**
     * Query operation type.
     */
    public QueryOperationType getOperation();

    /**
     * Fetch plan for controlling the loading of results.
     */
    public FetchPlan getFetchPlan();

    /**
     * Query string.
     */
    public String getQueryString();

    /**
     * Whether to ignore changes in the current transaction.
     */
    public boolean getIgnoreChanges();

    /**
     * Whether to ignore changes in the current transaction.
     */
    public OpenJPAQuery<X>setIgnoreChanges(boolean ignore);

    /**
     * Return the candidate collection, or <code>null</code> if an
     * extent was specified instead of a collection.
     */
    public Collection getCandidateCollection();

    /**
     * Set a collection of candidates.
     */
    public OpenJPAQuery<X> setCandidateCollection(Collection coll);

    /**
     * Query result element type.
     */
    public Class getResultClass();

    /**
     * Query result element type.
     */
    public OpenJPAQuery<X> setResultClass(Class type);

    /**
     * Whether subclasses are included in the query results.
     */
    public boolean hasSubclasses();

    /**
     * Whether subclasses are included in the query results.
     */
    public OpenJPAQuery<X> setSubclasses(boolean subs);

    /**
     * Return the 0-based start index for the returned results.
     */
    public int getFirstResult();

    /**
     * Return the maximum number of results to retrieve.
     * or {@link Integer#MAX_VALUE} for no limit.
     */
    public int getMaxResults();

    /**
     * Compile the query.
     */
    public OpenJPAQuery<X> compile();

    /**
     * Whether this query has positional parameters.
     */
    public boolean hasPositionalParameters();

    /**
     * The positional parameters for the query; empty array if none or
     * if query uses named parameters.
     */
    public Object[] getPositionalParameters();

    /**
     * The named parameters for the query; empty map if none or
     * if query uses positional parameters.
     */
    public Map<String, Object> getNamedParameters();

    /**
     * Set parameters.
     */
    public OpenJPAQuery<X> setParameters(Map params);

    /**
     * Set parameters.
     */
    public OpenJPAQuery<X> setParameters(Object... params);

    /**
     * Close all open query results.
     */
    public OpenJPAQuery<X>closeAll();

    /**
     * Returns a description of the commands that will be sent to
     * the datastore in order to execute this query. This will
     * typically be in the native query language of the database (e.g., SQL).
     *
     * @param params the named parameter map for the query invocation
     */
    public String[] getDataStoreActions(Map params);

    public OpenJPAQuery<X> setMaxResults(int maxResult);

    public OpenJPAQuery<X> setFirstResult(int startPosition);

    public OpenJPAQuery<X> setHint(String hintName, Object value);

    public OpenJPAQuery<X> setParameter(String name, Object value);

    public OpenJPAQuery<X> setParameter(String name, Date value, TemporalType temporalType);

    public OpenJPAQuery<X> setParameter(String name, Calendar value, TemporalType temporalType);

    public OpenJPAQuery<X> setParameter(int position, Object value);

    public OpenJPAQuery<X> setParameter(int position, Date value, TemporalType temporalType);

    public OpenJPAQuery<X> setParameter(int position, Calendar value, TemporalType temporalType);
    
    /**
     * Sets whether the type of user-supplied bind parameter value and the type of target persistent 
     * property they bind to are checked with strong or weak constraint.
     * <br>
     * The same can be set via {@link Query#setHint(String, Object) hint} without puncturing standard
     * JPA API.
     * 
     * @see Filters#canConvert(Class, Class, boolean)
     * @see Filters#convert(Object, Class, boolean)
     * 
     * @param hint a String or Boolean value.
     */
    public void setRelaxBindParameterTypeChecking(Object hint);
    
    /**
     * Gets whether the type of user-supplied bind parameter value and the type of target persistent 
     * property they bind to are checked with strong or weak constraint.
     * 
     * @return the booelan state. False by default, i.e. the type of a bind parameter value is checked
     * strongly against the target property type.  
     */
    public boolean getRelaxBindParameterTypeChecking();
    

    public OpenJPAQuery<X> setFlushMode(FlushModeType flushMode);

    /**
     * Return the current flush mode.
	 */
	public FlushModeType getFlushMode ();

    /**
     * @deprecated use the {@link QueryOperationType} instead.
     */
    public static final int OP_SELECT = QueryOperations.OP_SELECT;

    /**
     * @deprecated use the {@link QueryOperationType} instead.
     */
    public static final int OP_DELETE = QueryOperations.OP_DELETE;

    /**
     * @deprecated use the {@link QueryOperationType} instead.
     */
    public static final int OP_UPDATE = QueryOperations.OP_DELETE;

    /**
     * @deprecated use the {@link FlushModeType} enum instead.
     */
    public static final int FLUSH_TRUE = QueryFlushModes.FLUSH_TRUE;

    /**
     * @deprecated use the {@link FlushModeType} enum instead.
     */
    public static final int FLUSH_FALSE = QueryFlushModes.FLUSH_FALSE;

    /**
     * @deprecated use the {@link FlushModeType} enum instead.
     */
    public static final int FLUSH_WITH_CONNECTION =
        QueryFlushModes.FLUSH_WITH_CONNECTION;

    /**
     * @deprecated cast to {@link QueryImpl} instead. This
     * method pierces the published-API boundary, as does the SPI cast.
     */
    public OpenJPAQuery<X> addFilterListener(org.apache.openjpa.kernel.exps.FilterListener listener);

    /**
     * @deprecated cast to {@link QueryImpl} instead. This
     * method pierces the published-API boundary, as does the SPI cast.
     */
    public OpenJPAQuery<X> removeFilterListener(org.apache.openjpa.kernel.exps.FilterListener listener);

    /**
     * @deprecated cast to {@link QueryImpl} instead. This
     * method pierces the published-API boundary, as does the SPI cast.
     */
    public OpenJPAQuery<X> addAggregateListener(org.apache.openjpa.kernel.exps.AggregateListener listener);

    /**
     * @deprecated cast to {@link QueryImpl} instead. This
     * method pierces the published-API boundary, as does the SPI cast.
     */
    public OpenJPAQuery<X> removeAggregateListener(org.apache.openjpa.kernel.exps.AggregateListener listener);
    
    /**
     * Gets hints supported by this query.
     * 
     * @since 2.0.0
     */
    public Set<String> getSupportedHints();
}
