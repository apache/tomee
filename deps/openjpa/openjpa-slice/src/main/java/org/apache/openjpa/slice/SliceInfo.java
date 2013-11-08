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

import java.io.Serializable;
import java.util.List;

import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.InternalException;

/**
 * Holder of slice names where a persistent instance is stored. This structure
 * is held in StateManagers to track the origin of a persistent instance.
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class SliceInfo implements Serializable {
	private final boolean  _isReplicated;
	private String[] _slices;
	
	private static transient Localizer _loc = Localizer.forPackage(SliceInfo.class);
	
    /**
     * Generic constructor given one or more slice names. 
     * The replicated flag is set independently.
     */
    public SliceInfo(boolean replicated, String[] slices) {
        super();
        if (slices == null || slices.length == 0)
            throw new InternalException();
        _isReplicated = replicated;
        _slices = slices;
    }
    
    /**
     * Generic constructor given one or more slice names. 
     * The replicated flag is set independently.
     */
    public SliceInfo(boolean replicated, List<String> slices) {
        super();
        if (slices == null || slices.isEmpty())
            throw new InternalException();
        _isReplicated = replicated;
        _slices = slices.toArray(new String[slices.size()]);
    }
	
	/**
     * Constructor for non-replicated instance that is stored in a single slice.
	 */
    public SliceInfo(String slice) {
		this(false, new String[]{slice});
	}
	
    /**
     * Constructor for replicated instance that is stored in one or more slices.
     */
    public SliceInfo(String[] slices) {
        this(true, slices);
    }
    
    public SliceInfo(List<String> slices) {
        this(true, slices);
    }

    /**
     * Affirms if this receiver designates replicated instances.
     * @return
     */
	public boolean isReplicated() {
		return _isReplicated;
	}

	/**
	 * Gets the name of the slice(s) held by this receiver.
	 */
	public String[] getSlices() {
		return _slices;
	}
	
	/**
     * Sets this receiver as the given StateManager's internal instance-level 
     * data. If the given StateManager had existing instance-level data that is
	 * not a SliceInfo then raise an exception.
	 */
	public SliceInfo setInto(OpenJPAStateManager sm) {
		if (sm == null)
			throw new NullPointerException();
		Object previous = sm.setImplData(this, true);
		if (previous == null || previous instanceof SliceInfo)
			return (SliceInfo)previous;
        throw new InternalException(_loc.get("unknown-impl-data", previous,
		    previous.getClass().getName(), sm.getPersistenceCapable())
		    .getMessage());
	}
}
