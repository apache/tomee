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
package org.apache.tomee.microprofile.health;

import io.smallrye.health.ResponseProvider;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Dependent
public class MicroProfileHealthReporterProducer {

    @Produces
    @ApplicationScoped
    public MicroProfileHealthReporter reporter() {
        final String emptyLivenessChecksStatus = "UP";
        final String emptyReadinessChecksStatus = "UP";
        final String emptyStartupChecksStatus = "UP";

        // MicroProfile Health supports the mp.health.disable-default-procedures to let users disable any vendor procedures
        final boolean defaultServerProceduresDisabled = ConfigProvider.getConfig().getOptionalValue("mp.health.disable-default-procedures", Boolean.class).orElse(false);

        // MicroProfile Health supports the mp.health.default.readiness.empty.response to let users specify default empty readiness responses
        final String defaultReadinessEmptyResponse = ConfigProvider.getConfig().getOptionalValue("mp.health.default.readiness.empty.response", String.class).orElse("DOWN");

        // MicroProfile Health supports the mp.health.default.startup.empty.response to let users specify default empty startup responses
        final String defaultStartupEmptyResponse = ConfigProvider.getConfig().getOptionalValue("mp.health.default.startup.empty.response", String.class).orElse("DOWN");

        MicroProfileHealthReporter healthReporter = new MicroProfileHealthReporter(emptyLivenessChecksStatus, emptyReadinessChecksStatus,
                                                        emptyStartupChecksStatus, defaultServerProceduresDisabled,
                                                        defaultReadinessEmptyResponse, defaultStartupEmptyResponse);

        if (!defaultServerProceduresDisabled) {
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();

            // todo add our own server checks
        }

        HealthCheckResponse.setResponseProvider(new ResponseProvider());
        return healthReporter;
    }

    @PreDestroy
    public void removeResponseProvider() {
        HealthCheckResponse.setResponseProvider(null);
    }

}