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
package org.apache.openejb.data.proxy;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.JtaEntityManager;
import org.apache.openejb.persistence.JtaEntityManagerRegistry;
import org.apache.openejb.spi.ContainerSystem;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EntityManagerLookup {

    private static final Logger LOGGER = Logger.getLogger(EntityManagerLookup.class.getName());

    private EntityManagerLookup() {
    }

    public static EntityManager lookup(final String dataStore) {
        // First, try to resolve via CDI (e.g., @Produces EntityManagerFactory or @PersistenceUnit)
        final EntityManagerFactory cdiEmf = lookupViaCDI(dataStore);
        if (cdiEmf != null) {
            return wrapWithJta(cdiEmf, dataStore != null && !dataStore.isEmpty() ? dataStore : "default");
        }

        // Fall back to JNDI lookup
        return lookupViaJNDI(dataStore);
    }

    private static EntityManagerFactory lookupViaCDI(final String dataStore) {
        try {
            final BeanManager bm = CDI.current().getBeanManager();
            final Set<Bean<?>> beans = bm.getBeans(EntityManagerFactory.class);
            if (beans.isEmpty()) {
                return null;
            }

            // If a specific dataStore is requested, try to find a matching bean by name
            if (dataStore != null && !dataStore.isEmpty()) {
                for (final Bean<?> bean : beans) {
                    if (dataStore.equals(bean.getName())) {
                        return (EntityManagerFactory) bm.getReference(bean, EntityManagerFactory.class, bm.createCreationalContext(bean));
                    }
                }
            }

            // If there's exactly one EMF bean, use it
            if (beans.size() == 1) {
                final Bean<?> bean = bm.resolve(beans);
                return (EntityManagerFactory) bm.getReference(bean, EntityManagerFactory.class, bm.createCreationalContext(bean));
            }

            return null;
        } catch (final Exception e) {
            LOGGER.log(Level.FINE, "CDI lookup for EntityManagerFactory not available, falling back to JNDI", e);
            return null;
        }
    }

    private static EntityManager lookupViaJNDI(final String dataStore) {
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        if (containerSystem == null) {
            throw new IllegalStateException("ContainerSystem not available");
        }

        final Context jndiContext = containerSystem.getJNDIContext();
        final String jndiName = resolveJndiName(dataStore, jndiContext);

        try {
            final Object obj = jndiContext.lookup(jndiName);
            if (obj instanceof EntityManager em) {
                return em;
            }
            if (obj instanceof EntityManagerFactory emf) {
                return wrapWithJta(emf, extractUnitName(jndiName));
            }
            throw new IllegalStateException("JNDI lookup for '" + jndiName + "' returned unexpected type: " + obj.getClass());
        } catch (final NamingException e) {
            throw new IllegalStateException("Failed to lookup EntityManagerFactory for dataStore='" + dataStore + "'", e);
        }
    }

    private static EntityManager wrapWithJta(final EntityManagerFactory emf, final String unitName) {
        final JtaEntityManagerRegistry registry =
                SystemInstance.get().getComponent(JtaEntityManagerRegistry.class);
        if (registry != null) {
            return new JtaEntityManager(registry, emf, new HashMap<>(), unitName, "SYNCHRONIZED");
        }
        return emf.createEntityManager();
    }

    private static String extractUnitName(final String jndiName) {
        final int lastSlash = jndiName.lastIndexOf('/');
        return lastSlash >= 0 ? jndiName.substring(lastSlash + 1) : jndiName;
    }

    private static String resolveJndiName(final String dataStore, final Context jndiContext) {
        if (dataStore != null && !dataStore.isEmpty()) {
            // Try direct JNDI paths first
            final String[] candidates = {
                "openejb/PersistenceUnit/" + dataStore,
                "java:openejb/PersistenceUnit/" + dataStore
            };
            for (final String candidate : candidates) {
                try {
                    jndiContext.lookup(candidate);
                    return candidate;
                } catch (final NamingException ignored) {
                    // try next
                }
            }

            // Search by PU name within enumerated entries (handles app-scoped names)
            final String matchedName = findPersistenceUnitByName(dataStore, jndiContext);
            if (matchedName != null) {
                return matchedName;
            }

            return "openejb/PersistenceUnit/" + dataStore;
        }

        // Default: try to find any available persistence unit
        try {
            final Context puContext = (Context) jndiContext.lookup("openejb/PersistenceUnit");
            final javax.naming.NamingEnumeration<javax.naming.NameClassPair> list = puContext.list("");
            String found = null;
            while (list.hasMore()) {
                if (found != null) {
                    throw new IllegalStateException(
                        "Multiple persistence units found. Use @Repository(dataStore=\"...\") to specify which one to use.");
                }
                found = list.next().getName();
            }
            if (found != null) {
                return "openejb/PersistenceUnit/" + found;
            }
        } catch (final NamingException ignored) {
            // fall through
        }

        throw new IllegalStateException("No persistence unit found. Define a persistence unit or use @Repository(dataStore=\"...\")");
    }

    /**
     * Searches the JNDI persistence unit context for a PU whose name ends with the given dataStore name.
     * This handles cases where PUs are registered under app-scoped names (e.g., "AppName/unitName").
     */
    private static String findPersistenceUnitByName(final String dataStore, final Context jndiContext) {
        try {
            final Context puContext = (Context) jndiContext.lookup("openejb/PersistenceUnit");
            final javax.naming.NamingEnumeration<javax.naming.NameClassPair> list = puContext.list("");
            while (list.hasMore()) {
                final String name = list.next().getName();
                // Match exact name, app-scoped name ending with /dataStore,
                // or name starting with dataStore (OpenEJB may append suffixes like " 0")
                if (name.equals(dataStore) || name.endsWith("/" + dataStore)
                    || name.startsWith(dataStore + " ") || name.startsWith(dataStore + "/")) {
                    return "openejb/PersistenceUnit/" + name;
                }
            }
        } catch (final NamingException ignored) {
            // not available
        }
        return null;
    }
}
