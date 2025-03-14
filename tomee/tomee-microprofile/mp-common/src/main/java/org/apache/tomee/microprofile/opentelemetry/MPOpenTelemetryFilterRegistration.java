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
package org.apache.tomee.microprofile.opentelemetry;

import io.smallrye.opentelemetry.implementation.rest.OpenTelemetryServerFilter;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;
import org.apache.openejb.loader.SystemInstance;

@Provider
public class MPOpenTelemetryFilterRegistration implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if ("none".equals(SystemInstance.get().getOptions().get("tomee.mp.scan", "none"))) {
            return;
        }

        if (context.getConfiguration().isRegistered(OpenTelemetryServerFilter.class)) {
            return;
        }

        try {
            final OpenTelemetryServerFilter serverFilter = CDI.current().select(OpenTelemetryServerFilter.class).get();
            if (serverFilter != null) {
                context.register(serverFilter);
            }
        } catch (IllegalStateException e) {
            // noop
        }
    }
}