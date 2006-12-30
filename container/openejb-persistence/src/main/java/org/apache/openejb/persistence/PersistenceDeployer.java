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
package org.apache.openejb.persistence;

import org.apache.openejb.persistence.dd.JaxbPersistenceFactory;
import org.apache.openejb.persistence.dd.PersistenceUnit;
import org.apache.openejb.persistence.dd.TransactionType;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PersistenceDeployer {
    public static final String PERSISTENCE_SCHEMA = "http://java.sun.com/xml/ns/persistence";

    public static final String PROVIDER_PROP = "javax.persistence.provider";

    public static final String TRANSACTIONTYPE_PROP = "javax.persistence.transactionType";

    public static final String JTADATASOURCE_PROP = "javax.persistence.jtaDataSource";

    public static final String NON_JTADATASOURCE_PROP = "javax.persistence.nonJtaDataSource";

    /**
     * Used to resolve jta and non-jta data cource names for actual DataSource objects.
     */
    private final DataSourceResolver dataSourceResolver;

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

    public PersistenceDeployer(DataSourceResolver dataSourceResolver, PersistenceClassLoaderHandler persistenceClassLoaderHandler) {
        loadSystemProps();
        this.dataSourceResolver = dataSourceResolver;
        this.persistenceClassLoaderHandler = persistenceClassLoaderHandler;
    }

    private void loadSystemProps() {
        providerEnv = System.getProperty(PROVIDER_PROP);
        transactionTypeEnv = System.getProperty(TRANSACTIONTYPE_PROP);
        jtaDataSourceEnv = System.getProperty(JTADATASOURCE_PROP);
        nonJtaDataSourceEnv = System.getProperty(NON_JTADATASOURCE_PROP);
    }

    public Map<String, EntityManagerFactory> loadPersistence(ClassLoader cl, URL url) throws PersistenceDeployerException {
        try {
            Map<String, EntityManagerFactory> factories = new HashMap<String, EntityManagerFactory>();

            org.apache.openejb.persistence.dd.Persistence persistence = JaxbPersistenceFactory.getPersistence(url);

            List<PersistenceUnit> persistenceUnits = persistence.getPersistenceUnit();

            for (PersistenceUnit pu : persistenceUnits) {
                PersistenceUnitInfoImpl unitInfo = new PersistenceUnitInfoImpl(persistenceClassLoaderHandler);
                unitInfo.setPersistenceUnitName(pu.getName());
                if (providerEnv != null) {
                    unitInfo.setPersistenceProviderClassName(providerEnv);
                } else {
                    unitInfo.setPersistenceProviderClassName(pu.getProvider());
                }

                unitInfo.setClassLoader(cl);
                if (pu.isExcludeUnlistedClasses() == null) {
                    unitInfo.setExcludeUnlistedClasses(false);
                } else {
                    unitInfo.setExcludeUnlistedClasses(pu.isExcludeUnlistedClasses().booleanValue());
                }

                unitInfo.setJarFileUrls(pu.getJarFile());

                // JTA Datasource
                String dataSource = pu.getJtaDataSource();
                if (jtaDataSourceEnv != null) dataSource = jtaDataSourceEnv;

                if (dataSource != null) {
                    DataSource jtaDataSource = dataSourceResolver.getDataSource(dataSource);
                    unitInfo.setJtaDataSource(jtaDataSource);
                }

                unitInfo.setManagedClassNames(pu.getClazz());
                unitInfo.setMappingFileNames(pu.getMappingFile());

                // Handle Properties
                org.apache.openejb.persistence.dd.Properties puiProperties = pu.getProperties();
                if (puiProperties != null) {
                    Properties properties = new Properties();
                    for (org.apache.openejb.persistence.dd.Property property : puiProperties.getProperty()) {
                        properties.put(property.getName(), property.getValue());
                    }
                    unitInfo.setProperties(properties);
                }

                // Persistence Unit Transaction Type
                if (transactionTypeEnv != null) {
                    try {
                        // Override with sys vars
                        PersistenceUnitTransactionType type = Enum.valueOf(PersistenceUnitTransactionType.class, transactionTypeEnv.toUpperCase());
                        unitInfo.setTransactionType(type);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Unknown " + TRANSACTIONTYPE_PROP + ", valid options are " + PersistenceUnitTransactionType.JTA + " or " + PersistenceUnitTransactionType.RESOURCE_LOCAL);
                    }
                } else {
                    TransactionType tranType = pu.getTransactionType();
                    PersistenceUnitTransactionType type = Enum.valueOf(PersistenceUnitTransactionType.class, tranType.toString());
                    unitInfo.setTransactionType(type);
                }

                // Non JTA Datasource
                String nonJta = pu.getNonJtaDataSource();
                if (nonJtaDataSourceEnv != null) nonJta = nonJtaDataSourceEnv;

                if (nonJta != null) {
                    DataSource nonJtaDataSource = dataSourceResolver.getDataSource(dataSource);
                    unitInfo.setNonJtaDataSource(nonJtaDataSource);
                }

                String rootUrlPath = url.toExternalForm().replaceFirst("!?META-INF/persistence.xml$","");
                unitInfo.setPersistenceUnitRootUrl(new URL(rootUrlPath));

                String persistenceProviderClassName = unitInfo.getPersistenceProviderClassName();
                if (persistenceProviderClassName == null){
                    continue;
                }
                Class clazz = cl.loadClass(persistenceProviderClassName);
                PersistenceProvider persistenceProvider = (PersistenceProvider) clazz.newInstance();
                EntityManagerFactory emf = persistenceProvider.createContainerEntityManagerFactory(unitInfo, new HashMap());


                factories.put(unitInfo.getPersistenceUnitName(), emf);
            }

            return factories;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new PersistenceDeployerException(e);
        }

    }

    public Map<String, EntityManagerFactory> deploy(ClassLoader cl) throws PersistenceDeployerException {

        Map<String, EntityManagerFactory> factoryList = new HashMap<String, EntityManagerFactory>();
        // Read the persistence.xml files
        try {
            Enumeration<URL> urls = cl.getResources("META-INF/persistence.xml");

            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                factoryList.putAll(loadPersistence(cl, url));
            }

        } catch (IOException e) {
            throw new PersistenceDeployerException(e);
        }

        return factoryList;
    }
    
    public Map<String, EntityManagerFactory> deploy(List<URL> urls,ClassLoader cl) throws PersistenceDeployerException {

        Map<String, EntityManagerFactory> factories = new HashMap<String, EntityManagerFactory>();
        // Read the persistence.xml files      

        for (URL url : urls) {
            factories.putAll(loadPersistence(cl, url));
        }        
        return factories;
    }


}
