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
package org.apache.openjpa.meta;

import org.apache.commons.collections.bidimap.TreeBidiMap;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.StringDistance;
import org.apache.openjpa.util.MetaDataException;

/**
 * Strategies for persistent value generation.
 *
 * @author Abe White
 * @since 0.4.0
 */
public class ValueStrategies {

    /**
     * No value strategy.
     */
    public static final int NONE = 0;

    /**
     * "native" value strategy.
     */
    public static final int NATIVE = 1;

    /**
     * "sequence" value strategy.
     */
    public static final int SEQUENCE = 2;

    /**
     * "autoassigned" value strategy.
     */
    public static final int AUTOASSIGN = 3;

    /**
     * "increment" value strategy.
     */
    public static final int INCREMENT = 4;

    /**
     * "uuid-string" value strategy.
     */
    public static final int UUID_STRING = 5;

    /**
     * "uuid-hex" value strategy.
     */
    public static final int UUID_HEX = 6;

    /**
     * "uuid-type4-string" value strategy.
     */
    public static final int UUID_TYPE4_STRING = 7;

    /**
     * "uuid-type4-hex" value strategy.
     */
    public static final int UUID_TYPE4_HEX = 8;

    private static final Localizer _loc = Localizer.forPackage
        (ValueStrategies.class);

    // table of names and strategies
    private static final TreeBidiMap _map = new TreeBidiMap();

    static {
        _map.put("none", NONE);
        _map.put("native", NATIVE);
        _map.put("sequence", SEQUENCE);
        _map.put("autoassign", AUTOASSIGN);
        _map.put("increment", INCREMENT);
        _map.put("uuid-string", UUID_STRING);
        _map.put("uuid-hex", UUID_HEX);
        _map.put("uuid-type4-string", UUID_TYPE4_STRING);
        _map.put("uuid-type4-hex", UUID_TYPE4_HEX);
    }

    /**
     * Convert the given strategy to a name.
     */
    public static String getName(int strategy) {
        Object code = strategy;
        String name = (String) _map.getKey(code);
        if (name != null)
            return name;
        throw new IllegalArgumentException(code.toString());
    }

    /**
     * Convert the given strategy name to its constant.
     */
    public static int getCode(String val, Object context) {
        if (val == null)
            return NONE;
        Object code = _map.get(val);
        if (code != null)
            return ((Number) code).intValue();

        // not a recognized strategy; check for typo
        String closest = StringDistance.getClosestLevenshteinDistance(val,
            _map.keySet(), .5F);
        String msg;
        if (closest != null)
            msg = _loc.get("bad-value-strategy-hint", new Object[]{
                context, val, closest, _map.keySet() }).getMessage();
        else
            msg = _loc.get("bad-value-strategy", context, val, _map.keySet())
                .getMessage();
        throw new IllegalArgumentException(msg);
    }

    /**
     * Assert that the given strategy is supported by the current runtime.
     */
    public static void assertSupported(int strategy, MetaDataContext context,
        String attributeName) {
        OpenJPAConfiguration conf = context.getRepository().getConfiguration();
        boolean supported = true;
        switch (strategy) {
            case AUTOASSIGN:
                supported = conf.supportedOptions().contains
                    (OpenJPAConfiguration.OPTION_VALUE_AUTOASSIGN);
                break;
            case INCREMENT:
                supported = conf.supportedOptions().contains
                    (OpenJPAConfiguration.OPTION_VALUE_INCREMENT);
                break;
            case NATIVE:
                supported = context instanceof ClassMetaData;
                break;
        }
        if (!supported)
            throw new MetaDataException(_loc.get("unsupported-value-strategy",
                context, getName(strategy), attributeName));
	}
}
