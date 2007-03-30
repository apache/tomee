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
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.jpa.unit.JaxbPersistenceFactory;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.oejb2.*;
import org.apache.openejb.jee.oejb3.JaxbOpenejbJar3;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.xml.sax.SAXException;
import org.xml.sax.HandlerBase;
import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class ReadDescriptors implements DynamicDeployer {
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

            Object data = clientModule.getAltDDs().get("application-client.xml");
            if (data instanceof URL) {
                URL url = (URL) data;
                try {
                    ApplicationClient applicationClient = (ApplicationClient) JaxbJavaee.unmarshal(ApplicationClient.class, url.openStream());
                    clientModule.setApplicationClient(applicationClient);
                } catch (SAXException e) {
                    throw new OpenEJBException("Cannot parse the application-client.xml file: "+ url.toExternalForm(), e);
                } catch (JAXBException e) {
                    throw new OpenEJBException("Cannot unmarshall the application-client.xml file: "+ url.toExternalForm(), e);
                } catch (IOException e) {
                    throw new OpenEJBException("Cannot read the application-client.xml file: "+ url.toExternalForm(), e);
                } catch (Exception e) {
                    throw new OpenEJBException("Encountered unknown error parsing the application-client.xml file: "+ url.toExternalForm(), e);
                }
            } else {
                DeploymentLoader.logger.warning("No application-client.xml found assuming annotations present: " + appModule.getJarLocation() + ", module: " + clientModule.getModuleId());
                clientModule.setApplicationClient(new ApplicationClient());
            }

        }

        List<URL> persistenceUrls = (List<URL>) appModule.getAltDDs().get("persistence.xml");
        if (persistenceUrls != null) {
            for (URL url1 : persistenceUrls) {
                String moduleName1 = url1.toExternalForm().replaceFirst("!/?META-INF/persistence.xml$", "");
                moduleName1 = moduleName1.replaceFirst("/?META-INF/persistence.xml$", "/");
                if (moduleName1.startsWith("jar:")) moduleName1 = moduleName1.substring("jar:".length());
                if (moduleName1.startsWith("file:")) moduleName1 = moduleName1.substring("file:".length());
//                if (moduleName1.endsWith("/")) moduleName1 = moduleName1.substring(0, moduleName1.length() - 1);
                try {
                    Persistence persistence = JaxbPersistenceFactory.getPersistence(url1);
                    PersistenceModule persistenceModule = new PersistenceModule(moduleName1, persistence);
                    appModule.getPersistenceModules().add(persistenceModule);
                } catch (Exception e1) {
                    DeploymentLoader.logger.error("Unable to load Persistence Unit from EAR: " + appModule.getJarLocation() + ", module: " + moduleName1 + ". Exception: " + e1.getMessage(), e1);
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

                    Exception e = realIssue[0];
                    if (e instanceof SAXException) {
                        throw new OpenEJBException("Cannot parse the openejb-jar.xml.", e);
                    } else if (e instanceof JAXBException) {
                        throw new OpenEJBException("Cannot unmarshall the openejb-jar.xml.", e);
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

    private void readEjbJar(EjbModule ejbModule, AppModule appModule) throws OpenEJBException {
        Object data = ejbModule.getAltDDs().get("ejb-jar.xml");
        if (data instanceof URL) {
            URL url = (URL) data;
            try {
                EjbJar ejbJar = (EjbJar) JaxbJavaee.unmarshal(EjbJar.class, url.openStream());
                ejbModule.setEjbJar(ejbJar);
            } catch (SAXException e) {
                throw new OpenEJBException("Cannot parse the ejb-jar.xml file: "+ url.toExternalForm(), e);
            } catch (JAXBException e) {
                throw new OpenEJBException("Cannot unmarshall the ejb-jar.xml file: "+ url.toExternalForm(), e);
            } catch (IOException e) {
                throw new OpenEJBException("Cannot read the ejb-jar.xml file: "+ url.toExternalForm(), e);
            } catch (Exception e) {
                throw new OpenEJBException("Encountered unknown error parsing the ejb-jar.xml file: "+ url.toExternalForm(), e);
            }
        } else {
            DeploymentLoader.logger.warning("No ejb-jar.xml found assuming annotated beans present: " + appModule.getJarLocation() + ", module: " + ejbModule.getModuleId());
            ejbModule.setEjbJar(new EjbJar());
        }
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
