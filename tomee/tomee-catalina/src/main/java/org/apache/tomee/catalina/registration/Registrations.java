/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.tomee.catalina.registration;

import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.tomcat.util.descriptor.web.FilterDef;

import java.lang.reflect.Constructor;
import java.util.Map;

public final class Registrations {
    private Registrations() {
        // no-op
    }

    // hack to force filter to get a config otherwise it is ignored in the http routing
    public static void addFilterConfig(final Context context, final FilterDef filterDef) {
        try {
            final Constructor<ApplicationFilterConfig> cons = ApplicationFilterConfig.class.getDeclaredConstructor(Context.class, FilterDef.class);
            if (!cons.isAccessible()) {
                cons.setAccessible(true);
            }
            final ApplicationFilterConfig config = cons.newInstance(context, filterDef);
            ((Map<String, ApplicationFilterConfig>) Reflections.get(context, "filterConfigs")).put(filterDef.getFilterName(), config);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
