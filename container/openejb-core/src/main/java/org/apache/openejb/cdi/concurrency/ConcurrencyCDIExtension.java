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
package org.apache.openejb.cdi.concurrency;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Qualifier;
import org.apache.openejb.AppContext;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.WebBeansContext;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * CDI extension that registers concurrency resources as CDI beans
 * with qualifier support per Concurrency 3.1 spec (Section 5.4.1).
 *
 * <p>Resources defined via {@code @ManagedExecutorDefinition} (and similar)
 * or deployment descriptor {@code <managed-executor>} elements that specify
 * {@code qualifiers} become injectable via {@code @Inject @MyQualifier}.
 *
 * <p>Default resources (e.g. {@code java:comp/DefaultManagedExecutorService})
 * are always registered with {@code @Default} and {@code @Any} qualifiers.
 */
public class ConcurrencyCDIExtension implements Extension {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("cdi"), ConcurrencyCDIExtension.class);

    private static final String QUALIFIERS_PROPERTY = "Qualifiers";

    private static final String DEFAULT_MES_JNDI = "java:comp/DefaultManagedExecutorService";
    private static final String DEFAULT_MSES_JNDI = "java:comp/DefaultManagedScheduledExecutorService";
    private static final String DEFAULT_MTF_JNDI = "java:comp/DefaultManagedThreadFactory";
    private static final String DEFAULT_CS_JNDI = "java:comp/DefaultContextService";

    private static final String DEFAULT_MES_ID = "Default Executor Service";
    private static final String DEFAULT_MSES_ID = "Default Scheduled Executor Service";
    private static final String DEFAULT_MTF_ID = "Default Managed Thread Factory";
    private static final String DEFAULT_CS_ID = "Default Context Service";

    private enum ResourceKind {
        MANAGED_EXECUTOR(jakarta.enterprise.concurrent.ManagedExecutorService.class),
        MANAGED_SCHEDULED_EXECUTOR(jakarta.enterprise.concurrent.ManagedScheduledExecutorService.class),
        MANAGED_THREAD_FACTORY(jakarta.enterprise.concurrent.ManagedThreadFactory.class),
        CONTEXT_SERVICE(jakarta.enterprise.concurrent.ContextService.class);

        private final Class<?> type;

        ResourceKind(final Class<?> type) {
            this.type = type;
        }
    }

    void registerBeans(@Observes final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) {
        final OpenEjbConfiguration openEjbConfiguration = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
        if (openEjbConfiguration == null || openEjbConfiguration.facilities == null) {
            return;
        }

        final List<ResourceInfo> resources = openEjbConfiguration.facilities.resources;
        final Set<String> currentAppIds = findCurrentAppIds();

        for (final ResourceInfo resource : resources) {
            if (!isVisibleInCurrentApp(resource, currentAppIds)) {
                continue;
            }

            final ResourceKind resourceKind = findResourceKind(resource);
            if (resourceKind == null) {
                continue;
            }

            final List<String> qualifierNames = parseQualifiers(resource);
            if (qualifierNames.isEmpty()) {
                continue;
            }

            // Spec: qualifiers must not be used with java:global names
            if (isJavaGlobalName(resource.jndiName)) {
                afterBeanDiscovery.addDefinitionError(new IllegalArgumentException(resourceKind.type.getName()
                        + " with qualifiers must not use a java:global name: " + normalizeJndiName(resource.jndiName)));
                continue;
            }

            final Set<Annotation> qualifiers = validateAndCreateQualifiers(qualifierNames, resourceKind, afterBeanDiscovery);
            if (qualifiers == null) {
                continue;
            }

            logger.info("Registering CDI bean for " + resourceKind.type.getSimpleName()
                    + " resource '" + resource.id + "' with qualifiers " + qualifierNames);
            addQualifiedBean(afterBeanDiscovery, resourceKind.type, resource.id, qualifiers);
        }

        // Register default beans with @Default + @Any if no bean with @Default exists yet
        registerDefaultBeanIfMissing(afterBeanDiscovery, beanManager, resources,
                jakarta.enterprise.concurrent.ManagedExecutorService.class, DEFAULT_MES_JNDI, DEFAULT_MES_ID);
        registerDefaultBeanIfMissing(afterBeanDiscovery, beanManager, resources,
                jakarta.enterprise.concurrent.ManagedScheduledExecutorService.class, DEFAULT_MSES_JNDI, DEFAULT_MSES_ID);
        registerDefaultBeanIfMissing(afterBeanDiscovery, beanManager, resources,
                jakarta.enterprise.concurrent.ManagedThreadFactory.class, DEFAULT_MTF_JNDI, DEFAULT_MTF_ID);
        registerDefaultBeanIfMissing(afterBeanDiscovery, beanManager, resources,
                jakarta.enterprise.concurrent.ContextService.class, DEFAULT_CS_JNDI, DEFAULT_CS_ID);
    }

    /**
     * Validates qualifier class names per Concurrency 3.1 spec:
     * <ul>
     *   <li>Must be loadable annotation types</li>
     *   <li>Must be annotated with {@code @Qualifier}</li>
     *   <li>All members must have default values</li>
     *   <li>All members must be annotated with {@code @Nonbinding}</li>
     * </ul>
     */
    private Set<Annotation> validateAndCreateQualifiers(final List<String> qualifierNames,
                                                        final ResourceKind resourceKind,
                                                        final AfterBeanDiscovery afterBeanDiscovery) {
        final Set<Annotation> qualifiers = new LinkedHashSet<>();
        qualifiers.add(Any.Literal.INSTANCE);
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        for (final String qualifierName : qualifierNames) {
            final Class<?> qualifierClass;
            try {
                qualifierClass = loader.loadClass(qualifierName);
            } catch (final ClassNotFoundException e) {
                afterBeanDiscovery.addDefinitionError(new IllegalArgumentException("Qualifier class " + qualifierName
                        + " for " + resourceKind.type.getName() + " cannot be loaded", e));
                return null;
            }

            if (!qualifierClass.isAnnotation()) {
                afterBeanDiscovery.addDefinitionError(new IllegalArgumentException("Qualifier " + qualifierName
                        + " for " + resourceKind.type.getName() + " must be an annotation type"));
                return null;
            }

            @SuppressWarnings("unchecked")
            final Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) qualifierClass;
            if (!annotationClass.isAnnotationPresent(Qualifier.class)) {
                afterBeanDiscovery.addDefinitionError(new IllegalArgumentException("Qualifier " + qualifierName
                        + " for " + resourceKind.type.getName() + " must be annotated with @Qualifier"));
                return null;
            }

            for (final Method member : annotationClass.getDeclaredMethods()) {
                if (member.getDefaultValue() == null) {
                    afterBeanDiscovery.addDefinitionError(new IllegalArgumentException("Qualifier " + qualifierName
                            + " for " + resourceKind.type.getName() + " must not declare members without defaults"));
                    return null;
                }
                if (!member.isAnnotationPresent(Nonbinding.class)) {
                    afterBeanDiscovery.addDefinitionError(new IllegalArgumentException("Qualifier " + qualifierName
                            + " for " + resourceKind.type.getName() + " must use @Nonbinding on member " + member.getName()));
                    return null;
                }
            }

            qualifiers.add(createQualifierAnnotation(annotationClass));
        }

        return qualifiers;
    }

    private Annotation createQualifierAnnotation(final Class<? extends Annotation> qualifierType) {
        final Map<String, Object> values = new LinkedHashMap<>();
        for (final Method method : qualifierType.getDeclaredMethods()) {
            values.put(method.getName(), method.getDefaultValue());
        }

        final InvocationHandler handler = (final Object proxy, final Method method, final Object[] args) -> {
            final String name = method.getName();
            if ("annotationType".equals(name) && method.getParameterCount() == 0) {
                return qualifierType;
            }
            if ("equals".equals(name) && method.getParameterCount() == 1) {
                return annotationEquals(qualifierType, values, args[0]);
            }
            if ("hashCode".equals(name) && method.getParameterCount() == 0) {
                return annotationHashCode(values);
            }
            if ("toString".equals(name) && method.getParameterCount() == 0) {
                return annotationToString(qualifierType, values);
            }
            if (values.containsKey(name)) {
                return values.get(name);
            }
            throw new IllegalStateException("Unsupported annotation method: " + method);
        };

        return Annotation.class.cast(Proxy.newProxyInstance(
                qualifierType.getClassLoader(),
                new Class<?>[] { qualifierType },
                handler));
    }

    private boolean annotationEquals(final Class<? extends Annotation> qualifierType,
                                     final Map<String, Object> values,
                                     final Object other) {
        if (other == null || !qualifierType.isInstance(other)) {
            return false;
        }
        for (final Map.Entry<String, Object> entry : values.entrySet()) {
            try {
                final Method method = qualifierType.getMethod(entry.getKey());
                if (!memberValueEquals(entry.getValue(), method.invoke(other))) {
                    return false;
                }
            } catch (final Exception e) {
                return false;
            }
        }
        return true;
    }

    private int annotationHashCode(final Map<String, Object> values) {
        int hash = 0;
        for (final Map.Entry<String, Object> entry : values.entrySet()) {
            hash += (127 * entry.getKey().hashCode()) ^ memberValueHashCode(entry.getValue());
        }
        return hash;
    }

    private String annotationToString(final Class<? extends Annotation> qualifierType,
                                      final Map<String, Object> values) {
        final StringBuilder builder = new StringBuilder("@").append(qualifierType.getName()).append("(");
        boolean first = true;
        for (final Map.Entry<String, Object> entry : values.entrySet()) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        return builder.append(")").toString();
    }

    private int memberValueHashCode(final Object value) {
        final Class<?> valueType = value.getClass();
        if (!valueType.isArray()) {
            return value.hashCode();
        }
        if (valueType == byte[].class) {
            return Arrays.hashCode((byte[]) value);
        }
        if (valueType == short[].class) {
            return Arrays.hashCode((short[]) value);
        }
        if (valueType == int[].class) {
            return Arrays.hashCode((int[]) value);
        }
        if (valueType == long[].class) {
            return Arrays.hashCode((long[]) value);
        }
        if (valueType == char[].class) {
            return Arrays.hashCode((char[]) value);
        }
        if (valueType == float[].class) {
            return Arrays.hashCode((float[]) value);
        }
        if (valueType == double[].class) {
            return Arrays.hashCode((double[]) value);
        }
        if (valueType == boolean[].class) {
            return Arrays.hashCode((boolean[]) value);
        }
        return Arrays.hashCode((Object[]) value);
    }

    private boolean memberValueEquals(final Object left, final Object right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        final Class<?> valueType = left.getClass();
        if (!valueType.isArray()) {
            return left.equals(right);
        }
        if (valueType == byte[].class) {
            return Arrays.equals((byte[]) left, (byte[]) right);
        }
        if (valueType == short[].class) {
            return Arrays.equals((short[]) left, (short[]) right);
        }
        if (valueType == int[].class) {
            return Arrays.equals((int[]) left, (int[]) right);
        }
        if (valueType == long[].class) {
            return Arrays.equals((long[]) left, (long[]) right);
        }
        if (valueType == char[].class) {
            return Arrays.equals((char[]) left, (char[]) right);
        }
        if (valueType == float[].class) {
            return Arrays.equals((float[]) left, (float[]) right);
        }
        if (valueType == double[].class) {
            return Arrays.equals((double[]) left, (double[]) right);
        }
        if (valueType == boolean[].class) {
            return Arrays.equals((boolean[]) left, (boolean[]) right);
        }
        return Arrays.equals((Object[]) left, (Object[]) right);
    }

    private <T> void addQualifiedBean(final AfterBeanDiscovery afterBeanDiscovery,
                                      final Class<T> type,
                                      final String resourceId,
                                      final Set<Annotation> qualifiers) {
        afterBeanDiscovery.addBean()
                .id("tomee.concurrency." + type.getName() + "#" + resourceId + "#" + qualifiers.hashCode())
                .beanClass(type)
                .types(Object.class, type)
                .qualifiers(qualifiers.toArray(new Annotation[0]))
                .scope(ApplicationScoped.class)
                .createWith(creationalContext -> lookupByResourceId(type, resourceId));
    }

    private <T> void registerDefaultBeanIfMissing(final AfterBeanDiscovery afterBeanDiscovery,
                                                  final BeanManager beanManager,
                                                  final List<ResourceInfo> resources,
                                                  final Class<T> type,
                                                  final String jndiName,
                                                  final String defaultResourceId) {
        if (!beanManager.getBeans(type, Default.Literal.INSTANCE).isEmpty()) {
            return;
        }

        final String resourceId = findResourceId(resources, type, jndiName, defaultResourceId);
        logger.debug("Registering default CDI bean for " + type.getSimpleName() + " (resource '" + resourceId + "')");
        afterBeanDiscovery.addBean()
                .id("tomee.concurrency.default." + type.getName() + "#" + resourceId)
                .beanClass(type)
                .types(Object.class, type)
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith(creationalContext -> lookupDefaultResource(type, jndiName, resourceId));
    }

    private <T> String findResourceId(final List<ResourceInfo> resources,
                                      final Class<T> type,
                                      final String jndiName,
                                      final String defaultResourceId) {
        for (final ResourceInfo resource : resources) {
            if (!isResourceType(resource, type)) {
                continue;
            }
            final String normalized = normalizeJndiName(resource.jndiName);
            if (Objects.equals(normalized, normalizeJndiName(jndiName))) {
                return resource.id;
            }
        }
        return defaultResourceId;
    }

    private <T> T lookupByResourceId(final Class<T> type, final String resourceId) {
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        if (containerSystem == null) {
            throw new IllegalStateException("ContainerSystem is not available");
        }

        Object instance;
        try {
            instance = containerSystem.getJNDIContext().lookup("openejb/Resource/" + resourceId);
        } catch (final NamingException firstFailure) {
            try {
                instance = containerSystem.getJNDIContext().lookup("openejb:Resource/" + resourceId);
            } catch (final NamingException secondFailure) {
                throw new IllegalStateException("Unable to lookup resource " + resourceId, secondFailure);
            }
        }

        if (!type.isInstance(instance)) {
            throw new IllegalStateException("Resource " + resourceId + " is not of type " + type.getName()
                    + ", found " + (instance == null ? "null" : instance.getClass().getName()));
        }
        return type.cast(instance);
    }

    private <T> T lookupDefaultResource(final Class<T> type, final String jndiName, final String resourceId) {
        try {
            return lookupByJndiName(type, jndiName);
        } catch (final IllegalStateException firstFailure) {
            try {
                return lookupByResourceId(type, resourceId);
            } catch (final IllegalStateException secondFailure) {
                secondFailure.addSuppressed(firstFailure);
                throw secondFailure;
            }
        }
    }

    private <T> T lookupByJndiName(final Class<T> type, final String jndiName) {
        final Object instance;
        try {
            instance = InitialContext.doLookup(jndiName);
        } catch (final NamingException e) {
            throw new IllegalStateException("Unable to lookup resource " + jndiName, e);
        }

        if (!type.isInstance(instance)) {
            throw new IllegalStateException("Resource " + jndiName + " is not of type " + type.getName()
                    + ", found " + (instance == null ? "null" : instance.getClass().getName()));
        }
        return type.cast(instance);
    }

    private List<String> parseQualifiers(final ResourceInfo resource) {
        if (resource.properties == null) {
            return List.of();
        }
        final String value = resource.properties.getProperty(QUALIFIERS_PROPERTY);
        if (value == null || value.isBlank()) {
            return List.of();
        }

        final List<String> qualifiers = new ArrayList<>();
        for (final String item : value.split(",")) {
            final String qualifier = item.trim();
            if (!qualifier.isEmpty()) {
                qualifiers.add(qualifier);
            }
        }
        return qualifiers;
    }

    private ResourceKind findResourceKind(final ResourceInfo resource) {
        // Check MSES before MES since MSES extends MES
        if (isResourceType(resource, jakarta.enterprise.concurrent.ManagedScheduledExecutorService.class)) {
            return ResourceKind.MANAGED_SCHEDULED_EXECUTOR;
        }
        if (isResourceType(resource, jakarta.enterprise.concurrent.ManagedExecutorService.class)) {
            return ResourceKind.MANAGED_EXECUTOR;
        }
        if (isResourceType(resource, jakarta.enterprise.concurrent.ManagedThreadFactory.class)) {
            return ResourceKind.MANAGED_THREAD_FACTORY;
        }
        if (isResourceType(resource, jakarta.enterprise.concurrent.ContextService.class)) {
            return ResourceKind.CONTEXT_SERVICE;
        }
        return null;
    }

    private boolean isResourceType(final ResourceInfo resource, final Class<?> type) {
        return resource.types != null
                && (resource.types.contains(type.getName()) || resource.types.contains(type.getSimpleName()));
    }

    private boolean isJavaGlobalName(final String rawName) {
        final String normalized = normalizeJndiName(rawName);
        return normalized != null && normalized.startsWith("global/");
    }

    private String normalizeJndiName(final String rawName) {
        if (rawName == null) {
            return null;
        }
        return rawName.startsWith("java:") ? rawName.substring("java:".length()) : rawName;
    }

    private boolean isVisibleInCurrentApp(final ResourceInfo resource, final Set<String> currentAppIds) {
        if (resource.originAppName == null || resource.originAppName.isEmpty()) {
            return true;
        }
        return currentAppIds.contains(resource.originAppName);
    }

    private Set<String> findCurrentAppIds() {
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        if (containerSystem == null) {
            return Set.of();
        }

        final Set<String> appIds = new LinkedHashSet<>();
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        final WebBeansContext currentWbc;
        try {
            currentWbc = WebBeansContext.currentInstance();
        } catch (final RuntimeException re) {
            return Set.of();
        }

        for (final AppContext appContext : containerSystem.getAppContexts()) {
            if (appContext.getWebBeansContext() == currentWbc || appContext.getClassLoader() == tccl) {
                appIds.add(appContext.getId());
            }
        }
        return appIds;
    }
}
