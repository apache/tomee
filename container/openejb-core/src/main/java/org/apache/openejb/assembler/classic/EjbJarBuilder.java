/**
 *
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
package org.apache.openejb.assembler.classic;

import java.util.HashMap;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.util.Messages;
import org.apache.openejb.core.CoreDeploymentInfo;

/**
 * @version $Revision$ $Date$
 */
public class EjbJarBuilder {
    protected static final Messages messages = new Messages("org.apache.openejb.util.resources");

    private final ClassLoader classLoader;

    public EjbJarBuilder(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public HashMap<String, DeploymentInfo> build(EjbJarInfo ejbJar) throws OpenEJBException {
        HashMap<String, DeploymentInfo> deployments = new HashMap<String, DeploymentInfo>();
        for (EnterpriseBeanInfo ejbInfo: ejbJar.enterpriseBeans) {
            try {
                EnterpriseBeanBuilder deploymentBuilder = new EnterpriseBeanBuilder(classLoader, ejbInfo, ejbJar.defaultInterceptors);
                CoreDeploymentInfo deployment = (CoreDeploymentInfo) deploymentBuilder.build();
                deployment.setJarPath(ejbJar.jarPath);
                deployments.put(ejbInfo.ejbDeploymentId, deployment);
            } catch (Throwable e) {
                throw new OpenEJBException("Error building bean '"+ejbInfo.ejbName+"'.  Exception: "+e.getClass()+": "+e.getMessage(), e);
            }
        }
        return deployments;
    }
}
