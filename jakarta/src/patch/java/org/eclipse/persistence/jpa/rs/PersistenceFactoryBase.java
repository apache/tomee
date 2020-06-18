/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//         dclarke/tware - initial implementation
//      gonural - version based persistence context
//      Dmitry Kornilov - JPARS 2.0 related changes
package org.eclipse.persistence.jpa.rs;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryImpl;
import org.eclipse.persistence.internal.jpa.EntityManagerSetupImpl;
import org.eclipse.persistence.internal.jpa.deployment.PersistenceUnitProcessor;
import org.eclipse.persistence.internal.jpa.deployment.SEPersistenceUnitInfo;
import org.eclipse.persistence.jpa.Archive;
import org.eclipse.persistence.jpa.rs.exceptions.JPARSException;
import org.eclipse.persistence.jpa.rs.features.ServiceVersion;
import org.eclipse.persistence.jpa.rs.util.JPARSLogger;

/**
 * Manages the PersistenceContexts that are used by a JPA-RS deployment.  Provides a single point to bootstrap
 * and look up PersistenceContexts.
 *
 * @author tware
 */
public class PersistenceFactoryBase implements PersistenceContextFactory {
    protected final Map<String, Set<PersistenceContext>> dynamicPersistenceContexts = new HashMap<>();

    /**
     * Bootstrap a PersistenceContext based on an pre-existing EntityManagerFactory.
     *
     * @param name      persistence context name
     * @param emf       entity manager factory
     * @param baseURI   base URI
     * @param version   JPARS version. See {@link ServiceVersion} for more details.
     * @param replace   Indicates that existing persistence context with given name and version must be replaced
     *                  with the newly created one. If false passed the newly created context is not added to cache at all.
     * @return newly created persistence context
     */
    public PersistenceContext bootstrapPersistenceContext(String name, EntityManagerFactory emf, URI baseURI, String version, boolean replace) {
        final PersistenceContext persistenceContext = new PersistenceContext(name, (EntityManagerFactoryImpl) emf, baseURI, ServiceVersion.fromCode(version));

        if (replace) {
            addReplacePersistenceContext(persistenceContext);
        }

        return persistenceContext;
    }

    /**
     * Stop the factory. Remove all the PersistenceContexts.
     */
    @Override
    public void close() {
        synchronized (this) {
            for (Set<PersistenceContext> contextSet : dynamicPersistenceContexts.values()) {
                if (contextSet != null) {
                    for (PersistenceContext context : contextSet) {
                        context.stop();
                    }
                }
            }
            dynamicPersistenceContexts.clear();
        }
    }

    /**
     * Close the PersistenceContext of a given name and clean it out of our list of PersistenceContexts.
     *
     * @param name name of the persistence context to close.
     */
    @Override
    public void closePersistenceContext(String name) {
        synchronized (this) {
            Set<PersistenceContext> contextSet = dynamicPersistenceContexts.get(name);
            if (contextSet != null) {
                for (PersistenceContext context : contextSet) {
                    context.stop();
                }
            }
            dynamicPersistenceContexts.remove(name);
        }
    }

    /**
     * Close the PersistenceContext and clean it out of our list of PersistenceContexts.
     *
     * @param name name of the persistence context to close.
     * @param version persistence context version
     */
    public void closePersistenceContext(String name, String version) {
        synchronized (this) {
            final Set<PersistenceContext> contextSet = dynamicPersistenceContexts.get(name);
            if (contextSet != null) {
                for (Iterator<PersistenceContext> iter = contextSet.iterator(); iter.hasNext();) {
                    PersistenceContext context = iter.next();
                    if (context.getVersion().equals(version)) {
                        context.stop();
                        iter.remove();
                        break;
                    }
                }

                if (contextSet.size() == 0) {
                    dynamicPersistenceContexts.remove(name);
                } else {
                    dynamicPersistenceContexts.put(name, contextSet);
                }
            }
        }
    }

    /**
     * Provide an initial set of properties for bootstrapping PersistenceContexts.
     * @param dcl
     * @param originalProperties
     * @return
     */
    protected static Map<String, Object> createProperties(DynamicClassLoader dcl, Map<String, ?> originalProperties) {
        Map<String, Object> properties = new HashMap<>();

        properties.put(PersistenceUnitProperties.CLASSLOADER, dcl);
        properties.put(PersistenceUnitProperties.WEAVING, "static");

        // For now we'll copy the connection info from admin PU
        for (Map.Entry<String, ?> entry : originalProperties.entrySet()) {
            if (entry.getKey().startsWith("javax") || entry.getKey().startsWith("jakarta") || entry.getKey().startsWith("eclipselink.log") || entry.getKey().startsWith("eclipselink.target-server")) {
                properties.put(entry.getKey(), entry.getValue());
            }
        }
        return properties;
    }

    /**
     * Gets existing persistence context or create new based on given parameters if it doesn't exist.
     */
    @Override
    public PersistenceContext get(String persistenceUnitName, URI defaultURI, String version, Map<String, Object> initializationProperties) {
        PersistenceContext persistenceContext = getDynamicPersistenceContext(persistenceUnitName, version);

        if (persistenceContext == null) {
            try {
                DynamicClassLoader dcl = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());
                Map<String, Object> properties = new HashMap<>();
                properties.put(PersistenceUnitProperties.CLASSLOADER, dcl);
                if (initializationProperties != null) {
                    properties.putAll(initializationProperties);
                }

                EntityManagerFactoryImpl factory = (EntityManagerFactoryImpl) Persistence.createEntityManagerFactory(persistenceUnitName, properties);
                ClassLoader sessionLoader = factory.getServerSession().getLoader();
                if (!DynamicClassLoader.class.isAssignableFrom(sessionLoader.getClass())) {
                    properties = new HashMap<>();
                    dcl = new DynamicClassLoader(sessionLoader);
                    properties.put(PersistenceUnitProperties.CLASSLOADER, dcl);
                    if (initializationProperties != null) {
                        properties.putAll(initializationProperties);
                    }
                    factory.refreshMetadata(properties);
                }

                persistenceContext = bootstrapPersistenceContext(persistenceUnitName, factory, defaultURI, version, true);
            } catch (Exception e) {
                JPARSLogger.exception("exception_creating_persistence_context", new Object[] { persistenceUnitName, e.toString() }, e);
            }
        }

        if ((persistenceContext != null) && (!persistenceContext.isWeavingEnabled())) {
            JPARSLogger.error(persistenceContext.getServerSession().getSessionLog(), "weaving_required_for_relationships", new Object[] {});
            throw JPARSException.invalidConfiguration();
        }

        return persistenceContext;
    }

    /**
     * Returns names of all currently cached persistence contexts.
     */
    @Override
    public Set<String> getPersistenceContextNames() {
        Set<String> contextNames = new HashSet<>();
        try {
            Set<Archive> archives = PersistenceUnitProcessor.findPersistenceArchives();
            for (Archive archive : archives) {
                List<SEPersistenceUnitInfo> infos = PersistenceUnitProcessor.processPersistenceArchive(archive, Thread.currentThread().getContextClassLoader());
                for (SEPersistenceUnitInfo info : infos) {
                    if (!info.getPersistenceUnitName().equals("jpa-rs")) {
                        if (EntityManagerSetupImpl.mustBeCompositeMember(info)) {
                            continue;
                        }
                        contextNames.add(info.getPersistenceUnitName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        contextNames.addAll(dynamicPersistenceContexts.keySet());
        return contextNames;
    }

    /**
     * Gets cached persistence context by its name and JPARS version.
     *
     * @param name      persistent unit name.
     * @param version   JPARS version. See {@link ServiceVersion} for more details.
     * @return persistence context or null if doesn't exist.
     */
    public PersistenceContext getDynamicPersistenceContext(String name, String version) {
        synchronized (this) {
            Set<PersistenceContext> persistenceContextSet = dynamicPersistenceContexts.get(name);
            if (persistenceContextSet != null) {
                for (PersistenceContext persistenceContext : persistenceContextSet) {
                    if ((name != null) && (version != null)) {
                        if ((name.equals(persistenceContext.getName())) && (version.equals(persistenceContext.getVersion()))) {
                            return persistenceContext;
                        }
                    } else if (((version == null) && (persistenceContext.getVersion() == null)) &&
                            ((name != null) && (name.equals(persistenceContext.getName())))) {
                        return persistenceContext;
                    }
                }
            }
        }
        return null;
    }

    protected void addReplacePersistenceContext(PersistenceContext persistenceContext) {
        synchronized (this) {
            final PersistenceContext existingContext = getDynamicPersistenceContext(persistenceContext.getName(), persistenceContext.getVersion());

            Set<PersistenceContext> persistenceContextSet = dynamicPersistenceContexts.get(persistenceContext.getName());
            if (persistenceContextSet == null) {
                persistenceContextSet = new HashSet<>();
            }

            if (existingContext != null) {
                persistenceContextSet.remove(existingContext);
            }

            persistenceContextSet.add(persistenceContext);
            dynamicPersistenceContexts.put(persistenceContext.getName(), persistenceContextSet);
        }
    }
}