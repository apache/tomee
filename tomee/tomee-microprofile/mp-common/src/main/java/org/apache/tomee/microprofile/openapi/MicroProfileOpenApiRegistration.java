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
package org.apache.tomee.microprofile.openapi;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.OpenApiStaticFile;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.scanner.FilteredIndexView;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.annotation.HandlesTypes;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Path;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomee.microprofile.health.MicroProfileHealthChecksEndpoint;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import static io.smallrye.openapi.runtime.OpenApiProcessor.getFilter;
import static io.smallrye.openapi.runtime.OpenApiProcessor.modelFromAnnotations;
import static io.smallrye.openapi.runtime.OpenApiProcessor.modelFromReader;
import static io.smallrye.openapi.runtime.io.Format.JSON;
import static io.smallrye.openapi.runtime.io.Format.YAML;
import static java.lang.Thread.currentThread;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;

/**
 *
 * This servlet container initializer is responsible for registering the OpenAPI servlet in each application so /openapi
 * becomes available for applications to consume.
 *
 * It will first try to load static openapi files in format JSON, YAML and YML. If not found, it will try to generate the
 * model on the fly for the endpoints.
 *
 */
// we'll get all the classes of the application and when creating the index we'll apply the filtering
// we could have used only @Path from JAX RS because in the end, we only need those to generate the OpenAPI Model
@HandlesTypes({
    Path.class,
    ApplicationPath.class
})
public class MicroProfileOpenApiRegistration implements ServletContainerInitializer {

    private static final Logger LOGGER = Logger.getInstance(LogCategory.MICROPROFILE, MicroProfileOpenApiRegistration.class);

    @Override
    public void onStartup(final Set<Class<?>> classes, final ServletContext ctx) throws ServletException {

        LOGGER.info("Registering OpenAPI servlet on /openapi for application " + ctx.getContextPath());
        final ServletRegistration.Dynamic servletRegistration =
            ctx.addServlet("mp-openapi-servlet", MicroProfileOpenApiEndpoint.class);
        servletRegistration.addMapping("/openapi/*");

        // generate the OpenAPI document from static file or model reader or from annotations
        final Optional<OpenAPI> openAPI = generateOpenAPI(classes, ctx);
        openAPI.ifPresent(openApi -> setOpenApi(ctx, openAPI.get()));
    }

    /**
     * Builds the OpenAPI file and copies it to the deployed application.
     *
     * @return The generated OpenAPI model wrapped into an Optional
     */
    private Optional<OpenAPI> generateOpenAPI(final Set<Class<?>> classes, final ServletContext servletContext) {
        final OpenApiConfig openApiConfig = config(servletContext);
        final Index index = index(classes, servletContext, openApiConfig);
        final ClassLoader contextClassLoader = currentThread().getContextClassLoader();

        Optional<OpenAPI> annotationModel = ofNullable(modelFromAnnotations(openApiConfig, contextClassLoader, index));
        Optional<OpenAPI> readerModel = ofNullable(modelFromReader(openApiConfig, contextClassLoader));
        Optional<OpenAPI> staticFileModel = openApiFromStaticFile(servletContext);

        final OpenApiDocument document = OpenApiDocument.INSTANCE;
        try {
            document.reset();
            document.config(openApiConfig);
            annotationModel.ifPresent(document::modelFromAnnotations);
            readerModel.ifPresent(document::modelFromReader);
            staticFileModel.ifPresent(document::modelFromStaticFile);
            document.filter(getFilter(openApiConfig, contextClassLoader));
            document.initialize();
            return Optional.ofNullable(document.get());

        } finally {
            document.reset();
        }
    }

    /**
     * Provides the Jandex index.
     */
    private static Index index(final Set<Class<?>> classes, final ServletContext servletContext, final OpenApiConfig config) {
        FilteredIndexView filteredIndexView = new FilteredIndexView(null, config);
        Indexer indexer = new Indexer();

        // todo load all classes and build up an index with the content
        for (Class clazz : classes) {
            try {
                // We remove the OpenApinEndpoint so the /openapi is not generated
                if (clazz.equals(MicroProfileOpenApiEndpoint.class)
                || clazz.equals(MicroProfileHealthChecksEndpoint.class)) {
                    continue;
                }

                final DotName dotName = DotName.createSimple(clazz.getName());
                if (filteredIndexView.accepts(dotName)) {
                    indexer.indexClass(clazz);
                }
            } catch (IOException e) {
                // Ignore
            }
        }

        return indexer.complete();
    }

    /**
     * Creates the config from the microprofile-config.properties file in the application. The spec defines that the
     * config file may be present in two locations.
     */
    private static OpenApiConfig config(final ServletContext servletContext) {
        try {
            final Optional<URL> microprofileConfig = Stream.of(ofNullable(servletContext.getResource("/META-INF/microprofile-config.properties")),
                                                          ofNullable(servletContext.getResource("/WEB-INF/classes/META-INF/microprofile-config.properties")))
                                                      .filter(Optional::isPresent)
                                                      .findFirst()
                                                      .flatMap(url -> url);

            if (microprofileConfig.isEmpty()) {
                LOGGER.debug("Could not find OpenAPI config from MicroProfile Config files. Using default configuration.");
                return new OpenApiConfigImpl(ConfigProvider.getConfig());
            }

            LOGGER.debug("Building OpenAPI config from MicroProfile Config file " + microprofileConfig.get().toExternalForm());
            final Properties properties = IO.readProperties(microprofileConfig.get());

            final SmallRyeConfig config = new SmallRyeConfigBuilder()
                .addDefaultSources()
                .addDefaultInterceptors()
                .withSources(new PropertiesConfigSource(properties, "microprofile-config.properties"))
                .build();

            return new OpenApiConfigImpl(config);

        } catch (final IOException e) {
            LOGGER.error("Failed loading OpenAPI config from MicroProfile config file. Using default configuration", e);
            return new OpenApiConfigImpl(ConfigProvider.getConfig());
        }
    }

    private Optional<OpenAPI> openApiFromStaticFile(final ServletContext servletContext) {
        try {

            // look for static files already provided by the application
            final Optional<OpenAPI> staticOpenApi = Stream
                .of(readOpenApiFile(servletContext, "/META-INF/openapi.json", JSON),
                    readOpenApiFile(servletContext, "/META-INF/openapi.yaml", YAML),
                    readOpenApiFile(servletContext, "/META-INF/openapi.yml", YAML))
                .filter(Optional::isPresent)
                .findFirst()
                .flatMap(file -> file);

            if (staticOpenApi.isEmpty()) {
                LOGGER.debug("Could not find any static OpenAPI file in application " + servletContext.getContextPath());
            }

            return staticOpenApi;

        } catch (final Exception e) {
            LOGGER.error("Failed loading static OpenAPI files in application " + servletContext.getContextPath(), e);
            return Optional.empty();
        }
    }


    private Optional<OpenAPI> readOpenApiFile(
        final ServletContext servletContext,
        final String location,
        final Format format)
        throws Exception {

        final URL resource = servletContext.getResource(location);
        if (resource == null) {
            LOGGER.debug("Could not find static OpenAPI file " + location);
            return Optional.empty();
        }

        LOGGER.debug("Found static OpenAPI file " + location);

        final OpenApiDocument document = OpenApiDocument.INSTANCE;
        try (OpenApiStaticFile staticFile = new OpenApiStaticFile(resource.openStream(), format)) {
            Config config = ConfigProvider.getConfig();
            OpenApiConfig openApiConfig = new OpenApiConfigImpl(config);
            document.reset();
            document.config(openApiConfig);
            document.filter(OpenApiProcessor.getFilter(openApiConfig, Thread.currentThread().getContextClassLoader()));
            document.modelFromStaticFile(OpenApiProcessor.modelFromStaticFile(staticFile));
            document.initialize();
            return Optional.of(document.get());

        } finally {
            document.reset();
        }
    }

    // helper methods also used by the Servlet to retrieve the model and render it

    public static void setOpenApi(final ServletContext servletContext, final OpenAPI openAPI) {
        Objects.requireNonNull(servletContext);
        Objects.requireNonNull(openAPI);
        servletContext.setAttribute(MicroProfileOpenApiRegistration.class.getName() + ".OpenAPI", openAPI);
    }

    public static OpenAPI getOpenApi(final ServletContext servletContext) {
        Objects.requireNonNull(servletContext);
        return (OpenAPI) servletContext.getAttribute(MicroProfileOpenApiRegistration.class.getName() + ".OpenAPI");
    }

}