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

package org.apache.tomee.microprofile.config;

import org.apache.geronimo.microprofile.openapi.cdi.GeronimoOpenAPIExtension;
import org.apache.geronimo.microprofile.openapi.config.GeronimoOpenAPIConfig;
import org.apache.geronimo.microprofile.openapi.impl.filter.FilterImpl;
import org.apache.geronimo.microprofile.openapi.impl.loader.DefaultLoader;
import org.apache.geronimo.microprofile.openapi.impl.loader.yaml.Yaml;
import org.apache.geronimo.microprofile.openapi.impl.model.PathsImpl;
import org.apache.geronimo.microprofile.openapi.impl.processor.AnnotatedMethodElement;
import org.apache.geronimo.microprofile.openapi.impl.processor.AnnotatedTypeElement;
import org.apache.geronimo.microprofile.openapi.impl.processor.AnnotationProcessor;
import org.apache.geronimo.microprofile.openapi.impl.processor.spi.NamingStrategy;
import org.apache.geronimo.microprofile.openapi.jaxrs.JacksonOpenAPIYamlBodyWriter;
import org.eclipse.microprofile.openapi.OASConfig;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.OASModelReader;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

@SuppressWarnings("checkstyle:finalclass")
public class TomEEOpenAPIExtension extends GeronimoOpenAPIExtension implements Extension {

    private final Collection<Bean<?>> endpoints = new ArrayList<>();

    private final Map<Application, OpenAPI> openapis = new HashMap<>();

    private GeronimoOpenAPIConfig config;
    private AnnotationProcessor processor;
    private boolean skipScan;
    private Collection<String> classes;
    private Collection<String> packages;
    private Collection<String> excludePackages;
    private Collection<String> excludeClasses;
    private boolean jacksonIsPresent;

    void init(@Observes final BeforeBeanDiscovery beforeBeanDiscovery) {
        config = GeronimoOpenAPIConfig.create();
        processor = new AnnotationProcessor(config, loadNamingStrategy(config), null);
        skipScan = Boolean.parseBoolean(config.read(OASConfig.SCAN_DISABLE, "false"));
        classes = getConfigCollection(OASConfig.SCAN_CLASSES);
        packages = getConfigCollection(OASConfig.SCAN_PACKAGES);
        excludePackages = getConfigCollection(OASConfig.SCAN_EXCLUDE_PACKAGES);
        excludeClasses = getConfigCollection(OASConfig.SCAN_EXCLUDE_CLASSES);
        try {
            Yaml.getObjectMapper();
            jacksonIsPresent = true;
        } catch (final Error | RuntimeException e) {
            // no-op
        }
    }

    public MediaType getDefaultMediaType() {
        return jacksonIsPresent ? new MediaType("text", "vnd.yaml") : APPLICATION_JSON_TYPE;
    }

    private NamingStrategy loadNamingStrategy(final GeronimoOpenAPIConfig config) {
        return ofNullable(config.read("model.operation.naming.strategy", null))
                .map(String::trim)
                .filter(it -> !it.isEmpty())
                .map(it -> {
                    try {
                        return Thread.currentThread().getContextClassLoader().loadClass(it).getConstructor().newInstance();
                    } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException | ClassNotFoundException e) {
                        throw new IllegalArgumentException(e);
                    } catch (final InvocationTargetException ite) {
                        throw new IllegalArgumentException(ite.getTargetException());
                    }
                })
                .map(NamingStrategy.class::cast)
                .orElseGet(NamingStrategy.Default::new);
    }

    void vetoJacksonIfNotHere(@Observes final ProcessAnnotatedType<JacksonOpenAPIYamlBodyWriter> event) {
        if (!jacksonIsPresent) {
            event.veto();
        }
    }

    // adds a check to not register endpoints that have @RegisterRestClient
    <T> void findEndpointsAndApplication(@Observes final ProcessBean<T> event) {
        final String typeName = event.getAnnotated().getBaseType().getTypeName();
        if (classes == null && !skipScan && event.getAnnotated().isAnnotationPresent(Path.class) &&
                !event.getAnnotated().isAnnotationPresent(RegisterRestClient.class) &&
                !typeName.startsWith("org.apache.geronimo.microprofile.openapi.") &&
                (packages == null || packages.stream().anyMatch(typeName::startsWith))) {
            endpoints.add(event.getBean());
        }
    }

    public OpenAPI getOrCreateOpenAPI(final Application application) {
        if (classes != null) {
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            return openapis.computeIfAbsent(application,
                    app -> createOpenApi(application.getClass(), classes.stream().map(c -> {
                        try {
                            return loader.loadClass(c);
                        } catch (final ClassNotFoundException e) {
                            throw new IllegalArgumentException(e);
                        }
                    })));
        }
        if (packages == null && (!application.getSingletons().isEmpty() || !application.getClasses().isEmpty())) {
            return openapis.computeIfAbsent(application,
                    app -> createOpenApi(application.getClass(), Stream.concat(endpoints.stream().map(Bean::getBeanClass),
                            Stream.concat(app.getClasses().stream(), app.getSingletons().stream().map(Object::getClass)))));
        }
        return openapis.computeIfAbsent(application,
                app -> createOpenApi(application.getClass(), endpoints.stream().map(Bean::getBeanClass)));
    }

    private Collection<String> getConfigCollection(final String key) {
        return ofNullable(config.read(key, null))
                .map(vals -> Stream.of(vals.split(",")).map(String::trim).filter(v -> !v.isEmpty()).collect(toSet()))
                .orElse(null);
    }

    private OpenAPI createOpenApi(final Class<?> application, final Stream<Class<?>> beans) {
        final CDI<Object> current = CDI.current();
        final OpenAPI api = ofNullable(config.read(OASConfig.MODEL_READER, null))
                .map(value -> newInstance(current, value))
                .map(it -> OASModelReader.class.cast(it).buildModel())
                .orElseGet(() -> current.select(DefaultLoader.class).get().loadDefaultApi());

        final BeanManager beanManager = current.getBeanManager();
        processor.processApplication(api, new ElementImpl(beanManager.createAnnotatedType(application)));
        if (skipScan) {
            return api.paths(new PathsImpl());
        }

        // adds the context path to the base
        final Instance<ServletContext> servletContextInstance = current.select(ServletContext.class);
        final boolean appendContextPath = Boolean.valueOf(config.read("application.append-context-path", "true"));
        String contextPath = "";
        if (appendContextPath && !servletContextInstance.isAmbiguous() && !servletContextInstance.isUnsatisfied()) {
            contextPath = servletContextInstance.get().getContextPath();
        }

        final String base = contextPath + processor.getApplicationBinding(application);
        processor.beforeProcessing();
        beans.filter(c -> (excludeClasses == null || !excludeClasses.contains(c.getName())))
                .filter(c -> (excludePackages == null || excludePackages.stream().noneMatch(it -> c.getName().startsWith(it))))
                .map(beanManager::createAnnotatedType)
                .forEach(at -> processor.processClass(
                        base, api, new ElementImpl(at), at.getMethods().stream().map(MethodElementImpl::new)));

        return ofNullable(config.read(OASConfig.FILTER, null))
                .map(it -> newInstance(current, it))
                .map(i -> new FilterImpl(OASFilter.class.cast(i)).filter(api))
                .orElse(api);
    }

    private Object newInstance(final CDI<Object> current, final String value) {
        try {
            final Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(value.trim());
            try {
                final Instance<?> instance = current.select(clazz);
                if (!instance.isAmbiguous() && !instance.isUnsatisfied()) { // isResolvable is not always there
                    return instance.get();
                }
            } catch (final RuntimeException e) {
                // let do " new"
            }
            return clazz.getConstructor().newInstance();
        } catch (final Exception e) {
            throw new IllegalArgumentException("Can't load " + value, e);
        }
    }

    private static class MethodElementImpl extends ElementImpl implements AnnotatedMethodElement {

        private final AnnotatedMethod<?> delegate;

        private MethodElementImpl(final AnnotatedMethod<?> delegate) {
            super(delegate);
            this.delegate = delegate;
        }

        @Override
        public String getName() {
            return delegate.getJavaMember().getName();
        }

        @Override
        public Type getReturnType() {
            return delegate.getJavaMember().getGenericReturnType();
        }

        @Override
        public Class<?> getDeclaringClass() {
            return delegate.getDeclaringType().getJavaClass();
        }

        @Override
        public AnnotatedTypeElement[] getParameters() {
            return delegate.getParameters().stream().map(p -> new TypeElementImpl(p.getBaseType(), p)).toArray(TypeElementImpl[]::new);
        }
    }

    private static class TypeElementImpl extends ElementImpl implements AnnotatedTypeElement {

        private final Type type;

        private TypeElementImpl(final Type type, final Annotated delegate) {
            super(delegate);
            this.type = type;
        }

        @Override
        public Type getType() {
            return type;
        }
    }

    private static class ElementImpl implements AnnotatedElement {

        private final Annotated delegate;

        private ElementImpl(final Annotated annotated) {
            this.delegate = annotated;
        }

        @Override
        public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
            return delegate.getAnnotation(annotationClass);
        }

        @Override
        public Annotation[] getAnnotations() {
            return delegate.getAnnotations().toArray(new Annotation[0]);
        }

        @Override
        public <T extends Annotation> T[] getAnnotationsByType(final Class<T> annotationClass) {
            return delegate.getAnnotations(annotationClass).toArray((T[]) Array.newInstance(annotationClass, 0));
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return getAnnotations();
        }
    }
}