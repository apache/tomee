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

package org.apache.openejb.assembler.classic;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.PersistenceUnitInfoImpl;

import javax.persistence.EntityManagerFactory;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceProvider;
import javax.validation.ValidatorFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class EntityManagerFactoryCallable implements Callable<EntityManagerFactory> {
    public static final String OPENEJB_JPA_INIT_ENTITYMANAGER = "openejb.jpa.init-entitymanager";
    public static final String OPENJPA_ENTITY_MANAGER_FACTORY_POOL = "openjpa.EntityManagerFactoryPool";

    private final String persistenceProviderClassName;
    private final PersistenceUnitInfoImpl unitInfo;
    private final Map<ComparableValidationConfig, ValidatorFactory> potentialValidators;
    private ClassLoader appClassLoader;

    public EntityManagerFactoryCallable(final String persistenceProviderClassName, final PersistenceUnitInfoImpl unitInfo,
                                        final ClassLoader cl, final Map<ComparableValidationConfig, ValidatorFactory> validators) {
        this.persistenceProviderClassName = persistenceProviderClassName;
        this.unitInfo = unitInfo;
        this.appClassLoader = cl;
        this.potentialValidators = validators;
    }

    @Override
    public EntityManagerFactory call() throws Exception {
        final ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(appClassLoader);
        try {
            final Class<?> clazz = appClassLoader.loadClass(persistenceProviderClassName);
            final PersistenceProvider persistenceProvider = (PersistenceProvider) clazz.newInstance();

            // Create entity manager factories with the validator factory
            final Map<String, Object> properties = new HashMap<String, Object>();
            if (!ValidationMode.NONE.equals(unitInfo.getValidationMode())) {
                properties.put("javax.persistence.validation.factory", new ValidatorFactoryWrapper(potentialValidators));
            }

            customizeProperties(properties);

            final EntityManagerFactory emf = persistenceProvider.createContainerEntityManagerFactory(unitInfo, properties);

            if (unitInfo.getProperties() != null
                && "true".equalsIgnoreCase(unitInfo.getProperties().getProperty(OPENEJB_JPA_INIT_ENTITYMANAGER))
                || SystemInstance.get().getOptions().get(OPENEJB_JPA_INIT_ENTITYMANAGER, false)) {
                emf.createEntityManager().close();
            }

            if (unitInfo.getNonJtaDataSource() != null) {
                final ImportSql importer = new ImportSql(appClassLoader, unitInfo.getPersistenceUnitName(), unitInfo.getNonJtaDataSource());
                if (importer.hasSomethingToImport()) {
                    emf.createEntityManager().close(); // to let OpenJPA create the database if configured this way
                    importer.doImport();
                }
            }

            return emf;
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    // properties that have to be passed to properties parameters and not unit properties
    private void customizeProperties(final Map<String, Object> properties) {
        final String pool = SystemInstance.get().getProperty(OPENJPA_ENTITY_MANAGER_FACTORY_POOL);
        if (pool != null) {
            properties.put(OPENJPA_ENTITY_MANAGER_FACTORY_POOL, pool);
        }
    }

    public PersistenceUnitInfoImpl getUnitInfo() {
        return unitInfo;
    }

    public void overrideClassLoader(final ClassLoader loader) {
        appClassLoader = loader;
    }
}
