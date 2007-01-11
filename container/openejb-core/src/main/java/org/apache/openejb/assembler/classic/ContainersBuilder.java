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
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Logger;
import org.apache.xbean.recipe.ConstructionException;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.StaticRecipe;

import javax.transaction.TransactionManager;
import javax.naming.NamingException;
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
    private CoreContainerSystem containerSystem;

    public ContainersBuilder(CoreContainerSystem containerSystem, ContainerSystemInfo containerSystemInfo, Properties props) {
        this.props = props;
        this.containerInfos = containerSystemInfo.containers;
        String decorators = props.getProperty("openejb.container.decorators");
        this.decorators = (decorators == null) ? new String[]{} : decorators.split(":");
        this.containerSystem = containerSystem;
    }

    public Object buildContainers(HashMap<String, DeploymentInfo> deployments) throws OpenEJBException, NamingException {
        List<Container> containers = new ArrayList<Container>();
        for (ContainerInfo serviceInfo : containerInfos) {

            Map<String, CoreDeploymentInfo> deploymentsList = new HashMap<String, CoreDeploymentInfo>();

            Container container = buildContainer(serviceInfo, deploymentsList);
            container = wrapContainer(container);

            containers.add(container);

            Class interfce = AssemblerTool.serviceInterfaces.get(serviceInfo.serviceType);
            AssemblerTool.checkImplementation(interfce, container.getClass(), serviceInfo.serviceType, serviceInfo.id);


            this.containerSystem.getJNDIContext().bind("java:openejb/" + serviceInfo.serviceType + "/" + serviceInfo.id, container);

            SystemInstance.get().setComponent(interfce, container);

            props.put(interfce.getName(), container);
            props.put(serviceInfo.serviceType, container);
            props.put(serviceInfo.id, container);

        }
        return containers;
    }

    private Container buildContainer(ContainerInfo containerInfo, Map<String, CoreDeploymentInfo> deploymentsList) throws OpenEJBException {
        String containerName = containerInfo.id;
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
