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

import static org.apache.openejb.util.URLs.toFile;
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
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.config.sys.ServiceProvider;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.MakeTxLookup;
import org.apache.openejb.util.References;
import org.apache.openejb.util.CircularReferencesException;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
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
import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashSet;
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
        //  J2EE Connectors
        //
        buildConnectorModules(appModule, appInfo);

        //
        //  Persistence Units
        //
        buildPersistenceModules(appModule, appInfo);


        List<String> containerIds = configFactory.getContainerIds();
        for (ConnectorInfo connectorInfo : appInfo.connectors) {
            for (MdbContainerInfo containerInfo : connectorInfo.inbound) {
                containerIds.add(containerInfo.id);
            }
        }

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


                    if (!containerIds.contains(d.getContainerId()) && !skipMdb(bean)) {
                        String msg = messages.format("config.noContainerFound", d.getContainerId(), d.getEjbName());
                        logger.fatal(msg);
                        throw new OpenEJBException(msg);
                    }

                    bean.containerId = d.getContainerId();
                }

                ejbJarInfo.portInfos.addAll(configureWebservices(ejbModule.getWebservices()));
                configureWebserviceSecurity(ejbJarInfo, ejbModule);

                ejbJarInfos.put(ejbJarInfo.jarPath, ejbJarInfo);

                appInfo.ejbJars.add(ejbJarInfo);


            } catch (OpenEJBException e) {
                ConfigUtils.logger.warning("conf.0004", ejbModule.getJarLocation(), e.getMessage());
                throw e;
            }
        }

        // Create the JNDI info builder
        JndiEncInfoBuilder jndiEncInfoBuilder = new JndiEncInfoBuilder(appInfo);

        List<EnterpriseBeanInfo> beans = new ArrayList<EnterpriseBeanInfo>();
        // Build the JNDI tree for each ejb
        for (EjbModule ejbModule : appModule.getEjbModules()) {

            EjbJarInfo ejbJar = ejbJarInfos.get(ejbModule.getJarLocation());

            Map<String, EnterpriseBean> beanData = ejbModule.getEjbJar().getEnterpriseBeansByEjbName();

            for (EnterpriseBeanInfo beanInfo : ejbJar.enterpriseBeans) {
                beans.add(beanInfo);

                // Get the ejb-jar.xml object
                EnterpriseBean enterpriseBean = beanData.get(beanInfo.ejbName);

                // Build the JNDI info tree for the EJB
                JndiEncInfo jndi = jndiEncInfoBuilder.build(enterpriseBean, beanInfo.ejbName, ejbJar.moduleId);

                beanInfo.jndiEnc = jndi;


                jndiEncInfoBuilder.buildDependsOnRefs(ejbModule, enterpriseBean, beanInfo, ejbJar.moduleId);
            }
        }

        // Check for circular references in Singleton @DependsOn
        try {
            References.sort(beans, new References.Visitor<EnterpriseBeanInfo>(){
                public String getName(EnterpriseBeanInfo bean) {
                    return bean.ejbDeploymentId;
                }

                public Set<String> getReferences(EnterpriseBeanInfo bean) {
                    return new LinkedHashSet<String>(bean.dependsOn);
                }
            });
        } catch (CircularReferencesException e) {
            List<List> circuits = e.getCircuits();

        }

        //
        //  Application Clients
        //
        buildClientModules(appModule, appInfo, jndiEncInfoBuilder);

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
            File file = toFile(url);
            try {
                appInfo.libs.add(file.getCanonicalPath());
            } catch (IOException e) {
                throw new OpenEJBException("Invalid application lib path " + file.getAbsolutePath());
            }
        }

        if (appModule.getCmpMappings() != null) {
            try {
                String cmpMappingsXml = JpaJaxbUtil.marshal(EntityMappings.class, appModule.getCmpMappings());
//                System.out.println(cmpMappingsXml);
                appInfo.cmpMappingsXml = cmpMappingsXml;
            } catch (JAXBException e) {
                throw new OpenEJBException("Unable to marshal cmp entity mappings", e);
            }
        }

        ReportValidationResults reportValidationResults = new ReportValidationResults();
        reportValidationResults.deploy(appModule);

        logger.info("config.appLoaded", appInfo.jarPath);
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
            clientInfo.localClients.addAll(clientModule.getLocalClients());
            clientInfo.remoteClients.addAll(clientModule.getRemoteClients());
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
            configureWebserviceSecurity(webAppInfo, webModule);

            for (Servlet servlet : webModule.getWebApp().getServlet()) {
                ServletInfo servletInfo = new ServletInfo();
                servletInfo.servletName = servlet.getServletName();
                servletInfo.servletClass = servlet.getServletClass();
                webAppInfo.servlets.add(servletInfo);
            }

            appInfo.webApps.add(webAppInfo);
        }
    }

    private void buildConnectorModules(AppModule appModule, AppInfo appInfo) throws OpenEJBException {
        String appId = appModule.getModuleId();

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
                File file = toFile(url);
                try {
                    connectorInfo.libs.add(file.getCanonicalPath());
                } catch (IOException e) {
                    throw new IllegalArgumentException("Invalid application lib path " + file.getAbsolutePath());
                }
            }

            ResourceAdapter resourceAdapter = connector.getResourceAdapter();
            if (resourceAdapter.getResourceAdapterClass() != null) {
                String id = getId(connectorModule);
                String className = resourceAdapter.getResourceAdapterClass();

                ServiceProvider provider = new ServiceProvider(className, id, "Resource");
                provider.getTypes().add(className);

                ServiceUtils.registerServiceProvider(appId, provider);

                Resource resource = new Resource(id, className, appId + "#" + id);

                for (ConfigProperty property : resourceAdapter.getConfigProperty()) {
                    String name = property.getConfigPropertyName();
                    String value = property.getConfigPropertyValue();
                    if (value != null) {
                        resource.getProperties().setProperty(name, value);
                    }
                }
                connectorInfo.resourceAdapter = configFactory.configureService(resource, ResourceInfo.class);
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

                    String id = getId(connection, outbound, connectorModule);
                    String className = connection.getManagedConnectionFactoryClass();
                    String type = connection.getConnectionFactoryInterface();

                    ServiceProvider provider = new ServiceProvider(className, id, "Resource");
                    provider.getTypes().add(type);

                    ServiceUtils.registerServiceProvider(appId, provider);

                    Resource resource = new Resource(id, type, appId + "#" + id);
                    Properties properties = resource.getProperties();
                    for (ConfigProperty property : connection.getConfigProperty()) {
                        String name = property.getConfigPropertyName();
                        String value = property.getConfigPropertyValue();
                        if (value != null) {
                            properties.setProperty(name, value);
                        }
                    }
                    properties.setProperty("TransactionSupport", transactionSupport);
                    properties.setProperty("ResourceAdapter", connectorInfo.resourceAdapter.id);

                    ResourceInfo resourceInfo = configFactory.configureService(resource, ResourceInfo.class);
                    connectorInfo.outbound.add(resourceInfo);
                }
            }

            InboundResource inbound = resourceAdapter.getInboundResourceAdapter();
            if (inbound != null) {
                for (MessageListener messageListener : inbound.getMessageAdapter().getMessageListener()) {
                    String id = getId(messageListener, inbound, connectorModule);

                    Container container = new Container(id, "MESSAGE", null);

                    Properties properties = container.getProperties();
                    properties.setProperty("ResourceAdapter", connectorInfo.resourceAdapter.id);
                    properties.setProperty("MessageListenerInterface", messageListener.getMessageListenerType());
                    properties.setProperty("ActivationSpecClass", messageListener.getActivationSpec().getActivationSpecClass());

                    MdbContainerInfo mdbContainerInfo = configFactory.configureService(container, MdbContainerInfo.class);
                    connectorInfo.inbound.add(mdbContainerInfo);
                }
            }

            for (AdminObject adminObject : resourceAdapter.getAdminObject()) {

                String id = getId(adminObject, resourceAdapter, connectorModule);
                String className = adminObject.getAdminObjectClass();
                String type = adminObject.getAdminObjectInterface();

                ServiceProvider provider = new ServiceProvider(className, id, "Resource");
                provider.getTypes().add(type);

                ServiceUtils.registerServiceProvider(appId, provider);

                Resource resource = new Resource(id, type, appId + "#" + id);
                Properties properties = resource.getProperties();
                for (ConfigProperty property : adminObject.getConfigProperty()) {
                    String name = property.getConfigPropertyName();
                    String value = property.getConfigPropertyValue();
                    if (value != null) {
                        properties.setProperty(name, value);
                    }
                }
                ResourceInfo resourceInfo = configFactory.configureService(resource, ResourceInfo.class);
                connectorInfo.adminObject.add(resourceInfo);
            }

            appInfo.connectors.add(connectorInfo);
        }
    }

    private String getId(AdminObject adminObject, ResourceAdapter resourceAdapter, ConnectorModule connectorModule) {
        String id;
        if (adminObject.getId() != null) {
            id = adminObject.getId();
        } else if (resourceAdapter.getAdminObject().size() == 1) {
            id = connectorModule.getModuleId();
        } else {
            id = connectorModule.getModuleId() + "-" + adminObject.getAdminObjectInterface();
        }
        return id;
    }

    private String getId(MessageListener messageListener, InboundResource inbound, ConnectorModule connectorModule) {
        String id;
        if (messageListener.getId() != null) {
            id = messageListener.getId();
        } else if (inbound.getMessageAdapter().getMessageListener().size() == 1) {
            id = connectorModule.getModuleId();
        } else {
            id = connectorModule.getModuleId() + "-" + messageListener.getMessageListenerType();
        }
        return id;
    }

    private String getId(ConnectionDefinition connection, OutboundResourceAdapter outbound, ConnectorModule connectorModule) {
        String id;
        if (connection.getId() != null) {
            id = connection.getId();
        } else if (outbound.getConnectionDefinition().size() == 1) {
            id = connectorModule.getModuleId();
        } else {
            id = connectorModule.getModuleId() + "-" + connection.getConnectionFactoryInterface();
        }
        return id;
    }

    private String getId(ConnectorModule connectorModule) {
        String id = connectorModule.getConnector().getResourceAdapter().getId();
        if (id == null) {
            id = connectorModule.getModuleId() + "RA";
        }
        return id;
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

                Properties overrides = ConfigurationFactory.getSystemProperties(info.name, "PersistenceUnit");
                for (Map.Entry<Object, Object> entry : overrides.entrySet()) {
                    Object property = entry.getKey();
                    Object value = entry.getValue();
                    if (info.properties.contains(property)){
                        logger.debug("Overriding persistence-unit "+info.name +" property " + property + "="+value);
                    } else {
                        logger.debug("Adding persistence-unit "+info.name +" property " + property + "="+value);
                    }
                    info.properties.put(property, value);
                }

                PersistenceProviderProperties.apply(info);


                // Persistence Unit Root Url
                appInfo.persistenceUnits.add(info);
            }
        }
    }


    public static class PersistenceProviderProperties {
        private static void apply(PersistenceUnitInfo info) {
            // The result is that OpenEJB-specific configuration can be avoided when
            // using OpenEJB + Hibernate or another vendor.  A second benefit is that
            // if another vendor is used in production, the value will automatically
            // be reset for using OpenEJB in the test environment.  Ensuring the strategy
            // doesn't start with "org.hibernate.transaction" allows for a custom lookup
            // strategy to be used and not overridden.

            // DMB: This whole block could be a map, but I left it this way just
            // in case we decided we wanted to do other custom handing for the
            // providers listed.
            if ("org.hibernate.ejb.HibernatePersistence".equals(info.provider)){

                String lookupProperty = "hibernate.transaction.manager_lookup_class";
                String openejbLookupClass = MakeTxLookup.HIBERNATE_FACTORY;

                String className = info.properties.getProperty(lookupProperty);

                if (className == null || className.startsWith("org.hibernate.transaction")){
                    info.properties.setProperty(lookupProperty, openejbLookupClass);
                    logger.debug("Adjusting PersistenceUnit(name="+info.name+") property to "+lookupProperty+"="+openejbLookupClass);
                }
            } else if ("oracle.toplink.essentials.PersistenceProvider".equals(info.provider) ||
                    "oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider".equals(info.provider) ){

                String lookupProperty = "toplink.target-server";
                String openejbLookupClass = MakeTxLookup.TOPLINK_FACTORY;

                String className = info.properties.getProperty(lookupProperty);

                if (className == null || className.startsWith("oracle.toplink.transaction")){
                    info.properties.setProperty(lookupProperty, openejbLookupClass);
                    logger.debug("Adjusting PersistenceUnit(name="+info.name+") property to "+lookupProperty+"="+openejbLookupClass);
                }
                } else if ("org.eclipse.persistence.jpa.PersistenceProvider".equals(info.provider) || "org.eclipse.persistence.jpa.osgi.PersistenceProvider".equals(info.provider)){

                String lookupProperty = "eclipselink.target-server";
                String openejbLookupClass = MakeTxLookup.ECLIPSELINK_FACTORY;

                String className = info.properties.getProperty(lookupProperty);

                if (className == null || className.startsWith("org.eclipse.persistence.transaction")){
                    info.properties.setProperty(lookupProperty, openejbLookupClass);
                    logger.debug("Adjusting PersistenceUnit(name="+info.name+") property to "+lookupProperty+"="+openejbLookupClass);
                }
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

    void configureWebserviceSecurity(WebAppInfo info, WebModule module) {
        // no security to configure for WebModule 
        // --> this method should be removed
    }
    
    /*
     * left package-local for a unit test
     */
    void configureWebserviceSecurity(EjbJarInfo ejbJarInfo, EjbModule ejbModule) {
        Object altDD = ejbModule.getOpenejbJar();
	List<PortInfo> infoList = ejbJarInfo.portInfos;
	
	configureWebserviceScurity(infoList, altDD);
    }
    
    private void configureWebserviceScurity(List<PortInfo> infoList, Object altDD) {
        if (altDD == null || (! (altDD instanceof OpenejbJar))) return;
        
        OpenejbJar openejbJar = (OpenejbJar) altDD;
        Map<String, EjbDeployment> deploymentsByEjbName = openejbJar.getDeploymentsByEjbName();
        
        for (PortInfo portInfo : infoList) {
            EjbDeployment deployment = deploymentsByEjbName.get(portInfo.serviceLink);
            
            if (deployment == null) continue;
            portInfo.realmName = deployment.getProperties().getProperty("webservice.security.realm");
            portInfo.securityRealmName = deployment.getProperties().getProperty("webservice.security.securityRealm");
            if (deployment.getProperties().getProperty("webservice.security.transportGarantee") != null) {
                portInfo.transportGuarantee = deployment.getProperties().getProperty("webservice.security.transportGarantee");
            } else {
                portInfo.transportGuarantee = "NONE";
            }

            if (deployment.getProperties().getProperty("webservice.security.authMethod") != null) {
                portInfo.authMethod = deployment.getProperties().getProperty("webservice.security.authMethod");
            } else {
                portInfo.authMethod = "NONE";
            }
            portInfo.properties = deployment.getProperties();
        }
    }
    
    private static boolean skipMdb(EnterpriseBeanInfo bean) {
        return bean instanceof MessageDrivenBeanInfo && System.getProperty("duct tape") != null;
    }

}
