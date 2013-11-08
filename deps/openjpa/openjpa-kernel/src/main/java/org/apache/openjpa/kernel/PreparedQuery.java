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

import java.util.Map;

import org.apache.openjpa.kernel.PreparedQueryCache.Exclusion;

/**
 * A prepared query associates a compiled query to a <em>parsed state</em> that
 * can be executed possibly with more efficiency. An obvious example is to 
 * associate a compiled query to an executable SQL string. 
 * 
 * The query expressed in target language can be executed directly bypassing 
 * the critical translation cost to the data store target language on every 
 * execution. 
 * 
 * As the subsequent execution of a cached query will bypass normal query 
 * compilation, the post-compilation state of the original query is captured by 
 * this receiver to be transferred to the executable query instance.  
 * 
 * This receiver must not hold any context-sensitive reference or dependency.
 * Because the whole idea of preparing a query (for a cost) is to be able to
 * execute the same logical query in different persistence contexts. However,
 * a prepared query may not be valid when some parameters of execution context  
 * such as lock group or fetch plan changes in a way that will change the target
 * query. Refer to {@link PreparedQueryCache} for invalidation mechanism on
 * possible actions under such circumstances.
 * 
 * The query execution model <em>does</em> account for context changes that do 
 * not impact the target query e.g. bind variables. 
 * 
 * @author Pinaki Poddar
 *
 * @since 2.0.0
 */
public interface PreparedQuery  {
    /**
     * Get the immutable identifier of this receiver used for 
     * * {@link PreparedQueryCache cache}.
     */
	public String getIdentifier();
	
	/**
	 * Get the target database query.
	 */
    public String getTargetQuery();
    
    /**
     * Get the original query.
     */
    public String getOriginalQuery();
    
    /**
     * Gets the language in which this query is expressed.
     */
    public String getLanguage();
    
    /**
     * Fill in the post-compilation state of the given Query. This must be
     * called when a original query is substituted by this receiver and hence 
     * the original query is not parsed or compiled.
     * 
     * @param q A Query which has been substituted by this receiver and hence
     * does not have its post-compilation state.
     */
	public void setInto(Query q);
	
	/**
	 * Initialize from the given argument.  
	 * 
     * @param o an opaque instance supposed to carry post-execution data such
	 * as target database query, parameters of the query etc.
	 * 
	 * @return Exclusion if this receiver can initialize itself from the given
	 * argument. false otherwise.
	 */
	public Exclusion initialize(Object o);
	
	/**
	 * Affirms if this receiver has been initialized.
	 */
	public boolean isInitialized();
	
	/**
     * Get the list of parameters in a map where an entry represents a parameter
	 * key and value after replacing with the given user parameters. 
	 * 
	 * Must be invoked after initialize().  
	 * 
	 * @param user the map of parameter key and value set by the user on the
	 * original query.
	 */
	public Map reparametrize(Map user, Broker broker);
	
}
