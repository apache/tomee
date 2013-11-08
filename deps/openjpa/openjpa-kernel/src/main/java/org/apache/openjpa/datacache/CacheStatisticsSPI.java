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
package org.apache.openjpa.datacache;

/**
 * The provider extensions to the CacheStatistics interface.
 */
public interface CacheStatisticsSPI extends CacheStatistics {
    /**
     * Record a new cache get.
     * 
     * @param cls
     *            - The class describing the type that is contained in the cache.
     * @param hit
     *            - true for a cache hit, false otherwise
     */
    public void newGet(Class<?> cls, boolean hit);


    /**
     * Record a new cache put.
     * 
     * @param cls
     *            - The class describing the type that is contained in the cache.
     */
    public void newPut(Class<?> cls);


    /**
     * Enable statistics collection.
     */
    public void enable();

    /**
     * Disable statistics collection.
     */
    public void disable();
}
