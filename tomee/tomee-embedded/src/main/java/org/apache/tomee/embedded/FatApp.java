/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.embedded;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.Arrays.asList;

// Main like forcing --as-war --single-classloader
// common for fatjars
public final class FatApp {
    public static void main(final String[] args) {
        final Collection<String> a = args == null || args.length == 0 ? new ArrayList<>() : new ArrayList<>(asList(args));
        if (a.size() == 1 && "--help".equals(a.iterator().next())) {
            Main.main(new String[] {"--help"});
            return;
        }

        if (!a.contains("--as-war")) {
            a.add("--as-war");
        }
        if (!a.contains("--single-classloader")) {
            a.add("--single-classloader");
        }
        if (Thread.currentThread().getContextClassLoader().getResource("tomee-embedded.properties") != null) { // automatic
            a.add("--configuration-location=tomee-embedded.properties");
        }
        Main.main(a.toArray(new String[a.size()]));
    }

    private FatApp() {
        // no-op
    }
}
