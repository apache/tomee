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
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.MessageDestinationRef;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.EjbLink;
import org.apache.openejb.jee.oejb3.ResourceLink;

import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class ApplyOpenejbJar implements DynamicDeployer {

    public AppModule deploy(final AppModule appModule) throws OpenEJBException {

        for (final EjbModule ejbModule : appModule.getEjbModules()) {

            final Map<String, EjbDeployment> ejbDeployments = ejbModule.getOpenejbJar().getDeploymentsByEjbName();

            for (final EnterpriseBean enterpriseBean : ejbModule.getEjbJar().getEnterpriseBeans()) {

                // Get the OpenEJB deployment from openejb-jar.xml
                final EjbDeployment ejbDeployment = ejbDeployments.get(enterpriseBean.getEjbName());

                enterpriseBean.setId(ejbDeployment.getDeploymentId());

                // Copy all links over to mappedName

                for (final ResourceRef ref : enterpriseBean.getResourceRef()) {
                    final ResourceLink resourceLink = ejbDeployment.getResourceLink(ref.getName());
                    if (resourceLink != null && resourceLink.getResId() != null /* don't overwrite with null */) {
                        ref.setMappedName(resourceLink.getResId());
                    }
                }

                for (final ResourceEnvRef ref : enterpriseBean.getResourceEnvRef()) {
                    final ResourceLink resourceLink = ejbDeployment.getResourceLink(ref.getName());
                    if (resourceLink != null && resourceLink.getResId() != null /* don't overwrite with null */) {
                        ref.setMappedName(resourceLink.getResId());
                    }
                }

                for (final MessageDestinationRef ref : enterpriseBean.getMessageDestinationRef()) {
                    final ResourceLink resourceLink = ejbDeployment.getResourceLink(ref.getName());
                    if (resourceLink != null && resourceLink.getResId() != null /* don't overwrite with null */) {
                        ref.setMappedName(resourceLink.getResId());
                    }
                }

                for (final EjbRef ref : enterpriseBean.getEjbRef()) {
                    final EjbLink ejbLink = ejbDeployment.getEjbLink(ref.getName());
                    if (ejbLink != null && ejbLink.getDeployentId() != null /* don't overwrite with null */) {
                        ref.setMappedName(ejbLink.getDeployentId());
                    }
                }

                for (final EjbLocalRef ref : enterpriseBean.getEjbLocalRef()) {
                    final EjbLink ejbLink = ejbDeployment.getEjbLink(ref.getName());
                    if (ejbLink != null && ejbLink.getDeployentId() != null /* don't overwrite with null */) {
                        ref.setMappedName(ejbLink.getDeployentId());
                    }
                }
            }
        }


        return appModule;
    }
}
