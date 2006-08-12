/**
 * 
 * Copyright 2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.openejb.persistence;

import org.openejb.persistence.dd.PersistenceUnit;
import org.openejb.persistence.dd.Persistence;
import org.openejb.persistence.dd.TransactionType;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.io.InputStream;
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

    private String providerEnv = null;

    private String transactionTypeEnv = null;

    private String jtaDataSourceEnv = null;

    private String nonJtaDataSourceEnv = null;
    private final DataSourceResolver dataSourceResolver;

    public PersistenceDeployer(DataSourceResolver dataSourceResolver) {

        loadSystemProps();
        this.dataSourceResolver = dataSourceResolver;
    }

    private void loadSystemProps() {
        providerEnv = System.getProperty(PROVIDER_PROP);
        transactionTypeEnv = System.getProperty(TRANSACTIONTYPE_PROP);
        jtaDataSourceEnv = System.getProperty(JTADATASOURCE_PROP);
        nonJtaDataSourceEnv = System.getProperty(NON_JTADATASOURCE_PROP);
    }

    public Map<String, EntityManagerFactory> loadPersistence(ClassLoader cl, URL url) throws PersistenceDeployerException {

        try {

            Map<String, EntityManagerFactory> factories = new HashMap();

            org.openejb.persistence.dd.Persistence persistence = getPersistence(url);

            List<PersistenceUnit> persistenceUnits = persistence.getPersistenceUnit();

            PersistenceUnitInfoImpl unitInfo = new PersistenceUnitInfoImpl();
            for (PersistenceUnit pu : persistenceUnits) {
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
                List<org.openejb.persistence.dd.Property> puiProperties = pu.getProperties().getProperty();
                Properties properties = new Properties();
                for (org.openejb.persistence.dd.Property property : puiProperties) {
                    properties.put(property.getName(), property.getValue());
                }
                unitInfo.setProperties(properties);

                // Persistence Unit Transaction Type
                if (transactionTypeEnv != null) {
                    try {
                        // Override with sys vars
                        PersistenceUnitTransactionType type = Enum.valueOf(PersistenceUnitTransactionType.class, transactionTypeEnv.toUpperCase());
                        unitInfo.setTransactionType(type);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Unknown "+TRANSACTIONTYPE_PROP +", valid options are "+PersistenceUnitTransactionType.JTA+" or "+PersistenceUnitTransactionType.RESOURCE_LOCAL);
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

                // TODO - What do we do here?
                // unitInfo.setPersistenceUnitRootUrl()

                // TODO - What do we do here?
                // unitInfo.setNewTempClassLoader(???);

                Class clazz = (Class) cl.loadClass(unitInfo.getPersistenceProviderClassName());
                PersistenceProvider persistenceProvider = (PersistenceProvider) clazz.newInstance();
                EntityManagerFactory emf = persistenceProvider.createContainerManagerFactory(unitInfo);


                factories.put(unitInfo.getPersistenceUnitName(), emf);
            }

            return factories;
        } catch (Exception e) {
            throw new PersistenceDeployerException(e);
        }

    }

    public org.openejb.persistence.dd.Persistence getPersistence(URL url) throws Exception {
        InputStream persistenceDescriptor = null;

        try {

            persistenceDescriptor = url.openStream();

            JAXBContext jc = JAXBContext.newInstance(Persistence.class);
            Unmarshaller u = jc.createUnmarshaller();
            UnmarshallerHandler uh = u.getUnmarshallerHandler();

            // create a new XML parser
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            SAXParser parser = factory.newSAXParser();

            XMLReader xmlReader = parser.getXMLReader();

            // Create a filter to intercept events
            PersistenceFilter xmlFilter = new PersistenceFilter(xmlReader);

            // Be sure the filter has the JAXB content handler set (or it wont
            // work)
            xmlFilter.setContentHandler(uh);
            SAXSource source = new SAXSource(xmlFilter, new InputSource(persistenceDescriptor));

            return (org.openejb.persistence.dd.Persistence) u.unmarshal(source);

        } finally {
            if (persistenceDescriptor != null) persistenceDescriptor.close();
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


    // Inject the proper namespace
    class PersistenceFilter extends XMLFilterImpl {

        public PersistenceFilter(XMLReader arg0) {
            super(arg0);
        }

        @Override
        public void startElement(String arg0, String arg1, String arg2, Attributes arg3) throws SAXException {
            super.startElement(PERSISTENCE_SCHEMA, arg1, arg2, arg3);
        }
    }

}
