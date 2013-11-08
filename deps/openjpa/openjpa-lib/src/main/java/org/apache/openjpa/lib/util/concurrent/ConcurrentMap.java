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
package org.apache.openjpa.lib.util.concurrent;

import java.util.Iterator;
import java.util.Map;

/**
 * A highly concurrent map.
 *
 * @author Abe White
 */
public interface ConcurrentMap extends Map {

    /**
     * Remove an arbitrary(not strictly random) entry from the map. This
     * allows implementation of concurrent caches with size ceilings.
     *
     * @return the removed entry, or null if map is empty
     */
    public Map.Entry removeRandom();

    /**
     * Iterate over map entries, beginning at an arbitrary
     * (not strictly random) entry.
     */
    public Iterator randomEntryIterator();
}
