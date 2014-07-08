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
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.MessageDestinationRef;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceUnitRef;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.ServiceRef;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoMappedName implements DynamicDeployer {

    private static final String MAPPED_NAME_PREFIX = "jndi:java:comp/geronimo/env/";

    public AppModule deploy(final AppModule appModule) throws OpenEJBException {
        mapReferences(appModule);
        return appModule;
    }

    private void mapReferences(final AppModule appModule) {
        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            mapReferences(ejbModule.getEjbJar());
        }
    }

    private void mapReferences(final EjbJar ejbJar) {
        if (ejbJar == null) {
            return;
        }

        for (final EnterpriseBean enterpriseBean : ejbJar.getEnterpriseBeans()) {
            for (final EjbRef ref : enterpriseBean.getEjbRef()) {
                // remap only corba references
                final String mappedName = ref.getMappedName();
                if (mappedName != null &&
                    (mappedName.startsWith("jndi:corbaloc") || mappedName.startsWith("jndi:corbaname"))) {
                    final String refName = ref.getEjbRefName();
                    ref.setMappedName(MAPPED_NAME_PREFIX + refName);
                }
                if (null == mappedName && ref.getEjbRefName().equals("ejb/MEJB")) {
                    ref.setMappedName("mejb/ejb/mgmt/MEJB");
                }
            }
            for (final MessageDestinationRef ref : enterpriseBean.getMessageDestinationRef()) {
                final String refName = ref.getMessageDestinationRefName();
                ref.setMappedName(MAPPED_NAME_PREFIX + refName);
            }
            for (final PersistenceContextRef ref : enterpriseBean.getPersistenceContextRef()) {
                final String refName = ref.getPersistenceContextRefName();
                ref.setMappedName(MAPPED_NAME_PREFIX + refName);
            }
            for (final PersistenceUnitRef ref : enterpriseBean.getPersistenceUnitRef()) {
                final String refName = ref.getPersistenceUnitRefName();
                ref.setMappedName(MAPPED_NAME_PREFIX + refName);
            }
            for (final ResourceRef ref : enterpriseBean.getResourceRef()) {
                final String refName = ref.getResRefName();
                ref.setMappedName(MAPPED_NAME_PREFIX + refName);
            }
            for (final ResourceEnvRef ref : enterpriseBean.getResourceEnvRef()) {
                final String refName = ref.getResourceEnvRefName();
                ref.setMappedName(MAPPED_NAME_PREFIX + refName);
            }
            for (final ServiceRef ref : enterpriseBean.getServiceRef()) {
                final String refName = ref.getServiceRefName();
                ref.setMappedName(MAPPED_NAME_PREFIX + refName);
            }
        }
    }

}
