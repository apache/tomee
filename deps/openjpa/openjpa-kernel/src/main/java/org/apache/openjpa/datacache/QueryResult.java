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

import java.util.ArrayList;
import java.util.Collection;

/**
 * A query result.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
public class QueryResult
    extends ArrayList<Object> {

    private final long _ex;

    private long _timestamp = 0L;
    /**
     * Constructor; supply corresponding query key and result data.
     */
    public QueryResult(QueryKey key, Collection<Object> data) {
        super(data);

        if (key.getTimeout() == -1)
            _ex = -1;
        else
            _ex = System.currentTimeMillis() + key.getTimeout();
    }

    /**
     * Constructor to set internal data from a serializer.
     */
    public QueryResult(Collection<Object> data, long ex) {
        super(data);
        _ex = ex;
    }

    /**
     * Expiration time, or -1 for no timeout.
     */
    public long getTimeoutTime() {
        return _ex;
    }

    /**
     * Whether this data is timed out.
     */
    public boolean isTimedOut() {
        return _ex != -1 && _ex < System.currentTimeMillis();
	}

    /**
     * Sets the timestamp of the query result.
     * @param ts  -- Timestamp value in long
     */
    public void setTimestamp(long ts) {
        this._timestamp = ts;
    }

    /**
     * Returns the timestamp of the query result.
     * @return -- the timestamp value in long
     */
    public long getTimestamp() {
        return this._timestamp;
    }
}
