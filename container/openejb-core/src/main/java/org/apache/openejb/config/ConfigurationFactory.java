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
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.BmpEntityContainerInfo;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.CmpEntityContainerInfo;
import org.apache.openejb.assembler.classic.ConnectionManagerInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.ContainerSystemInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.FacilitiesInfo;
import org.apache.openejb.assembler.classic.JndiContextInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.assembler.classic.MessageDrivenBeanInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.OpenEjbConfigurationFactory;
import org.apache.openejb.assembler.classic.PersistenceUnitInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.assembler.classic.PortInfo;
import org.apache.openejb.assembler.classic.HandlerChainInfo;
import org.apache.openejb.assembler.classic.HandlerInfo;
import org.apache.openejb.assembler.classic.ServletInfo;
import org.apache.openejb.config.sys.ConnectionManager;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.sys.JndiProvider;
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.config.sys.ProxyFactory;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.config.sys.SecurityService;
import org.apache.openejb.config.sys.ServiceProvider;
import org.apache.openejb.config.sys.TransactionManager;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.ResourceAdapter;
import org.apache.openejb.jee.ConfigProperty;
import org.apache.openejb.jee.OutboundResourceAdapter;
import org.apache.openejb.jee.ConnectionDefinition;
import org.apache.openejb.jee.InboundResource;
import org.apache.openejb.jee.MessageListener;
import org.apache.openejb.jee.AdminObject;
import org.apache.openejb.jee.Webservices;
import org.apache.openejb.jee.WebserviceDescription;
import org.apache.openejb.jee.PortComponent;
import org.apache.openejb.jee.ServiceImplBean;
import org.apache.openejb.jee.HandlerChain;
import org.apache.openejb.jee.Handler;
import org.apache.openejb.jee.ParamValue;
import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.JpaJaxbUtil;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.jpa.unit.Property;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigurationFactory implements OpenEjbConfigurationFactory {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, "org.apache.openejb.util.resources");
    private static final Messages messages = new Messages("org.apache.openejb.util.resources");

    private String configLocation = "";

    private OpenEjbConfiguration sys;

    private EjbJarInfoBuilder ejbJarInfoBuilder = new EjbJarInfoBuilder();

    private Openejb openejb;

    private DynamicDeployer deployer;
    private final DeploymentLoader deploymentLoader;
    private final boolean offline;

    public ConfigurationFactory() {
        this(false);
    }

    public ConfigurationFactory(boolean offline, OpenEjbConfiguration configuration) {
        this(offline);
        sys = configuration;
    }

    public static class Chain implements DynamicDeployer{
        private final List<DynamicDeployer> chain = new ArrayList<DynamicDeployer>();

        public boolean add(DynamicDeployer o) {
            return chain.add(o);
        }

        public AppModule deploy(AppModule appModule) throws OpenEJBException {
            for (DynamicDeployer deployer : chain) {
                appModule = deployer.deploy(appModule);
            }
            return appModule;
        }
    }

    public ConfigurationFactory(boolean offline) {
        this.offline = offline;
        deploymentLoader = new DeploymentLoader();

        Chain chain = new Chain();

        chain.add(new ReadDescriptors());

        chain.add(new AnnotationDeployer());

        boolean shouldValidate = !SystemInstance.get().getProperty("openejb.validation.skip", "false").equalsIgnoreCase("true");
        if (shouldValidate) {
            chain.add(new ValidateModules());
        } else {
            DeploymentLoader.logger.info("Validation is disabled.");
        }

        chain.add(new InitEjbDeployments());

        String debuggableVmHackery = SystemInstance.get().getProperty("openejb.debuggable-vm-hackery", "false");
        if (debuggableVmHackery.equalsIgnoreCase("true")){
            chain.add(new DebuggableVmHackery());
        }

        chain.add(new WsDeployer());

        chain.add(new CmpJpaConversion());
        chain.add(new OpenEjb2Conversion());
        chain.add(new SunConversion());
        chain.add(new WlsConversion());

        if (System.getProperty("duct tape") != null){
            // must be after CmpJpaConversion since it adds new persistence-context-refs
            chain.add(new GeronimoMappedName());
        }

        if (offline) {
            AutoConfig autoConfig = new AutoConfig(this);
            autoConfig.autoCreateResources(false);
            autoConfig.autoCreateContainers(false);
            chain.add(autoConfig);
        } else {
            chain.add(new AutoConfig(this));
        }

        // TODO: How do we want this plugged in?
        //chain.add(new OutputGeneratedDescriptors());
        this.deployer = chain;
    }

    public void init(Properties props) throws OpenEJBException {

        configLocation = props.getProperty("openejb.conf.file");

        if (configLocation == null) {
            configLocation = props.getProperty("openejb.configuration");
        }

        configLocation = ConfigUtils.searchForConfiguration(configLocation, props);
        if (configLocation != null) {
            props.setProperty("openejb.configuration", configLocation);
        }

    }

    protected void install(ContainerInfo serviceInfo) throws OpenEJBException {
        if (sys != null) {
            sys.containerSystem.containers.add(serviceInfo);
        } else if (!offline) {
            Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
            assembler.createContainer(serviceInfo);
        }
    }

    protected void install(ResourceInfo serviceInfo) throws OpenEJBException {
        if (sys != null) {
            sys.facilities.resources.add(serviceInfo);
        } else if (!offline) {
            Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
            assembler.createResource(serviceInfo);
        }
    }

    public OpenEjbConfiguration getOpenEjbConfiguration() throws OpenEJBException {

        if (configLocation != null) {
            openejb = JaxbOpenejb.readConfig(configLocation);
        } else {
            openejb = JaxbOpenejb.createOpenejb();
        }

        sys = new OpenEjbConfiguration();
        sys.containerSystem = new ContainerSystemInfo();
        sys.facilities = new FacilitiesInfo();


        for (JndiProvider provider : openejb.getJndiProvider()) {
            JndiContextInfo info = configureService(provider, JndiContextInfo.class);
            sys.facilities.remoteJndiContexts.add(info);
        }

        sys.facilities.securityService = configureService(openejb.getSecurityService(), SecurityServiceInfo.class);

        sys.facilities.transactionService = configureService(openejb.getTransactionManager(), TransactionServiceInfo.class);

        // convert legacy connector declarations to resource declarations
        for (org.apache.openejb.config.sys.Connector connector : openejb.getConnector()) {
            Resource resource = JaxbOpenejb.createResource();
            resource.setJar(connector.getJar());
            resource.setId(connector.getId());
            resource.setProvider(connector.getProvider());

            resource.getProperties().clear();
            resource.getProperties().putAll(connector.getProperties());

            openejb.getResource().add(resource);
        }

        for (Resource resource : openejb.getResource()) {
            ResourceInfo resourceInfo = configureService(resource, ResourceInfo.class);
            sys.facilities.resources.add(resourceInfo);
        }

//        ConnectionManagerInfo service = configureService(openejb.getConnectionManager(), ConnectionManagerInfo.class);
//        sys.facilities.connectionManagers.add(service);

        sys.facilities.intraVmServer = configureService(openejb.getProxyFactory(), ProxyFactoryInfo.class);

        for (Container declaration : openejb.getContainer()) {
            Class<? extends ContainerInfo> infoClass = getContainerInfoType(declaration.getType());

            if (infoClass == null) {
                throw new OpenEJBException("Unrecognized contianer type " + declaration.getType());
            }

            ContainerInfo info = configureService(declaration, infoClass);

            sys.containerSystem.containers.add(info);
        }


        List<String> jarList = DeploymentsResolver.resolveAppLocations(openejb.getDeployments());
        for (String pathname : jarList) {

            try {
                File jarFile = new File(pathname);

                AppInfo appInfo = configureApplication(jarFile);

                sys.containerSystem.applications.add(appInfo);
            } catch (OpenEJBException alreadyHandled) {
            }
        }

        return sys;
    }


    public AppInfo configureApplication(File jarFile) throws OpenEJBException {
        logger.debug("Beginning load: " + jarFile.getAbsolutePath());

        AppInfo appInfo = null;
        try {
            AppModule appModule = deploymentLoader.load(jarFile);
            appInfo = configureApplication(appModule);
        } catch (OpenEJBException e) {
            String message = messages.format("conf.0004", jarFile.getAbsolutePath(), e.getMessage());
            // DO NOT REMOVE THE EXCEPTION FROM THIS LOG MESSAGE
            // removing this message causes NO messages to be printed when embedded
            logger.warning(message, e);
            throw e;
        }
        return appInfo;
    }

    public EjbJarInfo configureApplication(EjbJar ejbJar) throws OpenEJBException {
        EjbModule ejbModule = new EjbModule(ejbJar);
        return configureApplication(ejbModule);
    }

    public EjbJarInfo configureApplication(EjbModule ejbModule) throws OpenEJBException {
        AppModule appModule = new AppModule(ejbModule.getClassLoader(), ejbModule.getJarLocation());
        appModule.getEjbModules().add(ejbModule);
        AppInfo appInfo = configureApplication(appModule);
        return appInfo.ejbJars.get(0);
    }

    public ClientInfo configureApplication(ClientModule clientModule) throws OpenEJBException {
        AppModule appModule = new AppModule(clientModule.getClassLoader(), clientModule.getJarLocation());
        appModule.getClientModules().add(clientModule);
        AppInfo appInfo = configureApplication(appModule);
        return appInfo.clients.get(0);
    }

    public ConnectorInfo configureApplication(ConnectorModule connectorModule) throws OpenEJBException {
        AppModule appModule = new AppModule(connectorModule.getClassLoader(), connectorModule.getJarLocation());
        appModule.getResourceModules().add(connectorModule);
        AppInfo appInfo = configureApplication(appModule);
        return appInfo.connectors.get(0);
    }

    public WebAppInfo configureApplication(WebModule webModule) throws OpenEJBException {
        AppModule appModule = new AppModule(webModule.getClassLoader(), webModule.getJarLocation());
        appModule.getWebModules().add(webModule);
        AppInfo appInfo = configureApplication(appModule);
        return appInfo.webApps.get(0);
    }

    public AppInfo configureApplication(AppModule appModule) throws OpenEJBException {
        logger.info("Configuring app: "+appModule.getJarLocation());
        deployer.deploy(appModule);

        AppInfo appInfo = new AppInfo();
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            try {
                EjbJarInfo ejbJarInfo = ejbJarInfoBuilder.buildInfo(ejbModule);

                Map<String, EjbDeployment> deploymentsByEjbName = ejbModule.getOpenejbJar().getDeploymentsByEjbName();

                for (EnterpriseBeanInfo bean : ejbJarInfo.enterpriseBeans) {
                    EjbDeployment d = deploymentsByEjbName.get(bean.ejbName);

                    if (!getContainerIds().contains(d.getContainerId()) && !skipMdb(bean)) {
                        String msg = messages.format("config.noContainerFound", d.getContainerId(), d.getEjbName());
                        logger.fatal(msg);
                        throw new OpenEJBException(msg);
                    }

                    bean.containerId = d.getContainerId();
                }

                ejbJarInfo.portInfos.addAll(configureWebservices(ejbModule.getWebservices()));

                appInfo.ejbJars.add(ejbJarInfo);

            } catch (OpenEJBException e) {
                ConfigUtils.logger.warning("conf.0004", ejbModule.getJarLocation(), e.getMessage());
                throw e;
            }
        }

        for (PersistenceModule persistenceModule : appModule.getPersistenceModules()) {
            String rootUrl = persistenceModule.getRootUrl();
            Persistence persistence = persistenceModule.getPersistence();
            for (PersistenceUnit persistenceUnit : persistence.getPersistenceUnit()) {
                PersistenceUnitInfo info = new PersistenceUnitInfo();
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

        // process JNDI refs... all JDNI refs for the whole application
        // must be processed at the same time
        JndiEncInfoBuilder.initJndiReferences(appModule, appInfo);

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

            JndiEncInfoBuilder jndiEncInfoBuilder = new JndiEncInfoBuilder(appInfo.ejbJars);
            clientInfo.jndiEnc = jndiEncInfoBuilder.build(applicationClient, clientModule.getJarLocation(), clientInfo.moduleId);
            appInfo.clients.add(clientInfo);
        }

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
                connectorInfo.libs.add(file.getAbsolutePath());
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
                resourceInfo.properties.putAll(getSystemProperties(resourceInfo.id, "RESOURCE"));
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
                    resourceInfo.properties.putAll(getSystemProperties(resourceInfo.id, "RESOURCE"));
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

                    mdbContainerInfo.properties.putAll(getSystemProperties(mdbContainerInfo.id, "CONTAINER"));
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
                resourceInfo.properties.putAll(getSystemProperties(resourceInfo.id, "RESOURCE"));
                connectorInfo.adminObject.add(resourceInfo);
            }

            appInfo.connectors.add(connectorInfo);
        }

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

            JndiEncInfoBuilder jndiEncInfoBuilder = new JndiEncInfoBuilder(appInfo.ejbJars);
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

        appInfo.jarPath = appModule.getJarLocation();
        appInfo.watchedResources.addAll(appModule.getWatchedResources());
        List<URL> additionalLibraries = appModule.getAdditionalLibraries();
        for (URL url : additionalLibraries) {
            File file = new File(url.getPath());
            appInfo.libs.add(file.getAbsolutePath());
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

                List<HandlerChainInfo> handlerChains = toHandlerChainInfo(port.getHandlerChains());
                portInfo.handlerChains.addAll(handlerChains);

                // todo configure jaxrpc mappings here
                
                portMap.add(portInfo);
            }
        }
        return portMap;
    }

    public static List<HandlerChainInfo> toHandlerChainInfo(HandlerChains chains) {
        List<HandlerChainInfo> handlerChains = new ArrayList<HandlerChainInfo>();
        if (chains == null) return handlerChains;

        for (HandlerChain handlerChain : chains.getHandlerChain()) {
            HandlerChainInfo handlerChainInfo = new HandlerChainInfo();
            handlerChainInfo.serviceNamePattern = handlerChain.getServiceNamePattern();
            handlerChainInfo.portNamePattern = handlerChain.getPortNamePattern();
            handlerChainInfo.protocolBindings.addAll(handlerChain.getProtocolBindings());
            for (Handler handler : handlerChain.getHandler()) {
                HandlerInfo handlerInfo = new HandlerInfo();
                handlerInfo.handlerName = handler.getHandlerName();
                handlerInfo.handlerClass = handler.getHandlerClass();
                handlerInfo.soapHeaders.addAll(handler.getSoapHeader());
                handlerInfo.soapRoles.addAll(handler.getSoapRole());
                for (ParamValue param : handler.getInitParam()) {
                    handlerInfo.initParams.setProperty(param.getParamName(), param.getParamValue());
                }
                handlerChainInfo.handlers.add(handlerInfo);
            }
            handlerChains.add(handlerChainInfo);
        }
        return handlerChains;
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

    private static class DefaultService {
        private final Class<? extends Service> type;
        private final String id;

        public DefaultService(String id, Class<? extends Service> type) {
            this.id = id;
            this.type = type;
        }
    }

    private static final Map<Class<? extends ServiceInfo>, DefaultService> defaultProviders = new HashMap<Class<? extends ServiceInfo>, DefaultService>();

    private static final Map<Class<? extends ServiceInfo>, Class<? extends Service>> types = new HashMap<Class<? extends ServiceInfo>, Class<? extends Service>>();

    static {
        defaultProviders.put(MdbContainerInfo.class, new DefaultService("MESSAGE", Container.class));
        defaultProviders.put(StatefulSessionContainerInfo.class, new DefaultService("STATEFUL", Container.class));
        defaultProviders.put(StatelessSessionContainerInfo.class, new DefaultService("STATELESS", Container.class));
        defaultProviders.put(CmpEntityContainerInfo.class, new DefaultService("CMP_ENTITY", Container.class));
        defaultProviders.put(BmpEntityContainerInfo.class, new DefaultService("BMP_ENTITY", Container.class));
        defaultProviders.put(SecurityServiceInfo.class, new DefaultService("SecurityService", SecurityService.class));
        defaultProviders.put(TransactionServiceInfo.class, new DefaultService("TransactionManager", TransactionManager.class));
        defaultProviders.put(ConnectionManagerInfo.class, new DefaultService("ConnectionManager", ConnectionManager.class));
        defaultProviders.put(ProxyFactoryInfo.class, new DefaultService("ProxyFactory", ProxyFactory.class));

        for (Map.Entry<Class<? extends ServiceInfo>, DefaultService> entry : defaultProviders.entrySet()) {
            types.put(entry.getKey(), entry.getValue().type);
        }

        types.put(ResourceInfo.class, Resource.class);
    }


    public <T extends ServiceInfo> T configureService(Class<? extends T> type) throws OpenEJBException {
        Service service = getDefaultService(type);

        return configureService(service, type);
    }

    private <T extends ServiceInfo>Service getDefaultService(Class<? extends T> type) throws OpenEJBException {
        DefaultService defaultService = defaultProviders.get(type);

        Service service = null;
        try {
            service = JaxbOpenejb.create(defaultService.type);
            service.setType(defaultService.id);
        } catch (Exception e) {
            throw new OpenEJBException("Cannot instantiate class " + defaultService.type.getName(), e);
        }
        return service;
    }


    public <T extends ServiceInfo> T configureService(Service service, Class<? extends T> infoType) throws OpenEJBException {
        if (infoType == null) throw new NullPointerException("type");

        if (service == null) {
            service = getDefaultService(infoType);
        }

        String providerType = service.getClass().getSimpleName();

        ServiceProvider provider = resolveServiceProvider(service, infoType);

        if (provider == null){
            throw new NoSuchProviderException("Cannot determine a default provider for Service("+service.getId() +", "+infoType.getSimpleName()+")");
        }

        if (service.getId() == null) service.setId(provider.getId());

        logger.info("Configuring Service(id=" + service.getId() + ", type=" + provider.getService() + ", provider-id=" + provider.getId() + ")");

        Properties props = new Properties();
        props.putAll(provider.getProperties());
        props.putAll(service.getProperties());
        props.putAll(getSystemProperties(service.getId(), provider.getService()));

        if (providerType != null && !provider.getService().equals(providerType)) {
            throw new OpenEJBException(messages.format("conf.4902", service.getId(), providerType));
        }

        T info = null;

        try {
            info = infoType.newInstance();
        } catch (Exception e) {
            throw new OpenEJBException("Cannot instantiate class " + infoType.getName(), e);
        }

        info.service = provider.getService();
        info.types.addAll(provider.getTypes());
        info.description = provider.getDescription();
        info.displayName = provider.getDisplayName();
        info.className = provider.getClassName();
        info.factoryMethod = provider.getFactoryName();
        info.id = service.getId();
        info.properties = props;
        info.constructorArgs.addAll(parseConstructorArgs(provider));

        return info;
    }

    @SuppressWarnings({"unchecked"})
    private ServiceProvider resolveServiceProvider(Service service, Class infoType) throws OpenEJBException {

        if (service.getProvider() != null) {
            return ServiceUtils.getServiceProvider(service.getProvider());
        }

        if (service.getType() != null) {
            return ServiceUtils.getServiceProviderByType(service.getClass().getSimpleName(), service.getType());
        }

        if (service.getId() != null) {
            try {
                return ServiceUtils.getServiceProvider(service.getId());
            } catch (NoSuchProviderException e) {
            }
        }

        if (infoType != null) {
            Service defaultService = getDefaultService(infoType);
            return resolveServiceProvider(defaultService, null);
        }

        return null;
    }

    public <T extends ServiceInfo>T configureService(String id, Class<? extends T> type) throws OpenEJBException {
        return configureService(type, id, null, id, null);
    }

    /**
     * Resolving the provider for a particular service follows this algorithm:
     *
     * 1.  Attempt to load the provider specified by the 'providerId'.
     * 2.  If this fails, throw NoSuchProviderException
     * 3.  If providerId is null, attempt to load the specified provider using the 'serviceId' as the 'providerId'
     * 4.  If this fails, check the hardcoded defaults for a default providerId using the supplied 'type'
     * 5.  If this fails, throw NoSuchProviderException
     */
    public <T extends ServiceInfo>T configureService(Class<? extends T> type, String serviceId, Properties declaredProperties, String providerId, String serviceType) throws OpenEJBException {
        Class<? extends Service> serviceClass = types.get(type);
        Service service = null;
        try {
            service = serviceClass.newInstance();
        } catch (Exception e) {
            throw new OpenEJBException("Cannot instantiate service class '" + serviceClass.getName() + "'", e);
        }
        service.setId(serviceId);
        service.setProvider(providerId);

        if (declaredProperties != null) {
            service.getProperties().putAll(declaredProperties);
        }

        return configureService(service, type);
    }

    private Properties getSystemProperties(String serviceId, String serviceType) {
        String fullPrefix = serviceType.toUpperCase() + "." + serviceId + ".";
        String fullPrefix2 = serviceType.toUpperCase() + "." + serviceId + "|";
        String shortPrefix = serviceId + ".";
        String shortPrefix2 = serviceId + "|";

        // Override with system properties
        Properties serviceProperties = new Properties();
        Properties sysProps = new Properties(System.getProperties());
        sysProps.putAll(SystemInstance.get().getProperties());
        for (Map.Entry<Object, Object> entry : sysProps.entrySet()) {
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                if (name.startsWith(fullPrefix)) {
                    name = name.substring(fullPrefix.length());
                    serviceProperties.setProperty(name, (String) value);
                } else if (name.startsWith(fullPrefix2)) {
                    name = name.substring(fullPrefix2.length());
                    serviceProperties.setProperty(name, (String) value);
                } else if (name.startsWith(shortPrefix)) {
                    name = name.substring(shortPrefix.length());
                    serviceProperties.setProperty(name, (String) value);
                } else if (name.startsWith(shortPrefix2)) {
                    name = name.substring(shortPrefix2.length());
                    serviceProperties.setProperty(name, (String) value);
                }
            }
        }
        return serviceProperties;
    }

    static Map<String, Class<? extends ContainerInfo>> containerTypes = new HashMap<String, Class<? extends ContainerInfo>>();

    static {
        containerTypes.put(BeanTypes.STATELESS, StatelessSessionContainerInfo.class);
        containerTypes.put(BeanTypes.STATEFUL, StatefulSessionContainerInfo.class);
        containerTypes.put(BeanTypes.BMP_ENTITY, BmpEntityContainerInfo.class);
        containerTypes.put(BeanTypes.CMP_ENTITY, CmpEntityContainerInfo.class);
        containerTypes.put(BeanTypes.MESSAGE, MdbContainerInfo.class);
    }

    protected static Class<? extends ContainerInfo> getContainerInfoType(String ctype) {
        return containerTypes.get(ctype);
    }

    private List<String> parseConstructorArgs(ServiceProvider service) {
        String constructor = service.getConstructor();
        if (constructor == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(constructor.split("[ ,]+"));
    }


    protected List<String> getResourceIds() {
        return getResourceIds(null);
    }

    protected List<String> getResourceIds(String type) {
        List<String> resourceIds = new ArrayList<String>();

        OpenEjbConfiguration runningConfig = getRunningConfig();
        if (runningConfig != null) {
            for (ResourceInfo resourceInfo : runningConfig.facilities.resources) {
                if (isResourceType(resourceInfo.service, resourceInfo.types, type)) {
                    resourceIds.add(resourceInfo.id);
                }
            }
        }

        if (sys != null) {
            for (ResourceInfo resourceInfo : sys.facilities.resources) {
                if (isResourceType(resourceInfo.service, resourceInfo.types, type)) {
                    resourceIds.add(resourceInfo.id);
                }
            }

            // The only time we'd have one of these is if we were building
            // the above sys instance
            if (openejb != null) {
                for (Resource resource : openejb.getResource()) {
                    ArrayList<String> types = new ArrayList<String>();
                    if (resource.getType() != null){
                        types.add(resource.getType());
                    }
                    if (isResourceType("Resource", types, type)) {
                        resourceIds.add(resource.getId());
                    }
                }
            }
        }
        return resourceIds;
    }

    @SuppressWarnings({"UnusedDeclaration", "EmptyFinallyBlock"})
    private boolean isResourceType(String service, List<String> types, String type) {
        boolean b = false;
        try {
            if (type == null) return b = true;
            if (service == null) return b = false;
            return b = types.contains(type);
        } finally {
//            System.out.println("isResourceType: "+b+" ["+service +"] ["+type+"] ["+ Join.join(",", types)+"]");
//            Throwable throwable = new Exception().fillInStackTrace();
//            throwable.printStackTrace(System.out);
        }
    }

    protected List<String> getContainerIds() {
        List<String> containerIds = new ArrayList<String>();

        OpenEjbConfiguration runningConfig = getRunningConfig();
        if (runningConfig != null){
            for (ContainerInfo containerInfo : runningConfig.containerSystem.containers) {
                containerIds.add(containerInfo.id);
            }
        }

        if (sys != null) {
            for (ContainerInfo containerInfo : sys.containerSystem.containers) {
                containerIds.add(containerInfo.id);
            }

            // The only time we'd have one of these is if we were building
            // the above sys instance
            if (openejb != null) {
                for (Container container : openejb.getContainer()) {
                    containerIds.add(container.getId());
                }
            }
        }

        return containerIds;
    }

    protected List<ContainerInfo> getContainerInfos() {
        List<ContainerInfo> containers = new ArrayList<ContainerInfo>();

        OpenEjbConfiguration runningConfig = getRunningConfig();
        if (runningConfig != null) {
            for (ContainerInfo containerInfo : runningConfig.containerSystem.containers) {
                containers.add(containerInfo);
            }
        }

        if (sys != null) {
            for (ContainerInfo containerInfo : sys.containerSystem.containers) {
                containers.add(containerInfo);
            }
        }
        return containers;
    }


    private OpenEjbConfiguration getRunningConfig() {
        return SystemInstance.get().getComponent(OpenEjbConfiguration.class);
    }

    private static boolean skipMdb(EnterpriseBeanInfo bean) {
        return bean instanceof MessageDrivenBeanInfo && System.getProperty("duct tape") != null;
    }

}
