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
package org.apache.tomee.microprofile.tck.openapi;

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
import org.apache.tomee.microprofile.openapi.MicroProfileOpenApiEndpoint;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static io.smallrye.openapi.runtime.OpenApiProcessor.getFilter;
import static io.smallrye.openapi.runtime.OpenApiProcessor.modelFromAnnotations;
import static io.smallrye.openapi.runtime.OpenApiProcessor.modelFromReader;
import static io.smallrye.openapi.runtime.io.Format.JSON;
import static io.smallrye.openapi.runtime.io.Format.YAML;
import static io.smallrye.openapi.runtime.io.OpenApiSerializer.serialize;
import static java.lang.Thread.currentThread;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;

/**
 * Could probably be merged into the DeploymentProcessor. It's responsible for generating an OpenAPI document
 * and add it into the archive.
 */
public class MicroProfileOpenApiDeploymentProcessor implements ApplicationArchiveProcessor {
    private static Logger LOGGER = Logger.getLogger(MicroProfileOpenApiDeploymentProcessor.class.getName());
    public static volatile ClassLoader classLoader;

    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        if (archive instanceof WebArchive) {
            WebArchive war = (WebArchive) archive;

            generateOpenAPI(war);

            LOGGER.log(Level.FINE, () -> war.toString(true));
        }
    }

    /**
     * Builds the OpenAPI file and copies it to the deployed application.
     */
    private static void generateOpenAPI(final WebArchive war) {
        OpenApiConfig openApiConfig = config(war);
        Index index = index(war, openApiConfig);
        ClassLoader contextClassLoader = currentThread().getContextClassLoader();

        Optional<OpenAPI> annotationModel = ofNullable(modelFromAnnotations(openApiConfig, contextClassLoader, index));
        Optional<OpenAPI> readerModel = ofNullable(modelFromReader(openApiConfig, contextClassLoader));
        Optional<OpenAPI> staticFileModel = Stream.of(modelFromFile(war, "/META-INF/openapi.json", JSON),
                modelFromFile(war, "/META-INF/openapi.yaml", YAML),
                modelFromFile(war, "/META-INF/openapi.yml", YAML))
                .filter(Optional::isPresent)
                .findFirst()
                .flatMap(openAPI -> openAPI);

        OpenApiDocument document = OpenApiDocument.INSTANCE;
        document.reset();
        document.config(openApiConfig);
        annotationModel.ifPresent(document::modelFromAnnotations);
        readerModel.ifPresent(document::modelFromReader);
        staticFileModel.ifPresent(document::modelFromStaticFile);
        document.filter(getFilter(openApiConfig, contextClassLoader));
        document.initialize();
        OpenAPI openAPI = document.get();

        try {
            war.addAsManifestResource(new ByteArrayAsset(serialize(openAPI, JSON).getBytes(UTF_8)), "openapi.json");
            war.addAsManifestResource(new ByteArrayAsset(serialize(openAPI, YAML).getBytes(UTF_8)), "openapi.yaml");
        } catch (IOException e) {
            // Ignore
        }

        document.reset();
    }

    /**
     * Provides the Jandex index.
     */
    private static Index index(final WebArchive war, final OpenApiConfig config) {
        FilteredIndexView filteredIndexView = new FilteredIndexView(null, config);
        Indexer indexer = new Indexer();
        Collection<Node> classes = war.getContent(object -> object.get().endsWith(".class")).values();
        for (Node value : classes) {
            try {
                String resource = value.getPath().get().replaceAll("/WEB-INF/classes/", "");
                // We remove the OpenApinEndpoint so the /openapi is not generated
                if (resource.contains(MicroProfileOpenApiEndpoint.class.getSimpleName())) {
                    continue;
                }

                DotName dotName = DotName.createSimple(resource.replaceAll("/", ".").substring(0, resource.length() - 6));
                if (filteredIndexView.accepts(dotName)) {
                    indexer.index(MicroProfileOpenApiDeploymentProcessor.class.getClassLoader().getResourceAsStream(resource));
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
    private static OpenApiConfig config(final WebArchive war) {
        Optional<Node> microprofileConfig = Stream.of(ofNullable(war.get("/META-INF/microprofile-config.properties")),
                ofNullable(war.get("/WEB-INF/classes/META-INF/microprofile-config.properties")))
                .filter(Optional::isPresent)
                .findFirst()
                .flatMap(node -> node);

        if (!microprofileConfig.isPresent()) {
            return new OpenApiConfigImpl(ConfigProvider.getConfig());
        }

        Properties properties = new Properties();
        try (InputStreamReader reader = new InputStreamReader(microprofileConfig.get().getAsset().openStream(), UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SmallRyeConfig config = new SmallRyeConfigBuilder()
                .addDefaultSources()
                .addDefaultInterceptors()
                .withSources(new PropertiesConfigSource(properties, "microprofile-config.properties"))
                .build();

        return new OpenApiConfigImpl(config);
    }

    private static Optional<OpenAPI> modelFromFile(final WebArchive war, final String location,
            final Format format) {
        return ofNullable(war.get(location))
                .map(Node::getAsset)
                .map(asset -> new OpenApiStaticFile(asset.openStream(), format))
                .map(OpenApiProcessor::modelFromStaticFile);
    }
}
