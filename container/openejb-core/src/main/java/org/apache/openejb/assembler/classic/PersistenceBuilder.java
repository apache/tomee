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
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import jakarta.validation.ValidatorFactory;
import java.util.Map;

public class PersistenceBuilder {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, PersistenceBuilder.class);

    /**
     * External handler which handles adding a runtime ClassTransformer to the classloader.
     */
    private final PersistenceClassLoaderHandler persistenceClassLoaderHandler;

    public PersistenceBuilder(final PersistenceClassLoaderHandler persistenceClassLoaderHandler) {
        this.persistenceClassLoaderHandler = persistenceClassLoaderHandler;
    }

    public ReloadableEntityManagerFactory createEntityManagerFactory(final PersistenceUnitInfo info, final ClassLoader classLoader,
                                                                     final Map<ComparableValidationConfig, ValidatorFactory> validators,
                                                                     final boolean hasCdi) throws Exception {
        final PersistenceUnitInfoImpl unitInfo = new PersistenceUnitInfoImpl(persistenceClassLoaderHandler);

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

        unitInfo.setLazilyInitialized(info.webappName != null || "true".equalsIgnoreCase(info.properties.getProperty("tomee.jpa.factory.lazy",
                SystemInstance.get().getProperty("tomee.jpa.factory.lazy", "false"))));

        final Context context = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();

        // JTA Datasource
        String jtaDataSourceId = info.jtaDataSource;
        unitInfo.setJtaDataSourceName(jtaDataSourceId);
        if (jtaDataSourceId != null) {
            if (!SystemInstance.get().hasProperty("openejb.geronimo")) {

                final String initialJndiName = jtaDataSourceId;
                try {
                    if (!jtaDataSourceId.startsWith("java:openejb/Resource/")
                        && !jtaDataSourceId.startsWith("openejb/Resource/")) {
                        jtaDataSourceId = "openejb/Resource/" + jtaDataSourceId;
                    }

                    final CommonDataSource jtaDataSource = (CommonDataSource) context.lookup(jtaDataSourceId);
                    unitInfo.setJtaDataSource(jtaDataSource);
                } catch (final NamingException e) {
                    try {
                        unitInfo.setJtaDataSource((DataSource) new InitialContext().lookup(initialJndiName));
                    } catch (final NamingException ne) {
                        throw new OpenEJBException("Could not lookup <jta-data-source> '" + jtaDataSourceId + "' for unit '" + unitInfo.getPersistenceUnitName() + "'", e);
                    }
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
        final SharedCacheMode sharedCacheMode = Enum.valueOf(SharedCacheMode.class, info.sharedCacheMode);
        unitInfo.setSharedCacheMode(sharedCacheMode);

        // The validation mode to be used for the persistence unit
        final ValidationMode validationMode = Enum.valueOf(ValidationMode.class, info.validationMode);
        unitInfo.setValidationMode(validationMode);

        // Persistence Unit Transaction Type
        final PersistenceUnitTransactionType type = Enum.valueOf(PersistenceUnitTransactionType.class, info.transactionType);
        unitInfo.setTransactionType(type);

        // Non JTA Datasource
        String nonJtaDataSourceId = info.nonJtaDataSource;
        unitInfo.setNonJtaDataSourceName(nonJtaDataSourceId);
        if (nonJtaDataSourceId != null) {
            if (!SystemInstance.get().hasProperty("openejb.geronimo")) {
                final String initialJndiName = nonJtaDataSourceId;
                try {
                    if (!nonJtaDataSourceId.startsWith("java:openejb/Resource/")) {
                        nonJtaDataSourceId = "java:openejb/Resource/" + nonJtaDataSourceId;
                    }

                    final CommonDataSource nonJtaDataSource = (CommonDataSource) context.lookup(nonJtaDataSourceId);
                    unitInfo.setNonJtaDataSource(nonJtaDataSource);
                } catch (final NamingException e) {
                    try {
                        unitInfo.setNonJtaDataSource((DataSource) new InitialContext().lookup(initialJndiName));
                    } catch (final NamingException ne) {
                        throw new OpenEJBException("Could not lookup <non-jta-data-source> '" + nonJtaDataSourceId + "' for unit '" + unitInfo.getPersistenceUnitName() + "'", e);
                    }
                }
            }
        }

        // Persistence Unit Root Url
        unitInfo.setRootUrlAndJarUrls(info.persistenceUnitRootUrl, info.jarFiles);

        // create the persistence provider
        final String persistenceProviderClassName = unitInfo.getPersistenceProviderClassName();
        unitInfo.setPersistenceProviderClassName(persistenceProviderClassName);

        final EntityManagerFactoryCallable callable = new EntityManagerFactoryCallable(persistenceProviderClassName, unitInfo, classLoader, validators, hasCdi);
        return new ReloadableEntityManagerFactory(classLoader, callable, unitInfo);
    }

    public static String getOpenEJBJndiName(final String unit) {
        return Assembler.PERSISTENCE_UNIT_NAMING_CONTEXT + unit;
    }
}
