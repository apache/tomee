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

/**
 * A {@link SavepointManager} implementation which stores all data in memory.
 *
 * @author Steve Kim
 * @since 0.3.4
 */
public class InMemorySavepointManager
    implements SavepointManager {

    private boolean _preFlush = true;

    /**
     * Return whether to call {@link Broker#preFlush}
     * when a savepoint is set. While this will allow for tracking of
     * newly embedded fields, it has the side effect of going through
     * pre-flush operations.
     */
    public boolean getPreFlush() {
        return _preFlush;
    }

    /**
     * Set whether to call {@link Broker#preFlush}
     * when a savepoint is set. While this will allow for tracking of
     * newly embedded fields, it has the side effect of going through
     * pre-flush operations.
     */
    public void setPreFlush(boolean preFlush) {
        _preFlush = preFlush;
    }

    public OpenJPASavepoint newSavepoint(String name, Broker broker) {
        // pre-flush after creating savepoint b/c pre-flush may add/change
        // states
        OpenJPASavepoint save = new OpenJPASavepoint(broker, name, true);
        if (_preFlush)
            broker.preFlush();
        return save;
    }

    public boolean supportsIncrementalFlush() {
        // cannot incrementally flush as saved fields may become out of synch.
        return false;
	}
}
