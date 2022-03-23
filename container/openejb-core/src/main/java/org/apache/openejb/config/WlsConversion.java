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

import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.Jndi;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.wls.JaxbWls;
import org.apache.openejb.jee.wls.WeblogicEjbJar;
import org.apache.openejb.jee.wls.WeblogicEnterpriseBean;
import org.apache.openejb.loader.IO;

import jakarta.xml.bind.JAXBElement;
import java.io.ByteArrayInputStream;
import java.net.URL;

public class WlsConversion implements DynamicDeployer {
    public AppModule deploy(final AppModule appModule) {
        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            convertModule(ejbModule, appModule.getCmpMappings());
        }
        return appModule;
    }

    private <T> T getDescriptor(final EjbModule ejbModule, final String descriptor, final Class<T> type) {
        Object altDD = ejbModule.getAltDDs().get(descriptor);
        if (altDD instanceof String) {
            try {
                altDD = JaxbWls.unmarshal(type, new ByteArrayInputStream(((String) altDD).getBytes()));
            } catch (final Exception e) {
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD instanceof URL) {
            try {
                altDD = JaxbWls.unmarshal(type, IO.read((URL) altDD));
            } catch (final Exception e) {
                e.printStackTrace();
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD == null) {
            return null;
        }
        if (altDD instanceof JAXBElement) {
            final JAXBElement jaxbElement = (JAXBElement) altDD;
            altDD = jaxbElement.getValue();
        }
        return (T) altDD;
    }

    public void convertModule(final EjbModule ejbModule, final EntityMappings entityMappings) {

        // merge data from weblogic-ejb-jar.xml file
        final WeblogicEjbJar weblogicEjbJar = getDescriptor(ejbModule, "weblogic-ejb-jar.xml", WeblogicEjbJar.class);
        mergeEjbConfig(ejbModule, weblogicEjbJar);

    }

    private void mergeEjbConfig(final EjbModule ejbModule, final WeblogicEjbJar weblogicEjbJar) {
        final OpenejbJar openejbJar = ejbModule.getOpenejbJar();

        if (openejbJar == null) {
            return;
        }
        if (weblogicEjbJar == null) {
            return;
        }
        if (weblogicEjbJar.getWeblogicEnterpriseBean().size() == 0) {
            return;
        }

        for (final WeblogicEnterpriseBean ejb : weblogicEjbJar.getWeblogicEnterpriseBean()) {

            final EjbDeployment deployment = openejbJar.getDeploymentsByEjbName().get(ejb.getEjbName());
            if (deployment == null) {
                // warn no matching deployment
                continue;
            }

            // JNDI name of the remote home (legacy remote interface)
            if (ejb.getJndiName() != null) {
                deployment.getJndi().add(new Jndi(ejb.getJndiName(), "RemoteHome"));
            }

            // JNDI name of the remote home (legacy remote interface)
            if (ejb.getLocalJndiName() != null) {
                deployment.getJndi().add(new Jndi(ejb.getLocalJndiName(), "LocalHome"));
            }

            // TODO: What would be the default JNDI name for a business remote interface?
            //deployment.getJndi().add(new Jndi("{theFormat}", "Remote"));

            // TODO: What would be the default JNDI name for a business local interface?
            //deployment.getJndi().add(new Jndi("{theFormat}", "Local"));

        }
    }

}
