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

import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.util.Messages;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public class EjbJarBuilder {
    protected static final Messages messages = new Messages("org.apache.openejb.util.resources");

    private final ClassLoader classLoader;
    private final Properties props;

    public EjbJarBuilder(Properties props, ClassLoader classLoader) {
        this.props = props;
        this.classLoader = classLoader;
    }

    public HashMap<String, DeploymentInfo> build(EjbJarInfo ejbJar, LinkResolver<EntityManagerFactory> emfLinkResolver) throws OpenEJBException {
        HashMap<String, DeploymentInfo> deployments = new HashMap<String, DeploymentInfo>();

        InterceptorBindingBuilder interceptorBindingBuilder = new InterceptorBindingBuilder(classLoader, ejbJar);

        for (EnterpriseBeanInfo ejbInfo : ejbJar.enterpriseBeans) {
            try {
                EnterpriseBeanBuilder deploymentBuilder = new EnterpriseBeanBuilder(classLoader, ejbInfo, ejbJar.moduleId, new ArrayList<String>(), emfLinkResolver);
                CoreDeploymentInfo deployment = (CoreDeploymentInfo) deploymentBuilder.build();

                interceptorBindingBuilder.build(deployment, ejbInfo);

                deployment.setJarPath(ejbJar.jarPath);
                deployments.put(ejbInfo.ejbDeploymentId, deployment);

                Container container = (Container) props.get(ejbInfo.containerId);
                if (container == null) throw new IllegalStateException("Container does not exist: " + ejbInfo.containerId + ".  Referenced by deployment: " + deployment.getDeploymentID());
                // Don't deploy to the container, yet. That will be done by deploy() once Assembler as finished configuring the DeploymentInfo
                deployment.setContainer(container);
            } catch (Throwable e) {
                throw new OpenEJBException("Error building bean '" + ejbInfo.ejbName + "'.  Exception: " + e.getClass() + ": " + e.getMessage(), e);
            }
        }
        return deployments;
    }
    
    public void deploy(HashMap<String, DeploymentInfo> deployments) throws OpenEJBException {
        for (DeploymentInfo deployment : deployments.values()) {
            try {
                deployment.getContainer().deploy(deployment);
            } catch (Throwable t) {
                throw new OpenEJBException("Error deploying '"+deployment.getEjbName()+"'.  Exception: "+t.getClass()+": "+t.getMessage(), t);
            }
        }
    }
}
