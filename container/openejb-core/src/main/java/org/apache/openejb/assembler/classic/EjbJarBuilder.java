/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.assembler.classic;

import java.util.HashMap;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.CoreDeploymentInfo;

/**
 * @version $Revision$ $Date$
 */
public class EjbJarBuilder {
    private final ClassLoader classLoader;

    public EjbJarBuilder(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public HashMap<String, DeploymentInfo> build(EjbJarInfo ejbJar) throws OpenEJBException {
        HashMap<String, DeploymentInfo> deployments = new HashMap();
        EnterpriseBeanInfo[] ejbs = ejbJar.enterpriseBeans;
        for (int j = 0; j < ejbs.length; j++) {
            EnterpriseBeanInfo ejbInfo = ejbs[j];
            EnterpriseBeanBuilder deploymentBuilder = new EnterpriseBeanBuilder(classLoader, ejbInfo);
            CoreDeploymentInfo deployment = (CoreDeploymentInfo) deploymentBuilder.build();
            deployment.setJarPath(ejbJar.jarPath);
            deployments.put(ejbInfo.ejbDeploymentId, deployment);
        }
        return deployments;
    }
}
