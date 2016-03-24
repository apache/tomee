/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

final class NameFiltering {
    private static final List<String> INVALID = new ArrayList<String>();
    static { // for compatibility keep it
        INVALID.add("classes");
        INVALID.add("test-classes");
        INVALID.add("target");
        INVALID.add("build");
        INVALID.add("dist");
        INVALID.add("bin");
    }

    private NameFiltering() {
        // no-op
    }

    public static File filter(final File location) {
        final String lastName = location.getName();

        // maven
        if ("test-classes".equals(lastName) || "classes".equals(lastName)) {
            final File parentFile = location.getParentFile();
            if (parentFile != null && "target".equals(parentFile.getName())) {
                final File grandParentFile = parentFile.getParentFile();
                if (grandParentFile != null) {
                    return grandParentFile;
                }
            }
        }

        // gradle
        if ("test".equals(lastName) || "main".equals(lastName)) {
            final File parentFile = location.getParentFile();
            if (parentFile != null && "classes".equals(parentFile.getName())) {
                final File grandParentFile = parentFile.getParentFile();
                if (grandParentFile != null && "build".equals(grandParentFile.getName())) {
                    final File grandGrandParentFile = grandParentFile.getParentFile();
                    if (grandGrandParentFile != null) {
                        return grandGrandParentFile;
                    }
                }
            }
        }

        // common build folders + backward compatibility
        File current = location;
        while (current != null && INVALID.contains(current.getName())) {
            current = current.getParentFile();
        }
        return current == null ? location : current;
    }
}
