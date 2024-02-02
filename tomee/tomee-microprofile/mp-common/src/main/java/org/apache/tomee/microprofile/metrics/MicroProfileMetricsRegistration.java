/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
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
package org.apache.tomee.microprofile.metrics;

import io.smallrye.metrics.jaxrs.JaxRsMetricsServletFilter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;

import java.util.Set;

/**
 * Responsible for adding the SmallRye metrics filter in the chain to catch all servlet + JAX RS calls. It also
 * adds the Metrics servlet endpoint to render the results.
 *
 */
// todo do we want to be so restrictive with the HandlesTypes annotation for @Path @ApplicationPath @Servlet ... ?
public class MicroProfileMetricsRegistration implements ServletContainerInitializer {

    @Override
    public void onStartup(final Set<Class<?>> classes, final ServletContext ctx) throws ServletException {
        final FilterRegistration.Dynamic metricsServletFilter = ctx.addFilter("mp-metrics-filter", JaxRsMetricsServletFilter.class);
        metricsServletFilter.setAsyncSupported(true);
        metricsServletFilter.addMappingForUrlPatterns(null, false, "/*");

        final ServletRegistration.Dynamic servletRegistration = ctx.addServlet("mp-metrics-servlet", MicroProfileMetricsEndpoint.class);
        servletRegistration.addMapping("/metrics/*");
    }

}