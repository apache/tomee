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

import java.util.Arrays;
import java.util.List;

import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.StringDistance;

/**
 * Strategies for persistent value updates.
 *
 * @author Abe White
 * @since 0.4.0
 */
public class UpdateStrategies {

    /**
     * No update strategy.
     */
    public static final int NONE = 0;

    /**
     * Ignore updates; field is always considered clean.
     */
    public static final int IGNORE = 1;

    /**
     * Throw an exception on attempt to update.
     */
    public static final int RESTRICT = 2;

    private static final Localizer _loc = Localizer.forPackage
        (UpdateStrategies.class);

    /**
     * Convert the given strategy to a name.
     */
    public static String getName(int strategy) {
        switch (strategy) {
            case NONE:
                return "none";
            case IGNORE:
                return "ignore";
            case RESTRICT:
                return "restrict";
            default:
                throw new IllegalArgumentException(String.valueOf(strategy));
        }
    }

    /**
     * Convert the given strategy name to its constant.
     */
    public static int getCode(String val, Object context) {
        if ("none".equals(val))
            return NONE;
        if ("ignore".equals(val))
            return IGNORE;
        if ("restrict".equals(val))
            return RESTRICT;

        List opts = Arrays.asList(new String[]{ "none", "ignore", "restrict" });
        String closest = StringDistance.getClosestLevenshteinDistance(val,
            opts, .5F);
        String msg;
        if (closest != null)
            msg = _loc.get("bad-update-strategy-hint", new Object[]{
                context, val, closest, opts }).getMessage();
        else
            msg = _loc.get("bad-update-strategy", context, val, opts)
                .getMessage();
        throw new IllegalArgumentException(msg);
	}
}
