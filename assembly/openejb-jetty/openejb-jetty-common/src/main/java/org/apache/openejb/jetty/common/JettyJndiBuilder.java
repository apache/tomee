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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.jetty.common;

import org.apache.log4j.Logger;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.*;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.JtaEntityManager;
import org.apache.openejb.persistence.JtaEntityManagerRegistry;
import org.apache.openejb.spi.ContainerSystem;
import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.plus.jndi.NamingEntryUtil;
import org.eclipse.jetty.plus.jndi.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;

public class JettyJndiBuilder {
    private WebAppContext webAppContext;
    private WebAppInfo webApp;
    private Logger log = Logger.getLogger(JettyJndiBuilder.class);

    public JettyJndiBuilder(WebAppContext webAppContext, WebAppInfo webApp) {

        this.webAppContext = webAppContext;
        this.webApp = webApp;
    }

    public void mergeJndi() throws OpenEJBException {
        for (EnvEntryInfo ref : webApp.jndiEnc.envEntries) {
            mergeRef(ref);
        }
        for (EjbReferenceInfo ref : webApp.jndiEnc.ejbReferences) {
            mergeRef(ref);
        }
        for (EjbLocalReferenceInfo ref : webApp.jndiEnc.ejbLocalReferences) {
            mergeRef(ref);
        }
        for (PersistenceContextReferenceInfo ref : webApp.jndiEnc.persistenceContextRefs) {
            mergeRef(ref);
        }
        for (PersistenceUnitReferenceInfo ref : webApp.jndiEnc.persistenceUnitRefs) {
            mergeRef(ref);
        }
        for (ResourceReferenceInfo ref : webApp.jndiEnc.resourceRefs) {
            mergeRef(ref);
        }
        for (ResourceEnvReferenceInfo ref : webApp.jndiEnc.resourceEnvRefs) {
            mergeRef(ref);
        }
//        for (ServiceReferenceInfo ref : webApp.jndiEnc.serviceRefs) {
//            mergeRef(ref);
//        }
    }

    private void mergeRef(ResourceEnvReferenceInfo ref) {
        try {
            new Resource(webAppContext, cleanup(ref.referenceName), new LinkRef("java:openejb/Resource/" + ref.resourceID));
            setupEnv(cleanup(ref.referenceName));
        } catch (Exception e) {
            log.error("Error adding EJB ref" + cleanup(ref.referenceName), e);
        }
    }

    private void mergeRef(ResourceReferenceInfo ref) {
        try {
            new Resource(webAppContext, cleanup(ref.referenceName), new LinkRef("java:openejb/Resource/" + ref.resourceID));
            setupEnv(cleanup(ref.referenceName));
        } catch (Exception e) {
            log.error("Error adding EJB ref" + cleanup(ref.referenceName), e);
        }
    }

    private void mergeRef(PersistenceUnitReferenceInfo ref) {
        try {
            Context context = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();
            EntityManagerFactory factory;
            try {
                factory = (EntityManagerFactory) context.lookup("openejb/PersistenceUnit/" + ref.unitId);
            } catch (NamingException e) {
                throw new IllegalStateException("PersistenceUnit '" + ref.unitId + "' not found for EXTENDED ref '" + ref.referenceName + "'");
            }

            new Resource(webAppContext, cleanup(ref.referenceName), factory);
            setupEnv(cleanup(ref.referenceName));
        } catch (Exception e) {
            log.error("Error adding EJB ref" + cleanup(ref.referenceName), e);
        }
    }

    private void mergeRef(PersistenceContextReferenceInfo ref) {
        try {
            Context context = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();
            EntityManagerFactory factory;
            try {
                factory = (EntityManagerFactory) context.lookup("openejb/PersistenceUnit/" + ref.unitId);
            } catch (NamingException e) {
                throw new IllegalStateException("PersistenceUnit '" + ref.unitId + "' not found for EXTENDED ref '" + ref.referenceName + "'");
            }

            JtaEntityManagerRegistry jtaEntityManagerRegistry = SystemInstance.get().getComponent(JtaEntityManagerRegistry.class);
            JtaEntityManager jtaEntityManager = new JtaEntityManager(ref.persistenceUnitName, jtaEntityManagerRegistry, factory, ref.properties, ref.extended);

            new Resource(webAppContext, cleanup(ref.referenceName), jtaEntityManager);
            setupEnv(cleanup(ref.referenceName));
        } catch (Exception e) {
            log.error("Error adding EJB ref" + cleanup(ref.referenceName), e);
        }
    }

    private void mergeRef(EjbReferenceInfo ref) {
        try {
            String jndiName = new EJBHelper().getJndiName(ref);
            new Resource(webAppContext, cleanup(ref.referenceName), new LinkRef(jndiName));
            setupEnv(cleanup(ref.referenceName));
        } catch (Exception e) {
            log.error("Error adding EJB ref" + cleanup(ref.referenceName), e);
        }
    }

    private void setupEnv(String referenceName) throws NamingException {
        NamingEntryUtil.bindToENC(webAppContext, referenceName, referenceName);
    }

    private void mergeRef(EnvEntryInfo ref) {
        try {
            new EnvEntry(webAppContext, cleanup(ref.referenceName), ref.value, true);
            setupEnv(cleanup(ref.referenceName));
        } catch (Exception e) {
            log.error("Error adding env entry: " + cleanup(ref.referenceName), e);
        }
    }

    private String cleanup(String referenceName) {
        if (referenceName.startsWith("comp/env/")) {
            return referenceName.substring(9);
        }

        return referenceName;
    }

}
