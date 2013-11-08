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
package org.apache.openjpa.slice;

import java.util.List;
import java.util.Random;


/**
 * Policy to select one of the physical databases referred as <em>slice</em>
 * in which a given persistent instance will be stored.
 *  
 * The user application is required to implement this interface. Slice will
 * invoke the single method of this interface to determine the target slice for
 * a newly persistent or a detached instance being merged.
 *  
 * @author Pinaki Poddar 
 *
 */
public interface DistributionPolicy {
	/**
	 * Gets the name of the target slice where the given newly persistent or
	 * the detached, to-be-merged instance will be stored.
	 * <br>
	 * If the current state of the given instance is sufficient to determine
	 * the target slice, return null. In that case, the runtime will callback
	 * this method again before the instance being flushed to the datastore.
	 * By then, the policy <em>must</em> be able to determine the target slice.  
	 *  
	 * @param pc The newly persistent or to-be-merged object. 
	 * @param slices list of names of the active slices. The ordering of 
	 * the list is either explicit <code>openjpa.slice.Names</code> property
	 * or implicit i.e. alphabetic order of available identifiers if 
	 * <code>openjpa.slice.Names</code> is unspecified.  
     * @param context the generic persistence context managing the given
     * instance.
	 * 
	 * @return identifier of the slice. This name must match one of the
	 * given slice names. 
	 * @see DistributedConfiguration#getActiveSliceNames()
	 */
	String distribute(Object pc, List<String> slices, Object context);
	
	/**
	 * Implements a default distribution policy to store the given 
	 * instance to a randomly selected available slice.
	 *
	 */
	public static class Default implements DistributionPolicy {
		private static Random RNG = new Random();
        public String distribute(Object pc, List<String> slices, Object ctx) {
			return slices.get(RNG.nextInt(slices.size()));
		}
	}
}
