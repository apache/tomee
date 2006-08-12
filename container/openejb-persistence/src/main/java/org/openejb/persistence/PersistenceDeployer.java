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

import org.openejb.persistence.Persistence.PersistenceUnit;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
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

    public static final String FACTORY_JNDI_ROOT = "java:openejb/PersistenceFactories";

    private Properties jndiProperties = null;

    private InitialContext initialContext = null;

    private String providerEnv = null;

    private String transactionTypeEnv = null;

    private String jtaDataSourceEnv = null;

    private String nonJtaDataSourceEnv = null;

    public PersistenceDeployer() {
        this(new Properties());
    }

    public PersistenceDeployer(Properties jndiProperties) {

        loadSystemProps();

        this.jndiProperties = jndiProperties;

        try {
            initialContext = new InitialContext(this.jndiProperties);
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }

    private void loadSystemProps() {
        providerEnv = System.getProperty(PROVIDER_PROP);
        transactionTypeEnv = System.getProperty(TRANSACTIONTYPE_PROP);
        jtaDataSourceEnv = System.getProperty(JTADATASOURCE_PROP);
        nonJtaDataSourceEnv = System.getProperty(NON_JTADATASOURCE_PROP);
    }

    public void loadPersistence(ClassLoader cl, URL url) throws PersistenceDeployerException {

        try {

            org.openejb.persistence.Persistence persistence = getPersistence(url);

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
                    if (initialContext == null) {
                        throw new PersistenceDeployerException("No InitialContext, are you running in a JTA container?");
                    }
                    DataSource jtaDataSource = (DataSource) initialContext.lookup(dataSource);
                    unitInfo.setJtaDataSource(jtaDataSource);
                }

                unitInfo.setManagedClassNames(pu.getClazz());
                unitInfo.setMappingFileNames(pu.getMappingFile());

                // Handle Properties
                List<org.openejb.persistence.Persistence.PersistenceUnit.Properties.Property> puiProperties = pu.getProperties().getProperty();
                Properties properties = new Properties();
                for (org.openejb.persistence.Persistence.PersistenceUnit.Properties.Property property : puiProperties) {
                    properties.put(property.getName(), property.getValue());
                }
                unitInfo.setProperties(properties);

                // Persistence Unit Transaction Type
                if (transactionTypeEnv != null) {
                    // Override with sys vars
                    if (transactionTypeEnv.toUpperCase().equals("JTA")) {
                        unitInfo.setTransactionType(PersistenceUnitTransactionType.JTA);
                    }
                    if (transactionTypeEnv.toUpperCase().equals("RESOURCE_LOCAL")) {
                        unitInfo.setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
                    }
                } else {
                    PersistentUnitTransactionType tranType = pu.getTransactionType();
                    if ((tranType == null) || (tranType == PersistentUnitTransactionType.JTA)) {
                        unitInfo.setTransactionType(PersistenceUnitTransactionType.JTA);
                    } else {
                        unitInfo.setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
                    }
                }

                // Non JTA Datasource
                String nonJta = pu.getNonJtaDataSource();
                if (nonJtaDataSourceEnv != null) nonJta = nonJtaDataSourceEnv;

                if (nonJta != null) {
                    if (initialContext == null) {
                        throw new PersistenceDeployerException("InitialContext is null.");
                    }
                    DataSource nonJtaDataSource = (DataSource) initialContext.lookup(nonJta);
                    unitInfo.setNonJtaDataSource(nonJtaDataSource);
                }

                // TODO - What do we do here?
                // unitInfo.setPersistenceUnitRootUrl()

                // TODO - What do we do here?
                // unitInfo.setNewTempClassLoader(???);

                Class clazz = (Class) cl.loadClass(unitInfo.getPersistenceProviderClassName());
                PersistenceProvider persistenceProvider = (PersistenceProvider) clazz.newInstance();
                EntityManagerFactory emf = persistenceProvider.createContainerManagerFactory(unitInfo);

                // Store EntityManagerFactory in the JNDI
                bind(FACTORY_JNDI_ROOT + "/" + unitInfo.getPersistenceUnitName(), emf);

            }
        } catch (Exception e) {
            throw new PersistenceDeployerException(e);
        }

    }

    public org.openejb.persistence.Persistence getPersistence(URL url) throws Exception {
        InputStream persistenceDescriptor = null;

        try {

            persistenceDescriptor = url.openStream();

            JAXBContext jc = JAXBContext.newInstance("org.openejb.persistence");
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

            return (org.openejb.persistence.Persistence) u.unmarshal(source);

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
                loadPersistence(cl, url);
            }

        } catch (IOException e) {
            throw new PersistenceDeployerException(e);
        }

        return factoryList;
    }

    private void bind(String name, Object obj) throws NamingException {
        if (name.startsWith("java:"))
            name = name.substring(5);

        CompositeName composite = new CompositeName(name);
        Context ctx = initialContext;
        if (composite.size() > 1) {
            for (int i = 0; i < composite.size() - 1; i++) {
                try {
                    Object ctxObj = ctx.lookup(composite.get(i));
                    if (!(ctxObj instanceof Context)) {
                        throw new NamingException("Invalid JNDI path.");
                    }
                    ctx = (Context) ctxObj;
                } catch (NameNotFoundException e) {
                    //Name was not found, so add a new subcontext
                    ctx = ctx.createSubcontext(composite.get(i));
                }
            }
        }

        ctx.bind(composite.get(composite.size() - 1), obj);
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
