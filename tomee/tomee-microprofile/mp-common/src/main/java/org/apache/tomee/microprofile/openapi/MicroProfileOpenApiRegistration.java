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

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.OpenApiStaticFile;
import io.smallrye.openapi.runtime.io.Format;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.models.OpenAPI;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static io.smallrye.openapi.runtime.io.Format.JSON;
import static io.smallrye.openapi.runtime.io.Format.YAML;

/**
 * Responsible for adding the filter into the chain and doing all other initialization
 * <p>
 * todo do we want to be so restrictive with the HandlesTypes annotation?
 *
 * @HandlesTypes({Path.class, WebServlet.class,
 * WebFilter.class
 * })
 */
public class MicroProfileOpenApiRegistration implements ServletContainerInitializer {

    @Override
    public void onStartup(final Set<Class<?>> classes, final ServletContext ctx) throws ServletException {
        final ServletRegistration.Dynamic servletRegistration =
            ctx.addServlet("mp-openapi-servlet", MicroProfileOpenApiEndpoint.class);
        servletRegistration.addMapping("/openapi/*");

        openApi(ctx);
    }

    private void openApi(final ServletContext servletContext) {
        try {
            Optional<OpenAPI> staticOpenApi = Stream
                .of(readOpenApiFile(servletContext, "/META-INF/openapi.json", JSON),
                    readOpenApiFile(servletContext, "/META-INF/openapi.yaml", YAML),
                    readOpenApiFile(servletContext, "/META-INF/openapi.yml", YAML))
                .filter(Optional::isPresent)
                .findFirst()
                .flatMap(file -> file);

            staticOpenApi.ifPresent(openAPI -> setOpenApi(servletContext,openAPI));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setOpenApi(final ServletContext servletContext, final OpenAPI openAPI) {
        Objects.requireNonNull(servletContext);
        Objects.requireNonNull(openAPI);
        servletContext.setAttribute(MicroProfileOpenApiRegistration.class.getName() + ".OpenAPI", openAPI);
    }

    public static OpenAPI getOpenApi(final ServletContext servletContext) {
        Objects.requireNonNull(servletContext);
        return (OpenAPI) servletContext.getAttribute(MicroProfileOpenApiRegistration.class.getName() + ".OpenAPI");
    }

    private Optional<OpenAPI> readOpenApiFile(
        final ServletContext servletContext,
        final String location,
        final Format format)
        throws Exception {

        final URL resource = servletContext.getResource(location);
        if (resource == null) {
            return Optional.empty();
        }

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

}