/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openjpa.datacache;

import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * A default implementation that selects the cache by the type of the given managed instance.
 * The name of the cache is determined by {@link ClassMetaData#getDataCacheName() name as specified} by
 * the metadata. 
 * 
 * @see ClassMetaData#getDataCacheName()
 *
 */
public class DefaultCacheDistributionPolicy implements CacheDistributionPolicy {
    public String selectCache(OpenJPAStateManager sm, Object context) {
        return sm.getMetaData().getDataCacheName();
        
    }

    public void endConfiguration() {
    }

    public void setConfiguration(Configuration conf) {
    }

    public void startConfiguration() {
    }
}
