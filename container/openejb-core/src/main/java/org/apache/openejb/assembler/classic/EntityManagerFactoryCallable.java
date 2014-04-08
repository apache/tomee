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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class EntityManagerFactoryCallable implements Callable<EntityManagerFactory> {
    public static final String OPENEJB_JPA_INIT_ENTITYMANAGER = "openejb.jpa.init-entitymanager";

    private final String persistenceProviderClassName;
    private final PersistenceUnitInfoImpl unitInfo;
    private ClassLoader appClassLoader;

    public EntityManagerFactoryCallable(String persistenceProviderClassName, PersistenceUnitInfoImpl unitInfo, ClassLoader cl) {
        this.persistenceProviderClassName = persistenceProviderClassName;
        this.unitInfo = unitInfo;
        this.appClassLoader = cl;
    }

    @Override
    public EntityManagerFactory call() throws Exception {
        final ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(appClassLoader);
        try {
            final Class<?> clazz = appClassLoader.loadClass(persistenceProviderClassName);
            PersistenceProvider persistenceProvider = (PersistenceProvider) clazz.newInstance();

            // Create entity manager factories with the validator factory
            final Map<String, Object> properties = new HashMap<String, Object>();
            if (!ValidationMode.NONE.equals(unitInfo.getValidationMode())) {
                properties.put("javax.persistence.validator.ValidatorFactory", new ValidatorFactoryWrapper());
            }
            EntityManagerFactory emf = persistenceProvider.createContainerEntityManagerFactory(unitInfo, properties);

            if ((unitInfo.getProperties() != null
                    && "true".equalsIgnoreCase(unitInfo.getProperties().getProperty(OPENEJB_JPA_INIT_ENTITYMANAGER)))
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

    public PersistenceUnitInfoImpl getUnitInfo() {
        return unitInfo;
    }

    public void overrideClassLoader(final ClassLoader loader) {
        appClassLoader = loader;
    }
}
