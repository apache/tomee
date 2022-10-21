/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.microprofile.tck.fault.tolerance;

import io.smallrye.metrics.SharedMetricRegistries;
import org.apache.openejb.assembler.classic.event.AssemblerBeforeApplicationDestroyed;
import org.apache.openejb.observer.Observes;

public class CleanupMetricRegistries {
    public void beforeEachTestClass(@Observes AssemblerBeforeApplicationDestroyed event) {
        // TODO
        //  In MP FT 2.1, metrics are added to the "application" scope, which is automatically dropped
        //  by SmallRye Metrics when application is undeployed. Since MP FT 3.0, metrics are added to the "base" scope,
        //  which persists across application undeployments (see https://github.com/smallrye/smallrye-metrics/issues/12).
        //  However, MP FT TCK expects that this isn't the case. Specifically, AllMetricsTest and MetricsDisabledTest
        //  both use the same bean, AllMetricsBean, so if AllMetricsTest runs first, some histograms are created,
        //  and then MetricsDisabledTest fails, because those histograms are not expected to exist. Here, we drop all
        //  metric registries before each test class, to work around that.

        SharedMetricRegistries.dropAll();
    }
}
