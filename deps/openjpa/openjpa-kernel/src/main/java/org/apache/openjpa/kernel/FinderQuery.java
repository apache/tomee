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
 * A finder query is a query for an instance of a class by its primary key.
 * A finder query is parameterized by the type of key K, type of value V and 
 * type of result R.
 * 
 * @author Pinaki Poddar
 * 
 * @since 2.0.0
 *
 */
public interface FinderQuery<K,V,R>  {
    
    /**
     * Gets the identifier of this receiver.
     * 
     */
    public K getIdentifier();
    
    /**
     * Gets the value to which this receiver delegates its execution.
     * 
     * @return
     */
    public V getDelegate();
    
    /**
     * Execute the query for a given instance.
     * 
     * @param sm the StateManager for a given instance carrying the primary key
     * values.
     * @param store the data store against which the query is to be executed.
     * @param fetch fetch parameters
     * 
     * @return the result of execution.
     * 
     */
    public R execute(OpenJPAStateManager sm, StoreManager store, 
        FetchConfiguration fetch);
    
    /**
     * Gets the query string.
     * 
     */
    public String getQueryString();
}
