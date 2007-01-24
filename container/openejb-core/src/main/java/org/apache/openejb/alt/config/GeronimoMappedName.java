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
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.MessageDestinationRef;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceUnitRef;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.ServiceRef;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoMappedName implements DynamicDeployer {

    private static final String MAPPED_NAME_PREFIX = "jndi:java:comp/geronimo/env/";

    public GeronimoMappedName() {
    }

    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        mapReferences(appModule);
        return appModule;
    }

    private void mapReferences(AppModule appModule) {
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            mapReferences(ejbModule.getEjbJar());
        }
    }

    private void mapReferences(EjbJar ejbJar) {
        if (ejbJar == null){
            return;
        }

        for (EnterpriseBean enterpriseBean : ejbJar.getEnterpriseBeans()) {
            for (MessageDestinationRef ref : enterpriseBean.getMessageDestinationRef()) {
                String refName = ref.getMessageDestinationRefName();
                ref.setMappedName(MAPPED_NAME_PREFIX + refName);
            }
            for (PersistenceContextRef ref : enterpriseBean.getPersistenceContextRef()) {
                String refName = ref.getPersistenceContextRefName();
                ref.setMappedName(MAPPED_NAME_PREFIX + refName);
            }
            for (PersistenceUnitRef ref : enterpriseBean.getPersistenceUnitRef()) {
                String refName = ref.getPersistenceUnitRefName();
                ref.setMappedName(MAPPED_NAME_PREFIX + refName);
            }
            for (ResourceRef ref : enterpriseBean.getResourceRef()) {
                String refName = ref.getResRefName();
                ref.setMappedName(MAPPED_NAME_PREFIX + refName);
            }
            for (ResourceEnvRef ref : enterpriseBean.getResourceEnvRef()) {
                String refName = ref.getResourceEnvRefName();
                ref.setMappedName(MAPPED_NAME_PREFIX + refName);
            }
            for (ServiceRef ref : enterpriseBean.getServiceRef()) {
                String refName = ref.getServiceRefName();
                ref.setMappedName(MAPPED_NAME_PREFIX + refName);
            }
        }
    }

}
