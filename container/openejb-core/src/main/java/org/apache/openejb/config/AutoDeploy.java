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
package org.apache.openejb.config;

import java.util.List;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.ResourceLink;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

public class AutoDeploy implements DynamicDeployer {
    public static Messages messages = new Messages("org.apache.openejb.util.resources");
    public static Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");

    private final ConfigurationFactory configFactory;
    private boolean autoCreateContainers = true;
    private boolean autoCreateConnectors = true;

    public AutoDeploy(ConfigurationFactory configFactory) {
        this.configFactory = configFactory;
    }

    public boolean autoCreateConnectors() {
        return autoCreateConnectors;
    }

    public void autoCreateConnectors(boolean autoCreateConnectors) {
        this.autoCreateConnectors = autoCreateConnectors;
    }

    public boolean autoCreateContainers() {
        return autoCreateContainers;
    }

    public void autoCreateContainers(boolean autoCreateContainers) {
        this.autoCreateContainers = autoCreateContainers;
    }

    public void init() throws OpenEJBException {
    }

    public synchronized AppModule deploy(AppModule appModule) throws OpenEJBException {
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            deploy(ejbModule);
        }
        for (ClientModule clientModule : appModule.getClientModules()) {
            deploy(clientModule);
        }
        for (PersistenceModule persistenceModule : appModule.getPersistenceModules()) {
            deploy(persistenceModule);
        }
        return appModule;
    }

    public ClientModule deploy(ClientModule clientModule) throws OpenEJBException {
        return clientModule;
    }

    public EjbModule deploy(EjbModule ejbModule) throws OpenEJBException {
        OpenejbJar openejbJar;
        if (ejbModule.getOpenejbJar() != null) {
            openejbJar = ejbModule.getOpenejbJar();
        } else {
            openejbJar = new OpenejbJar();
            ejbModule.setOpenejbJar(openejbJar);
        }

        Bean[] beans = EjbJarUtils.getBeans(ejbModule.getEjbJar());

        for (Bean bean : beans) {

            EjbDeployment ejbDeployment = openejbJar.getDeploymentsByEjbName().get(bean.getEjbName());
            if (ejbDeployment == null) {
                throw new OpenEJBException("No ejb deployment found for ejb " + bean.getEjbName());
            }

            if (ejbDeployment.getContainerId() == null) {
                Class<? extends ContainerInfo> containerInfoType = ConfigurationFactory.getContainerInfoType(bean.getType());
                String containerId = getUsableContainer(containerInfoType);

                if (containerId == null){
                    if (autoCreateContainers) {
                        ContainerInfo containerInfo = configFactory.configureService(containerInfoType);
                        logger.warning("Auto-creating a container for bean " + ejbDeployment.getDeploymentId() + ": Container(type=" + bean.getType() + ", id=" + containerInfo.id + ")");
                        configFactory.install(containerInfo);
                        containerId = containerInfo.id;
                    } else {
                        throw new OpenEJBException("A container of type " + bean.getType() + " must be declared in the configuration file for bean: "+bean.getEjbName());
                    }
                }
                ejbDeployment.setContainerId(containerId);
            }

            // create the container if it doesn't exist
            if (!configFactory.getContainerIds().contains(ejbDeployment.getContainerId())) {

                if (autoCreateContainers){
                    ContainerInfo containerInfo = configFactory.configureService(ConfigurationFactory.getContainerInfoType(bean.getType()));
                    logger.warning("Auto-creating a container for bean " + ejbDeployment.getDeploymentId() + ": Container(type=" + bean.getType() + ", id=" + containerInfo.id + ")");
                    configFactory.install(containerInfo);
                } else {
                    throw new OpenEJBException("A container of type " + bean.getType() + " must be declared in the configuration file for bean: "+bean.getEjbName());
                }

            }

            // check the resource refs
            for (ResourceRef ref : bean.getResourceRef()) {
                if ((ref.getMappedName() + "").startsWith("jndi:")){
                    continue;
                }

                ResourceLink link = ejbDeployment.getResourceLink(ref.getResRefName());
                if (link == null) {
                    link = new ResourceLink();
                    List<String> connectorMap = configFactory.getConnectorIds();
                    String resRefName = ref.getResRefName();

                    String id = null;
                    if (!connectorMap.contains(resRefName)) {
                        String name = resRefName.replaceFirst(".*/", "");
                        if (!connectorMap.contains(name)) {
                            String message = "No existing Connector found while attempting to Auto-link unmapped res-ref-name '"+resRefName+"' for bean '" + ejbDeployment.getDeploymentId() + "'.  Looked for Connector(id=" + resRefName + ") and Connector(id=" + name + ")";
                            if (!autoCreateConnectors){
                                throw new OpenEJBException(message);
                            }

                            logger.error(message);

                            if (connectorMap.size() > 0) {
                                id = connectorMap.get(0);
                            } else {
                                ConnectorInfo connectorInfo = configFactory.configureService(ConnectorInfo.class);
                                id = connectorInfo.id;
                                logger.warning("Auto-creating a connector with res-id " + link.getResId() + " for bean '"+ejbDeployment.getDeploymentId()+"'.  THERE IS LITTLE CHANCE THIS WILL WORK!");
                                configFactory.install(connectorInfo);
                            }
                        }
                    }
                    logger.warning("Auto-linking res-ref-name '" + resRefName + "' in bean " + ejbDeployment.getDeploymentId() + " to Connector(id=" + id + ")");
                    link.setResId(id);
                    link.setResRefName(resRefName);
                    ejbDeployment.addResourceLink(link);
                } else {

                    List<String> connectorMap = configFactory.getConnectorIds();
                    if (!connectorMap.contains(link.getResId())) {
                        String message = "Bad resource-link in bean '" + ejbDeployment.getDeploymentId() + "': No such connector with specified res-id: ResourceLink(res-ref-name=" + link.getResRefName() + ", res-id" + link.getResId() + ")";
                        if (!autoCreateConnectors){
                            throw new OpenEJBException(message);
                        }

                        logger.error(message);

                        String id = null;
                        if (connectorMap.size() > 0) {
                            id = connectorMap.get(0);
                        } else {
                            ConnectorInfo connectorInfo = configFactory.configureService(ConnectorInfo.class);
                            id = connectorInfo.id;
                            logger.warning("Auto-creating a connector with res-id " + link.getResId() + " for bean '"+ejbDeployment.getDeploymentId()+"'.  THERE IS LITTLE CHANCE THIS WILL WORK!");
                            configFactory.install(connectorInfo);
                        }
                        String resRefName = ref.getResRefName();
                        String badResId = link.getResId();
                        logger.warning("Auto-linking res-ref-name '" + resRefName + "' in bean " + ejbDeployment.getDeploymentId() + " to Connector(id=" + id + ").  Ignoring configured link to non-existent Connector(id="+badResId +")");
                        link.setResId(id);
                        link.setResRefName(resRefName);
                    }
                }
            }
        }

        return ejbModule;
    }

    public PersistenceModule deploy(PersistenceModule persistenceModule) throws OpenEJBException {
        if (!autoCreateConnectors) {
            return persistenceModule;
        }

        Persistence persistence = persistenceModule.getPersistence();
        for (PersistenceUnit persistenceUnit : persistence.getPersistenceUnit()) {
            String jtaDataSourceId = getDataSourceId(persistenceUnit.getJtaDataSource(), persistenceUnit);
            if (jtaDataSourceId != null) {
                persistenceUnit.setJtaDataSource("java:openejb/Connector/" + jtaDataSourceId);
            }
            String nonJtaDataSourceId = getDataSourceId(persistenceUnit.getNonJtaDataSource(), persistenceUnit);
            if (nonJtaDataSourceId != null) {
                persistenceUnit.setNonJtaDataSource("java:openejb/Connector/" + nonJtaDataSourceId);
            }
        }

        return persistenceModule;
    }

    private String getDataSourceId(String dataSource, PersistenceUnit persistenceUnit) throws OpenEJBException {
        if(dataSource == null){
            return null;
        }
        if (dataSource.startsWith("java:comp/env")) {
            dataSource = dataSource.substring("java:comp/env".length());
        }

        String id = null;
        List<String> connectorMap = configFactory.getConnectorIds();
        if (!connectorMap.contains(dataSource)) {
            String name = dataSource.replaceFirst(".*/", "");
            if (!connectorMap.contains(name)) {
                String message = "No existing datasource found while attempting to Auto-link unmapped datasource '" + dataSource + "' for persistence-unit '" + persistenceUnit.getName() + "'.  Looked for Datasource(id=" + dataSource + ") and Datasource(id=" + name + ")";
                if (!autoCreateConnectors){
                    throw new OpenEJBException(message);
                }

                logger.error(message);

                if (connectorMap.size() > 0) {
                    id = connectorMap.get(0);
                } else {
                    ConnectorInfo connectorInfo = configFactory.configureService(ConnectorInfo.class);
                    id = connectorInfo.id;
                    logger.warning("Auto-creating a datasource with res-id " + id + " for persistence-unit '" + persistenceUnit.getName() + "'.  THERE IS LITTLE CHANCE THIS WILL WORK!");
                    configFactory.install(connectorInfo);
                }
            } else {
                return name;
            }
        } else {
            return dataSource;
        }
        return id;
    }

    private String getUsableContainer(Class<? extends ContainerInfo> containerInfoType) {
        for (ContainerInfo containerInfo : configFactory.getContainerInfos()) {
            if (containerInfo.getClass().equals(containerInfoType)){
                return containerInfo.id;
            }
        }

        return null;
    }
}
