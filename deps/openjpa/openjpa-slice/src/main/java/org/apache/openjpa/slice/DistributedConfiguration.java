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

import org.apache.openjpa.conf.OpenJPAConfiguration;

/**
 * A configuration for multiple data stores, each referred as <em>slice</em>.
 * This configuration allows each underlying slice be configured with its
 * own specific configuration properties such as JDBC Driver or connection
 * user/password etc. 
 * <br>
 * This configuration also extends by several policy plugins. A policy plugin 
 * is an user implementation of a specific policy interface. A policy implementation 
 * is invoked by Slice runtime.
 * <br>
 * Slice recognizes following policies 
 * <LI> {@link DistributionPolicy Distribution Policy} governs how new instances be distributed
 * among the slices.
 * <LI> {@link ReplicationPolicy Replication Policy} maintains the same instance in multiple slices.
 * <LI> {@link QueryTargetPolicy Query Target Policy} narrows the slices for a query execution.
 * <LI> {@link FinderTargetPolicy Finder Target Policy} narrows the slices for primary key based lookup 
 * 
 * @author Pinaki Poddar 
 *
 */
public interface DistributedConfiguration extends OpenJPAConfiguration {
	/**
	 * Gets the active slice identifiers. This list is determined by the
	 * configuration properties either by explicit listing in 
	 * <code>openjpa.slice.Names</code> property or by scanning 
	 * <code>openjpa.slice.*.*</code> properties.
	 * <br> 
	 * The ordering of the slice identifiers is determined when they are
	 * specified explicitly in <code>openjpa.slice.Names</code> property or 
	 * ordered alphabetically when found by scanning the properties.
	 * <br>
     * This list always returns the identifiers that are <em>active</em>, slices
	 * that can not be connected to are not included in this list.
	 */
	List<String> getActiveSliceNames();
	
	/**
	 * Gets the available slice identifiers irrespective of their status.
	 * @return
	 */
    List<String> getAvailableSliceNames();

	
	/**
	 * Gets the slices of given status.
	 * @param statuses list of status flags. If null, returns all slices 
	 * irrespective of status.
	 */
	List<Slice> getSlices(Slice.Status...statuses);
	
	/**
	 * Gets the Slice for a given name.
	 * Exception is raised if the given slice is not configured.
	 */
	Slice getSlice(String sliceName);
	
    /**
     * Gets the policy that governs how new instances will be distributed across
     * the available slices.
     */
    DistributionPolicy getDistributionPolicyInstance();
    
    /**
     * Sets the policy that governs how new instances will be distributed across
     * the available slices.
     */
    void setDistributionPolicyInstance(DistributionPolicy policy);
	
    /**
     * Gets the policy, as a plugin string, that governs how new instances will 
     * be distributed across the available slices.
     */
    String getDistributionPolicy();
    
    /**
     * Sets the policy, from the given plugin string, that governs how new 
     * instances will be distributed across the available slices.
     */
    void setDistributionPolicy(String policy);

    /**
     * Gets the policy that governs how new replicated instances will be 
     * replicated across the available slices.
     */
    ReplicationPolicy getReplicationPolicyInstance();
    
    /**
     * Gets the policy, as a plugin string, that governs how new replicated 
     * instances will be replicated across the available slices.
     */
    String getReplicationPolicy();
	
    /**
     * Sets the policy that governs how new replicated instances will be 
     * replicated across the available slices.
     */
    void setReplicationPolicyInstance(ReplicationPolicy policy);
    
    /**
     * Sets the policy, from the given plugin string, that governs how new 
     * replicated instances will be replicated across the available slices.
     */
    void setReplicationPolicy(String policy);
    
    /**
     * Affirms if the given type is replicated.
     */
    boolean isReplicated(Class<?> type);
    
    
    /**
     * Gets the policy that governs which slices will be targeted for a given query. 
     */
    QueryTargetPolicy getQueryTargetPolicyInstance();
    
    /**
     * Gets the policy, as a plugin string, that governs which slices will be targeted 
     * for a given query.
     */
    String getQueryTargetPolicy();
    
    /**
     * Sets the policy that governs which slices will be targeted for a given query.
     */
    void setQueryTargetPolicyInstance(QueryTargetPolicy policy);
    
    /**
     * Sets the policy, from the given plugin string, that governs which slices will 
     * be targeted for a given query.
     */
    void setQueryTargetPolicy(String policy);

    /**
     * Gets the policy that governs which slices will be targeted for a given finder. 
     */
    FinderTargetPolicy getFinderTargetPolicyInstance();
    
    /**
     * Gets the policy, as a plugin string, that governs which slices will be targeted 
     * for a given finder.
     */
    String getFinderTargetPolicy();
    
    /**
     * Sets the policy that governs which slices will be targeted for a given finder.
     */
    void setFinderTargetPolicyInstance(FinderTargetPolicy policy);
    
    /**
     * Sets the policy, from the given plugin string, that governs which slices will 
     * be targeted for a given finder.
     */
    void setFinderTargetPolicy(String policy);
    
}
