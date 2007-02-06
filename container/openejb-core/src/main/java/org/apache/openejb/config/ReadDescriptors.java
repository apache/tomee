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
import org.apache.openejb.jee.jpa.unit.JaxbPersistenceFactory;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.oejb2.EnterpriseBean;
import org.apache.openejb.jee.oejb2.GeronimoEjbJarType;
import org.apache.openejb.jee.oejb2.JaxbOpenejbJar2;
import org.apache.openejb.jee.oejb2.OpenejbJarType;
import org.apache.openejb.jee.oejb3.JaxbOpenejbJar3;
import org.apache.openejb.jee.oejb3.OpenejbJar;

import javax.xml.bind.JAXBElement;
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
                ApplicationClient applicationClient = DeploymentLoader.unmarshal(ApplicationClient.class, "META-INF/application-client.xml", url);
                clientModule.setApplicationClient(applicationClient);
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
                OpenejbJar openejbJar = JaxbOpenejbJar3.unmarshal(OpenejbJar.class, source.get());
                ejbModule.setOpenejbJar(openejbJar);
            } catch (Exception e) {
                OpenejbJar openejbJar = new OpenejbJar();
                ejbModule.getAltDDs().put("openejb-jar.xml", openejbJar);
                ejbModule.setOpenejbJar(openejbJar);

                try {
                    JAXBElement element = (JAXBElement) JaxbOpenejbJar2.unmarshal(OpenejbJarType.class, source.get());
                    OpenejbJarType o2 = (OpenejbJarType) element.getValue();

                    GeronimoEjbJarType g2 = new GeronimoEjbJarType();

                    g2.setEnvironment(o2.getEnvironment());
                    g2.setSecurity(o2.getSecurity());
                    g2.getService().addAll(o2.getService());
                    g2.getMessageDestination().addAll(o2.getMessageDestination());

                    for (EnterpriseBean bean : o2.getEnterpriseBeans()) {
                        g2.getAbstractNamingEntry().addAll(bean.getAbstractNamingEntry());
                        g2.getEjbLocalRef().addAll(bean.getEjbLocalRef());
                        g2.getEjbRef().addAll(bean.getEjbRef());
                        g2.getResourceEnvRef().addAll(bean.getResourceEnvRef());
                        g2.getResourceRef().addAll(bean.getResourceRef());
                        g2.getServiceRef().addAll(bean.getServiceRef());
                    }

                    ejbModule.getAltDDs().put("geronimo-openejb.xml", g2);
                } catch (Exception e1) {
                    throw new OpenEJBException(e);
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
            EjbJar ejbJar = DeploymentLoader.unmarshal(EjbJar.class, "META-INF/ejb-jar.xml", url);
            ejbModule.setEjbJar(ejbJar);
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
