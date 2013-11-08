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

/**
 * Standard query hint keys.
 */
public interface QueryHints {
    // These keys are directly handled in {@link QueryImpl} class.
    // Declaring a public static final String variable in this class will 
    // make it register as a supported hint key
    // if you do not want that then annotate as {@link Reflectable(false)}.
    public static final String HINT_SUBCLASSES          = "openjpa.Subclasses";
    public static final String HINT_FILTER_LISTENER     = "openjpa.FilterListener";
    public static final String HINT_FILTER_LISTENERS    = "openjpa.FilterListeners";
    public static final String HINT_AGGREGATE_LISTENER  = "openjpa.AggregateListener";
    public static final String HINT_AGGREGATE_LISTENERS = "openjpa.AggregateListeners";
    
    /** 
     * Hint to specify the number of rows to optimize for.
     */
    public static final String HINT_RESULT_COUNT = "openjpa.hint.OptimizeResultCount";
    
    /**
     * Hints to signal that the JPQL/SQL query string contains a parameter
     * marker <code>?</code> character. By default, the query string is parsed
     * to count number of parameters assuming that all <code>?</code> characters
     * designate a bind parameter. This assumption makes the parse faster.
     */
    public static final String HINT_PARAM_MARKER_IN_QUERY = "openjpa.hint.ParameterMarkerInQuery";
    
    /**
     * A directive to invalidate any prepared SQL that might have been cached
     * against a JPQL query. The target SQL corresponding to a JPQL depends on
     * several context parameters such as fetch configuration, lock mode etc.
     * If a query is executed repeatedly and hence its SQL is cached for faster
     * execution then if any of the contextual parameters change across query
     * execution then the user must supply this hint to invalidate the cached
     * SQL query. 
     * The alternative to monitor any such change for automatic invalidation 
     * has a constant performance penalty for the frequent use case where a 
     * query is repeatedly executed in different persistent context with the 
     * same fetch plan or locking.  
     * 
     * @see #HINT_IGNORE_PREPARED_QUERY
     */
    public static final String HINT_INVALIDATE_PREPARED_QUERY = "openjpa.hint.InvalidatePreparedQuery";
    
    /**
     * A directive to ignore any prepared SQL that might have been cached
     * against a JPQL query. The target SQL corresponding to a JPQL depends on
     * several context parameters such as fetch configuration, lock mode etc.
     * If a query is executed repeatedly and hence its SQL is cached for faster
     * execution then if any of the contextual parameters change across query
     * execution then the user must supply this hint to ignore the cached
     * SQL query for the current execution.
     * This is in contrast with invalidation hint that removes the cached 
     * version from cache altogether.
     * 
     * The cached SQL is retained and subsequent execution of the same query
     * string without this hint will reuse the cached SQL. 
     * 
     * @see #HINT_INVALIDATE_PREPARED_QUERY
     */
    public static final String HINT_IGNORE_PREPARED_QUERY = "openjpa.hint.IgnorePreparedQuery";

    /**
     * A directive to ignore any cached finder query for find() operation.
     * The cached entry, if any, remains in the cache.
     */
    public static final String HINT_IGNORE_FINDER = "openjpa.hint.IgnoreFinder";
    
    /**
     * A directive to invalidate any cached finder query.
     */
    public static final String HINT_INVALIDATE_FINDER = "openjpa.hint.InvalidateFinder";
    
    /**
     * A directive to overwrite a cached finder query by a new query. 
     */
    public static final String HINT_RECACHE_FINDER = 
        "openjpa.hint.RecacheFinder";
    
    /**
     * A boolean directive to relax checking of binding parameter value and the predicate
     * it binds to.
     */
    public static final String HINT_RELAX_BIND_PARAM_TYPE_CHECK = "openjpa.hint.RelaxParameterTypeChecking";

    /**
     * A boolean directive to generate literal directly into the SQL statement instead of using position parameter,
     * if possible.
     */
    public static final String HINT_USE_LITERAL_IN_SQL = "openjpa.hint.UseLiteralInSQL";
}
