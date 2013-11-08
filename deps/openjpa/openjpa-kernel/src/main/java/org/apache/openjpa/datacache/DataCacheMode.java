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
package org.apache.openjpa.datacache;

public enum DataCacheMode {
    /**
     * <p>All entities are cached regardless of annotations or xml configuration.</p>
     */
    ALL,
    /**
     * <p>No entities are cached regardless of annotations or xml configuration.</p>
     */
    NONE,
    /**
     * <p>
     * <b>Only</b> the entities which are configured to be in the cache will be
     * cached. Entities which do not specify whether they are cacheable will be
     * excluded
     * </p>
     * <p>
     * AKA opt-in.
     * </p>
     */
    ENABLE_SELECTIVE,
    /**
     * <p>
     * All entities except those which are explicitly excluded will be cached.
     * </p>
     * <p>
     * AKA opt-out
     * </p>
     */
    DISABLE_SELECTIVE,
    /**
     * <p>
     * Default value. In this case OpenJPA will behave as it did in previous
     * releases, and will take into account the includedTypes and excludedTypes
     * optional parameters on the <literal>openjpa.DataCache</literal>
     * configuration property.
     * </p>
     */
    UNSPECIFIED
}

