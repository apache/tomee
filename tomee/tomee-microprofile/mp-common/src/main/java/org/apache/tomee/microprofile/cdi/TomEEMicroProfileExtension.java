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
package org.apache.tomee.microprofile.cdi;

import org.apache.geronimo.microprofile.common.jaxrs.HealthChecksEndpoint;
import org.apache.geronimo.microprofile.impl.health.cdi.CdiHealthChecksEndpoint;
import org.apache.geronimo.microprofile.impl.health.cdi.GeronimoHealthExtension;
import org.apache.geronimo.microprofile.metrics.common.jaxrs.MetricsEndpoints;
import org.apache.geronimo.microprofile.metrics.jaxrs.CdiMetricsEndpoints;
import org.apache.geronimo.microprofile.openapi.jaxrs.OpenAPIEndpoint;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.assembler.classic.event.AssemblerAfterApplicationCreated;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.event.BeforeEvent;

import javax.annotation.Priority;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;
import java.util.Collection;
import java.util.Optional;

import static javax.interceptor.Interceptor.Priority.PLATFORM_AFTER;

public class TomEEMicroProfileExtension implements Extension {
    private boolean requiresConfig;
    private boolean requiresJwt;
    private boolean requiresFaultTolerance;
    private boolean requiresMetrics;
    private boolean requiresHealth;
    private boolean requiresOpenApi;
    private boolean requiresOpenTracing;
    private boolean requiresRestClient;

    void afterDeploymentValidation(
            @Observes
            @Priority(PLATFORM_AFTER + 10)
            final AfterDeploymentValidation afterDeploymentValidation,
            final BeanManager beanManager) {
        requiresConfig = true;

        requiresJwt = true;

        requiresFaultTolerance = true;

        requiresMetrics = false;

        final GeronimoHealthExtension healthExtension = beanManager.getExtension(GeronimoHealthExtension.class);
        requiresHealth = !Optional.ofNullable(healthExtension.getChecks()).map(Collection::isEmpty).orElse(true);

        requiresOpenApi = false;

        requiresOpenTracing = false;

        requiresRestClient = false;
    }

    public boolean requiresConfig() {
        return requiresConfig;
    }

    public boolean requiresJwt() {
        return requiresJwt;
    }

    public boolean requiresFaultTolerance() {
        return requiresFaultTolerance;
    }

    public boolean requiresMetrics() {
        return requiresMetrics;
    }

    public boolean requiresHealth() {
        return requiresHealth;
    }

    public boolean requiresOpenApi() {
        return requiresOpenApi;
    }

    public boolean requiresOpenTrakcing() {
        return requiresOpenTracing;
    }

    public boolean requiresRestClient() {
        return requiresRestClient;
    }

    static {
        SystemInstance.get().addObserver(new TomEEMicroProfileAfterApplicationCreated());
    }

    public static class TomEEMicroProfileAfterApplicationCreated {
        public void processApplication(
                @org.apache.openejb.observer.Observes
                final BeforeEvent<AssemblerAfterApplicationCreated> afterApplicationCreated) {

            final TomEEMicroProfileExtension microProfileExtension =
                    CDI.current().getBeanManager().getExtension(TomEEMicroProfileExtension.class);
            final AppInfo app = afterApplicationCreated.getEvent().getApp();
            for (final WebAppInfo webApp : app.webApps) {
                webApp.restClass.removeIf(className -> className.equals(HealthChecksEndpoint.class.getName()));
                webApp.restClass.removeIf(className -> className.equals(MetricsEndpoints.class.getName()));

                if (webApp.restApplications.isEmpty()) {
                    if (!microProfileExtension.requiresHealth()) {
                        webApp.restClass.removeIf(
                                className -> className.equals(CdiHealthChecksEndpoint.class.getName()));
                    }

                    if (!microProfileExtension.requiresMetrics()) {
                        webApp.restClass.removeIf(className -> className.equals(CdiMetricsEndpoints.class.getName()));
                    }

                    if (!microProfileExtension.requiresOpenApi()) {
                        webApp.restClass.removeIf(className -> className.equals(OpenAPIEndpoint.class.getName()));
                    }
                }
            }
        }
    }
}
