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
package org.apache.openjpa.lib.meta;

import java.security.AccessController;

import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;

/**
 * Utility class for performing common operations on {@link SourceTracker}s.
 *
 * @since 0.3.3.1
 */
public class SourceTrackers {

    private static final Localizer _loc =
        Localizer.forPackage(SourceTrackers.class);

    private static final String SEP = J2DoPrivHelper.getLineSeparator();

    /**
     * Create a message appropriate for display to the user describing
     * the location(s) that <code>trackers</code> were loaded from.
     *
     * @param trackers the source-trackers for which location info should
     * be provided.
     */
    public static String getSourceLocationMessage(SourceTracker[] trackers) {
        StringBuilder buf = new StringBuilder(20 * (trackers.length + 1));
        buf.append(_loc.get("source-trackers-location-header")).append(SEP);
        String sourceFilePath;
        for (int i = 0; i < trackers.length; i++) {
            sourceFilePath = (trackers[i].getSourceFile() == null ?
                _loc.get("source-tracker-file-unknown").getMessage() :
                AccessController.doPrivileged(
                    J2DoPrivHelper.getAbsolutePathAction(
                        trackers[i].getSourceFile())));
            buf.append("  ").append(_loc.get(
                "source-trackers-location-line-item",
                trackers[i].getResourceName(), sourceFilePath));
            if (i < trackers.length - 1)
                buf.append(SEP);
        }
        return buf.toString();
    }
}
