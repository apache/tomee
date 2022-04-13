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

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.config.event.EnhanceScannableUrlsEvent;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.observer.event.BeforeEvent;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomee.catalina.event.AfterApplicationCreated;
import org.apache.tomee.installer.Paths;
import org.apache.tomee.microprofile.health.MicroProfileHealthChecksEndpoint;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class TomEEMicroProfileListener {
    private static final String[] MICROPROFILE_LIBS_IMPLS_PREFIXES = new String[]{
            "mp-common" };

    private static final String[] MICROPROFILE_EXTENSIONS = new String[]{
            // kept until we move everything to Smallrye in case jars are still there by user choice.
            // "org.apache.geronimo.config.cdi.ConfigExtension",
            // "org.apache.safeguard.impl.cdi.SafeguardExtension",
            // "org.apache.geronimo.microprofile.impl.health.cdi.GeronimoHealthExtension",
            // "org.apache.geronimo.microprofile.metrics.cdi.MetricsExtension",
            // "org.apache.geronimo.microprofile.opentracing.microprofile.cdi.OpenTracingExtension",
            // "org.apache.geronimo.microprofile.openapi.cdi.GeronimoOpenAPIExtension",

            "org.apache.tomee.microprofile.jwt.cdi.MPJWTCDIExtension",
            "org.apache.cxf.microprofile.client.cdi.RestClientExtension",
            "io.smallrye.config.inject.ConfigExtension",
            "io.smallrye.metrics.setup.MetricCdiInjectionExtension",
            "io.smallrye.opentracing.SmallRyeTracingDynamicFeature",
            "io.smallrye.metrics.setup.MetricCdiInjectionExtension",
            };

    @SuppressWarnings("Duplicates")
    public void enhanceScannableUrls(@Observes final EnhanceScannableUrlsEvent enhanceScannableUrlsEvent) {

        final String mpScan = SystemInstance.get().getOptions().get("tomee.mp.scan", "none");

        if (mpScan.equals("none")) {
            Stream.of(MICROPROFILE_EXTENSIONS).forEach(
                    extension -> SystemInstance.get().setProperty(extension + ".active", "false"));

            return;
        }
        
        final List<URL> containerUrls = enhanceScannableUrlsEvent.getScannableUrls();

        for (final String extension : MICROPROFILE_EXTENSIONS) {
            try {
                CodeSource src = Class.forName(extension).getProtectionDomain().getCodeSource();
                if (src != null) {
                    containerUrls.add(src.getLocation());
                }
            } catch(final ClassNotFoundException | NoClassDefFoundError | IncompatibleClassChangeError e) {
                Logger.getInstance(LogCategory.OPENEJB.createChild("tomcat"), TomEEMicroProfileListener.class)
                      .error("Can't load MicroProfile extension " + extension, e);
                // ignored
            }
        }
        
        final Paths paths = new Paths(new File(System.getProperty("openejb.home")));
        for (final String prefix : MICROPROFILE_LIBS_IMPLS_PREFIXES) {
            final File file = paths.findTomEELibJar(prefix);
            if (file != null) {
                try {
                    containerUrls.add(file.toURI().toURL());
                } catch (final MalformedURLException e) {
                    // ignored
                }
            }
        }

        SystemInstance.get().setProperty("openejb.cxf-rs.cache-application", "false");
    }

    public void processApplication(@Observes final BeforeEvent<AfterApplicationCreated> afterApplicationCreated) {
        final ServletContext context = afterApplicationCreated.getEvent().getContext();
        final WebAppInfo webApp = afterApplicationCreated.getEvent().getWeb();

        // These remove duplicated REST API endpoints.
        webApp.restClass.removeIf(className -> className.equals(MicroProfileHealthChecksEndpoint.class.getName()));
        // webApp.restClass.removeIf(className -> className.equals(MetricsEndpoints.class.getName()));

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
                   webApp.restClass.removeIf(className -> className.equals(MicroProfileHealthChecksEndpoint.class.getName()));
                   // webApp.restClass.removeIf(className -> className.equals(CdiMetricsEndpoints.class.getName()));
                   // webApp.restClass.removeIf(className -> className.equals(OpenAPIEndpoint.class.getName()));
               });

    }
}
