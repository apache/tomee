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
package org.apache.openjpa.persistence.datacache;

import org.apache.openjpa.persistence.test.AllowFailure;

/**
 * Test data caching across multiple factories in the same JVM, using
 * the single-JVM commit provider to communicate updates..
 */
@AllowFailure(message="surefire excluded")
public class TestConcurrentDataCache
    extends CacheTest {

    public TestConcurrentDataCache(String test) {
        super(test);
    }

    protected String[] getConfs() {
        return new String[]{
            // the second cache is there solely to differentiate between
            // this PMF and the PMF created from confs2 below
            "openjpa.DataCache",
            "concurrent, concurrent(Name=differentiatingProperty1)",
            "openjpa.QueryCache", "true",
            "openjpa.RemoteCommitProvider", "sjvm",
            "openjpa.BrokerImpl", CacheTestBroker.class.getName(),
        };
    }

    protected String[] getConfs2() {
        return new String[]{
            // the second cache is there solely to differentiate between
            // this PMF and the PMF created from confs above
            "openjpa.DataCache",
            "concurrent, concurrent(Name=differentiatingProperty2)",
            "openjpa.QueryCache", "concurrent",
            "openjpa.RemoteCommitProvider", "sjvm",
            "openjpa.BrokerImpl", CacheTestBroker.class.getName(),
        };
    }
}
