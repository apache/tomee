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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.sys.ServicesJar;
import org.apache.openejb.config.sys.ServiceProvider;
import org.apache.openejb.config.sys.ListAdapter;
import org.apache.openejb.config.sys.PropertiesAdapter;
import org.apache.openejb.core.webservices.WsdlResolver;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.JavaWsdlMapping;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.TldTaglib;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.Webservices;
import org.apache.openejb.jee.jpa.unit.JaxbPersistenceFactory;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.oejb2.EnterpriseBean;
import org.apache.openejb.jee.oejb2.GeronimoEjbJarType;
import org.apache.openejb.jee.oejb2.JaxbOpenejbJar2;
import org.apache.openejb.jee.oejb2.OpenejbJarType;
import org.apache.openejb.jee.oejb2.RpcBean;
import org.apache.openejb.jee.oejb2.SessionBeanType;
import org.apache.openejb.jee.oejb2.TssLinkType;
import org.apache.openejb.jee.oejb2.WebServiceBindingType;
import org.apache.openejb.jee.oejb3.JaxbOpenejbJar3;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class ReadDescriptors implements DynamicDeployer {
    @SuppressWarnings({"unchecked"})
    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        for (EjbModule ejbModule : appModule.getEjbModules()) {

            if (ejbModule.getEjbJar() == null) {
                readEjbJar(ejbModule, appModule);
            }

            if (ejbModule.getOpenejbJar() == null) {
                readOpenejbJar(ejbModule);
            }
        }

        for (ClientModule clientModule : appModule.getClientModules()) {
            readAppClient(clientModule, appModule);
        }

        for (ConnectorModule connectorModule : appModule.getResourceModules()) {
            readConnector(connectorModule, appModule);
        }

        for (WebModule webModule : appModule.getWebModules()) {
            readWebApp(webModule, appModule);
        }

        List<URL> persistenceUrls = (List<URL>) appModule.getAltDDs().get("persistence.xml");
        if (persistenceUrls != null) {
            for (URL persistenceUrl : persistenceUrls) {
                String moduleName = persistenceUrl.toExternalForm().replaceFirst("!/?META-INF/persistence.xml$", "");
                moduleName = moduleName.replaceFirst("/?META-INF/persistence.xml$", "/");
                if (moduleName.startsWith("jar:")) moduleName = moduleName.substring("jar:".length());
                if (moduleName.startsWith("file:")) moduleName = moduleName.substring("file:".length());
//                if (moduleName1.endsWith("/")) moduleName1 = moduleName1.substring(0, moduleName1.length() - 1);
                try {
                    Persistence persistence = JaxbPersistenceFactory.getPersistence(persistenceUrl);
                    PersistenceModule persistenceModule = new PersistenceModule(moduleName, persistence);
                    persistenceModule.getWatchedResources().add(moduleName);
                    if ("file".equals(persistenceUrl.getProtocol())) {
                        persistenceModule.getWatchedResources().add(persistenceUrl.getPath());
                    }
                    appModule.getPersistenceModules().add(persistenceModule);
                } catch (Exception e1) {
                    DeploymentLoader.logger.error("Unable to load Persistence Unit from EAR: " + appModule.getJarLocation() + ", module: " + moduleName + ". Exception: " + e1.getMessage(), e1);
                }
            }
        }

        return appModule;

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

                    GeronimoEjbJarType g2 = new GeronimoEjbJarType();

                    g2.setEnvironment(o2.getEnvironment());
                    g2.setSecurity(o2.getSecurity());
                    g2.getService().addAll(o2.getService());
                    g2.getMessageDestination().addAll(o2.getMessageDestination());
                    g2.getPersistence().addAll(o2.getPersistence());

                    for (EnterpriseBean bean : o2.getEnterpriseBeans()) {
                        g2.getAbstractNamingEntry().addAll(bean.getAbstractNamingEntry());
                        g2.getPersistenceContextRef().addAll(bean.getPersistenceContextRef());
                        g2.getPersistenceUnitRef().addAll(bean.getPersistenceUnitRef());
                        g2.getEjbLocalRef().addAll(bean.getEjbLocalRef());
                        g2.getEjbRef().addAll(bean.getEjbRef());
                        g2.getResourceEnvRef().addAll(bean.getResourceEnvRef());
                        g2.getResourceRef().addAll(bean.getResourceRef());
                        g2.getServiceRef().addAll(bean.getServiceRef());

                        if (bean instanceof RpcBean) {
                            RpcBean rpcBean = (RpcBean) bean;
                            if (rpcBean.getTssLink() != null){
                                g2.getTssLink().add(new TssLinkType(rpcBean.getEjbName(), rpcBean.getTssLink(), rpcBean.getJndiName()));
                            }
                        }

                        if (bean instanceof SessionBeanType) {
                            SessionBeanType sb = (SessionBeanType) bean;
                            WebServiceBindingType b = new WebServiceBindingType();
                            b.setEjbName(sb.getEjbName());
                            b.setWebServiceAddress(sb.getWebServiceAddress());
                            b.setWebServiceVirtualHost(sb.getWebServiceVirtualHost());
                            b.setWebServiceSecurity(sb.getWebServiceSecurity());
                            if (b.containsData()){
                                g2.getWebServiceBinding().add(b);
                            }
                        }
                    }

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
                            FileOutputStream out = new FileOutputStream(tempFile);
                            InputStream in = source.get();
                            int b = in.read();
                            while (b != -1){
                                out.write(b);
                                b = in.read();
                            }
                            out.close();
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
            DeploymentLoader.logger.warning("No application-client.xml found assuming annotations present: " + appModule.getJarLocation() + ", module: " + clientModule.getModuleId());
            clientModule.setApplicationClient(new ApplicationClient());
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
            applicationClient = (ApplicationClient) JaxbJavaee.unmarshal(ApplicationClient.class, url.openStream());
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
            return (EjbJar) JaxbJavaee.unmarshal(EjbJar.class, url.openStream());
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

    private static boolean isEmptyEjbJar(URL url) throws IOException, ParserConfigurationException, SAXException {
        InputSource inputSource = new InputSource(url.openStream());

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        try {
            parser.parse(inputSource, new DefaultHandler(){
                public void startElement(String uri, String localName, String qName, Attributes att) throws SAXException {
                    if (!localName.equals("ejb-jar")) throw new SAXException(localName);
                }
            });
            return true;
        } catch (SAXException e) {
            return false;
        }
    }

    public static Webservices readWebservices(URL url) throws OpenEJBException {
        Webservices webservices = null;
        try {
            webservices = (Webservices) JaxbJavaee.unmarshal(Webservices.class, url.openStream());
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
        HandlerChains handlerChains = null;
        try {
            handlerChains = (HandlerChains) JaxbJavaee.unmarshal(HandlerChains.class, url.openStream());
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
        JavaWsdlMapping wsdlMapping = null;
        try {
            wsdlMapping = (JavaWsdlMapping) JaxbJavaee.unmarshal(JavaWsdlMapping.class, url.openStream());
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
        Definition definition = null;
        try {
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", true);
            reader.setFeature("javax.wsdl.importDocuments", true);
            WsdlResolver wsdlResolver = new WsdlResolver(new URL(url, ".").toExternalForm(), new InputSource(url.openStream()));
            definition = reader.readWSDL(wsdlResolver);
        } catch (IOException e) {
            throw new OpenEJBException("Cannot read the wsdl file: " + url.toExternalForm(), e);
        } catch (Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the wsdl file: " + url.toExternalForm(), e);
        }
        return definition;
    }

    public static Connector readConnector(URL url) throws OpenEJBException {
        Connector connector = null;
        try {
            connector = (Connector) JaxbJavaee.unmarshal(Connector.class, url.openStream());
        } catch (SAXException e) {
            throw new OpenEJBException("Cannot parse the web.xml file: " + url.toExternalForm(), e);
        } catch (JAXBException e) {
            throw new OpenEJBException("Cannot unmarshall the web.xml file: " + url.toExternalForm(), e);
        } catch (IOException e) {
            throw new OpenEJBException("Cannot read the web.xml file: " + url.toExternalForm(), e);
        } catch (Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the web.xml file: " + url.toExternalForm(), e);
        }
        return connector;
    }

    public static WebApp readWebApp(URL url) throws OpenEJBException {
        WebApp webApp = null;
        try {
            webApp = (WebApp) JaxbJavaee.unmarshal(WebApp.class, url.openStream());
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
        TldTaglib tldTaglib = null;
        try {
            tldTaglib = (TldTaglib) JaxbJavaee.unmarshal(TldTaglib.class, url.openStream());
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
            return url.openStream();
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

}
