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
package org.apache.openjpa.lib.log;

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Base type that aids in adapting an external log framework to the
 * {@link LogFactory}.
 *
 * @author Abe White
 */
public abstract class LogFactoryAdapter implements LogFactory {

    // cache category to log adapters
    private Map<String, Log> _logs = new ConcurrentHashMap<String, Log>();

    public Log getLog(String channel) {
        // no locking; OK if same adapter created multiple times
        Log log = _logs.get(channel);
        if (log == null) {
            log = newLogAdapter(channel);
            _logs.put(channel, log);
        }
        return log;
    }

    /**
     * Return a log adapter for the given channel. This method may be called
     * multiple times for the same channel in concurrent situations.
     */
    protected abstract Log newLogAdapter(String channel);
}
