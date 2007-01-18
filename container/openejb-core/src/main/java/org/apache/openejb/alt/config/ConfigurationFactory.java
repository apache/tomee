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
package org.apache.openejb.alt.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.alt.config.ejb.EjbDeployment;
import org.apache.openejb.alt.config.sys.ConnectionManager;
import org.apache.openejb.alt.config.sys.Connector;
import org.apache.openejb.alt.config.sys.Container;
import org.apache.openejb.alt.config.sys.JndiProvider;
import org.apache.openejb.alt.config.sys.Openejb;
import org.apache.openejb.alt.config.sys.ProxyFactory;
import org.apache.openejb.alt.config.sys.SecurityService;
import org.apache.openejb.alt.config.sys.ServiceProvider;
import org.apache.openejb.alt.config.sys.TransactionManager;
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
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.FacilitiesInfo;
import org.apache.openejb.assembler.classic.JndiContextInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.OpenEjbConfigurationFactory;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

import java.io.File;
import java.io.StringBufferInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Iterator;

public class ConfigurationFactory implements OpenEjbConfigurationFactory {

    private static final Logger logger = Logger.getInstance("OpenEJB.startup", "org.apache.openejb.util.resources");
    private static final Messages messages = new Messages("org.apache.openejb.util.resources");

    private String configLocation = "";

    private List<String> serviceIds = new ArrayList<String>();

    private OpenEjbConfiguration sys;

    private EjbJarInfoBuilder ejbJarInfoBuilder = new EjbJarInfoBuilder();

    private Openejb openejb;

    private DynamicDeployer deployer;
    private final DeploymentLoader deploymentLoader;
    private final boolean offline;

    public ConfigurationFactory() {
        this(false);
    }

    public ConfigurationFactory(boolean offline) {
        this.offline = offline;
        deploymentLoader = new DeploymentLoader();

        DynamicDeployer deployer;
        if (offline) {
            deployer = new AutoDeployer(this);

        } else {
            deployer = new AutoConfigAndDeploy(this);
        }

        deployer = new AnnotationDeployer(deployer);

        boolean shouldValidate = !SystemInstance.get().getProperty("openejb.validation.skip", "false").equalsIgnoreCase("true");
        if (shouldValidate) {
            deployer = new ValidateEjbModule(deployer);
        } else {
            DeploymentLoader.logger.info("Validation is disabled.");
        }
        this.deployer = deployer;
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

    protected void install(ConnectorInfo serviceInfo) throws OpenEJBException {
        if (sys != null) {
            sys.facilities.connectors.add(serviceInfo);
        } else if (!offline) {
            Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
            assembler.createConnector(serviceInfo);
        }
    }

    public OpenEjbConfiguration getOpenEjbConfiguration() throws OpenEJBException {

        if (configLocation != null) {
            openejb = ConfigUtils.readConfig(configLocation);
        } else {
            openejb = new Openejb();
        }

        sys = new OpenEjbConfiguration();
        sys.containerSystem = new ContainerSystemInfo();
        sys.facilities = new FacilitiesInfo();


        for (JndiProvider provider : openejb.getJndiProvider()) {

            JndiContextInfo info = configureService(provider, JndiContextInfo.class);
            sys.facilities.remoteJndiContexts.add(info);
        }

        sys.facilities.securityService = configureService(openejb.getSecurityService(), SecurityServiceInfo.class);

        sys.facilities.transactionService = configureService(openejb.getTransactionService(), TransactionServiceInfo.class);

        for (Connector connector : openejb.getConnector()) {

            ConnectorInfo connectorInfo = configureService(connector, ConnectorInfo.class);
            sys.facilities.connectors.add(connectorInfo);
        }

        ConnectionManagerInfo service = configureService(openejb.getConnectionManager(), ConnectionManagerInfo.class);
        sys.facilities.connectionManagers.add(service);

        sys.facilities.intraVmServer = configureService(openejb.getProxyFactory(), ProxyFactoryInfo.class);

        for (Container declaration : openejb.getContainer()) {

            Class<? extends ContainerInfo> infoClass = getContainerInfoType(declaration.getCtype());

            if (infoClass == null) {
                throw new OpenEJBException("Unrecognized contianer type " + declaration.getCtype());
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

            logger.info("Loaded Module: " + appModule.getJarLocation());

        } catch (OpenEJBException e) {
            String message = messages.format("conf.0004", jarFile.getAbsolutePath(), e.getMessage());
            logger.warning(message);
            throw e;
        }
        return appInfo;
    }

    public EjbJarInfo configureApplication(EjbModule ejbModule) throws OpenEJBException {
        AppModule appModule = new AppModule(ejbModule.getClassLoader(), null);
        appModule.getEjbModules().add(ejbModule);
        AppInfo appInfo = configureApplication(appModule);
        return appInfo.ejbJars.get(0);
    }

    public ClientInfo configureApplication(ClientModule clientModule) throws OpenEJBException {
        AppModule appModule = new AppModule(clientModule.getClassLoader(), null);
        appModule.getClientModules().add(clientModule);
        AppInfo appInfo = configureApplication(appModule);
        return appInfo.clients.get(0);
    }

    public AppInfo configureApplication(AppModule appModule) throws OpenEJBException {
        deployer.deploy(appModule);

        AppInfo appInfo = new AppInfo();
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            try {
                EjbJarInfo ejbJarInfo = ejbJarInfoBuilder.buildInfo(ejbModule);

                Map<String, EjbDeployment> deploymentsByEjbName = ejbModule.getOpenejbJar().getDeploymentsByEjbName();

                for (EnterpriseBeanInfo bean : ejbJarInfo.enterpriseBeans) {
                    EjbDeployment d = deploymentsByEjbName.get(bean.ejbName);

                    if (!getContainerIds().contains(d.getContainerId())) {
                        String msg = messages.format("config.noContainerFound", d.getContainerId(), d.getEjbName());
                        logger.fatal(msg);
                        throw new OpenEJBException(msg);
                    }

                    bean.containerId = d.getContainerId();
                }

                appInfo.ejbJars.add(ejbJarInfo);

            } catch (OpenEJBException e) {
                ConfigUtils.logger.i18n.warning("conf.0004", ejbModule.getJarURI(), e.getMessage());
                throw e;
            }
        }

        for (ClientModule clientModule : appModule.getClientModules()) {

            ApplicationClient applicationClient = clientModule.getApplicationClient();
            ClientInfo clientInfo = new ClientInfo();
            clientInfo.description = applicationClient.getDescription();
            clientInfo.displayName = applicationClient.getDisplayName();
            clientInfo.codebase = clientModule.getJarLocation();
            clientInfo.mainClass = clientModule.getMainClass();
            clientInfo.moduleId = getClientModuleId(clientModule);

            JndiEncInfoBuilder jndiEncInfoBuilder = new JndiEncInfoBuilder(appInfo.ejbJars);
            clientInfo.jndiEnc = jndiEncInfoBuilder.build(applicationClient, clientModule.getJarLocation());
            appInfo.clients.add(clientInfo);
        }

        appInfo.jarPath = appModule.getJarLocation();
        List<URL> additionalLibraries = appModule.getAdditionalLibraries();
        for (URL url : additionalLibraries) {
            File file = new File(url.getPath());
            appInfo.libs.add(file.getAbsolutePath());
        }
        return appInfo;
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

    private static final Map<Class<? extends ServiceInfo>, DefaultService> defaultProviders = new HashMap();

    static {
        defaultProviders.put(MdbContainerInfo.class, new DefaultService("Default MDB Container", Container.class));
        defaultProviders.put(StatefulSessionContainerInfo.class, new DefaultService("Default Stateful Container", Container.class));
        defaultProviders.put(StatelessSessionContainerInfo.class, new DefaultService("Default Stateless Container", Container.class));
        defaultProviders.put(CmpEntityContainerInfo.class, new DefaultService("Default CMP Container", Container.class));
        defaultProviders.put(BmpEntityContainerInfo.class, new DefaultService("Default BMP Container", Container.class));
        defaultProviders.put(SecurityServiceInfo.class, new DefaultService("Default Security Service", SecurityService.class));
        defaultProviders.put(TransactionServiceInfo.class, new DefaultService("Default Transaction Manager", TransactionManager.class));
        defaultProviders.put(ConnectionManagerInfo.class, new DefaultService("Default Local TX ConnectionManager", ConnectionManager.class));
        defaultProviders.put(ProxyFactoryInfo.class, new DefaultService("Default JDK 1.3 ProxyFactory", ProxyFactory.class));
        defaultProviders.put(ConnectorInfo.class, new DefaultService("Default JDBC Database", Connector.class));
    }

    public <T extends ServiceInfo> T configureService(Class<? extends T> type) throws OpenEJBException {
        DefaultService defaultService = defaultProviders.get(type);

        Service service = null;
        try {
            service = (Service) defaultService.type.newInstance();
            service.setProvider(defaultService.id);
            service.setId(defaultService.id);
        } catch (Exception e) {
            throw new OpenEJBException("Cannot instantiate class " + defaultService.type.getName(), e);
        }

        return configureService(service, type);
    }


    private <T extends ServiceInfo>T configureService(Service service, Class<? extends T> type) throws OpenEJBException {
        if (service == null) {
            return configureService(type);
        }

        Properties declaredProperties = getDeclaredProperties(service);

        return configureService(type, service.getId(), declaredProperties, service.getProvider(), service.getClass().getSimpleName());
    }

    /**
     *
     * @param type required
     * @param serviceId required
     * @param declaredProperties optional
     * @param providerId optional
     * @param serviceType optional
     * @return
     * @throws OpenEJBException
     */
    public <T extends ServiceInfo>T configureService(Class<? extends T> type, String serviceId, Properties declaredProperties, String providerId, String serviceType) throws OpenEJBException {
        if (type == null) throw new NullPointerException("type");
        if (serviceId == null) throw new NullPointerException("serviceId");
        if (providerId ==  null){
            providerId = serviceId;
        }
        if (declaredProperties == null){
            declaredProperties = new Properties();
        }

        ServiceProvider provider = ServiceUtils.getServiceProvider(providerId);

        Properties props = getDefaultProperties(provider);


        props.putAll(declaredProperties);

        Properties serviceProperties = getSystemProperties(serviceId);

        props.putAll(serviceProperties);

        if (serviceType != null && !provider.getProviderType().equals(serviceType)) {
            throw new OpenEJBException(messages.format("conf.4902", serviceId, serviceType));
        }

        T info = null;

        try {
            info = type.newInstance();
        } catch (Exception e) {
            throw new OpenEJBException("Cannot instantiate class " + type.getName(), e);
        }

        info.serviceType = provider.getProviderType();
        info.description = provider.getDescription();
        info.displayName = provider.getDisplayName();
        info.className = provider.getClassName();
        info.id = serviceId;
        info.properties = props;
        info.constructorArgs.addAll(parseConstructorArgs(provider));

//        String serviceId = serviceType + ":" + info.id;
//        if (serviceIds.contains(serviceId)) {
//            handleException("conf.0105", configLocation, info.id, serviceType);
//        }

//        serviceIds.add(serviceId);

        return info;
    }

    private <T extends ServiceInfo>Properties getSystemProperties(String serviceId) {
        // Override with system properties
        Properties serviceProperties = new Properties();
        String prefix = serviceId + ".";
        Properties sysProps = new Properties(System.getProperties());
        sysProps.putAll(SystemInstance.get().getProperties());
        for (Iterator iterator1 = sysProps.entrySet().iterator(); iterator1.hasNext();) {
            Map.Entry entry1 = (Map.Entry) iterator1.next();
            String key = (String) entry1.getKey();
            String value = (String) entry1.getValue();
            if (key.startsWith(prefix)) {
                key = key.replaceFirst(prefix, "");
                serviceProperties.setProperty(key, value);
            }
        }
        return serviceProperties;
    }

    private <T extends ServiceInfo>Properties getDeclaredProperties(Service service) throws OpenEJBException {
        /* 3. Load properties from the content in the Container
        *    element of the configuration file.
        */
        Properties declaredProperties = new Properties();
        try {
            if (service.getContent() != null) {
                String content = service.getContent();
                StringBufferInputStream in = new StringBufferInputStream(content);

                try {
                    declaredProperties.load(in);
                } catch (IOException ex) {
                    throw new OpenEJBException(ServiceUtils.messages.format("conf.0012", ex.getLocalizedMessage()));
                }

            }
        } catch (OpenEJBException ex) {
            throw new OpenEJBException(ServiceUtils.messages.format("conf.0014", service.getClass().getSimpleName(), service.getId(), configLocation, ex.getLocalizedMessage()));
        }
        return declaredProperties;
    }

    private <T extends ServiceInfo>Properties getDefaultProperties(ServiceProvider provider) throws OpenEJBException {
        Properties props = new Properties();

        try {
            /*
            * 2. Load properties from the content in the service provider
            *    element of the service-jar.xml
            */

            if (provider.getContent() != null) {
                String content = provider.getContent();

                StringBufferInputStream in = new StringBufferInputStream(content);

                try {
                    props.load(in);
                } catch (IOException ex) {
                    throw new OpenEJBException(ServiceUtils.messages.format("conf.0012", ex.getLocalizedMessage()));
                }

            }
        } catch (OpenEJBException ex) {
            throw new OpenEJBException(ServiceUtils.messages.format("conf.0013", provider.getId(), null, ex.getLocalizedMessage()));
        }
        return props;
    }

    static Map<String, Class<? extends ContainerInfo>> containerTypes = new HashMap();

    static {
        containerTypes.put(Bean.STATELESS, StatelessSessionContainerInfo.class);
        containerTypes.put(Bean.STATEFUL, StatefulSessionContainerInfo.class);
        containerTypes.put(Bean.BMP_ENTITY, BmpEntityContainerInfo.class);
        containerTypes.put(Bean.CMP_ENTITY, CmpEntityContainerInfo.class);
        containerTypes.put(Bean.CMP2_ENTITY, CmpEntityContainerInfo.class);
        containerTypes.put(Bean.MESSAGE, MdbContainerInfo.class);
    }

    protected static Class<? extends ContainerInfo> getContainerInfoType(String ctype) {
        return containerTypes.get(ctype);
    }

    private List<String> parseConstructorArgs(ServiceProvider service) {
        String constructor = service.getConstructor();
        if (constructor == null) {
            return Collections.EMPTY_LIST;
        }
        return Arrays.asList(constructor.split("[ ,]+"));
    }


    protected List<String> getConnectorIds() {
        List<String> connectorIds = new ArrayList();

        OpenEjbConfiguration runningConfig = getRunningConfig();
        for (ConnectorInfo connectorInfo : runningConfig.facilities.connectors) {
            connectorIds.add(connectorInfo.id);
        }

        if (sys != null) {
            for (ConnectorInfo connectorInfo : sys.facilities.connectors) {
                connectorIds.add(connectorInfo.id);
            }

            // The only time we'd have one of these is if we were building
            // the above sys instance
            if (openejb != null) {
                for (Connector connector : openejb.getConnector()) {
                    connectorIds.add(connector.getId());
                }
            }
        }
        return connectorIds;
    }

    protected List<String> getContainerIds() {
        List<String> containerIds = new ArrayList();

        OpenEjbConfiguration runningConfig = getRunningConfig();
        for (ContainerInfo containerInfo : runningConfig.containerSystem.containers) {
            containerIds.add(containerInfo.id);
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
        List<ContainerInfo> containers = new ArrayList();

        OpenEjbConfiguration runningConfig = getRunningConfig();
        for (ContainerInfo containerInfo : runningConfig.containerSystem.containers) {
            containers.add(containerInfo);
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

}
