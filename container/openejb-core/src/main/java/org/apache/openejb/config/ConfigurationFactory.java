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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.BmpEntityContainerInfo;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.CmpEntityContainerInfo;
import org.apache.openejb.assembler.classic.ConnectionManagerInfo;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.ContainerSystemInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.FacilitiesInfo;
import org.apache.openejb.assembler.classic.HandlerChainInfo;
import org.apache.openejb.assembler.classic.HandlerInfo;
import org.apache.openejb.assembler.classic.JndiContextInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.OpenEjbConfigurationFactory;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;
import static org.apache.openejb.config.ServiceUtils.implies;
import org.apache.openejb.config.sys.AbstractService;
import org.apache.openejb.config.sys.ConnectionManager;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.sys.Deployments;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.config.sys.JndiProvider;
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.config.sys.ProxyFactory;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.config.sys.SecurityService;
import org.apache.openejb.config.sys.ServiceProvider;
import org.apache.openejb.config.sys.TransactionManager;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Handler;
import org.apache.openejb.jee.HandlerChain;
import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.ParamValue;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.SuperProperties;
import org.apache.openejb.util.URISupport;

public class ConfigurationFactory implements OpenEjbConfigurationFactory {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, ConfigurationFactory.class);
    private static final Messages messages = new Messages(ConfigurationFactory.class);

    private String configLocation = "";

    private OpenEjbConfiguration sys;


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

        chain.add(new ClearEmptyMappedName());

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

        chain.add(new ApplyOpenejbJar());

        // TODO: How do we want this plugged in?
        chain.add(new OutputGeneratedDescriptors());

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

        loadPropertiesDeclaredConfiguration(openejb);

        sys = new OpenEjbConfiguration();
        sys.containerSystem = new ContainerSystemInfo();
        sys.facilities = new FacilitiesInfo();


        for (JndiProvider provider : openejb.getJndiProvider()) {
            JndiContextInfo info = configureService(provider, JndiContextInfo.class);
            sys.facilities.remoteJndiContexts.add(info);
        }

        sys.facilities.securityService = configureService(openejb.getSecurityService(), SecurityServiceInfo.class);

        sys.facilities.transactionService = configureService(openejb.getTransactionManager(), TransactionServiceInfo.class);

        List<ResourceInfo> resources = new ArrayList<ResourceInfo>();
        for (Resource resource : openejb.getResource()) {
            ResourceInfo resourceInfo = configureService(resource, ResourceInfo.class);
            resources.add(resourceInfo);
        }
        Collections.sort(resources, new ResourceInfoComparator(resources));

        sys.facilities.resources.addAll(resources);



//        ConnectionManagerInfo service = configureService(openejb.getConnectionManager(), ConnectionManagerInfo.class);
//        sys.facilities.connectionManagers.add(service);

        sys.facilities.intraVmServer = configureService(openejb.getProxyFactory(), ProxyFactoryInfo.class);

        for (Container declaration : openejb.getContainer()) {
            Class<? extends ContainerInfo> infoClass = getContainerInfoType(declaration.getType());

            if (infoClass == null) {
                throw new OpenEJBException("Unrecognized container type " + declaration.getType());
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

    private void loadPropertiesDeclaredConfiguration(Openejb openejb) {

        Properties sysProps = new Properties(System.getProperties());
        sysProps.putAll(SystemInstance.get().getProperties());

        for (Map.Entry<Object, Object> entry : sysProps.entrySet()) {

            Object o = entry.getValue();
            if (!(o instanceof String)) continue;
            if (!((String) o).startsWith("new://")) continue;

            String name = (String) entry.getKey();
            String value = (String) entry.getValue();

            try {
                URI uri = new URI(value);

                openejb.add(toConfigDeclaration(name, uri));
            } catch (URISyntaxException e) {
                logger.error("Error declaring service '" + name + "'. Invalid Service URI '" + value + "'.  java.net.URISyntaxException: " + e.getMessage());
            } catch (OpenEJBException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public Object toConfigDeclaration(String id, URI uri) throws OpenEJBException {
        String serviceType = null;
        try {
            serviceType = uri.getHost();

            Object object = null;
            try {
                object = JaxbOpenejb.create(serviceType);
            } catch (Exception e) {
                throw new OpenEJBException("Invalid URI '" + uri + "'. " + e.getMessage());
            }

            Map<String, String> map = null;
            try {
                map = URISupport.parseParamters(uri);
            } catch (URISyntaxException e) {
                throw new OpenEJBException("Unable to parse URI parameters '" + uri + "'. URISyntaxException: " + e.getMessage());
            }

            if (object instanceof AbstractService) {
                AbstractService service = (AbstractService) object;

                service.setId(id);


                service.setType(map.remove("type"));
                service.setProvider(map.remove("provider"));
                service.getProperties().putAll(map);
            } else if (object instanceof Deployments){
                Deployments deployments = (Deployments) object;
                deployments.setDir(map.remove("dir"));
                deployments.setJar(map.remove("jar"));
            }

            return object;
        } catch (Exception e) {
            throw new OpenEJBException("Error declaring service '" + id + "'. Unable to create Service definition from URI '" + uri.toString() + "'", e);
        }
    }

    public AppInfo configureApplication(File jarFile) throws OpenEJBException {
        logger.debug("Beginning load: " + jarFile.getAbsolutePath());

        AppInfo appInfo;
        try {
            AppModule appModule = deploymentLoader.load(jarFile);
            appInfo = configureApplication(appModule);
        } catch (ValidationFailedException e) {
            logger.warning("configureApplication.loadFailed", jarFile.getAbsolutePath(), e.getMessage()); // DO not include the stacktrace in the message
            throw e;
        } catch (OpenEJBException e) {
            // DO NOT REMOVE THE EXCEPTION FROM THIS LOG MESSAGE
            // removing this message causes NO messages to be printed when embedded
            logger.warning("configureApplication.loadFailed", e, jarFile.getAbsolutePath(), e.getMessage());
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

        logger.info("config.configApp", appModule.getJarLocation());
        deployer.deploy(appModule);
        AppInfoBuilder appInfoBuilder = new AppInfoBuilder(this);

        return appInfoBuilder.build(appModule);
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
        return configureService((Service)null, type);
    }

    private <T extends ServiceInfo>Service getDefaultService(Class<? extends T> type) throws OpenEJBException {
        DefaultService defaultService = defaultProviders.get(type);

        if (defaultService == null) return null;

        Service service;
        try {
            service = JaxbOpenejb.create(defaultService.type);
            service.setType(defaultService.id);
        } catch (Exception e) {
            String name = (defaultService == null || defaultService.type == null) ? "null" : defaultService.type.getName();
            throw new OpenEJBException("Cannot instantiate class " + name, e);
        }
        return service;
    }


    public <T extends ServiceInfo> T configureService(Service service, Class<? extends T> infoType) throws OpenEJBException {
        try {
            if (infoType == null) throw new NullPointerException("type");

            if (service == null) {
                service = getDefaultService(infoType);
                if (service == null){
                    throw new OpenEJBException(messages.format("configureService.noDefaultService", infoType.getName()));
                }
            }


            String providerType = service.getClass().getSimpleName();

            ServiceProvider provider = resolveServiceProvider(service, infoType);

            if (provider == null){
                List<ServiceProvider> providers = ServiceUtils.getServiceProvidersByServiceType(providerType);
                StringBuilder sb = new StringBuilder();
//                for (ServiceProvider p : providers) {
//                    sb.append(System.getProperty("line.separator"));
//                    sb.append("  <").append(p.getService());
//                    sb.append(" id=\"").append(service.getId()).append('"');
//                    sb.append(" provider=\"").append(p.getId()).append("\"/>");
//                }

                List<String> types = new ArrayList<String>();
                for (ServiceProvider p : providers) {
                    for (String type : p.getTypes()) {
                        if (types.contains(type)) continue;
                        types.add(type);
                        sb.append(System.getProperty("line.separator"));
                        sb.append("  <").append(p.getService());
                        sb.append(" id=\"").append(service.getId()).append('"');
                        sb.append(" type=\"").append(type).append("\"/>");
                    }
                }
                String noProviderMessage = messages.format("configureService.noProviderForService", providerType, service.getId(), service.getType(), service.getProvider(), sb.toString());
                throw new NoSuchProviderException(noProviderMessage);
            }

            if (service.getId() == null) service.setId(provider.getId());

            logger.info("configureService.configuring", service.getId(), provider.getService(), provider.getId());

            Properties props = new SuperProperties();
            props.putAll(provider.getProperties());
            props.putAll(service.getProperties());
            props.putAll(getSystemProperties(service.getId(), provider.getService()));

            if (providerType != null && !provider.getService().equals(providerType)) {
                throw new OpenEJBException(messages.format("configureService.wrongProviderType", service.getId(), providerType));
            }

            T info;

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

            specialProcessing(info);


            return info;
        } catch (NoSuchProviderException e) {
            String message = logger.fatal("configureService.failed", e, service.getId());
            throw new OpenEJBException(message + ": " + e.getMessage());
        } catch (Throwable e) {
            String message = logger.fatal("configureService.failed", e, service.getId());
            throw new OpenEJBException(message, e);
        }
    }

    private <T extends ServiceInfo> void specialProcessing(T info) {
        ServiceInfo serviceInfo = info;
        TopicOrQueueDefaults.process(serviceInfo);
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
            if (defaultService != null) {
                return resolveServiceProvider(defaultService, null);
            }
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
        Service service;
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

    protected static Properties getSystemProperties(String serviceId, String serviceType) {
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
        return getResourceIds(type, null);
    }

    protected List<String> getResourceIds(String type, Properties required) {
        List<String> resourceIds = new ArrayList<String>();

        if (required == null) required = new Properties();

        OpenEjbConfiguration runningConfig = getRunningConfig();
        if (runningConfig != null) {
            for (ResourceInfo resourceInfo : runningConfig.facilities.resources) {
                if (isResourceType(resourceInfo.service, resourceInfo.types, type) && implies(required, resourceInfo.properties)) {
                    resourceIds.add(resourceInfo.id);
                }
            }
        }

        if (sys != null) {
            for (ResourceInfo resourceInfo : sys.facilities.resources) {
                if (isResourceType(resourceInfo.service, resourceInfo.types, type) && implies(required, resourceInfo.properties)) {
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
                    if (isResourceType("Resource", types, type) && implies(required, resource.getProperties())) {
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


    private static class TopicOrQueueDefaults {
        public static void process(ServiceInfo provider) {
            if (!provider.service.equals("Resource")) return;
            if (!provider.types.contains("Topic") && !provider.types.contains("Queue")) return;
            if (!provider.className.matches("org.apache.activemq.command.ActiveMQ(Topic|Queue)")) return;

            String dest = provider.properties.getProperty("destination");
            if (dest == null || dest.length() == 0) {
                provider.properties.setProperty("destination", provider.id);
            }
        }
    }

    public static class ResourceInfoComparator implements Comparator<ResourceInfo> {
        private final List<String> ids;
        private static final int EQUAL = 0;
        private static final int GREATER = 1;
        private static final int LESS = -1;

        public ResourceInfoComparator(List<ResourceInfo> resources){
            ids = new ArrayList<String>();
            for (ResourceInfo info : resources) {
                ids.add(info.id);
            }
        }

        public int compare(ResourceInfo a, ResourceInfo b) {
            String refA = getReference(a);
            String refB = getReference(b);

            // both null or the same id
            if (refA == null && refB == null ||
                    refA != null && refA.equals(refB)) {
                return EQUAL;
            }

            // b is referencing a
            if (a.id.equals(refB)) {
                return LESS;
            }

            // a is referencing b
            if (b.id.equals(refA)) {
                return GREATER;
            }

            // a has a ref and b doesn't
            if (refA != null && refB == null){
                return GREATER;
            }

            // b has a ref and a doesn't
            if (refB != null && refA == null){
                return LESS;
            }

            return EQUAL;
        }

        public int hasReference(ResourceInfo info){
            for (Object value : info.properties.values()) {
                if (ids.contains(value)) return GREATER;
            }
            return EQUAL;
        }

        public String getReference(ResourceInfo info){
            for (Object value : info.properties.values()) {
                value = ((String)value).trim();
                if (ids.contains(value)) return (String) value;
            }
            return null;
        }
    }
}
