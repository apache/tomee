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
package org.apache.openejb.alt.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.EjbLocalReferenceInfo;
import org.apache.openejb.assembler.classic.EjbReferenceInfo;
import org.apache.openejb.assembler.classic.EjbReferenceLocationInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.EnvEntryInfo;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.classic.ResourceReferenceInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.PersistenceUnitInfo;
import org.apache.openejb.assembler.classic.InjectionInfo;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.PersistenceUnitRef;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.Injectable;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.io.File;

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
        byInterfaces.put("r="+bean.remote+":"+bean.home, bean);
        byInterfaces.put("r="+bean.businessRemote+":"+null, bean);
        byInterfaces.put("l="+bean.local+":"+bean.localHome, bean);
        byInterfaces.put("l="+bean.businessLocal+":"+null, bean);

        byEjbName.put(bean.ejbName, bean);
        // TODO: DMB: this path part should actually *only* be relative to the app archive,
        // this way will work to find them :)
        File file = new File(bean.codebase);
        String path = file.getName()+"#"+bean.ejbName;
        byEjbName.put(path, bean);
        file = file.getParentFile();
        while (file != null){
            path = file.getName() +"/"+ path;
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

        buildAmbiguousEjbRefInfos(jndi, jndiConsumer, ejbName);

        jndi.ejbReferences.addAll(buildEjbRefInfos(jndiConsumer, ejbName));

        jndi.ejbLocalReferences.addAll(buildEjbLocalRefInfos(jndiConsumer, ejbName));

        jndi.persistenceUnitRefs.addAll(buildPersistenceUnitRefInfos(jndiConsumer));
        return jndi;
    }

    private List<PersistenceUnitInfo> buildPersistenceUnitRefInfos(JndiConsumer jndiConsumer) {
        ArrayList<PersistenceUnitInfo> infos = new ArrayList<PersistenceUnitInfo>();
        for (PersistenceUnitRef puRef : jndiConsumer.getPersistenceUnitRef()) {
        	PersistenceUnitInfo info = new PersistenceUnitInfo();
            info.referenceName = puRef.getPersistenceUnitRefName();
            info.persistenceUnitName = puRef.getPersistenceUnitName();            
            infos.add(info);
        }
        return infos;       
    }

    private void buildAmbiguousEjbRefInfos(JndiEncInfo jndi, JndiConsumer jndiConsumer, String referringComponent) throws OpenEJBException {
        ArrayList<EjbRef> ejbRefs = new ArrayList<EjbRef>(jndiConsumer.getEjbRef());
        for (EjbRef ejb : ejbRefs) {
            if (ejb.getType() != EjbRef.Type.UNKNOWN) continue;

            String interfce = ejb.getRemote();

            EnterpriseBeanInfo otherBean = null;

            if (ejb.getEjbLink() != null) {
                String ejbLink = ejb.getEjbLink();
                otherBean = byEjbName.get(ejbLink);
            }

            if (otherBean != null){
                if (interfce.equals(otherBean.businessRemote)){
                    ejb.setType(EjbRef.Type.REMOTE);
                }  else {
                    ejb.setType(EjbRef.Type.LOCAL);
                    jndiConsumer.getEjbRef().remove(ejb);
                    jndiConsumer.getEjbLocalRef().add(new EjbLocalRef(ejb));
                }
            } else {
                if (byInterfaces.get("r="+ejb.getRemote()+":"+ejb.getHome()) != null){
                    ejb.setType(EjbRef.Type.REMOTE);
                } else {
                    ejb.setType(EjbRef.Type.LOCAL);
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
            info.location = new EjbReferenceLocationInfo();


            EnterpriseBeanInfo otherBean = null;

            if (ejb.getEjbLink() != null) {
                String ejbLink = ejb.getEjbLink();
                otherBean = byEjbName.get(ejbLink);
            } else {
                otherBean = byInterfaces.get("l="+ejb.getLocal()+":"+ejb.getLocalHome());
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
            info.location.ejbDeploymentId = otherBean.ejbDeploymentId;

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
            info.location = new EjbReferenceLocationInfo();

            EnterpriseBeanInfo otherBean = null;

            if (ejb.getEjbLink() != null) {
                String ejbLink = ejb.getEjbLink();
                otherBean = byEjbName.get(ejbLink);
            } else {
                otherBean = byInterfaces.get("r="+ejb.getRemote()+":"+ejb.getHome());
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
            info.location.ejbDeploymentId = otherBean.ejbDeploymentId;

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
            info.targets.addAll(buildInjectionInfos(env));
            infos.add(info);
        }
        return infos;
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
