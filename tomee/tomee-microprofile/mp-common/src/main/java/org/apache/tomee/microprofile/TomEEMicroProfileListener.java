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

import io.smallrye.opentracing.SmallRyeTracingDynamicFeature;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.config.event.EnhanceScannableUrlsEvent;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.observer.event.BeforeEvent;
import org.apache.openejb.server.cxf.rs.event.ExtensionProviderRegistration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomee.catalina.event.AfterApplicationCreated;
import org.apache.tomee.installer.Paths;
import org.apache.tomee.microprofile.health.MicroProfileHealthChecksEndpoint;
import org.apache.tomee.microprofile.openapi.MicroProfileOpenApiRegistration;
import org.apache.tomee.microprofile.opentracing.MicroProfileOpenTracingExceptionMapper;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TomEEMicroProfileListener {

    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB.createChild("tomcat"), TomEEMicroProfileListener.class);

    private static final String[] MICROPROFILE_LIBS_IMPLS_PREFIXES = new String[]{
        "mp-common"
    };

    private static final String[] MICROPROFILE_EXTENSIONS = new String[]{
        "org.apache.tomee.microprofile.jwt.cdi.MPJWTCDIExtension",
        "org.apache.cxf.microprofile.client.cdi.RestClientExtension",
        "io.smallrye.config.inject.ConfigExtension",
        "io.smallrye.metrics.setup.MetricCdiInjectionExtension",
        "io.smallrye.opentracing.SmallRyeTracingDynamicFeature",
        "io.smallrye.opentracing.contrib.interceptor.OpenTracingInterceptor",
        "io.smallrye.faulttolerance.FaultToleranceExtension",
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
                final CodeSource src = Class.forName(extension).getProtectionDomain().getCodeSource();
                if (src != null) {
                    containerUrls.add(src.getLocation());
                }
            } catch (final ClassNotFoundException | NoClassDefFoundError | IncompatibleClassChangeError e) {
                LOGGER.error("Can't load MicroProfile extension " + extension, e);
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
                   webApp.restClass.removeIf(
                       className -> className.equals(MicroProfileHealthChecksEndpoint.class.getName()));
               });

        // we need to register the OpenAPI servlet, but in order to generate the OpenAPI model, SmallRye uses Jandex,
        // a XBean Finder equivalent from JBoss. This seems to be a library used in many placed, so we want to build the
        // index only once and pass it everywhere it's needed. Also in order to build the index, we need the entire
        // application so doing it here from the AppInfo is way simpler
        try {
            final Index index = of(afterApplicationCreated.getEvent().getApp().libs);
            MicroProfileOpenApiRegistration.registerOpenApiServlet(context, index);

        } catch (final IOException e) {
            throw new IllegalStateException("Can't build Jandex index for application " + webApp.contextRoot, e);
        }

    }

    public void registerMicroProfileJaxRsProviders(@Observes final ExtensionProviderRegistration extensionProviderRegistration) {
        extensionProviderRegistration.getProviders().add(new SmallRyeTracingDynamicFeature());

        // The OpenTracing TCK tests that an exception is turned into a 500. JAX-RS 3.1 mandates a default mapper
        // which was not required on the current versions; see TOMEE-4133 for details.
        extensionProviderRegistration.getProviders().add(new MicroProfileOpenTracingExceptionMapper());
    }

    /**
     * Constructs an Index of the passed files and directories. Files may be class files or JAR files.
     * Directories are scanned for class files recursively. This is a copy of the Index.of() implementation which does
     * not handle directory recursively
     *
     * @param files class files, JAR files or directories containing class files to index
     * @return the index
     * @throws IllegalArgumentException if any passed {@code File} is null or not a class file, JAR file or directory
     */
    public static Index of(final List<String> files) throws IOException {
        final Indexer indexer = new Indexer();
        if (files == null) {
            return indexer.complete();
        }

        final Set<File> fileSet = files.stream().map(File::new).filter(File::exists).collect(Collectors.toSet());
        for (File file : fileSet) {
            if (file == null) {
                throw new IllegalArgumentException("File must not be null");
            } else if (file.isDirectory()) {
                List<File> classFiles = Files.collect(file, new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getName().endsWith(".class");
                    }
                });

                for (File classFile : classFiles) {
                    try (InputStream in = new FileInputStream(classFile)) {
                        indexer.index(in);
                    }
                }
            } else if (file.isFile() && file.getName().endsWith(".class")) {
                try (InputStream in = new FileInputStream(file)) {
                    indexer.index(in);
                }
            } else if (file.isFile() && file.getName().endsWith(".jar")) {
                if (NewLoaderLogic.skip(file.toURI().toURL())) {
                    continue;
                }
                try (JarFile jarFile = new JarFile(file)) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().endsWith(".class")) {
                            try (InputStream in = jarFile.getInputStream(entry)) {
                                indexer.index(in);
                            }
                        }
                    }
                }
            } else {
                LOGGER.warning("Can't add to Jandex index. Not a class file, JAR file or directory: " + file);
            }
        }

        return indexer.complete();
    }


}
