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

import java.util.EventObject;

/**
 * An event indicating the expiration of an object from the data cache,
 * or an expiration of a result list from the query cache.
 * The source of the event will be the cache.
 *
 * @since 0.3.0
 * @author Abe White
 */
public class ExpirationEvent
    extends EventObject {

    private final Object _key;
    private final boolean _expired;

    /**
     * Constructor.
     *
     * @param source the data or query cache
     * @param key the expired object oid or query key
     * @param expired <code>true</code> if the object was expired
     * naturally; else <code>false</code>.
     */
    public ExpirationEvent(Object source, Object key, boolean expired) {
        super(source);
        _key = key;
        _expired = expired;
    }

    /**
     * Return the expired object id or query key.
     */
    public Object getKey() {
        return _key;
    }

    /**
     * Return whether the expired object was expired naturally, or if
     * the object was explicitly removed.
     */
    public boolean getExpired() {
        return _expired;
	}
}
