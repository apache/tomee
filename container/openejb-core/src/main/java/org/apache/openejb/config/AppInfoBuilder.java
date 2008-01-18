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

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.PersistenceUnitInfo;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.assembler.classic.ServletInfo;
import org.apache.openejb.assembler.classic.PortInfo;
import org.apache.openejb.assembler.classic.HandlerChainInfo;
import org.apache.openejb.assembler.classic.MessageDrivenBeanInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Messages;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.jpa.unit.Property;
import org.apache.openejb.jee.jpa.JpaJaxbUtil;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.ResourceAdapter;
import org.apache.openejb.jee.ConfigProperty;
import org.apache.openejb.jee.OutboundResourceAdapter;
import org.apache.openejb.jee.ConnectionDefinition;
import org.apache.openejb.jee.InboundResource;
import org.apache.openejb.jee.MessageListener;
import org.apache.openejb.jee.AdminObject;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.Webservices;
import org.apache.openejb.jee.WebserviceDescription;
import org.apache.openejb.jee.PortComponent;
import org.apache.openejb.jee.ServiceImplBean;

import javax.xml.bind.JAXBException;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.Properties;
import java.util.Arrays;
import java.util.ArrayList;
import java.net.URL;
import java.io.File;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */
class AppInfoBuilder {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, "org.apache.openejb.util.resources");
    private static final Messages messages = new Messages("org.apache.openejb.util.resources");

    private final ConfigurationFactory configFactory;

    private EjbJarInfoBuilder ejbJarInfoBuilder = new EjbJarInfoBuilder();

    public AppInfoBuilder(ConfigurationFactory configFactory) {
        this.configFactory = configFactory;
    }

    public AppInfo build(AppModule appModule) throws OpenEJBException {
        AppInfo appInfo = new AppInfo();

        //
        //  Persistence Units
        //

        buildPersistenceModules(appModule, appInfo);


        //
        //  EJB Jars
        //

        Map<String,EjbJarInfo> ejbJarInfos = new TreeMap<String,EjbJarInfo>();
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            try {
                EjbJarInfo ejbJarInfo = ejbJarInfoBuilder.buildInfo(ejbModule);

                Map<String, EjbDeployment> deploymentsByEjbName = ejbModule.getOpenejbJar().getDeploymentsByEjbName();

                for (EnterpriseBeanInfo bean : ejbJarInfo.enterpriseBeans) {
                    EjbDeployment d = deploymentsByEjbName.get(bean.ejbName);

                    if (!configFactory.getContainerIds().contains(d.getContainerId()) && !skipMdb(bean)) {
                        String msg = messages.format("config.noContainerFound", d.getContainerId(), d.getEjbName());
                        logger.fatal(msg);
                        throw new OpenEJBException(msg);
                    }

                    bean.containerId = d.getContainerId();
                }

                ejbJarInfo.portInfos.addAll(configureWebservices(ejbModule.getWebservices()));

                ejbJarInfos.put(ejbJarInfo.moduleId, ejbJarInfo);

                appInfo.ejbJars.add(ejbJarInfo);


            } catch (OpenEJBException e) {
                ConfigUtils.logger.warning("conf.0004", ejbModule.getJarLocation(), e.getMessage());
                throw e;
            }
        }

        // Create the JNDI info builder
        JndiEncInfoBuilder jndiEncInfoBuilder = new JndiEncInfoBuilder(appInfo);

        // Build the JNDI tree for each ejb
        for (EjbModule ejbModule : appModule.getEjbModules()) {

            EjbJarInfo ejbJar = ejbJarInfos.get(ejbModule.getModuleId());

            Map<String, EnterpriseBean> beanData = ejbModule.getEjbJar().getEnterpriseBeansByEjbName();

            for (EnterpriseBeanInfo beanInfo : ejbJar.enterpriseBeans) {

                // Get the ejb-jar.xml object
                EnterpriseBean enterpriseBean = beanData.get(beanInfo.ejbName);

                // Build the JNDI info tree for the EJB
                JndiEncInfo jndi = jndiEncInfoBuilder.build(enterpriseBean, beanInfo.ejbName, ejbJar.moduleId);

                beanInfo.jndiEnc = jndi;
            }
        }

        //
        //  Application Clients
        //

        buildClientModules(appModule, appInfo, jndiEncInfoBuilder);

        //
        //  J2EE Connectors
        //

        buildConnectorModules(appModule, appInfo);

        //
        //  Webapps
        //

        buildWebModules(appModule, jndiEncInfoBuilder, appInfo);


        //
        //  Final AppInfo creation
        //

        appInfo.jarPath = appModule.getJarLocation();
        appInfo.watchedResources.addAll(appModule.getWatchedResources());
        List<URL> additionalLibraries = appModule.getAdditionalLibraries();
        for (URL url : additionalLibraries) {
            File file = new File(url.getPath());
            try {
                appInfo.libs.add(file.getCanonicalPath());
            } catch (IOException e) {
                throw new OpenEJBException("Invalid application lib path " + file.getAbsolutePath());
            }
        }

        if (appModule.getCmpMappings() != null) {
            try {
                String cmpMappingsXml = JpaJaxbUtil.marshal(EntityMappings.class, appModule.getCmpMappings());
                appInfo.cmpMappingsXml = cmpMappingsXml;
            } catch (JAXBException e) {
                throw new OpenEJBException("Unable to marshal cmp entity mappings", e);
            }
        }

        ReportValidationResults reportValidationResults = new ReportValidationResults();
        reportValidationResults.deploy(appModule);

        logger.info("Loaded Module: " + appInfo.jarPath);
        return appInfo;

    }

    private void buildClientModules(AppModule appModule, AppInfo appInfo, JndiEncInfoBuilder jndiEncInfoBuilder) throws OpenEJBException {
        for (ClientModule clientModule : appModule.getClientModules()) {
            ApplicationClient applicationClient = clientModule.getApplicationClient();
            ClientInfo clientInfo = new ClientInfo();
            clientInfo.description = applicationClient.getDescription();
            clientInfo.displayName = applicationClient.getDisplayName();
            clientInfo.codebase = clientModule.getJarLocation();
            clientInfo.mainClass = clientModule.getMainClass();
            clientInfo.callbackHandler = applicationClient.getCallbackHandler();
            clientInfo.moduleId = getClientModuleId(clientModule);
            clientInfo.watchedResources.addAll(clientModule.getWatchedResources());

            clientInfo.jndiEnc = jndiEncInfoBuilder.build(applicationClient, clientModule.getJarLocation(), clientInfo.moduleId);
            appInfo.clients.add(clientInfo);
        }
    }

    private void buildWebModules(AppModule appModule, JndiEncInfoBuilder jndiEncInfoBuilder, AppInfo appInfo) throws OpenEJBException {
        for (WebModule webModule : appModule.getWebModules()) {
            WebApp webApp = webModule.getWebApp();
            WebAppInfo webAppInfo = new WebAppInfo();
            webAppInfo.description = webApp.getDescription();
            webAppInfo.displayName = webApp.getDisplayName();
            webAppInfo.codebase = webModule.getJarLocation();
            webAppInfo.moduleId = webModule.getModuleId();
            webAppInfo.watchedResources.addAll(webModule.getWatchedResources());

            webAppInfo.host = webModule.getHost();
            webAppInfo.contextRoot = webModule.getContextRoot();

            webAppInfo.jndiEnc = jndiEncInfoBuilder.build(webApp, webModule.getJarLocation(), webAppInfo.moduleId);

            webAppInfo.portInfos.addAll(configureWebservices(webModule.getWebservices()));

            for (Servlet servlet : webModule.getWebApp().getServlet()) {
                ServletInfo servletInfo = new ServletInfo();
                servletInfo.servletName = servlet.getServletName();
                servletInfo.servletClass = servlet.getServletClass();
                webAppInfo.servlets.add(servletInfo);
            }

            appInfo.webApps.add(webAppInfo);
        }
    }

    private void buildConnectorModules(AppModule appModule, AppInfo appInfo) {
        for (ConnectorModule connectorModule : appModule.getResourceModules()) {
            //
            // DEVELOPERS NOTE:  if you change the id generation code here, you must change
            // the id generation code in AutoConfig$AppResources
            //

            Connector connector = connectorModule.getConnector();

            ConnectorInfo connectorInfo = new ConnectorInfo();
            connectorInfo.description = connector.getDescription();
            connectorInfo.displayName = connector.getDisplayName();
            connectorInfo.codebase = connectorModule.getJarLocation();
            connectorInfo.moduleId = connectorModule.getModuleId();
            connectorInfo.watchedResources.addAll(connectorModule.getWatchedResources());

            List<URL> libraries = connectorModule.getLibraries();
            for (URL url : libraries) {
                File file = new File(url.getPath());
                try {
                    connectorInfo.libs.add(file.getCanonicalPath());
                } catch (IOException e) {
                    throw new IllegalArgumentException("Invalid application lib path " + file.getAbsolutePath());                    
                }
            }

            ResourceAdapter resourceAdapter = connector.getResourceAdapter();
            if (resourceAdapter.getResourceAdapterClass() != null) {
                ResourceInfo resourceInfo = new ResourceInfo();
                resourceInfo.service = "Resource";
                if (resourceAdapter.getId() != null) {
                    resourceInfo.id = resourceAdapter.getId();
                } else {
                    resourceInfo.id = connectorModule.getModuleId() + "RA";
                }
                resourceInfo.className = resourceAdapter.getResourceAdapterClass();
                resourceInfo.properties = new Properties();
                for (ConfigProperty property : resourceAdapter.getConfigProperty()) {
                    String name = property.getConfigPropertyName();
                    String value = property.getConfigPropertyValue();
                    if (value != null) {
                        resourceInfo.properties.setProperty(name, value);
                    }
                }
                resourceInfo.properties.putAll(ConfigurationFactory.getSystemProperties(resourceInfo.id, "RESOURCE"));
                connectorInfo.resourceAdapter = resourceInfo;
            }

            OutboundResourceAdapter outbound = resourceAdapter.getOutboundResourceAdapter();
            if (outbound != null) {
                String transactionSupport = "none";
                switch (outbound.getTransactionSupport()) {
                    case LOCAL_TRANSACTION:
                        transactionSupport = "local";
                        break;
                    case NO_TRANSACTION:
                        transactionSupport = "none";
                        break;
                    case XA_TRANSACTION:
                        transactionSupport = "xa";
                        break;
                }
                for (ConnectionDefinition connection : outbound.getConnectionDefinition()) {
                    ResourceInfo resourceInfo = new ResourceInfo();
                    resourceInfo.service = "Resource";
                    if (connection.getId() != null) {
                        resourceInfo.id = connection.getId();
                    } else if (outbound.getConnectionDefinition().size() == 1) {
                        resourceInfo.id = connectorModule.getModuleId();
                    } else {
                        resourceInfo.id = connectorModule.getModuleId() + "-" + connection.getConnectionFactoryInterface();
                    }
                    resourceInfo.className = connection.getManagedConnectionFactoryClass();
                    resourceInfo.types.add(connection.getConnectionFactoryInterface());
                    resourceInfo.properties = new Properties();
                    for (ConfigProperty property : connection.getConfigProperty()) {
                        String name = property.getConfigPropertyName();
                        String value = property.getConfigPropertyValue();
                        if (value != null) {
                            resourceInfo.properties.setProperty(name, value);
                        }
                    }
                    resourceInfo.properties.setProperty("TransactionSupport", transactionSupport);
                    resourceInfo.properties.setProperty("ResourceAdapter", connectorInfo.resourceAdapter.id);
                    resourceInfo.properties.putAll(ConfigurationFactory.getSystemProperties(resourceInfo.id, "RESOURCE"));
                    connectorInfo.outbound.add(resourceInfo);
                }
            }

            InboundResource inbound = resourceAdapter.getInboundResourceAdapter();
            if (inbound != null) {
                for (MessageListener messageListener : inbound.getMessageAdapter().getMessageListener()) {
                    MdbContainerInfo mdbContainerInfo = new MdbContainerInfo();
                    mdbContainerInfo.service = "Container";
                    if (messageListener.getId() != null) {
                        mdbContainerInfo.id = messageListener.getId();
                    } else if (inbound.getMessageAdapter().getMessageListener().size() == 1) {
                        mdbContainerInfo.id = connectorModule.getModuleId();
                    } else {
                        mdbContainerInfo.id = connectorModule.getModuleId() + "-" + messageListener.getMessageListenerType();
                    }

                    mdbContainerInfo.properties = new Properties();
                    mdbContainerInfo.properties.setProperty("ResourceAdapter", connectorInfo.resourceAdapter.id);
                    mdbContainerInfo.properties.setProperty("MessageListenerInterface", messageListener.getMessageListenerType());
                    mdbContainerInfo.properties.setProperty("ActivationSpecClass", messageListener.getActivationSpec().getActivationSpecClass());

                    // todo provider system should fill in this information
                    mdbContainerInfo.types.add("MESSAGE");
                    mdbContainerInfo.className = "org.apache.openejb.core.mdb.MdbContainer";
                    mdbContainerInfo.constructorArgs.addAll(Arrays.asList("id", "transactionManager", "securityService", "ResourceAdapter", "MessageListenerInterface", "ActivationSpecClass", "InstanceLimit"));
                    mdbContainerInfo.properties.setProperty("InstanceLimit", "10");

                    mdbContainerInfo.properties.putAll(ConfigurationFactory.getSystemProperties(mdbContainerInfo.id, "CONTAINER"));
                    connectorInfo.inbound.add(mdbContainerInfo);
                }
            }

            for (AdminObject adminObject : resourceAdapter.getAdminObject()) {
                ResourceInfo resourceInfo = new ResourceInfo();
                resourceInfo.service = "Resource";
                if (adminObject.getId() != null) {
                    resourceInfo.id = adminObject.getId();
                } else if (resourceAdapter.getAdminObject().size() == 1) {
                    resourceInfo.id = connectorModule.getModuleId();
                } else {
                    resourceInfo.id = connectorModule.getModuleId() + "-" + adminObject.getAdminObjectInterface();
                }
                resourceInfo.className = adminObject.getAdminObjectClass();
                resourceInfo.types.add(adminObject.getAdminObjectInterface());
                resourceInfo.properties = new Properties();
                for (ConfigProperty property : adminObject.getConfigProperty()) {
                    String name = property.getConfigPropertyName();
                    String value = property.getConfigPropertyValue();
                    if (value != null) {
                        resourceInfo.properties.setProperty(name, value);
                    }
                }
                resourceInfo.properties.putAll(ConfigurationFactory.getSystemProperties(resourceInfo.id, "RESOURCE"));
                connectorInfo.adminObject.add(resourceInfo);
            }

            appInfo.connectors.add(connectorInfo);
        }
    }

    private void buildPersistenceModules(AppModule appModule, AppInfo appInfo) {
        for (PersistenceModule persistenceModule : appModule.getPersistenceModules()) {
            String rootUrl = persistenceModule.getRootUrl();
            Persistence persistence = persistenceModule.getPersistence();
            for (PersistenceUnit persistenceUnit : persistence.getPersistenceUnit()) {
                PersistenceUnitInfo info = new PersistenceUnitInfo();
                info.id = persistenceUnit.getName() + " " + rootUrl.hashCode();
                info.name = persistenceUnit.getName();
                info.watchedResources.addAll(persistenceModule.getWatchedResources());
                info.persistenceUnitRootUrl = rootUrl;
                info.provider = persistenceUnit.getProvider();
                info.transactionType = persistenceUnit.getTransactionType().toString();

                Boolean excludeUnlistedClasses = persistenceUnit.isExcludeUnlistedClasses();
                info.excludeUnlistedClasses = excludeUnlistedClasses != null && excludeUnlistedClasses;

                info.jtaDataSource = persistenceUnit.getJtaDataSource();
                info.nonJtaDataSource = persistenceUnit.getNonJtaDataSource();

                info.jarFiles.addAll(persistenceUnit.getJarFile());
                info.classes.addAll(persistenceUnit.getClazz());
                info.mappingFiles.addAll(persistenceUnit.getMappingFile());

                // Handle Properties
                org.apache.openejb.jee.jpa.unit.Properties puiProperties = persistenceUnit.getProperties();
                if (puiProperties != null) {
                    for (Property property : puiProperties.getProperty()) {
                        info.properties.put(property.getName(), property.getValue());
                    }
                }

                logger.info("Configuring PersistenceUnit(name="+info.name+", provider="+info.provider+")");
                // Persistence Unit Root Url
                appInfo.persistenceUnits.add(info);
            }
        }
    }

    private static String getClientModuleId(ClientModule clientModule) {
        String jarLocation = clientModule.getJarLocation();
        File file = new File(jarLocation);
        String name = file.getName();
        if (name.endsWith(".jar") || name.endsWith(".zip")) {
            name = name.replaceFirst("....$", "");
        }
        return name;
    }


    private List<PortInfo> configureWebservices(Webservices webservices) {
        List<PortInfo> portMap = new ArrayList<PortInfo>();
        if (webservices == null) {
            return portMap;
        }

        for (WebserviceDescription desc : webservices.getWebserviceDescription()) {
            String wsdlFile = desc.getWsdlFile();
            String serviceName = desc.getWebserviceDescriptionName();

            for (PortComponent port : desc.getPortComponent()) {
                PortInfo portInfo = new PortInfo();

                ServiceImplBean serviceImplBean = port.getServiceImplBean();
                portInfo.serviceId = desc.getId();
                portInfo.portId = port.getId();
                portInfo.serviceLink = serviceImplBean.getEjbLink();
                if (portInfo.serviceLink == null) {
                    portInfo.serviceLink = serviceImplBean.getServletLink();
                }

                portInfo.seiInterfaceName = port.getServiceEndpointInterface();
                portInfo.portName = port.getPortComponentName();
                portInfo.binding = port.getProtocolBinding();
                portInfo.serviceName = serviceName;
                portInfo.wsdlFile = wsdlFile;
                portInfo.mtomEnabled = port.isEnableMtom();
                portInfo.wsdlPort = port.getWsdlPort();
                portInfo.wsdlService = port.getWsdlService();
                portInfo.location = port.getLocation();

                List<HandlerChainInfo> handlerChains = ConfigurationFactory.toHandlerChainInfo(port.getHandlerChains());
                portInfo.handlerChains.addAll(handlerChains);

                // todo configure jaxrpc mappings here

                portMap.add(portInfo);
            }
        }
        return portMap;
    }

    private static boolean skipMdb(EnterpriseBeanInfo bean) {
        return bean instanceof MessageDrivenBeanInfo && System.getProperty("duct tape") != null;
    }

}
