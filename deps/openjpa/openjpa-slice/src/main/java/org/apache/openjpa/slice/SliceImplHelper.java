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

import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UserException;

/**
 * Utility methods to determine the target slices for a persistence capable
 * instance by calling back to user-specified distribution policy.
 * 
 * @author Pinaki Poddar
 *
 */
public class SliceImplHelper {
	private static final Localizer _loc =
		Localizer.forPackage(SliceImplHelper.class);
	
	/**
	 * Gets the target slices by calling user-specified {@link DistributionPolicy} or {@link ReplicationPolicy} 
     * depending on whether the given instance is {@link DistributedConfiguration#isReplicated(Class) replicated}.
     * The policy is invoked when an instance enters the managed life cycle. However, if the instance
     * being persisted is distributed to a target slice that is <em>not</em> determinable by its own basic attributes,
     * but on its associated instance then those association may not have been initialized at the point of entry. 
     * In such case, the policy may return null. However, when a target slice may not be determinable at the 
     * entry to managed life cycle, a target slice must be determinable by the time an instance is flushed.   
     * 
     * @param pc the managed instance whose target slice is to be determined.
     * @param conf to supply the distribution policy
     * @param ctx the (opaque) context of invocation. No semantics is currently associated.
     * 
     * @return information about the target slice for the given instance. Can be null if the policy
     * can not determine the target slice(s) based on the current state of the instance.  
	 */
	public static SliceInfo getSlicesByPolicy(Object pc, DistributedConfiguration conf, Object ctx) {
		List<String> actives = conf.getActiveSliceNames();
		Object policy = null;
		String[] targets = null;
		boolean replicated = isReplicated(pc, conf);
		if (replicated) {
			policy = conf.getReplicationPolicyInstance();
            targets = ((ReplicationPolicy)policy).replicate(pc, actives, ctx);
            assertSlices(targets, pc, conf.getActiveSliceNames(), policy);
		} else {
			policy = conf.getDistributionPolicyInstance();
			String target = ((DistributionPolicy)policy).distribute(pc, actives, ctx);
			if (target != null) {
			    targets = new String[]{target};
		        assertSlices(targets, pc, conf.getActiveSliceNames(), policy);
			}
		}
		return targets != null ? new SliceInfo(replicated, targets) : null;
	}
	
	private static void assertSlices(String[] targets, Object pc, List<String> actives, Object policy) {
	    if (targets == null || targets.length == 0)
            throw new UserException(_loc.get("no-policy-slice", new Object[] {
                policy.getClass().getName(), pc, actives}));
        for (String target : targets) 
            if (!actives.contains(target))
                throw new UserException(_loc.get("bad-policy-slice", 
                   new Object[] {policy.getClass().getName(), target, pc, 
                    actives}));
	}
	
    /**
     * Gets the target slices for the given StateManager.
     */
    public static SliceInfo getSlicesByPolicy(OpenJPAStateManager sm, 
        DistributedConfiguration conf, Object ctx) {
        return getSlicesByPolicy(sm.getPersistenceCapable(), conf, ctx);
    }
    
	/**
	 * Affirms if the given instance be replicated to multiple slices.
	 */
    public static boolean isReplicated(Object pc, DistributedConfiguration conf) {
        return pc == null ? false : conf.isReplicated(pc.getClass());
	}

	
	/**
	 * Affirms if the given StateManager has an assigned slice.
	 */
	public static boolean isSliceAssigned(OpenJPAStateManager sm) {
	     return sm != null && sm.getImplData() != null 
	         && sm.getImplData() instanceof SliceInfo;
	}

    /**
     * Gets the assigned slice information, if any, from the given StateManager.
     */
    public static SliceInfo getSliceInfo(OpenJPAStateManager sm) {
        return isSliceAssigned(sm) ? (SliceInfo) sm.getImplData() : null;
    }
}
