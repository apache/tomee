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
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.ContextReferenceInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EjbLocalReferenceInfo;
import org.apache.openejb.assembler.classic.EjbReferenceInfo;
import org.apache.openejb.assembler.classic.EjbResolver;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.EnvEntryInfo;
import org.apache.openejb.assembler.classic.InjectableInfo;
import org.apache.openejb.assembler.classic.InjectionInfo;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.classic.PersistenceContextReferenceInfo;
import org.apache.openejb.assembler.classic.PersistenceUnitReferenceInfo;
import org.apache.openejb.assembler.classic.PortRefInfo;
import org.apache.openejb.assembler.classic.ReferenceLocationInfo;
import org.apache.openejb.assembler.classic.ResourceEnvReferenceInfo;
import org.apache.openejb.assembler.classic.ResourceReferenceInfo;
import org.apache.openejb.assembler.classic.ServiceReferenceInfo;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EjbReference;
import org.apache.openejb.jee.EnterpriseBean;
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
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.URLs;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.openejb.assembler.classic.EjbResolver.Scope.EAR;
import static org.apache.openejb.assembler.classic.EjbResolver.Scope.EJBJAR;

/**
 * @version $Rev$ $Date$
 */
public class JndiEncInfoBuilder {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, JndiEncInfoBuilder.class);
    private final EjbResolver earResolver;
    private final Map<String, EjbResolver> ejbJarResolvers = new HashMap<String, EjbResolver>();
    private final AppInfo appInfo;

    public JndiEncInfoBuilder(AppInfo appInfo) {
        this.appInfo = appInfo;

        // Global-scoped EJB Resolver
        final EjbResolver globalResolver = SystemInstance.get().getComponent(EjbResolver.class);

        // EAR-scoped EJB Resolver
        this.earResolver = new EjbResolver(globalResolver, EAR, appInfo.ejbJars);

        // EJBJAR-scoped EJB Resolver(s)
        for (EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
            final EjbResolver ejbJarResolver = new EjbResolver(earResolver, EJBJAR, ejbJarInfo);
            this.ejbJarResolvers.put(ejbJarInfo.moduleName, ejbJarResolver);
        }
    }

    private EjbResolver getEjbResolver(String moduleId) {
        EjbResolver resolver = ejbJarResolvers.get(moduleId);
        if (resolver == null) {
            resolver = this.earResolver;
        }
        return resolver;
    }

    public void build(JndiConsumer jndiConsumer, String ejbName, String moduleId, URI moduleUri, JndiEncInfo moduleJndiEnc, JndiEncInfo compJndiEnc) throws OpenEJBException {
        assert moduleJndiEnc != null;
        assert compJndiEnc != null;
        assert jndiConsumer != null;              

        /* Build Environment entries *****************/
        buildEnvEntryInfos(jndiConsumer, moduleJndiEnc, compJndiEnc);

        /* Build Resource References *****************/
        buildResourceRefInfos(jndiConsumer, moduleJndiEnc, compJndiEnc);

        /* Build Resource Environment References *****************/
        buildResourceEnvRefInfos(jndiConsumer, moduleJndiEnc, compJndiEnc);

        buildEjbRefs(jndiConsumer, moduleUri, moduleId, ejbName, moduleJndiEnc, compJndiEnc);
        buildPersistenceUnitRefInfos(jndiConsumer, moduleJndiEnc, compJndiEnc);
        buildPersistenceContextRefInfos(jndiConsumer, moduleJndiEnc, compJndiEnc);
        buildServiceRefInfos(jndiConsumer, moduleJndiEnc, compJndiEnc);
    }

    private void buildEjbRefs(JndiConsumer jndiConsumer, URI moduleUri, String moduleId, String ejbName, JndiEncInfo moduleJndiEnc, JndiEncInfo compJndiEnc) throws OpenEJBException {
        final Collection<EjbRef> ejbRefs = jndiConsumer.getEjbRef();
        final Collection<EjbLocalRef> ejbLocalRefs = jndiConsumer.getEjbLocalRef();
        final List<EjbReference> references = new ArrayList<EjbReference>(ejbRefs.size() + ejbLocalRefs.size());
        references.addAll(ejbRefs);
        references.addAll(ejbLocalRefs);

        for (EjbReference ref : references) {
            final EjbReferenceInfo info = new EjbReferenceInfo();
            info.homeClassName = ref.getHome();
            info.interfaceClassName = ref.getInterface();
            info.referenceName = ref.getName();
            info.link = ref.getEjbLink();
            info.location = buildLocationInfo(ref);
            info.targets.addAll(buildInjectionInfos(ref));
            info.localbean = isIntefaceLocalBean(info.interfaceClassName);

            if (info.location != null) {
                if (ref.getRefType() == EjbReference.Type.LOCAL) {
                    insert(toLocal(info), appInfo.globalJndiEnc.ejbLocalReferences, appInfo.appJndiEnc.ejbLocalReferences, moduleJndiEnc.ejbLocalReferences, compJndiEnc.ejbLocalReferences);
                } else {
                    insert(info, appInfo.globalJndiEnc.ejbReferences, appInfo.appJndiEnc.ejbReferences, moduleJndiEnc.ejbReferences, compJndiEnc.ejbReferences);
                }
                continue;
            }

            final EjbResolver ejbResolver = getEjbResolver(moduleId);
            final String deploymentId = ejbResolver.resolve(new Ref(ref), moduleUri);
            info.ejbDeploymentId = deploymentId;

            if (info.ejbDeploymentId == null) {
                if (info.link != null) {
                    logger.warning("config.noBeanFoundEjbLink", ref.getName(), ejbName, ref.getEjbLink());
                } else {
                    logger.warning("config.noBeanFound", ref.getName(), ejbName, ref.getEjbLink());
                }

            } else {
                final EjbResolver.Scope scope = ejbResolver.getScope(deploymentId);
                info.externalReference = (scope != EAR && scope != EJBJAR);

                if (ref.getRefType() == EjbReference.Type.UNKNOWN) {
                    EnterpriseBeanInfo otherBean = ejbResolver.getEnterpriseBeanInfo(deploymentId);
                    if (otherBean != null) {
                        if (otherBean.businessLocal.contains(ref.getInterface()) || otherBean.ejbClass.equals(ref.getInterface())) {
                            ref.setRefType(EjbReference.Type.LOCAL);
                            ejbRefs.remove(ref);
                            ejbLocalRefs.add(new EjbLocalRef(ref));
                        } else {
                            ref.setRefType(EjbReference.Type.REMOTE);
                        }
                    }
                }
            }

            if (ref.getRefType() == EjbReference.Type.LOCAL) {
                insert(
                        toLocal(info),
                        appInfo.globalJndiEnc.ejbLocalReferences,
                        appInfo.appJndiEnc.ejbLocalReferences,
                        moduleJndiEnc.ejbLocalReferences,
                        compJndiEnc.ejbLocalReferences
                );

            } else {
                insert(
                        info,
                        appInfo.globalJndiEnc.ejbReferences,
                        appInfo.appJndiEnc.ejbReferences,
                        moduleJndiEnc.ejbReferences,
                        compJndiEnc.ejbReferences
                );
            }
        }
    }

    private EjbLocalReferenceInfo toLocal(EjbReferenceInfo referenceInfo) {
        final EjbLocalReferenceInfo local = new EjbLocalReferenceInfo();
        local.ejbDeploymentId = referenceInfo.ejbDeploymentId;
        local.externalReference = referenceInfo.externalReference;
        local.homeClassName = referenceInfo.homeClassName;
        local.interfaceClassName = referenceInfo.interfaceClassName;
        local.referenceName = referenceInfo.referenceName;
        local.link = referenceInfo.link;
        local.location = referenceInfo.location;
        local.targets.addAll(referenceInfo.targets);
        local.localbean = referenceInfo.localbean;
        return local;
    }

    private void buildServiceRefInfos(JndiConsumer jndiConsumer, JndiEncInfo moduleJndiEnc, JndiEncInfo compJndiEnc) {
        for (ServiceRef ref : jndiConsumer.getServiceRef()) {
            final ServiceReferenceInfo info = new ServiceReferenceInfo();

            info.referenceName = ref.getName();
            info.location = buildLocationInfo(ref);
            info.targets.addAll(buildInjectionInfos(ref));
            insert(
                    info,
                    appInfo.globalJndiEnc.serviceRefs,
                    appInfo.appJndiEnc.serviceRefs,
                    moduleJndiEnc.serviceRefs,
                    compJndiEnc.serviceRefs
            );

            if (SystemInstance.get().hasProperty("openejb.geronimo")) {
                continue;
            }

            info.id = ref.getMappedName();
            info.serviceQName = ref.getServiceQname();
            info.serviceType = ref.getServiceInterface();
            info.referenceType = ref.getServiceRefType();
            info.wsdlFile = ref.getWsdlFile();
            info.jaxrpcMappingFile = ref.getJaxrpcMappingFile();
            info.handlerChains.addAll(ConfigurationFactory.toHandlerChainInfo(ref.getAllHandlers()));

            for (PortComponentRef portComponentRef : ref.getPortComponentRef()) {
                final PortRefInfo portRefInfo = new PortRefInfo();
                portRefInfo.qname = portComponentRef.getQName();
                portRefInfo.serviceEndpointInterface = portComponentRef.getServiceEndpointInterface();
                portRefInfo.enableMtom = portComponentRef.isEnableMtom();
                portRefInfo.properties.putAll(portComponentRef.getProperties());
                info.portRefs.add(portRefInfo);
            }
        }
    }

    private void buildPersistenceUnitRefInfos(JndiConsumer jndiConsumer, JndiEncInfo moduleJndiEnc, JndiEncInfo compJndiEnc) {
        for (PersistenceUnitRef puRef : jndiConsumer.getPersistenceUnitRef()) {
            PersistenceUnitReferenceInfo info = new PersistenceUnitReferenceInfo();
            info.referenceName = puRef.getPersistenceUnitRefName();
            info.persistenceUnitName = puRef.getPersistenceUnitName();
            info.unitId = puRef.getMappedName();
            info.location = buildLocationInfo(puRef);
            info.targets.addAll(buildInjectionInfos(puRef));
            insert(info, appInfo.globalJndiEnc.persistenceUnitRefs, appInfo.appJndiEnc.persistenceUnitRefs, moduleJndiEnc.persistenceUnitRefs, compJndiEnc.persistenceUnitRefs);
        }
    }

    private void buildPersistenceContextRefInfos(JndiConsumer jndiConsumer, JndiEncInfo moduleJndiEnc, JndiEncInfo compJndiEnc) {
        for (PersistenceContextRef contextRef : jndiConsumer.getPersistenceContextRef()) {
            final PersistenceContextReferenceInfo info = new PersistenceContextReferenceInfo();
            info.referenceName = contextRef.getPersistenceContextRefName();
            info.persistenceUnitName = contextRef.getPersistenceUnitName();
            info.unitId = contextRef.getMappedName();
            info.location = buildLocationInfo(contextRef);
            info.extended = (contextRef.getPersistenceContextType() == PersistenceContextType.EXTENDED);

            final List<Property> persistenceProperty = contextRef.getPersistenceProperty();
            for (Property property : persistenceProperty) {
                String name = property.getName();
                String value = property.getValue();
                info.properties.setProperty(name, value);
            }
            info.targets.addAll(buildInjectionInfos(contextRef));

            insert(
                    info,
                    appInfo.globalJndiEnc.persistenceContextRefs,
                    appInfo.appJndiEnc.persistenceContextRefs,
                    moduleJndiEnc.persistenceContextRefs,
                    compJndiEnc.persistenceContextRefs
            );
        }
    }

    private void buildResourceRefInfos(JndiConsumer item, JndiEncInfo moduleJndiEnc, JndiEncInfo compJndiEnc) {
        for (ResourceRef res : item.getResourceRef()) {
            final ResourceReferenceInfo info;
            if (res instanceof ContextRef) {
                info = new ContextReferenceInfo();
            } else {
                info = new ResourceReferenceInfo();
            }

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

            insert(
                    info,
                    appInfo.globalJndiEnc.resourceRefs,
                    appInfo.appJndiEnc.resourceRefs,
                    moduleJndiEnc.resourceRefs,
                    compJndiEnc.resourceRefs
            );
        }
    }

    private void buildResourceEnvRefInfos(JndiConsumer item, JndiEncInfo moduleJndiEnc, JndiEncInfo compJndiEnc) {
        for (ResourceEnvRef res : item.getResourceEnvRef()) {
            final ResourceEnvReferenceInfo info = new ResourceEnvReferenceInfo();
            info.referenceName = res.getResourceEnvRefName();
            info.resourceEnvRefType = res.getResourceEnvRefType();
            info.resourceID = res.getMappedName();
            info.location = buildLocationInfo(res);
            info.targets.addAll(buildInjectionInfos(res));

            insert(
                    info,
                    appInfo.globalJndiEnc.resourceEnvRefs,
                    appInfo.appJndiEnc.resourceEnvRefs,
                    moduleJndiEnc.resourceEnvRefs,
                    compJndiEnc.resourceEnvRefs
            );
        }
        for (MessageDestinationRef res : item.getMessageDestinationRef()) {
            final ResourceEnvReferenceInfo info = new ResourceEnvReferenceInfo();
            info.referenceName = res.getMessageDestinationRefName();
            info.resourceEnvRefType = res.getMessageDestinationType();
            info.resourceID = res.getMappedName();
            info.location = buildLocationInfo(res);
            info.targets.addAll(buildInjectionInfos(res));

            insert(
                    info,
                    appInfo.globalJndiEnc.resourceEnvRefs,
                    appInfo.appJndiEnc.resourceEnvRefs,
                    moduleJndiEnc.resourceEnvRefs,
                    compJndiEnc.resourceEnvRefs
            );
        }
    }

    private void buildEnvEntryInfos(JndiConsumer item, JndiEncInfo moduleJndiEnc, JndiEncInfo compJndiEnc) {
        for (EnvEntry env : item.getEnvEntry()) {
            // ignore env entries without a value and lookup name
            //If the the reference name of the environment entry is belong to those shareable JNDI name space, it somewhat is a valid one            
            if (env.getEnvEntryValue() == null && env.getLookupName() == null && !isShareableJNDINamespace(env.getEnvEntryName())) {
                continue;
            }

            final EnvEntryInfo info = new EnvEntryInfo();
            info.referenceName = env.getEnvEntryName();
            info.type = env.getEnvEntryType();
            info.value = env.getEnvEntryValue();
            info.location = buildLocationInfo(env);
            info.targets.addAll(buildInjectionInfos(env));

            insert(
                    info,
                    appInfo.globalJndiEnc.envEntries,
                    appInfo.appJndiEnc.envEntries,
                    moduleJndiEnc.envEntries,
                    compJndiEnc.envEntries
            );
        }
    }

    private boolean isShareableJNDINamespace(String jndiName) {
        return jndiName.startsWith("java:global/") || jndiName.startsWith("java:app/") || jndiName.startsWith("java:module/");
    }

    private ReferenceLocationInfo buildLocationInfo(JndiReference reference) {
        final String lookupName = reference.getLookupName();
        if (lookupName != null) {
            final ReferenceLocationInfo location = new ReferenceLocationInfo();
            location.jndiName = lookupName;
            return location;
        }

        final String mappedName = reference.getMappedName();
        if (mappedName != null && mappedName.startsWith("jndi:")) {
            final ReferenceLocationInfo location = new ReferenceLocationInfo();
            final String name = mappedName.substring(5);

            if (name.startsWith("ext://")) {
                final URI uri = URLs.uri(name);
                location.jndiProviderId = uri.getHost();
                location.jndiName = uri.getPath();
            } else {
                location.jndiName = name;
            }
            return location;
        }
        return null;
    }

    private Collection<? extends InjectionInfo> buildInjectionInfos(Injectable injectable) {
        final List<InjectionInfo> infos = new ArrayList<InjectionInfo>();
        for (InjectionTarget target : injectable.getInjectionTarget()) {
            final InjectionInfo info = new InjectionInfo();
            info.className = target.getInjectionTargetClass();
            info.propertyName = target.getInjectionTargetName();
            infos.add(info);
        }
        return infos;
    }

    public void buildDependsOnRefs(EnterpriseBean enterpriseBean, EnterpriseBeanInfo beanInfo, String moduleId) throws OpenEJBException {
        if (!(enterpriseBean instanceof SessionBean)) {
            return;
        }

        final SessionBean sessionBean = (SessionBean) enterpriseBean;
        final URI moduleUri;
        if (moduleId == null) {
            moduleUri = null;
        } else {
            moduleUri = URLs.uri(moduleId);
        }
        final EjbResolver ejbResolver = getEjbResolver(moduleId);

        if (sessionBean.getDependsOn() != null) {
            for (String ejbName : sessionBean.getDependsOn()) {
                final String deploymentId = ejbResolver.resolve(new SimpleRef(ejbName), moduleUri);
                if (deploymentId != null) {
                    beanInfo.dependsOn.add(deploymentId);
                }
            }
        }
    }

    private boolean isIntefaceLocalBean(String interfaceClassName) {
        if (interfaceClassName == null) {
            return false;
        }
        final EnterpriseBeanInfo beanInfo = getInterfaceBeanInfo(interfaceClassName);
        return isLocalBean(beanInfo) && beanInfo.parents.contains(interfaceClassName);
    }

    private EnterpriseBeanInfo getInterfaceBeanInfo(String interfaceClassName) {
        if (interfaceClassName == null) {
            throw new IllegalArgumentException("interfaceClassName cannot be null");
        }

        final List<EjbJarInfo> ejbJars = appInfo.ejbJars;
        for (EjbJarInfo ejbJar : ejbJars) {
            final List<EnterpriseBeanInfo> enterpriseBeans = ejbJar.enterpriseBeans;
            for (EnterpriseBeanInfo enterpriseBean : enterpriseBeans) {
                if (interfaceClassName.equals(enterpriseBean.ejbClass)
                        || interfaceClassName.equals(enterpriseBean.local)
                        || interfaceClassName.equals(enterpriseBean.remote)
                        || enterpriseBean.businessLocal.contains(interfaceClassName)
                        || enterpriseBean.businessRemote.contains(interfaceClassName)) {
                    return enterpriseBean;
                }
            }
        }

        // look if it is an abstract injection (local bean)
        for (EjbJarInfo ejbJar : ejbJars) {
            for (EnterpriseBeanInfo enterpriseBean : ejbJar.enterpriseBeans) {
                if (enterpriseBean.parents.contains(interfaceClassName)) {
                    return enterpriseBean;
                }
            }
        }

        return null;
    }

    private boolean isLocalBean(EnterpriseBeanInfo beanInfo) {
        return beanInfo != null && beanInfo.localbean;
    }

    private static class SimpleRef implements EjbResolver.Reference {
        private final String name;

        public SimpleRef(String name) {
            this.name = name;
        }

        public String getEjbLink() {
            return name;
        }

        public String getHome() {
            return null;
        }

        public String getInterface() {
            return null;
        }

        public String getMappedName() {
            return null;
        }

        public String getName() {
            return name;
        }

        public EjbResolver.Type getRefType() {
            return EjbResolver.Type.UNKNOWN;
        }
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
            switch (ref.getRefType()) {
                case LOCAL: return EjbResolver.Type.LOCAL;
                case REMOTE: return EjbResolver.Type.REMOTE;
                case UNKNOWN: return EjbResolver.Type.UNKNOWN;
                default: return EjbResolver.Type.UNKNOWN;
            }
        }
    }

    public <I extends InjectableInfo> void insert(I i, List<I> global, List<I> app, List<I> module, List<I> comp) {
        String name = i.referenceName;
        if (!name.startsWith("java:")) {
            i.referenceName = "comp/env/" + name;
            comp.add(i);
        } else if (name.startsWith("java:global/")) {
            i.referenceName = name.substring(5);
            global.add(i);
        } else if (name.startsWith("java:app/")) {
            i.referenceName = name.substring(5);
            app.add(i);
        } else if (name.startsWith("java:module/")) {
            i.referenceName = name.substring(5);
            module.add(i);
        } else if (name.startsWith("java:comp/")) {
            i.referenceName = name.substring(5);
            comp.add(i);
        } else {
            logger.warning("config.invalid.referenceName.suffix", name);
        }
    }
}
