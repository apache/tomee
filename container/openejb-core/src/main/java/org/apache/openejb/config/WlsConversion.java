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

import org.apache.openejb.jee.sun.JaxbSun;
import org.apache.openejb.jee.sun.SunEjbJar;
import org.apache.openejb.jee.sun.SunCmpMappings;
import org.apache.openejb.jee.sun.ResourceRef;
import org.apache.openejb.jee.sun.ResourceEnvRef;
import org.apache.openejb.jee.sun.MessageDestinationRef;
import org.apache.openejb.jee.sun.PortInfo;
import org.apache.openejb.jee.sun.StubProperty;
import org.apache.openejb.jee.sun.SunCmpMapping;
import org.apache.openejb.jee.sun.WebserviceEndpoint;
import org.apache.openejb.jee.sun.Ejb;
import org.apache.openejb.jee.sun.Cmp;
import org.apache.openejb.jee.sun.OneOneFinders;
import org.apache.openejb.jee.sun.Finder;
import org.apache.openejb.jee.sun.EntityMapping;
import org.apache.openejb.jee.sun.ColumnPair;
import org.apache.openejb.jee.sun.CmpFieldMapping;
import org.apache.openejb.jee.sun.ColumnName;
import org.apache.openejb.jee.sun.CmrFieldMapping;
import org.apache.openejb.jee.ServiceRef;
import org.apache.openejb.jee.PortComponentRef;
import org.apache.openejb.jee.WebserviceDescription;
import org.apache.openejb.jee.PortComponent;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.ServiceImplBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.CmpField;
import org.apache.openejb.jee.wls.JaxbWls;
import org.apache.openejb.jee.wls.WeblogicEjbJar;
import org.apache.openejb.jee.wls.WeblogicEnterpriseBean;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.EjbLink;
import org.apache.openejb.jee.oejb3.ResourceLink;
import org.apache.openejb.jee.oejb3.Jndi;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.Entity;
import org.apache.openejb.jee.jpa.NamedQuery;
import org.apache.openejb.jee.jpa.Table;
import org.apache.openejb.jee.jpa.SecondaryTable;
import org.apache.openejb.jee.jpa.PrimaryKeyJoinColumn;
import org.apache.openejb.jee.jpa.Field;
import org.apache.openejb.jee.jpa.Column;
import org.apache.openejb.jee.jpa.RelationField;
import org.apache.openejb.jee.jpa.OneToOne;
import org.apache.openejb.jee.jpa.JoinColumn;
import org.apache.openejb.jee.jpa.OneToMany;
import org.apache.openejb.jee.jpa.ManyToOne;
import org.apache.openejb.jee.jpa.JoinTable;
import org.apache.openejb.jee.jpa.Id;
import org.apache.openejb.jee.jpa.Attributes;
import org.apache.openejb.jee.jpa.Basic;
import org.apache.openejb.jee.jpa.AttributeOverride;

import javax.xml.bind.JAXBElement;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.util.LinkedList;

public class WlsConversion implements DynamicDeployer {
    public AppModule deploy(AppModule appModule) {
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            convertModule(ejbModule, appModule.getCmpMappings());
        }
        return appModule;
    }

    private <T> T getDescriptor(EjbModule ejbModule, String descriptor, Class<T> type) {
        Object altDD = ejbModule.getAltDDs().get(descriptor);
        if (altDD instanceof String) {
            try {
                altDD = JaxbWls.unmarshal(type, new ByteArrayInputStream(((String)altDD).getBytes()));
            } catch (Exception e) {
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD instanceof URL) {
            try {
                altDD = JaxbWls.unmarshal(type, ((URL)altDD).openStream());
            } catch (Exception e) {
                e.printStackTrace();
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD == null) return null;
        if (altDD instanceof JAXBElement) {
            JAXBElement jaxbElement = (JAXBElement) altDD;
            altDD = jaxbElement.getValue();
        }
        return (T) altDD;
    }

    public void convertModule(EjbModule ejbModule, EntityMappings entityMappings) {

        // merge data from weblogic-ejb-jar.xml file
        WeblogicEjbJar weblogicEjbJar = getDescriptor(ejbModule, "weblogic-ejb-jar.xml", WeblogicEjbJar.class);
        mergeEjbConfig(ejbModule, weblogicEjbJar);

    }

    private void mergeEjbConfig(EjbModule ejbModule, WeblogicEjbJar weblogicEjbJar) {
        EjbJar ejbJar = ejbModule.getEjbJar();
        OpenejbJar openejbJar = ejbModule.getOpenejbJar();

        if (openejbJar == null) return;
        if (weblogicEjbJar == null) return;
        if (weblogicEjbJar.getWeblogicEnterpriseBean().size() == 0) return;

        for (WeblogicEnterpriseBean ejb : weblogicEjbJar.getWeblogicEnterpriseBean()) {

            EjbDeployment deployment = openejbJar.getDeploymentsByEjbName().get(ejb.getEjbName());
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
