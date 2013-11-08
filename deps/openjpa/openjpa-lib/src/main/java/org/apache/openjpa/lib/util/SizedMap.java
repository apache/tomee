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
package org.apache.openjpa.lib.util;

import java.util.Map;

/**
 * A {@link Map} type that maintains a maximum size, automatically
 * removing entries when the maximum is exceeded.
 *
 * @author Abe White
 */
public interface SizedMap extends Map {

    /**
     * The maximum number of entries, or Integer.MAX_VALUE for no limit.
     */
    public int getMaxSize();

    /**
     * The maximum number of entries, or Integer.MAX_VALUE for no limit.
     */
    public void setMaxSize(int max);

    /**
     * Whether the map is full.
     */
    public boolean isFull();

    /**
     * Overridable callback for when an overflow entry is automatically removed.
     */
    public void overflowRemoved(Object key, Object value);
}
