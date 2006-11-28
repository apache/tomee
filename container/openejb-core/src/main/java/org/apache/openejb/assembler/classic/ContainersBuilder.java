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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.assembler.classic;

import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Logger;
import org.apache.xbean.recipe.ConstructionException;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.StaticRecipe;

import javax.transaction.TransactionManager;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ContainersBuilder {

    private static final Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");

    private final Properties props;
    private final List<ContainerInfo> containerInfos;
    private final String[] decorators;

    public ContainersBuilder(ContainerSystemInfo containerSystemInfo, Properties props) {
        this.props = props;
        this.containerInfos = containerSystemInfo.containers;
        String decorators = props.getProperty("openejb.container.decorators");
        this.decorators = (decorators == null) ? new String[]{} : decorators.split(":");

    }

    public Object buildContainers(HashMap<String, DeploymentInfo> deployments) throws OpenEJBException {
        List<Container> containers = new ArrayList<Container>();
        for (ContainerInfo containerInfo : containerInfos) {

            Map<String, CoreDeploymentInfo> deploymentsList = new HashMap<String, CoreDeploymentInfo>();
            for (EnterpriseBeanInfo bean : containerInfo.ejbeans) {
                String ejbDeploymentId = bean.ejbDeploymentId;
                CoreDeploymentInfo deployment = (CoreDeploymentInfo) deployments.get(ejbDeploymentId);
                deploymentsList.put(ejbDeploymentId, deployment);
            }

            Container container = buildContainer(containerInfo, deploymentsList);
            container = wrapContainer(container);

            DeploymentInfo [] deploys = container.deployments();
            for (int x = 0; x < deploys.length; x++) {
                CoreDeploymentInfo di = (CoreDeploymentInfo) deploys[x];
                di.setContainer(container);
            }
            containers.add(container);
        }
        return containers;
    }

    private Container buildContainer(ContainerInfo containerInfo, Map<String, CoreDeploymentInfo> deploymentsList) throws OpenEJBException {
        String containerName = containerInfo.containerName;
        ContainerInfo service = containerInfo;

        Properties systemProperties = System.getProperties();
        synchronized (systemProperties) {
            String userDir = systemProperties.getProperty("user.dir");
            try {
                File base = SystemInstance.get().getBase().getDirectory();
                systemProperties.setProperty("user.dir", base.getAbsolutePath());

                ObjectRecipe containerRecipe = new ObjectRecipe(service.className, service.constructorArgs.toArray(new String[0]), null);
                containerRecipe.setAllProperties(service.properties);
                containerRecipe.setProperty("id", new StaticRecipe(containerName));
                containerRecipe.setProperty("transactionManager", new StaticRecipe(props.get(TransactionManager.class.getName())));
                containerRecipe.setProperty("securityService", new StaticRecipe(props.get(SecurityService.class.getName())));
                containerRecipe.setProperty("deployments", new StaticRecipe(deploymentsList));

                return (Container) containerRecipe.create();
            } catch (Exception e) {
                Throwable cause = (e.getCause() != null) ? e.getCause() : e;
                throw new OpenEJBException(AssemblerTool.messages.format("as0002", containerName, cause.getMessage()), cause);
            } finally {
                systemProperties.setProperty("user.dir", userDir);
            }
        }
    }

    private Container wrapContainer(Container container) {
        for (int i = 0; i < decorators.length && container instanceof RpcContainer; i++) {
            try {
                String decoratorName = decorators[i];
                ObjectRecipe decoratorRecipe = new ObjectRecipe(decoratorName,new String[]{"container"}, null);
                decoratorRecipe.setProperty("container", new StaticRecipe(container));
                container = (Container) decoratorRecipe.create();
            } catch (ConstructionException e) {
                logger.error("Container wrapper class " + decorators[i] + " could not be constructed and will be skipped.", e);
            }
        }
        return container;
    }
}
