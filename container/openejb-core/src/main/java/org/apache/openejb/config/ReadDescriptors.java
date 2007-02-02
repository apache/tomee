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
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.JaxbPersistenceFactory;
import org.apache.openejb.jee.oejb3.OpenejbJar;

import java.net.URL;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
class ReadDescriptors implements DynamicDeployer {
    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        for (EjbModule ejbModule : appModule.getEjbModules()) {

            Object data = ejbModule.getAltDDs().get("ejb-jar.xml");
            if (data instanceof URL) {
                URL url = (URL) data;
                EjbJar ejbJar = DeploymentLoader.unmarshal(EjbJar.class, "META-INF/ejb-jar.xml", url);
                ejbModule.setEjbJar(ejbJar);
            } else {
                DeploymentLoader.logger.warning("No ejb-jar.xml found assuming annotated beans present: " + appModule.getJarLocation() + ", module: " + ejbModule.getModuleId());
                ejbModule.setEjbJar(new EjbJar());
            }

            data = ejbModule.getAltDDs().get("openejb-jar.xml");
            if (data instanceof URL) {
                URL url = (URL) data;
                OpenejbJar openejbJar = DeploymentLoader.unmarshal(OpenejbJar.class, "META-INF/openejb-jar.xml", url);
                ejbModule.setOpenejbJar(openejbJar);
            }

        }

        for (ClientModule clientModule : appModule.getClientModules()) {

            Object data = clientModule.getAltDDs().get("ejb-jar.xml");
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
        for (URL url1 : persistenceUrls) {
            String moduleName1 = url1.toExternalForm().replaceFirst("!?/?META-INF/persistence.xml$", "");
            if (moduleName1.startsWith("jar:")) moduleName1 = moduleName1.substring("jar:".length());
            if (moduleName1.startsWith("file:")) moduleName1 = moduleName1.substring("file:".length());
            if (moduleName1.endsWith("/")) moduleName1 = moduleName1.substring(0, moduleName1.length() -1);
            try {
                Persistence persistence = JaxbPersistenceFactory.getPersistence(url1);
                PersistenceModule persistenceModule = new PersistenceModule(moduleName1, persistence);
                appModule.getPersistenceModules().add(persistenceModule);

            } catch (Exception e1) {
                DeploymentLoader.logger.error("Unable to load Persistence Unit from EAR: " + appModule.getJarLocation() + ", module: " + moduleName1 + ". Exception: " + e1.getMessage(), e1);
            }
        }

        return appModule;

    }
}
