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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.PersistenceClassLoaderHandler;
import org.apache.openejb.persistence.PersistenceUnitInfoImpl;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PersistenceBuilder {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, PersistenceBuilder.class);

    /**
     * External handler which handles adding a runtime ClassTransformer to the classloader.
     */
    private final PersistenceClassLoaderHandler persistenceClassLoaderHandler;

    public PersistenceBuilder(PersistenceClassLoaderHandler persistenceClassLoaderHandler) {
        this.persistenceClassLoaderHandler = persistenceClassLoaderHandler;
    }

    public EntityManagerFactory createEntityManagerFactory(PersistenceUnitInfo info, ClassLoader classLoader) throws Exception {
        PersistenceUnitInfoImpl unitInfo = new PersistenceUnitInfoImpl(persistenceClassLoaderHandler);

        // Persistence Unit Id
        unitInfo.setId(info.id);

        // Persistence Unit Name
        unitInfo.setPersistenceUnitName(info.name);

        // Persistence Provider Class Name
        unitInfo.setPersistenceProviderClassName(info.provider);

        // ClassLoader
        unitInfo.setClassLoader(classLoader);

        // Exclude Unlisted Classes
        unitInfo.setExcludeUnlistedClasses(info.excludeUnlistedClasses);

        Context context = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();

        // JTA Datasource
        String jtaDataSourceId = info.jtaDataSource;
        if (jtaDataSourceId != null) {
            if (!SystemInstance.get().hasProperty("openejb.geronimo")) {

                try {
                    if (!jtaDataSourceId.startsWith("java:openejb/Resource/")
                            && !jtaDataSourceId.startsWith("openejb/Resource/")) jtaDataSourceId = "openejb/Resource/"+jtaDataSourceId;

                    DataSource jtaDataSource = (DataSource) context.lookup(jtaDataSourceId);
                    unitInfo.setJtaDataSource(jtaDataSource);
                } catch (NamingException e) {
                    throw new OpenEJBException("Could not lookup <jta-data-source> '" + jtaDataSourceId + "' for unit '" + unitInfo.getPersistenceUnitName() + "'", e);
                }
            }
        }

        // Managed Class Names
        unitInfo.setManagedClassNames(info.classes);

        // Mapping File Names
        unitInfo.setMappingFileNames(info.mappingFiles);

        // Handle Properties
        unitInfo.setProperties(info.properties);
        
        // Schema version of the persistence.xml file
        unitInfo.setPersistenceXMLSchemaVersion(info.persistenceXMLSchemaVersion);
        
        // Second-level cache mode for the persistence unit
        SharedCacheMode sharedCacheMode = Enum.valueOf(SharedCacheMode.class, info.sharedCacheMode);
        unitInfo.setSharedCacheMode(sharedCacheMode);
        
        // The validation mode to be used for the persistence unit
        ValidationMode validationMode = Enum.valueOf(ValidationMode.class, info.validationMode);
        unitInfo.setValidationMode(validationMode);
        
        // Persistence Unit Transaction Type
        PersistenceUnitTransactionType type = Enum.valueOf(PersistenceUnitTransactionType.class, info.transactionType);
        unitInfo.setTransactionType(type);

        // Non JTA Datasource
        String nonJtaDataSourceId = info.nonJtaDataSource;
        if (nonJtaDataSourceId != null) {
            if (!SystemInstance.get().hasProperty("openejb.geronimo")) {
                try {
                    if (!nonJtaDataSourceId.startsWith("java:openejb/Resource/")) nonJtaDataSourceId = "java:openejb/Resource/"+nonJtaDataSourceId;

                    DataSource nonJtaDataSource = (DataSource) context.lookup(nonJtaDataSourceId);
                    unitInfo.setNonJtaDataSource(nonJtaDataSource);
                } catch (NamingException e) {
                    throw new OpenEJBException("Could not lookup <non-jta-data-source> '" + nonJtaDataSourceId + "' for unit '" + unitInfo.getPersistenceUnitName() + "'", e);
                }
            }
        }

        // Persistence Unit Root Url
        unitInfo.setRootUrlAndJarUrls(info.persistenceUnitRootUrl, info.jarFiles);

        // create the persistence provider
        String persistenceProviderClassName = unitInfo.getPersistenceProviderClassName();
        unitInfo.setPersistenceProviderClassName(persistenceProviderClassName);

        final long start = System.nanoTime();
        try {
            final ExecutorService executor = Executors.newSingleThreadExecutor(new EntityManagerFactoryThreadFactory(classLoader));
            final Future<EntityManagerFactory> future = executor.submit(
                    new EntityManagerFactoryCallable(persistenceProviderClassName, unitInfo)
            );
            return future.get(10, TimeUnit.MINUTES);
        } finally {
            final long time = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            logger.info("assembler.buildingPersistenceUnit", unitInfo.getPersistenceUnitName(), unitInfo.getPersistenceProviderClassName(), time+"");
            if (logger.isDebugEnabled()) {
                for (Map.Entry<Object, Object> entry : unitInfo.getProperties().entrySet()) {
                    logger.debug(entry.getKey() + "=" + entry.getValue());
                }
            }
        }
    }

    public static String getOpenEJBJndiName(String unit) {
        return Assembler.PERSISTENCE_UNIT_NAMING_CONTEXT + unit;
    }
}
