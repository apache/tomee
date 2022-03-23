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
import org.apache.openejb.cdi.CompositeBeans;
import org.apache.openejb.config.sys.JSonConfigReader;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.Connector10;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.FacesConfig;
import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.JavaWsdlMapping;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.Keyable;
import org.apache.openejb.jee.Listener;
import org.apache.openejb.jee.TldTaglib;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;
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
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.sxc.ApplicationClientXml;
import org.apache.openejb.sxc.EjbJarXml;
import org.apache.openejb.sxc.FacesConfigXml;
import org.apache.openejb.sxc.HandlerChainsXml;
import org.apache.openejb.sxc.TldTaglibXml;
import org.apache.openejb.sxc.WebXml;
import org.apache.openejb.sxc.WebservicesXml;
import org.apache.openejb.util.LengthInputStream;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Saxs;
import org.apache.openejb.util.URLs;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ReadDescriptors implements DynamicDeployer {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, ReadDescriptors.class);

    private static final boolean ROOT_URL_FROM_WEBINF = SystemInstance.get().getOptions().get("openejb.jpa.root-url-from-webinf", false);

    public static final TldTaglib SKIP_TAGLIB = new TldTaglib();

    @SuppressWarnings({"unchecked"})
    public AppModule deploy(final AppModule appModule) throws OpenEJBException {
        for (final EjbModule ejbModule : appModule.getEjbModules()) {

            if (ejbModule.getEjbJar() == null) {
                readEjbJar(ejbModule, appModule);
            }

            if (ejbModule.getOpenejbJar() == null) {
                readOpenejbJar(ejbModule);
            }

            if (ejbModule.getBeans() == null) {
                readBeans(ejbModule);
            }

            readValidationConfigType(ejbModule);
            readCmpOrm(ejbModule);
            readResourcesXml(ejbModule);
        }

        for (final ClientModule clientModule : appModule.getClientModules()) {
            readAppClient(clientModule, appModule);
            readValidationConfigType(clientModule);
            readResourcesXml(clientModule);
        }

        for (final ConnectorModule connectorModule : appModule.getConnectorModules()) {
            readConnector(connectorModule, appModule);
            readValidationConfigType(connectorModule);
            readResourcesXml(connectorModule);
        }

        for (final WebModule webModule : appModule.getWebModules()) {
            readWebApp(webModule, appModule);
            readValidationConfigType(webModule);
            readResourcesXml(webModule);
        }

        final List<Object> persistenceUrls = (List<Object>) appModule.getAltDDs().get("persistence.xml");
        if (persistenceUrls != null) {
            for (final Object persistenceUrl : persistenceUrls) {
                final boolean url = persistenceUrl instanceof URL;
                final Source source = getSource(persistenceUrl);

                final String moduleName;
                final String path;
                final String rootUrl;
                if (url) {
                    final URL pUrl = (URL) persistenceUrl;
                    File file = URLs.toFile(pUrl);
                    path = file.getAbsolutePath();

                    if (file.getName().endsWith("persistence.xml")) {
                        final File parentFile = file.getParentFile();
                        final String parent = parentFile.getName();
                        if (parent.equalsIgnoreCase("WEB-INF") || parent.equalsIgnoreCase("META-INF")) {
                            file = parentFile.getParentFile();
                        } else { // we don't really know so simply go back (users will often put persistence.xml in root resource folder with arquillian)
                            file = file.getParentFile();
                        }
                    }
                    moduleName = file.toURI().toString();

                    String tmpRootUrl = moduleName;

                    final String extForm = pUrl.toExternalForm();
                    if (extForm.contains("WEB-INF/classes/META-INF/")) {
                        if (!ROOT_URL_FROM_WEBINF) {
                            tmpRootUrl = extForm.substring(0, extForm.indexOf("/META-INF"));
                        } else {
                            tmpRootUrl = extForm.substring(0, extForm.indexOf("/classes/META-INF"));
                        }
                    }
                    if (tmpRootUrl.endsWith(".war")) {
                        tmpRootUrl = tmpRootUrl.substring(0, tmpRootUrl.length() - ".war".length());
                    }
                    rootUrl = tmpRootUrl;
                } else {
                    moduleName = "";
                    rootUrl = "";
                    path = null;
                }

                try {
                    final Persistence persistence = JaxbPersistenceFactory.getPersistence(Persistence.class, source.get());
                    final PersistenceModule persistenceModule = new PersistenceModule(appModule, rootUrl, persistence);
                    persistenceModule.getWatchedResources().add(moduleName);
                    if (url && "file".equals(((URL) persistenceUrl).getProtocol())) {
                        persistenceModule.getWatchedResources().add(path);
                    }
                    appModule.addPersistenceModule(persistenceModule);
                } catch (final Exception e1) {
                    DeploymentLoader.LOGGER.error("Unable to load Persistence Unit from EAR: " + appModule.getJarLocation() + ", module: " + moduleName + ". Exception: " + e1.getMessage(), e1);
                }
            }
        }

        final List<URL> persistenceFragmentUrls = (List<URL>) appModule.getAltDDs().get("persistence-fragment.xml");
        if (persistenceFragmentUrls != null) {
            for (final URL persistenceFragmentUrl : persistenceFragmentUrls) {
                try {
                    final PersistenceFragment persistenceFragment = JaxbPersistenceFactory.getPersistence(PersistenceFragment.class, persistenceFragmentUrl);
                    // merging
                    for (final PersistenceUnitFragment fragmentUnit : persistenceFragment.getPersistenceUnitFragment()) {
                        for (final PersistenceModule persistenceModule : appModule.getPersistenceModules()) {
                            final Persistence persistence = persistenceModule.getPersistence();
                            for (final PersistenceUnit unit : persistence.getPersistenceUnit()) {
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

                                for (final String clazz : fragmentUnit.getClazz()) {
                                    if (!unit.getClazz().contains(clazz)) {
                                        logger.info("Adding class " + clazz + " to persistence unit " + fragmentUnit.getName());
                                        unit.getClazz().add(clazz);
                                    }
                                }
                                for (final String mappingFile : fragmentUnit.getMappingFile()) {
                                    if (!unit.getMappingFile().contains(mappingFile)) {
                                        logger.info("Adding mapping file " + mappingFile + " to persistence unit " + fragmentUnit.getName());
                                        unit.getMappingFile().add(mappingFile);
                                    }
                                }
                                for (final String jarFile : fragmentUnit.getJarFile()) {
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
                } catch (final Exception e1) {
                    DeploymentLoader.LOGGER.error("Unable to load Persistence Unit Fragment from EAR: " + appModule.getJarLocation() + ", fragment: " + persistenceFragmentUrl.toString() + ". Exception: " + e1.getMessage(), e1);
                }
            }
        }

        return appModule;
    }

    public static void readResourcesXml(final Module module) {
        { // xml
            final Source url = getSource(module.getAltDDs().get("resources.xml"));
            if (url != null) {
                try {
                    final Resources openejb = JaxbOpenejb.unmarshal(Resources.class, url.get());
                    module.initResources(check(openejb));
                } catch (final Exception e) {
                    logger.warning("can't read " + url.toString() + " to load resources for module " + module.toString(), e);
                }
            }
        }
        { // json
            final Source url = getSource(module.getAltDDs().get("resources.json"));
            if (url != null) {
                try {
                    final Resources openejb = JSonConfigReader.read(Resources.class, url.get());
                    module.initResources(check(openejb));
                } catch (final Exception e) {
                    logger.warning("can't read " + url.toString() + " to load resources for module " + module.toString(), e);
                }
            }
        }
    }

    public static Resources check(final Resources resources) {
        final List<Resource> resourceList = resources.getResource();
        for (final Resource resource : resourceList) {
            if (resource.getClassName() != null) {
                try {
                    ParentClassLoaderFinder.Helper.get().loadClass(resource.getClassName());
                    continue;
                } catch (Exception e) {
                    // ignore if this class is not found in the classloader
                }

                // if the resource class cannot be loaded,
                // set the lazy property to true
                // and the app classloader property to true

                final Boolean lazySpecified = Boolean.valueOf(resource.getProperties().getProperty("Lazy", "false"));

                resource.getProperties().setProperty("Lazy", "true");
                resource.getProperties().setProperty("UseAppClassLoader", "true");

                if (!lazySpecified) {
                    resource.getProperties().setProperty("InitializeAfterDeployment", "true");
                }
            }
        }

        return resources;
    }

    private void readValidationConfigType(final Module module) throws OpenEJBException {
        if (module.getValidationConfig() != null) {
            return;
        }

        final Source value = getSource(module.getAltDDs().get("validation.xml"));
        if (value != null) {
            try {
                final ValidationConfigType validationConfigType = JaxbOpenejb.unmarshal(
                        ValidationConfigType.class, value.get(), false,
                        "http://xmlns.jcp.org/xml/ns/validation/configuration",
                        "http://jboss.org/xml/ns/javax/validation/configuration");
                module.setValidationConfig(validationConfigType);
            } catch (final Exception e) {
                logger.warning("can't read validation.xml to construct a validation factory, it will be ignored");
            }
        }
    }

    private void readOpenejbJar(final EjbModule ejbModule) throws OpenEJBException {
        final Source source = getSource(ejbModule.getAltDDs().get("openejb-jar.xml"));

        if (source != null) {
            try {
                // Attempt to parse it first as a v3 descriptor
                final OpenejbJar openejbJar = JaxbOpenejbJar3.unmarshal(OpenejbJar.class, source.get()).postRead();
                ejbModule.setOpenejbJar(openejbJar);
            } catch (final Exception v3ParsingException) {
                // Attempt to parse it second as a v2 descriptor
                final OpenejbJar openejbJar = new OpenejbJar();
                ejbModule.setOpenejbJar(openejbJar);

                try {
                    final JAXBElement element = (JAXBElement) JaxbOpenejbJar2.unmarshal(OpenejbJarType.class, source.get());
                    final OpenejbJarType o2 = (OpenejbJarType) element.getValue();
                    ejbModule.getAltDDs().put("openejb-jar.xml", o2);

                    final GeronimoEjbJarType g2 = OpenEjb2Conversion.convertToGeronimoOpenejbXml(o2);

                    ejbModule.getAltDDs().put("geronimo-openejb.xml", g2);
                } catch (final Exception v2ParsingException) {
                    // Now we have to determine which error to throw; the v3 file exception or the fallback v2 file exception.
                    final Exception[] realIssue = {v3ParsingException};

                    try {
                        final SAXParserFactory factory = Saxs.namespaceAwareFactory();
                        final SAXParser parser = factory.newSAXParser();
                        parser.parse(source.get(), new DefaultHandler() {
                            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
                                if (localName.equals("environment")) {
                                    realIssue[0] = v2ParsingException;
                                    throw new SAXException("Throw exception to stop parsing");
                                }
                                if (uri == null) {
                                    return;
                                }
                                if (uri.contains("openejb-jar-2.") || uri.contains("geronimo.apache.org/xml/ns")) {
                                    realIssue[0] = v2ParsingException;
                                    throw new SAXException("Throw exception to stop parsing");
                                }
                            }
                        });
                    } catch (final Exception dontCare) {
                        // no-op
                    }

                    String filePath = "<error: could not be written>";
                    try {
                        File tempFile;
                        try {
                            tempFile = File.createTempFile("openejb-jar-", ".xml");
                        } catch (final Throwable e) {
                            final File tmp = new File("tmp");
                            if (!tmp.exists() && !tmp.mkdirs()) {
                                throw new IOException("Failed to create local tmp directory: " + tmp.getAbsolutePath());
                            }

                            tempFile = File.createTempFile("openejb-jar-", ".xml", tmp);
                        }
                        try {
                            IO.copy(source.get(), tempFile);
                        } catch (final IOException e) {
                            // no-op
                        }
                        filePath = tempFile.getAbsolutePath();
                    } catch (final IOException e) {
                        // no-op
                    }

                    final Exception e = realIssue[0];
                    if (e instanceof SAXException) {
                        throw new OpenEJBException("Cannot parse the openejb-jar.xml. Xml content written to: " + filePath, e);
                    } else if (e instanceof JAXBException) {
                        throw new OpenEJBException("Cannot unmarshall the openejb-jar.xml. Xml content written to: " + filePath, e);
                    } else if (e instanceof IOException) {
                        throw new OpenEJBException("Cannot read the openejb-jar.xml.", e);
                    } else {
                        throw new OpenEJBException("Encountered unknown error parsing the openejb-jar.xml.", e);
                    }
                }
            }
        }

        final Source source1 = getSource(ejbModule.getAltDDs().get("geronimo-openejb.xml"));
        if (source1 != null) {
            try {
                GeronimoEjbJarType geronimoEjbJarType = null;
                final Object o = JaxbOpenejbJar2.unmarshal(GeronimoEjbJarType.class, source1.get());
                if (o instanceof GeronimoEjbJarType) {
                    geronimoEjbJarType = (GeronimoEjbJarType) o;
                } else if (o instanceof JAXBElement) {
                    final JAXBElement element = (JAXBElement) o;
                    geronimoEjbJarType = (GeronimoEjbJarType) element.getValue();
                }
                if (geronimoEjbJarType != null) {
                    final Object nested = geronimoEjbJarType.getOpenejbJar();
                    if (nested != null && nested instanceof OpenejbJar) {
                        final OpenejbJar existingOpenejbJar = ejbModule.getOpenejbJar();
                        if (existingOpenejbJar == null || existingOpenejbJar.getEjbDeploymentCount() <= 0) {
                            final OpenejbJar openejbJar = (OpenejbJar) nested;
                            ejbModule.getAltDDs().put("openejb-jar.xml", openejbJar);
                            ejbModule.setOpenejbJar(openejbJar);
                        }
                    }
                    ejbModule.getAltDDs().put("geronimo-openejb.xml", geronimoEjbJarType);
                }
            } catch (final Exception e) {
                throw new OpenEJBException("Failed parsing geronimo-openejb.xml", e);
            }
        }

    }

    private void readAppClient(final ClientModule clientModule, final AppModule appModule) throws OpenEJBException {
        if (clientModule.getApplicationClient() != null) {
            return;
        }

        final Object data = clientModule.getAltDDs().get("application-client.xml");
        if (data instanceof ApplicationClient) {
            clientModule.setApplicationClient((ApplicationClient) data);
        } else if (data instanceof URL) {
            final URL url = (URL) data;
            final ApplicationClient applicationClient = readApplicationClient(url);
            clientModule.setApplicationClient(applicationClient);
        } else {
            if (!clientModule.isEjbModuleGenerated()) {
                DeploymentLoader.LOGGER.debug("No application-client.xml found assuming annotations present: " + appModule.getJarLocation() + ", module: " + clientModule.getModuleId());
                clientModule.setApplicationClient(new ApplicationClient());
            }
        }
    }

    public void readEjbJar(final EjbModule ejbModule, final AppModule appModule) throws OpenEJBException {
        if (ejbModule.getEjbJar() != null) {
            return;
        }

        final Source data = getSource(ejbModule.getAltDDs().get("ejb-jar.xml"));
        if (data != null) {
            try {
                final EjbJar ejbJar = readEjbJar(data.get());
                ejbModule.setEjbJar(ejbJar);
            } catch (final IOException e) {
                throw new OpenEJBException(e);
            }
        } else {
            DeploymentLoader.LOGGER.debug("No ejb-jar.xml found assuming annotated beans present: " + appModule.getJarLocation() + ", module: " + ejbModule.getModuleId());
            ejbModule.setEjbJar(new EjbJar());
        }
    }

    private static void checkDuplicatedByBeansXml(final List<String> list, final List<String> duplicated) {
        for (String str : list) {
            if (list.indexOf(str) != list.lastIndexOf(str)) {
                duplicated.add(str);
            }
        }
    }

    public static void checkDuplicatedByBeansXml(final Beans beans, final Beans complete) {
        checkDuplicatedByBeansXml(beans.getAlternativeClasses(), complete.getDuplicatedAlternatives().getClasses());
        checkDuplicatedByBeansXml(beans.getAlternativeStereotypes(), complete.getDuplicatedAlternatives().getStereotypes());
        checkDuplicatedByBeansXml(beans.getDecorators(), complete.getDuplicatedDecorators());
        checkDuplicatedByBeansXml(beans.getInterceptors(), complete.getDuplicatedInterceptors());
    }

    private void readBeans(final EjbModule ejbModule) throws OpenEJBException {
        if (ejbModule.getBeans() != null) {
            return;
        }

        final Object raw = ejbModule.getAltDDs().get("beans.xml");
        final Source data = getSource(raw);
        if (data != null) {
            try {
                final Beans beans = readBeans(data.get());
                checkDuplicatedByBeansXml(beans, beans);
                if (UrlSource.class.isInstance(data)) {
                    beans.setUri(UrlSource.class.cast(data).getUrl().toExternalForm());
                } else {
                    beans.setUri("jar:file://" + ejbModule.getModuleId() + "!/META-INF/beans.xml");
                }
                ejbModule.setBeans(beans);
            } catch (final IOException e) {
                throw new OpenEJBException(e);
            }
        } else if (raw instanceof Beans) {
            ejbModule.setBeans((Beans) raw);
        } else if (List.class.isInstance(raw)) {
            final CompositeBeans compositeBeans = new CompositeBeans();
            final List list = List.class.cast(raw);
            if (!list.isEmpty()) {
                for (final Object o : list) {
                    try {
                        final UrlSource urlSource = UrlSource.class.cast(o);
                        mergeBeansXml(compositeBeans, readBeans(urlSource.get()), urlSource.getUrl());
                    } catch (final IOException e) {
                        throw new OpenEJBException(e);
                    }
                }
                ejbModule.setBeans(compositeBeans);
            }
        }
    }

    private static Beans mergeBeansXml(final CompositeBeans current, final Beans beans, final URL url) {
        current.mergeClasses(url, beans);
        current.getScan().getExclude().addAll(beans.getScan().getExclude());

        // check is done here since later we lost the data of the origin
        ReadDescriptors.checkDuplicatedByBeansXml(beans, current);

        String beanDiscoveryMode = beans.getBeanDiscoveryMode();
        if (beanDiscoveryMode == null) {
            beanDiscoveryMode = "ALL";
        }
        else if ("ALL".equalsIgnoreCase(beanDiscoveryMode) && beans.isTrim()) {
            beanDiscoveryMode = "TRIM";
        }

        current.getDiscoveryByUrl().put(url, beanDiscoveryMode);
        return current;
    }

    // package scoped for testing
    void readCmpOrm(final EjbModule ejbModule) throws OpenEJBException {
        final Object data = ejbModule.getAltDDs().get("openejb-cmp-orm.xml");
        if (data != null && !(data instanceof EntityMappings)) {
            if (data instanceof URL) {
                final URL url = (URL) data;
                try {
                    final EntityMappings entitymappings = (EntityMappings) JaxbJavaee.unmarshal(EntityMappings.class, IO.read(url));
                    ejbModule.getAltDDs().put("openejb-cmp-orm.xml", entitymappings);
                } catch (final SAXException e) {
                    throw new OpenEJBException("Cannot parse the openejb-cmp-orm.xml file: " + url.toExternalForm(), e);
                } catch (final JAXBException e) {
                    throw new OpenEJBException("Cannot unmarshall the openejb-cmp-orm.xml file: " + url.toExternalForm(), e);
                } catch (final IOException e) {
                    throw new OpenEJBException("Cannot read the openejb-cmp-orm.xml file: " + url.toExternalForm(), e);
                } catch (final Exception e) {
                    throw new OpenEJBException("Encountered unknown error parsing the openejb-cmp-orm.xml file: " + url.toExternalForm(), e);
                }
            }
        }
    }

    private void readConnector(final ConnectorModule connectorModule, final AppModule appModule) throws OpenEJBException {
        if (connectorModule.getConnector() != null) {
            return;
        }

        final Object data = connectorModule.getAltDDs().get("ra.xml");
        if (data instanceof Connector) {
            connectorModule.setConnector((Connector) data);
        } else if (data instanceof URL) {
            final URL url = (URL) data;
            final Connector connector = readConnector(url);
            connectorModule.setConnector(connector);
        } else {
            DeploymentLoader.LOGGER.debug("No ra.xml found assuming annotated beans present: " + appModule.getJarLocation() + ", module: " + connectorModule.getModuleId());
            connectorModule.setConnector(new Connector());
        }
    }

    private void readWebApp(final WebModule webModule, final AppModule appModule) throws OpenEJBException {
        if (webModule.getWebApp() != null) {
            mergeWebFragments(webModule);
            return;
        }

        final Object data = webModule.getAltDDs().get("web.xml");
        if (data instanceof WebApp) {
            webModule.setWebApp((WebApp) data);
        } else if (data instanceof URL) {
            final URL url = (URL) data;
            final WebApp webApp = readWebApp(url);
            webModule.setWebApp(webApp);
        } else {
            DeploymentLoader.LOGGER.debug("No web.xml found assuming annotated beans present: " + appModule.getJarLocation() + ", module: " + webModule.getModuleId());
            webModule.setWebApp(new WebApp());
        }

        mergeWebFragments(webModule);
    }

    private void mergeWebFragments(final WebModule webModule) {
        // web-fragment.xml, to get jndi entries to merge, other stuff is done by tomcat ATM
        final Collection<URL> urls = Collection.class.cast(webModule.getAltDDs().get("web-fragment.xml"));
        if (urls != null) {
            for (final URL rawUrl : urls) {
                if (rawUrl != null) {
                    final Source url = getSource(rawUrl);
                    try {
                        final WebFragment webFragment = WebFragment.class.cast(JaxbJavaee.unmarshal(WebFragment.class, url.get(), false));

                        // in tomcat if the env entry is already don't override it
                        mergeOnlyMissingEntries(webModule.getWebApp().getPersistenceContextRefMap(), webFragment.getPersistenceContextRef());
                        mergeOnlyMissingEntries(webModule.getWebApp().getPersistenceUnitRefMap(), webFragment.getPersistenceUnitRef());
                        mergeOnlyMissingEntries(webModule.getWebApp().getMessageDestinationRefMap(), webFragment.getMessageDestinationRef());
                        mergeOnlyMissingEntries(webModule.getWebApp().getDataSourceMap(), webFragment.getDataSource());
                        mergeOnlyMissingEntries(webModule.getWebApp().getJMSConnectionFactoriesMap(), webFragment.getJMSConnectionFactories());
                        mergeOnlyMissingEntries(webModule.getWebApp().getEjbLocalRefMap(), webFragment.getEjbLocalRef());
                        mergeOnlyMissingEntries(webModule.getWebApp().getEjbRefMap(), webFragment.getEjbRef());
                        mergeOnlyMissingEntries(webModule.getWebApp().getServiceRefMap(), webFragment.getServiceRef());
                        mergeOnlyMissingEntries(webModule.getWebApp().getEnvEntryMap(), webFragment.getEnvEntry());
                        mergeOnlyMissingEntries(webModule.getWebApp().getResourceEnvRefMap(), webFragment.getResourceEnvRef());
                        mergeOnlyMissingEntries(webModule.getWebApp().getResourceRefMap(), webFragment.getResourceRef());
                    } catch (final Exception e) {
                        logger.warning("can't read " + url.toString(), e);
                    }
                }
            }
        }
    }

    private static <A extends Keyable<String>> void mergeOnlyMissingEntries(final Map<String, A> existing, final Collection<A> news) {
        for (final A entry : news) {
            final String key = entry.getKey();
            if (!existing.containsKey(key)) {
                existing.put(key, entry);
            }
        }
    }

    public static ApplicationClient readApplicationClient(final URL url) throws OpenEJBException {
        final ApplicationClient applicationClient;
        try {
            applicationClient = ApplicationClientXml.unmarshal(url);
        } catch (final SAXException e) {
            throw new OpenEJBException("Cannot parse the application-client.xml file: " + url.toExternalForm(), e);
        } catch (final JAXBException e) {
            throw new OpenEJBException("Cannot unmarshall the application-client.xml file: " + url.toExternalForm(), e);
        } catch (final IOException e) {
            throw new OpenEJBException("Cannot read the application-client.xml file: " + url.toExternalForm(), e);
        } catch (final Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the application-client.xml file: " + url.toExternalForm(), e);
        }
        return applicationClient;
    }

    public static EjbJar readEjbJar(final InputStream is) throws OpenEJBException {
        try {
            final String content = IO.slurp(is);
            if (isEmptyEjbJar(new ByteArrayInputStream(content.getBytes()))) {
                final String id = getId(new ByteArrayInputStream(content.getBytes()));
                return new EjbJar(id);
            }
            return EjbJarXml.unmarshal(new ByteArrayInputStream(content.getBytes()));
        } catch (final SAXException e) {
            throw new OpenEJBException("Cannot parse the ejb-jar.xml", e); // file: " + url.toExternalForm(), e);
        } catch (final IOException e) {
            throw new OpenEJBException("Cannot read the ejb-jar.xml", e); // file: " + url.toExternalForm(), e);
        } catch (final Exception e) {
            throw new OpenEJBException("Encountered error parsing the ejb-jar.xml", e); // file: " + url.toExternalForm(), e);
        }
    }

    public static Beans readBeans(final InputStream inputStream) throws OpenEJBException {
        try {
            final String content = IO.slurp(inputStream).trim();
            if (content.length() == 0) { // otherwise we want to read <beans /> attributes
                final Beans beans = new Beans();
                beans.setBeanDiscoveryMode("ALL"); // backward compatibility
                return beans;
            }
            return (Beans) JaxbJavaee.unmarshalJavaee(Beans.class, new ByteArrayInputStream(content.getBytes()));
        } catch (final SAXException e) {
            throw new OpenEJBException("Cannot parse the beans.xml", e);// file: " + url.toExternalForm(), e);
        } catch (final JAXBException e) {
            e.printStackTrace();
            throw new OpenEJBException("Cannot unmarshall the beans.xml", e);// file: " + url.toExternalForm(), e);
        } catch (final IOException e) {
            throw new OpenEJBException("Cannot read the beans.xml", e);// file: " + url.toExternalForm(), e);
        } catch (final Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the beans.xml", e);// file: " + url.toExternalForm(), e);
        }
    }

    private static boolean isEmptyEjbJar(final InputStream is) throws IOException, ParserConfigurationException, SAXException {
        return isEmpty(is, "ejb-jar");
    }

    private static boolean isEmpty(final InputStream is, final String rootElement) throws IOException, ParserConfigurationException, SAXException {
        final LengthInputStream in = new LengthInputStream(is);
        final InputSource inputSource = new InputSource(in);

        final SAXParser parser;

        final Thread thread = Thread.currentThread();
        final ClassLoader original = thread.getContextClassLoader();
        thread.setContextClassLoader(Saxs.class.getClassLoader());
        try {
            parser = Saxs.namespaceAwareFactory().newSAXParser();
        } finally {
            thread.setContextClassLoader(original);
        }

        try {
            parser.parse(inputSource, new DefaultHandler() {
                public void startElement(final String uri, final String localName, final String qName, final Attributes att) throws SAXException {
                    if (!localName.equals(rootElement)) {
                        throw new SAXException(localName);
                    }
                }

                public InputSource resolveEntity(final String publicId, final String systemId) throws IOException, SAXException {
                    return new InputSource(new ByteArrayInputStream(new byte[0]));
                }
            });
            return true;
        } catch (final SAXException e) {
            return in.getLength() == 0;
        }
    }

    private static String getId(final InputStream is) {
        final String[] id = {null};

        try {
            final LengthInputStream in = new LengthInputStream(is);
            final InputSource inputSource = new InputSource(in);

            final SAXParser parser = Saxs.namespaceAwareFactory().newSAXParser();

            parser.parse(inputSource, new DefaultHandler() {
                public void startElement(final String uri, final String localName, final String qName, final Attributes att) throws SAXException {
                    id[0] = att.getValue("id");
                }

                public InputSource resolveEntity(final String publicId, final String systemId) throws IOException, SAXException {
                    return new InputSource(new ByteArrayInputStream(new byte[0]));
                }
            });
        } catch (final Exception e) {
            // no-op
        }

        return id[0];
    }

    public static Webservices readWebservices(final URL url) throws OpenEJBException {
        try {
            return WebservicesXml.unmarshal(url);
        } catch (final SAXException e) {
            throw new OpenEJBException("Cannot parse the webservices.xml file: " + url.toExternalForm(), e);
        } catch (final JAXBException e) {
            throw new OpenEJBException("Cannot unmarshall the webservices.xml file: " + url.toExternalForm(), e);
        } catch (final IOException e) {
            throw new OpenEJBException("Cannot read the webservices.xml file: " + url.toExternalForm(), e);
        } catch (final Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the webservices.xml file: " + url.toExternalForm(), e);
        }
    }

    public static HandlerChains readHandlerChains(final URL url) throws OpenEJBException {
        try {
            return HandlerChainsXml.unmarshal(url);
        } catch (final SAXException e) {
            throw new OpenEJBException("Cannot parse the webservices.xml file: " + url.toExternalForm(), e);
        } catch (final JAXBException e) {
            throw new OpenEJBException("Cannot unmarshall the webservices.xml file: " + url.toExternalForm(), e);
        } catch (final IOException e) {
            throw new OpenEJBException("Cannot read the webservices.xml file: " + url.toExternalForm(), e);
        } catch (final Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the webservices.xml file: " + url.toExternalForm(), e);
        }
    }

    public static JavaWsdlMapping readJaxrpcMapping(final URL url) throws OpenEJBException {
        final JavaWsdlMapping wsdlMapping;
        try {
            wsdlMapping = (JavaWsdlMapping) JaxbJavaee.unmarshalJavaee(JavaWsdlMapping.class, IO.read(url));
        } catch (final SAXException e) {
            throw new OpenEJBException("Cannot parse the JaxRPC mapping file: " + url.toExternalForm(), e);
        } catch (final JAXBException e) {
            throw new OpenEJBException("Cannot unmarshall the JaxRPC mapping file: " + url.toExternalForm(), e);
        } catch (final IOException e) {
            throw new OpenEJBException("Cannot read the JaxRPC mapping file: " + url.toExternalForm(), e);
        } catch (final Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the JaxRPC mapping file: " + url.toExternalForm(), e);
        }
        return wsdlMapping;
    }

    public static Connector readConnector(final URL url) throws OpenEJBException {
        Connector connector;
        try {
            connector = (Connector) JaxbJavaee.unmarshalJavaee(Connector.class, IO.read(url));
        } catch (final JAXBException e) {
            try {
                final Connector10 connector10 = (Connector10) JaxbJavaee.unmarshalJavaee(Connector10.class, IO.read(url));
                connector = Connector.newConnector(connector10);
            } catch (final ParserConfigurationException | SAXException e1) {
                throw new OpenEJBException("Cannot parse the ra.xml file: " + url.toExternalForm(), e);
            } catch (final JAXBException e1) {
                throw new OpenEJBException("Cannot unmarshall the ra.xml file: " + url.toExternalForm(), e);
            } catch (final IOException e1) {
                throw new OpenEJBException("Cannot read the ra.xml file: " + url.toExternalForm(), e);
            }
        } catch (final SAXException e) {
            throw new OpenEJBException("Cannot parse the ra.xml file: " + url.toExternalForm(), e);
        } catch (final IOException e) {
            throw new OpenEJBException("Cannot read the ra.xml file: " + url.toExternalForm(), e);
        } catch (final Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the ra.xml file: " + url.toExternalForm(), e);
        }
        return connector;
    }

    public static WebApp readWebApp(final URL url) throws OpenEJBException {
        final WebApp webApp;
        try {
            // webApp = (WebApp) JaxbJavaee.unmarshalJavaee(WebApp.class, IO.read(url));
            // don't use the SXC version with the accessors as it's not up to date
            webApp = WebXml.unmarshal(url);
        } catch (final SAXException e) {
            throw new OpenEJBException("Cannot parse the web.xml file: " + url.toExternalForm(), e);
        } catch (final JAXBException e) {
            throw new OpenEJBException("Cannot unmarshall the web.xml file: " + url.toExternalForm(), e);
        } catch (final IOException e) {
            throw new OpenEJBException("Cannot read the web.xml file: " + url.toExternalForm(), e);
        } catch (final Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the web.xml file: " + url.toExternalForm(), e);
        }
        return webApp;
    }

    public static TldTaglib readTldTaglib(final URL url) throws OpenEJBException {
        // TOMEE-164 Optimization on reading built-in tld files
        if (url.getPath().contains("jstl-1.2.jar") || ((url.getPath().contains("taglibs-standard-") || url.getPath().contains("taglibs-shade-") && url.getPath().contains(".jar!")))) {
            return SKIP_TAGLIB;
        }
        if (url.getPath().contains("myfaces-impl")) { // we should return SKIP_TAGLIB too
            final TldTaglib taglib = new TldTaglib();
            final Listener listener = new Listener();
            listener.setListenerClass("org.apache.myfaces.webapp.StartupServletContextListener");
            taglib.getListener().add(listener);
            return taglib;
        }

        try {
            return TldTaglibXml.unmarshal(url);
        } catch (final SAXException e) {
            final String message = "Cannot parse the JSP tag library definition file: " + url.toExternalForm();
            logger.warning(message);
            logger.debug(message, e);
        } catch (final JAXBException e) {
            final String message = "Cannot unmarshall the JSP tag library definition file: " + url.toExternalForm();
            logger.warning(message);
            logger.debug(message, e);
        } catch (final IOException e) {
            final String message = "Cannot read the JSP tag library definition file: " + url.toExternalForm();
            logger.warning(message);
            logger.debug(message, e);
        } catch (final Exception e) {
            final String message = "Encountered unknown error parsing the JSP tag library definition file: " + url.toExternalForm();
            logger.warning(message);
            logger.debug(message, e);
        }
        return SKIP_TAGLIB;
    }

    public static FacesConfig readFacesConfig(final URL url) throws OpenEJBException {
        try {
            final Source src = getSource(url);
            if (src == null) {
                return new FacesConfig();
            }

            final String content = IO.slurp(src.get());
            if (isEmpty(new ByteArrayInputStream(content.getBytes()), "faces-config")) {
                return new FacesConfig();
            }
            return FacesConfigXml.unmarshal(new ByteArrayInputStream(content.getBytes()));
        } catch (final SAXException e) {
            throw new OpenEJBException("Cannot parse the faces configuration file: " + url.toExternalForm(), e);
        } catch (final JAXBException e) {
            throw new OpenEJBException("Cannot unmarshall the faces configuration file: " + url.toExternalForm(), e);
        } catch (final IOException e) {
            throw new OpenEJBException("Cannot read the faces configuration file: " + url.toExternalForm(), e);
        } catch (final Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the faces configuration file: " + url.toExternalForm(), e);
        }
    }

    private static Source getSource(final Object o) {
        if (o instanceof URL) {
            return new UrlSource((URL) o);
        }

        if (o instanceof Source) {
            return (Source) o;
        }

        if (o instanceof String) {
            return new StringSource((String) o);
        }

        return null;
    }

    public interface Source {
        InputStream get() throws IOException;
    }

    public static class UrlSource implements Source {
        private final URL url;

        public UrlSource(final URL url) {
            this.url = url;
        }

        @Override
        public InputStream get() throws IOException {
            return IO.read(url);
        }

        public URL getUrl() {
            return url;
        }

        @Override
        public String toString() {
            return "UrlSource{url=" + url + '}';
        }
    }

    public static class StringSource implements Source {
        private final byte[] bytes;
        private final String toString;

        public StringSource(final String content) {
            toString = content;
            bytes = content.getBytes();
        }

        @Override
        public InputStream get() throws IOException {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public String toString() {
            return "StringSource{content=" + toString + '}';
        }
    }
}
