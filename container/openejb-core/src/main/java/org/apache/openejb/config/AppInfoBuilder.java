/*
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

import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.ClassListInfo;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.EntityManagerFactoryCallable;
import org.apache.openejb.assembler.classic.HandlerChainInfo;
import org.apache.openejb.assembler.classic.IdPropertiesInfo;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.assembler.classic.MessageDrivenBeanInfo;
import org.apache.openejb.assembler.classic.PersistenceUnitInfo;
import org.apache.openejb.assembler.classic.PortInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.ServletInfo;
import org.apache.openejb.assembler.classic.ValidatorBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.config.sys.ServiceProvider;
import org.apache.openejb.jee.AdminObject;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.ConfigProperty;
import org.apache.openejb.jee.ConnectionDefinition;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.InboundResourceadapter;
import org.apache.openejb.jee.MessageListener;
import org.apache.openejb.jee.OutboundResourceAdapter;
import org.apache.openejb.jee.PortComponent;
import org.apache.openejb.jee.ResourceAdapter;
import org.apache.openejb.jee.ServiceImplBean;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebserviceDescription;
import org.apache.openejb.jee.Webservices;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.JpaJaxbUtil;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.PojoDeployment;
import org.apache.openejb.jpa.integration.MakeTxLookup;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.CircularReferencesException;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.References;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.apache.openejb.util.URLs.toFile;

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
        final AppInfo appInfo = new AppInfo();
        appInfo.appId = appModule.getModuleId();
        appInfo.path = appModule.getJarLocation();
        appInfo.standaloneModule = appModule.isStandaloneModule();
        appInfo.delegateFirst = appModule.isDelegateFirst();
        appInfo.watchedResources.addAll(appModule.getWatchedResources());
        appInfo.mbeans.addAll(appModule.getAdditionalLibMbeans());
        appInfo.jaxRsProviders.addAll(appModule.getJaxRsProviders());
        appInfo.properties.putAll(appModule.getProperties());

        if (appInfo.appId == null) throw new IllegalArgumentException("AppInfo.appId cannot be null");
        if (appInfo.path == null) appInfo.path = appInfo.appId;

        buildPojoConfiguration(appModule, appInfo);

        buildAppResources(appModule, appInfo);
        buildAppServices(appModule, appInfo);

        //
        //  J2EE Connectors
        //
        buildConnectorModules(appModule, appInfo);

        //
        //  Persistence Units
        //
        buildPersistenceModules(appModule, appInfo);


        final List<String> containerIds = configFactory.getContainerIds();
        for (ConnectorInfo connectorInfo : appInfo.connectors) {
            for (MdbContainerInfo containerInfo : connectorInfo.inbound) {
                containerIds.add(containerInfo.id);
            }
        }

        //
        //  EJB Jars
        //
        final Map<EjbModule, EjbJarInfo> ejbJarInfos = new HashMap<EjbModule, EjbJarInfo>();
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            try {
                EjbJarInfo ejbJarInfo = ejbJarInfoBuilder.buildInfo(ejbModule);
                ejbJarInfo.mbeans = ejbModule.getMbeans();

                Map<String, EjbDeployment> deploymentsByEjbName = ejbModule.getOpenejbJar().getDeploymentsByEjbName();

                for (EnterpriseBeanInfo bean : ejbJarInfo.enterpriseBeans) {
                    EjbDeployment d = deploymentsByEjbName.get(bean.ejbName);

                    /*
                     * JRG - there's probably a better way of handling this, but this code handles the case when:
                     * 
                     * A connector with two or more inbound adapter is registered, causing two containers named with the format:
                     * 	<moduleId>-<message listener interface>
                     * 
                     * This code adjusts the container id for the associated MDBs by sticking the message listener interface on the end.
                     * 
                     */
                    if (bean instanceof MessageDrivenBeanInfo && !containerIds.contains(d.getContainerId()) && !skipMdb(bean)) {
                		MessageDrivenBeanInfo mdb = (MessageDrivenBeanInfo) bean;
                		String newContainerId = d.getContainerId() + "-" + mdb.mdbInterface;
                		if (containerIds.contains(newContainerId)) {
                			d.setContainerId(newContainerId);
                		}
                    }

                    if (!containerIds.contains(d.getContainerId()) && !skipMdb(bean)) {
                        String msg = messages.format("config.noContainerFound", d.getContainerId(), d.getEjbName());
                        logger.fatal(msg);
                        throw new OpenEJBException(msg);
                    }

                    bean.containerId = d.getContainerId();
                }


                for (PojoDeployment pojoDeployment : ejbModule.getOpenejbJar().getPojoDeployment()) {
                    final IdPropertiesInfo info = new IdPropertiesInfo();
                    info.id = pojoDeployment.getClassName();
                    info.properties.putAll(pojoDeployment.getProperties());
                    ejbJarInfo.pojoConfigurations.add(info);
                }

                ejbJarInfo.validationInfo = ValidatorBuilder.getInfo(ejbModule.getValidationConfig());
                ejbJarInfo.portInfos.addAll(configureWebservices(ejbModule.getWebservices()));
                ejbJarInfo.uniqueId = ejbModule.getUniqueId();
                configureWebserviceSecurity(ejbJarInfo, ejbModule);

                ejbJarInfos.put(ejbModule, ejbJarInfo);

                appInfo.ejbJars.add(ejbJarInfo);


            } catch (OpenEJBException e) {
                ConfigUtils.logger.warning("conf.0004", ejbModule.getJarLocation(), e.getMessage());
                throw e;
            }
        }
        // Create the JNDI info builder
        final JndiEncInfoBuilder jndiEncInfoBuilder = new JndiEncInfoBuilder(appInfo);
        if (appModule.getApplication() != null) {
            //TODO figure out how to prevent adding stuff to the module and comp contexts from the application
            //or maybe validate the xml so this won't happen.
            jndiEncInfoBuilder.build(appModule.getApplication(), appInfo.appId, null, appModule.getModuleUri(), new JndiEncInfo(), new JndiEncInfo());
        }

        final List<EnterpriseBeanInfo> beans = new ArrayList<EnterpriseBeanInfo>();
        // Build the JNDI tree for each ejb
        for (EjbModule ejbModule : appModule.getEjbModules()) {

            EjbJarInfo ejbJar = ejbJarInfos.get(ejbModule);

            Map<String, EnterpriseBean> beanData = ejbModule.getEjbJar().getEnterpriseBeansByEjbName();

            for (EnterpriseBeanInfo beanInfo : ejbJar.enterpriseBeans) {
                beans.add(beanInfo);

                // Get the ejb-jar.xml object
                EnterpriseBean enterpriseBean = beanData.get(beanInfo.ejbName);

                // Build the JNDI info tree for the EJB
                jndiEncInfoBuilder.build(enterpriseBean, beanInfo.ejbName, ejbJar.moduleName, ejbModule.getModuleUri(), ejbJar.moduleJndiEnc, beanInfo.jndiEnc);


                jndiEncInfoBuilder.buildDependsOnRefs(ejbModule, enterpriseBean, beanInfo, ejbJar.moduleName);
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
            // List<List> circuits = e.getCircuits();
            // TODO Seems we lost circular reference detection, or we do it elsewhere and don't need it here
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
        final List<URL> additionalLibraries = appModule.getAdditionalLibraries();
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
                appInfo.cmpMappingsXml = JpaJaxbUtil.marshal(EntityMappings.class, appModule.getCmpMappings());
            } catch (JAXBException e) {
                throw new OpenEJBException("Unable to marshal cmp entity mappings", e);
            }
        }

        ReportValidationResults reportValidationResults = new ReportValidationResults();
        reportValidationResults.deploy(appModule);

        logger.info("config.appLoaded", appInfo.path);

        appInfo.webAppAlone = appModule.isWebapp();

        return appInfo;

    }

    private void buildPojoConfiguration(final AppModule appModule, final AppInfo appInfo) {
        for (Map.Entry<String, PojoConfiguration> config : appModule.getPojoConfigurations().entrySet()) {
            final IdPropertiesInfo info = new IdPropertiesInfo();
            info.id = config.getKey();
            info.properties.putAll(config.getValue().getProperties());
            appInfo.pojoConfigurations.add(info);
        }
    }

    private void buildAppServices(final AppModule appModule, final AppInfo appInfo) throws OpenEJBException {
        final Collection<org.apache.openejb.config.sys.Service> services = appModule.getServices();
        for (org.apache.openejb.config.sys.Service service : services) {
            final ServiceInfo info = configFactory.configureService(service, ServiceInfo.class);
            appInfo.services.add(info);
        }
    }

    private void buildAppResources(AppModule module, AppInfo info) {
        for (Resource def : module.getResources()) {
            // the resource is already deployed
            // however we keep its id to be able to undeployed it later
            // note: if ApplicationWide property was specified
            // we want this application be managed only by the container
            // once deployed = not undeployed with the app
            // so we skip the undeployement skipping the id
            if (!def.getProperties().containsKey("ApplicationWide")) {
                info.resourceIds.add(def.getId());
            }
        }
    }

    private void buildClientModules(AppModule appModule, AppInfo appInfo, JndiEncInfoBuilder jndiEncInfoBuilder) throws OpenEJBException {
        for (ClientModule clientModule : appModule.getClientModules()) {
            ApplicationClient applicationClient = clientModule.getApplicationClient();
            ClientInfo clientInfo = new ClientInfo();
            clientInfo.description = applicationClient.getDescription();
            clientInfo.displayName = applicationClient.getDisplayName();
            clientInfo.path = clientModule.getJarLocation();
            clientInfo.mainClass = clientModule.getMainClass();
            clientInfo.localClients.addAll(clientModule.getLocalClients());
            clientInfo.remoteClients.addAll(clientModule.getRemoteClients());
            clientInfo.callbackHandler = applicationClient.getCallbackHandler();
            clientInfo.moduleId = getClientModuleId(clientModule);
            clientInfo.watchedResources.addAll(clientModule.getWatchedResources());
            clientInfo.validationInfo = ValidatorBuilder.getInfo(clientModule.getValidationConfig());
            clientInfo.uniqueId = clientModule.getUniqueId();

            jndiEncInfoBuilder.build(applicationClient, clientModule.getJarLocation(), clientInfo.moduleId, clientModule.getModuleUri(), clientInfo.jndiEnc, clientInfo.jndiEnc);
            appInfo.clients.add(clientInfo);
        }
    }

    private void buildWebModules(AppModule appModule, JndiEncInfoBuilder jndiEncInfoBuilder, AppInfo appInfo) throws OpenEJBException {
        for (WebModule webModule : appModule.getWebModules()) {
            WebApp webApp = webModule.getWebApp();
            WebAppInfo webAppInfo = new WebAppInfo();
            webAppInfo.description = webApp.getDescription();
            webAppInfo.displayName = webApp.getDisplayName();
            webAppInfo.path = webModule.getJarLocation();
            webAppInfo.moduleId = webModule.getModuleId();
            webAppInfo.watchedResources.addAll(webModule.getWatchedResources());
            webAppInfo.validationInfo = ValidatorBuilder.getInfo(webModule.getValidationConfig());
            webAppInfo.uniqueId = webModule.getUniqueId();
            webAppInfo.restApplications.addAll(webModule.getRestApplications());
            webAppInfo.restClass.addAll(webModule.getRestClasses());
            webAppInfo.ejbWebServices.addAll(webModule.getEjbWebServices());
            webAppInfo.ejbRestServices.addAll(webModule.getEjbRestServices());
            webAppInfo.jaxRsProviders.addAll(webModule.getJaxrsProviders());

            for (Map.Entry<String, Set<String>> entry : webModule.getWebAnnotatedClasses().entrySet()) {
                final ClassListInfo info = new ClassListInfo();
                info.name = entry.getKey();
                info.list.addAll(entry.getValue());
                webAppInfo.webAnnotatedClasses.add(info);
            }

            for (Map.Entry<String, Set<String>> entry : webModule.getJsfAnnotatedClasses().entrySet()) {
                final ClassListInfo info = new ClassListInfo();
                info.name = entry.getKey();
                info.list.addAll(entry.getValue());
                webAppInfo.jsfAnnotatedClasses.add(info);
            }

            webAppInfo.host = webModule.getHost();

            if (!webModule.isStandaloneModule()) {
                webAppInfo.contextRoot = appModule.getModuleId() + "/" + webModule.getContextRoot();
            } else {
                webAppInfo.contextRoot = webModule.getContextRoot();
            }

            jndiEncInfoBuilder.build(webApp, webModule.getJarLocation(), webAppInfo.moduleId, webModule.getModuleUri(), webAppInfo.jndiEnc, webAppInfo.jndiEnc);

            webAppInfo.portInfos.addAll(configureWebservices(webModule.getWebservices()));
            // configureWebserviceSecurity(webAppInfo, webModule);: was empty

            for (Servlet servlet : webModule.getWebApp().getServlet()) {
                ServletInfo servletInfo = new ServletInfo();
                servletInfo.servletName = servlet.getServletName();
                servletInfo.servletClass = servlet.getServletClass();
                servletInfo.mappings = webModule.getWebApp().getServletMappings(servletInfo.servletName);
                webAppInfo.servlets.add(servletInfo);
            }

            appInfo.webApps.add(webAppInfo);
        }
    }

    private void buildConnectorModules(AppModule appModule, AppInfo appInfo) throws OpenEJBException {
        String appId = appModule.getModuleId();

        for (ConnectorModule connectorModule : appModule.getConnectorModules()) {
            //
            // DEVELOPERS NOTE:  if you change the id generation code here, you must change
            // the id generation code in AutoConfig$AppResources
            //

            Connector connector = connectorModule.getConnector();

            ConnectorInfo connectorInfo = new ConnectorInfo();
            connectorInfo.description = connector.getDescription();
            connectorInfo.displayName = connector.getDisplayName();
            connectorInfo.path = connectorModule.getJarLocation();
            connectorInfo.moduleId = connectorModule.getModuleId();
            connectorInfo.watchedResources.addAll(connectorModule.getWatchedResources());
            connectorInfo.validationInfo = ValidatorBuilder.getInfo(connectorModule.getValidationConfig());
            connectorInfo.uniqueId = connectorModule.getUniqueId();
            connectorInfo.mbeans = connectorModule.getMbeans();

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
                    if (connectorInfo.resourceAdapter != null) {
                    	properties.setProperty("ResourceAdapter", connectorInfo.resourceAdapter.id);
                    }

                    ResourceInfo resourceInfo = configFactory.configureService(resource, ResourceInfo.class);
                    connectorInfo.outbound.add(resourceInfo);
                }
            }

            InboundResourceadapter inbound = resourceAdapter.getInboundResourceAdapter();
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

    private String getId(MessageListener messageListener, InboundResourceadapter inbound, ConnectorModule connectorModule) {
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
                
                info.persistenceXMLSchemaVersion = persistence.getVersion();
                info.sharedCacheMode = persistenceUnit.getSharedCacheMode().toString();
                info.validationMode = persistenceUnit.getValidationMode().toString();

                // Handle Properties
                info.properties.putAll(persistenceUnit.getProperties());

                PersistenceProviderProperties.apply(appModule.getClassLoader(), info);


                // Persistence Unit Root Url
                appInfo.persistenceUnits.add(info);
            }
        }
    }

    public static class PersistenceProviderProperties {
        public static final String OPENJPA_RUNTIME_UNENHANCED_CLASSES = "openjpa.RuntimeUnenhancedClasses";
        public static final String DEFAULT_RUNTIME_UNENHANCED_CLASSES = "supported";
        public static final String REMOVE_DEFAULT_RUNTIME_UNENHANCED_CLASSES = "disable";

        public static final String TABLE_PREFIX = "openejb.jpa.table_prefix";
        public static final String OPENJPA_METADATA_REPOSITORY = "openjpa.MetaDataRepository";
        public static final String PREFIX_METADATA_REPOSITORY = "org.apache.openejb.openjpa.PrefixMappingRepository";
        public static final String OPENJPA_SEQUENCE = "openjpa.Sequence";
        public static final String PREFIX_SEQUENCE = "org.apache.openejb.openjpa.PrefixTableJdbcSeq";

        public static final String PROVIDER_PROP = "javax.persistence.provider";
        public static final String TRANSACTIONTYPE_PROP = "javax.persistence.transactionType";
        public static final String JTADATASOURCE_PROP = "javax.persistence.jtaDataSource";
        public static final String NON_JTADATASOURCE_PROP = "javax.persistence.nonJtaDataSource";
        private static final String DEFAULT_PERSISTENCE_PROVIDER = "org.apache.openjpa.persistence.PersistenceProviderImpl";

        public static final String HIBERNATE_TRANSACTION_MANAGER_LOOKUP_CLASS = "hibernate.transaction.manager_lookup_class";
        public static final String HIBERNATE_JTA_PLATFORM = "hibernate.transaction.jta.platform";
        public static final String HIBERNATE_EJB_NAMING_STRATEGY_PROP = "hibernate.ejb.naming_strategy";
        private static final String HIBERNATE_EJB_NAMING_STRATEGY = "org.apache.openejb.jpa.integration.hibernate.PrefixNamingStrategy";

        private static final String ECLIPSELINK_SESSION_CUSTOMIZER = "eclipselink.session.customizer";
        private static final String PREFIX_SESSION_CUSTOMIZER = "org.apache.openejb.jpa.integration.eclipselink.PrefixSessionCustomizer";

        private static String providerEnv;
        private static String transactionTypeEnv;
        private static String jtaDataSourceEnv;
        private static String nonJtaDataSourceEnv;

        static {
            providerEnv = SystemInstance.get().getOptions().get(PROVIDER_PROP, (String) null);
            transactionTypeEnv = SystemInstance.get().getOptions().get(TRANSACTIONTYPE_PROP, (String) null);
            jtaDataSourceEnv = SystemInstance.get().getOptions().get(JTADATASOURCE_PROP, (String) null);
            nonJtaDataSourceEnv = SystemInstance.get().getOptions().get(NON_JTADATASOURCE_PROP, (String) null);
        }

        /**
         * @param classLoader the temp classloader, take care to only use getResource here
         * @param info the persistence unit info
         */
        private static void apply(final ClassLoader classLoader, final PersistenceUnitInfo info) {
            overrideFromSystemProp(info);

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

                // Apply the overrides that apply to all persistence units of this provider
                override(info, "hibernate");

                String className = info.properties.getProperty(HIBERNATE_JTA_PLATFORM);
                if (className == null) {
                    className = info.properties.getProperty(HIBERNATE_TRANSACTION_MANAGER_LOOKUP_CLASS);
                }
                // info.persistenceUnitRootUrl = null; // to avoid HHH015010

                final String prefix = info.properties.getProperty(TABLE_PREFIX);
                if (prefix != null) {
                    if (info.properties.containsKey(HIBERNATE_EJB_NAMING_STRATEGY_PROP)) {
                        logger.warning("can't statisfy table prefix since you provided a " + HIBERNATE_EJB_NAMING_STRATEGY_PROP + " property");
                    } else {
                        // to pass the config to the impl
                        info.properties.setProperty(HIBERNATE_EJB_NAMING_STRATEGY_PROP, HIBERNATE_EJB_NAMING_STRATEGY);
                    }
                }

                if (className == null || className.startsWith("org.hibernate.transaction") || className.startsWith("org.hibernate.service.jta.platform")){
                    // hibernate 4
                    String key = HIBERNATE_JTA_PLATFORM;
                    String value = MakeTxLookup.HIBERNATE_NEW_FACTORY;

                    if (classLoader.getResource(ClassLoaderUtil.resourceName("org.hibernate.service.jta.platform.spi.JtaPlatform")) == null) {
                        // hibernate 3. In the worse case it is set with a hibernate 4 and hibernate will convert it.
                        key = HIBERNATE_TRANSACTION_MANAGER_LOOKUP_CLASS;
                        value = MakeTxLookup.HIBERNATE_FACTORY;
                    }

                    if (classLoader.getResource(ClassLoaderUtil.resourceName(value)) != null) {
                        info.properties.setProperty(key, value);
                        logger.debug("Adjusting PersistenceUnit(name=" + info.name + ") property to " + key + "=" + value);
                    } else {
                        logger.debug("can't adjust hibernate jta bridge to openejb one");
                    }

                }
            } else if ("oracle.toplink.essentials.PersistenceProvider".equals(info.provider) ||
                    "oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider".equals(info.provider) ){

                // Apply the overrides that apply to all persistence units of this provider
                override(info, "toplink");

                String lookupProperty = "toplink.target-server";
                String openejbLookupClass = MakeTxLookup.TOPLINK_FACTORY;

                final String prefix = info.properties.getProperty(TABLE_PREFIX);
                if (prefix != null) {
                    logger.warning("table prefix feature is not supported for toplink");
                }

                String className = info.properties.getProperty(lookupProperty);

                if (className == null || className.startsWith("oracle.toplink.transaction")){
                    info.properties.setProperty(lookupProperty, openejbLookupClass);
                    logger.debug("Adjusting PersistenceUnit(name="+info.name+") property to "+lookupProperty+"="+openejbLookupClass);
                }
            } else if ("org.eclipse.persistence.jpa.PersistenceProvider".equals(info.provider) || "org.eclipse.persistence.jpa.osgi.PersistenceProvider".equals(info.provider)){

                // Apply the overrides that apply to all persistence units of this provider
                override(info, "eclipselink");

                String lookupProperty = "eclipselink.target-server";
                String openejbLookupClass = MakeTxLookup.ECLIPSELINK_FACTORY;

                String className = info.properties.getProperty(lookupProperty);

                if (className == null || className.startsWith("org.eclipse.persistence.transaction")){
                    if (classLoader.getResource(ClassLoaderUtil.resourceName(openejbLookupClass)) != null) {
                        info.properties.setProperty(lookupProperty, openejbLookupClass);
                        logger.debug("Adjusting PersistenceUnit(name="+info.name+") property to "+lookupProperty+"="+openejbLookupClass);
                    } else {
                        logger.debug("Can't adjusting PersistenceUnit(name="+info.name+") property to " + lookupProperty + "=" + openejbLookupClass + ", using default one");
                    }
                }

                final String prefix = info.properties.getProperty(TABLE_PREFIX);
                if (prefix != null) {
                    if (info.properties.containsKey(ECLIPSELINK_SESSION_CUSTOMIZER)) {
                        logger.warning("can't statisfy table prefix since you provided a " + ECLIPSELINK_SESSION_CUSTOMIZER + " property, add a call to org.apache.openejb.jpa.integration.eclipselink.PrefixSessionCustomizer");
                    } else {
                        // force eager loading otherwise we'll not get the prefix in the customizer
                        info.properties.setProperty(EntityManagerFactoryCallable.OPENEJB_JPA_INIT_ENTITYMANAGER, "true");
                        // to pass the config to the impl
                        info.properties.setProperty(ECLIPSELINK_SESSION_CUSTOMIZER, PREFIX_SESSION_CUSTOMIZER);
                    }
                }
            }  else if (info.provider == null || "org.apache.openjpa.persistence.PersistenceProviderImpl".equals(info.provider)){

                // Apply the overrides that apply to all persistence units of this provider
                override(info, "openjpa");

                String existing = info.properties.getProperty(OPENJPA_RUNTIME_UNENHANCED_CLASSES);

                if (existing == null){
                    info.properties.setProperty(OPENJPA_RUNTIME_UNENHANCED_CLASSES, DEFAULT_RUNTIME_UNENHANCED_CLASSES);
                    logger.debug("Adjusting PersistenceUnit(name=" + info.name + ") property to "
                        + OPENJPA_RUNTIME_UNENHANCED_CLASSES + "=" + DEFAULT_RUNTIME_UNENHANCED_CLASSES);
                } else if (REMOVE_DEFAULT_RUNTIME_UNENHANCED_CLASSES.equals(existing.trim())) {
                    info.properties.remove(OPENJPA_RUNTIME_UNENHANCED_CLASSES);
                    logger.info("Adjusting PersistenceUnit(name=" + info.name + ") removing property "
                                                                    + OPENJPA_RUNTIME_UNENHANCED_CLASSES);
                }

                final String prefix = info.properties.getProperty(TABLE_PREFIX);
                if (prefix != null) {
                    final String mapping = info.properties.getProperty(OPENJPA_METADATA_REPOSITORY);
                    if (mapping != null && !"org.apache.openjpa.jdbc.meta.MappingRepository".equals(mapping)) {
                        throw new OpenEJBRuntimeException("can't honor table prefixes since you provided a custom mapping repository: " + mapping);
                    }
                    info.properties.setProperty(OPENJPA_METADATA_REPOSITORY, PREFIX_METADATA_REPOSITORY + "(prefix=" + prefix + ")");
                    if (!info.properties.containsKey(OPENJPA_SEQUENCE)) {
                        info.properties.setProperty(OPENJPA_SEQUENCE, PREFIX_SEQUENCE + "(prefix=" + prefix + ")");
                    } else {
                        logger.warning("you configured a custom sequence so the prefix will be ignored");
                    }
                }

                final Set<String> keys = new HashSet<String>(info.properties.stringPropertyNames());
                for (String key : keys) {
                    if (key.matches("openjpa.Connection(DriverName|URL|UserName|Password)")) {
                        final Object o = info.properties.remove(key);
                        logger.warning("Removing PersistenceUnit(name=" + info.name + ") property " + key + "=" + o + "  [not valid in a container environment]");
                    } else { // try to convert it if necessary
                        JPAPropertyConverter.Pair pair = JPAPropertyConverter.toOpenJPAValue(key, info.properties.getProperty(key), info.properties);
                        if (pair != null && !info.properties.containsKey(pair.getKey())) {
                            logger.info("Converting PersistenceUnit(name=" + info.name + ") property "
                                    + key + "=" + info.properties.getProperty(key) +  " to " + pair.toString());
                            info.properties.remove(key);
                            info.properties.setProperty(pair.getKey(), pair.getValue());
                        }
                    }
                }
            }

            // Apply the overrides that apply to just this persistence unit
            override(info);
        }

        private static void overrideFromSystemProp(PersistenceUnitInfo info) {
            if (providerEnv != null) {
                info.provider = providerEnv;
            }
            if (info.provider == null) {
                info.provider = DEFAULT_PERSISTENCE_PROVIDER;
            }
            if (jtaDataSourceEnv != null) {
                info.jtaDataSource = jtaDataSourceEnv;
            }
            if (transactionTypeEnv != null) {
                info.transactionType = transactionTypeEnv.toUpperCase();
            }
            if (nonJtaDataSourceEnv != null) {
                info.nonJtaDataSource = nonJtaDataSourceEnv;
            }
        }

        private static void override(PersistenceUnitInfo info) {
            override(info, info.name);
        }

        private static void override(PersistenceUnitInfo info, String prefix) {

            Properties overrides = ConfigurationFactory.getSystemProperties(prefix, "PersistenceUnit");

            for (Map.Entry<Object, Object> entry : overrides.entrySet()) {
                final String property = (String) (prefix.equalsIgnoreCase(info.name) ? entry.getKey() : prefix + "." + entry.getKey());
                final String value = (String) entry.getValue();

                if ("openjpa.Log".equals(property) && info.properties.containsKey("openjpa.Log")) { // we set a default
                    continue;
                }

                if (info.properties.containsKey(property)){
                    logger.debug("Overriding persistence-unit "+info.name +" property " + property + "="+value);
                } else {
                    logger.debug("Adding persistence-unit "+info.name +" property " + property + "="+value);
                }
                info.properties.put(property, value);

                if (property.endsWith("openjpa.Specification")) {
                    info.persistenceXMLSchemaVersion = value.replace("JPA ", "");
                }
            }
        }
    }

    private static String getClientModuleId(ClientModule clientModule) {
        return clientModule.getModuleId();
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
        return bean instanceof MessageDrivenBeanInfo && SystemInstance.get().hasProperty("openejb.geronimo");
    }

}
