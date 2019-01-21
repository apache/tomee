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
package org.apache.tomee.microprofile;

import org.apache.geronimo.microprofile.common.jaxrs.HealthChecksEndpoint;
import org.apache.geronimo.microprofile.impl.health.cdi.CdiHealthChecksEndpoint;
import org.apache.geronimo.microprofile.metrics.common.jaxrs.MetricsEndpoints;
import org.apache.geronimo.microprofile.metrics.jaxrs.CdiMetricsEndpoints;
import org.apache.geronimo.microprofile.openapi.jaxrs.OpenAPIEndpoint;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.observer.event.BeforeEvent;
import org.apache.tomee.catalina.event.AfterApplicationCreated;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.Collection;

public class TomEEMicroProfileListener {
    public void processApplication(@Observes final BeforeEvent<AfterApplicationCreated> afterApplicationCreated) {
        final ServletContext context = afterApplicationCreated.getEvent().getContext();
        final WebAppInfo webApp = afterApplicationCreated.getEvent().getWeb();

        // These remove duplicated REST API endpoints.
        webApp.restClass.removeIf(className -> className.equals(HealthChecksEndpoint.class.getName()));
        webApp.restClass.removeIf(className -> className.equals(MetricsEndpoints.class.getName()));

        // There remove all of MP REST API endpoint if there is a servlet already registered in /*. The issue here is
        // that REST path has priority over servlet and there may override old applications that have servlets
        // with /* mapping.
        context.getServletRegistrations()
               .values()
               .stream()
               .map(ServletRegistration::getMappings)
               .flatMap(Collection::stream)
               .filter(mapping -> mapping.equals("/*"))
               .findFirst()
               .ifPresent(mapping -> {
                   webApp.restClass.removeIf(className -> className.equals(CdiHealthChecksEndpoint.class.getName()));
                   webApp.restClass.removeIf(className -> className.equals(CdiMetricsEndpoints.class.getName()));
                   webApp.restClass.removeIf(className -> className.equals(OpenAPIEndpoint.class.getName()));
               });
    }
}
