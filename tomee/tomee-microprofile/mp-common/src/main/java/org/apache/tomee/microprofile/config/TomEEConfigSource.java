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
package org.apache.tomee.microprofile.config;

import org.apache.openejb.AppContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.AppFinder;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class TomEEConfigSource implements ConfigSource {
    private final Map<String, String> configuration = new HashMap<>();

    public TomEEConfigSource() {
        final AppContext appContextOrWeb =
                AppFinder.findAppContextOrWeb(Thread.currentThread().getContextClassLoader(),
                                              AppFinder.AppContextTransformer.INSTANCE);

        if (appContextOrWeb != null) {
            final List<String> mpIgnoredApps =
                    asList(SystemInstance.get()
                                         .getProperty("mp.ignored.apps", "ROOT,docs,host-manager,manager")
                                         .split(","));

            if (mpIgnoredApps.stream().anyMatch(s -> s.equalsIgnoreCase(appContextOrWeb.getId()))) {
                openTracingFilterActive(false);
                metricsJaxRsActive(false);
            }
        }

        final String mpScan = SystemInstance.get().getOptions().get("tomee.mp.scan", "none");
        if (mpScan.equals("none")) {
            openTracingFilterActive(false);
            metricsJaxRsActive(false);
        }
    }

    @Override
    public Map<String, String> getProperties() {
        return configuration;
    }

    @Override
    public String getValue(final String propertyName) {
        return configuration.get(propertyName);
    }

    @Override
    public String getName() {
        return TomEEConfigSource.class.getSimpleName();
    }

    public void openTracingFilterActive(final boolean active) {
        configuration.put("geronimo.opentracing.filter.active", Boolean.toString(active));
    }

    public void metricsJaxRsActive(final boolean active) {
        configuration.put("geronimo.metrics.jaxrs.activated", Boolean.toString(active));
    }
}
