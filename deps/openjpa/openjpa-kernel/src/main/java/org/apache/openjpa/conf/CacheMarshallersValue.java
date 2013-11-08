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

import java.util.HashMap;
import java.util.Map;

import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.conf.PluginListValue;
import org.apache.openjpa.conf.NoOpCacheMarshaller;
import org.apache.openjpa.lib.conf.Configuration;

/**
 * A configuration value for handling and accessing cache marshallers.
 *
 * @since 1.1.0
 */
public class CacheMarshallersValue
    extends PluginListValue {

    private static final String KEY = "CacheMarshallers";
    private static final CacheMarshaller NO_OP_CACHE_MARSHALLER
        = new NoOpCacheMarshaller();
    private static final Localizer _loc =
        Localizer.forPackage(CacheMarshallersValue.class);

    private Configuration _conf;
    private Map<String,CacheMarshaller> _marshallers;
    private boolean _initialized;

    public CacheMarshallersValue(Configuration conf) {
        super(KEY);
        _conf = conf;
        setAlias("default", CacheMarshallerImpl.class.getName());
        setAlias("none", null);
        setDefault("none");
        setString("none");
        setScope(getClass());
    }

    public Object instantiate(Class<?> elemType, Configuration conf,
        boolean fatal) {
        CacheMarshaller[] ms = (CacheMarshaller[])
            super.instantiate(elemType, conf, fatal);
        if (ms != null) {
            _marshallers = new HashMap<String,CacheMarshaller>();
            for (int i = 0; i < ms.length; i++) {
                String mid = ms[i].getId();
                if (mid != null)
                    _marshallers.put(mid, ms[i]);
            }
        } else {
            _marshallers = null;
        }
        return ms;
    }

    /**
     * Return the {@link CacheMarshaller} to use for caching metadata of id
     * <code>id</code>. If no marshaller exists for the id, returns
     * {@link NoOpCacheMarshaller}.
     */
    public CacheMarshaller getMarshallerById(String id) {
        initialize();

        CacheMarshaller cm = (CacheMarshaller) _marshallers.get(id);
        if (cm == null) {
            if (getLog().isTraceEnabled())
                getLog().trace(_loc.get("cache-marshaller-not-found", id));
            return NO_OP_CACHE_MARSHALLER;
        } else {
            if (getLog().isTraceEnabled())
                getLog().trace(_loc.get("cache-marshaller-found", id,
                    cm.getClass().getName()));
            return cm;
        }
    }

    private Log getLog() {
        return _conf.getConfigurationLog();
    }

    /**
     * Return the {@link CacheMarshaller} to use for caching metadata of id
     * <code>id</code>. If no marshaller exists for the id, returns
     * {@link NoOpCacheMarshaller}.
     */
    public static CacheMarshaller getMarshallerById(Configuration c, String id){
        CacheMarshallersValue v =
            ((OpenJPAConfigurationImpl) c).cacheMarshallerPlugins;
        return v.getMarshallerById(id);
    }

    public Map<String,CacheMarshaller> getInstancesAsMap() {
        return _marshallers;
    }

    protected synchronized void initialize() {
        if (!_initialized) {
            instantiate(CacheMarshaller.class, _conf);
            _initialized = true;
        }
    }
}
