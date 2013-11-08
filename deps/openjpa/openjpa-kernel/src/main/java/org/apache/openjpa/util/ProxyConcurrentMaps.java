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

import java.util.Map;

/**
 * Utility methods used by concurrent map proxies.
 *
 */
public class ProxyConcurrentMaps extends ProxyMaps {
	/**
     * Call before invoking {@link Map#remove} on super.
     */
    public static boolean beforeRemove(ProxyMap map, Object key, Object value) {
        dirty(map, false);
        return map.containsKey(key);
    }
    
    /**
     * Call after invoking {@link Map#remove} on super.
     *
     * @param ret the return value from the super's method
     * @param before the return value from {@link #beforeRemove}
     * @return the value to return from {@link Map#remove}
     */
    public static boolean afterRemove(ProxyMap map, Object key, Object value, boolean ret, 
        boolean before) {
        if (before) {
            if (map.getChangeTracker() != null) {
                ((MapChangeTracker) map.getChangeTracker()).removed(key, ret);
            }
            removed(map, key, true);
            removed(map, ret, false);
        } 
        return ret;
    }
}
