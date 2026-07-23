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
package org.apache.openejb.cdi.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Qualifier;
import jakarta.persistence.Cache;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.SchemaManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.PersistenceUnitInfo;
import org.apache.openejb.cdi.OpenEJBLifecycle;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.naming.NamingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * CDI extension registering the beans required by the Jakarta Persistence 3.2 /
 * Jakarta EE 11 CDI integration (Platform specification, "Jakarta Persistence &amp;
 * Jakarta Context and Dependency Injection (CDI) Integration").
 *
 * <p>For each persistence unit the container must make available:
 * <ul>
 *   <li>an {@link EntityManagerFactory} bean, {@code @ApplicationScoped}, whose bean name
 *       is the persistence unit name;</li>
 *   <li>an {@link EntityManager} bean, in the scope given by the {@code <scope>} element
 *       (defaulting to {@code jakarta.transaction.TransactionScoped});</li>
 *   <li>{@link CriteriaBuilder}, {@link PersistenceUnitUtil}, {@link Cache},
 *       {@link SchemaManager} and {@link Metamodel} beans, {@code @Dependent}, each simply
 *       obtained from the matching getter of the {@code EntityManagerFactory}.</li>
 * </ul>
 *
 * <p>All of them carry the qualifiers given by the {@code <qualifier>} elements of
 * {@code persistence.xml}, or {@code @Default} when none is declared.
 */
public class JpaCDIExtension implements Extension {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("cdi"), JpaCDIExtension.class);

    private static final String PERSISTENCE_UNIT_NAMING_CONTEXT = "openejb/PersistenceUnit/";

    /**
     * Default scope of the {@code EntityManager} bean. Resolved reflectively so that the
     * extension keeps working on distributions without the Jakarta Transactions API.
     */
    private static final String TRANSACTION_SCOPED = "jakarta.transaction.TransactionScoped";

    void registerBeans(@Observes final AfterBeanDiscovery afterBeanDiscovery) {
        final AppInfo appInfo = OpenEJBLifecycle.CURRENT_APP_INFO.get();
        if (appInfo == null) {
            return;
        }

        for (final PersistenceUnitInfo unitInfo : appInfo.persistenceUnits) {
            final Set<Annotation> qualifiers = validateAndCreateQualifiers(unitInfo, afterBeanDiscovery);
            if (qualifiers == null) {
                continue;
            }

            final Class<? extends Annotation> entityManagerScope = resolveEntityManagerScope(unitInfo, afterBeanDiscovery);
            if (entityManagerScope == null) {
                continue;
            }

            logger.debug("Registering CDI beans for persistence unit '" + unitInfo.name + "'");

            afterBeanDiscovery.addBean()
                    .id("tomee.jpa." + EntityManagerFactory.class.getName() + "#" + unitInfo.id)
                    .beanClass(EntityManagerFactory.class)
                    .types(Object.class, EntityManagerFactory.class)
                    .qualifiers(qualifiers.toArray(new Annotation[0]))
                    .scope(ApplicationScoped.class)
                    .name(unitInfo.name)
                    .createWith(cc -> lookupEntityManagerFactory(unitInfo.id));

            // the EntityManager is created per contextual instance and closed when the context ends
            afterBeanDiscovery.addBean()
                    .id("tomee.jpa." + EntityManager.class.getName() + "#" + unitInfo.id)
                    .beanClass(EntityManager.class)
                    .types(Object.class, EntityManager.class)
                    .qualifiers(qualifiers.toArray(new Annotation[0]))
                    .scope(entityManagerScope)
                    .produceWith(instance -> lookupEntityManagerFactory(unitInfo.id).createEntityManager())
                    .disposeWith((em, cc) -> {
                        if (em.isOpen()) {
                            em.close();
                        }
                    });

            addUtilityBean(afterBeanDiscovery, unitInfo, qualifiers, CriteriaBuilder.class, EntityManagerFactory::getCriteriaBuilder);
            addUtilityBean(afterBeanDiscovery, unitInfo, qualifiers, PersistenceUnitUtil.class, EntityManagerFactory::getPersistenceUnitUtil);
            addUtilityBean(afterBeanDiscovery, unitInfo, qualifiers, Cache.class, EntityManagerFactory::getCache);
            addUtilityBean(afterBeanDiscovery, unitInfo, qualifiers, SchemaManager.class, EntityManagerFactory::getSchemaManager);
            addUtilityBean(afterBeanDiscovery, unitInfo, qualifiers, Metamodel.class, EntityManagerFactory::getMetamodel);
        }
    }

    /**
     * The five utility beans are {@code @Dependent} and simply delegate to the matching
     * getter of the {@code EntityManagerFactory}.
     */
    private <T> void addUtilityBean(final AfterBeanDiscovery afterBeanDiscovery,
                                    final PersistenceUnitInfo unitInfo,
                                    final Set<Annotation> qualifiers,
                                    final Class<T> type,
                                    final Function<EntityManagerFactory, T> accessor) {
        afterBeanDiscovery.addBean()
                .id("tomee.jpa." + type.getName() + "#" + unitInfo.id)
                .beanClass(type)
                .types(Object.class, type)
                .qualifiers(qualifiers.toArray(new Annotation[0]))
                .scope(Dependent.class)
                .createWith(cc -> accessor.apply(lookupEntityManagerFactory(unitInfo.id)));
    }

    /**
     * Builds the qualifier set of a persistence unit: the {@code <qualifier>} elements, or
     * {@code @Default} when none is declared. {@code @Any} is always added, as for any bean.
     *
     * @return {@code null} if a qualifier is invalid, in which case a definition error has
     * been reported
     */
    private Set<Annotation> validateAndCreateQualifiers(final PersistenceUnitInfo unitInfo,
                                                        final AfterBeanDiscovery afterBeanDiscovery) {
        final Set<Annotation> qualifiers = new LinkedHashSet<>();
        qualifiers.add(Any.Literal.INSTANCE);

        if (unitInfo.qualifiers.isEmpty()) {
            qualifiers.add(Default.Literal.INSTANCE);
            return qualifiers;
        }

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        for (final String qualifierName : unitInfo.qualifiers) {
            final Class<?> qualifierClass;
            try {
                qualifierClass = loader.loadClass(qualifierName);
            } catch (final ClassNotFoundException e) {
                afterBeanDiscovery.addDefinitionError(new IllegalArgumentException("Qualifier class " + qualifierName
                        + " of persistence unit " + unitInfo.name + " cannot be loaded", e));
                return null;
            }

            if (!qualifierClass.isAnnotation()) {
                afterBeanDiscovery.addDefinitionError(new IllegalArgumentException("Qualifier " + qualifierName
                        + " of persistence unit " + unitInfo.name + " must be an annotation type"));
                return null;
            }

            @SuppressWarnings("unchecked")
            final Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) qualifierClass;
            if (!annotationClass.isAnnotationPresent(Qualifier.class)) {
                afterBeanDiscovery.addDefinitionError(new IllegalArgumentException("Qualifier " + qualifierName
                        + " of persistence unit " + unitInfo.name + " must be annotated with @Qualifier"));
                return null;
            }

            qualifiers.add(createAnnotation(annotationClass));
        }

        return qualifiers;
    }

    /**
     * Resolves the {@code <scope>} element of the persistence unit, defaulting to
     * {@code jakarta.transaction.TransactionScoped}.
     *
     * @return {@code null} if the scope is invalid, in which case a definition error has
     * been reported
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Annotation> resolveEntityManagerScope(final PersistenceUnitInfo unitInfo,
                                                                  final AfterBeanDiscovery afterBeanDiscovery) {
        final String scope = unitInfo.scope == null || unitInfo.scope.isBlank() ? TRANSACTION_SCOPED : unitInfo.scope.trim();
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        final Class<?> scopeClass;
        try {
            scopeClass = loader.loadClass(scope);
        } catch (final ClassNotFoundException e) {
            if (unitInfo.scope == null || unitInfo.scope.isBlank()) {
                // no Jakarta Transactions on the classpath, fall back to @Dependent
                return Dependent.class;
            }
            afterBeanDiscovery.addDefinitionError(new IllegalArgumentException("Scope class " + scope
                    + " of persistence unit " + unitInfo.name + " cannot be loaded", e));
            return null;
        }

        if (!scopeClass.isAnnotation()) {
            afterBeanDiscovery.addDefinitionError(new IllegalArgumentException("Scope " + scope
                    + " of persistence unit " + unitInfo.name + " must be an annotation type"));
            return null;
        }

        return (Class<? extends Annotation>) scopeClass;
    }

    private EntityManagerFactory lookupEntityManagerFactory(final String unitId) {
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        if (containerSystem == null) {
            throw new IllegalStateException("ContainerSystem is not available");
        }

        final Object instance;
        try {
            instance = containerSystem.getJNDIContext().lookup(PERSISTENCE_UNIT_NAMING_CONTEXT + unitId);
        } catch (final NamingException e) {
            throw new IllegalStateException("Unable to lookup persistence unit " + unitId, e);
        }

        if (!(instance instanceof EntityManagerFactory)) {
            throw new IllegalStateException("Persistence unit " + unitId + " is not an EntityManagerFactory, found "
                    + (instance == null ? "null" : instance.getClass().getName()));
        }
        return EntityManagerFactory.class.cast(instance);
    }

    /**
     * Creates an instance of a member-less annotation type. Members are answered with their
     * default value, which the CDI qualifier rules guarantee to exist.
     */
    private Annotation createAnnotation(final Class<? extends Annotation> annotationType) {
        final Map<String, Object> values = new LinkedHashMap<>();
        for (final Method method : annotationType.getDeclaredMethods()) {
            values.put(method.getName(), method.getDefaultValue());
        }

        final InvocationHandler handler = (final Object proxy, final Method method, final Object[] args) -> {
            final String name = method.getName();
            if ("annotationType".equals(name) && method.getParameterCount() == 0) {
                return annotationType;
            }
            if ("equals".equals(name) && method.getParameterCount() == 1) {
                return annotationType.isInstance(args[0]);
            }
            if ("hashCode".equals(name) && method.getParameterCount() == 0) {
                return annotationType.hashCode();
            }
            if ("toString".equals(name) && method.getParameterCount() == 0) {
                return "@" + annotationType.getName() + "()";
            }
            if (values.containsKey(name)) {
                return values.get(name);
            }
            throw new IllegalStateException("Unsupported annotation method: " + method);
        };

        return Annotation.class.cast(Proxy.newProxyInstance(
                annotationType.getClassLoader(),
                new Class<?>[]{annotationType},
                handler));
    }
}
