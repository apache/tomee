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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.config.sys.Connector;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.ResourceLink;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoDeploy implements DynamicDeployer {
    public static Messages messages = new Messages("org.apache.openejb.util.resources");
    public static Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");

    private static final String DEFAULT_CONNECTOR_ID = "Default Unmanaged JDBC Database";
    private static Map<String,String> defaultConnectorIds = new HashMap<String,String>();

    static {
        defaultConnectorIds.put("javax.sql.DataSource", DEFAULT_CONNECTOR_ID);
        defaultConnectorIds.put("javax.jms.ConnectionFactory", "Default JMS Connection Factory");
        defaultConnectorIds.put("javax.jms.QueueConnectionFactory", "Default JMS Connection Factory");
        defaultConnectorIds.put("javax.jms.TopicConnectionFactory", "Default JMS Connection Factory");
    }

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
                        containerId = installContainer(containerInfo);
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
                    installContainer(containerInfo);
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
                    String resRefName = ref.getResRefName();
                    String id = getConnectorId(ejbDeployment.getDeploymentId(), resRefName, ref.getResType());
                    logger.warning("Auto-linking res-ref-name '" + resRefName + "' in bean " + ejbDeployment.getDeploymentId() + " to Connector(id=" + id + ")");

                    link = new ResourceLink();
                    link.setResId(id);
                    link.setResRefName(resRefName);
                    ejbDeployment.addResourceLink(link);
                } else {
                    String id = getConnectorId(ejbDeployment.getDeploymentId(), link.getResId(), ref.getResType());
                    link.setResId(id);
                    link.setResRefName(ref.getResRefName());
                }
            }
        }

        return ejbModule;
    }

    private String installContainer(ContainerInfo containerInfo) throws OpenEJBException {
        String resourceAdapterId = containerInfo.properties.getProperty("ResourceAdapter");
        if (resourceAdapterId != null) {
            String newResourceId = getResourceId(resourceAdapterId);
            if (resourceAdapterId != newResourceId) {
                containerInfo.properties.setProperty("ResourceAdapter", newResourceId);
            }
        }

        configFactory.install(containerInfo);
        return containerInfo.id;
    }

    public PersistenceModule deploy(PersistenceModule persistenceModule) throws OpenEJBException {
        if (!autoCreateConnectors) {
            return persistenceModule;
        }

        Persistence persistence = persistenceModule.getPersistence();
        for (PersistenceUnit persistenceUnit : persistence.getPersistenceUnit()) {
            String jtaDataSourceId = getConnectorId(persistenceUnit.getName(), persistenceUnit.getJtaDataSource(), DataSource.class.getName());
            if (jtaDataSourceId != null) {
                persistenceUnit.setJtaDataSource("java:openejb/Connector/" + jtaDataSourceId);
            }
            String nonJtaDataSourceId = getConnectorId(persistenceUnit.getName(), persistenceUnit.getNonJtaDataSource(), DataSource.class.getName());
            if (nonJtaDataSourceId != null) {
                persistenceUnit.setNonJtaDataSource("java:openejb/Connector/" + nonJtaDataSourceId);
            }
        }

        return persistenceModule;
    }

    private String getResourceId(String resourceId) throws OpenEJBException {
        if(resourceId == null){
            return null;
        }

        // try to lookup the resource in the container system
        List<String> resourceIds = configFactory.getResourceIds();
        if (resourceIds.contains(resourceId)) {
            return resourceId;
        }

        // throw an exception or log an error
        String message = "No existing resource adapter found while attempting to Auto-link unmapped resource adapter '" + resourceId + "'.";
        if (!autoCreateConnectors){
            throw new OpenEJBException(message);
        }
        logger.error(message);

        // if there is a provider with the specified name. use it
        if (!ServiceUtils.hasServiceProvider(resourceId)) {
            throw new OpenEJBException("No existing resource adapter defined with id '" + resourceId + "'.");
        }

        // auto create the resource adapter
        ResourceInfo resourceInfo = configFactory.configureService(ResourceInfo.class, resourceId, null, resourceId, null);
        configFactory.install(resourceInfo);
        return resourceInfo.id;
    }

    private String getConnectorId(String beanName, String connectorId, String type) throws OpenEJBException {
        if(connectorId == null){
            return null;
        }

        if (connectorId.startsWith("java:comp/env")) {
            connectorId = connectorId.substring("java:comp/env".length());
        }

        List<String> connectorIds = configFactory.getConnectorIds(type);
        if (connectorIds.contains(connectorId)) {
            return connectorId;
        }

        String name = connectorId.replaceFirst(".*/", "");
        if (connectorIds.contains(name)) {
            return name;
        }

        // throw an exception or log an error
        String message = "No existing connector found while attempting to Auto-link unmapped connector '" + connectorId + "' of type '" + type  + "' for '" + beanName + "'.  Looked for Conector(id=" + connectorId + ") and Connector(id=" + name + ")";
        if (!autoCreateConnectors){
            throw new OpenEJBException(message);
        }
        logger.error(message);

        // if there is a provider with the specified name. use it
        if (ServiceUtils.hasServiceProvider(name)) {
            ConnectorInfo connectorInfo = configFactory.configureService(ConnectorInfo.class, name, null, name, null);
            return installConnector(connectorInfo);
        }

        // if there is only one connector, use it
        if (connectorIds.size() > 0) {
            return connectorIds.get(0);
        }

        // Just use the default conector
        Service service = getDefaultConnector(type);
        ConnectorInfo connectorInfo = configFactory.configureService(service, ConnectorInfo.class);
        logger.warning("Auto-creating a connector with id '" + connectorInfo.id +  "' of type '" + type  + " for '" + beanName + "'.  THERE IS LITTLE CHANCE THIS WILL WORK!");
        return installConnector(connectorInfo);
    }

    private String installConnector(ConnectorInfo connectorInfo) throws OpenEJBException {
        String resourceAdapterId = connectorInfo.properties.getProperty("ResourceAdapter");
        if (resourceAdapterId != null) {
            String newResourceId = getResourceId(resourceAdapterId);
            if (resourceAdapterId != newResourceId) {
                connectorInfo.properties.setProperty("ResourceAdapter", newResourceId);
            }
        }

        configFactory.install(connectorInfo);
        return connectorInfo.id;
    }

    private Service getDefaultConnector(String type) {
        String providerId = defaultConnectorIds.get(type);
        if (providerId == null) {
            providerId = DEFAULT_CONNECTOR_ID;
        }
        Service service = new Connector();
        service.setProvider(providerId);
        service.setId(providerId);
        return service;
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
