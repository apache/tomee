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
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EjbLocalReferenceInfo;
import org.apache.openejb.assembler.classic.EjbReferenceInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.EnvEntryInfo;
import org.apache.openejb.assembler.classic.InjectionInfo;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.classic.PersistenceContextReferenceInfo;
import org.apache.openejb.assembler.classic.PersistenceUnitReferenceInfo;
import org.apache.openejb.assembler.classic.ReferenceLocationInfo;
import org.apache.openejb.assembler.classic.ResourceEnvReferenceInfo;
import org.apache.openejb.assembler.classic.ResourceReferenceInfo;
import org.apache.openejb.assembler.classic.ServiceReferenceInfo;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.LinkResolver;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.Injectable;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.JndiReference;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceContextType;
import org.apache.openejb.jee.PersistenceUnitRef;
import org.apache.openejb.jee.Property;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.ServiceRef;
import org.apache.openejb.jee.MessageDestinationRef;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.ResAuth;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.ResourceLink;
import org.apache.openejb.jee.oejb3.EjbLink;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.TreeMap;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @version $Rev$ $Date$
 */
public class JndiEncInfoBuilder {
    public static void initJndiReferences(AppModule appModule, AppInfo appInfo) throws OpenEJBException {
        // index ejb modules
        Map<String, EjbModule> ejbModules = new TreeMap<String, EjbModule>();
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            ejbModules.put(ejbModule.getModuleId(), ejbModule);
        }

        // Create the JNDI info builder
        JndiEncInfoBuilder jndiEncInfoBuilder = new JndiEncInfoBuilder(appInfo.ejbJars);

        // Build the JNDI tree for each ejb
        for (EjbJarInfo ejbJar : appInfo.ejbJars) {
            EjbModule ejbModule = ejbModules.get(ejbJar.moduleId);

            // index jaxb objects
            Map<String, EnterpriseBean> beanData = new TreeMap<String, EnterpriseBean>();
            for (EnterpriseBean enterpriseBean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                beanData.put(enterpriseBean.getEjbName(), enterpriseBean);
            }
            Map<String, EjbDeployment> ejbDeployments = ejbModule.getOpenejbJar().getDeploymentsByEjbName();

            for (EnterpriseBeanInfo beanInfo : ejbJar.enterpriseBeans) {
                String ejbName = beanInfo.ejbName;

                // Get the ejb-jar.xml object
                EnterpriseBean enterpriseBean = beanData.get(ejbName);

                // Get the OpenEJB deployment
                EjbDeployment ejbDeployment = ejbDeployments.get(ejbName);

                // build the tree
                initJndiReferences(enterpriseBean, ejbDeployment, beanInfo, jndiEncInfoBuilder, ejbJar.moduleId);
            }
        }
    }


    private static void initJndiReferences(EnterpriseBean enterpriseBean, EjbDeployment ejbDeployment, EnterpriseBeanInfo beanInfo, JndiEncInfoBuilder jndiEncInfoBuilder, String moduleId) throws OpenEJBException {
        // Link all the resource refs
        for (ResourceRef res : enterpriseBean.getResourceRef()) {
            ResourceLink resourceLink = ejbDeployment.getResourceLink(res.getResRefName());
            if (resourceLink != null && resourceLink.getResId() != null /* don't overwrite with null */) {
                res.setMappedName(resourceLink.getResId());
            }
        }

        for (ResourceEnvRef ref : enterpriseBean.getResourceEnvRef()) {
            ResourceLink resourceLink = ejbDeployment.getResourceLink(ref.getResourceEnvRefName());
            if (resourceLink != null && resourceLink.getResId() != null /* don't overwrite with null */) {
                ref.setMappedName(resourceLink.getResId());
            }
        }

        for (MessageDestinationRef ref : enterpriseBean.getMessageDestinationRef()) {
            ResourceLink resourceLink = ejbDeployment.getResourceLink(ref.getMessageDestinationRefName());
            if (resourceLink != null && resourceLink.getResId() != null /* don't overwrite with null */) {
                ref.setMappedName(resourceLink.getResId());
            }
        }

        // Link all the ejb refs
        for (EjbRef ejbRef : enterpriseBean.getEjbRef()) {
            EjbLink ejbLink = ejbDeployment.getEjbLink(ejbRef.getEjbRefName());
            if (ejbLink != null && ejbLink.getDeployentId() != null /* don't overwrite with null */) {
                ejbRef.setMappedName(ejbLink.getDeployentId());
            }
        }

        // Build the JNDI info tree for the EJB
        JndiEncInfo jndi = jndiEncInfoBuilder.build(enterpriseBean, beanInfo.ejbName, moduleId);
        beanInfo.jndiEnc = jndi;
    }

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, JndiEncInfoBuilder.class);
    protected static final Messages messages = new Messages(JndiEncInfoBuilder.class);

    private final Map<String,EnterpriseBeanInfo> allDeployments = new TreeMap<String,EnterpriseBeanInfo>();

    private final LinkResolver<String> ejbLinkResolver = new LinkResolver<String>();
    private final Map<Interfaces, String> remoteInterfaces = new TreeMap<Interfaces, String>();
    private final Map<Interfaces, String> localInterfaces = new TreeMap<Interfaces, String>();

    public JndiEncInfoBuilder(EjbJarInfo... ejbJarInfos) {
        this(Arrays.asList(ejbJarInfos));
    }

    public JndiEncInfoBuilder(Collection<EjbJarInfo> ejbJarInfos) {
        for (EjbJarInfo ejbJarInfo : ejbJarInfos) {
            for (EnterpriseBeanInfo bean : ejbJarInfo.enterpriseBeans) {
                index(ejbJarInfo.moduleId, bean);
            }
        }
    }

    private void index(String moduleId, EnterpriseBeanInfo bean) {
        // All deployments: deploymentId -> bean
        allDeployments.put(bean.ejbDeploymentId, bean);

        // add to the link resolver
        ejbLinkResolver.add(moduleId, bean.ejbName, bean.ejbDeploymentId);

        // Remote: Interfaces(home,object) -> deploymentId
        if (bean.remote != null) {
            remoteInterfaces.put(new Interfaces(bean.home, bean.remote), bean.ejbDeploymentId);
            remoteInterfaces.put(new Interfaces(bean.remote), bean.ejbDeploymentId);
        }
        for (String businessRemote : bean.businessRemote) {
            remoteInterfaces.put(new Interfaces(businessRemote), bean.ejbDeploymentId);
        }

        // Local: Interfaces(home,object) -> deploymentId
        if (bean.local != null) {
            localInterfaces.put(new Interfaces(bean.localHome, bean.local), bean.ejbDeploymentId);
            localInterfaces.put(new Interfaces(bean.local), bean.ejbDeploymentId);
        }
        for (String businessLocal : bean.businessLocal) {
            localInterfaces.put(new Interfaces(businessLocal), bean.ejbDeploymentId);
        }
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

        // URLs resource references become env entried
        jndi.envEntries.addAll(buildUrlRefInfos(jndiConsumer));

        /* Build Resource References *****************/
        jndi.resourceRefs.addAll(buildResourceRefInfos(jndiConsumer));

        /* Build Resource Environment References *****************/
        jndi.resourceEnvRefs.addAll(buildResourceEnvRefInfos(jndiConsumer));

        buildAmbiguousEjbRefInfos(jndiConsumer, moduleUri);

        jndi.ejbReferences.addAll(buildEjbRefInfos(jndiConsumer, ejbName, moduleUri));

        jndi.ejbLocalReferences.addAll(buildEjbLocalRefInfos(jndiConsumer, ejbName, moduleUri));

        jndi.persistenceUnitRefs.addAll(buildPersistenceUnitRefInfos(jndiConsumer));

        jndi.persistenceContextRefs.addAll(buildPersistenceContextRefInfos(jndiConsumer));

        jndi.serviceRefs.addAll(buildServiceRefInfos(jndiConsumer));

        return jndi;
    }

    private List<ServiceReferenceInfo> buildServiceRefInfos(JndiConsumer jndiConsumer) {
        ArrayList<ServiceReferenceInfo> infos = new ArrayList<ServiceReferenceInfo>();
        for (ServiceRef ref : jndiConsumer.getServiceRef()) {
            ServiceReferenceInfo info = new ServiceReferenceInfo();
            info.referenceName = ref.getName();
            info.location = buildLocationInfo(ref);
            info.targets.addAll(buildInjectionInfos(ref));
            infos.add(info);
        }
        return infos;
    }

    private List<PersistenceUnitReferenceInfo> buildPersistenceUnitRefInfos(JndiConsumer jndiConsumer) {
        ArrayList<PersistenceUnitReferenceInfo> infos = new ArrayList<PersistenceUnitReferenceInfo>();
        for (PersistenceUnitRef puRef : jndiConsumer.getPersistenceUnitRef()) {
            PersistenceUnitReferenceInfo info = new PersistenceUnitReferenceInfo();
            info.referenceName = puRef.getPersistenceUnitRefName();
            info.persistenceUnitName = puRef.getPersistenceUnitName();
            info.location = buildLocationInfo(puRef);
            info.targets.addAll(buildInjectionInfos(puRef));
            infos.add(info);
        }
        return infos;
    }

    private List<PersistenceContextReferenceInfo> buildPersistenceContextRefInfos(JndiConsumer jndiConsumer) {
        ArrayList<PersistenceContextReferenceInfo> infos = new ArrayList<PersistenceContextReferenceInfo>();

        for (PersistenceContextRef contextRef : jndiConsumer.getPersistenceContextRef()) {
            PersistenceContextReferenceInfo info = new PersistenceContextReferenceInfo();
            info.referenceName = contextRef.getPersistenceContextRefName();
            info.persistenceUnitName = contextRef.getPersistenceUnitName();
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

    private void buildAmbiguousEjbRefInfos(JndiConsumer jndiConsumer, URI moduleId) throws OpenEJBException {
        ArrayList<EjbRef> ejbRefs = new ArrayList<EjbRef>(jndiConsumer.getEjbRef());
        for (EjbRef ejb : ejbRefs) {
            if (ejb.getRefType() != EjbRef.Type.UNKNOWN) continue;

            String interfce = ejb.getRemote();

            EnterpriseBeanInfo otherBean = null;
            if (ejb.getEjbLink() != null) {
                String deploymentId = ejbLinkResolver.resolveLink(ejb.getEjbLink(), moduleId);
                if (deploymentId != null) {
                    otherBean = allDeployments.get(deploymentId);
                }
            }

            if (otherBean != null) {
                if (otherBean.businessRemote.contains(interfce)) {
                    ejb.setRefType(EjbRef.Type.REMOTE);
                } else {
                    ejb.setRefType(EjbRef.Type.LOCAL);
                    jndiConsumer.getEjbRef().remove(ejb);
                    jndiConsumer.getEjbLocalRef().add(new EjbLocalRef(ejb));
                }
            } else {
                if (remoteInterfaces.containsKey(new Interfaces(ejb.getHome(), ejb.getRemote()))) {
                    ejb.setRefType(EjbRef.Type.REMOTE);
                } else {
                    ejb.setRefType(EjbRef.Type.LOCAL);
                    jndiConsumer.getEjbRef().remove(ejb);
                    jndiConsumer.getEjbLocalRef().add(new EjbLocalRef(ejb));
                }
            }
        }
    }

    private List<EjbLocalReferenceInfo> buildEjbLocalRefInfos(JndiConsumer item, String referringComponent, URI moduleId) throws OpenEJBException {
        List<EjbLocalReferenceInfo> infos = new ArrayList<EjbLocalReferenceInfo>();
        for (EjbLocalRef ejb : item.getEjbLocalRef()) {
            EjbLocalReferenceInfo info = new EjbLocalReferenceInfo();

            info.homeType = ejb.getLocalHome();
            info.localType = ejb.getLocal();
            info.referenceName = ejb.getEjbRefName();

            // assign location to a global jndi name
            info.location = buildLocationInfo(ejb);

            if (info.location == null) {
                // we didn't have a global ref, try mapped name, ejb link or auto interface matching
                if (ejb.getMappedName() != null && !ejb.getMappedName().equals("")) {
                    // mapped name is the deployment id
                    info.ejbDeploymentId = ejb.getMappedName();
                } else if (ejb.getEjbLink() != null && !ejb.getEjbLink().equals(""))  {
                    String deploymentId = ejbLinkResolver.resolveLink(ejb.getEjbLink(), moduleId);

                    // didn't find an ejb
                    if (deploymentId == null) {
                        String msg = messages.format("config.noBeanFoundEjbLink", ejb.getEjbRefName(), referringComponent, ejb.getEjbLink());
                        logger.fatal(msg);
                        throw new OpenEJBException(msg);
                    }
                    info.ejbDeploymentId = deploymentId;
                } else {
                    String deploymentId = localInterfaces.get(new Interfaces(ejb.getLocalHome(), ejb.getLocal()));

                    // didn't find an ejb
                    if (deploymentId == null) {
                        String msg = messages.format("config.noBeanFound", ejb.getEjbRefName(), referringComponent);
                        logger.fatal(msg);
                        throw new OpenEJBException(msg);
                    }
                    info.ejbDeploymentId = deploymentId;
                }
            }

            info.targets.addAll(buildInjectionInfos(ejb));
            infos.add(info);
        }
        return infos;
    }

    private List<EjbReferenceInfo> buildEjbRefInfos(JndiConsumer jndi, String referringComponent, URI moduleId) throws OpenEJBException {
        List<EjbReferenceInfo> infos = new ArrayList<EjbReferenceInfo>();
        for (EjbRef ejb : jndi.getEjbRef()) {
            EjbReferenceInfo info = new EjbReferenceInfo();

            info.homeType = ejb.getHome();
            info.remoteType = ejb.getRemote();
            info.referenceName = ejb.getEjbRefName();

            // assign location to a global jndi name
            info.location = buildLocationInfo(ejb);

            if (info.location == null) {
                // we didn't have a global ref, try mapped name, ejb link or auto interface matching
                if (ejb.getMappedName() != null && !ejb.getMappedName().equals("")) {
                    // mapped name is the deployment id
                    info.ejbDeploymentId = ejb.getMappedName();

                    // if the deployment is not in the application, it is an external
                    // reference and will need a cross class loader proxy
                    if (!allDeployments.containsKey(info.ejbDeploymentId)) {
                        info.externalReference = true;
                    }
                } else if (ejb.getEjbLink() != null && !ejb.getEjbLink().equals(""))  {
                    String deploymentId = ejbLinkResolver.resolveLink(ejb.getEjbLink(), moduleId);

                    // didn't find an ejb
                    if (deploymentId == null) {
                        String msg = messages.format("config.noBeanFoundEjbLink", ejb.getEjbRefName(), referringComponent, ejb.getEjbLink());
                        logger.fatal(msg);
                        throw new OpenEJBException(msg);
                    }
                    info.ejbDeploymentId = deploymentId;
                } else {
                    String deploymentId = remoteInterfaces.get(new Interfaces(ejb.getHome(), ejb.getRemote()));

                    // didn't find an ejb
                    if (deploymentId == null) {
                        String msg = messages.format("config.noBeanFound", ejb.getEjbRefName(), referringComponent);
                        logger.fatal(msg);
                        throw new OpenEJBException(msg);
                    }
                    info.ejbDeploymentId = deploymentId;
                }
            }

            info.targets.addAll(buildInjectionInfos(ejb));
            infos.add(info);
        }
        return infos;
    }

    private List<ResourceReferenceInfo> buildResourceRefInfos(JndiConsumer item) {
        List<ResourceReferenceInfo> infos = new ArrayList<ResourceReferenceInfo>();
        for (ResourceRef res : item.getResourceRef()) {
            // skip URLs which are converted to env entries
            if (res.getResType().equals("java.net.URL")) {
                continue;
            }

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

    private List<EnvEntryInfo> buildUrlRefInfos(JndiConsumer item) {
        List<EnvEntryInfo> infos = new ArrayList<EnvEntryInfo>();
        for (ResourceRef res : item.getResourceRef()) {
            // only process URLs
            if (!res.getResType().equals("java.net.URL")) {
                continue;
            }

            // ignore env entries without a mapped name
            if (res.getMappedName() == null) {
                continue;
            }
            
            EnvEntryInfo info = new EnvEntryInfo();

            info.name = res.getResRefName();
            info.type = res.getResType();
            info.value = res.getMappedName();
            info.location = buildLocationInfo(res);
            info.targets.addAll(buildInjectionInfos(res));
            infos.add(info);
        }
        return infos;
    }

    private ReferenceLocationInfo buildLocationInfo(JndiReference reference) {
        String mappedName = reference.getMappedName();
        if (mappedName == null || !mappedName.startsWith("jndi:")) {
            return null;
        }
        ReferenceLocationInfo location = new ReferenceLocationInfo();
        location.jndiName = mappedName.replaceFirst("^jndi:", "");
        return location;
    }

    private Collection<? extends InjectionInfo> buildInjectionInfos(Injectable injectable) {
        ArrayList<InjectionInfo> infos = new ArrayList<InjectionInfo>();
        for (InjectionTarget target : injectable.getInjectionTarget()) {
            InjectionInfo info = new InjectionInfo();
            info.className = target.getInjectionTargetClass();
            info.propertyName = target.getInjectionTargetName();
            infos.add(info);
        }
        return infos;
    }

    private static class Interfaces implements Comparable {
        private final String homeInterface;
        private final String objectInterface;

        public Interfaces(String objectInterface) {
            if (objectInterface == null) throw new NullPointerException("objectInterface is null");
            this.homeInterface = "<none>";
            this.objectInterface = objectInterface;
        }

        public Interfaces(String homeInterface, String objectInterface) {
            if (homeInterface == null) homeInterface = "<none>";
            if (objectInterface == null) throw new NullPointerException("objectInterface is null");
            this.homeInterface = homeInterface;
            this.objectInterface = objectInterface;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Interfaces that = (Interfaces) o;

            return homeInterface.equals(that.homeInterface) && objectInterface.equals(that.objectInterface);
        }

        public int hashCode() {
            int result;
            result = homeInterface.hashCode();
            result = 31 * result + objectInterface.hashCode();
            return result;
        }

        public int compareTo(Object o) {
            if (this == o) return 0;

            Interfaces that = (Interfaces) o;
            return toString().compareTo(that.toString());
        }

        public String toString() {
            return homeInterface + ":" + objectInterface;
        }
    }
}
