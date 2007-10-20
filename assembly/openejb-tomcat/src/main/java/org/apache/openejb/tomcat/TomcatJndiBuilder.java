/**
 *
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
package org.apache.openejb.tomcat;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.NamingContextListener;
import org.apache.catalina.deploy.ContextEjb;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceEnvRef;
import org.apache.catalina.deploy.ContextTransaction;
import org.apache.catalina.deploy.NamingResources;
import org.apache.naming.factory.Constants;
import org.apache.naming.ContextAccessController;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.EjbLocalReferenceInfo;
import org.apache.openejb.assembler.classic.EjbReferenceInfo;
import org.apache.openejb.assembler.classic.EnvEntryInfo;
import org.apache.openejb.assembler.classic.LinkResolver;
import org.apache.openejb.assembler.classic.PersistenceContextReferenceInfo;
import org.apache.openejb.assembler.classic.PersistenceUnitReferenceInfo;
import org.apache.openejb.assembler.classic.ResourceEnvReferenceInfo;
import org.apache.openejb.assembler.classic.ResourceReferenceInfo;
import org.apache.openejb.assembler.classic.ServiceReferenceInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.JtaEntityManager;
import org.apache.openejb.persistence.JtaEntityManagerRegistry;
import static org.apache.openejb.tomcat.NamingUtil.DEPLOYMENT_ID;
import static org.apache.openejb.tomcat.NamingUtil.EXTENDED;
import static org.apache.openejb.tomcat.NamingUtil.EXTERNAL;
import static org.apache.openejb.tomcat.NamingUtil.JNDI_NAME;
import static org.apache.openejb.tomcat.NamingUtil.JNDI_PROVIDER_ID;
import static org.apache.openejb.tomcat.NamingUtil.LOCAL;
import static org.apache.openejb.tomcat.NamingUtil.NAME;
import static org.apache.openejb.tomcat.NamingUtil.RESOURCE_ID;
import static org.apache.openejb.tomcat.NamingUtil.UNIT;
import static org.apache.openejb.tomcat.NamingUtil.setStaticValue;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import java.net.URI;
import java.net.URISyntaxException;

public class TomcatJndiBuilder {
    private final StandardContext standardContext;
    private final WebAppInfo webAppInfo;
    private final LinkResolver<EntityManagerFactory> emfLinkResolver;
    private final boolean replaceEntry;
    private boolean useCrossClassLoaderRef = true;
    private NamingContextListener namingContextListener;

    public TomcatJndiBuilder(StandardContext standardContext, WebAppInfo webAppInfo, LinkResolver<EntityManagerFactory> emfLinkResolver) {
        this.standardContext = standardContext;
        this.namingContextListener = standardContext.getNamingContextListener();
        this.webAppInfo = webAppInfo;
        this.emfLinkResolver = emfLinkResolver;

        String parameter = standardContext.findParameter("openejb.start.late");
        replaceEntry = Boolean.parseBoolean(parameter);
    }

    public boolean isUseCrossClassLoaderRef() {
        return useCrossClassLoaderRef;
    }

    public void setUseCrossClassLoaderRef(boolean useCrossClassLoaderRef) {
        this.useCrossClassLoaderRef = useCrossClassLoaderRef;
    }

    public void mergeJndi() throws OpenEJBException {
        NamingResources naming = standardContext.getNamingResources();

        URI moduleUri;
        try {
            moduleUri = new URI(webAppInfo.moduleId);
        } catch (URISyntaxException e) {
            throw new OpenEJBException(e);
        }

        for (EnvEntryInfo ref : webAppInfo.jndiEnc.envEntries) {
            mergeRef(naming, ref);
        }
        for (EjbReferenceInfo ref : webAppInfo.jndiEnc.ejbReferences) {
            mergeRef(naming, ref);
        }
        for (EjbLocalReferenceInfo ref : webAppInfo.jndiEnc.ejbLocalReferences) {
            mergeRef(naming, ref);
        }
        for (PersistenceContextReferenceInfo ref : webAppInfo.jndiEnc.persistenceContextRefs) {
            mergeRef(naming, ref, moduleUri);
        }
        for (PersistenceUnitReferenceInfo ref : webAppInfo.jndiEnc.persistenceUnitRefs) {
            mergeRef(naming, ref, moduleUri);
        }
        for (ResourceReferenceInfo ref : webAppInfo.jndiEnc.resourceRefs) {
            mergeRef(naming, ref);
        }
        for (ResourceEnvReferenceInfo ref : webAppInfo.jndiEnc.resourceEnvRefs) {
            mergeRef(naming, ref);
        }
        for (ServiceReferenceInfo ref : webAppInfo.jndiEnc.serviceRefs) {
            mergeRef(naming, ref);
        }
        ContextTransaction contextTransaction = new ContextTransaction();
        contextTransaction.setProperty(Constants.FACTORY, UserTransactionFactory.class.getName());
        naming.setTransaction(contextTransaction);
    }

    public void mergeRef(NamingResources naming, EnvEntryInfo ref) {
        ContextEnvironment environment = naming.findEnvironment(ref.name);
        boolean addEntry = false;
        if (environment == null) {
            environment = new ContextEnvironment();
            environment.setName(ref.name);
            addEntry = true;
        }

        environment.setType(ref.type);
        environment.setValue(ref.value);

        if (addEntry) {
            naming.addEnvironment(environment);
        }

        if (replaceEntry) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) namingContextListener.removeEnvironment(environment.getName());
            namingContextListener.addEnvironment(environment);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }
    }

    public void mergeRef(NamingResources naming, EjbReferenceInfo ref) {
        ContextEjb ejb = naming.findEjb(ref.referenceName);
        boolean addEntry = false;
        if (ejb == null) {
            ejb = new ContextEjb();
            ejb.setName(ref.referenceName);
            addEntry = true;
        }

        ejb.setProperty(Constants.FACTORY, EjbFactory.class.getName());
        ejb.setProperty(NAME, ref.referenceName);
        ejb.setHome(ref.homeType);
        ejb.setRemote(ref.remoteType);
        ejb.setLink(null);
        ejb.setType(ref.remoteType);
        if (useCrossClassLoaderRef) {
            ejb.setProperty(EXTERNAL, "" + ref.externalReference);
        }

        if (ref.ejbDeploymentId != null) {
            ejb.setProperty(DEPLOYMENT_ID, ref.ejbDeploymentId);
        }

        if (ref.location != null) {
            ejb.setProperty(JNDI_NAME, ref.location.jndiName);
            ejb.setProperty(JNDI_PROVIDER_ID, ref.location.jndiProviderId);
        }

        if (addEntry) {
            naming.addEjb(ejb);
        }

        if (replaceEntry) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) namingContextListener.removeEjb(ejb.getName());
            namingContextListener.addEjb(ejb);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }
    }

    public void mergeRef(NamingResources naming, EjbLocalReferenceInfo ref) {
        // NamingContextListener.addLocalEjb is empty so we'll just use an ejb ref
        ContextEjb ejb = naming.findEjb(ref.referenceName);
        boolean addEntry = false;
        if (ejb == null) {
            ejb = new ContextEjb();
            ejb.setName(ref.referenceName);
            addEntry = true;
        }

        ejb.setProperty(Constants.FACTORY, EjbFactory.class.getName());
        ejb.setProperty(NAME, ref.referenceName);
        ejb.setHome(ref.homeType);
        ejb.setRemote(null);
        ejb.setProperty(LOCAL, ref.localType);
        ejb.setLink(null);
        ejb.setType(ref.localType);

        if (ref.ejbDeploymentId != null) {
            ejb.setProperty(DEPLOYMENT_ID, ref.ejbDeploymentId);
        }

        if (ref.location != null) {
            ejb.setProperty(JNDI_NAME, ref.location.jndiName);
            ejb.setProperty(JNDI_PROVIDER_ID, ref.location.jndiProviderId);
        }

        if (addEntry) {
            naming.addEjb(ejb);
        }

        if (replaceEntry) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) namingContextListener.removeEjb(ejb.getName());
            namingContextListener.addEjb(ejb);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }
    }

    public void mergeRef(NamingResources naming, PersistenceContextReferenceInfo ref, URI moduleUri) {
        ContextResource resource = naming.findResource(ref.referenceName);
        boolean addEntry = false;
        if (resource == null) {
            resource = new ContextResource();
            resource.setName(ref.referenceName);
            addEntry = true;
        }

        resource.setProperty(Constants.FACTORY, PersistenceContextFactory.class.getName());
        resource.setProperty(NAME, ref.referenceName);
        resource.setType(EntityManager.class.getName());

        if (ref.persistenceUnitName != null) {
            resource.setProperty(UNIT, ref.persistenceUnitName);
        }
        resource.setProperty(EXTENDED, "" + ref.extended);
        if (ref.properties != null) {
            // resource.setProperty(NamingConstants.PROPERTIES, ref.properties);
        }

        if (ref.location != null) {
            resource.setProperty(JNDI_NAME, ref.location.jndiName);
            resource.setProperty(JNDI_PROVIDER_ID, ref.location.jndiProviderId);
        } else {
            EntityManagerFactory factory = emfLinkResolver.resolveLink(ref.persistenceUnitName, moduleUri);
            if (factory == null) {
                throw new IllegalArgumentException("Persistence unit " + ref.persistenceUnitName + " for persistence-context-ref " + ref.referenceName + " not found");
            }

            JtaEntityManagerRegistry jtaEntityManagerRegistry = SystemInstance.get().getComponent(JtaEntityManagerRegistry.class);
            JtaEntityManager jtaEntityManager = new JtaEntityManager(jtaEntityManagerRegistry, factory, ref.properties, ref.extended);
            setStaticValue(resource, jtaEntityManager);
        }

        if (addEntry) {
            naming.addResource(resource);
        }

        if (replaceEntry) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) namingContextListener.removeResource(resource.getName());
            namingContextListener.addResource(resource);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }
    }

    public void mergeRef(NamingResources naming, PersistenceUnitReferenceInfo ref, URI moduleUri) {
        ContextResource resource = naming.findResource(ref.referenceName);
        boolean addEntry = false;
        if (resource == null) {
            resource = new ContextResource();
            resource.setName(ref.referenceName);
            addEntry = true;
        }

        resource.setProperty(Constants.FACTORY, PersistenceUnitFactory.class.getName());
        resource.setProperty(NAME, ref.referenceName);
        resource.setType(EntityManagerFactory.class.getName());

        if (ref.persistenceUnitName != null) {
            resource.setProperty(UNIT, ref.persistenceUnitName);
        }

        if (ref.location != null) {
            resource.setProperty(JNDI_NAME, ref.location.jndiName);
            resource.setProperty(JNDI_PROVIDER_ID, ref.location.jndiProviderId);
        } else {
            EntityManagerFactory factory = emfLinkResolver.resolveLink(ref.persistenceUnitName, moduleUri);
            if (factory == null) {
                throw new IllegalArgumentException("Persistence unit " + ref.persistenceUnitName + " for persistence-unit-ref " + ref.referenceName + " not found");
            }
            setStaticValue(resource, factory);
        }

        if (addEntry) {
            naming.addResource(resource);
        }

        if (replaceEntry) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) namingContextListener.removeResource(resource.getName());
            namingContextListener.addResource(resource);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }
    }

    public void mergeRef(NamingResources naming, ResourceReferenceInfo ref) {
        ContextResource resource = naming.findResource(ref.referenceName);
        boolean addEntry = false;
        if (resource == null) {
            resource = new ContextResource();
            resource.setName(ref.referenceName);
            addEntry = true;
        }

        resource.setProperty(Constants.FACTORY, ResourceFactory.class.getName());
        resource.setProperty(NAME, ref.referenceName);
        resource.setType(ref.referenceType);
        resource.setAuth(ref.referenceAuth);

        if (ref.resourceID != null) {
            resource.setProperty(RESOURCE_ID, ref.resourceID);
        }

        if (ref.properties != null) {
            // resource.setProperty(NamingConstants.PROPERTIES, ref.properties);
        }

        if (ref.location != null) {
            resource.setProperty(JNDI_NAME, ref.location.jndiName);
            resource.setProperty(JNDI_PROVIDER_ID, ref.location.jndiProviderId);
        }

        if (addEntry) {
            naming.addResource(resource);
        }

        if (replaceEntry) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) namingContextListener.removeResource(resource.getName());
            namingContextListener.addResource(resource);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }
    }

    public void mergeRef(NamingResources naming, ResourceEnvReferenceInfo ref) {
        ContextResourceEnvRef resourceEnv = naming.findResourceEnvRef(ref.resourceEnvRefName);
        boolean addEntry = false;
        if (resourceEnv == null) {
            resourceEnv = new ContextResourceEnvRef();
            resourceEnv.setName(ref.resourceEnvRefName);
            addEntry = true;
        }

        if (UserTransaction.class.getName().equals(ref.resourceEnvRefType)) {
            resourceEnv.setProperty(Constants.FACTORY, UserTransactionFactory.class.getName());
        } else {
            resourceEnv.setProperty(Constants.FACTORY, ResourceFactory.class.getName());
            resourceEnv.setProperty(NAME, ref.resourceEnvRefName);
            resourceEnv.setType(ref.resourceEnvRefType);

            if (ref.resourceID != null) {
                resourceEnv.setProperty(RESOURCE_ID, ref.resourceID);
            }

            if (ref.location != null) {
                resourceEnv.setProperty(JNDI_NAME, ref.location.jndiName);
                resourceEnv.setProperty(JNDI_PROVIDER_ID, ref.location.jndiProviderId);
            }
        }

        if (addEntry) {
            naming.addResourceEnvRef(resourceEnv);
        }

        if (replaceEntry) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) namingContextListener.removeResourceEnvRef(resourceEnv.getName());
            namingContextListener.addResourceEnvRef(resourceEnv);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void mergeRef(NamingResources naming, ServiceReferenceInfo ref) {
        // service refs aren't supported yet
    }

}
