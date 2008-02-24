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
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.EjbLocalReferenceInfo;
import org.apache.openejb.assembler.classic.EjbReferenceInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.EnvEntryInfo;
import org.apache.openejb.assembler.classic.InjectionInfo;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.classic.PersistenceContextReferenceInfo;
import org.apache.openejb.assembler.classic.PersistenceUnitReferenceInfo;
import org.apache.openejb.assembler.classic.PortRefInfo;
import org.apache.openejb.assembler.classic.ReferenceLocationInfo;
import org.apache.openejb.assembler.classic.ResourceEnvReferenceInfo;
import org.apache.openejb.assembler.classic.ResourceReferenceInfo;
import org.apache.openejb.assembler.classic.ServiceReferenceInfo;
import org.apache.openejb.assembler.classic.EjbResolver;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.Injectable;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.JndiReference;
import org.apache.openejb.jee.MessageDestinationRef;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceContextType;
import org.apache.openejb.jee.PersistenceUnitRef;
import org.apache.openejb.jee.PortComponentRef;
import org.apache.openejb.jee.Property;
import org.apache.openejb.jee.ResAuth;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.ServiceRef;
import org.apache.openejb.jee.EjbReference;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @version $Rev$ $Date$
 */
public class JndiEncInfoBuilder {


    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, JndiEncInfoBuilder.class);
    protected static final Messages messages = new Messages(JndiEncInfoBuilder.class);

    private final EjbResolver earResolver;
    private final Map<String, EjbResolver> ejbJarResolvers = new HashMap<String, EjbResolver>();

    public JndiEncInfoBuilder(AppInfo appInfo) {

        // Global-scoped EJB Resolver

        EjbResolver globalResolver = SystemInstance.get().getComponent(EjbResolver.class);

        // EAR-scoped EJB Resolver

        earResolver = new EjbResolver(globalResolver, EjbResolver.Scope.EAR, appInfo.ejbJars);

        // EJBJAR-scoped EJB Resolver(s)

        for (EjbJarInfo ejbJarInfo : appInfo.ejbJars) {

            EjbResolver ejbJarResolver = new EjbResolver(earResolver, EjbResolver.Scope.EJBJAR, ejbJarInfo);

            ejbJarResolvers.put(ejbJarInfo.moduleId, ejbJarResolver);
        }
    }

    private EjbResolver getEjbResolver(String moduleId) {
        EjbResolver resolver = ejbJarResolvers.get(moduleId);
        if (resolver == null){
            resolver = earResolver;
        }
        return resolver;
    }

    public JndiEncInfo build(JndiConsumer jndiConsumer, String ejbName, String moduleId) throws OpenEJBException {
        URI moduleUri = null;
        if (moduleId != null) {
            try {
                moduleUri = new URI(moduleId);
            } catch (URISyntaxException e) {
                throw new OpenEJBException("Illegal moduleId " + moduleId, e);
            }
        }

        JndiEncInfo jndi = new JndiEncInfo();

        /* Build Environment entries *****************/
        jndi.envEntries.addAll(buildEnvEntryInfos(jndiConsumer));

        /* Build Resource References *****************/
        jndi.resourceRefs.addAll(buildResourceRefInfos(jndiConsumer));

        /* Build Resource Environment References *****************/
        jndi.resourceEnvRefs.addAll(buildResourceEnvRefInfos(jndiConsumer));

        buildEjbRefs(jndiConsumer, moduleUri, moduleId, ejbName, jndi);

        jndi.persistenceUnitRefs.addAll(buildPersistenceUnitRefInfos(jndiConsumer, moduleUri));

        jndi.persistenceContextRefs.addAll(buildPersistenceContextRefInfos(jndiConsumer, moduleUri));

        jndi.serviceRefs.addAll(buildServiceRefInfos(jndiConsumer));

        return jndi;
    }

    private void buildEjbRefs(JndiConsumer jndiConsumer, URI moduleUri, String moduleId, String ejbName, JndiEncInfo jndi) throws OpenEJBException {

        int size = jndiConsumer.getEjbRef().size() + jndiConsumer.getEjbLocalRef().size();

        ArrayList<EjbReference> references = new ArrayList<EjbReference>(size);

        references.addAll(jndiConsumer.getEjbRef());
        references.addAll(jndiConsumer.getEjbLocalRef());

        for (EjbReference ref : references) {

            EjbReferenceInfo info = new EjbReferenceInfo();

            info.homeType = ref.getHome();
            info.interfaceType = ref.getInterface();
            info.referenceName = ref.getName();
            info.link = ref.getEjbLink();
            info.location = buildLocationInfo(ref);
            info.targets.addAll(buildInjectionInfos(ref));


            if (info.location != null) {
                if (ref.getRefType() == EjbReference.Type.LOCAL){
                    jndi.ejbLocalReferences.add(toLocal(info));
                } else {
                    jndi.ejbReferences.add(info);
                }
                continue;
            }

            EjbResolver ejbResolver = getEjbResolver(moduleId);

            String deploymentId = ejbResolver.resolve(new Ref(ref), moduleUri);

            info.ejbDeploymentId = deploymentId;

            if (info.ejbDeploymentId == null) {
                logger.warning("config.noBeanFoundEjbLink", ref.getName(), ejbName, ref.getEjbLink());
                if (ref.getRefType() == EjbReference.Type.LOCAL) {
                    jndi.ejbLocalReferences.add(toLocal(info));
                } else {
                    jndi.ejbReferences.add(info);
                }
                continue;
            }
            
            info.externalReference = ejbResolver.getScope(deploymentId) == EjbResolver.Scope.GLOBAL;


            if (ref.getRefType() == EjbReference.Type.UNKNOWN) {
                EnterpriseBeanInfo otherBean = ejbResolver.getEnterpriseBeanInfo(deploymentId);
                if (otherBean != null) {
                    if (otherBean.businessRemote.contains(ref.getInterface())) {
                        ref.setRefType(EjbReference.Type.REMOTE);
                    } else {
                        ref.setRefType(EjbReference.Type.LOCAL);
                        jndiConsumer.getEjbRef().remove(ref);
                        jndiConsumer.getEjbLocalRef().add(new EjbLocalRef(ref));
                    }
                }
            }


            if (ref.getRefType() == EjbReference.Type.LOCAL){
                jndi.ejbLocalReferences.add(toLocal(info));
            } else {
                jndi.ejbReferences.add(info);
            }

        }
    }

    private EjbLocalReferenceInfo toLocal(EjbReferenceInfo r) {
        EjbLocalReferenceInfo l = new EjbLocalReferenceInfo();
        l.ejbDeploymentId = r.ejbDeploymentId;
        l.externalReference = r.externalReference;
        l.homeType = r.homeType;
        l.interfaceType = r.interfaceType;
        l.referenceName = r.referenceName;
        l.link = r.link;
        l.location = r.location;
        l.targets.addAll(r.targets);
        return l;
    }

    private List<ServiceReferenceInfo> buildServiceRefInfos(JndiConsumer jndiConsumer) {
        ArrayList<ServiceReferenceInfo> infos = new ArrayList<ServiceReferenceInfo>();
        for (ServiceRef ref : jndiConsumer.getServiceRef()) {
            ServiceReferenceInfo info = new ServiceReferenceInfo();
            info.referenceName = ref.getName();
            info.location = buildLocationInfo(ref);
            info.targets.addAll(buildInjectionInfos(ref));
            infos.add(info);

            if (System.getProperty("duct tape") != null) continue;

            info.id = ref.getMappedName();
            info.serviceQName = ref.getServiceQname();
            info.serviceType = ref.getServiceInterface();
            info.referenceType = ref.getServiceRefType();
            info.wsdlFile = ref.getWsdlFile();
            info.jaxrpcMappingFile = ref.getJaxrpcMappingFile();
            info.handlerChains.addAll(ConfigurationFactory.toHandlerChainInfo(ref.getAllHandlers()));
            for (PortComponentRef portComponentRef : ref.getPortComponentRef()) {
                PortRefInfo portRefInfo = new PortRefInfo();
                portRefInfo.qname = portComponentRef.getQName();
                portRefInfo.serviceEndpointInterface = portComponentRef.getServiceEndpointInterface();
                portRefInfo.enableMtom = portComponentRef.isEnableMtom();
                portRefInfo.properties.putAll(portComponentRef.getProperties());
                info.portRefs.add(portRefInfo);
            }
        }
        return infos;
    }

    private List<PersistenceUnitReferenceInfo> buildPersistenceUnitRefInfos(JndiConsumer jndiConsumer, URI moduleId) {
        ArrayList<PersistenceUnitReferenceInfo> infos = new ArrayList<PersistenceUnitReferenceInfo>();
        for (PersistenceUnitRef puRef : jndiConsumer.getPersistenceUnitRef()) {
            PersistenceUnitReferenceInfo info = new PersistenceUnitReferenceInfo();
            info.referenceName = puRef.getPersistenceUnitRefName();
            info.persistenceUnitName = puRef.getPersistenceUnitName();
            info.unitId = puRef.getMappedName();
            info.location = buildLocationInfo(puRef);
            info.targets.addAll(buildInjectionInfos(puRef));
            infos.add(info);
        }
        return infos;
    }

    private List<PersistenceContextReferenceInfo> buildPersistenceContextRefInfos(JndiConsumer jndiConsumer, URI moduleId) {
        ArrayList<PersistenceContextReferenceInfo> infos = new ArrayList<PersistenceContextReferenceInfo>();

        for (PersistenceContextRef contextRef : jndiConsumer.getPersistenceContextRef()) {
            PersistenceContextReferenceInfo info = new PersistenceContextReferenceInfo();
            info.referenceName = contextRef.getPersistenceContextRefName();
            info.persistenceUnitName = contextRef.getPersistenceUnitName();
            info.unitId = contextRef.getMappedName();
            info.location = buildLocationInfo(contextRef);
            info.extended = (contextRef.getPersistenceContextType() == PersistenceContextType.EXTENDED);
            List<Property> persistenceProperty = contextRef.getPersistenceProperty();
            for (Property property : persistenceProperty) {
                String name = property.getName();
                String value = property.getValue();
                info.properties.setProperty(name, value);
            }
            info.targets.addAll(buildInjectionInfos(contextRef));
            infos.add(info);
        }
        return infos;
    }

    private List<ResourceReferenceInfo> buildResourceRefInfos(JndiConsumer item) {
        List<ResourceReferenceInfo> infos = new ArrayList<ResourceReferenceInfo>();
        for (ResourceRef res : item.getResourceRef()) {
            ResourceReferenceInfo info = new ResourceReferenceInfo();

            if (res.getResAuth() != null) {
                info.referenceAuth = res.getResAuth().toString();
            } else {
                info.referenceAuth = ResAuth.CONTAINER.toString();
            }
            info.referenceName = res.getResRefName();
            info.referenceType = res.getResType();
            info.resourceID = res.getMappedName();
            info.location = buildLocationInfo(res);
            info.targets.addAll(buildInjectionInfos(res));
            infos.add(info);
        }
        return infos;
    }

    private List<ResourceEnvReferenceInfo> buildResourceEnvRefInfos(JndiConsumer item) {
        List<ResourceEnvReferenceInfo> infos = new ArrayList<ResourceEnvReferenceInfo>();
        for (ResourceEnvRef res : item.getResourceEnvRef()) {
            ResourceEnvReferenceInfo info = new ResourceEnvReferenceInfo();
            info.resourceEnvRefName = res.getResourceEnvRefName();
            info.resourceEnvRefType = res.getResourceEnvRefType();
            info.resourceID = res.getMappedName();
            info.location = buildLocationInfo(res);
            info.targets.addAll(buildInjectionInfos(res));
            infos.add(info);
        }
        for (MessageDestinationRef res : item.getMessageDestinationRef()) {
            ResourceEnvReferenceInfo info = new ResourceEnvReferenceInfo();
            info.resourceEnvRefName = res.getMessageDestinationRefName();
            info.resourceEnvRefType = res.getMessageDestinationType();
            info.resourceID = res.getMappedName();
            info.location = buildLocationInfo(res);
            info.targets.addAll(buildInjectionInfos(res));
            infos.add(info);
        }
        return infos;
    }

    private List<EnvEntryInfo> buildEnvEntryInfos(JndiConsumer item) {
        List<EnvEntryInfo> infos = new ArrayList<EnvEntryInfo>();
        for (EnvEntry env : item.getEnvEntry()) {
            // ignore env entries without a value
            if (env.getEnvEntryValue() == null) {
                continue;
            }

            EnvEntryInfo info = new EnvEntryInfo();

            info.name = env.getEnvEntryName();
            info.type = env.getEnvEntryType();
            info.value = env.getEnvEntryValue();
            info.location = buildLocationInfo(env);
            info.targets.addAll(buildInjectionInfos(env));
            infos.add(info);
        }
        return infos;
    }

    public ReferenceLocationInfo buildLocationInfo(JndiReference reference) {
        String mappedName = reference.getMappedName();
        if (mappedName == null || !mappedName.startsWith("jndi:")) {
            return null;
        }
        ReferenceLocationInfo location = new ReferenceLocationInfo();
        location.jndiName = mappedName.replaceFirst("^jndi:", "");
        return location;
    }

    public Collection<? extends InjectionInfo> buildInjectionInfos(Injectable injectable) {
        ArrayList<InjectionInfo> infos = new ArrayList<InjectionInfo>();
        for (InjectionTarget target : injectable.getInjectionTarget()) {
            InjectionInfo info = new InjectionInfo();
            info.className = target.getInjectionTargetClass();
            info.propertyName = target.getInjectionTargetName();
            infos.add(info);
        }
        return infos;
    }

    /**
     * The assembler package cannot have a dependency on org.apache.openejb.jee
     * so we simply have a trimmed down copy of the org.apache.openejb.jee.EjbReference interface
     * and we adapt to it here.
     */
    private static class Ref implements EjbResolver.Reference {
        private final EjbReference ref;

        public Ref(EjbReference ref) {
            this.ref = ref;
        }

        public String getName() {
            return ref.getName();
        }

        public String getEjbLink() {
            return ref.getEjbLink();
        }

        public String getHome() {
            return ref.getHome();
        }

        public String getInterface() {
            return ref.getInterface();
        }

        public String getMappedName() {
            return ref.getMappedName();
        }

        public EjbResolver.Type getRefType() {
            // Could have used EjbResolver.Type.valueOf(..)
            // but this protects against an renaming
            switch(ref.getRefType()){
                case LOCAL: return EjbResolver.Type.LOCAL;
                case REMOTE: return EjbResolver.Type.REMOTE;
                case UNKNOWN: return EjbResolver.Type.UNKNOWN;
                default: return EjbResolver.Type.UNKNOWN;
            }
        }
    }
}
