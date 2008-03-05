/**
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

import java.io.File;
import java.util.HashMap;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.openejb.persistence.PersistenceClassLoaderHandler;
import org.apache.openejb.persistence.PersistenceUnitInfoImpl;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.OpenEJBException;

public class PersistenceBuilder {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, PersistenceBuilder.class);

    public static final String PROVIDER_PROP = "javax.persistence.provider";

    public static final String TRANSACTIONTYPE_PROP = "javax.persistence.transactionType";

    public static final String JTADATASOURCE_PROP = "javax.persistence.jtaDataSource";

    public static final String NON_JTADATASOURCE_PROP = "javax.persistence.nonJtaDataSource";

    private static final String DEFAULT_PERSISTENCE_PROVIDER = "org.apache.openjpa.persistence.PersistenceProviderImpl";

    /**
     * External handler which handles adding a runtime ClassTransformer to the classloader.
     */
    private final PersistenceClassLoaderHandler persistenceClassLoaderHandler;

    /**
     * If set, overrides the persistence provider class name in the persistence.xml.
     */
    private String providerEnv;

    /**
     * If set, overrides the transaction type in the persistence.xml.
     */
    private String transactionTypeEnv;

    /**
     * If set, overrides the jta data source class name in the persistence.xml.
     */
    private String jtaDataSourceEnv;

    /**
     * If set, overrides the non-jta data source class name in the persistence.xml.
     */
    private String nonJtaDataSourceEnv;

    public PersistenceBuilder(PersistenceClassLoaderHandler persistenceClassLoaderHandler) {
        loadSystemProps();
        this.persistenceClassLoaderHandler = persistenceClassLoaderHandler;
    }

    private void loadSystemProps() {
        providerEnv = System.getProperty(PROVIDER_PROP);
        transactionTypeEnv = System.getProperty(TRANSACTIONTYPE_PROP);
        jtaDataSourceEnv = System.getProperty(JTADATASOURCE_PROP);
        nonJtaDataSourceEnv = System.getProperty(NON_JTADATASOURCE_PROP);
    }

    public EntityManagerFactory createEntityManagerFactory(PersistenceUnitInfo info, ClassLoader classLoader) throws Exception {
        PersistenceUnitInfoImpl unitInfo = new PersistenceUnitInfoImpl(persistenceClassLoaderHandler);

        // Persistence Unit Id
        unitInfo.setId(info.id);

        // Persistence Unit Name
        unitInfo.setPersistenceUnitName(info.name);

        // Persistence Provider Class Name
        if (providerEnv != null) {
            unitInfo.setPersistenceProviderClassName(providerEnv);
        } else {
            unitInfo.setPersistenceProviderClassName(info.provider);
        }

        // ClassLoader
        unitInfo.setClassLoader(classLoader);

        // Exclude Unlisted Classes
        unitInfo.setExcludeUnlistedClasses(info.excludeUnlistedClasses);

        // JTA Datasource
        String jtaDataSourceId = info.jtaDataSource;
        if (jtaDataSourceEnv != null) jtaDataSourceId = jtaDataSourceEnv;
        if (jtaDataSourceId != null) {
            if (System.getProperty("duct tape") == null){

                try {
                    if (!jtaDataSourceId.startsWith("java:openejb/Resource/")) jtaDataSourceId = "java:openejb/Resource/"+jtaDataSourceId;

                    Context context = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();
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

        // Persistence Unit Transaction Type
        if (transactionTypeEnv != null) {
            try {
                // Override with sys vars
                PersistenceUnitTransactionType type = Enum.valueOf(PersistenceUnitTransactionType.class, transactionTypeEnv.toUpperCase());
                unitInfo.setTransactionType(type);
            } catch (IllegalArgumentException e) {
                throw (IllegalArgumentException)(new IllegalArgumentException("Unknown " + TRANSACTIONTYPE_PROP + ", valid options are " + PersistenceUnitTransactionType.JTA + " or " + PersistenceUnitTransactionType.RESOURCE_LOCAL).initCause(e));
            }
        } else {
            PersistenceUnitTransactionType type = Enum.valueOf(PersistenceUnitTransactionType.class, info.transactionType);
            unitInfo.setTransactionType(type);
        }

        // Non JTA Datasource
        String nonJtaDataSourceId = info.nonJtaDataSource;
        if (nonJtaDataSourceEnv != null) nonJtaDataSourceId = nonJtaDataSourceEnv;
        if (nonJtaDataSourceId != null) {
            if (System.getProperty("duct tape") == null){
                try {
                    if (!nonJtaDataSourceId.startsWith("java:openejb/Resource/")) nonJtaDataSourceId = "java:openejb/Resource/"+nonJtaDataSourceId;

                    Context context = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();
                    DataSource nonJtaDataSource = (DataSource) context.lookup(nonJtaDataSourceId);
                    unitInfo.setNonJtaDataSource(nonJtaDataSource);
                } catch (NamingException e) {
                    throw new OpenEJBException("Could not lookup <non-jta-data-source> '" + nonJtaDataSourceId + "' for unit '" + unitInfo.getPersistenceUnitName() + "'", e);
                }
            }
        }

        // Persistence Unit Root Url
        unitInfo.setRootUrlAndJarUrls(new File(info.persistenceUnitRootUrl).toURL(), info.jarFiles);

        // create the persistence provider
        String persistenceProviderClassName = unitInfo.getPersistenceProviderClassName();
        if (persistenceProviderClassName == null) {
            persistenceProviderClassName = DEFAULT_PERSISTENCE_PROVIDER;
        }
        unitInfo.setPersistenceProviderClassName(persistenceProviderClassName);

        Class clazz = classLoader.loadClass(persistenceProviderClassName);
        PersistenceProvider persistenceProvider = (PersistenceProvider) clazz.newInstance();

        logger.info("assembler.buildingPersistenceUnit", unitInfo.getPersistenceUnitName(), unitInfo.getPersistenceProviderClassName(), unitInfo.getPersistenceUnitRootUrl(), unitInfo.getTransactionType());

        // Create entity manager factory
        EntityManagerFactory emf = persistenceProvider.createContainerEntityManagerFactory(unitInfo, new HashMap());
        return emf;
    }
}
