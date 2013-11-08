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

import java.util.List;
import java.util.Map;

import org.apache.openjpa.lib.conf.Configurable;

/**
 * A cache to create and maintain {@link FinderQuery finder queries}. 
 * 
 * A finder query is a query to find instance of a given class by its primary
 * identity. This cache maintains finder queries by generic identifier of
 * parameterized type K.
 * 
 * A cached query by an identifier of parameterized type K. 
 * 
 * A query can be cached by an identifier and value represented by parameterized
 * type V. Caching results in creating a new instance of FinderQuery FQ from the
 * value V such that FQ can be executed to return a result of parameterized type
 * R. A request to cache may not be successful if this cache determines the 
 * value V to be not cachable.
 * 
 * Both get() and put() operations can be controlled by the hints associated 
 * with FetchConfiguration.
 *  
 * The target database query FQ associated to a cached finder query F  
 * <em>may</em> depend upon query execution context such as fetch plan or 
 * lock group. This cache, by design, does not monitor the context or 
 * automatically invalidate an entry when the original query F is executed  
 * with context parameters that affect the target query FQ. 
 * 
 * The user must notify this receiver to invalidate a cached entry when 
 * execution context changes in a way that will modify the resultant database 
 * language query FQ.
 * 
 * One of the built-in mechanism (available in JPA facade) is to set query hints
 * to either invalidate the query entirely or ignore the cached version for the
 * current execution. 
 * 
 * @see QueryHints#HINT_IGNORE_FINDER
 * @see QueryHints#HINT_INVALIDATE_FINDER
 * @see QueryHints#HINT_RECACHE_FINDER
 * 
 * This cache allows customization of whether a query can be cached or not
 * via either explicit marking of certain classes as non-cachable (which is 
 * irreversible) or addition/removal of exclusion patterns (which is reversible)
 * 
 * @author Pinaki Poddar
 *
 * @since 2.0.0
 */
public interface FinderCache<K,V,R> extends Configurable {
    /**
     * Get the FinderQuery for the given key.
     * 
     * @param key for which the finder is looked up
     * @param fecth may contain hints to control lookup operation
     * 
     * @return FinderQuery for the given mapping.
     */
    public FinderQuery<K,V,R> get(K key, FetchConfiguration fetch);

    /**
     * Cache a FinderQuery for the given key and value. 
     * 
     * @param key for which the finder is cached.
     * @param value used to construct the finder query 
     * @param fetch may contain hints to control cache operation.
     * 
     * @return the finder query that has been cached. It may be newly 
     * constructed or an existing query. If the given key-value can not be 
     * cached, then return null.
     */
    public FinderQuery<K,V,R> cache(K key, V value, FetchConfiguration fetch);
    
	/**
	 * Get a map view of the cached entries as strings.
	 */
	public Map<String, String> getMapView();

	/**
	 * Remove the FinderQuery for the given key from this cache.
	 */
	public boolean invalidate(K key);
	
    /**
     * Marks the given key as not amenable to caching.
     * Explicit marking helps to avoid repeated computational cost of 
     * determining whether finder for a key can be cached or not.
     * 
     * Explicit marking can not be reversed by removal of exclusion patterns.
     * 
     * @return finder query for the given class if it had been cached before. 
     * null otherwise.
     */
    public FinderQuery<K,V,R> markUncachable(K key);

    /**
	 * Affirms if the given key matches any of the exclusion patterns.
	 */
	public boolean isExcluded(K key);

	/**
	 * Gets the excluded stringified keys.
	 */
	public List<String> getExcludes();
	
	/**
     * Adds the given pattern to the list of excluded patterns. Any existing
	 * cache entry whose key matches the given pattern will be marked 
	 * non-cachable in a reversible manner. 
	 */
	public void addExclusionPattern(String pattern);
	
	/**
	 * Removes the given pattern from the list of excluded patterns. 
	 * Any excluded entry that matches the given pattern can now be cached
	 * again, unless it has been marked non-cachable explicitly.
	 */
	public void removeExclusionPattern(String pattern);
	
	/**
	 * Gets the simple statistics for executed finder queries.
	 */
	public QueryStatistics<K> getStatistics();
}
