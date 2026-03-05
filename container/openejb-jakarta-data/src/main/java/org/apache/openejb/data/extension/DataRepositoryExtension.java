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
package org.apache.openejb.data.extension;

import jakarta.data.repository.DataRepository;
import jakarta.data.repository.Repository;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;

import org.apache.openejb.loader.SystemInstance;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DataRepositoryExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(DataRepositoryExtension.class.getName());

    private final List<Class<?>> repositoryInterfaces = new ArrayList<>();
    private boolean active = true;

    public void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery event) {
        active = Boolean.parseBoolean(
                SystemInstance.get().getOptions().get("tomee.jakarta-data.active", "true"));
        if (!active) {
            LOGGER.info("Jakarta Data extension disabled via tomee.jakarta-data.active=false");
            return;
        }

        // Hibernate provides its own Jakarta Data implementation, so we back off
        if (hasHibernate()) {
            active = false;
            LOGGER.info("Hibernate detected as persistence provider, disabling TomEE Jakarta Data extension");
        }
    }

    private static boolean hasHibernate() {
        try {
            Thread.currentThread().getContextClassLoader()
                    .loadClass("org.hibernate.Session");
            return true;
        } catch (final ClassNotFoundException | NoClassDefFoundError e) {
            return false;
        }
    }

    public <T> void detectRepositories(@Observes @WithAnnotations(Repository.class) final ProcessAnnotatedType<T> event) {
        if (!active) {
            return;
        }
        final Class<T> javaClass = event.getAnnotatedType().getJavaClass();
        if (!javaClass.isInterface()) {
            return;
        }

        final Repository repoAnnotation = javaClass.getAnnotation(Repository.class);
        if (repoAnnotation == null) {
            return;
        }

        // Skip repositories with a non-empty provider that isn't ours
        final String provider = repoAnnotation.provider();
        if (provider != null && !provider.isEmpty()) {
            LOGGER.fine("Skipping Jakarta Data repository with provider '" + provider + "': " + javaClass.getName());
            return;
        }

        // Skip repositories whose entity type is not a JPA @Entity
        final Class<?> entityClass = resolveEntityClass(javaClass);
        if (entityClass != null && !entityClass.isAnnotationPresent(jakarta.persistence.Entity.class)) {
            LOGGER.fine("Skipping Jakarta Data repository for non-JPA entity " + entityClass.getName() + ": " + javaClass.getName());
            return;
        }

        LOGGER.info("Discovered Jakarta Data repository: " + javaClass.getName());
        repositoryInterfaces.add(javaClass);
        event.veto();
    }

    private static Class<?> resolveEntityClass(final Class<?> repoInterface) {
        for (final Type iface : repoInterface.getGenericInterfaces()) {
            if (iface instanceof ParameterizedType) {
                final ParameterizedType pt = (ParameterizedType) iface;
                final Type rawType = pt.getRawType();
                if (rawType instanceof Class && DataRepository.class.isAssignableFrom((Class<?>) rawType)) {
                    final Type entityType = pt.getActualTypeArguments()[0];
                    if (entityType instanceof Class) {
                        return (Class<?>) entityType;
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void registerRepositoryBeans(@Observes final AfterBeanDiscovery event) {
        if (!active) {
            return;
        }
        for (final Class<?> repoInterface : repositoryInterfaces) {
            LOGGER.info("Registering Jakarta Data repository bean: " + repoInterface.getName());
            event.addBean(new RepositoryBean(repoInterface));
        }
    }
}
