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
 * A {@link Map} type that can hold its keys, values, or both with
 * weak or soft references.
 *
 * @author Abe White
 */
public interface ReferenceMap extends Map {

    public static final int HARD = 0;
    public static final int WEAK = 1;
    public static final int SOFT = 2;

    /**
     * Purge stale entries.
     */
    public void removeExpired();

    /**
     * Overridable callback for when a key reference expires.
     *
     * @param value the value for the expired key
     */
    public void keyExpired(Object value);

    /**
     * Overridable callback for when a value reference expires.
     *
     * @param key the key for the expired value
     */
    public void valueExpired(Object key);
}
