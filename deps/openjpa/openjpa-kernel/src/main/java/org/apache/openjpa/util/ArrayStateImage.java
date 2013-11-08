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
package org.apache.openjpa.util;

import java.util.BitSet;

import org.apache.commons.lang.ObjectUtils;

/**
 * Utilities for dealing with a simple state image consisting of an
 * <code>Object[]</code> of field values with one extra index containing a
 * {@link BitSet} of loaded fields. This simplistic state image might be used
 * for optimistic versioning.
 *
 * @author Abe White
 * @nojavadoc
 */
public class ArrayStateImage {

    /**
     * Create a new state image for the given number of fields.
     */
    public static Object[] newImage(int numFields) {
        Object[] state = new Object[numFields + 1];
        state[numFields] = new BitSet(numFields);
        return state;
    }

    /**
     * Return true if the given version object appears to be an array state
     * image.
     */
    public static boolean isImage(Object obj) {
        if (!(obj instanceof Object[]))
            return false;
        Object[] arr = (Object[]) obj;
        return arr.length > 0 && arr[arr.length - 1] instanceof BitSet;
    }

    /**
     * Get the loaded mask from a state image.
     */
    public static BitSet getLoaded(Object[] state) {
        return (BitSet) state[state.length - 1];
    }

    /**
     * Set the loaded mask into a state image.
     */
    public static void setLoaded(Object[] state, BitSet loaded) {
        state[state.length - 1] = loaded;
    }

    /**
     * Clone a state array.
     */
    public static Object[] clone(Object[] state) {
        Object[] copy = new Object[state.length];
        System.arraycopy(state, 0, copy, 0, state.length - 1);
        BitSet loaded = (BitSet) state[state.length - 1];
        copy[copy.length - 1] = loaded.clone();
        return copy;
    }

    /**
     * Return whether the given images are equivalent from an optimistic
     * locking perspective.
     */
    public static boolean sameVersion(Object[] state1, Object[] state2) {
        if (state1 == state2)
            return true;

        // if either state is null, then we report that it is the
        // same: this is because a null version will indicate that
        // there are no loaded fields in the version at all, which
        // indicates that there is nothing to compare
        if (state1 == null || state2 == null)
            return true;

        // check only the fields that are in the loaded set for the
        // first version
        BitSet loaded1 = getLoaded(state1);
        BitSet loaded2 = getLoaded(state2);
        for (int i = 0, max = loaded1.length(); i < max; i++) {
            if (loaded1.get(i) && loaded2.get(i)
                && !ObjectUtils.equals(state1[i], state2[i]))
                return false;
        }
        return true;
	}
} 
