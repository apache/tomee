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
package org.apache.openjpa.conf;

import java.util.Map;

import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.PluginValue;
import org.apache.openjpa.conf.CacheMarshallersValue;
import org.apache.openjpa.meta.MetaDataRepository;

/**
 * A {@link PluginValue} that interacts with the {@link CacheMarshaller}
 * to cache the metadata repository between executions.
 *
 * @since 1.1.0
 */
public class MetaDataRepositoryValue
    extends PluginValue {

    private static final String KEY = "MetaDataRepository";

    public MetaDataRepositoryValue() {
        super(KEY, false);
        String[] aliases = new String[] {
            "default",
            MetaDataRepository.class.getName()
        };
        setAliases(aliases);
        setDefault(aliases[0]);
        setString(aliases[0]);
    }

    public Object instantiate(Class type, Configuration c, boolean fatal) {
        MetaDataRepository repos = null;
        OpenJPAConfiguration conf = (OpenJPAConfiguration) c;

        Object[] os = (Object[]) CacheMarshallersValue.getMarshallerById(
            conf, MetaDataCacheMaintenance.class.getName())
            .load();
        if (os != null) {
            repos = (MetaDataRepository) os[0];
            if (os[1] != null)
                // It's a bit odd that we do this in MetaDataRepositoryValue.
                // We need to serialize all the various bits of configuration
                // together; maybe we can move the caching logic somewhere
                // else?
                conf.getQueryCompilationCacheInstance().putAll((Map) os[1]);
        }

        if (repos == null)
            return super.instantiate(type, c, fatal);
        else
            return repos;
    }


}
