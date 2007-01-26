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
import org.apache.openejb.assembler.classic.MessageDestinationReferenceInfo;
import org.apache.openejb.assembler.classic.ServiceReferenceInfo;
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
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class JndiEncInfoBuilder {

    public static final Logger logger = Logger.getInstance("OpenEJB.startup", "org.apache.openejb.util.resources");
    protected static final Messages messages = new Messages("org.apache.openejb.util.resources");

    private final Map<String, EnterpriseBeanInfo> byEjbName = new HashMap<String, EnterpriseBeanInfo>();
    private final Map<String, EnterpriseBeanInfo> byInterfaces = new HashMap<String, EnterpriseBeanInfo>();

    public JndiEncInfoBuilder(Collection<EnterpriseBeanInfo> ejbBeanInfos, String withoutThisConstructorsClash) {
        for (EnterpriseBeanInfo bean : ejbBeanInfos) {
            index(bean);
        }
    }

    public JndiEncInfoBuilder(Collection<EjbJarInfo> ejbJarInfos) {
        for (EjbJarInfo ejbJarInfo : ejbJarInfos) {
            for (EnterpriseBeanInfo bean : ejbJarInfo.enterpriseBeans) {
                index(bean);
            }
        }
    }

    private void index(EnterpriseBeanInfo bean) {
        byInterfaces.put("r=" + bean.remote + ":" + bean.home, bean);
        byInterfaces.put("r=" + bean.businessRemote + ":" + null, bean);
        byInterfaces.put("l=" + bean.local + ":" + bean.localHome, bean);
        byInterfaces.put("l=" + bean.businessLocal + ":" + null, bean);

        byEjbName.put(bean.ejbName, bean);
        // TODO: DMB: this path part should actually *only* be relative to the app archive,
        // this way will work to find them :)
        File file = new File(bean.codebase);
        String path = file.getName() + "#" + bean.ejbName;
        byEjbName.put(path, bean);
        file = file.getParentFile();
        while (file != null) {
            path = file.getName() + "/" + path;
            byEjbName.put(path, bean);
            file = file.getParentFile();
        }
    }

    public JndiEncInfo build(JndiConsumer jndiConsumer, String ejbName) throws OpenEJBException {

        JndiEncInfo jndi = new JndiEncInfo();

        /* Build Environment entries *****************/
        jndi.envEntries.addAll(buildEnvEntryInfos(jndiConsumer));

        /* Build Resource References *****************/
        jndi.resourceRefs.addAll(buildResourceRefInfos(jndiConsumer));

        /* Build Resource Environment References *****************/
        jndi.resourceEnvRefs.addAll(buildResourceEnvRefInfos(jndiConsumer));

        buildAmbiguousEjbRefInfos(jndi, jndiConsumer, ejbName);

        jndi.ejbReferences.addAll(buildEjbRefInfos(jndiConsumer, ejbName));

        jndi.ejbLocalReferences.addAll(buildEjbLocalRefInfos(jndiConsumer, ejbName));

        jndi.persistenceUnitRefs.addAll(buildPersistenceUnitRefInfos(jndiConsumer));

        jndi.persistenceContextRefs.addAll(buildPersistenceContextRefInfos(jndiConsumer));

        jndi.messageDestinationRefs.addAll(buildMessageDestinationRefInfos(jndiConsumer));

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

    private List<MessageDestinationReferenceInfo> buildMessageDestinationRefInfos(JndiConsumer jndiConsumer) {
        ArrayList<MessageDestinationReferenceInfo> infos = new ArrayList<MessageDestinationReferenceInfo>();
        for (MessageDestinationRef ref : jndiConsumer.getMessageDestinationRef()) {
            MessageDestinationReferenceInfo info = new MessageDestinationReferenceInfo();
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

    private void buildAmbiguousEjbRefInfos(JndiEncInfo sjndi, JndiConsumer jndiConsumer, String referringComponent) throws OpenEJBException {
        ArrayList<EjbRef> ejbRefs = new ArrayList<EjbRef>(jndiConsumer.getEjbRef());
        for (EjbRef ejb : ejbRefs) {
            if (ejb.getRefType() != EjbRef.Type.UNKNOWN) continue;

            String interfce = ejb.getRemote();

            EnterpriseBeanInfo otherBean = null;

            if (ejb.getEjbLink() != null) {
                String ejbLink = ejb.getEjbLink();
                otherBean = byEjbName.get(ejbLink);
            }

            if (otherBean != null) {
                if (interfce.equals(otherBean.businessRemote)) {
                    ejb.setRefType(EjbRef.Type.REMOTE);
                } else {
                    ejb.setRefType(EjbRef.Type.LOCAL);
                    jndiConsumer.getEjbRef().remove(ejb);
                    jndiConsumer.getEjbLocalRef().add(new EjbLocalRef(ejb));
                }
            } else {
                if (byInterfaces.get("r=" + ejb.getRemote() + ":" + ejb.getHome()) != null) {
                    ejb.setRefType(EjbRef.Type.REMOTE);
                } else {
                    ejb.setRefType(EjbRef.Type.LOCAL);
                    jndiConsumer.getEjbRef().remove(ejb);
                    jndiConsumer.getEjbLocalRef().add(new EjbLocalRef(ejb));
                }
            }
        }
    }

    private List<EjbLocalReferenceInfo> buildEjbLocalRefInfos(JndiConsumer item, String referringComponent) throws OpenEJBException {
        List<EjbLocalReferenceInfo> infos = new ArrayList<EjbLocalReferenceInfo>();
        for (EjbLocalRef ejb : item.getEjbLocalRef()) {
            EjbLocalReferenceInfo info = new EjbLocalReferenceInfo();

            info.homeType = ejb.getLocalHome();
            info.referenceName = ejb.getEjbRefName();

            info.location = buildLocationInfo(ejb);

            if (info.location == null) {
                EnterpriseBeanInfo otherBean = null;

                if (ejb.getEjbLink() != null) {
                    String ejbLink = ejb.getEjbLink();
                    otherBean = byEjbName.get(ejbLink);
                } else {
                    otherBean = byInterfaces.get("l=" + ejb.getLocal() + ":" + ejb.getLocalHome());
                }

                if (otherBean == null) {
                    String msg;
                    if (ejb.getEjbLink() == null) {
                        msg = messages.format("config.noBeanFound", ejb.getEjbRefName(), referringComponent);
                    } else {
                        msg = messages.format("config.noBeanFoundEjbLink", ejb.getEjbRefName(), referringComponent, ejb.getEjbLink());
                    }

                    logger.fatal(msg);
                    throw new OpenEJBException(msg);
                }
                info.ejbDeploymentId = otherBean.ejbDeploymentId;
            }
            info.targets.addAll(buildInjectionInfos(ejb));
            infos.add(info);
        }
        return infos;
    }

    private List<EjbReferenceInfo> buildEjbRefInfos(JndiConsumer item, String referringComponent) throws OpenEJBException {
        List<EjbReferenceInfo> infos = new ArrayList<EjbReferenceInfo>();
        for (EjbRef ejb : item.getEjbRef()) {
            EjbReferenceInfo info = new EjbReferenceInfo();

            info.homeType = ejb.getHome();
            info.remoteType = ejb.getRemote();
            info.referenceName = ejb.getEjbRefName();

            info.location = buildLocationInfo(ejb);

            if (info.location == null) {
                EnterpriseBeanInfo otherBean = null;

                if (ejb.getEjbLink() != null) {
                    String ejbLink = ejb.getEjbLink();
                    otherBean = byEjbName.get(ejbLink);
                } else {
                    otherBean = byInterfaces.get("r=" + ejb.getRemote() + ":" + ejb.getHome());
                }

                if (otherBean == null) {
                    String msg;
                    if (ejb.getEjbLink() == null) {
                        msg = messages.format("config.noBeanFound", ejb.getEjbRefName(), referringComponent);
                    } else {
                        msg = messages.format("config.noBeanFoundEjbLink", ejb.getEjbRefName(), referringComponent, ejb.getEjbLink());
                    }

                    logger.fatal(msg);
                    throw new OpenEJBException(msg);
                }
                info.ejbDeploymentId = otherBean.ejbDeploymentId;
            }

            info.targets.addAll(buildInjectionInfos(ejb));
            infos.add(info);
        }
        return infos;
    }

    private List<ResourceReferenceInfo> buildResourceRefInfos(JndiConsumer item) {
        List<ResourceReferenceInfo> infos = new ArrayList<ResourceReferenceInfo>();
        for (ResourceRef res : item.getResourceRef()) {
            ResourceReferenceInfo info = new ResourceReferenceInfo();

            info.referenceAuth = res.getResAuth().toString();
            info.referenceName = res.getResRefName();
            info.referenceType = res.getResType();
            info.resourceID = res.getResLink();
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
            info.location = buildLocationInfo(res);
            info.targets.addAll(buildInjectionInfos(res));
            infos.add(info);
        }
        return infos;
    }

    private List<EnvEntryInfo> buildEnvEntryInfos(JndiConsumer item) {
        List<EnvEntryInfo> infos = new ArrayList<EnvEntryInfo>();
        for (EnvEntry env : item.getEnvEntry()) {
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

}
