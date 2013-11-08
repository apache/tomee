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

import java.util.Arrays;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.util.ImplHelper;

/**
 * A helper to determine the slice identifier of an instance.
 * 
 * @author Pinaki Poddar 
 *
 */
public class SlicePersistence {
    /**
     * The key for setting the Query hints. The value is comma-separated Slice
     * names. If the hint is specified then the query is executed only on the
     * listed slices. 
     */
    public static final String HINT_TARGET = ProductDerivation.HINT_TARGET;
    
	/**
	 * Get the slice identifier for the given instance if it is a managed
	 * instance and has been assigned to a slice.
     * If the given instance is replicated across multiple slices then returns
	 * comma-separated list of slice names.
	 * 
	 * @return name of the slice, if any. null otherwise.
	 */
	public static String getSlice(Object obj) {
		if (obj == null)
			return null;
        PersistenceCapable pc = ImplHelper.toPersistenceCapable(obj, null);
		if (pc == null)
			return null;
        OpenJPAStateManager sm = (OpenJPAStateManager)pc.pcGetStateManager();
        SliceInfo info = SliceImplHelper.getSliceInfo(sm);
		if (info == null)
			return null;
		String[] names = info.getSlices();
		return info.isReplicated() ? Arrays.toString(names) : names[0];
	}
	
	/**
     * Affirms if the given instance is replicated, provided the given instance
	 * is managed.
     */
    public static boolean isReplicated(Object obj) {
        if (obj == null)
            return false;
        PersistenceCapable pc = ImplHelper.toPersistenceCapable(obj, null);
        if (pc == null)
            return false;
        OpenJPAStateManager sm = (OpenJPAStateManager)pc.pcGetStateManager();
        SliceInfo info = SliceImplHelper.getSliceInfo(sm);
        if (info == null)
            return false;
        return info.isReplicated();
    }
}
