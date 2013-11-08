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
package org.apache.openjpa.instrumentation;

import java.util.Date;
import java.util.Set;

public interface PreparedQueryCacheInstrument {

    /**
     * Returns number of total exec requests since start.
     */
    public long getTotalExecutionCount(); 

    /**
     * Returns number of total exec requests since start.
     */
    public long getTotalExecutionCount(String query); 

    /**
     * Returns number of total execution requests since last reset
     */
    public long getExecutionCount();

    /**
     * Returns number of total execution requests since last reset
     */
    public long getExecutionCount(String query);

    /**
     * Returns number of total read requests that have been found in cache since 
     * last reset.
     */
    public long getHitCount();

    /**
     * Returns number of total read requests that have been found in cache since 
     * last reset.
     */
    public long getHitCount(String query);

    /**
     * Returns number of total read requests that has been found since start.
     */
    public long getTotalHitCount();

    /**
     * Returns number of total read requests that has been found since start.
     */
    public long getTotalHitCount(String query);

    /**
     * Resets cache statistics
     */
    public void reset();
    
    /**
     * Returns date since cache statistics collection were last reset.
     */
    public Date sinceDate();

    /**
     * Returns date cache statistics collection started.
     */
    public Date startDate();
    
    /**
     * Returns all queries currently tracked in the cache.
     * @return
     */
    public Set<String> queries();
}
