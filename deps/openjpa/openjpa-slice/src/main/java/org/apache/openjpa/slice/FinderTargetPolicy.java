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

/**
 * Policy to select one or more of the physical databases referred as 
 * <em>slice</em> in which a given finder will be executed.
 * 
 * @author Pinaki Poddar 
 * 
 * @see DistributionPolicy
 *
 */
public interface FinderTargetPolicy {
	/**
     * Gets the name of the slices where a given finder will be executed.
	 *  
	 * @param cls The class of the finder. 
	 * @param oid the primary key for the finder
	 * @param slices list of names of the active slices. The ordering of 
	 * the list is either explicit <code>openjpa.slice.Names</code> property
	 * or implicit i.e. alphabetic order of available identifiers if 
	 * <code>openjpa.slice.Names</code> is unspecified.  
     * @param context generic persistence context managing the given instance.
	 * 
	 * @return identifier of the slices. This names must match one of the
	 * given slice names. 
	 *  
	 * @see DistributedConfiguration#getActiveSliceNames()
	 */
	String[] getTargets(Class<?> cls, Object oid, List<String> slices, Object context);
}
