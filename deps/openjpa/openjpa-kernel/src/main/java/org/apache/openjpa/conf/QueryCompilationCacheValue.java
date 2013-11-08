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

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.PluginValue;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.openjpa.lib.util.ParseException;
import org.apache.openjpa.util.CacheMap;

/**
 * A cache of compiled queries.
 *
 * @author Abe White
 * @since 0.9.6 (also existed in prior versions of Kodo)
 * @nojavadoc
 */
public class QueryCompilationCacheValue
    extends PluginValue {

    public static final String[] ALIASES = {
        "true", CacheMap.class.getName(),
        "all", ConcurrentHashMap.class.getName(),
        "false", null,
    };

    public QueryCompilationCacheValue(String prop) {
        super(prop, true);
        setAliases(ALIASES);
        setDefault(ALIASES[0]);
        setClassName(ALIASES[1]);
    }

    public Object newInstance(String clsName, Class type,
        Configuration conf, boolean fatal) {
        // make sure map handles concurrency
        Map map;
        
        try {
            map = (Map) super.newInstance(clsName, type, conf, fatal);
        } catch (ParseException pe) {
            // OPENJPA256: this class differs from most plugins in that
            // the plugin type is the standard java interface Map.class (rather
            // than an openjpa-specific interface), which means that the
            // ClassLoader used to load the implementation will be the system
            // class loader; this presents a problem if OpenJPA is not in the
            // system classpath, so work around the problem by catching
            // the ParseException (which is what we wrap the
            // ClassNotFoundException in) and try again, this time using
            // this class' ClassLoader.
            map = (Map) super.newInstance(clsName,
                QueryCompilationCacheValue.class, conf, fatal);
        } catch (IllegalArgumentException iae) {
            // OPENJPA256: this class differs from most plugins in that
            // the plugin type is the standard java interface Map.class (rather
            // than an openjpa-specific interface), which means that the
            // ClassLoader used to load the implementation will be the system
            // class loader; this presents a problem if OpenJPA is not in the
            // system classpath, so work around the problem by catching
            // the IllegalArgumentException (which is what we wrap the
            // ClassNotFoundException in) and try again, this time using
            // this class' ClassLoader.
            map = (Map) super.newInstance(clsName,
                QueryCompilationCacheValue.class, conf, fatal);
        }

        if (map != null && !(map instanceof Hashtable)
            && !(map instanceof CacheMap)
            && !(map instanceof
                    org.apache.openjpa.lib.util.concurrent.ConcurrentMap)
            && !(map instanceof java.util.concurrent.ConcurrentMap))
            map = Collections.synchronizedMap(map);
        return map;
	}
}
