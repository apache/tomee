package org.apache.tomee.microprofile.config;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

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

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

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
import org.apache.openejb.server.rest.InternalApplication;
import org.eclipse.microprofile.openapi.OASConfig;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.OASModelReader;
import org.eclipse.microprofile.openapi.models.OpenAPI;

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
        processor = new AnnotationProcessor(config, loadNamingStrategy(config));
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

    <T> void findEndpointsAndApplication(@Observes final ProcessBean<T> event) {
        final String typeName = event.getAnnotated().getBaseType().getTypeName();
        if (classes == null && !skipScan && event.getAnnotated().isAnnotationPresent(Path.class) &&
                !typeName.startsWith("org.apache.geronimo.microprofile.openapi.") &&
                (packages == null || packages.stream().anyMatch(typeName::startsWith))) {
            endpoints.add(event.getBean());
        }
    }

    public OpenAPI getOrCreateOpenAPI(final Application application) {
        Class<? extends Application> applicationClass = getApplicationClass(application);

        if (classes != null) {
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            return openapis.computeIfAbsent(application,
                    app -> createOpenApi(applicationClass, classes.stream().map(c -> {
                        try {
                            return loader.loadClass(c);
                        } catch (final ClassNotFoundException e) {
                            throw new IllegalArgumentException(e);
                        }
                    })));
        }
        if (packages == null && (!application.getSingletons().isEmpty() || !application.getClasses().isEmpty())) {
            return openapis.computeIfAbsent(application,
                    app -> createOpenApi(applicationClass, Stream.concat(endpoints.stream().map(Bean::getBeanClass),
                            Stream.concat(app.getClasses().stream(), app.getSingletons().stream().map(Object::getClass)))));
        }
        return openapis.computeIfAbsent(application,
                app -> createOpenApi(applicationClass, endpoints.stream().map(Bean::getBeanClass)));
    }

    private Class<? extends Application> getApplicationClass(Application application) {
        if (application instanceof InternalApplication) {
            return InternalApplication.class.cast(application).getOriginal().getClass();
        } else {
            return application.getClass();
        }
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

        final String base = processor.getApplicationBinding(application);
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
                if (instance.isResolvable()) {
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