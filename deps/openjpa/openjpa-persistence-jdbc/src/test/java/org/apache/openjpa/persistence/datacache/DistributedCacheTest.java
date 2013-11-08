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

import org.apache.openjpa.lib.conf.Configurations;

/**
 * Distributed cache test.
 */
public class DistributedCacheTest
    extends CacheTest {

    private static String cache = null;
    private static String queryCache = null;
    private static String provider = null;

    public static void setCache(String plugin) {
        cache = plugin;
    }

    public static void setQueryCache(String plugin) {
        queryCache = plugin;
    }

    public static void setRemoteCommitProvider(String plugin) {
        provider = plugin;
    }

    public DistributedCacheTest(String test) {
        super(test);
    }

    public DistributedCacheTest(String test, Class testClass) {
        super(test);
    }

    protected String[] getConfs() {
        return getConfs(true);
    }

    protected String[] getConfs2() {
        return getConfs(false);
    }

    protected String[] getConfs(boolean confs1) {
        String fullProvider = provider;
        String props = Configurations.getProperties
            (System.getProperty("openjpa.RemoteCommitProvider"));
        if (props == null || props.length() == 0)
            props = Configurations.getProperties(provider);
        else
            fullProvider = Configurations.getPlugin(provider, props);
        if (props == null || props.length() == 0)
            throw new IllegalStateException
                ("must specify -Dkodo.RemoteCommitProvider=?");

        return new String[]{
            // use this property to differentiate factory
            "openjpa.DataCache", cache + ", true(Name=" + confs1 + ")",
            "openjpa.QueryCache", queryCache,
            "openjpa.RemoteCommitProvider", fullProvider,
            "openjpa.BrokerImpl", CacheTestBroker.class.getName(),
        };
    }
}
