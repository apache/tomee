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
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.core.webservices.WsdlResolver;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.Connector10;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.FacesConfig;
import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.JavaWsdlMapping;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.Listener;
import org.apache.openejb.jee.TldTaglib;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.Webservices;
import org.apache.openejb.jee.bval.ValidationConfigType;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.fragment.PersistenceFragment;
import org.apache.openejb.jee.jpa.fragment.PersistenceUnitFragment;
import org.apache.openejb.jee.jpa.unit.JaxbPersistenceFactory;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.oejb2.GeronimoEjbJarType;
import org.apache.openejb.jee.oejb2.JaxbOpenejbJar2;
import org.apache.openejb.jee.oejb2.OpenejbJarType;
import org.apache.openejb.jee.oejb3.JaxbOpenejbJar3;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.URLs;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class ReadDescriptors implements DynamicDeployer {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, ReadDescriptors.class);

    @SuppressWarnings({"unchecked"})
    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        for (EjbModule ejbModule : appModule.getEjbModules()) {

            if (ejbModule.getEjbJar() == null) {
                readEjbJar(ejbModule, appModule);
            }

            if (ejbModule.getOpenejbJar() == null) {
                readOpenejbJar(ejbModule);
            }

            if (ejbModule.getBeans() == null) {
                readBeans(ejbModule, appModule);
            }

            readValidationConfigType(ejbModule);
            readCmpOrm(ejbModule);
            readResourcesXml(ejbModule);
        }

        for (ClientModule clientModule : appModule.getClientModules()) {
            readAppClient(clientModule, appModule);
            readValidationConfigType(clientModule);
            readResourcesXml(clientModule);
        }

        for (ConnectorModule connectorModule : appModule.getConnectorModules()) {
            readConnector(connectorModule, appModule);
            readValidationConfigType(connectorModule);
            readResourcesXml(connectorModule);
        }

        for (WebModule webModule : appModule.getWebModules()) {
            readWebApp(webModule, appModule);
            readValidationConfigType(webModule);
            readResourcesXml(webModule);
        }

        List<URL> persistenceUrls = (List<URL>) appModule.getAltDDs().get("persistence.xml");
        if (persistenceUrls != null) {
            for (URL persistenceUrl : persistenceUrls) {
                File file = URLs.toFile(persistenceUrl);
                String path = file.getAbsolutePath();

                if (file.getName().endsWith("persistence.xml")) {
                    file = file.getParentFile().getParentFile();
                }
                String  moduleName = file.toURI().toString();

                String rootUrl = moduleName;

                String extForm = persistenceUrl.toExternalForm();
                if (extForm.contains("WEB-INF/classes/META-INF/")) {
                    rootUrl = extForm.substring(0, extForm.indexOf("/META-INF"));
                }
                if (rootUrl.endsWith(".war")) {
                    rootUrl = rootUrl.substring(0, rootUrl.length() - ".war".length());
                }

                try {
                    Persistence persistence = JaxbPersistenceFactory.getPersistence(Persistence.class, persistenceUrl);
                    PersistenceModule persistenceModule = new PersistenceModule(rootUrl, persistence);
                    persistenceModule.getWatchedResources().add(moduleName);
                    if ("file".equals(persistenceUrl.getProtocol())) {
                        persistenceModule.getWatchedResources().add(path);
                    }
                    appModule.getPersistenceModules().add(persistenceModule);
                } catch (Exception e1) {
                    DeploymentLoader.logger.error("Unable to load Persistence Unit from EAR: " + appModule.getJarLocation() + ", module: " + moduleName + ". Exception: " + e1.getMessage(), e1);
                }
            }
        }

        final List<URL> persistenceFragmentUrls = (List<URL>) appModule.getAltDDs().get("persistence-fragment.xml");
        if (persistenceFragmentUrls != null) {
            for (URL persistenceFragmentUrl : persistenceFragmentUrls) {
                try {
                    final PersistenceFragment persistenceFragment = JaxbPersistenceFactory.getPersistence(PersistenceFragment.class, persistenceFragmentUrl);
                    // merging
                    for (PersistenceUnitFragment fragmentUnit : persistenceFragment.getPersistenceUnitFragment()) {
                        for (PersistenceModule persistenceModule : appModule.getPersistenceModules()) {
                            final Persistence persistence = persistenceModule.getPersistence();
                            for (PersistenceUnit unit : persistence.getPersistenceUnit()) {
                                if (!fragmentUnit.getName().equals(unit.getName())) {
                                    continue;
                                }

                                if (!persistenceFragment.getVersion().equals(persistence.getVersion())) {
                                    logger.error("persistence unit version and fragment version are different, fragment will be ignored");
                                    continue;
                                }

                                if ("file".equals(persistenceFragmentUrl.getProtocol())) {
                                    persistenceModule.getWatchedResources().add(URLs.toFile(persistenceFragmentUrl).getAbsolutePath());
                                }

                                for (String clazz : fragmentUnit.getClazz()) {
                                    if (!unit.getClazz().contains(clazz)) {
                                        logger.info("Adding class " + clazz + " to persistence unit " + fragmentUnit.getName());
                                        unit.getClazz().add(clazz);
                                    }
                                }
                                for (String mappingFile : fragmentUnit.getMappingFile()) {
                                    if (!unit.getMappingFile().contains(mappingFile)) {
                                        logger.info("Adding mapping file " + mappingFile + " to persistence unit " + fragmentUnit.getName());
                                        unit.getMappingFile().add(mappingFile);
                                    }
                                }
                                for (String jarFile : fragmentUnit.getJarFile()) {
                                    if (!unit.getJarFile().contains(jarFile)) {
                                        logger.info("Adding jar file " + jarFile + " to persistence unit " + fragmentUnit.getName());
                                        unit.getJarFile().add(jarFile);
                                    }
                                }
                                if (fragmentUnit.isExcludeUnlistedClasses()) {
                                    unit.setExcludeUnlistedClasses(true);
                                    logger.info("Excluding unlisted classes for persistence unit " + fragmentUnit.getName());
                                } // else let the main persistence unit decide
                            }
                        }
                    }
                } catch (Exception e1) {
                    DeploymentLoader.logger.error("Unable to load Persistence Unit Fragment from EAR: " + appModule.getJarLocation() + ", fragment: " + persistenceFragmentUrl.toString() + ". Exception: " + e1.getMessage(), e1);
                }
            }
        }

        return appModule;

    }

    private static URL getUrl(Module module, String name) {
        URL url = (URL) module.getAltDDs().get(name);
        if (url == null && module.getClassLoader() != null) {
            url = module.getClassLoader().getResource("META-INF/" + name);
            if (url != null) {
                module.getAltDDs().put(name, url);
            }
        }
        return url;
    }

    public static void readResourcesXml(Module module) {
        URL url = getUrl(module, "resources.xml");
        if (url != null) {
            try {
                Resources openejb = JaxbOpenejb.unmarshal(Resources.class, IO.read(url));
                module.initResources(openejb);

                // warn if other entities than resources were declared
                if (openejb.getContainer().size() > 0) {
                    logger.warning("containers can't be declared at module level");
                }
                if (openejb.getConnectionManager() != null) {
                    logger.warning("connection manager can't be declared at module level");
                }
                if (openejb.getJndiProvider().size() > 0) {
                    logger.warning("jndi providers can't be declared at module level");
                }
            } catch (Exception e) {
                logger.warning("can't read " + url.toString() + " to load resources for module " + module.toString(), e);
            }
        }
    }

    private void readValidationConfigType(Module module) throws OpenEJBException {
        if (module.getValidationConfig() != null) {
            return;
        }
        URL url = getUrl(module, "validation.xml");
        if (url != null) {
            ValidationConfigType validationConfigType;
            try {
                validationConfigType = JaxbOpenejb.unmarshal(ValidationConfigType.class, IO.read(url), false);
                module.setValidationConfig(validationConfigType);
            } catch (Exception e) {
                logger.warning("can't read " + url.toString() + " to construct a validation factory, it will be ignored");
            }
        }
    }

    private void readOpenejbJar(EjbModule ejbModule) throws OpenEJBException {
        Source source = getSource(ejbModule.getAltDDs().get("openejb-jar.xml"));

        if (source != null) {
            try {
                // Attempt to parse it first as a v3 descriptor
                OpenejbJar openejbJar = JaxbOpenejbJar3.unmarshal(OpenejbJar.class, source.get());
                ejbModule.setOpenejbJar(openejbJar);
            } catch (final Exception v3ParsingException) {
                // Attempt to parse it second as a v2 descriptor
                OpenejbJar openejbJar = new OpenejbJar();
                ejbModule.setOpenejbJar(openejbJar);

                try {
                    JAXBElement element = (JAXBElement) JaxbOpenejbJar2.unmarshal(OpenejbJarType.class, source.get());
                    OpenejbJarType o2 = (OpenejbJarType) element.getValue();
                    ejbModule.getAltDDs().put("openejb-jar.xml", o2);

                    GeronimoEjbJarType g2 = OpenEjb2Conversion.convertToGeronimoOpenejbXml(o2);

                    ejbModule.getAltDDs().put("geronimo-openejb.xml", g2);
                } catch (final Exception v2ParsingException) {
                    // Now we have to determine which error to throw; the v3 file exception or the fallback v2 file exception.
                    final Exception[] realIssue = {v3ParsingException};

                    try {
                        SAXParserFactory factory = SAXParserFactory.newInstance();
                        factory.setNamespaceAware(true);
                        factory.setValidating(false);
                        SAXParser parser = factory.newSAXParser();
                        parser.parse(source.get(), new DefaultHandler() {
                            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                                if (localName.equals("environment")) {
                                    realIssue[0] = v2ParsingException;
                                    throw new SAXException("Throw exception to stop parsing");
                                }
                                if (uri == null) return;
                                if (uri.contains("openejb-jar-2.") || uri.contains("geronimo.apache.org/xml/ns")) {
                                    realIssue[0] = v2ParsingException;
                                    throw new SAXException("Throw exception to stop parsing");
                                }
                            }
                        });
                    } catch (Exception dontCare) {
                    }

                    String filePath = "<error: could not be written>";
                    try {
                        File tempFile = File.createTempFile("openejb-jar-", ".xml");
                        try {
                            IO.copy(source.get(), tempFile);
                        } catch (IOException e) {
                        }
                        filePath = tempFile.getAbsolutePath();
                    } catch (IOException e) {
                    }

                    Exception e = realIssue[0];
                    if (e instanceof SAXException) {
                        throw new OpenEJBException("Cannot parse the openejb-jar.xml. Xml content written to: "+filePath, e);
                    } else if (e instanceof JAXBException) {
                        throw new OpenEJBException("Cannot unmarshall the openejb-jar.xml. Xml content written to: "+filePath, e);
                    } else if (e instanceof IOException) {
                        throw new OpenEJBException("Cannot read the openejb-jar.xml.", e);
                    } else {
                        throw new OpenEJBException("Encountered unknown error parsing the openejb-jar.xml.", e);
                    }
                }
            }
        }

        Source source1 = getSource(ejbModule.getAltDDs().get("geronimo-openejb.xml"));
        if (source1 != null) {
            try {
                GeronimoEjbJarType geronimoEjbJarType = null;
                Object o = JaxbOpenejbJar2.unmarshal(GeronimoEjbJarType.class, source1.get());
                if (o instanceof GeronimoEjbJarType) {
                    geronimoEjbJarType = (GeronimoEjbJarType) o;
                } else if (o instanceof JAXBElement) {
                    JAXBElement element = (JAXBElement) o;
                    geronimoEjbJarType = (GeronimoEjbJarType) element.getValue();
                }
                if (geronimoEjbJarType != null) {
                    Object nested = geronimoEjbJarType.getOpenejbJar();
                    if (nested != null && nested instanceof OpenejbJar) {
                        OpenejbJar existingOpenejbJar = ejbModule.getOpenejbJar();
                        if (existingOpenejbJar == null || existingOpenejbJar.getEjbDeploymentCount() <= 0) {
                            OpenejbJar openejbJar = (OpenejbJar) nested;
                            ejbModule.getAltDDs().put("openejb-jar.xml", openejbJar);
                            ejbModule.setOpenejbJar(openejbJar);
                        }
                    }
                    ejbModule.getAltDDs().put("geronimo-openejb.xml", geronimoEjbJarType);
                }
            } catch (Exception e) {
                throw new OpenEJBException("Failed parsing geronimo-openejb.xml", e);
            }
        }

    }

    private void readAppClient(ClientModule clientModule, AppModule appModule) throws OpenEJBException {
        if (clientModule.getApplicationClient() != null) return;

        Object data = clientModule.getAltDDs().get("application-client.xml");
        if (data instanceof ApplicationClient) {
            clientModule.setApplicationClient((ApplicationClient) data);
        } else if (data instanceof URL) {
            URL url = (URL) data;
            ApplicationClient applicationClient = readApplicationClient(url);
            clientModule.setApplicationClient(applicationClient);
        } else {
            if (!clientModule.isEjbModuleGenerated()) {
                DeploymentLoader.logger.debug("No application-client.xml found assuming annotations present: " + appModule.getJarLocation() + ", module: " + clientModule.getModuleId());
                clientModule.setApplicationClient(new ApplicationClient());
            }
        }
    }

    private void readEjbJar(EjbModule ejbModule, AppModule appModule) throws OpenEJBException {
        if (ejbModule.getEjbJar() != null) return;

        Object data = ejbModule.getAltDDs().get("ejb-jar.xml");
        if (data instanceof EjbJar) {
            ejbModule.setEjbJar((EjbJar) data);
        } else if (data instanceof URL) {
            URL url = (URL) data;
            EjbJar ejbJar = readEjbJar(url);
            ejbModule.setEjbJar(ejbJar);
        } else {
            DeploymentLoader.logger.debug("No ejb-jar.xml found assuming annotated beans present: " + appModule.getJarLocation() + ", module: " + ejbModule.getModuleId());
            ejbModule.setEjbJar(new EjbJar());
        }
    }

    private void readBeans(EjbModule ejbModule, AppModule appModule) throws OpenEJBException {
        if (ejbModule.getBeans() != null) return;

        Object data = ejbModule.getAltDDs().get("beans.xml");
        if (data instanceof Beans) {
            ejbModule.setBeans((Beans) data);
        } else if (data instanceof URL) {
            URL url = (URL) data;
            Beans beans = readBeans(url);
            ejbModule.setBeans(beans);
        } else {
//            DeploymentLoader.logger.debug("No beans.xml found assuming annotated beans present: " + appModule.getJarLocation() + ", module: " + ejbModule.getModuleId());
//            ejbModule.setBeans(new Beans());
        }
    }

    private void readCmpOrm(EjbModule ejbModule) throws OpenEJBException {
        Object data = ejbModule.getAltDDs().get("openejb-cmp-orm.xml");
        if (data == null || data instanceof EntityMappings) {
            return;
        } else if (data instanceof URL) {
            URL url = (URL) data;
            try {
                EntityMappings entitymappings = (EntityMappings) JaxbJavaee.unmarshalJavaee(EntityMappings.class, IO.read(url));
                ejbModule.getAltDDs().put("openejb-cmp-orm.xml", entitymappings);
            } catch (SAXException e) {
                throw new OpenEJBException("Cannot parse the openejb-cmp-orm.xml file: " + url.toExternalForm(), e);
            } catch (JAXBException e) {
                throw new OpenEJBException("Cannot unmarshall the openejb-cmp-orm.xml file: " + url.toExternalForm(), e);
            } catch (IOException e) {
                throw new OpenEJBException("Cannot read the openejb-cmp-orm.xml file: " + url.toExternalForm(), e);
            } catch (Exception e) {
                throw new OpenEJBException("Encountered unknown error parsing the openejb-cmp-orm.xml file: " + url.toExternalForm(), e);
            }
        }
    }

    private void readConnector(ConnectorModule connectorModule, AppModule appModule) throws OpenEJBException {
        if (connectorModule.getConnector() != null) return;

        Object data = connectorModule.getAltDDs().get("ra.xml");
        if (data instanceof Connector) {
            connectorModule.setConnector((Connector) data);
        } else if (data instanceof URL) {
            URL url = (URL) data;
            Connector connector = readConnector(url);
            connectorModule.setConnector(connector);
        } else {
            DeploymentLoader.logger.debug("No ra.xml found assuming annotated beans present: " + appModule.getJarLocation() + ", module: " + connectorModule.getModuleId());
            connectorModule.setConnector(new Connector());
        }
    }

    private void readWebApp(WebModule webModule, AppModule appModule) throws OpenEJBException {
        if (webModule.getWebApp() != null) return;

        Object data = webModule.getAltDDs().get("web.xml");
        if (data instanceof WebApp) {
            webModule.setWebApp((WebApp) data);
        } else if (data instanceof URL) {
            URL url = (URL) data;
            WebApp webApp = readWebApp(url);
            webModule.setWebApp(webApp);
        } else {
            DeploymentLoader.logger.debug("No web.xml found assuming annotated beans present: " + appModule.getJarLocation() + ", module: " + webModule.getModuleId());
            webModule.setWebApp(new WebApp());
        }
    }

    public static ApplicationClient readApplicationClient(URL url) throws OpenEJBException {
        ApplicationClient applicationClient;
        try {
            applicationClient = (ApplicationClient) JaxbJavaee.unmarshalJavaee(ApplicationClient.class, IO.read(url));
        } catch (SAXException e) {
            throw new OpenEJBException("Cannot parse the application-client.xml file: "+ url.toExternalForm(), e);
        } catch (JAXBException e) {
            throw new OpenEJBException("Cannot unmarshall the application-client.xml file: "+ url.toExternalForm(), e);
        } catch (IOException e) {
            throw new OpenEJBException("Cannot read the application-client.xml file: "+ url.toExternalForm(), e);
        } catch (Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the application-client.xml file: "+ url.toExternalForm(), e);
        }
        return applicationClient;
    }

    public static EjbJar readEjbJar(URL url) throws OpenEJBException {
        try {
            if (isEmptyEjbJar(url)) return new EjbJar();
            return (EjbJar) JaxbJavaee.unmarshalJavaee(EjbJar.class, IO.read(url));
        } catch (SAXException e) {
            throw new OpenEJBException("Cannot parse the ejb-jar.xml file: " + url.toExternalForm(), e);
        } catch (JAXBException e) {
            throw new OpenEJBException("Cannot unmarshall the ejb-jar.xml file: " + url.toExternalForm(), e);
        } catch (IOException e) {
            throw new OpenEJBException("Cannot read the ejb-jar.xml file: " + url.toExternalForm(), e);
        } catch (Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the ejb-jar.xml file: " + url.toExternalForm(), e);
        }
    }

    public static Beans readBeans(URL url) throws OpenEJBException {
        try {
            if (isEmptyBeansXml(url)) return new Beans();
            return (Beans) JaxbJavaee.unmarshalJavaee(Beans.class, IO.read(url));
        } catch (SAXException e) {
            throw new OpenEJBException("Cannot parse the beans.xml file: " + url.toExternalForm(), e);
        } catch (JAXBException e) {
            throw new OpenEJBException("Cannot unmarshall the beans.xml file: " + url.toExternalForm(), e);
        } catch (IOException e) {
            throw new OpenEJBException("Cannot read the beans.xml file: " + url.toExternalForm(), e);
        } catch (Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the beans.xml file: " + url.toExternalForm(), e);
        }
    }

    private static boolean isEmptyEjbJar(URL url) throws IOException, ParserConfigurationException, SAXException {
        return isEmpty(url, "ejb-jar");
    }

    private static boolean isEmptyBeansXml(URL url) throws IOException, ParserConfigurationException, SAXException {
        return isEmpty(url, "beans");
    }

    private static boolean isEmpty(URL url, final String rootElement) throws IOException, ParserConfigurationException, SAXException {
        final LengthInputStream in = new LengthInputStream(IO.read(url));
        InputSource inputSource = new InputSource(in);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        try {
            parser.parse(inputSource, new DefaultHandler(){
                public void startElement(String uri, String localName, String qName, Attributes att) throws SAXException {
                    if (!localName.equals(rootElement)) throw new SAXException(localName);
                }

                public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
                    return new InputSource(new ByteArrayInputStream(new byte[0]));
                }
            });
            return true;
        } catch (SAXException e) {
            return in.getLength() == 0;
        }
    }

    public static Webservices readWebservices(URL url) throws OpenEJBException {
        Webservices webservices;
        try {
            webservices = (Webservices) JaxbJavaee.unmarshalJavaee(Webservices.class, IO.read(url));
        } catch (SAXException e) {
            throw new OpenEJBException("Cannot parse the webservices.xml file: " + url.toExternalForm(), e);
        } catch (JAXBException e) {
            throw new OpenEJBException("Cannot unmarshall the webservices.xml file: " + url.toExternalForm(), e);
        } catch (IOException e) {
            throw new OpenEJBException("Cannot read the webservices.xml file: " + url.toExternalForm(), e);
        } catch (Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the webservices.xml file: " + url.toExternalForm(), e);
        }
        return webservices;
    }

    public static HandlerChains readHandlerChains(URL url) throws OpenEJBException {
        HandlerChains handlerChains;
        try {
            handlerChains = (HandlerChains) JaxbJavaee.unmarshalHandlerChains(HandlerChains.class, IO.read(url));
        } catch (SAXException e) {
            throw new OpenEJBException("Cannot parse the webservices.xml file: " + url.toExternalForm(), e);
        } catch (JAXBException e) {
            throw new OpenEJBException("Cannot unmarshall the webservices.xml file: " + url.toExternalForm(), e);
        } catch (IOException e) {
            throw new OpenEJBException("Cannot read the webservices.xml file: " + url.toExternalForm(), e);
        } catch (Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the webservices.xml file: " + url.toExternalForm(), e);
        }
        return handlerChains;
    }

    public static JavaWsdlMapping readJaxrpcMapping(URL url) throws OpenEJBException {
        JavaWsdlMapping wsdlMapping;
        try {
            wsdlMapping = (JavaWsdlMapping) JaxbJavaee.unmarshalJavaee(JavaWsdlMapping.class, IO.read(url));
        } catch (SAXException e) {
            throw new OpenEJBException("Cannot parse the JaxRPC mapping file: " + url.toExternalForm(), e);
        } catch (JAXBException e) {
            throw new OpenEJBException("Cannot unmarshall the JaxRPC mapping file: " + url.toExternalForm(), e);
        } catch (IOException e) {
            throw new OpenEJBException("Cannot read the JaxRPC mapping file: " + url.toExternalForm(), e);
        } catch (Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the JaxRPC mapping file: " + url.toExternalForm(), e);
        }
        return wsdlMapping;
    }

    public static Definition readWsdl(URL url) throws OpenEJBException {
        Definition definition;
        try {
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", true);
            reader.setFeature("javax.wsdl.importDocuments", true);
            WsdlResolver wsdlResolver = new WsdlResolver(new URL(url, ".").toExternalForm(), new InputSource(IO.read(url)));
            definition = reader.readWSDL(wsdlResolver);
        } catch (IOException e) {
            throw new OpenEJBException("Cannot read the wsdl file: " + url.toExternalForm(), e);
        } catch (Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the wsdl file: " + url.toExternalForm(), e);
        }
        return definition;
    }

    public static Connector readConnector(URL url) throws OpenEJBException {
        Connector connector;
        try {
            connector = (Connector) JaxbJavaee.unmarshalJavaee(Connector.class, IO.read(url));
        } catch (JAXBException e) {
            try {
                Connector10 connector10 = (Connector10) JaxbJavaee.unmarshalJavaee(Connector10.class, IO.read(url));
                connector = Connector.newConnector(connector10);
            } catch (ParserConfigurationException e1) {
                throw new OpenEJBException("Cannot parse the ra.xml file: " + url.toExternalForm(), e);
            } catch (SAXException e1) {
                throw new OpenEJBException("Cannot parse the ra.xml file: " + url.toExternalForm(), e);
            } catch (JAXBException e1) {
                throw new OpenEJBException("Cannot unmarshall the ra.xml file: " + url.toExternalForm(), e);
            } catch (IOException e1) {
                throw new OpenEJBException("Cannot read the ra.xml file: " + url.toExternalForm(), e);
            }
        } catch (SAXException e) {
            throw new OpenEJBException("Cannot parse the ra.xml file: " + url.toExternalForm(), e);
        } catch (IOException e) {
            throw new OpenEJBException("Cannot read the ra.xml file: " + url.toExternalForm(), e);
        } catch (Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the ra.xml file: " + url.toExternalForm(), e);
        }
        return connector;
    }

    public static WebApp readWebApp(URL url) throws OpenEJBException {
        WebApp webApp;
        try {
            webApp = (WebApp) JaxbJavaee.unmarshalJavaee(WebApp.class, IO.read(url));
        } catch (SAXException e) {
            throw new OpenEJBException("Cannot parse the web.xml file: " + url.toExternalForm(), e);
        } catch (JAXBException e) {
            throw new OpenEJBException("Cannot unmarshall the web.xml file: " + url.toExternalForm(), e);
        } catch (IOException e) {
            throw new OpenEJBException("Cannot read the web.xml file: " + url.toExternalForm(), e);
        } catch (Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the web.xml file: " + url.toExternalForm(), e);
        }
        return webApp;
    }

    public static TldTaglib readTldTaglib(URL url) throws OpenEJBException {
        // TOMEE-164 Optimization on reading built-in tld files
        if (url.getPath().contains("jstl-1.2.jar")) return new TldTaglib();
        if (url.getPath().contains("myfaces-impl")) {
            final TldTaglib taglib = new TldTaglib();
            final Listener listener = new Listener();
            listener.setListenerClass("org.apache.myfaces.webapp.StartupServletContextListener");
            taglib.getListener().add(listener);
            return taglib;
        }

        TldTaglib tldTaglib;
        try {
            tldTaglib = (TldTaglib) JaxbJavaee.unmarshalTaglib(TldTaglib.class, IO.read(url));
        } catch (SAXException e) {
            throw new OpenEJBException("Cannot parse the JSP tag library definition file: " + url.toExternalForm(), e);
        } catch (JAXBException e) {
            throw new OpenEJBException("Cannot unmarshall the JSP tag library definition file: " + url.toExternalForm(), e);
        } catch (IOException e) {
            throw new OpenEJBException("Cannot read the JSP tag library definition file: " + url.toExternalForm(), e);
        } catch (Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the JSP tag library definition file: " + url.toExternalForm(), e);
        }
        return tldTaglib;
    }

    public static FacesConfig readFacesConfig(URL url) throws OpenEJBException {
        FacesConfig facesConfig;
        try {
     		facesConfig = (FacesConfig) JaxbJavaee
    		.unmarshalJavaee(FacesConfig.class, IO.read(url));
        } catch (SAXException e) {
            throw new OpenEJBException("Cannot parse the faces configuration file: " + url.toExternalForm(), e);
        } catch (JAXBException e) {
            throw new OpenEJBException("Cannot unmarshall the faces configuration file: " + url.toExternalForm(), e);
        } catch (IOException e) {
            throw new OpenEJBException("Cannot read the faces configuration file: " + url.toExternalForm(), e);
        } catch (Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the faces configuration file: " + url.toExternalForm(), e);
        }
        return facesConfig;
    }
    private Source getSource(Object o) {
        if (o instanceof URL) {
            return new UrlSource((URL) o);
        }

        if (o instanceof String) {
            return new StringSource((String) o);
        }

        return null;
    }

    public static abstract class Source {
        abstract InputStream get() throws IOException;
    }

    public static class UrlSource extends Source {
        private final URL url;

        public UrlSource(URL url) {
            this.url = url;
        }

        InputStream get() throws IOException {
            return IO.read(url);
        }
    }

    public static class StringSource extends Source {
        private byte[] bytes;

        public StringSource(String content) {
            bytes = content.getBytes();
        }

        InputStream get() throws IOException {
            return new ByteArrayInputStream(bytes);
        }
    }

    private static class LengthInputStream extends FilterInputStream {
        private long length;

        public LengthInputStream(InputStream in) throws IOException {
            super(in);
        }

        @Override
        public int read() throws IOException {
            final int i = super.read();
            if (i > 0) length++;
            return i;
        }

        @Override
        public int read(byte[] b) throws IOException {
            final int i = super.read(b);
            if (i > 0) length += i;
            return i;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            final int i = super.read(b, off, len);
            if (i > 0) length += i;
            return i;
        }

        public long getLength() {
            return length;
        }
    }
}
