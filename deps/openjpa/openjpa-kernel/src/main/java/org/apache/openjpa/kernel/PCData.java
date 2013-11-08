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
package org.apache.openjpa.kernel;

import java.io.Serializable;
import java.util.BitSet;

/**
 * Holds data about a single persistence capable instance. This interface is
 * used in the caching framework, and may also be used by simple store manager
 * back-ends to hold persistent state.
 *
 * @author Patrick Linskey
 */
public interface PCData
    extends Serializable {

    /**
     * Return the id of this instance.
     */
    public Object getId();

    /**
     * Return the type of this instance.
     */
    public Class getType();

    /**
     * Returns the instance-level impl data, or null if none.
     */
    public Object getImplData();

    /**
     * Sets the stored instance-level impl data.
     */
    public void setImplData(Object val);

    /**
     * Returns the current version object that this data was loaded from.
     */
    public Object getVersion();

    /**
     * Sets the current version object that this data was loaded from.
     */
    public void setVersion(Object version);

    /**
     * Loads all fields that are currently stored in the cache
     * into the given state manager.
     *
     * @param sm the state manager to load
     * @param fetch the fetch configuration to use for loading related objects
     * @param context current context information
     */
    public void load(OpenJPAStateManager sm, FetchConfiguration fetch,
        Object context);

    /**
     * Loads some or all of the marked fields from the cache into the
     * given state manager.
     *
     * @param sm the state manager to load
     * @param fields the fields to load; clear the bits for the fields
     * that are successfully loaded
     * @param fetch the fetch configuration to use for loading related objects
     * @param context current context information
     */
    public void load(OpenJPAStateManager sm, BitSet fields,
        FetchConfiguration fetch, Object context);

    /**
     * Store all loaded fields of the state manager.
     */
    public void store(OpenJPAStateManager sm);

    /**
     * Store the given fields from the state manager.
     */
    public void store(OpenJPAStateManager sm, BitSet fields);

    /**
     * Return the data stored in the given field index.
     */
    public Object getData(int i);

    /**
     * Whether the given field index has stored data.
     */
    public boolean isLoaded(int i);
    
    /**
     * Get the name of the cache where this data is stored.
     */
    public String getCache();
}
