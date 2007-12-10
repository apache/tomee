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
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.MessageDestinationRef;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.ResourceLink;
import org.apache.openejb.jee.oejb3.EjbLink;

import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class ApplyOpenejbJar implements DynamicDeployer {

    public AppModule deploy(AppModule appModule) throws OpenEJBException {

        for (EjbModule ejbModule : appModule.getEjbModules()) {

            Map<String, EjbDeployment> ejbDeployments = ejbModule.getOpenejbJar().getDeploymentsByEjbName();

            for (EnterpriseBean enterpriseBean : ejbModule.getEjbJar().getEnterpriseBeans()) {

                // Get the OpenEJB deployment from openejb-jar.xml
                EjbDeployment ejbDeployment = ejbDeployments.get(enterpriseBean.getEjbName());

                // Copy all links over to mappedName

                for (ResourceRef ref : enterpriseBean.getResourceRef()) {
                    ResourceLink resourceLink = ejbDeployment.getResourceLink(ref.getName());
                    if (resourceLink != null && resourceLink.getResId() != null /* don't overwrite with null */) {
                        ref.setMappedName(resourceLink.getResId());
                    }
                }

                for (ResourceEnvRef ref : enterpriseBean.getResourceEnvRef()) {
                    ResourceLink resourceLink = ejbDeployment.getResourceLink(ref.getName());
                    if (resourceLink != null && resourceLink.getResId() != null /* don't overwrite with null */) {
                        ref.setMappedName(resourceLink.getResId());
                    }
                }

                for (MessageDestinationRef ref : enterpriseBean.getMessageDestinationRef()) {
                    ResourceLink resourceLink = ejbDeployment.getResourceLink(ref.getName());
                    if (resourceLink != null && resourceLink.getResId() != null /* don't overwrite with null */) {
                        ref.setMappedName(resourceLink.getResId());
                    }
                }

                for (EjbRef ref : enterpriseBean.getEjbRef()) {
                    EjbLink ejbLink = ejbDeployment.getEjbLink(ref.getName());
                    if (ejbLink != null && ejbLink.getDeployentId() != null /* don't overwrite with null */) {
                        ref.setMappedName(ejbLink.getDeployentId());
                    }
                }

                for (EjbLocalRef ref : enterpriseBean.getEjbLocalRef()) {
                    EjbLink ejbLink = ejbDeployment.getEjbLink(ref.getName());
                    if (ejbLink != null && ejbLink.getDeployentId() != null /* don't overwrite with null */) {
                        ref.setMappedName(ejbLink.getDeployentId());
                    }
                }
            }
        }

        return appModule;
    }
}
