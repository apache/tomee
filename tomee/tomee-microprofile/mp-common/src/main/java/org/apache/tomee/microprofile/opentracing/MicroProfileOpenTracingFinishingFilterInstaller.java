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
package org.apache.tomee.microprofile.opentracing;

import io.smallrye.opentracing.contrib.jaxrs2.server.SpanFinishingFilter;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.EnumSet;

@WebListener
public class MicroProfileOpenTracingFinishingFilterInstaller implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        final ServletContext servletContext = servletContextEvent.getServletContext();

        // Span finishing filter
        final FilterRegistration.Dynamic filterRegistration = servletContext.addFilter("tracingFilter", new SpanFinishingFilter());
        filterRegistration.setAsyncSupported(true);
        filterRegistration.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "*");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}