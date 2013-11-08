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

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Counts number of read/write requests and hit ratio for a cache in total and
 * per-class basis.
 * 
 * All methods with Class as input argument treats null as 
 * <code>java.lang.Object</code>. All per-class statistics  depends on 
 * determining the runtime type of the instance being cached. If it is not
 * possible to determine the runtime type from the given context, the statistics
 * is registered under generic <code>java.lang.Object</code>. 
 * 
 * @since 1.3.0
 */
public interface CacheStatistics extends Serializable {

	/**
	 * Gets number of total read requests since last reset.
	 */
	public long getReadCount();

	/**
	 * Gets number of total read requests that has been found in cache since
	 * last reset.
	 */
	public long getHitCount();

	/**
	 * Gets number of total write requests since last reset.
	 */
	public long getWriteCount();

	/**
	 * Gets number of total read requests since start.
	 */
	public long getTotalReadCount();

	/**
	 * Gets number of total read requests that has been found in cache since
	 * start.
	 */
	public long getTotalHitCount();

	/**
	 * Gets number of total write requests since start.
	 */
	public long getTotalWriteCount();

    /**
     * Gets number of total read requests for the given class since last reset.
     * 
     * @deprecated - should use getReadCount(String c)
     */
    public long getReadCount(Class<?> cls);
  
    /**
     * Gets number of total read requests for the given class since last reset.
	 */
	public long getReadCount(String c);

    /**
     * Gets number of total read requests that has been found in cache for the given class since last reset.
     * 
     * @deprecated - should use getHitCount(String c)
     */
    public long getHitCount(Class<?> cls);
    
	/**
     * Gets number of total read requests that has been found in cache for the given class since last reset.
     */
	public long getHitCount(String c);
	
	/**
     * Gets number of total write requests for the given class since last reset.
     * 
     * @deprecated - should use getWriteCount(String c)
     */
    public long getWriteCount(Class<?> cls);

    /**
     * Gets number of total write requests for the given class since last reset.
	 */
	public long getWriteCount(String c);

	/**
     * Gets number of total read requests for the given class since start.
     * 
     * @deprecated - should use getTotalReadCount(String c)
     */
    public long getTotalReadCount(Class<?> cls);
    
	/**
	 * Gets number of total read requests for the given class since start.
	 */
	public long getTotalReadCount(String c);

	/**
     * Gets number of total read requests that has been found in cache for the given class since start.
     */
    public long getTotalHitCount(String c);

    /**
     * Gets number of total read requests that has been found in cache for the given class since start.
     * 
     * @deprecated - should use getTotalHitCount(String c)
     */
    public long getTotalHitCount(Class<?> cls);

	/**
     * Gets number of total write requests for the given class since start.
     */
    public long getTotalWriteCount(String c);

    /**
     * Gets number of total write requests for the given class since start.
     * 
     * @deprecated - should use getTotalWriteCount(String c)
     */
    public long getTotalWriteCount(Class<?> cls);

	/**
	 * Gets the time of last reset.
	 */
	public Date since();

	/**
	 * Gets the time of start.
	 */
	public Date start();

	/**
	 * Clears all accumulated statistics.
	 */
	public void reset();
	
	/**
	 * Returns whether or not statistics will be collected.
	 */
	public boolean isEnabled();
	
	/**
	 * Returns the types that are known to this cache being tracked.
	 */
    public Set<String> classNames();
    
    public Map<String, long[]> toMap();
    
}
