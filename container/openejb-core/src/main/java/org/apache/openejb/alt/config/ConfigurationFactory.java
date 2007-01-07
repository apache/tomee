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
import org.apache.openejb.alt.config.sys.Deployments;
import org.apache.openejb.alt.config.sys.JndiProvider;
import org.apache.openejb.alt.config.sys.Openejb;
import org.apache.openejb.alt.config.sys.ProxyFactory;
import org.apache.openejb.alt.config.sys.SecurityService;
import org.apache.openejb.alt.config.sys.ServiceProvider;
import org.apache.openejb.alt.config.sys.TransactionService;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.ConnectionManagerInfo;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.ContainerSystemInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.EntityContainerInfo;
import org.apache.openejb.assembler.classic.FacilitiesInfo;
import org.apache.openejb.assembler.classic.IntraVmServerInfo;
import org.apache.openejb.assembler.classic.JndiContextInfo;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.classic.ManagedConnectionFactoryInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.OpenEjbConfigurationFactory;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Collections;

public class ConfigurationFactory implements OpenEjbConfigurationFactory, ProviderDefaults {

    public static final Logger logger = Logger.getInstance("OpenEJB.startup", "org.apache.openejb.alt.config.rules");
    protected static final Messages messages = new Messages("org.apache.openejb.alt.config.rules");

    private String configLocation = "";

    private List<String> serviceIds = new ArrayList<String>();

    public static OpenEjbConfiguration sys;

    private final List<ContainerInfo> containers  = new ArrayList<ContainerInfo>();
    private final List<EntityContainerInfo> entityContainers = new ArrayList<EntityContainerInfo>();
    private final List<StatefulSessionContainerInfo> sfsbContainers = new ArrayList<StatefulSessionContainerInfo>();
    private final List<StatelessSessionContainerInfo> slsbContainers = new ArrayList<StatelessSessionContainerInfo>();
    private final List<MdbContainerInfo> mdbContainers = new ArrayList<MdbContainerInfo>();
    private Map<String,ContainerInfo> containerTable = new HashMap<String,ContainerInfo>();

    private Properties props;
    public EjbJarInfoBuilder ejbJarInfoBuilder = new EjbJarInfoBuilder();

    public void init(Properties props) throws OpenEJBException {
        this.props = props;

        configLocation = props.getProperty("openejb.conf.file");

        if (configLocation == null) {
            configLocation = props.getProperty("openejb.configuration");
        }

        configLocation = ConfigUtils.searchForConfiguration(configLocation, props);
        if (configLocation != null){
            this.props.setProperty("openejb.configuration", configLocation);
        }

    }

    public static void main(String[] args) {
        try {
            ConfigurationFactory conf = new ConfigurationFactory();
            conf.configLocation = args[0];
            conf.init(null);
            OpenEjbConfiguration openejb = conf.getOpenEjbConfiguration();

            ConfigurationPrinter.printConf(openejb);
        } catch (Exception e) {
            System.out.println("[OpenEJB] " + e.getMessage());
            e.printStackTrace();
        }
    }

    public OpenEjbConfiguration getOpenEjbConfiguration() throws OpenEJBException {

        Openejb openejb;
        DynamicDeployer deployer;
        if (configLocation != null) {
            openejb = ConfigUtils.readConfig(configLocation);
            deployer = new AutoDeployer(openejb);
        } else {
            openejb = new Openejb();
            deployer = new AutoConfigAndDeploy(openejb);
        }


        List<Deployments> deployments = new ArrayList<Deployments>(Arrays.asList(openejb.getDeployments()));

        //// getOption /////////////////////////////////  BEGIN  ////////////////////
        String flag = props.getProperty("openejb.deployments.classpath", "true").toLowerCase();
        boolean searchClassPath = flag.equals("true");
        //// getOption /////////////////////////////////  END  ////////////////////

        if (searchClassPath) {
            Deployments deployment = new Deployments();
            deployment.setClasspath(this.getClass().getClassLoader());
            deployments.add(deployment);
        }

        List<DeploymentModule> deployedJars = new DeploymentLoader().loadDeploymentsList(deployments, deployer);

        DeploymentModule[] jars = deployedJars.toArray(new DeploymentModule[]{});

        sys = new OpenEjbConfiguration();
        sys.containerSystem = new ContainerSystemInfo();
        sys.facilities = new FacilitiesInfo();

        initJndiProviders(openejb, sys.facilities);
        initTransactionService(openejb, sys.facilities);
        initConnectors(openejb, sys.facilities);
        initConnectionManagers(openejb, sys.facilities);
        initProxyFactory(openejb, sys.facilities);

        initContainerInfos(openejb);

        sys.containerSystem.containers.addAll(containers);
        sys.containerSystem.entityContainers.addAll(entityContainers);
        sys.containerSystem.statefulContainers.addAll(sfsbContainers);
        sys.containerSystem.statelessContainers.addAll(slsbContainers);
        sys.containerSystem.mdbContainers.addAll(mdbContainers);

        List<AppInfo> appInfos = new ArrayList<AppInfo>();
        {
            AppInfo appInfo = new AppInfo();
            for (DeploymentModule jar : jars) {
                if (!(jar instanceof EjbModule)) {
                    continue;
                }
                EjbModule ejbModule = (EjbModule) jar;
                try {
                    EjbJarInfo ejbJarInfo = ejbJarInfoBuilder.buildInfo(ejbModule);
                    if (ejbJarInfo == null) {
                        continue;
                    }
                    assignBeansToContainers(ejbJarInfo.enterpriseBeans, ejbModule.getOpenejbJar().getDeploymentsByEjbName());
                    appInfo.ejbJars.add(ejbJarInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                    ConfigUtils.logger.i18n.warning("conf.0004", ejbModule.getJarURI(), e.getMessage());
                }
            }

            appInfos.add(appInfo);
        }

        for (DeploymentModule module : jars) {
            if (!(module instanceof AppModule)) {
                continue;
            }
            AppModule appModule = (AppModule) module;

            AppInfo appInfo = new AppInfo();
            for (EjbModule ejbModule : appModule.getEjbModules()) {
                try {
                    EjbJarInfo ejbJarInfo = ejbJarInfoBuilder.buildInfo(ejbModule);
                    if (ejbJarInfo == null) {
                        continue;
                    }
                    assignBeansToContainers(ejbJarInfo.enterpriseBeans, ejbModule.getOpenejbJar().getDeploymentsByEjbName());
                    appInfo.ejbJars.add(ejbJarInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                    ConfigUtils.logger.i18n.warning("conf.0004", ejbModule.getJarURI(), e.getMessage());
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
                JndiEncInfo jndiEncInfo = jndiEncInfoBuilder.build(applicationClient, clientModule.getJarLocation());
                clientInfo.jndiEnc = jndiEncInfo;
                appInfo.clients.add(clientInfo);
            }

            appInfo.jarPath = appModule.getJarLocation();
            List<URL> additionalLibraries = appModule.getAdditionalLibraries();
            for (URL url : additionalLibraries) {
                File file = new File(url.getPath());
                appInfo.libs.add(file.getAbsolutePath());
            }
            appInfos.add(appInfo);
        }

        sys.containerSystem.applications.addAll(appInfos);

//        SecurityRoleInfo defaultRole = new SecurityRoleInfo();
//        defaultRole.description = "The role applied to recurity references that are not linked.";
//        defaultRole.roleName = EjbJarInfoBuilder.DEFAULT_SECURITY_ROLE;
//        ejbJarInfoBuilder.getSecurityRoleInfos().add(defaultRole);

        initSecutityService(openejb, sys.facilities);

        SystemInstance.get().setComponent(OpenEjbConfiguration.class, sys);
        return sys;
    }

    private static String getClientModuleId(ClientModule clientModule) {
        String jarLocation = clientModule.getJarLocation();
        File file = new File(jarLocation);
        String name = file.getName();
        if (name.endsWith(".jar") || name.endsWith(".zip")){
            name = name.replaceFirst("....$","");
        }
        return name;
    }


    private void initJndiProviders(Openejb openejb, FacilitiesInfo facilities)
            throws OpenEJBException {
        JndiProvider[] provider = openejb.getJndiProvider();

        if (provider == null || provider.length < 1) return;

        for (int i = 0; i < provider.length; i++) {
            JndiContextInfo info = createService(provider[i], new JndiContextInfo(), "Default Jndi Provider", JndiProvider.class);

            facilities.remoteJndiContexts.add(info);
        }
    }

    private void initSecutityService(Openejb openejb, FacilitiesInfo facilities) throws OpenEJBException {

        facilities.securityService = createService(openejb.getSecurityService(), new SecurityServiceInfo(), DEFAULT_SECURITY_SERVICE, SecurityService.class);
    }

    private void initTransactionService(Openejb openejb, FacilitiesInfo facilities) throws OpenEJBException {

        facilities.transactionService = createService(openejb.getTransactionService(), new TransactionServiceInfo(), DEFAULT_TRANSACTION_MANAGER, TransactionService.class);

    }

    private void initConnectors(Openejb openejb, FacilitiesInfo facilities)
            throws OpenEJBException {

        Connector[] conn = openejb.getConnector();

        if (conn == null || conn.length < 1) return;

        for (int i = 0; i < conn.length; i++) {

            ManagedConnectionFactoryInfo info = createService(conn[i], new ManagedConnectionFactoryInfo(), DEFAULT_JDBC_DATABASE, Connector.class);

            ConnectorInfo connectorInfo = new ConnectorInfo();
            connectorInfo.connectorId = info.id;
            // TODO: This should not be hardcoded
            connectorInfo.connectionManagerId = DEFAULT_LOCAL_TX_CON_MANAGER;
            connectorInfo.managedConnectionFactory = (ManagedConnectionFactoryInfo) info;

            facilities.connectors.add(connectorInfo);
        }
    }

    private void initConnectionManagers(Openejb openejb, FacilitiesInfo facilities)
            throws OpenEJBException {

        ConnectionManagerInfo service = createService(openejb.getConnectionManager(), new ConnectionManagerInfo(), DEFAULT_LOCAL_TX_CON_MANAGER, ConnectionManager.class);

        facilities.connectionManagers.add(service);
    }

    private void initProxyFactory(Openejb openejb, FacilitiesInfo facilities) throws OpenEJBException {
        String defaultId = null;
        try {
            String version = System.getProperty("java.vm.version");
            if (version.startsWith("1.1") || version.startsWith("1.2")) {
                defaultId = "Default JDK 1.2 ProxyFactory";
            } else {
                defaultId = "Default JDK 1.3 ProxyFactory";
            }
        } catch (Exception e) {

            throw new RuntimeException("Unable to determine the version of your VM.  No ProxyFactory Can be installed");
        }

        facilities.intraVmServer = createService(openejb.getProxyFactory(), new IntraVmServerInfo(), defaultId, ProxyFactory.class);

    }

    private <T extends ServiceInfo> T createService(Service service, T info, String defaultId, Class type) throws OpenEJBException {
        service = (Service) initService(service, defaultId, type);
        ServiceProvider provider = ServiceUtils.getServiceProvider(service);

        String serviceType = type.getSimpleName();
        checkType(provider, service, serviceType);

        info.serviceType = type.getSimpleName();
        info.codebase = service.getJar();
        info.description = provider.getDescription();
        info.displayName = provider.getDisplayName();
        info.className = provider.getClassName();
        info.id = service.getId();
        info.properties = ServiceUtils.assemblePropertiesFor(serviceType, service.getId(), service.getContent(), configLocation, provider);
        info.constructorArgs.addAll(parseConstructorArgs(provider));

        String serviceId = serviceType + ":" + info.id;
        if (serviceIds.contains(serviceId)) {
            handleException("conf.0105", configLocation, info.id, serviceType);
        }

        serviceIds.add(serviceId);

        return info;
    }

    private void initContainerInfos(Openejb conf) throws OpenEJBException {

        Container[] containers = conf.getContainer();

        for (Container declaration : containers) {
            ServiceInfo info;
            String defaultId;
            if (declaration.getCtype().equals("STATELESS")) {
                defaultId = DEFAULT_STATELESS_CONTAINER;
                info = new StatelessSessionContainerInfo();
                slsbContainers.add((StatelessSessionContainerInfo) info);
            } else if (declaration.getCtype().equals("STATEFUL")) {
                defaultId = DEFAULT_STATEFUL_CONTAINER;
                info = new StatefulSessionContainerInfo();
                sfsbContainers.add((StatefulSessionContainerInfo) info);
            } else if (declaration.getCtype().equals("BMP_ENTITY")) {
                defaultId = DEFAULT_BMP_CONTAINER;
                info = new EntityContainerInfo();
                entityContainers.add((EntityContainerInfo) info);
            } else if (declaration.getCtype().equals("CMP_ENTITY")) {
                defaultId = DEFAULT_CMP_CONTAINER;
                info = new EntityContainerInfo();
                entityContainers.add((EntityContainerInfo) info);
            } else if (declaration.getCtype().equals("CMP2_ENTITY")) {
                defaultId = DEFAULT_CMP2_CONTAINER;
                info = new EntityContainerInfo();
                entityContainers.add((EntityContainerInfo) info);
            } else if (declaration.getCtype().equals("MESSAGE")) {
                defaultId = DEFAULT_MDB_CONTAINER;
                info = new MdbContainerInfo();
                mdbContainers.add((MdbContainerInfo) info);
            } else {
                throw new OpenEJBException("Unrecognized contianer type " + declaration.getCtype());
            }

            createService(declaration, info, defaultId, Container.class);
        }

        this.containers.addAll(sfsbContainers);
        this.containers.addAll(slsbContainers);
        this.containers.addAll(entityContainers);
        this.containers.addAll(mdbContainers);

        for (ContainerInfo containerInfo : this.containers) {
            containerTable.put(containerInfo.id, containerInfo);
        }

    }

    private List<String> parseConstructorArgs(ServiceProvider service) {
        String constructor = service.getConstructor();
        if (constructor == null) {
            return Collections.EMPTY_LIST;
        }
        return Arrays.asList(constructor.split("[ ,]+"));
    }

    private void assignBeansToContainers(List<EnterpriseBeanInfo> beans, Map ejbds)
            throws OpenEJBException {

        for (EnterpriseBeanInfo bean : beans) {
            EjbDeployment d = (EjbDeployment) ejbds.get(bean.ejbName);

            ContainerInfo cInfo = containerTable.get(d.getContainerId());
            if (cInfo == null) {

                String msg = messages.format("config.noContainerFound", d.getContainerId(), d.getEjbName());

                logger.fatal(msg);
                throw new OpenEJBException(msg);
            }
            cInfo.ejbeans.add(bean);
        }
    }

    public Service initService(Service service, String defaultName, Class type) throws OpenEJBException {

        if (service == null) {
            try {
                service = (Service) type.newInstance();
                service.setProvider(defaultName);
                service.setId(defaultName);
            } catch (Exception e) {
                throw new OpenEJBException("Cannot instantiate class " + type);
            }
        } else if (service.getProvider() == null) {

            try {
                ServiceUtils.getServiceProvider(service.getId());
                service.setProvider(service.getId());
            } catch (Exception e) {

                service.setProvider(defaultName);
            }
        }

        return service;
    }

    private void checkType(ServiceProvider provider, Service service, String type)
            throws OpenEJBException {
        if (!provider.getProviderType().equals(type)) {
            handleException("conf.4902", service, type);
        }
    }

    public static void handleException(String errorCode, Object... args) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, args));
    }
}

