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
import org.apache.geronimo.microprofile.metrics.common.jaxrs.MetricsEndpoints;
import org.apache.geronimo.microprofile.metrics.jaxrs.CdiMetricsEndpoints;
import org.apache.geronimo.microprofile.openapi.jaxrs.OpenAPIEndpoint;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.assembler.classic.event.AssemblerAfterApplicationCreated;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.event.BeforeEvent;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.annotation.Priority;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.WithAnnotations;

import static javax.interceptor.Interceptor.Priority.LIBRARY_BEFORE;

public class TomEEMicroProfileExtension implements Extension {
    private static final int BEFORE_MICROPROFILE_EXTENSIONS = LIBRARY_BEFORE - 10;

    private boolean requiresConfig;
    private boolean requiresJwt;
    private boolean requiresFaultTolerance;
    private boolean requiresMetrics;
    private boolean requiresHealth;
    private boolean requiresOpenApi;
    private boolean requiresOpenTracing;
    private boolean requiresRestClient;

    void beforeBeanDiscovery(@Observes
                             @Priority(BEFORE_MICROPROFILE_EXTENSIONS) final BeforeBeanDiscovery beforeBeanDiscovery) {
        this.requiresConfig = true;
        this.requiresJwt = true;
        this.requiresFaultTolerance = true;
        // MP Metrics is not required unless specific annotations are found (or additional REST enpoints are deployed)
        this.requiresMetrics = false;
        this.requiresHealth = true;
        this.requiresOpenApi = true;
        this.requiresOpenTracing = true;
        this.requiresRestClient = true;
    }

    void processMPAnnotatedTypes(@Observes
                                 @Priority(BEFORE_MICROPROFILE_EXTENSIONS)
                                 @WithAnnotations({
                                         Metric.class,
                                         Counted.class,
                                         Gauge.class,
                                         Metered.class,
                                         Timed.class
                                 }) final ProcessAnnotatedType<?> processAnnotatedType) {

        final AnnotatedType<?> annotatedType = processAnnotatedType.getAnnotatedType();
        if (annotatedType.getJavaClass().getName().startsWith("org.apache.geronimo.microprofile")) {
            return;
        }

        hasMetricsAnnotations(annotatedType);
    }

    void processMPInjectionPoints(@Observes
                                  @Priority(BEFORE_MICROPROFILE_EXTENSIONS)
                                  final ProcessInjectionPoint<?, ?> processInjectionPoint) {
        hasMetricsAnnotations(processInjectionPoint.getInjectionPoint().getAnnotated());
    }

    void processMPBeans(@Observes
                        @Priority(BEFORE_MICROPROFILE_EXTENSIONS)
                        final AfterBeanDiscovery afterBeanDiscovery,
                        final BeanManager beanManager) {
        /*
        final List<Interceptor<?>> interceptors =
                beanManager.resolveInterceptors(AROUND_INVOKE, new AnnotationLiteral<Counted>() {});
        interceptors.isEmpty();
        */
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

    private void hasMetricsAnnotations(final Annotated annotated) {
        if (requiresMetrics) {
            return;
        }

        requiresMetrics = annotated.isAnnotationPresent(Metric.class) ||
                          annotated.isAnnotationPresent(Counted.class) ||
                          annotated.isAnnotationPresent(Gauge.class) ||
                          annotated.isAnnotationPresent(Metered.class) ||
                          annotated.isAnnotationPresent(Timed.class);
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
                if (webApp.restApplications.isEmpty()) {
                    webApp.restClass.removeIf(className -> className.equals(HealthChecksEndpoint.class.getName()));
                    webApp.restClass.removeIf(className -> className.equals(CdiHealthChecksEndpoint.class.getName()));

                    webApp.restClass.removeIf(className -> className.equals(MetricsEndpoints.class.getName()));
                    webApp.restClass.removeIf(className -> className.equals(CdiMetricsEndpoints.class.getName()));

                    webApp.restClass.removeIf(className -> className.equals(OpenAPIEndpoint.class.getName()));
                }
            }
        }
    }
}
