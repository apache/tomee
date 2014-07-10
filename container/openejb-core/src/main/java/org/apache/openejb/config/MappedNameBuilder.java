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
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.Jndi;
import org.apache.openejb.jee.oejb3.OpenejbJar;

import java.util.Map;

public class MappedNameBuilder implements DynamicDeployer {
    public AppModule deploy(final AppModule appModule) throws OpenEJBException {
        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            final OpenejbJar openejbJar = ejbModule.getOpenejbJar();
            if (openejbJar == null) {
                return appModule;
            }

            final Map<String, EjbDeployment> ejbDeployments = openejbJar.getDeploymentsByEjbName();
            for (final EnterpriseBean enterpriseBean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                final EjbDeployment ejbDeployment = ejbDeployments.get(enterpriseBean.getEjbName());

                if (ejbDeployment == null) {
                    continue;
                }

                final String mappedName = enterpriseBean.getMappedName();

                if (mappedName != null && mappedName.length() > 0) {
                    ejbDeployment.getJndi().add(new Jndi(mappedName, "Remote"));
                }
            }
        }

        return appModule;
    }
}
