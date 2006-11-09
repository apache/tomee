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

import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.EjbLocalReferenceInfo;
import org.apache.openejb.assembler.classic.EjbReferenceLocationInfo;
import org.apache.openejb.assembler.classic.EjbReferenceInfo;
import org.apache.openejb.assembler.classic.ResourceReferenceInfo;
import org.apache.openejb.assembler.classic.EnvEntryInfo;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.OpenEJBException;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * @version $Rev$ $Date$
 */
public class JndiEncInfoBuilder {

    private final Map<String, EnterpriseBeanInfo> beanInfos;

    public JndiEncInfoBuilder(Map<String, EnterpriseBeanInfo> beanInfos) {
        this.beanInfos = beanInfos;
    }

    public JndiEncInfo build(JndiConsumer jndiConsumer, String ejbName) throws OpenEJBException {

        JndiEncInfo jndi = new JndiEncInfo();

        /* Build Environment entries *****************/
        jndi.envEntries = buildEnvEntryInfos(jndiConsumer);

        /* Build Resource References *****************/
        jndi.resourceRefs = buildResourceRefInfos(jndiConsumer);

        jndi.ejbReferences = buildEjbRefInfos(jndiConsumer, ejbName);

        jndi.ejbLocalReferences = buildEjbLocalRefInfos(jndiConsumer, ejbName);

        return jndi;
    }

    private EjbLocalReferenceInfo[] buildEjbLocalRefInfos(JndiConsumer item, String referringComponent) throws OpenEJBException {
        List<EjbLocalReferenceInfo> infos = new ArrayList();
        for (EjbLocalRef ejb : item.getEjbLocalRef()) {
            EjbLocalReferenceInfo info = new EjbLocalReferenceInfo();

            info.homeType = ejb.getLocalHome();
            info.referenceName = ejb.getEjbRefName();
            info.location = new EjbReferenceLocationInfo();

            String ejbLink = ejb.getEjbLink();

            EnterpriseBeanInfo otherBean = (EnterpriseBeanInfo) beanInfos.get(ejbLink);
            if (otherBean == null) {
                String msg = ConfigurationFactory.messages.format("config.noBeanFound", ejb.getEjbRefName(), referringComponent);

                ConfigurationFactory.logger.fatal(msg);
                throw new OpenEJBException(msg);
            }
            info.location.ejbDeploymentId = otherBean.ejbDeploymentId;
            infos.add(info);
        }
        return infos.toArray(new EjbLocalReferenceInfo[]{});
    }

    private EjbReferenceInfo[] buildEjbRefInfos(JndiConsumer item, String referringComponent) throws OpenEJBException {
        List<EjbReferenceInfo> infos = new ArrayList();
        for (EjbRef ejb : item.getEjbRef()) {
            EjbReferenceInfo info = new EjbReferenceInfo();

            info.homeType = ejb.getHome();
            info.remoteType = ejb.getRemote();
            info.referenceName = ejb.getEjbRefName();
            info.location = new EjbReferenceLocationInfo();

            String ejbLink = ejb.getEjbLink();

            EnterpriseBeanInfo otherBean = (EnterpriseBeanInfo) beanInfos.get(ejbLink);
            if (otherBean == null) {
                String msg = ConfigurationFactory.messages.format("config.noBeanFound", ejb.getEjbRefName(), referringComponent);

                ConfigurationFactory.logger.fatal(msg);
                throw new OpenEJBException(msg);
            }
            info.location.ejbDeploymentId = otherBean.ejbDeploymentId;
            infos.add(info);
        }
        return infos.toArray(new EjbReferenceInfo[]{});
    }

    private ResourceReferenceInfo[] buildResourceRefInfos(JndiConsumer item) {
        List<ResourceReferenceInfo> infos = new ArrayList();
        for (ResourceRef res : item.getResourceRef()) {
            ResourceReferenceInfo info = new ResourceReferenceInfo();

            info.referenceAuth = res.getResAuth().toString();
            info.referenceName = res.getResRefName();
            info.referenceType = res.getResType();
            info.resourceID = res.getResLink();
            infos.add(info);
        }
        return infos.toArray(new ResourceReferenceInfo[]{});
    }

    private EnvEntryInfo[] buildEnvEntryInfos(JndiConsumer item) {
        List<EnvEntryInfo> infos = new ArrayList();
        for (EnvEntry env : item.getEnvEntry()) {
            EnvEntryInfo info = new EnvEntryInfo();

            info.name = env.getEnvEntryName();
            info.type = env.getEnvEntryType();
            info.value = env.getEnvEntryValue();

            infos.add(info);
        }
        return infos.toArray(new EnvEntryInfo[]{});
    }
}
