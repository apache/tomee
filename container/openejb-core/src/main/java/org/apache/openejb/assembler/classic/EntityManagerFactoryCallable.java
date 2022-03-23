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

import org.apache.openejb.OpenEJB;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.PersistenceUnitInfoImpl;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.InjectableBeanManager;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.PersistenceProvider;
import jakarta.transaction.Transaction;
import jakarta.validation.ValidatorFactory;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class EntityManagerFactoryCallable implements Callable<EntityManagerFactory> {
    public static final String OPENEJB_JPA_INIT_ENTITYMANAGER = "openejb.jpa.init-entitymanager";
    public static final String OPENJPA_ENTITY_MANAGER_FACTORY_POOL = "openjpa.EntityManagerFactoryPool";

    private final String persistenceProviderClassName;
    private final PersistenceUnitInfoImpl unitInfo;
    private final Map<ComparableValidationConfig, ValidatorFactory> potentialValidators;
    private final boolean cdi;
    private ClassLoader appClassLoader;
    private Class<?> provider;

    public EntityManagerFactoryCallable(final String persistenceProviderClassName, final PersistenceUnitInfoImpl unitInfo,
                                        final ClassLoader cl, final Map<ComparableValidationConfig, ValidatorFactory> validators,
                                        final boolean hasCdi) {
        this.persistenceProviderClassName = persistenceProviderClassName;
        this.unitInfo = unitInfo;
        this.appClassLoader = cl;
        this.potentialValidators = validators;
        this.cdi = hasCdi;
    }

    public Class<?> getProvider() {
        if (provider != null) { // no need of thread safety
            return provider;
        }
        final ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(appClassLoader);
        try {
            return (provider = appClassLoader.loadClass(persistenceProviderClassName));
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    @Override
    public EntityManagerFactory call() throws Exception {
        final ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(appClassLoader);
        try {
            final Class<?> clazz = appClassLoader.loadClass(persistenceProviderClassName);
            final PersistenceProvider persistenceProvider = (PersistenceProvider) clazz.newInstance();

            // Create entity manager factories with the validator factory
            final Map<String, Object> properties = new HashMap<>();
            if (!ValidationMode.NONE.equals(unitInfo.getValidationMode())) {
                properties.put("jakarta.persistence.validation.factory",
                        potentialValidators != null && potentialValidators.size() == 1 ? // optim to avoid lookups
                                ensureSerializable(potentialValidators.values().iterator().next()) :
                                new ValidatorFactoryWrapper(potentialValidators));
            }
            if (cdi && "true".equalsIgnoreCase(unitInfo.getProperties().getProperty("tomee.jpa.cdi", "true"))
                    && "true".equalsIgnoreCase(SystemInstance.get().getProperty("tomee.jpa.cdi", "true"))) {
                properties.put("jakarta.persistence.bean.manager",
                        Proxy.newProxyInstance(appClassLoader, new Class<?>[]{BeanManager.class}, new BmHandler()));
            }

            customizeProperties(properties);

            // ensure no tx is there cause a managed connection would fail if the provider setAutocCommit(true) and some hib* have this good idea
            final Transaction transaction;
            if (unitInfo.isLazilyInitialized()) {
                transaction = OpenEJB.getTransactionManager().suspend();
            } else {
                transaction = null;
            }
            final EntityManagerFactory emf;
            try {
                emf = persistenceProvider.createContainerEntityManagerFactory(unitInfo, properties);
            } finally {
                if (unitInfo.isLazilyInitialized() && transaction != null) {
                    OpenEJB.getTransactionManager().resume(transaction);
                }
            }

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

    private ValidatorFactory ensureSerializable(final ValidatorFactory factory) {
        if (Serializable.class.isInstance(factory)) {
            return factory;
        }
        return new SingleValidatorFactoryWrapper(factory);
    }

    private static class BmHandler implements InvocationHandler, Serializable {
        private transient volatile BeanManager bm;

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            try {
                return method.invoke(findBm(), args);
            } catch (final InvocationTargetException ite) {
                Logger.getInstance(LogCategory.OPENEJB_JPA, EntityManagerFactoryCallable.class)
                        .warning("Exception calling CDI, if a lifecycle issue you should maybe set tomee.jpa.factory.lazy=true", ite.getCause());
                throw ite.getCause();
            }
        }

        private Object findBm() {
            if (bm == null) {
                synchronized (this) {
                    if (bm == null) {
                        bm = new InjectableBeanManager(WebBeansContext.currentInstance().getBeanManagerImpl());
                    }
                }
            }
            return bm;
        }
    }
}
